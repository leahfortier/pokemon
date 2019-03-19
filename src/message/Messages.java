package message;

import battle.Battle;
import util.string.StringUtils;

import java.util.EnumMap;
import java.util.Map;

public class Messages {
    private static final Map<MessageState, MessageQueue> messageMap =
            new EnumMap<MessageState, MessageQueue>(MessageState.class) {{
                for (MessageState messageState : MessageState.values()) {
                    put(messageState, new MessageQueue());
                }
            }};

    private static MessageState messageState = MessageState.MAPPITY_MAP;

    private static MessageQueue getQueue() {
        return messageMap.get(messageState);
    }

    public static boolean isMessageState(MessageState checkMessageState) {
        return messageState == checkMessageState;
    }

    public static void setMessageState(MessageState newMessageState) {
        messageState = newMessageState;
    }

    public static void clearAllMessages() {
        messageMap.keySet().forEach(Messages::clearMessages);
    }

    public static void clearMessages(MessageState messageState) {
        messageMap.get(messageState).clear();
    }

    public static boolean hasMessages() {
        return !getQueue().isEmpty();
    }

    public static MessageUpdate getNextMessage() {
        return getQueue().poll();
    }

    public static MessageUpdate peek() {
        return getQueue().peek();
    }

    public static boolean nextMessageEmpty() {
        MessageUpdate nextMessage = getQueue().peek();
        return StringUtils.isNullOrEmpty(nextMessage.getMessage()) && !nextMessage.getUpdateType().isExitMessage();
    }

    public static void update(Battle b) {
        add(new MessageUpdate().updatePokemon(b, b.getPlayer().front()));
        add(new MessageUpdate().updatePokemon(b, b.getOpponent().front()));
    }

    public static void add(String message) {
        add(new MessageUpdate(message));
    }

    public static void add(MessageUpdate message) {
        getQueue().add(message);
    }

    public static void addToFront(String message) {
        addToFront(new MessageUpdate(message));
    }

    public static void addToFront(MessageUpdate messageUpdate) {
        getQueue().addFirst(messageUpdate);
    }

    public enum MessageState {
        FIGHTY_FIGHT,       // Battle View
        MAPPITY_MAP,        // Map View
        BAGGIN_IT_UP,       // Bag View
        SIMULATION_STATION  // AI Simulation
    }
}
