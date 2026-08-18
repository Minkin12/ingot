package dev.minkin.ingot.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "exercise_catalog")
@Getter
@Setter
public class ExerciseCatalogEntity {
    @PrimaryKey
    @NonNull
    private String name;          // canonical name, e.g. "Back Squat" — the join key everything else uses
    private String muscleGroup;   // "legs", "chest", "back", "shoulders", "arms", "core"
    private String equipment;     // "barbell", "dumbbell", "machine", "bodyweight", "cable"
    private String sourceLift;    // nullable — MajorLift jsonName if this exercise IS a tracked lift's canonical movement, else null
    private boolean isCompound;   // true for multi-joint movements, false for isolation — useful later for swap-suggestion filtering
}
