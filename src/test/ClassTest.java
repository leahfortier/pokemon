package test;

import battle.attack.Attack;
import battle.attack.AttackInterface;
import battle.effect.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.EffectInterfaces.AlwaysCritEffect;
import battle.effect.EffectInterfaces.ApplyDamageEffect;
import battle.effect.EffectInterfaces.AttackBlocker;
import battle.effect.EffectInterfaces.AttackSelectionEffect;
import battle.effect.EffectInterfaces.AttackingNoAdvantageChanger;
import battle.effect.EffectInterfaces.BarrierEffect;
import battle.effect.EffectInterfaces.BasicAccuracyBypassEffect;
import battle.effect.EffectInterfaces.BattleEndTurnEffect;
import battle.effect.EffectInterfaces.BeforeTurnEffect;
import battle.effect.EffectInterfaces.BracingEffect;
import battle.effect.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.EffectInterfaces.ChangeTypeEffect;
import battle.effect.EffectInterfaces.CritBlockerEffect;
import battle.effect.EffectInterfaces.CritStageEffect;
import battle.effect.EffectInterfaces.DamageTakenEffect;
import battle.effect.EffectInterfaces.DefendingNoAdvantageChanger;
import battle.effect.EffectInterfaces.DefogRelease;
import battle.effect.EffectInterfaces.DifferentStatEffect;
import battle.effect.EffectInterfaces.EffectBlockerEffect;
import battle.effect.EffectInterfaces.EffectReleaser;
import battle.effect.EffectInterfaces.EncounterRateMultiplier;
import battle.effect.EffectInterfaces.EndBattleEffect;
import battle.effect.EffectInterfaces.EndTurnEffect;
import battle.effect.EffectInterfaces.EntryEffect;
import battle.effect.EffectInterfaces.ForceMoveEffect;
import battle.effect.EffectInterfaces.GroundedEffect;
import battle.effect.EffectInterfaces.HalfWeightEffect;
import battle.effect.EffectInterfaces.LevitationEffect;
import battle.effect.EffectInterfaces.ModifyStageValueEffect;
import battle.effect.EffectInterfaces.MurderEffect;
import battle.effect.EffectInterfaces.NameChanger;
import battle.effect.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.EffectInterfaces.OpponentApplyDamageEffect;
import battle.effect.EffectInterfaces.OpponentEndAttackEffect;
import battle.effect.EffectInterfaces.OpponentIgnoreStageEffect;
import battle.effect.EffectInterfaces.OpponentPowerChangeEffect;
import battle.effect.EffectInterfaces.OpponentStatSwitchingEffect;
import battle.effect.EffectInterfaces.OpponentStatusReceivedEffect;
import battle.effect.EffectInterfaces.OpponentTakeDamageEffect;
import battle.effect.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.EffectInterfaces.PassableEffect;
import battle.effect.EffectInterfaces.PowerChangeEffect;
import battle.effect.EffectInterfaces.PriorityChangeEffect;
import battle.effect.EffectInterfaces.RapidSpinRelease;
import battle.effect.EffectInterfaces.RepellingEffect;
import battle.effect.EffectInterfaces.SelfAttackBlocker;
import battle.effect.EffectInterfaces.SemiInvulnerableBypasser;
import battle.effect.EffectInterfaces.SleepyFightsterEffect;
import battle.effect.EffectInterfaces.StageChangingEffect;
import battle.effect.EffectInterfaces.StatChangingEffect;
import battle.effect.EffectInterfaces.StatLoweredEffect;
import battle.effect.EffectInterfaces.StatModifyingEffect;
import battle.effect.EffectInterfaces.StatProtectingEffect;
import battle.effect.EffectInterfaces.StatSwitchingEffect;
import battle.effect.EffectInterfaces.StatusPreventionEffect;
import battle.effect.EffectInterfaces.StatusReceivedEffect;
import battle.effect.EffectInterfaces.SuperDuperEndTurnEffect;
import battle.effect.EffectInterfaces.SwitchOutEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import battle.effect.EffectInterfaces.TargetSwapperEffect;
import battle.effect.EffectInterfaces.TerrainCastEffect;
import battle.effect.EffectInterfaces.TrappingEffect;
import battle.effect.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.EffectInterfaces.WeatherEliminatingEffect;
import battle.effect.EffectInterfaces.WeatherExtendingEffect;
import battle.effect.EffectInterfaces.WildEncounterAlterer;
import battle.effect.EffectInterfaces.WildEncounterSelector;
import battle.effect.attack.AbilityChanger;
import battle.effect.attack.ChangeAttackTypeSource;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.attack.MultiTurnMove;
import battle.effect.attack.MultiTurnMove.ChargingMove;
import battle.effect.attack.MultiTurnMove.RechargingMove;
import battle.effect.battle.BattleEffect;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
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
