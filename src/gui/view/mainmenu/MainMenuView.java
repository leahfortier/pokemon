package gui.view.mainmenu;

import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonTransitions;
import gui.view.View;
import gui.view.ViewMode;
import input.ControlKey;
import input.InputControl;
import main.Game;
import save.SaveInfo;
import save.Settings;
import sound.SoundPlayer;
import util.FileIO;
import util.Folder;
import util.FontMetrics;
import util.TimeUtils;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MainMenuView extends View {
    static final int[] bgt = new int[] { 800, 1800, 1000, 2400, 1000 };

    static final int NUM_MAIN_BUTTONS = 4;
    private static final int BUTTON_WIDTH = 400;

    private static final BufferedImage MAIN_LOGO = FileIO.readImage(Folder.IMAGES + "MainLogo.png");

    private VisualState state;
    private Settings settings;

    private SaveInfo[] saveInfo;

    private boolean musicStarted = false;

    private int bgTime;
    private int bgIndex;

    public MainMenuView() {
        bgTime = 0;
        bgIndex = 0;

        settings = Settings.load();
        saveInfo = SaveInfo.updateSaveData();

        state = VisualState.MAIN;
    }

    Settings getSettings() {
        return this.settings;
    }

    void setVisualState(VisualState newState) {
        state = newState;
        state.set();
    }

    boolean hasSavedInfo(int saveNum) {
        return this.saveInfo[saveNum] != null;
    }

    void loadSave(int index) {
        Game.instance().loadPlayer(this.saveInfo[index].getPlayer());
    }

    void reloadSaveInfo() {
        this.saveInfo = SaveInfo.updateSaveData();
    }

    @Override
    public void update(int dt) {
        if (!musicStarted) {
            musicStarted = true;
            SoundPlayer.instance().playMusic(state.getTunes());
        }

        this.state.update(this);

        int nextIndex = (bgIndex + 1)%bgt.length;
        bgTime += 10;

        if (bgTime > bgt[nextIndex]) {
            bgTime -= bgt[nextIndex];
            bgIndex = nextIndex;
        }

        if (InputControl.instance().consumeIfDown(ControlKey.BACK)) {
            setVisualState(VisualState.MAIN);
        }
    }

    @Override
    public void draw(Graphics g) {
        Theme theme = settings.getTheme();
        theme.draw(g, bgTime, bgIndex);

        g.drawImage(MAIN_LOGO, 95, 54, null);

        this.state.draw(g, this);
    }

    void drawSaveInformation(Graphics g, Button b, int index, String emptyText) {
        g.setColor(Color.BLACK);
        SaveInfo info = saveInfo[index];

        if (info != null) {
            g.translate(b.x, b.y);

            FontMetrics.setFont(g, 20);

            g.drawString("Name:", 16, 28);
            TextUtils.drawRightAlignedString(g, info.getName(), 189, 28);

            g.drawString("Time:", 210, 28);
            TextUtils.drawRightAlignedString(g, "" + TimeUtils.formatSeconds(info.getSeconds()), 383, 28);

            g.drawString("Badges:", 16, 58);
            TextUtils.drawRightAlignedString(g, "" + info.getBadges(), 189, 58);

            g.drawString(PokeString.POKEDEX + ":", 210, 58);
            TextUtils.drawRightAlignedString(g, "" + info.getPokemonSeen(), 383, 58);

            g.translate(-b.x, -b.y);
        } else {
            b.label(g, 30, emptyText);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.MAIN_MENU_VIEW;
    }

    @Override
    public void movedToFront() {
        setVisualState(VisualState.MAIN);
        saveInfo = SaveInfo.updateSaveData();
    }

    static Button createMenuButton(int index) {
        return createMenuButton(index, ButtonTransitions.getBasicTransitions(index, NUM_MAIN_BUTTONS, 1));
    }

    static Button createMenuButton(int index, ButtonTransitions transitions) {
        return new Button(
                200,
                240 + index*85,
                BUTTON_WIDTH,
                75,
                ButtonHoverAction.BOX,
                transitions
        );
    }
}
