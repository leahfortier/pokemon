package draw.panel;

public interface Panel {
    int getX();
    int getY();
    int getWidth();
    int getHeight();

    default int rightX() {
        return this.getX() + this.getWidth();
    }

    default int bottomY() {
        return this.getY() + this.getHeight();
    }

    default int centerX() {
        return this.getX() + this.getWidth()/2;
    }

    default int centerY() {
        return this.getY() + this.getHeight()/2;
    }
}
