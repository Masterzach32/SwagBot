package net.masterzach32.swagbot.music.player;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.player.AudioSource;
import net.masterzach32.swagbot.music.player.AudioTrack;
import net.masterzach32.swagbot.utils.Constants;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

public class YouTubeAudio implements AudioSource {

    private String url, name, video_id;
    private boolean isLiveStream;

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
                ffmpeg = new ProcessBuilder(Constants.BINARY_STORAGE + "ffmpeg.exe", "-i", file.toString(), Constants.AUDIO_CACHE + file.getName().substring(0, file.getName().indexOf(video_id) + 11) + ".mp3")
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
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
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