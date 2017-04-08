package gui.view;

import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import item.berry.farm.BerryFarm;
import item.berry.farm.BerryStats;
import main.Game;
import main.Global;
import map.Direction;
import trainer.player.Player;
import util.FontMetrics;
import util.GeneralUtils;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class BerryView extends View {
    private static final Color BACKGROUND_COLOR = BagCategory.BERRY.getColor();

    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_COLS = 4;
    private static final int NUM_ROWS = BerryFarm.MAX_BERRIES/NUM_COLS;

    private static final int NUM_BUTTONS = ITEMS_PER_PAGE + 4;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int HARVEST = NUM_BUTTONS - 2;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 3;
    private static final int LEFT_ARROW = NUM_BUTTONS - 4;

    private final DrawPanel farmPanel;
    private final DrawPanel tabPanel;
    private final DrawPanel berryPanel;
    private final DrawPanel itemsPanel;
    private final DrawPanel selectedPanel;
    private final DrawPanel[] berryPanels;

    private final Button[] buttons;
    private final Button[] itemButtons;

    private int pageNum;
    private int selectedButton;
    private String message;
    private ItemNamesies selectedItem;

    private BerryFarm berryFarm;

    BerryView() {
        int tabHeight = 55;
        int spacing = 28;

        farmPanel = new DrawPanel(
                spacing,
                spacing + tabHeight,
                Point.subtract(Global.GAME_SIZE,
                        2*spacing,
                        2*spacing + tabHeight))
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBackgroundColor(BACKGROUND_COLOR)
                .withBlackOutline();

        tabPanel = new DrawPanel(
                farmPanel.x + 2*farmPanel.width/3,
                farmPanel.y - tabHeight + DrawUtils.OUTLINE_SIZE,
                farmPanel.width/6,
                tabHeight)
                .withBackgroundColor(BACKGROUND_COLOR)
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.DOWN)));

        int buttonHeight = 38;
        int selectedHeight = 82;
        int halfPanelWidth = (farmPanel.width - 3*spacing)/2;
        berryPanel = new DrawPanel(
                farmPanel.x + spacing,
                farmPanel.y + spacing,
                halfPanelWidth,
                farmPanel.height - 2*spacing)
                .withFullTransparency()
                .withBlackOutline();

        selectedPanel = new DrawPanel(
                berryPanel.rightX() + spacing,
                farmPanel.y + spacing,
                halfPanelWidth,
                selectedHeight)
                .withFullTransparency()
                .withBlackOutline();

        Button returnButton = new Button(
                selectedPanel.x,
                farmPanel.bottomY() - spacing - buttonHeight,
                halfPanelWidth,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { -1, RIGHT_ARROW, -1, RIGHT_ARROW });

        itemsPanel = new DrawPanel(
                selectedPanel.x,
                selectedPanel.bottomY() + buttonHeight + spacing,
                halfPanelWidth,
                berryPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing)
                .withFullTransparency()
                .withBlackOutline();

        Button[] berryButtons = berryPanel.getButtons(10, NUM_ROWS, NUM_COLS);
        berryPanels = new DrawPanel[NUM_ROWS*NUM_COLS];
        for (int i = 0; i < berryPanels.length; i++) {
            berryPanels[i] = new DrawPanel(berryButtons[i]).withBlackOutline().withFullTransparency();
        }

        selectedButton = 0;
        selectedItem = ItemNamesies.NO_ITEM;

        itemButtons = itemsPanel.getButtons(
                5,
                ITEMS_PER_PAGE/2 + 1,
                2,
                ITEMS_PER_PAGE/2,
                2,
                0,
                new int[] { -1, HARVEST, -1, RIGHT_ARROW }
        );

        buttons = new Button[NUM_BUTTONS];
        System.arraycopy(itemButtons, 0, buttons, 0, ITEMS_PER_PAGE);

        Button harvestButton = new Button(
                selectedPanel.x,
                selectedPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                selectedPanel.width,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { -1, RETURN, -1, 0 }
        );

        int arrowHeight = 20;
        Button leftArrow  = new Button(
                itemsPanel.x + itemsPanel.width/4,
                itemButtons[itemButtons.length - 1].centerY() + (itemButtons[2].y - itemButtons[0].y) - arrowHeight/2,
                35,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { RIGHT_ARROW, ITEMS_PER_PAGE - 2, RIGHT_ARROW, RETURN }
        );

        Button rightArrow = new Button(
                itemsPanel.rightX() - (leftArrow.x - itemsPanel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height,
                ButtonHoverAction.BOX,
                new int[] { LEFT_ARROW, ITEMS_PER_PAGE - 1, LEFT_ARROW, RETURN }
        );

        buttons[HARVEST] = harvestButton;
        buttons[LEFT_ARROW] = leftArrow;
        buttons[RIGHT_ARROW] = rightArrow;
        buttons[RETURN] = returnButton;
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();
        Set<ItemNamesies> berries = Game.getPlayer().getBag().getCategory(BagCategory.BERRY);

        selectedButton = Button.update(buttons, selectedButton);

        if (message != null) {
            if (input.consumeIfMouseDown(ControlKey.SPACE)) {
                message = null;
            }

            return;
        }

        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(berries, pageNum, ITEMS_PER_PAGE);
        for (int i = 0; i < ITEMS_PER_PAGE && iter.hasNext(); i++) {
            ItemNamesies item = iter.next();
            if (itemButtons[i].checkConsumePress()) {
                selectedItem = item;
                updateActiveButtons();
            }
        }

        if (buttons[HARVEST].checkConsumePress()) {
            message = berryFarm.harvest(selectedItem);
            updateActiveButtons();
        }

        if (buttons[LEFT_ARROW].checkConsumePress()) {
            pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages(berries.size()));
            updateActiveButtons();
        }

        if (buttons[RIGHT_ARROW].checkConsumePress()) {
            pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages(berries.size()));
            updateActiveButtons();
        }

        if (buttons[RETURN].checkConsumePress()
                || input.consumeIfDown(ControlKey.ESC)) {
            Game.instance().popView();
        }
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();
        Player player = Game.getPlayer();

        TileSet itemTiles = data.getItemTiles();
        Bag bag = player.getBag();

        // Background
        BasicPanels.drawCanvasPanel(g);

        // Info Boxes
        farmPanel.drawBackground(g);

        // Selected item Display
        selectedPanel.drawBackground(g);
        if (selectedItem != ItemNamesies.NO_ITEM) {
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

            if (selectedItemValue.hasQuantity()) {
                String quantityString = "x" + bag.getQuantity(selectedItem);
                TextUtils.drawRightAlignedString(g, quantityString, selectedPanel.rightX() - 2*spacing, startY);
            }

            FontMetrics.setFont(g, 14);
            TextUtils.drawWrappedText(
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
        Set<ItemNamesies> berries = bag.getCategory(BagCategory.BERRY);
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(berries, pageNum, ITEMS_PER_PAGE);

        for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++) {
            for (int y = 0; y < 2 && iter.hasNext(); y++, k++) {
                ItemNamesies item = iter.next();
                Item itemValue = item.getItem();
                Button itemButton = itemButtons[k];

                itemButton.fill(g, Color.WHITE);
                itemButton.blackOutline(g);

                g.translate(itemButton.x, itemButton.y);

                ImageUtils.drawCenteredImage(g, itemTiles.getTile(itemValue.getImageName()), 14, 14);

                g.drawString(item.getName(), 29, 18);

                if (itemValue.hasQuantity()) {
                    TextUtils.drawRightAlignedString(g, "x" + bag.getQuantity(item), 142, 18);
                }

                g.translate(-itemButton.x, -itemButton.y);
            }
        }

        // Draw page numbers
        FontMetrics.setFont(g, 16);
        TextUtils.drawCenteredString(g, (pageNum + 1) + "/" + totalPages(berries.size()), itemsPanel.centerX(), buttons[RIGHT_ARROW].centerY());

        // Left and Right arrows
        buttons[LEFT_ARROW].drawArrow(g, Direction.LEFT);
        buttons[RIGHT_ARROW].drawArrow(g, Direction.RIGHT);

        // Berry Panel
        berryPanel.drawBackground(g);
        for (int i = 0; i < berryPanels.length; i++) {
            DrawPanel panel = berryPanels[i];
            panel.drawBackground(g);

            BerryStats berry = berryFarm.getBerry(i);
            if (berry != null) {
                ImageUtils.drawCenteredImage(
                        g,
                        itemTiles.getTile(berry.getBerry().getImageName()),
                        panel.centerX(),
                        panel.y + panel.height / 3
                );

                TextUtils.drawCenteredString(
                        g,
                        berry.getTimeLeftString(),
                        panel.centerX(),
                        panel.y + 3* panel.height/4
                );
            }
        }

        // Welcome to the Hellmouth
        Button harvestButton = buttons[HARVEST];
        harvestButton.fillTransparent(g);
        harvestButton.blackOutline(g);
        harvestButton.label(g, 20, "Harvest!");

        Button returnButton = buttons[RETURN];
        returnButton.fillTransparent(g);
        returnButton.blackOutline(g);
        returnButton.label(g, 20, "Return");

        // Tab
        tabPanel.drawBackground(g);
        tabPanel.label(g, 16, "Berries!!");

        if (!StringUtils.isNullOrWhiteSpace(message)) {
            BasicPanels.drawFullMessagePanel(g, message);
        }
        else {
            for (Button button : buttons) {
                button.draw(g);
            }
        }
    }

    private int totalPages(int size) {
        return size/ITEMS_PER_PAGE + (size == 0 || size%ITEMS_PER_PAGE != 0 ? 1 : 0);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.BERRY_VIEW;
    }

    @Override
    public void movedToFront() {
        this.berryFarm = Game.getPlayer().getBerryFarm();
        this.message = null;
        this.selectedItem = ItemNamesies.NO_ITEM;

        updateActiveButtons();
    }

    private void updateActiveButtons() {
        Set<ItemNamesies> berries = Game.getPlayer().getBag().getCategory(BagCategory.BERRY);
        if (selectedItem == ItemNamesies.NO_ITEM) {
            selectedItem = berries.isEmpty() ? ItemNamesies.NO_ITEM : berries.iterator().next();
        }

        int displayed = berries.size();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            itemButtons[i].setActive(i < displayed - pageNum*ITEMS_PER_PAGE);
        }
    }
}
