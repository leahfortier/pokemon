package gui.view;

import gui.GameData;
import gui.TileSet;
import gui.panel.BasicPanels;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import util.DrawUtils;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class EvolutionView extends View {
	private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	private static final Point POKEMON_DRAW_LOCATION = Point.scaleDown(Global.GAME_SIZE, 2);
	
	private int animationEvolve;

	private ActivePokemon evolvingPokemon;
	private PokemonInfo preEvolution;
	private PokemonInfo postEvolution;
	private boolean isEgg;
	
	private State state;
	private String message;
		
	private enum State {
		START,
		EVOLVE,
		END,
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
				if (animationEvolve < 0) {
					state = State.END;
					setFinalMessage();
					addToPokedex();
				}
				break;
			case END:
				if (message != null) {
					if (input.consumeIfMouseDown()) {
						message = null;
					}

					if (input.consumeIfDown(ControlKey.SPACE)) {
						message = null;
					}
				}
				else {
					if (!isEgg) {
						evolvingPokemon.evolve(null, Game.getPlayer().evolution);
						Game.setViewMode(ViewMode.BAG_VIEW);
					}
					else {
						Game.setViewMode(ViewMode.MAP_VIEW);
					}
				}
				break;
		}
	}

	@Override
	public void draw(Graphics g) {
		final GameData data = Game.getData();

		TileSet tiles = data.getMenuTiles();
		TileSet pokemonTiles = data.getPokemonTilesMedium();
		
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		
		FontMetrics.setFont(g, 30);
		g.setColor(Color.BLACK);
		
		int preIndex = isEgg ? PokemonInfo.EGG_IMAGE : preEvolution.getImageNumber(evolvingPokemon.isShiny());
		int postIndex = isEgg ? preEvolution.getImageNumber(evolvingPokemon.isShiny()) : postEvolution.getImageNumber(evolvingPokemon.isShiny());
		
		BufferedImage currEvolution = pokemonTiles.getTile(preIndex);
		BufferedImage nextEvolution = pokemonTiles.getTile(postIndex);
		
		switch (state) {
			case START:
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
			BasicPanels.drawFullMessagePanel(g, message);
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
			message = "Your egg is hatching!"; // TODO: this is lame
		}
		else {
			message = "Your " + preEvolution.getName() + " is evolving!";
		}
	}
	
	private void addToPokedex() {
		Game.getPlayer().getPokedex().setCaught(isEgg ? preEvolution : postEvolution);
	}
	
	private void setFinalMessage() {
		if (isEgg) {
			message = "Your egg hatched into " + StringUtils.articleString(preEvolution.getName()) + "!";
		}
		else {
			message = "Your " + preEvolution.getName() + " evolved into " + StringUtils.articleString(postEvolution.getName()) + "!";
		}
	}

	@Override
	public void movedToFront() {
		state = State.START;

		CharacterData player = Game.getPlayer();

		setPokemon(player.evolvingPokemon, player.evolution);
		setInitialMessage();
		
		animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
		
		// TODO: Save current sound for when transitioning to the bag view.
		//Global.soundPlayer.playMusic(SoundTitle.EVOLUTION_VIEW);
	}
}
