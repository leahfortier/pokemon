package map.weather;

import main.Global;

import java.awt.Color;
import java.awt.Graphics;

class SunnyState extends WeatherDrawer {
    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(255, 255, 255, 64));
        g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
    }
}
