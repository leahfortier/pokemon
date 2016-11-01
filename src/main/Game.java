package main;

import battle.Battle;
import gui.GameData;
import gui.view.BagView;
import gui.view.BattleView;
import gui.view.EvolutionView;
import gui.view.MainMenuView;
import gui.view.MapView;
import gui.view.MartView;
import gui.view.OptionsView;
import gui.view.PCView;
import gui.view.PartyView;
import gui.view.PokedexView;
import gui.view.StartView;
import gui.view.TrainerCardView;
import gui.view.View;
import item.Item;
import item.hold.HoldItem;
import message.Messages;
import namesies.ItemNamesies;
import namesies.PokemonNamesies;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import util.InputControl;
import util.Save;

import java.awt.Graphics;
import java.util.EnumMap;
import java.util.Map;

public class Game {
	public enum ViewMode {
		BAG_VIEW,
		BATTLE_VIEW,
		EVOLUTION_VIEW,
		MAIN_MENU_VIEW,
		MAP_VIEW,
		MART_VIEW,
		OPTIONS_VIEW,
		PARTY_VIEW,
		PC_VIEW,
		POKEDEX_VIEW,
		START_VIEW,
		TRAINER_CARD_VIEW,
	}
	
	public final GameData data;
	private final Map<ViewMode, View> viewMap;
	
	public CharacterData characterData;
	private ViewMode currentViewMode;
	private View currentView;
	
	public Game() {
		data = new GameData();
		
		viewMap = new EnumMap<>(ViewMode.class);
		viewMap.put(ViewMode.MAIN_MENU_VIEW, new MainMenuView());
		
		currentViewMode = ViewMode.MAIN_MENU_VIEW;
		currentView = viewMap.get(ViewMode.MAIN_MENU_VIEW);
	}
	
	private void setupCharacter() {
		characterData.addPokemon(null, new ActivePokemon(PokemonNamesies.EEVEE, 1, false, true));
		characterData.front().giveItem((HoldItem)Item.getItem(ItemNamesies.ORAN_BERRY));
	}
	
	private void checkViewSwitch(InputControl input) {
		if (!currentView.getViewModel().equals(currentViewMode)) {
			input.resetKeys();
			currentView = viewMap.get(currentViewMode);
			currentView.movedToFront(this);
		}
	}

	public void update(int dt, InputControl input) {
		checkViewSwitch(input);
		currentView.update(dt, input, this);
		checkViewSwitch(input);
	}

	public void setBattleViews(final Battle battle, final boolean seenWildPokemon) {
		((BattleView)this.viewMap.get((ViewMode.BATTLE_VIEW))).setBattle(battle);
		((MapView)this.viewMap.get(ViewMode.MAP_VIEW)).setBattle(battle, seenWildPokemon);
	}

	public void setMapViewDialogue(final String dialogueName) {
		Messages.addMessage(this, this.data.getDialogue(dialogueName));
	}

	public void draw(Graphics g) {
		currentView.draw(g, data);
	}
	
	public void setViewMode(ViewMode mode) {
		currentViewMode = mode;
	}
	
	public ViewMode getCurrentViewMode() {
		return currentViewMode;
	}

	public void loadSave(int index) {
		characterData = Save.load(index, this);
		setViews();
	}
	
	public void newSave(int index) {
		characterData = new CharacterData(this);
		
		String startingMap = "PlayersHouseUp";
		String startingMapEntrance = "GameStartLocation";
		characterData.setMap(startingMap, startingMapEntrance);
		data.getMap(startingMap).setCharacterToEntrance(characterData, startingMapEntrance);
		
		characterData.setFileNum(index);
		setupCharacter();
		setViews();
	}

	// TODO: Should have an interface to handle this inside of the ViewMode enum
	private void setViews() {
		viewMap.put(ViewMode.MAP_VIEW, new MapView());
		viewMap.put(ViewMode.BAG_VIEW, new BagView(characterData));
		viewMap.put(ViewMode.PARTY_VIEW, new PartyView(characterData));
		viewMap.put(ViewMode.PC_VIEW, new PCView(characterData));
		viewMap.put(ViewMode.POKEDEX_VIEW, new PokedexView(characterData.getPokedex()));
		viewMap.put(ViewMode.MART_VIEW, new MartView(characterData));
		viewMap.put(ViewMode.TRAINER_CARD_VIEW, new TrainerCardView(characterData));
		viewMap.put(ViewMode.BATTLE_VIEW, new BattleView());
		viewMap.put(ViewMode.OPTIONS_VIEW, new OptionsView());
		viewMap.put(ViewMode.START_VIEW, new StartView(characterData));
		viewMap.put(ViewMode.EVOLUTION_VIEW, new EvolutionView(characterData));
	}
}
