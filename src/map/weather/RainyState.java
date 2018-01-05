package map.weather;

import draw.DrawUtils;
import main.Global;
import map.weather.WeatherState.WeatherDrawer;
import util.RandomUtils;

import java.awt.Color;
import java.awt.Graphics;

class RainyState implements WeatherDrawer {
    
    private int[] rainHeight;
    private int lightningFrame;
    
    RainyState() {
        rainHeight = new int[Global.GAME_SIZE.width/2];
    }
    
    // TODO: Make a fuckton of constants and whatever this is awful
    @Override
    public void draw(Graphics g) {
        DrawUtils.fillCanvas(g, new Color(0, 0, 0, 64));
        
        g.setColor(new Color(50, 50, 255, 128));
        
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
            int x = RandomUtils.getRandomInt(rainHeight.length);
            if (rainHeight[x] == 0) {
                rainHeight[x] = 1 + RandomUtils.getRandomInt(40);
            }
        }
        
        if (RandomUtils.getRandomInt(80) == 0 || (lightningFrame > 80 && RandomUtils.chanceTest(1, 4))) {
            lightningFrame = 128;
        }
        
        if (lightningFrame > 0) {
            DrawUtils.fillCanvas(g, new Color(255, 255, 255, lightningFrame));
            lightningFrame = 7*lightningFrame/8 - 1;
        } else {
            lightningFrame = 0;
        }
    }
}
