package util;

public class PokeString {

	public static final String POKE = "Pok\u00E9";
	public static final String POKEMON = POKE + "mon";
	public static final String POKEDEX = POKE + "dex";

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
				if (StringUtils.isLower(name.charAt(i + 1))) {
					char c = (char)(name.charAt(i + 1) - 'a' + 'A');
					className += c;
					i++;
					continue;
				}
				
				continue;
			}
			
			if (StringUtils.isSpecial(name.charAt(i))) {
				continue;
			}
			
			className += name.charAt(i);
		}
		
		return className;
	}
	
	public static String getNamesiesString(String name) {
		if (StringUtils.isNullOrWhiteSpace(name)) {
			return StringUtils.empty();
		}

		// Remove special characters and spaces
		name = removeSpecialCharacters(name).replace(" ", "");

		char[] nameChar = name.toCharArray();
		String enumName = nameChar[0] + "";
		
		for (int i = 1; i < nameChar.length; i++) {
			if (((StringUtils.isUpper(nameChar[i]) &&
					!StringUtils.isUpper(nameChar[i - 1])) || nameChar[i] == '-') &&
					nameChar[i - 1] != '_' &&
					enumName.charAt(enumName.length() - 1) != '_') {
				enumName += "_";
			}
			
			if (StringUtils.isSpecial(nameChar[i])) {
				continue;
			}
			
			enumName += nameChar[i];
		}
		
		return enumName.toUpperCase();
	}

	// TODO: New file
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
