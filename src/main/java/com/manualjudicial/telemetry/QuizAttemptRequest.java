package com.manualjudicial.telemetry;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizAttemptRequest {
    private Long chapterId;
    private Double score;
    private LocalDateTime startTime;
    private Integer secondsUsed;
}
