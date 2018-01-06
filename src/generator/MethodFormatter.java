package generator;

import util.StringAppender;

class MethodFormatter {
    
    private int tabs;
    private int parenthesesBalance;
    private boolean inSwitch;
    private boolean inCases;
    
    MethodFormatter(int tabs) {
        this.tabs = tabs;
        
        this.inSwitch = false;
        this.inCases = false;
    }
    
    // Appends the trimmed line with the appropriate spaces
    void appendLine(String line, StringAppender method) {
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
        
        if (line.startsWith("}")) {
            tabs--;
            inSwitch = false;
        }
        
        int numOpen = (int)line.chars().filter(num -> num == '(').count();
        int numClosed = (int)line.chars().filter(num -> num == ')').count();
        boolean previouslyInParentheses = parenthesesBalance > 0;
        parenthesesBalance += numOpen - numClosed;
        boolean nowInParentheses = parenthesesBalance > 0;
        
        if (previouslyInParentheses && !nowInParentheses) {
            tabs--;
        }
        
        // Add the tabs
        method.appendRepeat("\t", tabs);
        
        // Actually write the line
        method.appendLine(line);
        
        if (inSwitch && (line.equals("break;") || line.startsWith("return ") || line.equals("return;"))) {
            tabs--;
        }
    
        if (line.endsWith("{")) {
            tabs++;
        }
        
        if (!previouslyInParentheses && nowInParentheses) {
            tabs++;
        }
    }
}
