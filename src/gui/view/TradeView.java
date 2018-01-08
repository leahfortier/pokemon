package gui.view;

import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import message.Messages;
import pattern.TradePokemonMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
import util.FontMetrics;
import util.PokeString;
import util.StringUtils;

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
    private final DrawPanel messagePanel;
    private final DrawPanel cancelPanel;

    private final Button[] buttons;
    private final Button cancelButton;

    private final TileSet partyTiles;
    private final TileSet pokemonTiles;

    private int selectedIndex;
    private int tradeAnimationTime;

    private PokemonInfo offering;
    private PokemonInfo requested;

    private BufferedImage theirPokesBackImage;
    private BufferedImage theirPokesFrontImage;
    private BufferedImage myPokesBackImage;
    private BufferedImage myPokesFrontImage;

    private List<ActivePokemon> team;

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
        this.messagePanel = new DrawPanel(
                0,
                BasicPanels.getMessagePanelY() - messagePanelHeight + DrawUtils.OUTLINE_SIZE,
                Global.GAME_SIZE.width/2,
                messagePanelHeight
        )
                .withBlackOutline();

        this.buttons = BasicPanels.getFullMessagePanelButtons(panelWidth, 55, 2, NUM_COLS);
        this.cancelButton = buttons[buttons.length - 1];

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
            this.selectedIndex = Button.update(this.buttons, this.selectedIndex);

            InputControl input = InputControl.instance();
            exit = cancelButton.checkConsumePress() || input.consumeIfDown(ControlKey.ESC);

            for (int i = 0; i < team.size(); i++) {
                Button button = getTeamButton(i);
                if (button.checkConsumePress()) {
                    ActivePokemon myPokes = team.get(i);
                    if (myPokes.getPokemonInfo().namesies() == requested.namesies()) {
                        ActivePokemon theirPokes = new ActivePokemon(
                                offering.namesies(),
                                myPokes.getLevel(),
                                false,
                                true
                        );

                        this.myPokesFrontImage = pokemonTiles.getTile(myPokes.getImageName());
                        this.theirPokesFrontImage = pokemonTiles.getTile(theirPokes.getImageName());

                        this.myPokesBackImage = pokemonTiles.getTile(myPokes.getImageName(false));
                        this.theirPokesBackImage = pokemonTiles.getTile(theirPokes.getImageName(false));

                        tradeAnimationTime = TRADE_ANIMATION_LIFESPAN;

                        Messages.add("Traded " + myPokes.getName() + " for " + theirPokes.getName() + "!");
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
        return buttons[teamIndex + teamIndex/NUM_TEAM_COLS];
    }

    private void drawPanel(Graphics g, String label, DrawPanel panel, PokemonInfo pokemon) {
        FontMetrics.setFont(g, 22);

        TextUtils.drawCenteredHeightString(g, label, panel.x + panel.getBorderSize() + 10, panel.y + panel.height/3);
        ImageUtils.drawCenteredImageLabel(g, partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getName(), panel.centerX(), panel.y + 2*panel.height/3);
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

            BasicPanels.drawFullMessagePanel(g, StringUtils.empty());
            for (int i = 0; i < team.size(); i++) {
                Button button = getTeamButton(i);
                ActivePokemon pokemon = team.get(i);

                DrawPanel buttonPanel = new DrawPanel(button)
                        .withBackgroundColors(Type.getColors(pokemon.getActualType()))
                        .withTransparentCount(2)
                        .withBlackOutline();

                buttonPanel.drawBackground(g);
                buttonPanel.imageLabel(g, 22, partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getActualName());
            }

            cancelPanel.drawBackground(g);
            cancelPanel.label(g, 22, "Cancel");

            for (Button button : buttons) {
                button.draw(g);
            }
        }
    }

    private void updateActiveButtons() {
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            getTeamButton(i).setActive(i < team.size());
        }

        buttons[NUM_TEAM_COLS].setActive(false);
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
        this.selectedIndex = 0;

        updateActiveButtons();
    }

    public void setTrade(TradePokemonMatcher tradePokemonMatcher) {
        offering = tradePokemonMatcher.getTradePokemon().getInfo();
        requested = tradePokemonMatcher.getRequested().getInfo();

        this.canvasPanel.withBackgroundColors(new Color[] {
                requested.getType()[0].getColor(),
                offering.getType()[0].getColor()
        });

        this.offeringPanel.withBackgroundColors(Type.getColors(offering));
        this.requestedPanel.withBackgroundColors(Type.getColors(requested));
    }
}
