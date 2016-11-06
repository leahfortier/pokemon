package message;

import battle.Battle;
import battle.Move;
import gui.view.MapView;
import main.Game;
import main.Game.ViewMode;
import message.MessageUpdate.Update;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import util.StringUtils;

import java.util.ArrayDeque;

public class Messages {
    private static final ArrayDeque<MessageUpdate> mapMessages = new ArrayDeque<>();
    private static final ArrayDeque<MessageUpdate> battleMessages = new ArrayDeque<>();
    private static boolean fightyFight = false;

    private static ArrayDeque<MessageUpdate> getQueue() {
        return fightyFight ? battleMessages : mapMessages;
    }

    public static void clearAllMessages() {
        clearBattleMessages();
        clearMapMessages();
    }

    public static void clearBattleMessages() {
        battleMessages.clear();
    }

    public static void clearMapMessages() {
        mapMessages.clear();
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
        getQueue().addFirst(new MessageUpdate(message));
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

    public static void fightyFight() {
        fightyFight = true;
    }

    public static void mappityMap() {
        fightyFight = false;
    }
}
