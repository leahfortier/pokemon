package gui.view;

import draw.ImageUtils;
import draw.handler.NicknameHandler;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.IndexTileSet;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import message.MessageUpdate;
import message.MessageUpdateType;
import pokemon.species.PokemonNamesies;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.player.Player;
import util.Point;
import util.string.PokeString;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

class StartView extends View {
    private static final MessageUpdate[] dialogue = new MessageUpdate[] {
            new MessageUpdate("Welcome to the world of " + PokeString.POKEMON + "!"),
            new MessageUpdate("It's filled with many unique creatures, such as this Ditto.").withUpdate(MessageUpdateType.SHOW_POKEMON),
            new MessageUpdate("The people of the Hash Map region befriend, travel, and battle with their " + PokeString.POKEMON + "."),
            new MessageUpdate("So what's your name, eh?").withUpdate(MessageUpdateType.ENTER_NAME),
            new MessageUpdate(", are you ready to start your epic adventure? Well, off you go! I'll be seeing you soon!").withUpdate(MessageUpdateType.APPEND_TO_NAME)
    };

    private final Point mapleDraw;
    private final Point dittoDraw;

    private final NicknameHandler nicknameHandler;
    private final BufferedImage playerImage;
    private final BufferedImage mapleImage;
    private final BufferedImage dittoImage;

    private State state;

    private int dialogueIndex;
    private String message;

    private boolean ditto;

    public StartView() {
        GameData data = Game.getData();

        IndexTileSet trainerTiles = data.getTrainerTiles();
        TileSet pokemonTiles = data.getPokemonTilesSmall();

        playerImage = trainerTiles.getTile(0x4);
        mapleImage = trainerTiles.getTile(0x58);
        dittoImage = pokemonTiles.getTile(PokemonNamesies.DITTO.getInfo().getImageName());

        DrawPanel messagelessCanvas = BasicPanels.newMessagelessCanvasPanel();
        int centerY = messagelessCanvas.y + 2*messagelessCanvas.height/3;
        int mapleX = messagelessCanvas.x + messagelessCanvas.width/4;
        int dittoX = mapleX + mapleImage.getWidth() + 5;

        this.mapleDraw = new Point(mapleX, centerY);
        this.dittoDraw = new Point(dittoX, centerY);
        this.nicknameHandler = new NicknameHandler(messagelessCanvas.centerX(), centerY - mapleImage.getHeight()/2);
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        if (message != null && input.consumeIfMouseDown(ControlKey.SPACE)) {
            message = null;
        }

        if (message == null) {
            if (dialogueIndex == dialogue.length - 1) {
                Game.instance().setViewMode(ViewMode.MAP_VIEW);
            } else {
                dialogueIndex++;
                message = dialogue[dialogueIndex].getMessage();
                switch (dialogue[dialogueIndex].getUpdateType()) {
                    case ENTER_NAME:
                        state = State.NAME;
                        // Return here so that there's time for the message displayed when entering a name
                        // to start animating before starting to capture input
                        return;
                    case SHOW_POKEMON:
                        ditto = true;
                        state = State.DEFAULT;
                        break;
                    case APPEND_TO_NAME:
                        message = Game.getPlayer().getName() + message;
                        state = State.DEFAULT;
                        break;
                    default:
                        state = State.DEFAULT;
                        break;
                }
            }
        }

        if (state == State.NAME) {
            nicknameHandler.update();
            if (nicknameHandler.isFinished()) {
                state = State.DEFAULT;
                message = null;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);

        switch (state) {
            case DEFAULT:
                ImageUtils.drawBottomCenteredImage(g, mapleImage, mapleDraw);
                if (ditto) {
                    ImageUtils.drawBottomCenteredImage(g, dittoImage, dittoDraw);
                }
                break;
            case NAME:
                nicknameHandler.drawNickname(g);
                break;
        }

        if (message != null) {
            BasicPanels.drawFullMessagePanel(g, message);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.START_VIEW;
    }

    @Override
    public void movedToFront() {
        nicknameHandler.set(Game.getPlayer(), playerImage, Player.MAX_NAME_LENGTH);

        state = State.DEFAULT;

        dialogueIndex = 0;
        message = dialogue[dialogueIndex].getMessage();

        ditto = false;

        SoundPlayer.instance().playMusic(SoundTitle.NEW_GAME);
    }

    private enum State {
        DEFAULT,
        NAME
    }
}
