package gui.view;

import gui.Button;
import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import main.Game;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.PC;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Trainer;
import util.DrawUtils;
import util.InputControl;
import util.InputControl.Control;
import battle.Move;

public class PCView extends View {
	private static final int NUM_BUTTONS = PC.BOX_HEIGHT*PC.BOX_WIDTH + Trainer.MAX_POKEMON + 6;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int RELEASE = NUM_BUTTONS - 2;
	private static final int DEPOSIT_WITHDRAW = NUM_BUTTONS - 3;
	private static final int SWITCH = NUM_BUTTONS - 4;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int LEFT_ARROW = NUM_BUTTONS - 6;
	private static final int PARTY = PC.BOX_HEIGHT*PC.BOX_WIDTH;

	private PC pc;
	
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
	
	public PCView() {
		pc = Game.getPlayer().getPC();
		
		selectedButton = PARTY;
		
		buttons = new Button[NUM_BUTTONS];
		boxButtons = new Button[PC.BOX_HEIGHT][PC.BOX_WIDTH];
		for (int i = 0, k = 0; i < PC.BOX_HEIGHT; i++) {
			for (int j = 0; j < PC.BOX_WIDTH; j++, k++) {
				buttons[k] = boxButtons[i][j] = new Button(60 + 54*j, 96 + 54*i, 40, 40, Button.HoverAction.BOX, 
						new int[] {j == PC.BOX_WIDTH - 1 ? SWITCH : k + 1, // Right 
								i == 0 ? PARTY + j : k - PC.BOX_WIDTH, // Up
								j == 0 ? RELEASE : k - 1, // Left
								i == PC.BOX_HEIGHT - 1 ? (j < PC.BOX_WIDTH/2 ? LEFT_ARROW : RIGHT_ARROW) : k + PC.BOX_WIDTH}); // Down
			}
		}
		
		partyButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
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
		selected = Game.getPlayer().front();
	}
	
	public void update(int dt, InputControl input) {
		selectedButton = Button.update(buttons, selectedButton, input);

		for (int i = 0; i < PC.BOX_HEIGHT; i++) {
			for (int j = 0; j < PC.BOX_WIDTH; j++) {
				if (boxButtons[i][j].checkConsumePress()) {
					if (party && depositClicked) {
						pc.depositPokemonFromPlayer(selected, i, j);
						depositClicked = false;
					}
					else if (switchClicked) {
						pc.switchPokemon(selected, i, j);
						switchClicked = false;
					}
					else {
						selected = pc.getBoxPokemon()[i][j];
						party = false;
					}
					updateActiveButtons();
				}
			}
		}
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			if (partyButtons[i].checkConsumePress()) {
				if (party && depositClicked) {
					depositClicked = false;
				}
				else if (switchClicked) {
					pc.switchPokemon(selected, i);
					switchClicked = false;
				}
				else {
					selected = Game.getPlayer().getTeam().get(i);
					party = true;					
				}
				updateActiveButtons();
			}
		}
		
		if (leftButton.checkConsumePress())
		{
			pc.prevBox();
			movedToFront();
		}
		
		if (rightButton.checkConsumePress())
		{
			pc.nextBox();
			movedToFront();
		}
		
		if (switchButton.checkConsumePress())
		{
			switchClicked = !switchClicked;
			updateActiveButtons();
		}
		
		if (depositWithdrawButton.checkConsumePress()) {
			if (party) { // Deposit
				if (depositClicked) {
					pc.depositPokemon(selected);
				}

				depositClicked = !depositClicked;
			}
			else { // Withdraw
				pc.withdrawPokemon(selected);
			}

			updateActiveButtons();
		}
		
		if (releaseButton.checkConsumePress())
		{
			pc.releasePokemon(selected);
			movedToFront();
		}
		
		if (returnButton.checkConsumePress())
		{
			Game.setViewMode(ViewMode.MAP_VIEW);
		}
		
		if (input.isDown(Control.ESC))
		{
			input.consumeKey(Control.ESC);
			Game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g) {
		GameData data = Game.getData();

		TileSet tiles = data.getMenuTiles();
		TileSet typeTiles = data.getBattleTiles();
		TileSet partyTiles = data.getPartyTiles();
		TileSet pokemonTiles = data.getPokemonTilesSmall();

		ActivePokemon[][] box = pc.getBoxPokemon();
		
		// Box
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		g.setColor(pc.getBoxColor());
		g.fillRect(40, 40, 350, 418);
		g.drawImage(tiles.getTile(0x31), 40, 40, null);
		
		// Draw Box number
		g.setColor(Color.BLACK);
		DrawUtils.setFont(g, 20);
		DrawUtils.drawCenteredWidthString(g, "Box " + (pc.getBoxNum() + 1), 214, 65);
		
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
				
				BufferedImage pkmImg = partyTiles.getTile(p.getTinyImageIndex());
				DrawUtils.drawCenteredImage(g, pkmImg, 20, 20);
				
				g.translate(-boxButtons[j][i].x, -boxButtons[j][i].y);
			}
		}
		
		DrawUtils.setFont(g, 16);
		DrawUtils.drawCenteredWidthString(g, (pc.getBoxNum() + 1) + "/" + pc.getNumBoxes(), 215, 433);
		
		View.drawArrows(g, leftButton, rightButton);
		
		// Party
		g.setColor(Color.RED);
		g.fillRect(40, 478, 350, 82);
		g.drawImage(tiles.getTile(0x33), 40, 478, null);

		List<ActivePokemon> team = Game.getPlayer().getTeam();
		for (int i = 0; i < team.size(); i++)
		{
			g.translate(partyButtons[i].x, partyButtons[i].y);
			
			ActivePokemon p = team.get(i);
			if (p == selected)
			{
				g.drawImage(tiles.getTile(0x32), 0, 0, null);
			}
			
			BufferedImage pkmImg = partyTiles.getTile(p.getTinyImageIndex());
			DrawUtils.drawCenteredImage(g, pkmImg, 20, 20);
			
			g.translate(-partyButtons[i].x, -partyButtons[i].y);
		}
		
		// Description
		Type[] type = selected.getActualType();
		Color[] typeColors = Type.getColors(selected);
		
		g.setColor(typeColors[0]);
		g.fillPolygon(new int[] {410, 759, 759, 410}, new int[] {40, 40, 96, 445}, 4);
		g.setColor(typeColors[1]);
		g.fillPolygon(new int[] {410, 759, 759, 410}, new int[] {445, 96, 501, 501}, 4);
		
		if (switchClicked) {
			switchButton.greyOut(g, false);
		}

		if (!releaseButton.isActive()) {
			releaseButton.greyOut(g, true);
		}

		if (!depositWithdrawButton.isActive()) {
			depositWithdrawButton.greyOut(g, true);
		}
		else if (party && depositClicked) {
			depositWithdrawButton.greyOut(g, false);
		}
		
		g.drawImage(tiles.getTile(0x34), 410, 40, null);
		BufferedImage pkmImg = pokemonTiles.getTile(selected.getImageIndex());
		DrawUtils.drawCenteredImage(g, pkmImg, 479, 109);
		
		if (selected.isEgg()) {
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 20);
			
			g.drawString(selected.getActualName(), 541, 82);
			
			DrawUtils.setFont(g, 16);
			DrawUtils.drawWrappedText(g, selected.getEggMessage(), 427, 179, 740 - 427);
		}
		else {
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 20);
			
			g.drawString(selected.getActualName() + " " + selected.getGender().getCharacter(), 541, 82);
			DrawUtils.drawRightAlignedString(g, "Lv" + selected.getLevel(), 740, 82);
			g.drawString("#" + String.format("%03d", selected.getPokemonInfo().getNumber()), 541, 110);
			
			int index = 0;
			if (type[1] != Type.NO_TYPE) {
				g.drawImage(typeTiles.getTile(type[0].getImageIndex()), 669, 97, null);
				index = 1;
			}
			
			g.drawImage(typeTiles.getTile(type[index].getImageIndex()), 707, 97, null);
			
			DrawUtils.setFont(g, 16);
			
			// Total EXP
			g.drawString("EXP:", 540, 135);
			DrawUtils.drawRightAlignedString(g, selected.getTotalEXP() + "", 740, 135);
			
			// EXP to the next level
			g.drawString("To Next Lv:", 540, 156);
			DrawUtils.drawRightAlignedString(g, selected.expToNextLevel() + "", 740, 156);
			
			// Ability
			g.drawString(selected.getAbility().getName(), 427, 179);
			
			// Held Item
			DrawUtils.drawRightAlignedString(g, selected.getActualHeldItem().getName(), 740, 179);
			
			// Nature
			g.drawString(selected.getNature().getName() + " Nature", 427, 198);
			
			// Characteristic
			g.drawString(selected.getCharacteristic(), 427, 217);
			
			List<Move> moves = selected.getActualMoves();
			for (int i = 0; i < moves.size(); i++) {
				int x = i%2 == 0 ? 421 : 590;
				int y = i/2 == 0 ? 238 : 277;
				
				g.setColor(moves.get(i).getAttack().getActualType().getColor());
				g.fillRect(x, y, 159, 31);
				g.drawImage(tiles.getTile(0x35), x, y, null);
				
				g.setColor(Color.BLACK);
				DrawUtils.drawCenteredWidthString(g, moves.get(i).getAttack().getName(), x + 159/2, y + 20);
			}
			
			DrawUtils.drawRightAlignedString(g, "Stat", 635, 340);
			DrawUtils.drawRightAlignedString(g, "IV", 681, 340);
			DrawUtils.drawRightAlignedString(g, "EV", 735, 340);
			
			int[] stats = selected.getStats();
			int[] ivs = selected.getIVs();
			int[] evs = selected.getEVs();
			
			for (int i = 0; i < Stat.NUM_STATS; i++) {
				DrawUtils.setFont(g, 16);
				g.setColor(selected.getNature().getColor(i));
				g.drawString(Stat.getStat(i, false).getName(), 427, 360 + i*18 + i/2); // TODO: srsly what's with the i/2
				
				g.setColor(Color.BLACK);
				DrawUtils.setFont(g, 14);
				
				// TODO: What's up with the + i/2 in the y????
				DrawUtils.drawRightAlignedString(g, stats[i] + "", 635, 360 + i*18 + i/2);
				DrawUtils.drawRightAlignedString(g, ivs[i] + "", 681, 360 + i*18 + i/2);
				DrawUtils.drawRightAlignedString(g, evs[i] + "", 735, 360 + i*18 + i/2);
			}
		}
		
		// Return button box
		g.drawImage(tiles.getTile(0x36), 410, 522, null);
		
		// Buttons
		DrawUtils.setFont(g, 20);
		DrawUtils.drawCenteredWidthString(g, "Switch", 464, 489);
		DrawUtils.drawCenteredWidthString(g, party ? "Deposit" : "Withdraw", 584, 489);
		DrawUtils.drawCenteredWidthString(g, "Release", 699, 489);
		DrawUtils.drawCenteredWidthString(g, "Return", 584, 546);
		
		for (Button b : buttons) {
			b.draw(g);
		}
	}

	public ViewMode getViewModel() {
		return ViewMode.PC_VIEW;
	}

	public void movedToFront() {
		party = true;
		selected = Game.getPlayer().front();
		updateActiveButtons();
	}

	private void updateActiveButtons() {
		ActivePokemon[][] box = pc.getBoxPokemon();
		for (int i = 0; i < PC.BOX_HEIGHT; i++)
		{
			for (int j = 0; j < PC.BOX_WIDTH; j++)
			{
				boxButtons[i][j].setActive((party && depositClicked) || switchClicked || box[i][j] != null);
			}
		}

		CharacterData player = Game.getPlayer();
		List<ActivePokemon> team = player.getTeam();

		party = false;
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			partyButtons[i].setActive(i < team.size());
			if (i < team.size() && team.get(i) == selected) {
				party = true;
			}
		}
		
		if (party) {
			depositWithdrawButton.setActive(player.canDeposit(selected));
		}
		else {
			depositWithdrawButton.setActive(team.size() < Trainer.MAX_POKEMON);
		}
		
		releaseButton.setActive(!party || team.size() > 1);
	}
}
