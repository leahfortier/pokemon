package gui.view;

public enum ViewMode {
    BAG_VIEW(BagView::new),
    BATTLE_VIEW(BattleView::new),
    EVOLUTION_VIEW(EvolutionView::new),
    MAIN_MENU_VIEW(MainMenuView::new),
    MAP_VIEW(MapView::new),
    MART_VIEW(MartView::new),
    OPTIONS_VIEW(OptionsView::new),
    PARTY_VIEW(PartyView::new),
    PC_VIEW(PCView::new),
    POKEDEX_VIEW(PokedexView::new),
    START_VIEW(StartView::new),
    TRAINER_CARD_VIEW(TrainerCardView::new);

    private final ViewCreator viewCreator;

    ViewMode(ViewCreator viewCreator) {
        this.viewCreator = viewCreator;
    }

    private interface ViewCreator {
        View createView();
    }

    public View createView() {
        return this.viewCreator.createView();
    }
}
