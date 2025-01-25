package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * DBFluteハンズオン03のためのクラス
 * @author mayukorin
 */
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

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
        LocalDate birthdateForSearchPlusOneDay = birthdateForSearch.plusDays(1);
    
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // TODO done mayukorin [いいね] メソッドの呼び出し順序、いいですね！ by jflute (2025/01/20)
            // 実装順序は、データの取得、絞り込み、並び替え by jflute
            //  => http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus();
            cb.specify().columnMemberName();
            cb.specify().columnBirthdate();
            // TODO mayukorin specify[テーブル]だけやっても意味がないコードになります。カラムまで指定しないと。 by jflute (2025/01/20)
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            cb.query().setMemberName_LikeSearch(memberNamePrefixForSearch, op -> op.likePrefix());
            // TODO done mayukorin [tips] おおぉ、いきなり高度な機能を！でもできてますね。 by jflute (2025/01/20)
            // でもここでは lessEqual でも大丈夫でしたということで
            // あ、なるほど！ lessEqual でもできるのですね！
            // [思い出]
//            cb.query().setBirthdate_FromTo(null, birthdateForSearch, op -> {
//                op.allowOneSide(); // 指定日以前に生まれた人を検索したくて、何日以降に生まれたかは問わない
//                op.compareAsDate();
//            });
            cb.query().setBirthdate_LessEqual(birthdateForSearch);
            cb.query().addOrderBy_Birthdate_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            String memberName = member.getMemberName();
            // TODO done mayukorin ここはmemberを付けなくてもいいかなと。birthdateで十分member限定感あるので by jflute (2025/01/20)
            LocalDate birthdate = member.getBirthdate();
            MemberStatus memberStatus = member.getMemberStatus().get();
            log("memberName: {}, memberBirthdate: {}, memberStatusCodeName: {}, memberNamePrefixForSearch: {}, fromBirthDateForSearch: {}", memberName, birthdate, memberStatus.getMemberStatusName(), memberNamePrefixForSearch, birthdateForSearch);
            assertTrue(memberName.startsWith(memberNamePrefixForSearch));
            // TODO done mayukorin 細かいですが、ループの中で毎回同じ処理 plusDays(1) を実行してしまうのが無駄なので、ループ外に出しましょう by jflute (2025/01/20)
            assertTrue(birthdate.isBefore(birthdateForSearchPlusOneDay)); // 生年月日が指定日時ぴったりでもOK
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
            LocalDate birthdate = member.getBirthdate();
            OptionalEntity<MemberStatus> optMemberStatus = member.getMemberStatus();
            OptionalEntity<MemberSecurity> optMemberSecurity = member.getMemberSecurityAsOne();
            log("memberId: {}, birthdate: {}, memberStatus: {}, memberSecurity: {}", memberId, birthdate, optMemberStatus, optMemberSecurity);
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
            // TODO done mayukorin [いいね] 素晴らしい by jflute (2025/01/20)
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch(reminderQuestionKeyword, op -> op.likeContain()); // dbfluteでは、関連テーブルを使いたい目的に対応するメソッドがあるらしい。今回の目的はカラム取得ではなく絞り込みなので、setUpSelect使う必要なし。
        });
    
        // ## Assert ##
        assertHasAnyElement(memberList);

        // 1回の検索で、 memberList に紐づく memberSecurity を持ってくるようにした
        List<Integer> memberIds = memberList.stream().map(member -> member.getMemberId()).collect(Collectors.toList());
        ListResultBean<MemberSecurity> memberSecurities = memberSecurityBhv.selectList(cb -> {
            cb.specify().columnMemberId();
            cb.specify().columnReminderQuestion();
            cb.query().setMemberId_InScope(memberIds);
        });

        for (MemberSecurity memberSecurity : memberSecurities) {
            Integer memberId = memberSecurity.getMemberId();
            String reminderQuestion = memberSecurity.getReminderQuestion();
            log("memberId: {}, reminderQuestion: {}", memberId, reminderQuestion);
            assertContains(memberSecurity.getReminderQuestion(), reminderQuestionKeyword);
        }

        // [思い出]
//        memberList.forEach(member -> {
//            Integer memberId = member.getMemberId();
//
//            // Assertするために、MemberSecurityInfoを取ってくる（でもmember分検索してしまっているの微妙かも）
//            // TODO done mayukorin ちゃすかに by jflute (2025/01/20)
//            // TODO done mayukorin [読み物課題] 単純な話、getであんまり検索したくない by jflute (2025/01/20)
//            // https://jflute.hatenadiary.jp/entry/20151020/stopgetselect
//            Member memberSelectedById = memberBhv.selectEntity(cb -> {
//                cb.setupSelect_MemberSecurityAsOne();
//                cb.query().setMemberId_Equal(memberId);
//            }).get();
//            MemberSecurity memberSecurity = memberSelectedById.getMemberSecurityAsOne().get(); // memberSecurityは存在すること前提（1個前のテストでそれをassertしてる）
//
//            log("memberId: {}, memberSecurity: {}", memberId, memberSecurity);
//            assertContains(memberSecurity.getReminderQuestion(), reminderQuestionKeyword);
//        });
    }

    /**
     * 会員ステータスの表示順カラムで会員を並べて検索 <br>
     * 会員ステータスのデータ自体は要らない <br>
     * その次には、会員の会員IDの降順で並べる <br>
     * o 上に該当する会員が存在すること <br>
     * o 会員ステータスのデータが取れていないこと <br>
     * o 会員が会員ステータスごとに固まって並んでいること
     * をチェックしたい <br>
     */
    public void test_searchMemberOrderByStatusDisplayOrder() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.specify().columnMemberId();
            cb.specify().columnMemberStatusCode();
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        List<String> memberStatusCodeList = new ArrayList<>();

        for (Member member : members) {
            Integer memberId = member.getMemberId();
            String memberStatusCode = member.getMemberStatusCode();
            log("memberId: {}, memberStatusCode: {}", memberId, memberStatusCode);

            assertTrue(member.getMemberStatus().isEmpty()); // 会員ステータスのデータが取れていないことをチェック

            // 取得した会員の順に memberStatusCode を取り出して、memberStatusCodeList に加える
            // ただし、直前に追加した memberStatusCode と同じであれば、List には加えない
            // 例：[FML, FML, WDL, PRV, FML] の場合、memberStatusCodeList には [FML, WDL, PRV, FML] が入る
            if (memberStatusCodeList.isEmpty()) {
                memberStatusCodeList.add(memberStatusCode);
                continue;
            }
            String currentStatusCode = memberStatusCodeList.get(memberStatusCodeList.size() - 1);
            if (currentStatusCode.equals(memberStatusCode)) continue;
            memberStatusCodeList.add(memberStatusCode);
        }

        // 会員が会員ステータスごとに固まって並んでいることをチェックしていく
        log("memberStatusCodeList: {}", memberStatusCodeList);
        List<String> UniqueMemberStatusCodeList = new ArrayList<>();
        for (String statusCode : memberStatusCodeList) {
            log("statusCode: {}", statusCode);
            assertFalse(UniqueMemberStatusCodeList.contains(statusCode));  // UniqueMemberStatusCodeList に既に値が存在していたら、会員ステータスが飛び飛びに並んでいることになってしまう
            UniqueMemberStatusCodeList.add(statusCode);
        }
    }

    /**
     * 生年月日が存在する会員の購入を検索 <br>
     * o 会員名称と会員ステータス名称と商品名を取得する(ログ出力) <br>
     * o 購入日時の降順、購入価格の降順、商品IDの昇順、会員IDの昇順で並べる <br>
     * o OrderBy がたくさん追加されていることをログで目視確認すること <br>
     * o 購入に紐づく会員の生年月日が存在することをアサート <br>
     */
    public void test_searchPurchaseByBirthdayExistsMember() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Product();
            cb.specify().specifyMember().columnMemberName();
            cb.specify().specifyMember().columnBirthdate();
            cb.specify().specifyMember().specifyMemberStatus().columnMemberStatusName();
            cb.specify().specifyProduct().columnProductName();
            cb.query().queryMember().setBirthdate_IsNotNull();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.query().addOrderBy_PurchasePrice_Desc();
            cb.query().queryProduct().addOrderBy_ProductId_Asc();
            cb.query().queryMember().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        purchases.forEach(purchase -> {
            // 紐づく member・memberStatus・product は必ず存在する
            Member member = purchase.getMember().get();
            MemberStatus memberStatus = member.getMemberStatus().get();
            Product product = purchase.getProduct().get();

            log("memberName: {}, memberStatusName: {}, productName: {}", member.getMemberName(), memberStatus.getMemberStatusName(), product.getProductName());
            assertNotNull(member.getBirthdate());
        });
    }

    
}
