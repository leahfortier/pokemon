package gui.view;

import gui.Button;
import gui.GameData;
import gui.TileSet;
import item.Bag;
import item.Bag.BattleBagCategory;
import item.Item;
import item.use.PokemonUseItem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import sound.SoundTitle;
import trainer.CharacterData;
import trainer.Trainer;
import trainer.Trainer.Action;
import battle.Battle;
import battle.MessageUpdate;
import battle.MessageUpdate.Update;
import battle.Move;
import battle.effect.Status.StatusCondition;

public class BattleView extends View 
{
	// Menu Button Indexes
	private static final int FIGHT_BUTTON = 0;
	private static final int BAG_BUTTON = 1;
	private static final int SWITCH_BUTTON = 2;
	private static final int RUN_BUTTON = 3;
	
	// Battle Bag Categories
	private static final BattleBagCategory[] bagCategories = BattleBagCategory.values();
	
	// Bag Button Indexes
	private static final int ITEMS = bagCategories.length;
	private static final int ITEMS_PER_PAGE = 10;
	private static final int NUM_BAG_BUTTONS = bagCategories.length + ITEMS_PER_PAGE + 3;
	private static final int LAST_ITEM_BUTTON = NUM_BAG_BUTTONS - 1;
	private static final int BAG_RIGHT_BUTTON = NUM_BAG_BUTTONS - 2;
	private static final int BAG_LEFT_BUTTON = NUM_BAG_BUTTONS - 3;
	
	private static final int LOG_LEFT_BUTTON = 0;
	private static final int LOG_RIGHT_BUTTON = 1;
	private static final int LOGS_PER_PAGE = 23;
	
	// Swicth Button in Pokemon View Button Index
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
	private static final int[][] primaryColorx = {{0, 199, 94, 0}, {0, 191, 104, 0}};
	private static final int[][] primaryColory = {{0, 0, 105, 105}, {0, 0, 88, 88}};
	private static final int[][] secondaryColorx = {{294, 199, 94, 294}, {191, 294, 294, 104}};
	private static final int[][] secondaryColory = {{0, 0, 105, 105}, {0, 0, 88, 88}};
	
	// Polygons for Type Colors in the Pokemon View
	private static final int[] pkmnPrimaryColorx = {0, 349, 5, 0};
	private static final int[] pkmnPrimaryColory = {0, 0, 344, 344};
	private static final int[] pkmnSecondaryColorx = {344, 349, 349, 0};
	private static final int[] pkmnSecondaryColory = {0, 0, 344, 344};
	
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
	private Item selectedItem;
	
	// Current selected tab in Pokemon view and whether or not a switch is forced
	private int selectedPokemonTab;
	private boolean switchForced;
	
	private int logPage;
	private ArrayList<String> logMessages;
	
	private List<Move> selectedMoveList;
	
	// The last move that a Pokemon used
	private int lastMoveUsed;
	
	// Which Pokemon is trying to learn a new move, and which move
	private ActivePokemon learnedPokemon;
	private Move learnedMove;
	
	// Stat gains and corresponding new stat upgrades for leveling up/evolving
	private int[] statGains;
	private int[] newStats;
	
	// Contains the different types of states a battle can be in
	private enum VisualState
	{
		MESSAGE, BAG, INVALID_BAG, FIGHT, INVALID_FIGHT, POKEMON, 
		INVALID_POKEMON, MENU, LEARN_MOVE_QUESTION, LEARN_MOVE_DELETE,
		USE_ITEM, STAT_GAIN, LOG_VIEW
	};
	
	// Handles animation and keeps track of the current state
	private class PokemonAnimationState
	{
		private PokemonState oldState, state;
		private int animationHP, animationEvolve, animationExp, animationCatch;
		private int animationCatchDuration;
		
		public PokemonAnimationState()
		{
			oldState = new PokemonState();
			state = new PokemonState();
		}
		
		private void resetVals(ActivePokemon p)
		{
			resetVals(p.getHP(), p.getStatus().getType(), p.getType(currentBattle), p.isShiny(), p.getPokemonInfo(), 
					p.getName(), p.getMaxHP(), p.getLevel(), p.getGender(), p.expRatio());
		}
		
		// Resets all the values in a state
		private void resetVals(int hp, StatusCondition status, Type[] type, boolean shiny, PokemonInfo pokemon, String name, int maxHP, int level, Gender gender, float expRatio)
		{
			animationHP = 0;
			animationExp = 0;
			animationCatchDuration = 0;
			
			state.hp = oldState.hp = hp;
			state.status = oldState.status = status;
			state.type = type;
			state.shiny = shiny;
			state.imageNumber = pokemon.getImageNumber(state.shiny);
			state.caught = currentBattle.isWildBattle() && currentBattle.getPlayer().getPokedex().caught(pokemon.namesies());
			state.name = name;
			state.maxHp = oldState.maxHp = maxHP;
			state.level = level;
			state.gender = gender;
			state.expRatio = oldState.expRatio = expRatio;
		}
		
		private void startHpAnimation(int newHp) 
		{
			if (newHp == state.hp) return;
			
			oldState.hp = state.hp;
			state.hp = newHp;
			animationHP = (int)(Math.abs(oldState.hp - state.hp)*FRAMES_PER_HP_LOSS);
		}
		
		private void setMaxHP(int newMax)
		{
			state.maxHp = newMax;
		}
		
		private void setStatus(StatusCondition newStatus) 
		{
			oldState.status = state.status;
			state.status = newStatus;
		}
		
		private void setType(Type[] newType) 
		{
			state.type = newType;
		}
		
		private void startPokemonUpdateAnimation(PokemonInfo newPokemon, boolean newShiny, boolean animate) 
		{
			state.shiny = newShiny;
			if (state.imageNumber != 0)
			{
				oldState.imageNumber = state.imageNumber;
				if (animate) animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
			}
			
			state.imageNumber = newPokemon.getImageNumber(state.shiny);
			animationCatchDuration = 0;
		}
		
		private void startCatchAnimation(int duration)
		{
			if (duration == -1)
			{
				animationCatch = CATCH_ANIMATION_LIFESPAN;
				animationCatchDuration = -1;
			}
			else
			{
				animationCatch = animationCatchDuration = duration*CATCH_SHAKE_ANIMATION_LIFESPAN + 2*CATCH_TRANSFORM_ANIMATION_LIFESPAN;
			}
		}
		
		private void startExpAnimation(float newExpRatio, boolean levelUp) 
		{
			oldState.expRatio = levelUp ? 0 : state.expRatio;
			state.expRatio = newExpRatio;
			animationExp = (int)(100*Math.abs(oldState.expRatio - state.expRatio)*FRAMES_PER_HP_LOSS);
		}
		
		private void setLevel(int newLevel) 
		{
			state.level = newLevel;
		}
		
		private void setName(String newName) 
		{
			state.name = newName;
		}
		
		private void setGender(Gender newGender) 
		{
			state.gender = newGender;
		}
		
		private boolean isEmpty() 
		{
			return state.imageNumber == 0;
		}
		
		private boolean isAnimationPlaying()
		{
			return animationHP != 0 || animationEvolve != 0 || animationCatch != 0 || animationExp != 0;
		}
		
		// Draws a string with a shadow behind it the specified location
		private void drawShadowText(Graphics g, String text, int x, int y, int fontSize, boolean rightAligned)
		{
			g.setFont(Global.getFont(fontSize));
			g.setColor(new Color(128, 128, 128, 128));
			
			if (rightAligned) g.drawString(text, Global.rightX(text, x, fontSize), y);
			else g.drawString(text, x, y);
			
			g.setColor(Color.DARK_GRAY);
			
			if (rightAligned) g.drawString(text, Global.rightX(text, x - 2, fontSize), y - 2);
			else g.drawString(text, x - 2, y - 2);
		}
		
		// Draws all of the text inside the status box
		private void drawStatusBoxText(Graphics g, int isEnemy, TileSet tiles, ActivePokemon pokemon)
		{
			// Name, Gender, Level, Status Condition
			drawShadowText(g, state.name + " " + state.gender.getCharacter(), 20, 40, 27, false);
			drawShadowText(g, "Lv" + state.level, 272, 40, 27, true);
			drawShadowText(g, state.status.getName(), 20, 71, 24, false);
			
			// Only the player shows the HP Text
			if (isEnemy == 0)
			{
				// HP Text Animation
				int originalTime = Math.abs(state.hp - oldState.hp)*FRAMES_PER_HP_LOSS;
				String hpStr = state.hp + "/" + state.maxHp;
				if (animationHP > 0)
				{
					hpStr = (int)(state.hp + (oldState.hp - state.hp)*(animationHP/(float)originalTime)) + "/" + state.maxHp;
				}
				
				drawShadowText(g, hpStr, 273, 95, 24, true);
			}
			// Show whether or not the wild Pokemon has already been caught
			else if (state.caught)
			{
				g.drawImage(tiles.getTile(0x4), 296, 40, null);
			}
		}
		
		
		private void catchAnimation(Graphics g, BufferedImage plyrImg, int isEnemy, TileSet pkmTiles, int px, int py)
		{
			Graphics2D g2d = (Graphics2D)g;
			float[] pokeyScales = { 1f, 1f, 1f, 1f };
			float[] pokeyOffsets = { 255f, 255f, 255f, 0f };
			float[] ballScales = { 1f, 1f, 1f, 1f };
			float[] ballOffsets = { 255f, 255f, 255f, 0f };
			
			int xOffset = 0;
			
			int lifespan = animationCatchDuration == -1 ? CATCH_ANIMATION_LIFESPAN : animationCatchDuration;
			
			// Turn white
			if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3)
			{
				pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(1 - (animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - .7f)));
				ballScales[3] = 0;
			}
			// Transform into Pokeball
			else if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7)
		    {
		       pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
		       pokeyScales[3] = ((animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
		       ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
		       ballScales[3] = (1 - (animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
		    }
			// Restore color
			else if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN)
			{
				pokeyScales[3] = 0;
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.3f));
			}
			// Shake
			else if (animationCatchDuration == -1 || animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN)
			{
				pokeyScales[3] = 0;
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 0;
				xOffset = (int)(10*Math.sin(animationCatch/200.0));
			}
			// Turn white -- didn't catch
			else if (animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7)
			{
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(1f - (animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - 0.7f)));
				pokeyScales[3] = 0;
			}
			// Transform into Pokemon
			else if (animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3)
			{
				pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
				pokeyScales[3] = (1 - (animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
				ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
				ballScales[3] = ((animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
			}
			// Restore color
			else
			{
				ballScales[3] = 0;
				pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(animationCatch)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1.0f - .7f));
			}
			
			animationCatch -= Global.MS_BETWEEN_FRAMES;

			BufferedImage pkBall = pkmTiles.getTile(0x11111);

			g2d.drawImage(Global.colorImage(pkBall, ballScales, ballOffsets), px - pkBall.getWidth()/2 + xOffset, py - pkBall.getHeight(), null);
			g2d.drawImage(Global.colorImage(plyrImg, pokeyScales, pokeyOffsets), px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null);
		}
		
		// hi :)
		private void evolveAnimation(Graphics g, BufferedImage plyrImg, int isEnemy, TileSet pkmTiles, int px, int py)
		{
			Graphics2D g2d = (Graphics2D)g;
			
			float[] prevEvolutionScales = { 1f, 1f, 1f, 1f };
			float[] prevEvolutionOffsets = { 255f, 255f, 255f, 0f };
			float[] evolutionScales = { 1f, 1f, 1f, 1f };
			float[] evolutionOffsets = { 255f, 255f, 255f, 0f };
			
			// Turn white
			if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.7)
			{
				prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255*(1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.7f)/(EVOLVE_ANIMATION_LIFESPAN*(1 - 0.7f)));
				evolutionScales[3] = 0;
			}
			// Change form
			else if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.3)
			{
				prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255;
				prevEvolutionScales[3] = ((animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
				evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255;
				evolutionScales[3] = (1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
			}
			// Restore color
			else
			{
				prevEvolutionScales[3] = 0;
				evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255*(animationEvolve)/(EVOLVE_ANIMATION_LIFESPAN*(1-0.7f));
			}
			
			animationEvolve -= Global.MS_BETWEEN_FRAMES;
			
			BufferedImage prevEvo = pkmTiles.getTile(oldState.imageNumber + (isEnemy^1));

			g2d.drawImage(Global.colorImage(plyrImg, evolutionScales, evolutionOffsets), px-plyrImg.getWidth()/2, py-plyrImg.getHeight(), null);
			g2d.drawImage(Global.colorImage(prevEvo, prevEvolutionScales, prevEvolutionOffsets), px-prevEvo.getWidth()/2, py-prevEvo.getHeight(), null);
		}
		
		private void drawHealthBar(Graphics g)
		{
			// Draw the white background of the health bar
			g.setColor(Color.WHITE);
			g.fillRect(108, 53, 315 - 150, 124 - 105);
			
			// Get the ratio based off of the possible animation
			float ratio = state.hp/(float)state.maxHp;
			if (animationHP > 0)
			{
				animationHP -= HP_LOSS_RATIO*state.maxHp + 1;
				int originalTime = (int)(Math.abs(state.hp - oldState.hp)*FRAMES_PER_HP_LOSS);
				ratio = (state.hp + (oldState.hp - state.hp)*(animationHP/(float)originalTime))/(float)state.maxHp;
			}
			else animationHP = 0;
			
			// Set the proper color for the ratio and fill in the health bar as appropriate
			g.setColor(Global.getHPColor(ratio));
			if (animationHP > 0 && (animationHP/10)%2 == 0) g.setColor(g.getColor().darker());
			g.fillRoundRect(113, 57, (int)((312 - 155)*ratio), 119 - 109, 5, 5);
		}
		
		private void drawExpBar(Graphics g)
		{
			// Show the animation
			float expRatio = state.expRatio;
			if (animationExp > 0)
			{
				animationExp -= EXP_LOSS_RATIO;
				int originalTime = (int)(100*Math.abs(state.expRatio - oldState.expRatio)*FRAMES_PER_HP_LOSS);
				expRatio = (state.expRatio + (oldState.expRatio - state.expRatio)*(animationExp/(float)originalTime));
			}
			else animationExp = 0;
			
			// Experience bar background
			g.setColor(new Color(153, 153, 153));
			g.fillRect(36, 107, 294 - 36, 115 - 107); //463,  304
			
			// Experience bar foreground
			g.setColor(Global.EXP_BAR_COLOR);
			g.fillRect(36, 107, (int)((294 - 36)*expRatio), 115 - 107);
		}
		
		// Draws the status box, not including the text
		private void drawStatusBox(Graphics g, int isEnemy, ActivePokemon pokemon, TileSet pkmTiles, int px, int py) //-42 -52 
		{ 
			// Draw the colored type polygons
			Color[] typeColors = Type.getColors(state.type);
			g.setColor(typeColors[0]);
			g.fillPolygon(primaryColorx[isEnemy], primaryColory[isEnemy], 4);
			g.setColor(typeColors[1]);
			g.fillPolygon(secondaryColorx[isEnemy], secondaryColory[isEnemy], 4);
			
			// Draw health bar and player's EXP Bar
			drawHealthBar(g);
			if (isEnemy == 0) drawExpBar(g);
			
			// Draw the Pokemon image if applicable
			if (!isEmpty() && !pokemon.isSemiInvulnerable())
			{
				BufferedImage plyrImg = pkmTiles.getTile(state.imageNumber + (isEnemy^1));
				if (plyrImg != null)
				{
					if (animationEvolve > 0)
					{
						evolveAnimation(g, plyrImg, isEnemy, pkmTiles, px, py);	
					}
					else if (animationCatch > 0)
					{
						catchAnimation(g, plyrImg, isEnemy, pkmTiles, px, py);	
					}
					else
					{
						if (animationCatchDuration == -1) plyrImg = pkmTiles.getTile(0x11111);
						
						g.drawImage(plyrImg, px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null);
						
						animationEvolve = 0;
						animationCatch = 0;
					}
				}
			}
		}
	}
	
	// A class to hold the state of a Pokemon
	private class PokemonState
	{
		private int maxHp, hp, imageNumber, level;
		private String name;
		private StatusCondition status;
		private Type[] type;
		private float expRatio;
		private boolean shiny;
		private boolean caught;
		private Gender gender;
		
		public PokemonState()
		{
			type = new Type[2];
		}
	}
	
	public BattleView()
	{
		playerAnimation = new PokemonAnimationState();
		enemyAnimation = new PokemonAnimationState();
	}
	
	public void setBattle(Battle b)
	{		
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
		update = Update.NONE;
		
		// Back Button
		backButton = new Button(750, 560, 35, 20, null);
		
		// Menu Buttons
		menuButtons = new Button[4];
		menuButtons[FIGHT_BUTTON] = fightBtn = new Button(452, 473, 609 - 452, 515 - 473, Button.HoverAction.ARROW, new int[] {	BAG_BUTTON, SWITCH_BUTTON, RUN_BUTTON, SWITCH_BUTTON});
		menuButtons[BAG_BUTTON] = bagBtn = new Button(628, 473, 724 - 628, 513 - 473, Button.HoverAction.ARROW, new int[] {SWITCH_BUTTON, RUN_BUTTON, FIGHT_BUTTON, RUN_BUTTON});
		menuButtons[SWITCH_BUTTON] = pokemonBtn = new Button(452, 525, 609 - 452, 571 - 525, Button.HoverAction.ARROW, new int[] {RUN_BUTTON, FIGHT_BUTTON, BAG_BUTTON, FIGHT_BUTTON});
		menuButtons[RUN_BUTTON] = runBtn = new Button(628, 525, 724 - 628, 571 - 525, Button.HoverAction.ARROW, new int[] {FIGHT_BUTTON, BAG_BUTTON, SWITCH_BUTTON, BAG_BUTTON});
		
		// Move Buttons
		moveButtons = new Button[Move.MAX_MOVES];
		for (int y = 0, i = 0; y < 2; y++)
		{
			for (int x = 0; x < Move.MAX_MOVES/2; x++, i++)
			{
				moveButtons[i] = new Button(22 + x*190, 440 + 21 + y*62, 183, 55, Button.HoverAction.BOX, 
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
		
		bagTabButtons = new Button[bagCategories.length];
		for (int i = 0; i < bagCategories.length; i++)
		{
			bagButtons[i] = bagTabButtons[i] = new Button(i*89 + 30, 190, 89, 28, Button.HoverAction.BOX, 
					new int[] { (i + 1)%bagCategories.length, // Right
								LAST_ITEM_BUTTON, // Up
								(i - 1 + bagCategories.length)%bagCategories.length, // Left
								ITEMS }); // Down
		}
		
		bagButtons[BAG_LEFT_BUTTON] = bagLeftButton = new Button(135, 435, 35, 20, Button.HoverAction.BOX, new int[] {BAG_RIGHT_BUTTON, ITEMS + ITEMS_PER_PAGE - 2, -1, LAST_ITEM_BUTTON});
		bagButtons[BAG_RIGHT_BUTTON] = bagRightButton = new Button(250,435,35,20, Button.HoverAction.BOX, new int[] {-1, ITEMS + ITEMS_PER_PAGE - 1, BAG_LEFT_BUTTON, LAST_ITEM_BUTTON});
		bagButtons[LAST_ITEM_BUTTON] = bagLastUsedBtn = new Button(214, 517, 148, 28, Button.HoverAction.BOX, new int[] {-1, BAG_LEFT_BUTTON, -1, selectedBagTab});
		
		for (int y = 0, i = ITEMS; y < ITEMS_PER_PAGE/2; y++)
		{
			for (int x = 0; x < 2; x++, i++)
			{
				bagButtons[i] = new Button(55 + x*162, 243 + y*38, 148, 28, Button.HoverAction.BOX, 
						new int[] { (i + 1 - ITEMS)%ITEMS_PER_PAGE + ITEMS, // Right
									y == 0 ? selectedBagTab : i - 2, // Up
									(i - 1 - ITEMS + ITEMS_PER_PAGE)%ITEMS_PER_PAGE + ITEMS, // Left
									y == ITEMS_PER_PAGE/2 - 1 ? (x == 0 ? BAG_LEFT_BUTTON : BAG_RIGHT_BUTTON) : i + 2 }); // Down
			}
		}
		
		// Pokemon Switch View Buttons
		pokemonButtons = new Button[Trainer.MAX_POKEMON + 1];
		
		pokemonTabButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			pokemonButtons[i] = pokemonTabButtons[i] = new Button(32 + i*59, 192, 59, 34, Button.HoverAction.BOX, 
					new int[] { (i + 1)%Trainer.MAX_POKEMON, // Right
								POKEMON_SWITCH_BUTTON, // Up
								(i - 1 + Trainer.MAX_POKEMON)%Trainer.MAX_POKEMON, // Left
								POKEMON_SWITCH_BUTTON }); // Down
		}
		
		pokemonButtons[POKEMON_SWITCH_BUTTON] = pokemonSwitchButton = new Button(55, 509, 141, 36, Button.HoverAction.BOX, new int[]{-1, 0, -1, -1});
	
		logLeftButton = new Button(150, 550, 35, 20, Button.HoverAction.BOX, new int[] {LOG_RIGHT_BUTTON, -1, -1, -1});
		logRightButton = new Button(200, 550, 35, 20, Button.HoverAction.BOX, new int[] {-1, -1, LOG_LEFT_BUTTON, -1});
		logButtons = new Button[] {logLeftButton, logRightButton};
		
		
		currentBattle.getPlayer().clearLogMessages();
	}
	
	// Updates when in the menu state
	public void updateMenu(InputControl input)
	{
		// Update menu buttons
		selectedButton = Button.update(menuButtons, selectedButton, input);
		
		// Show Bag View
		if (bagBtn.checkConsumePress())
		{
			setVisualState(VisualState.BAG);
		}
		// Show Pokemon View
		else if (pokemonBtn.checkConsumePress())
		{
			setVisualState(VisualState.POKEMON);
		}
		// Attempt escape
		else if (runBtn.checkConsumePress())
		{
			setVisualState(VisualState.MESSAGE);
			currentBattle.runAway();
			cycleMessage(false);
		}
		// Show Fight View
		else if (fightBtn.checkConsumePress())
		{
			setVisualState(VisualState.FIGHT);
			
			// Move is forced -- don't show menu, but execute the move
			if (Move.forceMove(currentBattle, currentBattle.getPlayer().front()))
			{
				currentBattle.getPlayer().performAction(currentBattle, Action.FIGHT);
				setVisualState(VisualState.MESSAGE);
				cycleMessage(false);
			}
		}
		else if (input.isDown(Control.L))
		{
			input.consumeKey(Control.L);
			logPage = 0;
			logMessages = currentBattle.getPlayer().getLogMessages();
			
			if (logMessages.size() / LOGS_PER_PAGE > 0)
			{
				selectedButton = LOG_RIGHT_BUTTON;
				logRightButton.setActive(true);
				selectedButton = Button.update(logButtons, selectedButton, input);
			}
			else
			{
				logRightButton.setActive(false);
			}
			
			logLeftButton.setActive(false);
			setVisualState(VisualState.LOG_VIEW);
		}
	}
	
	// Updates when in fight mode (selecting a move menu)
	private void updateFight(InputControl input)
	{
		// Update move buttons and the back button
		selectedButton = Button.update(moveButtons, selectedButton, input);
		backButton.update(input, false, Control.BACK);
		
		// Get the Pokemon that is attacking and their corresponsing move list
		ActivePokemon front = currentBattle.getPlayer().front();
		
		for (int i = 0; i < selectedMoveList.size(); i++)
		{
			if (moveButtons[i].checkConsumePress())
			{
				lastMoveUsed = i;
				
				// Execute the move if valid
				if (Move.validMove(currentBattle, front, selectedMoveList.get(i), true))
				{
					currentBattle.getPlayer().performAction(currentBattle, Action.FIGHT);
					setVisualState(VisualState.MESSAGE);
					cycleMessage(false);
				}
				// An invalid move -- Don't let them select it
				else
				{
					cycleMessage(false);
					setVisualState(VisualState.INVALID_FIGHT);
				}
			}
		}
		
		// Return to main battle menu
		if (backButton.checkConsumePress())
		{
			setVisualState(VisualState.MENU);
		}
	}
	
	private void updateMessage(InputControl input)
	{
		boolean pressed = false;
		
		// Consume input for mouse clicks and spacebars
		if (input.mouseDown)
		{
			pressed = true;
			input.consumeMousePress();
		}
		if (input.isDown(Control.SPACE))
		{
			pressed = true;
			input.consumeKey(Control.SPACE);
		}
		
		// Don't go to the next message if an animation is playing 
		if (pressed && message != null && !playerAnimation.isAnimationPlaying() && !enemyAnimation.isAnimationPlaying())
		{
			if (state == VisualState.STAT_GAIN) setVisualState(VisualState.MESSAGE);
			cycleMessage(false);
		}
	}
	
	// Handles updates for the bag view
	private void updateBag(InputControl input)
	{
		// Update all bag buttons and the back button
		selectedButton = Button.update(bagButtons, selectedButton, input);
		backButton.update(input, false, Control.BACK);
		
		// Check tabs
		for (int i = 0; i < bagCategories.length; i++)
		{
			if (bagTabButtons[i].checkConsumePress())
			{
				bagPage = 0;
				selectedBagTab = i;
				setVisualState(state); // To update active buttons
			}
		}
		
		CharacterData player = currentBattle.getPlayer();
		Bag bag = player.getBag();
		Set<Item> toDraw = bag.getCategory(bagCategories[selectedBagTab]);
		Iterator<Item> iter = toDraw.iterator();
		
		for (int i = 0; i < bagPage*ITEMS_PER_PAGE; i++) iter.next();
		for (int i = ITEMS; i < ITEMS + ITEMS_PER_PAGE && iter.hasNext(); i++)
		{
			Item item = iter.next();
			if (bagButtons[i].checkConsumePress())
			{				
				// Pokemon Use Item -- Set item to be selected an change to Pokemon View
				if (item instanceof PokemonUseItem)
				{
					selectedItem = item;
					setVisualState(VisualState.USE_ITEM);
					break;
				}
				// Otherwise, just use it on the battle if successful
				else if (bag.battleUseItem(item, currentBattle.getPlayer().front(), currentBattle))
				{
					currentBattle.getPlayer().performAction(currentBattle, Action.ITEM);
					setVisualState(VisualState.MENU);
					cycleMessage(false);
					break;
				}
				// If the item cannot be used, do not consume
				else
				{
					cycleMessage(false);
					setVisualState(VisualState.INVALID_BAG);
				}
			}
		}
		
		// Selecting the Last Item Used Button
		if (bagLastUsedBtn.checkConsumePress())
		{
			Item lastItemUsed = bag.getLastUsedItem();
			if (lastItemUsed != Item.getItem(Namesies.NONE_ITEM) && bag.battleUseItem(lastItemUsed, player.front(), currentBattle))
			{
				player.performAction(currentBattle, Action.ITEM);
				setVisualState(VisualState.MENU);
				cycleMessage(false);
			}
			else
			{
				cycleMessage(false);
				setVisualState(VisualState.INVALID_BAG);
			}
		}
		
		// Next page
		if (bagRightButton.checkConsumePress())
		{
			if (bagPage == ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1)) bagPage = 0;
			else bagPage++;
			
			setVisualState(state); // To update active buttons
		}
		
		// Previous Page
		if (bagLeftButton.checkConsumePress())
		{
			if (bagPage == 0) bagPage = ((int)Math.ceil(toDraw.size()/(double)ITEMS_PER_PAGE) - 1);
			else bagPage--;
			
			setVisualState(state); // To update active buttons
		}
		
		// Return to main battle menu
		if (backButton.checkConsumePress())
		{
			setVisualState(VisualState.MENU);
		}
	}
	
	private void updatePokemon(InputControl input)
	{
		// Update the buttons
		selectedButton = Button.update(pokemonButtons, selectedButton, input);
		backButton.update(input, false, Control.BACK);
		
		CharacterData player = currentBattle.getPlayer();
		List<ActivePokemon> list = player.getTeam();
		for (int i = 0; i < list.size(); i++)
		{
			if (pokemonTabButtons[i].checkConsumePress())
			{
				selectedPokemonTab = i;
				setVisualState(state); //to update active buttons
			}
		}
		
		// Switch Switch Switcheroo
		if (pokemonSwitchButton.checkConsumePress())
		{
			ActivePokemon selectedPkm = list.get(selectedPokemonTab);
			
			// Use an item on this Pokemon instead of switching
			if (state == VisualState.USE_ITEM)
			{
				// Valid item
				if (player.getBag().battleUseItem(selectedItem, selectedPkm, currentBattle))
				{
					player.performAction(currentBattle, Action.ITEM);
					setVisualState(VisualState.MENU);
					cycleMessage(false);
				}
				// Invalid item
				else
				{
					cycleMessage(false);
					setVisualState(VisualState.INVALID_BAG);
				}
			}
			// Actual switcheroo
			else
			{
				if (player.canSwitch (currentBattle, selectedPokemonTab))
				{
					player.setFront(selectedPokemonTab);
					currentBattle.enterBattle(player.front());
					
					if (!switchForced) player.performAction(currentBattle, Action.SWITCH);
					
					cycleMessage(false);
					switchForced = false;
					lastMoveUsed = 0;
				}
				else
				{
					cycleMessage(false);
					setVisualState(VisualState.INVALID_POKEMON);
				}
			}
		}

		// Return to main menu if applicable
		if (backButton.checkConsumePress())
		{
			if (!switchForced) setVisualState(VisualState.MENU);
		}
	}
	
	private void updateLearnMoveQuestion(InputControl input)
	{
		yesButton.update(input);
		noButton.update(input);
		
		if (noButton.checkConsumePress())
		{
			// This is all done really silly, so we need to do this
			ArrayDeque<MessageUpdate> messages = currentBattle.getMessages();
			MessageUpdate message = messages.poll();
			for (int i = 0; i < Move.MAX_MOVES + 1; i++) messages.poll();
			messages.push(message);
			
			setVisualState(VisualState.MESSAGE);
			cycleMessage(false);
		}
		
		if (yesButton.checkConsumePress())
		{
			setVisualState(VisualState.LEARN_MOVE_DELETE);
		}
	}
	
	private void updateLearnMoveDelete(InputControl input)
	{
		selectedButton = Button.update(moveButtons, selectedButton, input);
		newMoveButton.update(input);
		
		for (int i = 0; i < moveButtons.length; i++)
		{
			if (moveButtons[i].checkConsumePress())
			{
				learnedPokemon.addMove(currentBattle, learnedMove, i);
				
				// This is all done really silly, so we need to do this
				ArrayDeque<MessageUpdate> messages = currentBattle.getMessages();
				MessageUpdate message = messages.poll();
				for (int j = 0; j < Move.MAX_MOVES; j++)
				{
					if (j == i) message = messages.poll();
					else messages.poll();
				}
				messages.push(message);
				
				setVisualState(VisualState.MESSAGE);
				cycleMessage(false);
			}
		}
		
		if (newMoveButton.checkConsumePress())
		{
			// This is all done really silly, so we need to do this
			ArrayDeque<MessageUpdate> messages = currentBattle.getMessages();
			MessageUpdate message = messages.poll();
			for (int i = 0; i < Move.MAX_MOVES + 1; i++) messages.poll();
			messages.push(message);
			
			setVisualState(VisualState.MESSAGE);
			cycleMessage(false);
		}
	}
	
	public void updateLog(InputControl input)
	{
		selectedButton = Button.update(logButtons, selectedButton, input);
		//logLeftButton.update(input);
		//logRightButton.update(input);
		backButton.update(input, false, Control.BACK);
		
		int maxLogPage = logMessages.size() / LOGS_PER_PAGE;
		
		if (logLeftButton.checkConsumePress())
		{
			selectedButton = LOG_LEFT_BUTTON;
			logRightButton.setForceHover(false);
			logPage = Math.max(0, logPage - 1);
		}
		if (logRightButton.checkConsumePress())
		{
			selectedButton = LOG_RIGHT_BUTTON;
			logLeftButton.setForceHover(false);
			logPage = Math.min(maxLogPage, logPage + 1);
			
		}
		
		logLeftButton.setActive(logPage > 0);
		logRightButton.setActive(logPage < maxLogPage);
		
		if (logPage == 0 && maxLogPage > 0)
			selectedButton = LOG_RIGHT_BUTTON;
		else if (logPage == maxLogPage)
			selectedButton = LOG_LEFT_BUTTON;
		
		if (backButton.checkConsumePress())
		{
			setVisualState(VisualState.MENU);
		}
	}

	public void update(int dt, InputControl input, Game game) 
	{
		switch (state)
		{
			case MESSAGE:
			case STAT_GAIN:
				updateMessage(input);
				break;
			case MENU:
				updateMenu(input);
				break;
			case FIGHT:
			case INVALID_FIGHT:
				updateFight(input);
				break;			
			case BAG:
			case INVALID_BAG:
				updateBag(input);
				break;
			case POKEMON:
			case INVALID_POKEMON:
			case USE_ITEM:
				updatePokemon(input);
				break;
			case LEARN_MOVE_QUESTION:
				updateLearnMoveQuestion(input);
				break;
			case LEARN_MOVE_DELETE:
				updateLearnMoveDelete(input);
				break;
			case LOG_VIEW:
				updateLog(input);
				break;
		}
		
		switch (update)
		{
			case LEARN_MOVE:
				setVisualState(VisualState.LEARN_MOVE_QUESTION);
				update = Update.NONE;
				break;
			case STAT_GAIN:
				setVisualState(VisualState.STAT_GAIN);
				update = Update.NONE;
				break;
			case FORCE_SWITCH:
				setVisualState(VisualState.POKEMON);
				switchForced = true;
				update = Update.NONE;
				break;
			case PROMPT_SWITCH:
				setVisualState(VisualState.POKEMON);
				update = Update.NONE;
				break;
			case EXIT_BATTLE:
				game.setViewMode(ViewMode.MAP_VIEW);
				update = Update.NONE;
				break;	
			case WIN_BATTLE:
				
				if(currentBattle.isWildBattle())
				{
					Global.soundPlayer.playMusic(SoundTitle.WILD_POKEMON_DEFEATED);
				}
				else
				{
					// TODO: Get trainer win music
					Global.soundPlayer.playMusic(SoundTitle.TRAINER_DEFEATED);
				}
				
				break;
			default:
				break;				
		}
	}
	
	private void setVisualState(VisualState newState)
	{
		if (state != newState) selectedButton = 0;
		state = newState;
		
		// Update the buttons that should be active
		switch (state)
		{
			case BAG:
			case INVALID_BAG:
				int pageSize = currentBattle.getPlayer().getBag().getCategory(bagCategories[selectedBagTab]).size();
				
				for (int i = 0; i < ITEMS_PER_PAGE; i++)
					bagButtons[ITEMS + i].setActive(i < pageSize - bagPage*ITEMS_PER_PAGE);
				
				bagLastUsedBtn.setActive(currentBattle.getPlayer().getBag().getLastUsedItem() != Item.getItem(Namesies.NONE_ITEM));
				
				for (Button b: bagButtons)
					b.setForceHover(false);
				
				break;
			case FIGHT:
			case INVALID_FIGHT:
				selectedButton = lastMoveUsed;
				selectedMoveList = currentBattle.getPlayer().front().getMoves(currentBattle);
				for (int i = 0; i < Move.MAX_MOVES; i++) 
					moveButtons[i].setActive(i < selectedMoveList.size());
				
				for (Button b: moveButtons)
					b.setForceHover(false);
				break;
			case POKEMON:
			case INVALID_POKEMON:
			case USE_ITEM:
				List<ActivePokemon> list = currentBattle.getPlayer().getTeam();
				for (int i = 0; i < pokemonTabButtons.length; i++) 
					pokemonTabButtons[i].setActive(i < list.size());
				
				if (state != VisualState.USE_ITEM) 
					pokemonSwitchButton.setActive(list.get(selectedPokemonTab).canFight());
				
				for (Button b: pokemonButtons) 
					b.setForceHover(false);
				
				break;
			case MENU:
				for (Button b: menuButtons)
					b.setForceHover(false);
				
				break;
			case LOG_VIEW:
			default:
				break;
		}
	}
	
	private void cycleMessage(boolean updated)
	{
		if (!updated) setVisualState(VisualState.MENU);
		
		Queue<MessageUpdate> messages = currentBattle.getMessages();
		
		if (!messages.isEmpty())
		{
			if (updated && messages.peek().getMessage().length() != 0) return;
			
			MessageUpdate newMessage = messages.remove();
			currentBattle.getPlayer().addLogMessage(newMessage);
			
			PokemonAnimationState state = newMessage.target() ? playerAnimation : enemyAnimation;
			if (newMessage.switchUpdate())
			{
				state.resetVals(newMessage.getHP(), newMessage.getStatus(), newMessage.getType(), newMessage.getShiny(), 
						newMessage.getPokemon(), newMessage.getName(), newMessage.getMaxHP(), newMessage.getLevel(), 
						newMessage.getGender(), newMessage.getEXPRatio());
			}
			else
			{
				if (newMessage.healthUpdate())
				{
					state.startHpAnimation(newMessage.getHP());
				}
				if (newMessage.maxHealthUpdate())
				{
					state.setMaxHP(newMessage.getMaxHP());
				}
				if (newMessage.statusUpdate())
				{
					state.setStatus(newMessage.getStatus());
				}
				if (newMessage.typeUpdate())
				{
					state.setType(newMessage.getType());
				}
				if (newMessage.catchUpdate())
				{
					state.startCatchAnimation(newMessage.getDuration() == -1? -1 : newMessage.getDuration());
				}
				if (newMessage.pokemonUpdate())
				{
					state.startPokemonUpdateAnimation(newMessage.getPokemon(), newMessage.getShiny(), newMessage.isAnimate());
				}
				if (newMessage.hasUpdateType())
				{
					update = newMessage.getUpdateType();
				}
				if (newMessage.expUpdate())
				{
					state.startExpAnimation(newMessage.getEXPRatio(), newMessage.levelUpdate());
				}
				if (newMessage.levelUpdate())
				{
					Global.soundPlayer.playSoundEffect(SoundTitle.LEVEL_UP);
					state.setLevel(newMessage.getLevel());
				}
				if (newMessage.nameUpdate())
				{
					state.setName(newMessage.getName());
				}
				if (newMessage.genderUpdate())
				{
					state.setGender(newMessage.getGender());
				}
				if (newMessage.learnMove())
				{
					learnedMove = newMessage.getMove();
					learnedPokemon = newMessage.getActivePokemon();
				}
				if (newMessage.gainUpdate())
				{
					newStats = newMessage.getNewStats();
					statGains = newMessage.getGain();
				}	
			}
			
			if (newMessage.getMessage().length() == 0)
			{
				cycleMessage(updated);
			}
			else
			{
				message = newMessage.getMessage();
				setVisualState(VisualState.MESSAGE);
				cycleMessage(true);
			}
		}
		else if (!updated)
		{
			message = null;
		}
	}
	
	private void drawBag(Graphics g, GameData data, TileSet tiles)
	{
		g.drawImage(tiles.getTile(0x10), 0, 160, null);
		g.drawImage(tiles.getTile(bagCategories[selectedBagTab].getImageNumber()), 30, 190, null);
		g.drawImage(tiles.getTile(bagCategories[selectedBagTab].getImageNumber() - 4), 30, 492, null);
		g.drawImage(tiles.getTile(0x20), 415, 440, null);
		
		Set<Item> toDraw = currentBattle.getPlayer().getBag().getCategory(bagCategories[selectedBagTab]);
		TileSet itemTiles = data.getItemTiles();

		g.setFont(Global.getFont(12));
		Iterator<Item> iter = toDraw.iterator();
		for (int i = 0; i < bagPage*ITEMS_PER_PAGE; i++) iter.next();
		for (int y = 0; y < ITEMS_PER_PAGE/2; y++)
		{
			for (int x = 0; x < 2 && iter.hasNext(); x++)
			{
				int dx = 55 + x*162, dy = 243 + y*38; 
				g.translate(dx, dy);
				
				// Draw box
				g.drawImage(tiles.getTile(0x11), 0, 0, null);
				
				// Draw item image
				Item i = iter.next();
				BufferedImage img = itemTiles.getTile(i.getIndex());
				g.drawImage(img, 14 - img.getWidth()/2, 14 - img.getHeight()/2, null);

				// Item name
				g.drawString(i.getName(), 28, 19);
				
				// Item quantity
				String countStr = "x" + currentBattle.getPlayer().getBag().getQuantity(i);
				g.drawString(countStr, 140 - countStr.length()*6, 19);
				
				g.translate(-dx, -dy);
			}
		}
		
		// Bag page number
		g.setFont(Global.getFont(20));
		String pageStr = (bagPage + 1) + "/" + Math.max(1, (int)Math.ceil(toDraw.size()/10.0));
		g.drawString(pageStr, 210 - 11*pageStr.length()/2, 450);
		
		// Left/Right Arrows
		View.drawArrows(g, bagLeftButton, bagRightButton);
		
		// Last Item Used
		Bag bag = currentBattle.getPlayer().getBag();
		Item lastUsedItem = bag.getLastUsedItem();
		if (lastUsedItem != Item.getItem(Namesies.NONE_ITEM))
		{
			g.translate(214, 517);
			g.setFont(Global.getFont(12));
			g.drawImage(tiles.getTile(0x11), 0, 0, null);
			
			BufferedImage img = itemTiles.getTile(lastUsedItem.getIndex());
			g.drawImage(img, 14 - img.getWidth()/2, 14 - img.getHeight()/2, null);

			g.drawString(lastUsedItem.getName(), 28, 19);
			
			String countStr = "x" + bag.getQuantity(lastUsedItem);
			g.drawString(countStr, 140 - countStr.length()*6, 19);

			g.translate(-214, -517);
		}
		
		// Message text
		String msgLine = state == VisualState.INVALID_BAG && message != null ? message : "Choose an item!";
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(30));
		Global.drawWrappedText(g, msgLine, 440, 485, 350);
		
		// Back Arrow
		View.drawArrows(g, null, backButton);
		
		for (Button b: bagButtons) b.draw(g);
		backButton.draw(g);
	}
	
	private void drawFight(Graphics g, TileSet tiles, ActivePokemon plyr)
	{
		g.drawImage(tiles.getTile(0x20), 415, 440, null);
		g.drawImage(tiles.getTile(0x21), 0, 440, null);
		
		List<Move> moves = plyr.getMoves(currentBattle);
		for (int y = 0, i = 0; y < 2; y++)
		{
			for (int x = 0; x < Move.MAX_MOVES/2 && i < moves.size(); x++, i++)
			{
				int dx = 22 + x*190, dy = 440 + 21 + y*62;
				g.translate(dx, dy);
				
				Move move = moves.get(i);
				g.setColor(move.getAttack().getActualType().getColor());
				g.fillRect(0, 0, 183, 55);
				g.drawImage(tiles.getTile(0x22), 0, 0, null);
				
				g.setColor(Color.BLACK);
				g.setFont(Global.getFont(22));
				g.drawString(move.getAttack().getName(), 10, 26);
				
				g.setFont(Global.getFont(18));
				String ppStr = "PP: " + move.getPP() + "/" + move.getMaxPP();
				g.drawString(ppStr, 170 - ppStr.length()*10, 45);
				
				BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
				g.drawImage(categoryImage, 12, 32, null);
				
				g.translate(-dx, -dy);
			}
		}
		
		String msgLine = state == VisualState.INVALID_FIGHT && message != null ? message : "Select a move!";
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(30));
		Global.drawWrappedText(g, msgLine, 440, 485, 350);
		
		View.drawArrows(g, null, backButton);
		
		for (int i = 0; i < Move.MAX_MOVES && i < moves.size(); i++) 
			moveButtons[i].draw(g);
		backButton.draw(g);
	}
	
	private void drawMenu(Graphics g, TileSet tiles, ActivePokemon plyr)
	{
		g.drawImage(tiles.getTile(0x3), 0, 439, null);
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		
		g.setFont(Global.getFont(30));
		g.setColor(Color.WHITE);
		
		Global.drawWrappedText(g, "What will " + plyr.getName() + " do?", 20, 485, 400);
		for (Button b: menuButtons)
			b.draw(g);
	}
	
	private void drawStatGain(Graphics g, TileSet tiles) 
	{
		g.drawImage(tiles.getTile(0x5), 0, 280, null);
		g.setColor(Color.BLACK);
		for (int i = 0; i < Stat.NUM_STATS; i++)
		{
			g.setFont(Global.getFont(16));
			g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);
			
			String s = (statGains[i] < 0 ? "" : " + ") + statGains[i];
			g.drawString(s, Global.rightX(s, 206, 16), 314 + i*21);
			
			s = newStats[i] + "";
			g.drawString(s, Global.rightX(s, 247, 16), 314 + i*21);
		}
	}
	
	private void drawMessage(Graphics g, TileSet tiles)
	{
		g.drawImage(tiles.getTile(0x3), 0, 439, null);
		g.setColor(Color.white);
		g.setFont(Global.getFont(30));
		Global.drawWrappedText(g, message, 30, 490, 720);
	}
	
	private void drawPokemonView(Graphics g, GameData data, TileSet tiles)
	{
		// Draw Background
		g.drawImage(tiles.getTile(0x10), 0, 160, null);
		
		// Get current Pokemon
		List<ActivePokemon> list = currentBattle.getPlayer().getTeam();
		ActivePokemon selectedPkm = list.get(selectedPokemonTab);
		
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
		
		// Draw Message Box
		g.drawImage(tiles.getTile(0x20), 415, 440, null);
		
		// Draw Box Outlines for Pokemon Info
		if (!selectedPkm.canFight()) // Fainted Pokemon and Eggs
		{
			g.drawImage(tiles.getTile(0x35), 30, 224, null);
			g.drawImage(tiles.getTile(0x31), 55, 249, null);
		}
		else
		{
			g.drawImage(tiles.getTile(0x34), 30, 224, null);
			g.drawImage(tiles.getTile(0x30), 55, 249, null);
		}
		
		if (selectedPkm.isEgg())
		{
			// Name
			g.setFont(Global.getFont(16));
			g.setColor(Color.BLACK);
			String nameStr = selectedPkm.getName();
			g.drawString(nameStr, 62, 269);
			
			// Description
			g.setFont(Global.getFont(14));
			String eggyMessage = selectedPkm.getEggMessage();
			Global.drawWrappedText(g, eggyMessage, 62, 288, 306, 8, 15);
		}
		else
		{
			// Name and Gender
			g.setFont(Global.getFont(16));
			g.setColor(Color.BLACK);
			String nameStr = selectedPkm.getName() + " " + selectedPkm.getGender().getCharacter();
			g.drawString(nameStr, 62, 269);
			
			// Status Condition
			String statusStr = selectedPkm.getStatus().getType().getName();
			g.drawString(statusStr, 179, 269);
			
			// Level
			String levelStr = "Lv" + selectedPkm.getLevel();
			g.drawString(levelStr, 220, 269);
			
			// Draw type tiles
			if (type[1] == Type.NONE)
			{
				g.drawImage(tiles.getTile(type[0].getImageIndex()), 322, 255, null);
			}
			else
			{
				g.drawImage(tiles.getTile(type[0].getImageIndex()), 285, 255, null);
				g.drawImage(tiles.getTile(type[1].getImageIndex()), 322, 255, null);
			}
			
			// Ability
			g.setFont(Global.getFont(14));
			String abilityStr = selectedPkm.getActualAbility().getName();
			g.drawString(abilityStr, 62, 288);
			
			// Experience
			g.drawString("EXP", 220, 288);
			
			String expStr = "" + selectedPkm.getTotalEXP();
			g.drawString(expStr, Global.rightX(expStr, 352, 14), 288);
			
			String heldItemStr = selectedPkm.getActualHeldItem().getName();
			g.drawString(heldItemStr, 62, 307);
			
			g.drawString("To Next Lv", 220, 307);
			String toNextLvStr = "" + selectedPkm.expToNextLevel();
			g.drawString(toNextLvStr, Global.rightX(toNextLvStr, 352, 14), 307);
			
			// Experience Bar
			float expRatio = selectedPkm.expRatio();
			g.setColor(Global.EXP_BAR_COLOR);
			g.fillRect(222, 315, (int)(137*expRatio), 10);
			
			// HP Bar
			g.setColor(selectedPkm.getHPColor());
			g.fillRect(57, 341, (int)(137*selectedPkm.getHPRatio()), 10);
			
			// Write stat names
			g.setFont(Global.getFont(16));
			for (int i = 0; i < Stat.NUM_STATS; i++)
			{
				g.setColor(selectedPkm.getNature().getColor(i));
				g.drawString(Stat.getStat(i, false).getShortName(), 62, 21*i + 372);
			}
			
			// Write stat values
			g.setColor(Color.BLACK);
			g.setFont(Global.getFont(14));
			
			int[] statsVal = selectedPkm.getStats();
			for (int i = 0; i < Stat.NUM_STATS; i++)
			{
				String valStr = "" + statsVal[i];
				if (i == Stat.HP.index()) valStr = selectedPkm.getHP() + "/" + statsVal[i];
				g.drawString(valStr, 188 - valStr.length()*8, 21*i + 372);
			}
			
			// Draw Move List
			g.setFont(Global.getFont(14));
			List<Move> movesList = selectedPkm.getActualMoves();
			for (int i = 0; i < movesList.size(); i++)
			{
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
				String ppStr = "PP: " + move.getPP() + "/" + move.getMaxPP();
				g.drawString(ppStr, 118 - ppStr.length()*8, 33);
				
				BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
				g.drawImage(categoryImage, 7, 21, null);
				g.translate(-dx, -dy);
			}
		}

		// Draw Switch/Use text
		g.setFont(Global.getFont(20));
		if (state == VisualState.USE_ITEM) g.drawString("Use!", 103, 533);
		else g.drawString("Switch!", 93, 533);
		
		// Draw tabs
		TileSet partyTiles = data.getPartyTiles();
		for (int i = 0; i < list.size(); i++)
		{
			ActivePokemon pkm = list.get(i);
			
			// Draw tab
			g.setColor(pkm.getActualType()[0].getColor());
			g.fillRect(32 + i*59, 192, 59, 34);
			if (i == selectedPokemonTab) g.drawImage(tiles.getTile(0x36), 30 + i*59, 190, null);
			else g.drawImage(tiles.getTile(0x33), 30 + i*59, 190, null);
			
			// Draw Pokemon Image
			BufferedImage img = partyTiles.getTile(pkm.getPokemonInfo().getNumber());
			if (pkm.isEgg()) img = partyTiles.getTile(0x10000);
			g.drawImage(img, 60 + i*59 - img.getWidth()/2, 205 - img.getHeight()/2, null);

			// Fade out fainted Pokemon
			if (!pkm.canFight())
			{
				g.setColor(new Color(0, 0, 0, 128));
				g.fillRect(32 + i*59, 192, 59, 34);
			}
		}
		
		// Draw Message
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(30));
		String msgLine = state == VisualState.INVALID_POKEMON && message != null ? message : "Select a Pok\u00e9mon!";
		Global.drawWrappedText(g, msgLine, 440, 485, 350);
		
		// Draw back arrow when applicable
		if (!switchForced)
		{
			View.drawArrows(g, null, backButton);
		}

		for (int i = 0; i < list.size(); i++)
			pokemonTabButtons[i].draw(g);
		
		if (state == VisualState.USE_ITEM || selectedPkm.canFight())
			pokemonSwitchButton.draw(g);
		
		backButton.draw(g);
	}

	private void drawLearnMoveQuestion(Graphics g, TileSet tiles)
	{
		g.drawImage(tiles.getTile(0x3), 0, 439, null);
		g.setColor(Color.white);
		g.setFont(Global.getFont(25));
		g.drawString("Delete a move in order to learn " + learnedMove.getAttack().getName() + "?", 30, 490);
		
		g.translate(yesButton.x, yesButton.y);
		
		g.setColor(Color.GREEN);
		g.fillRect(0, 0, 183, 55);
		g.drawImage(tiles.getTile(0x22), 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(22));
		g.drawString("Yes", 10, 26);
		
		g.translate(-yesButton.x, -yesButton.y);
		
		g.translate(noButton.x, noButton.y);
		
		g.setColor(Color.RED);
		g.fillRect(0, 0, 183, 55);
		g.drawImage(tiles.getTile(0x22), 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(22));
		g.drawString("No", 10, 26);
		
		g.translate(-noButton.x, -noButton.y);
		
		yesButton.draw(g);
		noButton.draw(g);
	}
	
	private void drawLearnMoveDelete(Graphics g, TileSet tiles)
	{
		g.drawImage(tiles.getTile(0x3), 0, 439, null);
		
		List<Move> moves = learnedPokemon.getActualMoves();
		for (int y = 0, i = 0; y < 2; y++)
		{
			for (int x = 0; x < Move.MAX_MOVES/2 && i < moves.size(); x++, i++)
			{
				int dx = 22 + x*190, dy = 440 + 21 + y*62;
				g.translate(dx, dy);
				
				Move move = moves.get(i);
				g.setColor(move.getAttack().getActualType().getColor());
				g.fillRect(0, 0, 183, 55);
				g.drawImage(tiles.getTile(0x22), 0, 0, null);
				
				g.setColor(Color.BLACK);
				g.setFont(Global.getFont(22));
				g.drawString(move.getAttack().getName(), 10, 26);
				
				g.setFont(Global.getFont(18));
				String ppStr = "PP: " + move.getPP() + "/" + move.getMaxPP();
				g.drawString(ppStr, 170 - ppStr.length()*10, 45);
				
				BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
				g.drawImage(categoryImage, 12, 32, null);
				
				g.translate(-dx, -dy);
			}
		}
		
		g.translate(newMoveButton.x, newMoveButton.y);
		Move move = learnedMove;
		Color boxColor = move.getAttack().getActualType().getColor();
		g.setColor(boxColor);
		g.fillRect(0, 0, 183, 55);
		g.drawImage(tiles.getTile(0x22), 0, 0, null);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(22));
		g.drawString(move.getAttack().getName(), 10, 26);
		
		g.setFont(Global.getFont(18));
		String ppStr = "PP: " + move.getPP() + "/" + move.getMaxPP();
		g.drawString(ppStr, 170 - ppStr.length()*10, 45);
		
		BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
		g.drawImage(categoryImage, 12, 32, null);
		
		g.translate(-newMoveButton.x, -newMoveButton.y);
		
		String msgLine = "Select a move to delete!";
		
		g.setColor(Color.WHITE);
		g.setFont(Global.getFont(25));
		g.drawString(msgLine, newMoveButton.x, 485);
		
		for (int i = 0; i < moves.size(); i++) 
			moveButtons[i].draw(g);
		
		newMoveButton.draw(g);
	}
	
	public void drawLog(Graphics g, TileSet tiles, CharacterData characterData)
	{
		g.drawImage(tiles.getTile(0x10), 0, 160, null);

		int start = logMessages.size() - 1 - logPage * LOGS_PER_PAGE;
		start = Math.max(0, start);
		
		int y = 200;
		g.setColor(Color.WHITE);
		g.setFont(Global.getFont(12));
		for (int i = start; i >= 0 && start - i < LOGS_PER_PAGE; i--, y += 15)
			g.drawString(logMessages.get(i), 25, y);
		
		View.drawArrows(g, logLeftButton, logRightButton);
		logLeftButton.draw(g);
		logRightButton.draw(g);

		// Draw Message Box
		g.drawImage(tiles.getTile(0x20), 415, 440, null);
				
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(40));
		g.drawString("Bob Loblaw's", 440, 500);
		g.drawString("Log Blog", 440, 550);
		
		// Draw back arrow when applicable
		View.drawArrows(g, null, backButton);
		backButton.draw(g);
	}
	
	public void draw(Graphics g, GameData data) 
	{
		Dimension d = Global.GAME_SIZE;
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, d.width, d.height);
		
		ActivePokemon plyr = currentBattle.getPlayer().front();
		ActivePokemon enmy = currentBattle.getOpponent().front();
		TileSet tiles = data.getBattleTiles();
		
		g.drawImage(tiles.getTile(0x100), 0, 0, null);		

		if (playerAnimation.isEmpty())
		{
			if (enemyAnimation.isEmpty())
			{
				g.setClip(0, 440, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			}
			else
			{
				g.setClip(0, 0, Global.GAME_SIZE.width, 250);
			}
		}
		else if (enemyAnimation.isEmpty())
		{
			g.setClip(0, 250, Global.GAME_SIZE.width, 440);
		}
		
		// Draw Status Box Backgrounds
		g.translate(42, 52);
		enemyAnimation.drawStatusBox(g, 1, enmy, data.getPokemonTilesMedium(), 565, 185);
		g.translate(-42, -52);

		g.translate(463,  304);
		playerAnimation.drawStatusBox(g, 0, plyr, data.getPokemonTilesLarge(), 190 - 463, 412 - 304);
		g.translate(-463, -304);

		// Draw Status Box Foregrounds
		g.drawImage(tiles.getTile(1), 0, 0, null);
		
		// Draw Status Box Text
		g.translate(42,  52);
		enemyAnimation.drawStatusBoxText(g, 1, tiles, enmy);
		g.translate(-42, -52);
		
		g.translate(463,  304);
		playerAnimation.drawStatusBoxText(g, 0, tiles, plyr);
		g.translate(-463, -304);
		
		g.setClip(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		
		switch (state)
		{
			case BAG:
			case INVALID_BAG:
				drawBag(g, data, tiles);
				break;
			case FIGHT:
			case INVALID_FIGHT:
				drawFight(g, tiles, plyr);
				break;
			case MENU:
				drawMenu(g, tiles, plyr);
				break;
			case STAT_GAIN:
				drawStatGain(g, tiles);
			case MESSAGE:
				drawMessage(g, tiles);
				break;
			case POKEMON:
			case INVALID_POKEMON:
			case USE_ITEM:
				drawPokemonView(g, data, tiles);
				break;
			case LEARN_MOVE_QUESTION:
				drawLearnMoveQuestion(g, tiles);
				break;
			case LEARN_MOVE_DELETE:
				drawLearnMoveDelete(g, tiles);
				break;
			case LOG_VIEW:
				drawLog(g, tiles, currentBattle.getPlayer());
		}
	}

	public ViewMode getViewModel() 
	{
		return ViewMode.BATTLE_VIEW;
	}

	public void movedToFront(Game game) 
	{
		System.out.println("moved to front cycle started");
		cycleMessage(false);
	}
}
