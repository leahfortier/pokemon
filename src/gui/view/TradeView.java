package gui.view;

import draw.button.Button;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import main.Game;
import pattern.TradePokemonMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.player.Player;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class TradeView extends View {
    private static final int NUM_COLS = 4;

    private final DrawPanel canvasPanel;

    private final Button[] buttons;
    private final Button cancelButton;

    private PokemonInfo offering;
    private PokemonInfo requested;

    private List<ActivePokemon> team;

    public TradeView() {
        this.canvasPanel = DrawPanel.fullGamePanel()
                .withTransparentCount(2)
                .withBorderPercentage(0);

        this.buttons = BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS);
        this.cancelButton = buttons[buttons.length - 1];
    }

    @Override
    public void update(int dt) {

    }

    @Override
    public void draw(Graphics g) {
        canvasPanel.drawBackground(g);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.TRADE_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        this.team = player.getTeam();
    }

    public void setTrade(TradePokemonMatcher tradePokemonMatcher) {
        offering = PokemonInfo.getPokemonInfo(tradePokemonMatcher.getTradePokemon());
        requested = PokemonInfo.getPokemonInfo(tradePokemonMatcher.getRequested());

        this.canvasPanel.withBackgroundColors(new Color[] {
                offering.getType()[0].getColor(),
                requested.getType()[0].getColor()
        });
    }
}
