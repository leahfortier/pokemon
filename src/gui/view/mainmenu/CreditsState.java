package gui.view.mainmenu;

import draw.TextUtils;
import draw.button.Button;
import gui.view.mainmenu.VisualState.VisualStateHandler;
import main.Global;
import sound.SoundTitle;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

class CreditsState implements VisualStateHandler {

    private static final String creditsHoz = "TEAM ROCKET    TEAM ROCKET";
    private static final String[] creditsText = { "",
            "Team Rocket", "",
            "Lead Programmers", "Leah Fortier", "Tyler Brazill", "Maxwell Miller", "",
            "Graphic Designers", "Josh Linge", "Jeb Ralston", "Jessica May", "",
            "Writers", "Jeb Ralston", "Jessica May", "",
            "UML MASTER", "Jeb Ralston", "",
            "Nintendo", "",
            "Game Freak", "",
            "Smogon XY Sprite Project", "",
            "Smogon Sun/Moon Sprite Project", "",
            "credits", "credits", "credits"
    };
    
    private int creditsTime1;
    private int creditsTime2;
    
    @Override
    public void set() {
        creditsTime1 = 0;
        creditsTime2 = 0;
    }
    
    @Override
    public void draw(Graphics g, MainMenuView view) {
        Dimension d = Global.GAME_SIZE;
        g.setClip(0, 220, d.width, d.height - 240);
        
        g.setColor(new Color(0, 0, 0, 64));
        FontMetrics.setFont(g, 512);
        g.drawString(creditsHoz, d.width - creditsTime2, d.height - 30);
        
        g.setColor(Color.BLACK);
        
        for (int i = 1; i < creditsText.length; i++) {
            if (creditsText[i - 1].isEmpty()) {
                FontMetrics.setFont(g, 40);
                TextUtils.drawCenteredWidthString(g, creditsText[i], d.width/2, i*40 + d.height - creditsTime1/5);
            }
            else {
                FontMetrics.setFont(g, 30);
                TextUtils.drawCenteredWidthString(g, creditsText[i], d.width/2, i*40 + d.height - creditsTime1/5);
            }
        }
        
        g.setClip(0, 0, d.width, d.height);
    }
    
    @Override
    public void update(MainMenuView view) {
        // TODO: These should be constants once I find out what they are
        creditsTime1 += 10;
        creditsTime1 %= 8000;
        
        creditsTime2 += 10;
        creditsTime2 %= 8500;
    }
    
    @Override
    public Button[] getButtons() {
        return new Button[0];
    }
    
    @Override
    public SoundTitle getTunes() {
        return SoundTitle.CREDITS_TUNE;
    }
}
