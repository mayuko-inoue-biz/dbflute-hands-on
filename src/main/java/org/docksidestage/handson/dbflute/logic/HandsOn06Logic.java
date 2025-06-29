package org.docksidestage.handson.dbflute.logic;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* [1on1でのふぉろー]
[有償]
Oracle (Oracle) :: 大文字
SQLServer (Microsoft) :: どっちでもない (内部的には区別してる) // MemberStatus
DB2 (IBM) :: 大文字

[OSS]
MySQL (Oracle) :: デフォルトどっちでもない、設定入れると小文字
　→ しかも、デフォルトだとSQLも大文字・小文字を区別することがある
　→ デフォルト、Linux/Macだと区別する、Windowsだと区別しない
　→ 設定入れると、小文字ベースになって、SQLで大文字・小文字を区別しなくなる
PostgreSQL (Community) :: 小文字

※基本的にはみんなSQLは大文字・小文字を区別しない


e.g. MEMBER_STATUS
why not MemberStatus
ans. create table MemberStatus → MEMBERSTATUS
 */

/* [1on1でのふぉろー]
pp conditionBean の名前の由来。なぜ「Bean」なのか、Spring の「Bean」と関係ある？
o そもそもJavaBeansという技術仕様がある (Javaはコーヒー？ということは豆？)
o ただし、JavaBeans自体がそこまで広まってないので、Beanはアドリブ的に入れ物クラスで広まってる
o SpringのBeanは、実質DIコンポーネントだけども、2004年発のフレームワークなので...
 i おそらくJavaBeansのニュアンスに影響を受けているのかな!?
o DBFluteのConditionBean (2005年くらい) は、まあSpringと似た感じ、若干入れ物寄り
 i 検索条件を指定するという振る舞いもあるけど、検索条件の情報を保持する入れ物クラスとも言える
 i Beanって最後に付けると、イメージが湧きやすかった!? (少なくとも当時は)
 i 結果的に良かったとは思っている。Conditionだけだと一般用語で認識しづらい用語になるけど...
 i ConditionBeanってのは固有名詞で独自の言葉なので、それだけでDBFluteと特定できる
 i (他のツールの用語と比較的かぶらない)
 i DBFluteは他にも独自の言葉になるように命名しているものが多くある
 i 業務的one-to-one, Decomment, ...
 i Beanは、言いやすい/聞きとりやすい言葉であるので、使いやすかった

pp BsクラスとExクラスの違い
o ジェネレーションギャップパターンのお話
o 教育番組は昔3チャンネルだった
o Exクラスの意義の話に加えて、vmの制御のコードまで見てもらった

pp 現場のEntityのお話
o おそらくデータの出どころの隠蔽
 */
/**
 * DBFluteハンズオン06のためのクラス
 * @author mayukorin
 */
public class HandsOn06Logic {

    private static final Logger logger = LoggerFactory.getLogger(HandsOn06Logic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    // ===================================================================================
    //                                                                               Logic
    //                                                                           =========
    /**
     * 指定された suffix で会員名称を後方一致検索 <br>
     * o 会員名称の昇順で並べる <br>
     * o suffixが無効な値なら例外: IllegalArgumentException <br>
     * o 会員名称、生年月日、正式会員日時を画面に表示する想定でログ出力 (Slf4j or CommonsLogging) <br>
     * o そのログのログレベル、INFO/DEBUGどちらがいいか考えて実装してみましょう (この先ずっと同じ) <br>
     * o このメソッドは、他の人が呼び出すことを想定して public にしましょう (この先ずっと同じ) <br>
     */
    public ListResultBean<Member> selectSuffixMemberList(String suffix) {
        if (suffix == null || suffix.trim().isEmpty()) {
            throw new IllegalArgumentException("suffixには、nullと空文字とトリムして空文字になる値は指定できません。指定されたsuffix: " + suffix);
        }

        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(suffix, op -> op.likeSuffix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        members.forEach(member -> {
            // info：本番のアプリケーションで必要な情報、debug：開発用にデバックのために出すイメージ
            // 今回、会員名称、生年月日、正式会員日時をログに出力するのはテストで落ちたとき使う目的だろうと思ったので、debugにしてみた。
            logger.debug("name: {}, birthdate: {}, formalizedDatetime: {}", member.getMemberName(), member.getBirthdate(), member.getFormalizedDatetime());
        });

        return members;
    }
}

/* [1on1でのふぉろー]
o 「Actionの作り方 (HTMLスタイル)」をやってみた
o https://dbflute.seasar.org/ja/lastaflute/howto/action/makeashtml.html
o 久保さんに質問したいこと
  - 「フォローしている会員の購入一覧」を条件で絞り込むところで、以下のようにExistsReferrerで指定して絞り込んでると思います
    - ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Product();
            cb.orScopeQuery(orCB -> {
                orCB.query().setMemberId_Equal(userId);
                orCB.query().queryMember().existsMemberFollowingByYourMemberId(followingCB -> {
                    followingCB.query().setMyMemberId_Equal(userId);
                });
            });
            ...
        });
    - 実際のSQLのイメージ
        - SELECT ... FROM PURCHASE
            INNER JOIN MEMBER ON ...
            INNER JOIN PRODUCT ON ...
            WHERE (PURCHASE.MEMBER_ID = ? OR EXISTS (SELECT MEMBER_ID FROM MEMBER_FOLLOWING WHERE YOUR_MEMBER_ID = MY_MEMBER_ID AND MY_MEMBER_ID = ?))

  - 自分的には、以下のように「その購入会員のIDSが（userIdがフォローしてる会員のID）に含まれる」みたいな絞り方の方が自然に思いつきやすかったのですが、そのような書き方はあまり推奨されてないのでしょうか？
  - DBFlute でMyselfInScopeそのような書き方ができそうかなと思ったのですが、ExistsReferrerよりはマイナーな感じがしたので...
  - 以下、SQLのイメージ（OR EXISTS ではなく、OR PURCHASE.MEMBER_ID IN を使う）
  - SELECT ... FROM PURCHASE
            INNER JOIN MEMBER ON ...
            INNER JOIN PRODUCT ON ...
            WHERE (PURCHASE.MEMBER_ID = ? OR PURCHASE.MEMBER_ID IN (SELECT MEMBER_ID FROM MEMBER_FOLLOWING WHERE YOUR_MEMBER_ID = MY_MEMBER_ID AND MY_MEMBER_ID = 1))
「フォローしている会員の購入一覧」を条件で絞り込むところで、
 */
