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
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Trainer;
import battle.Move;

public class PartyView extends View
{
	private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + Move.MAX_MOVES + 2;
	private static final int MOVES = Trainer.MAX_POKEMON;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int SWITCH = NUM_BUTTONS - 2;
	
	private CharacterData charData;
	private final int[] primaryColorx = {41, 633, 167,  41};
	private final int[] primaryColory = {93,  93, 559, 559};
	private final int[] secondaryColorx = {633, 759, 759, 167};
	private final int[] secondaryColory = { 93,  93, 559, 559};
	
	private Button[] buttons, tabButtons, moveButtons;
	private Button switchButton, returnButton;
	private int selectedTab;
	private int selectedButton;
	private int switchTabIndex;
	
	public PartyView(CharacterData data)
	{
		charData = data;
		selectedTab = 0;
		selectedButton = 0;
		switchTabIndex = -1;
		
		buttons = new Button[NUM_BUTTONS];
		tabButtons = new Button[Trainer.MAX_POKEMON];
		moveButtons = new Button[Move.MAX_MOVES];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) //r u l d
		{
			buttons[i] = tabButtons[i] = new Button(39 + i*120, 39, 122, 55, Button.HoverAction.BOX, 
					new int[] {i == Trainer.MAX_POKEMON - 1 ? 0 : i + 1, // Right 
							i < Trainer.MAX_POKEMON ? SWITCH: RETURN, // Up
							i == 0? Trainer.MAX_POKEMON - 1 : i - 1, // Left
							MOVES}); // Down
		}
		
		for (int i = 0; i < Move.MAX_MOVES; i++)
		{
			buttons[MOVES + i] = moveButtons[i] = new Button(426, 266 + i*49, 293, 40, Button.HoverAction.BOX, 
					new int[] {-1, // Right
							i == 0 ? 0 : MOVES + i - 1, // Up 
							SWITCH, // Left
							i == Move.MAX_MOVES - 1 ? RETURN : MOVES + i + 1}); // Down
		}
		
		buttons[10] = switchButton = new Button(69, 493, 317, 38, Button.HoverAction.BOX, new int[] {RETURN, MOVES + Move.MAX_MOVES - 1, RETURN, 0});
		buttons[11] = returnButton = new Button(414, 493, 317, 38, Button.HoverAction.BOX, new int[] {SWITCH, MOVES + Move.MAX_MOVES - 1, SWITCH, 0});
		updateActiveButtons();
	}

	@Override
	public void update(int dt, InputControl input, Game game) 
	{
		selectedButton = Button.update(buttons, selectedButton, input);
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			if (tabButtons[i].checkConsumePress())
			{
				if (switchTabIndex != -1)
				{
					charData.swapPokemon(i, switchTabIndex);
					selectedTab = i;
					switchTabIndex = -1;
				}
				else selectedTab = i;
				
				updateActiveButtons();
			}
		}
		
		if (returnButton.checkConsumePress())
		{
			game.setViewMode(ViewMode.MAP_VIEW);
		}
		
		if (switchButton.checkConsumePress())
		{
			switchTabIndex = switchTabIndex == -1 ? selectedTab : -1;
			updateActiveButtons();
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
		
		// Background
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		
		List<ActivePokemon> list = charData.getTeam();
		ActivePokemon selectedPkm = list.get(selectedTab);
		
		Type[] type = selectedPkm.getActualType();
		Color[] typeColors = Type.getColors(selectedPkm);
		
		// Type Polygons
		g.setColor(selectedTab == Trainer.MAX_POKEMON - 1 ? typeColors[1] : typeColors[0]);
		g.fillPolygon(primaryColorx, primaryColory, 4);
		
		g.setColor(selectedTab == Trainer.MAX_POKEMON - 1 ? typeColors[0] : typeColors[1]);
		g.fillPolygon(secondaryColorx, secondaryColory, 4);
		
		// Tabs
		if (!selectedPkm.canFight()) g.drawImage(tiles.getTile(0x12), 39, 92, null);
		else g.drawImage(tiles.getTile(0x11), 39, 92, null);
		
		// Info Boxes
		g.drawImage(tiles.getTile(0x13), 69, 122, null);
		
		// Draw Pokemon Image
		TileSet pkmTiles = data.getPokemonTilesSmall();
		BufferedImage pkmImg = pkmTiles.getTile(selectedPkm.isEgg() ? 0x10000 : selectedPkm.getPokemonInfo().getImageNumber(selectedPkm.isShiny()));
		g.drawImage(pkmImg, 121 - pkmImg.getWidth()/2, 174 - pkmImg.getHeight()/2, null);
		
		if (selectedPkm.isEgg())
		{
			g.setFont(Global.getFont(20));
			g.setColor(Color.BLACK);
			
			// Name
			g.drawString(selectedPkm.getName(), 213, 147);
			
			g.setFont(Global.getFont(16));
			
			// Description
			g.drawString(selectedPkm.getEggMessage(), 213, 170);
		}
		else
		{
			g.setFont(Global.getFont(20));
			g.setColor(Color.BLACK);
			
			// Name and Gender
			g.drawString(selectedPkm.getName() + " " + selectedPkm.getGender().getCharacter(), 213, 147);
			
			// Number
			g.drawString("#" + String.format("%03d", selectedPkm.getPokemonInfo().getNumber()), 378, 147);
			
			// Status Condition
			g.drawString(selectedPkm.getStatus().getType().getName(), 459, 147);
			
			// Level
			g.drawString("Lv" + selectedPkm.getLevel(), 525, 147);
			
			// Type Tiles
			if (type[1].equals(Type.NONE))
			{
				g.drawImage(typeTiles.getTile(type[0].getImageIndex()), 687, 133, null);
			}
			else
			{
				g.drawImage(typeTiles.getTile(type[0].getImageIndex()), 647, 133, null);
				g.drawImage(typeTiles.getTile(type[1].getImageIndex()), 687, 133, null);
			}
			
			g.setFont(Global.getFont(16));
			
			// Nature
			g.drawString(selectedPkm.getNature().getName() +" Nature", 213, 170);
			
			// Total EXP
			g.drawString("EXP:", 525, 170);
			String expStr = "" + selectedPkm.getTotalEXP();
			g.drawString(expStr, 718 - expStr.length()*7, 170);
			
			// Characteristic
			g.drawString(selectedPkm.getCharacteristic(), 213, 190);
			
			// EXP To Next Level
			g.drawString("To Next Lv:", 525, 190);
			String expToNextLvStr = "" + selectedPkm.expToNextLevel();
			g.drawString(expToNextLvStr, 718 - expToNextLvStr.length()*7, 190);
			
			// Held Item
			g.drawString(selectedPkm.getActualHeldItem().getName(), 213, 211);
			
			// Ability with description
			String abilityStr = selectedPkm.getActualAbility().getName() + " - ";
			g.drawString(abilityStr, 81, 272);
			g.setFont(Global.getFont(12));
			int dWidth = 81 + abilityStr.length()*9, dIndex = 0;
			StringBuilder dStr = new StringBuilder();
			String[] discriptionSplit = selectedPkm.getActualAbility().getDescription().split(" ");

			while (dIndex < discriptionSplit.length && dWidth+ (dStr.length() + discriptionSplit[dIndex].length() + 1)*4 < 300)
			{
				dStr.append(" " + discriptionSplit[dIndex]);
				dIndex++;
			}
			
			g.drawString(dStr.toString(), dWidth, 272);
			dStr = new StringBuilder();
			while (dIndex < discriptionSplit.length) dStr.append(discriptionSplit[dIndex++] + " ");
			g.drawString(dStr.toString(), 81, 290);
			
			// EXP Bar
			g.setColor(Global.EXP_BAR_COLOR);
			g.fillRect(527, 214, (int)(202*selectedPkm.expRatio()), 10);
			
			// HP Bar
			g.setColor(selectedPkm.getHPColor());
			g.fillRect(71, 329, (int)(155*selectedPkm.getHPRatio()), 10);
			
			g.setFont(Global.getFont(16));
			g.setColor(Color.BLACK);
			
			// Stats Box
			g.drawString("Stat", 250, 340);
			g.drawString("IV", 310, 340);
			g.drawString("EV", 355, 340);
			
			for (int i = 0; i < Stat.NUM_STATS; i++)
			{
				g.setColor(selectedPkm.getNature().getColor(i));
				g.drawString(Stat.getStat(i, false).getName(), 80, 19*i + 358);
			}
			
			int[] stats = selectedPkm.getStats();
			int[] ivs = selectedPkm.getIVs();
			int[] evs = selectedPkm.getEVs();
			
			g.setColor(Color.BLACK);
			g.setFont(Global.getFont(14));
			for (int i = 0; i < Stat.NUM_STATS; i++)
			{
				String valStr = "" + stats[i];
				if (i == Stat.HP.index()) valStr = selectedPkm.getHP() + "/" + stats[i];
				g.drawString(valStr, 285 - valStr.length()*8, 19*i + 358);
				
				valStr = "" + ivs[i];
				g.drawString(valStr, 327 - valStr.length()*8, 19*i + 358);
				
				valStr = "" + evs[i];
				g.drawString(valStr, 371 - valStr.length()*8, 19*i + 358);
			}
			
			// Move Box
			List<Move> moves = selectedPkm.getActualMoves();
			for (int i = 0; i < moves.size(); i++)
			{
				g.translate(moveButtons[i].x, moveButtons[i].y);
				
				Move move = moves.get(i);
				g.setColor(move.getAttack().getActualType().getColor());
				g.fillRect(0, 0, 293, 40);
				g.drawImage(tiles.getTile(0x18), 0, 0, null);
				
				g.setColor(Color.BLACK);
				if (selectedButton == MOVES + i)
				{
					g.setFont(Global.getFont(10));
					Global.drawWrappedText(g, move.getAttack().getName() + " - " + move.getAttack().getDescription(), 
							6, 11, 280, 6, 11);
				}
				else
				{
					g.setFont(Global.getFont(14));
					g.drawString(move.getAttack().getName(), 7, 16);
					g.drawString("PP:", 133, 16);
					String ppStr = move.getPP() + "/" + move.getMaxPP();
					g.drawString(ppStr, 205 - ppStr.length()*8, 16);
					
					g.setFont(Global.getFont(12));
					g.drawString("Accuracy:", 7, 32);
					String accStr = "" + move.getAttack().getAccuracyString();
					g.drawString(accStr, 100 - accStr.length()*8, 32);
					
					g.drawString("Power:", 133, 32);
					String powStr = move.getAttack().getPowerString();
					g.drawString(powStr, 205 - powStr.length()*8, 32);
					
					BufferedImage typeImage = typeTiles.getTile(move.getAttack().getActualType().getImageIndex());
					g.drawImage(typeImage, 241, 4, null);
					
					BufferedImage categoryImage = typeTiles.getTile(move.getAttack().getCategory().getImageNumber());
					g.drawImage(categoryImage, 241, 20, null);
				}
				
				g.translate(-moveButtons[i].x, -moveButtons[i].y);
			}
		}
		
		// Switch Box
		if (switchTabIndex != -1 || list.size() == 1)
		{ 
			g.drawImage(tiles.getTile(0x17), 69, 493, null);
			g.setColor(new Color(0, 0, 0, 128));
			g.fillRect(69, 493, 317, 38);
		}
		else
		{
			g.drawImage(tiles.getTile(0x16), 69, 493, null);
		}
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		g.drawString("Switch!", 190, 518);
		
		// Return Box
		g.drawImage(tiles.getTile(0x16), 414, 493, null);
		g.drawString("Return", 547, 518);
		
		TileSet partyTiles = data.getPartyTiles();
		g.setFont(Global.getFont(14));
		
		// Tabs
		for (int i = 0; i < list.size(); i++)
		{
			ActivePokemon pkm = list.get(i);
			g.translate(tabButtons[i].x, tabButtons[i].y);
			typeColors = Type.getColors(pkm);
			g.setColor(typeColors[0]);
			g.fillRect(0, 0, 122, 55);
			
			if (selectedTab == i) g.drawImage(tiles.getTile(0x14), 0 ,0, null);
			else g.drawImage(tiles.getTile(0x15), 0, 0, null);
			
			g.setColor(Color.BLACK);
			g.drawString(pkm.getName(), 40, 34);
			
			pkmImg = partyTiles.getTile(pkm.getPokemonInfo().getNumber());
			if (pkm.isEgg()) pkmImg = partyTiles.getTile(0x10000);
			g.drawImage(pkmImg, 19 - pkmImg.getWidth()/2, 26 - pkmImg.getHeight()/2, null);
			
			if (!pkm.canFight())
			{
				g.setColor(new Color(0, 0, 0, 128));
				g.fillRect(0, 0, 122, 55);
			}
			
			g.translate(-tabButtons[i].x, -tabButtons[i].y);
		}
		
		for (Button b: buttons) b.draw(g);
	}
	
	private void updateActiveButtons()
	{
		List<ActivePokemon> team = charData.getTeam();
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			tabButtons[i].setActive(i < team.size());
		}
		
		ActivePokemon pkm = team.get(selectedTab);
		List<Move> moves = pkm.getMoves();
		for (int i = 0; i < Move.MAX_MOVES; i++)
		{
			moveButtons[i].setActive(!pkm.isEgg() && i < moves.size());
		}
		
		switchButton.setActive(team.size() > 1);
	}

	public ViewMode getViewModel() 
	{
		return ViewMode.PARTY_VIEW;
	}

	public void movedToFront() 
	{
		selectedTab = 0;
		selectedButton = 0;
		switchTabIndex = -1;
		updateActiveButtons();
	}
}
