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
import pokemon.PC;
import pokemon.PokemonInfo;
import trainer.Pokedex;
import trainer.Pokedex.PokedexStatus;

public class PokedexView extends View
{
	private static final int PER_PAGE = PC.BOX_HEIGHT*PC.BOX_WIDTH;
	private static final int NUM_PAGES = PokemonInfo.NUM_POKEMON/PER_PAGE;
	
	private static final int NUM_BUTTONS = PER_PAGE + 3;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 2;
	private static final int LEFT_ARROW = NUM_BUTTONS - 3;

	private Pokedex pokedex;
	private PokemonInfo selected;
	private int selectedButton;
	private int pageNum;
	
	private Button[] buttons;
	private Button[][] boxButtons;
	private Button leftButton;
	private Button rightButton;
	private Button returnButton;
	
	public PokedexView(Pokedex pokedex)
	{
		this.pokedex = pokedex;
		selectedButton = 0;
		pageNum = 0;
		
		buttons = new Button[NUM_BUTTONS];
		boxButtons = new Button[PC.BOX_HEIGHT][PC.BOX_WIDTH];
		for (int i = 0, k = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++, k++)
			{
				buttons[k] = boxButtons[i][j] = new Button(60 + 54*j, 96 + 54*i, 40, 40, Button.HoverAction.BOX, 
						new int[] {j == PC.BOX_WIDTH - 1 ? RETURN : k + 1, // Right 
								i == 0 ? PER_PAGE + j : k - PC.BOX_WIDTH, // Up
								j == 0 ? RETURN : k - 1, // Left
								i == PC.BOX_HEIGHT - 1 ? (j < PC.BOX_WIDTH/2 ? LEFT_ARROW : RIGHT_ARROW) : k + PC.BOX_WIDTH}); // Down
			}
		}
		
		buttons[LEFT_ARROW] = leftButton = new Button(140, 418, 35, 20, Button.HoverAction.BOX, new int[] {RIGHT_ARROW, PC.BOX_WIDTH*(PC.BOX_HEIGHT - 1) + PC.BOX_WIDTH/2 - 1, -1, 0});
		buttons[RIGHT_ARROW] = rightButton = new Button(255, 418, 35, 20, Button.HoverAction.BOX, new int[] {RETURN, PC.BOX_WIDTH*(PC.BOX_HEIGHT - 1) + PC.BOX_WIDTH/2, LEFT_ARROW, 0});
		
		buttons[RETURN] = returnButton = new Button(410, 522, 350, 38, Button.HoverAction.BOX, new int[] {0, -1, RIGHT_ARROW, -1});
		
		selected = PokemonInfo.getPokemonInfo(1);
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
					selected = PokemonInfo.getPokemonInfo(getIndex(j, i) + 1);
				}
			}
		}
		
		if (leftButton.checkConsumePress())
		{
			if (pageNum == 0) pageNum = NUM_PAGES - 1; 
			else pageNum--;
		}
		
		if (rightButton.checkConsumePress())
		{
			if (pageNum == NUM_PAGES - 1) pageNum = 0;
			else pageNum++;
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
		
		// Box
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		g.setColor(Color.BLUE);
		g.fillRect(40, 40, 350, 418);
		g.drawImage(tiles.getTile(0x31), 40, 40, null);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		String s = "Pok\u00e9dex";
		g.drawString(s, Global.centerX(s, 214, 20), 65);
		
		for (int i = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++)
			{
				int number = getIndex(i, j) + 1;
				PokemonInfo p = PokemonInfo.getPokemonInfo(number);
				
				g.translate(boxButtons[j][i].x, boxButtons[j][i].y);

				if (pokedex.getStatus(p.namesies()) == PokedexStatus.NOT_SEEN)
				{
					g.setColor(new Color(0, 0, 0, 64));
					s = String.format("%03d", number);
					g.drawString(s, Global.centerX(s, 18, 20), 27);
				}
				else
				{
					if (p == selected)
					{
						g.drawImage(tiles.getTile(0x32), 0, 0, null);
					}
					
					BufferedImage pkmImg = partyTiles.getTile(number);
					g.drawImage(pkmImg, 20 - pkmImg.getWidth()/2, 20 - pkmImg.getHeight()/2, null);
					
					if (pokedex.getStatus(p.namesies()) == PokedexStatus.CAUGHT)
					{
						g.drawImage(typeTiles.getTile(0x4), 28, 28, null);
					}
				}
				
				g.translate(-boxButtons[j][i].x, -boxButtons[j][i].y);
			}
		}
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(16));
		s = (pageNum + 1) + "/" + NUM_PAGES;
		g.drawString(s, Global.centerX(s, 215, 20), 433);
		
		View.drawArrows(g, leftButton, rightButton);
		
		// Seen/Caught
		g.setColor(Color.RED);
		g.fillRect(40, 478, 350, 82);
		g.drawImage(tiles.getTile(0x33), 40, 478, null);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		g.drawString("Seen: " + pokedex.numSeen(), 70, 524);
		g.drawString("Caught: " + pokedex.numCaught(), 70 + 54*3, 524);
		
		// Description
		PokedexStatus status = pokedex.getStatus(selected.namesies());

		Type[] type = selected.getType();
		Color[] typeColors = Type.getColors(type);
		
		if (status == PokedexStatus.NOT_SEEN)
		{
			typeColors = new Color[] {Color.BLACK, Color.BLACK};	
		}
		
		g.setColor(typeColors[0]);
		g.fillPolygon(new int[] {410, 759, 759, 410}, new int[] {40, 40, 96, 445}, 4);
		g.setColor(typeColors[1]);
		g.fillPolygon(new int[] {410, 759, 759, 410}, new int[] {445, 96, 501, 501}, 4);
		
		g.drawImage(tiles.getTile(0x34), 410, 40, null);
		if (status == PokedexStatus.NOT_SEEN)
		{
			g.setColor(Color.BLACK);
			g.setFont(Global.getFont(80));
			g.drawString("?", 455, 137);
		}
		else
		{
			BufferedImage pkmImg = pokemonTiles.getTile(selected.getImageNumber(false));
			pkmImg.setRGB(0, 0, 0);
			g.drawImage(pkmImg, 479 - pkmImg.getWidth()/2, 109 - pkmImg.getHeight()/2, null);
		}
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		g.drawString(status == PokedexStatus.NOT_SEEN ? "?????" : selected.getName(), 541, 82);
		s = "#" + String.format("%03d", selected.getNumber());
		g.drawString(s, Global.rightX(s, 740, 20), 82);
		
		if (status != PokedexStatus.NOT_SEEN)
		{
			g.setFont(Global.getFont(16));
			g.drawString("Type:", 541, 110);
			
			g.drawImage(typeTiles.getTile(type[0].getImageIndex()), 596, 98, null);
			if (type[1] != Type.NONE) g.drawImage(typeTiles.getTile(type[1].getImageIndex()), 596 + 707 - 669, 98, null);
			
			g.drawString((status == PokedexStatus.SEEN ? "???" : selected.getClassification()) + " Pok\u00e9mon", 427, 179);
			g.drawString("Height: " + (status == PokedexStatus.SEEN ? "???'??\"" : String.format("%d'%02d\"", selected.getHeight()/12, selected.getHeight()%12)), 427, 198);
			g.drawString("Weight: " + (status == PokedexStatus.SEEN ? "???.?" : selected.getWeight()) + " lbs", 427, 217);
			
			if (status == PokedexStatus.CAUGHT) Global.drawWrappedText(g, selected.getFlavorText(), 427, 247, 350, 10, 21);
			
			g.drawString("Locations:", 427, 340);
			List<String> locations = pokedex.getLocations(selected.namesies());
			for (int i = 0; i < locations.size(); i++)
			{
				g.setFont(Global.getFont(16));
				g.drawString(locations.get(i), 457, 360 + i*18 + i/2);
			}	
		}
		
		// Buttons
		g.setFont(Global.getFont(20));
		
		if (status != PokedexStatus.NOT_SEEN)
		{
			BufferedImage pkmImg = partyTiles.getTile(selected.getNumber());
			for (int i = 0; i < 3; i++) g.drawImage(pkmImg, 464 + 120*i - pkmImg.getWidth()/2, 478 - pkmImg.getHeight()/2, null);			
		}
		
		g.drawImage(tiles.getTile(0x36), 410, 522, null);
		g.drawString("Return", Global.centerX("Return", 584, 20), 546);
		
		for (Button b : buttons) b.draw(g);
	}
	
	private int getIndex(int i, int j)
	{
		return PER_PAGE*pageNum + j*PC.BOX_WIDTH + i;
	}

	public ViewMode getViewModel()
	{
		return Game.ViewMode.POKEDEX_VIEW;
	}

	public void movedToFront(Game game) 
	{
		selected = PokemonInfo.getPokemonInfo(1);
	}
}
