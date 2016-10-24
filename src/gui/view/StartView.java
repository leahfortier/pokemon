package gui.view;

import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Graphics;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import namesies.Namesies;
import pokemon.PokemonInfo;
import sound.SoundTitle;
import trainer.CharacterData;
import util.DrawMetrics;
import util.InputControl;
import util.InputControl.Control;
import battle.MessageUpdate;
import battle.MessageUpdate.Update;
import util.StringUtils;

public class StartView extends View {
	// TODO: Message update shouldn't be in battle since it's used for other purposes as well
	private static final MessageUpdate[] dialogue = new MessageUpdate[] {
				new MessageUpdate("Welcome to the world of Pok\u00e9mon!"), // TODO: Constants blah blah blah
				new MessageUpdate("It's filled with many unique creatures, such as this Ditto.", Update.SHOW_POKEMON),
				new MessageUpdate("The people of the Hash Map region befriend, travel, and battle with their Pok\u00e9mon."),
				new MessageUpdate("Oh, have you seen any syrup aboot? Well, never mind..."),
				new MessageUpdate("I can see quite clearly that you're a boy, so what's your name, eh?", Update.ENTER_NAME),
				new MessageUpdate(", are you ready to start your epic adventure? Well, off you go! I'll be seeing you soon!", Update.APPEND_TO_NAME)
			};

	private CharacterData player;
	private State state;
	
	private int dialogueIndex;
	private String message;
	
	private String name;
	private boolean ditto;
		
	private enum State {
		DEFAULT,
		NAME
	}

	public StartView(CharacterData data) {
		player = data;
	}
	
	public void update(int dt, InputControl input, Game game) {
		switch (state) {
			case DEFAULT:
				if (message != null) {
					if (input.mouseDown) {
						input.consumeMousePress();
						message = null;
					}

					if (input.isDown(Control.SPACE)) {
						input.consumeKey(Control.SPACE);
						message = null;
					}
				}
				
				if (message == null) {
					if (dialogueIndex == dialogue.length - 1) {
						game.setViewMode(ViewMode.MAP_VIEW);
					}
					else {
						dialogueIndex++;
						message = dialogue[dialogueIndex].getMessage();
						switch (dialogue[dialogueIndex].getUpdateType()) {
							case ENTER_NAME:
								state = State.NAME;
								break;
							case SHOW_POKEMON:
								ditto = true;
								state = State.DEFAULT;
								break;
							case APPEND_TO_NAME:
								message = player.getName() + message;
								state = State.DEFAULT;
								break;
							default:
								state = State.DEFAULT;
								break;
						}
					}
				}
				break;
			case NAME:
				if (!input.isCapturingText()) {
					input.startTextCapture();
				}

				if (input.isCapturingText()) {
					name = input.getCapturedText();
					if (name.length() > CharacterData.MAX_NAME_LENGTH) {
						name = name.substring(0, CharacterData.MAX_NAME_LENGTH);
					}
				}

				if (input.isDown(Control.ENTER)) {
					input.stopTextCapture();
					input.consumeKey(Control.ENTER);
					player.setName(name.isEmpty() ? CharacterData.DEFAULT_NAME : name);
					state = State.DEFAULT;
				}
				break;
		}
		
	}

	public void draw(Graphics g, GameData data) {
		TileSet tiles = data.getMenuTiles();
		TileSet battleTiles = data.getBattleTiles();
		TileSet trainerTiles = data.getTrainerTiles();
		TileSet pokemonTiles = data.getPokemonTilesSmall();
		
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		
		DrawMetrics.setFont(g, 30);
		g.setColor(Color.WHITE);
		
		switch (state) {
			case DEFAULT:
				g.drawImage(trainerTiles.getTile(0x58), 200, 200, null);
				if (ditto) {
					g.drawImage(pokemonTiles.getTile(PokemonInfo.getPokemonInfo(Namesies.DITTO_POKEMON).getImageNumber(false)), 270, 255, null);
				}
				break;
			case NAME:
				g.drawImage(trainerTiles.getTile(0x4), 200, 230, null);

				StringBuilder display = new StringBuilder();
				for (int i = 0; i < CharacterData.MAX_NAME_LENGTH; i++) {
					if (i < name.length()) {
						display.append(name.charAt(i));
					}
					else {
						display.append("_");
					}

					display.append(" ");
				}
				
				g.drawString(display.toString(), 300, 260);
				break;
		}
		
		if (message != null) {
			g.drawImage(battleTiles.getTile(0x3), 0, 440, null);
			DrawMetrics.setFont(g, 30);
			DrawMetrics.drawWrappedText(g, message, 30, 490, 750);
		}
	}

	public ViewMode getViewModel() {
		return ViewMode.START_VIEW;
	}

	public void movedToFront(Game game) {
		state = State.DEFAULT;
		
		dialogueIndex = 0;
		message = dialogue[dialogueIndex].getMessage();
		
		name = StringUtils.empty();
		ditto = false;
		
		Global.soundPlayer.playMusic(SoundTitle.NEW_GAME);
	}
}
