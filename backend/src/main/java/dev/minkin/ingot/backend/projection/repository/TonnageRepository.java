package dev.minkin.ingot.backend.projection.repository;

import dev.minkin.ingot.backend.projection.entity.TonnageEntity;
import dev.minkin.ingot.backend.projection.model.TonnageWeekSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TonnageRepository extends JpaRepository<TonnageEntity, Long> {

    List<TonnageEntity> findAllByOrderByWeekNumberAscDayNumberAsc();

    List<TonnageEntity> findAllByExerciseNameOrderByWeekNumberAscDayNumberAsc(String exerciseName);

    Optional<TonnageEntity> findByWeekNumberAndDayNumberAndExerciseNameAndSetNumber(
            int weekNumber, int dayNumber, String exerciseName, int setNumber);

    List<TonnageEntity> findAllByWeekNumberAndDayNumber(int weekNumber, int dayNumber);

    @Query(value = """
            SELECT week_number, day_number, exercise_name,
                   SUM(CAST(weight_lbs AS DOUBLE PRECISION) * reps) AS total_tonnage,
                   COUNT(*) AS set_count
            FROM ingot.tonnage_sets
            WHERE week_number = :weekNumber
            GROUP BY week_number, day_number, exercise_name
            order by day_number
            """, nativeQuery = true)
    List<TonnageWeekSummary> getWeekSummary(@Param("weekNumber") int weekNumber);
}
