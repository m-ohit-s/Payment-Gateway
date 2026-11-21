package com.myspace.razorpayclone.operations.settlement.simulator;

import com.myspace.razorpayclone.common.enums.ChaosMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "settlement.simulator")
@Getter
@Setter
public class SettlementSimulatorConfig {
    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Integer minDelaySeconds = 2;
    private Integer maxDelaySeconds = 5;
    private Integer successRate = 80;
}
