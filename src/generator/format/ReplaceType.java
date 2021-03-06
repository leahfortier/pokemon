package generator.format;

public enum ReplaceType {
    BASIC("", (original, remaining) -> original),
    UPPER_CASE(index -> index + "", (original, remaining) -> original.toUpperCase()),
    UNDER_SPACE("_", (original, remaining) -> original.replaceAll("_", " ")),
    FINISH("-", (original, remaining) -> original + remaining);

    private final SuffixGetter suffixGetter;
    private final InputReplacer inputReplacer;

    ReplaceType(String replaceSuffix, InputReplacer inputReplacer) {
        this(index -> replaceSuffix, inputReplacer);
    }

    ReplaceType(SuffixGetter suffixGetter, InputReplacer inputReplacer) {
        this.suffixGetter = suffixGetter;
        this.inputReplacer = inputReplacer;
    }

    public String replaceBody(String body, String original, String remaining, int parameterIndex) {
        String suffix = this.suffixGetter.getSuffix(parameterIndex);
        String newValue = this.inputReplacer.replaceInput(original, remaining);

        return body.replace(String.format("{%d%s}", parameterIndex, suffix), newValue);
    }

    @FunctionalInterface
    private interface SuffixGetter {
        String getSuffix(int index);
    }

    @FunctionalInterface
    private interface InputReplacer {
        String replaceInput(String original, String remaining);
    }
}
