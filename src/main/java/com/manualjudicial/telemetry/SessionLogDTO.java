package com.manualjudicial.telemetry;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionLogDTO {
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private Long durationMs;
}
