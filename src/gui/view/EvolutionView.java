package gui.view;

import battle.attack.Attack;
import battle.attack.Move;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.panel.BasicPanels;
import draw.button.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.Stat;
import pokemon.evolution.BaseEvolution;
import trainer.player.Player;
import type.Type;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.List;

class EvolutionView extends View {
	private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	private static final Point POKEMON_DRAW_LOCATION = Point.scaleDown(Global.GAME_SIZE, 2);
	private static final int NUM_COLS = 4;

	private final DrawPanel canvasPanel;
	private final DrawPanel statsPanel;

	private final DrawPanel moveDetailsPanel;
	private final Button[] buttons;
	private Move learnedMove;
	private int selectedButton;

	private int animationEvolve;

	private ActivePokemon evolvingPokemon;
	private PokemonInfo preEvolution;
	private PokemonInfo postEvolution;
	private boolean isEgg;
	
	private State state;
	private ArrayDeque<MessageUpdate> messages;
		
	private enum State {
		START,
		EVOLVE,
		CANCELED,
		LEARN_MOVE_QUESTION,
		LEARN_MOVE_DELETE,
		END
	}

	EvolutionView() {
		this.canvasPanel = DrawPanel.fullGamePanel()
				.withTransparentCount(2)
				.withBorderPercentage(0);

		this.statsPanel = new DrawPanel(0, 280, 273, 161).withBlackOutline();

		moveDetailsPanel = new DrawPanel(0, 440 - 161, 385, 161).withBorderPercentage(8).withBlackOutline().withTransparentCount(2);

		// Create a button for each known move and then one for the new move and one for not learning
		buttons = BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS);
	}

	private void updateActiveButtons() {
		switch (state) {
			case LEARN_MOVE_QUESTION:
				for (int i = 0; i < buttons.length; i++) {
					Button button = buttons[i];
					button.setActive(button == yesButton() || button == noButton());
					if (button == yesButton()) {
						selectedButton = i;
					}
				}
				break;
			case LEARN_MOVE_DELETE:
				for (int y = 0; y < 2; y++) {
					for (int x = 0; x < NUM_COLS; x++) {
						int index = Point.getIndex(x, y, NUM_COLS);
						buttons[index].setActive(x < Move.MAX_MOVES/2 || buttons[index] == newMoveButton());
						if (buttons[index] == newMoveButton()) {
							selectedButton = index;
						}
					}
				}
				break;
			default:
				for (Button button : buttons) {
					button.setActive(false);
				}
				break;
		}

	}

	// Bottom center left
	private Button yesButton() {
		return buttons[NUM_COLS + 1];
	}

	// Bottom center right
	private Button noButton() {
		return buttons[NUM_COLS + 2];
	}

	private Button newMoveButton() {
		return this.buttons[buttons.length - 2];
	}

	@Override
	public void update(int dt) {
		if (BasicPanels.isAnimatingMessage()) {
			return;
		}

		selectedButton = Button.update(buttons, selectedButton);

		InputControl input = InputControl.instance();
		switch (state) {
			case START:
				if (!messages.isEmpty()) {
					if (input.consumeIfMouseDown(ControlKey.SPACE)) {
						messages.pop();
					}
				}
				else {
					state = State.EVOLVE;
				}
				break;
			case EVOLVE:
				if (input.consumeIfDown(ControlKey.BACK) && !isEgg) {
					state = State.CANCELED;
					setCancelledMessage();
				}

				if (animationEvolve <= 0) {
					state = State.END;

					if (isEgg) {
						messages.add(new MessageUpdate("Your egg hatched into " + StringUtils.articleString(preEvolution.getName()) + "!"));
					} else {
						int[] gains = evolvingPokemon.evolve(Game.getPlayer().getEvolution());
						int[] stats = evolvingPokemon.getStats();

						messages.add(new MessageUpdate(
								"Your " + preEvolution.getName() + " evolved into " + StringUtils.articleString(postEvolution.getName()) + "!")
								.withStatGains(gains, stats));
					}

					Game.getPlayer().pokemonEvolved(evolvingPokemon);

				}
				break;
			case LEARN_MOVE_QUESTION:
				if (noButton().checkConsumePress()) {
					state = State.END;
					messages.pop();
					messages.add(new MessageUpdate(evolvingPokemon.getActualName() + " did not learn " + learnedMove.getAttack().getName() + "."));
					updateActiveButtons();
				}

				if (yesButton().checkConsumePress()) {
					state = State.LEARN_MOVE_DELETE;
					messages.pop();
					updateActiveButtons();
				}
				break;
			case LEARN_MOVE_DELETE:
				for (int y = 0, moveIndex = 0; y < 2; y++) {
					for (int x = 0; x < Move.MAX_MOVES/2; x++, moveIndex++) {
						int index = Point.getIndex(x, y, NUM_COLS);
						if (buttons[index].checkConsumePress()) {
							ActivePokemon learner = evolvingPokemon;
							String learnerName = learner.getActualName();

							Move learnMove = learnedMove;
							String learnMoveName = learnMove.getAttack().getName();
							String deleteMoveName = learner.getActualMoves().get(moveIndex).getAttack().getName();

							learner.addMove(learnMove, moveIndex, true);

							messages.addFirst(new MessageUpdate("...and " + learnerName + " learned " + learnMoveName + "!"));
							messages.addFirst(new MessageUpdate(learnerName + " forgot how to use " + deleteMoveName + "..."));

							state = State.END;
							updateActiveButtons();
						}
					}
				}

				if (newMoveButton().checkConsumePress()) {
					ActivePokemon learner = evolvingPokemon;
					Move move = learnedMove;

					messages.pop();
					messages.addFirst(new MessageUpdate(learner.getActualName() + " did not learn " + move.getAttack().getName() + "."));
					state = State.END;
					updateActiveButtons();
				}
				break;
			case END:
			case CANCELED:
				if (!messages.isEmpty()) {
					if (input.consumeIfMouseDown(ControlKey.SPACE)) {
						if (Messages.peek().learnMove()) {
							MessageUpdate message = Messages.getNextMessage();
							messages.pop();

							learnedMove = message.getMove();
							messages.add(new MessageUpdate(
									evolvingPokemon.getName() + " is trying to learn " + learnedMove.getAttack().getName() + "...")
									.withUpdate(Update.LEARN_MOVE)
									.withLearnMove(evolvingPokemon, learnedMove));
							messages.add(new MessageUpdate("Delete a move in order to learn " + learnedMove.getAttack().getName() + "?"));
							updateActiveButtons();
						}
						else {
							MessageUpdate message = messages.pop();
							if (message.learnMove()) {
								state = State.LEARN_MOVE_QUESTION;
								learnedMove = message.getMove();
								updateActiveButtons();
							}
						}
					}
				}
				else {
					Game.instance().popView();
				}
				break;
		}
	}

	@Override
	public void draw(Graphics g) {
		final GameData data = Game.getData();
		final boolean endState = state == State.END || state == State.LEARN_MOVE_DELETE || state == State.LEARN_MOVE_QUESTION;

		if (!endState) {
			if (isEgg) {
				canvasPanel.withBackgroundColor(Type.NORMAL.getColor());
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

		if (!messages.isEmpty()) {
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
		} else if (state == State.LEARN_MOVE_DELETE) {
			BasicPanels.drawFullMessagePanel(g, StringUtils.empty());
		}

		TileSet pokemonTiles = data.getPokemonTilesLarge();

		FontMetrics.setFont(g, 30);
		g.setColor(Color.BLACK);

		String preIndex = isEgg ? ActivePokemon.SPRITE_EGG_IMAGE_NAME : preEvolution.getImageName(evolvingPokemon.isShiny());
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
			case LEARN_MOVE_QUESTION:
				drawButton(g, yesButton(), new Color(120, 200, 80), "Yes");
				drawButton(g, noButton(), new Color(220, 20, 20), "No");
				break;
			case LEARN_MOVE_DELETE:
				List<Move> moves = evolvingPokemon.getActualMoves();
				Attack selected = null;
				for (int y = 0, moveIndex = 0; y < 2; y++) {
					for (int x = 0; x < Move.MAX_MOVES/2; x++, moveIndex++) {
						int index = Point.getIndex(x, y, NUM_COLS);
						Move move = moves.get(moveIndex);

						buttons[index].drawMoveButton(g, move);
						if (index == selectedButton) {
							selected = move.getAttack();
						}
					}
				}

				moveDetailsPanel.drawMovePanel(g, selected == null ? learnedMove.getAttack() : selected);
				newMoveButton().drawMoveButton(g, learnedMove);
				break;
		}

		for (Button button : buttons) {
			button.draw(g);
		}
	}

	private void drawButton(Graphics g, Button button, Color color, String label) {
		button.fillBordered(g, color);
		button.blackOutline(g);
		button.label(g, 30, label);
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
		}
		else {
			isEgg = false;
			postEvolution = evolve.getEvolution();
		}
	}
	
	private void setInitialMessage() {
		if (isEgg) {
			messages.add(new MessageUpdate("Your egg is hatching!"));
		}
		else {
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

		Player player = Game.getPlayer();

		setPokemon(player.getEvolvingPokemon(), player.getEvolution());
		setInitialMessage();
		
		animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
		
		// TODO: Save current sound for when transitioning to the bag view.
		//Global.soundPlayer.playMusic(SoundTitle.EVOLUTION_VIEW);

		updateActiveButtons();
	}
}
