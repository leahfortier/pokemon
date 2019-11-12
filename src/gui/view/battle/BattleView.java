package gui.view.battle;

import battle.Battle;
import battle.effect.battle.weather.WeatherEffect;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.button.Button;
import draw.layout.ButtonLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.Panel;
import draw.panel.WrapPanel;
import gui.view.View;
import gui.view.ViewMode;
import input.ControlKey;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.Direction;
import map.daynight.DayCycle;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.active.PartyPokemon;
import trainer.TrainerAction;
import trainer.player.Player;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class BattleView extends View {
    private static final int BUTTON_WIDTH = 183;
    private static final int BUTTON_HEIGHT = 55;

    // Holds the animation for the player and the opponent
    private final PokemonAnimationState playerAnimation;
    private final PokemonAnimationState enemyAnimation;

    // Different panels to draw
    private final WrapPanel menuMessagePanel;
    private final DrawPanel buttonsPanel;
    private final DrawPanel largeMenuPanel;

    // All the different buttons!!
    private final Button backButton;

    // The current battle in view and the current state the battle is in
    private Battle currentBattle;
    private VisualState state;

    // The current message being displayed and current update type
    private String message;
    private MessageUpdateType updateType;

    // Displayable current weather and terrain
    private WeatherEffect weather;
    private TerrainType terrain;
    private Boolean isInterior;

    public BattleView() {
        playerAnimation = new PokemonAnimationState(this, true);
        enemyAnimation = new PokemonAnimationState(this, false);

        menuMessagePanel = new WrapPanel(415, 440, 385, 161, 30)
                .withBorderColor(new Color(53, 53, 129))
                .withMinFontSize(25, false);
        buttonsPanel = new DrawPanel(0, 440, 417, 161).withBorderColor(Color.GRAY).withBorderPercentage(5);
        largeMenuPanel = new DrawPanel(0, 160, 417, 440).withBorderPercentage(3).withBlackOutline();

        // Back Button
        backButton = new Button(750, 560, 35, 20).asArrow(Direction.RIGHT);
    }

    public void drawLargeMenuPanel(Graphics g) {
        largeMenuPanel.drawBackground(g);
    }

    public void drawFullMessagePanel(Graphics g) {
        BasicPanels.drawFullMessagePanel(g, this.message);
    }

    public void drawMenuMessagePanel(Graphics g, String text) {
        menuMessagePanel.drawBackground(g);
        menuMessagePanel.drawMessage(g, text);
    }

    public void drawButtonsPanel(Graphics g) {
        buttonsPanel.drawBackground(g);
    }

    public Panel getMenuPanelSizing() {
        return menuMessagePanel.sizing();
    }

    public Panel getLargePanelSizing() {
        return largeMenuPanel.sizing();
    }

    public int getLargePanelBorderSize() {
        return largeMenuPanel.getBorderSize();
    }

    public ButtonLayout createPanelLayout(int numOptions) {
        return new ButtonLayout(buttonsPanel, 2, numOptions/2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void setBattle(Battle b) {
        currentBattle = b;

        playerAnimation.resetBattle(b.getPlayer().front());
        enemyAnimation.resetBattle(b.getOpponent().front());

        // Reset each state
        for (VisualState state : VisualState.values()) {
            this.state = state;
            state.handler().setBattle(this);
        }

        setVisualState(VisualState.MESSAGE);
        updateType = MessageUpdateType.NO_UPDATE;
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        state.handler().update();
        updateType.performUpdate(this);
    }

    public Battle getCurrentBattle() {
        return this.currentBattle;
    }

    public void setSwitchForced() {
        VisualState.setSwitchForced();
    }

    public boolean hasMessage() {
        return !StringUtils.isNullOrEmpty(message);
    }

    public String getMessage(VisualState messageState, String defaultMessage) {
        if (this.isState(messageState) && hasMessage()) {
            return message;
        }

        return defaultMessage;
    }

    public boolean isState(VisualState state) {
        return this.state == state;
    }

    public void drawBackButton(Graphics g) {
        backButton.drawPanel(g);
        backButton.drawHover(g);
    }

    public void updateBackButton() {
        backButton.update(false, ControlKey.BACK);

        // Return to main battle menu
        if (backButton.checkConsumePress()) {
            setVisualState(VisualState.MENU);
        }
    }

    public boolean isPlayingAnimation() {
        return playerAnimation.isAnimationPlaying() || enemyAnimation.isAnimationPlaying();
    }

    public void clearUpdate() {
        this.updateType = MessageUpdateType.NO_UPDATE;
    }

    // Just for updates and whatnot
    public void setVisualState() {
        setVisualState(this.state);
    }

    public void setVisualState(VisualState newState) {
        if (!this.isState(newState)) {
            state = newState;
            state.handler().movedToFront();
        } else {
            // Update the buttons that should be active
            state.handler().set();
        }
    }

    public void cycleMessage() {
        this.cycleMessage(false);
    }

    private void cycleMessage(boolean updated) {
        if (!updated) {
            setVisualState(VisualState.MENU);
        }

        if (Messages.hasMessages()) {
            if (updated && !Messages.nextMessageEmpty()) {
                return;
            }

            MessageUpdate newMessage = Messages.getNextMessage();
            this.checkMessage(newMessage);

            if (newMessage.getMessage().isEmpty()) {
                cycleMessage(updated);
            } else {
                message = newMessage.getMessage();
                setVisualState(VisualState.MESSAGE);
                cycleMessage(true);
            }
        } else if (!updated) {
            message = null;
        }
    }

    // Called after receiving a new message -- sets appropriate update things and such
    private void checkMessage(MessageUpdate newMessage) {
        // Every state deserves a peek! (Not just the current one)
        for (VisualState state : VisualState.values()) {
            state.handler().checkMessage(newMessage);
        }

        // Animation state is also curious
        PokemonAnimationState animationState = newMessage.isPlayer() ? playerAnimation : enemyAnimation;
        animationState.checkMessage(newMessage);

        if (newMessage.hasUpdateType()) {
            updateType = newMessage.getUpdateType();
        }

        if (newMessage.weatherUpdate()) {
            weather = newMessage.getWeather();
        }

        if (newMessage.terrainUpdate()) {
            terrain = newMessage.getTerrain();
            if (isInterior != null) {
                isInterior = terrain.isInterior();
            }
        }
    }

    // Executes a move which has already been set and checked for validness and such
    public void executeMove() {
        Game.getPlayer().performAction(currentBattle, TrainerAction.FIGHT);
        this.setVisualState(VisualState.MESSAGE);
        this.cycleMessage();
    }

    // Uses the item on the selected Pokemon (already checked for UseItem at this point)
    // Item may still fail its use and will reset back to the bag state in this case
    public void useItem(ItemNamesies item, PartyPokemon selected) {
        Player player = Game.getPlayer();

        // Try using the item on the selected Pokemon
        if (player.getBag().battleUseItem(item, selected, currentBattle)) {
            player.performAction(currentBattle, TrainerAction.ITEM);
            this.setVisualState(VisualState.MENU);
            this.cycleMessage();
        } else {
            // Item could not be used, reset to bag state
            this.cycleMessage();
            this.setVisualState(VisualState.INVALID_BAG);
        }
    }

    @Override
    public void draw(Graphics g) {
        // Get background based on terrain type
        DrawUtils.fillCanvas(g, terrain.getColor());

        // Player's battle circle
        g.drawImage(terrain.getPlayerCircleImage(), 0, 351, null);

        // Opponent battle circle
        g.drawImage(terrain.getOpponentCircleImage(), 450, 192, null);

        if (isInterior != null && !isInterior) {
            DayCycle.getTimeOfDay().draw(g);
        }

        BufferedImage image = Game.getData().getWeatherTiles().getTile(weather.getImageName());
        ImageUtils.drawCenteredImage(g, image, Global.GAME_SIZE.width/2, image.getHeight());

        if (playerAnimation.isEmpty()) {
            if (enemyAnimation.isEmpty()) {
                g.setClip(0, 440, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
            } else {
                g.setClip(0, 0, Global.GAME_SIZE.width, 250);
            }
        } else if (enemyAnimation.isEmpty()) {
            g.setClip(0, 250, Global.GAME_SIZE.width, 440);
        }

        // Draw Status Box Backgrounds
        playerAnimation.drawStatusBox(g);
        enemyAnimation.drawStatusBox(g);

        g.setClip(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);

        state.handler().draw(g);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.BATTLE_VIEW;
    }

    @Override
    public void movedToFront() {
        this.cycleMessage();
    }
}
