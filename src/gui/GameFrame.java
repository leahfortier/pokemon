package gui;

import item.Item;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.Timer;

import battle.Attack;
import battle.effect.BattleEffect;
import battle.effect.PokemonEffect;
import battle.effect.TeamEffect;
import main.Game;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import main.StuffGen;
import pokemon.Ability;
import pokemon.PokemonInfo;

public class GameFrame
{
//	 public static boolean GENERATE_STUFF = true;
	public static boolean GENERATE_STUFF = false;
	public static boolean DEV_MODE = true;

	private static JFrame frame;

	public static void main(String[] args)
	{
		if (GENERATE_STUFF)
		{
			new StuffGen();
			
			// Make sure these don't throw any errors
			PokemonInfo.loadPokemonInfo();
			Attack.loadMoves();
			PokemonEffect.loadEffects();
			TeamEffect.loadEffects();
			BattleEffect.loadEffects();
			Ability.loadAbilities();
			Item.loadItems();
			
			System.out.println("GEN GEN GEN");
			
			return;
		}

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setSize(800, 622);

		Canvas gui = new Canvas();
		gui.setSize(Global.GAME_SIZE);
		frame.getContentPane().add(gui);
		frame.pack();
		frame.setVisible(true);
		// frame.setResizable(false);
		// frame.setSize(Global.GAME_SIZE);
		// frame.pack();

		Thread gameThread = new Thread(new GameLoop(gui));
		gameThread.start();

		gui.requestFocusInWindow();
	}

	private static class GameLoop implements Runnable
	{
		private Canvas gui;
		private Game game;
		private InputControl control;
		private BufferStrategy strategy;

		private DevConsole console;

		public GameLoop(Canvas canvas)
		{
			gui = canvas;
			control = new InputControl();
			canvas.addKeyListener(control);
			canvas.addMouseListener(control);
			canvas.addMouseMotionListener(control);

			if (DEV_MODE) console = new DevConsole();
		}

		public void run()
		{
			gui.createBufferStrategy(2);
			strategy = gui.getBufferStrategy();

			// load font thingy
			Graphics g = strategy.getDrawGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, 800, 600);
			g.setColor(Color.white);
			g.setFont(Global.getFont(72));
			g.drawString("LOADING...", 30, 570);
			g.dispose();
			strategy.show();

			game = new Game();

			Timer fpsTimer = new Timer((int) Global.MS_BETWEEN_FRAMES, new ActionListener()
			{
				int frameCount = 0;
				long fpsTime = 0;
				long prevTime = System.currentTimeMillis();

				public void actionPerformed(ActionEvent e)
				{
					long time = System.currentTimeMillis();
					long dt = time - prevTime;
					fpsTime += dt;
					prevTime = time;
					if (fpsTime > 1000)
					{
						fpsTime %= 1000;
						frame.setTitle("Pokemon++          FPS:" + frameCount);
						frameCount = 1;
					}
					else frameCount++;
					drawFrame((int) dt);
				}
			});
			
			fpsTimer.setCoalesce(true);
			fpsTimer.start();
		}

		void drawFrame(int dt)
		{
			game.update(dt, control);

			Graphics g = strategy.getDrawGraphics();
			game.draw(g);

			// This will fail if it can't acquire the lock on control (just
			// won't display or anything)
			if (control.isDown(Control.CONSOLE))
			{
				control.consumeKey(Control.CONSOLE);
				console.init(control);
			}

			if (DEV_MODE && console.isShown())
			{
				console.update(dt, control, game);
				console.draw(g, game.data);
			}

			g.dispose();

			strategy.show();
		}
	}
}
