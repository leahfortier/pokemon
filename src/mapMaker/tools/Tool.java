package mapMaker.tools;

import mapMaker.MapMaker;
import util.Point;

import javax.swing.DefaultListModel;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public abstract class Tool {
    protected final MapMaker mapMaker;
    private final ToolType toolType;

    Tool(MapMaker mapMaker, ToolType toolType) {
        this.mapMaker = mapMaker;
        this.toolType = toolType;
    }

    // Can be overridden as necessary by subclasses
    public void click(Point clickLocation) {}
    public void released(Point releasedLocation) {}
    public void pressed(Point pressedLocation) {}
    public void drag(Point dragLocation) {}
    public void draw(Graphics g) {}
    public void reset() {}
    public void undo() {}

    public ToolType getToolType() {
        return this.toolType;
    }

    public static DefaultListModel<Tool> getToolListModel(MapMaker mapMaker) {
        DefaultListModel<Tool> toolListModel = new DefaultListModel<>();
        for (ToolType toolType : ToolType.values()) {
            toolListModel.addElement(toolType.createTool(mapMaker));
        }

        return toolListModel;
    }

    public enum ToolType {
        MOVE(KeyEvent.VK_1, MoveTool::new),
        SINGLE_CLICK(KeyEvent.VK_2, SingleClickTool::new),
        RECTANGLE(KeyEvent.VK_3, RectangleTool::new),
        TRIGGER(KeyEvent.VK_4, TriggerTool::new),
        SELECT(KeyEvent.VK_5, SelectTool::new);

        private final int keyEvent;
        private final ToolCreator toolCreator;

        ToolType(int keyEvent, ToolCreator toolCreator) {
            this.keyEvent = keyEvent;
            this.toolCreator = toolCreator;
        }

        public int getKeyEvent() {
            return this.keyEvent;
        }

        public Tool createTool(MapMaker mapMaker) {
            return this.toolCreator.createTool(mapMaker);
        }

        @FunctionalInterface
        private interface ToolCreator {
            Tool createTool(MapMaker mapMaker);
        }
    }
}
