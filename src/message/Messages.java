package message;

import battle.Battle;
import battle.Move;
import message.MessageUpdate.Update;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import util.StringUtils;

import java.util.ArrayDeque;

public class Messages {
    private static ArrayDeque<MessageUpdate> messages;

    public static void clear() {
        messages = new ArrayDeque<>();
    }

    public static ArrayDeque<MessageUpdate> getMessages() {
        return messages;
    }
    
    // Just a plain old regular message
    public static void addMessage(String message) {
        messages.add(new MessageUpdate(message));
    }

    public static void addMessage(String message, Battle b, ActivePokemon p) {
        messages.add(new MessageUpdate(message));

        messages.add(new MessageUpdate(p.getHP(), p.user()));
        messages.add(new MessageUpdate(p.getHP(), p.getMaxHP(), p.user()));
        messages.add(new MessageUpdate(p.getStatus().getType(), p.user()));
        messages.add(new MessageUpdate(p.getDisplayType(b), p.user()));
        messages.add(new MessageUpdate(p.getName(), p.user()));
        messages.add(new MessageUpdate(p.getGender(), p.user()));
    }

    // TODO: What is the point of switching?
    public static void addMessage(String message, Battle b, ActivePokemon p, boolean switching) {
        messages.add(new MessageUpdate(message, p, b));
    }

    public static void addMessage(String message, Battle b, ActivePokemon gainer, int[] statGains, int[] stats) {
        addMessage(StringUtils.empty(), b, gainer);
        messages.add(new MessageUpdate(statGains, stats));
    }

    // Image update
    public static void addMessage(String message, PokemonInfo pokemon, boolean shiny, boolean animation, boolean target) {
        messages.add(new MessageUpdate(message, pokemon, shiny, animation, target));
    }

    public static void addMessage(String message, Update update) {
        messages.add(new MessageUpdate(message, update));
    }

    public static void addMessage(String message, int duration) {
        messages.add(new MessageUpdate(message, duration));
    }

    public static void addMessage(String message, Battle b, ActivePokemon gainer, float expRatio, boolean levelUp) {
        addMessage(message, b, gainer);
        messages.add(new MessageUpdate(gainer.getLevel(), expRatio, levelUp));
    }

    // Learning a move
    public static void addMessage(String message, ActivePokemon p, Move move) {
        messages.add(new MessageUpdate(message, p, move));
    }
}
