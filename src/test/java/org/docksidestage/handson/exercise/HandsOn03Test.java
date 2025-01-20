package org.docksidestage.handson.exercise;

import java.time.LocalDate;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * DBFluteハンズオン03のためのクラス
 * @author mayukorin
 */
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    /**
     * 会員名称がSで始まる1968年1月1日以前に生まれた会員を検索する <br>
     * o 上記条件に合致する会員がDBに存在するか <br>
     * o 検索した会員が上記条件に合致するか <br>
     * をチェックしたい <br>
     * また、1968年1月1日は含まれることに注意。
     */
    public void test_searchMemberByNamePrefixAndBirthDate() throws Exception {
        // ## Arrange ##
        String memberNamePrefixForSearch = "S";
        LocalDate birthdateForSearch = LocalDate.of(1968, 1, 1);
    
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // TODO mayukorin [いいね] メソッドの呼び出し順序、いいですね！ by jflute (2025/01/20)
            // 実装順序は、データの取得、絞り込み、並び替え by jflute
            //  => http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus();
            cb.specify().columnMemberName();
            cb.specify().columnBirthdate();
            // TODO mayukorin specify[テーブル]だけやっても意味がないコードになります。カラムまで指定しないと。 by jflute (2025/01/20)
            cb.specify().specifyMemberStatus();
            cb.query().setMemberName_LikeSearch(memberNamePrefixForSearch, op -> op.likePrefix());
            // TODO mayukorin [tips] おおぉ、いきなり高度な機能を！でもできてますね。 by jflute (2025/01/20)
            // でもここでは lessEqual でも大丈夫でしたということで
            cb.query().setBirthdate_FromTo(null, birthdateForSearch, op -> {
                op.allowOneSide(); // 指定日以前に生まれた人を検索したくて、何日以降に生まれたかは問わない
                op.compareAsDate();
            });
            cb.query().addOrderBy_Birthdate_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            String memberName = member.getMemberName();
            // TODO mayukorin ここはmemberを付けなくてもいいかなと。birthdateで十分member限定感あるので by jflute (2025/01/20)
            LocalDate memberBirthdate = member.getBirthdate();
            MemberStatus memberStatus = member.getMemberStatus().get();
            log("memberName: {}, memberBirthDate: {}, memberStatusCodeName: {}, memberNamePrefixForSearch: {}, fromBirthDateForSearch: {}", memberName, memberBirthdate, memberStatus.getMemberStatusName(), memberNamePrefixForSearch, birthdateForSearch);
            assertTrue(memberName.startsWith(memberNamePrefixForSearch));
            // TODO mayukorin 細かいですが、ループの中で毎回同じ処理 plusDays(1) を実行してしまうのが無駄なので、ループ外に出しましょう by jflute (2025/01/20)
            assertTrue(memberBirthdate.isBefore(birthdateForSearch.plusDays(1))); // 生年月日が指定日時ぴったりでもOK
        });
    }

    /**
     * 会員ステータスと会員セキュリティ情報も取得して会員を検索 <br>
     * o 会員が存在すること <br>
     * o 会員ステータスと会員セキュリティ情報が存在すること <br>
     * をチェックしたい <br>
     * また、カージナリティを意識したい
     */
    public void test_searchMemberWithStatusAndSecurityInfo() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.specify().columnMemberId();
            cb.specify().columnBirthdate();
            cb.specify().specifyMemberStatus();
            cb.specify().specifyMemberServiceAsOne();
            cb.query().addOrderBy_Birthdate_Desc();
            cb.query().addOrderBy_MemberId_Asc();
        });
    
        // ## Assert ##
        // TODO jflute 1on1でカーディナリティのフォロー予定 (2025/01/20)
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            Integer memberId = member.getMemberId();
            LocalDate memberBirthdate = member.getBirthdate();
            OptionalEntity<MemberStatus> optMemberStatus = member.getMemberStatus();
            OptionalEntity<MemberSecurity> optMemberSecurity = member.getMemberSecurityAsOne();
            log("memberId: {}, birthDate: {}, memberStatus: {}, memberSecurity: {}", memberId, memberBirthdate, optMemberStatus, optMemberSecurity);
            assertTrue(optMemberStatus.isPresent());
            assertTrue(optMemberSecurity.isPresent());
        });
    }

    /**
     * 会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索 <br>
     * o 上に該当する会員が存在すること <br>
     * o リマインダ質問に2が含まれていること <br>
     * をチェックしたい <br>
     * ただし、検索時に会員セキュリティ情報のデータ自体は要らない
     */
    public void test_searchMemberBySecurityInfoReminderQuestion() throws Exception {
        // ## Arrange ##
        String reminderQuestionKeyword = "2";
    
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.specify().columnMemberId();
            cb.specify().columnMemberName();
            // TODO mayukorin [いいね] 素晴らしい by jflute (2025/01/20)
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch(reminderQuestionKeyword, op -> op.likeContain()); // dbfluteでは、関連テーブルを使いたい目的に対応するメソッドがあるらしい。今回の目的はカラム取得ではなく絞り込みなので、setUpSelect使う必要なし。
        });
    
        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            Integer memberId = member.getMemberId();

            // Assertするために、MemberSecurityInfoを取ってくる（でもmember分検索してしまっているの微妙かも）
            // TODO mayukorin ちゃすかに by jflute (2025/01/20)
            Member memberSelectedById = memberBhv.selectEntity(cb -> {
                cb.setupSelect_MemberSecurityAsOne();
                cb.query().setMemberId_Equal(memberId);
            }).get();
            MemberSecurity memberSecurity = memberSelectedById.getMemberSecurityAsOne().get(); // memberSecurityは存在すること前提（1個前のテストでそれをassertしてる）

            log("memberId: {}, memberSecurity: {}", memberId, memberSecurity);
            assertContains(memberSecurity.getReminderQuestion(), reminderQuestionKeyword);
        });
    }
}
