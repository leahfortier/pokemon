package generator.update;

import generator.GeneratorType;
import generator.update.GeneratorUpdater.BaseParser;
import main.Global;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringAppender;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;

// Creates input files for serebii scripts
// Updates descriptions in input generator files (with output serebii parser files)
public abstract class GeneratorUpdater<NamesiesType extends Enum, ParserType extends BaseParser> {
    private final GeneratorType generatorType;
    private final String baseFileName;

    private Map<NamesiesType, ParserType> parseMoves;

    protected GeneratorUpdater(GeneratorType generatorType, String baseFileName) {
        this.generatorType = generatorType;
        this.baseFileName = baseFileName;
    }

    protected abstract Map<NamesiesType, ParserType> createEmpty();
    protected abstract Set<NamesiesType> getToParse();
    protected abstract NamesiesType getNamesies(String name);
    protected abstract String getName(NamesiesType namesies);
    protected abstract String getDescription(NamesiesType namesies);
    protected abstract ParserType createParser(Scanner in);

    // True for input, false for output false
    private String getFileName(boolean input) {
        return Folder.SCRIPTS_COMPARE + this.baseFileName + "." + (input ? "in" : "out");
    }

    private void loadParseMoves() {
        parseMoves = this.createEmpty();

        Scanner in = FileIO.openFile(this.getFileName(false));
        while (in.hasNext()) {
            ParserType parser = this.createParser(in);
            parseMoves.put(this.getNamesies(parser.name), parser);
        }
    }

    private void writeScriptInputList() {
        Set<NamesiesType> toParse = this.getToParse();

        String out = new StringAppender()
                .appendJoin("\n", toParse, this::getName)
                .toString();

        FileIO.overwriteFile(this.getFileName(true), out);
    }

    private String getNewDescription(String name) {
        if (parseMoves == null) {
            this.loadParseMoves();
        }

        NamesiesType namesies = this.getNamesies(name);
        ParserType parser = parseMoves.get(namesies);
        if (parser != null) {
            return parser.description;
        } else {
            return this.getDescription(namesies);
        }
    }

    public Iterable<ParserType> getParsers() {
        if (parseMoves == null) {
            this.loadParseMoves();
        }

        return parseMoves.values();
    }

    private void updateDescription() {
        final String genFilePath = this.generatorType.getInputPath();

        Scanner in = FileIO.openFile(genFilePath);
        StringAppender out = new StringAppender();

        // Format stuff at the top of the file
        while (in.hasNext()) {
            String line = in.nextLine();
            out.appendLine(line);

            line = line.trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.equals("***")) {
                break;
            }

            while (in.hasNext()) {
                line = in.nextLine();
                out.appendLine(line);

                line = line.trim();
                if (line.equals("*")) {
                    break;
                }
            }
        }

        // Read the classes
        while (in.hasNext()) {
            String line = in.nextLine();
            out.appendLine(line);
            line = line.trim();

            // Ignore comments and white space
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Get the class name
            String name = line.replace(":", "");
            String newDescription = this.getNewDescription(name);
            boolean replacedDescription = false;

            // Read in all of the fields
            while (in.hasNextLine()) {
                line = in.nextLine();

                if (line.trim().equals("*")) {
                    out.appendLine(line);
                    break;
                }

                String[] split = line.split(":", 2);
                if (split.length != 2) {
                    Global.error("Field key and value must be separated by a colon: " + line);
                }

                String key = split[0].trim();
                if (key.equals("Desc")) {
                    out.appendLine("\t" + key + ": " + newDescription);
                    replacedDescription = true;
                } else {
                    out.appendLine(line);
                }

                String value = split[1].trim();
                if (value.isEmpty()) {
                    while (in.hasNext()) {
                        line = in.nextLine();
                        out.appendLine(line);

                        line = line.trim();
                        if (line.equals("###")) {
                            break;
                        }
                    }
                }
            }

            if (!replacedDescription) {
                Global.error("No description found for " + name);
            }
        }

        FileIO.overwriteFile(genFilePath, out.toString());
    }

    public void update() {
        this.writeScriptInputList();
//        this.updateDescription();
    }

    public static void updateAll() {
        new MoveUpdater().update();
        new AbilityUpdater().update();
        new ItemUpdater().update();
    }

    public abstract static class BaseParser {
        public String name;
        public String description;
    }
}
