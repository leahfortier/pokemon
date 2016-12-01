package gui.view.battle;

import battle.Battle;
import gui.Button;
import gui.GameData;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
import main.Game;
import main.Global;
import map.TerrainType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pokemon.ActivePokemon;
import sound.SoundPlayer;
import sound.SoundTitle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class BattleView extends View {
	
	// The current battle in view, the current message being displayed, and the current selected button
	private Battle currentBattle;
	public String message;
	private int selectedButton;
	
	// The current state that the battle is in and current update type
	public VisualState state;
	private Update update;
	
	// Holds the animation for the player and the opponent
	public final PokemonAnimationState playerAnimation;
	public final PokemonAnimationState enemyAnimation;

	// All the different buttons!!
	public final Button backButton;
	
	public BattleView() {
		playerAnimation = new PokemonAnimationState(this);
		enemyAnimation = new PokemonAnimationState(this);

		// Back Button
		backButton = new Button(750, 560, 35, 20, null);
	}
	
	public void setBattle(Battle b) {
		currentBattle = b;
		selectedButton = 0;
		
		playerAnimation.resetVals(b.getPlayer().front());
		enemyAnimation.resetVals(b.getOpponent().front());
		
		playerAnimation.state.imageNumber = 0;
		enemyAnimation.state.imageNumber = 0;
		
		setVisualState(VisualState.MESSAGE);
		update = Update.NO_UPDATE;
		state.reset();

		currentBattle.getPlayer().clearLogMessages();
	}

	@Override
	public void update(int dt) {
		state.update(this);
		update.performUpdate(this);
	}
	
	public Battle getCurrentBattle() {
		return this.currentBattle;
	}
	
	public void setSwitchForced() {
		VisualState.setSwitchForced();
	}

	public void setSelectedButton(Button[] buttons) {
		int buttonIndex = Button.update(buttons, this.selectedButton);
		this.setSelectedButton(buttonIndex);
	}

	public void setSelectedButton(int buttonIndex) {
		this.selectedButton = buttonIndex;
	}
	
	public void clearUpdate() {
		this.update = Update.NO_UPDATE;
	}
	
	public void setVisualState(VisualState newState) {
		if (state != newState) {
			selectedButton = 0;
		}

		state = newState;
		
		// Update the buttons that should be active
		state.set(this);
	}

	public void cycleMessage(boolean updated) {
		if (!updated) {
			setVisualState(VisualState.MENU);
		}

		if (Messages.hasMessages()) {
			if (updated && !Messages.nextMessageEmpty()) {
				return;
			}
			
			MessageUpdate newMessage = Messages.getNextMessage();
			currentBattle.getPlayer().addLogMessage(newMessage);
			
			PokemonAnimationState state = newMessage.target() ? playerAnimation : enemyAnimation;
			if (newMessage.switchUpdate()) {
				state.resetVals(
						newMessage.getHP(),
						newMessage.getStatus(),
						newMessage.getType(),
						newMessage.getShiny(),
						newMessage.getPokemon(),
						newMessage.getName(),
						newMessage.getMaxHP(),
						newMessage.getLevel(),
						newMessage.getGender(),
						newMessage.getEXPRatio());
			}
			else {
				// TODO: Fuck this I hate this
				if (newMessage.healthUpdate()) {
					state.startHpAnimation(newMessage.getHP());
				}

				if (newMessage.maxHealthUpdate()) {
					state.setMaxHP(newMessage.getMaxHP());
				}

				if (newMessage.statusUpdate()) {
					state.setStatus(newMessage.getStatus());
				}

				if (newMessage.typeUpdate()) {
					state.setType(newMessage.getType());
				}

				if (newMessage.catchUpdate()) {
					state.startCatchAnimation(newMessage.getDuration() == -1? -1 : newMessage.getDuration());
				}

				if (newMessage.pokemonUpdate()) {
					state.startPokemonUpdateAnimation(newMessage.getPokemon(), newMessage.getShiny(), newMessage.isAnimate());
				}

				if (newMessage.hasUpdateType()) {
					update = newMessage.getUpdateType();
				}

				if (newMessage.expUpdate()) {
					state.startExpAnimation(newMessage.getEXPRatio(), newMessage.levelUpdate());
				}

				if (newMessage.levelUpdate()) {
					SoundPlayer.soundPlayer.playSoundEffect(SoundTitle.LEVEL_UP);
					state.setLevel(newMessage.getLevel());
				}

				if (newMessage.nameUpdate()) {
					state.setName(newMessage.getName());
				}

				if (newMessage.genderUpdate()) {
					state.setGender(newMessage.getGender());
				}

				this.state.checkMessage(newMessage);
			}
			
			if (newMessage.getMessage().isEmpty()) {
				cycleMessage(updated);
			}
			else {
				message = newMessage.getMessage();
				setVisualState(VisualState.MESSAGE);
				cycleMessage(true);
			}
		}
		else if (!updated) {
			message = null;
		}
	}

	@Override
	public void draw(Graphics g) {
		Dimension d = Global.GAME_SIZE;
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, d.width, d.height);
		
		ActivePokemon player = currentBattle.getPlayer().front();
		ActivePokemon opponent = currentBattle.getOpponent().front();

		GameData data = Game.getData();
		TileSet tiles = data.getBattleTiles();
		 
		// Get background based on terrain type
		TerrainType terrainType = currentBattle.getTerrainType();
		g.drawImage(tiles.getTile(terrainType.getBackgroundIndex()), 0, 0, null);

		// Player's battle circle
		g.drawImage(tiles.getTile(terrainType.getPlayerCircleIndex()), 0, 331, null);
		
		// Opponent battle circle
		g.drawImage(tiles.getTile(terrainType.getOpponentCircleIndex()), 450, 192, null);
		
		if (playerAnimation.isEmpty()) {
			if (enemyAnimation.isEmpty()) {
				g.setClip(0, 440, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
			}
			else {
				g.setClip(0, 0, Global.GAME_SIZE.width, 250);
			}
		}
		else if (enemyAnimation.isEmpty()) {
			g.setClip(0, 250, Global.GAME_SIZE.width, 440);
		}
		
		// Draw Status Box Backgrounds
		g.translate(463,  304);
		playerAnimation.drawStatusBox(g, 0, player, data.getPokemonTilesLarge(), 190 - 463, 412 - 304);
		g.translate(-463, -304);

		g.translate(42, 52);
		enemyAnimation.drawStatusBox(g, 1, opponent, data.getPokemonTilesMedium(), 565, 185);
		g.translate(-42, -52);

		// Draw Status Box Foregrounds
		g.drawImage(tiles.getTile(1), 0, 0, null);
		
		// Draw Status Box Text
		g.translate(463,  304);
		playerAnimation.drawStatusBoxText(g, 0, tiles, player);
		g.translate(-463, -304);
		
		g.translate(42,  52);
		enemyAnimation.drawStatusBoxText(g, 1, tiles, opponent);
		g.translate(-42, -52);
		
		g.setClip(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
		
		state.draw(this, g, tiles);
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.BATTLE_VIEW;
	}

	@Override
	public void movedToFront() {
		cycleMessage(false);
	}
}
