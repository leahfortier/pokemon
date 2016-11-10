package mapMaker.tools;

import util.Point;
import mapMaker.MapMaker;
import mapMaker.TriggerModelType;
import mapMaker.data.PlaceableTrigger;
import util.DrawMetrics;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class TriggerTool extends Tool {
    private JPopupMenu triggerListPopup;
    private JPopupMenu triggerOptionsPopup;

    private Point mouseLocation;

    private PlaceableTrigger[] triggers;
    private PlaceableTrigger selectedTrigger;

    public TriggerTool(MapMaker mapMaker) {
        super(mapMaker);

        selectedTrigger = null;
        triggerListPopup = new JPopupMenu();
        triggerOptionsPopup = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit");
        triggerOptionsPopup.add(editItem);

        editItem.addActionListener(event -> mapMaker.triggerData.editTrigger(selectedTrigger));

        JMenuItem moveItem = new JMenuItem("Move");
        triggerOptionsPopup.add(moveItem);
        moveItem.addActionListener(event -> {
            mapMaker.toolList.setSelectedIndex(1);
            TriggerModelType triggerModelType = mapMaker.triggerData.getTriggerModelType(selectedTrigger);
            if (triggerModelType != null) {
                mapMaker.editTypeComboBox.setSelectedIndex(4);

                mapMaker.triggerData.moveTrigger(selectedTrigger);
                mapMaker.triggerToolMoveSelected = true;

                mapMaker.tileList.setSelectedIndex(triggerModelType.ordinal());
            }
        });

        JMenuItem removeItem = new JMenuItem("Remove");
        triggerOptionsPopup.add(removeItem);
        removeItem.addActionListener(event -> mapMaker.triggerData.removeTrigger(selectedTrigger));
    }

    @Override
    public void click(Point clickLocation) {
        if (mapMaker.currentMapName == null) {
            return;
        }

        this.mouseLocation = Point.copy(clickLocation);
        Point location = DrawMetrics.getLocation(this.mouseLocation, mapMaker.getMapLocation());

        System.out.println("Trigger click: " + this.mouseLocation);

        triggers = mapMaker.triggerData.getTrigger(location);
        triggerListPopup.removeAll();

        for (PlaceableTrigger trigger : triggers) {
            JMenuItem menuItem = new JMenuItem(trigger.name + " (" + trigger.triggerType + ")");
            triggerListPopup.add(menuItem);
            menuItem.addActionListener(event -> {
                Component[] components = triggerListPopup.getComponents();
                // TODO: If someone reads this, please suggest a better way to find the index of the selected item...
                for (Component component : components) {
                    if (((JMenuItem) component).getText().equals(event.getActionCommand())) {
                        for (PlaceableTrigger trigger1 : triggers) {
                            if (event.getActionCommand().equals(trigger1.name + " (" + trigger1.triggerType + ")")) {
                                //System.out.println("Clicked " + e.getActionCommand());
                                selectedTrigger = trigger1;
                                break;
                            }
                        }
                    }
                }

                //triggerListPopup.removeAll();
                triggerOptionsPopup.show(mapMaker.canvas, mouseLocation.x, mouseLocation.y);
            });
        }

        triggerListPopup.show(mapMaker.canvas, mouseLocation.x, mouseLocation.y);
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = DrawMetrics.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        DrawMetrics.outlineTile(g, mouseHoverLocation, mapMaker.getMapLocation(), Color.BLUE);
    }

    public String toString() {
        return "Trigger";
    }
}
