package gui.view;

import battle.ActivePokemon;
import draw.Alignment;
import draw.DrawUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import draw.panel.WrapPanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import message.Messages;
import pokemon.active.PartyPokemon;
import pokemon.species.PokemonNamesies;
import trainer.Trainer;
import trainer.TrainerType;
import trainer.player.Player;
import type.PokeType;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

public class TradeView extends View {
    private static final int NUM_BUTTONS = Trainer.MAX_POKEMON;
    private static final int PARTY = 0;

    private static final int TRADE_ANIMATION_LIFESPAN = 6000;

    private final PanelList panels;
    private final DrawPanel canvasPanel;
    private final DrawPanel fullOfferingPanel;
    private final DrawPanel offeringPokemonPanel;
    private final DrawPanel fullRequestedPanel;
    private final DrawPanel requestedPokemonPanel;
    private final WrapPanel messagePanel;

    private final ButtonList buttons;
    private final Button[] partyButtons;

    private TileSet partyTiles;
    private TileSet pokemonTiles;

    private int tradeAnimationTime;

    private PokemonNamesies offering;
    private PokemonNamesies requested;

    private BufferedImage theirPokesBackImage;
    private BufferedImage theirPokesFrontImage;
    private BufferedImage myPokesBackImage;
    private BufferedImage myPokesFrontImage;

    private List<PartyPokemon> team;

    private State state;

    public TradeView() {
        state = State.QUESTION;

        this.canvasPanel = BasicPanels.newFullGamePanel()
                                      .withTransparentCount(2)
                                      .withBorderPercentage(0);

        int panelWidth = 183;
        int spacing = 20;

        this.fullOfferingPanel = new DrawPanel(spacing, spacing, panelWidth, 90)
                .withTransparentCount(2)
                .withBlackOutline();

        this.fullRequestedPanel = new DrawPanel(
                Global.GAME_SIZE.width - spacing - panelWidth,
                BasicPanels.getMessagePanelY() - spacing - this.fullOfferingPanel.height,
                panelWidth,
                this.fullOfferingPanel.height
        )
                .withTransparentCount(2)
                .withBlackOutline();

        int messagePanelHeight = 80;
        this.messagePanel = new WrapPanel(
                0,
                BasicPanels.getMessagePanelY() - messagePanelHeight + DrawUtils.OUTLINE_SIZE,
                Global.GAME_SIZE.width/2,
                messagePanelHeight,
                22
        )
                .withBlackOutline();

        partyButtons = BasicPanels.getFullMessagePanelLayout(2, Trainer.MAX_POKEMON/2, 10)
                                  .withStartIndex(PARTY)
                                  .withPressIndex(this::pressPokemonButton)
                                  .withButtonSetup(panel -> panel.skipInactive()
                                                                 .withTransparentCount(2)
                                                                 .withBlackOutline()
                                                                 .withBorderPercentage(15)
                                                                 .withLabelSize(20))
                                  .getButtons();

        this.buttons = new ButtonList(NUM_BUTTONS);
        this.buttons.set(PARTY, partyButtons);

        Entry<DrawPanel, DrawPanel> offeringLabels = createTradeLabelPanels(fullOfferingPanel, "Offering:");
        DrawPanel offeringLabel = offeringLabels.getKey();
        this.offeringPokemonPanel = offeringLabels.getValue();

        Entry<DrawPanel, DrawPanel> requestedLabels = createTradeLabelPanels(fullRequestedPanel, "Requested:");
        DrawPanel requestedLabel = requestedLabels.getKey();
        this.requestedPokemonPanel = requestedLabels.getValue();

        panels = new PanelList(
                fullOfferingPanel, offeringLabel, offeringPokemonPanel,
                fullRequestedPanel, requestedLabel, requestedPokemonPanel,
                messagePanel
        );
    }

    // Split the trade panel into two horizontal subpanels (top for trade label, bottom for pokemon label)
    private Entry<DrawPanel, DrawPanel> createTradeLabelPanels(DrawPanel fullTradePanel, String label) {
        DrawPanel[] splitPanels = new DrawLayout(fullTradePanel, 2, 1, 0).getPanels();

        DrawPanel labelPanel = splitPanels[0].withNoBackground()
                                             .withLabel(label, 22, Alignment.LEFT);

        DrawPanel pokemonPanel = splitPanels[1].withNoBackground()
                                               .withLabelSize(20);

        return new SimpleEntry<>(labelPanel, pokemonPanel);
    }

    private void pressPokemonButton(int index) {
        PartyPokemon myPokes = team.get(index);
        if (!myPokes.isEgg() && myPokes.namesies() == requested) {
            ActivePokemon theirPokes = new ActivePokemon(
                    offering,
                    myPokes.getLevel(),
                    TrainerType.PLAYER
            );

            team.set(index, theirPokes);

            this.myPokesFrontImage = pokemonTiles.getTile(myPokes.getImageName());
            this.theirPokesFrontImage = pokemonTiles.getTile(theirPokes.getImageName());

            this.myPokesBackImage = pokemonTiles.getTile(myPokes.getImageName(false));
            this.theirPokesBackImage = pokemonTiles.getTile(theirPokes.getImageName(false));

            tradeAnimationTime = TRADE_ANIMATION_LIFESPAN;
            state = State.TRADE;

            Messages.add("Traded " + myPokes.getActualName() + " for " + theirPokes.getName() + "!");
        } else {
            Messages.add("Hm... Not exactly what I was hoping for... but thanks anyways?");
            state = State.END;
        }
    }

    @Override
    public void update(int dt) {
        if (state == State.TRADE) {
            tradeAnimationTime -= dt;
            if (tradeAnimationTime <= 0) {
                this.state = State.END;
            }
            return;
        }

        this.buttons.update();
        if (this.buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        InputControl input = InputControl.instance();
        switch (state) {
            case QUESTION:
                if (input.consumeIfDown(ControlKey.ESC, ControlKey.BACK)) {
                    this.state = State.END;
                }
                break;
            case END:
                Game.instance().popView();
                break;
        }
    }

    private void drawTradeAnimation(Graphics g) {
        if (myPokesFrontImage == null ||
                theirPokesFrontImage == null ||
                myPokesBackImage == null ||
                theirPokesBackImage == null) {
            return;
        }

        int drawWidth = Global.GAME_SIZE.width;
        int drawHeightLeft = Global.GAME_SIZE.height/2;
        int drawHeightRight = drawHeightLeft - myPokesFrontImage.getHeight();

        BufferedImage a = myPokesBackImage;
        BufferedImage b = theirPokesFrontImage;
        float timesies = tradeAnimationTime;
        boolean swap = false;

        // Images slide in from sides
        if (tradeAnimationTime < TRADE_ANIMATION_LIFESPAN/2.0f) {
            a = theirPokesBackImage;
            b = myPokesFrontImage;
            timesies = TRADE_ANIMATION_LIFESPAN - timesies;
            swap = true;
        }

        float yomama = TRADE_ANIMATION_LIFESPAN/2.0f;
        float normalizedTime = 1 - (timesies - yomama)/(yomama);
        int drawWidthA = drawWidth + a.getWidth();
        int drawWidthB = drawWidth + b.getWidth();
        int distA = (int)(drawWidthA*normalizedTime);
        int distB = (int)(drawWidthB*normalizedTime);
        if (swap) {
            distA = drawWidthA - distA;
            distB = drawWidthB - distB;
        }

        g.drawImage(a, distA - a.getWidth(), drawHeightLeft, null);
        g.drawImage(b, Global.GAME_SIZE.width - distB, drawHeightRight, null);
    }

    @Override
    public void draw(Graphics g) {
        canvasPanel.drawBackground(g);

        if (tradeAnimationTime > 0) {
            drawTradeAnimation(g);
        } else {
            BasicPanels.drawFullMessagePanel(g, "");
            panels.drawAll(g);
            buttons.drawPanels(g);

            this.messagePanel.drawMessage(g, "Which " + PokeString.POKEMON + " would you like to trade?");

            buttons.drawHover(g);
        }
    }

    private void updateActiveButtons() {
        // Set up Pokemon button background and labels
        for (int i = 0; i < partyButtons.length; i++) {
            Button button = partyButtons[i];
            button.setActive(i < team.size());
            if (button.isActive()) {
                PartyPokemon pokemon = team.get(i);
                button.panel()
                      .withTypeColors(pokemon)
                      .withImageLabel(partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getActualName());
            }
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.TRADE_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        GameData data = Game.getData();

        this.partyTiles = data.getPartyTiles();
        this.pokemonTiles = data.getPokemonTilesLarge();

        this.team = player.getTeam();
        this.buttons.setSelected(PARTY);
        this.state = State.QUESTION;
        this.tradeAnimationTime = 0;

        updateActiveButtons();
    }

    public void setTrade(PokemonNamesies tradePokemon, PokemonNamesies requestedPokemon) {
        this.movedToFront();

        this.offering = tradePokemon;
        this.requested = requestedPokemon;

        this.canvasPanel.withBackgroundColors(new Color[] {
                this.getFirstTypeColor(requested),
                this.getFirstTypeColor(offering)
        });

        setTradePanel(fullOfferingPanel, offeringPokemonPanel, offering);
        setTradePanel(fullRequestedPanel, requestedPokemonPanel, requestedPokemon);
    }

    private void setTradePanel(DrawPanel fullTradePanel, DrawPanel labelPanel, PokemonNamesies pokemon) {
        fullTradePanel.withBackgroundColors(PokeType.getColors(pokemon));
        labelPanel.withImageLabel(
                partyTiles.getTile(pokemon.getInfo().getTinyImageName()),
                pokemon.getName()
        );
    }

    private Color getFirstTypeColor(PokemonNamesies pokemonNamesies) {
        return pokemonNamesies.getInfo().getType().getFirstType().getColor();
    }

    private static enum State {
        QUESTION,
        TRADE,
        END
    }
}
