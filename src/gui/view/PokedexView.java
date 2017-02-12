package gui.view;

import gui.GameData;
import gui.TileSet;
import gui.button.Button;
import gui.button.ButtonHoverAction;
import gui.panel.BasicPanels;
import gui.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import map.Direction;
import pokemon.PC;
import pokemon.PokemonInfo;
import trainer.pokedex.Pokedex;
import type.Type;
import util.DrawUtils;
import util.FontMetrics;
import util.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

class PokedexView extends View {
	private static final int PER_PAGE = PC.BOX_HEIGHT*PC.BOX_WIDTH;
	private static final int NUM_PAGES = PokemonInfo.NUM_POKEMON/PER_PAGE;
	
	private static final int NUM_BUTTONS = PER_PAGE + 3;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 2;
	private static final int LEFT_ARROW = NUM_BUTTONS - 3;

	private final DrawPanel pokedexPanel;
	private final DrawPanel titlePanel;
	private final DrawPanel countPanel;
	private final DrawPanel infoPanel;
	private final DrawPanel imagePanel;
	private final DrawPanel basicInfoPanel;
	private final DrawPanel descriptionPanel;
	private final DrawPanel locationPanel;

	private final Pokedex pokedex;

	private PokemonInfo selected;
	private int selectedButton;
	private int pageNum;

	private int numSeen;
	private int numCaught;
	
	private final Button[] buttons;
	private final Button[][] pokemonButtons;
	private final Button leftButton;
	private final Button rightButton;
	private final Button returnButton;
	
	PokedexView() {
		pokedexPanel = new DrawPanel(40, 40, 350, 418)
				.withBackgroundColor(Color.BLUE)
				.withTransparentCount(2)
				.withBorderPercentage(0)
				.withBlackOutline();

		titlePanel = new DrawPanel(pokedexPanel.x, pokedexPanel.y, pokedexPanel.width, 37)
				.withBackgroundColor(null)
				.withBlackOutline();

		countPanel = new DrawPanel(pokedexPanel.x, 478, pokedexPanel.width, 82)
				.withBackgroundColor(Color.RED)
				.withTransparentCount(2)
				.withBorderPercentage(0)
				.withBlackOutline();

		infoPanel = new DrawPanel(410, pokedexPanel.y, pokedexPanel.width, 462)
				.withTransparentBackground()
				.withBorderPercentage(0)
				.withBlackOutline();

		basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 190)
				.withFullTransparency()
				.withBlackOutline();

		int locationPanelHeight = 148;
		locationPanel = new DrawPanel(
				infoPanel.x,
				infoPanel.y + infoPanel.height - locationPanelHeight - DrawUtils.OUTLINE_SIZE,
				infoPanel.width,
				locationPanelHeight + DrawUtils.OUTLINE_SIZE)
				.withFullTransparency()
				.withBlackOutline();

		descriptionPanel = new DrawPanel(
				infoPanel.x,
				basicInfoPanel.y + basicInfoPanel.height - DrawUtils.OUTLINE_SIZE,
				infoPanel.width,
				infoPanel.height - basicInfoPanel.height - locationPanel.height + 2
						*DrawUtils.OUTLINE_SIZE)
				.withFullTransparency()
				.withBlackOutline();

		imagePanel = new DrawPanel(
				infoPanel.x + 18,
				infoPanel.y + 18,
				104,
				104)
				.withFullTransparency()
				.withBlackOutline();

		this.pokedex = Game.getPlayer().getPokedex();
		selectedButton = 0;
		pageNum = 0;
		
		buttons = new Button[NUM_BUTTONS];
		pokemonButtons = new Button[PC.BOX_HEIGHT][PC.BOX_WIDTH];
		for (int i = 0, k = 0; i < PC.BOX_HEIGHT; i++) {
			for (int j = 0; j < PC.BOX_WIDTH; j++, k++) {
				buttons[k] = pokemonButtons[i][j] = new Button(
						60 + 54*j,
						96 + 54*i,
						40,
						40,
						ButtonHoverAction.BOX,
						Button.getBasicTransitions(k, PC.BOX_HEIGHT, PC.BOX_WIDTH, 0, new int[] { RETURN, RIGHT_ARROW, -1, RIGHT_ARROW })
				);
			}
		}
		
		buttons[LEFT_ARROW] = leftButton = new Button(140, 418, 35, 20, ButtonHoverAction.BOX, new int[] { RIGHT_ARROW, PC.BOX_WIDTH*(PC.BOX_HEIGHT - 1) + PC.BOX_WIDTH/2 - 1, -1, 0 });
		buttons[RIGHT_ARROW] = rightButton = new Button(255, 418, 35, 20, ButtonHoverAction.BOX, new int[] { RETURN, PC.BOX_WIDTH*(PC.BOX_HEIGHT - 1) + PC.BOX_WIDTH/2, LEFT_ARROW, 0 });
		
		buttons[RETURN] = returnButton = new Button(410, 522, 350, 38, ButtonHoverAction.BOX, new int[] { 0, -1, RIGHT_ARROW, -1 });
		
		selected = PokemonInfo.getPokemonInfo(1);
	}

	@Override
	public void update(int dt) {
		selectedButton = Button.update(buttons, selectedButton);

		for (int i = 0; i < PC.BOX_HEIGHT; i++) {
			for (int j = 0; j < PC.BOX_WIDTH; j++) {
				if (pokemonButtons[i][j].checkConsumePress()) {
					selected = PokemonInfo.getPokemonInfo(getIndex(j, i) + 1);
				}
			}
		}
		
		if (leftButton.checkConsumePress()) {
			if (pageNum == 0) {
				pageNum = NUM_PAGES - 1;
			}
			else {
				pageNum--;
			}
		}
		
		if (rightButton.checkConsumePress()) {
			if (pageNum == NUM_PAGES - 1) {
				pageNum = 0;
			}
			else {
				pageNum++;
			}
		}
				
		if (returnButton.checkConsumePress()) {
			Game.instance().setViewMode(ViewMode.MAP_VIEW);
		}
		
		if (InputControl.instance().consumeIfDown(ControlKey.ESC)) {
			Game.instance().setViewMode(ViewMode.MAP_VIEW);
		}
	}

	@Override
	public void draw(Graphics g) {
		GameData data = Game.getData();

		TileSet partyTiles = data.getPartyTiles();
		TileSet pokedexTiles = data.getPokedexTilesSmall();

		BasicPanels.drawCanvasPanel(g);

		pokedexPanel.drawBackground(g);
		titlePanel.drawBackground(g);
		titlePanel.label(g, 20, PokeString.POKEDEX);
		
		for (int i = 0; i < PC.BOX_HEIGHT; i++) {
			for (int j = 0; j < PC.BOX_WIDTH; j++) {
				int number = getIndex(i, j) + 1;
				PokemonInfo p = PokemonInfo.getPokemonInfo(number);
				Button pokemonButton = pokemonButtons[j][i];

				if (pokedex.isNotSeen(p.namesies())) {
					pokemonButton.label(g, 20, new Color(0, 0, 0, 64), String.format("%03d", number));
				}
				else {
					if (p == selected) {
						pokemonButton.blackOutline(g);
					}

					pokemonButton.imageLabel(g, partyTiles.getTile(number));

					if (pokedex.isCaught(p.namesies())) {
						BufferedImage pokeball = TileSet.TINY_POKEBALL;
						g.drawImage(
								pokeball,
								pokemonButton.x + pokemonButton.width - 3*pokeball.getWidth()/2,
								pokemonButton.y + pokemonButton.height - 3*pokeball.getHeight()/2,
								null
						);
					}
				}
			}
		}
		
		// Draw page numbers and arrows
		g.setColor(Color.BLACK);
		FontMetrics.setFont(g, 16);
		DrawUtils.drawCenteredWidthString(g, (pageNum + 1) + "/" + NUM_PAGES, pokedexPanel.centerX(), 433);

		leftButton.drawArrow(g, Direction.LEFT);
		rightButton.drawArrow(g, Direction.RIGHT);
		
		// Seen/Caught
		countPanel.drawBackground(g);
		countPanel.label(g, 20, "Seen: " + numSeen + "     Caught: " + numCaught);
		
		// Description
		Type[] type = selected.getType();
		Color[] typeColors = Type.getColors(type);

		boolean notSeen = pokedex.isNotSeen(selected.namesies());
		boolean caught = pokedex.isCaught(selected.namesies());

		if (notSeen) {
			typeColors = new Color[] { Color.BLACK, Color.BLACK };	
		}

		infoPanel.withBackgroundColors(typeColors)
				.drawBackground(g);

		basicInfoPanel.drawBackground(g);
		descriptionPanel.drawBackground(g);
		locationPanel.drawBackground(g);

		imagePanel.drawBackground(g);
		if (notSeen) {
			imagePanel.label(g, 80, "?");
		}
		else {
			BufferedImage pkmImg = pokedexTiles.getTile(selected.getNumber());
			pkmImg.setRGB(0, 0, 0);

			imagePanel.imageLabel(g, pkmImg);
		}
		
		g.setColor(Color.BLACK);
		FontMetrics.setFont(g, 20);
		g.drawString(notSeen ? "?????" : selected.getName(), 541, 82);
		DrawUtils.drawRightAlignedString(g, "#" + String.format("%03d", selected.getNumber()), 740, 82);
		
		if (!notSeen) {
			FontMetrics.setFont(g, 16);
			g.drawString("Type:", 541, 110);

			g.drawImage(type[0].getImage(), 596, 98, null);
			if (type[1] != Type.NO_TYPE) {
				g.drawImage(type[1].getImage(), 596 + 707 - 669, 98, null);
			}
			
			g.drawString((!caught ? "???" : selected.getClassification()) + " " + PokeString.POKEMON, 427, 179);
			g.drawString("Height: " + (!caught ? "???'??\"" : selected.getHeightString()), 427, 198);
			g.drawString("Weight: " + (!caught ? "???.?" : selected.getWeight()) + " lbs", 427, 217);
			
			if (caught) {
				descriptionPanel.drawMessage(g, 16, selected.getFlavorText());
			}

			int locationX = locationPanel.x + locationPanel.getTextSpace(g);
			g.drawString("Locations:", locationX, locationPanel.y + FontMetrics.getDistanceBetweenRows(g));
			List<String> locations = pokedex.getLocations(selected.namesies());
			for (int i = 0; i < locations.size(); i++) {
				g.drawString(locations.get(i), locationX, locationPanel.y + (i + 2)*FontMetrics.getDistanceBetweenRows(g));
			}	
		}
		
		// Return button
		returnButton.fillTransparent(g, Color.YELLOW);
		returnButton.fillTransparent(g);
		returnButton.blackOutline(g);
		returnButton.label(g, 20, "Return");
		
		for (Button button : buttons) {
			button.draw(g);
		}
	}
	
	private int getIndex(int i, int j) {
		return PER_PAGE*pageNum + j*PC.BOX_WIDTH + i;
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.POKEDEX_VIEW;
	}

	@Override
	public void movedToFront() {
		selected = PokemonInfo.getPokemonInfo(1);
		numSeen = pokedex.numSeen();
		numCaught = pokedex.numCaught();
	}
}
