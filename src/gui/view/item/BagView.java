package gui.view.item;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.Alignment;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.layout.ButtonLayout.ButtonIndexAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
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
import map.Direction;
import message.MessageUpdate;
import message.Messages;
import message.Messages.MessageState;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import trainer.Trainer;
import trainer.player.Player;
import util.FontMetrics;
import util.GeneralUtils;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

public class BagView extends View {
    private static final BagCategory[] CATEGORIES = BagCategory.values();
    private static final int ITEMS_PER_PAGE = 10;

    private static final int NUM_BUTTONS = CATEGORIES.length + Trainer.MAX_POKEMON + ITEMS_PER_PAGE
            + MoveList.MAX_MOVES + UseState.values().length + 3;
    private static final int TABS = 0;
    private static final int PARTY = TABS + CATEGORIES.length;
    private static final int ITEMS = PARTY + Trainer.MAX_POKEMON;
    private static final int MOVES = ITEMS + ITEMS_PER_PAGE;
    private static final int USE_STATES = MOVES + MoveList.MAX_MOVES;
    private static final int RIGHT_USE_STATE = USE_STATES + UseState.values().length - 1;
    private static final int USE = USE_STATES + UseState.USE.ordinal();
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 2;
    private static final int LEFT_ARROW = NUM_BUTTONS - 3;

    private final BagLayout layout;
    private final PanelList panels;

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] partyButtons;
    private final Button[] moveButtons;
    private final Button[] itemButtons;
    private final Button giveButton;
    private final Button useButton;
    private final Button rightArrow;
    private final Button leftArrow;

    private Bag bag;
    private List<PartyPokemon> team;
    private TileSet partyTiles;

    private State state;
    private ItemNamesies selectedItem;
    private PartyPokemon selectedPokemon;

    private int pageNum;
    private BagCategory selectedTab;
    private MessageUpdate message;

    public BagView() {
        selectedTab = CATEGORIES[0];
        selectedItem = ItemNamesies.NO_ITEM;

        // Show quantities
        layout = new BagLayout(true);

        this.buttons = new ButtonList(NUM_BUTTONS);

        Button returnButton = layout.createReturnButton(
                new ButtonTransitions().right(PARTY).up(RIGHT_ARROW).left(PARTY).down(TABS),
                this::returnToMap
        );

        tabButtons = layout.getTabButtons(TABS, RETURN, USE, this::changeCategory);

        // Party buttons don't skip when inactive since they're inactive by default
        // (Only active when PokemonUseItem is selected)
        partyButtons = getLeftLayout(
                PARTY,
                new ButtonTransitions().right(USE_STATES).up(TABS).left(MOVES).down(TABS),
                index -> UseState.applyPokemon(this, Game.getPlayer().getTeam().get(index))
        ).getButtons();

        // Move buttons are fine to skip when inactive since they're only visible when active
        moveButtons = getLeftLayout(
                MOVES,
                new ButtonTransitions().right(PARTY).up(TABS).left(RIGHT_USE_STATE).down(TABS),
                this::useMoveItem
        ).withButtonSetup(ButtonPanel::skipInactive).getButtons();

        itemButtons = layout.getItemButtons(
                ITEMS,
                new ButtonTransitions().up(USE).down(RIGHT_ARROW),
                index -> selectedItem = GeneralUtils.getPageValue(this.getDisplayItems(), pageNum, ITEMS_PER_PAGE, index)
        );

        UseState[] useStates = UseState.values();
        Button[] useButtons = layout.getSelectedButtonLayout(UseState.values().length)
                                    .withStartIndex(USE_STATES)
                                    .withDefaultTransitions(new ButtonTransitions().right(MOVES)
                                                                                   .up(TABS)
                                                                                   .left(PARTY)
                                                                                   .down(ITEMS))
                                    .withPressIndex(index -> pressState(useStates[index]))
                                    .withButtonSetup((panel, index) -> panel.greyInactive()
                                                                            .withLabel(useStates[index].displayName, 20))
                                    .getTabs();
        giveButton = useButtons[UseState.GIVE.ordinal()];
        useButton = useButtons[UseState.USE.ordinal()];

        leftArrow = new Button(
                layout.leftArrow,
                new ButtonTransitions().right(RIGHT_ARROW).up(ITEMS + ITEMS_PER_PAGE - 2).left(RIGHT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        rightArrow = new Button(
                layout.rightArrow,
                new ButtonTransitions().right(LEFT_ARROW).up(ITEMS + ITEMS_PER_PAGE - 1).left(LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        buttons.set(TABS, tabButtons);
        buttons.set(PARTY, partyButtons);
        buttons.set(MOVES, moveButtons);
        buttons.set(ITEMS, itemButtons);
        buttons.set(USE_STATES, useButtons);
        buttons.set(LEFT_ARROW, leftArrow);
        buttons.set(RIGHT_ARROW, rightArrow);
        buttons.set(RETURN, returnButton);

        panels = new PanelList(layout.bagPanel, layout.leftPanel, layout.selectedPanel, layout.itemsPanel);

        movedToFront();
    }

    private ButtonLayout getLeftLayout(int startIndex, ButtonTransitions defaultTransitions, ButtonIndexAction indexAction) {
        return new ButtonLayout(layout.leftPanel, Trainer.MAX_POKEMON, 1, 10)
                .withStartIndex(startIndex)
                .withDefaultTransitions(defaultTransitions)
                .withPressIndex(indexAction)
                .withDrawSetup(panel -> panel.withTransparentCount(2)
                                             .withBorderPercentage(15)
                                             .withBlackOutline());
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();

        if (message != null && input.consumeIfMouseDown(ControlKey.SPACE)) {
            message = null;
        }

        while ((message == null || StringUtils.isNullOrWhiteSpace(message.getMessage()))
                && Messages.hasMessages()) {
            message = Messages.getNextMessage();
            if (message.isViewChange()) {
                Game.instance().setViewMode(message.getViewMode());
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

    public void returnToMap() {
        Messages.clearMessages(MessageState.BAGGIN_IT_UP);
        Messages.setMessageState(MessageState.MAPPITY_MAP);
        Game.instance().setViewMode(ViewMode.MAP_VIEW);
    }

    @Override
    public void draw(Graphics g) {
        // Setup Use State buttons
        UseState.forEach(useState -> {
            Button useButton = buttons.get(USE_STATES + useState.ordinal());
            useState.setup(useButton.panel(), selectedTab.getColor());
        });

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Draw selected item
        layout.drawSelectedItem(g, selectedItem);

        // Draw page numbers
        layout.drawPageNumbers(g, pageNum, totalPages());

        if (state == State.MOVE_SELECT) {
            // Draw moves
            drawMoves(g);
        } else {
            // Draw Pokemon Info
            drawPokemonInfo(g);
        }

        // Messages or buttons
        if (message != null && !StringUtils.isNullOrWhiteSpace(message.getMessage())) {
            BasicPanels.drawFullMessagePanel(g, message.getMessage());
        } else {
            buttons.drawHover(g);
        }
    }

    // Should only be called when in the MoveSelect state
    private void drawMoves(Graphics g) {
        MoveList moveList = selectedPokemon.getActualMoves();

        for (int i = 0; i < moveList.size(); i++) {
            Move move = moveList.get(i);
            Attack attack = move.getAttack();
            ButtonPanel panel = moveButtons[i].panel();

            BufferedImage typeImage = attack.getActualType().getImage();
            BufferedImage categoryImage = attack.getCategory().getImage();

            // Spacing is one space of the name font size
            FontMetrics.setBlackFont(g, 18);
            int spacing = FontMetrics.getTextWidth(g);
            int fullSpacing = spacing + panel.getBorderSize();

            // Name is one space from border, images are one space from right border, PP is one space from images
            int startX = panel.x + fullSpacing;
            int rightX = panel.rightX() - fullSpacing;
            int ppRightX = rightX - typeImage.getWidth() - spacing;
            int centerY = panel.centerY();

            // Half of the image height spaced in between the two images
            int imageSpacing = typeImage.getHeight()/2;
            int topImageY = centerY - imageSpacing/2 - typeImage.getHeight();
            int bottomImageY = centerY + imageSpacing/2;

            TextUtils.drawCenteredHeightString(g, attack.getName(), startX, centerY);

            FontMetrics.setBlackFont(g, 14);
            TextUtils.drawCenteredHeightString(g, "PP: " + move.getPPString(), ppRightX, centerY, Alignment.RIGHT);

            ImageUtils.drawRightAlignedImage(g, typeImage, rightX, topImageY);
            ImageUtils.drawRightAlignedImage(g, categoryImage, rightX, bottomImageY);
        }
    }

    private void drawPokemonInfo(Graphics g) {
        for (int i = 0; i < team.size(); i++) {
            PartyPokemon p = team.get(i);
            Button pokemonButton = partyButtons[i];

            g.translate(pokemonButton.x, pokemonButton.y);

            DrawPanel pokemonPanel = new DrawPanel(0, 0, pokemonButton.width, pokemonButton.height);

            BufferedImage img = partyTiles.getTile(p.getTinyImageName());
            ImageUtils.drawCenteredImage(g, img, 30, pokemonPanel.centerY());

            FontMetrics.setBlackFont(g, 14);

            // Name and Gender
            g.drawString(p.getNameAndGender(), 50, 22);

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

                    FontMetrics.setBlackFont(g, 12);

                    g.drawString(p.getActualHeldItem().getName(), 50, 47);
                    TextUtils.drawRightAlignedString(g, p.getHpString(), 293, 47);
                }

                // Taint the whole button with faint if they dead
                pokemonPanel.faintOut(g, p);
            }

            g.translate(-pokemonButton.x, -pokemonButton.y);
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
        state = State.ITEM_SELECT;

        Set<ItemNamesies> list = this.getDisplayItems();
        selectedItem = list.isEmpty() ? ItemNamesies.NO_ITEM : list.iterator().next();

        // No more items on the current page
        if (list.size() < (pageNum + 1)*ITEMS_PER_PAGE) {
            pageNum = 0;
        }

        // Set background color and tab outlines
        layout.setupTabs(tabButtons, selectedTab);

        UseState.forEach(UseState::reset);
        updateActiveButtons();
    }

    void giveItem(PartyPokemon p) {
        bag.giveItem(p, this.selectedItem);
        this.deactivateState(UseState.GIVE);
    }

    void useItem(PartyPokemon p) {
        if (!p.isEgg() && this.selectedItem.getItem() instanceof MoveUseItem) {
            this.selectedPokemon = p;
            this.state = State.MOVE_SELECT;
            this.buttons.setSelected(MOVES);
            this.updateActiveButtons();
        } else {
            bag.usePokemonItem(this.selectedItem, p);
            this.deactivateState(UseState.USE);
        }
    }

    private void usePlayerItem() {
        bag.usePlayerItem(this.selectedItem);
        this.deactivateState(UseState.USE);
    }

    private void useMoveItem(int index) {
        Move move = selectedPokemon.getActualMoves().get(index);
        bag.useMoveItem(selectedItem, selectedPokemon, move);
        this.deactivateState(UseState.USE);
    }

    void takeItem(PartyPokemon p) {
        bag.takeItem(p);
        this.deactivateState(UseState.TAKE);
    }

    private void deactivateState(UseState state) {
        state.reset();

        this.buttons.setSelected(USE_STATES + state.ordinal());
        this.state = State.ITEM_SELECT;

        if (!bag.hasItem(this.selectedItem)) {
            this.updateCategory();
        }

        this.updateActiveButtons();
    }

    private void pressState(UseState useState) {
        // Switch the state (turns off other states as well)
        useState.switchClicked();

        if (useState.isClicked()) {
            // State is now selected  -- switch to Pokemon select to choose which pokemon to use the item with
            this.state = State.POKEMON_SELECT;
            this.buttons.setSelected(PARTY);
        } else {
            // No longer selected -- revert back to item select
            this.state = State.ITEM_SELECT;
        }

        // PlayerUseItems don't require selecting a Pokemon -- automatically use as soon as Use is pressed
        if (useState == UseState.USE && this.selectedItem.getItem() instanceof PlayerUseItem) {
            this.usePlayerItem();
        }

        this.updateActiveButtons();
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        GameData data = Game.getData();

        bag = player.getBag();
        team = player.getTeam();
        partyTiles = data.getPartyTiles();

        pageNum = 0;

        // Set selected button to be the first tab and switch to first tab
        this.buttons.setSelected(TABS);
        this.changeCategory(0);
    }

    private Set<ItemNamesies> getDisplayItems() {
        return bag.getCategory(selectedTab);
    }

    private int totalPages() {
        return GeneralUtils.getTotalPages(this.getDisplayItems().size(), ITEMS_PER_PAGE);
    }

    private void updateActiveButtons() {
        // Only active during pokemon state, but draw for all states other than move
        for (int i = 0; i < partyButtons.length; i++) {
            Button button = partyButtons[i];
            ButtonPanel panel = button.panel();
            button.setActive(state == State.POKEMON_SELECT && i < team.size());
            panel.setSkip(state == State.MOVE_SELECT || i >= team.size());
            if (!panel.isSkipping()) {
                panel.withTypeColors(team.get(i));
            }
        }

        // Set active items from layout and change active to only item select (will still draw for other states though)
        layout.setupItems(itemButtons, this.getDisplayItems(), pageNum);
        for (Button itemButton : itemButtons) {
            itemButton.setActive(state == State.ITEM_SELECT && itemButton.isActive());
        }

        // Move buttons skip draw when inactive and are only active during move select
        MoveList moves = state == State.MOVE_SELECT ? selectedPokemon.getActualMoves() : null;
        for (int i = 0; i < moveButtons.length; i++) {
            // Active when state is move select and move index is in range
            boolean active = moves != null && i < moves.size();
            Button button = moveButtons[i];
            button.setActive(active);

            // Set attack type background color if active
            if (active) {
                Attack attack = moves.get(i).getAttack();
                button.panel().withBackgroundColor(attack.getActualType().getColor());
            }
        }

        // Can only use arrows during arrow select (but always draw)
        leftArrow.setActive(state == State.ITEM_SELECT);
        rightArrow.setActive(leftArrow.isActive());

        // Can't give or use what you don't have!
        if (selectedItem == ItemNamesies.NO_ITEM || !bag.hasItem(selectedItem)) {
            selectedItem = ItemNamesies.NO_ITEM;
            giveButton.setActive(false);
            useButton.setActive(false);
        } else {
            Item selectedItemValue = selectedItem.getItem();
            giveButton.setActive(selectedItemValue.isHoldable());
            useButton.setActive(selectedItemValue instanceof BagUseItem);
        }
    }

    private enum State {
        ITEM_SELECT,
        POKEMON_SELECT,
        MOVE_SELECT,
    }
}
