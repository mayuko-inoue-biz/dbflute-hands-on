package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberAddressBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO mayukorin additionalForeignKeyMap.dfprop, 一部インデントがおかしい by jflute (2025/04/08)
/**
 * DBFluteハンズオン05のためのクラス
 * @author mayukorin
 */
public class HandsOn05Test extends UnitContainerTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberAddressBhv memberAddressBhv;
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    // =================================`==================================================
    //                                                                 業務的one-to-oneとは？
    //                                                                           =========
    /**
     * 会員住所情報を検索 <br>
     * o 会員名称、有効開始日、有効終了日、住所、地域名称をログに出して確認する <br>
     * o 会員IDの昇順、有効開始日の降順で並べる <br>
     */
    public void test_searchMemberAddress() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<MemberAddress> memberAddresses = memberAddressBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Region();
            cb.query().addOrderBy_MemberId_Asc();
            cb.query().addOrderBy_ValidBeginDate_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberAddresses);
        memberAddresses.forEach(memberAddress -> {
            // Not Null の FK カラムなため、member・region は 必ず存在する
            // TODO mayukorin [いいね] yes !!! by jflute (2025/04/08)
            Member member = memberAddress.getMember().get();
            Region region = memberAddress.getRegion().get();

            log("name: {}, validBeginDate: {}, validEndDate: {}, address: {}, region: {}",
                    member.getMemberName(), memberAddress.getValidBeginDate(), memberAddress.getValidEndDate(), memberAddress.getAddress(), region.getRegionName());
        });
    }

    // ====================================================================================
    //                                                          業務的one-to-oneを利用した実装
    //                                                                           =========
    /**
     * 会員と共に現在の住所を取得して検索 <br>
     * o SetupSelectのJavaDocにcommentがあることを確認すること <br>
     * o 会員名称と住所をログに出して確認すること <br>
     * o 現在日付はスーパークラスのメソッドを利用 ("c" 始まりのメソッド) <br>
     * o 会員住所情報が取得できていることをアサート <br>
     */
    public void test_searchCurrentMemberAddress() throws Exception {
        // ## Arrange ##

        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberAddressAsValid(currentLocalDate());
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            log("memberName: {}, memberAddress: {}",
                    member.getMemberName(), member.getMemberAddressAsValid());
            // TODO done jflute「会員と共に現在の住所を取得して検索」は、現住所が存在する会員を取得するイメージでしょうか？ 今の実装だと「会員と現住所が存在したら取得」になってしまっていて、assert で落ちるなと思ってますby m.inoue (2025/04/05)
            // TODO mayukorin [へんじ] いえ、「共に現在の住所」ということでくっつけるだけです。なので住所が存在しない人も検索対象です by jflute (2025/04/08)
            assertTrue(member.getMemberAddressAsValid().isPresent());
        });
    }

    /**
     * 千葉に住んでいる会員の支払済み購入を検索 <br>
     * o 会員ステータス名称と住所をログに出して確認すること <br>
     * o 購入に紐づいている会員の住所の地域が千葉であることをアサート <br>
     */
    public void test_searchPaidPurchaseOfChibaMember() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberAddressAsValid(currentLocalDate()).withRegion();
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().queryMember().queryMemberAddressAsValid(currentLocalDate()).queryRegion().setRegionId_Equal_千葉();
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Member member = purchase.getMember().get();
            MemberAddress memberAddress = member.getMemberAddressAsValid().get();

            log("memberStatusName: {}, memberAddress: {}",
                    member.getMemberStatus().get().getMemberStatusName(), memberAddress.getAddress());

            assertTrue(memberAddress.getRegion().get().isRegionId千葉());
        });
    }

    // ====================================================================================
    //                                                          導出的one-to-oneを利用した実装
    //                                                                           =========
    // [1on1でのふぉろー] 現場での導出的one-to-oneを一緒に見た
    /**
     * 最終ログイン時の会員ステータスを取得して会員を検索 <br>
     * o SetupSelectのJavaDocに自分で設定したcommentが表示されることを目視確認 <br>
     * o 会員名称と最終ログイン日時と最終ログイン時の会員ステータス名称をログに出す <br>
     * o 最終ログイン日時が取得できてることをアサート <br>
     */
    public void test_searchMemberWithLastLoginMemberStatus() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberLoginAsLatest().withMemberStatus();
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            // TODO m.inoue これも、searchCurrentMemberAddress と同じ問題で、最終ログイン時がない会員も取ってきてるので、例外になっている。「最終ログイン時がある会員においてちゃんと最終ログインが取得できていること」を確かめれば良い？ (2025/04/05)
            MemberLogin memberLoginAsLatest = member.getMemberLoginAsLatest().get();
            MemberStatus memberStatusAsLatest = memberLoginAsLatest.getMemberStatus().get();

            log("memberName: {}, lastLoginDatetime: {}, lastLoginMemberStatusName: {}",
                    member.getMemberName(), memberLoginAsLatest.getLoginDatetime(), memberStatusAsLatest.getMemberStatusName());
            assertNotNull(memberLoginAsLatest.getLoginDatetime());
        });
    }
    
    // TODO jflute 業務的many-to-oneの話もする (2025/04/08)
}
