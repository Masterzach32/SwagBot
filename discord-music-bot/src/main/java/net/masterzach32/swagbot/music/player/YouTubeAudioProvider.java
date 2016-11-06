package net.masterzach32.swagbot.music.player;

import net.masterzach32.swagbot.App;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.util.audio.processors.VolumeProcessor;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YouTubeAudioProvider implements IAudioProvider, Closeable {

    private volatile AudioInputStream stream;
    private AudioInputStreamProvider provider;
    private VolumeProcessor volumeProcessor;
    private String video_id;

    private Process ytdlProcess;
    private Process ffmpegProcess;

    private Thread ytdlToFFmpegThread;
    private Thread ytdlErrGobler;
    private Thread ffmpegErrGobler;

    public YouTubeAudioProvider(String video_id) throws IOException, UnsupportedAudioFileException {
        stream = null;
        provider = null;
        volumeProcessor = new VolumeProcessor();
        this.video_id = video_id;
    }

    public boolean isReady() {
        if (stream == null)
            synchronized(this) {
                if (stream == null) try {
                    stream = getStream();
                    provider = new AudioInputStreamProvider(stream);
                    volumeProcessor.setProvider(provider);
                } catch (IOException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        return provider.isReady();
    }

    public byte[] provide() {
        if (stream == null)
            synchronized(this) {
                if (stream == null) try {
                    stream = getStream();
                    provider = new AudioInputStreamProvider(stream);
                    volumeProcessor.setProvider(provider);
                } catch (IOException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        return provider.provide();
    }

    public int getChannels() {
        return stream.getFormat().getChannels();
    }

    public AudioEncodingType getAudioEncodingType() {
        return AudioEncodingType.PCM;
    }

    public void setVolume(float vol) {
        volumeProcessor.setVolume(vol);
    }

    public AudioInputStream getStream() throws IOException, UnsupportedAudioFileException {
        List<String> ytdl = new ArrayList<>();
        ytdl.add("python");
        ytdl.add("./bin/youtube-dl");
        ytdl.add("-q"); //quiet. No standard out.
        ytdl.add("-f"); //Format to download. Attempts best audio-only, followed by best video/audio combo
        ytdl.add("bestaudio/best");
        ytdl.add("--no-playlist"); //If the provided link is part of a Playlist, only grabs the video, not playlist too.
        ytdl.add("-4"); //Forcing Ipv4 for OVH's Ipv6 range is blocked by youtube
        ytdl.add("--no-cache-dir"); //We don't want random screaming
        ytdl.add("-o"); //Output, output to STDout
        ytdl.add("-");

        List<String> ffmpeg = new ArrayList<>();
        ffmpeg.add("ffmpeg");
        ffmpeg.add("-i"); //Input file, specifies to read from STDin (pipe)
        ffmpeg.add("-");
        ffmpeg.add("-vcodec");
        ffmpeg.add("mp4");
        ffmpeg.add("-map"); //Makes sure to only output audio, even if the specified format supports other streams
        ffmpeg.add("a");
        ffmpeg.add("-f"); // format to mp3
        ffmpeg.add("mp3");
        ffmpeg.add("-loglevel"); // don't print anything to the console
        ffmpeg.add("quiet");
        ffmpeg.add("-"); //Used to specify STDout as the output location (pipe)

        ProcessBuilder pBuilder = new ProcessBuilder();

        ytdl.add("--");
        ytdl.add(video_id);
        pBuilder.command(ytdl);
        ytdlProcess = pBuilder.start();

        pBuilder.command(ffmpeg);
        ffmpegProcess = pBuilder.start();

        final Process ytdlProcessF = ytdlProcess;
        final Process ffmpegProcessF = ffmpegProcess;

        ytdlToFFmpegThread = new Thread("ytdlToFFmpeg Bridge") {
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

        ytdlErrGobler = new Thread("ytdlErrGobler") {
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

        ffmpegErrGobler = new Thread("ffmpegErrGobler") {
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
        return AudioSystem.getAudioInputStream(ffmpegProcessF.getInputStream());
    }

    public void close() {
        try {
            if(stream != null)
                stream.close();
            if(ytdlProcess != null)
                ytdlProcess.destroyForcibly();
            if(ffmpegProcess != null)
                ffmpegProcess.destroyForcibly();
            if(ytdlToFFmpegThread != null)
                ytdlToFFmpegThread.join();
            if(ytdlErrGobler != null)
                ytdlErrGobler.join();
            if(ffmpegErrGobler != null)
                ffmpegErrGobler.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}