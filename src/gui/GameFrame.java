package gui;

import main.Game;
import main.Global;
import pokemon.PokemonInfo;
import util.DrawUtils;
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
	private static final boolean DEV_MODE = true;

	private static JFrame frame;

	public static void main(String[] args) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		Canvas gui = new Canvas();
		gui.setSize(Global.GAME_SIZE);
		frame.setResizable(false);

		frame.getContentPane().add(gui);
		frame.pack();
		frame.setVisible(true);

		Thread gameThread = new Thread(new GameLoop(gui));
		gameThread.start();

		gui.requestFocusInWindow();
	}
	
	private static void loadAllTheThings() {
		PokemonInfo.loadPokemonInfo();
		DrawUtils.loadFontMetricsMap();
	}

	private static class GameLoop implements Runnable {
		private final Canvas gui;
		private final InputControl control;
		private final DevConsole console;

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
			DrawUtils.setFont(g, 72);
			g.drawString("LOADING...", 30, 570);
			g.dispose();
			strategy.show();

			Game.create();
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
			Game.update(dt, control);

			Graphics g = strategy.getDrawGraphics();
			Game.draw(g);

			// This will fail if it can't acquire the lock on control (just won't display or anything)
			if (control.isDown(Control.CONSOLE)) {
				control.consumeKey(Control.CONSOLE);
				console.init(control);
			}

			if (DEV_MODE && console.isShown()) {
				console.update(dt, control);
				console.draw(g);
			}

			g.dispose();

			strategy.show();
		}
	}
}
