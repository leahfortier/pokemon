package test;

import battle.attack.MoveCategory;
import battle.effect.battle.weather.WeatherEffect;
import battle.effect.battle.weather.WeatherNamesies;
import draw.ImageUtils;
import generator.update.UpdateGen;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import map.overworld.TerrainType;
import org.junit.Assert;
import org.junit.Test;
import pokemon.breeding.Eggy;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import test.general.BaseTest;
import test.general.TestUtils;
import trainer.player.medal.Medal;
import type.Type;
import util.file.FileIO;
import util.file.Folder;

import javax.imageio.ImageIO;
import java.awt.Dimension;
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

            String imageName = imageFile.getPath();
            Assert.assertTrue(imageName, imageFile.getName().endsWith(".png"));

            BufferedImage rawImage = ImageIO.read(imageFile);
            Assert.assertEquals(imageName, 4, rawImage.getSampleModel().getNumBands());
            Assert.assertEquals(imageName, DataBuffer.TYPE_BYTE, rawImage.getSampleModel().getDataType());
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
            checkExists(Folder.ATTACK_TILES, category.getImageName());
        }

        for (BagCategory category : BagCategory.values()) {
            checkExists(Folder.BAG_TILES, category.getImageName());
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
            checkExists(Folder.TERRAIN_TILES, terrainType.getImageName());
        }

        for (Type type : Type.values()) {
            checkExists(Folder.TYPE_TILES, type.getImageName());
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
        // Type and category images must be the same size
        Assert.assertEquals(Type.IMAGE_SIZE, MoveCategory.IMAGE_SIZE);

        DimensionChecker typeDimension = new DimensionChecker(Type.IMAGE_SIZE).mustEquals(0);
        for (Type type : Type.values()) {
            checkMaxSize(Folder.TYPE_TILES, type.getImageName(), typeDimension);
        }

        DimensionChecker moveCategoryDimension = new DimensionChecker(MoveCategory.IMAGE_SIZE).mustEquals(0);
        for (MoveCategory category : MoveCategory.values()) {
            checkMaxSize(Folder.ATTACK_TILES, category.getImageName(), moveCategoryDimension);
        }

        DimensionChecker bagCategoryDimension = new DimensionChecker(BagCategory.IMAGE_SIZE).mustEquals(0);
        for (BagCategory category : BagCategory.values()) {
            checkMaxSize(Folder.BAG_TILES, category.getImageName(), bagCategoryDimension);
        }

        DimensionChecker itemDimension = new DimensionChecker(Item.MAX_IMAGE_SIZE).trimmed();
        for (ItemNamesies itemName : ItemNamesies.values()) {
            if (itemName == ItemNamesies.NO_ITEM) {
                continue;
            }

            checkMaxSize(Folder.ITEM_TILES, itemName.getItem().getImageName(), itemDimension);
        }

        DimensionChecker partyDimension = new DimensionChecker(PokemonInfo.MAX_PARTY_IMAGE_SIZE).singleDimensionEquals(0);
        DimensionChecker pokedexDimension = new DimensionChecker(140, 190).singleDimensionEquals(2);
        DimensionChecker pokemonDimension = new DimensionChecker(96, 96).trimmed();
        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            checkMaxSize(num, "", Folder.POKEDEX_TILES, pokedexDimension);
            checkMaxSize(num, "-small", Folder.PARTY_TILES, partyDimension);

            String[] spriteSuffixes = { "", "-back", "-shiny", "-shiny-back" };
            for (String suffix : spriteSuffixes) {
                checkMaxSize(num, suffix, Folder.POKEMON_TILES, pokemonDimension);
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
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            int num = pokemonInfo.getNumber();
            String message = num + " " + pokemonInfo.getName();

            File frontImageFile = FileIO.getImageFile(num, "", Folder.POKEMON_TILES);
            File backImageFile = FileIO.getImageFile(num, "-back", Folder.POKEMON_TILES);
            File shinyFrontImageFile = FileIO.getImageFile(num, "-shiny", Folder.POKEMON_TILES);
            File shinyBackImageFile = FileIO.getImageFile(num, "-shiny-back", Folder.POKEMON_TILES);

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

        TestUtils.assertWarning(message + " Same as Shiny", ImageUtils.pixelsDiff(basicImage, shinyImage) > 0);
    }

    private void assertDifferent(String message, File frontImageFile, File backImageFile) {
        if (!frontImageFile.exists() || !backImageFile.exists()) {
            return;
        }

        BufferedImage frontImage = FileIO.readImage(frontImageFile);
        BufferedImage backImage = FileIO.readImage(backImageFile);

        TestUtils.assertWarning(message + " Same Front and Back", ImageUtils.pixelsDiff(frontImage, backImage) != 0);
        TestUtils.assertWarning(message + " Inverted Front and Back", ImageUtils.pixelsDiff(backImage, ImageUtils.invertImage(frontImage)) != 0);
    }

    private static class DimensionChecker {
        private final int maxWidth;
        private final int maxHeight;

        private int minWidth;
        private int minHeight;

        private boolean mustEqual;
        private boolean singleDimensionEquals;
        private boolean trimmed;
        private int delta;

        DimensionChecker(Dimension dimension) {
            this(dimension.width, dimension.height);
        }

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

        DimensionChecker trimmed() {
            this.trimmed = true;
            return this;
        }

        void assertMatch(File imageFile) {
            if (imageFile.exists()) {
                BufferedImage image = FileIO.readImage(imageFile);
                int width = image.getWidth();
                int height = image.getHeight();

                String message = getFailMessage(imageFile, image, ">");
                Assert.assertTrue(message, width <= maxWidth);
                Assert.assertTrue(message, height <= maxHeight);

                message = getFailMessage(imageFile, image, "<");
                Assert.assertTrue(message, width >= minWidth);
                Assert.assertTrue(message, height >= minHeight);

                if (mustEqual) {
                    message = getFailMessage(imageFile, image, "!=");
                    Assert.assertTrue(message, width >= maxWidth - delta);
                    Assert.assertTrue(message, height >= maxHeight - delta);
                }

                if (singleDimensionEquals) {
                    message = getFailMessage(imageFile, image, "!=");
                    Assert.assertTrue(message, width >= maxWidth - delta || height >= maxHeight - delta);
                }

                if (trimmed) {
                    BufferedImage trimmed = ImageUtils.trimImage(image);
                    message = imageFile.getPath() + ": "
                            + UpdateGen.getCoordinatesString(image)
                            + " not trimmed "
                            + UpdateGen.getCoordinatesString(trimmed);

                    TestUtils.assertEqualProperty(message, image, trimmed, BufferedImage::getWidth);
                    TestUtils.assertEqualProperty(message, image, trimmed, BufferedImage::getHeight);
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
