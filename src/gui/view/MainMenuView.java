package gui.view;

import gui.Button;
import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;

public class MainMenuView extends View
{
	private enum VisualState
	{
		MAIN, LOAD, NEW, OPTIONS, CREDITS
	};

	private VisualState state;

	private enum Theme
	{
		BASIC, SCENIC
	};

	private Theme theme;

	private String[] mainStrings = { "Load Game", "New Game", "Options", "Quit" };
	private String[] optionsStrings = { "Theme", "Mute", "Credits", "Return" };
	private Button[] mainButtons, loadButtons, newButtons, optionsButtons;
	private int selectedButton;
	private boolean deletePressed;
	private boolean musicStarted = false;

	private int bgTime, bgIndex;
	private int[] bgt = new int[] { 800, 1800, 1000, 2400, 1000 };
	private int[] bgx = new int[] { -300, -400, -800, -800, -200 };
	private int[] bgy = new int[] { -300, -130, -130, -500, -500 };

	private int creditsTime1, creditsTime2;
	private String creditsHoz = "TEAM ROCKET    TEAM ROCKET";
	private String[] creditsText = { "", "Team Rocket", "", "Lead Programmers", "Leah Fortier", "Tyler Brazill", "Maxwell Miller", "",
			"", "Graphic Designers", "Josh Linge", "Jeb Ralston", "Jessica May", "", "Writers", "Jeb Ralston", "Jessica May", "", 
			"UML MASTER", "Jeb Ralston", "", "Nintendo", "", "Game Freak", "", "credits", "credits", "credits" };

	private class SaveInfo
	{
		private String name;
		private long time;
		private int badges, pokemon;

		public SaveInfo(String name, long time, int badges, int pokemon)
		{
			this.name = name;
			this.time = time;
			this.badges = badges;
			this.pokemon = pokemon;
		}
	}

	private SaveInfo[] saveInfo;

	public MainMenuView()
	{
		selectedButton = creditsTime1 = creditsTime2 = 0;
		bgTime = bgIndex = 0;
		deletePressed = false;
		// theme = Theme.SCENIC;

		loadSettings();

		updateSaveData();

		mainButtons = new Button[4];
		for (int i = 0; i < 4; i++)
			// r u l d
			mainButtons[i] = new Button(200, 240 + i * 85, 400, 75, Button.HoverAction.BOX, new int[] { -1, i == 0 ? 3 : i - 1, -1, i == 3 ? 0 : i + 1 });

		loadButtons = new Button[5];
		for (int i = 0; i < 3; i++)
			loadButtons[i] = new Button(200, 240 + i * 85, 400, 75, Button.HoverAction.BOX, new int[] { -1, i == 0 ? 4 : i - 1, -1, i + 1 });
		loadButtons[3] = new Button(200, 495, 195, 75, Button.HoverAction.BOX, new int[] { 4, 2, -1, 0 });
		loadButtons[4] = new Button(405, 495, 195, 75, Button.HoverAction.BOX, new int[] { -1, 2, 3, 0 });

		newButtons = new Button[4];
		for (int i = 0; i < 4; i++)
			newButtons[i] = new Button(200, 240 + i * 85, 400, 75, Button.HoverAction.BOX, new int[] { -1, i == 0 ? 3 : i - 1, -1, i == 3 ? 0 : i + 1 });

		optionsButtons = new Button[4];
		for (int i = 0; i < 4; i++)
			optionsButtons[i] = new Button(200, 240 + i * 85, 400, 75, Button.HoverAction.BOX, new int[] { -1, i == 0 ? 3 : i - 1, -1, i == 3 ? 0 : i + 1 });
		state = VisualState.MAIN;
		selectedButton = creditsTime1 = creditsTime2 = 0;
	}

	private void setVisualState(VisualState s)
	{
		state = s;
		selectedButton = creditsTime1 = creditsTime2 = 0;
		switch (state)
		{
			case CREDITS:
				break;
			case LOAD:
				for (Button b : loadButtons)
					b.setForceHover(false);
				break;
			case MAIN:
				for (Button b : mainButtons)
					b.setForceHover(false);
				break;
			case NEW:
				for (Button b : newButtons)
					b.setForceHover(false);
				break;
			case OPTIONS:
				for (Button b : optionsButtons)
					b.setForceHover(false);
				break;
			default:
				break;
		}
		
		switch (state)
		{
			case CREDITS:
				Global.soundPlayer.playMusic("doubletrouble");
				break;
			default:
				Global.soundPlayer.playMusic("dancemix");
				break;
		}
	}

	public void update(int dt, InputControl input, Game game)
	{
		if (!musicStarted){
			musicStarted = true;
			Global.soundPlayer.playMusic("dancemix");
		}
		int pressed = -1;
		switch (state)
		{
			case MAIN:
				selectedButton = Button.update(mainButtons, selectedButton, input);
				if (mainButtons[selectedButton].checkConsumePress())
				{
					pressed = selectedButton;
				}
				
				switch (pressed)
				{
					case 0: // load
						setVisualState(VisualState.LOAD);
						break;
					case 1: // new
						setVisualState(VisualState.NEW);
						break;
					case 2: // options
						setVisualState(VisualState.OPTIONS);
						break;
					case 3: // quit
						System.exit(0);
						break;
					default:
						break;
				}
				break;
			case LOAD:
				selectedButton = Button.update(loadButtons, selectedButton, input);
				if (loadButtons[selectedButton].checkConsumePress())
				{
					pressed = selectedButton;
				}
				
				switch (pressed)
				{
					case 0: // load
					case 1:
					case 2:
						if (deletePressed && saveInfo[pressed] != null)
						{
							deletePressed = false;
							game.deleteSave(pressed); // TODO ask to delete
														// first
							System.out.println("deleted");
							updateSaveData();
						}
						else if (saveInfo[pressed] != null)
						{
							game.loadSave(pressed);
							game.setViewMode(ViewMode.MAP_VIEW);
						}
						break;
					case 3: // return
						setVisualState(VisualState.MAIN);
						break;
					case 4: // delete
						deletePressed = true;
						System.out.println("pressed delete");
						break;
					default:
						break;
				}
				
				if (input.isDown(Control.BACK))
				{
					input.consumeKey(Control.BACK);
					setVisualState(VisualState.MAIN);
				}
				break;
			case NEW:
				selectedButton = Button.update(newButtons, selectedButton, input);
				if (newButtons[selectedButton].checkConsumePress())
				{
					pressed = selectedButton;
				}
				
				switch (pressed)
				{
					case 0: // new
					case 1:
					case 2:
						/*
						 * if (saveInfo[pressed] != null){ //TODO ask to delete
						 * first
						 * 
						 * }else{
						 * 
						 * }
						 */
						game.newSave(pressed);
						game.setViewMode(ViewMode.START_VIEW);
						break;
					case 3: // return
						setVisualState(VisualState.MAIN);
						break;
					default:
						break;
				}
				
				if (input.isDown(Control.BACK))
				{
					input.consumeKey(Control.BACK);
					setVisualState(VisualState.MAIN);
				}
				break;
			case OPTIONS:
				selectedButton = Button.update(optionsButtons, selectedButton, input);
				if (optionsButtons[selectedButton].checkConsumePress())
				{
					pressed = selectedButton;
				}
				
				switch (pressed)
				{
					case 0: // theme
						theme = (theme == Theme.BASIC ? Theme.SCENIC : Theme.BASIC);
						saveSettings();
						break;
					case 1: // mute
						Global.soundPlayer.toggleMusic();
						saveSettings();
						break;
					case 2: // credits
						setVisualState(VisualState.CREDITS);
						break;
					case 3: // return
						setVisualState(VisualState.MAIN);
						break;
					default:
						break;
				}
				
				if (input.isDown(Control.BACK))
				{
					input.consumeKey(Control.BACK);
					setVisualState(VisualState.MAIN);
				}
				break;
			case CREDITS:
				if (input.mouseDown)
				{
					input.consumeMousePress();
					setVisualState(VisualState.MAIN);
				}
				if (input.isDown(Control.SPACE) || input.isDown(Control.BACK))
				{
					if (input.isDown(Control.SPACE)) input.consumeKey(Control.SPACE);
					else input.consumeKey(Control.BACK);
					
					setVisualState(VisualState.MAIN);
				}
				creditsTime1 += 10;
				if (creditsTime1 > 8000) creditsTime1 -= 8000;
				creditsTime2 += 10;
				if (creditsTime2 > 8500) creditsTime2 -= 8500;
				break;
		}

		bgTime += 10;
		if (bgTime > bgt[(bgIndex + 1) % bgt.length])
		{
			bgTime -= bgt[(bgIndex + 1) % bgt.length];
			bgIndex = (bgIndex + 1) % bgt.length;
		}
	}

	public void draw(Graphics g, GameData data)
	{
		TileSet tiles = data.getMainMenuTiles();

		Color themeColor;
		switch (theme)
		{
			case BASIC:
				themeColor = new Color(255, 210, 86);
				g.setColor(new Color(68, 123, 184));
				g.fillRect(0, 0, 800, 600);
				g.drawImage(tiles.getTile(0x01), 0, 0, null);
				break;
			case SCENIC:
				themeColor = new Color(68, 123, 184);
				float locRatio = 1.0f - (float) bgTime / (float) bgt[(bgIndex + 1) % bgt.length];
				int xLoc = (int) (bgx[bgIndex] * locRatio + (1.0f - locRatio) * bgx[(bgIndex + 1) % bgt.length]);
				int yLoc = (int) (bgy[bgIndex] * locRatio + (1.0f - locRatio) * bgy[(bgIndex + 1) % bgt.length]);
				;
				g.drawImage(tiles.getTile(0x06), xLoc, yLoc, null);
				g.drawImage(tiles.getTile(0x02), 0, 0, null);
				break;
			default:
				themeColor = Color.white;
				break;
		}

		g.drawImage(tiles.getTile(0x03), 95, 54, null);

		switch (state)
		{
			case MAIN:
				g.setFont(Global.getFont(40));
				for (int i = 0; i < 4; i++)
				{
					g.translate(mainButtons[i].x, mainButtons[i].y);

					g.setColor(themeColor);
					g.fillRect(0, 0, mainButtons[i].width, mainButtons[i].height);

					g.drawImage(tiles.getTile(0x04), 0, 0, null);

					g.setColor(Color.black);
					g.drawString(mainStrings[i], 200 - 20*mainStrings[i].length()/2, 50);

					g.translate(-mainButtons[i].x, -mainButtons[i].y);
				}

				for (Button b : mainButtons)
					b.draw(g);
				break;
			case LOAD:
				for (int i = 0; i < 3; i++)
				{
					g.translate(loadButtons[i].x, loadButtons[i].y);
					
					g.setColor(themeColor);
					g.fillRect(0, 0, loadButtons[i].width, loadButtons[i].height);
					g.drawImage(tiles.getTile(0x04), 0, 0, null);

					g.setColor(Color.black);
					SaveInfo info = saveInfo[i];
					if (info != null)
					{
						g.setFont(Global.getFont(20));

						g.drawString("Name:", 16, 28);
						g.drawString(info.name, 189 - 10 * info.name.length(), 28);

						g.drawString("Time:", 210, 28);
						String timeStr = "" + formatTime(info.time);
						g.drawString(timeStr, 383 - 10 * timeStr.length(), 28);

						g.drawString("Badges:", 16, 58);
						String badgesStr = "" + info.badges;
						g.drawString(badgesStr, 189 - 10 * badgesStr.length(), 58);

						g.drawString("Pokedex:", 210, 58);
						String pokemonStr = "" + info.pokemon;
						g.drawString(pokemonStr, 383 - 10 * pokemonStr.length(), 58);
					}
					else
					{
						g.setFont(Global.getFont(30));
						g.drawString("Empty", 165, 47);
					}

					g.translate(-loadButtons[i].x, -loadButtons[i].y);
				}

				g.setFont(Global.getFont(30));
				g.setColor(themeColor);
				g.fillRect(200, 495, 195, 75);
				g.drawImage(tiles.getTile(0x05), 200, 495, null);
				g.setColor(Color.BLACK);
				g.drawString("Return", 293 - 50, 541);

				g.setFont(Global.getFont(30));
				g.setColor(themeColor);
				g.fillRect(405, 495, 195, 75);
				g.drawImage(tiles.getTile(0x05), 405, 495, null);
				g.setColor(Color.black);
				g.drawString("Delete", 497 - 50, 541);

				for (Button b : loadButtons)
					b.draw(g);
				break;
			case NEW:
				for (int i = 0; i < 3; i++)
				{
					g.translate(newButtons[i].x, newButtons[i].y);
					g.setColor(themeColor);
					g.fillRect(0, 0, newButtons[i].width, newButtons[i].height);
					g.drawImage(tiles.getTile(0x04), 0, 0, null);

					g.setColor(Color.BLACK);
					SaveInfo info = saveInfo[i];
					if (info != null)
					{
						g.setFont(Global.getFont(20));

						g.drawString("Name:", 16, 28);
						g.drawString(info.name, 189 - 10 * info.name.length(), 28);

						g.drawString("Time:", 210, 28);
						String timeStr = "" + formatTime(info.time);
						g.drawString(timeStr, 383 - 10 * timeStr.length(), 28);

						g.drawString("Badges:", 16, 58);
						String badgesStr = "" + info.badges;
						g.drawString(badgesStr, 189 - 10 * badgesStr.length(), 58);

						g.drawString("Pokedex:", 210, 58);
						String pokemonStr = "" + info.pokemon;
						g.drawString(pokemonStr, 383 - 10 * pokemonStr.length(), 58);
					}
					else
					{
						g.setFont(Global.getFont(30));
						g.drawString("New Save", 140, 47);
					}

					g.translate(-newButtons[i].x, -newButtons[i].y);
				}

				g.setFont(Global.getFont(40));
				g.translate(200, 240 + 3 * 85);
				g.setColor(themeColor);
				g.fillRect(0, 0, 400, 75);
				g.drawImage(tiles.getTile(0x04), 0, 0, null);
				g.setColor(Color.black);
				g.drawString("Return", 200 - 20 * "Return".length() / 2, 50);
				g.translate(-200, -(240 + 3 * 85));

				for (Button b : newButtons)
					b.draw(g);
				break;
			case OPTIONS:
				g.setFont(Global.getFont(40));
				for (int i = 0; i < 4; i++)
				{
					g.translate(optionsButtons[i].x, optionsButtons[i].y);

					g.setColor(themeColor);
					g.fillRect(0, 0, optionsButtons[i].width, optionsButtons[i].height);

					g.drawImage(tiles.getTile(0x04), 0, 0, null);

					g.setColor(Color.black);
					g.drawString(optionsStrings[i], 200 - 20 * optionsStrings[i].length() / 2, 50);

					g.translate(-optionsButtons[i].x, -optionsButtons[i].y);
				}

				for (Button b : optionsButtons)
					b.draw(g);
				break;
			case CREDITS:
				Dimension d = Global.GAME_SIZE;
				g.setClip(0, 220, d.width, d.height - 240);

				g.setColor(new Color(0, 0, 0, 64));
				g.setFont(Global.getFont(512));
				g.drawString(creditsHoz, d.width - creditsTime2, d.height - 30);

				g.setColor(Color.BLACK);
				g.setFont(Global.getFont(40));
				for (int i = 1; i < creditsText.length; i++)
				{
					if (creditsText[i - 1].equals(""))
					{
						g.setFont(Global.getFont(40));
						g.drawString(creditsText[i], d.width/2 - 22*creditsText[i].length()/2, i*40 + d.height - creditsTime1/5);
					}
					else
					{
						g.setFont(Global.getFont(30));
						g.drawString(creditsText[i], d.width/2 - 16 * creditsText[i].length()/2, i*40 + d.height - creditsTime1/5);
					}
				}
				g.setClip(0, 0, d.width, d.height);
				break;
		}
	}

	public ViewMode getViewModel()
	{
		return ViewMode.MAIN_MENU_VIEW;
	}

	private void updateSaveData()
	{
		saveInfo = new SaveInfo[3];
		for (int i = 0; i < 3; i++)
		{
			File file = new File("saves" + Global.FILE_SLASH + "Preview" + (i + 1) + ".out");
			if (file.exists())
			{
				Scanner in = Global.openFile(file);
				saveInfo[i] = new SaveInfo(in.next(), in.nextLong(), in.nextInt(), in.nextInt());
				in.close();
			}
		}
	}

	private void loadSettings()
	{
		File saveDir = new File("saves" + Global.FILE_SLASH);
		if (!saveDir.exists()) saveDir.mkdirs();

		File file = new File("saves" + Global.FILE_SLASH + "settings.txt");
		if (file.exists())
		{
			Scanner in = Global.openFile(file);
			theme = in.nextInt() == 0 ? Theme.BASIC : Theme.SCENIC;
			
			// Is muted
			if (in.nextInt() == 1)
				Global.soundPlayer.toggleMusic();
		}
		else
		{
			theme = Theme.BASIC;
			saveSettings();
		}
	}

	private void saveSettings()
	{
		try
		{
			PrintStream settingsOut = new PrintStream("saves" + Global.FILE_SLASH + "settings.txt");
			settingsOut.print((theme == Theme.BASIC ? 0 : 1) + " " + (Global.soundPlayer.isMuted() ? 1 : 0));
			settingsOut.close();
		}
		catch (IOException e)
		{
			Global.error("Could not create options file. Wut");
		}
	}
	
	private String formatTime(long l)
	{
		return (l/(3600) + ":" + String.format("%02d", ((l%3600)/60)));
	}

	public void movedToFront(Game game)
	{
		setVisualState(VisualState.MAIN);
		updateSaveData();
	}

}
