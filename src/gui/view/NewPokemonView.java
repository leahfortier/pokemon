package gui.view;

import gui.button.Button;
import gui.panel.BasicPanels;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import util.DrawUtils;
import util.PokeString;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class NewPokemonView extends View {
    private static final int NUM_COLS = 4;

    private final Button[] buttons;

    private ActivePokemon newPokemon;

    private State state;
    private String message;
    private int selectedButton;

    private String nickname;

    private enum State {
        POKEDEX,
        NICKNAME_QUESTION,
        NICKNAME,
        END
    }

    NewPokemonView() {
        this.buttons = BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS);
        for (Button button : buttons) {
            button.setActive(false);
        }
    }

    // Bottom center left
    private Button yesButton() {
        return buttons[NUM_COLS + 1];
    }

    // Bottom center right
    private Button noButton() {
        return buttons[NUM_COLS + 2];
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();

        switch (state) {
            case POKEDEX:
                if (input.consumeIfDown(ControlKey.SPACE)) {
                    message = null;
                }

                if (message == null) {
                    setState(State.NICKNAME_QUESTION);
                }
                break;
            case NICKNAME_QUESTION:
                selectedButton = Button.update(buttons, selectedButton);
                if (noButton().checkConsumePress()) {
                    setState(State.END);
                }

                if (yesButton().checkConsumePress()) {
                    setState(State.NICKNAME);
                }
                break;
            case NICKNAME:
                if (!input.isCapturingText()) {
                    input.startTextCapture();
                }

                nickname = input.getCapturedText();
                if (nickname.length() > ActivePokemon.MAX_NAME_LENGTH) {
                    nickname = nickname.substring(0, ActivePokemon.MAX_NAME_LENGTH);
                }

                if (input.consumeIfDown(ControlKey.ENTER)) {
                    input.stopTextCapture();
                    newPokemon.setNickname(nickname);
                    setState(State.END);
                }
                break;
            case END:
                if (input.consumeIfDown(ControlKey.SPACE)) {
                    message = null;
                }

                if (message == null) {
                    Game.instance().setViewMode(ViewMode.MAP_VIEW);
                }
                break;
        }
    }

    private void drawButton(Graphics g, Button button, Color color, String label) {
        button.fillBordered(g, color);
        button.blackOutline(g);
        button.label(g, 30, label);
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);
        if (message != null) {
            BasicPanels.drawFullMessagePanel(g, message);
        }

        switch (state) {
            case POKEDEX:
                BufferedImage pokemonImage = Game.getData().getPokedexTilesLarge().getTile(newPokemon.getTinyImageIndex());
                DrawUtils.drawCenteredImage(g, pokemonImage, Global.GAME_SIZE.width/2, BasicPanels.getMessagePanelY()/2);
                break;
            case NICKNAME_QUESTION:
                drawButton(g, yesButton(), new Color(120, 200, 80), "Yes");
                drawButton(g, noButton(), new Color(220, 20, 20), "No");
                break;
            case NICKNAME:
                StringBuilder display = new StringBuilder();
                for (int i = 0; i < ActivePokemon.MAX_NAME_LENGTH; i++) {
                    if (i < nickname.length()) {
                        display.append(nickname.charAt(i));
                    }
                    else {
                        display.append("_");
                    }

                    display.append(" ");
                }

                g.drawString(display.toString(), 300, 260);
                break;
        }

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.NEW_POKEMON_VIEW;
    }

    private void setState(State state) {
        for (Button button : buttons) {
            button.setActive(false);
        }

        CharacterData player = Game.getPlayer();

        String pokemonName = newPokemon.getActualName();

        this.state = state;
        switch (state) {
            case POKEDEX:
                if (player.getPokedex().isCaught(newPokemon)) {
                    setState(State.NICKNAME_QUESTION);
                } else {
                    message = pokemonName + " was registered in the " + PokeString.POKEDEX + "!";
                    Game.getPlayer().getPokedex().setCaught(newPokemon.getPokemonInfo());
                }
                break;
            case NICKNAME_QUESTION:
                if (newPokemon.isEgg()) {
                    setState(State.END);
                } else {
                    for (int i = 0; i < buttons.length; i++) {
                        Button button = buttons[i];
                        button.setActive(button == yesButton() || button == noButton());
                        if (button == noButton()) {
                            this.selectedButton = i;
                        }
                    }

                    message = "Would you like to give " + pokemonName + " a nickname?";
                }
                break;
            case NICKNAME:
                message = "What would you like to name " + pokemonName + "?";
                break;
            case END:
                Integer boxNum = player.getPokemonBox();
                if (boxNum != null) {
                    message = pokemonName + " was sent to Box " + boxNum + " in your PC!";
                }
                break;
        }
    }

    @Override
    public void movedToFront() {
        CharacterData player = Game.getPlayer();
        this.newPokemon = player.getNewPokemon();
        this.nickname = StringUtils.empty();
        this.selectedButton = 0;

        setState(State.POKEDEX);
    }
}
