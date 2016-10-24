package gui;

import item.Item;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.Game;
import main.Global;
import namesies.Namesies.NamesiesType;
import namesies.Namesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import util.DrawMetrics;
import util.InputControl;
import util.InputControl.Control;
import battle.Attack;
import battle.Move;

class DevConsole {
	private int key;
	private String currText;
	private boolean show;

	DevConsole() {
		key = InputControl.INVALID_LOCK;
		currText = "";
		show = false;
	}

	// Try to initialize the console, but if you can't just don't do anything
	boolean init(InputControl input) {
		key = input.getLock();
		if (key == InputControl.INVALID_LOCK) {
			return false;
		}

		currText = "";
		show = true;
		return true;
	}

	public void update(int dt, InputControl input, Game game) {
		if (key == InputControl.INVALID_LOCK) {
			return; // Shouldn't even ever be here!
		}

		if (!input.isCapturingText()) {
			input.startTextCapture();
		}

		if (input.isCapturingText()) {
			currText = input.getCapturedText();
		}

		if (input.isDown(Control.ENTER, key)) {
			input.consumeKey(Control.ENTER, key);
			execute(game, input.stopTextCapture());
		}

		if (input.isDown(Control.ESC, key)) {
			input.consumeKey(Control.ESC, key);
			tearDown(input);
		}
	}

	private void execute(Game game, String command) {
		Scanner in = new Scanner(command);
		in.useDelimiter("\\s+");

		if (!in.hasNext()) {
			in.close();
			return;
		}

		String curr = in.next();

		switch (curr.toLowerCase()) {
			case "give":
				give(game, in);
				break;
			case "global":
				global(game, in);
				break;
			case "tele":
			case "teleport":
				transition(game, in);
				break;
			default:
				;
		}
		
		in.close();
	}
	
	private void transition(Game game, Scanner in) {
		if (game.characterData == null) {
			Global.error("Can't teleport before loading a player!");
		}

		if (!in.hasNext()) {
			Global.error("Teleport to which map???");
		}
		
		String mapName = in.next();
		String mapEntrance = null;
		
		if (in.hasNext()) {
			mapEntrance = in.next();
		}
		
		System.out.println("Teleporting Player to map " + mapName +" and to " +(mapEntrance == null? "location (0,0)": "map entrance "+mapEntrance) +".");
		game.characterData.setMap(mapName, mapEntrance);
		
		if (mapEntrance != null) {
			game.data.getMap(mapName).setCharacterToEntrance(game.characterData, mapEntrance);
		}
	}
	
	private void global(Game game, Scanner in) {
		if (game.characterData == null) {
			Global.error("Can't give before loading a player!");
		}

		if (!in.hasNext()) {
			Global.error("Add what global????");
		}

		String global = in.next();
		System.out.println("Adding global \"" + global + "\".");
		game.characterData.addGlobal(global);
	}

	private void give(Game game, Scanner in) {
		if (!in.hasNext()) {
			Global.error("Give what???");
		}

		if (game.characterData == null) {
			Global.error("Can't give before loading a player!");
		}

		String curr = in.next();
		switch (curr.toLowerCase()) {
			case "pokemon":
				String pokemonName = "";
				int level = ActivePokemon.MAX_LEVEL;
				List<Move> moves = null;
				boolean shiny = false;
				
				pokemonName = in.next();
				Namesies namesies = Namesies.getValueOf(pokemonName, NamesiesType.POKEMON);

				boolean valid = true;
				while (in.hasNext() && valid) {
					String token = in.next();

					switch (token.toLowerCase()) {
						case "level:":
							level = Integer.parseInt(in.next());
							break;
						case "shiny":
							shiny = true;
							break;
						case "moves:":
							moves = new ArrayList<>();
							in.useDelimiter(",");
							for (int i = 0; i < Move.MAX_MOVES; ++i) {
								String s = in.next().trim();
								if (!Attack.isAttack(s)) {
									Global.error("Invalid move: " + s);
								}
								
								// TODO: 'None' isn't a valid attack so can this if statement be deleted?
								if (!"none".equals(s.toLowerCase())) {
									moves.add(new Move(Attack.getAttackFromName(s)));
								}
							}
							
							in.useDelimiter("\\s+");
							break;
						default:
							valid = false;
							Global.error("error on token " + token);
							break;
					}
				}

				System.out.println("adding " + pokemonName + " " + (shiny ? " shiny " : "") + (moves == null ? " " : moves.toString()));

				ActivePokemon pokemon = new ActivePokemon(PokemonInfo.getPokemonInfo(namesies), level, false, true);
				if (moves != null) {
					pokemon.setMoves(moves);
				}

				if (shiny) {
					pokemon.setShiny();
				}

				game.characterData.addPokemon(null, pokemon);
				break;
			case "item":
				String itemName = in.next().replaceAll("_", " ");
				int amount = 1;
				if (in.hasNext()) {
					amount = Integer.parseInt(in.next());
				}

				if (!Item.isItem(itemName)) {
					Global.error("Invalid item: " + itemName);
				}
				
				game.characterData.addItem(Item.getItemFromName(itemName), amount);
				break;
		}
	}

	public void draw(Graphics g, GameData data) {
		if (!show) {
			return; // Fixes a minor graphical stutter when tearing down
		}

		g.translate(0, Global.GAME_SIZE.height - 20);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Global.GAME_SIZE.width, 20);

		g.setColor(Color.DARK_GRAY);
		g.drawLine(0, 0, Global.GAME_SIZE.width, 0);

		g.setColor(Color.WHITE);
		DrawMetrics.setFont(g, 14);
		g.drawString(currText, 2, 16);

		g.translate(0, -Global.GAME_SIZE.height + 20);
	}

	public void show() {
		show = true;
	}

	// Tear down, release locks, etc... This needs to be the only way to get out
	// of here, or bad things can happen!
	private void tearDown(InputControl input) {
		show = false;
		input.stopTextCapture();
		input.releaseLock(key);
		currText = "";
	}

	boolean isShown() {
		return show;
	}

}
