package dev.minkin.ingot.backend.projection.controller;

import dev.minkin.ingot.backend.projection.entity.TonnageEntity;
import dev.minkin.ingot.backend.projection.model.TonnageWeekSummary;
import dev.minkin.ingot.backend.projection.repository.TonnageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class TonnageController {

    TonnageRepository tonnageRepository;

    public TonnageController(TonnageRepository tonnageRepository) {
        this.tonnageRepository = tonnageRepository;
    }

    @GetMapping("/tonnage")
    public List<TonnageEntity> getAllTonnage() {
        return tonnageRepository.findAllByOrderByWeekNumberAscDayNumberAsc();
    }

    @GetMapping("/tonnage/exercise/{exerciseName}")
    public List<TonnageEntity> getTonnageForExercise(@PathVariable String exerciseName) {
        return tonnageRepository.findAllByExerciseNameOrderByWeekNumberAscDayNumberAsc(exerciseName);
    }

    @GetMapping("/tonnage/week/{weekNumber}/day/{dayNumber}")
    public List<TonnageEntity> getTonnageForDay(@PathVariable int weekNumber, @PathVariable int dayNumber) {
        return tonnageRepository.findAllByWeekNumberAndDayNumber(weekNumber, dayNumber);
    }

    @GetMapping("/tonnage/week/{weekNumber}")
    public List<TonnageWeekSummary> getTonnageForWeek(@PathVariable int weekNumber) {
        return tonnageRepository.getWeekSummary(weekNumber);
    }

}
