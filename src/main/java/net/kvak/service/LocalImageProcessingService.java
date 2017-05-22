package net.kvak.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by korteke on 22/05/17.
 * Snippet from https://github.com/lmammino
 */

@Component
@Slf4j
public class LocalImageProcessingService {

    @Value("${norppis.templateImagePath}")
    private String templateImagePath;

    @Value("${norppis.differencePercentLimit}")
    private String differencePercentLimit;

    public boolean compareAgainstTemplate(String filePath) {
        log.info("Comparing image against template");
        log.debug("Difference Percent Limit: {}%", Float.valueOf(differencePercentLimit));

        int difference = 0;
        float differencePercent = 0f;
        IplImage srcImage = cvLoadImage(filePath);
        IplImage templateImage = cvLoadImage(templateImagePath);

        int imgWidth = srcImage.width();
        int imgHeight = srcImage.height();

        BufferedImage cImage = IplImageToBufferedImage(srcImage);
        BufferedImage pImage = IplImageToBufferedImage(templateImage);
        BufferedImage dImage = new BufferedImage(imgWidth, imgHeight, cImage.getType());

        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                Color cColor = new Color(cImage.getRGB(x, y));
                Color pColor = new Color(pImage.getRGB(x, y));

                int cR = cColor.getRed();
                int cG = cColor.getGreen();
                int cB = cColor.getBlue();

                int pR = pColor.getRed();
                int pG = pColor.getGreen();
                int pB = pColor.getBlue();

                int dR = Math.abs(cR - pR);
                if (dR > 30)
                    dR += 100;
                int dG = Math.abs(cG - pG);
                int dB = Math.abs(cB - pB);

                difference += (dR + dG + dB) / 3;

                Color color = new Color(Math.max(0, Math.min(255, cR + dR)),
                        Math.max(0, Math.min(255, cG + dG)), Math.max(0,
                        Math.min(255, cB + dB)));
                dImage.setRGB(x, y, color.getRGB());
            }
        }

        differencePercent = (float) (difference) / (255 * imgWidth * imgHeight) * 100;
        log.info("Difference: {}%", differencePercent);

        if (differencePercent > Float.valueOf(differencePercentLimit)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convert IplImage to BufferedImage
     * @param src
     * @return
     */
    public static BufferedImage IplImageToBufferedImage(IplImage src) {
        OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter paintConverter = new Java2DFrameConverter();
        Frame frame = grabberConverter.convert(src);
        return paintConverter.getBufferedImage(frame,1);
    }
}