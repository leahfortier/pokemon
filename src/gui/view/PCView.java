package gui.view;

import gui.Button;
import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.PC;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Trainer;
import battle.Move;

public class PCView extends View
{
	private static final int NUM_BUTTONS = PC.BOX_HEIGHT*PC.BOX_WIDTH + Trainer.MAX_POKEMON + 6;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int RELEASE = NUM_BUTTONS - 2;
	private static final int DEPOSIT_WITHDRAW = NUM_BUTTONS - 3;
	private static final int SWITCH = NUM_BUTTONS - 4;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int LEFT_ARROW = NUM_BUTTONS - 6;
	private static final int PARTY = PC.BOX_HEIGHT*PC.BOX_WIDTH;

	private CharacterData player;
	private ActivePokemon selected;
	private boolean party;
	private int selectedButton;
	private boolean depositClicked;
	private boolean switchClicked;
	
	private Button[] buttons;
	private Button[][] boxButtons;
	private Button[] partyButtons;
	private Button leftButton;
	private Button rightButton;
	private Button switchButton;
	private Button depositWithdrawButton;
	private Button releaseButton;
	private Button returnButton;
	
	public PCView(CharacterData c)
	{
		player = c;
		selectedButton = PARTY;
		
		buttons = new Button[NUM_BUTTONS];
		boxButtons = new Button[PC.BOX_HEIGHT][PC.BOX_WIDTH];
		for (int i = 0, k = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++, k++)
			{
				buttons[k] = boxButtons[i][j] = new Button(60 + 54*j, 96 + 54*i, 40, 40, Button.HoverAction.BOX, 
						new int[] {j == PC.BOX_WIDTH - 1 ? SWITCH : k + 1, // Right 
								i == 0 ? PARTY + j : k - PC.BOX_WIDTH, // Up
								j == 0 ? RELEASE : k - 1, // Left
								i == PC.BOX_HEIGHT - 1 ? (j < PC.BOX_WIDTH/2 ? LEFT_ARROW : RIGHT_ARROW) : k + PC.BOX_WIDTH}); // Down
			}
		}
		
		partyButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			buttons[PARTY + i] = partyButtons[i] = new Button(60 + 54*i, 499, 40, 40, Button.HoverAction.BOX,
					new int[] {i == Trainer.MAX_POKEMON - 1 ? RETURN : PARTY + i + 1, // Right
							i < PC.BOX_WIDTH/2 ? LEFT_ARROW : RIGHT_ARROW, // Up
							i == 0 ? RETURN : PARTY + i - 1, // Left
							i}); // Down
		}
		
		buttons[LEFT_ARROW] = leftButton = new Button(140, 418, 35, 20, Button.HoverAction.BOX, new int[] {RIGHT_ARROW, PC.BOX_WIDTH*(PC.BOX_HEIGHT-1) + PC.BOX_WIDTH/2 - 1, -1, PARTY});
		buttons[RIGHT_ARROW] = rightButton = new Button(255, 418, 35, 20, Button.HoverAction.BOX, new int[] {SWITCH, PC.BOX_WIDTH*(PC.BOX_HEIGHT-1) + PC.BOX_WIDTH/2, LEFT_ARROW, PARTY});
		
		buttons[SWITCH] = switchButton = new Button(410, 464, 118, 38, Button.HoverAction.BOX, new int[] {DEPOSIT_WITHDRAW, -1, RIGHT_ARROW, RETURN});
		buttons[DEPOSIT_WITHDRAW] = depositWithdrawButton = new Button(526, 464, 118, 38, Button.HoverAction.BOX, new int[] {RELEASE, -1, SWITCH, RETURN});
		buttons[RELEASE] = releaseButton = new Button(642, 464, 118, 38, Button.HoverAction.BOX, new int[] {0, -1, DEPOSIT_WITHDRAW, RETURN});
		
		buttons[RETURN] = returnButton = new Button(410, 522, 350, 38, Button.HoverAction.BOX, new int[] {0, SWITCH, PARTY + Trainer.MAX_POKEMON - 1, -1});
		
		party = true;
		selected = player.front();
	}
	
	public void update(int dt, InputControl input, Game game)
	{
		selectedButton = Button.update(buttons, selectedButton, input);

		for (int i = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++)
			{
				if (boxButtons[i][j].checkConsumePress())
				{
					if (party && depositClicked)
					{
						player.getPC().depositPokemon(player, selected, i, j);
						depositClicked = false;
					}
					else if (switchClicked)
					{
						player.getPC().switchPokemon(player, selected, i, j);
						switchClicked = false;
					}
					else
					{
						selected = player.getPC().getBoxPokemon()[i][j];
						party = false;
					}
					updateActiveButtons();
				}
			}
		}
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			if (partyButtons[i].checkConsumePress())
			{
				if (party && depositClicked)
				{
					depositClicked = false;
				}
				else if (switchClicked)
				{
					player.getPC().switchPokemon(player, selected, i);
					switchClicked = false;
				}
				else
				{
					selected = player.getTeam().get(i);
					party = true;					
				}
				updateActiveButtons();
			}
		}
		
		if (leftButton.checkConsumePress())
		{
			player.getPC().prevBox();
			movedToFront(game);
		}
		
		if (rightButton.checkConsumePress())
		{
			player.getPC().nextBox();
			movedToFront(game);
		}
		
		if (switchButton.checkConsumePress())
		{
			switchClicked = !switchClicked;
			updateActiveButtons();
		}
		
		if (depositWithdrawButton.checkConsumePress())
		{
			if (party) // Deposit
			{
				if (depositClicked) player.getPC().depositPokemon(player, selected);
				depositClicked = !depositClicked;
				updateActiveButtons();
			}
			else // Withdraw
			{
				player.getPC().withdrawPokemon(player, selected);
				updateActiveButtons();
			}
		}
		
		if (releaseButton.checkConsumePress())
		{
			player.getPC().releasePokemon(player, selected);
			movedToFront(game);
		}
		
		if (returnButton.checkConsumePress())
		{
			game.setViewMode(ViewMode.MAP_VIEW);
		}
		
		if (input.isDown(Control.ESC))
		{
			input.consumeKey(Control.ESC);
			game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g, GameData data)
	{
		TileSet tiles = data.getMenuTiles();
		TileSet typeTiles = data.getBattleTiles();
		TileSet partyTiles = data.getPartyTiles();
		TileSet pokemonTiles = data.getPokemonTilesSmall();
		
		PC pc = player.getPC();
		ActivePokemon[][] box = pc.getBoxPokemon();
		
		// Box
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		g.setColor(pc.getBoxColor());
		g.fillRect(40, 40, 350, 418);
		g.drawImage(tiles.getTile(0x31), 40, 40, null);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		String s = "Box " + (pc.getBoxNum() + 1);
		g.drawString(s, Global.centerX(s, 214, 20), 65);
		
		for (int i = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++)
			{
				ActivePokemon p = box[j][i];
				if (p == null) continue;
				
				g.translate(boxButtons[j][i].x, boxButtons[j][i].y);
				
				if (p == selected)
				{
					g.drawImage(tiles.getTile(0x32), 0, 0, null);
				}
				
				BufferedImage pkmImg = partyTiles.getTile(p.isEgg() ? 0x10000 : p.getPokemonInfo().getNumber());
				g.drawImage(pkmImg, 20 - pkmImg.getWidth()/2, 20 - pkmImg.getHeight()/2, null);
				
				g.translate(-boxButtons[j][i].x, -boxButtons[j][i].y);
			}
		}
		
		g.setFont(Global.getFont(16));
		s = (pc.getBoxNum() + 1) + "/" + pc.getNumBoxes();
		g.drawString(s, Global.centerX(s, 215, 20), 433);
		
		View.drawArrows(g, leftButton, rightButton);
		
		// Party
		g.setColor(Color.RED);
		g.fillRect(40, 478, 350, 82);
		g.drawImage(tiles.getTile(0x33), 40, 478, null);
		
		List<ActivePokemon> team = player.getTeam();
		for (int i = 0; i < team.size(); i++)
		{
			g.translate(partyButtons[i].x, partyButtons[i].y);
			
			ActivePokemon p = team.get(i);
			if (p == selected)
			{
				g.drawImage(tiles.getTile(0x32), 0, 0, null);
			}
			
			BufferedImage pkmImg = partyTiles.getTile(p.isEgg() ? 0x10000 : p.getPokemonInfo().getNumber());
			g.drawImage(pkmImg, 20 - pkmImg.getWidth()/2, 20 - pkmImg.getHeight()/2, null);
			
			g.translate(-partyButtons[i].x, -partyButtons[i].y);
		}
		
		// Description
		Type[] type = selected.getActualType();
		Color[] typeColors = Type.getColors(selected);
		
		g.setColor(typeColors[0]);
		g.fillPolygon(new int[] {410, 759, 759, 410}, new int[] {40, 40, 96, 445}, 4);
		g.setColor(typeColors[1]);
		g.fillPolygon(new int[] {410, 759, 759, 410}, new int[] {445, 96, 501, 501}, 4);
		
		if (switchClicked) greyOut(g, switchButton, false);
		if (!releaseButton.isActive()) greyOut(g, releaseButton, true);
		if (!depositWithdrawButton.isActive()) greyOut(g, depositWithdrawButton, true); 
		else if (party && depositClicked) greyOut(g, depositWithdrawButton, false);
		
		g.drawImage(tiles.getTile(0x34), 410, 40, null);
		BufferedImage pkmImg = pokemonTiles.getTile(selected.isEgg() ? 0x10000 : selected.getPokemonInfo().getImageNumber(selected.isShiny()));
		g.drawImage(pkmImg, 479 - pkmImg.getWidth()/2, 109 - pkmImg.getHeight()/2, null);
		
		if (selected.isEgg())
		{
			g.setColor(Color.BLACK);
			g.setFont(Global.getFont(20));
			
			g.drawString(selected.getName(), 541, 82);
			
			g.setFont(Global.getFont(16));
			g.drawString(selected.getEggMessage(), 427, 179); // TODO: Wrapped text
		}
		else
		{
			g.setColor(Color.BLACK);
			g.setFont(Global.getFont(20));
			
			g.drawString(selected.getName() + " " + selected.getGender().getCharacter(), 541, 82);
			s = "Lv" + selected.getLevel();
			g.drawString(s, Global.rightX(s, 740, 20), 82);
			g.drawString("#" + String.format("%03d", selected.getPokemonInfo().getNumber()), 541, 110);
			
			int index = 0;
			if (type[1] != Type.NONE)
			{
				g.drawImage(typeTiles.getTile(type[0].getImageIndex()), 669, 97, null);
				index = 1;
			}
			g.drawImage(typeTiles.getTile(type[index].getImageIndex()), 707, 97, null);
			
			g.setFont(Global.getFont(16));
			
			g.drawString("EXP:", 540, 135);
			s = selected.getTotalEXP() + "";
			g.drawString(s, Global.rightX(s, 740, 16), 135);
			
			g.drawString("To Next Lv:", 540, 156);
			s = selected.expToNextLevel() + "";
			g.drawString(s, Global.rightX(s, 740, 16), 156);
			
			g.drawString(selected.getAbility().getName(), 427, 179);
			
			s = selected.getActualHeldItem().getName();
			g.drawString(s, Global.rightX(s, 740, 16), 179);
			
			g.drawString(selected.getNature().getName() + " Nature", 427, 198);
			g.drawString(selected.getCharacteristic(), 427, 217);
			
			List<Move> moves = selected.getActualMoves();
			for (int i = 0; i < moves.size(); i++)
			{
				int x = i%2 == 0 ? 421 : 590;
				int y = i/2 == 0 ? 238 : 277;
				
				g.setColor(moves.get(i).getAttack().getActualType().getColor());
				g.fillRect(x, y, 159, 31);
				g.drawImage(tiles.getTile(0x35), x, y, null);
				g.setColor(Color.BLACK);
				s = moves.get(i).getAttack().getName();
				g.drawString(s, Global.centerX(s, x + 77, 16) - 3, y + 20);
			}
			
			g.drawString("Stat", Global.rightX("Stat", 635, 16), 340);
			g.drawString("IV", Global.rightX("IV", 681, 16), 340);
			g.drawString("EV", Global.rightX("EV", 735, 16), 340);
			
			int[] stats = selected.getStats();
			int[] ivs = selected.getIVs();
			int[] evs = selected.getEVs();
			for (int i = 0; i < Stat.NUM_STATS; i++)
			{
				g.setFont(Global.getFont(16));
				g.setColor(selected.getNature().getColor(i));
				g.drawString(Stat.getStat(i, false).getName(), 427, 360 + i*18 + i/2);
				
				g.setFont(Global.getFont(14));
				g.setColor(Color.BLACK);
				g.drawString(stats[i] + "", Global.rightX(stats[i] + "", 635, 14), 360 + i*18 + i/2);
				g.drawString(ivs[i] + "", Global.rightX(ivs[i] + "", 681, 14), 360 + i*18 + i/2);
				g.drawString(evs[i] + "", Global.rightX(evs[i] + "", 735, 14), 360 + i*18 + i/2);
			}
		}
		
		// Buttons
		g.setFont(Global.getFont(20));
		s = "Switch";
		g.drawString(s, Global.centerX(s, 464, 20), 489);
		s = party ? "Deposit" : "Withdraw";
		g.drawString(s, Global.centerX(s, 584, 20), 489);
		s = "Release";
		g.drawString(s, Global.centerX(s, 699, 20), 489);
		
		g.drawImage(tiles.getTile(0x36), 410, 522, null);
		g.drawString("Return", Global.centerX("Return", 584, 20), 546);
		
		for (Button b : buttons) b.draw(g);
	}

	public ViewMode getViewModel()
	{
		return Game.ViewMode.PC_VIEW;
	}

	public void movedToFront(Game game) 
	{
		party = true;
		selected = player.front();
		updateActiveButtons();
	}
	
	private void greyOut(Graphics g, Button b, boolean totesBlacks)
	{
		Color temp = g.getColor();
		g.setColor(totesBlacks ? Color.BLACK : g.getColor().darker());
		g.fillRect(b.x, b.y, b.width, b.height);
		g.setColor(temp);
	}

	private void updateActiveButtons()
	{
		ActivePokemon[][] box = player.getPC().getBoxPokemon();
		for (int i = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++)
			{
				boxButtons[i][j].setActive((party && depositClicked) || switchClicked || box[i][j] != null);
			}
		}
		
		party = false;
		List<ActivePokemon> team = player.getTeam();
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)			
		{
			partyButtons[i].setActive(i < team.size());
			if (i < team.size() && team.get(i) == selected) party = true;
		}
		
		if (party) depositWithdrawButton.setActive(player.canDeposit(selected));
		else depositWithdrawButton.setActive(team.size() < Trainer.MAX_POKEMON);
		
		releaseButton.setActive(!party || team.size() > 1);
	}
}
