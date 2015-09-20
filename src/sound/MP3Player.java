package sound;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import main.FileIO;
import main.Global;

public class MP3Player extends Thread 
{
	private String mp3FileName;
	private Player player;
	private boolean loop;
	
	public MP3Player(String mp3FileName)
	{
		this.mp3FileName = mp3FileName;
		loop = false;
		
		try 
		{
			player = new Player(loadMP3File(mp3FileName));
		}
		catch (JavaLayerException e) {}
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
				player.play();

				if (this.loop)
				{
					player = new Player(loadMP3File(mp3FileName));
				}
			}
			catch (JavaLayerException e) 
			{
				break;
			}
		} while (this.loop);
	}
	
	private BufferedInputStream loadMP3File(String fileName) 
	{
		fileName += ".mp3";
		
		BufferedInputStream mp3 = null;
		try 
		{
			FileInputStream fis = new FileInputStream(FileIO.makePath("rec", "snd") + fileName);
			mp3 = new BufferedInputStream(fis);
		}
		catch (FileNotFoundException e) 
		{
			Global.error("Failed to load " + fileName + ":\n" + e);
		}
		
		return mp3;
	}
	
	public void close() 
	{
		loop = false;
		player.close();
		this.interrupt();
	}
}
