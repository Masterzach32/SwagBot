package net.masterzach32.swagbot.utils.exceptions;

import java.io.File;

public class FFMPEGException extends Throwable {

    private int exitCode;
    private File file;
    private String url;

    public FFMPEGException(File file, String url, int exitCode) {
        this.file = file;
        this.url = url;
        this.exitCode = exitCode;
    }

    public File getFile() {
        return file;
    }

    public String getUrl() {
        return url;
    }

    public int getExitCode() {
        return exitCode;
    }
}