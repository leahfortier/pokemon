package mapMaker.tools;

import mapMaker.MapMaker;
import mapMaker.data.PlaceableTrigger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class TriggerTool extends Tool {
    private JPopupMenu triggerListPopup;
    private JPopupMenu triggerOptionsPopup;

    private int mouseX;
    private int mouseY;

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
            int index = mapMaker.triggerData.getTriggerTypeIndex(selectedTrigger);
            if (index != -1) {
                mapMaker.editTypeComboBox.setSelectedIndex(4);

                mapMaker.triggerData.moveTrigger(selectedTrigger);
                mapMaker.triggerToolMoveSelected = true;

                mapMaker.tileList.setSelectedIndex(index);
            }
        });

        JMenuItem removeItem = new JMenuItem("Remove");
        triggerOptionsPopup.add(removeItem);
        removeItem.addActionListener(event -> mapMaker.triggerData.removeTrigger(selectedTrigger));
    }

    public String toString() {
        return "Trigger";
    }

    public void click(int x, int y) {
        if (mapMaker.currentMapName == null) {
            return;
        }

        mouseX = x;
        mouseY = y;

        x = (int) Math.floor((x - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        y = (int) Math.floor((y - mapMaker.mapY) * 1.0 / MapMaker.tileSize);
        System.out.println("Trigger click: " + x + " " + y);

        triggers = mapMaker.triggerData.getTrigger(x, y);
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
                triggerOptionsPopup.show(mapMaker.canvas, mouseX, mouseY);
            });
        }

        triggerListPopup.show(mapMaker.canvas, mouseX, mouseY);
    }

    public void draw(Graphics g) {
        int mhx = (int) Math.floor((mapMaker.mouseHoverX - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        int mhy = (int) Math.floor((mapMaker.mouseHoverY - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        g.setColor(Color.blue);
        g.drawRect(mhx * MapMaker.tileSize + mapMaker.mapX, mhy * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize, MapMaker.tileSize);
    }

    public void released(int x, int y) {
    }

    public void pressed(int x, int y) {
    }

    public void drag(int x, int y) {
    }

    public void reset() {
    }
}
