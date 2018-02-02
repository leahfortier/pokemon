package sound;

public class SoundPlayer {
    private static SoundPlayer instance;
    public static SoundPlayer instance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }

        return instance;
    }

    private boolean muted;
    private SoundTitle music;
    private MP3Player musicPlayer;
    private MP3Player soundEffectPlayer;

    private SoundPlayer() {
        music = null;
        musicPlayer = null;
        soundEffectPlayer = null;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void playSound(SoundTitle sound) {
        if (sound.isMusic()) {
            this.playMusic(sound);
        } else {
            this.playSoundEffect(sound);
        }
    }

    public void playMusic(SoundTitle newMusic) {

        // If we're trying to play the same song that's already playing don't do anything.
        if (music == newMusic) {
            return;
        }

        if (musicPlayer != null) {
            musicPlayer.close();
        }

        music = newMusic;

        if (!muted) {
            createPlayer(music);
        }
    }

    private void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.close();
            musicPlayer = null;
        }
    }

    private void createPlayer(SoundTitle music) {
        MP3Player player = new MP3Player(music.getSoundTitle());
        player.setLoop(music.isMusic());
        player.start();

        if (music.isMusic()) {
            musicPlayer = player;
        } else {
            soundEffectPlayer = player;
        }
    }

    private void resumeMusic() {
        if (musicPlayer != null) {
            musicPlayer.close();
        }

        if (music != null) {
            createPlayer(music);
        }
    }

    // Toggly Boggly
    public void toggleMusic() {
        if (muted) {
            resumeMusic();
        } else {
            pauseMusic();
        }

        muted = !muted;
    }

    public void stopSong() {
        if (musicPlayer != null) {
            musicPlayer.close();
            musicPlayer = null;
            music = null;
        }
    }

    public void playSoundEffect(SoundTitle soundEffect) {
        if (soundEffectPlayer != null) {
            soundEffectPlayer.close();
        }

        // SRSLY
        if (!muted) {
            createPlayer(soundEffect);
        }
    }

    public boolean soundEffectIsPlaying() {
        return soundEffectPlayer != null && soundEffectPlayer.isAlive();
    }
}
