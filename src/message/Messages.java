package message;

import battle.Battle;
import battle.attack.Move;
import message.MessageUpdate.Update;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
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

    public static void addMessage(MessageUpdate message) {
        getQueue().add(message);
    }

    public static void addMessageToFront(String message) {
        addMessageToFront(new MessageUpdate(message));
    }

    public static void addMessageToFront(MessageUpdate messageUpdate) {
        getQueue().addFirst(messageUpdate);
    }

    // Just a plain old regular message
    public static void addMessage(String message) {
        addMessage(new MessageUpdate(message));
    }

    public static void addMessage(String message, Battle b, ActivePokemon p) {
        addMessage(new MessageUpdate(message));

        addMessage(new MessageUpdate(p.getHP(), p.user()));
        addMessage(new MessageUpdate(p.getHP(), p.getMaxHP(), p.user()));
        addMessage(new MessageUpdate(p.getStatus().getType(), p.user()));
        addMessage(new MessageUpdate(p.getDisplayType(b), p.user()));
        addMessage(new MessageUpdate(p.getName(), p.user()));
        addMessage(new MessageUpdate(p.getGender(), p.user()));
    }

    // TODO: What is the point of switching?
    public static void addMessage(String message, Battle b, ActivePokemon p, boolean switching) {
        addMessage(new MessageUpdate(message, p, b));
    }

    public static void addMessage(String message, Battle b, ActivePokemon gainer, int[] statGains, int[] stats) {
        addMessage(StringUtils.empty(), b, gainer);
        addMessage(new MessageUpdate(statGains, stats));
    }

    // Image update
    public static void addMessage(String message, PokemonInfo pokemon, boolean shiny, boolean animation, boolean target) {
        addMessage(new MessageUpdate(message, pokemon, shiny, animation, target));
    }

    public static void addMessage(String message, Update update) {
        addMessage(new MessageUpdate(message, update));
    }

    public static void addMessage(String message, int duration) {
        addMessage(new MessageUpdate(message, duration));
    }

    public static void addMessage(String message, Battle b, ActivePokemon gainer, float expRatio, boolean levelUp) {
        addMessage(message, b, gainer);
        addMessage(new MessageUpdate(gainer.getLevel(), expRatio, levelUp));
    }

    // Learning a move
    public static void addMessage(String message, ActivePokemon p, Move move) {
        addMessage(new MessageUpdate(message, p, move));
    }
}
