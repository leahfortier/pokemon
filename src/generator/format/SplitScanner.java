package generator.format;

import main.Global;
import util.string.StringUtils;

public class SplitScanner {
    private final String[] split;
    private final String original;
    private final String delimiter;
    private int index;
    private int tempIndex;

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
            return StringUtils.empty();
        }

        String[] newSplit = this.original.split(delimiter, this.index + 1);
        return newSplit[index];
    }

    public void setTempIndex() {
        this.tempIndex = index;
    }

    public void restoreTempIndex() {
        this.index = tempIndex;
    }

    public void reset() {
        this.index = 0;
    }
}
