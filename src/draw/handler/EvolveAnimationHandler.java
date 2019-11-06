package draw.handler;

import draw.ImageUtils;
import draw.panel.Panel;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class EvolveAnimationHandler {
    private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;

    private final Point pokemonDrawLocation;

    private int animationEvolve;
    private BufferedImage currEvolution;
    private BufferedImage nextEvolution;

    public EvolveAnimationHandler(Panel sizing) {
        this.pokemonDrawLocation = sizing.centerPoint();
    }

    public boolean isFinished() {
        return this.animationEvolve <= 0;
    }

    public void set(BufferedImage currEvolution, BufferedImage nextEvolution) {
        this.currEvolution = currEvolution;
        this.nextEvolution = nextEvolution;

        animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
    }

    public void drawBase(Graphics g) {
        ImageUtils.drawBottomCenteredImage(g, currEvolution, pokemonDrawLocation);
    }

    public void drawEvolve(Graphics g) {
        animationEvolve = ImageUtils.transformAnimation(
                g,
                animationEvolve,
                EVOLVE_ANIMATION_LIFESPAN,
                nextEvolution,
                currEvolution,
                pokemonDrawLocation
        );
    }

    public void drawEnd(Graphics g) {
        ImageUtils.drawBottomCenteredImage(g, nextEvolution, pokemonDrawLocation);
    }
}
