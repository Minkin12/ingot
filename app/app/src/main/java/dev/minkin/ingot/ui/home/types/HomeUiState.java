package dev.minkin.ingot.ui.home.types;

import java.util.List;

import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import lombok.Value;

@Value
public class HomeUiState {
    int selectedWeekNumber;
    List<WorkoutState> workouts;
    SessionCoordinates upNext;
}
