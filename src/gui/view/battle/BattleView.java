package gui.view.battle;

import battle.Battle;
import battle.attack.Move;
import draw.TextUtils;
import draw.button.Button;
import draw.button.panel.BasicPanels;
import draw.button.panel.DrawPanel;
import gui.view.View;
import gui.view.ViewMode;
import input.ControlKey;
import main.Game;
import main.Global;
import map.Direction;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pokemon.ActivePokemon;
import draw.DrawUtils;
import util.FontMetrics;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class BattleView extends View {
	private static final int BUTTON_WIDTH = 183;
	private static final int BUTTON_HEIGHT = 55;

	// The current battle in view, the current message being displayed, and the current selected button
	private Battle currentBattle;
	private String message;
	private int selectedButton;
	
	// The current state that the battle is in and current update type
	private VisualState state;
	private Update update;
	
	// Holds the animation for the player and the opponent
	private final PokemonAnimationState playerAnimation;
	private final PokemonAnimationState enemyAnimation;

	// Different panels to draw
	private final DrawPanel menuMessagePanel;
	private final DrawPanel buttonsPanel;
	private final DrawPanel largeMenuPanel;

	// All the different buttons!!
	private final Button backButton;

	// Which Pokemon is trying to learn a new move, and which move
	private ActivePokemon learnedPokemon;
	private Move learnedMove;
	
	public BattleView() {
		playerAnimation = new PokemonAnimationState(this, true);
		enemyAnimation = new PokemonAnimationState(this, false);

		menuMessagePanel = new DrawPanel(415, 440, 385, 161).withBorderColor(new Color(53, 53, 129));
		buttonsPanel = new DrawPanel(0, 440, 417, 161).withBorderColor(Color.GRAY).withBorderPercentage(5);
		largeMenuPanel = new DrawPanel(0, 160, 417, 440).withBorderPercentage(3).withBlackOutline();

		// Back Button
		backButton = new Button(750, 560, 35, 20);
	}

	public void drawLargeMenuPanel(Graphics g) {
		largeMenuPanel.drawBackground(g);
	}

	public void drawFullMessagePanel(Graphics g) {
		drawFullMessagePanel(g, message);
	}

	public void drawFullMessagePanel(Graphics g, String text) {
		BasicPanels.drawFullMessagePanel(g, text);
	}

	public void drawMenuMessagePanel(Graphics g, String text) {
		menuMessagePanel.drawBackground(g);
		menuMessagePanel.drawMessage(g, 30, text);
	}

	public void drawButtonsPanel(Graphics g) {
		buttonsPanel.drawBackground(g);
	}

	public Button[] createPanelButtons() {
		return createPanelButtons(buttonsPanel, 2, 2);
	}

	public Button[] createMessagePanelButtons(int numRows, int numCols) {
		return BasicPanels.getFullMessagePanelButtons(BUTTON_WIDTH, BUTTON_HEIGHT, numRows, numCols);
	}

	private Button[] createPanelButtons(DrawPanel buttonsPanel, int numRows, int numCols) {
		return buttonsPanel.getButtons(BUTTON_WIDTH, BUTTON_HEIGHT, numRows, numCols);
	}
	
	public void setBattle(Battle b) {
		currentBattle = b;
		selectedButton = 0;
		
		playerAnimation.resetBattle(b.getPlayer().front());
		enemyAnimation.resetBattle(b.getOpponent().front());

		learnedMove = null;
		learnedPokemon = null;
		
		setVisualState(VisualState.MESSAGE);
		update = Update.NO_UPDATE;
		state.reset();

		Game.getPlayer().clearLogMessages();
	}

	@Override
	public void update(int dt) {
		state.update(this);
		update.performUpdate(this);
	}
	
	public Battle getCurrentBattle() {
		return this.currentBattle;
	}

	public void setSwitchForced() {
		VisualState.setSwitchForced();
	}

	public void setSelectedButton(Button[] buttons) {
		int buttonIndex = Button.update(buttons, this.selectedButton);
		this.setSelectedButton(buttonIndex);
	}

	public void setSelectedButton(int buttonIndex) {
		this.selectedButton = buttonIndex;
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

	public Move getLearnedMove() {
		return this.learnedMove;
	}

	public ActivePokemon getLearnedPokemon() {
		return this.learnedPokemon;
	}

	public boolean isState(VisualState state) {
		return this.state == state;
	}

	public void drawBackButton(Graphics g) {
		drawBackButton(g, true);
	}

	public void drawBackButton(Graphics g, boolean drawArrows) {
		g.setColor(Color.BLACK);
		if (drawArrows) {
			backButton.drawArrow(g, Direction.RIGHT);
		}

		backButton.draw(g);
	}

	public void updateBackButton() {
		updateBackButton(true);
	}

	public void updateBackButton(boolean setToMainMenu) {
		backButton.update(false, ControlKey.BACK);

		// Return to main battle menu
		if (backButton.checkConsumePress() && setToMainMenu) {
			setVisualState(VisualState.MENU);
		}
	}

	public boolean isPlayingAnimation() {
		return playerAnimation.isAnimationPlaying() || enemyAnimation.isAnimationPlaying();
	}

	public void clearUpdate() {
		this.update = Update.NO_UPDATE;
	}

	// Just for updates and whatnot
	public void setVisualState() {
		setVisualState(this.state);
	}
	
	public void setVisualState(VisualState newState) {
		if (state != newState) {
			selectedButton = 0;
		}

		state = newState;
		
		// Update the buttons that should be active
		state.set(this);
	}

	public void cycleMessage(boolean updated) {
		if (!updated) {
			setVisualState(VisualState.MENU);
		}

		if (Messages.hasMessages()) {
			if (updated && !Messages.nextMessageEmpty()) {
				return;
			}
			
			MessageUpdate newMessage = Messages.getNextMessage();
			currentBattle.getPlayer().addLogMessage(newMessage);
			
			PokemonAnimationState state = newMessage.isPlayer() ? playerAnimation : enemyAnimation;
			state.checkMessage(newMessage);
			if (!newMessage.switchUpdate()) {
				if (newMessage.hasUpdateType()) {
					update = newMessage.getUpdateType();
				}

				if (newMessage.learnMove()) {
					learnedMove = newMessage.getMove();
					learnedPokemon = newMessage.getActivePokemon();
				}

				this.state.checkMessage(newMessage);
			}
			
			if (newMessage.getMessage().isEmpty()) {
				cycleMessage(updated);
			}
			else {
				message = newMessage.getMessage();
				setVisualState(VisualState.MESSAGE);
				cycleMessage(true);
			}
		}
		else if (!updated) {
			message = null;
		}
	}

	@Override
	public void draw(Graphics g) {
		DrawUtils.fillCanvas(g, Color.BLACK);
		
		ActivePokemon player = currentBattle.getPlayer().front();
		ActivePokemon opponent = currentBattle.getOpponent().front();
		 
		// Get background based on terrain type
		TerrainType terrainType = currentBattle.getTerrainType();
		g.drawImage(terrainType.getBackgroundImage(), 0, 0, null);

		// Player's battle circle
		g.drawImage(terrainType.getPlayerCircleImage(), 0, 331, null);
		
		// Opponent battle circle
		g.drawImage(terrainType.getOpponentCircleImage(), 450, 192, null);
		
		if (playerAnimation.isEmpty()) {
			if (enemyAnimation.isEmpty()) {
				g.setClip(0, 440, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			}
			else {
				g.setClip(0, 0, Global.GAME_SIZE.width, 250);
			}
		}
		else if (enemyAnimation.isEmpty()) {
			g.setClip(0, 250, Global.GAME_SIZE.width, 440);
		}
		
		// Draw Status Box Backgrounds
		playerAnimation.drawStatusBox(g, player);
		enemyAnimation.drawStatusBox(g, opponent);
		
		g.setClip(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		
		state.draw(this, g);
	}

	public void drawMoveButton(Graphics g, Button moveButton, Move move) {
		int dx = moveButton.x;
		int dy = moveButton.y;

		g.translate(dx, dy);

		new DrawPanel(0, 0, moveButton.width, moveButton.height)
				.withTransparentBackground(move.getAttack().getActualType().getColor())
				.withBorderPercentage(15)
				.withBlackOutline()
				.drawBackground(g);

		g.setColor(Color.BLACK);
		FontMetrics.setFont(g, 22);
		g.drawString(move.getAttack().getName(), 10, 26);

		FontMetrics.setFont(g, 18);
		TextUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

		BufferedImage categoryImage = move.getAttack().getCategory().getImage();
		g.drawImage(categoryImage, 12, 32, null);

		g.translate(-dx, -dy);

		moveButton.draw(g);
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.BATTLE_VIEW;
	}

	@Override
	public void movedToFront() {
		cycleMessage(false);
	}
}
