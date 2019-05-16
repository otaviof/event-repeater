package otaviof.github.io.eventrepeater.repeater;

import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;
import otaviof.github.io.eventrepeater.config.Config;
import otaviof.github.io.eventrepeater.config.RepeaterConfig;

@EnableAutoConfiguration
@Service
@Slf4j
public class RepeaterService {
    private final Tracer tracer;
    private final Config config;

    private List<Repeater> repeaters;

    public RepeaterService(Tracer tracer, Config config) {
        this.tracer = tracer;
        this.config = config;
        this.repeaters = new ArrayList<>();

        bootstrap();
    }

    private void bootstrap() {
        log.info("Bootstrapping repeater tuples...");

        for (RepeaterConfig repeaterConfig : config.getRepeaters()) {
            log.info("Spinning up repeater route, from '{}' to '{}'",
                    repeaterConfig.getFrom(), repeaterConfig.getTo());
            var repeater = new Repeater(tracer, config.getKafka(), repeaterConfig);
            repeaters.add(repeater);
        }
    }
}
