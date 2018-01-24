package generator.fieldinfo;

import generator.format.SplitScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class InfoList {
    protected final List<FieldInfo> infoList;

    protected InfoList(Scanner in) {
        this.infoList = new ArrayList<>();

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            SplitScanner split = new SplitScanner(line);
            if (this.shouldCreateInfo(split)) {
                this.infoList.add(new FieldInfo(split));
            }
        }
    }

    protected boolean shouldCreateInfo(SplitScanner split) {
        return true;
    }
}
