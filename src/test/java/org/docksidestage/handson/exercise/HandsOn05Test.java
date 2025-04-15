package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberAddressBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done mayukorin additionalForeignKeyMap.dfprop, 一部インデントがおかしい by jflute (2025/04/08)
// [1on1でのふぉろー] 設定ファイルこそ、綺麗にしておきたい話
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
            // done mayukorin [いいね] yes !!! by jflute (2025/04/08)
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
            // done jflute「会員と共に現在の住所を取得して検索」は、現住所が存在する会員を取得するイメージでしょうか？ 今の実装だと「会員と現住所が存在したら取得」になってしまっていて、assert で落ちるなと思ってますby m.inoue (2025/04/05)
            // done mayukorin [へんじ] いえ、「共に現在の住所」ということでくっつけるだけです。なので住所が存在しない人も検索対象です by jflute (2025/04/08)
            // done jflute 変えてみたのですが、下だと、setupSelect してなくてもassertが通るので、「会員住所情報が取得できていること」を確かめることにはなってないですかね？ by m.inoue (2025/04/13)
            // TODO mayukorin membersには住所が存在しない人も混ざっているが、"会員住所情報が取得できていることのアサート" では気にしなくていい by jflute (2025/04/15)
            assertTrue(member.getMemberAddressAsValid().isPresent());
            
            // [1on1でのふぉろー] これだと、getterが絶対にnot nullなので意味がないアサートになっている
            //assertNotNull(member.getMemberAddressAsValid());
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
            // TODO mayukorin MEMBER_ADDRESS自身が、REGION_IDを持ってるので、REGIONまで行かなくてもいい by jflute (2025/04/15)
            cb.query().queryMember().queryMemberAddressAsValid(currentLocalDate()).queryRegion().setRegionId_Equal_千葉();
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Member member = purchase.getMember().get();
            MemberAddress memberAddress = member.getMemberAddressAsValid().get();

            log("memberStatusName: {}, memberAddress: {}",
                    member.getMemberStatus().get().getMemberStatusName(), memberAddress.getAddress());

            // TODO mayukorin ここもおんなじ by jflute (2025/04/15)
            // [1on1でのふぉろー] 名前によるイメージではなく、構造に着目して欲しい
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
    
    // done jflute 業務的many-to-oneの話もする (2025/04/08)
    //
    // many-to-manyという言葉:
    //  o PURCHASE to MEMBER
    //  o PURCHASE to PRODUCT
    //
    // 具象的には、MEMBERとPRODUCTは、one-to-many-to-one
    // 抽象的には、MEMBERとPRODUCTは、many-to-many
    //
    // ここでは、many-to-manyの関係性のあるテーブル間の話になる。
    //
    // many-to-manyの関連を表現する業務:
    // PURCHASE以外にも...e.g. 会員お気に入り商品、会員ブロック商品、...
    // これらは、テーブルに落とし込むと、すべて会員IDと商品IDを持つテーブルになる。
    //
    // こういったAとBをつなぐ業務関連テーブルというのがたくさんできあがる
    // many-to-manyの対象が商品だけとも限らない、会員とレストランとかなんとかとか...
    // ということで、めちゃめちゃたくさん関連テーブルができあがる。
    //
    // もし、これをテーブル数が多いなぁと思って、一つのテーブルに集約したいと思っちゃったら...
    //
    // 会員汎用関連管理テーブル:
    // 会員IDは固定
    // 対象IDは変わってくる。商品IDだったりレストランIDだったり...
    // 対象IDが何の種類のIDなのか？を示す値として、別途対象種別コードを持つ
    //
    // o 会員ID: これは固定の会員ID
    // o 対象ID: 商品IDだったりレストランIDあったり // FK制約貼れない => オーソドックスなDB設計ではない
    // o 対象種別コード: e.g. 商品 or レストラン
    //
    // 関連テーブルを一つに集約することができる。(ただしリスクはあるけど)
    // このときのSQLは...
    // 相手をjoinするときに、基点テーブル側に固定条件を入れないといけなくなる。
    //
    // from 会員汎用関連管理 gene
    //   left outer join レストラン res
    //     on gene.対象ID = res.レストランID
    //    and gene.対象種別コード = 'レストラン'
    //
    // 業務的one-to-oneとの違い: 相手を絞るか、自分を絞るか
    // 業務的one-to-oneは、相手を絞る。
    // 業務的many-to-oneは、自分を絞って汎用的なIDを特定しないといけない。
    //  -> 一応、DBFluteで業務的many-to-oneも表現しようと思えばできる。(推奨するわけではないが)
}
