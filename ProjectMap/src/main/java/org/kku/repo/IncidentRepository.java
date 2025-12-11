package org.kku.repo;

import org.kku.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // ค้นหาเหตุการณ์ที่ยังไม่ได้ดำเนินการและกำลังดำเนินการ
    @Query("SELECT i FROM Incident i WHERE i.status IN ('รอการดำเนินการ', 'กำลังดำเนินการ') ORDER BY i.createdAt DESC")
    List<Incident> findActiveIncidents();

    // ค้นหาตามประเภทเหตุการณ์
    List<Incident> findByTypeOrderByCreatedAtDesc(String type);

    // ค้นหาตามสถานะ
    List<Incident> findByStatusOrderByCreatedAtDesc(String status);

    // ค้นหาเหตุการณ์ในช่วงวันที่
    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
    List<Incident> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ค้นหาเหตุการณ์ในพื้นที่ (ใช้สำหรับ map bounds)
    @Query("SELECT i FROM Incident i WHERE i.latitude BETWEEN :minLat AND :maxLat AND i.longitude BETWEEN :minLng AND :maxLng ORDER BY i.createdAt DESC")
    List<Incident> findIncidentsInBounds(
        @Param("minLat") double minLat, 
        @Param("maxLat") double maxLat, 
        @Param("minLng") double minLng, 
        @Param("maxLng") double maxLng
    );

    // นับเหตุการณ์ตามสถานะ
    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countIncidentsByStatus();

    // นับเหตุการณ์ตามประเภท
    @Query("SELECT i.type, COUNT(i) FROM Incident i GROUP BY i.type")
    List<Object[]> countIncidentsByType();

    // เหตุการณ์ล่าสุด 10 รายการ
    @Query("SELECT i FROM Incident i ORDER BY i.createdAt DESC")
    List<Incident> findRecentIncidents();
}
