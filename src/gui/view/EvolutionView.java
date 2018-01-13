package gui.view;

import draw.ImageUtils;
import draw.TextUtils;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LearnMovePanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import battle.ActivePokemon;
import pokemon.PartyPokemon;
import pokemon.PokemonInfo;
import pokemon.Stat;
import pokemon.evolution.BaseEvolution;
import trainer.player.EvolutionInfo;
import type.Type;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

class EvolutionView extends View {
    private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
    private static final Point POKEMON_DRAW_LOCATION = Point.scaleDown(Global.GAME_SIZE, 2);

    private final DrawPanel canvasPanel;
    private final DrawPanel statsPanel;

    private LearnMovePanel learnMovePanel;

    private int animationEvolve;

    private ActivePokemon evolvingPokemon;
    private PokemonInfo preEvolution;
    private PokemonInfo postEvolution;
    private boolean isEgg;

    private State state;
    private ArrayDeque<MessageUpdate> messages;

    EvolutionView() {
        this.canvasPanel = BasicPanels.newFullGamePanel()
                                      .withTransparentCount(2)
                                      .withBorderPercentage(0);

        this.statsPanel = new DrawPanel(0, 280, 273, 161).withBlackOutline();
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        if (state == State.START) {
            if (!messages.isEmpty()) {
                if (input.consumeIfMouseDown(ControlKey.SPACE)) {
                    messages.pop();
                }
            } else {
                state = State.EVOLVE;
            }
        }

        if (state == State.EVOLVE) {
            if (input.consumeIfDown(ControlKey.BACK) && !isEgg) {
                state = State.CANCELED;
                setCancelledMessage();
            }

            if (animationEvolve <= 0) {
                state = State.END;

                if (isEgg) {
                    messages.add(new MessageUpdate("Your egg hatched into " + StringUtils.articleString(preEvolution.getName()) + "!"));
                    Game.getPlayer().getMedalCase().hatch(evolvingPokemon);
                } else {
                    int[] gains = evolvingPokemon.evolve(Game.getPlayer().getEvolutionInfo().getEvolution());
                    int[] stats = evolvingPokemon.getStats();

                    messages.add(new MessageUpdate(
                            "Your " + preEvolution.getName() + " evolved into " + StringUtils.articleString(postEvolution.getName()) + "!")
                                         .withStatGains(gains, stats));
                }

                Game.getPlayer().pokemonEvolved(evolvingPokemon);
            }
        }

        boolean finishedLearningMove = false;
        if (state == State.LEARN_MOVE) {
            learnMovePanel.update();
            if (learnMovePanel.isFinished()) {
                state = State.END;
                finishedLearningMove = true;
            }
        }

        if (state == State.END || state == State.CANCELED) {
            if (!messages.isEmpty()) {
                if (input.consumeIfMouseDown(ControlKey.SPACE) || finishedLearningMove) {
                    while (Messages.peek().learnMove()) {
                        MessageUpdate message = Messages.getNextMessage();
                        messages.add(new MessageUpdate()
                                             .withUpdate(MessageUpdateType.LEARN_MOVE)
                                             .withLearnMove(evolvingPokemon, message.getMove())
                        );
                    }

                    while (!messages.isEmpty()) {
                        MessageUpdate message = messages.pop();
                        if (message.learnMove()) {
                            this.learnMovePanel = new LearnMovePanel(evolvingPokemon, message.getMove());
                            state = State.LEARN_MOVE;
                            break;
                        }

                        if (!messages.isEmpty() && !StringUtils.isNullOrWhiteSpace(messages.peek().getMessage())) {
                            break;
                        }
                    }
                }
            } else {
                Game.instance().popView();
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        final GameData data = Game.getData();
        final boolean endState = state == State.END || state == State.LEARN_MOVE;

        if (!endState) {
            if (isEgg) {
                canvasPanel.withBackgroundColors(new Color[] { Type.NORMAL.getColor(), Type.NORMAL.getColor() });
            } else {
                canvasPanel.withBackgroundColors(Type.getColors(preEvolution.getType()));
            }
        } else {
            if (isEgg) {
                canvasPanel.withBackgroundColors(Type.getColors(preEvolution.getType()));
            } else {
                canvasPanel.withBackgroundColors(Type.getColors(evolvingPokemon));
            }
        }

        canvasPanel.drawBackground(g);

        if (state == State.LEARN_MOVE) {
            // TODO: Why are we drawing this twice? Can this just be a state != LEARN_MOVE?
            this.learnMovePanel.draw(g);
        } else if (!messages.isEmpty()) {
            MessageUpdate message = messages.peek();
            BasicPanels.drawFullMessagePanel(g, message.getMessage());
            if (message.gainUpdate()) {
                statsPanel.drawBackground(g);

                int[] statGains = message.getGain();
                int[] newStats = message.getNewStats();

                g.setColor(Color.BLACK);
                for (int i = 0; i < Stat.NUM_STATS; i++) {
                    FontMetrics.setFont(g, 16);
                    g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);

                    TextUtils.drawRightAlignedString(g, (statGains[i] < 0 ? "" : " + ") + statGains[i], 206, 314 + i*21);
                    TextUtils.drawRightAlignedString(g, newStats[i] + "", 247, 314 + i*21);
                }
            }
        }

        TileSet pokemonTiles = data.getPokemonTilesLarge();

        FontMetrics.setFont(g, 30);
        g.setColor(Color.BLACK);

        String preIndex = isEgg ? PartyPokemon.SPRITE_EGG_IMAGE_NAME : preEvolution.getImageName(evolvingPokemon.isShiny());
        String postIndex = isEgg ? preEvolution.getImageName(evolvingPokemon.isShiny()) : postEvolution.getImageName(evolvingPokemon.isShiny());

        BufferedImage currEvolution = pokemonTiles.getTile(preIndex);
        BufferedImage nextEvolution = pokemonTiles.getTile(postIndex);

        if (endState) {
            ImageUtils.drawBottomCenteredImage(g, nextEvolution, POKEMON_DRAW_LOCATION);
        }

        switch (state) {
            case START:
            case CANCELED:
                ImageUtils.drawBottomCenteredImage(g, currEvolution, POKEMON_DRAW_LOCATION);
                break;
            case EVOLVE:
                evolveAnimation(g, currEvolution, nextEvolution);
                break;
            case LEARN_MOVE:
                learnMovePanel.draw(g);
                break;
        }
    }

    private void evolveAnimation(Graphics g, BufferedImage currEvolution, BufferedImage nextEvolution) {
        animationEvolve = ImageUtils.transformAnimation(
                g,
                animationEvolve,
                EVOLVE_ANIMATION_LIFESPAN,
                nextEvolution,
                currEvolution,
                POKEMON_DRAW_LOCATION
        );
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.EVOLUTION_VIEW;
    }

    private void setPokemon(ActivePokemon pokemon, BaseEvolution evolve) {
        evolvingPokemon = pokemon;
        preEvolution = pokemon.getPokemonInfo();

        if (evolve == null) {
            isEgg = true;
        } else {
            isEgg = false;
            postEvolution = evolve.getEvolution();
        }
    }

    private void setInitialMessage() {
        if (isEgg) {
            messages.add(new MessageUpdate("Your egg is hatching!"));
        } else {
            messages.add(new MessageUpdate("Your " + preEvolution.getName() + " is evolving!"));
        }
    }

    private void setCancelledMessage() {
        messages.add(new MessageUpdate("Whattt!?!?!??!! " + preEvolution.getName() + " stopped evolving!!!!"));
    }

    @Override
    public void movedToFront() {
        state = State.START;
        messages = new ArrayDeque<>();

        EvolutionInfo evolutionInfo = Game.getPlayer().getEvolutionInfo();

        setPokemon(evolutionInfo.getEvolvingPokemon(), evolutionInfo.getEvolution());
        setInitialMessage();

        animationEvolve = EVOLVE_ANIMATION_LIFESPAN;

        // TODO: Save current sound for when transitioning to the bag view.
        //Global.soundPlayer.playMusic(SoundTitle.EVOLUTION_VIEW);
    }

    private enum State {
        START,
        EVOLVE,
        CANCELED,
        LEARN_MOVE,
        END
    }
}
