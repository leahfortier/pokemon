package gui.view;

import battle.attack.Move;
import battle.effect.status.StatusCondition;
import gui.Button;
import gui.ButtonHoverAction;
import gui.GameData;
import gui.TileSet;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import item.hold.HoldItem;
import item.use.MoveUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;
import main.Game;
import main.Type;
import map.Direction;
import message.MessageUpdate;
import message.Messages;
import message.Messages.MessageState;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer;
import util.DrawUtils;
import input.InputControl;
import input.ControlKey;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class BagView extends View {
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
	
	private final int[] primaryColorX = { 0, 184, 124, 0 };
	private final int[] primaryColorY = { 0, 0, 61, 61 };
	private final int[] secondaryColorX = { 184, 308, 308, 124 };
	private final int[] secondaryColorY = { 0, 0, 61, 61 };
	
	private int pageNum;
	private int selectedTab;
	private int selectedButton;
	private MessageUpdate message;

	private BagState state;
	private ItemNamesies selectedItem;
	private ActivePokemon selectedPokemon;
	
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

	// TODO: There is a really annoying bug that sometimes happens where two buttons have hoverAction at the same time -- mainly one of the useButtons and generally the first Pokemon in the party, but the useButton is the active one and the party one just looks active and is really confusing
	private enum UseState {
		GIVE("Give", BagView.GIVE, (state, bagView, p) -> {
            Game.getPlayer().getBag().giveItem(p, bagView.selectedItem);
            state.deactivate(bagView);
        }),
		USE("Use", BagView.USE, (state, bagView, p) -> {
			if (bagView.selectedItem.getItem() instanceof MoveUseItem) {
				bagView.selectedPokemon = p;
				bagView.state = BagState.MOVE_SELECT;
			} else {
				Game.getPlayer().getBag().useItem(bagView.selectedItem, p);
				state.deactivate(bagView);
			}
        }),
		// TODO: Change back to discard -- maybe have discard when over an item, and take when over a Pokemon
		TAKE("Take", BagView.TAKE, (state, bagView, p) -> {
            Game.getPlayer().getBag().takeItem(p);
            state.deactivate(bagView);
        });
		
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
			
			if (!Game.getPlayer().getBag().hasItem(bagView.selectedItem)) {
				bagView.changeCategory(bagView.selectedTab);
			}
			
			bagView.updateActiveButtons();
		}

		private interface UseButton {
			void useButton(UseState state, BagView bagView, ActivePokemon p);
		}
	}

	BagView() {
		selectedTab = 0;
		selectedButton = 0;
		selectedItem = ItemNamesies.NO_ITEM;
		
		buttons = new Button[NUM_BUTTONS];
		tabButtons = new Button[CATEGORIES.length];
		for (int i = 0; i < CATEGORIES.length; i++) {
			buttons[i] = tabButtons[i] = new Button(
					42 + 102*i,
					42,
					104,
					52,
					ButtonHoverAction.BOX,
					new int[] {
							Button.basicTransition(i, 1, CATEGORIES.length, Direction.RIGHT),
						 	RETURN, // Up
							Button.basicTransition(i, 1, CATEGORIES.length, Direction.LEFT),
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
					ButtonHoverAction.BOX,
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
					ButtonHoverAction.BOX,
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
						ButtonHoverAction.BOX,
						new int[] {
								j == 0 ? ITEMS + k + 1 : PARTY, // Right
								i == 0 ? USE : ITEMS + k - 2, // Up
								j == 1 ? ITEMS + k - 1 : PARTY, // Left
								i == ITEMS_PER_PAGE/2 - 1 ? (j == 0 ? LEFT_ARROW : RIGHT_ARROW) : ITEMS + k + 2 // Down
						});
			}
		}
		
		buttons[LEFT_ARROW] = new Button(498, 451, 35, 20, ButtonHoverAction.BOX, new int[] { RIGHT_ARROW, ITEMS_PER_PAGE - 2, PARTY, RETURN });
		buttons[RIGHT_ARROW] = new Button(613, 451, 35, 20, ButtonHoverAction.BOX, new int[] { PARTY, ITEMS_PER_PAGE - 1, LEFT_ARROW, RETURN });
		
		buttons[GIVE] = new Button(410, 193, 110, 38, ButtonHoverAction.BOX, new int[] { USE, selectedTab, PARTY, ITEMS });
		buttons[USE] = new Button(518, 193, 110, 38, ButtonHoverAction.BOX, new int[] { TAKE, selectedTab, GIVE, ITEMS });
		buttons[TAKE] = new Button(628, 193, 110, 38, ButtonHoverAction.BOX, new int[] { PARTY, selectedTab, USE, ITEMS + 1 });
		
		buttons[RETURN] = new Button(410, 500, 328, 38, ButtonHoverAction.BOX, new int[] { PARTY, LEFT_ARROW, PARTY, selectedTab });
		
		movedToFront();
	}

	@Override
	public void update(int dt) {
		CharacterData player = Game.getPlayer();
		InputControl input = InputControl.instance();

		if (message != null) {
			if (input.consumeIfMouseDown()) {
				message = null;
			}

			if (input.consumeIfDown(ControlKey.SPACE)) {
				message = null;
			}

			// TODO: There was a return here before, make sure it's okay to remove it
		}

		if (message == null) {
            if (Messages.hasMessages()) {
                message = Messages.getNextMessage();
            }
        }

		selectedButton = Button.update(buttons, selectedButton);

		for (int i = 0; i < CATEGORIES.length; i++) {
			if (tabButtons[i].checkConsumePress()) {
				changeCategory(i);
			}
		}

		for (int i = 0; i < Move.MAX_MOVES; ++i) {
			if (moveButtons[i].checkConsumePress()) {
				Move m = selectedPokemon.getActualMoves().get(i);
				player.getBag().useMoveItem(selectedItem, selectedPokemon, m);
				updateActiveButtons();
			}
		}
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			if (partyButtons[i].checkConsumePress()) {
				for (UseState useState : UseState.values()) {
					if (useState.clicked) {
						useState.useButton.useButton(useState, this, player.getTeam().get(i));
                        updateActiveButtons();
					}
				}
			}
		}

		// TODO: Maybe there should be a method that returns the iterator set to the appropriate page
		Set<ItemNamesies> list = player.getBag().getCategory(CATEGORIES[selectedTab]);
		Iterator<ItemNamesies> iter = list.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}

		// TODO: Why does this loop need to be like this?
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				ItemNamesies item = iter.next();
				if (itemButtons[k].checkConsumePress()) {
					selectedItem = item;
					updateActiveButtons();
				}
			}
		}
		
		// Check the use buttons
		for (UseState useState : UseState.values()) {
			if (buttons[useState.buttonIndex].checkConsumePress()) {
				if (!useState.clicked) {
					state = BagState.POKEMON_SELECT;
				}
				else {
					state = BagState.ITEM_SELECT;
				}
				
				useState.clicked = !useState.clicked;
				
				for (UseState otherState : UseState.values()) {
					if (useState == otherState) {
						continue;
					}

					otherState.clicked = false;
				}
				
				if (useState == UseState.USE && selectedItem.getItem() instanceof TrainerUseItem) {
					player.getBag().useItem(selectedItem, player);
				}

				updateActiveButtons();
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
			returnToMap();
		}

		if (input.consumeIfDown(ControlKey.ESC)) {
			returnToMap();
		}

		updateActiveButtons();
	}

	private void returnToMap() {
        Messages.clearMessages(MessageState.BAGGIN_IT_UP);
        Messages.setMessageState(MessageState.MAPPITY_MAP);
        Game.setViewMode(ViewMode.MAP_VIEW);
    }

    @Override
	public void draw(Graphics g) {
		GameData data = Game.getData();
		CharacterData player = Game.getPlayer();

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

		for (UseState useState : UseState.values()) {

			// Grey out selected buttons
			if (useState.clicked) {
				buttons[useState.buttonIndex].greyOut(g, false);
			}

			// Grey out inactive buttons
			if (!buttons[useState.buttonIndex].isActive()) {
				buttons[useState.buttonIndex].greyOut(g, true);
			}
		}

		// Draw Use State buttons
		g.drawImage(tiles.getTile(0x28), 410, 193, null);
		g.setColor(Color.BLACK);
		FontMetrics.setFont(g, 20);
		for (UseState useState : UseState.values()) {
			DrawUtils.drawCenteredString(g, useState.displayName, buttons[useState.buttonIndex]);
		}

		// Item Display
		if (selectedItem != ItemNamesies.NO_ITEM) {
			Item selectedItemValue = selectedItem.getItem();

			// Draw item image
			BufferedImage img = itemTiles.getTile(selectedItemValue.getImageIndex());
			DrawUtils.drawCenteredImage(g, img, 430, 132);
			
			g.setColor(Color.BLACK);
			FontMetrics.setFont(g, 20);
			g.drawString(selectedItem.getName(), 448, 138);
			
			if (selectedItemValue.hasQuantity()) {
				DrawUtils.drawRightAlignedString(g, "x" + bag.getQuantity(selectedItem), 726, 138);
			}
			
			FontMetrics.setFont(g, 14);
			DrawUtils.drawWrappedText(g, selectedItemValue.getDescription(), 418, 156, 726 - buttons[GIVE].x);
		}
		
		FontMetrics.setFont(g, 12);
		g.setColor(Color.BLACK);
		
		// Draw each items in category
		Set<ItemNamesies> list = bag.getCategory(CATEGORIES[selectedTab]);
		Iterator<ItemNamesies> iter = list.iterator();
		
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}
		
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				g.translate(itemButtons[k].x, itemButtons[k].y);
				ItemNamesies item = iter.next();
				Item itemValue = item.getItem();
				
				g.drawImage(tiles.getTile(0x26), 0, 0, null);
				DrawUtils.drawCenteredImage(g, itemTiles.getTile(itemValue.getImageIndex()), 14, 14);
				
				g.drawString(item.getName(), 29, 18);
				
				if (itemValue.hasQuantity()) {
					DrawUtils.drawRightAlignedString(g, "x" + bag.getQuantity(item), 142, 18);
				}
				
				g.translate(-itemButtons[k].x, -itemButtons[k].y);
			}
		}
		
		// Draw page numbers
		FontMetrics.setFont(g, 16);
		DrawUtils.drawCenteredWidthString(g, (pageNum + 1) + "/" + totalPages(list.size()), 573, 466);
		
		// Left and Right arrows
		View.drawArrows(g, buttons[LEFT_ARROW], buttons[RIGHT_ARROW]);
		
		// Draw moves
		if (state == BagState.MOVE_SELECT) {
			List<Move> moveList = selectedPokemon.getActualMoves();
			
			for (int i = 0; i < moveList.size(); i++) {
				g.translate(moveButtons[i].x, moveButtons[i].y);
				
				Move m = moveList.get(i);

				moveButtons[i].fillTranslated(g, m.getAttack().getActualType().getColor());
				
				g.drawImage(tiles.getTile(0x25), 0, 0, null);
				
				g.drawImage(battleTiles.getTile(m.getAttack().getActualType().getImageIndex()), 254, 14, null);
				g.drawImage(battleTiles.getTile(m.getAttack().getCategory().getImageNumber()), 254, 33, null);
				
				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 14);
				g.drawString("PP: " + m.getPP() + "/" + m.getMaxPP(), 166, moveButtons[i].height/2 + 5); // TODO: Center the height properly

				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 20);
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
				g.fillPolygon(primaryColorX, primaryColorY, 4);
				g.setColor(typeColors[1]);
				g.fillPolygon(secondaryColorX, secondaryColorY, 4);
				
				g.drawImage(tiles.getTile(0x25), 0, 0, null);
				
				BufferedImage img = partyTiles.getTile(p.getTinyImageIndex());
				DrawUtils.drawCenteredImage(g, img, 30, 30); // TODO: This looks slightly off
				
				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 14);
				
				if (p.isEgg()) {
					g.drawString(p.getActualName(), 50, 22);	
				}
				else {
					// Name, Gender, and Level
					g.drawString(p.getActualName() + " " + p.getGender().getCharacter(), 50, 22);
					g.drawString("Lv" + p.getLevel(), 153, 22);
					
					// Status condition
					DrawUtils.drawRightAlignedString(g, p.getStatus().getType().getName(), 293, 22);
					
					// Draw HP Box
					g.fillRect(50, 26, 244, 11);
					g.setColor(Color.WHITE);
					g.fillRect(52, 28, 240, 7);
					g.setColor(p.getHPColor());
					g.fillRect(52, 28, (int)(p.getHPRatio()*240), 7);
					
					g.setColor(Color.BLACK);
					FontMetrics.setFont(g, 12);
					
					g.drawString(p.getActualHeldItem().getName(), 50, 47);
					DrawUtils.drawRightAlignedString(g, p.getHP() + "/" + p.getMaxHP(), 293, 47);
					
					if (p.hasStatus(StatusCondition.FAINTED)) {
						// TODO: Look if this color appears in multiple place and see if it should be a constant
						partyButtons[i].fillTranslated(g, new Color(0, 0, 0, 128));
					}	
				}
				
				g.translate(-partyButtons[i].x, -partyButtons[i].y);
			}
		}
		
		g.setColor(Color.BLACK);
		FontMetrics.setFont(g, 20);
		
		g.drawImage(tiles.getTile(0x27), 410, 500, null);
		DrawUtils.drawCenteredWidthString(g, "Return", 573, 525);
		
		for (int i = 0; i < CATEGORIES.length; i++) {
			g.translate(tabButtons[i].x, tabButtons[i].y);
			
			FontMetrics.setFont(g, 14);
			tabButtons[i].fillTranslated(g, CATEGORIES[i].getColor());
			
			if (selectedTab == i) {
				g.drawImage(tiles.getTile(0x23), 0, 0, null);
			}
			else {
				g.drawImage(tiles.getTile(0x24), 0, 0, null);
			}
			
			g.setColor(Color.BLACK);
			
			BufferedImage img = bagTiles.getTile(CATEGORIES[i].getImageNumber());
			DrawUtils.drawCenteredImage(g, img, 16, 26);
			g.drawString(CATEGORIES[i].getName(), 30, 30);
			
			g.translate(-tabButtons[i].x, -tabButtons[i].y);
		}
		
		if (message != null) {
			g.drawImage(battleTiles.getTile(0x3), 0, 440, null);
			g.setColor(Color.BLACK);
			
			FontMetrics.setFont(g, 30);
			DrawUtils.drawWrappedText(g, message.getMessage(), 30, 490, 750);
		}
		else {
			for (Button b : buttons) {
				b.draw(g);
			}
		}
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.BAG_VIEW;
	}

	private void changeCategory(int index) {
		if (selectedTab != index) {
			pageNum = 0;
		}
		
		selectedTab = index;
		state = BagState.ITEM_SELECT;

		Set<ItemNamesies> list = Game.getPlayer().getBag().getCategory(CATEGORIES[selectedTab]);
		selectedItem = list.size() > 0 ? list.iterator().next() : ItemNamesies.NO_ITEM;
		
		updateActiveButtons();
	}

	@Override
	public void movedToFront() {
		changeCategory(0);
	}
	
	private int totalPages(int size) {
		return size/ITEMS_PER_PAGE + (size == 0 || size%ITEMS_PER_PAGE != 0 ? 1 : 0);
	}
	
	private void updateActiveButtons() {
		CharacterData player = Game.getPlayer();

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

		if (selectedItem == ItemNamesies.NO_ITEM || !player.getBag().hasItem(selectedItem)) {
			selectedItem = ItemNamesies.NO_ITEM;
			buttons[GIVE].setActive(false);
			buttons[USE].setActive(false);
		} else {
			Item selectedItemValue = selectedItem.getItem();
			buttons[GIVE].setActive(selectedItemValue instanceof HoldItem);
			buttons[USE].setActive(selectedItemValue instanceof UseItem);
		}
	}
}
