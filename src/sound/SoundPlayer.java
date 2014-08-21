package sound;

public class SoundPlayer {
	
	private boolean muted;	
	private String musicName;
	private	MP3Player musicPlayer;
	private MP3Player soundEffectPlayer;
	
	public SoundPlayer() 
	{
		musicName = null;
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
	
	public void playMusic(String music) 
	{
		// If we're trying to play the same song that's already playing
		// don't do anything.
		if (music.equals(musicName))
		{
			return;
		}
		
		if (musicPlayer != null)
		{
			musicPlayer.close();
		}
		
		musicName = music;
		
		if (!muted)
		{
			musicPlayer = new MP3Player(music);
			musicPlayer.setLoop(true);
			musicPlayer.start();
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
	
	public void resumeMusic()
	{
		if (musicPlayer != null)
		{
			musicPlayer.close();
		}
		
		if (musicName != null)
		{
			musicPlayer = new MP3Player(musicName);
			musicPlayer.setLoop(true);
			musicPlayer.start();
		}
	}
	
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
			musicName = null;
		}
	}
	
	public void playSoundEffect(String soundEffect) 
	{
		if (soundEffectPlayer != null)
		{
			soundEffectPlayer.close();
		}
		
		soundEffectPlayer = new MP3Player(soundEffect);
		soundEffectPlayer.setLoop(false);
		soundEffectPlayer.start();
	}
	
	public boolean soundEffectIsPlaying() 
	{
		return soundEffectPlayer != null && soundEffectPlayer.isAlive();
	}
}
