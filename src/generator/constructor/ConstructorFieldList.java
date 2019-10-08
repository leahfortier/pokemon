package generator.constructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ConstructorFieldList implements Iterable<ConstructorField> {
    private final List<ConstructorField> infoList;

    public ConstructorFieldList(Scanner in) {
        this.infoList = new ArrayList<>();

        if (in == null) {
            return;
        }

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            this.infoList.add(new ConstructorField(line));
        }
    }

    @Override
    public Iterator<ConstructorField> iterator() {
        return this.infoList.iterator();
    }
}
