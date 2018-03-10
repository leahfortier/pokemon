package generator.update;

import generator.GeneratorType;
import main.Global;
import util.file.FileIO;
import util.string.StringAppender;

import java.util.Scanner;

// Updates input generator files
public abstract class GeneratorUpdater {
    private final GeneratorType generatorType;

    protected GeneratorUpdater(GeneratorType generatorType) {
        this.generatorType = generatorType;
    }

    protected abstract void writeScriptInputList();
    protected abstract String getNewDescription(String name);

    public final void updateDescription() {
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
}
