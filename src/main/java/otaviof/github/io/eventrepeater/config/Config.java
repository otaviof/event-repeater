package otaviof.github.io.eventrepeater.config;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("eventrepeater")
@Validated
@Getter
@Setter
public class Config {
    @NotNull
    private KafkaConfig kafka;

    @NotNull
    @NotEmpty
    private List<RepeaterConfig> repeaters;
}
