package org.kku.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.kku.repo.IncidentRepository;
import org.kku.model.Incident;

@RestController
public class AiController {

    private final IncidentRepository incidentRepository;

    public AiController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**
     * Simple AI-like classifier (rule-based placeholder).
     * Input: { "text": "..." }
     * Output: { type, severity, confidence }
     */
    @PostMapping("/api/ai/classify")
    public ResponseEntity<Map<String, Object>> classify(@RequestBody Map<String, String> payload) {
        String text = (payload != null) ? payload.getOrDefault("text", "").toLowerCase() : "";
        String type = "help";
        String severity = "medium";
        double confidence = 0.6;

        if (text.contains("ไฟ") || text.contains("ควัน") || text.contains("ไหม้")) {
            type = "fire"; severity = "high"; confidence = 0.85;
        } else if (text.contains("รถชน") || text.contains("อุบัติเหตุ") || text.contains("บาดเจ็บ")) {
            type = "accident"; severity = text.contains("บาดเจ็บ") ? "high" : "medium"; confidence = 0.8;
        } else if (text.contains("ทะเลาะ") || text.contains("ทำร้าย") || text.contains("ความขัดแย้ง")) {
            type = "conflict"; severity = "medium"; confidence = 0.75;
        } else if (text.contains("ป่วย") || text.contains("หมดสติ") || text.contains("ช่วยชีวิต")) {
            type = "medical"; severity = "high"; confidence = 0.85;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("type", type);
        res.put("severity", severity);
        res.put("confidence", confidence);
        return ResponseEntity.ok(res);
    }

    /**
     * Hotspot analysis: group incidents into small geo-cells and rank.
     * Input (optional): { "minLat":..., "maxLat":..., "minLng":..., "maxLng":..., "sinceDays": 30 }
     * Output: { hotspots: [ { centerLat, centerLng, count, topTypes, recommendation } ] }
     */
    @PostMapping("/api/ai/hotspots")
    public ResponseEntity<Map<String, Object>> hotspots(@RequestBody(required = false) Map<String, Object> payload) {
        double minLat = parseDouble(payload, "minLat", 16.460);
        double maxLat = parseDouble(payload, "maxLat", 16.485);
        double minLng = parseDouble(payload, "minLng", 102.810);
        double maxLng = parseDouble(payload, "maxLng", 102.835);
        int sinceDays = parseInt(payload, "sinceDays", 30);
        double cellSize = parseDouble(payload, "cellSize", 0.001); // ~100m default
        int minCount = parseInt(payload, "minCount", 3); // minimum incidents to consider a hotspot

        List<Incident> incidents = incidentRepository.findIncidentsInBounds(minLat, maxLat, minLng, maxLng);
        // filter by date window
        var since = java.time.LocalDateTime.now().minusDays(sinceDays);
        incidents = incidents.stream().filter(i -> i.getCreatedAt().isAfter(since)).collect(Collectors.toList());

        Map<String, List<Incident>> cells = new HashMap<>();
        for (Incident i : incidents) {
            String key = cellKey(i.getLatitude(), i.getLongitude(), cellSize);
            cells.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        List<Map<String, Object>> hotspots = new ArrayList<>();
        for (Map.Entry<String, List<Incident>> entry : cells.entrySet()) {
            List<Incident> group = entry.getValue();
            int count = group.size();
            if (count < minCount) continue; // only consider meaningful clusters

            double centerLat = group.stream().mapToDouble(Incident::getLatitude).average().orElse(0);
            double centerLng = group.stream().mapToDouble(Incident::getLongitude).average().orElse(0);

            Map<String, Long> typeCounts = group.stream().collect(Collectors.groupingBy(Incident::getType, Collectors.counting()));
            List<String> topTypes = typeCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            String recommendation = buildRecommendation(count, topTypes);

            Map<String, Object> cell = new HashMap<>();
            cell.put("centerLat", centerLat);
            cell.put("centerLng", centerLng);
            cell.put("count", count);
            cell.put("topTypes", topTypes);
            cell.put("recommendation", recommendation);
            cell.put("heatWeight", computeHeatWeight(count));
            cell.put("timePeaks", computeTimePeaks(group));
            hotspots.add(cell);
        }

        hotspots.sort(Comparator.comparingInt(h -> - (int) h.get("count")));
        Map<String, Object> result = new HashMap<>();
        result.put("hotspots", hotspots);
        result.put("totalIncidents", incidents.size());
        result.put("params", Map.of(
            "cellSize", cellSize,
            "minCount", minCount,
            "sinceDays", sinceDays
        ));
        return ResponseEntity.ok(result);
    }

    private String cellKey(double lat, double lng, double cellSize) {
        long latIndex = Math.round(lat / cellSize);
        long lngIndex = Math.round(lng / cellSize);
        return latIndex + ":" + lngIndex;
    }

    private String buildRecommendation(int count, List<String> topTypes) {
        String dominant = topTypes.isEmpty() ? "misc" : topTypes.get(0);
        if (count >= 10) {
            return "ความถี่สูงมากในพื้นที่ — พิจารณาเพิ่มกำลังเจ้าหน้าที่และตั้งจุดเฝ้าระวัง";
        }
        if (count >= 5) {
            switch (dominant) {
                case "fire":
                    return "พบเหตุไฟบ่อย — เตรียมอุปกรณ์ดับเพลิงและตรวจสอบความเสี่ยงเชื้อเพลิง";
                case "medical":
                    return "เหตุการแพทย์บ่อย — ประสานหน่วยแพทย์และเตรียมเส้นทางเข้าถึง";
                case "accident":
                    return "อุบัติเหตุบ่อย — พิจารณาจัดการจราจร/ติดป้ายเตือน/เพิ่มแสงสว่าง";
                case "conflict":
                    return "ความขัดแย้งบ่อย — เพิ่มการลาดตระเวนและมาตรการป้องกัน";
                default:
                    return "ความถี่ปานกลาง — เพิ่มความถี่ในการตรวจพื้นที่ช่วงพีค";
            }
        }
        return "ความถี่ต่ำ — ติดตามแนวโน้มต่อเนื่อง";
    }

    private double computeHeatWeight(int count) {
        // Simple scaling: cap at 1.0 for count >= 10
        return Math.max(0.1, Math.min(1.0, count / 10.0));
    }

    private Map<String, Integer> computeTimePeaks(List<Incident> group) {
        int morning = 0;   // 05:00 - 11:59
        int evening = 0;   // 12:00 - 17:59
        int night = 0;     // 18:00 - 04:59
        for (Incident i : group) {
            java.time.LocalDateTime t = i.getCreatedAt();
            int h = t.getHour();
            if (h >= 5 && h < 12) morning++;
            else if (h >= 12 && h < 18) evening++;
            else night++;
        }
        Map<String, Integer> peaks = new HashMap<>();
        peaks.put("morning", morning);
        peaks.put("evening", evening);
        peaks.put("night", night);
        return peaks;
    }

    private double parseDouble(Map<String, Object> payload, String key, double defaultVal) {
        if (payload == null || !payload.containsKey(key)) return defaultVal;
        try { return Double.parseDouble(String.valueOf(payload.get(key))); } catch (Exception e) { return defaultVal; }
    }

    private int parseInt(Map<String, Object> payload, String key, int defaultVal) {
        if (payload == null || !payload.containsKey(key)) return defaultVal;
        try { return Integer.parseInt(String.valueOf(payload.get(key))); } catch (Exception e) { return defaultVal; }
    }
}
