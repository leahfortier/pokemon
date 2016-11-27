package gui.view;

import gui.Button;
import gui.TileSet;
import main.Game;
import main.Global;
import sound.SoundPlayer;
import sound.SoundTitle;
import util.DrawUtils;
import util.InputControl;
import util.InputControl.Control;
import util.Save;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class MainMenuView extends View {
	private static final int NUM_MAIN_BUTTONS = 4;

	private static final int[] bgt = new int[] { 800, 1800, 1000, 2400, 1000 };
	private static final int[] bgx = new int[] { -300, -400, -800, -800, -200 };
	private static final int[] bgy = new int[] { -300, -130, -130, -500, -500 };
	
	private static final String creditsHoz = "TEAM ROCKET    TEAM ROCKET";
	private static final String[] creditsText = { "", "Team Rocket", "", "Lead Programmers", "Leah Fortier", "Tyler Brazill", "Maxwell Miller", "",
			"", "Graphic Designers", "Josh Linge", "Jeb Ralston", "Jessica May", "", "Writers", "Jeb Ralston", "Jessica May", "", 
			"UML MASTER", "Jeb Ralston", "", "Nintendo", "", "Game Freak", "", "credits", "credits", "credits" };
	
	private static final String[] MAIN_HEADERS = { "Load Game", "New Game", "Options", "Quit" };
	private static final String[] OPTIONS_HEADERS = { "Theme", "Mute", "Credits", "Return" };
	
	private VisualState state;
	private Theme theme;
	
	private Save.SavePreviewInfo[] saveInfo;
	
	private int selectedButton;
	private boolean deletePressed;
	private boolean musicStarted = false;

	private int bgTime;
	private int bgIndex;

	private int creditsTime1;
	private int creditsTime2;

	public MainMenuView() {
		selectedButton = 0;
		
		creditsTime1 = 0;
		creditsTime2 = 0;
		
		bgTime = 0;
		bgIndex = 0;
		
		deletePressed = false;
		
		theme = Save.loadSettings();
		saveInfo = Save.updateSaveData();

		for (int i = 0; i < VisualState.MAIN.buttons.length; i++) {
			VisualState.MAIN.buttons[i] = new Button(
					200,
					240 + i*85,
					400,
					75,
					Button.HoverAction.BOX,
					new int[] { 
						Button.NO_TRANSITION, // Right
						Button.basicUp(i, VisualState.MAIN.buttons.length), // Up 
						Button.NO_TRANSITION, // Left
						Button.basicDown(i, VisualState.MAIN.buttons.length) // Down 
					});
		}	

		for (int i = 0; i < Save.NUM_SAVES; i++) {
			VisualState.LOAD.buttons[i] = new Button(
					VisualState.MAIN.buttons[i].x,
					VisualState.MAIN.buttons[i].y,
					VisualState.MAIN.buttons[i].width,
					VisualState.MAIN.buttons[i].height,
					Button.HoverAction.BOX,
					new int[] {
							Button.NO_TRANSITION, // Right
							Button.basicUp(i, VisualState.LOAD.buttons.length), // Up
							Button.NO_TRANSITION, // Left
							i + 1 // Down
					});
		}
			
		VisualState.LOAD.buttons[Save.NUM_SAVES] = new Button(
				VisualState.MAIN.buttons[NUM_MAIN_BUTTONS - 1].x,
				VisualState.MAIN.buttons[NUM_MAIN_BUTTONS - 1].y,
				VisualState.MAIN.buttons[NUM_MAIN_BUTTONS - 1].width/2 - 5,
				VisualState.MAIN.buttons[NUM_MAIN_BUTTONS - 1].height,
				Button.HoverAction.BOX,
				new int[] {
						Save.NUM_SAVES + 1, // Right -- to the delete button
						Save.NUM_SAVES - 1, // Up -- to the last save file
						Save.NUM_SAVES + 1, // Left -- to the delete button
						0  // Down -- to the first save file
				});

		VisualState.LOAD.buttons[Save.NUM_SAVES + 1] = new Button(
				VisualState.MAIN.buttons[NUM_MAIN_BUTTONS - 1].x + VisualState.MAIN.buttons[NUM_MAIN_BUTTONS - 1].width/2 + 5,
				VisualState.LOAD.buttons[Save.NUM_SAVES].y,
				VisualState.LOAD.buttons[Save.NUM_SAVES].width,
				VisualState.LOAD.buttons[Save.NUM_SAVES].height,
				Button.HoverAction.BOX,
				new int[] {
						Save.NUM_SAVES, // Right -- to the return button
						Save.NUM_SAVES - 1, // Up -- to the last save file
						Save.NUM_SAVES, // Left -- to the return button
						0 // Down -- to the first save file
				});

		for (int i = 0; i < VisualState.NEW.buttons.length; i++) {
			VisualState.NEW.buttons[i] = new Button(
					VisualState.MAIN.buttons[i].x,
					VisualState.MAIN.buttons[i].y,
					VisualState.MAIN.buttons[i].width,
					VisualState.MAIN.buttons[i].height,
					Button.HoverAction.BOX, 
					new int[] { 
						Button.NO_TRANSITION, // Right
						Button.basicUp(i, VisualState.NEW.buttons.length), // Up
						Button.NO_TRANSITION, // Left
						Button.basicDown(i, VisualState.NEW.buttons.length) // Down
					});
		}

		for (int i = 0; i < VisualState.OPTIONS.buttons.length; i++) {
			VisualState.OPTIONS.buttons[i] = new Button(
					VisualState.MAIN.buttons[i].x,
					VisualState.MAIN.buttons[i].y,
					VisualState.MAIN.buttons[i].width,
					VisualState.MAIN.buttons[i].height,
					Button.HoverAction.BOX,
					new int[] {
							Button.NO_TRANSITION, // Right
							Button.basicUp(i, VisualState.OPTIONS.buttons.length), // Up
							Button.NO_TRANSITION, // Left
							Button.basicDown(i, VisualState.OPTIONS.buttons.length) // Down
					});
		}
		
		state = VisualState.MAIN;
		selectedButton = creditsTime1 = creditsTime2 = 0;
	}
	
	private enum VisualState {
		MAIN(NUM_MAIN_BUTTONS, SoundTitle.MAIN_MENU_TUNE), 
		LOAD(Save.NUM_SAVES + 2, SoundTitle.MAIN_MENU_TUNE), 
		NEW(Save.NUM_SAVES + 1, SoundTitle.MAIN_MENU_TUNE), 
		OPTIONS(NUM_MAIN_BUTTONS, SoundTitle.MAIN_MENU_TUNE), 
		CREDITS(0, SoundTitle.CREDITS_TUNE);
		
		private final Button[] buttons;
		private final SoundTitle tunes;
		
		VisualState(int numButtons, SoundTitle tunes) {
			this.buttons = new Button[numButtons];
			this.tunes = tunes;
		} 
	}
	
	public enum Theme {
		BASIC(new Color(255, 210, 86), (g, tiles, bgTime, bgIndex) -> {
                    g.setColor(new Color(68, 123, 184));
                    g.fillRect(0, 0, 800, 600);
                    g.drawImage(tiles.getTile(0x01), 0, 0, null);
                }),
		SCENIC(new Color(68, 123, 184), (g, tiles, bgTime, bgIndex) -> {
			float locRatio = 1.0f - (float) bgTime / (float) bgt[(bgIndex + 1) % bgt.length];
            int xLoc = (int) (bgx[bgIndex]*locRatio + (1.0f - locRatio) * bgx[(bgIndex + 1)%bgt.length]);
            int yLoc = (int) (bgy[bgIndex]*locRatio + (1.0f - locRatio) * bgy[(bgIndex + 1)%bgt.length]);

            g.drawImage(tiles.getTile(0x06), xLoc, yLoc, null);
            g.drawImage(tiles.getTile(0x02), 0, 0, null);
        });
		
		private final Color themeColor;
		private final ThemeDraw draw;
		
		Theme(Color themeColor, ThemeDraw draw) {
			this.themeColor = themeColor;
			this.draw = draw;
		}
		
		private interface ThemeDraw {
			void draw(Graphics g, TileSet tiles, int bgTime, int bgIndex);
		}
	}

	private void setVisualState(VisualState newState) {
		state = newState;
		selectedButton = creditsTime1 = creditsTime2 = 0;
		
		for (Button b : state.buttons) {
			b.setForceHover(false);
		}
		
		SoundPlayer.soundPlayer.playMusic(state.tunes);
	}

	public void update(int dt, InputControl input) {
		if (!musicStarted) {
			musicStarted = true;
			SoundPlayer.soundPlayer.playMusic(state.tunes);
		}
		
		int pressed = -1;
		
		if (state.buttons.length > 0) {
			selectedButton = Button.update(state.buttons, selectedButton, input);
			if (state.buttons[selectedButton].checkConsumePress()) {
				pressed = selectedButton;
			}	
		}
		
		switch (state) {
			case MAIN:		
				switch (pressed) {
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
				switch (pressed) {
					case 0: // load
					case 1:
					case 2:
						if (deletePressed && saveInfo[pressed] != null) {
							deletePressed = false;
							Save.deleteSave(pressed); // TODO: ask to delete first
							saveInfo = Save.updateSaveData();
						}
						else if (saveInfo[pressed] != null) {
							Game.loadSave(pressed);
							Game.setViewMode(ViewMode.MAP_VIEW);
						}
						break;
					case 3: // return
						setVisualState(VisualState.MAIN);
						break;
					case 4: // delete
						deletePressed = true;
						break;
					default:
						break;
				}
				break;
			case NEW:
				switch (pressed) {
					case 0: // new
					case 1:
					case 2: 
						// TODO: Ask to delete
						Game.newSave(pressed);
						Game.setViewMode(ViewMode.START_VIEW);
						break;
					case 3: // return
						setVisualState(VisualState.MAIN);
						break;
					default:
						break;
				}				
				break;
			case OPTIONS:
				switch (pressed) {
					case 0: // theme
						theme = Theme.values()[(theme.ordinal() + 1)%Theme.values().length];
						Save.saveSettings(theme);
						break;
					case 1: // mute
						SoundPlayer.soundPlayer.toggleMusic();
						Save.saveSettings(theme);
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
				break;
			case CREDITS:
				// TODO: These should be constants once I find out what they are
				creditsTime1 += 10;
				creditsTime1 %= 8000;
				
				creditsTime2 += 10;
				creditsTime2 %= 8500;
				break;
		}

		int nextIndex = (bgIndex + 1)%bgt.length;
		bgTime += 10;
		
		if (bgTime > bgt[nextIndex]) {
			bgTime -= bgt[nextIndex];
			bgIndex = nextIndex;
		}
		
		if (input.consumeIfDown(Control.BACK)) {
			setVisualState(VisualState.MAIN);
		}
	}

	// TODO: I think there might be a method in the draw thingy that does thing
	private void drawButton(Graphics g, TileSet tiles, Button b) {
		g.translate(b.x, b.y);
		
		g.setColor(theme.themeColor);
		g.fillRect(0, 0, b.width, b.height);
		
		// Full size buttons are 0x04 and the half-size ones are 0x05
		int tileIndex = b.width == VisualState.MAIN.buttons[0].width ? 0x04 : 0x05;
		
		g.drawImage(tiles.getTile(tileIndex), 0, 0, null);
		
		g.translate(-b.x, -b.y);
		
		// Should not be inside the translate or everything is fucked hxc
		b.draw(g);
	}
	
	public void draw(Graphics g) {
		TileSet tiles = Game.getData().getMainMenuTiles();

		theme.draw.draw(g, tiles, bgTime, bgIndex);
		
		g.drawImage(tiles.getTile(0x03), 95, 54, null);
		for (Button b : state.buttons) {
			drawButton(g, tiles, b);
		}
		
		g.setColor(Color.BLACK);
		switch (state) {
			case MAIN:
				DrawUtils.setFont(g, 40);
				for (int i = 0; i < NUM_MAIN_BUTTONS; i++) {
					DrawUtils.drawCenteredString(g, MAIN_HEADERS[i], state.buttons[i]);
				}
				break;
			case LOAD:
				// Draw each save information button
				for (int i = 0; i < Save.NUM_SAVES; i++) {
					drawSaveInformation(g, state.buttons[i], i, "Empty");
				}
				
				// Return and Delete
				DrawUtils.setFont(g, 30);
				DrawUtils.drawCenteredString(g, "Return", state.buttons[Save.NUM_SAVES]);
				DrawUtils.drawCenteredString(g, "Delete", state.buttons[Save.NUM_SAVES + 1]);
				break;
			case NEW:
				for (int i = 0; i < Save.NUM_SAVES; i++) {
					drawSaveInformation(g, state.buttons[i], i, "New Save");
				}
				
				DrawUtils.setFont(g, 40);
				DrawUtils.drawCenteredString(g, "Return", state.buttons[Save.NUM_SAVES]);
				break;
			case OPTIONS:
				DrawUtils.setFont(g, 40);
				for (int i = 0; i < NUM_MAIN_BUTTONS; i++) {
					DrawUtils.drawCenteredString(g, OPTIONS_HEADERS[i], state.buttons[i]);
				}
				break;
			case CREDITS:
				drawCredits(g);
				break;
			default:
				break;
		}
	}
	
	private void drawCredits(Graphics g) {
		Dimension d = Global.GAME_SIZE;
		g.setClip(0, 220, d.width, d.height - 240);

		g.setColor(new Color(0, 0, 0, 64));
		DrawUtils.setFont(g, 512);
		g.drawString(creditsHoz, d.width - creditsTime2, d.height - 30);

		g.setColor(Color.BLACK);
		
		for (int i = 1; i < creditsText.length; i++) {
			if (creditsText[i - 1].isEmpty()) {
				DrawUtils.setFont(g, 40);
				DrawUtils.drawCenteredWidthString(g, creditsText[i], d.width/2, i*40 + d.height - creditsTime1/5);
			}
			else {
				DrawUtils.setFont(g, 30);
				DrawUtils.drawCenteredWidthString(g, creditsText[i], d.width/2, i*40 + d.height - creditsTime1/5);
			}
		}
		
		g.setClip(0, 0, d.width, d.height);
	}
	
	private void drawSaveInformation(Graphics g, Button b, int index, String emptyText) {
		g.setColor(Color.BLACK);
		Save.SavePreviewInfo info = saveInfo[index];
		
		if (info != null) {
			g.translate(b.x, b.y);
			
			DrawUtils.setFont(g, 20);

			g.drawString("Name:", 16, 28);
			DrawUtils.drawRightAlignedString(g, info.getName(), 189, 28);

			g.drawString("Time:", 210, 28);
			DrawUtils.drawRightAlignedString(g, "" + Save.formatTime(info.getTime()), 383, 28);

			g.drawString("Badges:", 16, 58);
			DrawUtils.drawRightAlignedString(g, "" + info.getBadges(), 189, 58);

			g.drawString("Pokedex:", 210, 58);
			DrawUtils.drawRightAlignedString(g, "" + info.getPokemonSeen(), 383, 58);
			
			g.translate(-b.x, -b.y);
		}
		else {
			DrawUtils.setFont(g, 30);
			DrawUtils.drawCenteredString(g, emptyText, b);
		}
	}

	public ViewMode getViewModel() {
		return ViewMode.MAIN_MENU_VIEW;
	}

	public void movedToFront() {
		setVisualState(VisualState.MAIN);
		saveInfo = Save.updateSaveData();
	}

}
