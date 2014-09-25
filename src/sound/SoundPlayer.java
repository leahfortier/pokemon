package sound;

public class SoundPlayer 
{
	private boolean muted;	
	private SoundTitle music;
	private	MP3Player musicPlayer;
	private MP3Player soundEffectPlayer;
	
	public SoundPlayer() 
	{
		music = null;
		musicPlayer = null;
		soundEffectPlayer = null;
	}
	
	public boolean isMuted() 
	{
		return muted;
	}
	
	public void setMuted(boolean muted)
	{
		this.muted = muted;
	}
	
	public void playMusic(SoundTitle newMusic) 
	{
		// If we're trying to play the same song that's already playing
		// don't do anything.
		if (music == newMusic)
		{
			return;
		}
		
		if (musicPlayer != null)
		{
			musicPlayer.close();
		}
		
		music = newMusic;
		
		if (!muted)
		{
			createPlayer(music);
		}
	}
	
	public void pauseMusic() 
	{
		if (musicPlayer != null)
		{
			musicPlayer.close();
			musicPlayer = null;
		}
	}
	
	private void createPlayer(SoundTitle music)
	{
		MP3Player player = new MP3Player(music.getSoundTitle());
		player.setLoop(music.isMusic());
		player.start();
		
		if (music.isMusic())
		{
			musicPlayer = player;
		}
		else
		{
			soundEffectPlayer = player;
		}
	}
	
	public void resumeMusic()
	{
		if (musicPlayer != null)
		{
			musicPlayer.close();
		}
		
		if (music != null)
		{
			createPlayer(music);
		}
	}
	
	// Toggly Boggly
	public void toggleMusic()
	{
		if (muted)
			resumeMusic();
		else
			pauseMusic();
		
		muted = !muted;
	}
	
	public void stopSong() 
	{
		if (musicPlayer != null)
		{
			musicPlayer.close();
			musicPlayer = null;
			music = null;
		}
	}
	
	public void playSoundEffect(SoundTitle soundEffect) 
	{
		if (soundEffectPlayer != null)
		{
			soundEffectPlayer.close();
		}

		createPlayer(soundEffect);
	}
	
	public boolean soundEffectIsPlaying() 
	{
		return soundEffectPlayer != null && soundEffectPlayer.isAlive();
	}
}
