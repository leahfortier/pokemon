package test.maps;

import mapMaker.tools.Tool;
import mapMaker.tools.Tool.ToolType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class MapMakerTest {
    @Test
    public void toolTest() {
        Set<Integer> keyEvents = new HashSet<>();
        for (ToolType toolType : ToolType.values()) {
            // Make sure all key events are unique
            int keyEvent = toolType.getKeyEvent();
            Assert.assertFalse(keyEvents.contains(keyEvent));
            keyEvents.add(keyEvent);

            // Make sure the tool created from the tool type has the same corresponding tool type
            Tool tool = toolType.createTool(null);
            Assert.assertEquals(toolType, tool.getToolType());
        }
    }
}
