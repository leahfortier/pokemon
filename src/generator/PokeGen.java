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
import item.Item;
import item.ItemNamesies;
import item.use.TechnicalMachine;
import main.Global;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringAppender;
import util.StringUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

class PokeGen {
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
        
        namesiesMap.values().forEach(NamesiesGen::writeNamesies);
    }
    
    private ClassFields readFields(Scanner in, String name, String className, int index) {
        ClassFields fields = StuffGen.readFields(in);
        inputFormatter.validate(fields);
        
        fields.setClassName(className);
        
        fields.add("Namesies", name);
        fields.add("Index", index + "");
        
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
        String constructor = getConstructor(fields);
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
        out.appendLine("\t/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/"); // DON'T DO IT
        
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
    
    private static void readFileFormat(Scanner in) {
        constructorKeys = new ArrayList<>();
        fieldKeys = new ArrayList<>();
        failureInfo = null;
        
        while (in.hasNext()) {
            String line = in.nextLine().trim();
            
            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            if (line.equals("***")) {
                return;
            }
            
            String formatType = line.replace(":", "");
            if (formatType.equals("Failure")) {
                failureInfo = new FailureInfo(in);
                continue;
            }
            
            while (in.hasNextLine()) {
                line = in.nextLine().trim();
                if (line.equals("*")) {
                    break;
                }
                
                String[] split = line.split(" ", 2);
                String key = split[0].trim();
                String value = split[1].trim();
                
                Entry<String, String> entry = new SimpleEntry<>(key, value);
                
                switch (formatType) {
                    case "Constructor":
                        constructorKeys.add(entry);
                        break;
                    case "Fields":
                        fieldKeys.add(entry);
                        break;
                    default:
                        Global.error("Invalid format type " + formatType);
                }
            }
        }
    }
    
    private static List<Entry<String, String>> constructorKeys;
    private static List<Entry<String, String>> fieldKeys;
    private static FailureInfo failureInfo;
    
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
                    inputFormatter.getOverrideMethods(),
                    fields,
                    currentInterfaces,
                    StringUtils.empty(),
                    this.currentGen.getSuperClassName(),
                    inputFormatter
            );
            
            interfaces.addAll(currentInterfaces);
            
            currentInterfaces = nextInterfaces;
            nextInterfaces = new ArrayList<>();
        }
        
        if (failureInfo != null) {
            methods.appendPrefix(failureInfo.writeFailure(fields, this.currentGen.getSuperClassName(), inputFormatter));
        }
        
        return methods.toString();
    }
    
    private void addTMs(StringAppender classes) {
        if (this.currentGen != Generator.ITEM_GEN) {
            Global.error("Can only add TMs for the Item class");
        }
        
        Scanner in = FileIO.openFile(FileName.TM_LIST);
        while (in.hasNext()) {
            String attackName = in.nextLine().trim();
            String className = StringUtils.getClassName(attackName);
            
            AttackNamesies namesies = AttackNamesies.getValueOf(attackName);
            Attack attack = namesies.getAttack();
            
            String itemName = attackName + " TM";
            className += "TM";
            
            ClassFields fields = new ClassFields();
            fields.setClassName(className);
            fields.add("Namesies", attackName + "_TM");
            fields.add("Desc", attack.getDescription());
            
            fields.add("Int", TechnicalMachine.class.getSimpleName());
            fields.add("TM", namesies.name());
            
            classes.append(createClass(itemName, className, fields));
        }
    }
    
    private boolean getPhysicalContact(ClassFields fields) {
        boolean physicalContact = false;
        if (this.currentGen == Generator.ATTACK_GEN) {
            String category = fields.getRequired("Cat");
            physicalContact = category.toUpperCase().equals(MoveCategory.PHYSICAL.name());
            
            String physicalContactField = fields.getAndRemove("PhysicalContact");
            if (physicalContactField != null) {
                physicalContact = Boolean.parseBoolean(physicalContactField);
            }
        }
        
        return physicalContact;
    }
    
    // For the super call inside the class constructor, returns the comma-separated field values
    // (It's what's inside the super parentheses)
    // Example: 'AttackNamesies.ATTACK_NAME, "Attack Description", 35, Type.NORMAL, MoveCategory.PHYSICAL'
    private String getInternalConstructorValues(ClassFields fields) {
        StringAppender superValues = new StringAppender();
        for (Entry<String, String> pair : constructorKeys) {
            String value = inputFormatter.getConstructorValue(pair, fields);
            superValues.appendDelimiter(", ", value);
        }
        
        return superValues.toString();
    }
    
    private String getConstructor(ClassFields fields) {
        boolean physicalContact = getPhysicalContact(fields);
        
        StringAppender constructor = new StringAppender();
        constructor.appendLine("super(" + getInternalConstructorValues(fields) + ");");
        
        for (Entry<String, String> pair : fieldKeys) {
            String fieldKey = pair.getKey();
            fields.getPerformAndRemove(
                    fieldKey,
                    value -> constructor.appendLine(inputFormatter.getAssignment(pair.getValue(), value))
            );
        }
        
        fields.getPerformAndRemove("StatChange", value -> {
            String[] mcSplit = value.split(" ");
            for (int i = 0, index = 1; i < Integer.parseInt(mcSplit[0]); i++) {
                constructor.append("super.statChanges[Stat.")
                           .append(mcSplit[index++].toUpperCase())
                           .append(".index()] = ")
                           .append(mcSplit[index++])
                           .append(";\n");
            }
        });
        
        if (physicalContact) {
            constructor.appendFormat(
                    "super.moveTypes.add(%s.%s);\n",
                    MoveType.class.getSimpleName(),
                    MoveType.PHYSICAL_CONTACT
            );
        }
        
        fields.getPerformAndRemove("Activate", constructor::append);
        
        return new MethodInfo(
                fields.getClassName() + "()",
                constructor.toString(),
                AccessModifier.PACKAGE_PRIVATE
        ).writeFunction();
    }
}
