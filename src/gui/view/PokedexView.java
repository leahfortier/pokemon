package gui.view;

import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.panel.BasicPanels;
import draw.button.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import map.Direction;
import pokemon.Gender;
import pokemon.PokemonInfo;
import trainer.pokedex.Pokedex;
import type.Type;
import util.FontMetrics;
import util.PokeString;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class PokedexView extends View {
	private static final int NUM_COLS = 6;
	private static final int NUM_ROWS = 6;

	private static final int PER_PAGE = NUM_ROWS * NUM_COLS;
	private static final int NUM_PAGES = (int)Math.ceil((double)PokemonInfo.NUM_POKEMON/PER_PAGE);
	private static final int NUM_TAB_BUTTONS = TabInfo.values().length;

	private static final int NUM_BUTTONS = PER_PAGE + NUM_TAB_BUTTONS + 3;

	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 2;
	private static final int LEFT_ARROW = NUM_BUTTONS - 3;

	private final DrawPanel pokedexPanel;
	private final DrawPanel titlePanel;
	private final DrawPanel countPanel;
	private final DrawPanel infoPanel;
	private final DrawPanel imagePanel;
	private final DrawPanel basicInfoPanel;

	private final Pokedex pokedex;

	private PokemonInfo selected;
	private int selectedButton;
	private int selectedTab;
	private int pageNum;

	private int numSeen;
	private int numCaught;
	
	private final Button[] buttons;
	private final Button[][] pokemonButtons;
	private final Button[] tabButtons;
	private final Button leftButton;
	private final Button rightButton;
	private final Button returnButton;

	private enum TabInfo {
		MAIN,
		STATS,
		MOVES,
		EVOLUTION,
		LOCATION;

		private final String label;

		TabInfo() {
			this.label = StringUtils.properCase(this.name().toLowerCase());
		}
	}

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
				.withTransparentCount(2)
				.withBorderPercentage(0)
				.withBlackOutline();

		imagePanel = new DrawPanel(
				infoPanel.x + 18,
				infoPanel.y + 18,
				104,
				104)
				.withFullTransparency()
				.withBlackOutline();

		basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 200)
				.withBackgroundColor(null)
				.withBorderPercentage(0)
				.withBlackOutline();

		this.pokedex = Game.getPlayer().getPokedex();
		selectedButton = 0;
		selectedTab = TabInfo.MAIN.ordinal();
		pageNum = 0;

		buttons = new Button[NUM_BUTTONS];
		pokemonButtons = new Button[NUM_ROWS][NUM_COLS];
		for (int i = 0, k = 0; i < NUM_ROWS; i++) {
			for (int j = 0; j < NUM_COLS; j++, k++) {
				buttons[k] = pokemonButtons[i][j] = new Button(
						60 + 54*j,
						96 + 54*i,
						40,
						40,
						ButtonHoverAction.BOX,
						Button.getBasicTransitions(k, NUM_ROWS, NUM_COLS, 0, new int[] { RETURN, RIGHT_ARROW, -1, RIGHT_ARROW })
				);
			}
		}

		int buttonHeight = 38;
		tabButtons = new Button[NUM_TAB_BUTTONS];
		for (int i = 0; i < tabButtons.length; i++) {
			buttons[PER_PAGE + i] = tabButtons[i] = Button.createTabButton(
					i,
					infoPanel.x,
					infoPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
					infoPanel.width,
					buttonHeight,
					NUM_TAB_BUTTONS,
					Button.getBasicTransitions(i, 1, tabButtons.length, PER_PAGE, new int[] { LEFT_ARROW, RETURN, RIGHT_ARROW, RETURN })
			);
		}

		buttons[LEFT_ARROW] = leftButton = new Button(140, 418, 35, 20, ButtonHoverAction.BOX, new int[] { RIGHT_ARROW, NUM_COLS *(NUM_ROWS - 1) + NUM_COLS /2 - 1, -1, 0 });
		buttons[RIGHT_ARROW] = rightButton = new Button(255, 418, 35, 20, ButtonHoverAction.BOX, new int[] { RETURN, NUM_COLS *(NUM_ROWS - 1) + NUM_COLS /2, LEFT_ARROW, 0 });

		buttons[RETURN] = returnButton = new Button(410, 522, 350, 38, ButtonHoverAction.BOX, new int[] { 0, PER_PAGE, RIGHT_ARROW, PER_PAGE });

		selected = PokemonInfo.getPokemonInfo(1);
	}

	@Override
	public void update(int dt) {
		selectedButton = Button.update(buttons, selectedButton);

		for (int i = 0; i < NUM_ROWS; i++) {
			for (int j = 0; j < NUM_COLS; j++) {
				if (pokemonButtons[i][j].checkConsumePress()) {
					selected = PokemonInfo.getPokemonInfo(getIndex(j, i) + 1);
				}
			}
		}

		for (int i = 0; i < tabButtons.length; i++) {
			if (tabButtons[i].checkConsumePress()) {
				selectedTab = i;
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
			Game.instance().popView();
		}
		
		if (InputControl.instance().consumeIfDown(ControlKey.ESC)) {
			Game.instance().popView();
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

		for (int i = 0; i < NUM_ROWS; i++) {
			for (int j = 0; j < NUM_COLS; j++) {
				int number = getIndex(i, j) + 1;
				if (number > PokemonInfo.NUM_POKEMON) {
					continue;
				}

				PokemonInfo p = PokemonInfo.getPokemonInfo(number);
				Button pokemonButton = pokemonButtons[j][i];

				if (pokedex.isNotSeen(p.namesies())) {
					pokemonButton.label(g, 20, new Color(0, 0, 0, 64), String.format("%03d", number));
				}
				else {
					if (p == selected) {
						pokemonButton.blackOutline(g);
					}

					pokemonButton.imageLabel(g, partyTiles.getTile(p.getTinyImageName()));

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
		TextUtils.drawCenteredWidthString(g, (pageNum + 1) + "/" + NUM_PAGES, pokedexPanel.centerX(), 433);

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

		for (int i = 0; i < tabButtons.length; i++) {
			List<Direction> toOutline = new ArrayList<>();
			toOutline.add(Direction.RIGHT);

			if (i == 0) {
				toOutline.add(Direction.LEFT);
			}

			if (i != selectedTab) {
				toOutline.add(Direction.UP);
			}

			Button tab = tabButtons[i];
			DrawUtils.blackOutline(g, tab.x, tab.y, tab.width, tab.height, toOutline.toArray(new Direction[0]));
			tab.label(g, 12, TabInfo.values()[i].label);
		}

		// Image
		imagePanel.drawBackground(g);
		if (notSeen) {
			imagePanel.label(g, 80, "?");
		}
		else {
			BufferedImage pkmImg = pokedexTiles.getTile(selected.getBaseImageName());
			pkmImg.setRGB(0, 0, 0);

			imagePanel.imageLabel(g, pkmImg);
		}

		g.setColor(Color.BLACK);
		int spacing = 15;

		// Name
		FontMetrics.setFont(g, 20);
		int leftX = imagePanel.rightX() + spacing;
		int textY = imagePanel.y + FontMetrics.getTextHeight(g) + spacing;
		g.drawString(notSeen ? "?????" : selected.getName(), leftX, textY);

		// Number
		int numberSpacing = 10;
		FontMetrics.setFont(g, 18);
		TextUtils.drawRightAlignedString(
				g,
				"#" + String.format("%03d", selected.getNumber()),
				infoPanel.rightX() - numberSpacing,
				infoPanel.y + FontMetrics.getTextHeight(g) + numberSpacing
		);

		if (!notSeen) {
			// Type tiles
			ImageUtils.drawTypeTiles(g, type, infoPanel.rightX() - spacing, textY);

			textY += FontMetrics.getDistanceBetweenRows(g);

			// Classification
			FontMetrics.setFont(g, 16);
			g.drawString(
					(!caught ? "???" : selected.getClassification()) + " " + PokeString.POKEMON,
					leftX,
					textY
			);

			textY += FontMetrics.getDistanceBetweenRows(g);
			g.drawString("Height: " + (!caught ? "???'??\"" : selected.getHeightString()), leftX, textY);

			textY += FontMetrics.getDistanceBetweenRows(g);
			g.drawString("Weight: " + (!caught ? "???.?" : selected.getWeight()) + " lbs", leftX, textY);

			if (caught) {
				textY = imagePanel.bottomY() + FontMetrics.getTextHeight(g) + spacing;
				infoPanel.drawMessage(g, selected.getFlavorText(), textY);

				leftX = imagePanel.x;
				textY = basicInfoPanel.bottomY() + FontMetrics.getTextHeight(g) + spacing;
				g.drawString("Abilities: " + selected.getAbilitiesString(), leftX, textY);

				textY += FontMetrics.getTextHeight(g) + spacing;
				g.drawString("Gender Ratio: " + Gender.getGenderString(selected), leftX, textY);

				textY += FontMetrics.getTextHeight(g) + spacing;
				g.drawString("Capture Rate: " + selected.getCatchRate(), leftX, textY);

				textY += FontMetrics.getTextHeight(g) + spacing;
				g.drawString("Base EXP Yield: " + selected.getBaseEXP(), leftX, textY);

				if (selected.canBreed()) {
					textY += FontMetrics.getTextHeight(g) + spacing;
					g.drawString("Egg Steps: " + selected.getEggSteps(), leftX, textY);
				}
			}


//			int locationX = locationPanel.x + locationPanel.getTextSpace(g);
//			g.drawString("Locations:", locationX, locationPanel.y + FontMetrics.getDistanceBetweenRows(g));
//			List<String> locations = pokedex.getLocations(selected.namesies());
//			for (int i = 0; i < locations.size(); i++) {
//				g.drawString(locations.get(i), locationX, locationPanel.y + (i + 2)*FontMetrics.getDistanceBetweenRows(g));
//			}
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
		return PER_PAGE*pageNum + j* NUM_COLS + i;
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
