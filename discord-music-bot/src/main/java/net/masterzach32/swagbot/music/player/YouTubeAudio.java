package net.masterzach32.swagbot.music.player;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.player.source.RemoteSource;
import net.dv8tion.jda.player.source.RemoteStream;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.player.AudioSource;
import net.masterzach32.swagbot.music.player.AudioTrack;
import net.masterzach32.swagbot.utils.Constants;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static net.dv8tion.jda.player.source.AudioStream.TIME_PATTERN;

public class YouTubeAudio implements AudioSource {

    private String url, name, video_id;
    private boolean isLiveStream;

    public static List<String> FFMPEG_LAUNCH_ARGS =
            Arrays.asList(
                    "ffmpeg",       //Program launch
                    "-i", "-",      //Input file, specifies to read from STDin (pipe)
                    "-f", "s16be",  //Format.  PCM, signed, 16bit, Big Endian
                    "-ac", "2",     //Channels. Specify 2 for stereo audio.
                    "-ar", "48000", //Rate. Opus requires an audio rate of 48000hz
                    "-map", "a",    //Makes sure to only output audio, even if the specified format supports other streams
                    "-"             //Used to specify STDout as the output location (pipe)
            );

    public YouTubeAudio(String url) throws UnirestException {
        this.url = url;
        video_id = url.substring(url.indexOf("?v=") + 3, url.indexOf("=") + 12);
        HttpResponse<JsonNode> response =  Unirest.get("https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet" +
                "&id=" + video_id +
                "&key=" + App.prefs.getGoogleAuthKey()).asJson();
        if(response.getStatus() == 200) {
            JSONObject json = response.getBody().getArray().getJSONObject(0).getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
            name = json.getString("title");
            isLiveStream = json.getString("liveBroadcastContent").equals("live");
        } else
            App.logger.warn("Youtube Data API responded with status code " + response.getStatus() + " for video id " + video_id);

    }

    public String getTitle() {
        return name;
    }

    public String getSource() {
        return "youtube";
    }

    public String getUrl() {
        return url;
    }

    public String getStreamUrl() {
        return null;
    }

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException {
        for (File file : App.manager.getFile(Constants.AUDIO_CACHE).listFiles())
            if (file.getName().contains(video_id))
                return new AudioTrack(file, url, shouldAnnounce, name, user);

        /*try {
            Process ytdlProcess = null, ffmpegProcess = null;
            ProcessBuilder pBuilder = new ProcessBuilder();
            List<String> YOUTUBE_DL_LAUNCH_ARGS =
                    Arrays.asList(
                            "py",               //Launch python executor
                            "./bin/youtube-dl",         //youtube-dl program file
                            "-q",                   //quiet. No standard out.
                            "-f", "bestaudio/best", //Format to download. Attempts best audio-only, followed by best video/audio combo
                            "--no-playlist",        //If the provided link is part of a Playlist, only grabs the video, not playlist too.
                            "-4",                   //Forcing Ipv4 for OVH's Ipv6 range is blocked by youtube
                            "--no-cache-dir",       //We don't want random screaming
                            "-o", "-",               //Output, output to STDout
                            "--", url
                    );

            pBuilder.command(YOUTUBE_DL_LAUNCH_ARGS);
            ytdlProcess = pBuilder.start();

            pBuilder.command(FFMPEG_LAUNCH_ARGS);
            ffmpegProcess = pBuilder.start();

            final Process ytdlProcessF = ytdlProcess;
            final Process ffmpegProcessF = ffmpegProcess;

            Thread ytdlToFFmpegThread = new Thread("RemoteSource ytdlToFFmpeg Bridge") {
                @Override
                public void run() {
                    InputStream fromYTDL = null;
                    OutputStream toFFmpeg = null;
                    try {
                        fromYTDL = ytdlProcessF.getInputStream();
                        toFFmpeg = ffmpegProcessF.getOutputStream();

                        byte[] buffer = new byte[1024];
                        int amountRead = -1;
                        while (!isInterrupted() && ((amountRead = fromYTDL.read(buffer)) > -1))
                        {
                            toFFmpeg.write(buffer, 0, amountRead);
                        }
                        toFFmpeg.flush();
                    } catch (IOException e) {
                        //If the pipe being closed caused this problem, it was because it tried to write when it closed.
                        String msg = e.getMessage().toLowerCase();
                        if (e.getMessage().contains("The pipe has been ended") || e.getMessage().contains("Broken pipe"))
                            App.logger.trace("RemoteStream encountered an 'error' : " + e.getMessage() + " (not really an error.. probably)");
                        else
                            App.logger.trace(e.toString());
                    } finally {
                        try {
                            if (fromYTDL != null)
                                fromYTDL.close();
                        } catch (IOException e) {}
                        try {
                            if (toFFmpeg != null)
                                toFFmpeg.close();
                        } catch (IOException e) {}
                    }
                }
            };

            Thread ytdlErrGobler = new Thread("RemoteStream ytdlErrGobler") {
                @Override
                public void run() {
                    try {
                        InputStream fromYTDL = null;

                        fromYTDL = ytdlProcessF.getErrorStream();
                        if (fromYTDL == null)
                            App.logger.error("RemoteStream: YTDL-ErrGobler: fromYTDL is null");

                        byte[] buffer = new byte[1024];
                        int amountRead = -1;
                        while (!isInterrupted() && ((amountRead = fromYTDL.read(buffer)) > -1)) {
                            App.logger.warn("ERR YTDL: " + new String(Arrays.copyOf(buffer, amountRead)));
                        }
                    } catch (IOException e) {
                        App.logger.trace(e.toString());
                    }
                }
            };

            Thread ffmpegErrGobler = new Thread("RemoteStream ffmpegErrGobler") {
                @Override
                public void run() {
                    try {
                        InputStream fromFFmpeg = null;

                        fromFFmpeg = ffmpegProcessF.getErrorStream();
                        if (fromFFmpeg == null)
                            App.logger.error("RemoteStream: FFmpeg-ErrGobler: fromYTDL is null");

                        byte[] buffer = new byte[1024];
                        int amountRead = -1;
                        while (!isInterrupted() && ((amountRead = fromFFmpeg.read(buffer)) > -1)) {
                            String info = new String(Arrays.copyOf(buffer, amountRead));
                            if (info.contains("time=")) {
                                Matcher m = TIME_PATTERN.matcher(info);
                            }
                        }
                    } catch (IOException e) {
                        App.logger.trace(e.toString());
                    }
                }
            };

            ytdlToFFmpegThread.start();
            ytdlErrGobler.start();
            ffmpegErrGobler.start();
            return new AudioTrack(ffmpegProcess.getInputStream(), url, shouldAnnounce, getTitle(), user);
        } catch (IOException e) {
            App.logger.trace(e.toString());
        }*/

        App.logger.info("downloading:" + video_id);
        ProcessBuilder yt_dn = new ProcessBuilder("py", Constants.BINARY_STORAGE + "youtube-dl", url);
        int yt_err = -1;

        try {
            final Process process = yt_dn.start();
            final StringWriter writer = new StringWriter();

            new Thread(() -> {
                try {
                    IOUtils.copy(process.getInputStream(), writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            yt_err = process.waitFor();
            final String processOutput = writer.toString();
            App.logger.trace(processOutput);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        App.logger.info("youtube-dl:" + video_id + " exit:" + yt_err);
        if(yt_err != 0)
            throw new YouTubeDLException(url, yt_err);

        File yt = null;
        ProcessBuilder ffmpeg = null;
        int ffmpeg_err = -1;
        for (File file : App.manager.getFile(Constants.WORKING_DIRECTORY).listFiles())
            if (file.getName().contains(video_id)) {
                ffmpeg = new ProcessBuilder(Constants.BINARY_STORAGE + "ffmpeg.exe", "-i", file.toString(), Constants.AUDIO_CACHE + video_id + ".mp3")
                        .redirectErrorStream(true);
                yt = file;
            }
        try {
            final Process process = ffmpeg.start();
            InputStream is = process.getInputStream();
            String processOutput = convertStreamToStr(is);
            ffmpeg_err = process.waitFor();
            App.logger.trace(processOutput);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        App.logger.info("ffmpeg:" + video_id + " exit:" + ffmpeg_err);
        if(ffmpeg_err != 0)
            throw new FFMPEGException(yt, url, ffmpeg_err);

        if (yt != null)
            yt.delete();

        for (File file : App.manager.getFile(Constants.AUDIO_CACHE).listFiles())
            if (file.getName().contains(video_id))
                return new AudioTrack(file, url, shouldAnnounce, name, user);
        return null;
    }

    public boolean isLive() {
        return isLiveStream;
    }

    public static String convertStreamToStr(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
        else {
            return "";
        }
    }
}