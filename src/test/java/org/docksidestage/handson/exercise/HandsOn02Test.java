package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    
    public void test_existsTestData() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        int memberCnt = memberBhv.selectCount(cb -> {
            cb.query().addOrderBy_MemberId_Asc();
        });
        // ## Assert ##
        assertTrue(memberCnt > 0);
    }

    public void test_searchByMemberNamePrefix() throws Exception {
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
    
    public void test_searchByMemberId() throws Exception {
        // ## Arrange ##
        Integer memberId = 1;
        // ## Act ##
        OptionalEntity<Member> member = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(memberId);
        });
        // ## Assert ##
        assertTrue(member.isPresent());
        assertEquals(memberId, member.get().getMemberId());
        log("memberName: {}, memberId: {}, memberIdForSearch", member.get().getMemberName(), member.get().getMemberId(), memberId);
    }

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
