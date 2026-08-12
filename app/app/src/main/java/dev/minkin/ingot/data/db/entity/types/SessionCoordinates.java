package dev.minkin.ingot.data.db.entity.types;


import java.util.Objects;

public class SessionCoordinates {
    public int weekNumber;
    public int dayNumber;

    public SessionCoordinates(int weekNumber, int dayNumber){
        this.weekNumber = weekNumber;
        this.dayNumber = dayNumber;
    }
    public SessionCoordinates(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionCoordinates)) return false;
        SessionCoordinates other = (SessionCoordinates) o;
        return weekNumber == other.weekNumber && dayNumber == other.dayNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weekNumber, dayNumber);
    }
}
