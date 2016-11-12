package gui.view;

import gui.GameData;
import gui.TileSet;
import main.Game;
import main.Game.ViewMode;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import trainer.Pokedex.PokedexStatus;
import util.DrawUtils;
import util.InputControl;
import util.InputControl.Control;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class EvolutionView extends View {
	private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	
	private static final float[] prevEvolutionScales = { 1f, 1f, 1f, 1f };
	private static final float[] prevEvolutionOffsets = { 255f, 255f, 255f, 0f };
	private static final float[] evolutionScales = { 1f, 1f, 1f, 1f };
	private static final float[] evolutionOffsets = { 255f, 255f, 255f, 0f };
	
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
	
	public void update(int dt, InputControl input) {
		switch (state) {
			case START:
				if (message != null) {
					if (input.mouseDown) {
						input.consumeMousePress();
						message = null;
					}

					if (input.isDown(Control.SPACE)) {
						input.consumeKey(Control.SPACE);
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
					if (input.mouseDown) {
						input.consumeMousePress();
						message = null;
					}

					if (input.isDown(Control.SPACE)) {
						input.consumeKey(Control.SPACE);
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

	public void draw(Graphics g) {
		final GameData data = Game.getData();

		TileSet tiles = data.getMenuTiles();
		TileSet battleTiles = data.getBattleTiles();
		TileSet pokemonTiles = data.getPokemonTilesMedium();
		
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		
		DrawUtils.setFont(g, 30);
		g.setColor(Color.BLACK);
		
		int preIndex = isEgg ? PokemonInfo.EGG_IMAGE : preEvolution.getImageNumber(evolvingPokemon.isShiny());
		int postIndex = isEgg ? preEvolution.getImageNumber(evolvingPokemon.isShiny()) : postEvolution.getImageNumber(evolvingPokemon.isShiny());
		
		BufferedImage currEvolution = pokemonTiles.getTile(preIndex);
		BufferedImage nextEvolution = pokemonTiles.getTile(postIndex);
		
		int drawWidth = Global.GAME_SIZE.width/2;
		int drawHeight = Global.GAME_SIZE.height/2;
		
		switch (state) {
			case START:
				DrawUtils.drawCenteredImage(g, currEvolution, drawWidth, drawHeight);
				break;
			case EVOLVE:
				evolveAnimation(g, currEvolution, nextEvolution, drawWidth, drawHeight);
				break;
			case END:
				DrawUtils.drawCenteredImage(g, nextEvolution, drawWidth, drawHeight);
				break;
		}
		
		if (message != null) {
			g.drawImage(battleTiles.getTile(0x3), 0, 440, null);
			DrawUtils.setFont(g, 30);
			DrawUtils.drawWrappedText(g, message, 30, 490, 750);
		}
	}
	
	private void evolveAnimation(Graphics g, BufferedImage currEvolution, BufferedImage nextEvolution, int px, int py) {
		Graphics2D g2d = (Graphics2D)g;

		// TODO: Is this the same as the one in battle view?
		// Turn white
		if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.7) {
			prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255*(1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.7f)/(EVOLVE_ANIMATION_LIFESPAN*(1 - 0.7f)));
			evolutionScales[3] = 0;
		}
		// Change form
		else if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.3) {
			prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255;
			prevEvolutionScales[3] = ((animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
			evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255;
			evolutionScales[3] = (1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
		}
		// Restore color
		else {
			prevEvolutionScales[3] = 0;
			evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255*(animationEvolve)/(EVOLVE_ANIMATION_LIFESPAN*(1 - 0.7f));
		}
		
		animationEvolve -= Global.MS_BETWEEN_FRAMES;
		
		// TODO: Why does this need Graphics2D instead of just Graphics for just drawing an image? See if this can use the center function as well
		g2d.drawImage(DrawUtils.colorImage(nextEvolution, evolutionScales, evolutionOffsets), px-nextEvolution.getWidth()/2, py-nextEvolution.getHeight()/2, null);
		g2d.drawImage(DrawUtils.colorImage(currEvolution, prevEvolutionScales, prevEvolutionOffsets), px-currEvolution.getWidth()/2, py-currEvolution.getHeight()/2, null);
	}

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
		Game.getPlayer().getPokedex().setStatus(isEgg ? preEvolution : postEvolution, PokedexStatus.CAUGHT);
	}
	
	private void setFinalMessage() {
		if (isEgg) {
			message = "Your egg hatched into " + StringUtils.articleString(preEvolution.getName()) + "!";
		}
		else {
			message = "Your " + preEvolution.getName() + " evolved into " + StringUtils.articleString(postEvolution.getName()) + "!";
		}
	}
	
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
