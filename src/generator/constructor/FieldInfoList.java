package generator.constructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class FieldInfoList implements Iterable<FieldInfo> {
    private final List<FieldInfo> infoList;

    public FieldInfoList(Scanner in) {
        this.infoList = new ArrayList<>();

        if (in == null) {
            return;
        }

        while (in.hasNext()) {
            String line = in.nextLine().trim();
            if (line.equals("*")) {
                break;
            }

            this.infoList.add(new FieldInfo(line));
        }
    }

    @Override
    public Iterator<FieldInfo> iterator() {
        return this.infoList.iterator();
    }
}
