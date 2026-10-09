package com.internhub.controller;

import com.internhub.entity.Notification;
import com.internhub.repository.NotificationRepository;
import com.internhub.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private Long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getUser().getId();
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication auth) {
        Long userId = getUserId(auth);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
        List<Map<String, Object>> result = notifications.stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", n.getId());
            m.put("message", n.getMessage());
            m.put("type", n.getType());
            m.put("read", n.isReadStatus());
            m.put("createdDate", n.getCreatedDate());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication auth) {
        long count = notificationRepository.countByUserIdAndReadStatusFalse(getUserId(auth));
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (!n.getUser().getId().equals(userId)) return ResponseEntity.status(403).body("Forbidden");
        n.setReadStatus(true);
        notificationRepository.save(n);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        Long userId = getUserId(auth);
        List<Notification> unread = notificationRepository.findByUserIdAndReadStatusFalse(userId);
        unread.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (!n.getUser().getId().equals(userId)) return ResponseEntity.status(403).body("Forbidden");
        notificationRepository.delete(n);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
