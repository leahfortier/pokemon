package test.general;

import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.effect.Effect;
import battle.effect.EffectInterface;
import battle.effect.EffectInterfaces.ChoiceEffect;
import battle.effect.EffectInterfaces.EffectPreventionAbility;
import battle.effect.EffectInterfaces.EntryHazard;
import battle.effect.EffectInterfaces.MoldBreakerEffect;
import battle.effect.EffectInterfaces.MultipleEffectPreventionAbility;
import battle.effect.EffectInterfaces.PartialTrappingEffect;
import battle.effect.EffectInterfaces.PassableEffect;
import battle.effect.EffectInterfaces.SingleEffectPreventionAbility;
import battle.effect.EffectInterfaces.StatModifyingStatus;
import battle.effect.EffectInterfaces.SwappableEffect;
import battle.effect.InvokeInterfaces.AbsorbDamageEffect;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.InvokeInterfaces.AttackingNoAdvantageChanger;
import battle.effect.InvokeInterfaces.BarrierEffect;
import battle.effect.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.InvokeInterfaces.BeforeAttackPreventingEffect;
import battle.effect.InvokeInterfaces.BracingEffect;
import battle.effect.InvokeInterfaces.ChangeAttackTypeEffect;
import battle.effect.InvokeInterfaces.ChangeMoveListEffect;
import battle.effect.InvokeInterfaces.ChangeTypeEffect;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.DamageTakenEffect;
import battle.effect.InvokeInterfaces.DefendingNoAdvantageChanger;
import battle.effect.InvokeInterfaces.DifferentStatEffect;
import battle.effect.InvokeInterfaces.DoubleWeightEffect;
import battle.effect.InvokeInterfaces.EffectExtendingEffect;
import battle.effect.InvokeInterfaces.EffectPreventionEffect;
import battle.effect.InvokeInterfaces.EffectReceivedEffect;
import battle.effect.InvokeInterfaces.EncounterRateMultiplier;
import battle.effect.InvokeInterfaces.EndBattleEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.ForceMoveEffect;
import battle.effect.InvokeInterfaces.GroundedEffect;
import battle.effect.InvokeInterfaces.HalfWeightEffect;
import battle.effect.InvokeInterfaces.LevitationEffect;
import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.MurderEffect;
import battle.effect.InvokeInterfaces.NameChanger;
import battle.effect.InvokeInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.OpponentEndAttackEffect;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentPowerChangeEffect;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.InvokeInterfaces.OpponentStatusReceivedEffect;
import battle.effect.InvokeInterfaces.OpponentTakeDamageEffect;
import battle.effect.InvokeInterfaces.OpponentTrappingEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.PriorityChangeEffect;
import battle.effect.InvokeInterfaces.RapidSpinRelease;
import battle.effect.InvokeInterfaces.RepellingEffect;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.InvokeInterfaces.SleepyFightsterEffect;
import battle.effect.InvokeInterfaces.StageChangingEffect;
import battle.effect.InvokeInterfaces.StartAttackEffect;
import battle.effect.InvokeInterfaces.StatChangingEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import battle.effect.InvokeInterfaces.StatusBoosterEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.StatusReceivedEffect;
import battle.effect.InvokeInterfaces.SuperDuperEndTurnEffect;
import battle.effect.InvokeInterfaces.SwitchOutEffect;
import battle.effect.InvokeInterfaces.TakeDamageEffect;
import battle.effect.InvokeInterfaces.TargetSwapperEffect;
import battle.effect.InvokeInterfaces.TerrainCastEffect;
import battle.effect.InvokeInterfaces.TrappingEffect;
import battle.effect.InvokeInterfaces.WeatherBlockerEffect;
import battle.effect.InvokeInterfaces.WeatherChangedEffect;
import battle.effect.InvokeInterfaces.WeatherEliminatingEffect;
import battle.effect.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.InvokeInterfaces.WildEncounterSelector;
import battle.effect.attack.MultiTurnMove;
import battle.effect.attack.MultiTurnMove.ChargingMove;
import battle.effect.attack.MultiTurnMove.RechargingMove;
import battle.effect.battle.BattleEffect;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.ChangeAbilitySource;
import battle.effect.source.ChangeAttackTypeSource;
import battle.effect.source.ChangeTypeSource;
import battle.effect.status.StatusCondition;
import battle.effect.status.StatusInterface;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffect;
import item.Item;
import item.ItemInterface;
import item.ItemNamesies;
import item.hold.HoldItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pokemon.ability.Ability;
import pokemon.ability.AbilityInterface;
import pokemon.species.PokemonNamesies;
import save.Save;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import type.Type;
import util.GeneralUtils;
import util.file.FileIO;
import util.serialization.Serializable;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class ClassTest extends BaseTest {
    private static final List<Class<?>> effectListWithAttackClasses = Arrays.asList(
            ApplyDamageEffect.class,
            MurderEffect.class,
            SemiInvulnerableBypasser.class,
            BasicAccuracyBypassEffect.class,
            CritBlockerEffect.class,
            OpponentIgnoreStageEffect.class,
            OpponentStatSwitchingEffect.class,
            PowerChangeEffect.class,
            AlwaysCritEffect.class,
            SleepyFightsterEffect.class,
            SelfAttackBlocker.class,
            CritStageEffect.class,
            ForceMoveEffect.class,
            StatSwitchingEffect.class,
            StatusBoosterEffect.class
    );

    private static List<Class<?>> classes;

    @BeforeClass
    public static void setup() {
        try {
            classes = getClasses();
        } catch (ClassNotFoundException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void containsTest() {
        List<Class> toCheck = Arrays.asList(
                Attack.class,
                ClassTest.class,
                Save.class,
                StatusNamesies.class,
                Type.class,
                RapidSpinRelease.class,
                PokemonEffectNamesies.AQUA_RING.getEffect().getClass(),
                ItemNamesies.CELL_BATTERY.getItem().getClass(),
                Type.BUG.getDeclaringClass(),
                PokemonNamesies.class
        );

        for (Class checkeroo : toCheck) {
            Assert.assertTrue(checkeroo.getSimpleName(), classes.contains(checkeroo));
        }

        Assert.assertFalse(classes.contains(List.class));
    }

    @Test
    public void instanceOfTest() {
        Class<?>[] castSources = { Attack.class, Ability.class, HoldItem.class };
        Class<?>[] pokemonEffectNoBattleList = { Ability.class, HoldItem.class };
        Class<?>[] pokemonEffectList = GeneralUtils.append(pokemonEffectNoBattleList, StatusCondition.class, PokemonEffect.class);
        Class<?>[] teamEffectList = GeneralUtils.append(pokemonEffectList, TeamEffect.class);
        Class<?>[] effectListSourcesNoAttack = GeneralUtils.append(teamEffectList, BattleEffect.class);
        Class<?>[] effectListSourcesWithAttack = GeneralUtils.append(effectListSourcesNoAttack, Attack.class);
        for (Class<?> classy : classes) {
            checkInstance(classy, ItemInterface.class, Item.class);
            checkInstance(classy, AttackInterface.class, Attack.class);
            checkInstance(classy, AbilityInterface.class, Ability.class);
            checkInstance(classy, EffectInterface.class, Effect.class);

            checkInstance(classy, PassableEffect.class, PokemonEffect.class);
            checkInstance(classy, PartialTrappingEffect.class, PokemonEffect.class);
            checkInstance(classy, SwappableEffect.class, TeamEffect.class);
            checkInstance(classy, BarrierEffect.class, TeamEffect.class);
            checkInstance(classy, EntryHazard.class, TeamEffect.class);
            checkInstance(classy, BattleEndTurnEffect.class, BattleEffect.class);
            checkInstance(classy, EndTurnEffect.class, teamEffectList);
            checkInstance(classy, SwitchOutEffect.class, pokemonEffectList);
            checkInstance(classy, EndBattleEffect.class, teamEffectList);
            checkInstance(classy, WeatherEliminatingEffect.class, teamEffectList);
            checkInstance(classy, WeatherChangedEffect.class, teamEffectList);
            checkInstance(classy, TerrainCastEffect.class, teamEffectList);
            checkInstance(classy, EffectReceivedEffect.class, teamEffectList);

            // Teams and Opponent things
            checkInstance(classy, Team.class, Trainer.class, WildPokemon.class);
            checkInstance(classy, Opponent.class, EnemyTrainer.class, WildPokemon.class);

            // MultiTurnMove should not be directly inherited
            checkInstance(classy, MultiTurnMove.class, ChargingMove.class, RechargingMove.class);

            // EffectPreventionAbility should be single or multiple
            checkInstance(classy, EffectPreventionAbility.class, SingleEffectPreventionAbility.class, MultipleEffectPreventionAbility.class);

            // NameChanger only looks at the ability
            checkInstance(classy, NameChanger.class, Ability.class);

            // Mold Breaker effects are only checked on the ability and the attack
            checkInstance(classy, MoldBreakerEffect.class, Ability.class, Attack.class);

            // ChoiceEffect only works with named sources
            checkInstance(classy, ChoiceEffect.class, Ability.class, Item.class);

            // Casted from CastSource.getSource()
            checkInstance(classy, ChangeAbilitySource.class, castSources);
            checkInstance(classy, ChangeAttackTypeSource.class, castSources);
            checkInstance(classy, ChangeTypeSource.class, castSources);

            // Pokemon effects outside of battle
            checkInstance(classy, WildEncounterAlterer.class, pokemonEffectNoBattleList);
            checkInstance(classy, WildEncounterSelector.class, pokemonEffectNoBattleList);
            checkInstance(classy, RepellingEffect.class, pokemonEffectNoBattleList);
            checkInstance(classy, EncounterRateMultiplier.class, pokemonEffectNoBattleList);

            // Invoked from battle.getEffectsList() without attack
            checkInstance(classy, OpponentApplyDamageEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, SuperDuperEndTurnEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, TakeDamageEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentTakeDamageEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, EntryEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatLoweredEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, LevitationEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, GroundedEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentAccuracyBypassEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, AttackSelectionEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, WeatherBlockerEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, TrappingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentTrappingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, BeforeAttackPreventingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StartAttackEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, EffectPreventionEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, TargetSwapperEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatProtectingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatusPreventionEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, BracingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, ChangeTypeEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, DifferentStatEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, PriorityChangeEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, ChangeAttackTypeEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, AttackingNoAdvantageChanger.class, effectListSourcesNoAttack);
            checkInstance(classy, DefendingNoAdvantageChanger.class, effectListSourcesNoAttack);
            checkInstance(classy, ChangeMoveListEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, HalfWeightEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, DoubleWeightEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StageChangingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatModifyingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatChangingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentPowerChangeEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, AbsorbDamageEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, DamageTakenEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatusReceivedEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentStatusReceivedEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentEndAttackEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, AttackBlocker.class, effectListSourcesNoAttack);
            checkInstance(classy, ModifyStageValueEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, EffectExtendingEffect.class, effectListSourcesNoAttack);

            // Invoked from battle.getEffectsList() with attack
            for (Class<?> effectListWithAttackClass : effectListWithAttackClasses) {
                checkInstance(classy, effectListWithAttackClass, effectListSourcesWithAttack);
            }

            // If a status is a stat modifier, then it must be a StatModifyingStatus
            checkAllInstances(classy, StatModifyingStatus.class, StatusInterface.class, StatModifyingEffect.class);
        }
    }

    @Test
    public void containsInstanceTest() {
        // Invoked from battle.getEffectsList() with attack
        for (Class<?> effectListWithAttackClass : effectListWithAttackClasses) {
            containsInstance(effectListWithAttackClass, Attack.class);
        }
    }

    @Test
    public void annotationTest() {
        for (Class<?> classy : classes) {
            // All tests should inherit from BaseTest
            annotationImpliesInstance(classy, Test.class, BaseTest.class);

            // Should pretty much always be using @BeforeClass instead
            Assert.assertFalse(hasMethodAnnotation(classy, Before.class));
        }
    }

    @Test
    public void usedInterfaceTest() {
        for (Class<?> classy : classes) {
            // For all non-functional interfaces, make sure it has a least one object inheriting it
            // Otherwise it is unused and should be deleted
            if (classy.isInterface() && !hasAnnotation(classy, FunctionalInterface.class)) {
                containsInstance(classy, Object.class);
            }
        }
    }

    @Test
    public void serializableTest() {
        for (Class<?> classy : classes) {
            // Classes must have a serialVersionUID if and only if it is serializable
            if (!classy.isInterface()) {
                boolean isSerializable = Serializable.class.isAssignableFrom(classy);
                boolean hasSerialId = hasDeclaredField(classy, "serialVersionUID");
                Assert.assertEquals(classy.getName(), isSerializable, hasSerialId);
            }
        }
    }

    private boolean hasDeclaredField(Class<?> classy, String fieldName) {
        try {
            classy.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    // If toCheck is an instance of assigned (interface), then is MUST be an instance of at least one of implies (classes)
    private void checkInstance(Class<?> toCheck, Class<?> assigned, Class<?>... implies) {
        // assigned must be an interface
        Assert.assertTrue(assigned.isInterface());

        // Only check for class implementations
        if (!toCheck.isInterface() && assigned.isAssignableFrom(toCheck)) {
            String message = String.format(
                    "%s is an instance of %s but not in %s.",
                    toCheck.getSimpleName(),
                    assigned.getSimpleName(),
                    Arrays.stream(implies).map(Class::getSimpleName).collect(Collectors.toList())
            );
            Assert.assertTrue(message, isAnyInstance(toCheck, implies));
        }
    }

    // If toCheck is an instance of all assigned (interfaces), then is MUST be an instance of implies
    private void checkAllInstances(Class<?> toCheck, Class<?> implies, Class<?>... allAssigned) {
        // Only check for class implementations
        if (toCheck.isInterface()) {
            return;
        }

        if (isAllInstance(toCheck, allAssigned)) {
            String message = String.format(
                    "%s is an instance of each of %s but is not a %s.",
                    toCheck.getSimpleName(),
                    Arrays.stream(allAssigned).map(Class::getSimpleName).collect(Collectors.toList()),
                    implies.getSimpleName()
            );
            Assert.assertTrue(message, implies.isAssignableFrom(toCheck));
        }
    }

    // For all classes that implement assigned, at least one must be of type mustContain
    private void containsInstance(Class<?> assigned, Class<?> mustContain) {
        // assigned must be in interface
        Assert.assertTrue(assigned.isInterface());

        boolean contains = false;
        for (Class<?> classy : classes) {
            if (classy.equals(assigned)) {
                continue;
            }

            if (assigned.isAssignableFrom(classy) && mustContain.isAssignableFrom(classy)) {
                contains = true;
                break;
            }
        }

        String message = String.format(
                "%s does not have required instance of %s.",
                assigned.getSimpleName(),
                mustContain.getSimpleName()
        );
        Assert.assertTrue(message, contains);
    }

    // If a class has the annotationClass on any method, then it must be an instance of at least one instances
    private void annotationImpliesInstance(Class<?> toCheck, Class<? extends Annotation> annotationClass, Class<?>... instances) {
        if (hasMethodAnnotation(toCheck, annotationClass)) {
            String message = String.format(
                    "%s has %s annotation but is not in %s.",
                    toCheck.getSimpleName(),
                    annotationClass.getSimpleName(),
                    Arrays.stream(instances).map(Class::getSimpleName).collect(Collectors.toList())
            );
            Assert.assertTrue(message, isAnyInstance(toCheck, instances));
        }
    }

    // Returns true if toCheck has the annotationClass annotation on itself
    private boolean hasAnnotation(Class<?> toCheck, Class<? extends Annotation> annotationClass) {
        Annotation[] annotations = toCheck.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    // Returns true if toCheck has the annotationClass annotation on ANY of its methods
    private boolean hasMethodAnnotation(Class<?> toCheck, Class<? extends Annotation> annotationClass) {
        Method[] methods = toCheck.getMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Returns true if toCheck is an instance of at least one of instances
    private boolean isAnyInstance(Class<?> toCheck, Class<?>... instances) {
        for (Class<?> instance : instances) {
            if (instance.isAssignableFrom(toCheck)) {
                return true;
            }
        }
        return false;
    }

    // Returns true if toCheck is an instance of at all of instances
    private boolean isAllInstance(Class<?> toCheck, Class<?>... instances) {
        for (Class<?> instance : instances) {
            if (!instance.isAssignableFrom(toCheck)) {
                return false;
            }
        }
        return true;
    }

    // Scans all classes accessible from the context class loader which belong to the given package and subpackages
    private static List<Class<?>> getClasses() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertNotNull(classLoader);

        String packageName = "";
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(FileIO.newFile(resource.getFile()));
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    // Recursive method used to find all classes in a given directory and subdirs.
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        Assert.assertNotNull(files);

        String nextPackagePrefix = packageName;
        if (!packageName.isEmpty()) {
            nextPackagePrefix += ".";
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // TODO: Python libraries are currently breaking this and I'll deal with it later
//                Assert.assertFalse(file.getAbsolutePath(), file.getName().contains("."));
                classes.addAll(findClasses(file, nextPackagePrefix + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(nextPackagePrefix + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }
}
