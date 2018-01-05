package gui.view;

import java.awt.Graphics;

public abstract class View {
    public abstract void update(int dt);
    public abstract void draw(Graphics g);
    public abstract ViewMode getViewModel();
    public abstract void movedToFront();
}
