package com.project.telegrambot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@JsonIgnoreProperties
public class TemperatureForecast {

    @JsonProperty("Minimum")
    private MinimumTemperature minimumTemperature;

    @JsonProperty("Maximum")
    private MaximumTemperature maximumTemperature;
}
