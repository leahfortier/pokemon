package util;

import main.Global;

import java.util.Arrays;

public class StringUtils {
    
    // Utility class -- should not be instantiated
    private StringUtils() {
        Global.error("Save class cannot be instantiated.");
    }
    
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    public static boolean isNullOrWhiteSpace(String s) {
        return isNullOrEmpty(s) || s.trim().isEmpty();
    }
    
    public static String nullWhiteSpace(String s) {
        return isNullOrWhiteSpace(s) ? null : s;
    }
    
    public static String empty() {
        return "";
    }
    
    public static boolean isSpecial(char c) {
        return !isLower(c) && !isUpper(c) && !isNumber(c) && c != '_';
    }
    
    public static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }
    
    public static boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }
    
    public static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }
    
    public static String articleString(final String s) {
        if (isNullOrEmpty(s)) {
            return empty();
        }
        
        boolean vowelStart = (s.charAt(0) + "").matches("[AEIOU]");
        return "a" + (vowelStart ? "n" : "") + " " + s;
    }
    
    public static String repeat(String repeat, int numTimes) {
        return new StringAppender()
                .appendRepeat(repeat, numTimes)
                .toString();
    }
    
    // Convert to list of strings and join by a space
    public static String spaceSeparated(Object... values) {
        return new StringAppender()
                .appendJoin(" ", Arrays.asList(values))
                .toString();
    }
    
    // Examples:
    //   red -> Red
    //   water stone -> Water Stone
    //   x-scissor -> X-Scissor
    // For all upper-case words first do toLowercase
    public static String properCase(String string) {
        string = string.trim();
        if (isNullOrEmpty(string)) {
            return empty();
        }
        
        StringAppender s = new StringAppender();
        while (!string.isEmpty()) {
            s.append(string.substring(0, 1).toUpperCase());
            string = string.substring(1, string.length());
            
            if (string.isEmpty()) {
                break;
            }
            
            char c = ' ';
            int index = string.indexOf(c);
            int indexOther = string.indexOf('-');
            if (indexOther != -1 && (indexOther < index || index == -1)) {
                c = '-';
                index = indexOther;
            }
            
            if (index == -1) {
                s.append(string.substring(0, string.length()));
                string = "";
            } else {
                s.append(string.substring(0, index)).append(c);
                string = string.substring(index + 1, string.length());
            }
        }
        
        return s.toString();
    }
    
    public static String getNamesiesString(String name) {
        if (isNullOrWhiteSpace(name)) {
            return empty();
        }
        
        // TODO: Look at this again -- why are we removing spaces?
        // Remove special characters and spaces
        name = SpecialCharacter.removeSpecialCharacters(name).replace(" ", "");
        
        char[] nameChar = name.toCharArray();
        StringAppender enumName = new StringAppender(nameChar[0] + "");
        
        for (int i = 1; i < nameChar.length; i++) {
            if (((isUpper(nameChar[i]) &&
                    !isUpper(nameChar[i - 1])) || nameChar[i] == '-') &&
                    nameChar[i - 1] != '_' &&
                    enumName.charAt(enumName.length() - 1) != '_') {
                enumName.append("_");
            }
            
            if (isSpecial(nameChar[i])) {
                continue;
            }
            
            enumName.append(nameChar[i]);
        }
        
        return enumName.toString().toUpperCase();
    }
    
    // Creates the className from the name
    public static String getClassName(String name) {
        name = SpecialCharacter.removeSpecialCharacters(name);
        
        StringAppender className = new StringAppender();
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '-') {
                if (isLower(name.charAt(i + 1))) {
                    char c = (char)(name.charAt(i + 1) - 'a' + 'A');
                    className.append(c);
                    i++;
                    continue;
                }
                
                continue;
            }
            
            if (isSpecial(name.charAt(i))) {
                continue;
            }
            
            className.append(name.charAt(i));
        }
        
        return className.toString();
    }
}
