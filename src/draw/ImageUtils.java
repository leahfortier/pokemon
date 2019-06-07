package draw;

import main.Global;
import type.PokeType;
import util.FontMetrics;
import util.Point;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public final class ImageUtils {
    private static final float[] SILHOUETTE_SCALE = new float[] { 0, 0, 0, 255 };
    private static final float[] SILHOUETTE_OFFSET = new float[] { 0, 0, 0, 0 };

    public static void drawTypeTiles(Graphics g, PokeType type, int rightX, int textY) {
        BufferedImage firstType = type.getFirstType().getImage();

        int drawX = rightX - firstType.getWidth();
        int drawY = textY - firstType.getHeight();

        if (type.isDualTyped()) {
            BufferedImage secondType = type.getSecondType().getImage();
            int leftDrawX = drawX - firstType.getWidth() - 8;

            g.drawImage(firstType, leftDrawX, drawY, null);
            g.drawImage(secondType, drawX, drawY, null);
        } else {
            g.drawImage(firstType, drawX, drawY, null);
        }
    }

    public static void drawBottomCenteredImage(Graphics g, BufferedImage image, Point center) {
        drawBottomCenteredImage(g, image, center.x, center.y);
    }

    public static void drawBottomCenteredImage(Graphics g, BufferedImage image, int x, int y) {
        g.drawImage(
                image,
                x - image.getWidth()/2,
                y - image.getHeight(),
                null
        );
    }

    public static void drawCenteredHeightImage(Graphics g, BufferedImage image, int x, int centerY) {
        g.drawImage(image, x, centerY - image.getHeight()/2, null);
    }

    public static void drawCenteredHeightImageLabel(Graphics g, BufferedImage image, String text, int x, int centerY) {
        int imageWidth = image.getWidth();
        int spacing = FontMetrics.getTextWidth(g, "  ");

        drawCenteredHeightImage(g, image, x, centerY);
        TextUtils.drawCenteredHeightString(g, text, x + imageWidth + spacing, centerY);
    }

    public static void drawCenteredImageLabel(Graphics g, BufferedImage image, String text, int x, int y) {
        text = " " + text;

        int imageWidth = image.getWidth();
        int textWidth = FontMetrics.getTextWidth(g, text);
        int halfSize = (imageWidth + textWidth)/2;

        drawCenteredHeightImage(g, image, x - halfSize, y);
        TextUtils.drawCenteredHeightString(g, text, x - halfSize + imageWidth, y);
    }

    public static void drawCenteredImageLabel(Graphics g, BufferedImage image, String text, Point center) {
        drawCenteredImageLabel(g, image, text, center.x, center.y);
    }

    public static void drawCenteredImage(Graphics g, BufferedImage image, Point center) {
        drawCenteredImage(g, image, center.x, center.y);
    }

    public static void drawCenteredImage(Graphics g, BufferedImage image, int x, int y) {
        g.drawImage(
                image,
                x - image.getWidth()/2,
                y - image.getHeight()/2,
                null
        );
    }

    public static BufferedImage scaleImageByHeight(BufferedImage img, int maxHeight) {
        return scaleImage(img, (float)maxHeight/img.getHeight());
    }

    public static BufferedImage scaleImageByWidth(BufferedImage img, int maxWidth) {
        return scaleImage(img, (float)maxWidth/img.getWidth());
    }

    // DO NOT RETURN IMG WHEN SCALE IS ONE BECAUSE IT DOES OTHER THINGS TOO MAINLY CHANGING THE TYPE
    public static BufferedImage scaleImage(BufferedImage img, float scale) {
        int scaledWidth = (int)(img.getWidth()*scale);
        int scaledHeight = (int)(img.getHeight()*scale);

        Image tmp = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        BufferedImage buffer = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

        buffer.getGraphics().drawImage(tmp, 0, 0, null);

        return buffer;
    }

    // ALL IMAGES SHOULD BE READ USING THIS
    public static BufferedImage read(File imageFile, float scale) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return ImageUtils.scaleImage(image, scale);
    }

    public static BufferedImage silhouette(BufferedImage image) {
        return colorImage(image, SILHOUETTE_SCALE, SILHOUETTE_OFFSET);
    }

    public static BufferedImage colorImage(BufferedImage image, float[] scale, float[] offset) {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);

        image = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);

        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int[] pixels = raster.getPixel(x, y, (int[])null);

                for (int currComponent = 0; currComponent < pixels.length; ++currComponent) {
                    pixels[currComponent] = Math.round(pixels[currComponent]*scale[currComponent] + offset[currComponent]);
                    pixels[currComponent] = Math.min(Math.max(pixels[currComponent], 0), 255);
                }

                if (pixels[3] == 0) {
                    pixels[0] = pixels[1] = pixels[2] = 0;
                }

                raster.setPixel(x, y, pixels);
            }
        }
        return image;
    }

    // Returns a new image where it is just flipped horizontally
    public static BufferedImage invertImage(BufferedImage image) {
        BufferedImage inverted = new BufferedImage(
                image.getColorModel(),
                image.copyData(null),
                image.isAlphaPremultiplied(),
                null
        );

        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                inverted.setRGB(width - x - 1, y, image.getRGB(x, y));
            }
        }

        return inverted;
    }

    public static int transformAnimation(
            Graphics g,
            int animationValue,
            int animationLifespan,
            BufferedImage first,
            BufferedImage second,
            Point drawLocation) {

        float[] firstScales = { 1f, 1f, 1f, 1f };
        float[] firstOffsets = { 255f, 255f, 255f, 0f };
        float[] secondScales = { 1f, 1f, 1f, 1f };
        float[] secondOffsets = { 255f, 255f, 255f, 0f };

        // Turn white
        if (animationValue > animationLifespan*0.7) {
            firstOffsets[0] = firstOffsets[1] = firstOffsets[2] = 255*(1 - (animationValue - animationLifespan*0.7f)/(animationLifespan*(1 - 0.7f)));
            secondScales[3] = 0;
        }
        // Change form
        else if (animationValue > animationLifespan*0.3) {
            firstOffsets[0] = firstOffsets[1] = firstOffsets[2] = 255;
            firstScales[3] = ((animationValue - animationLifespan*0.3f)/(animationLifespan*(0.7f - 0.3f)));
            secondOffsets[0] = secondOffsets[1] = secondOffsets[2] = 255;
            secondScales[3] = (1 - (animationValue - animationLifespan*0.3f)/(animationLifespan*(0.7f - 0.3f)));
        }
        // Restore color
        else {
            firstScales[3] = 0;
            secondOffsets[0] = secondOffsets[1] = secondOffsets[2] = 255*(animationValue)/(animationLifespan*(1 - 0.7f));
        }

        animationValue -= Global.MS_BETWEEN_FRAMES;

        drawBottomCenteredImage(g, colorImage(first, secondScales, secondOffsets), drawLocation);
        drawBottomCenteredImage(g, colorImage(second, firstScales, firstOffsets), drawLocation);

        return animationValue;
    }

    public static BufferedImage createNewImage(Dimension dimension) {
        return new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
    }

    public static BufferedImage trimImage(BufferedImage image) {
        int leftmost = image.getWidth();
        int topmost = image.getHeight();
        int rightmost = 0;
        int bottommost = 0;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (new Color(image.getRGB(i, j), true).getTransparency() == Transparency.OPAQUE) {
                    leftmost = Math.min(i, leftmost);
                    rightmost = Math.max(i, rightmost);
                    topmost = Math.min(j, topmost);
                    bottommost = Math.max(j, bottommost);
                }
            }
        }

        return image.getSubimage(leftmost, topmost, rightmost - leftmost + 1, bottommost - topmost + 1);
    }

    public static int numOpaquePixels(BufferedImage image) {
        int numOpaque = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (new Color(image.getRGB(i, j), true).getTransparency() == Transparency.OPAQUE) {
                    numOpaque++;
                }
            }
        }

        return numOpaque;
    }

    // Returns true if the number of non-matching opaque pixels for images of the same size is zero
    public static boolean equals(BufferedImage first, BufferedImage second) {
        return pixelsDiff(first, second) == 0;
    }

    // Returns the number of non-matching opaque pixels for images of the same size
    public static int pixelsDiff(BufferedImage first, BufferedImage second) {
        if (first.getWidth() != second.getWidth() || first.getHeight() != second.getHeight()) {
            return -1;
        }

        int numDiff = 0;
        for (int i = 0; i < first.getWidth(); i++) {
            for (int j = 0; j < first.getHeight(); j++) {
                Color firstColor = new Color(first.getRGB(i, j), true);
                Color secondColor = new Color(second.getRGB(i, j), true);
                boolean firstOpaque = firstColor.getTransparency() == Transparency.OPAQUE;
                boolean secondOpaque = secondColor.getTransparency() == Transparency.OPAQUE;

                // Difference when only one is opaque or when both are opaque but different colors
                if ((firstOpaque && !secondOpaque)
                        || (secondOpaque && !firstOpaque)
                        || (firstOpaque && !firstColor.equals(secondColor))) {
                    numDiff++;
                }
            }
        }

        return numDiff;
    }
}
