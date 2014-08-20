package gui.view;

import gui.Button;
import gui.ButtonHoverAction;
import gui.GameData;
import gui.TileSet;
import item.Item;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import main.Game;
import main.Game.ViewMode;
import main.InputControl.Control;
import main.Global;
import main.InputControl;
import trainer.CharacterData;

public class MartView extends View
{
	private static String[] forSale = new String[] {"Potion", "Pok\u00e9 Ball", "Antidote", "Paralyze Heal", "Burn Heal"};
	
	private final int[] leftArrowX = {35, 19, 19, 3, 19, 19, 35};
	private final int[] leftArrowY = {5, 5, 0, 10, 20, 15, 15};
	private final int[] rightArrowX = {0, 16, 16, 32, 16, 16, 0};
	private final int[] rightArrowY = {5, 5, 0, 10, 20, 15, 15};
	
	private final int arrowY = 255;
	private final int leftArrowx = 285, rightArrowx = leftArrowx + 45;
	
	private CharacterData player;
	private Button[] itemButtons;
	private Button leftArrow, rightArrow;
	private Button buyButton;
	private Item[] itemsForSale;
	private Item selected;
	private int selectedIndex;
	private int amount;
	
	public MartView(CharacterData c)
	{
		player = c;
		
		itemsForSale = new Item[forSale.length];
		itemButtons = new Button[forSale.length];
		for (int i = 0; i < forSale.length; i++)
		{
			itemsForSale[i] = Item.getItem(forSale[i]);
			itemButtons[i] = new Button(500, 20+i*38, 180, 28, boxHoverAction);
		}
		
		leftArrow = new Button(leftArrowx, arrowY, 25, 20, boxHoverAction);
		rightArrow = new Button(rightArrowx, arrowY, 25, 20, boxHoverAction);
		
		buyButton = new Button(17, 330, 368, 28, boxHoverAction);
		
		setSelected(0);
	}

	public void update(int dt, InputControl input, Game game)
	{		
		for (int i = 0; i < itemButtons.length; i++)
		{
			Button b = itemButtons[i];
			b.update(input);
			if (b.isPress())
			{
				b.consumePress();
				setSelected(i);
			}
		}
		
		leftArrow.update(input);
		if (leftArrow.isPress())
		{
			leftArrow.consumePress(); 
			
			if (amount == 1) amount = player.getDatCashMoney()/selected.getPrice();
			else amount--;
		}
		
		rightArrow.update(input);
		if (rightArrow.isPress())
		{
			rightArrow.consumePress();
			
			if (amount ==  player.getDatCashMoney()/selected.getPrice()) amount = 1;
			else amount++;
		}
			
		buyButton.update(input);
		if (buyButton.isPress())
		{
			buyButton.consumePress();
			
			player.sucksToSuck(amount*selected.getPrice());
			for (int i = 0; i < amount; i++) player.getBag().addItem(selected);
			
			setSelected(selectedIndex);
		}
		
		if (input.mouseDown)
		{
			System.out.println(input.mouseX + " " + input.mouseY);
			input.consumeMousePress();
		}
		
		if (input.isDown(Control.ESC))
		{
			input.consumeKey(Control.ESC);
			game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g, GameData data)
	{
		Dimension d = Global.GAME_SIZE;
		
		g.setColor(new Color(68, 123, 184));
		g.fillRect(0, 0, d.width, d.height);
		
		g.setColor(Color.WHITE);
		g.fillRect(17, 20, 368, 28);
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		g.drawString("Money: "+Global.MONEY_SYMBOL+player.getDatCashMoney(), 28, 39);
		
		TileSet itemTiles = data.getItemTiles();
		
		g.setColor(Color.WHITE);
		g.fillRect(17, 75, 368, 110);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		
		BufferedImage img = itemTiles.getTile(selected.getIndex());
		g.drawImage(img, 20 + 14-img.getWidth()/2, 14-img.getHeight()/2 + 78, null);
		g.drawString(selected.getName(), 54, 100);
		g.drawString("In Bag: "+player.getBag().getQuantity(selected), 260, 100);
		
		g.setFont(Global.getFont(16));
		Global.drawWrappedText(g, selected.getDesc(), 30, 130, 368, 10, 17);
		
		g.setColor(Color.WHITE);
		g.fillRect(17, 200, 368, 110);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		
		g.drawString("Price: "+Global.MONEY_SYMBOL+selected.getPrice()*amount, 35, 230);
		g.drawString("Amount: "+amount, 35, 270);
		
		g.translate(leftArrowx, arrowY);
		g.fillPolygon(leftArrowX, leftArrowY, leftArrowX.length);
		g.translate(-leftArrowx, -arrowY);
		g.translate(rightArrowx, arrowY);
		g.fillPolygon(rightArrowX, rightArrowY, rightArrowX.length);
		g.translate(-rightArrowx, -arrowY);
		
		g.setColor(Color.WHITE);
		g.fillRect(buyButton.x, buyButton.y, buyButton.w, buyButton.h);
		
		g.setColor(Color.BLACK);
		g.setFont(Global.getFont(20));
		g.drawString("BUY", buyButton.x + buyButton.w/2 - 18, buyButton.y + buyButton.h/2 + 8);
		
		g.setFont(Global.getFont(12));
		for (int i = 0; i < itemButtons.length; i++)
		{
			Button b = itemButtons[i];
			g.setColor(Color.WHITE);
			g.fillRect(b.x, b.y, b.w, b.h);
			b.draw(g);
			
			g.setColor(Color.BLACK);
			Item item = itemsForSale[i];
			img = itemTiles.getTile(item.getIndex());
			g.drawImage(img, b.x + 14-img.getWidth()/2, 14-img.getHeight()/2 + b.y, null);

			g.drawString(item.getName(), b.x+30, 19+b.y);
			
			if (item.getPrice() > player.getDatCashMoney()) g.setColor(Color.RED);
			String priceStr = Global.MONEY_SYMBOL+item.getPrice();
			g.drawString(priceStr, b.x + 170 - priceStr.length()*6, 19+b.y);
		}
	}

	public ViewMode getViewModel()
	{
		return ViewMode.MART_VIEW;
	}

	public void movedToFront() {}
	
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
	
	private void setSelected(int index)
	{
		selectedIndex = index;
		selected = itemsForSale[index];
		amount = itemsForSale[index].getPrice() <= player.getDatCashMoney() ? 1 : 0;
	}
}
