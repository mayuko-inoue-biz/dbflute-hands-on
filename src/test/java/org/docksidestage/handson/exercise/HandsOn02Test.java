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
        // TODO mayukorin [いいね] 良い変数名、わかりやすい！ by jflute (2025/01/14)
        String memberNamePrefix = "S";

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberNamePrefix, op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        // ## Assert ##
        // TODO mayukorin [いいね] ちゃんと素通り防止できてますね！ by jflute (2025/01/14)
        // ちなみに、UnitTestでループするときはずっとそうなので、assH -> assertHasAnyElement()
        // という専用のメソッドあるので、そっちを使ってみてください。
        assertFalse(memberList.isEmpty());
        memberList.forEach(member -> {
            // TODO mayukorin 細かいですが、member.getMemberName() が二度呼ばれてコードが横長なので... by jflute (2025/01/14)
            // 変数に抽出してみましょう。IntelliJのショートカットがあるはずです。
            log("memberName: {}, memberNamePrefixForSearch: {}", member.getMemberName(), memberNamePrefix);
            assertTrue(member.getMemberName().startsWith(memberNamePrefix));
        });
    }

    /**
     * 会員IDが1の会員を検索
     */
    public void test_searchByMemberIdOne() throws Exception {
        // ## Arrange ##
        // TODO mayukorin 99999は一時的なお試しで、コミットはしないようにしましょう by jflute (2025/01/14)
        Integer memberId = 99999;

        // ## Act ##
        OptionalEntity<Member> member = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(memberId);
        });

        // ## Assert ##
        // TODO mayukorin 丁寧で明示的で悪くはないですが、この場合 DBFlute の get() で例外の方がデバッグしやすいですね by jflute (2025/01/14)
        // assertTrue()を削除して99999で実行して例外メッセージを読んでみましょう。
        assertTrue(member.isPresent()); // memberがnullのときに下のgetMemberId()でExceptionにならないようにisPresent()を追加したが、不要かも
        // TODO mayukorin せめて member.get() は変数に抽出しちゃった方がスッキリするかと思います by jflute (2025/01/14)
        assertEquals(memberId, member.get().getMemberId());
        // TODO mayukorin テスト内でログを出すとき、assertの前に出す方が落ちたときにそのログを見てデバッグできます by jflute (2025/01/14)
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
        // TODO mayukorin こっちも素通り防止を by jflute (2025/01/14)
        memberList.forEach(member -> {
            log("memberId: {}, birthDate: {}", member.getMemberName(), member.getBirthdate());
            assertNull(member.getBirthdate());
        });
    }
}
