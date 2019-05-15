package otaviof.github.io.eventrepeater.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepeaterConfig {
    @NotNull
    @NotEmpty
    private String from;

    @NotNull
    @NotEmpty
    private String to;

    private int delayMs = 0;
}
