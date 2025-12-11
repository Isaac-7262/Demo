package org.kku.web;

import org.kku.model.Incident;
import org.kku.repo.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.kku.service.IncidentEventsService;
import org.kku.model.Message;
import org.kku.repo.MessageRepository;

@Controller
public class IncidentController {

    @Autowired
    private IncidentRepository incidentRepository;

    @Value("${kku.map.lat:16.473}")
    private double defaultLat;

    @Value("${kku.map.lng:102.823}")
    private double defaultLng;

    @Value("${kku.map.zoom:15}")
    private int defaultZoom;

    @Autowired
    private IncidentEventsService eventsService;
    @Autowired
    private MessageRepository messageRepository;

    @Value("${uploads.dir:uploads}")
    private String uploadDir;

    /**
     * หน้าแรก - แสดงแผนที่และเหตุการณ์ทั้งหมด
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mapLat", defaultLat);
        model.addAttribute("mapLng", defaultLng);
        model.addAttribute("mapZoom", defaultZoom);
        
        // ส่งสถิติพื้นฐานไปยังหน้า
        List<Incident> activeIncidents = incidentRepository.findActiveIncidents();
        model.addAttribute("activeIncidentsCount", activeIncidents.size());
        
        return "index";
    }

    /**
     * หน้าแดชบอร์ดสำหรับเจ้าหน้าที่
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("mapLat", defaultLat);
        model.addAttribute("mapLng", defaultLng);
        model.addAttribute("mapZoom", defaultZoom);
        
        // ส่งข้อมูลสถิติ
        List<Incident> allIncidents = incidentRepository.findAll();
        List<Incident> activeIncidents = incidentRepository.findActiveIncidents();
        
        model.addAttribute("totalIncidents", allIncidents.size());
        model.addAttribute("activeIncidents", activeIncidents);
        model.addAttribute("activeIncidentsCount", activeIncidents.size());
        
        return "dashboard";
    }

    // ==== Chat endpoints (simple) ====
    @GetMapping("/api/incidents/{id}/messages")
    @ResponseBody
    public ResponseEntity<?> getMessages(
            @PathVariable Long id,
            @RequestParam(name = "token", required = false) String token,
            @RequestParam(name = "dashboard", required = false, defaultValue = "false") boolean fromDashboard
    ) {
        Optional<Incident> opt = incidentRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        // ตรวจสอบสิทธิ์: เจ้าหน้าที่ต้องเรียกจากหน้า dashboard เท่านั้น
        boolean isOfficer = false;
        if (fromDashboard) {
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                // ต้องไม่ใช่ anonymous และต้องมี role OFFICER หรือ ADMIN
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    isOfficer = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"));
                }
            } catch (Exception ignored) {}
        }

        // ตรวจสอบว่าเป็นเจ้าของเหตุการณ์ (มี token ถูกต้อง)
        String editToken = opt.get().getEditToken();
        boolean isOwner = (token != null && !token.isEmpty() && editToken != null && token.equals(editToken));

        if (!isOfficer && !isOwner) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", "ไม่ได้รับอนุญาตให้ดูข้อความ");
            return ResponseEntity.status(403).body(res);
        }

        return ResponseEntity.ok(messageRepository.findByIncidentIdOrderByCreatedAtAsc(id));
    }

    @PostMapping("/api/incidents/{id}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> postMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @RequestParam(name = "token", required = false) String token,
            @RequestParam(name = "dashboard", required = false, defaultValue = "false") boolean fromDashboard
    ) {
        Optional<Incident> opt = incidentRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        // ตรวจสิทธิ์ผู้ส่ง - เจ้าหน้าที่ต้องเรียกจากหน้า dashboard
        String author = "REPORTER";
        boolean isOfficer = false;
        if (fromDashboard) {
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                // ต้องไม่ใช่ anonymous และต้องมี role OFFICER หรือ ADMIN
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    isOfficer = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"));
                }
            } catch (Exception ignored) {}
        }
        
        if (isOfficer) {
            author = "OFFICER";
        } else {
            // ถ้าไม่ใช่เจ้าหน้าที่ ต้องมี token ที่ถูกต้อง
            String editToken = opt.get().getEditToken();
            boolean isOwner = (token != null && !token.isEmpty() && editToken != null && token.equals(editToken));
            if (!isOwner) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", false);
                res.put("message", "ไม่ได้รับอนุญาต");
                return ResponseEntity.status(403).body(res);
            }
        }

        String content = payload.getOrDefault("content", "").trim();
        if (content.isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", "ข้อความว่าง");
            return ResponseEntity.badRequest().body(res);
        }

        Message msg = new Message(id, author, content);
        messageRepository.save(msg);
        eventsService.sendEvent("message-created", msg);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", msg);
        return ResponseEntity.ok(res);
    }

    // === REST API Endpoints ===

    /**
     * ดึงเหตุการณ์ทั้งหมดในรูปแบบ JSON สำหรับแสดงบนแผนที่
     */
    @GetMapping("/api/incidents")
    @ResponseBody
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    /**
     * ดึงเหตุการณ์ที่ยังคงดำเนินการอยู่
     */
    @GetMapping("/api/incidents/active")
    @ResponseBody
    public List<Incident> getActiveIncidents() {
        return incidentRepository.findActiveIncidents();
    }

    /**
     * ดึงเหตุการณ์ตามประเภท
     */
    @GetMapping("/api/incidents/type/{type}")
    @ResponseBody
    public List<Incident> getIncidentsByType(@PathVariable String type) {
        return incidentRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    /**
     * ดึงข้อมูลเหตุการณ์ตาม ID
     */
    @GetMapping("/api/incidents/{id}")
    @ResponseBody
    public ResponseEntity<Incident> getIncidentById(@PathVariable Long id) {
        Optional<Incident> incident = incidentRepository.findById(id);
        return incident.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * สร้างเหตุการณ์ใหม่
     */
    @PostMapping("/api/incidents")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createIncident(@RequestBody Map<String, Object> incidentData) {
        try {
            Incident incident = new Incident();
            incident.setType((String) incidentData.get("type"));
            incident.setDescription((String) incidentData.get("description"));
            incident.setLatitude(Double.parseDouble(incidentData.get("latitude").toString()));
            incident.setLongitude(Double.parseDouble(incidentData.get("longitude").toString()));
            incident.setReporter((String) incidentData.get("reporter"));
            incident.setReporterContact((String) incidentData.get("reporterContact"));
            
            // ตั้งค่า severity level ถ้ามี
            if (incidentData.containsKey("severityLevel")) {
                incident.setSeverityLevel((String) incidentData.get("severityLevel"));
            }

            incident.setCreatedAt(LocalDateTime.now());
            incident.setUpdatedAt(LocalDateTime.now());
            incident.setEditToken(UUID.randomUUID().toString().replace("-", ""));
            
            Incident savedIncident = incidentRepository.save(incident);
            eventsService.incidentCreated(savedIncident);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "รายงานเหตุการณ์สำเร็จ");
            response.put("incident", savedIncident);
            response.put("editToken", savedIncident.getEditToken());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาดในการรายงานเหตุการณ์: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * ส่งแจ้งเหตุแบบ form-data พร้อมแนบรูปภาพ (เหมาะกับมือถือ)
     */
    @PostMapping(value = "/api/incidents/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
        public ResponseEntity<Map<String, Object>> submitIncident(
            @RequestParam String type,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String reporter,
            @RequestParam(required = false) String reporterContact,
            @RequestParam(required = false, name = "severityLevel") String severityLevel,
            @RequestParam(required = false, name = "image") MultipartFile image
        ) {
        try {
            Incident incident = new Incident();
            incident.setType(type);
            incident.setLatitude(latitude);
            incident.setLongitude(longitude);
            incident.setDescription(description);
            incident.setReporter(reporter);
            incident.setReporterContact(reporterContact);
            if (severityLevel != null && !severityLevel.isBlank()) incident.setSeverityLevel(severityLevel);
            incident.setCreatedAt(LocalDateTime.now());
            incident.setUpdatedAt(LocalDateTime.now());
            incident.setEditToken(UUID.randomUUID().toString().replace("-", ""));

            // บันทึกรูปภาพถ้ามี
            if (image != null && !image.isEmpty()) {
                java.nio.file.Path dir = java.nio.file.Paths.get(uploadDir).toAbsolutePath();
                java.nio.file.Files.createDirectories(dir);
                String ext = Optional.ofNullable(image.getOriginalFilename())
                        .filter(fn -> fn.contains("."))
                        .map(fn -> fn.substring(fn.lastIndexOf('.')))
                        .orElse(".jpg");
                String filename = "incident_" + System.currentTimeMillis() + ext;
                java.nio.file.Path target = dir.resolve(filename);
                image.transferTo(target.toFile());
                incident.setImageUrl("/uploads/" + filename);
            }

            Incident savedIncident = incidentRepository.save(incident);
            eventsService.incidentCreated(savedIncident);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ส่งแจ้งเหตุสำเร็จ");
            response.put("incident", savedIncident);
            response.put("editToken", savedIncident.getEditToken());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาด: " + ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * อัปเดตสถานะเหตุการณ์
     */
    @PutMapping("/api/incidents/{id}/status")
    @ResponseBody
        public ResponseEntity<Map<String, Object>> updateIncidentStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> statusUpdate,
            @RequestParam(name = "token", required = false) String token) {
        
        Optional<Incident> optionalIncident = incidentRepository.findById(id);
        
        if (optionalIncident.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "ไม่พบเหตุการณ์ที่ต้องการอัปเดต");
            return ResponseEntity.notFound().build();
        }

        Incident incident = optionalIncident.get();
        String newStatus = statusUpdate.get("status");
        String notes = statusUpdate.get("notes");
        // ตรวจสิทธิ์: ถ้าไม่ได้ล็อกอินเป็น OFFICER จะต้องมี token ตรงกับ incident
        boolean authorized = true;
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            authorized = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("OFFICER"));
        } catch (Exception ignored) {}
        if (!authorized) {
            if (token == null || token.isBlank() || !token.equals(optionalIncident.get().getEditToken())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "ไม่ได้รับอนุญาตให้อัปเดตสถานะ");
                return ResponseEntity.status(403).body(response);
            }
        }

        incident.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            incident.setOfficerNotes(notes);
        }
        incident.setUpdatedAt(LocalDateTime.now());
        
        incidentRepository.save(incident);
        eventsService.incidentUpdated(incident);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "อัปเดตสถานะเหตุการณ์สำเร็จ");
        response.put("incident", incident);
        
        return ResponseEntity.ok(response);
    }

    /**
     * ลบเหตุการณ์ (สำหรับเจ้าหน้าที่)
     */
    @DeleteMapping("/api/incidents/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteIncident(@PathVariable Long id) {
        Optional<Incident> optionalIncident = incidentRepository.findById(id);
        
        if (optionalIncident.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "ไม่พบเหตุการณ์ที่ต้องการลบ");
            return ResponseEntity.notFound().build();
        }

        incidentRepository.deleteById(id);
        eventsService.incidentDeleted(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "ลบเหตุการณ์สำเร็จ");
        
        return ResponseEntity.ok(response);
    }

    /**
     * SSE stream สำหรับอัปเดตแบบเรียลไทม์
     */
    @GetMapping(path = "/api/incidents/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return eventsService.subscribe();
    }

    /**
     * ดึงสถิติเหตุการณ์
     */
    @GetMapping("/api/incidents/stats")
    @ResponseBody
    public Map<String, Object> getIncidentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // นับตามสถานะ
        List<Object[]> statusCounts = incidentRepository.countIncidentsByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] result : statusCounts) {
            statusStats.put((String) result[0], (Long) result[1]);
        }
        
        // นับตามประเภท
        List<Object[]> typeCounts = incidentRepository.countIncidentsByType();
        Map<String, Long> typeStats = new HashMap<>();
        for (Object[] result : typeCounts) {
            String type = (String) result[0];
            String typeDisplay = getTypeDisplayName(type);
            typeStats.put(typeDisplay, (Long) result[1]);
        }
        
        stats.put("statusStats", statusStats);
        stats.put("typeStats", typeStats);
        stats.put("totalIncidents", incidentRepository.count());
        stats.put("activeIncidents", incidentRepository.findActiveIncidents().size());
        
        return stats;
    }

    /**
     * Helper method สำหรับแปลงชื่อประเภทเหตุการณ์
     */
    private String getTypeDisplayName(String type) {
        switch (type) {
            case "accident": return "อุบัติเหตุ";
            case "medical": return "ผู้ป่วยฉุกเฉิน";
            case "conflict": return "ทะเลาะวิวาท";
            case "fire": return "เหตุไฟไหม้";
            case "help": return "ขอความช่วยเหลือ";
            default: return "อื่นๆ";
        }
    }

    // === เก่า - ข้างล่างนี้สำหรับความเข้ากันได้ ===
    
    @PostMapping("/incidents")
    @ResponseBody
    public Incident create(@RequestBody Incident incident) {
        return incidentRepository.save(incident);
    }

    @GetMapping("/incidents")
    @ResponseBody
    public List<Incident> all() {
        return incidentRepository.findAll();
    }

    @PostMapping("/incidents/{id}/status")
    @ResponseBody
    public Incident updateStatus(@PathVariable Long id, @RequestParam String status) {
        Incident incident = incidentRepository.findById(id).orElseThrow();
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }
}
