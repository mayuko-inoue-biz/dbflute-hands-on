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
            cb.query().queryMember().setMemberStatusCode_Equal("WDL");
            cb.query().setPaymentCompleteFlg_Equal(0);
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            // NotNullのFKカラムなため、以下は必ず存在する
            Product product = purchase.getProduct().get();
            Member member = purchase.getMember().get();

            log("unpaid product: {}, member: {}, paymentCompleteFlg: {}", product.getProductName(), member.getMemberName(), purchase.getPaymentCompleteFlg());

            assertEquals(0, purchase.getPaymentCompleteFlg());
        });
    }

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
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            OptionalEntity<MemberWithdrawal> optMemberWithdrawal = member.getMemberWithdrawalAsOne();
            log("member: {}, memberWithdrawal: {}", member.getMemberName(), optMemberWithdrawal);

            if (member.getMemberStatusCode().equals("WDL")) { // 退会会員
                assertNotNull(optMemberWithdrawal.get().getWithdrawalReason()); // 会員退会情報を持っていることをアサート（仮に持ってない場合（データ不備）落ちるように）
            } else { // 退会会員でない会員
                assertException(RelationEntityNotFoundException.class, () -> optMemberWithdrawal.get()); // 会員退会情報を持っていないことをアサート
            }
        });
    }
}
