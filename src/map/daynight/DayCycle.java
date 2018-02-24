package map.daynight;

import draw.DrawUtils;
import util.ReverseIterable;
import util.TimeUtils;

import java.awt.Color;
import java.awt.Graphics;

public enum DayCycle {
    DAWN(5, new Color(248, 150, 63, 128)),
    DAY(8, null),
    DUSK(17, new Color(199, 159, 255, 128)),
    NIGHT(20, new Color(18, 20, 100, 128));

    private final int startHour;
    private final Color filter;

    DayCycle(int startHour, Color filter) {
        this.startHour = startHour;
        this.filter = filter;
    }

    public void draw(Graphics g) {
        if (this.filter != null) {
            DrawUtils.fillCanvas(g, filter);
        }
    }

    public static DayCycle getTimeOfDay() {
        int hour = TimeUtils.getHourOfDay();
        DayCycle[] values = DayCycle.values();

        for (DayCycle dayCycle : new ReverseIterable<>(values)) {
            if (hour >= dayCycle.startHour) {
                return dayCycle;
            }
        }

        return values[values.length - 1];
    }
}
