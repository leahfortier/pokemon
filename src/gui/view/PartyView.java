package gui.view;

import battle.attack.Attack;
import battle.attack.Move;
import gui.GameData;
import gui.TileSet;
import gui.button.Button;
import gui.button.ButtonHoverAction;
import gui.panel.BasicPanels;
import gui.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.Ability;
import trainer.Trainer;
import type.Type;
import util.DrawUtils;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;

class PartyView extends View {
	private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + Move.MAX_MOVES + 2;
	private static final int MOVES = Trainer.MAX_POKEMON;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int SWITCH = NUM_BUTTONS - 2;

	private final DrawPanel pokemonPanel;
	private final DrawPanel imagePanel;
	private final DrawPanel basicInformationPanel;
	private final DrawPanel abilityPanel;
	private final DrawPanel statsPanel;
	private final DrawPanel movesPanel;

	private final DrawPanel hpBar;
	private final DrawPanel expBar;

	private final Button[] buttons;
	private final Button[] tabButtons;
	private final Button[] moveButtons;

	private final Button switchButton;
	private final Button returnButton;
	
	private int selectedTab;
	private int selectedButton;
	private int switchTabIndex;
	
	PartyView() {
		selectedTab = 0;
		selectedButton = 0;
		switchTabIndex = -1;

		int tabHeight = 55;
		int spacing = 28;

		pokemonPanel = new DrawPanel(
				spacing,
				spacing + tabHeight,
				Point.subtract(Global.GAME_SIZE,
						2*spacing,
						2*spacing + tabHeight)
				)
				.withTransparentBackground()
				.withBorderPercentage(0)
				.withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP))
		);

		imagePanel = new DrawPanel(
				pokemonPanel.x + spacing,
				pokemonPanel.y + spacing,
				104,
				104)
				.withFullTransparency()
				.withBlackOutline();

		basicInformationPanel = new DrawPanel(
				imagePanel.x + imagePanel.width + spacing,
				imagePanel.y,
				pokemonPanel.width - 3*spacing - imagePanel.width,
				imagePanel.height)
				.withFullTransparency()
				.withBlackOutline();

		int barHeight = 15;
		int expBarWidth = basicInformationPanel.width/3;
		expBar = new DrawPanel(
				basicInformationPanel.x + basicInformationPanel.width - expBarWidth,
				basicInformationPanel.y + basicInformationPanel.height - DrawUtils.OUTLINE_SIZE,
				expBarWidth,
				barHeight)
				.withBlackOutline();

		int buttonHeight = 38;
		int halfPanelWidth =(pokemonPanel.width - 3*spacing)/2;
		int statsPanelHeight = 138;

		abilityPanel = new DrawPanel(
				imagePanel.x,
				imagePanel.y + imagePanel.height + spacing,
				halfPanelWidth,
				pokemonPanel.height - 5*spacing - imagePanel.height - buttonHeight - statsPanelHeight)
				.withFullTransparency()
				.withBlackOutline();

		statsPanel = new DrawPanel(
				abilityPanel.x,
				abilityPanel.y + abilityPanel.height + spacing,
				halfPanelWidth,
				statsPanelHeight)
				.withFullTransparency()
				.withBlackOutline();

		hpBar = new DrawPanel(
				statsPanel.x,
				statsPanel.y,
				statsPanel.width/2,
				barHeight)
				.withBlackOutline();

		movesPanel = new DrawPanel(
				abilityPanel.x + abilityPanel.width + spacing,
				abilityPanel.y,
				halfPanelWidth,
				statsPanel.y + statsPanel.height - abilityPanel.y)
				.withFullTransparency()
				.withBlackOutline();

		switchButton = new Button(
				statsPanel.x,
				statsPanel.y + statsPanel.height + spacing,
				halfPanelWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { RETURN, 0, RETURN, 0 }
		);

		returnButton = new Button(
				movesPanel.x,
				switchButton.y,
				halfPanelWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { SWITCH, 0, SWITCH, 0 }
		);

		tabButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			tabButtons[i] = Button.createTabButton(
					i,
					pokemonPanel.x,
					pokemonPanel.y,
					pokemonPanel.width,
					tabHeight,
					tabButtons.length,
					new int[] {
							i == Trainer.MAX_POKEMON - 1 ? 0 : i + 1, // Right
							i < Trainer.MAX_POKEMON ? SWITCH : RETURN, // Up
							i == 0 ? Trainer.MAX_POKEMON - 1 : i - 1, // Left
							MOVES // Down
					});
		}

		moveButtons = movesPanel.getButtons(10, Move.MAX_MOVES, 1);

		buttons = new Button[NUM_BUTTONS];

		System.arraycopy(tabButtons, 0, buttons, 0, tabButtons.length);
		System.arraycopy(moveButtons, 0, buttons, MOVES, moveButtons.length);

		buttons[SWITCH] = switchButton;
		buttons[RETURN] = returnButton;

		updateActiveButtons();
	}

	@Override
	public void update(int dt) {
		selectedButton = Button.update(buttons, selectedButton);
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			if (tabButtons[i].checkConsumePress()) {
				if (switchTabIndex != -1) {
					Game.getPlayer().swapPokemon(i, switchTabIndex);
					selectedTab = i;
					switchTabIndex = -1;
				}
				else {
					selectedTab = i;
				}
				
				updateActiveButtons();
			}
		}
		
		if (returnButton.checkConsumePress()) {
			Game.instance().popView();
		}
		
		if (switchButton.checkConsumePress()) {
			switchTabIndex = switchTabIndex == -1 ? selectedTab : -1;
			updateActiveButtons();
		}
		
		if (InputControl.instance().consumeIfDown(ControlKey.ESC)) {
			Game.instance().popView();
		}
	}

	@Override
	public void draw(Graphics g) {
		GameData data = Game.getData();

		// Background
		BasicPanels.drawCanvasPanel(g);
		
		List<ActivePokemon> list = Game.getPlayer().getTeam();
		ActivePokemon selectedPkm = list.get(selectedTab);
		
		Type[] type = selectedPkm.getActualType();

		// Draw type color polygons
		pokemonPanel.withBackgroundColors(Type.getColors(selectedPkm));
		if (!selectedPkm.canFight()) {
			pokemonPanel.greyOut();
		}

		pokemonPanel.drawBackground(g);
		
		// Draw Pokemon Image
		imagePanel.drawBackground(g);
		TileSet pkmTiles = data.getPokemonTilesSmall();
		BufferedImage pkmImg = pkmTiles.getTile(selectedPkm.getImageIndex());
		imagePanel.imageLabel(g, pkmImg);

		// Draw basic information panel
		basicInformationPanel.drawBackground(g);

		FontMetrics.setFont(g, 20);
		g.setColor(Color.BLACK);

		int inset = FontMetrics.getDistanceBetweenRows(g)/2;
		int nameX = basicInformationPanel.x + inset;
		int topLineY = basicInformationPanel.y + inset + FontMetrics.getTextHeight(g);

		// Name and Gender
		g.drawString(selectedPkm.getActualName() + " " + selectedPkm.getGenderString(), nameX, topLineY);

		if (selectedPkm.isEgg()) {
			FontMetrics.setFont(g, 16);
			
			// Description
			DrawUtils.drawWrappedText(g, selectedPkm.getEggMessage(),
					basicInformationPanel.x + inset,
					topLineY + inset + FontMetrics.getTextHeight(g),
					basicInformationPanel.width - 2*inset);
		}
		else {
			// Number
			g.drawString("#" + String.format("%03d", selectedPkm.getPokemonInfo().getNumber()), 378, topLineY);
			
			// Status Condition
			g.drawString(selectedPkm.getStatus().getType().getName(), 459, topLineY);
			
			// Level
			int levelX = 525;
			g.drawString("Lv" + selectedPkm.getLevel(), levelX, topLineY);
			
			FontMetrics.setFont(g, 16);
			int secondLineY = topLineY + inset + FontMetrics.getTextHeight(g);
			int thirdLineY = secondLineY + inset + FontMetrics.getTextHeight(g);
			int fourthLineY = thirdLineY + inset + FontMetrics.getTextHeight(g);
			int rightAlignedX = basicInformationPanel.x + basicInformationPanel.width - inset;

			// Type Tiles
			DrawUtils.drawTypeTiles(g, type, rightAlignedX, topLineY);
			
			// Nature
			g.drawString(selectedPkm.getNature().getName() +" Nature", nameX, secondLineY);
			
			// Total EXP
			g.drawString("EXP:", levelX, secondLineY);
			DrawUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), rightAlignedX, secondLineY);
			
			// Characteristic
			g.drawString(selectedPkm.getCharacteristic(), nameX, thirdLineY);
			
			// EXP To Next Level
			g.drawString("To Next Lv:", levelX, thirdLineY);
			DrawUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), rightAlignedX, thirdLineY);
			
			// Held Item
			g.drawString(selectedPkm.getActualHeldItem().getName(), nameX, fourthLineY);
			
			// Ability with description
			Ability ability = selectedPkm.getActualAbility();
			abilityPanel.drawBackground(g);
			abilityPanel.drawMessage(g, 16, ability.getName() + " - " + ability.getDescription());
			
			// EXP Bar
			expBar.fillBar(g, DrawUtils.EXP_BAR_COLOR, selectedPkm.expRatio());
			
			FontMetrics.setFont(g, 16);
			g.setColor(Color.BLACK);
			
			// Stats Box
			statsPanel.drawBackground(g);

			int spacing = statsPanel.height/(Stat.NUM_STATS + 1);
			int firstRowY = statsPanel.y + spacing - 2;

			g.drawString("Stat", 250, firstRowY);
			g.drawString("IV", 310, firstRowY);
			g.drawString("EV", 355, firstRowY);
			
			for (int i = 0; i < Stat.NUM_STATS; i++) {
				g.setColor(selectedPkm.getNature().getColor(i));
				g.drawString(Stat.getStat(i, false).getName(), statsPanel.x + 10, firstRowY + (i + 1)*spacing);
			}
			
			int[] stats = selectedPkm.getStats();
			int[] ivs = selectedPkm.getIVs();
			int[] evs = selectedPkm.getEVs();

			FontMetrics.setFont(g, 14);
			g.setColor(Color.BLACK);

			for (int i = 0; i < Stat.NUM_STATS; i++) {
				final String statString;
				if (i == Stat.HP.index()) {
					statString = selectedPkm.getHP() + "/" + stats[i];
				} else {
					statString = "" + stats[i];
				}

				int drawY = firstRowY + (i + 1)*spacing;
				DrawUtils.drawRightAlignedString(g, statString, 285, drawY);
				DrawUtils.drawRightAlignedString(g, "" + ivs[i], 327, drawY);
				DrawUtils.drawRightAlignedString(g, "" + evs[i], 371, drawY);
			}

			// HP Bar
			hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());
			
			// Move Box
			movesPanel.drawBackground(g);
			List<Move> moves = selectedPkm.getActualMoves();
			for (int i = 0; i < moves.size(); i++) {
				Move move = moves.get(i);
				Attack attack = move.getAttack();
				Button moveButton = moveButtons[i];

				g.translate(moveButton.x, moveButton.y);

				DrawPanel movePanel = new DrawPanel(0, 0, moveButton.width, moveButton.height)
						.withTransparentBackground(attack.getActualType().getColor())
						.withTransparentCount(2)
						.withBorderPercentage(15)
						.withBlackOutline();
				movePanel.drawBackground(g);

				g.setColor(Color.BLACK);
				if (selectedButton == MOVES + i) {
					movePanel.drawMessage(g, 10, attack.getDescription());
				}
				else {
					FontMetrics.setFont(g, 14);

					int moveInset = movePanel.getBorderSize() + 4;
					int firstY = moveInset + FontMetrics.getTextHeight(g);
					int secondY = movePanel.height - moveInset;

					int middleX = movePanel.width/2;
					int rightAlignedMiddleX = middleX + 72;

					// Attack name
					g.drawString(attack.getName(), moveInset, firstY);
					
					// PP
					g.drawString("PP:", middleX, firstY);
					DrawUtils.drawRightAlignedString(g, move.getPP() + "/" + move.getMaxPP(), rightAlignedMiddleX, firstY);
					
					// Accuracy
					FontMetrics.setFont(g, 12);
					g.drawString("Accuracy:", moveInset, secondY);
					DrawUtils.drawRightAlignedString(g, attack.getAccuracyString(), moveInset + 93, secondY);
					
					// Power
					g.drawString("Power:", middleX, secondY);
					DrawUtils.drawRightAlignedString(g, attack.getPowerString(), rightAlignedMiddleX, secondY);

					BufferedImage typeImage = move.getAttack().getActualType().getImage();
					int imageX = movePanel.width - moveInset - typeImage.getWidth();
					g.drawImage(typeImage, imageX, firstY - typeImage.getHeight() + 2, null);

					BufferedImage categoryImage = move.getAttack().getCategory().getImage();
					g.drawImage(categoryImage, imageX, secondY - categoryImage.getHeight() + 2, null);
				}

				g.translate(-moveButton.x, -moveButton.y);
			}
		}
		
		// Switch Box
		if (switchTabIndex != -1 || list.size() == 1) {
			switchButton.greyOut(g);
		}

		switchButton.fillTransparent(g);
		switchButton.blackOutline(g);
		switchButton.label(g, 20, "Switch!");
		
		// Return Box
		returnButton.fillTransparent(g);
		returnButton.blackOutline(g);
		returnButton.label(g, 20, "Return");
		
		TileSet partyTiles = data.getPartyTiles();
		FontMetrics.setFont(g, 14);
		
		// Tabs
		for (int i = 0; i < list.size(); i++) {
			ActivePokemon pkm = list.get(i);
			Button tabButton = tabButtons[i];

			// Color tab
			int colorIndex = 0;
			if (i == tabButtons.length - 1 && pkm.isDualTyped()) {
				colorIndex = 1;
			}

			tabButton.fill(g, Type.getColors(pkm)[colorIndex]);

			// Fade out fainted Pokemon
			if (!pkm.canFight()) {
				tabButton.greyOut(g);
			}

			// Transparenty
			tabButton.fillTransparent(g);

			// Outline in black
			tabButton.outlineTab(g, i, selectedTab);

			g.translate(tabButton.x, tabButton.y);
			
			g.setColor(Color.BLACK);
			g.drawString(pkm.getActualName(), 40, 34);
			
			pkmImg = partyTiles.getTile(pkm.getTinyImageIndex());
			DrawUtils.drawCenteredImage(g, pkmImg, 19, 26);
			
			g.translate(-tabButton.x, -tabButton.y);
		}
		
		for (Button button: buttons) {
			button.draw(g);
		}
	}
	
	private void updateActiveButtons() {
		List<ActivePokemon> team = Game.getPlayer().getTeam();
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			tabButtons[i].setActive(i < team.size());
		}
		
		ActivePokemon pkm = team.get(selectedTab);
		List<Move> moves = pkm.getActualMoves();
		for (int i = 0; i < Move.MAX_MOVES; i++) {
			moveButtons[i].setActive(!pkm.isEgg() && i < moves.size());
		}
		
		switchButton.setActive(team.size() > 1);
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.PARTY_VIEW;
	}

	@Override
	public void movedToFront() {
		selectedTab = 0;
		selectedButton = 0;
		switchTabIndex = -1;
		updateActiveButtons();
	}
}
