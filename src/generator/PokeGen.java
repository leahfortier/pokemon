package generator;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import generator.fields.ClassFields;
import generator.format.InputFormatter;
import generator.format.MethodWriter;
import main.Global;
import pokemon.active.Nature;
import util.GeneralUtils;
import util.file.FileIO;
import util.file.FileName;
import util.string.PokeString;
import util.string.StringAppender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PokeGen {
    private final InputFormatter inputFormatter;
    private NamesiesGen namesiesGen;
    private GeneratorType currentGen;

    public PokeGen(InputFormatter inputFormatter) {
        this.inputFormatter = inputFormatter;

        final Map<NamesiesType, NamesiesGen> namesiesMap = new HashMap<>();

        // Go through each PokeGen and generate
        for (GeneratorType generatorType : GeneratorType.values()) {
            this.currentGen = generatorType;

            final NamesiesType namesiesType = generatorType.getNamesiesType();
            if (!namesiesMap.containsKey(namesiesType)) {
                namesiesMap.put(namesiesType, new NamesiesGen(namesiesType));
            }

            this.namesiesGen = namesiesMap.get(namesiesType);
            this.superGen();
        }

        this.inputFormatter.close();

        namesiesMap.values().forEach(NamesiesGen::writeNamesies);
    }

    private String createClass(ClassFields fields) {
        String name = fields.getName();
        String className = fields.getClassName();

        this.namesiesGen.createNamesies(name, className);

        List<String> interfaces = new ArrayList<>();
        String additionalMethods = getAdditionalMethods(fields, interfaces);
        String constructor = inputFormatter.getConstructor(fields);
        String implementsString = inputFormatter.getImplementsString(interfaces);
        String extraFields = fields.getAndRemove("Field");
        String headerComments = fields.getAndRemove("Comments");

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
        inputFormatter.readFileFormat(in);

        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Get the class name
            String name = line.replace(":", "");

            // Read in all of the fields
            ClassFields fields = new ClassFields(in, name);
            inputFormatter.validate(fields);

            // Create class and append
            out.append(createClass(fields));
        }

        if (this.currentGen == GeneratorType.ITEM_GEN) {
            addTMsAndMints(out);
        }

        out.append("}");

        this.writeGen(this.currentGen.getOutputPath(), out.toString());
    }

    protected void writeGen(String fileName, String contents) {
        FileIO.overwriteFile(fileName, contents);
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
            moreFields = MethodWriter.addMethodInfo(
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

        return methods.toString();
    }

    private void addTMsAndMints(StringAppender tmClasses) {
        if (this.currentGen != GeneratorType.ITEM_GEN) {
            Global.error("Can only add TMs/Mints for the Item class");
        }

        // Add TMs
        Scanner in = FileIO.openFile(FileName.TM_LIST);
        while (in.hasNext()) {
            String attackName = in.nextLine().trim();

            AttackNamesies namesies = AttackNamesies.getValueOf(attackName);
            Attack attack = namesies.getNewAttack();

            String itemName = attackName + " TM";
            ClassFields fields = new ClassFields(itemName);

            fields.addNew("Desc", attack.getDescription());
            fields.addNew("TM", namesies.name());

            tmClasses.append(createClass(fields));
        }

        // Add Mints
        // Note: Game only includes Serious neutral mint, but just including all of them for that flavor
        for (Nature nature : Nature.values()) {
            String itemName = nature.getName() + " Mint";
            ClassFields fields = new ClassFields(itemName);

            // Note: Technically should use shortened versions like Sp. Atk/Def instead of Sp. Attack/Defense
            String ending = nature.isNeutral()
                            ? "all of its stats will grow at an equal rate"
                            : "its " + nature.getBeneficial().getShortName() + " will grow more easily, " +
                                    "but its " + nature.getHindering().getShortName() + " will grow more slowly";
            fields.addNew("Desc", "When a " + PokeString.POKEMON + " smells this mint, " + ending + ".");
            fields.addNew("Mint", nature.getName());

            tmClasses.append(createClass(fields));
        }
    }

    private boolean getPhysicalContact(ClassFields fields) {
        boolean physicalContact = false;
        if (this.currentGen == GeneratorType.ATTACK_GEN) {
            String category = fields.getRequired("Cat");
            physicalContact = category.toUpperCase().equals(MoveCategory.PHYSICAL.name());

            String physicalContactField = fields.getAndRemove("PhysicalContact");
            if (physicalContactField != null) {
                physicalContact = GeneralUtils.parseBoolean(physicalContactField);
            }
        }

        return physicalContact;
    }
}
