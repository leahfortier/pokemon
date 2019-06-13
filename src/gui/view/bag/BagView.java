package gui.view.bag;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
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
import item.use.MoveUseItem;
import item.use.PlayerUseItem;
import item.use.TechnicalMachine;
import item.use.UseItem.BagUseItem;
import main.Game;
import main.Global;
import map.Direction;
import message.MessageUpdate;
import message.Messages;
import message.Messages.MessageState;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import trainer.Trainer;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;
import util.GeneralUtils;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BagView extends View {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_BUTTONS = CATEGORIES.length + Trainer.MAX_POKEMON + ITEMS_PER_PAGE + MoveList.MAX_MOVES + 6 /* Misc Buttons */;
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

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] partyButtons;
    private final Button[] moveButtons;
    private final Button[] itemButtons;
    private final Button rightArrow;
    private final Button leftArrow;
    private final Button returnButton;

    private BagState state;
    private ItemNamesies selectedItem;
    private PartyPokemon selectedPokemon;

    private int pageNum;

    private BagCategory selectedTab;
    private MessageUpdate message;
    private LearnMovePanel learnMovePanel;

    public BagView() {
        selectedTab = CATEGORIES[0];
        selectedItem = ItemNamesies.NO_ITEM;

        BagPanel panel = new BagPanel();

        bagPanel = panel.bagPanel;
        pokemonPanel = panel.pokemonPanel;
        selectedPanel = panel.selectedPanel;
        itemsPanel = panel.itemsPanel;

        Button[] buttons = new Button[NUM_BUTTONS];
        this.buttons = new ButtonList(buttons);

        returnButton = new Button(
                panel.returnPanel,
                new ButtonTransitions().right(PARTY).up(RIGHT_ARROW).left(PARTY).down(0),
                this::returnToMap
        );

        tabButtons = new Button[CATEGORIES.length];
        for (int i = 0; i < tabButtons.length; i++) {
            final int index = i;
            tabButtons[i] = new Button(
                    panel.tabPanels[i],
                    new ButtonTransitions()
                            .up(RETURN)
                            .down(USE)
                            .basic(Direction.RIGHT, i, 1, tabButtons.length)
                            .basic(Direction.LEFT, i, 1, tabButtons.length),
                    () -> changeCategory(index)
            );
        }

        partyButtons = pokemonPanel.getButtons(
                10,
                Trainer.MAX_POKEMON,
                1,
                PARTY,
                new ButtonTransitions().right(GIVE).up(0).left(MOVES).down(0),
                index -> UseState.applyPokemon(this, Game.getPlayer().getTeam().get(index))
        );

        moveButtons = pokemonPanel.getButtons(
                10,
                Trainer.MAX_POKEMON,
                1,
                MOVES,
                new ButtonTransitions().right(PARTY).up(0).left(GIVE).down(0),
                this::useMoveItem
        );

        itemButtons = panel.getItemButtons(
                ITEMS,
                new ButtonTransitions().up(USE).down(RIGHT_ARROW),
                index -> selectedItem = GeneralUtils.getPageValue(Game.getPlayer().getBag().getCategory(selectedTab), pageNum, ITEMS_PER_PAGE, index)
        );

        UseState[] useStates = UseState.values();
        int lastIndex = useStates.length - 1;
        for (int i = 0; i < useStates.length; i++) {
            UseState useState = useStates[i];
            buttons[useState.buttonIndex] = new Button(
                    panel.buttonPanels[i],
                    new ButtonTransitions()
                            .right(i == lastIndex ? PARTY : useStates[i + 1].buttonIndex)
                            .up(selectedTab.ordinal())
                            .left(i == 0 ? PARTY : useStates[i - 1].buttonIndex)
                            .down(i <= useStates.length/2 ? ITEMS : ITEMS + 1),
                    () -> pressState(useState)
            );
        }

        leftArrow = new Button(
                panel.leftArrow,
                new ButtonTransitions().right(RIGHT_ARROW).up(ITEMS + ITEMS_PER_PAGE - 2).left(RIGHT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        rightArrow = new Button(
                panel.rightArrow,
                new ButtonTransitions().right(LEFT_ARROW).up(ITEMS + ITEMS_PER_PAGE - 1).left(LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        System.arraycopy(tabButtons, 0, buttons, 0, CATEGORIES.length);
        System.arraycopy(partyButtons, 0, buttons, PARTY, Trainer.MAX_POKEMON);
        System.arraycopy(moveButtons, 0, buttons, MOVES, MoveList.MAX_MOVES);
        System.arraycopy(itemButtons, 0, buttons, ITEMS, ITEMS_PER_PAGE);

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

            buttons.update();
            if (buttons.consumeSelectedPress()) {
                updateActiveButtons();
            }

            if (input.consumeIfDown(ControlKey.ESC)) {
                returnToMap();
            }
        }
    }

    public void returnToMap() {
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
        UseState.forEach(useState -> useState.draw(g, buttons.get(useState.buttonIndex), selectedTab.getColor()));

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
        TextUtils.drawCenteredString(g, (pageNum + 1) + "/" + totalPages(), itemsPanel.centerX(), rightArrow.centerY());

        // Left and Right arrows
        leftArrow.drawArrow(g, Direction.LEFT);
        rightArrow.drawArrow(g, Direction.RIGHT);

        // Draw moves
        pokemonPanel.drawBackground(g);
        if (state == BagState.MOVE_SELECT) {
            MoveList moveList = selectedPokemon.getActualMoves();

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
                        .withBackgroundColors(PokeType.getColors(p))
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
                        TextUtils.drawRightAlignedString(g, p.getStatus().getShortName(), 293, 22);

                        // Draw HP Box
                        g.fillRect(50, 26, 244, 11);
                        g.setColor(Color.WHITE);
                        g.fillRect(52, 28, 240, 7);
                        g.setColor(p.getHPColor());
                        g.fillRect(52, 28, (int)(p.getHPRatio()*240), 7);

                        g.setColor(Color.BLACK);
                        FontMetrics.setFont(g, 12);

                        g.drawString(p.getActualHeldItem().getName(), 50, 47);
                        TextUtils.drawRightAlignedString(g, p.getHpString(), 293, 47);

                        if (!p.canFight()) {
                            // TODO: Look if this color appears in multiple place and see if it should be a constant
                            // TODO: I think this can just be greyOut -- maybe I should make some sort of translation button thing
                            pokemonButton.fillTranslated(g, new Color(0, 0, 0, 128));
                        }
                    }
                }

                g.translate(-pokemonButton.x, -pokemonButton.y);
            }
        }

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
            buttons.draw(g);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.BAG_VIEW;
    }

    private void updateCategory() {
        changeCategory(this.selectedTab.ordinal());
    }

    private void changeCategory(int index) {
        if (selectedTab.ordinal() != index) {
            pageNum = 0;
        }

        selectedTab = CATEGORIES[index];
        state = BagState.ITEM_SELECT;

        Set<ItemNamesies> list = Game.getPlayer().getBag().getCategory(selectedTab);
        selectedItem = list.isEmpty() ? ItemNamesies.NO_ITEM : list.iterator().next();

        // No more items on the current page
        if (list.size() < (pageNum + 1)*ITEMS_PER_PAGE) {
            pageNum = 0;
        }

        UseState.forEach(UseState::reset);

        updateActiveButtons();
    }

    void giveItem(PartyPokemon p) {
        Game.getPlayer().getBag().giveItem(p, this.selectedItem);
        this.deactivateState(UseState.GIVE);
    }

    void useItem(PartyPokemon p) {
        if (!p.isEgg() && this.selectedItem.getItem() instanceof MoveUseItem) {
            this.selectedPokemon = p;
            this.state = BagState.MOVE_SELECT;
            this.buttons.setSelected(MOVES);
            this.updateActiveButtons();
        } else {
            Game.getPlayer().getBag().usePokemonItem(this.selectedItem, p);
            this.deactivateState(UseState.USE);
        }
    }

    private void usePlayerItem() {
        Game.getPlayer().getBag().usePlayerItem(this.selectedItem);
        this.deactivateState(UseState.USE);
    }

    private void useMoveItem(int index) {
        Move move = selectedPokemon.getActualMoves().get(index);
        Game.getPlayer().getBag().useMoveItem(selectedItem, selectedPokemon, move);
        this.deactivateState(UseState.USE);
    }

    void takeItem(PartyPokemon p) {
        Game.getPlayer().getBag().takeItem(p);
        this.deactivateState(UseState.TAKE);
    }

    private void deactivateState(UseState state) {
        state.reset();

        this.buttons.setSelected(state.buttonIndex);
        this.state = BagState.ITEM_SELECT;

        if (!Game.getPlayer().getBag().hasItem(this.selectedItem)) {
            this.updateCategory();
        }

        this.updateActiveButtons();
    }

    private void pressState(UseState useState) {
        // Switch the state (turns off other states as well)
        useState.switchClicked();

        if (useState.isClicked()) {
            // State is now selected  -- switch to Pokemon select to choose which pokemon to use the item with
            this.state = BagState.POKEMON_SELECT;
            this.buttons.setSelected(PARTY);
        } else {
            // No longer selected -- revert back to item select
            this.state = BagState.ITEM_SELECT;
        }

        // PlayerUseItems don't require selecting a Pokemon -- automatically use as soon as Use is pressed
        if (useState == UseState.USE && this.selectedItem.getItem() instanceof PlayerUseItem) {
            this.usePlayerItem();
        }

        this.updateActiveButtons();
    }

    @Override
    public void movedToFront() {
        // Set selected button to be the first tab and switch to first tab
        this.buttons.setSelected(0);
        this.changeCategory(0);
    }

    private int totalPages() {
        int size = Game.getPlayer().getBag().getCategory(selectedTab).size();
        return GeneralUtils.getTotalPages(size, ITEMS_PER_PAGE);
    }

    private void updateActiveButtons() {
        Player player = Game.getPlayer();

        List<PartyPokemon> team = player.getTeam();
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            partyButtons[i].setActive(state == BagState.POKEMON_SELECT && i < team.size());
        }

        int displayed = player.getBag().getCategory(selectedTab).size();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            itemButtons[i].setActive(state == BagState.ITEM_SELECT && i < displayed - pageNum*ITEMS_PER_PAGE);
        }

        for (int i = 0; i < MoveList.MAX_MOVES; i++) {
            moveButtons[i].setActive(state == BagState.MOVE_SELECT && i < selectedPokemon.getActualMoves().size());
        }

        leftArrow.setActive(state == BagState.ITEM_SELECT);
        rightArrow.setActive(state == BagState.ITEM_SELECT);

        if (selectedItem == ItemNamesies.NO_ITEM || !player.getBag().hasItem(selectedItem)) {
            selectedItem = ItemNamesies.NO_ITEM;
            buttons.get(GIVE).setActive(false);
            buttons.get(USE).setActive(false);
        } else {
            Item selectedItemValue = selectedItem.getItem();
            buttons.get(GIVE).setActive(selectedItemValue.isHoldable());
            buttons.get(USE).setActive(selectedItemValue instanceof BagUseItem);
        }
    }
}
