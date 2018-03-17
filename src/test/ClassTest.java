package test;

import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.effect.attack.MultiTurnMove;
import battle.effect.attack.MultiTurnMove.ChargingMove;
import battle.effect.attack.MultiTurnMove.RechargingMove;
import battle.effect.battle.BattleEffect;
import battle.effect.interfaces.EffectReleaser;
import battle.effect.interfaces.InvokeInterfaces.AbsorbDamageEffect;
import battle.effect.interfaces.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.interfaces.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.interfaces.InvokeInterfaces.AttackBlocker;
import battle.effect.interfaces.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.interfaces.InvokeInterfaces.AttackingNoAdvantageChanger;
import battle.effect.interfaces.InvokeInterfaces.BarrierEffect;
import battle.effect.interfaces.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.interfaces.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.BeforeTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.BracingEffect;
import battle.effect.interfaces.InvokeInterfaces.ChangeAttackTypeEffect;
import battle.effect.interfaces.InvokeInterfaces.ChangeMoveListEffect;
import battle.effect.interfaces.InvokeInterfaces.ChangeTypeEffect;
import battle.effect.interfaces.InvokeInterfaces.CritBlockerEffect;
import battle.effect.interfaces.InvokeInterfaces.CritStageEffect;
import battle.effect.interfaces.InvokeInterfaces.DamageTakenEffect;
import battle.effect.interfaces.InvokeInterfaces.DefendingNoAdvantageChanger;
import battle.effect.interfaces.InvokeInterfaces.DefogRelease;
import battle.effect.interfaces.InvokeInterfaces.DifferentStatEffect;
import battle.effect.interfaces.InvokeInterfaces.EffectBlockerEffect;
import battle.effect.interfaces.InvokeInterfaces.EncounterRateMultiplier;
import battle.effect.interfaces.InvokeInterfaces.EndBattleEffect;
import battle.effect.interfaces.InvokeInterfaces.EndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.EntryEffect;
import battle.effect.interfaces.InvokeInterfaces.ForceMoveEffect;
import battle.effect.interfaces.InvokeInterfaces.GroundedEffect;
import battle.effect.interfaces.InvokeInterfaces.HalfWeightEffect;
import battle.effect.interfaces.InvokeInterfaces.LevitationEffect;
import battle.effect.interfaces.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.interfaces.InvokeInterfaces.MurderEffect;
import battle.effect.interfaces.InvokeInterfaces.NameChanger;
import battle.effect.interfaces.InvokeInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentEndAttackEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentPowerChangeEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentStatusReceivedEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentTakeDamageEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentTrappingEffect;
import battle.effect.interfaces.InvokeInterfaces.PowerChangeEffect;
import battle.effect.interfaces.InvokeInterfaces.PriorityChangeEffect;
import battle.effect.interfaces.InvokeInterfaces.RapidSpinRelease;
import battle.effect.interfaces.InvokeInterfaces.RepellingEffect;
import battle.effect.interfaces.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.interfaces.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.interfaces.InvokeInterfaces.SleepyFightsterEffect;
import battle.effect.interfaces.InvokeInterfaces.StageChangingEffect;
import battle.effect.interfaces.InvokeInterfaces.StatChangingEffect;
import battle.effect.interfaces.InvokeInterfaces.StatLoweredEffect;
import battle.effect.interfaces.InvokeInterfaces.StatModifyingEffect;
import battle.effect.interfaces.InvokeInterfaces.StatProtectingEffect;
import battle.effect.interfaces.InvokeInterfaces.StatSwitchingEffect;
import battle.effect.interfaces.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.interfaces.InvokeInterfaces.StatusReceivedEffect;
import battle.effect.interfaces.InvokeInterfaces.SuperDuperEndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.SwitchOutEffect;
import battle.effect.interfaces.InvokeInterfaces.TakeDamageEffect;
import battle.effect.interfaces.InvokeInterfaces.TargetSwapperEffect;
import battle.effect.interfaces.InvokeInterfaces.TerrainCastEffect;
import battle.effect.interfaces.InvokeInterfaces.TrappingEffect;
import battle.effect.interfaces.InvokeInterfaces.WeatherBlockerEffect;
import battle.effect.interfaces.InvokeInterfaces.WeatherEliminatingEffect;
import battle.effect.interfaces.InvokeInterfaces.WeatherExtendingEffect;
import battle.effect.interfaces.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.interfaces.InvokeInterfaces.WildEncounterSelector;
import battle.effect.interfaces.PassableEffect;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.AbilityChanger;
import battle.effect.source.ChangeAttackTypeSource;
import battle.effect.source.ChangeTypeSource;
import battle.effect.status.StatusCondition;
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
import pokemon.species.PokemonNamesies;
import save.Save;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import type.Type;
import util.GeneralUtils;
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
            StatSwitchingEffect.class,
            OpponentStatSwitchingEffect.class,
            PowerChangeEffect.class,
            AlwaysCritEffect.class,
            SleepyFightsterEffect.class,
            SelfAttackBlocker.class,
            CritStageEffect.class,
            ForceMoveEffect.class
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
            checkInstance(classy, NameChanger.class, Ability.class);
            checkInstance(classy, PassableEffect.class, PokemonEffect.class);
            checkInstance(classy, BattleEndTurnEffect.class, BattleEffect.class);
            checkInstance(classy, EndTurnEffect.class, teamEffectList);
            checkInstance(classy, SwitchOutEffect.class, pokemonEffectList);
            checkInstance(classy, EndBattleEffect.class, teamEffectList);

            // Teams and Opponent things
            checkInstance(classy, Team.class, Trainer.class, WildPokemon.class);
            checkInstance(classy, Opponent.class, EnemyTrainer.class, WildPokemon.class);

            // Effect Releaser -- Pokemon and team effects only and not directly inherited
            checkInstance(classy, EffectReleaser.class, PokemonEffect.class, TeamEffect.class);
            checkInstance(classy, EffectReleaser.class, DefogRelease.class, RapidSpinRelease.class, BarrierEffect.class);

            // MultiTurnMove should not be directly inherited
            checkInstance(classy, MultiTurnMove.class, ChargingMove.class, RechargingMove.class);

            // Casted from CastSource.getSource()
            checkInstance(classy, AbilityChanger.class, castSources);
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
            checkInstance(classy, BeforeTurnEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, EffectBlockerEffect.class, effectListSourcesNoAttack);
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
            checkInstance(classy, StageChangingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatModifyingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatChangingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentPowerChangeEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, AbsorbDamageEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, DamageTakenEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, StatusReceivedEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentStatusReceivedEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, OpponentEndAttackEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, TerrainCastEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, AttackBlocker.class, effectListSourcesNoAttack);
            checkInstance(classy, ModifyStageValueEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, WeatherEliminatingEffect.class, effectListSourcesNoAttack);
            checkInstance(classy, WeatherExtendingEffect.class, effectListSourcesNoAttack);

            // Invoked from battle.getEffectsList() with attack
            for (Class<?> effectListWithAttackClass : effectListWithAttackClasses) {
                checkInstance(classy, effectListWithAttackClass, effectListSourcesWithAttack);
            }
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
        // assigned must be in interface
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

    // Scans all classes accessible from the context class loader which belong to the given package and subpackages.
    private static List<Class<?>> getClasses() throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertNotNull(classLoader);

        String packageName = "";
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
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
                Assert.assertFalse(file.getName().contains("."));
                classes.addAll(findClasses(file, nextPackagePrefix + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(nextPackagePrefix + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }
}
