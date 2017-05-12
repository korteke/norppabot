package net.kvak.configuration;
import lombok.Getter;
import lombok.NonNull;
import net.pushover.client.PushoverClient;
import net.pushover.client.PushoverRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by korteke on 20/03/2017.
 */

@Configuration
public class PushoverConfiguration {

    @NonNull
    @Getter
    @Value("${pushover.apiToken}")
    private String apiToken;

    @NonNull
    @Getter
    @Value("${pushover.userToken}")
    private String userToken;

    @NonNull
    @Getter
    @Value("${pushover.enabled}")
    private String enabled;

    @NonNull
    @Getter
    @Value("${pushover.title}")
    private String title;

    @Bean
    public PushoverConfiguration pushoverConfig() {
        return new PushoverConfiguration();
    }

    @Bean
    public PushoverClient pushoverClient() {
        return new PushoverRestClient();
    }

}