package org.kku.repo;

import org.kku.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);

    @Query("SELECT m FROM Message m WHERE m.incidentId = :incidentId ORDER BY m.createdAt DESC LIMIT 1")
    List<Message> findLatestByIncidentId(Long incidentId);
}
