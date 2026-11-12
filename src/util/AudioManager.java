package util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class AudioManager {
    private static final Map<String, Clip> SFX = new HashMap<>();
    private static Clip music;

    private static Clip load(String path) {
        try (InputStream in = AudioManager.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("Audio nao encontrado: " + path);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            System.err.println("Erro ao carregar " + path + ": " + e);
            return null;
        }
    }

    public static void playMusic(String path) {
        stopMusic();
        music = load(path);
        if (music != null) music.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void stopMusic() {
        if (music != null) {
            music.stop();
            music.close();
            music = null;
        }
    }

    public static void playSound(String path) {
        Clip c = SFX.get(path);
        if (c == null) {
            c = load(path);
            if (c == null) return;
            SFX.put(path, c);
        }
        c.stop();
        c.setFramePosition(0);
        c.start();
    }
}
