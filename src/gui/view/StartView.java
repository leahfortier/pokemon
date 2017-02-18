package gui.view;

import gui.GameData;
import gui.TileSet;
import draw.button.panel.BasicPanels;
import gui.IndexTileSet;
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

	private boolean ditto;
		
	private enum State {
		DEFAULT,
		NAME
	}

	@Override
	public void update(int dt) {
		CharacterData player = Game.getPlayer();
		InputControl input = InputControl.instance();

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
				Game.instance().setViewMode(ViewMode.MAP_VIEW);
			} else {
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

		if (state == State.NAME) {
			if (!input.isCapturingText()) {
				input.startTextCapture();
			}

			if (input.consumeIfDown(ControlKey.ENTER)) {
				input.stopTextCapture();

				String name = input.getCapturedText(CharacterData.MAX_NAME_LENGTH);
				player.setName(name.isEmpty() ? CharacterData.DEFAULT_NAME : name);

				state = State.DEFAULT;
			}
		}
		
	}

	@Override
	public void draw(Graphics g) {
		GameData data = Game.getData();

		IndexTileSet trainerTiles = data.getTrainerTiles();
		TileSet pokemonTiles = data.getPokemonTilesSmall();

		BasicPanels.drawCanvasPanel(g);
		
		FontMetrics.setFont(g, 30);
		g.setColor(Color.BLACK);
		
		switch (state) {
			case DEFAULT:
				g.drawImage(trainerTiles.getTile(0x58), 200, 200, null);
				if (ditto) {
					g.drawImage(pokemonTiles.getTile(PokemonInfo.getPokemonInfo(PokemonNamesies.DITTO).getImageName()), 270, 255, null);
				}
				break;
			case NAME:
				g.drawImage(trainerTiles.getTile(0x4), 200, 230, null);
				g.drawString(InputControl.instance().getInputCaptureString(CharacterData.MAX_NAME_LENGTH), 300, 260);
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

		ditto = false;
		
		SoundPlayer.soundPlayer.playMusic(SoundTitle.NEW_GAME);
	}
}
