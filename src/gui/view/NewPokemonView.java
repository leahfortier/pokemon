package gui.view;

import gui.TileSet;
import gui.button.Button;
import gui.panel.BasicPanels;
import gui.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer;
import util.DrawUtils;
import util.Point;
import util.PokeString;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

class NewPokemonView extends View {
    private static final int NUM_COLS = 4;

    private final Button[] buttons;

    private ActivePokemon newPokemon;
    private Integer boxNum;

    private State state;
    private String message;
    private int selectedButton;

    private String nickname;

    private enum State {
        POKEDEX,
        NICKNAME_QUESTION,
        NICKNAME,
        LOCATION,
        PARTY_SELECTION,
        END
    }

    NewPokemonView() {
        this.buttons = BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS);
        for (Button button : buttons) {
            button.setActive(false);
        }
    }

    // Bottom center left
    private Button leftButton() {
        return buttons[NUM_COLS + 1];
    }

    // Bottom center right
    private Button rightButton() {
        return buttons[NUM_COLS + 2];
    }

    @Override
    public void update(int dt) {
        selectedButton = Button.update(buttons, selectedButton);
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
                if (rightButton().checkConsumePress()) {
                    setState(State.LOCATION);
                }

                if (leftButton().checkConsumePress()) {
                    setState(State.NICKNAME);
                }
                break;
            case NICKNAME:
                if (!input.isCapturingText()) {
                    input.startTextCapture();
                }

                // TODO: Make a method for this
                nickname = input.getCapturedText();
                if (nickname.length() > ActivePokemon.MAX_NAME_LENGTH) {
                    nickname = nickname.substring(0, ActivePokemon.MAX_NAME_LENGTH);
                }

                if (input.consumeIfDown(ControlKey.ENTER)) {
                    input.stopTextCapture();
                    newPokemon.setNickname(nickname);
                    setState(State.LOCATION);
                }
                break;
            case LOCATION:
                if (rightButton().checkConsumePress()) {
                    setState(State.END);
                }

                if (leftButton().checkConsumePress()) {
                    setState(State.PARTY_SELECTION);
                }
                break;
            case PARTY_SELECTION:
                if (input.consumeIfDown(ControlKey.BACK)) {
                    setState(State.END);
                }

                CharacterData player = Game.getPlayer();
                List<ActivePokemon> party = player.getTeam();
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < Trainer.MAX_POKEMON/2; col++) {
                        int buttonIndex = Point.getIndex(col, row, NUM_COLS);
                        int partyIndex = Point.getIndex(col, row, Trainer.MAX_POKEMON/2);

                        Button pokemonButton = buttons[buttonIndex];
                        if (pokemonButton.checkConsumePress()) {
                            ActivePokemon newPokemon = this.newPokemon;
                            this.newPokemon = party.get(partyIndex);

                            player.getPC().switchPokemon(newPokemon, partyIndex);
                            setState(State.END);
                            break;
                        }
                    }
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
                drawButton(g, leftButton(), new Color(120, 200, 80), "Yes");
                drawButton(g, rightButton(), new Color(220, 20, 20), "No");
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
            case LOCATION:
                drawButton(g, leftButton(), new Color(35, 120, 220), "Party");
                drawButton(g, rightButton(), new Color(255, 215, 0), "PC");
                break;
            case PARTY_SELECTION:
                BasicPanels.drawFullMessagePanel(g, StringUtils.empty());

                List<ActivePokemon> party = Game.getPlayer().getTeam();
                TileSet partyTiles = Game.getData().getPartyTiles();

                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < Trainer.MAX_POKEMON/2; col++) {
                        int buttonIndex = Point.getIndex(col, row, NUM_COLS);
                        int partyIndex = Point.getIndex(col, row, Trainer.MAX_POKEMON/2);

                        Button pokemonButton = buttons[buttonIndex];
                        ActivePokemon partyPokemon = party.get(partyIndex);
                        BufferedImage partyPokemonImage = partyTiles.getTile(partyPokemon.getTinyImageIndex());

                        DrawPanel pokemonPanel = new DrawPanel(pokemonButton)
                                .withBackgroundColors(Type.getColors(partyPokemon))
                                .withTransparentCount(2)
                                .withBlackOutline();
                        pokemonPanel.drawBackground(g);
                        pokemonPanel.imageLabel(g, 20, partyPokemonImage, partyPokemon.getActualName());
                    }
                }
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
                    setState(State.LOCATION);
                } else {
                    for (int i = 0; i < buttons.length; i++) {
                        Button button = buttons[i];
                        button.setActive(button == leftButton() || button == rightButton());
                        if (button == rightButton()) {
                            this.selectedButton = i;
                        }
                    }

                    message = "Would you like to give " + pokemonName + " a nickname?";
                }
                break;
            case NICKNAME:
                message = "What would you like to name " + pokemonName + "?";
                break;
            case LOCATION:
                if (!player.fullParty()) {
                    setState(State.END);
                } else {
                    for (int i = 0; i < buttons.length; i++) {
                        Button button = buttons[i];
                        button.setActive(button == leftButton() || button == rightButton());
                        if (button == rightButton()) {
                            selectedButton = i;
                        }
                    }

                    message = "Where would you like to send " + pokemonName + "?";
                }
                break;
            case PARTY_SELECTION:
                message = null;
                selectedButton = 0;

                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < NUM_COLS; col++) {
                        int index = Point.getIndex(col, row, NUM_COLS);
                        buttons[index].setActive(col < Trainer.MAX_POKEMON/2);
                    }
                }
                break;
            case END:
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
        this.boxNum = player.getNewPokemonBox();

        this.nickname = StringUtils.empty();
        this.selectedButton = 0;

        setState(State.POKEDEX);
    }
}
