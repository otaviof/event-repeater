package io.github.otaviof.eventrepeater.config;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaConfig {
    @NotEmpty
    private String schemaRegistryUrl;

    @NotEmpty
    private String brokers;
}
