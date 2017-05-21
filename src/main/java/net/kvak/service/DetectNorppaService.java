package net.kvak.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.kvak.messaging.PushoverMessageClient;
import net.kvak.model.NorppaStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by korteke on 11/05/2017.
 */
@Component
@Slf4j
public class DetectNorppaService {

    @Autowired
    private NorppaStatus norppaStatus;

    @Autowired
    private PushoverMessageClient pushoverMessageClient;

    @Autowired
    private Twitter twitter;

    @NonNull
    @Value("${ffmpeg.filePath}")
    private String filePath;

    @NonNull
    @Value("${twitter.message}")
    private String message;

    @NonNull
    @Value("${norppis.minConfidence}")
    private String minConfidence;

    public void detect(byte[] imageBytes) {

        AWSCredentials credentials;

        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch(Exception e) {
            throw new AmazonClientException("Check credentials", e);
        }

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(ByteBuffer.wrap(imageBytes)))
                .withMaxLabels(10)
                .withMinConfidence(Float.valueOf(minConfidence));

        try {
            boolean prevStatus = norppaStatus.isNorppaDetected();
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List <Label> labels = result.getLabels();

            log.info("Analyzing scene");
            for (Label label: labels) {
                log.info("{} - confidence: {}%",label.getName(),roundFloat(label.getConfidence()));
                if ("animal".equals(label.getName().toLowerCase())) {
                    norppaStatus.setNorppaDetected(true);
                    log.info("Animal detected");
                    if (!prevStatus) {
                        log.info("New animal detection");
                        try {
                            StatusUpdate statusUpdate = new StatusUpdate(message);
                            File file = new File(filePath);
                            statusUpdate.setMedia(file);

                            Status status = twitter.updateStatus(statusUpdate);
                            StringBuilder sb = new StringBuilder();
                            sb.append("https://twitter.com/");
                            sb.append(status.getUser().getScreenName());
                            sb.append("/status/");
                            sb.append(status.getId());

                            pushoverMessageClient.sendMessage("Animal detected. confidence: " +
                                    roundFloat(label.getConfidence()) + "%\n URL: " + sb.toString());
                        } catch (TwitterException e) {
                            log.error("Twitter exception {}", e.getErrorMessage());
                        }
                    }
                    break;

                } else {
                    norppaStatus.setNorppaDetected(false);
                }
            }
        } catch(AmazonRekognitionException e) {
            e.printStackTrace();
        }

        log.info("Status isNorppaDetected - {}", norppaStatus.isNorppaDetected());
    }

    private double roundFloat(double d) {
        DecimalFormat dFormat = new DecimalFormat("#.##");
        return Double.valueOf(dFormat.format(d));
    }
}