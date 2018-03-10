package gui.view.map;

import battle.Battle;
import draw.DrawUtils;
import draw.TileUtils;
import gui.GameData;
import gui.IndexTileSet;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.MapDataType;
import map.MapName;
import map.PathDirection;
import map.area.AreaData;
import map.daynight.DayCycle;
import map.entity.Entity;
import map.entity.movable.MovableEntity;
import map.entity.movable.NPCEntity;
import map.entity.movable.PlayerEntity;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.player.Player;
import trainer.player.medal.Medal;
import util.Point;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MapView extends View {

    private static final PathDirection[] deltaDirections = {
            PathDirection.WAIT,
            PathDirection.LEFT,
            PathDirection.UP
    };

    private MapName currentMapName;
    private AreaData currentArea;
    private MapData currentMap;
    private SoundTitle currentMusicTitle;

    private Point start;
    private Point end;
    private Point draw;

    private int areaDisplayTime;

    private int medalDisplayTime;
    private Medal displayMedal;

    private VisualState state;
    private MessageUpdate currentMessage;

    public MapView() {
        currentMapName = null;
        setState(VisualState.MAP);

        areaDisplayTime = 0;
        medalDisplayTime = 0;

        start = new Point();
        end = new Point();
        draw = new Point();
    }

    boolean isState(VisualState state) {
        return this.state == state;
    }

    public void setState(VisualState newState) {
        this.state = newState;
        this.state.set(this);
    }

    MapData getCurrentMap() {
        return this.currentMap;
    }

    AreaData getCurrentArea() {
        return this.currentArea;
    }

    MessageUpdate getCurrentMessage() {
        return this.currentMessage;
    }

    @Override
    public void draw(Graphics g) {
        DrawUtils.fillCanvas(g, Color.BLACK);
        drawTiles(g);

        if (currentArea != null) {
            currentArea.getWeather().draw(g);

            if (!currentArea.getTerrain().isInterior()) {
                DayCycle.getTimeOfDay().draw(g);
            }

            // Area Transition
            if (areaDisplayTime > 0) {
                DrawUtils.drawAreaTransitionAnimation(g, currentArea.getAreaName(), areaDisplayTime);
            }
        }

        if (medalDisplayTime > 0) {
            DrawUtils.drawAreaTransitionAnimation(g, "Medal Earned: " + displayMedal.getMedalName() + "!", medalDisplayTime);
        }

        state.draw(g, this);
    }

    private void drawTiles(Graphics g) {
        GameData data = Game.getData();
        IndexTileSet mapTiles = data.getMapTiles();

        // Background
        for (int y = start.y; y < end.y; y++) {
            for (int x = start.x; x < end.x; x++) {
                int bgTile = currentMap.getRGB(x, y, MapDataType.BACKGROUND);
                if (TileSet.isValidMapTile(bgTile)) {
                    BufferedImage img = mapTiles.getTile(bgTile);
                    TileUtils.drawTileImage(g, img, x, y, draw);
                }
            }
        }

        // Back-foreground
        for (int y = start.y; y < end.y; y++) {
            for (int x = start.x; x < end.x; x++) {

                // Draw foreground tiles
                int fgTile = currentMap.getRGB(x, y, MapDataType.BACK_FOREGROUND);
                if (TileSet.isValidMapTile(fgTile)) {
                    BufferedImage img = mapTiles.getTile(fgTile);
                    TileUtils.drawTileImage(g, img, x, y, draw);
                }
            }
        }

        // Foreground
        for (int y = start.y; y < end.y; y++) {
            for (int x = start.x; x < end.x; x++) {

                // Draw foreground tiles
                int fgTile = currentMap.getRGB(x, y, MapDataType.FOREGROUND);
                if (TileSet.isValidMapTile(fgTile)) {
                    BufferedImage img = mapTiles.getTile(fgTile);
                    TileUtils.drawTileImage(g, img, x, y, draw);
                }

                // Draw entities
                // Check for entities above and to the left of this location to see if they just moved out and draw them again.
                for (PathDirection pathDirection : deltaDirections) {
                    Point delta = pathDirection.getDeltaPoint();
                    Point newPoint = Point.add(delta, x, y);

                    Entity newPointEntity = currentMap.getEntity(newPoint);
                    if (newPointEntity == null) {
                        continue;
                    }

                    // TODO: I'm getting really confused about this whole check up and left only thing what is happening
                    // If entity is a movable entity and they are moving right or down, do not draw them again.
                    if (newPointEntity instanceof MovableEntity) {
                        Direction transitionDirection = ((MovableEntity)newPointEntity).getDirection();
                        if (!delta.isZero() && (transitionDirection == Direction.RIGHT || transitionDirection == Direction.DOWN)) {
                            continue;
                        }
                    }
                    // Not a movable entity, only draw once.
                    else if (!delta.isZero()) {
                        continue;
                    }

                    // TODO: Supes Haxorus plz fix
                    if (newPointEntity instanceof NPCEntity && newPointEntity.getEntityName().equals("MaplesLab_NPC_Prof_Mapes_01")) {
                        newPointEntity.draw(g, new Point(draw.x + Global.TILE_SIZE/2, draw.y), !delta.isZero());
                    } else {
                        // TODO: Checking zero logic seems like it can be simplified
                        newPointEntity.draw(g, draw, !delta.isZero());
                    }
                }

                // Draw grass tiles
                int grassTile = currentMap.getRGB(x, y, MapDataType.TALL_GRASS);
                if (TileSet.isValidMapTile(grassTile)) {
                    BufferedImage img = mapTiles.getTile(grassTile);
                    TileUtils.drawGrassTile(g, img, x, y, draw);
                }
            }
        }
    }

    @Override
    public void update(int dt) {
        Player player = Game.getPlayer();
        PlayerEntity playerEntity = player.getEntity();

        boolean showMessage = !this.isState(VisualState.BATTLE);

        checkMapReset();

        if (areaDisplayTime > 0) {
            areaDisplayTime -= dt;
        } else if (medalDisplayTime > 0) {
            medalDisplayTime -= dt;
        }

        // New area
        AreaData area = currentMap.getArea(player.getLocation());
        String areaName = area.getAreaName();

        // If new area has a new name, display the area name animation
        if (currentArea != null && !StringUtils.isNullOrEmpty(areaName) && !areaName.equals(currentArea.getAreaName())) {
            areaDisplayTime = DrawUtils.AREA_NAME_ANIMATION_LIFESPAN;
        } else if (areaDisplayTime <= 0 && medalDisplayTime <= 0 && player.getMedalCase().isThereMedalToShow()) {
            displayMedal = player.getMedalCase().getNextMedalToShow();
            medalDisplayTime = DrawUtils.AREA_NAME_ANIMATION_LIFESPAN;
        }

        player.setArea(currentMapName, area);
        currentArea = area;

        // Queue to play new area's music.
        SoundTitle areaMusic = area.getMusic();
        if (currentMusicTitle != areaMusic) {
            currentMusicTitle = areaMusic;
            playAreaMusic();
        }

        this.state.update(dt, this);

        Point tilesLocation = Point.scaleDown(Global.GAME_SIZE, Global.TILE_SIZE);

        this.draw = playerEntity.getDrawLocation();
        this.start = Point.scaleDown(Point.negate(this.draw), Global.TILE_SIZE);
        this.end = Point.add(this.start, tilesLocation, new Point(6, 6)); // TODO: What is the 6, 6 all about?

        // Update each non-player entity on the map
        currentMap.updateEntities(dt, this);

        if (this.isState(VisualState.MAP)) {
            playerEntity.update(dt, currentMap, this);
        }

        if (showMessage && emptyMessage() && Messages.hasMessages()) {
            cycleMessage();
            if (this.currentMessage != null && this.currentMessage.getUpdateType() != MessageUpdateType.ENTER_BATTLE) {
                setState(VisualState.MESSAGE);
            }
        }

        if (isState(VisualState.MESSAGE) && emptyMessage()) {
            setState(VisualState.MAP);
        }
    }

    private boolean emptyMessage() {
        return this.currentMessage == null || StringUtils.isNullOrEmpty(this.currentMessage.getMessage());
    }

    private void checkMapReset() {
        GameData data = Game.getData();
        Player player = Game.getPlayer();

        if (player.mapReset() || currentMapName == null || !currentMapName.equals(player.getMapName())) {
            currentMapName = player.getMapName();
            currentMap = data.getMap(currentMapName);

            if (player.mapReset()) {
                player.setMapReset(false);
                currentMap.setCharacterToEntrance();
            }

            currentMap.populateEntities();
            setState(VisualState.MAP);
        }
    }

    void cycleMessage() {
        currentMessage = Messages.getNextMessage();

        // Check if the next message is a trigger and execute if it is
        if (currentMessage.trigger()) {
            Trigger trigger = currentMessage.getTrigger();
            if (trigger.isTriggered()) {
                trigger.execute();
                if (!this.isState(VisualState.MESSAGE)) {
                    currentMessage = null;
                }
            }
        } else if (currentMessage.isViewChange()) {
            Game.instance().setViewMode(currentMessage.getViewMode());
        } else if (currentMessage.resetState()) {
            resetMessageState();
        }
    }

    void resetMessageState() {
        Game.getPlayer().getEntity().resetCurrentInteractionEntity();
        this.currentMessage = null;
        if (!VisualState.hasBattle()) {
            setState(VisualState.MAP);
        }
    }

    private void playAreaMusic() {
        final SoundTitle music;
        if (currentMusicTitle != null) {
            music = currentMusicTitle;
        } else {
            if (currentArea != null) {
                System.err.println("No music specified for current area " + currentArea.getAreaName() + ".");
            }

            music = SoundTitle.DEFAULT_TUNE;
        }

        SoundPlayer.instance().playMusic(music);
    }

    public void setBattle(Battle battle, boolean seenWild) {
        this.setState(VisualState.BATTLE);
        VisualState.setBattle(battle, seenWild);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.MAP_VIEW;
    }

    @Override
    public void movedToFront() {
        playAreaMusic();
    }
}
