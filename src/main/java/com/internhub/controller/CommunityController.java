package com.internhub.controller;

import com.internhub.entity.*;
import com.internhub.repository.EventRepository;
import com.internhub.repository.PostRepository;
import com.internhub.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final PostRepository postRepository;
    private final EventRepository eventRepository;

    public CommunityController(PostRepository postRepository, EventRepository eventRepository) {
        this.postRepository = postRepository;
        this.eventRepository = eventRepository;
    }

    private User getUser(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = posts.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("content", p.getContent());
            m.put("likes", p.getLikesCount());
            m.put("authorEmail", p.getAuthor().getEmail());
            m.put("createdAt", p.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(Authentication auth, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Content is required"));
        }
        Post p = new Post();
        p.setAuthor(getUser(auth));
        p.setContent(content);
        postRepository.save(p);
        return ResponseEntity.ok(Map.of("message", "Post created"));
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents() {
        List<Event> events = eventRepository.findAllByOrderByEventDateAsc();
        List<Map<String, Object>> result = events.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("description", e.getDescription());
            m.put("type", e.getEventType());
            m.put("location", e.getLocation());
            m.put("date", e.getEventDate());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }
}
