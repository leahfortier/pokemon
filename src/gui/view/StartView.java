package gui.view;

import gui.GameData;
import gui.TileSet;
import gui.panel.BasicPanels;
import input.ControlKey;
import input.InputControl;
import main.Game;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.CharacterData;
import util.FontMetrics;
import util.PokeString;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;

class StartView extends View {
	
	private static final MessageUpdate[] dialogue = new MessageUpdate[] {
				new MessageUpdate("Welcome to the world of " + PokeString.POKEMON + "!"),
				new MessageUpdate("It's filled with many unique creatures, such as this Ditto.").withUpdate(Update.SHOW_POKEMON),
				new MessageUpdate("The people of the Hash Map region befriend, travel, and battle with their " + PokeString.POKEMON + "."),
				new MessageUpdate("Oh, have you seen any syrup aboot? Well, never mind..."),
				new MessageUpdate("I can see quite clearly that you're a boy, so what's your name, eh?").withUpdate(Update.ENTER_NAME),
				new MessageUpdate(", are you ready to start your epic adventure? Well, off you go! I'll be seeing you soon!").withUpdate(Update.APPEND_TO_NAME)
			};

	private State state;
	
	private int dialogueIndex;
	private String message;
	
	private String name;
	private boolean ditto;
		
	private enum State {
		DEFAULT,
		NAME
	}

	@Override
	public void update(int dt) {
		CharacterData player = Game.getPlayer();
		InputControl input = InputControl.instance();

		switch (state) {
			case DEFAULT:
				if (message != null) {
					if (input.consumeIfMouseDown()) {
						message = null;
					}

					if (input.consumeIfDown(ControlKey.SPACE)) {
						message = null;
					}
				}
				
				if (message == null) {
					if (dialogueIndex == dialogue.length - 1) {
						Game.setViewMode(ViewMode.MAP_VIEW);
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

				if (input.consumeIfDown(ControlKey.ENTER)) {
					input.stopTextCapture();
					player.setName(name.isEmpty() ? CharacterData.DEFAULT_NAME : name);
					state = State.DEFAULT;
				}
				break;
		}
		
	}

	@Override
	public void draw(Graphics g) {
		GameData data = Game.getData();

		TileSet trainerTiles = data.getTrainerTiles();
		TileSet pokemonTiles = data.getPokemonTilesSmall();

		BasicPanels.drawCanvasPanel(g);
		
		FontMetrics.setFont(g, 30);
		g.setColor(Color.BLACK);
		
		switch (state) {
			case DEFAULT:
				g.drawImage(trainerTiles.getTile(0x58), 200, 200, null);
				if (ditto) {
					g.drawImage(pokemonTiles.getTile(PokemonInfo.getPokemonInfo(PokemonNamesies.DITTO).getImageNumber()), 270, 255, null);
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
			BasicPanels.drawFullMessagePanel(g, message);
		}
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.START_VIEW;
	}

	@Override
	public void movedToFront() {
		state = State.DEFAULT;
		
		dialogueIndex = 0;
		message = dialogue[dialogueIndex].getMessage();
		
		name = StringUtils.empty();
		ditto = false;
		
		SoundPlayer.soundPlayer.playMusic(SoundTitle.NEW_GAME);
	}
}
