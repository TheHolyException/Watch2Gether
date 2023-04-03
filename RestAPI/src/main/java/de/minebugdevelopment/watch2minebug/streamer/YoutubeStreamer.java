package de.minebugdevelopment.watch2minebug.streamer;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import de.minebugdevelopment.watch2minebug.utils.executor.ExecutorHandler;
import de.minebugdevelopment.watch2minebug.utils.executor.ExecutorTask;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public class YoutubeStreamer extends Streamer {

    private final File downloadFolder;
    private final YoutubeDownloader downloader;
    private final ExecutorHandler parseHandler;
    private final ExecutorHandler downloadHandler;
    private final File ffmpeg;

    private final ThreadLocal<Consumer<Integer>> progressCallback = new ThreadLocal<>();

    private static YoutubeStreamer instance;

    static {
        YoutubeStreamer.instance = new YoutubeStreamer();
    }

    public static YoutubeStreamer getInstance() {
        return instance;
    }

    private YoutubeStreamer() {
        downloadFolder = new File("./videos/.downloading/");
        downloader = new YoutubeDownloader();
        parseHandler = new ExecutorHandler(Executors.newFixedThreadPool(4));
        downloadHandler = new ExecutorHandler(Executors.newFixedThreadPool(10));

        if (!downloadFolder.exists()) downloadFolder.mkdirs();

        URL url = YoutubeStreamer.class.getClassLoader().getResource("ffmpeg.exe");
        assert url != null;

        try {
            ffmpeg = new File(url.toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void downloadVideo(String link) {
        downloadVideo(link, null, null);
    }

    public void downloadVideo(String link, Consumer<File> onCompleted) {
        downloadVideo(link, onCompleted, null);
    }

    public void downloadVideo(String link, Consumer<File> onCompleted, Consumer<Integer> _progressCallback) {
        parseHandler.putTask(new ExecutorTask(() -> {
            progressCallback.set(_progressCallback);
            File file = processVideoDownload(link);
            if (onCompleted != null && file != null) onCompleted.accept(file);
        }));
    }

    public File processVideoDownload(String link) {
        try {
            VideoInfo videoInfo = downloader.getVideoInfo(new RequestVideoInfo(link)).data();

            VideoFormat videoFormat = videoInfo.bestVideoFormat();
            AudioFormat audioFormat = videoInfo.bestAudioFormat();

            AtomicReference<File> videoFile = new AtomicReference<>(
                    new File(downloadFolder,
                            String.format("V%s-%s",
                                    videoInfo.details().title(),
                                    System.currentTimeMillis())
                    )
            );
            AtomicReference<File> audioFile = new AtomicReference<>(
                    new File(downloadFolder,
                            String.format("A%s-%s",
                                    videoInfo.details().title(),
                                    System.currentTimeMillis())
                    )
            );

            downloadContent(videoFile, audioFile, videoFormat, audioFormat);

            File outputFile = new File(downloadFolder, cleanFilename(videoInfo.details().title()) + ".mp4");
            if (outputFile.exists()) Files.delete(outputFile.toPath());

            ProcessBuilder builder = new ProcessBuilder(ffmpeg.getAbsolutePath(), "-i", videoFile.get().getAbsolutePath(), "-i", audioFile.get().getAbsolutePath(), "-acodec", "copy", "-vcodec", "copy", outputFile.getAbsolutePath());
            StringBuilder command = new StringBuilder();
            for (String s : builder.command()) {
                command.append(s).append(" ");
            }
            System.out.println(command);
            //if (log.isDebugEnabled())
                builder.redirectErrorStream(true);


            Process ffmpegProcess;
            ffmpegProcess = builder.start();

            InputStream outStr = ffmpegProcess.getInputStream();
            new Thread(() -> {
                try{
                    DataInputStream dis = new DataInputStream(outStr);
                    while(dis.readLine() != null);
                }catch(Throwable t){
                    t.printStackTrace();
                }
            }).start();

            ffmpegProcess.waitFor();

            File target = new File(outputFile.getParentFile().getParentFile().getAbsolutePath(), outputFile.getName());
            Files.deleteIfExists(videoFile.get().toPath());
            Files.deleteIfExists(audioFile.get().toPath());
            Files.deleteIfExists(target.toPath());

            if (!outputFile.renameTo(target)) log.warn("Failed to rename file {}", outputFile.getName());
            return outputFile;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    private void downloadContent(AtomicReference<File> videoFile, AtomicReference<File> audioFile, VideoFormat videoFormat, AudioFormat audioFormat) {
        final AtomicInteger videoProgress = new AtomicInteger(0);
        final AtomicInteger audioProgress = new AtomicInteger(0);

        RequestVideoFileDownload requestVideo = new RequestVideoFileDownloadRewrite(videoFormat)
                .saveTo(videoFile.get().getParentFile())
                .renameTo(cleanFilename(videoFile.get().getName()))
                .overwriteIfExists(true)
                .callback(new AbstractYoutubeCallback<File>() {
                    @Override
                    public void onDownloading(int i) {
                        videoProgress.set(i);
                    }
                });

        RequestVideoFileDownload requestAudio = new RequestVideoFileDownloadRewrite(audioFormat)
                .saveTo(audioFile.get().getParentFile())
                .renameTo(cleanFilename(audioFile.get().getName()))
                .overwriteIfExists(true)

                .callback(new AbstractYoutubeCallback<File>() {
                    @Override
                    public void onDownloading(int i) {
                        audioProgress.set(i);
                    }
                });

        Timer progressCheckInterval = new Timer();
        Consumer<Integer> progCallback;
        if ((progCallback = progressCallback.get()) != null) {
            progressCheckInterval.schedule(new TimerTask() {
                @Override
                public void run() {
                    progCallback.accept(videoProgress.get()/2 + audioProgress.get()/2);
                }
            }, 500, 500);
        }


        long groupID = System.currentTimeMillis();

        downloadHandler.putTask(new ExecutorTask(() -> videoFile.set(downloader.downloadVideoFile(requestVideo).data()), groupID));
        downloadHandler.putTask(new ExecutorTask(() -> audioFile.set(downloader.downloadVideoFile(requestAudio).data()), groupID));

        downloadHandler.awaitGroup(groupID);
        progressCheckInterval.cancel();

    }

    private static String cleanFilename(String filename) {
        final char[] illegalChars = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>','|', '\"', ':', '#', '.', ' ', ',', 'ä', 'ö', 'ü', 'ß'};
        for (char c : illegalChars) {
            filename = filename.replace(c, '_');
        }
        return filename;
    }


    public static class AbstractYoutubeCallback<T> implements YoutubeProgressCallback<T> {
        @Override
        public void onDownloading(int i) {}

        @Override
        public void onFinished(T t) {}

        @Override
        public void onError(Throwable throwable) {}
    }

    public static class RequestVideoFileDownloadRewrite extends RequestVideoFileDownload {
        public RequestVideoFileDownloadRewrite(Format format) {
            super(format);
        }

        @Override
        public File getOutputFile() {
            File f = super.getOutputFile();
            return new File(f.getAbsolutePath().replace("."+getFormat().extension().value(), ""));
        }
    }
}
