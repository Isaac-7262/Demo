package org.kku.service;

import org.kku.model.Incident;
import org.kku.repo.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service layer สำหรับจัดการเหตุการณ์
 */
@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    /**
     * บันทึกเหตุการณ์ใหม่
     */
    public Incident createIncident(Incident incident) {
        // Validate incident data
        if (incident.getLatitude() < -90 || incident.getLatitude() > 90) {
            throw new IllegalArgumentException("ละติจูดไม่ถูกต้อง");
        }
        if (incident.getLongitude() < -180 || incident.getLongitude() > 180) {
            throw new IllegalArgumentException("ลองจิจูดไม่ถูกต้อง");
        }
        if (incident.getDescription() == null || incident.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("รายละเอียดเหตุการณ์ไม่สามารถเว้นได้");
        }

        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        
        return incidentRepository.save(incident);
    }

    /**
     * ดึงเหตุการณ์ทั้งหมด
     */
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    /**
     * ดึงเหตุการณ์ที่ยังดำเนินการอยู่
     */
    public List<Incident> getActiveIncidents() {
        return incidentRepository.findActiveIncidents();
    }

    /**
     * ดึงเหตุการณ์ตาม ID
     */
    public Optional<Incident> getIncidentById(Long id) {
        return incidentRepository.findById(id);
    }

    /**
     * ดึงเหตุการณ์ตามประเภท
     */
    public List<Incident> getIncidentsByType(String type) {
        return incidentRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    /**
     * ดึงเหตุการณ์ตามสถานะ
     */
    public List<Incident> getIncidentsByStatus(String status) {
        return incidentRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * อัปเดตสถานะเหตุการณ์
     */
    public Incident updateIncidentStatus(Long id, String newStatus, String notes) {
        Optional<Incident> optionalIncident = incidentRepository.findById(id);
        
        if (optionalIncident.isEmpty()) {
            throw new IllegalArgumentException("ไม่พบเหตุการณ์ที่ต้องการอัปเดต");
        }

        Incident incident = optionalIncident.get();
        incident.setStatus(newStatus);
        
        if (notes != null && !notes.trim().isEmpty()) {
            incident.setOfficerNotes(notes);
        }
        
        incident.setUpdatedAt(LocalDateTime.now());
        
        return incidentRepository.save(incident);
    }

    /**
     * ลบเหตุการณ์
     */
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new IllegalArgumentException("ไม่พบเหตุการณ์ที่ต้องการลบ");
        }
        incidentRepository.deleteById(id);
    }

    /**
     * คำนวณเวลาที่ผ่านไป (Time Ago)
     */
    public String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = ChronoUnit.MINUTES.between(dateTime, now);
        long hoursAgo = ChronoUnit.HOURS.between(dateTime, now);
        long daysAgo = ChronoUnit.DAYS.between(dateTime, now);

        if (minutesAgo < 1) {
            return "เพิ่งแจ้ง";
        } else if (minutesAgo < 60) {
            return minutesAgo + " นาทีที่แล้ว";
        } else if (hoursAgo < 24) {
            return hoursAgo + " ชั่วโมงที่แล้ว";
        } else {
            return daysAgo + " วันที่แล้ว";
        }
    }

    /**
     * คำนวณระยะเวลาในการตอบสนอง (Response Time)
     */
    public String getResponseTime(Incident incident) {
        if (incident.getStatus().equals("รอการดำเนินการ")) {
            // ยังไม่ได้ดำเนินการ
            return getTimeAgo(incident.getCreatedAt());
        } else {
            // คำนวณเวลาจากรายงานไปถึงการดำเนินการ
            long secondsElapsed = ChronoUnit.SECONDS.between(incident.getCreatedAt(), incident.getUpdatedAt());
            
            if (secondsElapsed < 60) {
                return secondsElapsed + " วินาที";
            } else if (secondsElapsed < 3600) {
                long minutes = secondsElapsed / 60;
                return minutes + " นาที";
            } else {
                long hours = secondsElapsed / 3600;
                return hours + " ชั่วโมง";
            }
        }
    }

    /**
     * ตรวจสอบว่าเหตุการณ์เป็นเหตุด่วน
     */
    public boolean isUrgent(Incident incident) {
        // เหตุการณ์ถูกมองว่าเป็นเหตุด่วนถ้า:
        // 1. สถานะเป็น "กำลังดำเนินการ" หรือ "รอการดำเนินการ"
        // 2. และค่อนข้างเก่า (เกิน 1 ชั่วโมง)
        
        if (!incident.getStatus().equals("ดำเนินการแล้ว")) {
            long hoursElapsed = ChronoUnit.HOURS.between(incident.getCreatedAt(), LocalDateTime.now());
            return hoursElapsed >= 1;
        }
        return false;
    }

    /**
     * ดึงสถิติเหตุการณ์
     */
    public IncidentStats getIncidentStats() {
        List<Incident> allIncidents = getAllIncidents();
        List<Incident> activeIncidents = getActiveIncidents();
        
        IncidentStats stats = new IncidentStats();
        stats.setTotalIncidents(allIncidents.size());
        stats.setActiveIncidents(activeIncidents.size());
        stats.setPendingIncidents((int) allIncidents.stream()
            .filter(i -> i.getStatus().equals("รอการดำเนินการ"))
            .count());
        stats.setProgressIncidents((int) allIncidents.stream()
            .filter(i -> i.getStatus().equals("กำลังดำเนินการ"))
            .count());
        stats.setResolvedIncidents((int) allIncidents.stream()
            .filter(i -> i.getStatus().equals("ดำเนินการแล้ว"))
            .count());

        return stats;
    }

    /**
     * ข้อมูลสถิติเหตุการณ์
     */
    public static class IncidentStats {
        private int totalIncidents;
        private int activeIncidents;
        private int pendingIncidents;
        private int progressIncidents;
        private int resolvedIncidents;

        // Getters and Setters
        public int getTotalIncidents() { return totalIncidents; }
        public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }

        public int getActiveIncidents() { return activeIncidents; }
        public void setActiveIncidents(int activeIncidents) { this.activeIncidents = activeIncidents; }

        public int getPendingIncidents() { return pendingIncidents; }
        public void setPendingIncidents(int pendingIncidents) { this.pendingIncidents = pendingIncidents; }

        public int getProgressIncidents() { return progressIncidents; }
        public void setProgressIncidents(int progressIncidents) { this.progressIncidents = progressIncidents; }

        public int getResolvedIncidents() { return resolvedIncidents; }
        public void setResolvedIncidents(int resolvedIncidents) { this.resolvedIncidents = resolvedIncidents; }
    }
}
