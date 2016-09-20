package net.masterzach32.swagbot.music;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.Constants;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class YouTubeAudio implements AudioSource {

    private String url, name, video_id;

    public YouTubeAudio(String url) throws UnirestException {
        this.url = url;
        video_id = url.substring(url.indexOf("?v=") + 3, url.indexOf("=") + 12);
        name = Unirest.get("https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet" +
                "&id=" + video_id +
                "&key=" + App.prefs.getGoogleAuthKey()).asJson().getBody().getArray().getJSONObject(0).getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("title");
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
            yt_err = yt_dn.redirectOutput(new File(Constants.LOG_STORAGE + "youtube-dl.log")).start().waitFor();
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
                ffmpeg = new ProcessBuilder(Constants.BINARY_STORAGE + "ffmpeg.exe", "-i", file.toString(), Constants.AUDIO_CACHE + file.getName().substring(0, file.getName().indexOf(video_id) + 11) + ".mp3");
                yt = file;
            }
        try {
            ffmpeg_err = ffmpeg.redirectOutput(new File(Constants.LOG_STORAGE + "ffmpeg.log")).start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        App.logger.info("ffmpeg:" + video_id + " exit:" + ffmpeg_err);
        if(ffmpeg_err != 0)
            throw new FFMPEGException(yt, url, ffmpeg_err);

        if (yt != null)
            yt.delete();

        return new AudioTrack(new File(Constants.AUDIO_CACHE + name + "-" + video_id + ".mp3"), url, shouldAnnounce, name, user);
    }
}