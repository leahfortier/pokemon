package map.entity.npc;

import map.Direction;
import map.entity.Entity;
import map.entity.EntityData;
import pattern.NPCMatcher;

import java.util.Map;

public class NPCEntityData extends EntityData {
	private NPCEntity entity;
	public String path;
	public int spriteIndex;
	public Direction defaultDirection;

	private Map<String, NPCInteraction> interactions;
	private String startKey;

	public NPCEntityData(NPCMatcher matcher) {
		super(matcher);

		x = matcher.getX();
		y = matcher.getY();
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
