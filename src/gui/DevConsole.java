package gui;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import input.InputControl;
import input.ControlKey;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
	boolean init() {
		key = InputControl.instance().getLock();
		if (key == InputControl.INVALID_LOCK) {
			return false;
		}

		currText = "";
		show = true;
		return true;
	}

	public void update() {
		if (key == InputControl.INVALID_LOCK) {
			return; // Shouldn't even ever be here!
		}

		InputControl input = InputControl.instance();
		if (!input.isCapturingText()) {
			input.startTextCapture();
		}

		if (input.isCapturingText()) {
			currText = input.getCapturedText();
		}

		if (input.consumeIfDown(ControlKey.ENTER, key)) {
			execute(input.stopTextCapture());
		}

		if (input.consumeIfDown(ControlKey.ESC, key)) {
			tearDown();
		}
	}

	private void execute(String command) {
		Scanner in = new Scanner(command);
		in.useDelimiter("\\s+");

		if (!in.hasNext()) {
			in.close();
			return;
		}

		String curr = in.next();
		switch (curr.toLowerCase()) {
			case "give":
				give(in);
				break;
			case "global":
				global(in);
				break;
			case "tele":
			case "teleport":
				transition(in);
				break;
			default:
				break;
		}
		
		in.close();
	}
	
	private void transition(Scanner in) {
		if (Game.getPlayer() == null) {
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
		
		System.out.println("Teleporting Player to map " + mapName +" and to " + (mapEntrance == null? "location (0,0)": "map entrance "+mapEntrance) +".");
		Game.getPlayer().setMap(mapName, mapEntrance);
		
		if (mapEntrance != null) {
			Game.getData().getMap(mapName).setCharacterToEntrance();
		}
	}
	
	private void global(Scanner in) {
		if (Game.getPlayer() == null) {
			Global.error("Can't give before loading a player!");
		}

		if (!in.hasNext()) {
			Global.error("Add what global????");
		}

		String global = in.next();
		System.out.println("Adding global \"" + global + "\".");
		Game.getPlayer().addGlobal(global);
	}

	private void give(Scanner in) {
		if (!in.hasNext()) {
			Global.error("Give what???");
		}

		if (Game.getPlayer() == null) {
			Global.error("Can't give before loading a player!");
		}

		String curr = in.next();
		switch (curr.toLowerCase()) {
			case "pokemon":
				PokemonNamesies namesies = PokemonNamesies.getValueOf(in.next());

				// Default values
				int level = ActivePokemon.MAX_LEVEL;
				List<Move> moves = null;
				boolean shiny = false;

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
									moves.add(new Move(AttackNamesies.getValueOf(s).getAttack()));
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

				System.out.println("adding " + namesies.getName() + " " + (shiny ? " shiny " : "") + (moves == null ? " " : moves.toString()));

				ActivePokemon pokemon = new ActivePokemon(namesies, level, false, true);
				if (moves != null) {
					pokemon.setMoves(moves);
				}

				if (shiny) {
					pokemon.setShiny();
				}

				Game.getPlayer().addPokemon(null, pokemon);
				break;
			case "item":
				String itemName = in.next().replaceAll("_", " ");
				int amount = 1;
				if (in.hasNext()) {
					amount = Integer.parseInt(in.next());
				}
				
				Game.getPlayer().addItem(ItemNamesies.getValueOf(itemName), amount);
				break;
		}
	}

	public void draw(Graphics g) {
		if (!show) {
			return; // Fixes a minor graphical stutter when tearing down
		}

		g.translate(0, Global.GAME_SIZE.height - 20);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Global.GAME_SIZE.width, 20);

		g.setColor(Color.DARK_GRAY);
		g.drawLine(0, 0, Global.GAME_SIZE.width, 0);

		g.setColor(Color.WHITE);
		FontMetrics.setFont(g, 14);
		g.drawString(currText, 2, 16);

		g.translate(0, -Global.GAME_SIZE.height + 20);
	}

	public void show() {
		show = true;
	}

	// Tear down, release locks, etc... This needs to be the only way to get out
	// of here, or bad things can happen!
	private void tearDown() {
		InputControl input = InputControl.instance();

		show = false;
		input.stopTextCapture();
		input.releaseLock(key);
		currText = "";
	}

	boolean isShown() {
		return show;
	}

}
