package net.kvak.configuration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by korteke on 19/05/2017.
 */

@Slf4j
@Configuration
public class TwitterConfiguration {

    @NonNull
    @Value("${twitter.consumerKey}")
    private String consumerKey;

    @NonNull
    @Value("${twitter.consumerSecret}")
    private String consumerSecret;

    @NonNull
    @Value("${twitter.accessToken}")
    private String accessToken;

    @NonNull
    @Value("${twitter.accessTokenSecret}")
    private String accessTokenSecret;

    private ConfigurationBuilder createConfigurationBuilder() {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setDebugEnabled(true);

        log.debug("createConfigurationBuilder");
        return configurationBuilder;
    }

    @Bean
    public Twitter twitter() {
        TwitterFactory factory = new TwitterFactory(createConfigurationBuilder().build());
        Twitter twitter = factory.getInstance();

        log.debug("twitter");
        return twitter;
    }
}