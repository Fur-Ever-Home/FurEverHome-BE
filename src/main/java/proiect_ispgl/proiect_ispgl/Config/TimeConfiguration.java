package proiect_ispgl.proiect_ispgl.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfiguration {
    @Bean
    public Clock fundingClock(@Value("${app.funding.time-zone:Europe/Bucharest}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
