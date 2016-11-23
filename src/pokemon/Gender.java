package pokemon;

import main.Global;

import java.awt.Color;
import java.io.Serializable;

public enum Gender implements Serializable {
	MALE("\u2642", new Color(55, 125, 220)), 
	FEMALE("\u2640", new Color(220, 50, 70)), 
	GENDERLESS(" ", Color.WHITE);

	private final String character;
	private final Color color;

	Gender(String character, Color color) {
		this.character = character;
		this.color = color;
	}

	public String getCharacter() {
		return character;
	}

	public Color getColor() {
		return color;
	}
	
	public static Gender getGender (int ratio) {
		if (ratio == -1) {
			return GENDERLESS;
		}

		return Global.chanceTest(ratio) ? MALE : FEMALE;
	}
	
	public static boolean oppositeGenders(ActivePokemon me, ActivePokemon o) {
		if (me.getGender() == MALE) {
			return o.getGender() == FEMALE;
		}
		else if (me.getGender() == FEMALE) {
			return o.getGender() == MALE;
		}
		else {
			return false;
		}
	}
}
