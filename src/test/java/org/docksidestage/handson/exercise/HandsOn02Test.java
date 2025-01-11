package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * DBFluteハンズオン02のクラス
 * @author mayukorin
 */
public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    /**
     * テストデータがあることをテスト
     */
    public void test_existsTestData() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        int memberCnt = memberBhv.selectCount(cb -> {
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertTrue(memberCnt > 0);
    }

    /**
     * 会員名称がSで始まる会員を検索
     */
    public void test_searchByMemberNamePrefixS() throws Exception {
        // ## Arrange ##
        String memberNamePrefix = "S";

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberNamePrefix, op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        // ## Assert ##
        assertFalse(memberList.isEmpty());
        memberList.forEach(member -> {
            log("memberName: {}, memberNamePrefixForSearch: {}", member.getMemberName(), memberNamePrefix);
            assertTrue(member.getMemberName().startsWith(memberNamePrefix));
        });
    }

    /**
     * 会員IDが1の会員を検索
     */
    public void test_searchByMemberIdOne() throws Exception {
        // ## Arrange ##
        Integer memberId = 99999;

        // ## Act ##
        OptionalEntity<Member> member = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(memberId);
        });

        // ## Assert ##
        assertTrue(member.isPresent()); // memberがnullのときに下のgetMemberId()でExceptionにならないようにisPresent()を追加したが、不要かも
        assertEquals(memberId, member.get().getMemberId());
        log("memberName: {}, memberId: {}, memberIdForSearch", member.get().getMemberName(), member.get().getMemberId(), memberId);
    }

    /**
     * 生年月日がない会員を検索
     */
    public void test_searchMemberByNoBirthDate() throws Exception {
        // ## Arrange ##

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Desc();
        });

        // ## Assert ##
        memberList.forEach(member -> {
            log("memberId: {}, birthDate: {}", member.getMemberName(), member.getBirthdate());
            assertNull(member.getBirthdate());
        });
    }
}
