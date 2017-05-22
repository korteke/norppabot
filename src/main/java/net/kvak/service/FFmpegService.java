package net.kvak.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by korteke on 11/05/2017.
 */
@Service
@Slf4j
public class FFmpegService {

    @NonNull
    @Value("${ffmpeg.norppaUrl}")
    private String norppaUrl;

    @NonNull
    @Value("${ffmpeg.filePath}")
    private String filePath;

    @NonNull
    @Value("${ffmpeg.execPath}")
    private String execPath;

    @NonNull
    @Value("${ffmpeg.probeExecPath}")
    private String probeExecPath;

    @Autowired
    private DetectNorppaService detectNorppaService;

    @Autowired
    private LocalImageProcessingService localImageProcessingService;

    @Scheduled(cron = "${norppis.schedule}",zone = "Europe/Helsinki")
    public void getFrameFromNorppalive() throws IOException {

        FFmpeg ffmpeg = new FFmpeg(execPath);
        FFprobe ffprobe = new FFprobe(probeExecPath);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(norppaUrl)
                .addOutput(filePath)
                .setFormat("image2")
                .addExtraArgs("-ss","1.0")
                .addExtraArgs("-t", "1")
                .addExtraArgs("-vframes", "1")
                .addExtraArgs("-qscale","1")
                .addExtraArgs("-q:v", "1")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        log.debug("Image to local detection module");

        if (localImageProcessingService.compareAgainstTemplate(filePath)) {
            log.info("Sending image to Amazon rekognition");
            detectNorppaService.detect(extractImageBytes(filePath));
        } else {
            log.info("Not sending image to Amazon Rekognition. Trying to save little bit of money :)");
        }
    }

    /**
     * Convert image path to bytes
     *
     * @param imagePath
     * @return
     * @throws IOException
     */
    private byte[] extractImageBytes (String imagePath) throws IOException {
        File file = new File(imagePath);
        return Files.readAllBytes(file.toPath());
    }
}