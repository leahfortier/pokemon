package gui.view;

import battle.attack.Attack;
import battle.attack.Move;
import draw.Alignment;
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
import main.Global;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.Ability;
import trainer.Player;
import trainer.Trainer;
import type.Type;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

class PartyView extends View {
	private static final int NUM_BOTTOM_BUTTONS = 3;
	private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + Move.MAX_MOVES + NUM_BOTTOM_BUTTONS;
	private static final int MOVES = Trainer.MAX_POKEMON;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int SWITCH = NUM_BUTTONS - 2;
	private static final int NICKNAME = NUM_BUTTONS - 3;

	private final DrawPanel pokemonPanel;
	private final DrawPanel imagePanel;
	private final DrawPanel basicInformationPanel;
	private final DrawPanel abilityPanel;
	private final DrawPanel statsPanel;
	private final DrawPanel movesPanel;
	private final DrawPanel nicknamePanel;

	private final DrawPanel hpBar;
	private final DrawPanel expBar;

	private final Button[] buttons;
	private final Button[] tabButtons;
	private final Button[] moveButtons;

	private final Button nicknameButton;
	private final Button switchButton;
	private final Button returnButton;
	
	private int selectedTab;
	private int selectedButton;
	private int switchTabIndex;
	private boolean nicknameView;
	
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
				.withBlackOutline();

		imagePanel = new DrawPanel(
				pokemonPanel.x + spacing,
				pokemonPanel.y + spacing,
				104,
				104)
				.withFullTransparency()
				.withBlackOutline();

		basicInformationPanel = new DrawPanel(
				imagePanel.rightX() + spacing,
				imagePanel.y,
				pokemonPanel.width - 3*spacing - imagePanel.width,
				imagePanel.height)
				.withFullTransparency()
				.withBlackOutline();

		int barHeight = 15;
		int expBarWidth = basicInformationPanel.width/3;
		expBar = new DrawPanel(
				basicInformationPanel.rightX() - expBarWidth,
				basicInformationPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
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
				abilityPanel.rightX() + spacing,
				abilityPanel.y,
				halfPanelWidth,
				statsPanel.bottomY() - abilityPanel.y)
				.withFullTransparency()
				.withBlackOutline();

		int buttonWidth = (basicInformationPanel.rightX() - imagePanel.x - (NUM_BOTTOM_BUTTONS - 1)*spacing)/NUM_BOTTOM_BUTTONS;
		nicknameButton = new Button(
				imagePanel.x,
				statsPanel.bottomY() + spacing,
				buttonWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { SWITCH, 0, RETURN, 0 }
		);

		switchButton = new Button(
				nicknameButton.rightX() + spacing,
				nicknameButton.y,
				buttonWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { RETURN, 0, NICKNAME, 0 }
		);

		returnButton = new Button(
				switchButton.rightX() + spacing,
				switchButton.y,
				buttonWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { NICKNAME, MOVES + Move.MAX_MOVES - 1, SWITCH, 0 }
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
					Button.getBasicTransitions(i, 1, Trainer.MAX_POKEMON, 0, new int[] { -1, RETURN, -1, MOVES })
			);
		}

		nicknamePanel = new DrawPanel(
				pokemonPanel.x,
				tabButtons[0].y,
				pokemonPanel.width,
				tabButtons[0].height + pokemonPanel.height)
				.withTransparentBackground()
				.withBorderPercentage(0)
				.withBlackOutline();

		moveButtons = movesPanel.getButtons(10, Move.MAX_MOVES, 1, MOVES, new int[] { -1, 0, -1, RETURN });

		buttons = new Button[NUM_BUTTONS];

		System.arraycopy(tabButtons, 0, buttons, 0, tabButtons.length);
		System.arraycopy(moveButtons, 0, buttons, MOVES, moveButtons.length);

		buttons[NICKNAME] = nicknameButton;
		buttons[SWITCH] = switchButton;
		buttons[RETURN] = returnButton;

		updateActiveButtons();
	}

	@Override
	public void update(int dt) {
		Player player = Game.getPlayer();
		InputControl input = InputControl.instance();

		selectedButton = Button.update(buttons, selectedButton);

		if (nicknameView) {
			if (!input.isCapturingText()) {
				input.startTextCapture();
			}

			if (input.consumeIfDown(ControlKey.ENTER)) {
				String nickname = input.stopAndResetCapturedText();
				player.getTeam().get(selectedTab).setNickname(nickname);

				nicknameView = false;
				updateActiveButtons();
			}
		}
		else {
			for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
				if (tabButtons[i].checkConsumePress()) {
					if (switchTabIndex != -1) {
						player.swapPokemon(i, switchTabIndex);
						selectedTab = i;
						switchTabIndex = -1;
					} else {
						selectedTab = i;
					}

					updateActiveButtons();
				}
			}

			if (nicknameButton.checkConsumePress()) {
				nicknameView = true;
				updateActiveButtons();
			}

			if (returnButton.checkConsumePress()) {
				Game.instance().popView();
			}

			if (switchButton.checkConsumePress()) {
				switchTabIndex = switchTabIndex == -1 ? selectedTab : -1;
				updateActiveButtons();
			}

			if (input.consumeIfDown(ControlKey.ESC)) {
				Game.instance().popView();
			}
		}
	}

	@Override
	public void draw(Graphics g) {
		GameData data = Game.getData();
		Player player = Game.getPlayer();

		// Background
		BasicPanels.drawCanvasPanel(g);
		
		List<ActivePokemon> list = player.getTeam();
		ActivePokemon selectedPkm = list.get(selectedTab);

		TileSet pkmTiles = data.getPokemonTilesSmall();
		BufferedImage pkmImg = pkmTiles.getTile(selectedPkm.getImageName());

		if (nicknameView) {
			nicknamePanel.withBackgroundColors(Type.getColors(selectedPkm), true);
			nicknamePanel.drawBackground(g);

			FontMetrics.setFont(g, 30);

			String nickname = InputControl.instance().getInputCaptureString(ActivePokemon.MAX_NAME_LENGTH);
			ImageUtils.drawCenteredImageLabel(g, pkmImg, nickname, Global.GAME_SIZE.width/2, Global.GAME_SIZE.height/2);
		}
		else {
			// Pokemon info
			drawPokemonInfo(g, pkmImg, selectedPkm);

			// Tabs
			drawTabs(g, data, list);

			// Nickname button
			if (!nicknameButton.isActive()) {
				nicknameButton.greyOut(g);
			}

			nicknameButton.fillTransparent(g);
			nicknameButton.blackOutline(g);
			nicknameButton.label(g, 20, "Nickname!!");

			// Switch Box
			if (!switchButton.isActive()) {
				switchButton.greyOut(g);
			} else if (switchTabIndex != -1) {
				switchButton.greyOut(g, false);
			}

			switchButton.fillTransparent(g);
			switchButton.blackOutline(g);
			switchButton.label(g, 20, "Switch!");

			// Return Box
			returnButton.fillTransparent(g);
			returnButton.blackOutline(g);
			returnButton.label(g, 20, "Return");
		}
		
		for (Button button: buttons) {
			button.draw(g);
		}
	}

	private void drawTabs(Graphics g, GameData data, List<ActivePokemon> list) {
		TileSet partyTiles = data.getPartyTiles();
		FontMetrics.setFont(g, 14);

		for (int i = 0; i < list.size(); i++) {
			ActivePokemon pkm = list.get(i);
			Button tabButton = tabButtons[i];

			// Color tab
			tabButton.fill(g, Type.getColors(pkm)[0]);

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

			BufferedImage pkmImg = partyTiles.getTile(pkm.getTinyImageName());
			ImageUtils.drawCenteredImage(g, pkmImg, 19, 26);

			g.translate(-tabButton.x, -tabButton.y);
		}
	}

	private void drawPokemonInfo(Graphics g, BufferedImage pkmImg, ActivePokemon selectedPkm) {
		// Draw type color polygons
		pokemonPanel.withBackgroundColors(Type.getColors(selectedPkm), true);
		if (!selectedPkm.canFight()) {
			pokemonPanel.greyOut();
		}

		pokemonPanel.drawBackground(g);

		// Draw Pokemon Image
		imagePanel.drawBackground(g);
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
			TextUtils.drawWrappedText(g, selectedPkm.getEggMessage(),
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
			int rightAlignedX = basicInformationPanel.rightX() - inset;

			// Type Tiles
			ImageUtils.drawTypeTiles(g, selectedPkm.getActualType(), rightAlignedX, topLineY);

			// Nature
			g.drawString(selectedPkm.getNature().getName() +" Nature", nameX, secondLineY);

			// Total EXP
			g.drawString("EXP:", levelX, secondLineY);
			TextUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), rightAlignedX, secondLineY);

			// Characteristic
			g.drawString(selectedPkm.getCharacteristic(), nameX, thirdLineY);

			// EXP To Next Level
			g.drawString("To Next Lv:", levelX, thirdLineY);
			TextUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), rightAlignedX, thirdLineY);

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

			List<Move> moves = selectedPkm.getActualMoves();

			// Stats Box or Move description
			if (selectedButton >= MOVES && selectedButton < MOVES + Move.MAX_MOVES) {
				drawMoveDescriptionPanel(g, statsPanel, moves.get(selectedButton - MOVES).getAttack());
			}
			else {
				drawStatBox(g, selectedPkm);
			}

			// Move Box
			movesPanel.drawBackground(g);
			for (int i = 0; i < moves.size(); i++) {
				Move move = moves.get(i);
				Attack attack = move.getAttack();
				Button moveButton = moveButtons[i];

				DrawPanel movePanel = new DrawPanel(moveButton)
						.withTransparentBackground(attack.getActualType().getColor())
						.withTransparentCount(2)
						.withBorderPercentage(20)
						.withBlackOutline();
				movePanel.drawBackground(g);

				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 18);

				int moveInset = movePanel.getBorderSize() + 10;
				TextUtils.drawCenteredHeightString(g, attack.getName(), movePanel.x + moveInset, movePanel.centerY());
				TextUtils.drawCenteredHeightString(g, String.format("PP: %d/%d", move.getPP(), move.getMaxPP()), movePanel.rightX() - moveInset, movePanel.centerY(), Alignment.RIGHT);
			}
		}
	}

	private void drawStatBox(Graphics g, ActivePokemon selectedPkm) {
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
			TextUtils.drawRightAlignedString(g, statString, 285, drawY);
			TextUtils.drawRightAlignedString(g, "" + ivs[i], 327, drawY);
			TextUtils.drawRightAlignedString(g, "" + evs[i], 371, drawY);
		}

		// HP Bar
		hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());
	}

	private void drawMoveDescriptionPanel(Graphics g, DrawPanel moveDetailsPanel, Attack move) {
		moveDetailsPanel
				.withTransparentBackground(move.getActualType().getColor())
				.drawBackground(g);

		FontMetrics.setFont(g, 20);
		int spacing = 15;
		int y = moveDetailsPanel.y + spacing + FontMetrics.getTextHeight(g);
		g.drawString(move.getName(), moveDetailsPanel.x + spacing, y);

		BufferedImage typeImage = move.getActualType().getImage();
		int imageY = y - typeImage.getHeight();
		int imageX = moveDetailsPanel.rightX() - spacing - typeImage.getWidth();
		g.drawImage(typeImage, imageX, imageY, null);

		BufferedImage categoryImage = move.getCategory().getImage();
		imageX -= categoryImage.getWidth() + spacing;
		g.drawImage(categoryImage, imageX, imageY, null);

		y += FontMetrics.getDistanceBetweenRows(g);

		FontMetrics.setFont(g, 18);
		g.drawString("Power: " + move.getPowerString(), moveDetailsPanel.x + spacing, y);
		TextUtils.drawRightAlignedString(g, "Acc: " + move.getAccuracyString(), moveDetailsPanel.rightX() - spacing, y);

		y += FontMetrics.getDistanceBetweenRows(g) + 2;

		FontMetrics.setFont(g, 16);
		TextUtils.drawWrappedText(g,
				move.getDescription(),
				moveDetailsPanel.x + spacing,
				y,
				moveDetailsPanel.width - 2*spacing
		);
	}

	private void updateActiveButtons() {
		if (nicknameView) {
			for (Button button : buttons) {
				button.setActive(false);
			}
		}
		else {
			List<ActivePokemon> team = Game.getPlayer().getTeam();
			for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
				tabButtons[i].setActive(i < team.size());
			}

			ActivePokemon pkm = team.get(selectedTab);
			List<Move> moves = pkm.getActualMoves();
			for (int i = 0; i < Move.MAX_MOVES; i++) {
				moveButtons[i].setActive(!pkm.isEgg() && i < moves.size());
			}

			nicknameButton.setActive(!pkm.isEgg());
			switchButton.setActive(team.size() > 1);
			returnButton.setActive(true);
		}
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
		nicknameView = false;
		updateActiveButtons();
	}
}
