package generator.update;

import battle.attack.AttackNamesies;
import battle.attack.MoveCategory;
import generator.GeneratorType;
import generator.update.MoveUpdater.MoveParser;
import type.Type;
import util.GeneralUtils;
import util.string.StringUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class MoveUpdater extends GeneratorUpdater<AttackNamesies, MoveParser> {
    public MoveUpdater() {
        super(GeneratorType.ATTACK_GEN, "moves");
    }

    @Override
    protected Map<AttackNamesies, MoveParser> createEmpty() {
        return new EnumMap<>(AttackNamesies.class);
    }

    @Override
    protected Set<AttackNamesies> getToParse() {
        Set<AttackNamesies> toParse = EnumSet.allOf(AttackNamesies.class);
        toParse.remove(AttackNamesies.CONFUSION_DAMAGE);
        toParse.remove(AttackNamesies.FAKE_FREEZER);
        return toParse;
    }

    @Override
    protected AttackNamesies getNamesies(String name) {
        return AttackNamesies.getValueOf(name);
    }

    @Override
    protected String getName(AttackNamesies namesies) {
        return namesies.getName();
    }

    @Override
    public String getDescription(AttackNamesies namesies) {
        return namesies.getNewAttack().getDescription();
    }

    @Override
    protected MoveParser createParser(Scanner in) {
        return new MoveParser(in);
    }

    // Contains the raw information about a move as parsed from serebii
    // Does not include any updates that I may have made to the rules (those are updated in the move parser test)
    public class MoveParser extends BaseParser {
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
        public final boolean bitingMove;
        public final boolean snatchable;
        public final boolean gravity;
        public final boolean defrosty;
        public final boolean magicBouncy;
        public final boolean protecty;
        public final boolean mirrorMovey;

        private MoveParser(Scanner in) {
            name = in.nextLine().trim();
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
            bitingMove = GeneralUtils.parseBoolean(in.nextLine().trim());
            snatchable = GeneralUtils.parseBoolean(in.nextLine().trim());
            gravity = GeneralUtils.parseBoolean(in.nextLine().trim());
            defrosty = GeneralUtils.parseBoolean(in.nextLine().trim());
            magicBouncy = GeneralUtils.parseBoolean(in.nextLine().trim());
            protecty = GeneralUtils.parseBoolean(in.nextLine().trim());
            mirrorMovey = GeneralUtils.parseBoolean(in.nextLine().trim());

            attackNamesies = getNamesies(name);
        }
    }
}
