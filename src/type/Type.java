package type;

import battle.Battle;
import battle.attack.MoveType;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import util.FileIO;
import util.Folder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public enum Type implements Serializable {
	NORMAL(0, "Normal", () -> TypeAdvantage.NORMAL, new Color(230, 230, 250), -1),
	FIRE(1, "Fire", () -> TypeAdvantage.FIRE, new Color(220, 20, 20), 8),
	WATER(2, "Water", () -> TypeAdvantage.WATER, new Color(35, 120, 220), 9),
	ELECTRIC(3, "Electric", () -> TypeAdvantage.ELECTRIC, new Color(255, 215, 0), 11),
	GRASS(4, "Grass", () -> TypeAdvantage.GRASS, new Color(120, 200, 80), 10),
	ICE(5, "Ice", () -> TypeAdvantage.ICE, new Color(5, 235, 235), 13),
	FIGHTING(6, "Fighting", () -> TypeAdvantage.FIGHTING, new Color(165, 82, 57), 0),
	POISON(7, "Poison", () -> TypeAdvantage.POISON, new Color(150, 30, 160), 2),
	GROUND(8, "Ground", () -> TypeAdvantage.GROUND, new Color(190, 170, 120), 3),
	FLYING(9, "Flying", () -> TypeAdvantage.FLYING, new Color(135, 206, 250), 1),
	PSYCHIC(10, "Psychic", () -> TypeAdvantage.PSYCHIC, new Color(218, 112, 214), 12),
	BUG(11, "Bug", () -> TypeAdvantage.BUG, new Color(153, 235, 27), 5),
	ROCK(12, "Rock", () -> TypeAdvantage.ROCK, new Color(169, 135, 70), 4),
	GHOST(13, "Ghost", () -> TypeAdvantage.GHOST, new Color(92, 61, 139), 6),
	DRAGON(14, "Dragon", () -> TypeAdvantage.DRAGON, new Color(106, 90, 205), 14),
	DARK(15, "Dark", () -> TypeAdvantage.DARK, new Color(49, 79, 79), 15),
	STEEL(16, "Steel", () -> TypeAdvantage.STEEL, new Color(200, 200, 210), 7),
	FAIRY(17, "Fairy", () -> TypeAdvantage.FAIRY, new Color(221, 160, 221), -1),
	NO_TYPE(18, "Unknown", () -> TypeAdvantage.NO_TYPE, new Color(255, 255, 255, 0), -1); // TODO: TYPE: NULL MUTHAFUCKA

	private final int index;
	private final String name;
	private final AdvantageGetter advantageGetter;
	private final Color color;
	private final int hiddenIndex;
	private final BufferedImage image;

	Type(int index, String name, AdvantageGetter advantageGetter, Color color, int hiddenIndex) {
		this.index = index;
		this.name = name;
		this.advantageGetter = advantageGetter;
		this.color = color;
		this.hiddenIndex = hiddenIndex;

		String imageName = "Type" + name;
		this.image = FileIO.readImage(Folder.TYPE_TILES + imageName);
	}

	public int getIndex() {
		return this.index;
	}

	public String getName() {
		return this.name;
	}

	public TypeAdvantage getAdvantage() {
		return this.advantageGetter.getAdvantage();
	}

	public Color getColor() {
		return this.color;
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
	
	public static Color[] getColors(Type[] t) {
		return new Color[] { t[0].getColor(), t[t[1] == Type.NO_TYPE ? 0 : 1].getColor() };
	}
	
	public static Color[] getColors(ActivePokemon p) {
		return getColors(p.isEgg() ? new Type[] { Type.NORMAL, Type.NO_TYPE } : p.getActualType());
	}
	
	public static Type getHiddenType(int hiddenIndex) {
		for (Type type : values()) {
			if (type.hiddenIndex == hiddenIndex) {
				return type;
			}
		}

		Global.error("Invalid hidden type index " + hiddenIndex);
		return null;
	}

	public static boolean blockAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
		if (defending.isType(b, Type.GRASS) && attacking.getAttack().isMoveType(MoveType.POWDER)) {
			Messages.add(new MessageUpdate(defending.getName() + " is immune to Powder moves!"));
			return true;
		}

		return false;
	}

	private interface AdvantageGetter {
		TypeAdvantage getAdvantage();
	}
}
