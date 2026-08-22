package dev.minkin.ingot.backend.projection.controller;

import dev.minkin.ingot.backend.projection.entity.TrainingMaxHistoryEntity;
import dev.minkin.ingot.backend.projection.repository.TrainingMaxHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
public class TrainingMaxHistoryController {

    TrainingMaxHistoryRepository trainingMaxHistoryRepository;

    @GetMapping("/training-maxes")
    public List<TrainingMaxHistoryEntity> getCurrentMaxes() {
        return trainingMaxHistoryRepository.getCurrentMaxes();
    }

    @GetMapping("/training-maxes/{lift}/history")
    public List<TrainingMaxHistoryEntity> getHistoryForLift(@PathVariable String lift) {
        return trainingMaxHistoryRepository.findAllByLiftOrderByAchievedAtDesc(lift);
    }

    @GetMapping("/training-maxes/sync")
    public List<TrainingMaxHistoryEntity> getMaxesSince(@RequestParam(required = false) Long since) {
        if (since == null) {
            return trainingMaxHistoryRepository.getCurrentMaxes();
        }
        return trainingMaxHistoryRepository.findAllByAchievedAtGreaterThanOrderByAchievedAtAsc(since);
    }
}