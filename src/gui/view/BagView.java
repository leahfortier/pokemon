package gui.view;

import gui.Button;
import gui.ButtonHoverAction;
import gui.GameData;
import gui.TileSet;
import item.Bag;
import item.Bag.BagCategory;
import item.Item;
import item.hold.HoldItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Pokedex.PokedexStatus;
import trainer.Trainer;
import battle.Move;
import battle.effect.Status.StatusCondition;

public class BagView extends View
{
	private static final BagCategory[] categories = BagCategory.values();
	private static final int ITEMS_PER_PAGE = 10;
	
	private static final int NUM_BUTTONS = categories.length + Trainer.MAX_POKEMON + ITEMS_PER_PAGE + Move.MAX_MOVES + 6 /* Misc Buttons */;
	private static final int PARTY = categories.length;
	private static final int ITEMS = PARTY + Trainer.MAX_POKEMON;
	private static final int MOVES = ITEMS + ITEMS_PER_PAGE;
	private static final int RETURN = NUM_BUTTONS - 1;
	private static final int DISCARD = NUM_BUTTONS - 2;
	private static final int USE = NUM_BUTTONS - 3;
	private static final int GIVE = NUM_BUTTONS - 4;
	private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
	private static final int LEFT_ARROW = NUM_BUTTONS - 6;
	
	private final int[] primaryColorx = {0, 184, 124, 0};
	private final int[] primaryColory = {0, 0, 61, 61};
	private final int[] secondaryColorx = {184, 308, 308, 124};
	private final int[] secondaryColory = {0, 0, 61, 61};
	
	private final int[] rightArrowx = {0, 16, 16, 32, 16, 16, 0};
	private final int[] rightArrowy = {5, 5, 0, 10, 20, 15, 15};
	private final int[] leftArrowx = {35, 19, 19, 3, 19, 19, 35};
	private final int[] leftArrowy = {5, 5, 0, 10, 20, 15, 15};
	
	private final String[] useNames = {"Give", "Use", "Take"}; // TODO: Change take back to Discard
	
	private int pageNum;
	private int selectedTab;
	private int selectedButton;
	private String message;
	
	private boolean giveClicked;
	private boolean useClicked;
	private boolean takeClicked;

	private BagState state;
	private Item selected;
	private ActivePokemon selectedPokemon;
	private CharacterData player;
	
	private Button[] buttons;
	private Button[] tabButtons;
	private Button[] partyButtons;
	private Button[] moveButtons;
	private Button giveButton;
	private Button useButton;
	private Button discardButton;
	private Button[] itemButtons;
	private Button leftButton;
	private Button rightButton;
	private Button returnButton;
		
	private enum BagState
	{
		ITEM_SELECT, POKEMON_SELECT, MOVE_SELECT;
	}

	public BagView(CharacterData data)
	{
		player = data;
		selectedTab = 0;
		selectedButton = 0;
		
		buttons = new Button[NUM_BUTTONS];
		tabButtons = new Button[categories.length];
		for (int i = 0; i < categories.length; i++)
		{
			buttons[i] = tabButtons[i] = new Button(42 + 102*i, 42, 104, 52, boxHoverAction,
					new int[] {i == categories.length - 1 ? 0 : i + 1, // Right
						 	RETURN, // Up
						 	i == 0 ? categories.length - 1 : i - 1, // Left
						 	USE}); // Down
		}
		
		partyButtons = new Button[Trainer.MAX_POKEMON];
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			buttons[PARTY + i] = partyButtons[i] = new Button(72, 122 + 69*i, 308, 61, boxHoverAction,
					new int[] {i < Trainer.MAX_POKEMON/3 ? GIVE : (i < 2*Trainer.MAX_POKEMON/3 ? ITEMS : RETURN), // Right
							i == 0 ? 0 : PARTY + i - 1, // Up
							i < Move.MAX_MOVES ? MOVES + i : (i < Trainer.MAX_POKEMON/3 ? DISCARD : (i < 2*Trainer.MAX_POKEMON/3 ? ITEMS : RETURN)), // Left
							i == Trainer.MAX_POKEMON -1 ? selectedTab : PARTY + i + 1}); // Down
		}
		
		moveButtons = new Button[Move.MAX_MOVES];
		for (int i = 0; i < Move.MAX_MOVES; i++)
		{
			buttons[MOVES + i] = moveButtons[i] = new Button(72, 122 + 69*i, 308, 61, boxHoverAction, 
					new int[] {PARTY + i, // Right
							i == 0 ? 0 : MOVES + i - 1, // Up
							i < Trainer.MAX_POKEMON/3 ? DISCARD : (i < 2*Trainer.MAX_POKEMON/3 ? ITEMS : RETURN), // Left, needs to behave like party buttons
							i == 3 ? selectedTab : MOVES + i + 1}); // Down
		}
		
		itemButtons = new Button[ITEMS_PER_PAGE];
		for (int i = 0, k = 0; i < ITEMS_PER_PAGE/2; i++)
		{
			for (int j = 0; j < 2; j++, k++)
			{
				buttons[ITEMS + k] = itemButtons[k] = new Button(421 + 160*j, 261 + 38*i, 148, 28, boxHoverAction,
						new int[] {j == 0 ? ITEMS + k + 1 : PARTY, // Right
								i == 0 ? USE : ITEMS + k - 2, // Up
								j == 1 ? ITEMS + k - 1 : PARTY, // Left
								i == ITEMS_PER_PAGE/2 - 1 ? (j == 0 ? LEFT_ARROW : RIGHT_ARROW) : ITEMS + k + 2}); // Down 
			}
		}
		
		buttons[LEFT_ARROW] = leftButton = new Button(498, 451, 35, 20, boxHoverAction, new int[] {RIGHT_ARROW, ITEMS_PER_PAGE - 2, PARTY, RETURN});
		buttons[RIGHT_ARROW] = rightButton = new Button(613, 451, 35, 20, boxHoverAction, new int[] {PARTY, ITEMS_PER_PAGE - 1, LEFT_ARROW, RETURN});
		
		buttons[GIVE] = giveButton = new Button(410, 193, 110, 38, boxHoverAction, new int[] {USE, selectedTab, PARTY, ITEMS});
		buttons[USE] = useButton = new Button(518, 193, 110, 38, boxHoverAction, new int[] {DISCARD, selectedTab, GIVE, ITEMS});
		buttons[DISCARD] = discardButton = new Button(628, 193, 110, 38, boxHoverAction, new int[] {PARTY, selectedTab, USE, ITEMS + 1});
		
		buttons[RETURN] = returnButton = new Button(410, 500, 328, 38, boxHoverAction, new int[] {PARTY, LEFT_ARROW, PARTY, selectedTab});
		
		movedToFront();
	}
	
	private void addUseMessages(boolean success, ActivePokemon p)
	{	
		if (success)
		{
			addMessage(player.getName() + " used the " + selected.getName() + "! " + ((UseItem)selected).getSuccessMessage(p));
			if (p != null) player.getPokedex().setStatus(p, PokedexStatus.CAUGHT); // TODO: This is hopefully a temporary solution to updating the Pokedex for Evolution by stone
		}
		else
		{
			addMessage("It won't have any effect.");	
		}
		
		useClicked = false;
		state = BagState.ITEM_SELECT;
		if (player.getBag().getQuantity(selected) == 0) changeCategory(selectedTab);
	}

	public void update(int dt, InputControl input, Game game)
	{
		if (message != null)
		{
			if (input.mouseDown)
			{
				input.consumeMousePress();
				message = null;
			}
			
			if (input.isDown(Control.SPACE))
			{
				input.consumeKey(Control.SPACE);
				message = null;
			}
			
			return;
		}
		
		selectedButton = Button.update(buttons, selectedButton, input);
		
		for (int i = 0; i < categories.length; i++)
		{
			if (tabButtons[i].isPress())
			{
				tabButtons[i].consumePress();
				changeCategory(i);
			}
		}
		
		for (int i = 0; i < Move.MAX_MOVES; ++i)
		{
			if (moveButtons[i].isPress())
			{
				moveButtons[i].consumePress();
				
				Move m = selectedPokemon.getMove(i);
				 
				addUseMessages(player.getBag().useMoveItem(selected, m), selectedPokemon);
				
				useClicked = false;
				state = BagState.ITEM_SELECT;
				if (player.getBag().getQuantity(selected) == 0) changeCategory(selectedTab);
			}
		}
		
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)
		{
			if (partyButtons[i].isPress())
			{
				partyButtons[i].consumePress();
				ActivePokemon p = player.getTeam().get(i);
				Bag bag = player.getBag();
				
				if (giveClicked)
				{
					addMessage(bag.giveItem(player, p, selected));
					giveClicked = false;
					selectedButton = GIVE;
					state = BagState.ITEM_SELECT;
					if (bag.getQuantity(selected) == 0) changeCategory(selectedTab);
					updateActiveButtons();
				}
				else if (useClicked)
				{			
					if (p.isEgg())
					{
						addUseMessages(false, p);
					}
					else if (selected instanceof PokemonUseItem) 
					{
						addUseMessages(bag.useItem(selected, p), p);
					}
					else if (selected instanceof MoveUseItem)
					{
						selectedPokemon = p;
						state = BagState.MOVE_SELECT;
					}
					
					updateActiveButtons();
				}
				else if (takeClicked)
				{
					addMessage(bag.takeItem(player, p));
					takeClicked = false;
					state = BagState.ITEM_SELECT;
					updateActiveButtons();
				}
			}
		}
		
		Set<Item> list = player.getBag().getCategory(categories[selectedTab]);
		Iterator<Item> iter = list.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) iter.next();
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++)
		{
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++)
			{
				Item item = iter.next();
				if (itemButtons[k].isPress())
				{
					itemButtons[k].consumePress();
					selected = item;
					updateActiveButtons();
				}
			}
		}
		
		if (giveButton.isPress())
		{
			giveButton.consumePress();
			if (!giveClicked) state = BagState.POKEMON_SELECT;
			else state = BagState.ITEM_SELECT;
			
			giveClicked = !giveClicked;
			useClicked = false;
			takeClicked = false;
			updateActiveButtons();
		}
		
		if (useButton.isPress())
		{
			useButton.consumePress();
			if (!useClicked) state = BagState.POKEMON_SELECT;
			else state = BagState.ITEM_SELECT;
			
			useClicked = !useClicked;
			giveClicked = false;
			takeClicked = false;
			updateActiveButtons();
			
			if (selected instanceof TrainerUseItem)
			{
				addUseMessages(player.getBag().useItem(selected, player), null);
			}
		}
		
		// Take has higher priority than discard so it is being implemented instead even though its location doesn't make sense
		if (discardButton.isPress())
		{
			discardButton.consumePress();
			if (!takeClicked) state = BagState.POKEMON_SELECT;
			else state = BagState.ITEM_SELECT;
			
			takeClicked = !takeClicked;
			giveClicked = false;
			useClicked = false;
			updateActiveButtons();
		}
		
		if (leftButton.isPress())
		{
			leftButton.consumePress();
			if (pageNum == 0) pageNum = totalPages(list.size()) - 1; 
			else pageNum--;
			updateActiveButtons();
		}
		
		if (rightButton.isPress())
		{
			rightButton.consumePress();
			if (pageNum == totalPages(list.size()) - 1) pageNum = 0;
			else pageNum++;
			updateActiveButtons();
		}
		
		if (returnButton.isPress())
		{
			returnButton.consumePress();
			game.setViewMode(ViewMode.MAP_VIEW);
		}

		if (input.isDown(Control.ESC))
		{
			input.consumeKey(Control.ESC);
			game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g, GameData data)
	{
		TileSet tiles = data.getMenuTiles();
		TileSet itemTiles = data.getItemTiles();
		TileSet partyTiles = data.getPartyTiles();
		TileSet bagTiles = data.getBagTiles();
		TileSet battleTiles = data.getBattleTiles();
		String s;
		
		Bag bag = player.getBag();
		List<ActivePokemon> team = player.getTeam();
		
		// Background
		g.drawImage(tiles.getTile(0x2), 0,0, null);
		
		// Info Boxes
		g.setColor(categories[selectedTab].getColor());
		g.fillRect(42, 92, 716, 466);
		
		g.drawImage(tiles.getTile(0x21), 42, 92, null);
		g.drawImage(tiles.getTile(0x22), 62, 112, null);
		
		// Item Display
		if (selected != null)
		{
			// Grey out selected and inactive buttons
			if (giveClicked) greyOut(g, giveButton, false);
			if (useClicked) greyOut(g, useButton, false);
			if (takeClicked) greyOut(g, discardButton, false);
			if (!giveButton.isActive()) greyOut(g, giveButton, true);
			if (!useButton.isActive()) greyOut(g, useButton, true);
			if (!discardButton.isActive()) greyOut(g, discardButton, true);

			// Draw item image
			BufferedImage img = itemTiles.getTile(selected.getIndex());
			g.drawImage(img, 430 - img.getWidth()/2, 132 - img.getHeight()/2, null);
			
			g.setColor(Color.BLACK);
			g.setFont(Global.getFont(20));
			g.drawString(selected.getName(), 448, 138);
			s = "x" + bag.getQuantity(selected);
			g.drawString(s, 726 - s.length()*10, 138);
			
			g.setFont(Global.getFont(14));
			Global.drawWrappedText(g, selected.getDesc(), 418, 156, 200, 5, 15);
			
			g.setFont(Global.getFont(20));
			g.drawImage(tiles.getTile(0x28), 410, 193, null);
			for (int i = 0; i < useNames.length; i++)
			{
				g.drawString(useNames[i], 470 + 109*i - 14*useNames[i].length()/2, 193 + 25);
			}
		}
		
		g.setFont(Global.getFont(12));
		g.setColor(Color.BLACK);
		
		// Draw each items in category
		Set<Item> list = bag.getCategory(categories[selectedTab]);
		Iterator<Item> iter = list.iterator();
		for (int i = 0; i < pageNum*ITEMS_PER_PAGE; i++) iter.next();
		for (int x = 0, k = 0; x < ITEMS_PER_PAGE/2; x++)
		{
			for (int y = 0; y < 2 && iter.hasNext(); y++, k++)
			{
				g.translate(itemButtons[k].x, itemButtons[k].y);
				Item item = iter.next();
				
				g.drawImage(tiles.getTile(0x26), 0,0, null);
				
				BufferedImage img = itemTiles.getTile(item.getIndex());
				g.drawImage(img, 14-img.getWidth()/2, 14-img.getHeight()/2, null);
				
				g.drawString(item.getName(), 29, 18);
				
				s = "x" + bag.getQuantity(item);
				g.drawString(s, 142 - s.length()*7, 18);
				
				g.translate(-itemButtons[k].x, -itemButtons[k].y);
			}
		}
		
		// Draw page numbers
		g.setFont(Global.getFont(16));
		s = (pageNum + 1) + "/" + totalPages(list.size());
		g.drawString(s, Global.centerX(s, 573, 16), 466);
		
		// Left and Right arrows
		g.translate(leftButton.x, leftButton.y);
		g.fillPolygon(leftArrowx, leftArrowy, leftArrowx.length);
		g.translate(-leftButton.x, -leftButton.y);
		g.translate(rightButton.x, rightButton.y);
		g.fillPolygon(rightArrowx, rightArrowy, rightArrowx.length);
		g.translate(-rightButton.x, -rightButton.y);
		
		// Draw moves
		if (state == BagState.MOVE_SELECT)
		{
			for (int i = 0; i < selectedPokemon.getMoves().size(); i++)
			{
				Move m = selectedPokemon.getMove(i);
				g.translate(moveButtons[i].x, moveButtons[i].y);
				
				g.setColor(m.getAttack().getActualType().getColor());
				g.fillRect(0, 0, moveButtons[i].w, moveButtons[i].h);
				
				g.drawImage(tiles.getTile(0x25), 0, 0, null);
				
				g.drawImage(battleTiles.getTile(m.getAttack().getActualType().getImageIndex()), 254, 14, null);
				g.drawImage(battleTiles.getTile(m.getAttack().getCategory().getImageNumber()), 254, 33, null);
				
				g.setColor(Color.BLACK);
				g.setFont(Global.getFont(14));
				g.drawString("PP: " + m.getPP() + "/" + m.getMaxPP(), 166, moveButtons[i].h/2 + 5);

				g.setColor(Color.BLACK);
				g.setFont(Global.getFont(20));
				g.drawString(m.getAttack().getName(), 20, 38);
				
				g.translate(-moveButtons[i].x, -moveButtons[i].y);
			}
		}
		// Draw Pokemon Info
		else
		{
			for (int i = 0; i < team.size(); i++)
			{
				g.translate(partyButtons[i].x, partyButtons[i].y);
				
				ActivePokemon p = team.get(i);
				Color[] typeColors = Type.getColors(p);
				
				g.setColor(typeColors[0]);
				g.fillPolygon(primaryColorx, primaryColory, 4);
				g.setColor(typeColors[1]);
				g.fillPolygon(secondaryColorx, secondaryColory, 4);
				
				g.drawImage(tiles.getTile(0x25), 0, 0, null);
				
				BufferedImage img = partyTiles.getTile(p.isEgg() ? 0x10000 : p.getPokemonInfo().getNumber());
				g.drawImage(img, 30 - img.getWidth()/2, 30 - img.getHeight()/2, null);
				
				if (p.isEgg())
				{
					g.setColor(Color.BLACK);
					g.setFont(Global.getFont(14));
					g.drawString(p.getName(), 50, 22);	
				}
				else
				{
					g.setColor(Color.BLACK);
					g.setFont(Global.getFont(14));
					g.drawString(p.getName() + " " + p.getGender().getCharacter(), 50, 22);
					g.drawString("Lv" + p.getLevel(), 153, 22);
					s = p.getStatus().getType().getName();
					g.drawString(s, Global.rightX(s, 293, 14), 22);
					
					g.fillRect(50, 26, 244, 11);
					g.setColor(Color.WHITE);
					g.fillRect(52, 28, 240, 7);
					
					g.setColor(p.getHPColor());
					g.fillRect(52, 28, (int)(p.getHPRatio()*240), 7);
					
					g.setColor(Color.BLACK);
					g.setFont(Global.getFont(12));
					g.drawString(p.getActualHeldItem().getName(), 50, 47);
					s = p.getHP()+"/"+p.getStat(Stat.HP);
					g.drawString(s, Global.rightX(s, 293, 12), 47);
					
					if (p.hasStatus(StatusCondition.FAINTED))
					{
						g.setColor(new Color(0, 0, 0, 128));
						g.fillRect(0, 0, 308, 61);
					}	
				}
				
				g.translate(-partyButtons[i].x, -partyButtons[i].y);
			}
		}
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		g.drawImage(tiles.getTile(0x27), 410, 500, null);
		g.drawString("Return", Global.centerX("Return", 573, 20), 525);
		
		for (int i = 0; i < categories.length; i++)
		{
			g.translate(tabButtons[i].x, tabButtons[i].y);
			
			g.setFont(Global.getFont(14));
			g.setColor(categories[i].getColor());
			g.fillRect(0, 0, 104, 52);
			
			if (selectedTab == i) g.drawImage(tiles.getTile(0x23), 0, 0, null);
			else g.drawImage(tiles.getTile(0x24), 0, 0, null);
			
			g.setColor(Color.BLACK);
			
			BufferedImage img = bagTiles.getTile(categories[i].getImageNumber());
			g.drawImage(img, 16-img.getWidth()/2, 26-img.getHeight()/2, null);
			g.drawString(categories[i].getName(), 30, 30);
			
			g.translate(-tabButtons[i].x, -tabButtons[i].y);
		}
		
		if (message != null){
			g.drawImage(battleTiles.getTile(0x3), 0, 440, null);
			g.setFont(Global.getFont(30));
			g.setColor(Color.WHITE);
			Global.drawWrappedText(g, message, 30, 490, 750);
		}else{
			for (Button b : buttons)
				b.draw(g);
		}
	}

	public ViewMode getViewModel()
	{
		return ViewMode.BAG_VIEW;
	}

	private void changeCategory(int index)
	{
		if (selectedTab != index) pageNum = 0;
		selectedTab = index;
		state = BagState.ITEM_SELECT;

		Set<Item> list = player.getBag().getCategory(categories[selectedTab]);
		selected = list.size() > 0 ? list.iterator().next() : null;
		
		updateActiveButtons();
	}

	public void movedToFront()
	{
		changeCategory(0);
	}
	
	private int totalPages(int size)
	{ 
		return size/ITEMS_PER_PAGE + (size == 0 || size%ITEMS_PER_PAGE != 0 ? 1 : 0);
	}
	
	private void greyOut(Graphics g, Button b, boolean totesBlacks)
	{
		Color temp = g.getColor();
		g.setColor(totesBlacks ? Color.BLACK : g.getColor().darker());
		g.fillRect(b.x, b.y, b.w, b.h);
		g.setColor(temp);
	}
	
	private void addMessage(String message)
	{
		this.message = message;
	}
	
	private ButtonHoverAction boxHoverAction = new ButtonHoverAction()
	{
		Stroke lineStroke = new BasicStroke(5f);
		int time = 0;
		public void draw(Graphics g, Button button) {
			time = (time+1)%80;
			g.setColor(new Color(0,0,0, 55+150*(Math.abs(time-40))/40));
			Graphics2D g2d = (Graphics2D)g;
			Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(lineStroke);
			g.drawRect(button.x-2, button.y-2, button.w+3, button.h+4);
			g2d.setStroke(oldStroke);
		}
	};
	
	private void updateActiveButtons()
	{
		List<ActivePokemon> team = player.getTeam();
		for (int i = 0; i < Trainer.MAX_POKEMON; i++)			
		{
			partyButtons[i].setActive(state == BagState.POKEMON_SELECT && i < team.size());
		}
		
		int displayed = player.getBag().getCategory(categories[selectedTab]).size();
		for (int i = 0; i < ITEMS_PER_PAGE; i++)
		{
			itemButtons[i].setActive(state == BagState.ITEM_SELECT && i < displayed - pageNum*ITEMS_PER_PAGE);
		}
		
		for (int i = 0; i < Move.MAX_MOVES; i++)
		{
			moveButtons[i].setActive(state == BagState.MOVE_SELECT && i < selectedPokemon.getMoves().size());
		}
		
		giveButton.setActive(selected instanceof HoldItem);
		useButton.setActive(selected instanceof UseItem);
	}
}
