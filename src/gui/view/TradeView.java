package gui.view;

import battle.ActivePokemon;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
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
import util.FontMetrics;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class TradeView extends View {
    private static final int NUM_TEAM_COLS = Trainer.MAX_POKEMON/2;
    private static final int NUM_COLS = NUM_TEAM_COLS + 1;

    private static final int TRADE_ANIMATION_LIFESPAN = 6000;

    private final DrawPanel canvasPanel;
    private final DrawPanel offeringPanel;
    private final DrawPanel requestedPanel;
    private final WrapPanel messagePanel;
    private final DrawPanel cancelPanel;

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

        this.offeringPanel = new DrawPanel(spacing, spacing, panelWidth, 90)
                .withTransparentCount(2)
                .withBlackOutline();

        this.requestedPanel = new DrawPanel(
                Global.GAME_SIZE.width - spacing - panelWidth,
                BasicPanels.getMessagePanelY() - spacing - this.offeringPanel.height,
                panelWidth,
                this.offeringPanel.height
        )
                .withTransparentCount(2)
                .withBlackOutline();

        int messagePanelHeight = 80;
        this.messagePanel = new WrapPanel(
                0,
                BasicPanels.getMessagePanelY() - messagePanelHeight + DrawUtils.OUTLINE_SIZE,
                Global.GAME_SIZE.width/2,
                messagePanelHeight
        )
                .withBlackOutline();

        this.buttons = new ButtonList(BasicPanels.getFullMessagePanelButtons(panelWidth, 55, 2, NUM_COLS));
        this.cancelButton = buttons.get(buttons.size() - 1);

        this.cancelPanel = new DrawPanel(cancelButton)
                .withBackgroundColor(Type.NORMAL.getColor())
                .withTransparentCount(2)
                .withBlackOutline();

        GameData data = Game.getData();
        this.partyTiles = data.getPartyTiles();
        this.pokemonTiles = data.getPokemonTilesLarge();
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

    private void drawPanel(Graphics g, String label, DrawPanel panel, PokemonNamesies pokemon) {
        FontMetrics.setFont(g, 22);

        TextUtils.drawCenteredHeightString(g, label, panel.x + panel.getBorderSize() + 10, panel.y + panel.height/3);
        ImageUtils.drawCenteredImageLabel(g, partyTiles.getTile(pokemon.getInfo().getTinyImageName()), pokemon.getName(), panel.centerX(), panel.y + 2*panel.height/3);
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
            requestedPanel.drawBackground(g);
            offeringPanel.drawBackground(g);

            drawPanel(g, "Offering:", offeringPanel, offering);
            drawPanel(g, "Requested:", requestedPanel, requested);

            this.messagePanel.drawBackground(g);
            this.messagePanel.drawMessage(g, 22, "Which " + PokeString.POKEMON + " would you like to trade?");

            BasicPanels.drawFullMessagePanel(g, "");
            for (int i = 0; i < team.size(); i++) {
                Button button = getTeamButton(i);
                PartyPokemon pokemon = team.get(i);

                DrawPanel buttonPanel = new DrawPanel(button)
                        .withBackgroundColors(PokeType.getColors(pokemon))
                        .withTransparentCount(2)
                        .withBlackOutline();

                buttonPanel.drawBackground(g);
                buttonPanel.imageLabel(g, 22, partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getActualName());
            }

            cancelPanel.drawBackground(g);
            cancelPanel.label(g, 22, "Cancel");

            buttons.draw(g);
        }
    }

    private void updateActiveButtons() {
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            getTeamButton(i).setActive(i < team.size());
        }

        // TODO: Which button is this??
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
    }

    public void setTrade(PokemonNamesies tradePokemon, PokemonNamesies requestedPokemon) {
        this.offering = tradePokemon;
        this.requested = requestedPokemon;

        this.canvasPanel.withBackgroundColors(new Color[] {
                this.getFirstTypeColor(requested),
                this.getFirstTypeColor(offering)
        });

        this.offeringPanel.withBackgroundColors(PokeType.getColors(offering));
        this.requestedPanel.withBackgroundColors(PokeType.getColors(requested));
    }

    private Color getFirstTypeColor(PokemonNamesies pokemonNamesies) {
        return pokemonNamesies.getInfo().getType().getFirstType().getColor();
    }
}
