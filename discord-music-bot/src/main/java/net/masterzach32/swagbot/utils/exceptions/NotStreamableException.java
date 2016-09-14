package net.masterzach32.swagbot.utils.exceptions;

public class NotStreamableException extends Throwable {

    private String provider, url;

    public NotStreamableException(String provider, String url) {
        this.provider = provider;
        this.url = url;
    }

    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }
}