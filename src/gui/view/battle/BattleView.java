package gui.view.battle;

import battle.Battle;
import battle.attack.Move;
import gui.Button;
import gui.GameData;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
import item.ItemNamesies;
import item.bag.BattleBagCategory;
import main.Game;
import main.Global;
import map.TerrainType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pokemon.ActivePokemon;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.CharacterData;
import trainer.Trainer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

public class BattleView extends View {

	// Menu Button Indexes
	private static final int FIGHT_BUTTON = 0;
	private static final int BAG_BUTTON = 1;
	private static final int SWITCH_BUTTON = 2;
	private static final int RUN_BUTTON = 3;
	
	// Battle Bag Categories
	public static final BattleBagCategory[] BATTLE_BAG_CATEGORIES = BattleBagCategory.values();
	
	// Bag Button Indexes
	public static final int ITEMS = BATTLE_BAG_CATEGORIES.length;
	public static final int ITEMS_PER_PAGE = 10;
	private static final int NUM_BAG_BUTTONS = BATTLE_BAG_CATEGORIES.length + ITEMS_PER_PAGE + 3;
	private static final int LAST_ITEM_BUTTON = NUM_BAG_BUTTONS - 1;
	private static final int BAG_RIGHT_BUTTON = NUM_BAG_BUTTONS - 2;
	private static final int BAG_LEFT_BUTTON = NUM_BAG_BUTTONS - 3;

	public static final int LOG_LEFT_BUTTON = 0;
	public static final int LOG_RIGHT_BUTTON = 1;
	public static final int LOGS_PER_PAGE = 23;
	
	// Switch Button in Pokemon View Button Index
	private static final int POKEMON_SWITCH_BUTTON = Trainer.MAX_POKEMON;

	// Loss Constants <-- Super Meaningful Comment
	public static final int FRAMES_PER_HP_LOSS = 20;
	public static final float HP_LOSS_RATIO = 0.1f;
	public static final float EXP_LOSS_RATIO = 15f;
	
	// Evolution and Catch Lifespans
	public static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	public static final int CATCH_SHAKE_ANIMATION_LIFESPAN = 1000;
	public static final int CATCH_TRANSFORM_ANIMATION_LIFESPAN = 2000;
	public static final int CATCH_ANIMATION_LIFESPAN = CATCH_SHAKE_ANIMATION_LIFESPAN*CharacterData.CATCH_SHAKES + CATCH_TRANSFORM_ANIMATION_LIFESPAN;
	
	// Polygons for Type Colors in Status Box -- First array is for player, Second array is for the opponent
	public static final int[][] primaryColorx = { { 0, 199, 94, 0 }, { 0, 191, 104, 0 } };
	public static final int[][] primaryColory = { { 0, 0, 105, 105 }, { 0, 0, 88, 88 } };
	public static final int[][] secondaryColorx = { { 294, 199, 94, 294 }, { 191, 294, 294, 104 } };
	public static final int[][] secondaryColory = { { 0, 0, 105, 105 }, { 0, 0, 88, 88 } };
	
	// Polygons for Type Colors in the Pokemon View
	public static final int[] pkmnPrimaryColorx = { 0, 349, 5, 0 };
	public static final int[] pkmnPrimaryColory = { 0, 0, 344, 344 };
	public static final int[] pkmnSecondaryColorx = { 344, 349, 349, 0 };
	public static final int[] pkmnSecondaryColory = { 0, 0, 344, 344 };
	
	// The current battle in view, the current message being displayed, and the current selected button
	public Battle currentBattle;
	public String message;
	public int selectedButton;
	
	// The current state that the battle is in and current update type
	public VisualState state;
	private Update update;
	
	// Holds the animation for the player and the opponent
	public PokemonAnimationState playerAnimation, enemyAnimation;

	// All the different buttons!!
	public Button[] moveButtons, bagButtons, menuButtons, pokemonButtons;
	public Button[] bagTabButtons, pokemonTabButtons, logButtons;
	public Button fightBtn, bagBtn, pokemonBtn, runBtn, backButton;
	public Button bagRightButton, bagLeftButton, bagLastUsedBtn, pokemonSwitchButton;
	public Button yesButton, noButton, newMoveButton;
	public Button logLeftButton, logRightButton;
	
	// Current bag page, bag category, and selected item
	public int bagPage;
	public int selectedBagTab;
	public ItemNamesies selectedItem;
	
	// Current selected tab in Pokemon view and whether or not a switch is forced
	public int selectedPokemonTab;
	public boolean switchForced;

	public int logPage;
	public List<String> logMessages;

	public List<Move> selectedMoveList;
	
	// The last move that a Pokemon used
	public int lastMoveUsed;

	// TODO: move
	// Which Pokemon is trying to learn a new move, and which move
	public ActivePokemon learnedPokemon;
	public Move learnedMove;
	
	// Stat gains and corresponding new stat upgrades for leveling up/evolving
	public int[] statGains;
	public int[] newStats;
	
	public BattleView() {
		playerAnimation = new PokemonAnimationState(this);
		enemyAnimation = new PokemonAnimationState(this);
	}
	
	public void setBattle(Battle b) {
		currentBattle = b;
		selectedBagTab = 0;
		bagPage = 0;
		selectedPokemonTab = 0;
		selectedButton = 0;
		lastMoveUsed = 0;
		switchForced = false;
		
		playerAnimation.resetVals(b.getPlayer().front());
		enemyAnimation.resetVals(b.getOpponent().front());
		
		playerAnimation.state.imageNumber = 0;
		enemyAnimation.state.imageNumber = 0;
		
		learnedMove = null;
		learnedPokemon = null;
		
		setVisualState(VisualState.MESSAGE);
		update = Update.NO_UPDATE;
		
		// Back Button
		backButton = new Button(750, 560, 35, 20, null);
		
		// Menu Buttons
		menuButtons = new Button[4]; // TODO: ugly equals
		menuButtons[FIGHT_BUTTON] = fightBtn = new Button(
				452,
				473,
				609 - 452,
				515 - 473,
				Button.HoverAction.ARROW,
				new int[] {	BAG_BUTTON, SWITCH_BUTTON, RUN_BUTTON, SWITCH_BUTTON}
		);
		menuButtons[BAG_BUTTON] = bagBtn = new Button(
				628,
				473,
				724 - 628,
				513 - 473,
				Button.HoverAction.ARROW,
				new int[] { SWITCH_BUTTON, RUN_BUTTON, FIGHT_BUTTON, RUN_BUTTON }
		);
		menuButtons[SWITCH_BUTTON] = pokemonBtn = new Button(
				452,
				525,
				609 - 452,
				571 - 525,
				Button.HoverAction.ARROW,
				new int[] { RUN_BUTTON, FIGHT_BUTTON, BAG_BUTTON, FIGHT_BUTTON }
		);
		menuButtons[RUN_BUTTON] = runBtn = new Button(
				628,
				525,
				724 - 628,
				571 - 525,
				Button.HoverAction.ARROW,
				new int[] { FIGHT_BUTTON, BAG_BUTTON, SWITCH_BUTTON, BAG_BUTTON }
		);
		
		// Move Buttons
		moveButtons = new Button[Move.MAX_MOVES];
		for (int y = 0, i = 0; y < 2; y++) {
			for (int x = 0; x < Move.MAX_MOVES/2; x++, i++) {
				moveButtons[i] = new Button(
						22 + x*190,
						440 + 21 + y*62,
						183,
						55,
						Button.HoverAction.BOX,
						new int[] { (i + 1)%Move.MAX_MOVES, // Right
									((i - Move.MAX_MOVES/2) + Move.MAX_MOVES)%Move.MAX_MOVES, // Up
									((i - 1) + Move.MAX_MOVES)%Move.MAX_MOVES, // Left
									(i + Move.MAX_MOVES/2)%Move.MAX_MOVES }); // Down
			}
		}
		
		// Learn Move Buttons
		yesButton = new Button(moveButtons[2].x, moveButtons[2].y, moveButtons[2].width, moveButtons[2].height, Button.HoverAction.BOX);
		noButton = new Button(moveButtons[3].x, moveButtons[3].y, moveButtons[3].width, moveButtons[3].height, Button.HoverAction.BOX);
		newMoveButton = new Button(moveButtons[3].x + moveButtons[3].width + moveButtons[2].x, moveButtons[3].y, moveButtons[3].width, moveButtons[3].height, Button.HoverAction.BOX);
		
		// Bag View Buttons 
		bagButtons = new Button[NUM_BAG_BUTTONS];
		
		bagTabButtons = new Button[BATTLE_BAG_CATEGORIES.length];
		for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
			bagButtons[i] = bagTabButtons[i] = new Button(
					i*89 + 30,
					190,
					89,
					28,
					Button.HoverAction.BOX,
					new int[] { (i + 1)% BATTLE_BAG_CATEGORIES.length, // Right
								LAST_ITEM_BUTTON, // Up
								(i - 1 + BATTLE_BAG_CATEGORIES.length)% BATTLE_BAG_CATEGORIES.length, // Left
								ITEMS }  // Down
			);
		}
		
		bagButtons[BAG_LEFT_BUTTON] = bagLeftButton = new Button(135, 435, 35, 20, Button.HoverAction.BOX, new int[] {BAG_RIGHT_BUTTON, ITEMS + ITEMS_PER_PAGE - 2, -1, LAST_ITEM_BUTTON});
		bagButtons[BAG_RIGHT_BUTTON] = bagRightButton = new Button(250,435,35,20, Button.HoverAction.BOX, new int[] {-1, ITEMS + ITEMS_PER_PAGE - 1, BAG_LEFT_BUTTON, LAST_ITEM_BUTTON});
		bagButtons[LAST_ITEM_BUTTON] = bagLastUsedBtn = new Button(214, 517, 148, 28, Button.HoverAction.BOX, new int[] {-1, BAG_LEFT_BUTTON, -1, selectedBagTab});
		
		for (int y = 0, i = ITEMS; y < ITEMS_PER_PAGE/2; y++) {
			for (int x = 0; x < 2; x++, i++) {
				bagButtons[i] = new Button(
						55 + x*162,
						243 + y*38,
						148,
						28,
						Button.HoverAction.BOX,
						new int[] { (i + 1 - ITEMS)%ITEMS_PER_PAGE + ITEMS, // Right
									y == 0 ? selectedBagTab : i - 2, // Up
									(i - 1 - ITEMS + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS, // Left
									y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + 2 }
				); // Down
			}
		}
		
		// Pokemon Switch View Buttons
		pokemonButtons = new Button[Trainer.MAX_POKEMON + 1];
		
		pokemonTabButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
			pokemonButtons[i] = pokemonTabButtons[i] = new Button(32 + i*59, 192, 59, 34, Button.HoverAction.BOX, 
					new int[] { (i + 1)%Trainer.MAX_POKEMON, // Right
								POKEMON_SWITCH_BUTTON, // Up
								(i - 1 + Trainer.MAX_POKEMON)%Trainer.MAX_POKEMON, // Left
								POKEMON_SWITCH_BUTTON }); // Down
		}
		
		pokemonButtons[POKEMON_SWITCH_BUTTON] = pokemonSwitchButton = new Button(55, 509, 141, 36, Button.HoverAction.BOX, new int[] { -1, 0, -1, -1 });
	
		logLeftButton = new Button(150, 550, 35, 20, Button.HoverAction.BOX, new int[] { LOG_RIGHT_BUTTON, -1, -1, -1 });
		logRightButton = new Button(200, 550, 35, 20, Button.HoverAction.BOX, new int[] { -1, -1, LOG_LEFT_BUTTON, -1 });
		logButtons = new Button[] { logLeftButton, logRightButton };

		currentBattle.getPlayer().clearLogMessages();
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
		this.switchForced = true;
	}
	
	public void clearUpdate() {
		this.update = Update.NO_UPDATE;
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
			
			PokemonAnimationState state = newMessage.target() ? playerAnimation : enemyAnimation;
			if (newMessage.switchUpdate()) {
				state.resetVals(newMessage.getHP(), newMessage.getStatus(), newMessage.getType(), newMessage.getShiny(), 
						newMessage.getPokemon(), newMessage.getName(), newMessage.getMaxHP(), newMessage.getLevel(), 
						newMessage.getGender(), newMessage.getEXPRatio());
			}
			else {
				// TODO: Fuck this I hate this
				if (newMessage.healthUpdate()) {
					state.startHpAnimation(newMessage.getHP());
				}

				if (newMessage.maxHealthUpdate()) {
					state.setMaxHP(newMessage.getMaxHP());
				}

				if (newMessage.statusUpdate()) {
					state.setStatus(newMessage.getStatus());
				}

				if (newMessage.typeUpdate()) {
					state.setType(newMessage.getType());
				}

				if (newMessage.catchUpdate()) {
					state.startCatchAnimation(newMessage.getDuration() == -1? -1 : newMessage.getDuration());
				}

				if (newMessage.pokemonUpdate()) {
					state.startPokemonUpdateAnimation(newMessage.getPokemon(), newMessage.getShiny(), newMessage.isAnimate());
				}

				if (newMessage.hasUpdateType()) {
					update = newMessage.getUpdateType();
				}

				if (newMessage.expUpdate()) {
					state.startExpAnimation(newMessage.getEXPRatio(), newMessage.levelUpdate());
				}

				if (newMessage.levelUpdate()) {
					SoundPlayer.soundPlayer.playSoundEffect(SoundTitle.LEVEL_UP);
					state.setLevel(newMessage.getLevel());
				}

				if (newMessage.nameUpdate()) {
					state.setName(newMessage.getName());
				}

				if (newMessage.genderUpdate()) {
					state.setGender(newMessage.getGender());
				}

				if (newMessage.learnMove()) {
					learnedMove = newMessage.getMove();
					learnedPokemon = newMessage.getActivePokemon();
				}

				if (newMessage.gainUpdate()) {
					newStats = newMessage.getNewStats();
					statGains = newMessage.getGain();
				}
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
		Dimension d = Global.GAME_SIZE;
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, d.width, d.height);
		
		ActivePokemon plyr = currentBattle.getPlayer().front();
		ActivePokemon enmy = currentBattle.getOpponent().front();

		GameData data = Game.getData();
		TileSet tiles = data.getBattleTiles();
		 
		// Get background based on terrain type
		TerrainType terrainType = currentBattle.getTerrainType();
		g.drawImage(tiles.getTile(terrainType.getBackgroundIndex()), 0, 0, null);

		// Player's battle circle
		g.drawImage(tiles.getTile(terrainType.getPlayerCircleIndex()), 0, 331, null);
		
		// Opponent battle circle
		g.drawImage(tiles.getTile(terrainType.getOpponentCircleIndex()), 450, 192, null);
		
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
		g.translate(463,  304);
		playerAnimation.drawStatusBox(g, 0, plyr, data.getPokemonTilesLarge(), 190 - 463, 412 - 304);
		g.translate(-463, -304);

		g.translate(42, 52);
		enemyAnimation.drawStatusBox(g, 1, enmy, data.getPokemonTilesMedium(), 565, 185);
		g.translate(-42, -52);

		// Draw Status Box Foregrounds
		g.drawImage(tiles.getTile(1), 0, 0, null);
		
		// Draw Status Box Text
		g.translate(463,  304);
		playerAnimation.drawStatusBoxText(g, 0, tiles, plyr);
		g.translate(-463, -304);
		
		g.translate(42,  52);
		enemyAnimation.drawStatusBoxText(g, 1, tiles, enmy);
		g.translate(-42, -52);
		
		g.setClip(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		
		state.draw(this, g, tiles);
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
