package gui.view;

import draw.TextUtils;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import sound.SoundPlayer;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

class OptionsView extends View {
    private boolean musicOn;
    private Color color;
    
    OptionsView() {
        musicOn = SoundPlayer.soundPlayer.isMuted();
        color = new Color(0, 0, 0);
    }
    
    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();
        if (input.consumeIfMouseDown(ControlKey.SPACE)) {
            musicOn = !musicOn;
            SoundPlayer.soundPlayer.toggleMusic();
        }
        
        if (input.consumeIfDown(ControlKey.ESC)) {
            Game.instance().popView();
        }
    }
    
    private void setNextColor() {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        
        r += 13;
        g += 34;
        b += 21;
        
        r %= 255;
        g %= 255;
        b %= 255;
        
        this.color = new Color(r, g, b);
    }
    
    @Override
    public void draw(Graphics g) {
        Dimension d = Global.GAME_SIZE;

//        g.setColor(Color.BLACK);
        g.setColor(color);
        setNextColor();
        
        g.fillRect(0, 0, d.width, d.height);
        
        g.setColor(Color.WHITE);
        FontMetrics.setFont(g, 150);
        TextUtils.drawCenteredWidthString(g, "VOLUME", d.width/2, d.height/4);
        
        if (musicOn) {
            g.setColor(Color.GREEN);
            g.fillRect(d.width/2, d.height/2 - 50, 200, 100);
            
            g.setColor(Color.DARK_GRAY);
            g.fillRect(d.width/2 - 200, d.height/2 - 50, 200, 100);
            
            g.setColor(Color.BLACK);
            FontMetrics.setFont(g, 100);
            g.drawString("OFF", d.width/2 - 180, d.height/2 + 32);
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(d.width/2, d.height/2 - 50, 200, 100);
            
            g.setColor(Color.RED);
            g.fillRect(d.width/2 - 200, d.height/2 - 50, 200, 100);
            
            g.setColor(Color.BLACK);
            FontMetrics.setFont(g, 100);
            g.drawString("ON", d.width/2 + 40, d.height/2 + 32);
        }
    }
    
    @Override
    public ViewMode getViewModel() {
        return ViewMode.OPTIONS_VIEW;
    }
    
    @Override
    public void movedToFront() {}
}
