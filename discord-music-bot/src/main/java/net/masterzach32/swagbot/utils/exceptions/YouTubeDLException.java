package net.masterzach32.swagbot.utils.exceptions;

public class YouTubeDLException extends Throwable {

    private int exitCode;
    private String url;

    public YouTubeDLException(String url, int exitCode) {
        super("An error occurred downloading " + url);
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