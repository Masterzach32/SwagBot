package net.masterzach32.swagbot.utils.exceptions;

public class YouTubeDLException extends Throwable {

    private int exitCode;
    private String url;

    public YouTubeDLException(String url, int exitCode) {
        this.url = url;
        this.exitCode = exitCode;
    }

    public String getUrl() {
        return url;
    }

    public int getExitCode() {
        return exitCode;
    }
}