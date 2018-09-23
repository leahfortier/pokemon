package gui;

import draw.DrawUtils;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.species.PokemonInfo;
import util.FontMetrics;
import util.RandomUtils;
import util.TimeUtils;
import util.file.FileIO;
import util.string.PokeString;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Taskbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class GameFrame {
    private static final boolean DEV_MODE = true;

    private static final JFrame frame = new JFrame();

    public static void main(String[] args) throws InterruptedException {
        frame.setVisible(true);

        Canvas gui = new Canvas();
        gui.setSize(Global.GAME_SIZE);

        frame.getContentPane().add(gui);

        frame.setTitle(Global.TITLE);

        BufferedImage frameIcon = FileIO.readImage(Global.FRAME_ICON);
        frame.setIconImage(frameIcon);
        Taskbar.getTaskbar().setIconImage(frameIcon);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();

        Thread gameThread = new Thread(new GameLoop(gui));
        gameThread.start();

        gui.requestFocusInWindow();
    }

    private static void loadAllTheThings() {
        System.out.println("Random Seed: " + RandomUtils.getSeed());

        PokemonInfo.loadPokemonInfo();
        FontMetrics.loadFontMetricsMap();
    }

    private static class GameLoop implements Runnable {
        private final Canvas gui;
        private final DevConsole console;

        private BufferStrategy strategy;

        private GameLoop(Canvas canvas) {
            gui = canvas;
            InputControl control = InputControl.instance();

            canvas.addKeyListener(control);
            canvas.addMouseListener(control);
            canvas.addMouseMotionListener(control);

            console = new DevConsole();
        }

        @Override
        public void run() {
            gui.createBufferStrategy(2);
            strategy = gui.getBufferStrategy();

            Graphics g = strategy.getDrawGraphics();
            DrawUtils.fillCanvas(g, Color.BLACK);

            g.setColor(Color.WHITE);
            FontMetrics.setFont(g, 72);
            g.drawString("LOADING...", 30, 570);
            g.dispose();
            strategy.show();

            Game.instance();
            loadAllTheThings();

            Timer fpsTimer = new Timer((int)Global.MS_BETWEEN_FRAMES, new ActionListener() {
                private int frameCount = 0;
                private long fpsTime = 0;
                private long prevTime = TimeUtils.getCurrentTimestamp();

                @Override
                public void actionPerformed(ActionEvent event) {
                    long time = TimeUtils.getCurrentTimestamp();
                    long dt = time - prevTime;

                    fpsTime += dt;
                    prevTime = time;

                    if (fpsTime > 1000) {
                        fpsTime %= 1000;
                        frame.setTitle(PokeString.POKEMON + "++          FPS:" + frameCount);
                        frameCount = 1;
                    } else {
                        frameCount++;
                    }

                    drawFrame((int)dt);
                }
            });

            fpsTimer.setCoalesce(true);
            fpsTimer.start();
        }

        private void drawFrame(int dt) {
            Game.instance().update(dt);

            Graphics g = strategy.getDrawGraphics();
            Game.instance().draw(g);

            // This will fail if it can't acquire the lock on control (just won't display or anything)
            if (DEV_MODE) {
                if (InputControl.instance().consumeIfDown(ControlKey.CONSOLE)) {
                    console.init();
                }

                if (console.isShown()) {
                    console.update();
                    console.draw(g);
                }
            }

            g.dispose();

            strategy.show();
        }
    }
}
