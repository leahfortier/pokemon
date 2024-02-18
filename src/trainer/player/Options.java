package trainer.player;

import sound.SoundPlayer;
import util.serialization.Serializable;

public class Options implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final float DEFAULT_ANIMATION_SPEED = 1f;
    private static final float FAST_ANIMATION_SPEED = 10f;

    private boolean isMuted;
    private boolean battleMusic;
    private float animationSpeed;

    public Options() {
        // Default to same options as settings
        isMuted = SoundPlayer.instance().isMuted();
        System.out.println("options create new: " + this.isMuted);
        battleMusic = true;
        animationSpeed = DEFAULT_ANIMATION_SPEED;
    }

    public float getAnimationSpeed() {
        return this.animationSpeed;
    }

    public void toggleMuted() {
        this.isMuted = !this.isMuted;
        SoundPlayer.instance().toggleMusic();
    }

    public boolean isMuted() {
        return this.isMuted;
    }
}
