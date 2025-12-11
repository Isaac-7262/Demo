package org.kku.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.kku.model.Incident;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@EnableScheduling
public class IncidentEventsService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    
    public IncidentEventsService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        
        // ส่ง initial event เพื่อยืนยันว่า connection สำเร็จ
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        
        return emitter;
    }

    public void sendEvent(String event, Object payload) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                emitter.send(SseEmitter.event().name(event).data(json, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }
    
    // Heartbeat ทุก 15 วินาที เพื่อป้องกัน connection timeout
    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data(System.currentTimeMillis()));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }

    public void incidentCreated(Incident incident) { sendEvent("incident-created", incident); }
    public void incidentUpdated(Incident incident) { sendEvent("incident-updated", incident); }
    public void incidentDeleted(Long id) { sendEvent("incident-deleted", id); }
    
    public int getActiveConnections() { return emitters.size(); }
}
