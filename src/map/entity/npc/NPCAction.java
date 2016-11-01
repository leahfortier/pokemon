package map.entity.npc;

public abstract class NPCAction {
    public static class Dialogue extends NPCAction {
        String text;
    }

    public static class GiveItem extends NPCAction {
        String itemName;
    }

    public static class Battle extends NPCAction {
        String text;
    }
}
