package draw.handler;

import draw.panel.DrawPanel;
import draw.panel.Panel;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import pokemon.active.PartyPokemon;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

// Panel which holds and handles nickname displaying, capturing, and setting
public class NicknameHandler {
    // Panel will never have any background or anything, just the image/nickname label
    private final DrawPanel spacingPanel;

    private int maxLength;
    private Nicknamed selected;
    private BufferedImage selectedImage;

    private boolean finished;

    // Since this is all we really need
    // Basically is just creating a panel that is just a point (no width or height)
    public NicknameHandler(int centerX, int centerY) {
        this(new DrawPanel(centerX, centerY, 0, 0));
    }

    public NicknameHandler(Panel sizing) {
        this.spacingPanel = new DrawPanel(sizing)
                .withNoBackground()
                .withLabelSize(30);

        this.finished = true;
    }

    public void set(PartyPokemon selected) {
        TileSet spriteTiles = Game.getData().getPokemonTilesSmall();
        this.set(selected, spriteTiles.getTile(selected.getImageName()), PartyPokemon.MAX_NAME_LENGTH);
    }

    public void set(Nicknamed selected, BufferedImage image, int maxLength) {
        this.selected = selected;
        this.selectedImage = image;
        this.maxLength = maxLength;
        this.finished = false;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void update() {
        if (finished) {
            return;
        }

        InputControl input = InputControl.instance();
        if (!input.isCapturingText()) {
            input.startTextCapture();
        }

        if (input.consumeIfDown(ControlKey.ENTER)) {
            String nickname = input.stopAndResetCapturedText();
            selected.setNickname(nickname);
            this.finished = true;
        }
    }

    public void drawNickname(Graphics g) {
        String nickname = InputControl.instance().getInputCaptureString(maxLength);
        spacingPanel.withImageLabel(selectedImage, nickname)
                    .draw(g);
    }

    public interface Nicknamed {
        void setNickname(String nickname);
    }
}
