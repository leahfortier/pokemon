package gui.view;

import gui.Button;
import gui.DrawMetrics;
import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import main.Save;
import map.AreaData;
import map.AreaData.WeatherState;
import map.DialogueSequence;
import map.MapData;
import map.entity.Entity;
import map.entity.MovableEntity;
import map.entity.MovableEntity.Direction;
import map.entity.NPCEntity;
import map.entity.PlayerEntity;
import map.triggers.Trigger;
import pokemon.ActivePokemon;
import sound.SoundTitle;
import trainer.CharacterData;
import battle.Battle;

public class MapView extends View
{
	private static final String[] MENU_TEXT = {"Pok\u00E9dex", "Pok\u00E9mon", "Bag", "Player___", "Options", "Save", "Exit", "Return"};
	
	private static final int AREA_NAME_ANIMATION_LIFESPAN = 2000;
	private static final int BATTLE_INTRO_ANIMATION_LIFESPAN = 1000;
	
	private static final int[] ddx = {0, -1, 0};
	private static final int[] ddy = {0, 0, -1};
	
	public String currentMapName;
	public AreaData currentArea;
	public MapData currentMap;
	public Trigger currentMusicTrigger;
	public String queuedDialogueName;
	
	private Entity[][] entities;
	private ArrayList<Entity> entityList;
	private LinkedList<Entity> removeQueue;
	private PlayerEntity playerEntity;
	
	private DialogueSequence currentDialogue;
	private int dialogueSelection;
	private int startX, startY, endX, endY;
	private float drawX, drawY;
	
	private int areaDisplayTime;
	
	private Battle battle;
	private boolean seenWild;
	private int battleAnimationTime;
	
	private BufferedImage battleImageSlideRight;
	private BufferedImage battleImageSlideLeft;
	
	private enum VisualState
	{
		MESSAGE, MENU, MAP, BATTLE_ANIMATION
	};
	
	private VisualState state;
	private WeatherState weatherState;
	
	private int selectedButton;
	private final Button[] menuButtons;
	
	private int[] rainHeight;
	private Random rand = new Random();
	private int lightningFrame;
	
	public MapView()
	{
		currentMapName = "";
		currentArea = null;
		currentDialogue = null;
		rainHeight = new int[Global.GAME_SIZE.width/2];
		state = VisualState.MAP;
		selectedButton = 0;
		
		areaDisplayTime = 0;
		
		weatherState = WeatherState.NORMAL;
		
		menuButtons = new Button[MENU_TEXT.length];
		
		for (int i = 0; i < menuButtons.length; i++)
		{
			menuButtons[i] = new Button(558, 72*i + 10, 240, 70, Button.HoverAction.ARROW, 
					new int[] {	Button.NO_TRANSITON, // Right 
								Button.basicUp(i, menuButtons.length), // Up
								Button.NO_TRANSITON, // Left
								Button.basicDown(i, menuButtons.length) // Down
							});
		}
	}
	
	public void draw(Graphics g, GameData data) 
	{
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		
		TileSet mapTiles = data.getMapTiles();
		
		for (int y = startY; y < endY; y++)
		{
			for (int x = startX; x < endX; x++)
			{
				int bgTile = currentMap.getBgTile(x,y);
				int dx = (int)drawX + x*Global.TILESIZE;
				int dy = (int)drawY + y*Global.TILESIZE;
				
				if ((bgTile>>24) != 0)
				{
					BufferedImage img = mapTiles.getTile(bgTile);
					g.drawImage(img, dx + (Global.TILESIZE - img.getWidth()), dy + (Global.TILESIZE - img.getHeight()), null);
				}
			}
		}
		
		for (int y = startY; y < endY; y++)
		{
			for (int x = startX; x < endX; x++)
			{
				int dx = (int) (drawX) + x*Global.TILESIZE;
				int dy = (int) (drawY) + y*Global.TILESIZE;
				int fgTile = currentMap.getFgTile(x, y);
				
				if ((fgTile>>24) != 0)
				{
					BufferedImage img = mapTiles.getTile(fgTile);
					g.drawImage(img, dx + (Global.TILESIZE - img.getWidth()), dy + (Global.TILESIZE - img.getHeight()), null);
				}
				
				//draw entities, and check for entities above and to the left of this location to see if they just moved out and draw them again.
				for (int d = 0; d < ddx.length; d++)
				{
					int nx, ny;
					nx = ddx[d] + x;
					ny = ddy[d] + y;
					
					if (nx < 0 || ny < 0 || nx >= entities.length || ny >= entities[0].length)
						continue;
					
					if (entities[nx][ny] != null)
					{
						//If entity is a movable entity and they are moving right or down, do not draw them again.
						if(entities[nx][ny] instanceof MovableEntity)
						{
							Direction td = ((MovableEntity)entities[nx][ny]).getDirection();
							if (d != 0 && ((td == Direction.RIGHT) || (td == Direction.DOWN)))
								continue;
						}
						//Not a movable entity, only draw once.
						else if(d != 0)
						{
							continue;
						}
						
						entities[nx][ny].draw(g, data, drawX, drawY, d > 0);
					}
				}
			}
		}

		drawWeatherEffects(g);
		
		//Area Transition
		if (areaDisplayTime > 0)
		{
			drawAreaTransitionAnimation(g);
		}
		
		switch (state)
		{
			case MESSAGE:
				BufferedImage bg = data.getBattleTiles().getTile(3);
				g.drawImage(bg, 0, 439, null);
				
				DrawMetrics.setFont(g, 30);
				g.setColor(Color.WHITE);
				
				int height = DrawMetrics.drawWrappedText(g, currentDialogue.text, 30, 490, 720);
				
				// TODO: wtf is this variable name
				int i1 = 0;
				
				for (String choice: currentDialogue.choices)
				{
					if (choice == null)
						break;
				
					if (i1 == dialogueSelection)
						g.fillOval(50, height + i1*36, 10, 10);
					
					g.drawString(choice, 80, height + (i1++)*36);
				}
				break;
			case MENU:
				TileSet menuTiles = data.getMenuTiles();
				
				g.drawImage(menuTiles.getTile(1), 527, 0, null);
				DrawMetrics.setFont(g, 40);
				g.setColor(Color.black);
				
				for (int i = 0; i < MENU_TEXT.length; i++)
					g.drawString(MENU_TEXT[i], 558, 59 + 72*i);
				
				for (Button b: menuButtons)
					b.draw(g);
				break;
			case BATTLE_ANIMATION:
				if(battleImageSlideRight != null && battleImageSlideLeft != null)
					drawBattleIntroAnimation(g);
				break;
			case MAP:
				break;
			default:
				break;
		}
	}
	
	private void drawWeatherEffects(Graphics g)
	{
		switch(weatherState)
		{
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
	
	private void drawSun(Graphics g)
	{
		g.setColor(new Color(255, 255, 255, 64));
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
	}
	
	private void drawRain(Graphics g)
	{
		g.setColor(new Color(0,0,0, 64));
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		g.setColor(new Color(50,50,255, 128));
		for (int i = 0; i < rainHeight.length; i++)
			if (rainHeight[i] != 0){
				g.drawRect(i*2, rainHeight[i] - 40, 1, 40);
				rainHeight[i] += 50;
				if (rainHeight[i] > Global.GAME_SIZE.height + 40)
					rainHeight[i] = 0;
			}
		for (int i = 0; i < 50; i++){
				int x = rand.nextInt(rainHeight.length);
				if (rainHeight[x] == 0)
					rainHeight[x] = 1 + rand.nextInt(40);
			}
		
		if (rand.nextInt(80) == 0 || (lightningFrame > 80 && rand.nextInt(4) == 0)){
			lightningFrame = 128;
		}
		if (lightningFrame > 0){
			g.setColor(new Color(255,255,255, lightningFrame));
			g.fillRect(0,0,Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			lightningFrame = 7*lightningFrame/8 - 1;
		}else lightningFrame = 0;
	}
	
	private void drawAreaTransitionAnimation(Graphics g)
	{
		int fontSize = 30;
		
		int insideWidth = DrawMetrics.getSuggestedWidth(currentArea.getAreaName(), fontSize);
		int insideHeight = DrawMetrics.getSuggestedHeight(fontSize);
		
		int borderSize = 2;
		int graySize = 10;
		
		int totalWidth = borderSize*2 + graySize*2 + insideWidth;
		int totalHeight = borderSize*2 + graySize*2 + insideHeight;
		
		int yValue = 0;
		
		// Calculate exit location
		if (areaDisplayTime/(double)AREA_NAME_ANIMATION_LIFESPAN < .2)
		{
			yValue = -1*(int)(((AREA_NAME_ANIMATION_LIFESPAN - areaDisplayTime)/(double)AREA_NAME_ANIMATION_LIFESPAN - 4/5.0) * 5 * (insideHeight + (2*graySize)));
		}
		// Calculate entrance location
		else if (areaDisplayTime/(double)AREA_NAME_ANIMATION_LIFESPAN > .8)
		{
			yValue = -1*(int)(((areaDisplayTime)/(double)AREA_NAME_ANIMATION_LIFESPAN - 4/5.0) * 5 * (insideHeight + 2*graySize));
		}
		
		// Black border
		g.setColor(Color.BLACK);
		g.fillRect(0, yValue, totalWidth, totalHeight);
		
		// Gray border
		g.setColor(new Color(0x333333));
		g.fillRect(borderSize, yValue + borderSize, insideWidth + 2*graySize, insideHeight + 2*graySize);
		
		// Lighter gray inside
		g.setColor(new Color(0x666666));
		g.fillRect(borderSize + graySize, yValue + graySize + borderSize, insideWidth, insideHeight);
		
		g.setColor(Color.WHITE);
		DrawMetrics.setFont(g, fontSize);
		
		DrawMetrics.drawCenteredString(g, currentArea.getAreaName(), 0, yValue, totalWidth, totalHeight);
	}
	
	// Display battle intro animation.
	private void drawBattleIntroAnimation(Graphics g)
	{
		int drawWidth = Global.GAME_SIZE.width/2;
		int drawHeightLeft;
		int drawHeighRight;
		float moveInAnimationPercentage;
		float fadeOutPercentage = 0.2f;
		
		if (battle.isWildBattle())
		{
			drawHeighRight = drawHeightLeft = Global.GAME_SIZE.height * 5 / 8;
			drawHeightLeft -= battleImageSlideLeft.getHeight()/2;
			drawHeighRight -= battleImageSlideRight.getHeight();
			
			moveInAnimationPercentage = 0.5f;
		}
		else
		{
			drawHeighRight = drawHeightLeft = Global.GAME_SIZE.height/2;
			drawHeightLeft -= battleImageSlideLeft.getHeight();
			//drawHeighRight -= battleImageSlideRight.getHeight();
			
			moveInAnimationPercentage = 0.4f;
		}
		
		
		// Images slide in from sides
		if(battleAnimationTime > BATTLE_INTRO_ANIMATION_LIFESPAN*(1-moveInAnimationPercentage))
		{
			float normalizedTime = (battleAnimationTime - BATTLE_INTRO_ANIMATION_LIFESPAN*(1-moveInAnimationPercentage))/ (BATTLE_INTRO_ANIMATION_LIFESPAN*moveInAnimationPercentage);

			int dist = -battleImageSlideRight.getWidth()/2 -drawWidth;
			dist = (int)(dist * normalizedTime);
			
			g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2 + dist, drawHeightLeft, null);
			
			dist = Global.GAME_SIZE.width;
			dist = (int)(dist * normalizedTime);
			
			g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2 + dist, drawHeighRight, null);
		}
		
		// Hold images
		else 
		{
			g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2, drawHeightLeft, null);
			g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2, drawHeighRight, null);
			
			//Fade to black before battle appears.
			if (battleAnimationTime < BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage)
			{
				int f = Math.min(255, (int)((BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage - battleAnimationTime)/ (BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage) *255));
				
				g.setColor(new Color(0, 0, 0, f));
				g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			}
		}
	}
	

	public void update(int dt, InputControl input, Game game) 
	{
		CharacterData character = game.charData;
		MENU_TEXT[3] = character.getName();

		if (!currentMapName.equals(character.mapName) || character.mapReset)
		{
			currentMapName = character.mapName;
			currentMap = game.data.getMap(currentMapName);
			
			if (character.mapReset) 
			{
				character.mapReset = false;
				currentMap.setCharacterToEntrance(character, character.mapEntranceName);
			}
			
			currentArea = null;
			
			entities = currentMap.populateEntities(character, game.data);
			
			Direction prevDir = character.direction;
			
			playerEntity = new PlayerEntity(character);
			playerEntity.setDirection(prevDir);
			entities[character.locationX][character.locationY] = playerEntity;
			
			entityList = new ArrayList<>();
			for (Entity[] er: entities)
				for (Entity e: er)
					if (e != null)
						entityList.add(e);
			
			removeQueue = new LinkedList<>();
			state = VisualState.MAP;
		}
		
		if (areaDisplayTime > 0)
		{
			areaDisplayTime -= dt;
		}
		
		//New area
		AreaData area = game.data.getArea(currentMap.getAreaName(character.locationX, character.locationY));
		if (currentArea == null || !area.getAreaName().equals(currentArea.getAreaName()))
		{
			character.areaName = area.getAreaName();
			currentArea = area;

			areaDisplayTime = AREA_NAME_ANIMATION_LIFESPAN;
			weatherState = area.getWeather();
			
			//Queue to play new area's music.
			currentMusicTrigger = game.data.getTrigger(area.getMusicTriggerName());
			//System.out.println(currentMusicTrigger);
			
			playAreaMusic(game);
		}
		
		switch (state)
		{
			case BATTLE_ANIMATION:
				if(battleImageSlideLeft == null || battleImageSlideRight == null)
					loadBattleImages(game);
				
				if(battleAnimationTime < 0)
				{
					battle = null;
					game.setViewMode(ViewMode.BATTLE_VIEW);
					state = VisualState.MAP;
				}
				
				battleAnimationTime -= dt;
				break;		
			case MAP:
				if (input.isDown(Control.ESC)){
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
				if (input.isDown(Control.DOWN)){
					input.consumeKey(Control.DOWN);
					dialogueSelection++;
				}else if (input.isDown(Control.UP)){
					input.consumeKey(Control.UP);
					dialogueSelection--;
				}
				if (currentDialogue.next.length != 0){
					if (dialogueSelection < 0)
						dialogueSelection += currentDialogue.next.length;
					dialogueSelection %= currentDialogue.next.length;
				}
				if (input.isDown(Control.SPACE) && !Global.soundPlayer.soundEffectIsPlaying()){
					input.consumeKey(Control.SPACE);
					currentDialogue.choose(dialogueSelection, this, game);
					
					if (queuedDialogueName != null){
						currentDialogue = game.data.getDialogue(queuedDialogueName);
						queuedDialogueName = null;
						dialogueSelection = 0;
					}
					else {
						currentDialogue = null;
						if(battle == null)
							state = VisualState.MAP;
					}
				}
				break;
			case MENU:
				selectedButton = Button.update(menuButtons, selectedButton, input);
				int clicked = -1;
				for (int i = 0; i < menuButtons.length; i++){
					if (menuButtons[i].checkConsumePress()){
						clicked = i;
					}
				}
				
				switch (clicked)
				{
					case -1: break; //no click
					case 0: //pokedex
						game.setViewMode(ViewMode.POKEDEX_VIEW);
						break;
					case 1: //pokemon
						game.setViewMode(ViewMode.PARTY_VIEW);
						break;
					case 2: //bag
						game.setViewMode(Game.ViewMode.BAG_VIEW);
						break;
					case 3: //player
						game.setViewMode(Game.ViewMode.TRAINER_CARD_VIEW);
						break;
					case 4: //options
						game.setViewMode(Game.ViewMode.OPTIONS_VIEW);
						break;
					case 5: //save
						// TODO: Question user if they would like to save first.
						Save.save(game.charData);
						currentDialogue = game.data.getDialogue("savedGame");
						state = VisualState.MESSAGE;
						break;
					case 6: //exit
						// TODO: Confirmation
						game.setViewMode(ViewMode.MAIN_MENU_VIEW);
						break;
					case 7: //return
						state = VisualState.MAP;
						break;
				}
				
				if (input.isDown(Control.ESC))
				{
					input.consumeKey(Control.ESC);
					state = VisualState.MAP;
				}
				
				break;
		}
		
		Dimension d = Global.GAME_SIZE;
		float[] drawLoc = playerEntity.getDrawLocation(d);

		drawX = drawLoc[0];
		drawY = drawLoc[1];
		
		int tilesX = d.width/Global.TILESIZE;
		int tilesY = d.height/Global.TILESIZE;
		
		startX = (int) (-drawX/Global.TILESIZE);
		startY = (int) (-drawY/Global.TILESIZE);
		
		endX = startX + tilesX + 6;
		endY = startY + tilesY + 6;
		
		// Check for any NPCs facing the player
		if (!playerEntity.isStalled() && state == VisualState.MAP)
		{
			for (int dist = 1; dist <= NPCEntity.NPC_SIGHT_DISTANCE; ++dist)
			{
				// TODO: Move to movable entity
				for (Direction direction : Direction.values())
				{
					int x = playerEntity.getX() - direction.dx*dist;
					int y = playerEntity.getY() - direction.dy*dist;
				
					if (!currentMap.inBounds(x, y))
						continue;
					
					if (entities[x][y] != null && entities[x][y] instanceof NPCEntity && ((NPCEntity)entities[x][y]).isFacing(playerEntity.getX(), playerEntity.getY()) && ((NPCEntity)entities[x][y]).getWalkToPlayer())
					{
						NPCEntity npc = (NPCEntity)entities[x][y];
						if (!npc.getWalkingToPlayer() && game.data.getTrigger(npc.getWalkTrigger()).isTriggered(game.charData))
						{
							playerEntity.stall();
							npc.setDirection(direction);
							npc.walkTowards(dist - 1, direction);
							
							if (npc.isTrainer())
							{
								// TODO: Get trainer spotted music
								Global.soundPlayer.playMusic(SoundTitle.TRAINER_SPOTTED);
							}
						}
					}
				}
			}
		}

		for (Entity e: entityList)
			if (e != null && (state == VisualState.MAP || e != playerEntity))
				e.update(dt, entities, currentMap, input, this);
		
		if (state == VisualState.MAP) 
			playerEntity.triggerCheck(game, currentMap);
		
		while (!removeQueue.isEmpty())
		{
			Entity e = removeQueue.removeFirst();
			entityList.remove(e);
			entities[e.getX()][e.getY()] = null;
		}
		
		//CharacterData has a message to display and no current message is being displayed.
		if(character.messages != null && queuedDialogueName == null && currentDialogue == null)
		{
			currentDialogue = character.messages;
			character.messages = null;
			queuedDialogueName = null;
			dialogueSelection = 0;

			state = VisualState.MESSAGE;
		}

		if (queuedDialogueName != null && (currentDialogue == null || !queuedDialogueName.equals(currentDialogue.name)))
		{
			currentDialogue = game.data.getDialogue(queuedDialogueName);
			queuedDialogueName = null;
			dialogueSelection = 0;
			state = VisualState.MESSAGE;
		}
	}
	
	public void playAreaMusic(Game game)
	{
		if (currentMusicTrigger != null)
		{
			currentMusicTrigger.execute(game);
		}
		else if(currentArea != null)
		{
			System.err.println("No music specified for current area " + currentArea.getAreaName() + ".");
			Global.soundPlayer.playMusic(SoundTitle.DEFAULT_TUNE);
		}
	}

	public Game.ViewMode getViewModel() 
	{
		return Game.ViewMode.MAP_VIEW;
	}

	public void setDialogue(String dialogueName) 
	{
		queuedDialogueName = dialogueName;
	}
	
	public void setBattle(Battle battle, boolean seenWild)
	{
		this.battle = battle;
		battleAnimationTime = BATTLE_INTRO_ANIMATION_LIFESPAN;
		state = VisualState.BATTLE_ANIMATION;
		battleImageSlideLeft = null;
		battleImageSlideRight = null;
		this.seenWild = seenWild;
		
		this.battle.setTerrainType(currentArea.getTerrain(), true);
		
		if (battle.isWildBattle())
		{
			Global.soundPlayer.playMusic(SoundTitle.WILD_POKEMON_BATTLE);
		}
		else
		{
			// TODO: Get trainer battle music
			Global.soundPlayer.playMusic(SoundTitle.TRAINER_BATTLE);
		}
	}
	
	private void loadBattleImages(Game game)
	{
		if (battle.isWildBattle())
		{
			battleImageSlideLeft = game.data.getBattleTiles().getTile(0x300 + currentArea.getTerrain().ordinal());
			
			ActivePokemon p = battle.getOpponent().front();
			battleImageSlideRight = game.data.getPokemonTilesLarge().getTile(p.getImageIndex());
			
			if(seenWild)
			{
				battleImageSlideRight = Global.colorImage(battleImageSlideRight, new float[] {0,0,0,1}, new float[] {0,0,0,0});
			}
		}
		else
		{
			battleImageSlideRight = game.data.getBattleTiles().getTile(0x00100001);
			battleImageSlideLeft = game.data.getBattleTiles().getTile(0x00100000);
		}
	}
	
	public void addEntity(Entity e)
	{
		entities[e.getX()][e.getY()] = e;
		entityList.add(e);
	}
	
	public void removeEntity(Entity e)
	{
		removeQueue.add(e);
	}

	public void movedToFront(Game game) 
	{
		playAreaMusic(game);
	}
}
