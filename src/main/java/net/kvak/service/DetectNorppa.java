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
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by korteke on 11/05/2017.
 */
@Component
@Slf4j
public class DetectNorppa {

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
                .withMaxLabels(10)
                .withMinConfidence(77F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List <Label> labels = result.getLabels();

            System.out.println("Detected labels");
            for (Label label: labels) {
                log.debug(label.getName() + ": " + label.getConfidence().toString());
            }
        } catch(AmazonRekognitionException e) {
            e.printStackTrace();
        }
    }
}
