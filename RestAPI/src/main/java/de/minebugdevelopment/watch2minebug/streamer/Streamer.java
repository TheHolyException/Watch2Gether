package de.minebugdevelopment.watch2minebug.streamer;

import java.io.File;
import java.util.function.Consumer;

public abstract class Streamer {

    abstract void downloadVideo(String link, Consumer<File> onCompleted, Consumer<Integer> _progressCallback);

}
