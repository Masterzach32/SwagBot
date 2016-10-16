package net.masterzach32.swagbot.utils.exceptions;

public class YouTubeAPIException extends Throwable {

    public YouTubeAPIException(String url) {
        super("Could not parse API call on " + url);
    }
}
