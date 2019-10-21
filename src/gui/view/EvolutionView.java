package gui.view;

import battle.ActivePokemon;
import draw.ImageUtils;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LearnMovePanel;
import draw.panel.StatGainPanel;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import message.MessageQueue;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.breeding.Eggy;
import pokemon.evolution.BaseEvolution;
import pokemon.species.PokemonNamesies;
import trainer.player.EvolutionInfo;
import type.PokeType;
import type.Type;
import util.Point;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class EvolutionView extends View {
    private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
    private static final Point POKEMON_DRAW_LOCATION = Point.scaleDown(Global.GAME_SIZE, 2);

    private final DrawPanel canvasPanel;
    private final StatGainPanel statsPanel;

    private LearnMovePanel learnMovePanel;

    private int animationEvolve;

    private ActivePokemon evolvingPokemon;
    private PokemonNamesies preEvolution;
    private PokemonNamesies postEvolution;
    private boolean isEgg;

    private State state;
    private MessageQueue messages;

    EvolutionView() {
        this.canvasPanel = BasicPanels.newFullGamePanel()
                                      .withTransparentCount(2)
                                      .withBorderPercentage(0);

        this.statsPanel = new StatGainPanel();
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
            // Eggs can't be cancelled
            if (input.consumeIfDown(ControlKey.BACK) && !isEgg) {
                state = State.CANCELLED;
                setCancelledMessage();
            }

            if (animationEvolve <= 0) {
                state = State.END;

                if (isEgg) {
                    messages.add("Your egg hatched into " + StringUtils.articleString(preEvolution.getName()) + "!");
                    Game.getPlayer().getMedalCase().hatch(evolvingPokemon);
                } else {
                    int[] gains = evolvingPokemon.evolve(Game.getPlayer().getEvolutionInfo().getEvolution());
                    int[] stats = evolvingPokemon.stats().getClonedStats();

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

        if (state == State.END || state == State.CANCELLED) {
            if (!messages.isEmpty()) {
                if (input.consumeIfMouseDown(ControlKey.SPACE) || finishedLearningMove) {
                    while (Messages.hasMessages() && Messages.peek().learnMove()) {
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

                        if (!messages.isEmptyMessage()) {
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
        final boolean finishedEvolving = state == State.END || state == State.LEARN_MOVE;

        // Use pre-evolution for evolved eggs and unevolved Pokemon
        Color[] backgroundColors = PokeType.getColors(preEvolution);
        if (finishedEvolving && !isEgg) {
            backgroundColors = PokeType.getColors(evolvingPokemon);
        } else if (!finishedEvolving && isEgg) {
            // Unhatched egg -- use normal-type colors
            backgroundColors = new PokeType(Type.NORMAL).getColors();
        }

        canvasPanel.withBackgroundColors(backgroundColors)
                   .drawBackground(g);

        if (state != State.LEARN_MOVE && !messages.isEmpty()) {
            MessageUpdate message = this.messages.peek();
            BasicPanels.drawFullMessagePanel(g, message.getMessage());
            if (message.gainUpdate()) {
                statsPanel.drawStatGain(g, message);
            }
        }

        TileSet pokemonTiles = Game.getData().getPokemonTilesLarge();
        String preImageName = isEgg ? Eggy.SPRITE_EGG_IMAGE_NAME : getImageName(preEvolution);
        String postImageName = getImageName(isEgg ? preEvolution : postEvolution);
        BufferedImage currEvolution = pokemonTiles.getTile(preImageName);
        BufferedImage nextEvolution = pokemonTiles.getTile(postImageName);

        if (finishedEvolving) {
            ImageUtils.drawBottomCenteredImage(g, nextEvolution, POKEMON_DRAW_LOCATION);
        }

        switch (state) {
            case START:
            case CANCELLED:
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

    private String getImageName(PokemonNamesies pokemonNamesies) {
        return pokemonNamesies.getInfo().getImageName(evolvingPokemon.isShiny());
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
        preEvolution = pokemon.namesies();

        if (evolve == null) {
            isEgg = true;
        } else {
            isEgg = false;
            postEvolution = evolve.getEvolution();
        }
    }

    private void setInitialMessage() {
        if (isEgg) {
            messages.add("Your egg is hatching!");
        } else {
            messages.add("Your " + preEvolution.getName() + " is evolving!");
        }
    }

    private void setCancelledMessage() {
        messages.add("Whattt!?!?!??!! " + preEvolution.getName() + " stopped evolving!!!!");
    }

    @Override
    public void movedToFront() {
        state = State.START;
        messages = new MessageQueue();

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
        CANCELLED,
        LEARN_MOVE,
        END
    }
}
