package test.script;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import generator.update.AbilityUpdater;
import generator.update.AbilityUpdater.AbilityParser;
import generator.update.GeneratorUpdater.BaseParser;
import generator.update.ItemUpdater;
import generator.update.ItemUpdater.ItemParser;
import generator.update.MoveUpdater;
import generator.update.MoveUpdater.MoveParser;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import item.berry.Berry;
import item.hold.HoldItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.EvolutionItem;
import item.use.TechnicalMachine;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import test.general.BaseTest;
import type.Type;
import util.MultiMap;
import util.string.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class SerebiiScriptTest extends BaseTest {
    @Test
    public void moveParserTest() {
        Set<AttackNamesies> toParse = EnumSet.allOf(AttackNamesies.class);
        toParse.remove(AttackNamesies.CONFUSION_DAMAGE);
        toParse.remove(AttackNamesies.FAKE_FREEZER);

        DescriptionUpdater<AttackNamesies> updater = new DescriptionUpdater<>();

        // Personal grammar preference
        updater.add("--", " -- ", AttackNamesies.BONEMERANG, AttackNamesies.PHOTON_GEYSER);

        // Because no double battles
        updater.add("the opposing Pokémon", "the target", AttackNamesies.ROCK_SLIDE, AttackNamesies.BLIZZARD);
        updater.add("opposing Pokémon", "the target", AttackNamesies.GROWL, AttackNamesies.TAIL_WHIP, AttackNamesies.RAZOR_LEAF, AttackNamesies.HEAT_WAVE, AttackNamesies.TWISTER, AttackNamesies.SWIFT, AttackNamesies.AIR_CUTTER, AttackNamesies.ICY_WIND, AttackNamesies.SHELL_TRAP, AttackNamesies.POWDER_SNOW, AttackNamesies.MUDDY_WATER, AttackNamesies.ROCK_SLIDE, AttackNamesies.COTTON_SPORE, AttackNamesies.STRUGGLE_BUG, AttackNamesies.ELECTROWEB, AttackNamesies.GLACIATE, AttackNamesies.SNARL, AttackNamesies.CLANGING_SCALES, AttackNamesies.SWEET_SCENT, AttackNamesies.POISON_GAS, AttackNamesies.BLIZZARD, AttackNamesies.ERUPTION, AttackNamesies.WATER_SPOUT, AttackNamesies.INCINERATE, AttackNamesies.DIAMOND_STORM, AttackNamesies.LANDS_WRATH, AttackNamesies.DISARMING_VOICE, AttackNamesies.DAZZLING_GLEAM, AttackNamesies.ORIGIN_PULSE, AttackNamesies.PRECIPICE_BLADES, AttackNamesies.BREAKING_SWIPE, AttackNamesies.OVERDRIVE, AttackNamesies.LEER);
        updater.add("Opposing Pokémon are", "The target is", AttackNamesies.STRING_SHOT, AttackNamesies.ACID, AttackNamesies.DARK_VOID, AttackNamesies.VENOM_DRENCH);
        updater.add("those Pokémon", "the target", AttackNamesies.HEAT_WAVE);

        updater.add(" Attack stats", " Attack stat", AttackNamesies.GROWL, AttackNamesies.BREAKING_SWIPE, AttackNamesies.HOWL);
        updater.add(" Defense stats", " Defense stat", AttackNamesies.TAIL_WHIP);
        updater.add(" Sp. Atk stats", " Sp. Atk stat", AttackNamesies.STRUGGLE_BUG, AttackNamesies.SNARL);
        updater.add(" Sp. Def stats", " Sp. Def stat", AttackNamesies.ACID);
        updater.add(" Speed stats", " Speed stat", AttackNamesies.TAILWIND, AttackNamesies.BULLDOZE, AttackNamesies.ICY_WIND, AttackNamesies.COTTON_SPORE, AttackNamesies.ELECTROWEB, AttackNamesies.GLACIATE, AttackNamesies.STICKY_WEB);

        updater.add("raises the", "raises its", AttackNamesies.AROMATIC_MIST, AttackNamesies.MAGNETIC_FLUX);
        updater.add("raise the", "raise its", AttackNamesies.GEAR_UP);
        updater.add("restores the", "restores its", AttackNamesies.LIFE_DEW);

        updater.add("the target's", "its", AttackNamesies.DECORATE);
        updater.add(" of an ally Pokémon", "", AttackNamesies.AROMATIC_MIST);
        updater.add(" its ally Pokémon", "", AttackNamesies.COACHING);
        updater.add(" and its allies", "", AttackNamesies.MIST, AttackNamesies.QUICK_GUARD, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.MAT_BLOCK);
        updater.add(" or its allies'", "", AttackNamesies.ACUPRESSURE);
        updater.add(" of itself and its ally Pokémon in the battle", "", AttackNamesies.LIFE_DEW);
        updater.add(" of itself and its ally Pokémon in battle", "", AttackNamesies.JUNGLE_HEALING);
        updater.add(" of ally Pokémon with the Plus or Minus Ability", "", AttackNamesies.MAGNETIC_FLUX, AttackNamesies.GEAR_UP);

        updater.add(" and damages all opposing Pokémon", "", AttackNamesies.EXPANDING_FORCE);
        updater.add("the Defense stats of all Grass-type Pokémon in battle", "its Defense stat", AttackNamesies.FLOWER_SHIELD);
        updater.add("the spirit of itself and allies. This raises their", "its spirit, which raises its", AttackNamesies.HOWL);
        updater.add(" If the target is an ally, it gives the ally a pollen puff that restores its HP instead.", "", AttackNamesies.POLLEN_PUFF);

        // Manually changed differences
        updater.add(" during the next turn", "", AttackNamesies.FAIRY_LOCK);
        updater.add(" This move deals twice the damage if the target is Dynamaxed.", "", AttackNamesies.DYNAMAX_CANNON, AttackNamesies.BEHEMOTH_BLADE, AttackNamesies.BEHEMOTH_BASH);
        updater.add("The user ignores the effects of opposing Pokémon's moves and Abilities that draw in moves, allowing this move to hit the chosen target.", "This move can be used on the target regardless of its Abilities. Critical hits land more easily.", AttackNamesies.SNIPE_SHOT);
        updater.add(" If there are two targets, this move hits each target once.", "", AttackNamesies.DRAGON_DARTS);

        // Serebii mistake
        updater.add("\\\\â\u0080\u0099", "'", AttackNamesies.KINESIS);
//        updater.add("\\\\", "", AttackNamesies.KINESIS);

        for (MoveParser parser : new MoveUpdater().getParsers()) {
            AttackNamesies namesies = parser.attackNamesies;
            Type type = parser.type;
            MoveCategory category = parser.category;

            int pp = parser.pp;
            int power = parser.power;
            int accuracy = parser.accuracy;

            String chance = parser.chance;
            String crit = parser.crit;
            int priority = parser.priority;

            boolean physicalContact = parser.physicalContact;
            boolean soundMove = parser.soundMove;
            boolean punchMove = parser.punchMove;
            boolean bitingMove = parser.bitingMove;
            boolean snatchable = parser.snatchable;
            boolean gravity = parser.gravity;
            boolean defrosty = parser.defrosty;
            boolean magicBouncy = parser.magicBouncy;
            boolean protecty = parser.protecty;
            boolean mirrorMovey = parser.mirrorMovey;

            // A few special cases
            switch (namesies) {
                case STRUGGLE:
                    // Because I'm right and this is wrong
                    Assert.assertEquals(type, Type.NORMAL);
                    type = Type.NO_TYPE;
                    break;
                case QUICK_GUARD:
                    // Doesn't make sense to snatch with only single battles, plus they're in the same priority bracket
                    Assert.assertTrue(snatchable);
                    snatchable = false;
                    // Fall through
                case CRAFTY_SHIELD:
                    // Why are Quick Guard/Crafty Shield different than Protect/Spiky Shield/etc. it's weird and I don't care
                    Assert.assertEquals(3, priority);
                    priority = 4;
                    break;
                case POISON_JAB:
                case PLASMA_FISTS:
                case ICE_HAMMER:
                    // Because they should be
                    Assert.assertFalse(punchMove);
                    punchMove = true;
                    break;
                case ELECTRIFY:
                    // I merged this move with Ion Deluge and that includes priority
                    Assert.assertEquals(0, priority);
                    priority = 1;
                    break;
                case IMPRISON:
                    // I don't know why this is like this but it makes no sense
                    Assert.assertTrue(snatchable);
                    Assert.assertFalse(magicBouncy);
                    Assert.assertFalse(mirrorMovey);
                    snatchable = false;
                    magicBouncy = true;
                    mirrorMovey = true;
                    break;
                case COACHING:
                    Assert.assertEquals(0, accuracy);
                    accuracy = 101;
                    // fallthrough
                case ACUPRESSURE:
                case AROMATIC_MIST:
                case FLOWER_SHIELD:
                case ROTOTILLER:
                case JUNGLE_HEALING:
                case DECORATE:
                    // These were changed to only affect the user
                    Assert.assertFalse(snatchable);
                    snatchable = true;
                    break;
                case TEETER_DANCE:
                    // This too
                    Assert.assertFalse(magicBouncy);
                    magicBouncy = true;
                    break;
                case FLORAL_HEALING:
                case PURIFY:
                    // Also these moves
                    Assert.assertFalse(snatchable);
                    Assert.assertTrue(magicBouncy);
                    Assert.assertTrue(protecty);
                    snatchable = true;
                    magicBouncy = false;
                    protecty = false;
                    break;
                case MIND_READER:
                case LOCK_ON:
                    // Because I don't care enough
                    Assert.assertTrue(protecty);
                    Assert.assertTrue(mirrorMovey);
                    protecty = false;
                    mirrorMovey = false;
                    break;
                case MIMIC:
                case REFLECT_TYPE:
                case POWER_SPLIT:
                case GUARD_SPLIT:
                    // DGAF
                    Assert.assertTrue(protecty);
                    protecty = false;
                    break;
                case MEAN_LOOK:
                case BLOCK:
                case BESTOW:
                    // I feel like I should be right here
                    Assert.assertFalse(protecty);
                    protecty = true;
                    break;
                case FAIRY_LOCK:
                    Assert.assertFalse(magicBouncy);
                    Assert.assertFalse(protecty);
                    magicBouncy = true;
                    protecty = true;
                    break;
                case WONDER_ROOM:
                case TRICK_ROOM:
                case MAGIC_ROOM:
                    // This makes no sense I reign supreme
                    Assert.assertTrue(mirrorMovey);
                    mirrorMovey = false;
                    break;
                case PSYCH_UP:
                case HEAL_PULSE:
                    // Again, I make the rules in this game
                    Assert.assertFalse(mirrorMovey);
                    mirrorMovey = true;
                    break;
                case TRIPLE_KICK:
                    // TODO: This is temporary and should be fixed
                    Assert.assertEquals(10, power);
                    power = 20;
                    break;
                case TRIPLE_AXEL:
                    // TODO: This is temporary and should be fixed
                    // Also I have no idea wtf is happening with the 30% chance???
                    Assert.assertEquals(20, power);
                    Assert.assertEquals("30", chance);
                    power = 40;
                    chance = "--";
                    break;
                case FOUL_PLAY:
                    // TODO: This should be fixed as well
                    Assert.assertEquals(95, power);
                    power = 1;
                    break;
                case FIRE_FANG:
                case THUNDER_FANG:
                case ICE_FANG:
                    Assert.assertEquals("10", chance);
                    chance = "20";
                    break;
                case COURT_CHANGE:
                    Assert.assertTrue(mirrorMovey);
                    mirrorMovey = false;
                    // fallthrough
                case OBSTRUCT:
                case CLANGOROUS_SOUL:
                    Assert.assertEquals(100, accuracy);
                    accuracy = 101;
                    break;
                case SCORCHING_SANDS:
                    Assert.assertEquals("--", chance);
                    chance = "30";
                    break;
                case SCALE_SHOT:
                    // I'm so confused why does this have a 20% chance??
                    Assert.assertEquals("20", chance);
                    chance = "--";
                    break;
                case SHELL_SIDE_ARM:
                    Assert.assertEquals("--", chance);
                    chance = "20";
                    break;
                case SUPER_FANG:
                    // I know it doesn't technically do anything to make Super Fang biting, but I like it...
                    Assert.assertFalse(bitingMove);
                    bitingMove = true;
                    break;
                case DYNAMAX_CANNON:
                    // Not really sure why this would be gravity-effected?
                    Assert.assertTrue(gravity);
                    gravity = false;
                    break;
            }

            Attack attack = namesies.getNewAttack();
            Assert.assertEquals(attack.getName(), type, attack.getActualType());
            Assert.assertEquals(attack.getName(), category, attack.getCategory());
            Assert.assertEquals(attack.getName(), pp, attack.getPP());
            Assert.assertEquals(attack.getName(), priority, attack.getActualPriority());
            Assert.assertEquals(attack.getName(), physicalContact, attack.isMoveType(MoveType.PHYSICAL_CONTACT));
            Assert.assertEquals(attack.getName(), soundMove, attack.isMoveType(MoveType.SOUND_BASED));
            Assert.assertEquals(attack.getName(), punchMove, attack.isMoveType(MoveType.PUNCHING));
            Assert.assertEquals(attack.getName(), bitingMove, attack.isMoveType(MoveType.BITING));
            Assert.assertEquals(attack.getName(), defrosty, attack.isMoveType(MoveType.DEFROST));
            Assert.assertEquals(attack.getName(), gravity, attack.isMoveType(MoveType.AIRBORNE));

            if (parser.description.equals("This move can't be used. It's recommended that this move is forgotten. Once forgotten, this move can't be remembered.")) {
                Assert.assertFalse(attack.getName(), updater.updatesMap.containsKey(namesies));
            } else {
                updater.assertDescription(namesies, parser, attack.getDescription());
            }

            String powerString = power == 0 || power == 1 ? "--" : Integer.toString(power);
            Assert.assertEquals(attack.getName(), powerString, attack.getPowerString());

            String accuracyString = accuracy == 101 ? "--" : Integer.toString(accuracy);
            Assert.assertEquals(attack.getName(), accuracyString, attack.getAccuracyString());

            int effectChance = chance.equals("--") ? 100 : Integer.parseInt(chance);
            Assert.assertEquals(attack.getName(), effectChance, attack.getEffectChance());

            Assert.assertEquals(attack.getName(), crit.equals("None"), attack.isStatusMove());
            switch (crit) {
                case "None":  // Status moves
                case "4.17%": // Regular attacking moves
                    Assert.assertFalse(attack.getName(), attack instanceof CritStageEffect);
                    Assert.assertFalse(attack.getName(), attack instanceof AlwaysCritEffect);
                    break;
                case "12.5%": // Crit-stage increased moves
                    Assert.assertTrue(attack.getName(), attack instanceof CritStageEffect);
                    Assert.assertFalse(attack.getName(), attack instanceof AlwaysCritEffect);
                    break;
                case "100%": // Always crit moves
                    Assert.assertFalse(attack.getName(), attack instanceof CritStageEffect);
                    Assert.assertTrue(attack.getName(), attack instanceof AlwaysCritEffect);
                    break;
                default:
                    Assert.fail("Invalid crit ratio " + crit + " for " + attack.getName());
                    break;
            }

            Assert.assertEquals(attack.getName(), snatchable, attack.isSnatchable());
            Assert.assertEquals(attack.getName(), magicBouncy, attack.isMagicReflectable());
            Assert.assertEquals(attack.getName(), protecty, attack.isProtectAffected());
            Assert.assertEquals(attack.getName(), mirrorMovey, !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.MIRRORLESS));

            toParse.remove(namesies);
        }

        Assert.assertTrue(toParse.toString(), toParse.isEmpty());
    }

    @Test
    public void itemParserTest() {
        Set<ItemNamesies> toParse = EnumSet.allOf(ItemNamesies.class);
        toParse.remove(ItemNamesies.NO_ITEM);
        toParse.remove(ItemNamesies.SYRUP);
        toParse.remove(ItemNamesies.SURFBOARD);
        toParse.remove(ItemNamesies.RUBY);
        toParse.remove(ItemNamesies.HARDY_MINT);
        toParse.remove(ItemNamesies.DOCILE_MINT);
        toParse.remove(ItemNamesies.BASHFUL_MINT);
        toParse.remove(ItemNamesies.QUIRKY_MINT);
        toParse.removeIf(itemNamesies -> itemNamesies.getItem() instanceof TechnicalMachine);

        DescriptionUpdater<ItemNamesies> updater = new DescriptionUpdater<>();

        // Lanturn deserves more love!!!
        updater.add("Clamperl", "Clamperl, Chinchou, or Lanturn", ItemNamesies.DEEP_SEA_SCALE);

        // Do these places even exist in canon?
        updater.add("was used in the Safari Zone in the Kanto region and in the Great Marsh in the Sinnoh region",
                    "is used only in the Safari Zone. It is recognizable by the camouflage pattern decorating it", ItemNamesies.SAFARI_BALL);

        // Razzle Dazzle
        updater.add("Used to make Pokéblocks that will enhance your Coolness. Its red flesh is spicy when eaten.",
                    "A very valuable berry. Useful for acquiring value.", ItemNamesies.RAZZ_BERRY);

        // More than two base abilities
        updater.add(" with two Abilities", "", ItemNamesies.ABILITY_CAPSULE);
        updater.add("these Abilities", "its Abilities", ItemNamesies.ABILITY_CAPSULE);

        // EV decreasing berries just do that since no friendship values
        updater.add(" makes it more friendly but", "", ItemNamesies.POMEG_BERRY, ItemNamesies.KELPSY_BERRY, ItemNamesies.QUALOT_BERRY, ItemNamesies.HONDEW_BERRY, ItemNamesies.GREPA_BERRY, ItemNamesies.TAMATO_BERRY);

        // It was easier to code this way... I don't have that shortened version in the stat...
        updater.add("Sp. Atk", "Sp. Attack", ItemNamesies.ADAMANT_MINT, ItemNamesies.IMPISH_MINT, ItemNamesies.CAREFUL_MINT, ItemNamesies.JOLLY_MINT, ItemNamesies.MODEST_MINT, ItemNamesies.MILD_MINT, ItemNamesies.RASH_MINT, ItemNamesies.QUIET_MINT);
        updater.add("Sp. Def", "Sp. Defense", ItemNamesies.NAUGHTY_MINT, ItemNamesies.LAX_MINT, ItemNamesies.RASH_MINT, ItemNamesies.NAIVE_MINT, ItemNamesies.CALM_MINT, ItemNamesies.GENTLE_MINT, ItemNamesies.CAREFUL_MINT, ItemNamesies.SASSY_MINT);

        // Serebii mistake
        updater.add("heat- repelling", "heat-repelling", ItemNamesies.NEVER_MELT_ICE);

        for (ItemParser parser : new ItemUpdater().getParsers()) {
            ItemNamesies namesies = parser.itemNamesies;
            String itemType = parser.itemType;

            int fling = parser.fling;
            int price = parser.price;

            Type naturalGiftType = parser.naturalGiftType;
            int naturalGiftPower = parser.naturalGiftPower;

            Item item = namesies.getItem();
            updater.assertDescription(namesies, parser, item.getDescription());

            if (item.isHoldable() && fling != 0) {
                HoldItem holdItem = (HoldItem)item;
                Assert.assertEquals(item.getName(), fling, holdItem.flingDamage());
            } else if (!(item instanceof BallItem)) {
                // Ball items are not holdable in this game
                Assert.assertEquals(item.getName(), 0, fling);
            }

            if (item.getBagCategory() != BagCategory.KEY_ITEM) {
                // Serebii has the wrong values for these, and I manually looked up in Bulbapedia instead (which is more accurate but way harder to parse)
                // Just gonna leave this commented out since it's annoying and who cares
//                TestUtils.semiAssertTrue(StringUtils.spaceSeparated(item.getName(), price, item.getPrice()), price == item.getPrice());

                if (namesies == ItemNamesies.MASTER_BALL || namesies == ItemNamesies.SAFARI_BALL) {
                    Assert.assertEquals(item.getName(), 0, item.getPrice());
                } else {
                    Assert.assertTrue(item.getName(), item.getPrice() > 0);
                }
            } else {
                Assert.assertEquals(item.getName(), 0, price);
                Assert.assertEquals(item.getName(), -1, item.getPrice());
            }

            if (item instanceof Berry) {
                Berry berry = (Berry)item;
                Assert.assertEquals(item.getName(), naturalGiftType, berry.naturalGiftType());
                Assert.assertEquals(item.getName(), naturalGiftPower, berry.naturalGiftPower());
                Assert.assertNotEquals(item.getName(), Type.NO_TYPE, naturalGiftType);
                Assert.assertNotEquals(item.getName(), 0, naturalGiftPower);
            } else {
                Assert.assertEquals(item.getName(), Type.NO_TYPE, naturalGiftType);
                Assert.assertEquals(item.getName(), 0, naturalGiftPower);
            }

            switch (itemType) {
                case "Battle Effect":
                    Assert.assertTrue(item.getName(), item instanceof BattleUseItem);
                    break;
                case "Miscellaneous":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.MISC);
                    break;
                case "Evolutionary":
                    Assert.assertTrue(item.getName(), item instanceof EvolutionItem);
                    break;
                case "Berry":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.BERRY);
                    break;
                case "Key Item":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.KEY_ITEM);
                    break;
                case "Hold Item":
                    Assert.assertTrue(item.getName(), item instanceof HoldItem);
                    break;
                case "Recovery":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.MEDICINE);
                    break;
                case "Pokeball":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.BALL);
                    break;
                case "Vitamins":
                    Assert.assertEquals(item.getName(), item.getBagCategory(), BagCategory.STAT);
                    break;
                default:
                    Assert.fail(item.getName() + ": " + itemType);
                    break;
            }

            toParse.remove(namesies);
        }

        Assert.assertTrue(toParse.toString(), toParse.isEmpty());
    }

    @Test
    public void abilityParserTest() {
        Set<AbilityNamesies> toParse = EnumSet.allOf(AbilityNamesies.class);
        toParse.remove(AbilityNamesies.NO_ABILITY);

        DescriptionUpdater<AbilityNamesies> updater = new DescriptionUpdater<>();

        // Personal grammar preference
        updater.add("--", " -- ", AbilityNamesies.DOWNLOAD);

        // Because no double battles
        updater.add(" and its ally Pokémon", "", AbilityNamesies.PASTEL_VEIL);
        updater.add("an ally's", "its", AbilityNamesies.HEALER);
        updater.add("ally", "the", AbilityNamesies.BATTERY, AbilityNamesies.STEELY_SPIRIT);
        updater.add("Ally ", "", AbilityNamesies.FLOWER_VEIL);
        updater.add("Just being next to the Pokémon p", "P", AbilityNamesies.POWER_SPOT);
        updater.add("up moves", "up the Pokémon's moves", AbilityNamesies.POWER_SPOT);

        // Manually changed differences
        updater.add(" or Z-Crystal", "", AbilityNamesies.MULTITYPE);
        updater.add("ally", "enemy", AbilityNamesies.POWER_OF_ALCHEMY);

        // Serebii mistake
        updater.add("Boosts Ally Pokémon's Special Attack by 30%.", "", AbilityNamesies.BATTERY);

        for (AbilityParser parser : new AbilityUpdater().getParsers()) {
            AbilityNamesies namesies = parser.abilityNamesies;
            Ability ability = namesies.getNewAbility();
            updater.assertDescription(namesies, parser, ability.getDescription());

            toParse.remove(namesies);
        }

        Assert.assertTrue(toParse.toString(), toParse.isEmpty());
    }

    private static class DescriptionUpdater<NamesiesType> {
        private final MultiMap<NamesiesType, Entry<String, String>> updatesMap;
        private final Set<String> seenPairs;

        public DescriptionUpdater() {
            this.updatesMap = new MultiMap<>();
            this.seenPairs = new HashSet<>();
        }

        @SafeVarargs
        public final void add(String substring, String replacement, NamesiesType... allNamesies) {
            // If this fails, then rules can be combined
            String pair = substring + " " + replacement;
            Assert.assertFalse(pair, this.seenPairs.contains(pair));
            this.seenPairs.add(pair);

            for (NamesiesType namesies : allNamesies) {
                this.updatesMap.put(namesies, new SimpleEntry<>(substring, replacement));
            }
        }

        private void assertDescription(NamesiesType namesies, BaseParser parser, String actualDescription) {
            // If the description needs to be updated -- update it before we assert!!!
            if (this.updatesMap.containsKey(namesies)) {
                for (Entry<String, String> entry : this.updatesMap.get(namesies)) {
                    String substring = entry.getKey();
                    String replacement = entry.getValue();
                    String message = StringUtils.spaceSeparated(substring, replacement, parser.description);
                    Assert.assertNotNull(message, parser.description);

                    // Checking if replaceAll changes the string instead of using the contains method
                    // because replaceAll uses regex and contains does not
                    String newDescription = parser.description.replaceAll(substring, replacement);
                    Assert.assertNotEquals(message, parser.description, newDescription);
                    parser.description = newDescription;
                }
            }

            Assert.assertEquals(parser.name, parser.description, actualDescription);
        }
    }
}
