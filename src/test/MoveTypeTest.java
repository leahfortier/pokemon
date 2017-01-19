package test;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.MoveType;
import org.junit.Assert;
import org.junit.Test;

public class MoveTypeTest {
    @Test
    public void testPhysicalContact() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getAttack();
            Assert.assertFalse("Status moves cannot have physical contact. Move: " + attack.getName(),
                    attack.isStatusMove() && attack.isMoveType(MoveType.PHYSICAL_CONTACT));
        }
    }
}
