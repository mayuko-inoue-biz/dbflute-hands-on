package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.exception.RelationEntityNotFoundException;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberWithdrawal;
import org.docksidestage.handson.dbflute.exentity.Product;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * DBFluteハンズオン04のためのクラス
 * @author mayukorin
 */
public class HandsOn04Test extends UnitContainerTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private PurchaseBhv purchaseBhv;
    @Resource
    private MemberBhv memberBhv;

    // =================================`==================================================
    //                                                                       ベタベタのやり方
    //                                                                           =========
    /**
     * 退会会員の未払い購入を検索 <br>
     * o 退会会員のステータスコードは "WDL"。ひとまずベタで <br>
     * o 支払完了フラグは "0" で未払い。ひとまずベタで <br>
     * o 購入日時の降順で並べる <br>
     * o 会員名称と商品名と一緒にログに出力 <br>
     * o 購入が未払いであることをアサート <br>
     */
    public void test_searchUnpaidOfWithdrawalMember() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Product();
//            cb.query().queryMember().setMemberStatusCode_Equal("WDL");
            cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
//            cb.query().setPaymentCompleteFlg_Equal(0);
            cb.query().setPaymentCompleteFlg_Equal_False();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            // TODO mayukorin [いいね] うむ by jflute (2025/03/17)
            // NotNullのFKカラムなため、以下は必ず存在する
            Product product = purchase.getProduct().get();
            Member member = purchase.getMember().get();

            log("unpaid product: {}, member: {}, paymentCompleteFlg: {}", product.getProductName(), member.getMemberName(), purchase.getPaymentCompleteFlg());

            // TODO mayukorin [いいね] yes, ぜひ判定メソッドうまく使いこなしてくださいませ by jflute (2025/03/17)
//            assertEquals(0, purchase.getPaymentCompleteFlg());
            assertTrue(purchase.isPaymentCompleteFlgFalse()); // 区分値を定義したら、判定メソッドも自動生成してくれるのか！ by mayukorin
        });
    }
    
    // [1on1でのふぉろー] 現場での区分値も照らし合わせてみた
    // o アプリ区分値のお話
    // o dfpropファイル分割のお話
    // o commentのお話 (おうむ返しコメント残念話)

    /**
     * 会員退会情報も取得して会員を検索 <br>
     * o 退会会員でない会員は、会員退会情報を持っていないことをアサート <br>
     * o 退会会員のステータスコードは "WDL"。ひとまずベタで <br>
     * o 不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと <br>
     */
    public void test_searchMemberWithMemberWithdrawal() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            //cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            OptionalEntity<MemberWithdrawal> optMemberWithdrawal = member.getMemberWithdrawalAsOne();
            log("member: {}, memberWithdrawal: {}", member.getMemberName(), optMemberWithdrawal);

            // if (member.getMemberStatusCode().equals("WDL")) { // 退会会員
            if (member.isMemberStatusCode退会会員()) {
//              // TODO mayukorin [いいね] 退会情報がそもそも取得してなかったら、退会会員でない会員の方アサートが意味ないですもんね by jflute (2025/03/17)
                // [1on1でのふぉろー] setupSelectし忘れてたら？のお話
                // ただ、まゆこりんさんの実装だと、たまたま？意図して？ちゃんと落ちる。
                // expected: RelationEntityNotFoundException but: NonSetupSelectRelationAccessException
                // アサートしている例外がちゃんとピンポイントだから。
                // ということで、assertNotNull(op... 自体は、setupSelectしていることを保証する重要なアサートでした。
                // (アサートを保証するアサートみたいな)
                
                // TODO mayukorin 厳密には、会員退会情報を持ってない場合は、get()の時点で落ちるので... by jflute (2025/03/17)
                // optMemberWithdrawalがpresentかどうかをアサートする方が論理的には合っています。
                assertNotNull(optMemberWithdrawal.get().getWithdrawalReason()); // 会員退会情報を持っていることをアサート（仮に持ってない場合（データ不備）落ちるように）
            } else { // 退会会員でない会員
                // TODO mayukorin テストデータが偏っていたら、ここの分岐に入る保証がない by jflute (2025/03/17)
                assertException(RelationEntityNotFoundException.class, () -> optMemberWithdrawal.get()); // 会員退会情報を持っていないことをアサート

                // setupSelectされているからこそ、ちゃんと業務的なアサートになる
                // (し忘れると、そもそも誰もwithdrawalを持ってないので、意味のないアサートになっちゃう)
                //assertFalse(optMemberWithdrawal.isPresent()); // わかりやすく説明するために追加 by jflute
            }
        });
    }

    // ====================================================================================
    //                                                               区分値メソッドを使って実装
    //                                                                           ==========
    /**
     * 一番若い仮会員の会員を検索 <br>
     * o 区分値メソッドの JavaDoc コメントを確認する <br>
     * o 会員ステータス名称も取得する(ログに出力) <br>
     * o 会員が仮会員であることをアサート <br>
     * o できれば、テストメソッド内の検索回数は一回で... <br>
     */
    public void test_searchYoungestPRVMember() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        // TODO mayukorin 修行++: 同率首位の人がいたら一緒に検索するようにしてみましょう by jflute (2025/03/17)
        // [1on1でのふぉろー] fetchFirst(1)も業務で使うときあるけど...order byがユニークじゃないのでランダム性があるのは良くない。
        // もし、fetchFirst(1)やるなら、第二ソートキーとしてIDを割り切りで入れるとか。(order byのユニーク性という概念)
        OptionalEntity<Member> optMember = memberBhv.selectEntity(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.query().addOrderBy_Birthdate_Desc();
            cb.fetchFirst(1);
        });

        // ## Assert ##
        Member member = optMember.get();
        log("member: {}, birthday: {}, memberStatus: {}", member.getMemberName(), member.getBirthdate(), member.getMemberStatus().get().getMemberStatusName());
        assertTrue(member.isMemberStatusCode仮会員());
    }

    /**
     * 支払済みの購入の中で一番若い正式会員のものだけ検索 <br>
     * o 会員ステータス名称も取得する(ログに出力) <br>
     * o 購入日時の降順で並べる <br>
     * o 購入の紐づいている会員が正式会員であることをアサート <br>
     * ※これ難しい...かも!? (解釈に "曖昧さ" あり、実際にデータが存在している方を優先) <br>
     */
    public void test_searchPaidOfYoungestFMLMember() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.query().queryMember().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_正式会員();
                memberCB.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().setPaymentCompleteFlg_Equal_True();
                });
            });
            // TODO mayukorin 外側の条件が一つ足りない。紛れが起きてしまいます by jflute (2025/03/17)
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Member member = purchase.getMember().get();
            log("purchase; {}, paymentCompleteFlg: {}, purchaseDatetime: {}, member: {}, birthday: {}, memberStatusCode: {}",
                    purchase.getPurchaseId(), purchase.getPaymentCompleteFlgAsFlg(), purchase.getPurchaseDatetime(), member.getMemberName(), member.getBirthdate(), member.getMemberStatus().get().getMemberStatusName());
            assertTrue(member.isMemberStatusCode正式会員());
        });
    }

    /**
     * 生産販売可能な商品の購入を検索 <br>
     * o 商品ステータス名称、退会理由テキスト (退会理由テーブル) も取得する(ログに出力) ※1 <br>
     * o 購入価格の降順で並べる <br>
     * o 購入の紐づいている商品が生産販売可能であることをアサート <br>
     * ※1: ログについて、値がない項目は "none" を出力。if文使わないように。ヒント: Java8なら flatMap()
     */
    public void test_searchPurchaseOfSalePossibleProduct() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Member().withMemberWithdrawalAsOne().withWithdrawalReason();
            cb.query().queryProduct().setProductStatusCode_Equal_生産販売可能();
            cb.query().addOrderBy_PurchasePrice_Desc();
            // TODO mayukorin 謎の空行 by jflute (2025/03/17)

        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Product product = purchase.getProduct().get();
            Member member = purchase.getMember().get();
            OptionalEntity<MemberWithdrawal> optMemberWithdrawalAsOne = member.getMemberWithdrawalAsOne();
            // TODO mayukorin どこかで改行するか...map()のところを変数に切り出すか...してもらえるとありがたいです by jflute (2025/03/17)
            log("purchase; {}, productStatus: {}, withdrawal reason: {}", product.getProductName(), product.getProductStatus().get().getProductStatusName(), optMemberWithdrawalAsOne.map(mw -> mw.getWithdrawalReason().map(wr -> wr.getWithdrawalReasonText()).orElse("none")).orElse("none"));
            assertTrue(product.isProductStatusCode生産販売可能());
        });
    }
}
