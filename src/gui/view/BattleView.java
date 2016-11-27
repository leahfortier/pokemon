package gui.view;

import battle.Battle;
import battle.attack.Move;
import battle.effect.status.StatusCondition;
import gui.Button;
import gui.GameData;
import gui.TileSet;
import item.ItemNamesies;
import item.bag.Bag;
import item.bag.BattleBagCategory;
import item.use.PokemonUseItem;
import main.Game;
import main.Global;
import main.Type;
import map.TerrainType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.CharacterData;
import trainer.Trainer;
import trainer.Trainer.Action;
import util.DrawUtils;
import input.InputControl;
import input.ControlKey;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BattleView extends View {

	// Menu Button Indexes
	private static final int FIGHT_BUTTON = 0;
	private static final int BAG_BUTTON = 1;
	private static final int SWITCH_BUTTON = 2;
	private static final int RUN_BUTTON = 3;
	
	// Battle Bag Categories
	private static final BattleBagCategory[] BATTLE_BAG_CATEGORIES = BattleBagCategory.values();
	
	// Bag Button Indexes
	private static final int ITEMS = BATTLE_BAG_CATEGORIES.length;
	private static final int ITEMS_PER_PAGE = 10;
	private static final int NUM_BAG_BUTTONS = BATTLE_BAG_CATEGORIES.length + ITEMS_PER_PAGE + 3;
	private static final int LAST_ITEM_BUTTON = NUM_BAG_BUTTONS - 1;
	private static final int BAG_RIGHT_BUTTON = NUM_BAG_BUTTONS - 2;
	private static final int BAG_LEFT_BUTTON = NUM_BAG_BUTTONS - 3;
	
	private static final int LOG_LEFT_BUTTON = 0;
	private static final int LOG_RIGHT_BUTTON = 1;
	private static final int LOGS_PER_PAGE = 23;
	
	// Switch Button in Pokemon View Button Index
	private static final int POKEMON_SWITCH_BUTTON = Trainer.MAX_POKEMON;
	
	// Loss Constants <-- Super Meaningful Comment
	private static final int FRAMES_PER_HP_LOSS = 20;
	private static final float HP_LOSS_RATIO = 0.1f;
	private static final float EXP_LOSS_RATIO = 15f;
	
	// Evolution and Catch Lifespans
	private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	private static final int CATCH_SHAKE_ANIMATION_LIFESPAN = 1000;
	private static final int CATCH_TRANSFORM_ANIMATION_LIFESPAN = 2000;
	private static final int CATCH_ANIMATION_LIFESPAN = CATCH_SHAKE_ANIMATION_LIFESPAN*CharacterData.CATCH_SHAKES + CATCH_TRANSFORM_ANIMATION_LIFESPAN;
	
	// Polygons for Type Colors in Status Box -- First array is for player, Second array is for the opponent
	private static final int[][] primaryColorx = { { 0, 199, 94, 0 }, { 0, 191, 104, 0 } };
	private static final int[][] primaryColory = { { 0, 0, 105, 105 }, { 0, 0, 88, 88 } };
	private static final int[][] secondaryColorx = { { 294, 199, 94, 294 }, { 191, 294, 294, 104 } };
	private static final int[][] secondaryColory = { { 0, 0, 105, 105 }, { 0, 0, 88, 88 } };
	
	// Polygons for Type Colors in the Pokemon View
	private static final int[] pkmnPrimaryColorx = { 0, 349, 5, 0 };
	private static final int[] pkmnPrimaryColory = { 0, 0, 344, 344 };
	private static final int[] pkmnSecondaryColorx = { 344, 349, 349, 0 };
	private static final int[] pkmnSecondaryColory = { 0, 0, 344, 344 };
	
	// The current battle in view, the current message being displayed, and the current selected button
	private Battle currentBattle;
	private String message;
	private int selectedButton;
	
	// The current state that the battle is in and current update type
	private VisualState state;
	private Update update;
	
	// Holds the animation for the player and the opponent
	private PokemonAnimationState playerAnimation, enemyAnimation;
	
	// All the different buttons!!
	private Button[] moveButtons, bagButtons, menuButtons, pokemonButtons;
	private Button[] bagTabButtons, pokemonTabButtons, logButtons;
	private Button fightBtn, bagBtn, pokemonBtn, runBtn, backButton;
	private Button bagRightButton, bagLeftButton, bagLastUsedBtn, pokemonSwitchButton;
	private Button yesButton, noButton, newMoveButton;
	private Button logLeftButton, logRightButton;
	
	// Current bag page, bag category, and selected item
	private int bagPage;
	private int selectedBagTab;
	private ItemNamesies selectedItem;
	
	// Current selected tab in Pokemon view and whether or not a switch is forced
	private int selectedPokemonTab;
	private boolean switchForced;
	
	private int logPage;
	private List<String> logMessages;
	
	private List<Move> selectedMoveList;
	
	// The last move that a Pokemon used
	private int lastMoveUsed;
	
	// Which Pokemon is trying to learn a new move, and which move
	private ActivePokemon learnedPokemon;
	private Move learnedMove;
	
	// Stat gains and corresponding new stat upgrades for leveling up/evolving
	private int[] statGains;
	private int[] newStats;
	
	public BattleView() {
		playerAnimation = new PokemonAnimationState();
		enemyAnimation = new PokemonAnimationState();
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
	
	// Contains the different types of states a battle can be in
	public enum VisualState {
		MESSAGE(updateMessage),
		BAG(updateBag), 
		INVALID_BAG(updateBag), 
		FIGHT(updateFight), 
		INVALID_FIGHT(updateFight), 
		POKEMON(updatePokemon), 
		INVALID_POKEMON(updatePokemon), 
		MENU(updateMenu), 
		LEARN_MOVE_QUESTION(updateLearnMoveQuestion), 
		LEARN_MOVE_DELETE(updateLearnMoveDelete),
		USE_ITEM(updatePokemon), 
		STAT_GAIN(updateMessage), 
		LOG_VIEW(updateLog);
		
		private final UpdateVisualState updateVisualState;
		
		VisualState(UpdateVisualState updateVisualState) {
			this.updateVisualState = updateVisualState;
		}
		
		public interface UpdateVisualState {
			void update(BattleView view);
			void set(BattleView view);
			void draw(BattleView view, Graphics g, TileSet tiles);
		}
	};
	
	// Handles animation and keeps track of the current state
	private class PokemonAnimationState {
		private PokemonState oldState, state;
		private int animationHP, animationEvolve, animationExp, animationCatch;
		private int animationCatchDuration;
		
		PokemonAnimationState() {
			oldState = new PokemonState();
			state = new PokemonState();
		}
		
		private void resetVals(ActivePokemon p) {
			resetVals(
					p.getHP(),
					p.getStatus().getType(),
					p.getDisplayType(currentBattle),
					p.isShiny(),
					p.getPokemonInfo(),
					p.getName(),
					p.getMaxHP(),
					p.getLevel(),
					p.getGender(),
					p.expRatio()
			);
		}
		
		// Resets all the values in a state
		private void resetVals(
				int hp,
				StatusCondition status,
				Type[] type,
				boolean shiny,
				PokemonInfo pokemon,
				String name,
				int maxHP,
				int level,
				Gender gender,
				float expRatio
		) {
			animationHP = 0;
			animationExp = 0;
			animationCatchDuration = 0;
			
			state.hp = oldState.hp = hp;
			state.status = oldState.status = status;
			state.type = type;
			state.shiny = shiny;
			state.imageNumber = pokemon.getImageNumber(state.shiny);
			state.caught = currentBattle.isWildBattle() && currentBattle.getPlayer().getPokedex().isCaught(pokemon.namesies());
			state.name = name;
			state.maxHp = oldState.maxHp = maxHP;
			state.level = level;
			state.gender = gender;
			state.expRatio = oldState.expRatio = expRatio;
		}
		
		private void startHpAnimation(int newHp) {
			if (newHp == state.hp) {
				return;
			}
			
			oldState.hp = state.hp;
			state.hp = newHp;
			animationHP = Math.abs(oldState.hp - state.hp)*FRAMES_PER_HP_LOSS;
		}
		
		private void setMaxHP(int newMax) {
			state.maxHp = newMax;
		}
		
		private void setStatus(StatusCondition newStatus) {
			oldState.status = state.status;
			state.status = newStatus;
		}
		
		private void setType(Type[] newType) 
		{
			state.type = newType;
		}
		
		private void startPokemonUpdateAnimation(PokemonInfo newPokemon, boolean newShiny, boolean animate) {
			state.shiny = newShiny;
			if (state.imageNumber != 0) {
				oldState.imageNumber = state.imageNumber;
				if (animate) {
					animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
				}
			}
			
			state.imageNumber = newPokemon.getImageNumber(state.shiny);
			animationCatchDuration = 0;
		}
		
		private void startCatchAnimation(int duration) {
			if (duration == -1) { // TODO: There should be a constant for this
				animationCatch = CATCH_ANIMATION_LIFESPAN;
				animationCatchDuration = -1;
			}
			else {
				// TODO: uggy
				animationCatch = animationCatchDuration = duration*CATCH_SHAKE_ANIMATION_LIFESPAN + 2*CATCH_TRANSFORM_ANIMATION_LIFESPAN;
			}
		}
		
		private void startExpAnimation(float newExpRatio, boolean levelUp) {
			oldState.expRatio = levelUp ? 0 : state.expRatio;
			state.expRatio = newExpRatio;
			animationExp = (int)(100*Math.abs(oldState.expRatio - state.expRatio)*FRAMES_PER_HP_LOSS);
		}
		
		private void setLevel(int newLevel) {
			state.level = newLevel;
		}
		
		private void setName(String newName) {
			state.name = newName;
		}
		
		private void setGender(Gender newGender) {
			state.gender = newGender;
		}
		
		private boolean isEmpty() {
			return state.imageNumber == 0;
		}
		
		private boolean isAnimationPlaying() {
			return animationHP != 0 || animationEvolve != 0 || animationCatch != 0 || animationExp != 0;
		}
		
		// Draws all of the text inside the status box
		private void drawStatusBoxText(Graphics g, int isEnemy, TileSet tiles, ActivePokemon pokemon) {
			// Name, Gender, Level, Status Condition
			DrawUtils.setFont(g, 27);
			DrawUtils.drawShadowText(g, state.name + " " + state.gender.getCharacter(), 20, 40, false);
			DrawUtils.drawShadowText(g, "Lv" + state.level, 272, 40, true);
			
			DrawUtils.setFont(g, 24);
			DrawUtils.drawShadowText(g, state.status.getName(), 20, 71, false);
			
			// Only the player shows the HP Text
			if (isEnemy == 0) {
				// HP Text Animation
				int originalTime = Math.abs(state.hp - oldState.hp)*FRAMES_PER_HP_LOSS;
				String hpStr = state.hp + "/" + state.maxHp;
				if (animationHP > 0) {
					hpStr = (int)(state.hp + (oldState.hp - state.hp)*(animationHP/(float)originalTime)) + "/" + state.maxHp;
				}
				
				DrawUtils.setFont(g, 24);
				DrawUtils.drawShadowText(g, hpStr, 273, 95, true);
			}
			// Show whether or not the wild Pokemon has already been caught
			else if (state.caught) {
				g.drawImage(tiles.getTile(0x4), 296, 40, null);
			}
		}
		
		// TODO: Is this code duplicated in other places? Like the evolution view by any chance
		// Might want to include a helper class that contains a generic method for different types of animations
		private void catchAnimation(Graphics g, BufferedImage plyrImg, int isEnemy, TileSet pkmTiles, int px, int py) {
			Graphics2D g2d = (Graphics2D)g;
			float[] pokeyScales = { 1f, 1f, 1f, 1f };
			float[] pokeyOffsets = { 255f, 255f, 255f, 0f };
			float[] ballScales = { 1f, 1f, 1f, 1f };
			float[] ballOffsets = { 255f, 255f, 255f, 0f };
			
			int xOffset = 0;
			
			int lifespan = animationCatchDuration == -1 ? CATCH_ANIMATION_LIFESPAN : animationCatchDuration;
			
			// Turn white
			if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3) {
				pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(1 - (animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - .7f)));
				ballScales[3] = 0;
			}
			// Transform into Pokeball
			else if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7) {
		       pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
		       pokeyScales[3] = ((animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
		       ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
		       ballScales[3] = (1 - (animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
		    }
			// Restore color
			else if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN) {
				pokeyScales[3] = 0;
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.3f));
			}
			// Shake
			else if (animationCatchDuration == -1 || animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN) {
				pokeyScales[3] = 0;
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 0;
				xOffset = (int)(10*Math.sin(animationCatch/200.0));
			}
			// Turn white -- didn't catch
			else if (animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7) {
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(1f - (animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - 0.7f)));
				pokeyScales[3] = 0;
			}
			// Transform into Pokemon
			else if (animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3) {
				pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
				pokeyScales[3] = (1 - (animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
				ballScales[3] = ((animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
			}
			// Restore color
			else {
				ballScales[3] = 0;
				pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(animationCatch)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1.0f - .7f));
			}
			
			animationCatch -= Global.MS_BETWEEN_FRAMES;

			BufferedImage pkBall = pkmTiles.getTile(0x11111);

			g2d.drawImage(DrawUtils.colorImage(pkBall, ballScales, ballOffsets), px - pkBall.getWidth()/2 + xOffset, py - pkBall.getHeight(), null);
			g2d.drawImage(DrawUtils.colorImage(plyrImg, pokeyScales, pokeyOffsets), px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null);
		}
		
		// hi :)
		// TODO: is there any way to combine these?
		private void evolveAnimation(Graphics g, BufferedImage plyrImg, int isEnemy, TileSet pkmTiles, int px, int py) {
			Graphics2D g2d = (Graphics2D)g;
			
			float[] prevEvolutionScales = { 1f, 1f, 1f, 1f };
			float[] prevEvolutionOffsets = { 255f, 255f, 255f, 0f };
			float[] evolutionScales = { 1f, 1f, 1f, 1f };
			float[] evolutionOffsets = { 255f, 255f, 255f, 0f };
			
			// Turn white
			if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.7) {
				prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255*(1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.7f)/(EVOLVE_ANIMATION_LIFESPAN*(1 - 0.7f)));
				evolutionScales[3] = 0;
			}
			// Change form
			else if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.3) {
				prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255;
				prevEvolutionScales[3] = ((animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
				evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255;
				evolutionScales[3] = (1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
			}
			// Restore color
			else {
				prevEvolutionScales[3] = 0;
				evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255*(animationEvolve)/(EVOLVE_ANIMATION_LIFESPAN*(1-0.7f));
			}
			
			animationEvolve -= Global.MS_BETWEEN_FRAMES;
			
			BufferedImage prevEvo = pkmTiles.getTile(oldState.imageNumber + (isEnemy^1));

			g2d.drawImage(DrawUtils.colorImage(plyrImg, evolutionScales, evolutionOffsets), px-plyrImg.getWidth()/2, py-plyrImg.getHeight(), null);
			g2d.drawImage(DrawUtils.colorImage(prevEvo, prevEvolutionScales, prevEvolutionOffsets), px-prevEvo.getWidth()/2, py-prevEvo.getHeight(), null);
		}
		
		private void drawHealthBar(Graphics g) {
			// Draw the white background of the health bar
			g.setColor(Color.WHITE);
			g.fillRect(108, 53, 315 - 150, 124 - 105);
			
			// Get the ratio based off of the possible animation
			float ratio = state.hp/(float)state.maxHp;
			if (animationHP > 0) {
				animationHP -= HP_LOSS_RATIO*state.maxHp + 1;
				int originalTime = Math.abs(state.hp - oldState.hp)*FRAMES_PER_HP_LOSS;
				ratio = (state.hp + (oldState.hp - state.hp)*(animationHP/(float)originalTime))/(float)state.maxHp;
			}
			else {
				animationHP = 0;
			}
			
			// Set the proper color for the ratio and fill in the health bar as appropriate
			g.setColor(DrawUtils.getHPColor(ratio));
			if (animationHP > 0 && (animationHP/10)%2 == 0) {
				g.setColor(g.getColor().darker());
			}

			g.fillRoundRect(113, 57, (int)((312 - 155)*ratio), 119 - 109, 5, 5);
		}
		
		private void drawExpBar(Graphics g) {
			// Show the animation
			float expRatio = state.expRatio;
			if (animationExp > 0) {
				animationExp -= EXP_LOSS_RATIO;
				int originalTime = (int)(100*Math.abs(state.expRatio - oldState.expRatio)*FRAMES_PER_HP_LOSS);
				expRatio = (state.expRatio + (oldState.expRatio - state.expRatio)*(animationExp/(float)originalTime));
			}
			else {
				animationExp = 0;
			}
			
			// Experience bar background
			g.setColor(new Color(153, 153, 153));
			g.fillRect(36, 107, 294 - 36, 115 - 107); //463,  304
			
			// Experience bar foreground
			g.setColor(DrawUtils.EXP_BAR_COLOR);
			g.fillRect(36, 107, (int)((294 - 36)*expRatio), 115 - 107);
		}
		
		// Draws the status box, not including the text
		private void drawStatusBox(Graphics g, int isEnemy, ActivePokemon pokemon, TileSet pkmTiles, int px, int py) { //-42 -52
			// Draw the colored type polygons
			Color[] typeColors = Type.getColors(state.type);
			g.setColor(typeColors[0]);
			g.fillPolygon(primaryColorx[isEnemy], primaryColory[isEnemy], 4);
			g.setColor(typeColors[1]);
			g.fillPolygon(secondaryColorx[isEnemy], secondaryColory[isEnemy], 4);
			
			// Draw health bar and player's EXP Bar
			drawHealthBar(g);
			if (isEnemy == 0) {
				drawExpBar(g);
			}
			
			// Draw the Pokemon image if applicable
			if (!isEmpty() && !pokemon.isSemiInvulnerable()) {
				BufferedImage plyrImg = pkmTiles.getTile(state.imageNumber + (isEnemy^1));
				if (plyrImg != null) {
					if (animationEvolve > 0) {
						evolveAnimation(g, plyrImg, isEnemy, pkmTiles, px, py);	
					}
					else if (animationCatch > 0) {
						catchAnimation(g, plyrImg, isEnemy, pkmTiles, px, py);	
					}
					else {
						if (animationCatchDuration == -1) {
							plyrImg = pkmTiles.getTile(0x11111);
						}
						
						g.drawImage(plyrImg, px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null); // TODO: Why is height not /2 -- can this use the centered image function?
						
						animationEvolve = 0;
						animationCatch = 0;
					}
				}
			}
		}
	}

	// TODO: Should those set methods up there be inside this class and should it be moved to its own file?
	// A class to hold the state of a Pokemon
	private class PokemonState {
		private int maxHp, hp, imageNumber, level;
		private String name;
		private StatusCondition status;
		private Type[] type;
		private float expRatio;
		private boolean shiny;
		private boolean caught;
		private Gender gender;
		
		PokemonState() {
			type = new Type[2];
		}
	}
	
	// Updates when in the menu state
	private static VisualState.UpdateVisualState updateMenu = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {
			for (Button b: view.menuButtons) {
				b.setForceHover(false);
			}
		}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x3), 0, 439, null);
			g.drawImage(tiles.getTile(0x2), 0, 0, null);
			
			g.setColor(Color.BLACK);
			
			ActivePokemon playerPokemon = view.currentBattle.getPlayer().front();
			
			DrawUtils.setFont(g, 30);
			DrawUtils.drawWrappedText(g, "What will " + playerPokemon.getActualName() + " do?", 20, 485, 400);
			
			for (Button b: view.menuButtons) {
				b.draw(g);
			}
		}

		@Override
		public void update(BattleView view) {
			// Update menu buttons
			view.selectedButton = Button.update(view.menuButtons, view.selectedButton);
			
			// Show Bag View
			if (view.bagBtn.checkConsumePress()) {
				view.setVisualState(VisualState.BAG);
			}
			// Show Pokemon View
			else if (view.pokemonBtn.checkConsumePress()) {
				view.setVisualState(VisualState.POKEMON);
			}
			// Attempt escape
			else if (view.runBtn.checkConsumePress()) {
				view.setVisualState(VisualState.MESSAGE);
				view.currentBattle.runAway();
				view.cycleMessage(false);
			}
			// Show Fight View TODO: Semi-invulnerable moves look awful and weird
			else if (view.fightBtn.checkConsumePress() || view.currentBattle.getPlayer().front().isSemiInvulnerable()) {
				view.setVisualState(VisualState.FIGHT);
				
				// Move is forced -- don't show menu, but execute the move
				if (Move.forceMove(view.currentBattle, view.currentBattle.getPlayer().front())) {
					view.currentBattle.getPlayer().performAction(view.currentBattle, Action.FIGHT);
					view.setVisualState(VisualState.MESSAGE);
					view.cycleMessage(false);
				}
			}
			else if (InputControl.instance().consumeIfDown(ControlKey.L)) {
				view.logPage = 0;
				view.logMessages = view.currentBattle.getPlayer().getLogMessages();
				
				if (view.logMessages.size() / LOGS_PER_PAGE > 0) {
					view.selectedButton = LOG_RIGHT_BUTTON;
					view.logRightButton.setActive(true);
					view.selectedButton = Button.update(view.logButtons, view.selectedButton);
				}
				else {
					view.logRightButton.setActive(false);
				}
				
				view.logLeftButton.setActive(false);
				view.setVisualState(VisualState.LOG_VIEW);
			}
		}
	};
	
	// Updates when in fight mode (selecting a move menu)
	private static VisualState.UpdateVisualState updateFight = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {
			view.selectedButton = view.lastMoveUsed;
			view.selectedMoveList = view.currentBattle.getPlayer().front().getMoves(view.currentBattle);
			for (int i = 0; i < Move.MAX_MOVES; i++) {
				view.moveButtons[i].setActive(i < view.selectedMoveList.size());
			}
			
			for (Button b: view.moveButtons) {
				b.setForceHover(false);
			}
		}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x20), 415, 440, null);
			g.drawImage(tiles.getTile(0x21), 0, 440, null);
			
			ActivePokemon playerPokemon = view.currentBattle.getPlayer().front();
			
			List<Move> moves = playerPokemon.getMoves(view.currentBattle);
			for (int y = 0, i = 0; y < 2; y++) {
				for (int x = 0; x < Move.MAX_MOVES/2 && i < moves.size(); x++, i++) {
					int dx = 22 + x*190, dy = 440 + 21 + y*62;
					g.translate(dx, dy);
					
					Move move = moves.get(i);
					g.setColor(move.getAttack().getActualType().getColor());
					g.fillRect(0, 0, 183, 55);
					g.drawImage(tiles.getTile(0x22), 0, 0, null);
					
					g.setColor(Color.BLACK);
					DrawUtils.setFont(g, 22);
					g.drawString(move.getAttack().getName(), 10, 26);
					
					DrawUtils.setFont(g, 18);
					DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);
					
					BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
					g.drawImage(categoryImage, 12, 32, null);
					
					g.translate(-dx, -dy);
				}
			}

			// TODO: See if I can use isNullOrEmpty
			String msgLine = view.state == VisualState.INVALID_FIGHT && view.message != null ? view.message : "Select a move!";
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 30);
			DrawUtils.drawWrappedText(g, msgLine, 440, 485, 350); // TODO: Is this duplicated code?
			
			View.drawArrows(g, null, view.backButton);
			
			for (int i = 0; i < Move.MAX_MOVES && i < moves.size(); i++) {
				view.moveButtons[i].draw(g);
			}
			
			view.backButton.draw(g);
		}

		@Override
		public void update(BattleView view) {
			// Update move buttons and the back button
			view.selectedButton = Button.update(view.moveButtons, view.selectedButton);
			view.backButton.update(false, ControlKey.BACK);
			
			// Get the Pokemon that is attacking and their corresponsing move list
			ActivePokemon front = view.currentBattle.getPlayer().front();
			
			for (int i = 0; i < view.selectedMoveList.size(); i++) {
				if (view.moveButtons[i].checkConsumePress()) {
					view.lastMoveUsed = i;
					
					// Execute the move if valid
					if (Move.validMove(view.currentBattle, front, view.selectedMoveList.get(i), true)) {
						view.currentBattle.getPlayer().performAction(view.currentBattle, Action.FIGHT);
						view.setVisualState(VisualState.MESSAGE);
						view.cycleMessage(false);
					}
					// An invalid move -- Don't let them select it
					else {
						view.cycleMessage(false);
						view.setVisualState(VisualState.INVALID_FIGHT);
					}
				}
			}
			
			// Return to main battle menu
			if (view.backButton.checkConsumePress()) {
				view.setVisualState(VisualState.MENU);
			}
		}
	};
	
	private static VisualState.UpdateVisualState updateMessage = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x3), 0, 439, null);
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 30);
			
			DrawUtils.drawWrappedText(g, view.message, 30, 490, 720);
			
			if (view.state == VisualState.STAT_GAIN) {
				g.drawImage(tiles.getTile(0x5), 0, 280, null);
				g.setColor(Color.BLACK);
				for (int i = 0; i < Stat.NUM_STATS; i++)
				{
					DrawUtils.setFont(g, 16);
					g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);
					
					DrawUtils.drawRightAlignedString(g, (view.statGains[i] < 0 ? "" : " + ") + view.statGains[i], 206, 314 + i*21);
					DrawUtils.drawRightAlignedString(g, view.newStats[i] + "", 247, 314 + i*21);
				}
			}
		}

		@Override
		public void update(BattleView view) {
			boolean pressed = false;
			InputControl input = InputControl.instance();
			
			// Consume input for mouse clicks and spacebars
			if (input.consumeIfMouseDown()) {
				pressed = true;
			}

			if (input.consumeIfDown(ControlKey.SPACE)) {
				pressed = true;
			}
			
			// Don't go to the next message if an animation is playing 
			if (pressed && view.message != null && !view.playerAnimation.isAnimationPlaying() && !view.enemyAnimation.isAnimationPlaying()) {
				if (view.state == VisualState.STAT_GAIN) view.setVisualState(VisualState.MESSAGE);
				view.cycleMessage(false);
			}			
		}
	};
	
	// Handles updates for the bag view
	private static VisualState.UpdateVisualState updateBag = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {
			int pageSize = view.currentBattle.getPlayer().getBag().getCategory(BATTLE_BAG_CATEGORIES[view.selectedBagTab]).size();
			
			for (int i = 0; i < ITEMS_PER_PAGE; i++) {
				view.bagButtons[ITEMS + i].setActive(i < pageSize - view.bagPage*ITEMS_PER_PAGE);
			}

			// TODO: Make a method for this
			view.bagLastUsedBtn.setActive(view.currentBattle.getPlayer().getBag().getLastUsedItem() != ItemNamesies.NO_ITEM);
			
			for (Button b: view.bagButtons) {
				b.setForceHover(false);
			}
			
		}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x10), 0, 160, null);
			g.drawImage(tiles.getTile(BATTLE_BAG_CATEGORIES[view.selectedBagTab].getImageNumber()), 30, 190, null);
			g.drawImage(tiles.getTile(BATTLE_BAG_CATEGORIES[view.selectedBagTab].getImageNumber() - 4), 30, 492, null);
			g.drawImage(tiles.getTile(0x20), 415, 440, null);
			
			Bag bag = view.currentBattle.getPlayer().getBag();
			
			Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[view.selectedBagTab]);
			TileSet itemTiles = Game.getData().getItemTiles();

			DrawUtils.setFont(g, 12);
			Iterator<ItemNamesies> iter = toDraw.iterator();
			for (int i = 0; i < view.bagPage*ITEMS_PER_PAGE; i++) {
				iter.next();
			}

			for (int y = 0; y < ITEMS_PER_PAGE/2; y++) {
				for (int x = 0; x < 2 && iter.hasNext(); x++) {
					int dx = 55 + x*162, dy = 243 + y*38;
					
					g.translate(dx, dy);
					
					// Draw box
					g.drawImage(tiles.getTile(0x11), 0, 0, null);
					
					// Draw item image
					ItemNamesies item = iter.next();
					BufferedImage img = itemTiles.getTile(item.getItem().getImageIndex());
					DrawUtils.drawCenteredImage(g, img, 14, 14);

					// Item name
					g.drawString(item.getName(), 28, 19);
					
					// Item quantity
					DrawUtils.drawRightAlignedString(g, "x" + view.currentBattle.getPlayer().getBag().getQuantity(item), 140, 19);
					
					g.translate(-dx, -dy);
				}
			}
			
			// Bag page number
			DrawUtils.setFont(g, 20);
			DrawUtils.drawCenteredWidthString(g, (view.bagPage + 1) + "/" + Math.max(1, (int)Math.ceil(toDraw.size()/10.0)), 210, 450);
			
			// Left/Right Arrows
			View.drawArrows(g, view.bagLeftButton, view.bagRightButton);
			
			// Last Item Used
			ItemNamesies lastUsedItem = bag.getLastUsedItem();

			// TODO: Should have a method to check if it is the empty item
			if (lastUsedItem != ItemNamesies.NO_ITEM) {
				g.translate(214, 517);
				DrawUtils.setFont(g, 12);
				g.drawImage(tiles.getTile(0x11), 0, 0, null);

				BufferedImage img = itemTiles.getTile(lastUsedItem.getItem().getImageIndex());
				DrawUtils.drawCenteredImage(g, img, 14, 14);

				g.drawString(lastUsedItem.getName(), 28, 19);
				DrawUtils.drawRightAlignedString(g, "x" + bag.getQuantity(lastUsedItem), 140, 19);

				g.translate(-214, -517);
			}

			// Messages text
			String msgLine = view.state == VisualState.INVALID_BAG && view.message != null ? view.message : "Choose an item!";
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 30);
			DrawUtils.drawWrappedText(g, msgLine, 440, 495, 350);
			
			// Back Arrow
			View.drawArrows(g, null, view.backButton);
			
			for (Button b: view.bagButtons) {
				b.draw(g);
			}
			
			view.backButton.draw(g);
		}

		@Override
		public void update(BattleView view) {
			// Update all bag buttons and the back button
			view.selectedButton = Button.update(view.bagButtons, view.selectedButton);
			view.backButton.update(false, ControlKey.BACK);
			
			// Check tabs
			for (int i = 0; i < BATTLE_BAG_CATEGORIES.length; i++) {
				if (view.bagTabButtons[i].checkConsumePress()) {
					view.bagPage = 0;
					view.selectedBagTab = i;
					view.setVisualState(view.state); // To update active buttons
				}
			}
			
			CharacterData player = view.currentBattle.getPlayer();
			Bag bag = player.getBag();
			Set<ItemNamesies> toDraw = bag.getCategory(BATTLE_BAG_CATEGORIES[view.selectedBagTab]);
			Iterator<ItemNamesies> iter = toDraw.iterator();
			
			// Skip ahead to the current page
			for (int i = 0; i < view.bagPage*ITEMS_PER_PAGE; i++) {
				iter.next();
			}
			
			// Go through each item on the page
			for (int i = ITEMS; i < ITEMS + ITEMS_PER_PAGE && iter.hasNext(); i++) {
				ItemNamesies item = iter.next();
				if (view.bagButtons[i].checkConsumePress()) {
					// Pokemon Use Item -- Set item to be selected an change to Pokemon View
					if (item.getItem() instanceof PokemonUseItem) {
						view.selectedItem = item;
						view.setVisualState(VisualState.USE_ITEM);
						break;
					}
					// Otherwise, just use it on the battle if successful
					else if (bag.battleUseItem(item, view.currentBattle.getPlayer().front(), view.currentBattle)) {
						view.currentBattle.getPlayer().performAction(view.currentBattle, Action.ITEM);
						view.setVisualState(VisualState.MENU);
						view.cycleMessage(false);
						break;
					}
					// If the item cannot be used, do not consume
					else {
						view.cycleMessage(false);
						view.setVisualState(VisualState.INVALID_BAG);
					}
				}
			}
			
			// Selecting the Last Item Used Button
			if (view.bagLastUsedBtn.checkConsumePress()) {
				ItemNamesies lastItemUsed = bag.getLastUsedItem();
				if (lastItemUsed != ItemNamesies.NO_ITEM && bag.battleUseItem(lastItemUsed, player.front(), view.currentBattle)) {
					player.performAction(view.currentBattle, Action.ITEM);
					view.setVisualState(VisualState.MENU);
					view.cycleMessage(false);
				}
				else {
					view.cycleMessage(false);
					view.setVisualState(VisualState.INVALID_BAG);
				}
			}
			
			// Next page
			if (view.bagRightButton.checkConsumePress()) {
				// TODO: Should have a method to get the max pages also this should just use mod right?
				if (view.bagPage == ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1)) {
					view.bagPage = 0;
				}
				else {
					view.bagPage++;
				}
				
				view.setVisualState(view.state); // To update active buttons
			}
			
			// Previous Page
			if (view.bagLeftButton.checkConsumePress()) {
				if (view.bagPage == 0) {
					view.bagPage = ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1);
				}
				else {
					view.bagPage--;
				}
				
				view.setVisualState(view.state); // To update active buttons
			}
			
			// Return to main battle menu
			if (view.backButton.checkConsumePress()) {
				view.setVisualState(VisualState.MENU);
			}
		}
	};
	
	private static VisualState.UpdateVisualState updatePokemon = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {
			List<ActivePokemon> list = view.currentBattle.getPlayer().getTeam();
			for (int i = 0; i < view.pokemonTabButtons.length; i++) {
				view.pokemonTabButtons[i].setActive(i < list.size());
			}
			
			if (view.state != VisualState.USE_ITEM) {
				view.pokemonSwitchButton.setActive(list.get(view.selectedPokemonTab).canFight());
			}
			
			for (Button b: view.pokemonButtons) {
				b.setForceHover(false);
			}
		}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			// Draw Background
			g.drawImage(tiles.getTile(0x10), 0, 160, null);
			
			// Get current Pokemon
			List<ActivePokemon> list = view.currentBattle.getPlayer().getTeam();
			ActivePokemon selectedPkm = list.get(view.selectedPokemonTab);
			
			// Draw type color polygons
			Type[] type = selectedPkm.getActualType();
			Color[] typeColors = Type.getColors(selectedPkm);
			
			g.translate(31, 224);
			g.setColor(typeColors[0]);
			g.fillPolygon(pkmnPrimaryColorx, pkmnPrimaryColory, 4);
			g.translate(-31, -224);
			
			g.translate(36, 224);
			g.setColor(typeColors[1]);
			g.fillPolygon(pkmnSecondaryColorx, pkmnSecondaryColory, 4);
			g.translate(-36, -224);
			
			// Draw Messages Box
			g.drawImage(tiles.getTile(0x20), 415, 440, null);
			
			// Draw Box Outlines for Pokemon Info
			if (!selectedPkm.canFight()) { // Fainted Pokemon and Eggs
				g.drawImage(tiles.getTile(0x35), 30, 224, null);
				g.drawImage(tiles.getTile(0x31), 55, 249, null);
			}
			else {
				g.drawImage(tiles.getTile(0x34), 30, 224, null);
				g.drawImage(tiles.getTile(0x30), 55, 249, null);
			}
			
			if (selectedPkm.isEgg()) {
				// Name
				DrawUtils.setFont(g, 16);
				g.setColor(Color.BLACK);
				String nameStr = selectedPkm.getActualName();
				g.drawString(nameStr, 62, 269);
				
				// Description
				DrawUtils.setFont(g, 14);
				DrawUtils.drawWrappedText(g, selectedPkm.getEggMessage(), 62, 288, 306);
			}
			else {
				// Name and Gender
				DrawUtils.setFont(g, 16);
				g.setColor(Color.BLACK);
				String nameStr = selectedPkm.getActualName() + " " + selectedPkm.getGender().getCharacter();
				g.drawString(nameStr, 62, 269);
				
				// Status Condition
				String statusStr = selectedPkm.getStatus().getType().getName();
				g.drawString(statusStr, 179, 269);
				
				// Level
				String levelStr = "Lv" + selectedPkm.getLevel();
				g.drawString(levelStr, 220, 269);
				
				// Draw type tiles
				if (type[1] == Type.NO_TYPE) {
					g.drawImage(tiles.getTile(type[0].getImageIndex()), 322, 255, null);
				}
				else {
					g.drawImage(tiles.getTile(type[0].getImageIndex()), 285, 255, null);
					g.drawImage(tiles.getTile(type[1].getImageIndex()), 322, 255, null);
				}
				
				// Ability
				DrawUtils.setFont(g, 14);
				g.drawString(selectedPkm.getActualAbility().getName(), 62, 288);
				
				// Experience
				g.drawString("EXP", 220, 288);
				DrawUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), 352, 288);

				g.drawString(selectedPkm.getActualHeldItem().getName(), 62, 307);
				
				g.drawString("To Next Lv", 220, 307);
				DrawUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), 352, 307);
				
				// Experience Bar
				float expRatio = selectedPkm.expRatio();
				g.setColor(DrawUtils.EXP_BAR_COLOR);
				g.fillRect(222, 315, (int)(137*expRatio), 10);
				
				// HP Bar
				g.setColor(selectedPkm.getHPColor());
				g.fillRect(57, 341, (int)(137*selectedPkm.getHPRatio()), 10);
				
				// Write stat names
				DrawUtils.setFont(g, 16);
				for (int i = 0; i < Stat.NUM_STATS; i++) {
					g.setColor(selectedPkm.getNature().getColor(i));
					g.drawString(Stat.getStat(i, false).getShortName(), 62, 21*i + 372);
				}
				
				// Write stat values
				g.setColor(Color.BLACK);
				
				int[] statsVal = selectedPkm.getStats();
				for (int i = 0; i < Stat.NUM_STATS; i++) {
					String valStr = i == Stat.HP.index() ? selectedPkm.getHP() + "/" + statsVal[i] : "" + statsVal[i];
					DrawUtils.drawRightAlignedString(g, valStr, 188, 21*i + 372);
				}
				
				// Draw Move List
				List<Move> movesList = selectedPkm.getActualMoves();
				for (int i = 0; i < movesList.size(); i++) {
					int dx = 228, dy = 359 + i*46;
					g.translate(dx, dy);
					
					// Draw Color background
					Move move = movesList.get(i);
					g.setColor(move.getAttack().getActualType().getColor());
					g.fillRect(0, 0, 125, 40);
					g.drawImage(tiles.getTile(0x32), 0, 0, null);
					
					// Draw attack name
					g.setColor(Color.BLACK);
					g.drawString(move.getAttack().getName(), 7, 17);
					
					// Draw PP amount
					DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 118, 33);
					
					BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
					g.drawImage(categoryImage, 7, 21, null);
					
					g.translate(-dx, -dy);
				}
			}

			// Draw Switch/Use text
			DrawUtils.setFont(g, 20);
			if (view.state == VisualState.USE_ITEM) {
				g.drawString("Use!", 103, 533);
			}
			else {
				g.drawString("Switch!", 93, 533);
			}
			
			// Draw tabs
			TileSet partyTiles = Game.getData().getPartyTiles();
			for (int i = 0; i < list.size(); i++) {
				ActivePokemon pkm = list.get(i);
				
				// Draw tab
				if(pkm.isEgg()) {
					g.setColor(Type.getColors(selectedPkm)[0]);				
				}
				else {
					g.setColor(pkm.getActualType()[0].getColor());
				}
				
				g.fillRect(32 + i*59, 192, 59, 34);
				if (i == view.selectedPokemonTab) {
					g.drawImage(tiles.getTile(0x36), 30 + i*59, 190, null);
				}
				else {
					g.drawImage(tiles.getTile(0x33), 30 + i*59, 190, null);
				}
				
				// Draw Pokemon Image
				BufferedImage img = partyTiles.getTile(pkm.getTinyImageIndex());
				DrawUtils.drawCenteredImage(g, img, 60 + i*59, 205);

				// Fade out fainted Pokemon
				if (!pkm.canFight()) {
					g.setColor(new Color(0, 0, 0, 128));
					g.fillRect(32 + i*59, 192, 59, 34);
				}
			}
			
			// Draw Messages
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 30);
			String msgLine = view.state == VisualState.INVALID_POKEMON && view.message != null ? view.message : "Select a Pok\u00e9mon!";
			DrawUtils.drawWrappedText(g, msgLine, 440, 485, 350);
			
			// Draw back arrow when applicable
			if (!view.switchForced) {
				View.drawArrows(g, null, view.backButton);
			}

			for (int i = 0; i < list.size(); i++) {
				view.pokemonTabButtons[i].draw(g);
			}
			
			if (view.state == VisualState.USE_ITEM || selectedPkm.canFight()) {
				view.pokemonSwitchButton.draw(g);
			}
			
			view.backButton.draw(g);
		}

		@Override
		public void update(BattleView view) {
			// Update the buttons
			view.selectedButton = Button.update(view.pokemonButtons, view.selectedButton);
			view.backButton.update(false, ControlKey.BACK);
			
			CharacterData player = view.currentBattle.getPlayer();
			List<ActivePokemon> list = player.getTeam();
			for (int i = 0; i < list.size(); i++) {
				if (view.pokemonTabButtons[i].checkConsumePress()) {
					view.selectedPokemonTab = i;
					view.setVisualState(view.state); //to update active buttons
				}
			}
			
			// Switch Switch Switcheroo
			if (view.pokemonSwitchButton.checkConsumePress()) {
				ActivePokemon selectedPkm = list.get(view.selectedPokemonTab);
				
				// Use an item on this Pokemon instead of switching
				if (view.state == VisualState.USE_ITEM) {
					// Valid item
					if (player.getBag().battleUseItem(view.selectedItem, selectedPkm, view.currentBattle)) {
						player.performAction(view.currentBattle, Action.ITEM);
						view.setVisualState(VisualState.MENU);
						view.cycleMessage(false);
					}
					// Invalid item
					else {
						view.cycleMessage(false);
						view.setVisualState(VisualState.INVALID_BAG);
					}
				}
				// Actual switcheroo
				else {
					if (player.canSwitch(view.currentBattle, view.selectedPokemonTab)) {
						player.setFront(view.selectedPokemonTab);
						view.currentBattle.enterBattle(player.front());
						
						if (!view.switchForced) {
							player.performAction(view.currentBattle, Action.SWITCH);
						}
						
						view.cycleMessage(false);
						view.switchForced = false;
						view.lastMoveUsed = 0;
					}
					else {
						view.cycleMessage(false);
						view.setVisualState(VisualState.INVALID_POKEMON);
					}
				}
			}
	
			// Return to main menu if applicable
			if (view.backButton.checkConsumePress()) {
				if (!view.switchForced) {
					view.setVisualState(VisualState.MENU);
				}
			}
		}
	};
		
	private static VisualState.UpdateVisualState updateLearnMoveQuestion = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x3), 0, 439, null);
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 25);
			g.drawString("Delete a move in order to learn " + view.learnedMove.getAttack().getName() + "?", 30, 490);
			
			g.translate(view.yesButton.x, view.yesButton.y);
			
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, 183, 55);
			g.drawImage(tiles.getTile(0x22), 0, 0, null);
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 22);
			g.drawString("Yes", 10, 26);
			
			g.translate(-view.yesButton.x, -view.yesButton.y);
			
			g.translate(view.noButton.x, view.noButton.y);
			
			g.setColor(Color.RED);
			g.fillRect(0, 0, 183, 55);
			g.drawImage(tiles.getTile(0x22), 0, 0, null);
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 22);
			g.drawString("No", 10, 26);
			
			g.translate(-view.noButton.x, -view.noButton.y);
			
			view.yesButton.draw(g);
			view.noButton.draw(g);
		}

		@Override
		public void update(BattleView view) {
			view.yesButton.update();
			view.noButton.update();
			
			if (view.noButton.checkConsumePress()) {
				// This is all done really silly, so we need to do this
				MessageUpdate message = Messages.getNextMessage();
				for (int i = 0; i < Move.MAX_MOVES + 1; i++) {
					Messages.getNextMessage();
				}

				Messages.addMessage(message);
				
				view.setVisualState(VisualState.MESSAGE);
				view.cycleMessage(false);
			}
			
			if (view.yesButton.checkConsumePress()) {
				view.setVisualState(VisualState.LEARN_MOVE_DELETE);
			}
		}
	};
	
	private static VisualState.UpdateVisualState updateLearnMoveDelete = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x3), 0, 439, null);
			
			List<Move> moves = view.learnedPokemon.getActualMoves();
			for (int y = 0, i = 0; y < 2; y++) {
				for (int x = 0; x < Move.MAX_MOVES/2 && i < moves.size(); x++, i++) {
					int dx = 22 + x*190, dy = 440 + 21 + y*62;
					g.translate(dx, dy);
					
					Move move = moves.get(i);
					g.setColor(move.getAttack().getActualType().getColor());
					g.fillRect(0, 0, 183, 55);
					g.drawImage(tiles.getTile(0x22), 0, 0, null);
					
					g.setColor(Color.BLACK);
					DrawUtils.setFont(g, 22);
					g.drawString(move.getAttack().getName(), 10, 26);
					
					DrawUtils.setFont(g, 18);
					DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);
					
					BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
					g.drawImage(categoryImage, 12, 32, null);
					
					g.translate(-dx, -dy);
				}
			}
			
			g.translate(view.newMoveButton.x, view.newMoveButton.y);
			Move move = view.learnedMove;
			Color boxColor = move.getAttack().getActualType().getColor();
			g.setColor(boxColor);
			g.fillRect(0, 0, 183, 55);
			g.drawImage(tiles.getTile(0x22), 0, 0, null);
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 22);
			g.drawString(move.getAttack().getName(), 10, 26);
			
			DrawUtils.setFont(g, 18);
			DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);
			
			BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
			g.drawImage(categoryImage, 12, 32, null);
			
			g.translate(-view.newMoveButton.x, -view.newMoveButton.y);
			
			String msgLine = "Select a move to delete!";
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 25);
			g.drawString(msgLine, view.newMoveButton.x, 485);
			
			for (int i = 0; i < moves.size(); i++) {
				view.moveButtons[i].draw(g);
			}
			
			view.newMoveButton.draw(g);
		}

		@Override
		public void update(BattleView view) {
			view.selectedButton = Button.update(view.moveButtons, view.selectedButton);
			view.newMoveButton.update();
			
			for (int i = 0; i < view.moveButtons.length; i++) {
				if (view.moveButtons[i].checkConsumePress()) {
					view.learnedPokemon.addMove(view.currentBattle, view.learnedMove, i);
					
					// This is all done really silly, so we need to do this
					MessageUpdate message = Messages.getNextMessage();
					for (int j = 0; j < Move.MAX_MOVES; j++) {
						if (j == i) {
							message = Messages.getNextMessage();
						}
						else {
							Messages.getNextMessage();
						}
					}

					Messages.addMessage(message);
					
					view.setVisualState(VisualState.MESSAGE);
					view.cycleMessage(false);
				}
			}
			
			if (view.newMoveButton.checkConsumePress()) {
				// This is all done really silly, so we need to do this
				MessageUpdate message = Messages.getNextMessage();
				for (int i = 0; i < Move.MAX_MOVES + 1; i++) {
					Messages.getNextMessage();
				}

				Messages.addMessage(message);
				
				view.setVisualState(VisualState.MESSAGE);
				view.cycleMessage(false);
			}
		}
	};
	
	private static VisualState.UpdateVisualState updateLog = new VisualState.UpdateVisualState() {

		@Override
		public void set(BattleView view) {}

		@Override
		public void draw(BattleView view, Graphics g, TileSet tiles) {
			g.drawImage(tiles.getTile(0x10), 0, 160, null);

			int start = view.logMessages.size() - 1 - view.logPage * LOGS_PER_PAGE;
			start = Math.max(0, start);
			
			int y = 200;
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 12);
			for (int i = start; i >= 0 && start - i < LOGS_PER_PAGE; i--, y += 15) {
				g.drawString(view.logMessages.get(i), 25, y);
			}
			
			View.drawArrows(g, view.logLeftButton, view.logRightButton);
			view.logLeftButton.draw(g);
			view.logRightButton.draw(g);

			// Draw Messages Box
			g.drawImage(tiles.getTile(0x20), 415, 440, null);
					
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 40);
			g.drawString("Bob Loblaw's", 440, 500);
			g.drawString("Log Blog", 440, 550);
			
			// Draw back arrow when applicable
			View.drawArrows(g, null, view.backButton);
			view.backButton.draw(g);
		}

		@Override
		public void update(BattleView view) {
			view.selectedButton = Button.update(view.logButtons, view.selectedButton);
			view.backButton.update(false, ControlKey.BACK);
			
			int maxLogPage = view.logMessages.size()/LOGS_PER_PAGE;
			
			if (view.logLeftButton.checkConsumePress()) {
				view.selectedButton = LOG_LEFT_BUTTON;
				view.logRightButton.setForceHover(false);
				view.logPage = Math.max(0, view.logPage - 1);
			}
			
			if (view.logRightButton.checkConsumePress()) {
				view.selectedButton = LOG_RIGHT_BUTTON;
				view.logLeftButton.setForceHover(false);
				view.logPage = Math.min(maxLogPage, view.logPage + 1);
			}
			
			view.logLeftButton.setActive(view.logPage > 0);
			view.logRightButton.setActive(view.logPage < maxLogPage);
			
			if (view.logPage == 0 && maxLogPage > 0) {
				view.selectedButton = LOG_RIGHT_BUTTON;
			}
			else if (view.logPage == maxLogPage) {
				view.selectedButton = LOG_LEFT_BUTTON;
			}
			
			if (view.backButton.checkConsumePress()) {
				view.setVisualState(VisualState.MENU);
			}
		}
	};

	@Override
	public void update(int dt) {
		state.updateVisualState.update(this);
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
		state.updateVisualState.set(this);
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
		
		state.updateVisualState.draw(this, g, tiles);
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
