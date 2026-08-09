package dev.minkin.ingot.engine.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class Program {
    String name;
    String units;
    List<Week> weeks;
    @JsonAlias("inferred_1rms_lbs")
    private Map<String, Double> oneRepMaxes;

    public Week getWeek(int index){
        for (Week week : weeks) {
            if (week.number == index){
                return week;
            }
        }
        return null;
    }

    public Day getDay(int weekNumber, int dayNumber){
        for (Week week : weeks) {
            if (weekNumber == week.getNumber()){
                for (Day day : week.getDays()){
                    if (dayNumber == day.getDayNumber()){
                        return day;
                    }
                }
            }
        }
        return null;
    }
}
