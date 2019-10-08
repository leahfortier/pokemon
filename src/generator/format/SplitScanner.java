package generator.format;

import main.Global;

public class SplitScanner {
    private final String[] split;
    private final String original;
    private final String delimiter;
    private int index;

    public SplitScanner(String toSplit) {
        this.delimiter = " ";
        this.split = toSplit.split(delimiter);
        this.original = toSplit;
        this.index = 0;
    }

    public boolean hasNext() {
        return index < split.length;
    }

    public String next() {
        if (!hasNext()) {
            Global.error("No more elements to get. Original: " + this.original);
        }

        return split[index++];
    }

    public String getRemaining() {
        if (!this.hasNext()) {
            return "";
        }

        String[] newSplit = this.original.split(delimiter, this.index + 1);
        return newSplit[index];
    }
}
