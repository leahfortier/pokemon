package gui.view;

import battle.ActivePokemon;
import draw.handler.EvolveAnimationHandler;
import draw.handler.LearnMoveHandler;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.StatGainPanel;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
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
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class EvolutionView extends View {
    private final DrawPanel canvasPanel;
    private final StatGainPanel statsPanel;

    private final EvolveAnimationHandler animationHandler;
    private LearnMoveHandler learnMoveHandler;

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
        this.animationHandler = new EvolveAnimationHandler(this.canvasPanel);
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
                // No more messages -- time to evolve!
                this.setState(State.EVOLVE);
            }
        }

        if (state == State.EVOLVE) {
            // Eggs can't be cancelled
            if (input.consumeIfDown(ControlKey.BACK) && !isEgg) {
                messages.add("Whattt!?!?!??!! " + preEvolution.getName() + " stopped evolving!!!!");
                this.setState(State.CANCELLED);
            }

            if (animationHandler.isFinished()) {
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
                this.setState(State.END);
            }
        }

        boolean finishedLearningMove = false;
        if (state == State.LEARN_MOVE) {
            learnMoveHandler.update();
            if (learnMoveHandler.isFinished()) {
                finishedLearningMove = true;
                this.setState(State.END);
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
                            this.learnMoveHandler = new LearnMoveHandler(evolvingPokemon, message.getMove());
                            this.setState(State.LEARN_MOVE);
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
        canvasPanel.drawBackground(g);

        if (state != State.LEARN_MOVE && !messages.isEmpty()) {
            MessageUpdate message = this.messages.peek();
            BasicPanels.drawFullMessagePanel(g, message.getMessage());
            if (message.gainUpdate()) {
                statsPanel.drawStatGain(g, message);
            }
        }

        switch (state) {
            case START:
            case CANCELLED:
                animationHandler.drawBase(g);
                break;
            case EVOLVE:
                animationHandler.drawEvolve(g);
                break;
            case LEARN_MOVE:
                learnMoveHandler.draw(g);
                break;
        }

        if (this.isFinishedEvolving()) {
            animationHandler.drawEnd(g);
        }
    }

    private String getImageName(PokemonNamesies pokemonNamesies) {
        return pokemonNamesies.getInfo().getImageName(evolvingPokemon.isShiny());
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

        TileSet pokemonTiles = Game.getData().getPokemonTilesLarge();
        String preImageName = isEgg ? Eggy.SPRITE_EGG_IMAGE_NAME : getImageName(preEvolution);
        String postImageName = getImageName(isEgg ? preEvolution : postEvolution);
        BufferedImage currEvolution = pokemonTiles.getTile(preImageName);
        BufferedImage nextEvolution = pokemonTiles.getTile(postImageName);

        this.animationHandler.set(currEvolution, nextEvolution);
    }

    private void setInitialMessage() {
        if (isEgg) {
            messages.add("Your egg is hatching!");
        } else {
            messages.add("Your " + preEvolution.getName() + " is evolving!");
        }
    }

    @Override
    public void movedToFront() {
        EvolutionInfo evolutionInfo = Game.getPlayer().getEvolutionInfo();
        setPokemon(evolutionInfo.getEvolvingPokemon(), evolutionInfo.getEvolution());

        this.setState(State.START);

        messages = new MessageQueue();
        setInitialMessage();

        // TODO: Save current sound for when transitioning to the bag view.
        //Global.soundPlayer.playMusic(SoundTitle.EVOLUTION_VIEW);
    }

    private boolean isFinishedEvolving() {
        return state == State.END || state == State.LEARN_MOVE;
    }

    private void setState(State state) {
        this.state = state;

        // Use pre-evolution for evolved eggs and unevolved Pokemon
        final boolean finishedEvolving = this.isFinishedEvolving();
        Color[] backgroundColors = PokeType.getColors(preEvolution);
        if (finishedEvolving && !isEgg) {
            backgroundColors = PokeType.getColors(evolvingPokemon);
        } else if (!finishedEvolving && isEgg) {
            // Unhatched egg -- use normal-type colors
            backgroundColors = new PokeType(Type.NORMAL).getColors();
        }

        canvasPanel.withBackgroundColors(backgroundColors);
    }

    private enum State {
        START,
        EVOLVE,
        CANCELLED,
        LEARN_MOVE,
        END
    }
}
