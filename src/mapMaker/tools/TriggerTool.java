package mapMaker.tools;

import draw.TileUtils;
import mapMaker.EditType;
import mapMaker.MapMaker;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.LocationTriggerMatcher;
import util.Point;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;

class TriggerTool extends Tool {
    private JPopupMenu triggerListPopup;
    private JPopupMenu triggerOptionsPopup;

    private List<LocationTriggerMatcher> triggers;
    private LocationTriggerMatcher selectedTrigger;

    TriggerTool(MapMaker mapMaker) {
        super(mapMaker);

        selectedTrigger = null;

        triggerListPopup = new JPopupMenu();
        triggerListPopup.setLightWeightPopupEnabled(false);

        triggerOptionsPopup = new JPopupMenu();
        triggerOptionsPopup.setLightWeightPopupEnabled(false);

        JMenuItem editItem = new JMenuItem("Edit");
        triggerOptionsPopup.add(editItem);

        editItem.addActionListener(event -> mapMaker.getTriggerData().editTrigger(selectedTrigger));

        JMenuItem moveItem = new JMenuItem("Move");
        triggerOptionsPopup.add(moveItem);
        moveItem.addActionListener(event -> {
            mapMaker.setTool(ToolType.SINGLE_CLICK);
            TriggerModelType triggerModelType = selectedTrigger.getTriggerModelType();
            if (triggerModelType != null) {
                mapMaker.setEditType(EditType.TRIGGERS);

                mapMaker.getTriggerData().moveTrigger(selectedTrigger);
                mapMaker.triggerToolMoveSelected = true;

                mapMaker.setSelectedTileIndex(triggerModelType.ordinal());
            }
        });

        JMenuItem removeItem = new JMenuItem("Remove");
        triggerOptionsPopup.add(removeItem);
        removeItem.addActionListener(event -> mapMaker.getTriggerData().removeTrigger(selectedTrigger));
    }

    @Override
    public void click(Point clickLocation) {
        if (!mapMaker.hasMap()) {
            return;
        }

        Point location = TileUtils.getLocation(clickLocation, mapMaker.getMapLocation());

        System.out.println("Trigger click: " + clickLocation);

        triggers = mapMaker.getTriggerData().getEntitiesAtLocation(location);
        triggerListPopup.removeAll();

        for (LocationTriggerMatcher trigger : triggers) {
            JMenuItem menuItem = new JMenuItem(trigger.getBasicName() + " (" + trigger.getTriggerModelType() + ")");
            triggerListPopup.add(menuItem);
            menuItem.addActionListener(event -> {
                Component[] components = triggerListPopup.getComponents();
                // TODO: If someone reads this, please suggest a better way to find the index of the selected item...
                for (Component component : components) {
                    if (((JMenuItem) component).getText().equals(event.getActionCommand())) {
                        for (LocationTriggerMatcher trigger1 : triggers) {
                            if (event.getActionCommand().equals(trigger1.getBasicName() + " (" + trigger1.getTriggerModelType() + ")")) {
                                //System.out.println("Clicked " + e.getActionCommand());
                                selectedTrigger = trigger1;
                                break;
                            }
                        }
                    }
                }

                //triggerListPopup.removeAll();
                triggerOptionsPopup.show(mapMaker.canvas, clickLocation.x, clickLocation.y);
            });
        }

        triggerListPopup.show(mapMaker.canvas, clickLocation.x, clickLocation.y);
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = TileUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        TileUtils.outlineTile(g, mouseHoverLocation, mapMaker.getMapLocation(), Color.BLUE);
    }

    public String toString() {
        return "Trigger";
    }
}
