package sound;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import main.Global;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MP3Player extends Thread 
{
	private String mp3FileName;
	private Player player;
	private boolean loop;
	
	public MP3Player(String mp3FileName)
	{
		this.mp3FileName = mp3FileName;
		loop = false;
		player = null;
	}
		
	public void setLoop(boolean loop)
	{
		this.loop = loop;
	}
	
	public void run() 
	{	
		do 
		{
			try 
			{
				player = new Player(loadMP3File(mp3FileName));
				player.play();
			}
			catch (JavaLayerException e) 
			{
				break;
			}
		} while (this.loop);
	}
	
	private BufferedInputStream loadMP3File(String fileName) 
	{
		BufferedInputStream bis = null;
		try 
		{
			FileInputStream fis = new FileInputStream("rec" + Global.FILE_SLASH + "snd" + Global.FILE_SLASH + fileName + ".mp3");
			bis = new BufferedInputStream(fis);
		}
		catch (FileNotFoundException e) 
		{
			Global.error("Failed to load " + fileName + ".mp3:\n" + e);
		}
		
		return bis;
	}
	
	public void close() 
	{
		loop = false;
		player.close();
		this.interrupt();
	}
}
