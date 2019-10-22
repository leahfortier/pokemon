package gui.view;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LabelPanel;
import draw.panel.PanelList;
import draw.panel.WrapPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.active.PartyPokemon;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import trainer.Trainer;
import trainer.player.NewPokemonInfo;
import trainer.player.Player;
import type.PokeType;
import util.Point;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class NewPokemonView extends View {
    private static final int NUM_COLS = 4;
    private static final int LEFT_BUTTON = NUM_COLS + 1; // Bottom center left
    private static final int RIGHT_BUTTON = NUM_COLS + 2; // Bottom center right

    private static final int TEXT_SPACING = 15;

    private static final int BOX_SPACING = (BasicPanels.getMessagePanelY()
            - new LabelPanel(0, 0, 30, TEXT_SPACING, "").height
            - new LabelPanel(0, 0, 24, TEXT_SPACING, "").height
            - 5*new LabelPanel(0, 0, 22, TEXT_SPACING, "").height)/6;

    private final PanelList panels;
    private final DrawPanel canvasPanel;
    private final DrawPanel messagelessCanvasPanel;

    private PanelList infoPanels;
    private LabelPanel namePanel;
    private LabelPanel weightPanel;
    private final DrawPanel imagePanel;
    private final WrapPanel descriptionPanel;

    private final ButtonList buttons;
    private final Button[] partyButtons;
    private final Button leftAnswerButton;
    private final Button rightAnswerButton;

    private PartyPokemon newPokemon;
    private Integer boxNum;

    private State state;
    private String message;
    private boolean displayInfo;

    NewPokemonView() {
        this.canvasPanel = BasicPanels.newFullGamePanel()
                                      .withTransparentCount(2)
                                      .withBorderPercentage(0);

        // Canvas panel truncated at the message
        this.messagelessCanvasPanel = new DrawPanel(
                canvasPanel.x, canvasPanel.y, canvasPanel.width,
                BasicPanels.getMessagePanelY() - canvasPanel.y
        ).withNoBackground().withLabelSize(30);

        // Placeholder panels
        this.setLabelPanels(PokemonNamesies.BULBASAUR.getInfo());

        // Image panel is from the top of the name panel to the bottom of the weight panel
        int imagePanelLength = weightPanel.bottomY() - namePanel.y;
        this.imagePanel = new DrawPanel(
                Global.GAME_SIZE.width - BOX_SPACING - imagePanelLength,
                namePanel.y,
                imagePanelLength,
                imagePanelLength
        ).withFullTransparency()
         .withBlackOutline();

        descriptionPanel = new WrapPanel(
                weightPanel.x,
                weightPanel.bottomY() + BOX_SPACING,
                Global.GAME_SIZE.width - 2*BOX_SPACING,
                3*weightPanel.height,
                22
        ).withFullTransparency()
         .withBlackOutline();

        this.buttons = new ButtonList(BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS));
        this.buttons.forEach(button -> button.setup(textButtonSetup()));
        this.buttons.setInactive();

        // Bottom center left
        this.leftAnswerButton = buttons.get(LEFT_BUTTON);

        // Bottom center right
        this.rightAnswerButton = buttons.get(RIGHT_BUTTON);

        this.partyButtons = new Button[Trainer.MAX_POKEMON];
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < Trainer.MAX_POKEMON/2; col++) {
                int buttonIndex = Point.getIndex(col, row, NUM_COLS);
                int partyIndex = Point.getIndex(col, row, Trainer.MAX_POKEMON/2);

                partyButtons[partyIndex] = buttons.get(buttonIndex);
            }
        }

        this.panels = new PanelList(canvasPanel, messagelessCanvasPanel);
    }

    private ButtonPanelSetup textButtonSetup() {
        return panel -> panel.skipInactive()
                             .withTransparentBackground()
                             .withTransparentCount(2)
                             .withBorderPercentage(15)
                             .withBlackOutline();
    }

    // Panels need to be recreated for each new pokemon because their sizing changes to fit the text
    private void setLabelPanels(PokemonInfo pokemonInfo) {
        namePanel = new LabelPanel(
                BOX_SPACING, BOX_SPACING, 30, TEXT_SPACING,
                String.format("%-10s   #%03d", pokemonInfo.getName(), pokemonInfo.getNumber())
        );

        LabelPanel classificationPanel = new LabelPanel(
                namePanel.x,
                namePanel.bottomY() + BOX_SPACING,
                24,
                TEXT_SPACING,
                pokemonInfo.getClassification() + " " + PokeString.POKEMON
        );

        LabelPanel heightPanel = new LabelPanel(
                classificationPanel.x,
                classificationPanel.bottomY() + BOX_SPACING,
                22,
                TEXT_SPACING,
                "Height: " + pokemonInfo.getHeightString()
        );

        weightPanel = new LabelPanel(
                heightPanel.x,
                heightPanel.bottomY() + BOX_SPACING,
                22,
                TEXT_SPACING,
                "Weight: " + pokemonInfo.getWeight() + "lbs"
        );

        this.infoPanels = new PanelList(
                imagePanel, namePanel, classificationPanel,
                heightPanel, weightPanel, descriptionPanel
        );
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        buttons.update();
        InputControl input = InputControl.instance();

        switch (state) {
            case POKEDEX:
                if (input.consumeIfMouseDown(ControlKey.SPACE)) {
                    if (!displayInfo) {
                        displayInfo = true;
                    } else {
                        message = null;
                    }
                }

                if (message == null) {
                    setState(State.NICKNAME_QUESTION);
                }
                break;
            case NICKNAME_QUESTION:
                if (rightAnswerButton.checkConsumePress()) {
                    setState(State.LOCATION);
                }

                if (leftAnswerButton.checkConsumePress()) {
                    setState(State.NICKNAME);
                }
                break;
            case NICKNAME:
                if (!input.isCapturingText()) {
                    input.startTextCapture();
                }

                if (input.consumeIfDown(ControlKey.ENTER)) {
                    String nickname = input.stopAndResetCapturedText();
                    newPokemon.setNickname(nickname);

                    setState(State.LOCATION);
                }
                break;
            case LOCATION:
                if (rightAnswerButton.checkConsumePress()) {
                    setState(State.END);
                }

                if (leftAnswerButton.checkConsumePress()) {
                    setState(State.PARTY_SELECTION);
                }
                break;
            case PARTY_SELECTION:
                if (input.consumeIfDown(ControlKey.BACK)) {
                    setState(State.END);
                }

                Player player = Game.getPlayer();
                List<PartyPokemon> party = player.getTeam();
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < Trainer.MAX_POKEMON/2; col++) {
                        int buttonIndex = Point.getIndex(col, row, NUM_COLS);
                        int partyIndex = Point.getIndex(col, row, Trainer.MAX_POKEMON/2);

                        Button pokemonButton = buttons.get(buttonIndex);
                        if (pokemonButton.checkConsumePress()) {
                            PartyPokemon newPokemon = this.newPokemon;
                            this.newPokemon = party.get(partyIndex);

                            player.getPC().switchPokemon(newPokemon, partyIndex);
                            setState(State.END);
                            break;
                        }
                    }
                }
                break;
            case END:
                if (input.consumeIfMouseDown(ControlKey.SPACE)) {
                    message = null;
                }

                if (message == null) {
                    Game.instance().setViewMode(ViewMode.MAP_VIEW);
                }
                break;
        }
    }

    // Can't include the font size in the setup method because answer buttons overlap with party buttons
    // which use a different font size
    private void setupTextButton(Button button, Color color, String label) {
        button.panel()
              .withBackgroundColor(color)
              .withLabel(label, 30);
    }

    private void drawSetup() {
        BufferedImage pokemonImage = Game.getData().getPokedexTilesLarge().getTile(newPokemon.getBaseImageName());
        if (displayInfo) {
            imagePanel.withImageLabel(pokemonImage);
            messagelessCanvasPanel.skipDraw();
        } else if (state == State.NICKNAME) {
            BufferedImage spriteImage = Game.getData().getPokemonTilesSmall().getTile(newPokemon.getImageName());
            String nickname = InputControl.instance().getInputCaptureString(PartyPokemon.MAX_NAME_LENGTH);

            messagelessCanvasPanel.withImageLabel(spriteImage, nickname);
        } else if (state != State.END) {
            // Pass null to indicate no text to draw with label
            messagelessCanvasPanel.withImageLabel(pokemonImage, null);
        }

        switch (state) {
            case NICKNAME_QUESTION:
                setupTextButton(leftAnswerButton, new Color(120, 200, 80), "Yes");
                setupTextButton(rightAnswerButton, new Color(220, 20, 20), "No");
                break;
            case LOCATION:
                setupTextButton(leftAnswerButton, new Color(35, 120, 220), "Party");
                setupTextButton(rightAnswerButton, new Color(255, 215, 0), "PC");
                break;
            case PARTY_SELECTION:
                List<PartyPokemon> party = Game.getPlayer().getTeam();
                TileSet partyTiles = Game.getData().getPartyTiles();
                for (int i = 0; i < party.size(); i++) {
                    PartyPokemon partyPokemon = party.get(i);
                    BufferedImage partyPokemonImage = partyTiles.getTile(partyPokemon.getTinyImageName());
                    partyButtons[i].panel()
                                   .withBackgroundColors(PokeType.getColors(partyPokemon))
                                   .withImageLabel(partyPokemonImage, partyPokemon.getActualName())
                                   .withLabelSize(20);
                }
        }
    }

    @Override
    public void draw(Graphics g) {
        drawSetup();

        // Background
        panels.drawAll(g);
        this.drawMessage(g);
        buttons.drawPanels(g);

        // Info panels
        if (displayInfo) {
            infoPanels.drawAll(g);
            this.drawFlavorText(g, newPokemon.getPokemonInfo());
        }

        buttons.drawHover(g);
    }

    // Draws the message panel and the current message if applicable
    private void drawMessage(Graphics g) {
        if (message != null) {
            BasicPanels.drawFullMessagePanel(g, message);
        } else if (state == State.PARTY_SELECTION) {
            BasicPanels.drawFullMessagePanel(g, "");
        }
    }

    public WrapMetrics drawFlavorText(Graphics g, PokemonInfo pokemonInfo) {
        return descriptionPanel.drawMessage(g, pokemonInfo.getFlavorText());
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.NEW_POKEMON_VIEW;
    }

    private void setState(State state) {
        this.displayInfo = false;
        message = null;
        buttons.setInactive();

        Player player = Game.getPlayer();
        NewPokemonInfo newPokemonInfo = player.getNewPokemonInfo();
        String pokemonName = newPokemon.getActualName();

        this.state = state;
        switch (state) {
            case POKEDEX:
                if (newPokemonInfo.isFirstNewPokemon()) {
                    message = newPokemon.namesies().getName() + " was registered in the " + PokeString.POKEDEX + "!";
                } else {
                    setState(State.NICKNAME_QUESTION);
                }
                break;
            case NICKNAME_QUESTION:
                leftAnswerButton.setActive(true);
                rightAnswerButton.setActive(true);
                buttons.setSelected(RIGHT_BUTTON);
                message = "Would you like to give " + pokemonName + " a nickname?";
                break;
            case NICKNAME:
                message = "What would you like to name " + pokemonName + "?";
                break;
            case LOCATION:
                if (player.fullParty() && !player.getTeam().contains(newPokemon)) {
                    leftAnswerButton.setActive(true);
                    rightAnswerButton.setActive(true);
                    buttons.setSelected(RIGHT_BUTTON);
                    message = "Where would you like to send " + pokemonName + "?";
                } else {
                    setState(State.END);
                }
                break;
            case PARTY_SELECTION:
                message = null;
                this.buttons.setSelected(0);

                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < NUM_COLS; col++) {
                        int index = Point.getIndex(col, row, NUM_COLS);
                        buttons.get(index).setActive(col < Trainer.MAX_POKEMON/2);
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
        NewPokemonInfo newPokemonInfo = Game.getPlayer().getNewPokemonInfo();
        this.newPokemon = newPokemonInfo.getNewPokemon();
        this.boxNum = newPokemonInfo.getNewPokemonBox();
        this.setLabelPanels(newPokemon.getPokemonInfo());

        this.buttons.setSelected(0);

        this.canvasPanel.withBackgroundColors(PokeType.getColors(this.newPokemon));

        if (newPokemon.isEgg()) {
            setState(State.LOCATION);
        } else {
            setState(State.POKEDEX);
        }

        if (state == State.END) {
            Game.instance().popView();
        }
    }

    private enum State {
        POKEDEX,
        NICKNAME_QUESTION,
        NICKNAME,
        LOCATION,
        PARTY_SELECTION,
        END
    }
}
