package dev.minkin.ingot.data.db.entity.types;


public class SessionCoordinates {
    public int weekNumber;
    public int dayNumber;

    public SessionCoordinates(int weekNumber, int dayNumber){
        this.weekNumber = weekNumber;
        this.dayNumber = dayNumber;
    }
    public SessionCoordinates(){}
}
