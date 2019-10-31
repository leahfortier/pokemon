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
import type.Type;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

public class TradeView extends View {
    private static final int NUM_TEAM_COLS = Trainer.MAX_POKEMON/2;
    private static final int NUM_COLS = NUM_TEAM_COLS + 1;

    private static final int TRADE_ANIMATION_LIFESPAN = 6000;

    private final PanelList panels;
    private final DrawPanel canvasPanel;
    private final DrawPanel fullOfferingPanel;
    private final DrawPanel offeringPokemonPanel;
    private final DrawPanel fullRequestedPanel;
    private final DrawPanel requestedPokemonPanel;
    private final WrapPanel messagePanel;

    private final ButtonList buttons;
    private final Button cancelButton;

    private final TileSet partyTiles;
    private final TileSet pokemonTiles;

    private int tradeAnimationTime;

    private PokemonNamesies offering;
    private PokemonNamesies requested;

    private BufferedImage theirPokesBackImage;
    private BufferedImage theirPokesFrontImage;
    private BufferedImage myPokesBackImage;
    private BufferedImage myPokesFrontImage;

    private List<PartyPokemon> team;

    public TradeView() {
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

        this.buttons = new ButtonList(
                BasicPanels.getFullMessagePanelLayout(2, NUM_COLS, 10)
                           .withButtonSetup(panel -> panel.skipInactive()
                                                          .withTransparentBackground()
                                                          .withTransparentCount(2)
                                                          .withBlackOutline()
                                                          .withLabelSize(20))
                           .getButtons()
        );

        this.cancelButton = buttons.get(buttons.size() - 1)
                                   .setup(panel -> panel.withLabel("Cancel")
                                                        .withBackgroundColor(Type.NORMAL.getColor()));

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

        GameData data = Game.getData();
        this.partyTiles = data.getPartyTiles();
        this.pokemonTiles = data.getPokemonTilesLarge();
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

    @Override
    public void update(int dt) {
        boolean exit;
        if (tradeAnimationTime > 0) {
            tradeAnimationTime -= dt;
            exit = tradeAnimationTime <= 0;
        } else {
            this.buttons.update();

            InputControl input = InputControl.instance();
            exit = cancelButton.checkConsumePress() || input.consumeIfDown(ControlKey.ESC);

            for (int i = 0; i < team.size(); i++) {
                Button button = getTeamButton(i);
                if (button.checkConsumePress()) {
                    PartyPokemon myPokes = team.get(i);
                    if (!myPokes.isEgg() && myPokes.namesies() == requested) {
                        ActivePokemon theirPokes = new ActivePokemon(
                                offering,
                                myPokes.getLevel(),
                                TrainerType.PLAYER
                        );

                        team.set(i, theirPokes);

                        this.myPokesFrontImage = pokemonTiles.getTile(myPokes.getImageName());
                        this.theirPokesFrontImage = pokemonTiles.getTile(theirPokes.getImageName());

                        this.myPokesBackImage = pokemonTiles.getTile(myPokes.getImageName(false));
                        this.theirPokesBackImage = pokemonTiles.getTile(theirPokes.getImageName(false));

                        tradeAnimationTime = TRADE_ANIMATION_LIFESPAN;

                        Messages.add("Traded " + myPokes.getActualName() + " for " + theirPokes.getName() + "!");
                    } else {
                        Messages.add("Hm... Not exactly what I was hoping for... but thanks anyways?");
                        exit = true;
                    }
                }
            }
        }

        if (exit) {
            Game.instance().popView();
        }
    }

    private Button getTeamButton(int teamIndex) {
        return buttons.get(teamIndex + teamIndex/NUM_TEAM_COLS);
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
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            getTeamButton(i).setActive(i < team.size());
        }

        // Unused button above cancel
        buttons.get(NUM_TEAM_COLS).setActive(false);
        cancelButton.setActive(true);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.TRADE_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        this.team = player.getTeam();
        this.buttons.setSelected(0);

        updateActiveButtons();

        // Set up Pokemon button background and labels
        for (int i = 0; i < team.size(); i++) {
            PartyPokemon pokemon = team.get(i);
            this.getTeamButton(i)
                .panel()
                .withBackgroundColors(PokeType.getColors(pokemon))
                .withImageLabel(partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getActualName());
        }
    }

    public void setTrade(PokemonNamesies tradePokemon, PokemonNamesies requestedPokemon) {
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
}
