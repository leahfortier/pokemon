package test;

import battle.attack.MoveCategory;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.Weather;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import map.overworld.TerrainType;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.player.medal.Medal;
import type.Type;
import util.FileIO;
import util.Folder;
import util.StringUtils;

import java.io.File;

public class ImageTest {
    @Test
    public void missingTest() {
        for (int num = 1; num <= PokemonInfo.NUM_POKEMON; num++) {
            checkExists(num, "", Folder.POKEDEX_TILES, true);
            checkExists(num, "-small", Folder.PARTY_TILES, true);
            checkExists(num, "", Folder.POKEMON_TILES, false);
            checkExists(num, "-back", Folder.POKEMON_TILES, false);
            checkExists(num, "-shiny", Folder.POKEMON_TILES, false);
            checkExists(num, "-shiny-back", Folder.POKEMON_TILES, false);
        }

        checkExists(Folder.PARTY_TILES, ActivePokemon.TINY_EGG_IMAGE_NAME, true);
        checkExists(Folder.POKEDEX_TILES, ActivePokemon.BASE_EGG_IMAGE_NAME, true);
        checkExists(Folder.POKEMON_TILES, ActivePokemon.SPRITE_EGG_IMAGE_NAME, true);
        checkExists(Folder.POKEMON_TILES, "substitute", true);
        checkExists(Folder.POKEMON_TILES, "substitute-back", true);

        for (MoveCategory category : MoveCategory.values()) {
            checkExists(Folder.ATTACK_TILES, "MoveCategory" + StringUtils.properCase(category.name().toLowerCase()), true);
        }

        for (BagCategory category : BagCategory.values()) {
            checkExists(Folder.BAG_TILES, "cat_" + category.getDisplayName().replaceAll("\\s", "").toLowerCase(), true);
        }

        for (ItemNamesies itemName : ItemNamesies.values()) {
            if (itemName == ItemNamesies.NO_ITEM) {
                continue;
            }

            Item item = itemName.getItem();
            checkExists(Folder.ITEM_TILES, item.getImageName(), false);
        }

        for (Medal medal : Medal.values()) {
            checkExists(Folder.MEDAL_TILES, medal.getImageName(), true);
            Assert.assertNotEquals(medal.getImageName(), Medal.getUnknownMedalImageName());
        }

        for (TerrainType terrainType : TerrainType.values()) {
            checkExists(Folder.TERRAIN_TILES, StringUtils.properCase(terrainType.name().toLowerCase()) + "Circle", true);
        }

        for (Type type : Type.values()) {
            checkExists(Folder.TYPE_TILES, "Type" + type.getName(), true);
        }

        for (EffectNamesies effectNamesies : EffectNamesies.values()) {
            Effect effect = effectNamesies.getEffect();
            if (effect instanceof Weather) {
                Weather weather = (Weather)effect;
                checkExists(Folder.WEATHER_TILES, weather.getImageName(), true);
            }
        }
    }

    private void checkExists(int num, String suffix, String folderPath, boolean required) {
        checkExists(FileIO.getImageFile(num, suffix, folderPath), required);
    }

    private void checkExists(String folderPath, String imageName, boolean required) {
        if (!imageName.endsWith(".png")) {
            imageName += ".png";
        }

        checkExists(new File(folderPath + imageName), required);
    }

    private void checkExists(File imageFile, boolean required) {
        if (!imageFile.exists()) {
            String message = imageFile.getPath() + " does not exist.";
            if (required) {
                Assert.fail(message);
            } else {
                System.err.println(message);
            }
        }
    }
}
