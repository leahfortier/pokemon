package map.entity.npc;

import map.Direction;
import map.entity.Entity;
import map.entity.EntityData;
import pattern.AreaDataMatcher.NPCMatcher;

import java.util.Map;

public class NPCEntityData extends EntityData {
	private NPCEntity entity;
	public String path;
	public int spriteIndex;
	public Direction defaultDirection;

	private Map<String, NPCInteraction> interactions;
	private String startKey;

	public NPCEntityData(NPCMatcher matcher) {
		super(matcher.name, matcher.condition);

		x = matcher.startX;
		y = matcher.startY;
		path = matcher.getPath();
		spriteIndex = matcher.spriteIndex;
		defaultDirection = matcher.direction;

		interactions = matcher.getInteractionMap();
		startKey = matcher.getStartKey();
	}

	public Entity getEntity() {
		if (entity == null) {
			entity = new NPCEntity(name, x, y, path, defaultDirection, spriteIndex, interactions, startKey);
		}

		return entity;
	}
}
