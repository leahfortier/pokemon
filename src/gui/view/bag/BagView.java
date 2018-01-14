package gui.view.bag;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LearnMovePanel;
import gui.GameData;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
import input.ControlKey;
import input.InputControl;
import item.Item;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BagCategory;
import item.use.TechnicalMachine;
import main.Game;
import main.Global;
import map.Direction;
import message.MessageUpdate;
import message.Messages;
import message.Messages.MessageState;
import pokemon.PartyPokemon;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
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

    private final Button[] buttons;
    private final Button[] tabButtons;
    private final Button[] partyButtons;
    private final Button[] moveButtons;
    private final Button[] itemButtons;

    BagState state;
    ItemNamesies selectedItem;
    PartyPokemon selectedPokemon;

    private int pageNum;
    private int selectedButton;

    private BagCategory selectedTab;
    private MessageUpdate message;
    private LearnMovePanel learnMovePanel;

    public BagView() {
        int tabHeight = 55;
        int spacing = 28;

        bagPanel = new DrawPanel(
                spacing,
                spacing + tabHeight,
                Point.subtract(
                        Global.GAME_SIZE,
                        2*spacing,
                        2*spacing + tabHeight
                )
        )
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
                bagPanel.height - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedPanel = new DrawPanel(
                pokemonPanel.rightX() + spacing,
                bagPanel.y + spacing,
                halfPanelWidth,
                selectedHeight
        )
                .withFullTransparency()
                .withBlackOutline();

        Button returnButton = new Button(
                selectedPanel.x,
                bagPanel.bottomY() - spacing - buttonHeight,
                halfPanelWidth,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { PARTY, RIGHT_ARROW, PARTY, 0 },
                this::returnToMap
        );

        itemsPanel = new DrawPanel(
                selectedPanel.x,
                selectedPanel.bottomY() + buttonHeight + spacing,
                halfPanelWidth,
                pokemonPanel.height - selectedPanel.height - 2*buttonHeight - 2*spacing
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedTab = CATEGORIES[0];
        selectedButton = 0;
        selectedItem = ItemNamesies.NO_ITEM;

        tabButtons = new Button[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
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
                    },
                    () -> changeCategory(index)
            );
        }

        partyButtons = pokemonPanel.getButtons(
                10,
                Trainer.MAX_POKEMON,
                1,
                PARTY,
                new int[] { GIVE, 0, MOVES, 0 },
                index -> {
                    for (UseState useState : UseState.values()) {
                        useState.use(this, Game.getPlayer().getTeam().get(index));
                    }
                }
        );

        moveButtons = pokemonPanel.getButtons(
                10,
                Trainer.MAX_POKEMON,
                1,
                MOVES,
                new int[] { PARTY, 0, GIVE, 0 },
                index -> Game.getPlayer().getBag().useMoveItem(selectedItem, selectedPokemon, selectedPokemon.getActualMoves().get(index))
        );

        itemButtons = itemsPanel.getButtons(
                5,
                ITEMS_PER_PAGE/2 + 1,
                2,
                ITEMS_PER_PAGE/2,
                2,
                ITEMS,
                new int[] { -1, USE, -1, RIGHT_ARROW },
                index -> selectedItem = GeneralUtils.getPageValue(Game.getPlayer().getBag().getCategory(selectedTab), pageNum, ITEMS_PER_PAGE, index)
        );

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
                            selectedTab.ordinal(), // Up
                            tabIndex == 0 ? PARTY : useStates[tabIndex - 1].buttonIndex, // Left
                            tabIndex <= useStates.length/2 ? ITEMS : ITEMS + 1 // Down
                    },
                    () -> useState.update(this)
            );
        }

        int arrowHeight = 20;
        Button leftArrow = new Button(
                itemsPanel.x + itemsPanel.width/4,
                itemButtons[itemButtons.length - 1].centerY() + (itemButtons[2].y - itemButtons[0].y) - arrowHeight/2,
                35,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { RIGHT_ARROW, ITEMS + ITEMS_PER_PAGE - 2, RIGHT_ARROW, RETURN },
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        Button rightArrow = new Button(
                itemsPanel.rightX() - (leftArrow.x - itemsPanel.x) - leftArrow.width,
                leftArrow.y,
                leftArrow.width,
                leftArrow.height,
                ButtonHoverAction.BOX,
                new int[] { LEFT_ARROW, ITEMS + ITEMS_PER_PAGE - 1, LEFT_ARROW, RETURN },
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        buttons[LEFT_ARROW] = leftArrow;
        buttons[RIGHT_ARROW] = rightArrow;

        buttons[RETURN] = returnButton;

        movedToFront();
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();

        if (learnMovePanel != null) {
            learnMovePanel.update();
            if (learnMovePanel.isFinished()) {
                learnMovePanel = null;
            }
        } else {
            if (message != null && input.consumeIfMouseDown(ControlKey.SPACE)) {
                message = null;
            }

            while ((message == null || StringUtils.isNullOrWhiteSpace(message.getMessage()))
                    && Messages.hasMessages()) {
                message = Messages.getNextMessage();

                if (message.isViewChange()) {
                    Game.instance().setViewMode(message.getViewMode());
                }

                if (message.learnMove()) {
                    this.learnMovePanel = new LearnMovePanel(message.getMoveLearner(), message.getMove());
                    break;
                }
            }

            selectedButton = Button.update(buttons, selectedButton);
            if (buttons[selectedButton].checkConsumePress()) {
                updateActiveButtons();
            }

            if (input.consumeIfDown(ControlKey.ESC)) {
                returnToMap();
            }
        }
    }

    private void returnToMap() {
        Messages.clearMessages(MessageState.BAGGIN_IT_UP);
        Messages.setMessageState(MessageState.MAPPITY_MAP);
        Game.instance().setViewMode(ViewMode.MAP_VIEW);
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();
        Player player = Game.getPlayer();

        TileSet itemTiles = data.getItemTiles();
        TileSet partyTiles = data.getPartyTiles();

        Bag bag = player.getBag();
        List<PartyPokemon> team = player.getTeam();

        // Background
        BasicPanels.drawCanvasPanel(g);

        // Info Boxes
        bagPanel.withBackgroundColor(selectedTab.getColor())
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
        Set<ItemNamesies> list = bag.getCategory(selectedTab);
        Iterator<ItemNamesies> iter = GeneralUtils.pageIterator(list, pageNum, ITEMS_PER_PAGE);
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
        TextUtils.drawCenteredString(g, (pageNum + 1) + "/" + totalPages(), itemsPanel.centerX(), buttons[RIGHT_ARROW].centerY());

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
                TextUtils.drawCenteredHeightString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 166, movePanel.centerY());

                g.setColor(Color.BLACK);
                FontMetrics.setFont(g, 20);
                g.drawString(attack.getName(), 20, 38);

                g.translate(-moveButton.x, -moveButton.y);
            }
        }
        // Draw Pokemon Info
        else {
            for (int i = 0; i < team.size(); i++) {
                PartyPokemon p = team.get(i);
                Button pokemonButton = partyButtons[i];

                g.translate(pokemonButton.x, pokemonButton.y);

                DrawPanel pokemonPanel = new DrawPanel(0, 0, pokemonButton.width, pokemonButton.height)
                        .withBackgroundColors(Type.getColors(p))
                        .withTransparentCount(2)
                        .withBorderPercentage(15)
                        .withBlackOutline();
                pokemonPanel.drawBackground(g);

                BufferedImage img = partyTiles.getTile(p.getTinyImageName());
                ImageUtils.drawCenteredImage(g, img, 30, pokemonPanel.centerY());

                g.setColor(Color.BLACK);
                FontMetrics.setFont(g, 14);

                // Name and Gender
                g.drawString(p.getActualName() + " " + p.getGenderString(), 50, 22);

                if (!p.isEgg()) {
                    if (selectedTab == BagCategory.TM && selectedItem != ItemNamesies.NO_ITEM) {
                        AttackNamesies tm = ((TechnicalMachine)selectedItem.getItem()).getAttack();

                        final String message;
                        if (p.hasActualMove(tm)) {
                            message = "Learned.";
                        } else if (p.getPokemonInfo().canLearnMove(tm)) {
                            message = "Able!";
                        } else {
                            message = "Unable...";
                        }

                        pokemonPanel.drawRightLabel(g, 18, message);
                    } else {
                        // Level
                        g.drawString("Lv" + p.getLevel(), 153, 22);

                        // Status condition
                        TextUtils.drawRightAlignedString(g, p.getStatus().getType().getName(), 293, 22);

                        // Draw HP Box
                        g.fillRect(50, 26, 244, 11);
                        g.setColor(Color.WHITE);
                        g.fillRect(52, 28, 240, 7);
                        g.setColor(p.getHPColor());
                        g.fillRect(52, 28, (int)(p.getHPRatio()*240), 7);

                        g.setColor(Color.BLACK);
                        FontMetrics.setFont(g, 12);

                        g.drawString(p.getActualHeldItem().getName(), 50, 47);
                        TextUtils.drawRightAlignedString(g, p.getHP() + "/" + p.getMaxHP(), 293, 47);

                        if (!p.canFight()) {
                            // TODO: Look if this color appears in multiple place and see if it should be a constant
                            pokemonButton.fillTranslated(g, new Color(0, 0, 0, 128));
                        }
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
            tabButton.outlineTab(g, i, selectedTab.ordinal());

            g.translate(tabButton.x, tabButton.y);

            g.setColor(Color.BLACK);
            FontMetrics.setFont(g, 14);

            ImageUtils.drawCenteredImage(g, CATEGORIES[i].getIcon(), 16, 26);
            g.drawString(CATEGORIES[i].getDisplayName(), 30, 30);

            g.translate(-tabButton.x, -tabButton.y);
        }

        if (learnMovePanel != null) {
            learnMovePanel.draw(g);
        } else if (message != null && !StringUtils.isNullOrWhiteSpace(message.getMessage())) {
            BasicPanels.drawFullMessagePanel(g, message.getMessage());
        } else {
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
        changeCategory(this.selectedTab.ordinal());
    }

    private void changeCategory(int index) {
        if (selectedTab.ordinal() != index) {
            pageNum = 0;
        }

        selectedTab = CATEGORIES[index];
        state = BagState.ITEM_SELECT;

        Set<ItemNamesies> list = Game.getPlayer().getBag().getCategory(selectedTab);
        selectedItem = list.size() > 0 ? list.iterator().next() : ItemNamesies.NO_ITEM;

        // No more items on the current page
        if (list.size() < (pageNum + 1)*ITEMS_PER_PAGE) {
            pageNum = 0;
        }

        updateActiveButtons();
    }

    @Override
    public void movedToFront() {
        changeCategory(0);
    }

    private int totalPages() {
        int size = Game.getPlayer().getBag().getCategory(selectedTab).size();
        return size/ITEMS_PER_PAGE + (size == 0 || size%ITEMS_PER_PAGE != 0 ? 1 : 0);
    }

    void updateActiveButtons() {
        Player player = Game.getPlayer();

        List<PartyPokemon> team = player.getTeam();
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            partyButtons[i].setActive(state == BagState.POKEMON_SELECT && i < team.size());
        }

        int displayed = player.getBag().getCategory(selectedTab).size();
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
            buttons[GIVE].setActive(selectedItemValue.isHoldable());
            buttons[USE].setActive(selectedItemValue.isUsable());
        }
    }
}
