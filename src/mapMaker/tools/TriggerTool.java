package mapMaker.tools;

import draw.TileUtils;
import mapMaker.EditType;
import mapMaker.MapMaker;
import pattern.location.LocationTriggerMatcher;
import pattern.location.SinglePointTriggerMatcher;
import util.Action;
import util.Point;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.util.List;

public class TriggerTool extends Tool {
    private final TriggerToolPopup triggerListPopup;
    private final TriggerToolPopup triggerOptionsPopup;

    private final JMenuItem editOption;
    private final JMenuItem moveOption;
    private final JMenuItem addOption;
    private final JMenuItem removeOption;
    private final JMenuItem removeSomeOption;

    private LocationTriggerMatcher selectedTrigger;

    private State state;

    TriggerTool(MapMaker mapMaker) {
        super(mapMaker, ToolType.TRIGGER);

        selectedTrigger = null;

        triggerListPopup = new TriggerToolPopup();
        triggerOptionsPopup = new TriggerToolPopup();

        editOption = createMenuItem("Edit", event -> mapMaker.getTriggerData().editTrigger(selectedTrigger));
        removeOption = createMenuItem("Remove", event -> mapMaker.getTriggerData().removeTrigger(selectedTrigger));

        addOption = createMenuItem("Add", setPlaceableTriggerState(State.ADD));
        removeSomeOption = createMenuItem("Remove Some", setPlaceableTriggerState(State.REMOVE));

        moveOption = createMenuItem("Move", setTriggerState(
                State.MOVE, ToolType.SINGLE_CLICK,
                () -> mapMaker.getTriggerData().moveTrigger(selectedTrigger)
                                    )
        );
    }

    private JMenuItem createMenuItem(String label, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    // Set the tool type to rectangle and the set the placeable trigger for the map maker
    private ActionListener setPlaceableTriggerState(State state) {
        return setTriggerState(state, ToolType.RECTANGLE, () -> mapMaker.setPlaceableTrigger(selectedTrigger));
    }

    private ActionListener setTriggerState(State state, ToolType toolType, Action action) {
        return event -> {
            mapMaker.setTool(toolType);
            this.state = state;

            mapMaker.setEditType(EditType.TRIGGERS);
            action.performAction();
            mapMaker.setSelectedTileIndex(selectedTrigger.getTriggerModelType().ordinal());

            // State needs to be reset back to remove since it will be reset when selecting the tile index above
            if (state == State.REMOVE) {
                this.state = state;
            }
        };
    }

    public boolean inUse() {
        return this.state != State.NONE;
    }

    public boolean isRemoving() {
        return this.state == State.REMOVE;
    }

    @Override
    public void reset() {
        this.state = State.NONE;
    }

    @Override
    public void click(Point clickLocation) {
        if (!mapMaker.hasMap()) {
            return;
        }

        Point location = TileUtils.getLocation(clickLocation, mapMaker.getMapLocation());
        System.out.println("Trigger click: " + clickLocation);

        List<LocationTriggerMatcher> triggers = mapMaker.getTriggerData().getEntitiesAtLocation(location);
        triggerListPopup.removeAll();

        // Add each trigger at this location to the triggerListPopup
        for (LocationTriggerMatcher trigger : triggers) {
            triggerListPopup.add(trigger.getBasicName() + " (" + trigger.getTriggerModelType() + ")", event -> {
                selectedTrigger = trigger;

                // Add options for this trigger
                // Only single triggers can move, only multi triggers can add and partially move
                triggerOptionsPopup.removeAll();
                triggerOptionsPopup.addAll(editOption, removeOption);
                if (trigger instanceof SinglePointTriggerMatcher) {
                    triggerOptionsPopup.addAll(moveOption);
                } else {
                    triggerOptionsPopup.addAll(addOption, removeSomeOption);
                }

                // Show trigger options
                triggerOptionsPopup.show(clickLocation);
            });
        }

        // Show the list of triggers
        triggerListPopup.show(clickLocation);
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = TileUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        TileUtils.outlineTile(g, mouseHoverLocation, mapMaker.getMapLocation(), Color.BLUE);
    }

    @Override
    public String toString() {
        return "Trigger";
    }

    public class TriggerToolPopup extends JPopupMenu {
        public TriggerToolPopup() {
            this.setLightWeightPopupEnabled(false);
        }

        public void add(String label, ActionListener actionListener) {
            this.add(createMenuItem(label, actionListener));
        }

        public void addAll(JMenuItem... menuItems) {
            for (JMenuItem menuItem : menuItems) {
                this.add(menuItem);
            }
        }

        public void show(Point clickLocation) {
            this.show(mapMaker.canvas, clickLocation.x, clickLocation.y);
        }
    }

    private enum State {
        NONE,
        MOVE,
        ADD,
        REMOVE
    }
}
