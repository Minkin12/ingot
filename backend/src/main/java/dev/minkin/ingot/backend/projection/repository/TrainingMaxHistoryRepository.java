package dev.minkin.ingot.backend.projection.repository;

import dev.minkin.ingot.backend.projection.entity.TrainingMaxHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingMaxHistoryRepository extends JpaRepository<TrainingMaxHistoryEntity, String> {

    List<TrainingMaxHistoryEntity> findAllByLiftOrderByAchievedAtDesc(String lift);

    List<TrainingMaxHistoryEntity> findAllByOrderByAchievedAtDesc();
    
    Optional<TrainingMaxHistoryEntity> findTopByLiftOrderByAchievedAtDesc(String lift);

    @Query(value = """
            SELECT * FROM ingot.training_max_history h
            WHERE h.achieved_at = (
                SELECT MAX(h2.achieved_at)
                FROM ingot.training_max_history h2
                WHERE h2.lift = h.lift
            )
            """, nativeQuery = true)
    List<TrainingMaxHistoryEntity> getCurrentMaxes();

    @Query(value = """
            SELECT * FROM ingot.training_max_history
            WHERE lift = :lift
            ORDER BY achieved_at DESC
            """, nativeQuery = true)
    List<TrainingMaxHistoryEntity> getHistoryForLift(@Param("lift") String lift);




}
