package gui.view;

import draw.DrawUtils;
import draw.ImageUtils;
import draw.PolygonUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.InputControl;
import item.Item;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.Direction;
import trainer.Badge;
import trainer.player.Player;
import trainer.player.medal.Medal;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;
import util.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

class MartView extends View {
	private List<ItemNamesies> forSaleItems;
	
	private static final Color BACKGROUND_COLOR = new Color (68, 123, 184);
	
	private static final int ITEMS_PER_PAGE = 10;
	
	private static final int NUM_BUTTONS = ITEMS_PER_PAGE + 6;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int BUY = NUM_BUTTONS - 2;
	private static final int AMOUNT_LEFT_ARROW = NUM_BUTTONS - 3;
	private static final int AMOUNT_RIGHT_ARROW = NUM_BUTTONS - 4;
	private static final int SHOP_RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int SHOP_LEFT_ARROW = NUM_BUTTONS - 6;

	private final DrawPanel shopPanel;
	private final DrawPanel tabPanel;
	private final DrawPanel moneyPanel;
	private final DrawPanel itemsPanel;
	private final DrawPanel selectedPanel;
	private final DrawPanel amountPanel;
	private final DrawPanel playerMoneyPanel;
	private final DrawPanel inBagPanel;
	private final DrawPanel itemAmountPanel;
	
	private final Button[] buttons;
	private final Button[] itemButtons;
	private final Button amountLeftButton;
	private final Button amountRightButton;
	private final Button buyButton;
	private final Button shopLeftButton;
	private final Button shopRightButton;
	private final Button returnButton;

	private int pageNum;
	private int selectedButton;
	private int itemAmount;

	private ItemNamesies selectedItem;
	
	MartView() {
		int tabHeight = 55;
		int spacing = 28;

		shopPanel = new DrawPanel(
				spacing,
				spacing + tabHeight,
				Point.subtract(Global.GAME_SIZE,
						2*spacing,
						2*spacing + tabHeight))
				.withBackgroundColor(BACKGROUND_COLOR)
				.withTransparentBackground()
				.withBorderPercentage(0)
				.withBlackOutline();

		tabPanel = new DrawPanel(
				shopPanel.x + shopPanel.width/6,
				shopPanel.y - tabHeight + DrawUtils.OUTLINE_SIZE,
				shopPanel.width/6,
				tabHeight)
				.withBackgroundColor(BACKGROUND_COLOR)
				.withTransparentBackground()
				.withBorderPercentage(0)
				.withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.DOWN)));

		int buttonHeight = 38;
		int selectedHeight = 82;
		int halfPanelWidth = (shopPanel.width - 3*spacing)/2;

		moneyPanel = new DrawPanel(
				shopPanel.x + spacing,
				shopPanel.y + spacing,
				halfPanelWidth,
				shopPanel.height - 2*spacing)
				.withFullTransparency()
				.withBlackOutline();

		selectedPanel = new DrawPanel(
				moneyPanel.x + moneyPanel.width + spacing,
				shopPanel.y + spacing,
				halfPanelWidth,
				selectedHeight)
				.withFullTransparency()
				.withBlackOutline();

		Button[] fakeButtons = moneyPanel.getButtons(10, 6, 1);
		playerMoneyPanel = new DrawPanel(fakeButtons[0]).withBlackOutline();
		inBagPanel = new DrawPanel(fakeButtons[1]).withBlackOutline();
		itemAmountPanel = new DrawPanel(fakeButtons[4]).withBlackOutline();

		buyButton = new Button(
				fakeButtons[5].x,
				fakeButtons[5].y,
				fakeButtons[5].width,
				fakeButtons[5].height,
				ButtonHoverAction.BOX,
				new int[] { RETURN, BUY, RETURN, BUY },
				() -> {
					Player player = Game.getPlayer();
					player.sucksToSuck(itemAmount*selectedItem.getItem().getPrice());
					player.getBag().addItem(selectedItem, itemAmount);
					player.getMedalCase().itemsBought.increase(itemAmount);

					if (selectedItem == ItemNamesies.POKE_BALL && itemAmount >= 10) {
						player.getBag().addItem(ItemNamesies.PREMIER_BALL);
						player.getMedalCase().earnMedal(Medal.SMART_SHOPPER);
					}

					setSelectedItem(selectedItem);
				}
		);

		amountLeftButton = new Button(
				selectedPanel.x,
				selectedPanel.y + selectedPanel.height - DrawUtils.OUTLINE_SIZE,
				selectedPanel.width/3,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { AMOUNT_RIGHT_ARROW, RETURN, BUY, 0 },
				() -> this.updateItemAmount(-1)
		);

		amountRightButton = new Button(
				selectedPanel.rightX() - amountLeftButton.width,
				amountLeftButton.y,
				amountLeftButton.width,
				amountLeftButton.height,
				ButtonHoverAction.BOX,
				new int[] { AMOUNT_LEFT_ARROW, RETURN, AMOUNT_LEFT_ARROW, 1 },
				() -> this.updateItemAmount(1)
		);

		amountPanel = new DrawPanel(
				amountLeftButton.x + amountLeftButton.width - DrawUtils.OUTLINE_SIZE,
				amountLeftButton.y,
				selectedPanel.width - amountLeftButton.width - amountRightButton.width + 2*DrawUtils.OUTLINE_SIZE,
				amountLeftButton.height)
				.withFullTransparency()
				.withBlackOutline();

		returnButton = Button.createExitButton(
				selectedPanel.x,
				shopPanel.y + shopPanel.height - spacing - buttonHeight,
				halfPanelWidth,
				buttonHeight,
				ButtonHoverAction.BOX,
				new int[] { BUY, SHOP_LEFT_ARROW, BUY, AMOUNT_LEFT_ARROW });

		itemsPanel = new DrawPanel(
				selectedPanel.x,
				selectedPanel.y + selectedPanel.height + buttonHeight + spacing,
				halfPanelWidth,
				moneyPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing)
				.withFullTransparency()
				.withBlackOutline();

		selectedButton = 0;
		itemAmount = 1;

		buttons = new Button[NUM_BUTTONS];

		itemButtons = itemsPanel.getButtons(
				5,
				ITEMS_PER_PAGE/2 + 1,
				2,
				ITEMS_PER_PAGE/2,
				2,
				0,
				new int[] { -1, AMOUNT_RIGHT_ARROW, -1, SHOP_RIGHT_ARROW },
				index -> setSelectedItem(GeneralUtils.getPageValue(forSaleItems, pageNum, ITEMS_PER_PAGE, index)));
		System.arraycopy(itemButtons, 0, buttons, 0, ITEMS_PER_PAGE);
		
		buttons[SHOP_LEFT_ARROW] = shopLeftButton = new Button(
				498,
				451,
				35,
				20,
				ButtonHoverAction.BOX,
				new int[] { SHOP_RIGHT_ARROW, ITEMS_PER_PAGE - 2, SHOP_RIGHT_ARROW, RETURN },
				() -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
		);

		buttons[SHOP_RIGHT_ARROW] = shopRightButton = new Button(
				613,
				451,
				35,
				20,
				ButtonHoverAction.BOX,
				new int[] { SHOP_LEFT_ARROW, ITEMS_PER_PAGE - 1, SHOP_LEFT_ARROW, RETURN },
				() -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
		);

		buttons[BUY] = buyButton;

		buttons[AMOUNT_LEFT_ARROW] = amountLeftButton;
		buttons[AMOUNT_RIGHT_ARROW] = amountRightButton;
		buttons[RETURN] = returnButton;

		resetForSaleItems();
		setSelectedItem(forSaleItems.get(0));
		updateActiveButtons();
	}

	@Override
	public void update(int dt) {
		selectedButton = Button.update(buttons, selectedButton);
		if (buttons[selectedButton].checkConsumePress()) {
			updateActiveButtons();
		}

		InputControl.instance().popViewIfEscaped();
	}

	@Override
	public void draw(Graphics g) {
		GameData data = Game.getData();
		Player player = Game.getPlayer();

		TileSet itemTiles = data.getItemTiles();
		
		// Background
		BasicPanels.drawCanvasPanel(g);
		
		// Info Boxes
		shopPanel.drawBackground(g);
		
		if (!amountLeftButton.isActive()) {
			amountLeftButton.greyOut(g);
			amountRightButton.greyOut(g);
		}
		
		// Item Display
		selectedPanel.drawBackground(g);
		if (selectedItem != null) {
			int spacing = 8;

			Item selectedItemValue = selectedItem.getItem();

			g.setColor(Color.BLACK);
			FontMetrics.setFont(g, 20);

			int startY = selectedPanel.y + FontMetrics.getDistanceBetweenRows(g);
			int nameX = selectedPanel.x + 2*spacing + Global.TILE_SIZE; // TODO: Why are we using Tile Size in the bag view

			// Draw item image
			BufferedImage img = itemTiles.getTile(selectedItemValue.getImageName());
			ImageUtils.drawBottomCenteredImage(g, img, selectedPanel.x + (nameX - selectedPanel.x)/2, startY);

			g.drawString(selectedItem.getName(), nameX, startY);

			FontMetrics.setFont(g, 14);
			TextUtils.drawWrappedText(
					g,
					selectedItemValue.getDescription(),
					selectedPanel.x + spacing,
					startY + FontMetrics.getDistanceBetweenRows(g),
					selectedPanel.width - 2*spacing
			);

			amountPanel.drawBackground(g);
			amountPanel.label(g, 20, itemAmount + "");

			amountLeftButton.fillTransparent(g);
			amountLeftButton.blackOutline(g);
			PolygonUtils.drawCenteredArrow(g, amountLeftButton.centerX(), amountLeftButton.centerY(), 35, 20, Direction.LEFT);

			amountRightButton.fillTransparent(g);
			amountRightButton.blackOutline(g);
			PolygonUtils.drawCenteredArrow(g, amountRightButton.centerX(), amountRightButton.centerY(), 35, 20, Direction.RIGHT);
		}
		
		FontMetrics.setFont(g, 12);
		g.setColor(Color.BLACK);
		
		// Draw each items in category
		itemsPanel.drawBackground(g);
		Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(forSaleItems, pageNum, ITEMS_PER_PAGE);
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
				ItemNamesies item = iter.next();
				BufferedImage img = itemTiles.getTile(item.getItem().getImageName());

				Button itemButton = itemButtons[k];
				itemButton.fill(g, Color.WHITE);
				itemButton.blackOutline(g);

				g.translate(itemButton.x, itemButton.y);

				ImageUtils.drawCenteredImage(g, img, 14, 14);
				g.drawString(item.getName(), 29, 18);
				
				g.translate(-itemButton.x, -itemButton.y);
			}
		}
		
		// Draw page numbers
		FontMetrics.setFont(g, 16);
		TextUtils.drawCenteredWidthString(g, (pageNum + 1) + "/" + totalPages(), 573, 466);
		
		// Left and Right arrows
		shopLeftButton.drawArrow(g, Direction.LEFT);
		shopRightButton.drawArrow(g, Direction.RIGHT);

		moneyPanel.drawBackground(g);

		// Player Money
		playerMoneyPanel.drawBackground(g);
		playerMoneyPanel.label(g, 18, "Money: " + Global.MONEY_SYMBOL + player.getDatCashMoney());
		
		// In bag display
		inBagPanel.drawBackground(g);
		inBagPanel.label(g, 18, "In Bag: " + player.getBag().getQuantity(selectedItem));
		
		// Total display
		itemAmountPanel.drawBackground(g);
		itemAmountPanel.label(g, 18, "Total: " + Global.MONEY_SYMBOL + selectedItem.getItem().getPrice()*itemAmount);

		// Buy button
		buyButton.fillTransparent(g);
		if (!buyButton.isActive()) {
			buyButton.greyOut(g);
		}

		buyButton.label(g, 24, "BUY");
		buyButton.blackOutline(g);

		// Return button
		returnButton.fillTransparent(g);
		returnButton.blackOutline(g);
		returnButton.label(g, 20, "Return");
		
		// Tab
		tabPanel.drawBackground(g);
		tabPanel.label(g, 16, PokeString.POKE + " Mart");
		
		for (Button button : buttons) {
			button.draw(g);
		}
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.MART_VIEW;
	}

	private void resetForSaleItems() {
		Player player = Game.getPlayer();

		this.forSaleItems = new ArrayList<>();
		Collections.addAll(forSaleItems,
				ItemNamesies.POTION,
				ItemNamesies.POKE_BALL,
				ItemNamesies.ANTIDOTE,
				ItemNamesies.PARALYZE_HEAL,
				ItemNamesies.BURN_HEAL,
				ItemNamesies.AWAKENING,
				ItemNamesies.ICE_HEAL
		);

		if (player.hasBadge(Badge.ROUND)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.GREAT_BALL,
					ItemNamesies.SUPER_POTION,
					ItemNamesies.REPEL
			);
		}

		if (player.hasBadge(Badge.SECOND)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.REVIVE
			);
		}

		if (player.hasBadge(Badge.THIRD)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.SUPER_REPEL
			);
		}

		if (player.hasBadge(Badge.FOURTH)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.ULTRA_BALL,
					ItemNamesies.HYPER_POTION
			);
		}

		if (player.hasBadge(Badge.FIFTH)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.FULL_HEAL,
					ItemNamesies.MAX_REPEL
			);
		}

		if (player.hasBadge(Badge.SIXTH)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.MAX_POTION
			);
		}

		if (player.hasBadge(Badge.SEVENTH)) {
			Collections.addAll(forSaleItems,
					ItemNamesies.FULL_RESTORE
			);
		}

		this.forSaleItems.sort((firstItemName, secondItemName) -> {
            Item firstItem = firstItemName.getItem();
            Item secondItem = secondItemName.getItem();

            if (firstItem.getBagCategory() != secondItem.getBagCategory()) {
                return firstItem.getBagCategory().ordinal() - secondItem.getBagCategory().ordinal();
            }

            if (firstItem.getPrice() != secondItem.getPrice()) {
                return firstItem.getPrice() - secondItem.getPrice();
            }

            return firstItem.getName().compareTo(secondItem.getName());
        });
	}

	@Override
	public void movedToFront() {
		this.resetForSaleItems();
		this.updateActiveButtons();
	}

	private void updateItemAmount(int delta) {
		this.itemAmount = GeneralUtils.wrapIncrement(this.itemAmount, delta, 1, this.maxPurchaseAmount());
	}

	private int maxPurchaseAmount() {
		return Game.getPlayer().getDatCashMoney()/selectedItem.getItem().getPrice();
	}

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

	private void setSelectedItem(ItemNamesies item) {
		selectedItem = item;
		itemAmount = selectedItem.getItem().getPrice() <= Game.getPlayer().getDatCashMoney() ? 1 : 0;
	}
}
