package org.kku.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // accident, medical, conflict, fire, help

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 100)
    private String reporter;

    @Column(length = 200)
    private String reporterContact; // เพิ่มช่องทางติดต่อ

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "รอการดำเนินการ"; // รอการดำเนินการ, กำลังดำเนินการ, ดำเนินการแล้ว

    @Column(name = "officer_notes", length = 1000)
    private String officerNotes; // บันทึกของเจ้าหน้าที่

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "severity_level")
    private String severityLevel = "ปกติ"; // ปกติ, ด่วน, ฉุกเฉิน

    @Column(name = "image_url", length = 500)
    private String imageUrl; // path/URL ของรูปที่แนบ

    @Column(name = "edit_token", length = 64, unique = true)
    private String editToken; // โทเคนสำหรับผู้แจ้งในการแก้ไขสถานะของเหตุการณ์

    // Constructors
    public Incident() {}

    public Incident(String type, String description, double latitude, double longitude, String reporter) {
        this.type = type;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reporter = reporter;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public String getReporterContact() { return reporterContact; }
    public void setReporterContact(String reporterContact) { this.reporterContact = reporterContact; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getOfficerNotes() { return officerNotes; }
    public void setOfficerNotes(String officerNotes) { 
        this.officerNotes = officerNotes; 
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { 
        this.severityLevel = severityLevel; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getEditToken() { return editToken; }
    public void setEditToken(String editToken) { this.editToken = editToken; }

    // Helper methods
    public String getTypeDisplayName() {
        switch (type) {
            case "accident": return "อุบัติเหตุ";
            case "medical": return "ผู้ป่วยฉุกเฉิน";
            case "conflict": return "ทะเลาะวิวาท";
            case "fire": return "เหตุไฟไหม้";
            case "help": return "ขอความช่วยเหลือ";
            default: return "อื่นๆ";
        }
    }

    public String getStatusColor() {
        switch (status) {
            case "รอการดำเนินการ": return "#dc3545"; // แดง
            case "กำลังดำเนินการ": return "#ffc107"; // เหลือง
            case "ดำเนินการแล้ว": return "#28a745"; // เขียว
            default: return "#6c757d"; // เทา
        }
    }
}
