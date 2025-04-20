package org.docksidestage.handson.dbflute.logic;

import java.util.Arrays;
import java.util.List;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * DBFluteハンズオン06のためのクラス
 * @author mayukorin
 */
public class HandsOn06LogicTest extends UnitContainerTestCase {

    public void test_selectSuffixMemberList_指定したsuffixで検索されること() throws Exception {
        // ## Arrange ##
        String suffix = "vic";
        HandsOn06Logic logic = new HandsOn06Logic();
        inject(logic);

        // ## Act ##
        ListResultBean<Member> members = logic.selectSuffixMemberList(suffix);

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberName().endsWith(suffix));
        });
    }

    public void test_selectSuffixMemberList_suffixが無効な値なら例外が発生すること() throws Exception {
        // ## Arrange ##
        List<String> invalidSuffixList = Arrays.asList(null, "", "   ", " \t\n");
        HandsOn06Logic logic = new HandsOn06Logic();
        inject(logic);

        // ## Act, Assert ##
        for (String suffix : invalidSuffixList) {
            assertException(IllegalArgumentException.class, () -> {
                logic.selectSuffixMemberList(suffix);
            });
        }
    }
}
