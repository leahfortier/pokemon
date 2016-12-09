package message;

import util.StringUtils;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;

public class Messages {
    private static final Map<MessageState, ArrayDeque<MessageUpdate>> messageMap =
        new EnumMap<MessageState, ArrayDeque<MessageUpdate>>(MessageState.class) {{
            for (MessageState messageState : MessageState.values()) {
                put(messageState, new ArrayDeque<>());
            }
        }};

    private static MessageState messageState = MessageState.MAPPITY_MAP;

    public enum MessageState {
        FIGHTY_FIGHT,   // Battle View
        MAPPITY_MAP,    // Map View
        BAGGIN_IT_UP;   // Bag View
    }

    private static ArrayDeque<MessageUpdate> getQueue() {
        return messageMap.get(messageState);
    }

    public static void setMessageState(MessageState newMessageState) {
        messageState = newMessageState;
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

    public static boolean nextMessageEmpty() {
        return StringUtils.isNullOrEmpty(getQueue().peek().getMessage());
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
}
