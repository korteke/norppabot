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
import lombok.extern.slf4j.Slf4j;
import net.kvak.model.NorppaStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by korteke on 11/05/2017.
 */
@Component
@Slf4j
public class DetectNorppa {

    @Autowired
    NorppaStatus norppaStatus;

    public DetectNorppa() {
    }

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
                .withMaxLabels(10);
                //.withMinConfidence(75F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List <Label> labels = result.getLabels();

            log.info("Analyzing scene");
            for (Label label: labels) {
                if ("animal".equals(label.toString().toLowerCase()) && !norppaStatus.isNorppaDetected()) {
                    log.info("Animal detected");
                    norppaStatus.setNorppaDetected(true);
                    // Send tweet & Pushover
                } else {
                    norppaStatus.setNorppaDetected(false);
                }
                log.info(label.getName() + " - confidence: " + roundFloat(label.getConfidence()) +"%");
            }
        } catch(AmazonRekognitionException e) {
            e.printStackTrace();
        }
    }

    private double roundFloat(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
}
