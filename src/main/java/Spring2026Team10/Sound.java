package Spring2026Team10;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Handles the loading and playback of all audio in game.
 * <p>
 *     Uses the javax.sound.sampled API to load .wav files into memory and control their playback.
 *     Can be used to play, stop, and loop sounds.
 * </p>
 */
public class Sound {
    Clip clip;
    URL soundURL[] = new URL[30];

    /**
     * Constructs a new Sound object and initializes the audio file directory.
     * Loads all file paths for all in game audio assists into an array, mapping each sound to a specific integer index.
     */
    public Sound() {
        soundURL[0] = getClass().getResource("/Sounds/Background.wav");
        soundURL[1] = getClass().getResource("/Sounds/Bear.wav");
        soundURL[2] = getClass().getResource("/Sounds/Coin_Pickup.wav");
        soundURL[3] = getClass().getResource("/Sounds/Damage.wav");
        soundURL[4] = getClass().getResource("/Sounds/Drink.wav");
        soundURL[5] = getClass().getResource("/Sounds/Game_Win.wav");
        soundURL[6] = getClass().getResource("/Sounds/Handcuffs.wav");
        soundURL[7] = getClass().getResource("/Sounds/Game_Over.wav");
        soundURL[8] = getClass().getResource("/Sounds/Button_click.wav");
    }

    /**
     * Loads a specific audio file into the clip.
     * Prints an error message if the file is missing.
     * @param i The index of the audio file in the SoundURL array.
     */
    public void setFile(int i) {
        try {
            if (soundURL[i] == null) {
                System.err.println("Audio file missing for index: " + i);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays the currently loaded audio clip.
     */
    public void play() {
        if (clip != null) {
            clip.start();
        }
    }

    /**
     * Loops the currently loaded audio clip endlessly
     */
    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Stops the playback of the current audio clip.
     */
    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

}
