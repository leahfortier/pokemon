package map.entity;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Global;
import map.Direction;
import pattern.AreaDataMatcher.NPCMatcher;
import util.StringUtils;

public class NPCEntityData extends EntityData {
	private static final Pattern multiVariablePattern = Pattern.compile("(\\w+)(?:\\[(\\d+)\\])?:\\s*(?:(\\w+)|\"([^\"]*)\")");

	private NPCEntity entity;
	public String path;
	public int spriteIndex;
	public Direction defaultDirection;

	public String[] firstDialogue;
	public String[] secondDialogue;
	public String trainerInfo;
	public String itemInfo;

	public String firstTriggers;
	public String secondTriggers;

	public int walkToPlayer; // TODO: why is this an int? Should it be a boolean?

	public NPCEntityData(NPCMatcher matcher) {
		super(matcher.name, matcher.condition);

		// TODO: trainer info and interactions and actions and yeah
		x = matcher.startX;
		y = matcher.startY;
		trigger = matcher.trigger;
		path = matcher.getPath();
		spriteIndex = matcher.spriteIndex;
		defaultDirection = matcher.direction;
		firstDialogue = new String[] { matcher.text };
		secondDialogue = new String[] { matcher.text };
		walkToPlayer = matcher.walkToPlayer ? 1 : 0;
	}

	public NPCEntityData(String name, String contents) {
		super(name, contents);

		entity = null;

		defaultDirection = Direction.DOWN;
		spriteIndex = 0;
		path = Direction.WAIT_CHARACTER + "";

		walkToPlayer = -1;

		// TODO: Why are these arrays with a seemingly arbitrary size?
		firstDialogue = new String[100];
		secondDialogue = new String[100];
		
		int FDSize = -1;
		int SDSize = -1;
		int val = -1;

		Matcher m = multiVariablePattern.matcher(contents);
		while (m.find()) {
			switch (m.group(1)) {
				case "startX":
					x = Integer.parseInt(m.group(3));
					break;
				case "startY":
					y = Integer.parseInt(m.group(3));
					break;
				case "trigger":
					trigger = m.group(3);
					break;
				case "path":
					path = m.group(3);
					break;
				case "spriteIndex":
					spriteIndex = Integer.parseInt(m.group(3));
					break;
				case "direction":
					String direction = m.group(3);
					try {
						defaultDirection = Direction.values()[Integer.parseInt(direction)];
					}
					catch (NumberFormatException exception) {
						defaultDirection = Direction.valueOf(direction);
					}
					break;
				case "firstDialogue":
					val = Integer.parseInt(m.group(2));
					firstDialogue[val] = m.group(4);
					FDSize = Math.max(FDSize, val);
					break;
				case "secondDialogue":
					val = Integer.parseInt(m.group(2));
					secondDialogue[val] = m.group(4);
					SDSize = Math.max(SDSize, val);
					break;
				case "trainer":
					trainerInfo = m.group(4);
					break;
				case "give":
					itemInfo = m.group(4);
					break;
				case "firstTriggers":
					firstTriggers = m.group(4);
					break;
				case "secondTriggers":
					secondTriggers = m.group(4);
					break;
				case "walkToPlayer":
					walkToPlayer = m.group(3).equals("true") ? 1 : 0;
					break;
			}
		}

		if (walkToPlayer == -1 && trainerInfo != null) {
			walkToPlayer = 1;
		}

		firstDialogue = Arrays.copyOf(firstDialogue, FDSize + 1);
		if (firstDialogue.length == 0 || (firstDialogue.length > 0 && firstDialogue[0] == null && FDSize > -1)) {
			Global.error("firstDialogue missing for NPC " + name + ".");
		}

		secondDialogue = Arrays.copyOf(secondDialogue, SDSize + 1);
		if (secondDialogue.length > 0 && secondDialogue[0] == null && FDSize > -1) {
			Global.error("secondDialogue missing for NPC " + name + ".");
		}

		// if (firstDialogue.length > 0)
		// System.out.println(Arrays.toString(firstDialogue));
		// if (secondDialogue.length > 0)
		// System.out.println(Arrays.toString(secondDialogue));
	}

	public NPCEntityData(String name, String condition, int x, int y, String trigger, String path, int direction, int index, String[] firstDialogue, String[] secondDialogue, String trainerInfo, String itemInfo, String firstTriggers, String secondTriggers, boolean walkToPlayer) {
		super(name, condition);

		this.name = name;
		this.trigger = trigger;
		this.path = path;
		this.spriteIndex = index;

		this.walkToPlayer = walkToPlayer ? 1 : 0;

		this.firstDialogue = firstDialogue;
		this.secondDialogue = secondDialogue;
		this.trainerInfo = trainerInfo;
		this.itemInfo = itemInfo;
		this.firstTriggers = firstTriggers;
		this.secondTriggers = secondTriggers;

		this.x = x;
		this.y = y;

		this.defaultDirection = Direction.values()[direction];
	}

	public Entity getEntity() {
		if (entity == null) {
			// entity = new NPCEntity(x, y, trigger, path, defaultDirection,
			// spriteIndex);
			entity = new NPCEntity(name, x, y, trigger, path, defaultDirection, spriteIndex, firstDialogue, secondDialogue, trainerInfo, itemInfo, firstTriggers, secondTriggers, walkToPlayer == 1);
		}
		return entity;
	}

	public String entityDataAsString() {
		StringBuilder ret = new StringBuilder();
		StringUtils.appendLine(ret, "NPC " + name + "{");

		if (!condition.getOriginalConditionString().isEmpty()) {
			StringUtils.appendLine(ret, "\tcondition: " + condition.getOriginalConditionString());
		}

		StringUtils.appendLine(ret, "\tstartX: " + x);
		StringUtils.appendLine(ret, "\tstartY: " + y);
		StringUtils.appendLine(ret, "\tpath: " + path);
		StringUtils.appendLine(ret, "\tspriteIndex: " + spriteIndex);
		StringUtils.appendLine(ret, "\tdirection: " + defaultDirection);
		StringUtils.appendLine(ret, "\twalkToPlayer: " + (walkToPlayer == 1));

		for (int currDialogue = 0; currDialogue < firstDialogue.length; ++currDialogue) {
			StringUtils.appendLine(ret, "\tfirstDialogue[" + currDialogue + "]: \"" + firstDialogue[currDialogue] + "\"");
		}

		if (secondDialogue != null) {
			for (int currDialogue = 0; currDialogue < secondDialogue.length; ++currDialogue) {
				StringUtils.appendLine(ret, "\tsecondDialogue[" + currDialogue + "]: \"" + secondDialogue[currDialogue] + "\"");
			}
		}

		if (itemInfo != null) {
			StringUtils.appendLine(ret, "\tgive: \"" + itemInfo.trim() + "\"");
		}

		if (trainerInfo != null) {
			StringUtils.appendLine(ret, "\ttrainer: \"" + trainerInfo.trim() + "\"");
		}

		if (firstTriggers != null) {
			StringUtils.appendLine(ret, "\tfirstTriggers: \"" + firstTriggers.trim() + "\"");

		}

		if (secondTriggers != null) {
			StringUtils.appendLine(ret, "\tsecondTriggers: \"" + secondTriggers.trim() + "\"");

		}

		ret.append("}\n");

		return ret.toString();
	}
}
