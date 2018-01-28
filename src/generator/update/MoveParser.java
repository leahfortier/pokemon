package generator.update;

import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import type.Type;
import util.FileIO;
import util.Folder;
import util.GeneralUtils;
import util.StringAppender;
import util.StringUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

// Contains the raw information about a move as parsed from serebii
// Does not include any updates that I may have made to the rules (those are updated in the move parser test)
public class MoveParser {
    private static final String SCRIPTS_INPUT_FILE_NAME = Folder.SCRIPTS + "moves.in";
    private static final String SCRIPTS_OUTPUT_FILE_NAME = Folder.SCRIPTS + "moves.out";

    private static Map<AttackNamesies, MoveParser> parseMoves;

    public final AttackNamesies attackNamesies;
    public final Type type;
    public final MoveCategory category;

    public final int pp;
    public final int power;
    public final int accuracy;

    public final String description;
    public final String chance;
    public final String crit;
    public final int priority;

    public final boolean physicalContact;
    public final boolean soundMove;
    public final boolean punchMove;
    public final boolean snatchable;
    public final boolean defrosty;
    public final boolean magicBouncy;
    public final boolean protecty;
    public final boolean mirrorMovey;

    private MoveParser(Scanner in) {
        attackNamesies = AttackNamesies.getValueOf(in.nextLine().trim());
        type = Type.valueOf(in.nextLine().trim().toUpperCase());
        category = MoveCategory.valueOf(in.nextLine().trim().toUpperCase());

        pp = Integer.parseInt(in.nextLine().trim());
        power = Integer.parseInt(in.nextLine().trim());
        accuracy = Integer.parseInt(in.nextLine().trim());

        description = in.nextLine().trim();
        chance = StringUtils.trimSuffix(in.nextLine().trim(), "%").trim();
        crit = in.nextLine().trim();
        priority = Integer.parseInt(in.nextLine().trim());

        physicalContact = GeneralUtils.parseBoolean(in.nextLine().trim());
        soundMove = GeneralUtils.parseBoolean(in.nextLine().trim());
        punchMove = GeneralUtils.parseBoolean(in.nextLine().trim());
        snatchable = GeneralUtils.parseBoolean(in.nextLine().trim());
        defrosty = GeneralUtils.parseBoolean(in.nextLine().trim());
        magicBouncy = GeneralUtils.parseBoolean(in.nextLine().trim());
        protecty = GeneralUtils.parseBoolean(in.nextLine().trim());
        mirrorMovey = GeneralUtils.parseBoolean(in.nextLine().trim());
    }

    public static void writeMovesList() {
        Set<AttackNamesies> toParse = EnumSet.allOf(AttackNamesies.class);
        toParse.remove(AttackNamesies.CONFUSION_DAMAGE);

        String out = new StringAppender()
                .appendJoin("\n", toParse, AttackNamesies::getName)
                .toString();
        FileIO.overwriteFile(SCRIPTS_INPUT_FILE_NAME, out);
    }

    public static MoveParser getParseMove(AttackNamesies attackNamesies) {
        if (parseMoves == null) {
            readMoves();
        }

        return parseMoves.get(attackNamesies);
    }

    public static Iterable<MoveParser> getParseMoves() {
        if (parseMoves == null) {
            readMoves();
        }

        return parseMoves.values();
    }

    private static void readMoves() {
        if (parseMoves != null) {
            return;
        }

        parseMoves = new EnumMap<>(AttackNamesies.class);

        Scanner in = FileIO.openFile(SCRIPTS_OUTPUT_FILE_NAME);
        while (in.hasNext()) {
            MoveParser moveParser = new MoveParser(in);
            parseMoves.put(moveParser.attackNamesies, moveParser);
        }
    }
}
