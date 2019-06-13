package test;

import battle.attack.MoveCategory;
import battle.effect.battle.weather.WeatherEffect;
import battle.effect.battle.weather.WeatherNamesies;
import draw.ImageUtils;
import generator.update.UpdateGen;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import main.Global;
import map.overworld.TerrainType;
import org.junit.Assert;
import org.junit.Test;
import pokemon.breeding.Eggy;
import pokemon.species.PokemonInfo;
import trainer.player.medal.Medal;
import type.Type;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

public class ImageTest extends BaseTest {

    @Test
    public void metadataTest() throws IOException {
        for (File imageFile : FileIO.listFiles(Folder.IMAGES)) {
            if (imageFile.isDirectory() || imageFile.isHidden() || imageFile.getName().endsWith(".txt")) {
                continue;
            }

            Assert.assertTrue(imageFile.getPath(), imageFile.getName().endsWith(".png"));

            BufferedImage rawImage = ImageIO.read(imageFile);
            Assert.assertEquals(imageFile.getPath(), 4, rawImage.getSampleModel().getNumBands());
            Assert.assertEquals(imageFile.getPath(), DataBuffer.TYPE_BYTE, rawImage.getSampleModel().getDataType());
        }
    }

    @Test
    public void missingTest() {
        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            checkExists(num, "", Folder.POKEDEX_TILES);
            checkExists(num, "-small", Folder.PARTY_TILES);
            checkExists(num, "", Folder.POKEMON_TILES);
            checkExists(num, "-back", Folder.POKEMON_TILES);
            checkExists(num, "-shiny", Folder.POKEMON_TILES);
            checkExists(num, "-shiny-back", Folder.POKEMON_TILES);
        }

        checkExists(Folder.PARTY_TILES, Eggy.TINY_EGG_IMAGE_NAME);
        checkExists(Folder.POKEDEX_TILES, Eggy.BASE_EGG_IMAGE_NAME);
        checkExists(Folder.POKEMON_TILES, Eggy.SPRITE_EGG_IMAGE_NAME);
        checkExists(Folder.POKEMON_TILES, "substitute");
        checkExists(Folder.POKEMON_TILES, "substitute-back");

        for (MoveCategory category : MoveCategory.values()) {
            checkExists(Folder.ATTACK_TILES, "MoveCategory" + StringUtils.properCase(category.name().toLowerCase()));
        }

        for (BagCategory category : BagCategory.values()) {
            checkExists(Folder.BAG_TILES, "cat_" + category.getDisplayName().replaceAll("\\s", "").toLowerCase());
        }

        for (ItemNamesies itemName : ItemNamesies.values()) {
            if (itemName == ItemNamesies.NO_ITEM) {
                continue;
            }

            Item item = itemName.getItem();
            try {
                checkExists(Folder.ITEM_TILES, item.getImageName());
            } catch (AssertionError error) {
                System.err.println(error.getMessage());
            }
        }

        for (Medal medal : Medal.values()) {
            checkExists(Folder.MEDAL_TILES, medal.getImageName());
            Assert.assertNotEquals(medal.getImageName(), Medal.getUnknownMedalImageName());
        }

        for (TerrainType terrainType : TerrainType.values()) {
            checkExists(Folder.TERRAIN_TILES, StringUtils.properCase(terrainType.name().toLowerCase()) + "Circle");
        }

        for (Type type : Type.values()) {
            checkExists(Folder.TYPE_TILES, "Type" + type.getName());
        }

        for (WeatherNamesies effectNamesies : WeatherNamesies.values()) {
            WeatherEffect weather = effectNamesies.getEffect();
            checkExists(Folder.WEATHER_TILES, weather.getImageName());
        }
    }

    private void checkExists(int num, String suffix, String folderPath) {
        checkExists(FileIO.getImageFile(num, suffix, folderPath));
    }

    private void checkExists(String folderPath, String imageName) {
        if (!imageName.endsWith(".png")) {
            imageName += ".png";
        }

        checkExists(FileIO.newFile(folderPath + imageName));
    }

    private void checkExists(File imageFile) {
        Assert.assertTrue(imageFile.getPath() + " does not exist.", imageFile.exists());
    }

    @Test
    public void sizeTest() {
        // Party and item tiles are tile size
        DimensionChecker tileDimension = new DimensionChecker(Global.TILE_SIZE, Global.TILE_SIZE);

        for (ItemNamesies itemName : ItemNamesies.values()) {
            if (itemName == ItemNamesies.NO_ITEM) {
                continue;
            }
            checkMaxSize(Folder.ITEM_TILES, itemName.getItem().getImageName(), tileDimension);
        }

        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            checkMaxSize(num, "", Folder.POKEDEX_TILES, new DimensionChecker(140, 190).singleDimensionEquals(2));
            checkMaxSize(num, "-small", Folder.PARTY_TILES, tileDimension.singleDimensionEquals(0));

            String[] spriteSuffixes = { "", "-back", "-shiny", "-shiny-back" };
            for (String suffix : spriteSuffixes) {
                checkMaxSize(num, suffix, Folder.POKEMON_TILES, new DimensionChecker(96, 96));
            }
        }
    }

    private void checkMaxSize(int num, String suffix, String folderPath, DimensionChecker dimensionChecker) {
        checkMaxSize(FileIO.getImageFile(num, suffix, folderPath), dimensionChecker);
    }

    private void checkMaxSize(String folderPath, String imageName, DimensionChecker dimensionChecker) {
        if (!imageName.endsWith(".png")) {
            imageName += ".png";
        }

        checkMaxSize(FileIO.newFile(folderPath + imageName), dimensionChecker);
    }

    private void checkMaxSize(File imageFile, DimensionChecker dimensionChecker) {
        dimensionChecker.assertMatch(imageFile);
    }

    @Test
    public void spriteTest() {
        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            File frontImageFile = FileIO.getImageFile(num, "", Folder.POKEMON_TILES);
            File backImageFile = FileIO.getImageFile(num, "-back", Folder.POKEMON_TILES);
            File shinyFrontImageFile = FileIO.getImageFile(num, "-shiny", Folder.POKEMON_TILES);
            File shinyBackImageFile = FileIO.getImageFile(num, "-shiny-back", Folder.POKEMON_TILES);

            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(num);
            String message = num + " " + pokemonInfo.getName();

            // Front and back images should always exist together
            Assert.assertEquals(message, frontImageFile.exists(), backImageFile.exists());
            Assert.assertEquals(message, shinyFrontImageFile.exists(), shinyBackImageFile.exists());

            // Same direction images should have the same silhouette
            assertSameSilhouette(message + " Front", frontImageFile, shinyFrontImageFile);
            assertSameSilhouette(message + " Back", backImageFile, shinyBackImageFile);

            // Front and back images should not be the same
            assertDifferent(message, frontImageFile, backImageFile);
            assertDifferent(message + " Shiny", shinyFrontImageFile, shinyBackImageFile);
        }
    }

    private void assertSameSilhouette(String message, File basicImageFile, File shinyImageFile) {
        if (!basicImageFile.exists() || !shinyImageFile.exists()) {
            return;
        }

        BufferedImage basicImage = FileIO.readImage(basicImageFile);
        BufferedImage shinyImage = FileIO.readImage(shinyImageFile);

        Assert.assertEquals(message + " Width", basicImage.getWidth(), shinyImage.getWidth());
        Assert.assertEquals(message + " Height", basicImage.getHeight(), shinyImage.getHeight());

        int firstNumOpaque = ImageUtils.numOpaquePixels(basicImage);
        int secondNumOpaque = ImageUtils.numOpaquePixels(shinyImage);
        TestUtils.assertGreater(message + " Num Opaque", firstNumOpaque, 0);
        TestUtils.assertGreater(message + " Num Opaque", secondNumOpaque, 0);
        TestUtils.assertAlmostEquals(message + " Num Opaque", firstNumOpaque, secondNumOpaque, 5);
        TestUtils.assertAlmostEquals(
                message + " Pixels (" + firstNumOpaque + "/" + basicImage.getWidth()*basicImage.getHeight() + ")", 0,
                ImageUtils.pixelsDiff(ImageUtils.silhouette(basicImage), ImageUtils.silhouette(shinyImage)), 5
        );

        try {
            Assert.assertTrue(message + " Same as Shiny", ImageUtils.pixelsDiff(basicImage, shinyImage) > 0);
        } catch (AssertionError error) {
            System.err.println(error.getMessage());
        }
    }

    private void assertDifferent(String message, File frontImageFile, File backImageFile) {
        if (!frontImageFile.exists() || !backImageFile.exists()) {
            return;
        }

        BufferedImage frontImage = FileIO.readImage(frontImageFile);
        BufferedImage backImage = FileIO.readImage(backImageFile);

        try {
            Assert.assertTrue(message + " Same Front and Back", ImageUtils.pixelsDiff(frontImage, backImage) != 0);
            Assert.assertTrue(message + " Inverted Front and Back", ImageUtils.pixelsDiff(backImage, ImageUtils.invertImage(frontImage)) != 0);
        } catch (AssertionError error) {
            System.err.println(error.getMessage());
        }
    }

    private static class DimensionChecker {
        private final int maxWidth;
        private final int maxHeight;

        private int minWidth;
        private int minHeight;

        private boolean mustEqual;
        private boolean singleDimensionEquals;
        private int delta;

        DimensionChecker(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        DimensionChecker withMin(int minWidth, int minHeight) {
            this.minWidth = minWidth;
            this.minHeight = minHeight;
            return this;
        }

        DimensionChecker mustEquals(int delta) {
            this.mustEqual = true;
            this.delta = delta;
            return this;
        }

        DimensionChecker singleDimensionEquals(int delta) {
            this.singleDimensionEquals = true;
            this.delta = delta;
            return this;
        }

        void assertMatch(File imageFile) {
            if (imageFile.exists()) {
                BufferedImage image = FileIO.readImage(imageFile);

                String message = getFailMessage(imageFile, image, ">");
                Assert.assertTrue(message, image.getWidth() <= maxWidth);
                Assert.assertTrue(message, image.getHeight() <= maxHeight);

                message = getFailMessage(imageFile, image, "<");
                Assert.assertTrue(message, image.getWidth() >= minWidth);
                Assert.assertTrue(message, image.getHeight() >= minHeight);

                if (mustEqual) {
                    message = getFailMessage(imageFile, image, "!=");
                    Assert.assertTrue(message, image.getWidth() >= maxWidth - delta);
                    Assert.assertTrue(message, image.getHeight() >= maxHeight - delta);
                }

                if (singleDimensionEquals) {
                    message = getFailMessage(imageFile, image, "!=");
                    Assert.assertTrue(message, image.getWidth() >= maxWidth - delta || image.getHeight() >= maxHeight - delta);
                }
            }
        }

        private String getFailMessage(File imageFile, BufferedImage image, String operator) {
            return imageFile.getPath() + ": "
                    + UpdateGen.getCoordinatesString(image)
                    + " " + operator + " "
                    + UpdateGen.getCoordinatesString(maxWidth, maxHeight)
                    + (delta == 0 ? "" : ", Delta: " + delta);
        }
    }
}
