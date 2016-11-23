package gui.view;

import battle.Battle;
import gui.Button;
import gui.GameData;
import gui.TileSet;
import main.Game;
import main.Global;
import map.AreaData;
import map.AreaData.WeatherState;
import map.Direction;
import map.MapData;
import map.entity.Entity;
import map.entity.EntityAction;
import map.entity.MovableEntity;
import map.entity.PlayerEntity;
import map.entity.NPCEntity;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pokemon.ActivePokemon;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.CharacterData;
import util.DrawUtils;
import util.InputControl;
import util.InputControl.Control;
import util.PokeString;
import util.Save;
import util.StringUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MapView extends View {
	// TODO: Create enum to hold these and handle operations
	private static final String[] MENU_TEXT = {
			PokeString.POKEDEX,
			PokeString.POKEMON,
			"Bag",
			"Player___",
			"Options",
			"Save",
			"Exit",
			"Return"
	};
	
	private static final int AREA_NAME_ANIMATION_LIFESPAN = 2000;
	private static final int BATTLE_INTRO_ANIMATION_LIFESPAN = 1000;
	
	private static final int[] ddx = { 0, -1, 0 };
	private static final int[] ddy = { 0, 0, -1 };
	
	private String currentMapName;
	private AreaData currentArea;
	private MapData currentMap;
	private SoundTitle currentMusicTitle;
	
	private Entity[][] entities;
	private List<Entity> entityList;
	private Queue<Entity> removeQueue;
	private PlayerEntity playerEntity;

	private MessageUpdate currentMessage;
	private int dialogueSelection;

	private int startX, startY, endX, endY;
	private float drawX, drawY;
	
	private int areaDisplayTime;
	
	private Battle battle;
	private boolean seenWild;
	private int battleAnimationTime;
	
	private BufferedImage battleImageSlideRight;
	private BufferedImage battleImageSlideLeft;
	
	private enum VisualState {
		BATTLE_ANIMATION,
		MAP,
		MENU,
		MESSAGE
	}
	
	private VisualState state;
	private WeatherState weatherState;
	
	private int selectedButton;
	private final Button[] menuButtons;
	
	private int[] rainHeight;
	private int lightningFrame;
	
	MapView() {
		currentMapName = StringUtils.empty();
		rainHeight = new int[Global.GAME_SIZE.width/2];
		state = VisualState.MAP;
		selectedButton = 0;
		
		areaDisplayTime = 0;
		
		weatherState = WeatherState.NORMAL;
		
		menuButtons = new Button[MENU_TEXT.length];
		
		for (int i = 0; i < menuButtons.length; i++) {
			menuButtons[i] = new Button(558, 72*i + 10, 240, 70, Button.HoverAction.ARROW, 
					new int[] {	Button.NO_TRANSITION, // Right
								Button.basicUp(i, menuButtons.length), // Up
								Button.NO_TRANSITION, // Left
								Button.basicDown(i, menuButtons.length) // Down
							});
		}
	}

	// TODO: This method should be split up further
	public void draw(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);

		GameData data = Game.getData();
		TileSet mapTiles = data.getMapTiles();
		
		for (int y = startY; y < endY; y++) {
			for (int x = startX; x < endX; x++) {
				int bgTile = currentMap.getBgTile(x,y);
				int dx = (int)drawX + x*Global.TILE_SIZE;
				int dy = (int)drawY + y*Global.TILE_SIZE;
				
				if ((bgTile>>24) != 0) {
					BufferedImage img = mapTiles.getTile(bgTile);
					g.drawImage(img, dx + (Global.TILE_SIZE - img.getWidth()), dy + (Global.TILE_SIZE - img.getHeight()), null);
				}
			}
		}
		
		for (int y = startY; y < endY; y++) {
			for (int x = startX; x < endX; x++) {
				int dx = (int) (drawX) + x*Global.TILE_SIZE;
				int dy = (int) (drawY) + y*Global.TILE_SIZE;
				int fgTile = currentMap.getFgTile(x, y);
				
				if ((fgTile>>24) != 0) {
					BufferedImage img = mapTiles.getTile(fgTile);
					g.drawImage(img, dx + (Global.TILE_SIZE - img.getWidth()), dy + (Global.TILE_SIZE - img.getHeight()), null);
				}
				
				//draw entities, and check for entities above and to the left of this location to see if they just moved out and draw them again.
				for (int d = 0; d < ddx.length; d++) {
					int nx, ny;
					nx = ddx[d] + x;
					ny = ddy[d] + y;
					
					if (nx < 0 || ny < 0 || nx >= entities.length || ny >= entities[0].length) {
						continue;
					}

					if (entities[nx][ny] != null) {
						// If entity is a movable entity and they are moving right or down, do not draw them again.
						if (entities[nx][ny] instanceof MovableEntity) {
							Direction td = ((MovableEntity)entities[nx][ny]).getDirection();
							if (d != 0 && ((td == Direction.RIGHT) || (td == Direction.DOWN)))
								continue;
						}
						// Not a movable entity, only draw once.
						else if (d != 0) {
							continue;
						}
						
						entities[nx][ny].draw(g, drawX, drawY, d > 0);
					}
				}
			}
		}

		drawWeatherEffects(g);
		
		// Area Transition
		if (areaDisplayTime > 0) {
			drawAreaTransitionAnimation(g);
		}
		
		switch (state) {
			case MESSAGE:
				BufferedImage bg = data.getBattleTiles().getTile(3);
				g.drawImage(bg, 0, 439, null);
				
				DrawUtils.setFont(g, 30);
				g.setColor(Color.BLACK);

				int height = DrawUtils.drawWrappedText(g, currentMessage.getMessage(), 30, 490, 720);
				if (currentMessage.isChoice()) {
					ChoiceMatcher[] choices = currentMessage.getChoices();
					for (int i = 0; i < choices.length; i++) {
						int y = height + i*DrawUtils.getDistanceBetweenRows(g);
						if (i == dialogueSelection) {
							g.fillOval(50, y - DrawUtils.getTextHeight(g)/2 - 5, 10, 10);
						}

						g.drawString(choices[i].text, 80, y);
					}
				}
				break;
			case MENU:
				TileSet menuTiles = data.getMenuTiles();
				
				g.drawImage(menuTiles.getTile(1), 527, 0, null);
				DrawUtils.setFont(g, 40);
				g.setColor(Color.BLACK);
				
				for (int i = 0; i < MENU_TEXT.length; i++) {
					g.drawString(MENU_TEXT[i], 558, 59 + 72*i);
				}
				
				for (Button b: menuButtons) {
					b.draw(g);
				}
				break;
			case BATTLE_ANIMATION:
				if (battleImageSlideRight != null && battleImageSlideLeft != null) {
					drawBattleIntroAnimation(g);
				}
				break;
			case MAP:
				break;
			default:
				break;
		}
	}

	// TODO: Weather should likely have its own class
	private void drawWeatherEffects(Graphics g) {
		switch(weatherState) {
			case SUN:
				drawSun(g);
				break;
			case RAIN:
				drawRain(g);
				break;
			case NORMAL:
			default:
				break;
		}
	}
	
	private void drawSun(Graphics g) {
		g.setColor(new Color(255, 255, 255, 64));
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
	}

	// TODO: Make a fuckton of constants and whatever this is awful
	private void drawRain(Graphics g) {
		g.setColor(new Color(0,0,0, 64));
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		g.setColor(new Color(50,50,255, 128));

		for (int i = 0; i < rainHeight.length; i++) {
			if (rainHeight[i] != 0) {
				g.drawRect(i*2, rainHeight[i] - 40, 1, 40);
				rainHeight[i] += 50;

				if (rainHeight[i] > Global.GAME_SIZE.height + 40) {
					rainHeight[i] = 0;
				}
			}
		}

		for (int i = 0; i < 50; i++) {
			int x = Global.getRandomInt(rainHeight.length);
			if (rainHeight[x] == 0){
				rainHeight[x] = 1 + Global.getRandomInt(40);
			}
		}
		
		if (Global.getRandomInt(80) == 0 || (lightningFrame > 80 && Global.getRandomInt(4) == 0)) {
			lightningFrame = 128;
		}

		if (lightningFrame > 0) {
			g.setColor(new Color(255,255,255, lightningFrame));
			g.fillRect(0,0,Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			lightningFrame = 7*lightningFrame/8 - 1;
		} else {
			lightningFrame = 0;
		}
	}
	
	private void drawAreaTransitionAnimation(Graphics g) {
		int fontSize = 30;
		
		int insideWidth = DrawUtils.getSuggestedWidth(currentArea.getAreaName(), fontSize);
		int insideHeight = DrawUtils.getSuggestedHeight(fontSize);
		
		int borderSize = 2;
		int graySize = 10;
		
		int totalWidth = borderSize*2 + graySize*2 + insideWidth;
		int totalHeight = borderSize*2 + graySize*2 + insideHeight;
		
		int yValue = 0;
		
		// Calculate exit location
		if (areaDisplayTime/(double)AREA_NAME_ANIMATION_LIFESPAN < .2) {
			yValue = -1*(int)(((AREA_NAME_ANIMATION_LIFESPAN - areaDisplayTime)/(double)AREA_NAME_ANIMATION_LIFESPAN - 4/5.0) * 5 * (insideHeight + (2*graySize)));
		}
		// Calculate entrance location
		else if (areaDisplayTime/(double)AREA_NAME_ANIMATION_LIFESPAN > .8) {
			yValue = -1*(int)(((areaDisplayTime)/(double)AREA_NAME_ANIMATION_LIFESPAN - 4/5.0) * 5 * (insideHeight + 2*graySize));
		}
		
		// Black border
		g.setColor(Color.BLACK);
		g.fillRect(0, yValue, totalWidth, totalHeight);

		// Light grey border
		g.setColor(new Color(195, 195, 195));
		g.fillRect(borderSize, yValue + borderSize, insideWidth + 2*graySize, insideHeight + 2*graySize);

		// White inside
		g.setColor(Color.WHITE);
		g.fillRect(borderSize + graySize, yValue + graySize + borderSize, insideWidth, insideHeight);

		g.setColor(Color.BLACK);
		DrawUtils.setFont(g, fontSize);
		DrawUtils.drawCenteredString(g, currentArea.getAreaName(), 0, yValue, totalWidth, totalHeight);
	}
	
	// Display battle intro animation.
	private void drawBattleIntroAnimation(Graphics g) {
		int drawWidth = Global.GAME_SIZE.width/2;
		int drawHeightLeft;
		int drawHeightRight;
		float moveInAnimationPercentage;
		float fadeOutPercentage = 0.2f;
		
		if (battle.isWildBattle()) {
			drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height * 5 / 8;
			drawHeightLeft -= battleImageSlideLeft.getHeight()/2;
			drawHeightRight -= battleImageSlideRight.getHeight();
			
			moveInAnimationPercentage = 0.5f;
		}
		else {
			drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height/2;
			drawHeightLeft -= battleImageSlideLeft.getHeight();
			//drawHeightRight -= battleImageSlideRight.getHeight();
			
			moveInAnimationPercentage = 0.4f;
		}
		
		
		// Images slide in from sides
		if (battleAnimationTime > BATTLE_INTRO_ANIMATION_LIFESPAN*(1-moveInAnimationPercentage)) {
			float normalizedTime = (battleAnimationTime - BATTLE_INTRO_ANIMATION_LIFESPAN*(1-moveInAnimationPercentage))/ (BATTLE_INTRO_ANIMATION_LIFESPAN*moveInAnimationPercentage);

			int dist = -battleImageSlideRight.getWidth()/2 -drawWidth;
			dist = (int)(dist * normalizedTime);
			
			g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2 + dist, drawHeightLeft, null);
			
			dist = Global.GAME_SIZE.width;
			dist = (int)(dist * normalizedTime);
			
			g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2 + dist, drawHeightRight, null);
		}
		// Hold images
		else {
			g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2, drawHeightLeft, null);
			g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2, drawHeightRight, null);
			
			//Fade to black before battle appears.
			if (battleAnimationTime < BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage)
			{
				int f = Math.min(255, (int)((BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage - battleAnimationTime)/ (BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage) *255));
				
				g.setColor(new Color(0, 0, 0, f));
				g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			}
		}
	}
	

	public void update(int dt, InputControl input) {
		boolean showMessage = true;

		GameData data = Game.getData();
		CharacterData character = Game.getPlayer();
		MENU_TEXT[3] = character.getName();

		if (!currentMapName.equals(character.mapName) || character.mapReset) {
			currentMapName = character.mapName;
			currentMap = data.getMap(currentMapName);
			
			if (character.mapReset) {
				character.mapReset = false;
				currentMap.setCharacterToEntrance(character.mapEntranceName);
			}

			entities = currentMap.populateEntities();

			Direction prevDir = character.direction;

			playerEntity = new PlayerEntity(character);
			playerEntity.setDirection(prevDir);
			entities[character.locationX][character.locationY] = playerEntity;
			
			entityList = new ArrayList<>();
			for (Entity[] er: entities) {
				for (Entity e: er) {
					if (e != null) {
						entityList.add(e);
					}
				}
			}
			
			removeQueue = new LinkedList<>();
			state = VisualState.MAP;
		}
		
		if (areaDisplayTime > 0) {
			areaDisplayTime -= dt;
		}
		
		// New area
		AreaData area = currentMap.getArea(character.locationX, character.locationY);
		String areaName = area.getAreaName();

		character.areaName = areaName;
		currentArea = area;

		// If new area has a new name, display the area name animation
		if (!StringUtils.isNullOrEmpty(areaName) && !areaName.equals(currentArea.getAreaName())) {
			areaDisplayTime = AREA_NAME_ANIMATION_LIFESPAN;
		}

		weatherState = area.getWeather();

		// Queue to play new area's music.
		SoundTitle areaMusic = area.getMusic();
		if (currentMusicTitle != areaMusic) {
			currentMusicTitle = areaMusic;
			playAreaMusic();
		}
		
		switch (state) {
			case BATTLE_ANIMATION:
				if (battleImageSlideLeft == null || battleImageSlideRight == null) {
					loadBattleImages();
				}

				if (battleAnimationTime < 0) {
					battle = null;
					Game.setViewMode(ViewMode.BATTLE_VIEW);
					state = VisualState.MAP;
				}
				
				battleAnimationTime -= dt;
				showMessage = false;
				break;
			case MAP:
				if (input.isDown(Control.ESC)) {
					input.consumeKey(Control.ESC);
					state = VisualState.MENU;
				}

//				if (input.isDown(Control.SPACE)){
//					input.consumeKey(Control.SPACE);
//					//game.setViewMode(Game.ViewMode.BATTLE_VIEW);
//					game.setViewMode(Game.ViewMode.EVOLUTION_VIEW);
//				}
				break;
			case MESSAGE:
				if (currentMessage.isChoice()) {
					if (input.isDown(Control.DOWN)) {
						input.consumeKey(Control.DOWN);
						dialogueSelection++;
					} else if (input.isDown(Control.UP)) {
						input.consumeKey(Control.UP);
						dialogueSelection--;
					}

					dialogueSelection += currentMessage.getChoices().length;
					dialogueSelection %= currentMessage.getChoices().length;
				}

				if (input.isDown(Control.SPACE) && !SoundPlayer.soundPlayer.soundEffectIsPlaying()) {
					input.consumeKey(Control.SPACE);

					if (currentMessage.isChoice()) {
						ChoiceMatcher choice = currentMessage.getChoices()[dialogueSelection];
						Trigger trigger = EntityAction.addActionGroupTrigger(null, null, choice.getActions());
						Messages.addMessageToFront(new MessageUpdate("", trigger.getName(), Update.TRIGGER));
					}

					boolean newMessage = false;
					while (Messages.hasMessages()) {
						cycleMessage();

						if (state != VisualState.MESSAGE || !StringUtils.isNullOrEmpty(currentMessage.getMessage())) {
							newMessage = true;
							break;
						}
					}

					if (!newMessage && !Messages.hasMessages()) {
						PlayerEntity.currentInteractionEntity = null; // TODO: Make this not static
						currentMessage = null;
						if (battle == null) {
							state = VisualState.MAP;
						}
					}
				}
				break;
			case MENU:
				selectedButton = Button.update(menuButtons, selectedButton, input);
				int clicked = -1;
				for (int i = 0; i < menuButtons.length; i++) {
					if (menuButtons[i].checkConsumePress()) {
						clicked = i;
					}
				}

				// TODO: Handle this better
				switch (clicked) {
					case -1: // no click
						break;
					case 0: // pokedex
						Game.setViewMode(ViewMode.POKEDEX_VIEW);
						break;
					case 1: // pokemon
						Game.setViewMode(ViewMode.PARTY_VIEW);
						break;
					case 2: // bag
						Game.setViewMode(ViewMode.BAG_VIEW);
						break;
					case 3: // player
						Game.setViewMode(ViewMode.TRAINER_CARD_VIEW);
						break;
					case 4: // options
						Game.setViewMode(ViewMode.OPTIONS_VIEW);
						break;
					case 5: // save
						// TODO: Question user if they would like to save first.
						Save.save();
						Messages.addMessage("Your game has now been saved!");
						state = VisualState.MESSAGE;
						break;
					case 6: // exit
						// TODO: Confirmation
						Game.setViewMode(ViewMode.MAIN_MENU_VIEW);
						break;
					case 7: // return
						state = VisualState.MAP;
						break;
				}
				
				if (input.isDown(Control.ESC)) {
					input.consumeKey(Control.ESC);
					state = VisualState.MAP;
				}
				break;
		}
		
		Dimension d = Global.GAME_SIZE;
		float[] drawLoc = playerEntity.getDrawLocation(d);

		drawX = drawLoc[0];
		drawY = drawLoc[1];
		
		int tilesX = d.width/Global.TILE_SIZE;
		int tilesY = d.height/Global.TILE_SIZE;
		
		startX = (int) (-drawX/Global.TILE_SIZE);
		startY = (int) (-drawY/Global.TILE_SIZE);
		
		endX = startX + tilesX + 6;
		endY = startY + tilesY + 6;
		
		// Check for any NPCs facing the player
		if (!playerEntity.isStalled() && state == VisualState.MAP) {
			// TODO: Need to make sure every space is passable between the npc and player
			for (int dist = 1; dist <= NPCEntity.NPC_SIGHT_DISTANCE; ++dist) {
				// TODO: Move to movable entity
				for (Direction direction : Direction.values()) {
					int x = playerEntity.getX() - direction.dx*dist;
					int y = playerEntity.getY() - direction.dy*dist;
				
					if (!currentMap.inBounds(x, y)) {
						continue;
					}
					
					if (entities[x][y] != null
							&& entities[x][y] instanceof NPCEntity
							&& ((NPCEntity)entities[x][y]).isFacing(playerEntity.getX(), playerEntity.getY())
							&& ((NPCEntity)entities[x][y]).shouldWalkToPlayer()) {

						NPCEntity npc = (NPCEntity)entities[x][y];
						if (!npc.getWalkingToPlayer() && data.getTrigger(npc.getWalkTrigger()).isTriggered()) {
							playerEntity.stall();
							npc.setDirection(direction);
							npc.walkTowards(dist - 1, direction);
							
							if (npc.isTrainer()) {
								SoundPlayer.soundPlayer.playMusic(SoundTitle.TRAINER_SPOTTED);
							}
						}
					}
				}
			}
		}

		entityList.stream()
				.filter(entity -> entity != null && (state == VisualState.MAP || entity != playerEntity))
				.forEach(entity -> entity.update(dt, entities, currentMap, input, this));
		
		if (state == VisualState.MAP) {
			playerEntity.triggerCheck(currentMap);
		}
		
		while (!removeQueue.isEmpty()) {
			Entity e = removeQueue.poll();
			entityList.remove(e);
			entities[e.getX()][e.getY()] = null;
		}

		if (showMessage && (this.currentMessage == null || StringUtils.isNullOrEmpty(this.currentMessage.getMessage())) && Messages.hasMessages()) {
			cycleMessage();
			if (this.currentMessage != null && this.currentMessage.getUpdateType() != Update.ENTER_BATTLE) {
				state = VisualState.MESSAGE;
			}
		}
	}

	private void cycleMessage() {
		currentMessage = Messages.getNextMessage();

		if (currentMessage.trigger()) {
			Trigger trigger = Game.getData().getTrigger(currentMessage.getTriggerName());

			if (trigger.isTriggered()) {
				trigger.execute();
				if (state != VisualState.MESSAGE) {
					currentMessage = null;
				}

			}
		}
	}

	private void playAreaMusic() {
		if (currentMusicTitle != null) {
			SoundPlayer.soundPlayer.playMusic(currentMusicTitle);
		}
		else if(currentArea != null) {
			System.err.println("No music specified for current area " + currentArea.getAreaName() + ".");
			SoundPlayer.soundPlayer.playMusic(SoundTitle.DEFAULT_TUNE);
		}
	}

	public ViewMode getViewModel() {
		return ViewMode.MAP_VIEW;
	}
	
	public void setBattle(Battle battle, boolean seenWild) {
		this.battle = battle;
		battleAnimationTime = BATTLE_INTRO_ANIMATION_LIFESPAN;
		state = VisualState.BATTLE_ANIMATION;
		battleImageSlideLeft = null;
		battleImageSlideRight = null;
		this.seenWild = seenWild;
		
		this.battle.setTerrainType(currentArea.getTerrain(), true);
		
		if (battle.isWildBattle()) {
			SoundPlayer.soundPlayer.playMusic(SoundTitle.WILD_POKEMON_BATTLE);
		}
		else {
			SoundPlayer.soundPlayer.playMusic(SoundTitle.TRAINER_BATTLE);
		}
	}

	private void loadBattleImages() {
		GameData data = Game.getData();

		if (battle.isWildBattle()) {
			battleImageSlideLeft = data.getBattleTiles().getTile(0x300 + currentArea.getTerrain().ordinal());
			
			ActivePokemon p = battle.getOpponent().front();
			battleImageSlideRight = data.getPokemonTilesLarge().getTile(p.getImageIndex());
			
			if (seenWild) {
				battleImageSlideRight = DrawUtils.colorImage(battleImageSlideRight, new float[] { 0, 0, 0, 1 }, new float[] { 0, 0, 0, 0 });
			}
		}
		else {
			battleImageSlideRight = data.getBattleTiles().getTile(0x00100001);
			battleImageSlideLeft = data.getBattleTiles().getTile(0x00100000);
		}
	}
	
	public void removeEntity(Entity e) {
		removeQueue.add(e);
	}

	public void movedToFront() {
		playAreaMusic();
	}
}
