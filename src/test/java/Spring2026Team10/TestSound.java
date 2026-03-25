package Spring2026Team10;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestSound {
    private Sound sound;

    @Test
    public void testNullClip() {
        //Null audio file should be ignored to prevent blocking state changes
        sound = new Sound();
        assertDoesNotThrow(() -> {
            sound.play();
            sound.loop();
            sound.stop();
        }, "Null audio files should be safely ignored");

        assertNull(sound.clip, "Audio Clip should be null");
    }

    @Test
    public void testPlayback() {
        //Test setting a valid audio file and playback
        sound = new Sound();
        assertDoesNotThrow(() -> {
            sound.setFile(8);
            //verify clip was loaded
            assertNotNull(sound.clip, "Audio Clip should be loaded/ initialized");

            sound.play();
            sound.loop();
            sound.stop();
        }, "Loading and playing a valid audio file should not throw execptions");
    }

    @Test
    public void testInvalidIndex() {
        sound = new Sound();
        sound.setFile(25);
        assertNull(sound.clip, "Audio Clip should be null");
    }



}
