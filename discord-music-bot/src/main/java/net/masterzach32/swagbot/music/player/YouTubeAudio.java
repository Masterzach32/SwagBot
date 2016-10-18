package net.masterzach32.swagbot.music.player;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.Constants;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YouTubeAudio extends AudioSource {

    private String video_id;
    private boolean isLiveStream;

    public YouTubeAudio(String url) throws UnirestException, YouTubeAPIException {
        this.url = url;
        this.source = "youtube";
        video_id = url.substring(url.indexOf("?v=") + 3, url.indexOf("=") + 12);
        HttpResponse<JsonNode> response =  Unirest.get("https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet" +
                "&id=" + video_id +
                "&key=" + App.prefs.getGoogleAuthKey()).asJson();
        if(response.getStatus() == 200) {
            try {
                JSONObject json = response.getBody().getObject().getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                title = json.getString("title");
                isLiveStream = json.getString("liveBroadcastContent").equals("live");
            } catch (JSONException e){
                throw new YouTubeAPIException(url);
            }
        } else
            App.logger.warn("Youtube Data API responded with status code " + response.getStatus() + " for video id " + video_id);

    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getStreamUrl() {
        return null;
    }

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException {
        Process ytdlProcess;
        Process ffmpegProcess;

        Thread ytdlToFFmpegThread;
        Thread ytdlErrGobler;
        Thread ffmpegErrGobler;

        List<String> ytdlLaunchArgs = new ArrayList<>();
        ytdlLaunchArgs.add("python");
        ytdlLaunchArgs.add("./bin/youtube-dl");
        ytdlLaunchArgs.add("-q");
        ytdlLaunchArgs.add("-f");
        ytdlLaunchArgs.add("bestaudio/best");
        ytdlLaunchArgs.add("--no-playlist");
        ytdlLaunchArgs.add("-4");
        ytdlLaunchArgs.add("--no-cache-dir");
        ytdlLaunchArgs.add("-o");
        ytdlLaunchArgs.add("-");
        List<String> ffmpegLaunchArgs = new ArrayList<>();
        ffmpegLaunchArgs.add("ffmpeg");
        ffmpegLaunchArgs.add("-i");
        ffmpegLaunchArgs.add("-");
        ffmpegLaunchArgs.add("-f");
        ffmpegLaunchArgs.add("s16be");
        ffmpegLaunchArgs.add("-ac");
        ffmpegLaunchArgs.add("2");
        ffmpegLaunchArgs.add("-ar");
        ffmpegLaunchArgs.add("48000");
        ffmpegLaunchArgs.add("-map");
        ffmpegLaunchArgs.add("a");
        ffmpegLaunchArgs.add("-");

        try {
            ProcessBuilder pBuilder = new ProcessBuilder();

            ytdlLaunchArgs.add("--");
            ytdlLaunchArgs.add(url);
            pBuilder.command(ytdlLaunchArgs);
            ytdlProcess = pBuilder.start();

            pBuilder.command(ffmpegLaunchArgs);
            ffmpegProcess = pBuilder.start();

            final Process ytdlProcessF = ytdlProcess;
            final Process ffmpegProcessF = ffmpegProcess;

            ytdlToFFmpegThread = new Thread("RemoteSource ytdlToFFmpeg Bridge") {
                @Override
                public void run() {
                    InputStream fromYTDL = null;
                    OutputStream toFFmpeg = null;
                    try {
                        fromYTDL = ytdlProcessF.getInputStream();
                        toFFmpeg = ffmpegProcessF.getOutputStream();

                        byte[] buffer = new byte[1024];
                        int amountRead = -1;
                        while (!isInterrupted() && ((amountRead = fromYTDL.read(buffer)) > -1)) {
                            toFFmpeg.write(buffer, 0, amountRead);
                        }
                        toFFmpeg.flush();
                    } catch (IOException e) {
                        //If the pipe being closed caused this problem, it was because it tried to write when it closed.
                        String msg = e.getMessage().toLowerCase();
                        if (e.getMessage().contains("The pipe has been ended") || e.getMessage().contains("Broken pipe"))
                            App.logger.error("RemoteStream encountered an 'error' : " + e.getMessage() + " (not really an error.. probably)");
                        else
                            e.printStackTrace();
                    } finally {
                        try {
                            if (fromYTDL != null)
                                fromYTDL.close();
                        } catch (Throwable e) {
                        }
                        try {
                            if (toFFmpeg != null)
                                toFFmpeg.close();
                        } catch (Throwable e) {
                        }
                    }
                }
            };

            ytdlErrGobler = new Thread("RemoteStream ytdlErrGobler") {
                @Override
                public void run() {
                    InputStream fromYTDL = null;
                    try {
                        fromYTDL = ytdlProcessF.getErrorStream();
                        if (fromYTDL == null)
                            App.logger.error("RemoteStream: YTDL-ErrGobler: fromYTDL is null");

                        byte[] buffer = new byte[1024];
                        int amountRead = -1;
                        while (!isInterrupted() && ((amountRead = fromYTDL.read(buffer)) > -1)) {
                            App.logger.warn("ERR YTDL: " + new String(Arrays.copyOf(buffer, amountRead)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fromYTDL != null)
                                fromYTDL.close();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            };

            ffmpegErrGobler = new Thread("RemoteStream ffmpegErrGobler") {
                @Override
                public void run() {
                    InputStream fromFFmpeg = null;
                    try {
                        fromFFmpeg = ffmpegProcessF.getErrorStream();
                        if (fromFFmpeg == null)
                            App.logger.error("RemoteStream: FFmpeg-ErrGobler: fromYTDL is null");

                        byte[] buffer = new byte[1024];
                        int amountRead = -1;
                        while (!isInterrupted() && ((amountRead = fromFFmpeg.read(buffer)) > -1)) {
                            String info = new String(Arrays.copyOf(buffer, amountRead));
                            /*if (info.contains("time=")) {
                                Matcher m = TIME_PATTERN.matcher(info);
                                if (m.find()) {
                                    timestamp = AudioTimestamp.fromFFmpegTimestamp(m.group());
                                }
                            }*/
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fromFFmpeg != null)
                                fromFFmpeg.close();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            };

            ytdlToFFmpegThread.start();
            ytdlErrGobler.start();
            ffmpegErrGobler.start();
            return new AudioTrack(ffmpegProcess.getInputStream(), url, shouldAnnounce, title, user);
        } catch (IOException e) {
            e.printStackTrace();
            /*try {
                close();
            } catch (IOException e1) {
                e.printStackTrace();
            }*/
        }
        return null;
    }

    public boolean isLive() {
        return isLiveStream;
    }
}