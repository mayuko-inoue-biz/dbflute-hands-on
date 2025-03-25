package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.exception.RelationEntityNotFoundException;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
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
            // done mayukorin [いいね] うむ by jflute (2025/03/17)
            // NotNullのFKカラムなため、以下は必ず存在する
            Product product = purchase.getProduct().get();
            Member member = purchase.getMember().get();

            log("unpaid product: {}, member: {}, paymentCompleteFlg: {}", product.getProductName(), member.getMemberName(), purchase.getPaymentCompleteFlg());

            // done mayukorin [いいね] yes, ぜひ判定メソッドうまく使いこなしてくださいませ by jflute (2025/03/17)
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
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        // TODO mayukorin existsがあれば、isなくてもいいです (慣習として) by jflute (2025/03/25)
        // booleanを表現する単語として、三単現の動詞であれば、booleanっぽくなる。
        //  e.g. exists, has が代表選手、他だと e.g. can, may の助動詞も使われる。
        // そしてデフォルトがisみたいな感じ。(いい言葉が思い浮かばなかったらisみたいな!?)
        // まあ、isは状態を示すものなので、状態がしっくり来る場合は積極的に使う。
        boolean isExistsWDLMember = false;
        assertHasAnyElement(members);

        // [1on1でのふぉろー] リストを分割して、それぞれでチェックを掛けるっていうのもアリ。
        // そうすることで、全体としては処理が増えるけど、個別で処理がシンプルになって簡単に考えやすくなるかもしれない。
        //wdlMemberList
        //assertHasAnyElement(wdlMemberList);
        //
        //otherMemberList
        //assertHasAnyElement(otherMemberList);

        for(Member member : members) {
            OptionalEntity<MemberWithdrawal> optMemberWithdrawal = member.getMemberWithdrawalAsOne();
            log("member: {}, memberWithdrawal: {}", member.getMemberName(), optMemberWithdrawal);

            // if (member.getMemberStatusCode().equals("WDL")) { // 退会会員
            if (member.isMemberStatusCode退会会員()) {
//              // done mayukorin [いいね] 退会情報がそもそも取得してなかったら、退会会員でない会員の方アサートが意味ないですもんね by jflute (2025/03/17)
                // [1on1でのふぉろー] setupSelectし忘れてたら？のお話
                // ただ、まゆこりんさんの実装だと、たまたま？意図して？ちゃんと落ちる。
                // expected: RelationEntityNotFoundException but: NonSetupSelectRelationAccessException
                // アサートしている例外がちゃんとピンポイントだから。
                // ということで、assertNotNull(op... 自体は、setupSelectしていることを保証する重要なアサートでした。
                // (アサートを保証するアサートみたいな)
                
                // done mayukorin 厳密には、会員退会情報を持ってない場合は、get()の時点で落ちるので... by jflute (2025/03/17)
                // optMemberWithdrawalがpresentかどうかをアサートする方が論理的には合っています。
//                assertNotNull(optMemberWithdrawal.get().getWithdrawalReason()); // 会員退会情報を持っていることをアサート（仮に持ってない場合（データ不備）落ちるように）
                assertTrue(optMemberWithdrawal.isPresent());
                // TODO mayukorin setupSelectをし忘れてて、かつ、退会会員が一人もいなかったら by jflute (2025/03/25)
            } else { // 退会会員でない会員
                // done mayukorin テストデータが偏っていたら、ここの分岐に入る保証がない by jflute (2025/03/17)
                // ほんとですね！素通り防止忘れてました！ありがとうございます！ by mayukorin
                assertException(RelationEntityNotFoundException.class, () -> optMemberWithdrawal.get()); // 会員退会情報を持っていないことをアサート
                isExistsWDLMember = true;

                // setupSelectされているからこそ、ちゃんと業務的なアサートになる
                // (し忘れると、そもそも誰もwithdrawalを持ってないので、意味のないアサートになっちゃう)
                //assertFalse(optMemberWithdrawal.isPresent()); // わかりやすく説明するために追加 by jflute
            }
        }
        assertTrue(isExistsWDLMember);
        
        // [1on1でのふぉろー]
        // o 論理をわかってて省く人と、よくわかってなくて自然と省いちゃっただけの人
        // o 省くならわかってて省いて欲しい

        // [1on1でのふぉろー]
        // o 英語の文法、どこまで反映する話
        // o EntityAlreadyDeletedExceptionの最初の失敗談のお話
        // o 英語の新聞の見出しのお話
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
        // done mayukorin 修行++: 同率首位の人がいたら一緒に検索するようにしてみましょう by jflute (2025/03/17)
        // [1on1でのふぉろー] fetchFirst(1)も業務で使うときあるけど...order byがユニークじゃないのでランダム性があるのは良くない。
        // もし、fetchFirst(1)やるなら、第二ソートキーとしてIDを割り切りで入れるとか。(order byのユニーク性という概念)
//        OptionalEntity<Member> optMember = memberBhv.selectEntity(cb -> {
//            cb.setupSelect_MemberStatus();
//            cb.query().setMemberStatusCode_Equal_仮会員();
//            cb.query().addOrderBy_Birthdate_Desc();
//            cb.fetchFirst(1);
//        });
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.query().scalar_Equal().max(memberCB -> { // 「あるカラムと そのカラムの導出値(最大値や合計値) との比較で絞り込む」と書いてあるから、今回は specify() した Birthdate と比較する by mayukorin
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_仮会員();
            });
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            MemberStatus memberStatus = member.getMemberStatus().get();
            log("member: {}, birthday: {}, memberStatus: {}", member.getMemberName(), member.getBirthdate(), memberStatus.getMemberStatusName());
            assertTrue(member.isMemberStatusCode仮会員());
        });
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
            // done mayukorin 外側の条件が一つ足りない。紛れが起きてしまいます by jflute (2025/03/17)
            // ありがとうございます！同じ誕生日の正式会員ではない会員の購入が入ってしまう可能性があるということですね！ by mayukorin
            cb.query().queryMember().setMemberStatusCode_Equal_正式会員();
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Member member = purchase.getMember().get();
            MemberStatus memberStatus = member.getMemberStatus().get();
            log("purchase; {}, paymentCompleteFlg: {}, purchaseDatetime: {}, member: {}, birthday: {}, memberStatusCode: {}",
                    purchase.getPurchaseId(), purchase.getPaymentCompleteFlgAsFlg(), purchase.getPurchaseDatetime(), member.getMemberName(), member.getBirthdate(), memberStatus.getMemberStatusName());
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
            // done mayukorin 謎の空行 by jflute (2025/03/17)
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Product product = purchase.getProduct().get();
            ProductStatus productStatus = product.getProductStatus().get();
            Member member = purchase.getMember().get();
            OptionalEntity<MemberWithdrawal> optMemberWithdrawalAsOne = member.getMemberWithdrawalAsOne();
            // [1on1でのふぉろー] 頭文字省略語のお話
            // 個人的には、mwってなんだ？少しだけ読み手に負担を掛ける気がしている。
            // SQLの方の話も先取り
            // SQLのエリアス名、頭文字省略は...うーん
            // https://jflute.hatenadiary.jp/entry/20140908/sqlalias
            // ↑こういうこと考えながら名前って考える
            String withdrawalReasonText = optMemberWithdrawalAsOne.map(mw -> mw.getWithdrawalReason().map(wr -> wr.getWithdrawalReasonText()).orElse("none"))
                    .orElse("none"); // 値がない場合は none
            // done mayukorin どこかで改行するか...map()のところを変数に切り出すか...してもらえるとありがたいです by jflute (2025/03/17)
            log("purchase; {}, productStatus: {}, withdrawal reason: {}", product.getProductName(), productStatus.getProductStatusName(), withdrawalReasonText);
            assertTrue(product.isProductStatusCode生産販売可能());
        });
    }

    /**
     * 正式会員と退会会員の会員を検索 <br>
     * o 会員ステータスの表示順で並べる <br>
     * o 会員が正式会員と退会会員であることをアサート <br>
     * o 両方とも存在していることをアサート <br>
     * o (検索されたデータに対して)Entity上だけで正式会員を退会会員に変更する <br>
     * o 変更した後、Entityが退会会員に変更されていることをアサート <br>
     * o 変更した後、データベース上は退会会員に変更されて "いない" ことをアサート ※1 <br>
     * ※1: DBFluteは、Entityのデータを変更しても、updateをしない限りDBは不変である。(Thanks, nakano)
     */
    public void test_searchFMLAndRPVMember() throws Exception {
        // ## Arrange ##

        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            // TODO mayukorin 修行++: InScopeを使ったやり方もやってみてください by jflute (2025/03/25)
            // 同じカラムで、複数の等値条件だったら、SQLだと in が使えます。(DBFluteではInScope)
            cb.orScopeQuery(orCB -> {
                orCB.query().setMemberStatusCode_Equal_正式会員();
                orCB.query().setMemberStatusCode_Equal_退会会員();
            });
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
        });

        // ## Assert ##
        boolean isExistsFMLMember = false;
        boolean isExistsWDLMember = false;
        Member fmlMemberToChangeOnEntity = null;

        assertHasAnyElement(members);

        for(Member member : members)  {
            log("member: {}, status", member.getMemberName(), member.getMemberStatusCode());

            assertTrue(member.isMemberStatusCode正式会員() || member.isMemberStatusCode退会会員());

            if (member.isMemberStatusCode正式会員()) {
                if (!isExistsFMLMember) {
                    fmlMemberToChangeOnEntity = member; // 「Entity上だけで正式会員を退会会員に変更する」用の会員をセット
                }
                isExistsFMLMember = true;
            } else if (member.isMemberStatusCode退会会員()) { // 何回も isMemberStatus 呼び出してるの微妙...？ by mayukorin
                // ↑ふぉろー: isメソッドは中身の処理は簡易なので、何度も呼び出してもパフォーマンス上問題はないです by jflute
                isExistsWDLMember = true;
            }
        }

        assertTrue(isExistsFMLMember);
        assertTrue(isExistsWDLMember);

        fmlMemberToChangeOnEntity.setMemberStatusCode_退会会員(); // Entity上だけで正式会員を退会会員に変更する
        assertTrue(fmlMemberToChangeOnEntity.isMemberStatusCode退会会員()); // Entityが退会会員に変更されていることをアサート

        // 変更した後、データベース上は退会会員に変更されて "いない" ことをアサート
        Integer fmlMemberIdChangeOnEntity = fmlMemberToChangeOnEntity.getMemberId();
        Member fmlMemberChangeOnEntitySelectedFromDB = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(fmlMemberIdChangeOnEntity);
        }).get();
        assertFalse(fmlMemberChangeOnEntitySelectedFromDB.isMemberStatusCode退会会員());
        
        // [1on1でのふぉろー]
        // o Entityをいくら変更しても、DBは変わらない
        // o あくまで Behavior で update やってはじめてDBが変わる
        // o EntityはJavaのメモリ上の入れ物という感じ
        //
        // o 世の中、そうじゃないO/Rマッパーもあります
        // o setterで値を書き換えたら、裏側で(タイミングは色々だけど)updateを自動で走らすものもある
        // o その場合、EntityはDBと直結した入れ物みたいな感覚
        //
        // o DBFluteは、updateという振る舞いを開発者に意識してもらう、タイミングを明確にしてもらう
        // o DBFluteは、updateによるわかりやすさを重視している (setだけだと追うのが大変)
        //
        // o フレームワークの思想で、フレームワークの仕様が変わる
        // o そのフレームワークは何を大事にしているのか？それを理解すると機能が理解しやすくなる
    }

    // TODO jflute 次回1on1ここから (2025/03/25)
    /**
     * 銀行振込で購入を支払ったことのある、会員ステータスごとに一番若い会員を検索 <br>
     * o 正式会員で一番若い、仮会員で一番若い、という風にそれぞれのステータスで若い会員を検索 <br>
     * o 一回の ConditionBean による検索で会員たちを検索すること (PartitionBy...) <br>
     * o ログのSQLを見て、検索が妥当であることを目視で確認すること <br>
     * o (検索されたデータに対して)Entity上だけで正式会員を退会会員に変更する <br>
     * o 検索結果が想定されるステータスの件数以上であることをアサート <br>
     * o ひとまず動作する実装ができたら、ArrangeQueryを活用してみましょう <br>
     */
    public void test_searchYoungestMemberByStatusUsingBankTransfer() throws Exception {
        // ## Arrange ##
    
        // ## Act ##
        boolean isExistsFMLMember = false;
        boolean isExistsWDLMember = false;
        boolean isExistsRPVMember = false;

        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().arrangeBankTransferPaidMember();
            }).partitionBy(colCB -> {
                colCB.specify().columnMemberStatusCode();
            });
            cb.query().arrangeBankTransferPaidMember();
            // CBインスタンスは再利用しない!?を読んだ
            // https://dbflute.seasar.org/ja/manual/function/genbafit/implfit/whererecycle/index.html
            // 「どういう風に絞り込むか？は再利用できることが多い一方で、何を取得するか？は一概に共通化できない。」
            // 確かに。by mayukorin
        });

        // ## Assert ##
        assertHasAnyElement(members);
        for(Member member : members) {
           log("member: {}, status: {}, birthday: {}", member.getMemberName(), member.getMemberStatusCode(), member.getBirthdate());
           if (member.isMemberStatusCode正式会員()) {
               isExistsFMLMember = true;
           } else if (member.isMemberStatusCode退会会員()) {
               isExistsWDLMember = true;
           } else {
               isExistsRPVMember = true;
           }
        }

        assertTrue(members.size() >= CDef.MemberStatus.listAll().size());

        assertTrue(isExistsFMLMember);
        assertTrue(isExistsWDLMember);
        assertTrue(isExistsRPVMember); // これ入れるんだったら上の、members.size() >= CDef.MemberStatus.listAll().size() は不要かも by mayukorin
    }
}
