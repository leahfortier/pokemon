package test.script;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.effect.EffectInterfaces.PartialTrappingEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import org.junit.Assert;
import pokemon.Stat;
import test.TestUtils;
import test.battle.TestStages;
import type.Type;
import util.string.StringUtils;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Contains the raw information about a move as parsed from PokeAPI
// Includes any updates that I may have made to the rules or API fixes since it is a bit inconsistent
public class MoveApiParser {
    // Everything is fucking public and non-final because this is a test object so who cares
    public AttackNamesies namesies;
    public String description;
    public Type type;
    public String target;
    public MoveCategory moveCategory;
    public String accuracy;
    public int pp;
    public String power;
    public int effectChance;
    public int priority;
    public int[] statChanges;
    public String moveTypeThingy;
    public String ailmentString;
    public Object ailment;
    public String minHits;
    public String maxHits;
    public String minTurns;
    public String maxTurns;
    public String drain;
    public String healing;
    public String critRate;
    public int ailmentChance;
    public int flinchChance;
    public int statChance;
    public String shortEffect;
    public String effectDescription;

    MoveApiParser(Scanner in) {
        namesies = AttackNamesies.getValueOf(in.nextLine());
        Attack attack = namesies.getNewAttack();

        description = getDescription(attack, in.nextLine());
        String message = attack.getName() + " " + description;

        type = Type.valueOf(in.nextLine());
        target = in.nextLine();
        moveCategory = MoveCategory.valueOf(in.nextLine());
        accuracy = getIntString(message, in.nextLine(), "--");
        pp = Integer.parseInt(in.nextLine());
        power = getIntString(message, in.nextLine(), "--");
        effectChance = getInt(message, in.nextLine(), 100);
        priority = Integer.parseInt(in.nextLine());
        statChanges = getStatChanges(message, namesies, description, in.nextLine());
        moveTypeThingy = in.nextLine();
        ailmentString = in.nextLine();
        ailment = getAilment(message, attack, ailmentString);
        minHits = in.nextLine();
        maxHits = in.nextLine();
        minTurns = in.nextLine();
        maxTurns = in.nextLine();
        drain = in.nextLine();
        healing = in.nextLine();
        critRate = in.nextLine();
        ailmentChance = getInt(message, in.nextLine(), 0);
        flinchChance = getInt(message, in.nextLine(), 0);
        statChance = getInt(message, in.nextLine(), 0);
        shortEffect = in.nextLine();
        effectDescription = in.nextLine();

        this.specialCases(message);
    }

    private void specialCases(String message) {
        switch (namesies) {
            case STRUGGLE:
                Assert.assertEquals(message, Type.NORMAL, type);
                type = Type.NO_TYPE;
                break;
            case QUICK_GUARD:
            case CRAFTY_SHIELD:
                // Why are Quick Guard/Crafty Shield different than Protect/Spiky Shield/etc. it's weird and I don't care
                Assert.assertEquals(message, 3, priority);
                priority = 4;
                break;
            case ELECTRIFY:
                // I merged this move with Ion Deluge and that includes priority
                Assert.assertEquals(message, 0, priority);
                priority = 1;
                break;
            case TRIPLE_KICK:
                // TODO: This is temporary and should be fixed
                Assert.assertEquals("10", power);
                power = "20";
                break;
            case FOUL_PLAY:
                // TODO: This should be fixed as well
                Assert.assertEquals("95", power);
                power = "--";
                break;
            case FIRE_FANG:
            case ICE_FANG:
            case THUNDER_FANG:
                Assert.assertTrue(message, ailment instanceof StatusNamesies);
                Assert.assertNotEquals(message, StatusNamesies.NO_STATUS, ailment);
                Assert.assertEquals(message, 10, effectChance);
                Assert.assertEquals(message, 10, flinchChance);
                Assert.assertEquals(message, 10, ailmentChance);
                ailment = null;
                effectChance = 20;
                ailmentChance = 0;
                flinchChance = 0;
                break;
            case SKULL_BASH:
                Assert.assertEquals(message, 100, ailmentChance);
                Assert.assertEquals(message, 0, statChance);
                ailmentChance = 0;
                statChance = 100;
                break;
            case LUNGE:
            case FIRE_LASH:
            case TROP_KICK:
                Assert.assertEquals(message, 0, statChance);
                statChance = 100;
                break;
            case SHADOW_BONE:
            case LIQUIDATION:
                Assert.assertEquals(message, 0, statChance);
                statChance = 20;
                break;
            case SMACK_DOWN:
            case FROST_BREATH:
            case TOXIC_THREAD:
            case THROAT_CHOP:
                Assert.assertEquals(message, 100, ailmentChance);
                ailmentChance = 0;
                break;
        }
    }

    private String getDescription(Attack attack, String description) {
        Pattern dashyDash = Pattern.compile("([^\\s])--([^\\s])");
        Matcher dashyMatcher = dashyDash.matcher(description);
        description = dashyMatcher.replaceAll(matchResult -> matchResult.group(1) + " -- " + matchResult.group(2));

        switch (attack.namesies()) {
            case SKY_DROP:
            case AROMATIC_MIST:
            case FLOWER_SHIELD:
            case ROTOTILLER:
            case MAGNETIC_FLUX:
            case GEAR_UP:
            case POLLEN_PUFF:
                // Manually altered moves
                Assert.assertNotEquals(attack.getName(), description, attack.getDescription());
                description = attack.getDescription();
                break;

        }

        return description;
    }

    private int getInt(String message, String intString, int defaultValue) {
        return Integer.parseInt(getIntString(message, intString, Integer.toString(defaultValue)));
    }

    private String getIntString(String message, String intString, String defaultValue) {
        try {
            Integer.parseInt(intString);
        } catch (NumberFormatException ex) {
            Assert.assertEquals(message, "None", intString);
            intString = defaultValue;
        }

        return intString;
    }

    // So like the API can't really handle negative stage changes for the user for some reason
    // Double check with the hardcoded description (which should legit say the changes so if that changes it will enforce looking at it again)
    // Also yes I understand that this literally returns the passed in hardcoded stages but I don't care
    // I want it to be in the same line as the hardcoded description and might need it in here if something else changes fucking sue me
    private int[] fixEmptyStages(String message, int[] statChanges, String description, TestStages hardcodedStages, String hardcodedDescription) {
        TestUtils.assertEquals(message, new TestStages().get(), statChanges);
        statChanges = hardcodedStages.get();
        Assert.assertEquals(message, description, hardcodedDescription);
        return statChanges;
    }

    // Honestly there are so many issues with this I just put it in a separate method
    private int[] getStatChanges(String message, AttackNamesies attack, String description, String statChangesString) {
        int[] statChanges = new int[Stat.NUM_BATTLE_STATS];
        Scanner statScanner = new Scanner(statChangesString);
        int numChanges = statScanner.nextInt();
        for (int i = 0; i < numChanges; i++) {
            Stat stat = Stat.valueOf(statScanner.next());
            int change = statScanner.nextInt();
            statChanges[stat.index()] = change;
        }

        switch (attack) {
            case GROWTH:
                int[] growthStages = new TestStages().set(Stat.ATTACK, 1).set(Stat.SP_ATTACK, 1).get();
                TestUtils.assertEquals(message, growthStages, statChanges);
                statChanges = new TestStages().get();
                break;
            case SKULL_BASH:
                // This API isn't all it's cracked up to be apparently (or I'm dumb and just can't find where this information is hiding)
                // Just confirm that the description doesn't change to something that says otherwise
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, 1),
                        "The user tucks in its head to raise its Defense stat on the first turn, then rams the target on the next turn."
                );
                break;
            case DRAGON_ASCENT:
                // So it doesn't look like this API can handle stat changes that lower the user's stats not sure why
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, -1).set(Stat.SP_DEFENSE, -1),
                        "After soaring upward, the user attacks its target by dropping out of the sky at high speeds. But it lowers its own Defense and Sp. Def stats in the process."
                );
                break;
            case SHELL_SMASH:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, -1).set(Stat.SP_DEFENSE, -1).set(Stat.ATTACK, 2).set(Stat.SP_ATTACK, 2).set(Stat.SPEED, 2),
                        "The user breaks its shell, which lowers Defense and Sp. Def stats but sharply raises its Attack, Sp. Atk, and Speed stats."
                );
                break;
            case ICE_HAMMER:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.SPEED, -1),
                        "The user swings and hits with its strong, heavy fist. It lowers the user's Speed, however."
                );
                break;
            case DEFOG:
                // Okay now honestly I have no idea why this one is here
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.EVASION, -1),
                        "A strong wind blows away the target's barriers such as Reflect or Light Screen. This also lowers the target's evasiveness."
                );
                break;
            case MEMENTO:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.ATTACK, -2).set(Stat.SP_ATTACK, -2),
                        "The user faints when using this move. In return, this harshly lowers the target's Attack and Sp. Atk stats."
                );
                break;
            case STRENGTH_SAP:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.ATTACK, -1),
                        "The user restores its HP by the same amount as the target's Attack stat. It also lowers the target's Attack stat."
                );
                break;
            case TOXIC_THREAD:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.SPEED, -1),
                        "The user shoots poisonous threads to poison the target and lower the target's Speed stat."
                );
                break;
            case LUNGE:
                // Not sure why this is here like it's a pretty normal use-case
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.ATTACK, -1),
                        "The user makes a lunge at the target, attacking with full force. This also lowers the target's Attack stat."
                );
                break;
            case TROP_KICK:
                // Not sure why this is here like it's a pretty normal use-case
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.ATTACK, -1),
                        "The user lands an intense kick of tropical origins on the target. This also lowers the target's Attack stat."
                );
                break;
            case FIRE_LASH:
                // Not sure why this is here like it's a pretty normal use-case
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, -1),
                        "The user strikes the target with a burning lash. This also lowers the target's Defense stat."
                );
                break;
            case SHADOW_BONE:
                // Not sure why this is here like it's a pretty normal use-case
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, -1),
                        "The user attacks by beating the target with a bone that contains a spirit. This may also lower the target's Defense stat."
                );
                break;
            case LIQUIDATION:
                // Yeah it looks like I will be manually adding all gen 7 moves that decrease stats... that was so not the point of this whole thing
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, -1),
                        "The user slams into the target using a full-force blast of water. This may also lower the target's Defense stat."
                );
                break;
            case CLANGING_SCALES:
                // Not sure why this is here like it's a pretty normal use-case
                // Also wtf is that description for the decrease in defense??
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.DEFENSE, -1),
                        "The user rubs the scales on its entire body and makes a huge noise to attack the opposing Pokémon. The user's Defense stat goes down after the attack."
                );
                break;
            case FLEUR_CANNON:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.SP_ATTACK, -2),
                        "The user unleashes a strong beam. The attack's recoil harshly lowers the user's Sp. Atk stat."
                );
                break;
            case TEARFUL_LOOK:
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.ATTACK, -1).set(Stat.SP_ATTACK, -1),
                        "The user gets teary eyed to make the target lose its combative spirit. This lowers the target's Attack and Sp. Atk stats."
                );
                break;
            case DIAMOND_STORM:
                // GET WITH THE PROGRAM THIS WAS CHANGED IN GEN 7 SUCKERS
                TestUtils.assertEquals(message, new TestStages().set(Stat.DEFENSE, 1).get(), statChanges);
                statChanges = new TestStages().set(Stat.DEFENSE, 2).get();
                Assert.assertEquals(message, description, "The user whips up a storm of diamonds to damage opposing Pokémon. This may also sharply raise the user's Defense stat.");
                break;
            case GEAR_UP:
                // This is a move that we manually changed so it's cool -- not sure if there's really a purpose in
                // hardcoding the description when it was changed but whatever
                statChanges = fixEmptyStages(
                        message, statChanges, description,
                        new TestStages().set(Stat.ATTACK, 1).set(Stat.SP_ATTACK, 1),
                        "The user engages its gears to raise its Attack and Sp. Atk stats."
                );
        }

        return statChanges;
    }

    private Object getAilment(String message, Attack attack, String ailmentString) {
        switch (ailmentString) {
            // Perish Song is a special case and it is not included in its effect list since it applies to both
            case "PERISH_SONG":
            case "NONE":
                return null;
            case "PARALYSIS":
                return StatusNamesies.PARALYZED;
            case "POISON":
                return StatusNamesies.POISONED;
            case "SLEEP":
                return StatusNamesies.ASLEEP;
            case "BURN":
                return StatusNamesies.BURNED;
            case "FREEZE":
                return StatusNamesies.FROZEN;
            case "NO_TYPE_IMMUNITY":
                return PokemonEffectNamesies.FORESIGHT;
            case "TRAP":
                PokemonEffectNamesies partialTrap = getPartialTrap(message, attack);
                Assert.assertTrue(message, partialTrap.getEffect() instanceof PartialTrappingEffect);
                return partialTrap;
            case "UNKNOWN":
                return getUnknownAilment(message, attack);
        }

        PokemonEffectNamesies pokemonEffect = StringUtils.enumTryValueOf(PokemonEffectNamesies.class, ailmentString);
        if (pokemonEffect != null) {
            return pokemonEffect;
        }

        Assert.fail(message + "\nUnknown ailment " + ailmentString);
        return null;
    }

    // Used when the ailment string is "UNKNOWN"
    private Object getUnknownAilment(String message, Attack attack) {
        switch (attack.namesies()) {
            case TRI_ATTACK:
                return null;
            case TELEKINESIS:
                return PokemonEffectNamesies.TELEKINESIS;
            case SMACK_DOWN:
                return PokemonEffectNamesies.GROUNDED;
        }

        Assert.fail(message + "\nSuper unknown ailment.");
        return null;
    }

    // Used when the ailment string is "TRAP"
    private PokemonEffectNamesies getPartialTrap(String message, Attack attack) {
        switch (attack.namesies()) {
            case FIRE_SPIN:
                return PokemonEffectNamesies.FIRE_SPIN;
            case WRAP:
                return PokemonEffectNamesies.WRAPPED;
            case SAND_TOMB:
                return PokemonEffectNamesies.SAND_TOMB;
            case CLAMP:
                return PokemonEffectNamesies.CLAMPED;
            case WHIRLPOOL:
                return PokemonEffectNamesies.WHIRLPOOLED;
            case BIND:
                return PokemonEffectNamesies.BINDED;
            case MAGMA_STORM:
                return PokemonEffectNamesies.MAGMA_STORM;
            case INFESTATION:
                return PokemonEffectNamesies.INFESTATION;
        }

        Assert.fail(message + "\nNot a partially trapping move.");
        return PokemonEffectNamesies.FIRE_SPIN;
    }
}
