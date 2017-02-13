package gui.view;

import draw.Alignment;
import gui.GameData;
import gui.TileSet;
import draw.button.panel.BasicPanels;
import draw.button.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import message.MessageUpdate;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.Stat;
import pokemon.evolution.BaseEvolution;
import trainer.CharacterData;
import draw.DrawUtils;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class EvolutionView extends View {
	private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	private static final Point POKEMON_DRAW_LOCATION = Point.scaleDown(Global.GAME_SIZE, 2);

	private final DrawPanel statsPanel;

	private int animationEvolve;

	private ActivePokemon evolvingPokemon;
	private PokemonInfo preEvolution;
	private PokemonInfo postEvolution;
	private boolean isEgg;
	
	private State state;
	private MessageUpdate message;
		
	private enum State {
		START,
		EVOLVE,
		END,
		CANCELED
	}

	EvolutionView() {
		this.statsPanel = new DrawPanel(0, 280, 273, 161).withBlackOutline();
	}

	@Override
	public void update(int dt) {
		InputControl input = InputControl.instance();
		switch (state) {
			case START:
				if (message != null) {
					if (input.consumeIfMouseDown()) {
						message = null;
					}

					if (input.consumeIfDown(ControlKey.SPACE)) {
						message = null;
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

					int[] gains = evolvingPokemon.evolve(Game.getPlayer().getEvolution());
					int[] stats = evolvingPokemon.getStats();

					setFinalMessage(gains, stats);
					addToPokedex();
				}
				break;
			case END:
			case CANCELED:
				if (message != null) {
					if (input.consumeIfMouseDown()) {
						message = null;
					}

					if (input.consumeIfDown(ControlKey.SPACE)) {
						message = null;
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

		TileSet pokemonTiles = data.getPokemonTilesMedium();

		BasicPanels.drawCanvasPanel(g);
		
		FontMetrics.setFont(g, 30);
		g.setColor(Color.BLACK);
		
		int preIndex = isEgg ? PokemonInfo.EGG_IMAGE : preEvolution.getImageNumber(evolvingPokemon.isShiny());
		int postIndex = isEgg ? preEvolution.getImageNumber(evolvingPokemon.isShiny()) : postEvolution.getImageNumber(evolvingPokemon.isShiny());
		
		BufferedImage currEvolution = pokemonTiles.getTile(preIndex);
		BufferedImage nextEvolution = pokemonTiles.getTile(postIndex);
		
		switch (state) {
			case START:
			case CANCELED:
				DrawUtils.drawBottomCenteredImage(g, currEvolution, POKEMON_DRAW_LOCATION);
				break;
			case EVOLVE:
				evolveAnimation(g, currEvolution, nextEvolution);
				break;
			case END:
				DrawUtils.drawBottomCenteredImage(g, nextEvolution, POKEMON_DRAW_LOCATION);
				break;
		}
		
		if (message != null) {
			BasicPanels.drawFullMessagePanel(g, message.getMessage());
			if (message.gainUpdate()) {
				statsPanel.drawBackground(g);

				int[] statGains = message.getGain();
				int[] newStats = message.getNewStats();

				g.setColor(Color.BLACK);
				for (int i = 0; i < Stat.NUM_STATS; i++) {
					FontMetrics.setFont(g, 16);
					g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);

					Alignment.drawRightAlignedString(g, (statGains[i] < 0 ? "" : " + ") + statGains[i], 206, 314 + i*21);
					Alignment.drawRightAlignedString(g, newStats[i] + "", 247, 314 + i*21);
				}
			}
		}
	}
	
	private void evolveAnimation(Graphics g, BufferedImage currEvolution, BufferedImage nextEvolution) {
		animationEvolve = DrawUtils.transformAnimation(
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
			message = new MessageUpdate("Your egg is hatching!");
		}
		else {
			message = new MessageUpdate("Your " + preEvolution.getName() + " is evolving!");
		}
	}
	
	private void addToPokedex() {
		Game.getPlayer().getPokedex().setCaught(isEgg ? preEvolution : postEvolution);
	}

	private void setCancelledMessage() {
		message = new MessageUpdate("Whattt!?!?!??!! " + preEvolution.getName() + " stopped evolving!!!!");
	}
	
	private void setFinalMessage(int[] gains, int[] newStats) {
		if (isEgg) {
			message = new MessageUpdate("Your egg hatched into " + StringUtils.articleString(preEvolution.getName()) + "!");
		}
		else {
			message = new MessageUpdate("Your " + preEvolution.getName() + " evolved into " + StringUtils.articleString(postEvolution.getName()) + "!").withStatGains(gains, newStats);
		}
	}

	@Override
	public void movedToFront() {
		state = State.START;

		CharacterData player = Game.getPlayer();

		setPokemon(player.getEvolvingPokemon(), player.getEvolution());
		setInitialMessage();
		
		animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
		
		// TODO: Save current sound for when transitioning to the bag view.
		//Global.soundPlayer.playMusic(SoundTitle.EVOLUTION_VIEW);
	}
}
