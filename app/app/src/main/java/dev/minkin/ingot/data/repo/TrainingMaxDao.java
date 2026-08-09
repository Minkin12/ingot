package dev.minkin.ingot.data.repo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;

@Dao
public interface TrainingMaxDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertMax(TrainingMaxEntity trainingMaxEntity);

    @Query("SELECT t.* FROM training_max t " +
            "INNER JOIN (" +
            "  SELECT lift, MAX(effectiveAt) AS maxEffectiveAt " +
            "  FROM training_max GROUP BY lift" +
            ") latest ON t.lift = latest.lift AND t.effectiveAt = latest.maxEffectiveAt")
    List<TrainingMaxEntity> selectCurrentMaxes();

    @Query("Select * from training_max where lift == :lift order by effectiveAt desc")
    List<TrainingMaxEntity> selectAllMaxesForLift(String lift);
}
