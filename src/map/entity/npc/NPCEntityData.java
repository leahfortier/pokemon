package map.entity.npc;

import map.Direction;
import map.entity.Entity;
import map.entity.EntityData;
import pattern.AreaDataMatcher.NPCMatcher;
import util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NPCEntityData extends EntityData {
	private static final Pattern multiVariablePattern = Pattern.compile("(\\w+)(?:\\[(\\d+)\\])?:\\s*(?:(\\w+)|\"([^\"]*)\")");

	private NPCEntity entity;
	public String path;
	public int spriteIndex;
	public Direction defaultDirection;

	private Map<String, List<NPCAction>> interactions;
	private String startKey;

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
		walkToPlayer = matcher.walkToPlayer ? 1 : 0;

		interactions = matcher.getInteractionMap();
		startKey = matcher.getStartKey();
	}

	public NPCEntityData(String name, String contents) {
		super(name, contents);

		entity = null;

		defaultDirection = Direction.DOWN;
		spriteIndex = 0;
		path = Direction.WAIT_CHARACTER + "";

		walkToPlayer = -1;

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
				case "walkToPlayer":
					walkToPlayer = m.group(3).equals("true") ? 1 : 0;
					break;
			}
		}
	}

	public NPCEntityData(String name,
						 String condition,
						 int x,
						 int y,
						 String trigger,
						 String path,
						 int direction,
						 int index,
						 boolean walkToPlayer) {
		super(name, condition);

		this.name = name;
		this.trigger = trigger;
		this.path = path;
		this.spriteIndex = index;

		this.walkToPlayer = walkToPlayer ? 1 : 0;

		this.x = x;
		this.y = y;

		this.defaultDirection = Direction.values()[direction];
	}

	public Entity getEntity() {
		if (entity == null) {
			entity = new NPCEntity(name, x, y, path, defaultDirection, spriteIndex, walkToPlayer == 1, interactions, startKey);
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

		ret.append("}\n");

		return ret.toString();
	}
}
