package dev.minkin.ingot.data.remote.types;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainingMaxHistoryDto {
    private String lift;
    private String valueLbs;
    private long achievedAt;
    private String sourceEventId;

}