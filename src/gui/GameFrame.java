package gui;

import battle.Attack;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import generator.StuffGen;
import item.Item;
import main.Game;
import main.Global;
import pokemon.Ability;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import util.DrawMetrics;
import util.InputControl;
import util.InputControl.Control;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;

public class GameFrame {
	public static final boolean GENERATE_STUFF = true;
//	public static final boolean GENERATE_STUFF = false;
	private static final boolean DEV_MODE = true;

	private static JFrame frame;

	public static void main(String[] args) {
		if (GENERATE_STUFF) {
			new StuffGen();

			// Make sure these don't throw any errors
			loadAllTheThings();

			// Load all maps and test if all triggers and NPC data is correct
			Game g = new Game();
			g.data.testMaps(new CharacterData(g));

			System.out.println("GEN GEN GEN");
			
			return;
		}

		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// frame.setSize(800, 622);

		Canvas gui = new Canvas();
		gui.setSize(Global.GAME_SIZE);
		frame.setResizable(false);

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
	
	private static void loadAllTheThings() {
		PokemonInfo.loadPokemonInfo();
		Attack.loadMoves();
		PokemonEffect.loadEffects();
		TeamEffect.loadEffects();
		BattleEffect.loadEffects();
		Ability.loadAbilities();
		Item.loadItems();
		DrawMetrics.loadFontMetricsMap();
	}

	private static class GameLoop implements Runnable {
		private final Canvas gui;
		private final InputControl control;
		private final DevConsole console;
		
		private Game game;
		private BufferStrategy strategy;

		private GameLoop(Canvas canvas) {
			gui = canvas;
			control = new InputControl();
			
			canvas.addKeyListener(control);
			canvas.addMouseListener(control);
			canvas.addMouseMotionListener(control);

			console = DEV_MODE ? new DevConsole() : null;
		}

		public void run() {
			gui.createBufferStrategy(2);
			strategy = gui.getBufferStrategy();

			// Load font thingy
			Graphics g = strategy.getDrawGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			
			g.setColor(Color.WHITE);
			DrawMetrics.setFont(g, 72);
			g.drawString("LOADING...", 30, 570);
			g.dispose();
			strategy.show();

			game = new Game();
			loadAllTheThings();

			Timer fpsTimer = new Timer((int) Global.MS_BETWEEN_FRAMES, new ActionListener() {
				int frameCount = 0;
				long fpsTime = 0;
				long prevTime = System.currentTimeMillis();

				public void actionPerformed(ActionEvent event) {
					long time = System.currentTimeMillis();
					long dt = time - prevTime;
					
					fpsTime += dt;
					prevTime = time;
					
					if (fpsTime > 1000) {
						fpsTime %= 1000;
						frame.setTitle("Pokemon++          FPS:" + frameCount);
						frameCount = 1;
					}
					else {
						frameCount++;
					}
					
					drawFrame((int) dt);
				}
			});
			
			fpsTimer.setCoalesce(true);
			fpsTimer.start();
		}

		private void drawFrame(int dt) {
			game.update(dt, control);

			Graphics g = strategy.getDrawGraphics();
			game.draw(g);

			// This will fail if it can't acquire the lock on control (just won't display or anything)
			if (control.isDown(Control.CONSOLE)) {
				control.consumeKey(Control.CONSOLE);
				console.init(control);
			}

			if (DEV_MODE && console.isShown()) {
				console.update(dt, control, game);
				console.draw(g, game.data);
			}

			g.dispose();

			strategy.show();
		}
	}
}
