package dev.minkin.ingot.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training_max")
public class TrainingMaxEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // MajorLift jsonName ("squat", "bench", "deadlift", "hip_thrust").
    public String lift;

    public double valueLbs;

    // Epoch millis when this max became effective ie when a PR happened
    public long effectiveAt;
}
