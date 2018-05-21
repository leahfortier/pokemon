package generator.format;

import util.string.StringAppender;

public class MethodFormatter {

    private int tabs;
    private int parenthesesBalance;
    private boolean inArrayDeclaration;
    private boolean inLambda;
    private boolean inSwitch;
    private boolean inCases;

    public MethodFormatter(int tabs) {
        this.tabs = tabs;

        this.parenthesesBalance = 0;
        this.inArrayDeclaration = false;
        this.inLambda = false;
        this.inSwitch = false;
        this.inCases = false;
    }

    // Appends the trimmed line with the appropriate tabs
    // Code inside parentheses and array declaration will have two tabs indented
    public void appendLine(String line, StringAppender method) {
        line = line.trim();

        if (line.startsWith("switch (")) {
            inSwitch = true;
        }

        if (inSwitch) {
            boolean inBefore = inCases;
            inCases = line.startsWith("case ") || line.equals("default:");

            if (inBefore && !inCases) {
                tabs++;
            }
        }

        boolean previousLamba = inLambda;
        if (line.startsWith("}")) {
            tabs--;
            inSwitch = false;
            inLambda = false;
            if (inArrayDeclaration) {
                tabs--;
                inArrayDeclaration = false;
            }
        }

        int numOpen = (int)line.chars().filter(c -> c == '(').count();
        int numClosed = (int)line.chars().filter(c -> c == ')').count();
        boolean previouslyInParentheses = parenthesesBalance > 0;
        parenthesesBalance += numOpen - numClosed;
        boolean nowInParentheses = parenthesesBalance > 0;

        if (previouslyInParentheses && !nowInParentheses && !previousLamba) {
            tabs -= 2;
        }

        // Add the tabs
        if (!line.isEmpty()) {
            // Add an extra tab if the line starts with a '.' indicating a chained method call
            method.appendRepeat("\t", tabs + (line.startsWith(".") ? 1 : 0));
        }

        // Actually write the line
        method.appendLine(line);

        if (inSwitch && (line.equals("break;") || line.startsWith("return ") || line.equals("return;"))) {
            tabs--;
        }

        if (line.endsWith("{")) {
            tabs++;
            if (line.endsWith(" = {") || line.endsWith("[] {")) {
                tabs++;
                inArrayDeclaration = true;
            } else if (line.endsWith(" -> {")) {
                inLambda = true;
            }
        }

        if (!previouslyInParentheses && nowInParentheses && !inLambda) {
            tabs += 2;
        }
    }
}
