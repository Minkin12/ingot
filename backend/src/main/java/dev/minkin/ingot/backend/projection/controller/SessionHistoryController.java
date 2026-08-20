package dev.minkin.ingot.backend.projection.controller;

import dev.minkin.ingot.backend.projection.entity.SessionHistoryEntity;
import dev.minkin.ingot.backend.projection.repository.SessionHistoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SessionHistoryController {

    private final SessionHistoryRepository repository;

    public SessionHistoryController(SessionHistoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/history")
    public List<SessionHistoryEntity> getHistory() {
        return repository.findAllByOrderByCompletedAtDesc();
    }
}
