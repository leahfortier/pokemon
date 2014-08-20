package gui;

import item.Item;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Scanner;

import main.Game;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import battle.Attack;
import battle.Move;

public class DevConsole
{
	boolean show;

	int key;
	String currText;

	public DevConsole()
	{
		key = InputControl.INVALID_LOCK;
		currText = "";
		show = false;
	}

	// Try to initialize the console, but if you can't just don't do anything
	public boolean init(InputControl input)
	{
		key = input.getLock();
		if (key == InputControl.INVALID_LOCK) return false;

		currText = "";
		show = true;
		return true;
	}

	public void update(int dt, InputControl input, Game game)
	{
		if (key == InputControl.INVALID_LOCK) return; // Shouldn't even ever be here!

		if (!input.isCapturingText())
		{
			input.startTextCapture();
		}

		if (input.isCapturingText())
		{
			currText = input.getCapturedText();
		}

		if (input.isDown(Control.ENTER, key))
		{
			input.consumeKey(Control.ENTER, key);
			execute(game, input.stopTextCapture());
		}

		if (input.isDown(Control.ESC, key))
		{
			input.consumeKey(Control.ESC, key);
			tearDown(input);
		}
	}

	private void execute(Game game, String command)
	{
		Scanner in = new Scanner(command);
		in.useDelimiter("\\s+");

		if (!in.hasNext())
		{
			in.close();
			return;
		}

		String curr = in.next();

		switch (curr.toLowerCase())
		{
			case "give":
				give(game, in);
				break;
			case "global":
				global(game, in);
				break;
			default:
				;
		}
		
		in.close();
	}
	
	private void global(Game game, Scanner in) 
	{
		if (!in.hasNext())
		{
			System.err.println("Add what global?");
			return;
		}
		if (game.charData == null) 
		{
			System.err.println("Can't give before loading a player!.");
			return;
		}

		String curr = in.next();

		game.charData.addGlobal(curr);
	}

	private void give(Game game, Scanner in)
	{
		if (!in.hasNext())
		{
			System.err.println("Give what?");
			return;
		}
		if (game.charData == null) 
		{
			System.err.println("Can't give before loading a player!.");
			return;
		}

		String curr = in.next();

		switch (curr.toLowerCase())
		{
			case "pokemon":
				String pokemonName = "";
				int level = 100;
				ArrayList<Move> moves = null;
				boolean shiny = false;
				
				pokemonName = in.next();
				if(!PokemonInfo.isPokemon(pokemonName))
				{
					System.err.println("Invalid Pokemon: " + pokemonName);
					return;
				}

				boolean valid = true;
				while (in.hasNext() && valid)
				{
					String tok = in.next();

					switch (tok.toLowerCase())
					{
						case "level:":
							level = Integer.parseInt(in.next());
							break;
						case "shiny":
							shiny = true;
							break;
						case "moves:":
							moves = new ArrayList<>();
							in.useDelimiter(",");
							for (int i = 0; i < 4; ++i)
							{
								String s = in.next().trim();
								if(!Attack.isAttack(s))
								{
									System.err.println("Invalid move: " + s);
									return;
								}
								if (!"none".equals(s.toLowerCase())) moves.add(new Move(Attack.getAttack(s)));
							}
							in.useDelimiter("\\s+");
							break;
						default:
							System.err.println("error on token " + tok);
							valid = false;
							break;
					}
				}

				System.out.println("adding " + pokemonName + " " + (shiny ? " shiny " : "") + (moves == null ? " " : moves.toString()));

				ActivePokemon pokemon = new ActivePokemon(PokemonInfo.getPokemonInfo(pokemonName), level, false, true);
				if (moves != null) pokemon.setMoves(moves);
				if (shiny) pokemon.setShiny();

				game.charData.addPokemon(null, pokemon);

				break;
			case "item":
				String itemName = in.next().replaceAll("_", " ");
				int amt = 1;
				if(in.hasNext()) amt = Integer.parseInt(in.next());
				if(!Item.isItem(itemName))
				{
					System.out.println("Invalid item: " + itemName);
					return;
				}
				game.charData.addItem(Item.getItem(itemName), amt);
				break;
			default:
				;
		}
	}

	public void draw(Graphics g, GameData data)
	{
		if (!show) return; // Fixes a minor graphical stutter when tearing down

		g.translate(0, Global.GAME_SIZE.height - 20);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Global.GAME_SIZE.width, 20);

		g.setColor(Color.DARK_GRAY);
		g.drawLine(0, 0, Global.GAME_SIZE.width, 0);

		g.setColor(Color.WHITE);
		g.setFont(Global.getFont(14));
		g.drawString(currText, 2, 16);

		g.translate(0, -Global.GAME_SIZE.height + 20);
	}

	public void show()
	{
		show = true;
	}

	// Tear down, release locks, etc... This needs to be the only way to get out
	// of here, or bad things can happen!
	private void tearDown(InputControl input)
	{
		show = false;
		input.stopTextCapture();
		input.releaseLock(key);
		currText = "";
	}

	public boolean isShown()
	{
		return show;
	}

}
