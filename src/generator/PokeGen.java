package generator;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Weather;
import generator.fieldinfo.ConstructorInfo;
import generator.fieldinfo.FailureInfo;
import generator.fieldinfo.InfoList;
import generator.format.InputFormatter;
import generator.format.MethodInfo;
import item.Item;
import item.ItemNamesies;
import item.use.TechnicalMachine;
import main.Global;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.GeneralUtils;
import util.StringAppender;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class PokeGen {
    private static ConstructorInfo constructorInfo;
    private static FailureInfo failureInfo;

    private final InputFormatter inputFormatter;
    private NamesiesGen namesiesGen;
    private Generator currentGen;

    PokeGen(InputFormatter inputFormatter) {
        this.inputFormatter = inputFormatter;

        final Map<Class, NamesiesGen> namesiesMap = new HashMap<>();

        // Go through each PokeGen and generate
        for (Generator generator : Generator.values()) {
            this.currentGen = generator;

            final Class namesiesClass = generator.getNamesiesClass();
            if (!namesiesMap.containsKey(namesiesClass)) {
                namesiesMap.put(namesiesClass, new NamesiesGen(generator.getOutputFolder(), namesiesClass));
            }

            this.namesiesGen = namesiesMap.get(namesiesClass);
            this.superGen();
        }

        this.inputFormatter.close();

        namesiesMap.values().forEach(NamesiesGen::writeNamesies);
    }

    private ClassFields readFields(Scanner in, String name, String className, int index) {
        ClassFields fields = StuffGen.readFields(in);
        inputFormatter.validate(fields);

        fields.setClassName(className);

        fields.addNew("Namesies", name);
        fields.addNew("Index", index + "");

        // NumTurns matches to both MinTurns and MaxTurns
        fields.getPerformAndRemove("NumTurns", numTurns -> {
            fields.addNew("MinTurns", numTurns);
            fields.addNew("MaxTurns", numTurns);
        });

        return fields;
    }

    private String createClass(String name, String className, ClassFields fields) {
        this.namesiesGen.createNamesies(name, className);

        List<String> interfaces = new ArrayList<>();
        String additionalMethods = getAdditionalMethods(fields, interfaces);
        String constructor = constructorInfo.getConstructor(fields);
        String implementsString = inputFormatter.getImplementsString(interfaces);
        String extraFields = fields.getAndRemove("Field");
        String headerComments = fields.getAndRemove("Comments");

        fields.remove("Index");

        fields.confirmEmpty();

        return StuffGen.createClass(
                headerComments,
                className,
                this.currentGen.getSuperClassName(),
                implementsString,
                extraFields,
                constructor,
                additionalMethods,
                false
        );
    }

    private void superGen() {
        StringAppender out = StuffGen.startGen(this.currentGen.getOutputPath());
        out.appendLine("\n\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/"); // DON'T DO IT

        Scanner in = FileIO.openFile(this.currentGen.getInputPath());
        readFileFormat(in);

        // TODO: Do we still use this?
        // The image index file for the item generator
        int index = 0;

        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Get the name
            String name = line.replace(":", "");
            String className = StringUtils.getClassName(name);

            // Read in all of the fields
            ClassFields fields = readFields(in, name, className, index);

            // Create class and append
            out.append(createClass(name, className, fields));
        }

        if (this.currentGen == Generator.ITEM_GEN) {
            addTMs(out);
        }

        out.append("}");

        FileIO.overwriteFile(this.currentGen.getOutputPath(), out.toString());
    }

    private String getAdditionalMethods(ClassFields fields, List<String> interfaces) {

        StringAppender methods = new StringAppender();

        // Add all the interfaces to the interface list
        List<String> currentInterfaces = new ArrayList<>();
        List<String> finalCurrentInterfaces = currentInterfaces; // Java is dumb
        fields.getPerformAndRemove("Int", value -> Collections.addAll(finalCurrentInterfaces, value.split(", ")));

        List<String> nextInterfaces = new ArrayList<>();

        boolean moreFields = true;
        while (moreFields) {
            moreFields = MethodInfo.addMethodInfo(
                    methods,
                    fields,
                    currentInterfaces,
                    this.currentGen.getSuperClassName(),
                    inputFormatter
            );

            interfaces.addAll(currentInterfaces);

            currentInterfaces = nextInterfaces;
            nextInterfaces = new ArrayList<>();
        }

        if (this.getPhysicalContact(fields)) {
            fields.addNew("MoveType", MoveType.PHYSICAL_CONTACT.name());
        }

        if (failureInfo != null) {
            methods.appendPrefix(failureInfo.writeFailure(fields, this.currentGen.getSuperClassName(), inputFormatter));
        }

        return methods.toString();
    }

    private void addTMs(StringAppender tmClasses) {
        if (this.currentGen != Generator.ITEM_GEN) {
            Global.error("Can only add TMs for the Item class");
        }

        Scanner in = FileIO.openFile(FileName.TM_LIST);
        while (in.hasNext()) {
            String attackName = in.nextLine().trim();
            String className = StringUtils.getClassName(attackName);

            AttackNamesies namesies = AttackNamesies.getValueOf(attackName);
            Attack attack = namesies.getNewAttack();

            String itemName = attackName + " TM";
            className += "TM";

            ClassFields fields = new ClassFields();
            fields.setClassName(className);
            fields.addNew("Namesies", attackName + "_TM");
            fields.addNew("Desc", attack.getDescription());

            fields.addNew("Int", TechnicalMachine.class.getSimpleName());
            fields.addNew("TM", namesies.name());

            tmClasses.append(createClass(itemName, className, fields));
        }
    }

    private boolean getPhysicalContact(ClassFields fields) {
        boolean physicalContact = false;
        if (this.currentGen == Generator.ATTACK_GEN) {
            String category = fields.getRequired("Cat");
            physicalContact = category.toUpperCase().equals(MoveCategory.PHYSICAL.name());

            String physicalContactField = fields.getAndRemove("PhysicalContact");
            if (physicalContactField != null) {
                physicalContact = GeneralUtils.parseBoolean(physicalContactField);
            }
        }

        return physicalContact;
    }

    private static void readFileFormat(Scanner in) {
        failureInfo = null;

        InfoList superInfo = new InfoList(null);
        InfoList fieldKeys = new InfoList(null);
        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.equals("***")) {
                break;
            }

            String formatType = line.replace(":", "");
            switch (formatType) {
                case "Constructor":
                    superInfo = new InfoList(in);
                    break;
                case "Fields":
                    fieldKeys = new InfoList(in);
                    break;
                case "Failure":
                    failureInfo = new FailureInfo(in);
                    break;
                default:
                    Global.error("Invalid format type " + formatType);
                    break;
            }
        }

        constructorInfo = new ConstructorInfo(superInfo, fieldKeys);
    }

    private enum Generator {
        ATTACK_GEN("Moves.txt", Folder.ATTACK, Attack.class, AttackNamesies.class),
        POKEMON_EFFECT_GEN("PokemonEffects.txt", Folder.GENERIC_EFFECT, PokemonEffect.class, EffectNamesies.class),
        TEAM_EFFECT_GEN("TeamEffects.txt", Folder.GENERIC_EFFECT, TeamEffect.class, EffectNamesies.class),
        BATTLE_EFFECT_GEN("BattleEffects.txt", Folder.GENERIC_EFFECT, BattleEffect.class, EffectNamesies.class),
        WEATHER_GEN("Weather.txt", Folder.GENERIC_EFFECT, Weather.class, EffectNamesies.class),
        ABILITY_GEN("Abilities.txt", Folder.ABILITY, Ability.class, AbilityNamesies.class),
        ITEM_GEN("Items.txt", Folder.ITEMS, Item.class, ItemNamesies.class);

        private final String inputPath;
        private final String outputPath;
        private final String outputFolder;
        private final String superClassName;
        private final Class namesiesClass;

        Generator(String inputFileName, String outputFolder, Class superClass, Class namesiesClass) {
            this.inputPath = Folder.GENERATOR + inputFileName;
            this.outputPath = outputFolder + superClass.getSimpleName() + ".java";
            this.outputFolder = outputFolder;
            this.superClassName = superClass.getSimpleName();
            this.namesiesClass = namesiesClass;
        }

        public String getInputPath() {
            return this.inputPath;
        }

        public String getOutputPath() {
            return this.outputPath;
        }

        public String getOutputFolder() {
            return this.outputFolder;
        }

        public String getSuperClassName() {
            return this.superClassName;
        }

        public Class getNamesiesClass() {
            return this.namesiesClass;
        }
    }
}
