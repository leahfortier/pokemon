package test.battle;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.Effect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.SapHealthEffect;
import battle.effect.attack.SelfHealingMove;
import battle.effect.battle.BattleEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import generator.update.MoveUpdater;
import generator.update.MoveUpdater.MoveParser;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.IndividualValues;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;
import test.TestUtils;
import trainer.Team;
import type.Type;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AttackTest extends BaseTest {
    @Test
    public void moveTypeTest() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getNewAttack();

            // Physical contact moves cannot be status moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.PHYSICAL_CONTACT) && attack.isStatusMove());

            // Snatch only affects self-target status moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.NON_SNATCHABLE) && !attack.isSelfTargetStatusMove());

            // Magic Coat only affects non-self target status moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.NO_MAGIC_COAT) && !(!attack.isSelfTarget() && attack.isStatusMove()));

            // Protect and Mirror Move do not apply to self-target status moves or field moves
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.PROTECT_PIERCING) && (attack.isSelfTargetStatusMove() || attack.isMoveType(MoveType.FIELD)));
            Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.MIRRORLESS) && (attack.isSelfTargetStatusMove() || attack.isMoveType(MoveType.FIELD)));

            // All SelfHealingMoves and SapHealthEffects should be Healing move type
            if (attack instanceof SelfHealingMove || attack instanceof SapHealthEffect) {
                Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.HEALING));
            }

            // DANCE DANCE REVOLUTION
            if (attack.getName().contains("Dance")) {
                Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.DANCE));
            }

            // Crit stage moves cannot be status moves
            if (attack instanceof CritStageEffect || attack instanceof AlwaysCritEffect) {
                Assert.assertNotEquals(attack.getName(), MoveCategory.STATUS, attack.getCategory());
            }

            // Moves that cast battle effects are field moves
            for (EffectNamesies effectNamesies : attack.getEffects()) {
                Effect effect = effectNamesies.getEffect();
                if (effect instanceof BattleEffect) {
                    Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.NO_MAGIC_COAT));
                    Assert.assertTrue(attack.getName(), attack.isMoveType(MoveType.FIELD));
                }
            }

            // Status moves must apply their effects 100% of the time
            if (attack.isStatusMove()) {
                Assert.assertEquals(attack.getName(), 100, attack.getEffectChance());
            }
        }
    }

    @Test
    public void parserTest() {
        Set<AttackNamesies> toParse = EnumSet.allOf(AttackNamesies.class);
        toParse.remove(AttackNamesies.CONFUSION_DAMAGE);

        for (MoveParser moveParser : new MoveUpdater().getParseMoves()) {
            AttackNamesies attackNamesies = moveParser.attackNamesies;
            Type type = moveParser.type;
            MoveCategory category = moveParser.category;

            int pp = moveParser.pp;
            int power = moveParser.power;
            int accuracy = moveParser.accuracy;

            String chance = moveParser.chance;
            String crit = moveParser.crit;
            int priority = moveParser.priority;

            boolean physicalContact = moveParser.physicalContact;
            boolean soundMove = moveParser.soundMove;
            boolean punchMove = moveParser.punchMove;
            boolean snatchable = moveParser.snatchable;
            boolean defrosty = moveParser.defrosty;
            boolean magicBouncy = moveParser.magicBouncy;
            boolean protecty = moveParser.protecty;
            boolean mirrorMovey = moveParser.mirrorMovey;

            // A few special cases
            switch (attackNamesies) {
                case STRUGGLE:
                    // Because I'm right and this is wrong
                    Assert.assertEquals(type, Type.NORMAL);
                    type = Type.NO_TYPE;
                    break;
                case QUICK_GUARD:
                    // Doesn't make sense to snatch with only single battles, plus they're in the same priority bracket
                    Assert.assertEquals(true, snatchable);
                    snatchable = false;
                    // Fall through
                case CRAFTY_SHIELD:
                    // Why are Quick Guard/Crafty Shield different than Protect/Spiky Shield/etc. it's weird and I don't care
                    Assert.assertEquals(3, priority);
                    priority = 4;
                    break;
                case POISON_JAB:
                case PLASMA_FISTS:
                    // Because they should be
                    Assert.assertEquals(false, punchMove);
                    punchMove = true;
                    break;
                case ELECTRIFY:
                    // I merged this move with Ion Deluge and that include priority
                    Assert.assertEquals(0, priority);
                    priority = 1;
                    break;
                case IMPRISON:
                    // I don't know why this is like this but it makes no sense
                    Assert.assertEquals(true, snatchable);
                    Assert.assertEquals(false, magicBouncy);
                    Assert.assertEquals(false, mirrorMovey);
                    snatchable = false;
                    magicBouncy = true;
                    mirrorMovey = true;
                    break;
                case ACUPRESSURE:
                case AROMATIC_MIST:
                case FLOWER_SHIELD:
                case ROTOTILLER:
                    // These were changed to only affect the user
                    Assert.assertEquals(false, snatchable);
                    snatchable = true;
                    break;
                case TEETER_DANCE:
                    // This too
                    Assert.assertEquals(false, magicBouncy);
                    magicBouncy = true;
                    break;
                case FLORAL_HEALING:
                case PURIFY:
                    // Also these moves
                    Assert.assertEquals(false, snatchable);
                    Assert.assertEquals(true, magicBouncy);
                    Assert.assertEquals(true, protecty);
                    snatchable = true;
                    magicBouncy = false;
                    protecty = false;
                    break;
                case MIND_READER:
                case LOCK_ON:
                    // Because I don't care enough
                    Assert.assertEquals(true, protecty);
                    Assert.assertEquals(true, mirrorMovey);
                    protecty = false;
                    mirrorMovey = false;
                    break;
                case MIMIC:
                case REFLECT_TYPE:
                case POWER_SPLIT:
                case GUARD_SPLIT:
                    // DGAF
                    Assert.assertEquals(true, protecty);
                    protecty = false;
                    break;
                case MEAN_LOOK:
                case BLOCK:
                case BESTOW:
                    // I feel like I should be right here
                    Assert.assertEquals(false, protecty);
                    protecty = true;
                    break;
                case WONDER_ROOM:
                case TRICK_ROOM:
                case MAGIC_ROOM:
                case FAIRY_LOCK:
                    // This makes no sense I reign supreme
                    Assert.assertEquals(true, mirrorMovey);
                    mirrorMovey = false;
                    break;
                case PSYCH_UP:
                case HEAL_PULSE:
                    // Again, I make the rules in this game
                    Assert.assertEquals(false, mirrorMovey);
                    mirrorMovey = true;
                    break;
                case TRIPLE_KICK:
                    // TODO: This is temporary and should be fixed
                    Assert.assertEquals(10, power);
                    power = 20;
                    break;
                case FOUL_PLAY:
                    // TODO: This should be fixed as well
                    Assert.assertEquals(95, power);
                    power = 1;
                    break;
            }

            Attack attack = attackNamesies.getNewAttack();
            Assert.assertEquals(attack.getName(), type, attack.getActualType());
            Assert.assertEquals(attack.getName(), category, attack.getCategory());
            Assert.assertEquals(attack.getName(), pp, attack.getPP());
            Assert.assertEquals(attack.getName(), priority, attack.getActualPriority());
            Assert.assertEquals(attack.getName(), physicalContact, attack.isMoveType(MoveType.PHYSICAL_CONTACT));
            Assert.assertEquals(attack.getName(), soundMove, attack.isMoveType(MoveType.SOUND_BASED));
            Assert.assertEquals(attack.getName(), punchMove, attack.isMoveType(MoveType.PUNCHING));
            Assert.assertEquals(attack.getName(), defrosty, attack.isMoveType(MoveType.DEFROST));

            String powerString = power == 0 || power == 1 ? "--" : Integer.toString(power);
            Assert.assertEquals(attack.getName(), powerString, attack.getPowerString());

            String accuracyString = accuracy == 0 ? "--" : Integer.toString(accuracy);
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

            Assert.assertEquals(attack.getName(), snatchable, attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.NON_SNATCHABLE));
            Assert.assertEquals(attack.getName(), magicBouncy, !attack.isSelfTarget() && attack.isStatusMove() && !attack.isMoveType(MoveType.NO_MAGIC_COAT));
            Assert.assertEquals(attack.getName(), protecty, !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.PROTECT_PIERCING));
            Assert.assertEquals(attack.getName(), mirrorMovey, !attack.isSelfTargetStatusMove() && !attack.isMoveType(MoveType.FIELD) && !attack.isMoveType(MoveType.MIRRORLESS));

            toParse.remove(attackNamesies);
        }

        Assert.assertTrue(toParse.isEmpty());
    }

    @Test
    public void baseAccuracyTest() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getNewAttack();

            try {
                // If the accuracy string is "--", then the move should ALWAYS hit
                Integer.parseInt(attack.getAccuracyString());

                // Self-target status moves and field moves should always have "--" accuracy string
                Assert.assertFalse(attack.getName(), attack.isSelfTargetStatusMove());
                Assert.assertFalse(attack.getName(), attack.isMoveType(MoveType.FIELD));
            } catch (NumberFormatException ex) {
                // Super perfect always hit moves -- test with -6 accuracy and +6 evasion, move should still hit
                TestBattle battle = TestBattle.create();
                TestPokemon attacking = battle.getAttacking();
                TestPokemon defending = battle.getDefending();

                attacking.getStages().setStage(Stat.ACCURACY, -Stat.MAX_STAT_CHANGES);
                defending.getStages().setStage(Stat.EVASION, Stat.MAX_STAT_CHANGES);

                attacking.setupMove(attackNamesies, battle);

                int moveAccuracy = attacking.getAttack().getAccuracy(battle, attacking, defending);
                int accuracy = Stat.getStat(Stat.ACCURACY, attacking, defending, battle);
                int evasion = Stat.getStat(Stat.EVASION, defending, attacking, battle);

                int totalAccuracy = (int)(moveAccuracy*((double)accuracy/(double)evasion));
                Assert.assertTrue(attack.getName(), accuracy < 100);
                Assert.assertTrue(attack.getName(), evasion > 100);
                Assert.assertTrue(attack.getName(), totalAccuracy > 100);
            }
        }
    }

    @Test
    public void recoilTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.ROCK_HEAD);
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.TAKE_DOWN);
        attacking.assertFullHealth();
        defending.assertNotFullHealth();

        defending.fullyHeal();
        defending.assertFullHealth();

        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        attacking.assertNotFullHealth();
        defending.assertNotFullHealth();

        int damage = attacking.getMaxHP() - attacking.getHP();
        Assert.assertTrue(defending.getMaxHP() - defending.getHP() == (int)(Math.ceil(damage/3.0)));

        attacking.fullyHeal();
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);
        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        defending.assertFullHealth();

        // Struggle should still cause recoil damage even if they have Rock Head/Magic Guard
        attacking.fullyHeal();
        battle.attackingFight(AttackNamesies.STRUGGLE);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.ROCK_HEAD));
        attacking.assertHealthRatio(.75);
    }

    @Test
    public void captivateTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.MALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        // TODO: Test genderless too
        attacking.apply(false, AttackNamesies.CAPTIVATE, battle);

        defending.withGender(Gender.FEMALE);
        attacking.apply(true, AttackNamesies.CAPTIVATE, battle);

        attacking.withAbility(AbilityNamesies.OBLIVIOUS);
        attacking.apply(true, AttackNamesies.CAPTIVATE, battle);

        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        defending.withAbility(AbilityNamesies.OBLIVIOUS);
        attacking.apply(false, AttackNamesies.CAPTIVATE, battle);
    }

    @Test
    public void ohkoTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.MAGIKARP, PokemonNamesies.DRAGONITE);
        TestPokemon defending = battle.getDefending();

        // Ground type should not effect Flying type
        battle.attackingFight(AttackNamesies.FISSURE);
        defending.assertFullHealth();

        // OHKO,MF
        battle.attackingFight(AttackNamesies.HORN_DRILL);
        Assert.assertTrue(defending.isFainted(battle));

        // Sturdy prevents OHKO
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.STURDY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        defending.assertFullHealth();

        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.isFainted(battle));

        // Sheer Cold doesn't work against Ice types
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.PROTEAN);
        battle.defendingFight(AttackNamesies.HAZE);
        Assert.assertTrue(defending.isType(battle, Type.ICE));
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        defending.assertFullHealth();
    }

    @Test
    public void selfSwitchingMoves() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.CHANSEY, PokemonNamesies.SHUCKLE);
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon attacking2 = TestPokemon.newPlayerPokemon(PokemonNamesies.HAPPINY);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // Use U-Turn -- make sure they swap
        battle.attackingFight(AttackNamesies.U_TURN);
        Assert.assertTrue(battle.getPlayer().front() == attacking2);

        // TODO: Baton Pass
        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void swapOpponentMoves() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.STEELIX, PokemonNamesies.SHUCKLE);
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon attacking2 = TestPokemon.newPlayerPokemon(PokemonNamesies.REGIROCK);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getAttacking() == attacking1);

        // Use Dragon Tail -- make sure they swap
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.DRAGON_TAIL);
        Assert.assertTrue(battle.getAttacking() == attacking2);

        // Don't swap with Suction Cups
        attacking2.withAbility(AbilityNamesies.SUCTION_CUPS);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.CIRCLE_THROW);
        Assert.assertTrue(battle.getAttacking() == attacking2);

        attacking2.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.ROAR);
        Assert.assertTrue(battle.getAttacking() == attacking1);

        // Don't swap when ingrained
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.WHIRLWIND);
        Assert.assertTrue(battle.getAttacking() == attacking1);

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void curseTest() {
        // TODO: Protean
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Non-Ghost type curse -- apply stat changes
        battle.attackingFight(AttackNamesies.CURSE);
        Assert.assertFalse(attacking.isType(battle, Type.GHOST));
        Assert.assertTrue(attacking.getStages().getStage(Stat.ATTACK) == 1);
        Assert.assertTrue(attacking.getStages().getStage(Stat.DEFENSE) == 1);
        Assert.assertTrue(attacking.getStages().getStage(Stat.SPEED) == -1);
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.CURSE));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CURSE));

        // Add Ghost Type
        battle.defendingFight(AttackNamesies.TRICK_OR_TREAT);
        Assert.assertTrue(attacking.isType(battle, Type.GHOST));
        attacking.assertFullHealth();
        defending.assertFullHealth();

        // Make sure stat changes remain the same and target gets curse effect
        battle.attackingFight(AttackNamesies.CURSE);
        Assert.assertTrue(attacking.getStages().getStage(Stat.ATTACK) == 1);
        Assert.assertTrue(attacking.getStages().getStage(Stat.DEFENSE) == 1);
        Assert.assertTrue(attacking.getStages().getStage(Stat.SPEED) == -1);
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.CURSE));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.CURSE));
        attacking.assertHealthRatio(.5);
        defending.assertHealthRatio(.75);
    }

    // Used for attacks that have a random element to them -- like Tri-Attack and Acupressure -- required running several times
    @Test
    public void randomEffectsTest() {
        boolean triAttackAlwaysSame = true;
        Map<StatusNamesies, Boolean> triAttackStatusMap = new EnumMap<>(StatusNamesies.class);
        triAttackStatusMap.put(StatusNamesies.NO_STATUS, false);
        triAttackStatusMap.put(StatusNamesies.PARALYZED, false);
        triAttackStatusMap.put(StatusNamesies.BURNED, false);
        triAttackStatusMap.put(StatusNamesies.FROZEN, false);

        boolean acupressureAlwaysSame = true;
        boolean[] acupressureStats = new boolean[Stat.NUM_BATTLE_STATS];

        for (int i = 0; i < 1000; i++) {
            TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
            TestPokemon attacking = battle.getAttacking();
            TestPokemon defending = battle.getDefending();

            // Tri-Attack
            battle.fight(AttackNamesies.TRI_ATTACK, AttackNamesies.TRI_ATTACK);

            StatusNamesies attackingCondition = attacking.getStatus().namesies();
            StatusNamesies defendingCondition = defending.getStatus().namesies();

            Assert.assertTrue(triAttackStatusMap.containsKey(attackingCondition));
            Assert.assertTrue(triAttackStatusMap.containsKey(defendingCondition));

            triAttackStatusMap.put(attackingCondition, true);
            triAttackStatusMap.put(defendingCondition, true);

            if (attackingCondition != defendingCondition) {
                triAttackAlwaysSame = false;
            }

            battle.emptyHeal();

            // Acupressure
            battle.fight(AttackNamesies.ACUPRESSURE, AttackNamesies.ACUPRESSURE);

            boolean foundAttacking = false;
            boolean foundDefending = false;
            for (Stat stat : Stat.BATTLE_STATS) {
                int attackingStage = attacking.getStage(stat);
                int defendingStage = defending.getStage(stat);
                if (attackingStage != defendingStage) {
                    acupressureAlwaysSame = false;
                }

                int index = stat.index();
                if (attackingStage > 0) {
                    Assert.assertFalse(foundAttacking);
                    acupressureStats[index] = true;
                    foundAttacking = true;
                }

                if (defendingStage > 0) {
                    Assert.assertFalse(foundDefending);
                    acupressureStats[index] = true;
                    foundDefending = true;
                }
            }
            Assert.assertTrue(foundAttacking);
            Assert.assertTrue(foundDefending);
        }

        Assert.assertFalse(triAttackAlwaysSame);
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.NO_STATUS));
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.PARALYZED));
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.BURNED));
        Assert.assertTrue(triAttackStatusMap.get(StatusNamesies.FROZEN));

        Assert.assertFalse(acupressureAlwaysSame);
        for (int j = 0; j < acupressureStats.length; j++) {
            Assert.assertTrue(Stat.getStat(j, true).getName(), acupressureStats[j]);
        }
    }

    @Test
    public void roostTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DRAGONITE, PokemonNamesies.MAGIKARP);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Should fail with full hp
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        attacking.apply(false, AttackNamesies.ROOST, battle);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));

        // Reduce health and apply again
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        attacking.assertNotFullHealth();
        attacking.apply(true, AttackNamesies.ROOST, battle);
        Assert.assertFalse(attacking.isType(battle, Type.FLYING));
        attacking.assertFullHealth();

        // Should fail because attack is already maxed -- flying type should come back at the end of the turn
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        attacking.assertFullHealth();
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));

        // Clear stat changes and reduce again
        Assert.assertTrue(attacking.getStage(Stat.ATTACK) == Stat.MAX_STAT_CHANGES);
        battle.attackingFight(AttackNamesies.HAZE);
        Assert.assertTrue(attacking.getStage(Stat.ATTACK) == 0);
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertTrue(attacking.getStage(Stat.ATTACK) == Stat.MAX_STAT_CHANGES);

        // Using a full turn should bring the flying type back at the end
        attacking.assertNotFullHealth();
        battle.attackingFight(AttackNamesies.ROOST);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        attacking.assertFullHealth();

        defending.apply(false, AttackNamesies.MUD_SLAP, battle);
        defending.apply(true, AttackNamesies.TACKLE, battle);
        attacking.assertNotFullHealth();
        attacking.apply(true, AttackNamesies.ROOST, battle);
        attacking.assertFullHealth();
        defending.apply(true, AttackNamesies.MUD_SLAP, battle);
        attacking.assertNotFullHealth();
    }

    @Test
    public void lastResortTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withMoves(AttackNamesies.LAST_RESORT);
        battle.getDefending().withMoves(AttackNamesies.SPLASH, AttackNamesies.ENDURE);

        // Should work if it is the only move the pokemon knows
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);

        // Should fail if multiple moves and hasn't used all of them yet
        battle.emptyHeal();
        attacking.withMoves(AttackNamesies.TACKLE, AttackNamesies.LAST_RESORT);
        attacking.apply(false, AttackNamesies.LAST_RESORT, battle);

        // Use the other move and then it should work
        Move tackle = attacking.getMoves(battle).get(0);
        attacking.setMove(tackle);
        Assert.assertFalse(tackle.used());
        battle.fight();
        Assert.assertTrue(tackle.used());
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);
    }

    @Test
    public void psychoShiftTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.MAGIC_GUARD);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.MAGIC_GUARD);

        battle.fight(AttackNamesies.SOAK, AttackNamesies.SOAK);
        Assert.assertTrue(attacking.isType(battle, Type.WATER));
        Assert.assertTrue(defending.isType(battle, Type.WATER));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BURNED));
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BURNED));
        Assert.assertFalse(defending.hasStatus());

        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BURNED));
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));

        battle.attackingFight(AttackNamesies.REFRESH);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.POISONED));
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BADLY_POISONED));
        Assert.assertFalse(defending.hasStatus());

        attacking.withAbility(AbilityNamesies.PROTEAN);
        Assert.assertTrue(attacking.isType(battle, Type.WATER));
        battle.attackingFight(AttackNamesies.CLEAR_SMOG);
        Assert.assertTrue(attacking.isType(battle, Type.POISON));
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.POISONED));
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BADLY_POISONED));
        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        Assert.assertTrue(attacking.isType(battle, Type.PSYCHIC));
        battle.attackingFight(AttackNamesies.ACID_ARMOR);
        Assert.assertTrue(attacking.isType(battle, Type.POISON));
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);

        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.PROTEAN));
        battle.attackingFight(AttackNamesies.DRAGON_DANCE);
        Assert.assertTrue(attacking.getType(battle).toString(), attacking.isType(battle, Type.DRAGON));
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.POISONED));

        defending.withAbility(AbilityNamesies.IMMUNITY);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.POISONED));
        Assert.assertFalse(defending.hasStatus());

        battle.emptyHeal();
        attacking.withMoves(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        battle.defendingFight(AttackNamesies.SPORE);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.ASLEEP));
        Assert.assertFalse(defending.hasStatus());
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusNamesies.ASLEEP));

        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.ASLEEP));
        Assert.assertFalse(defending.hasStatus());

        defending.withAbility(AbilityNamesies.INSOMNIA);
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.ASLEEP));
        Assert.assertFalse(defending.hasStatus());
    }

    @Test
    public void powderMoveTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Powder moves shouldn't work on Grass-type Pokemon
        Assert.assertTrue(attacking.isType(battle, Type.GRASS));
        defending.apply(false, AttackNamesies.SLEEP_POWDER, battle);
        attacking.apply(true, AttackNamesies.SLEEP_POWDER, battle);

        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.SOAK);
        Assert.assertFalse(attacking.isType(battle, Type.GRASS));
        defending.apply(true, AttackNamesies.SLEEP_POWDER, battle);

        // Or Pokemon with Overcoat
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.OVERCOAT);
        attacking.apply(false, AttackNamesies.SLEEP_POWDER, battle);
        attacking.apply(false, AttackNamesies.COTTON_SPORE, battle);
        attacking.apply(true, AttackNamesies.LEECH_SEED, battle);

        // Or Pokemon holding Safety Goggles
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.NO_ABILITY);
        defending.giveItem(ItemNamesies.SAFETY_GOGGLES);
        attacking.apply(false, AttackNamesies.SPORE, battle);
        battle.emptyHeal();
        attacking.apply(false, AttackNamesies.POISON_POWDER, battle);
        attacking.apply(true, AttackNamesies.VINE_WHIP, battle);
    }

    @Test
    public void bugBiteTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DRAGONITE, PokemonNamesies.DRAGONITE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.giveItem(ItemNamesies.RAWST_BERRY);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertTrue(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BURNED));

        battle.fight(AttackNamesies.ENDURE, AttackNamesies.BUG_BITE);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        battle.attackingFight(AttackNamesies.RECYCLE);
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.RAWST_BERRY));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        battle.defendingFight(AttackNamesies.POISON_POWDER);
        Assert.assertTrue(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertTrue(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());

        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertTrue(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus());

        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus());
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
    }

    @Test
    public void powerChangeTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Acrobatics has double power when not holding an item
        attacking.setupMove(AttackNamesies.ACROBATICS, battle);
        TestUtils.assertEquals(2, battle.getDamageModifier(attacking, defending));
        attacking.giveItem(ItemNamesies.POTION);
        TestUtils.assertEquals(1, battle.getDamageModifier(attacking, defending));

        // Body Slam -- doubles when the opponent uses Minimize
        attacking.setupMove(AttackNamesies.BODY_SLAM, battle);
        TestUtils.assertEquals(1, battle.getDamageModifier(attacking, defending));
        defending.apply(true, AttackNamesies.MINIMIZE, battle);
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.USED_MINIMIZE));
        TestUtils.assertEquals(2, battle.getDamageModifier(attacking, defending));
    }

    @Test
    public void moveCountTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.SHUCKLE);

        assertModifier(1, AttackNamesies.ROLLOUT, battle);
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(3, AttackNamesies.ROLLOUT, battle);
        assertModifier(4, AttackNamesies.ROLLOUT, battle);
        assertModifier(5, AttackNamesies.ROLLOUT, battle);

        // Max is 5
        assertModifier(5, AttackNamesies.ROLLOUT, battle);
        assertModifier(5, AttackNamesies.ROLLOUT, battle);

        // Other count-based moves (obviously) do not stack
        assertModifier(1, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(1, AttackNamesies.ICE_BALL, battle);
        assertModifier(1, AttackNamesies.ROLLOUT, battle);

        // Defense Curl increases multiplier by 2 for Rollout and Ice Ball (but not for Fury Cutter)
        assertModifier(1, AttackNamesies.DEFENSE_CURL, battle);
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(4, AttackNamesies.ROLLOUT, battle);
        assertModifier(6, AttackNamesies.ROLLOUT, battle);
        assertModifier(8, AttackNamesies.ROLLOUT, battle);
        assertModifier(10, AttackNamesies.ROLLOUT, battle);
        assertModifier(10, AttackNamesies.ROLLOUT, battle);
        assertModifier(1, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(2, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(3, AttackNamesies.FURY_CUTTER, battle);

        // Minimize does nothing (not sure why I included it)
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(1, AttackNamesies.MINIMIZE, battle);
        assertModifier(1, AttackNamesies.FURY_CUTTER, battle);
        assertModifier(2, AttackNamesies.ROLLOUT, battle);
        assertModifier(2, AttackNamesies.ICE_BALL, battle);
        assertModifier(4, AttackNamesies.ICE_BALL, battle);
    }

    private void assertModifier(double expectedModifier, AttackNamesies attack, TestBattle battle) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.fullyHeal();
        battle.setExpectedDamageModifier(expectedModifier);
        battle.attackingFight(attack);
        TestUtils.assertEquals(expectedModifier, battle.getDamageModifier(attacking, defending));
    }

    @Test
    public void evasionRemovalTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.DOUBLE_TEAM, AttackNamesies.MINIMIZE);
        Assert.assertEquals(1, attacking.getStage(Stat.EVASION));
        Assert.assertEquals(2, defending.getStage(Stat.EVASION));

        battle.attackingFight(AttackNamesies.FORESIGHT);
        Assert.assertEquals(1, attacking.getStage(Stat.EVASION));
        Assert.assertEquals(0, defending.getStage(Stat.EVASION));

        battle.defendingFight(AttackNamesies.MIRACLE_EYE);
        Assert.assertEquals(0, attacking.getStage(Stat.EVASION));
        Assert.assertEquals(0, defending.getStage(Stat.EVASION));
    }

    @Test
    public void changeAbilityTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.OVERGROW);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.BLAZE);

        battle.attackingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.BLAZE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.OVERGROW));

        battle.attackingFight(AttackNamesies.WORRY_SEED);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.BLAZE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.INSOMNIA));

        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.INSOMNIA));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.BLAZE));

        battle.attackingFight(AttackNamesies.ENTRAINMENT);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.INSOMNIA));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.INSOMNIA));

        battle.attackingFight(AttackNamesies.SIMPLE_BEAM);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.INSOMNIA));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.SIMPLE));

        battle.attackingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.INSOMNIA));

        battle.defendingFight(AttackNamesies.ROLE_PLAY);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.SIMPLE));

        battle.defendingFight(AttackNamesies.GASTRO_ACID);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.NO_ABILITY));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.SIMPLE));

        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.NO_ABILITY));
    }

    @Test
    public void rapidSpinDefogTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.GRIP_CLAW);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.MAGIC_GUARD).withItem(ItemNamesies.LIGHT_CLAY);
        Team defendingTeam = battle.getOpponent();

        // Add effects
        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        battle.attackingFight(AttackNamesies.TOXIC_SPIKES);
        battle.attackingFight(AttackNamesies.SPIKES);
        battle.attackingFight(AttackNamesies.LEECH_SEED); // Rapid Spin only
        battle.attackingFight(AttackNamesies.WRAP); // Rapid Spin only
        battle.defendingFight(AttackNamesies.LIGHT_SCREEN); // Defog only
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.LIGHT_SCREEN));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.WRAPPED));

        // Make sure effects persist
        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.CONSTRICT);
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.LIGHT_SCREEN));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.WRAPPED));

        // Use Rapid Spin -- should remove the appropriate effects
        battle.defendingFight(AttackNamesies.RAPID_SPIN);
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.LIGHT_SCREEN));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.STEALTH_ROCK));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.SPIKES));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.LEECH_SEED));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.WRAPPED));

        // Add effects back
        battle.emptyHeal();
        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        battle.attackingFight(AttackNamesies.TOXIC_SPIKES);
        battle.attackingFight(AttackNamesies.SPIKES);
        battle.attackingFight(AttackNamesies.LEECH_SEED);
        battle.attackingFight(AttackNamesies.WRAP);
        battle.defendingFight(AttackNamesies.LIGHT_SCREEN);
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.LIGHT_SCREEN));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.WRAPPED));

        // Wrong attacker -- effects shouldn't change
        battle.attackingFight(AttackNamesies.RAPID_SPIN);
        battle.defendingFight(AttackNamesies.DEFOG);
        Assert.assertEquals(-1, attacking.getStage(Stat.EVASION));
        Assert.assertEquals(0, defending.getStage(Stat.EVASION));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.LIGHT_SCREEN));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(TeamEffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.WRAPPED));

        // Correct defog attacker -- should only remove the appropriate effects
        battle.attackingFight(AttackNamesies.DEFOG);
        Assert.assertEquals(-1, attacking.getStage(Stat.EVASION));
        Assert.assertEquals(-1, defending.getStage(Stat.EVASION));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.LIGHT_SCREEN));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.STEALTH_ROCK));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
        Assert.assertFalse(defendingTeam.hasEffect(TeamEffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.WRAPPED));
    }

    @Test
    public void restTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.defendingFight(AttackNamesies.FALSE_SWIPE);
        battle.defendingFight(AttackNamesies.TOXIC);

        attacking.assertNotFullHealth();
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BADLY_POISONED));

        battle.attackingFight(AttackNamesies.REST);
        attacking.assertFullHealth();
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.ASLEEP));

        // Resting Pokemon should be asleep for exactly two turns -- False Swipe should fail here and the next turn
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        defending.assertFullHealth();
        attacking.assertFullHealth();
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.ASLEEP));

        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        defending.assertFullHealth();
        attacking.assertFullHealth();
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.ASLEEP));

        // Should wake up on this turn
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        defending.assertNotFullHealth();
        attacking.assertFullHealth();
        Assert.assertFalse(attacking.hasStatus());
    }

    @Test
    public void sketchTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();

        // Should originally fail since no move to copy
        attacking.apply(false, AttackNamesies.SKETCH, battle);
        attacking.withMoves(AttackNamesies.SKETCH);
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SKETCH));
        attacking.apply(false, AttackNamesies.SKETCH, battle);

        battle.defendingFight(AttackNamesies.SPLASH);
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SKETCH));
        attacking.apply(true, AttackNamesies.SKETCH, battle);
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SKETCH));
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SPLASH));

        // Should fail since the user doesn't actual know Sketch
        battle.defendingFight(AttackNamesies.SWORDS_DANCE);
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SKETCH));
        attacking.apply(false, AttackNamesies.SKETCH, battle);
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SKETCH));
        Assert.assertFalse(attacking.hasActualMove(AttackNamesies.SWORDS_DANCE));
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SPLASH));

        // Should fail if the user already knows the sketchy moves
        attacking.withMoves(AttackNamesies.SKETCH, AttackNamesies.GROWL);
        battle.defendingFight(AttackNamesies.GROWL);
        attacking.apply(false, AttackNamesies.SKETCH, battle);
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.SKETCH));
        Assert.assertTrue(attacking.hasActualMove(AttackNamesies.GROWL));
    }

    @Test
    public void fellStingerTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.KARTANA, PokemonNamesies.HAPPINY);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.NO_ABILITY); // Not Beast Boost
        TestPokemon defending = battle.getDefending();

        // Stage should not change without MURDER
        battle.fight(AttackNamesies.FELL_STINGER, AttackNamesies.ENDURE);
        Assert.assertFalse(defending.isActuallyDead());
        new TestStages().test(attacking);
        new TestStages().test(defending);

        // Kill kill kill MURDER MURDER MURDER
        battle.attackingFight(AttackNamesies.FELL_STINGER);
        Assert.assertTrue(defending.isActuallyDead());
        new TestStages().set(Stat.ATTACK, 3).test(attacking);
    }

    @Test
    public void stageSwapTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        new TestStages().test(attacking);
        new TestStages().test(defending);

        // Screech is -2 defense to opponent, Swords Dance is +2 attack for use
        battle.fight(AttackNamesies.SCREECH, AttackNamesies.SWORDS_DANCE);
        new TestStages().test(attacking);
        new TestStages().set(Stat.DEFENSE, -2)
                        .set(Stat.ATTACK, 2)
                        .test(defending);

        // Swaps attacking stats
        battle.attackingFight(AttackNamesies.POWER_SWAP);
        new TestStages().set(Stat.ATTACK, 2)
                        .test(attacking);
        new TestStages().set(Stat.DEFENSE, -2)
                        .test(defending);

        battle.attackingFight(AttackNamesies.POWER_SWAP);
        new TestStages().test(attacking);
        new TestStages().set(Stat.DEFENSE, -2)
                        .set(Stat.ATTACK, 2)
                        .test(defending);

        // Does the same exact thing regardless of attacker
        battle.defendingFight(AttackNamesies.POWER_SWAP);
        new TestStages().set(Stat.ATTACK, 2)
                        .test(attacking);
        new TestStages().set(Stat.DEFENSE, -2)
                        .test(defending);

        // Swaps all stats
        battle.defendingFight(AttackNamesies.HEART_SWAP);
        new TestStages().set(Stat.DEFENSE, -2)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .test(defending);

        // Quiver Dance increases Sp. Attack, Sp. Defense, and Speed by 1 for user,
        // Sand Attack decreases opponent Accuracy by 1
        battle.fight(AttackNamesies.QUIVER_DANCE, AttackNamesies.SAND_ATTACK);
        new TestStages().set(Stat.DEFENSE, -2)
                        .set(Stat.SP_ATTACK, 1)
                        .set(Stat.SP_DEFENSE, 1)
                        .set(Stat.SPEED, 1)
                        .set(Stat.ACCURACY, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .test(defending);

        // Swaps defensive stats
        battle.attackingFight(AttackNamesies.GUARD_SWAP);
        new TestStages().set(Stat.SP_ATTACK, 1)
                        .set(Stat.SPEED, 1)
                        .set(Stat.ACCURACY, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, 1)
                        .test(defending);

        // Calm Mind increases Sp. Attack and Sp. Defense by 1 for the user
        battle.fight(AttackNamesies.CALM_MIND, AttackNamesies.CALM_MIND);
        new TestStages().set(Stat.SP_ATTACK, 2)
                        .set(Stat.SP_DEFENSE, 1)
                        .set(Stat.SPEED, 1)
                        .set(Stat.ACCURACY, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_ATTACK, 1)
                        .set(Stat.SP_DEFENSE, 2)
                        .test(defending);

        battle.attackingFight(AttackNamesies.GUARD_SWAP);
        new TestStages().set(Stat.SP_ATTACK, 2)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, 2)
                        .set(Stat.SPEED, 1)
                        .set(Stat.ACCURACY, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 1)
                        .set(Stat.SP_DEFENSE, 1)
                        .test(defending);

        // Decrease defending speed by 2, then swap speeds
        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.SPEED_SWAP);
        new TestStages().set(Stat.SP_ATTACK, 2)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, 2)
                        .set(Stat.SPEED, -2)
                        .set(Stat.ACCURACY, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 1)
                        .set(Stat.SP_DEFENSE, 1)
                        .set(Stat.SPEED, 1)
                        .test(defending);

        // Just for the hell of it
        battle.defendingFight(AttackNamesies.HEART_SWAP);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 1)
                        .set(Stat.SP_DEFENSE, 1)
                        .set(Stat.SPEED, 1)
                        .test(attacking);
        new TestStages().set(Stat.SP_ATTACK, 2)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, 2)
                        .set(Stat.SPEED, -2)
                        .set(Stat.ACCURACY, -1)
                        .test(defending);

        battle.defendingFight(AttackNamesies.POWER_SWAP);
        new TestStages().set(Stat.SP_ATTACK, 2)
                        .set(Stat.SP_DEFENSE, 1)
                        .set(Stat.SPEED, 1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 1)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, 2)
                        .set(Stat.SPEED, -2)
                        .set(Stat.ACCURACY, -1)
                        .test(defending);
    }

    @Test
    public void spectralThiefTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.PROTEAN);

        new TestStages().test(attacking);
        new TestStages().test(defending);

        battle.defendingFight(AttackNamesies.SHELL_SMASH);
        Assert.assertTrue(defending.isType(battle, Type.NORMAL));
        new TestStages().test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .set(Stat.DEFENSE, -1)
                        .set(Stat.SP_DEFENSE, -1)
                        .test(defending);

        // Should fail since target it normal-type -- make sure it didn't steal stats
        Assert.assertTrue(attacking.lastMoveSucceeded());
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        Assert.assertFalse(attacking.lastMoveSucceeded());
        new TestStages().test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .set(Stat.DEFENSE, -1)
                        .set(Stat.SP_DEFENSE, -1)
                        .test(defending);

        defending.withAbility(AbilityNamesies.STURDY);
        battle.fight(AttackNamesies.SOAK, AttackNamesies.GROWL);
        Assert.assertFalse(defending.isType(battle, Type.NORMAL));
        new TestStages().set(Stat.ATTACK, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .set(Stat.DEFENSE, -1)
                        .set(Stat.SP_DEFENSE, -1)
                        .test(defending);

        // Steal stat gains!
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        new TestStages().set(Stat.ATTACK, 1)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .test(attacking);
        new TestStages().set(Stat.DEFENSE, -1)
                        .set(Stat.SP_DEFENSE, -1)
                        .test(defending);

        battle.defendingFight(AttackNamesies.SHELL_SMASH);
        new TestStages().set(Stat.ATTACK, 1)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, -2)
                        .test(defending);

        battle.emptyHeal();

        // Contrary will give stat decreases instead of gains
        attacking.withAbility(AbilityNamesies.CONTRARY);
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        new TestStages().set(Stat.ATTACK, -1)
                        .test(attacking);
        new TestStages().set(Stat.DEFENSE, -2)
                        .set(Stat.SP_DEFENSE, -2)
                        .test(defending);

        battle.defendingFight(AttackNamesies.SHELL_SMASH);
        new TestStages().set(Stat.ATTACK, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .set(Stat.DEFENSE, -3)
                        .set(Stat.SP_DEFENSE, -3)
                        .test(defending);

        battle.emptyHeal();

        // Simple will double the gains!
        attacking.withAbility(AbilityNamesies.SIMPLE);
        battle.attackingFight(AttackNamesies.SPECTRAL_THIEF);
        new TestStages().set(Stat.ATTACK, 3)
                        .set(Stat.SP_ATTACK, 4)
                        .set(Stat.SPEED, 4)
                        .test(attacking);
        new TestStages().set(Stat.DEFENSE, -3)
                        .set(Stat.SP_DEFENSE, -3)
                        .test(defending);

        // TODO: Test Substitute
    }

    @Test
    public void storedPowerTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();

        battle.setExpectedDamageModifier(1.0);
        battle.attackingFight(AttackNamesies.STORED_POWER);

        battle.emptyHeal();
        battle.attackingFight(AttackNamesies.MINIMIZE);
        new TestStages().set(Stat.EVASION, 2).test(attacking);

        battle.setExpectedDamageModifier(3.0);
        battle.attackingFight(AttackNamesies.STORED_POWER);

        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.SCREECH);
        new TestStages().set(Stat.EVASION, 2).set(Stat.DEFENSE, -2).test(attacking);

        // Stored power ignores negative stat gains
        battle.setExpectedDamageModifier(3.0);
        battle.attackingFight(AttackNamesies.STORED_POWER);
    }

    @Test
    public void naturalGiftTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.SYLVEON);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Fails if not holding anything
        attacking.apply(false, AttackNamesies.NATURAL_GIFT, battle);
        defending.assertFullHealth();

        // Non-berry items will fails
        attacking.withItem(ItemNamesies.ABSORB_BULB);
        attacking.apply(false, AttackNamesies.NATURAL_GIFT, battle);
        defending.assertFullHealth();
        Assert.assertTrue(attacking.isHoldingItem(battle));

        // Occa Berry (Fire-type) will succeed and be consumed
        attacking.withItem(ItemNamesies.OCCA_BERRY);
        attacking.apply(true, AttackNamesies.NATURAL_GIFT, battle);
        Assert.assertTrue(attacking.isAttackType(Type.FIRE));
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        attacking.assertFullHealth();
        defending.assertNotFullHealth();

        battle.emptyHeal();
        battle.clearAllEffects();
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        // Haban Berry (Dragon-type) will fail against Sylveon
        attacking.withItem(ItemNamesies.HABAN_BERRY);
        attacking.apply(false, AttackNamesies.NATURAL_GIFT, battle);
        Assert.assertTrue(attacking.isAttackType(Type.DRAGON));
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.HABAN_BERRY));

        // Oran Berry should not heal when consumed
        battle.falseSwipePalooza(false);
        Assert.assertEquals(1, attacking.getHP());
        attacking.withItem(ItemNamesies.ORAN_BERRY);
        attacking.apply(true, AttackNamesies.NATURAL_GIFT, battle);
        Assert.assertTrue(attacking.isAttackType(Type.POISON));
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        Assert.assertEquals(1, attacking.getHP());
        defending.assertNotFullHealth();
    }

    @Test
    public void incinerateTest() {
        incinerateTest(ItemNamesies.ABSORB_BULB, false);
        incinerateTest(ItemNamesies.ORAN_BERRY, true);
        incinerateTest(ItemNamesies.FIRE_GEM, true);

        // Will be incinerated if the Fire-type incinerate is not super-effective
        incinerateTest(ItemNamesies.OCCA_BERRY, true);

        // Special case for Occa Berry which will be consumed once hit with the super-effective Fire-type incinerate
        // Should have the Eaten Berry effect in this case
        TestBattle battle = TestBattle.create(PokemonNamesies.CHARMANDER, PokemonNamesies.BULBASAUR);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.withItem(ItemNamesies.OCCA_BERRY);
        battle.setExpectedDamageModifier(.5);
        battle.fight(AttackNamesies.INCINERATE, AttackNamesies.ENDURE);
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
        defending.assertNotFullHealth();
        attacking.assertFullHealth();
    }

    private void incinerateTest(ItemNamesies victimItem, boolean incinerated) {
        PokemonManipulator manipulator = (battle, attacking, defending) -> {
            // Make sure victim does not consume the effects of the berry when eaten (in this example Oran Berry)
            // Use Substitute so Incinerate damage will be absorbed and Oran Berry will not be able to activate naturally before incinerate to death
            battle.defendingFight(AttackNamesies.SUBSTITUTE);
            defending.assertHealthRatio(.75);

            defending.withItem(victimItem);
            battle.attackingFight(AttackNamesies.INCINERATE);
        };

        PokemonManipulator sticksies = (battle, attacking, defending) -> {
            Assert.assertTrue(defending.hasAbility(AbilityNamesies.STICKY_HOLD));
            Assert.assertTrue(defending.isHoldingItem(battle));
            Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
            Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
            Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
            Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
            defending.assertHealthRatio(.75);
            attacking.assertFullHealth();
        };

        PokemonManipulator nonStick = (battle, attacking, defending) -> {
            Assert.assertNotEquals(incinerated, defending.isHoldingItem(battle));
            Assert.assertEquals(incinerated, defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
            Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
            Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
            Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
            defending.assertHealthRatio(.75);
            attacking.assertFullHealth();
        };

        new TestInfo(PokemonNamesies.XURKITREE, PokemonNamesies.IGGLYBUFF)
                .with(manipulator)
                .doubleTake(AbilityNamesies.STICKY_HOLD, nonStick, sticksies);

        PokemonManipulator moldBreaker = PokemonManipulator.giveAttackingAbility(AbilityNamesies.MOLD_BREAKER);
        new TestInfo(PokemonNamesies.XURKITREE, PokemonNamesies.IGGLYBUFF)
                .with(moldBreaker.add(manipulator))
                .doubleTake(AbilityNamesies.STICKY_HOLD, nonStick);
    }

    @Test
    public void hiddenPowerTest() {
        hiddenPowerTest(new int[] { 30, 31, 31, 30, 31, 31 }, Type.GRASS);
        hiddenPowerTest(new int[] { 30, 30, 30, 30, 30, 30 }, Type.FIGHTING);
        hiddenPowerTest(new int[] { 31, 31, 31, 31, 31, 31 }, Type.DARK);
        hiddenPowerTest(new int[] { 31, 31, 31, 30, 31, 31 }, Type.ELECTRIC);

        checkHiddenPower(new int[] { 14, 11, 13, 12, 19, 15 }, Type.GRASS);
        checkHiddenPower(new int[] { 31, 31, 30, 30, 30, 30 }, Type.FIGHTING);

        // All even IVs is Fighting-type Hidden Power
        // All odd IVs is Dark-type Hidden Power
        int[] evenIVs = new int[Stat.NUM_STATS];
        int[] oddIVs = new int[Stat.NUM_STATS];
        Arrays.fill(oddIVs, 1);
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            while (oddIVs[i] < IndividualValues.MAX_IV) {
                evenIVs[i] += 2;
                checkHiddenPower(evenIVs, Type.FIGHTING);

                oddIVs[i] += 2;
                checkHiddenPower(oddIVs, Type.DARK);
            }
        }
    }

    private void checkHiddenPower(int[] IVs, Type hiddenType) {
        TestPokemon pokemon = TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR).withIVs(IVs);
        Assert.assertEquals(hiddenType, pokemon.computeHiddenPowerType());
    }

    private void hiddenPowerTest(int[] IVs, Type hiddenType) {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.SANDSHREW);
        TestPokemon attacking = battle.getAttacking().withIVs(IVs);
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ENDURE);
        Assert.assertTrue(Arrays.toString(IVs) + " " + attacking.getAttackType(), attacking.isAttackType(hiddenType));
        Assert.assertEquals(hiddenType == Type.ELECTRIC, defending.fullHealth());

        battle.emptyHeal();

        // Electrify has priority so it will go first and will change Hidden Power's type to be electric
        // Will then fail against Sandshrew
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ELECTRIFY);
        Assert.assertTrue(attacking.isAttackType(Type.ELECTRIC));
        defending.assertFullHealth();

        // Using again will be back to hidden type
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ENDURE);
        Assert.assertTrue(attacking.isAttackType(hiddenType));
        Assert.assertEquals(hiddenType == Type.ELECTRIC, defending.fullHealth());

        battle.emptyHeal();

        // Normalize makes Hidden Power Normal-type
        attacking.withAbility(AbilityNamesies.NORMALIZE);
        battle.setExpectedDamageModifier(1.2);
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ENDURE);
        Assert.assertTrue(attacking.isAttackType(Type.NORMAL));
        defending.assertNotFullHealth();

        battle.emptyHeal();

        // Electrify overrides Normalize
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ELECTRIFY);
        Assert.assertTrue(attacking.isAttackType(Type.ELECTRIC));
        defending.assertFullHealth();

        // Double check this override when it is actually effective to make sure Normalize isn't being activated for the 20% boost
        // Yes I realize this here doesn't have much to do with Hidden Power fucking sue me
        battle.attackingFight(AttackNamesies.SOAK);
        Assert.assertTrue(defending.isType(battle, Type.WATER));
        Assert.assertFalse(defending.isType(battle, Type.ELECTRIC));
        battle.setExpectedDamageModifier(1.0);
        battle.fight(AttackNamesies.HIDDEN_POWER, AttackNamesies.ELECTRIFY);
        Assert.assertTrue(attacking.isAttackType(Type.ELECTRIC));
        defending.assertNotFullHealth();
    }

    @Test
    public void powderTest() {

        // Just make sure it works on a non-Grass type
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.SQUIRTLE),
                (battle, attacking, defending) -> Assert.assertTrue(defending.lastMoveSucceeded())
        );

        // Powder doesn't work on Grass-type Pokemon
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.BULBASAUR),
                (battle, attacking, defending) -> Assert.assertFalse(defending.lastMoveSucceeded())
        );

        // Powder doesn't work on Pokemon with Overcoat
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.SQUIRTLE, AbilityNamesies.OVERCOAT),
                (battle, attacking, defending) -> Assert.assertFalse(defending.lastMoveSucceeded())
        );

        // Powder doesn't work on Pokemon with Safety Goggles
        powderTest(
                AttackNamesies.POWDER, false,
                new TestInfo().attacking(PokemonNamesies.SQUIRTLE, ItemNamesies.SAFETY_GOGGLES),
                (battle, attacking, defending) -> Assert.assertFalse(defending.lastMoveSucceeded())
        );

        // Powder only affects Fire-type moves
        powderTest(
                AttackNamesies.WATER_GUN, false, new TestInfo(),
                (battle, attacking, defending) -> attacking.assertNotFullHealth()
        );

        // Using a Fire-type move under the effects of Powder will decrease health by 25%
        powderTest(AttackNamesies.EMBER, true, new TestInfo(), PokemonManipulator.empty());

        // Even if it is a status move
        powderTest(AttackNamesies.WILL_O_WISP, true, new TestInfo(), PokemonManipulator.empty());

        // Fiery Dance will not trigger Dancer
        // Succeeding the .75 health ratio is the real test for this
        powderTest(
                AttackNamesies.FIERY_DANCE, true,
                new TestInfo().attacking(AbilityNamesies.DANCER),
                PokemonManipulator.empty()
        );

        // Natural Gift will fail if holding a Fire-type berry and the berry will not be consumed
        powderTest(
                AttackNamesies.NATURAL_GIFT, true,
                new TestInfo().defending(ItemNamesies.OCCA_BERRY),
                (battle, attacking, defending) -> {
                    Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.OCCA_BERRY));
                    Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
                    Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
                }
        );

        // Make sure health does not decrease when the user has Magic Guard, but user is still blocked from using the attack
        powderTest(
                AttackNamesies.EMBER, true,
                new TestInfo().defending(AbilityNamesies.MAGIC_GUARD),
                (battle, attacking, defending) -> {
                    // These are mostly checked already but just want to reinforce in this case
                    defending.assertFullHealth();
                    attacking.assertFullHealth();
                }
        );
    }

    private void powderTest(AttackNamesies attackNamesies, boolean explodes, TestInfo testInfo, PokemonManipulator afterCheck) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        testInfo.manipulate(battle);

        battle.fight(AttackNamesies.POWDER, attackNamesies);
        Assert.assertTrue(attacking.lastMoveSucceeded());

        defending.assertHealthRatio(explodes && !defending.hasAbility(AbilityNamesies.MAGIC_GUARD) ? .75 : 1);
        if (explodes) {
            Assert.assertFalse(defending.lastMoveSucceeded());
            attacking.assertFullHealth();
            Assert.assertFalse(attacking.hasStatus());
            new TestStages().test(attacking);
        }

        Assert.assertEquals(explodes, defending.isAttackType(Type.FIRE));

        // Neither will have the effect since it goes away at the end of the turn
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.POWDER));
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.POWDER));

        afterCheck.manipulate(battle);
    }
}
