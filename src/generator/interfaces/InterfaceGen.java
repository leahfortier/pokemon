package generator.interfaces;

import battle.effect.InvokeInterfaces;
import generator.StuffGen;
import util.file.FileIO;
import util.file.FileName;
import util.file.Folder;
import util.string.StringAppender;

import java.util.Scanner;

public class InterfaceGen {
    private static final String INTERFACE_PATH = Folder.INTERFACE_EFFECT + InvokeInterfaces.class.getSimpleName() + ".java";

    public InterfaceGen() {
        final Scanner in = FileIO.openFile(FileName.INTERFACES);
        final StringAppender out = StuffGen.startGen(INTERFACE_PATH);

        // Go through the entire file
        while (in.hasNext()) {
            String line = in.nextLine().trim();

            // Ignore comments and white space at beginning of file
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            final String interfaceName = line.replace(":", "");
            final Interface effectInterface = new Interface(in, interfaceName);

            out.append(effectInterface.writeInterface());
        }

        out.append("}");

        FileIO.overwriteFile(INTERFACE_PATH, out.toString());
    }
}
