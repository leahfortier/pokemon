package gui.view;

import gui.Button;
import gui.ButtonHoverAction;
import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import main.Game;
import main.Game.ViewMode;
import main.InputControl.Control;
import main.Global;
import main.InputControl;
import map.DialogueSequence;
import map.MapData;
import map.entity.Entity;
import map.entity.NPCEntity;
import map.entity.PlayerEntity;
import trainer.CharacterData;

public class MapView extends View{
	public String currentMapName;
	public String currentAreaName;
	public MapData currentMap;
	public String queuedDialogueName;
	
	private Entity[][] entities;
	private ArrayList<Entity> entityList;
	private LinkedList<Entity> removeQueue;
	private PlayerEntity playerEntity;
	private int[] ddx = {0, -1, 0};
	private int[] ddy = {0, 0, -1};
	
	private DialogueSequence currentDialogue;
	private int dialogueSelection;
	private int startX, startY, endX, endY;
	private float drawX, drawY;
	
	private int areaDisplayTime;
	private static final int totalAreaDisplayTime = 2000;
	
	private enum VisualState{
		MESSAGE, MENU, MAP
	};
	VisualState state;
	
	int selectedButton;
	Button[] menuButtons;
	String[] menuText = {"Pok\u00E9dex", "Pok\u00E9mon", "Bag", "Player___", "Options", "Save", "Exit", "Return"};
	private ButtonHoverAction arrowHoverAction = new ButtonHoverAction()
	{
		final int[] tx = {0, 11, 0};
		final int[] ty = {0, 12, 23};
		int time = 0;
		
		public void draw(Graphics g, Button button) 
		{
			time = (time+1)%80;
			int x = button.x-10;
			int y = button.y+button.h/2-12;
			g.translate(x, y);
			g.setColor(new Color(0,0,0, 55+200*(Math.abs(time-40))/40));
			g.fillPolygon(tx, ty, 3);
			g.translate(-x, -y);
		}
	};
	
//	private int[] rainHeight;
//	private Random rand = new Random();
//	private int lightningFrame;
	public MapView(){
		currentMapName = "";
		currentAreaName = "";
		currentDialogue = null;
//		rainHeight = new int[Global.GAME_SIZE.width/2];
		state = VisualState.MAP;
		selectedButton = 0;
		
		areaDisplayTime = 0;
		
		menuButtons = new Button[8];
		for (int i = 0; i<menuButtons.length; i++) //RIGHT, UP, LEFT, DOWN
			menuButtons[i] = new Button(558, 72*i+10, 240, 70, arrowHoverAction, new int[]{-1, i==0?7:i-1, -1, i==7?0:i+1});
	}
	
	@Override
	public void draw(Graphics g, GameData data) {
		g.setColor(Color.black);
		g.fillRect(0,0,Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		
		TileSet mapTiles = data.getMapTiles();
		
		for (int y = startY; y<endY; y++)
			for (int x = startX; x<endX; x++){
				int bgTile = currentMap.getBgTile(x,y);
				int dx = (int) (drawX)+x*Global.TILESIZE;
				int dy = (int) (drawY)+y*Global.TILESIZE;
				if ((bgTile>>24) != 0){
					BufferedImage img = mapTiles.getTile(bgTile);
					g.drawImage(img, dx+(Global.TILESIZE-img.getWidth()), dy+(Global.TILESIZE-img.getHeight()), null);
				}
			}
		for (int y = startY; y<endY; y++)
			for (int x = startX; x<endX; x++){
				int dx = (int) (drawX)+x*Global.TILESIZE;
				int dy = (int) (drawY)+y*Global.TILESIZE;
				int fgTile = currentMap.getFgTile(x, y);
				if ((fgTile>>24) != 0){
					BufferedImage img = mapTiles.getTile(fgTile);
					g.drawImage(img, dx+(Global.TILESIZE-img.getWidth()), dy+(Global.TILESIZE-img.getHeight()), null);
				}
				for (int d = 0; d<ddx.length; d++){
					int nx, ny;
					nx = ddx[d]+x;
					ny = ddy[d]+y;
					if (nx < 0 || ny < 0 || nx >= entities.length || ny >= entities[0].length)
						continue;
					if (entities[nx][ny] != null){
						int td = entities[nx][ny].getDirection();
						if (d != 0 && ((td == 0) || (td == 3)))
							continue;
						entities[nx][ny].draw(g, data, drawX, drawY, d>0);
					}
				}
			}
		
		//Area Transition
		if (areaDisplayTime > 0)
		{
			int fontSize = 30;
			
			int width = (currentAreaName.length() + 3) * fontSize/2;
			int height = fontSize + fontSize/2;
			
			int borderSize = 2;
			int graySize = 10;
			
			int yValue = 0;
			//Calculate exit location
			if (areaDisplayTime/(double)totalAreaDisplayTime < .2)
			{
				yValue = -1*(int)(((totalAreaDisplayTime - areaDisplayTime)/(double)totalAreaDisplayTime -4/5.0) * 5 * (height+(2*graySize)));
			}
			
			//Calculate entrance location
			else if (areaDisplayTime/(double)totalAreaDisplayTime > .8)
			{
				yValue = -1*(int)(((areaDisplayTime)/(double)totalAreaDisplayTime -4/5.0) * 5 * (height+(2*graySize)));
			}
			
			//Black border
			g.setColor(Color.black);
			g.fillRect(0, yValue, width+(2*graySize), height+(2*graySize));
			//Gray border
			g.setColor(new Color(0x333333));
			g.fillRect(borderSize, yValue+borderSize, width+(2*graySize)-(2*borderSize), height+(2*graySize)-(2*borderSize));
			//Lighter gray inside
			g.setColor(new Color(0x666666));
			g.fillRect(graySize, yValue+graySize, width, height);
			
			g.setFont(Global.getFont(fontSize));
			g.setColor(Color.white);
			Global.drawStringCenterX(currentAreaName, (width+(2*graySize))/2, yValue + fontSize/3 + graySize + height/2, g);
		}
		
		/*/sun
		g.setColor(new Color(255, 255, 255, 64));
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		*/
		
		/*/ rain
		g.setColor(new Color(0,0,0, 64));
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		g.setColor(new Color(50,50,255, 128));
		for (int i = 0; i<rainHeight.length; i++)
			if (rainHeight[i] != 0){
				g.drawRect(i*2, rainHeight[i]-40, 1, 40);
				rainHeight[i] += 50;
				if (rainHeight[i] > Global.GAME_SIZE.height+40)
					rainHeight[i] = 0;
			}
		for (int i = 0; i<50; i++){
				int x = rand.nextInt(rainHeight.length);
				if (rainHeight[x] == 0)
					rainHeight[x] = 1+rand.nextInt(40);
			}
		
		if (rand.nextInt(80) == 0 || (lightningFrame > 80 && rand.nextInt(4) == 0)){
			lightningFrame = 128;
		}
		if (lightningFrame > 0){
			g.setColor(new Color(255,255,255, lightningFrame));
			g.fillRect(0,0,Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			lightningFrame = 7*lightningFrame/8-1;
		}else lightningFrame = 0;
		*/
		
		switch (state){
		case MESSAGE:
			BufferedImage bg = data.getBattleTiles().getTile(3);
			g.drawImage(bg, 0, 439, null);
			
			g.setFont(Global.getFont(30));
			g.setColor(Color.white);
			//g.drawString(currentDialogue.text, 40, 490);
			int h = Global.drawWrappedText(g, currentDialogue.text, 30, 490, 720);
			int i1 = 0;
			for (String choice: currentDialogue.choices){
				if (choice == null)
					break;
				if (i1 == dialogueSelection)
					g.fillOval(50, h+i1*36, 10, 10);
				g.drawString(choice, 80, h+(i1++)*36);
			}
			break;
		case MENU:
			TileSet menuTiles = data.getMenuTiles();
			g.drawImage(menuTiles.getTile(1), 527, 0, null);
			g.setFont(Global.getFont(40));
			g.setColor(Color.black);
			for (int i = 0; i<menuText.length; i++)
				g.drawString(menuText[i], 558, 59+72*i);
			for (Button b: menuButtons)
				b.draw(g);
			break;
		case MAP:
			break;
		default:
			break;
		}
	}

	@Override
	public void update(int dt, InputControl input, Game game) {
		CharacterData character = game.charData;
		menuText[3] = character.getName();
		if (!currentMapName.equals(character.mapName) || character.mapReset){
			currentMapName = character.mapName;
			currentMap = game.data.getMap(currentMapName);
			
			if (character.mapReset) {
				character.mapReset = false;
				currentMap.setCharacterToEntrance(character, character.mapEntranceName);
			}
			
			currentAreaName = "";
			
			entities = currentMap.populateEntities(character, game.data);
			int prevDir = character.direction;
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
		if (!game.data.getArea(currentMap.getAreaName(character.locationX, character.locationY)).equals(currentAreaName))
		{
			currentAreaName = character.areaName = game.data.getArea(currentMap.getAreaName(character.locationX, character.locationY));
			areaDisplayTime = totalAreaDisplayTime;
			//System.out.println(character.areaName);
			
			//Queue to play new area's music.
			//If we ever support that.
		}
		
		
		switch (state){
		case MAP:
			if (input.isDown(Control.ESC)){
				input.consumeKey(Control.ESC);
				state = VisualState.MENU;
			}
//			if (input.bKey.isDown){
//				input.bKey.consume();
//				game.setViewMode(Game.ViewMode.BATTLE_VIEW);
//			}
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
			if (input.isDown(Control.SPACE)){
				input.consumeKey(Control.SPACE);
				currentDialogue.choose(dialogueSelection, this, game);
				
				if (queuedDialogueName != null){
					currentDialogue = game.data.getDialogue(queuedDialogueName);
					queuedDialogueName = null;
					dialogueSelection = 0;
				}
				else {
					currentDialogue = null;
					state = VisualState.MAP;
				}
			}
			break;
		case MENU:
			selectedButton = Button.update(menuButtons, selectedButton, input);
			int clicked = -1;
			for (int i = 0; i<menuButtons.length; i++){
				if (menuButtons[i].isPress()){
					menuButtons[i].consumePress();
					clicked = i;
				}
			}
			switch (clicked){
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
				game.charData.save();
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
		endX = startX+tilesX+6;
		endY = startY+tilesY+6;
		
		//Check for any NPCs facing the player
		if (!playerEntity.isStalled() && state == VisualState.MAP)
		{
			for (int dist = 1; dist <= NPCEntity.NPC_SIGHT_DISTANCE; ++dist)
			{
				for (int dir = 0; dir < Entity.tdx.length; ++dir)
				{
					int x = playerEntity.charX + Entity.tdx[dir]*dist;
					int y = playerEntity.charY + Entity.tdy[dir]*dist;
				
					if (!currentMap.inBounds(x, y))
						continue;
					
					if (entities[x][y] != null && entities[x][y] instanceof NPCEntity && entities[x][y].isFacing(playerEntity.charX, playerEntity.charY) && ((NPCEntity)entities[x][y]).getWalkToPlayer())
					{
						NPCEntity npc = (NPCEntity)entities[x][y];
						if (!npc.getWalkingToPlayer() && game.data.getTrigger(npc.getWalkTrigger()).isTriggered(game.charData))
						{
							playerEntity.stall();
							npc.setDirection(dir);
							npc.walkTowards(dist-1, dir);
						}
					}
				}
			}
		}

		for (Entity e: entityList)
			if (e != null && (state == VisualState.MAP || e != playerEntity))
				e.update(dt, entities, currentMap, input, this);
		
		if (state == VisualState.MAP) playerEntity.triggerCheck(game, currentMap);
		
		
		while (!removeQueue.isEmpty())
		{
			Entity e = removeQueue.removeFirst();
			entityList.remove(e);
			entities[e.charX][e.charY] = null;
		}

		if (queuedDialogueName != null && (currentDialogue == null || !queuedDialogueName.equals(currentDialogue.name))){
			currentDialogue = game.data.getDialogue(queuedDialogueName);
			queuedDialogueName = null;
			dialogueSelection = 0;
			state = VisualState.MESSAGE;
		}
	}

	@Override
	public Game.ViewMode getViewModel() {
		return Game.ViewMode.MAP_VIEW;
	}

	public void setDialogue(String dialogueName) {
		queuedDialogueName = dialogueName;
	}
	
	public void addEntity(Entity e){
		entities[e.charX][e.charY] = e;
		entityList.add(e);
	}
	public void removeEntity(Entity e){
		removeQueue.add(e);
	}

	public void movedToFront() {
		Global.soundPlayer.playMusic("lalala");
	}
}
