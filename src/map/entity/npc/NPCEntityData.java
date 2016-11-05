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

		x = matcher.startX;
		y = matcher.startY;
		path = matcher.getPath();
		spriteIndex = matcher.spriteIndex;
		defaultDirection = matcher.direction;
		walkToPlayer = matcher.walkToPlayer ? 1 : 0;

		interactions = matcher.getInteractionMap();
		startKey = matcher.getStartKey();
	}

	public Entity getEntity() {
		if (entity == null) {
			entity = new NPCEntity(name, x, y, path, defaultDirection, spriteIndex, walkToPlayer == 1, interactions, startKey);
		}

		return entity;
	}
}
