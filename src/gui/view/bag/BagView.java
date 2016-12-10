package gui.view.bag;

import battle.attack.Attack;
import battle.attack.Move;
import battle.effect.status.StatusCondition;
import gui.button.Button;
import gui.button.ButtonHoverAction;
import gui.GameData;
import gui.TileSet;
import gui.panel.BasicPanels;
import gui.panel.DrawPanel;
import gui.view.View;
import gui.view.ViewMode;
import input.ControlKey;
import input.InputControl;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import item.hold.HoldItem;
import item.use.UseItem;
import main.Game;
import main.Global;
import main.Type;
import map.Direction;
import message.MessageUpdate;
import message.Messages;
import message.Messages.MessageState;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer;
import util.DrawUtils;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BagView extends View {
	private static final BagCategory[] CATEGORIES = BagCategory.values();
	private static final int ITEMS_PER_PAGE = 10;
	
	private static final int NUM_BUTTONS = CATEGORIES.length + Trainer.MAX_POKEMON + ITEMS_PER_PAGE + Move.MAX_MOVES + 6 /* Misc Buttons */;
	private static final int PARTY = CATEGORIES.length;
	private static final int ITEMS = PARTY + Trainer.MAX_POKEMON;
	private static final int MOVES = ITEMS + ITEMS_PER_PAGE;
	private static final int RETURN = NUM_BUTTONS - 1;
	static final int TAKE = NUM_BUTTONS - 2;
	static final int USE = NUM_BUTTONS - 3;
	static final int GIVE = NUM_BUTTONS - 4;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int LEFT_ARROW = NUM_BUTTONS - 6;

	private final DrawPanel bagPanel;
	private final DrawPanel pokemonPanel;
	private final DrawPanel itemsPanel;
	private final DrawPanel selectedPanel;

	private int pageNum;
	private int selectedTab;
	private int selectedButton;
	private MessageUpdate message;

	BagState state;
	ItemNamesies selectedItem;
	ActivePokemon selectedPokemon;
	
	private Button[] buttons;
	
	private Button[] tabButtons;
	private Button[] partyButtons;
	private Button[] moveButtons;
	private Button[] itemButtons;

	public BagView() {
		int tabHeight = 55;
		int spacing = 28;

		bagPanel = new DrawPanel(
				spacing,
				spacing + tabHeight,
				Point.subtract(Global.GAME_SIZE,
						2*spacing,
						2*spacing + tabHeight))
				.withTransparentBackground()
				.withBorderPercentage(0)
				.withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP)));

		int buttonHeight = 38;
		int selectedHeight = 82;
		int halfPanelWidth = (bagPanel.width - 3*spacing)/2;

		pokemonPanel = new DrawPanel(
				bagPanel.x + spacing,
				bagPanel.y + spacing,
				halfPanelWidth,
				bagPanel.height - 2*spacing)
				.withFullTransparency()
				.withBlackOutline();

		selectedPanel = new DrawPanel(
				pokemonPanel.x + pokemonPanel.width + spacing,
				bagPanel.y + spacing,
				halfPanelWidth,
				selectedHeight)
				.withFullTransparency()
				.withBlackOutline();

		Button returnButton = new Button(
				selectedPanel.x,
				bagPanel.y + bagPanel.height - spacing - buttonHeight,
				halfPanelWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { PARTY, RIGHT_ARROW, PARTY, 0 });

		itemsPanel = new DrawPanel(
				selectedPanel.x,
				selectedPanel.y + selectedPanel.height + buttonHeight + spacing,
				halfPanelWidth,
				pokemonPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing)
				.withFullTransparency()
				.withBlackOutline();

		selectedTab = 0;
		selectedButton = 0;
		selectedItem = ItemNamesies.NO_ITEM;
		
		tabButtons = new Button[CATEGORIES.length];
		for (int i = 0; i < CATEGORIES.length; i++) {
			tabButtons[i] = Button.createTabButton(
					i,
					bagPanel.x,
					bagPanel.y,
					bagPanel.width,
					tabHeight,
					tabButtons.length,
					new int[] {
							Button.basicTransition(i, 1, CATEGORIES.length, Direction.RIGHT),
						 	RETURN, // Up
							Button.basicTransition(i, 1, CATEGORIES.length, Direction.LEFT),
						 	USE // Down
					});
		}

		partyButtons = pokemonPanel.getButtons(10, Trainer.MAX_POKEMON, 1, PARTY, new int[] { GIVE, 0, MOVES, 0 });
		moveButtons = pokemonPanel.getButtons(10, Trainer.MAX_POKEMON, 1, MOVES, new int[] { PARTY, 0, GIVE, 0 });
		itemButtons = itemsPanel.getButtons(5, ITEMS_PER_PAGE/2 + 1, 2, ITEMS_PER_PAGE/2, 2, ITEMS, new int[] { -1, USE, -1, RIGHT_ARROW });

		buttons = new Button[NUM_BUTTONS];
		System.arraycopy(tabButtons, 0, buttons, 0, CATEGORIES.length);
		System.arraycopy(partyButtons, 0, buttons, PARTY, Trainer.MAX_POKEMON);
		System.arraycopy(moveButtons, 0, buttons, MOVES, Move.MAX_MOVES);
		System.arraycopy(itemButtons, 0, buttons, ITEMS, ITEMS_PER_PAGE);

		UseState[] useStates = UseState.values();
		int lastIndex = useStates.length - 1;
		for (UseState useState : useStates) {
			int tabIndex = useState.ordinal();
			buttons[useState.buttonIndex] = Button.createTabButton(
					tabIndex,
					selectedPanel.x,
					selectedPanel.y + selectedPanel.height + buttonHeight - 2*DrawUtils.OUTLINE_SIZE,
					selectedPanel.width,
					buttonHeight,
					useStates.length,
					new int[] {
							tabIndex == lastIndex ? PARTY : useStates[tabIndex + 1].buttonIndex, // Right
							selectedTab, // Up
							tabIndex == 0 ? PARTY : useStates[tabIndex - 1].buttonIndex, // Left
							tabIndex <= useStates.length/2 ? ITEMS : ITEMS + 1 // Down
					}
			);
		}

		int arrowHeight = 20;
		Button leftArrow  = new Button(
				itemsPanel.x + itemsPanel.width/4,
				itemButtons[itemButtons.length - 1].centerY() + (itemButtons[2].y - itemButtons[0].y) - arrowHeight/2,
				35,
				arrowHeight,
				ButtonHoverAction.BOX,
				new int[] { RIGHT_ARROW, ITEMS + ITEMS_PER_PAGE - 2, RIGHT_ARROW, RETURN }
		);

		Button rightArrow = new Button(
				itemsPanel.x + itemsPanel.width - (leftArrow.x - itemsPanel.x) - leftArrow.width,
				leftArrow.y,
				leftArrow.width,
				leftArrow.height,
				ButtonHoverAction.BOX,
				new int[] { LEFT_ARROW, ITEMS + ITEMS_PER_PAGE - 1, LEFT_ARROW, RETURN }
		);

		buttons[LEFT_ARROW] = leftArrow;
		buttons[RIGHT_ARROW] = rightArrow;

		buttons[RETURN] = returnButton;
		
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
		}

		while ((message == null || StringUtils.isNullOrWhiteSpace(message.getMessage()))
				&& Messages.hasMessages()) {
			message = Messages.getNextMessage();

			if (message.isViewChange()) {
				Game.instance().setViewMode(message.getViewMode());
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
					useState.use(this, player.getTeam().get(i));
				}
			}
		}

		// TODO: Maybe there should be a method that returns the iterator set to the appropriate page
		Set<ItemNamesies> list = player.getBag().getCategory(CATEGORIES[selectedTab]);
		Iterator<ItemNamesies> iter = list.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}

		for (int i = 0; i < ITEMS_PER_PAGE && iter.hasNext(); i++) {
			ItemNamesies item = iter.next();
			if (itemButtons[i].checkConsumePress()) {
				selectedItem = item;
				updateActiveButtons();
			}
		}
		
		// Check the use buttons
		for (UseState useState : UseState.values()) {
			if (buttons[useState.buttonIndex].checkConsumePress()) {
				useState.update(this);
			}
		}

		if (buttons[LEFT_ARROW].checkConsumePress()) {
			pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages(list.size()));
			updateActiveButtons();
		}
		
		if (buttons[RIGHT_ARROW].checkConsumePress()) {
			pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages(list.size()));
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
        Game.instance().setViewMode(ViewMode.MAP_VIEW);
    }

    @Override
	public void draw(Graphics g) {
		GameData data = Game.getData();
		CharacterData player = Game.getPlayer();

		TileSet itemTiles = data.getItemTiles();
		TileSet partyTiles = data.getPartyTiles();
		
		Bag bag = player.getBag();
		List<ActivePokemon> team = player.getTeam();
		
		// Background
		BasicPanels.drawCanvasPanel(g);
		
		// Info Boxes
		bagPanel.withBackgroundColor(CATEGORIES[selectedTab].getColor())
				.drawBackground(g);

		// Draw Use State buttons
		for (UseState useState : UseState.values()) {
			useState.draw(g, buttons[useState.buttonIndex]);
		}

		// Selected item Display
		selectedPanel.drawBackground(g);
		if (selectedItem != ItemNamesies.NO_ITEM) {
			int spacing = 8;

			Item selectedItemValue = selectedItem.getItem();

			// Draw item image
			BufferedImage img = itemTiles.getTile(selectedItemValue.getImageIndex());
			g.drawImage(img, selectedPanel.x + 5, selectedPanel.y, null);
			
			g.setColor(Color.BLACK);
			FontMetrics.setFont(g, 20);

			int startY = selectedPanel.y + FontMetrics.getDistanceBetweenRows(g);

			g.drawString(selectedItem.getName(), selectedPanel.x + 2*spacing + Global.TILE_SIZE, startY);
			
			if (selectedItemValue.hasQuantity()) {
				String quantityString = "x" + bag.getQuantity(selectedItem);
				DrawUtils.drawRightAlignedString(g, quantityString, selectedPanel.x + selectedPanel.width - 2*spacing, startY);
			}
			
			FontMetrics.setFont(g, 14);
			DrawUtils.drawWrappedText(
					g,
					selectedItemValue.getDescription(),
					selectedPanel.x + spacing,
					startY + FontMetrics.getDistanceBetweenRows(g),
					selectedPanel.width - 2*spacing
			);
		}
		
		FontMetrics.setFont(g, 12);
		g.setColor(Color.BLACK);
		
		// Draw each items in category
		itemsPanel.drawBackground(g);
		Set<ItemNamesies> list = bag.getCategory(CATEGORIES[selectedTab]);
		Iterator<ItemNamesies> iter = list.iterator();
		
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}
		
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				ItemNamesies item = iter.next();
				Item itemValue = item.getItem();
				Button itemButton = itemButtons[k];

				itemButton.fill(g, Color.WHITE);
				itemButton.blackOutline(g);

				g.translate(itemButton.x, itemButton.y);

				DrawUtils.drawCenteredImage(g, itemTiles.getTile(itemValue.getImageIndex()), 14, 14);
				
				g.drawString(item.getName(), 29, 18);
				
				if (itemValue.hasQuantity()) {
					DrawUtils.drawRightAlignedString(g, "x" + bag.getQuantity(item), 142, 18);
				}
				
				g.translate(-itemButton.x, -itemButton.y);
			}
		}
		
		// Draw page numbers
		FontMetrics.setFont(g, 16);
		DrawUtils.drawCenteredString(g, (pageNum + 1) + "/" + totalPages(list.size()), itemsPanel.centerX(), buttons[RIGHT_ARROW].centerY());
		
		// Left and Right arrows
		buttons[LEFT_ARROW].drawArrow(g, Direction.LEFT);
		buttons[RIGHT_ARROW].drawArrow(g, Direction.RIGHT);
		
		// Draw moves
		pokemonPanel.drawBackground(g);
		if (state == BagState.MOVE_SELECT) {
			List<Move> moveList = selectedPokemon.getActualMoves();
			
			for (int i = 0; i < moveList.size(); i++) {
				Move move = moveList.get(i);
				Attack attack = move.getAttack();
				Button moveButton = moveButtons[i];

				g.translate(moveButton.x, moveButton.y);

				DrawPanel movePanel = new DrawPanel(0, 0, moveButton.width, moveButton.height)
						.withTransparentBackground(attack.getActualType().getColor())
						.withTransparentCount(2)
						.withBorderPercentage(15)
						.withBlackOutline();
				movePanel.drawBackground(g);


				g.drawImage(attack.getActualType().getImage(), 254, 14, null);
				g.drawImage(attack.getCategory().getImage(), 254, 33, null);
				
				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 14);
				DrawUtils.drawCenteredHeightString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 166, movePanel.centerY());

				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 20);
				g.drawString(attack.getName(), 20, 38);
				
				g.translate(-moveButton.x, -moveButton.y);
			}
		}
		// Draw Pokemon Info
		else {
			for (int i = 0; i < team.size(); i++) {
				ActivePokemon p = team.get(i);
				Button pokemonButton = partyButtons[i];

				g.translate(pokemonButton.x, pokemonButton.y);

				DrawPanel pokemonPanel = new DrawPanel(0, 0, pokemonButton.width, pokemonButton.height)
						.withBackgroundColors(Type.getColors(p))
						.withTransparentCount(2)
						.withBorderPercentage(15)
						.withBlackOutline();
				pokemonPanel.drawBackground(g);


				BufferedImage img = partyTiles.getTile(p.getTinyImageIndex());
				DrawUtils.drawCenteredImage(g, img, 30, 30); // TODO: This looks slightly off
				
				g.setColor(Color.BLACK);
				FontMetrics.setFont(g, 14);

				// Name and Gender
				g.drawString(p.getActualName() + " " + p.getGenderString(), 50, 22);

				if (!p.isEgg()) {
					// Level
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
						pokemonButton.fillTranslated(g, new Color(0, 0, 0, 128));
					}	
				}
				
				g.translate(-pokemonButton.x, -pokemonButton.y);
			}
		}
		
		Button returnButton = buttons[RETURN];
		returnButton.fillTransparent(g);
		returnButton.blackOutline(g);
		returnButton.label(g, 20, "Return");
		
		for (int i = 0; i < CATEGORIES.length; i++) {
			Button tabButton = tabButtons[i];
			tabButton.fillTransparent(g, CATEGORIES[i].getColor());
			tabButton.outlineTab(g, i, selectedTab);

			g.translate(tabButton.x, tabButton.y);

			g.setColor(Color.BLACK);
			FontMetrics.setFont(g, 14);

			DrawUtils.drawCenteredImage(g, CATEGORIES[i].getIcon(), 16, 26);
			g.drawString(CATEGORIES[i].getDisplayName(), 30, 30);
			
			g.translate(-tabButton.x, -tabButton.y);
		}
		
		if (message != null && !StringUtils.isNullOrWhiteSpace(message.getMessage())) {
			BasicPanels.drawFullMessagePanel(g, message.getMessage());
		}
		else {
			for (Button button : buttons) {
				button.draw(g);
			}
		}
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.BAG_VIEW;
	}

	void setSelectedButton(UseState useState) {
		selectedButton = useState.buttonIndex;
	}

	void updateCategory() {
		changeCategory(this.selectedTab);
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
	
	void updateActiveButtons() {
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

		buttons[LEFT_ARROW].setActive(state == BagState.ITEM_SELECT);
		buttons[RIGHT_ARROW].setActive(state == BagState.ITEM_SELECT);

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
