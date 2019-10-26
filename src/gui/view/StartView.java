package gui.view;

import draw.panel.BasicPanels;
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
import util.FontMetrics;
import util.string.PokeString;

import java.awt.Graphics;

class StartView extends View {

    private static final MessageUpdate[] dialogue = new MessageUpdate[] {
            new MessageUpdate("Welcome to the world of " + PokeString.POKEMON + "!"),
            new MessageUpdate("It's filled with many unique creatures, such as this Ditto.").withUpdate(MessageUpdateType.SHOW_POKEMON),
            new MessageUpdate("The people of the Hash Map region befriend, travel, and battle with their " + PokeString.POKEMON + "."),
            new MessageUpdate("Oh, have you seen any syrup aboot? Well, never mind..."),
            new MessageUpdate("I can see quite clearly that you're a boy, so what's your name, eh?").withUpdate(MessageUpdateType.ENTER_NAME),
            new MessageUpdate(", are you ready to start your epic adventure? Well, off you go! I'll be seeing you soon!").withUpdate(MessageUpdateType.APPEND_TO_NAME)
    };

    private State state;

    private int dialogueIndex;
    private String message;

    private boolean ditto;

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        Player player = Game.getPlayer();
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
                        message = player.getName() + message;
                        state = State.DEFAULT;
                        break;
                    default:
                        state = State.DEFAULT;
                        break;
                }
            }
        }

        if (state == State.NAME) {
            if (!input.isCapturingText()) {
                input.startTextCapture();
            }

            if (input.consumeIfDown(ControlKey.ENTER)) {
                input.stopTextCapture();

                String name = input.getCapturedText(Player.MAX_NAME_LENGTH);
                player.setName(name.isEmpty() ? Player.DEFAULT_NAME : name);

                state = State.DEFAULT;
                message = null;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();

        IndexTileSet trainerTiles = data.getTrainerTiles();
        TileSet pokemonTiles = data.getPokemonTilesSmall();

        BasicPanels.drawCanvasPanel(g);

        FontMetrics.setBlackFont(g, 30);

        switch (state) {
            case DEFAULT:
                g.drawImage(trainerTiles.getTile(0x58), 200, 200, null);
                if (ditto) {
                    g.drawImage(pokemonTiles.getTile(PokemonNamesies.DITTO.getInfo().getImageName()), 270, 255, null);
                }
                break;
            case NAME:
                g.drawImage(trainerTiles.getTile(0x4), 200, 230, null);
                g.drawString(InputControl.instance().getInputCaptureString(Player.MAX_NAME_LENGTH), 300, 260);
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
