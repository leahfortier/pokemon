package draw.panel;

import input.ControlKey;
import input.InputControl;
import main.Game;
import pokemon.active.PartyPokemon;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

// Panel which holds and handles nickname displaying, capturing, and setting
public class NicknamePanel extends DrawPanel {
    private PartyPokemon selected;
    private BufferedImage selectedImage;

    private boolean finished;

    public NicknamePanel(Panel sizing) {
        this(sizing.getX(), sizing.getY(), sizing.getWidth(), sizing.getHeight());
    }

    public NicknamePanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.withNoBackground();
        this.withLabelSize(30);

        this.finished = true;
    }

    @Override
    public NicknamePanel withBorderlessTransparentBackground() {
        return (NicknamePanel)super.withBorderlessTransparentBackground();
    }

    @Override
    public NicknamePanel withBlackOutline() {
        return (NicknamePanel)super.withBlackOutline();
    }

    public void set(PartyPokemon selected) {
        this.selected = selected;
        this.selectedImage = Game.getData().getPokemonTilesSmall().getTile(selected.getImageName());
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

    @Override
    public void draw(Graphics g) {
        String nickname = InputControl.instance().getInputCaptureString(PartyPokemon.MAX_NAME_LENGTH);
        this.withImageLabel(selectedImage, nickname);

        super.draw(g);
    }
}
