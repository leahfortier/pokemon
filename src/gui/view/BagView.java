package gui.view;

import gui.Button;
import gui.GameData;
import gui.TileSet;
import item.Bag;
import item.BagCategory;
import item.Item;
import item.hold.HoldItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import main.Game;
import main.Game.ViewMode;
import main.Type;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer;
import util.DrawMetrics;
import util.InputControl;
import util.InputControl.Control;
import battle.Move;
import battle.effect.generic.Status.StatusCondition;

public class BagView extends View {
	private static final BagCategory[] CATEGORIES = BagCategory.values();
	private static final int ITEMS_PER_PAGE = 10;
	
	private static final int NUM_BUTTONS = CATEGORIES.length + Trainer.MAX_POKEMON + ITEMS_PER_PAGE + Move.MAX_MOVES + 6 /* Misc Buttons */;
	private static final int PARTY = CATEGORIES.length;
	private static final int ITEMS = PARTY + Trainer.MAX_POKEMON;
	private static final int MOVES = ITEMS + ITEMS_PER_PAGE;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int TAKE = NUM_BUTTONS - 2;
	private static final int USE = NUM_BUTTONS - 3;
	private static final int GIVE = NUM_BUTTONS - 4;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int LEFT_ARROW = NUM_BUTTONS - 6;
	
	private final int[] primaryColorx = { 0, 184, 124, 0 };
	private final int[] primaryColory = { 0, 0, 61, 61 };
	private final int[] secondaryColorx = { 184, 308, 308, 124 };
	private final int[] secondaryColory = { 0, 0, 61, 61 };
	
	private int pageNum;
	private int selectedTab;
	private int selectedButton;
	private String message;

	private BagState state;
	private Item selectedItem;
	private ActivePokemon selectedPokemon;
	private CharacterData player;
	
	private Button[] buttons;
	
	private Button[] tabButtons;
	private Button[] partyButtons;
	private Button[] moveButtons;
	private Button[] itemButtons;
		
	private enum BagState {
		ITEM_SELECT,
		POKEMON_SELECT,
		MOVE_SELECT,
	}

	// TODO: Should this be in its own file?
	// TODO: There is a really annoying bug that sometimes happens where two buttons have hoverAction at the same time -- mainly one of the useButtons and generally the first Pokemon in the party, but the useButton is the active one and the party one just looks active and is really confusing
	private enum UseState {
		GIVE("Give", BagView.GIVE, (state, bagView, p) -> {
            bagView.addMessage(bagView.player.getBag().giveItem(bagView.player, p, bagView.selectedItem));
            state.deactivate(bagView);
        }),
		USE("Use", BagView.USE, (state, bagView, p) -> {
            if (p.isEgg()) {
                UseState.addUseMessages(bagView, false, p);
            }
            else if (bagView.selectedItem instanceof PokemonUseItem) {
                Bag bag = bagView.player.getBag();
                UseState.addUseMessages(bagView, bag.useItem(bagView.player, bagView.selectedItem, p), p);
            }
            else if (bagView.selectedItem instanceof MoveUseItem) {
                bagView.selectedPokemon = p;
                bagView.state = BagState.MOVE_SELECT;

                bagView.updateActiveButtons();
            }
        }),
		// TODO: Change back to discard -- maybe have discard when over an item, and take when over a Pokemon
		TAKE("Take", BagView.TAKE, (state, bagView, p) -> {
            bagView.addMessage(bagView.player.getBag().takeItem(bagView.player, p));
            state.deactivate(bagView);
        });
		
		private static final UseState[] USE_STATE_VALUES = UseState.values();
		
		private final String displayName;
		private final int buttonIndex;
		private final UseButton useButton;
		
		private boolean clicked;
		
		UseState(String displayName, int buttonIndex, UseButton useButton) {
			this.displayName = displayName;
			this.buttonIndex = buttonIndex;
			this.useButton = useButton;
		}
		
		private void deactivate(BagView bagView) {
			this.clicked = false;
			bagView.selectedButton = this.buttonIndex;
			bagView.state = BagState.ITEM_SELECT;
			
			if (bagView.player.getBag().getQuantity(bagView.selectedItem) == 0) {
				bagView.changeCategory(bagView.selectedTab);
			}
			
			bagView.updateActiveButtons();
		}
		
		private static void addUseMessages(BagView bagView, boolean success, ActivePokemon p) {
			Item selected = bagView.selectedItem;
			
			if (success) {
				bagView.addMessage(bagView.player.getName() + " used the " + selected.getName() + "! " + ((UseItem)selected).getSuccessMessage(p));
			}
			else {
				bagView.addMessage("It won't have any effect.");	
			}
			
			UseState.USE.deactivate(bagView);
		}
		
		private interface UseButton {
			void useButton(UseState state, BagView bagView, ActivePokemon p);
		}
	}

	public BagView(CharacterData data) {
		player = data;
		selectedTab = 0;
		selectedButton = 0;
		
		buttons = new Button[NUM_BUTTONS];
		tabButtons = new Button[CATEGORIES.length];
		for (int i = 0; i < CATEGORIES.length; i++) {
			buttons[i] = tabButtons[i] = new Button(
					42 + 102*i,
					42,
					104,
					52,
					Button.HoverAction.BOX,
					new int[] {
							i == CATEGORIES.length - 1 ? 0 : i + 1, // Right
						 	RETURN, // Up
						 	i == 0 ? CATEGORIES.length - 1 : i - 1, // Left
						 	USE // Down
					});
		}
		
		partyButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			buttons[PARTY + i] = partyButtons[i] = new Button(
					72,
					122 + 69*i,
					308,
					61,
					Button.HoverAction.BOX,
					new int[] {
							i < Trainer.MAX_POKEMON/3 ? GIVE : (i < 2*Trainer.MAX_POKEMON/3 ? ITEMS : RETURN), // Right
							i == 0 ? 0 : PARTY + i - 1, // Up
							i < Move.MAX_MOVES ? MOVES + i : (i < Trainer.MAX_POKEMON/3 ? TAKE : (i < 2*Trainer.MAX_POKEMON/3 ? ITEMS : RETURN)), // Left
							i == Trainer.MAX_POKEMON -1 ? selectedTab : PARTY + i + 1 // Down
					});
		}
		
		moveButtons = new Button[Move.MAX_MOVES];
		for (int i = 0; i < Move.MAX_MOVES; i++) {
			buttons[MOVES + i] = moveButtons[i] = new Button(
					72,
					122 + 69*i,
					308,
					61,
					Button.HoverAction.BOX,
					new int[] {
							PARTY + i, // Right
							i == 0 ? 0 : MOVES + i - 1, // Up
							i < Trainer.MAX_POKEMON/3 ? TAKE : (i < 2*Trainer.MAX_POKEMON/3 ? ITEMS : RETURN), // Left, needs to behave like party buttons
							i == 3 ? selectedTab : MOVES + i + 1 // Down
					});
		}
		
		itemButtons = new Button[ITEMS_PER_PAGE];
		for (int i = 0, k = 0; i < ITEMS_PER_PAGE/2; i++) {
			for (int j = 0; j < 2; j++, k++) {
				// TODO: uggy assignments
				buttons[ITEMS + k] = itemButtons[k] = new Button(
						421 + 160*j,
						261 + 38*i,
						148,
						28,
						Button.HoverAction.BOX,
						new int[] {
								j == 0 ? ITEMS + k + 1 : PARTY, // Right
								i == 0 ? USE : ITEMS + k - 2, // Up
								j == 1 ? ITEMS + k - 1 : PARTY, // Left
								i == ITEMS_PER_PAGE/2 - 1 ? (j == 0 ? LEFT_ARROW : RIGHT_ARROW) : ITEMS + k + 2 // Down
						});
			}
		}
		
		buttons[LEFT_ARROW] = new Button(498, 451, 35, 20, Button.HoverAction.BOX, new int[] { RIGHT_ARROW, ITEMS_PER_PAGE - 2, PARTY, RETURN});
		buttons[RIGHT_ARROW] = new Button(613, 451, 35, 20, Button.HoverAction.BOX, new int[] { PARTY, ITEMS_PER_PAGE - 1, LEFT_ARROW, RETURN});
		
		buttons[GIVE] = new Button(410, 193, 110, 38, Button.HoverAction.BOX, new int[] { USE, selectedTab, PARTY, ITEMS });
		buttons[USE] = new Button(518, 193, 110, 38, Button.HoverAction.BOX, new int[] { TAKE, selectedTab, GIVE, ITEMS });
		buttons[TAKE] = new Button(628, 193, 110, 38, Button.HoverAction.BOX, new int[] { PARTY, selectedTab, USE, ITEMS + 1 });
		
		buttons[RETURN] = new Button(410, 500, 328, 38, Button.HoverAction.BOX, new int[] { PARTY, LEFT_ARROW, PARTY, selectedTab });
		
		movedToFront(null);
	}
	
	

	public void update(int dt, InputControl input, Game game) {
		if (message != null) {
			if (input.mouseDown) {
				input.consumeMousePress();
				message = null;
			}
			
			if (input.isDown(Control.SPACE)) {
				input.consumeKey(Control.SPACE);
				message = null;
			}
			
			return;
		}
		
		selectedButton = Button.update(buttons, selectedButton, input);
		
		for (int i = 0; i < CATEGORIES.length; i++) {
			if (tabButtons[i].checkConsumePress()) {
				changeCategory(i);
			}
		}
		
		for (int i = 0; i < Move.MAX_MOVES; ++i) {
			if (moveButtons[i].checkConsumePress()) {
				Move m = selectedPokemon.getActualMoves().get(i);
				UseState.addUseMessages(this, player.getBag().useMoveItem(selectedItem, selectedPokemon, m), selectedPokemon);
			}
		}
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			if (partyButtons[i].checkConsumePress()) {
				for (UseState useState : UseState.USE_STATE_VALUES) {
					if (useState.clicked) {
						useState.useButton.useButton(useState, this, player.getTeam().get(i));
					}
				}
			}
		}

		// TODO: Maybe there should be a method that returns the iterator set to the appropriate page
		Set<Item> list = player.getBag().getCategory(CATEGORIES[selectedTab]);
		Iterator<Item> iter = list.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}

		// TODO: Why does this loop need to be like this?
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				Item item = iter.next();
				if (itemButtons[k].checkConsumePress()) {
					selectedItem = item;
					updateActiveButtons();
				}
			}
		}
		
		// Check the use buttons
		for (UseState useState : UseState.USE_STATE_VALUES) {
			if (buttons[useState.buttonIndex].checkConsumePress()) {
				if (!useState.clicked) {
					state = BagState.POKEMON_SELECT;
				}
				else {
					state = BagState.ITEM_SELECT;
				}
				
				useState.clicked = !useState.clicked;
				
				for (UseState otherState : UseState.USE_STATE_VALUES) {
					if (useState == otherState) {
						continue;
					}

					otherState.clicked = false;
				}
				
				updateActiveButtons();
				
				if (useState == UseState.USE && selectedItem instanceof TrainerUseItem) {
					UseState.addUseMessages(this, player.getBag().useItem(selectedItem, player), null);
				}
			}
		}

		// TODO: These should be methods
		if (buttons[LEFT_ARROW].checkConsumePress()) {
			if (pageNum == 0) {
				pageNum = totalPages(list.size()) - 1;
			}
			else {
				pageNum--;
			}

			updateActiveButtons();
		}
		
		if (buttons[RIGHT_ARROW].checkConsumePress()) {
			if (pageNum == totalPages(list.size()) - 1) {
				pageNum = 0;
			}
			else {
				pageNum++;
			}

			updateActiveButtons();
		}
		
		if (buttons[RETURN].checkConsumePress()) {
			game.setViewMode(ViewMode.MAP_VIEW);
		}

		if (input.isDown(Control.ESC)) {
			input.consumeKey(Control.ESC);
			game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g, GameData data) {
		TileSet tiles = data.getMenuTiles();
		TileSet itemTiles = data.getItemTiles();
		TileSet partyTiles = data.getPartyTiles();
		TileSet bagTiles = data.getBagTiles();
		TileSet battleTiles = data.getBattleTiles();
		
		Bag bag = player.getBag();
		List<ActivePokemon> team = player.getTeam();
		
		// Background
		g.drawImage(tiles.getTile(0x2), 0,0, null);
		
		// Info Boxes
		g.setColor(CATEGORIES[selectedTab].getColor());
		g.fillRect(42, 92, 716, 466);
		
		g.drawImage(tiles.getTile(0x21), 42, 92, null);
		g.drawImage(tiles.getTile(0x22), 62, 112, null);
		
		// Item Display
		if (selectedItem != null) {
			for (UseState useState : UseState.USE_STATE_VALUES) {

				// Grey out selected buttons
				if (useState.clicked) {
					buttons[useState.buttonIndex].greyOut(g, false);
				}
				
				// Grey out inactive buttons
				if (!buttons[useState.buttonIndex].isActive()) {
					buttons[useState.buttonIndex].greyOut(g, true);
				}
			}

			// Draw item image
			BufferedImage img = itemTiles.getTile(selectedItem.getImageIndex());
			DrawMetrics.drawCenteredImage(g, img, 430, 132);
			
			g.setColor(Color.BLACK);
			DrawMetrics.setFont(g, 20);
			g.drawString(selectedItem.getName(), 448, 138);
			
			if (selectedItem.hasQuantity()) {
				DrawMetrics.drawRightAlignedString(g, "x" + bag.getQuantity(selectedItem), 726, 138);
			}
			
			DrawMetrics.setFont(g, 14);
			DrawMetrics.drawWrappedText(g, selectedItem.getDescription(), 418, 156, 726 - buttons[GIVE].x);
			
			g.drawImage(tiles.getTile(0x28), 410, 193, null);
			DrawMetrics.setFont(g, 20);
			for (UseState useState : UseState.USE_STATE_VALUES) {
				DrawMetrics.drawCenteredString(g, useState.displayName, buttons[useState.buttonIndex]);
			}
		}
		
		DrawMetrics.setFont(g, 12);
		g.setColor(Color.BLACK);
		
		// Draw each items in category
		Set<Item> list = bag.getCategory(CATEGORIES[selectedTab]);
		Iterator<Item> iter = list.iterator();
		
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}
		
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				g.translate(itemButtons[k].x, itemButtons[k].y);
				Item item = iter.next();
				
				g.drawImage(tiles.getTile(0x26), 0, 0, null);
				DrawMetrics.drawCenteredImage(g, itemTiles.getTile(item.getImageIndex()), 14, 14);
				
				g.drawString(item.getName(), 29, 18);
				
				if (item.hasQuantity()) {
					DrawMetrics.drawRightAlignedString(g, "x" + bag.getQuantity(item), 142, 18);
				}
				
				g.translate(-itemButtons[k].x, -itemButtons[k].y);
			}
		}
		
		// Draw page numbers
		DrawMetrics.setFont(g, 16);
		DrawMetrics.drawCenteredWidthString(g, (pageNum + 1) + "/" + totalPages(list.size()), 573, 466);
		
		// Left and Right arrows
		View.drawArrows(g, buttons[LEFT_ARROW], buttons[RIGHT_ARROW]);
		
		// Draw moves
		if (state == BagState.MOVE_SELECT) {
			List<Move> moveList = selectedPokemon.getActualMoves();
			
			for (int i = 0; i < moveList.size(); i++) {
				g.translate(moveButtons[i].x, moveButtons[i].y);
				
				Move m = moveList.get(i);
				
				g.setColor(m.getAttack().getActualType().getColor());
				g.fillRect(0, 0, moveButtons[i].width, moveButtons[i].height);
				
				g.drawImage(tiles.getTile(0x25), 0, 0, null);
				
				g.drawImage(battleTiles.getTile(m.getAttack().getActualType().getImageIndex()), 254, 14, null);
				g.drawImage(battleTiles.getTile(m.getAttack().getCategory().getImageNumber()), 254, 33, null);
				
				g.setColor(Color.BLACK);
				DrawMetrics.setFont(g, 14);
				g.drawString("PP: " + m.getPP() + "/" + m.getMaxPP(), 166, moveButtons[i].height/2 + 5); // TODO: Center the height properly

				g.setColor(Color.BLACK);
				DrawMetrics.setFont(g, 20);
				g.drawString(m.getAttack().getName(), 20, 38);
				
				g.translate(-moveButtons[i].x, -moveButtons[i].y);
			}
		}
		// Draw Pokemon Info
		else {
			for (int i = 0; i < team.size(); i++) {
				g.translate(partyButtons[i].x, partyButtons[i].y);
				
				ActivePokemon p = team.get(i);
				Color[] typeColors = Type.getColors(p);
				
				g.setColor(typeColors[0]);
				g.fillPolygon(primaryColorx, primaryColory, 4);
				g.setColor(typeColors[1]);
				g.fillPolygon(secondaryColorx, secondaryColory, 4);
				
				g.drawImage(tiles.getTile(0x25), 0, 0, null);
				
				BufferedImage img = partyTiles.getTile(p.getTinyImageIndex());
				DrawMetrics.drawCenteredImage(g, img, 30, 30); // TODO: This looks slightly off
				
				g.setColor(Color.BLACK);
				DrawMetrics.setFont(g, 14);
				
				if (p.isEgg()) {
					g.drawString(p.getActualName(), 50, 22);	
				}
				else {
					// Name, Gender, and Level
					g.drawString(p.getActualName() + " " + p.getGender().getCharacter(), 50, 22);
					g.drawString("Lv" + p.getLevel(), 153, 22);
					
					// Status condition
					DrawMetrics.drawRightAlignedString(g, p.getStatus().getType().getName(), 293, 22);
					
					// Draw HP Box
					g.fillRect(50, 26, 244, 11);
					g.setColor(Color.WHITE);
					g.fillRect(52, 28, 240, 7);
					g.setColor(p.getHPColor());
					g.fillRect(52, 28, (int)(p.getHPRatio()*240), 7);
					
					g.setColor(Color.BLACK);
					DrawMetrics.setFont(g, 12);
					
					g.drawString(p.getActualHeldItem().getName(), 50, 47);
					DrawMetrics.drawRightAlignedString(g, p.getHP() + "/" + p.getMaxHP(), 293, 47);
					
					if (p.hasStatus(StatusCondition.FAINTED)) {
						g.setColor(new Color(0, 0, 0, 128)); // TODO: Look if this color appears in multiple place and see if it should be a constant
						g.fillRect(0, 0, partyButtons[i].width, partyButtons[i].height);
					}	
				}
				
				g.translate(-partyButtons[i].x, -partyButtons[i].y);
			}
		}
		
		g.setColor(Color.BLACK);
		DrawMetrics.setFont(g, 20);
		
		g.drawImage(tiles.getTile(0x27), 410, 500, null);
		DrawMetrics.drawCenteredWidthString(g, "Return", 573, 525);
		
		for (int i = 0; i < CATEGORIES.length; i++) {
			g.translate(tabButtons[i].x, tabButtons[i].y);
			
			DrawMetrics.setFont(g, 14);
			g.setColor(CATEGORIES[i].getColor());
			g.fillRect(0, 0, tabButtons[i].width, tabButtons[i].height);
			
			if (selectedTab == i) g.drawImage(tiles.getTile(0x23), 0, 0, null);
			else g.drawImage(tiles.getTile(0x24), 0, 0, null);
			
			g.setColor(Color.BLACK);
			
			BufferedImage img = bagTiles.getTile(CATEGORIES[i].getImageNumber());
			DrawMetrics.drawCenteredImage(g, img, 16, 26);
			g.drawString(CATEGORIES[i].getName(), 30, 30);
			
			g.translate(-tabButtons[i].x, -tabButtons[i].y);
		}
		
		if (message != null) {
			g.drawImage(battleTiles.getTile(0x3), 0, 440, null);
			g.setColor(Color.WHITE);
			
			DrawMetrics.setFont(g, 30);
			DrawMetrics.drawWrappedText(g, message, 30, 490, 750);
		}
		else {
			for (Button b : buttons) {
				b.draw(g);
			}
		}
	}

	public ViewMode getViewModel() {
		return ViewMode.BAG_VIEW;
	}

	private void changeCategory(int index) {
		if (selectedTab != index) 
			pageNum = 0;
		
		selectedTab = index;
		state = BagState.ITEM_SELECT;

		Set<Item> list = player.getBag().getCategory(CATEGORIES[selectedTab]);
		selectedItem = list.size() > 0 ? list.iterator().next() : null;
		
		updateActiveButtons();
	}

	public void movedToFront(Game game) {
		changeCategory(0);
	}
	
	private int totalPages(int size) {
		// TODO: isn't 0%n always 0?
		return size/ITEMS_PER_PAGE + (size == 0 || size%ITEMS_PER_PAGE != 0 ? 1 : 0);
	}
	
	private void addMessage(String message) {
		this.message = message;
	}
	
	private void updateActiveButtons() {
		List<ActivePokemon> team = player.getTeam();
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			partyButtons[i].setActive(state == BagState.POKEMON_SELECT && i < team.size());
		}
		
		int displayed = player.getBag().getCategory(CATEGORIES[selectedTab]).size();
		for (int i = 0; i < ITEMS_PER_PAGE; i++) {
			itemButtons[i].setActive(state == BagState.ITEM_SELECT && i < displayed - pageNum*ITEMS_PER_PAGE);
		}
		
		for (int i = 0; i < Move.MAX_MOVES; i++) {
			moveButtons[i].setActive(state == BagState.MOVE_SELECT && i < selectedPokemon.getActualMoves().size());
		}
		
		buttons[GIVE].setActive(selectedItem instanceof HoldItem);
		buttons[USE].setActive(selectedItem instanceof UseItem);
	}
}
