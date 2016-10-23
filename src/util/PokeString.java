package util;

import main.Namesies.NamesiesType;

public class PokeString {

	// Converts special characters to a simple version (example: poke e -> e) 
	public static String removeSpecialCharacters(String input) {
		for (SpecialCharacter specialCharacter : SpecialCharacter.values()) {
			input = input.replaceAll(specialCharacter.unicodeLiteral, specialCharacter.replaceCharacter);
			input = input.replaceAll(specialCharacter.specialCharacter, specialCharacter.replaceCharacter);
		}
		
		return input;
	}
	
	// Removes the special characters as well as some symbols
	public static String removeSpecialSymbols(String input) {
		return removeSpecialCharacters(input).replaceAll("[.'-]", "");
	}
	
	// Takes the special characters and replaces them with their literal unicode
	public static String convertSpecialToUnicode(String input) {
		for (SpecialCharacter specialCharacter : SpecialCharacter.values()) {
			input = input.replaceAll(specialCharacter.specialCharacter, specialCharacter.unicodeLiteral);
		}
		
		return input;
	}
	
	// Takes the unicode literals and replaces them with their special character value
	public static String restoreSpecialFromUnicode(String input) {
		for (SpecialCharacter specialCharacter : SpecialCharacter.values()) {
			input = input.replaceAll(specialCharacter.unicodeLiteral, specialCharacter.specialCharacter);
		}

		return input;
	}
	
	// Creates the className from the name and adds to the appropriate fields
	public static String writeClassName(String name) {
		name = removeSpecialCharacters(name);
		
		String className = "";
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '-') {
				if (isLower(name.charAt(i + 1))) {
					char c = (char)(name.charAt(i + 1) - 'a' + 'A');
					className += c;
					i++;
					continue;
				}
				
				continue;
			}
			
			if (isSpecial(name.charAt(i))) {
				continue;
			}
			
			className += name.charAt(i);
		}
		
		return className;
	}
	
	public static String getNamesiesString(String name, NamesiesType superClass) {
		// Remove special characters and spaces
		name = removeSpecialCharacters(name).replace(" ", "");
		
		char[] nameChar = (name + "_" + superClass).toCharArray();
		String enumName = nameChar[0] + "";
		
		for (int i = 1; i < nameChar.length; i++) {
			if (((isUpper(nameChar[i]) &&
					!isUpper(nameChar[i - 1])) || nameChar[i] == '-') &&
					nameChar[i - 1] != '_' &&
					enumName.charAt(enumName.length() - 1) != '_') {
				enumName += "_";
			}
			
			if (isSpecial(nameChar[i])) {
				continue;
			}
			
			enumName += nameChar[i];
		}
		
		return enumName.toUpperCase();
	}

	// TODO: These should all move to String util or something
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
	
	// TODO: Before the special chars were O for the genders, wtf is that about
	public enum SpecialCharacter {
		POKE_E("\\\\u00e9", "\u00e9", "e"),
		FEMALE("\\\\u2640", "\u2640", "F"),
		MALE("\\\\u2642", "\u2642", "M");
		
		private final String unicodeLiteral;
		private final String specialCharacter;
		private final String replaceCharacter;
		
		SpecialCharacter(String unicodeLiteral, String specialCharacter, String replaceCharacter) {
			this.unicodeLiteral = unicodeLiteral;
			this.specialCharacter = specialCharacter;
			this.replaceCharacter = replaceCharacter;
		}
	}
}
