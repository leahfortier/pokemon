package draw.panel;

import util.Point;

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

    default Point centerPoint() {
        return new Point(this.centerX(), this.centerY());
    }

    // Basically like a copy of this panel, but just the sizing attributes so other parts cannot be changed
    default Panel sizing() {
        Panel sizing = this;
        return new Panel() {
            @Override
            public int getX() {
                return sizing.getX();
            }

            @Override
            public int getY() {
                return sizing.getY();
            }

            @Override
            public int getWidth() {
                return sizing.getWidth();
            }

            @Override
            public int getHeight() {
                return sizing.getHeight();
            }
        };
    }
}
