package util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringAppender {
    private final StringBuilder stringBuilder;
    private boolean lastEmpty;

    public StringAppender() {
        this.stringBuilder = new StringBuilder();
    }

    public StringAppender(String defaultString) {
        this.stringBuilder = new StringBuilder(defaultString);
    }

    public StringAppender append(char c) {
        return this.append(Character.toString(c));
    }

    public StringAppender append(String s) {
        if (!StringUtils.isNullOrEmpty(s)) {
            this.stringBuilder.append(s);
            lastEmpty = false;
        } else {
            lastEmpty = true;
        }

        return this;
    }

    // Appends the delimiter before s ONLY if the current builder is nonempty
    // Only appends the delimiter when s is nonempty
    public StringAppender appendDelimiter(String delimiter, String s) {
        if (StringUtils.isNullOrEmpty(s)) {
            return this;
        }

        if (delimiter == null) {
            delimiter = "";
        }

        if (!this.isEmpty()) {
            s = delimiter + s;
        }

        return this.append(s);
    }

    // Appends the delimiter after s when s is nonempty
    public StringAppender appendPostDelimiter(String delimiter, String s) {
        if (StringUtils.isNullOrEmpty(s)) {
            return this;
        }

        if (delimiter == null) {
            delimiter = "";
        }

        return this.append(s + delimiter);
    }

    public StringAppender appendIf(boolean condition, String s) {
        if (condition) {
            this.append(s);
        }
        return this;
    }

    public StringAppender appendLineIf(boolean condition, String s) {
        return this.appendIf(condition && s != null, s + "\n");
    }

    public StringAppender appendIfLastNonempty(String s) {
        return this.appendIf(!this.lastEmpty, s);
    }

    // Appends s and then a new line
    // Does nothing is s is null
    public StringAppender appendLine(String s) {
        return this.appendIf(s != null, s + "\n");
    }

    public StringAppender appendLine() {
        return this.appendLine("");
    }

    public StringAppender appendFormat(String format, Object... args) {
        return this.append(String.format(format, args));
    }

    // Appends the repeat string numTimes times
    public StringAppender appendRepeat(String repeat, int numTimes) {
        return this.appendJoin("", numTimes, index -> repeat);
    }

    // Applies the index mapper method numTimes and joins the results by the delimiter and appends
    // Example:
    //   delimiter: " ",
    //   numTimes: 3,
    //   indexMapper: i -> (i + 1) + ""
    //   appends: "1 2 3"
    public StringAppender appendJoin(String delimiter, int numTimes, Function<Integer, String> indexMapper) {
        List<String> joinees = new ArrayList<>();
        for (int i = 0; i < numTimes; i++) {
            joinees.add(indexMapper.apply(i));
        }
        return this.appendJoin(delimiter, joinees);
    }

    // Joins the objects by the delimiter and appends
    public StringAppender appendJoin(String delimiter, Collection<?> joinees) {
        return this.appendJoin(delimiter, joinees, Objects::toString);
    }

    public <T> StringAppender appendJoin(String delimiter, T[] joinees, Function<T, String> mapper) {
        return this.appendJoin(delimiter, Arrays.asList(joinees), mapper);
    }

    // Applies the mapper to the joinees, joins by the delimiter, and appends
    public <T> StringAppender appendJoin(String delimiter, Collection<T> joinees, Function<T, String> mapper) {
        if (delimiter == null) {
            delimiter = "";
        }

        return this.append(joinees.stream().map(mapper).collect(Collectors.joining(delimiter)));
    }

    // Appends to the beginning
    public StringAppender appendPrefix(String prefix) {
        return this.insert(0, prefix);
    }

    public StringAppender insert(int index, String s) {
        this.stringBuilder.insert(index, s);
        return this;
    }

    public StringAppender clear() {
        return this.setLength(0);
    }

    public StringAppender setLength(int newLength) {
        this.stringBuilder.setLength(newLength);
        return this;
    }

    public char charAt(int index) {
        return this.stringBuilder.charAt(index);
    }

    public boolean isEmpty() {
        return this.stringBuilder.length() == 0;
    }

    public int length() {
        return this.stringBuilder.length();
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }
}
