package gui.view;

import gui.Button;
import gui.GameData;
import gui.TileSet;
import item.Item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.Namesies;
import main.Type;
import trainer.CharacterData;
import util.DrawMetrics;
import util.InputControl;
import util.InputControl.Control;

public class MartView extends View {

	// TODO: Need to eventually make this dynamic
	private static final Namesies[] FOR_SALE_NAMES = new Namesies[] {Namesies.POTION_ITEM, Namesies.POKE_BALL_ITEM, Namesies.ANTIDOTE_ITEM, Namesies.PARALYZE_HEAL_ITEM, Namesies.BURN_HEAL_ITEM};
	private static ArrayList<Item> forSaleItems;
	
	private static final Color BACKGROUND_COLOR = new Color (68, 123, 184);
	
	private static final int ITEMS_PER_PAGE = 10;
	
	private static final int NUM_BUTTONS = ITEMS_PER_PAGE + 6;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int BUY = NUM_BUTTONS - 2;
	private static final int AMOUNT_LEFT_ARROW = NUM_BUTTONS - 3;
	private static final int AMOUNT_RIGHT_ARROW = NUM_BUTTONS - 4;
	private static final int SHOP_RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int SHOP_LEFT_ARROW = NUM_BUTTONS - 6;
	
	private static final Color SIDE_BOX_COLOR = Type.NORMAL.getColor();
	private static final int SIDE_BOX_X = 72;
	private static final int SIDE_BOX_GAP = 69;
	private static final int BOX_TEXT_X = 50;
	private static final int BOX_TEXT_Y = 35;
	
	private static final int MONEY_BOX_Y = 122;
	private static final int IN_BAG_BOX_Y = MONEY_BOX_Y + SIDE_BOX_GAP;
	private static final int TOTAL_BOX_Y = MONEY_BOX_Y + SIDE_BOX_GAP*4;
	
	private static final int[] primaryColorx = { 0, 184, 124, 0 };
	private static final int[] primaryColory = { 0, 0, 61, 61 };
	private static final int[] secondaryColorx = { 184, 308, 308, 124 };
	private static final int[] secondaryColory = { 0, 0, 61, 61 };
		
	private int pageNum;
	private int selectedButton;
	private int itemAmount;
	
	private Item selectedItem;
	private CharacterData player;
	
	private Button[] buttons;
	private Button[] itemButtons;
	private Button amountLeftButton;
	private Button amountRightButton;
	private Button buyButton;
	private Button shopLeftButton;
	private Button shopRightButton;
	private Button returnButton;
	
	public MartView(CharacterData data) {
		player = data;
		selectedButton = 0;
		itemAmount = 1;

		// TODO: uggy
		buttons = new Button[NUM_BUTTONS];
		
		itemButtons = new Button[ITEMS_PER_PAGE];
		for (int i = 0, k = 0; i < ITEMS_PER_PAGE/2; i++) {
			for (int j = 0; j < 2; j++, k++) {
				buttons[k] = itemButtons[k] = new Button(
						421 + 160*j,
						261 + 38*i,
						148,
						28,
						Button.HoverAction.BOX,
						new int[] {
								j == 0 ? k + 1 : k - 1, // Right
								i == 0 ? (j == 0 ? AMOUNT_LEFT_ARROW : AMOUNT_RIGHT_ARROW) : k - 2, // Up
								j == 1 ? k - 1 : k + 1, // Left
								i == ITEMS_PER_PAGE/2 - 1 ? (j == 0 ? SHOP_LEFT_ARROW : SHOP_RIGHT_ARROW) : k + 2 // Down
						});
			}
		}
		
		buttons[SHOP_LEFT_ARROW] = shopLeftButton = new Button(498, 451, 35, 20, Button.HoverAction.BOX, new int[] { SHOP_RIGHT_ARROW, ITEMS_PER_PAGE - 2, SHOP_RIGHT_ARROW, RETURN });
		buttons[SHOP_RIGHT_ARROW] = shopRightButton = new Button(613, 451, 35, 20, Button.HoverAction.BOX, new int[] { SHOP_LEFT_ARROW, ITEMS_PER_PAGE - 1, SHOP_LEFT_ARROW, RETURN });
		
		buttons[AMOUNT_LEFT_ARROW] = amountLeftButton = new Button(410, 193, 110, 38, Button.HoverAction.BOX, new int[] { AMOUNT_RIGHT_ARROW, RETURN, BUY, 0 });
		buttons[AMOUNT_RIGHT_ARROW] = amountRightButton = new Button(628, 193, 110, 38, Button.HoverAction.BOX, new int[] { AMOUNT_LEFT_ARROW, RETURN, AMOUNT_LEFT_ARROW, 1 });

		buttons[BUY] = buyButton = new Button(SIDE_BOX_X, MONEY_BOX_Y + SIDE_BOX_GAP*5, 308, 61, Button.HoverAction.BOX, new int[] { RETURN, BUY, RETURN, BUY });
		
		buttons[RETURN] = returnButton = new Button(410, 500, 328, 38, Button.HoverAction.BOX, new int[] { BUY, SHOP_LEFT_ARROW, BUY, AMOUNT_LEFT_ARROW });
		
		if (forSaleItems == null) {
			forSaleItems = new ArrayList<>();
			for (Namesies itemName : FOR_SALE_NAMES) {
				forSaleItems.add(Item.getItem(itemName));
			}
		}
		
		setSelectedItem(forSaleItems.get(0));
		updateActiveButtons();
	}

	public void update(int dt, InputControl input, Game game) {
		selectedButton = Button.update(buttons, selectedButton, input);
			
		Iterator<Item> iter = forSaleItems.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}

		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				Item item = iter.next();
				if (itemButtons[k].checkConsumePress()) {
					setSelectedItem(item);
					updateActiveButtons();
				}
			}
		}

		// TODO: Need to have a method to get the max purchase amount
		if (amountLeftButton.checkConsumePress()) {
			if (itemAmount == 1) {
				itemAmount = player.getDatCashMoney()/selectedItem.getPrice();
			}
			else {
				itemAmount--;
			}
			
			updateActiveButtons();
		}
		
		if (amountRightButton.checkConsumePress()) {
			if (itemAmount ==  player.getDatCashMoney()/selectedItem.getPrice()) {
				itemAmount = 1;
			}
			else {
				itemAmount++;
			}
			
			updateActiveButtons();
		}		
		
		if (buyButton.checkConsumePress()) {
			player.sucksToSuck(itemAmount*selectedItem.getPrice());
			for (int i = 0; i < itemAmount; i++) {
				player.getBag().addItem(selectedItem);
			}
			
			setSelectedItem(selectedItem);
			updateActiveButtons();
		}
		
		if (shopLeftButton.checkConsumePress()) {
			if (pageNum == 0) {
				pageNum = totalPages() - 1;
			}
			else {
				pageNum--;
			}
			
			updateActiveButtons();
		}
		
		if (shopRightButton.checkConsumePress()) {
			if (pageNum == totalPages() - 1) {
				pageNum = 0;
			}
			else {
				pageNum++;
			}
			
			updateActiveButtons();
		}
		
		if (returnButton.checkConsumePress()) {
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
		
		// Background
		g.drawImage(tiles.getTile(0x2), 0,0, null);
		
		// Info Boxes
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(42, 92, 716, 466);
		
		g.drawImage(tiles.getTile(0x21), 42, 92, null);
		g.drawImage(tiles.getTile(0x22), 62, 112, null);
		
		if (!amountLeftButton.isActive()) {
			amountLeftButton.greyOut(g, false);
			amountRightButton.greyOut(g, false);
		}
		
		// Item Display
		if (selectedItem != null) {
			// Draw item image
			BufferedImage img = itemTiles.getTile(selectedItem.getImageIndex());
			DrawMetrics.drawCenteredImage(g, img, 430, 132);
			
			g.setColor(Color.BLACK);
			DrawMetrics.setFont(g, 20);
			g.drawString(selectedItem.getName(), 448, 138);
			
			DrawMetrics.setFont(g, 14);
			DrawMetrics.drawWrappedText(g, selectedItem.getDescription(), 418, 156, 726 - amountLeftButton.x);
			
			DrawMetrics.setFont(g, 20);
			g.drawImage(tiles.getTile(0x28), 410, 193, null);
			
			g.drawString(itemAmount + "", 568, 218);
			View.drawArrows(g, amountLeftButton, amountRightButton, 35, 10);
		}
		
		DrawMetrics.setFont(g, 12);
		g.setColor(Color.BLACK);
		
		// Draw each items in category
		Iterator<Item> iter = forSaleItems.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) {
			iter.next();
		}

		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				g.translate(itemButtons[k].x, itemButtons[k].y);
				Item item = iter.next();
				
				g.drawImage(tiles.getTile(0x26), 0,0, null);
				
				BufferedImage img = itemTiles.getTile(item.getImageIndex());
				DrawMetrics.drawCenteredImage(g, img, 14, 14);
				
				g.drawString(item.getName(), 29, 18);
				
				g.translate(-itemButtons[k].x, -itemButtons[k].y);
			}
		}
		
		// Draw page numbers
		DrawMetrics.setFont(g, 16);
		DrawMetrics.drawCenteredWidthString(g, (pageNum + 1) + "/" + totalPages(), 573, 466);
		
		// Left and Right arrows
		View.drawArrows(g, shopLeftButton, shopRightButton);

		// TODO: Honeslty I want to completely destroy the practice of doing the translate shit everywhere -- maybe can have an interface or something that takes in what to translate and requires a draw method and then takes care of the translation
		// Player Money
		g.translate(SIDE_BOX_X, MONEY_BOX_Y);
		
		g.setColor(SIDE_BOX_COLOR);
		g.fillPolygon(primaryColorx, primaryColory, 4);
		g.setColor(SIDE_BOX_COLOR);
		g.fillPolygon(secondaryColorx, secondaryColory, 4);
		
		g.drawImage(tiles.getTile(0x25), 0, 0, null);
		
		g.setColor(Color.BLACK);
		DrawMetrics.setFont(g, 18);
		g.drawString("Money: " + Global.MONEY_SYMBOL + player.getDatCashMoney(), BOX_TEXT_X, BOX_TEXT_Y);
		
		g.translate(-SIDE_BOX_X, -MONEY_BOX_Y);
		
		// In bag display
		g.translate(SIDE_BOX_X, IN_BAG_BOX_Y);
		
		g.setColor(SIDE_BOX_COLOR);
		g.fillPolygon(primaryColorx, primaryColory, 4);
		g.fillPolygon(secondaryColorx, secondaryColory, 4);
		
		g.drawImage(tiles.getTile(0x25), 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.drawString("In Bag: " + player.getBag().getQuantity(selectedItem), BOX_TEXT_X, BOX_TEXT_Y);
		
		g.translate(-SIDE_BOX_X, -IN_BAG_BOX_Y);
		
		// Total display
		g.translate(SIDE_BOX_X, TOTAL_BOX_Y);
						
		g.setColor(SIDE_BOX_COLOR);
		g.fillPolygon(primaryColorx, primaryColory, 4);
		g.fillPolygon(secondaryColorx, secondaryColory, 4);
		
		g.drawImage(tiles.getTile(0x25), 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.drawString("Total: " + Global.MONEY_SYMBOL + selectedItem.getPrice()*itemAmount, BOX_TEXT_X, BOX_TEXT_Y);
		
		g.translate(-SIDE_BOX_X, -TOTAL_BOX_Y);
		
		// Buy button
		g.translate(buyButton.x, buyButton.y);
						
		g.setColor(SIDE_BOX_COLOR);
		g.fillPolygon(primaryColorx, primaryColory, 4);
		g.fillPolygon(secondaryColorx, secondaryColory, 4);
		
		g.drawImage(tiles.getTile(0x25), 0, 0, null);
		
		DrawMetrics.setFont(g, 24);
		g.setColor(Color.BLACK);
		g.drawString("BUY", 120, 39);

		buyButton.greyOut(g, true);
		
		if (!buyButton.isActive()) {
			g.setColor(new Color(0, 0, 0, 128));
			g.fillRect(0, 0, buyButton.width, buyButton.height);
		}
		
		g.translate(-buyButton.x, -buyButton.y);
		
		// Return button
		g.setColor(Color.BLACK);
		DrawMetrics.setFont(g, 20);
		g.drawImage(tiles.getTile(0x27), 410, 500, null);
		DrawMetrics.drawCenteredWidthString(g, "Return", 573, 525);
		
		// Tab
		int tabX = 42 + 102, tabY = 42;
		g.translate(tabX, tabY);
		
		DrawMetrics.setFont(g, 16);
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, 104, 52);
		
		g.drawImage(tiles.getTile(0x23), 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.drawString("Pok\u00e9 Mart", 13, 30);
		
		g.translate(-tabX, -tabY);
		
		for (Button b : buttons) {
			b.draw(g);
		}
	}

	public ViewMode getViewModel() {
		return ViewMode.MART_VIEW;
	}

	public void movedToFront(Game game) { }
	
	private int totalPages() {
		int size = forSaleItems.size();
		return size/ITEMS_PER_PAGE + (size == 0 || size%ITEMS_PER_PAGE != 0 ? 1 : 0);
	}
	
	private void updateActiveButtons() {
		int displayed = forSaleItems.size();
		for (int i = 0; i < ITEMS_PER_PAGE; i++) {
			itemButtons[i].setActive(i < displayed - pageNum*ITEMS_PER_PAGE);
		}
		
		boolean amountSet = itemAmount > 0;
		amountLeftButton.setActive(amountSet);
		amountRightButton.setActive(amountSet);
		buyButton.setActive(amountSet);
	}	

	private void setSelectedItem(Item item) {
		selectedItem = item;
		itemAmount = selectedItem.getPrice() <= player.getDatCashMoney() ? 1 : 0;
	}
}
