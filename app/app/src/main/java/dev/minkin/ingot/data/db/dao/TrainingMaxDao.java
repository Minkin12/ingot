package dev.minkin.ingot.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;

@Dao
public abstract class TrainingMaxDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract void insertMax(TrainingMaxEntity trainingMaxEntity);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract void insertOutboxEvent(OutboxEntity outboxEntity);

    @Query("SELECT t.* FROM training_max t " +
            "INNER JOIN (" +
            "  SELECT lift, MAX(effectiveAt) AS maxEffectiveAt " +
            "  FROM training_max GROUP BY lift" +
            ") latest ON t.lift = latest.lift AND t.effectiveAt = latest.maxEffectiveAt")
    public abstract List<TrainingMaxEntity> selectCurrentMaxes();

    @Query("Select * from training_max where lift == :lift order by effectiveAt desc")
    public abstract List<TrainingMaxEntity> selectAllMaxesForLift(String lift);

    @Transaction
    public void insertMaxAndQueue(TrainingMaxEntity trainingMaxEntity, OutboxEntity outboxEntity){
        insertMax(trainingMaxEntity);
        insertOutboxEvent(outboxEntity);
    }
}
