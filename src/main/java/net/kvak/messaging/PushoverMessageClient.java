package net.kvak.messaging;

import lombok.extern.slf4j.Slf4j;
import net.kvak.configuration.PushoverConfiguration;
import net.pushover.client.PushoverClient;
import net.pushover.client.PushoverException;
import net.pushover.client.PushoverMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by korteke on 12/05/2017.
 */

@Slf4j
@Component
public class PushoverMessageClient implements MessageClient {

    @Autowired
    private PushoverClient pushoverClient;

    @Autowired
    private PushoverConfiguration pushoverConfiguration;

    @Override
    public boolean sendMessage(String message) {

        try {
            pushoverClient.pushMessage(PushoverMessage.builderWithApiToken(pushoverConfiguration.getApiToken())
                    .setUserId(pushoverConfiguration.getUserToken())
                    .setTitle(pushoverConfiguration.getTitle())
                    .setMessage(message)
                    .build());
        } catch (PushoverException e) {
            log.error("Error. Can't send pushover message!");
            return false;
        }

        log.info("sendMessage");
        return true;
    }

}