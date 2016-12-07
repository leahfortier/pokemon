package gui.view.mainmenu;

import gui.button.Button;
import gui.button.ButtonHoverAction;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
import input.ControlKey;
import input.InputControl;
import main.Game;
import sound.SoundPlayer;
import util.DrawUtils;
import util.FontMetrics;
import util.Save;
import util.Save.SavePreviewInfo;

import java.awt.Color;
import java.awt.Graphics;

public class MainMenuView extends View {
	private static final int BUTTON_WIDTH = 400;
	static final int NUM_MAIN_BUTTONS = 4;

	static final int[] bgt = new int[] { 800, 1800, 1000, 2400, 1000 };

	private VisualState state;
	private Theme theme;
	
	private SavePreviewInfo[] saveInfo;
	
	private int selectedButton;
	private boolean musicStarted = false;

	private int bgTime;
	private int bgIndex;

	public MainMenuView() {
		selectedButton = 0;
		
		bgTime = 0;
		bgIndex = 0;
		
		theme = Save.loadSettings();
		saveInfo = Save.updateSaveData();
		
		state = VisualState.MAIN;
	}

	static Button createMenuButton(int index) {
		return createMenuButton(index, Button.getBasicTransitions(index, NUM_MAIN_BUTTONS, 1));
	}

	static Button createMenuButton(int index, int[] transitions) {
		return new Button(
				200,
				240 + index*85,
				BUTTON_WIDTH,
				75,
				ButtonHoverAction.BOX,
				transitions);
	}

	void toggleTheme() {
		theme = Theme.values()[(theme.ordinal() + 1)%Theme.values().length];
	}

	void saveSettings() {
		Save.saveSettings(this.theme);
	}

	int getPressed(Button[] buttons) {
		int pressed = -1;

		selectedButton = Button.update(buttons, selectedButton);
		if (buttons[selectedButton].checkConsumePress()) {
			pressed = selectedButton;
		}

		return pressed;
	}

	void setVisualState(VisualState newState) {
		state = newState;
		selectedButton = 0;

		for (Button button : state.getButtons()) {
			button.setForceHover(false);
		}

		state.set();
		
		SoundPlayer.soundPlayer.playMusic(state.getTunes());
	}

	boolean hasSavedInfo(int saveNum) {
		return this.saveInfo[saveNum] != null;
	}

	void reloadSaveInfo() {
		this.saveInfo = Save.updateSaveData();
	}

	@Override
	public void update(int dt) {
		if (!musicStarted) {
			musicStarted = true;
			SoundPlayer.soundPlayer.playMusic(state.getTunes());
		}

		this.state.update(this);

		int nextIndex = (bgIndex + 1)%bgt.length;
		bgTime += 10;
		
		if (bgTime > bgt[nextIndex]) {
			bgTime -= bgt[nextIndex];
			bgIndex = nextIndex;
		}
		
		if (InputControl.instance().consumeIfDown(ControlKey.BACK)) {
			setVisualState(VisualState.MAIN);
		}
	}

	private void drawButton(Graphics g, TileSet tiles, Button button) {
		g.translate(button.x, button.y);

		button.fillTranslated(g, theme.getThemeColor());
		
		// Full size buttons are 0x04 and the half-size ones are 0x05
		int tileIndex = button.width == BUTTON_WIDTH ? 0x04 : 0x05;
		
		g.drawImage(tiles.getTile(tileIndex), 0, 0, null);
		
		g.translate(-button.x, -button.y);
		
		// Should not be inside the translate or everything is fucked hxc
		button.draw(g);
	}

	@Override
	public void draw(Graphics g) {
		TileSet tiles = Game.getData().getMainMenuTiles();

		theme.draw(g, tiles, bgTime, bgIndex);
		
		g.drawImage(tiles.getTile(0x03), 95, 54, null);
		for (Button b : state.getButtons()) {
			drawButton(g, tiles, b);
		}
		
		g.setColor(Color.BLACK);
		this.state.draw(g, this);
	}
	
	void drawSaveInformation(Graphics g, Button b, int index, String emptyText) {
		g.setColor(Color.BLACK);
		Save.SavePreviewInfo info = saveInfo[index];

		if (info != null) {
			g.translate(b.x, b.y);

			FontMetrics.setFont(g, 20);

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
			b.label(g, 30, emptyText);
		}
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.MAIN_MENU_VIEW;
	}

	@Override
	public void movedToFront() {
		setVisualState(VisualState.MAIN);
		saveInfo = Save.updateSaveData();
	}

}
