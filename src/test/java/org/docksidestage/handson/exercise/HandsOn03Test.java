package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.exception.NonSpecifiedColumnAccessException;
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

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // done mayukorin こんな感じで、スクロールしたときのクラスの見通しよくしてみましょう。タグコメントと言います by jflute (2025/01/27)
    // ===================================================================================
    //                                                                       Silverストレッチ
    //                                                                       ==============
    // 例えば、こちらのクラスを見てみてください。
    // https://github.com/lastaflute/lastaflute-example-harbor/blob/master/src/main/java/org/docksidestage/app/web/product/ProductListAction.java
    // https://github.com/lastaflute/lastaflute-example-harbor/blob/master/src/main/java/org/docksidestage/app/web/signup/SignupAction.java
    // ハンズオンでも実務でも、クラスのメンバー (フィールドやメソッド) の業務的なまとまりを意識してクラス作りをして頂けると嬉しいです。
    // _ta で補完できます。特に大きなクラスでは、カテゴリごとに定義をまとめる習慣を付けていきましょう。
    // 別に、一タグコメント、一メソッドじゃなくてもOKです。まとまった単位で付けていくとよいでしょう。
    // ちなみに、インスタンス変数は、オブジェクト指向の "属性" ということで、Attribute。これは _taattr で用意されています。
    // _/_/_/_/_/_/_/_/_/_/
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
        // done mayukorin 変数抽出でループ外に出すのOKです。一方で、Assertでしか使わない変数なので... by jflute (2025/01/27)
        // ArrangeじゃなくてAssertコメント配下に宣言でOKです。(ArrangeはActのための準備という感覚で)
        LocalDate birthdateForSearch = LocalDate.of(1968, 1, 1);
    
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // done mayukorin [いいね] メソッドの呼び出し順序、いいですね！ by jflute (2025/01/20)
            // 実装順序は、データの取得、絞り込み、並び替え by jflute
            //  => http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus();
            cb.specify().columnMemberName();
            cb.specify().columnBirthdate();
            // done mayukorin specify[テーブル]だけやっても意味がないコードになります。カラムまで指定しないと。 by jflute (2025/01/20)
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            cb.query().setMemberName_LikeSearch(memberNamePrefixForSearch, op -> op.likePrefix());
            // done mayukorin [tips] おおぉ、いきなり高度な機能を！でもできてますね。 by jflute (2025/01/20)
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
        LocalDate birthdateForSearchPlusOneDay = birthdateForSearch.plusDays(1);
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            String memberName = member.getMemberName();
            // done mayukorin ここはmemberを付けなくてもいいかなと。birthdateで十分member限定感あるので by jflute (2025/01/20)
            LocalDate birthdate = member.getBirthdate();
            MemberStatus memberStatus = member.getMemberStatus().get(); // NotNullのFKカラムなため、memberStatusはからなず存在する
            log("memberName: {}, memberBirthdate: {}, memberStatusCodeName: {}, memberNamePrefixForSearch: {}, fromBirthDateForSearch: {}", memberName, birthdate, memberStatus.getMemberStatusName(), memberNamePrefixForSearch, birthdateForSearch);
            assertTrue(memberName.startsWith(memberNamePrefixForSearch));
            // done mayukorin 細かいですが、ループの中で毎回同じ処理 plusDays(1) を実行してしまうのが無駄なので、ループ外に出しましょう by jflute (2025/01/20)
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
            // done mayukorin ここも同じでカラムまで指定しないと意味がないです by jflute (2025/01/27)
            // ちなみに、SpecifyColumnを使うか使わないかはお任せしますが、デフォルトでは不要です。
            // 現場によってはカラム指定まで細かくやるところありますが、DBFluteとしてはSpecifyColumnは追加的な機能です。
            // 教えていただき、ありがとうございます。なるほど、カラム指定した方がパフォーマンス的に良いのかなと思ったのですが、全カラム取得する場合とあまり変わらないことが多いのですね by m.inoue (2025/01/28)
            // https://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/specify/specifycolumn.html#balancepolicy
            // を読みました。
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            cb.specify().specifyMemberSecurityAsOne().columnVersionNo();
            cb.query().addOrderBy_Birthdate_Desc();
            cb.query().addOrderBy_MemberId_Asc();
        });
    
        // ## Assert ##
        // done jflute 1on1でカーディナリティのフォロー予定 (2025/01/20)
        // 会員ステータスは、会員からみて必ず存在する理由は？ => NotNullのFKカラムだから (物理的に存在する)
        // 会員セキュリティは、会員からみて必ず存在する理由は？ => 業務的な制約として1:1と決めているから by ER図
        // (リレーションシップ線の黒丸が目印: 黒丸がないので必ず存在する1:1と言える)
        // (さらには、テーブルコメントに「会員とは one-to-one で、会員一人につき必ず一つのセキュリティ情報がある」って書いてある)
        //
        // 1:1って見たとき聞いたとき...それはカーディナリティの抽象度が高い1:1なのか？細かい1:1なのか？気にする必要がある。
        // → 必ず存在する1:1なのか？ (1 : 1)
        // → いないかもしれない1:1なのか？ (1 : 0..1)
        //
        // jflute流の言葉の使い方ですが...
        // 抽象度ファーストレベルの1:1 => 必ず存在するかどうかは気にせず、とにかく数だけ1:1であると言っている
        // 抽象度セカンドレベルの1:1 => 必ず存在する1:1 (いないかもしれないなら 1 : 0..1 と言う)
        //
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
     * ただし、検索時に会員セキュリティ情報のデータ自体は要らない <br>
     * ※修行++: 実装できたら、(もし複数回検索していたら) Assert内の検索が一回になるようにしてみましょう。 その際、Act内で検索しているデータを、Assert内でもう一度検索することなく実現してみましょう。
     */
    public void test_searchMemberBySecurityInfoReminderQuestion() throws Exception {
        // ## Arrange ##
        String reminderQuestionKeyword = "2";
    
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.specify().columnMemberId();
            cb.specify().columnMemberName();
            // done mayukorin [いいね] 素晴らしい by jflute (2025/01/20)
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch(reminderQuestionKeyword, op -> op.likeContain()); // dbfluteでは、関連テーブルを使いたい目的に対応するメソッドがあるらしい。今回の目的はカラム取得ではなく絞り込みなので、setUpSelect使う必要なし。
        });
    
        // ## Assert ##
        assertHasAnyElement(memberList);

        // done mayukorin [いいね] 素晴らし、とても良いです by jflute (2025/02/03)
        // [1on1でのふぉろー] UnitTestだから妥協できるとは言っても、普段から意識しておくことで、大事な場面でスムーズにできる(と思う)
        // 1回の検索で、 memberList に紐づく memberSecurity を持ってくるようにした
        List<Integer> memberIds = memberList.stream().map(member -> member.getMemberId()).collect(Collectors.toList());
        ListResultBean<MemberSecurity> memberSecurities = memberSecurityBhv.selectList(cb -> {
            cb.specify().columnMemberId();
            cb.specify().columnReminderQuestion();
            cb.query().setMemberId_InScope(memberIds);
        });

        // done mayukorin 念のため、securitiesが空っぽでないこともアサートしておきましょう by jflute (2025/01/27)
        assertHasAnyElement(memberSecurities);
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
//            // done mayukorin ちゃすかに by jflute (2025/01/20)
//            // done mayukorin [読み物課題] 単純な話、getであんまり検索したくない by jflute (2025/01/20)
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

    // ===================================================================================
    //                                                                        Goldストレッチ
    //                                                                        ============
    // done jflute section3の4は後でじっくりレビュー (2025/01/27)
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
        // done mayukorin 変数名にもう少しニュアンス(このリストの役割)を入れたいところですね by jflute (2025/01/28)
        // このリストは何が入るのですか？って聞かれたらなんて答えます？その答えを変数名に入れることができれば...
        // ありがとうございます！変数名考えてみたのですが、あまり分かりやすい名前思いつかなかったです.
        // done mayukorin まあ、アバウト (memberStatusCodeList) よりは良いので、悪くはないですよ。 by jflute (2025/02/03)
        // もう少しスッキリさを演出するとしたら...
        //  e.g. statusTransitionByMemberOrder, transitionStatusList, changedStatusList
        //     , switchedStatusList, switchedNewStatusList, switchedNextStatusList
        List<String> memberStatusCodeTransitionByMemberOrder = new ArrayList<>();

        // done jflute できてるので、1on1にて別の方法について一緒に考えてもらう (2025/01/28)
        // 模範を見て少し学びました。
        for (Member member : members) {
            Integer memberId = member.getMemberId();
            String memberStatusCode = member.getMemberStatusCode();
            log("memberId: {}, memberStatusCode: {}", memberId, memberStatusCode);

            assertTrue(member.getMemberStatus().isEmpty()); // 会員ステータスのデータが取れていないことをチェック

            // done mayukorin [いいね] スーパー良いコメント by jflute (2025/02/03)
            // 取得した会員の順に memberStatusCode を取り出して、memberStatusCodeList に加える
            // ただし、直前に追加した memberStatusCode と同じであれば、List には加えない
            // 例：[FML, FML, WDL, PRV, FML] の場合、memberStatusCodeList には [FML, WDL, PRV, FML] が入る
            if (memberStatusCodeTransitionByMemberOrder.isEmpty()) {
                memberStatusCodeTransitionByMemberOrder.add(memberStatusCode);
                continue;
            }
            String currentStatusCode = memberStatusCodeTransitionByMemberOrder.get(memberStatusCodeTransitionByMemberOrder.size() - 1);
            if (currentStatusCode.equals(memberStatusCode)) continue;
            memberStatusCodeTransitionByMemberOrder.add(memberStatusCode);
        }

        // 会員が会員ステータスごとに固まって並んでいることをチェックしていく
        log("memberStatusCodeTransitionByMemberOrder: {}", memberStatusCodeTransitionByMemberOrder);
        // done mayukorin 変数名は先頭小文字で by jflute (2025/01/28)
        List<String> uniqueMemberStatusCodeList = new ArrayList<>();
        for (String statusCode : memberStatusCodeTransitionByMemberOrder) {
            log("statusCode: {}", statusCode);
            assertFalse(uniqueMemberStatusCodeList.contains(statusCode));  // UniqueMemberStatusCodeList に既に値が存在していたら、会員ステータスが飛び飛びに並んでいることになってしまう
            uniqueMemberStatusCodeList.add(statusCode);
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
            // done mayukorin PURCHASEテーブルがPRODUCT_IDもMEMBER_IDも持っているので... by jflute (2025/01/27)
            // 実はここは query[関連テーブル]() をやらなくても実現できてしまいます。
            // ほんとですね！教えていただき、ありがとうございます！ by m.inoue (2025/01/28)
            cb.query().addOrderBy_ProductId_Asc();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            // 紐づく member・memberStatus・product は必ず存在する。それぞれNotNullのFKカラムなため。
            Member member = purchase.getMember().get();
            MemberStatus memberStatus = member.getMemberStatus().get();
            Product product = purchase.getProduct().get();

            log("memberName: {}, memberStatusName: {}, productName: {}", member.getMemberName(), memberStatus.getMemberStatusName(), product.getProductName());
            assertNotNull(member.getBirthdate());
        });
    }

    /**
     * 2005年10月の1日から3日までに正式会員になった会員を検索 <br>
     * o 画面からの検索条件で2005年10月1日と2005年10月3日がリクエストされたと想定して... <br>
     * o Arrange で String の "2005/10/01", "2005/10/03" を一度宣言してから日時クラスに変換し... <br>
     * o 自分で日付移動などはせず、DBFluteの機能を使って、そのままの日付(日時)を使って条件を設定 <br>
     * o 会員ステータスも一緒に取得 <br>
     * o ただし、会員ステータス名称だけ取得できればいい (説明や表示順カラムは不要) <br>
     * o 会員名称に "vi" を含む会員を検索 <br>
     * o 会員名称と正式会員日時と会員ステータス名称をログに出力 <br>
     * o 会員ステータスがコードと名称だけが取得されていることをアサート <br>
     * o 会員の正式会員日時が指定された条件の範囲内であることをアサート <br>
     */
    public void test_searchMemberByFormalizedDatetimeAndNameKeyword() throws Exception {
        // ## Arrange ##
        // TODO mayukorin 修行++のadjust...を使って10/1ぴったりのデータを作って実行してみましょう by jflute (2025/02/03)
        String fromDateStr = "2005/10/01";
        LocalDateTime fromLocalDateTime = convertStrDateToLocalDateTime(fromDateStr);
        LocalDate fromLocalDateMinusOneDay = convertLocalDateTimeToLocalDate(fromLocalDateTime).minusDays(1);

        String toDateStr = "2005/10/03";
        LocalDateTime toLocalDateTime = convertStrDateToLocalDateTime(toDateStr);
        LocalDate toLocalDatePlusOneDay = convertLocalDateTimeToLocalDate(toLocalDateTime).plusDays(1);

        String nameKeyword = "vi";

        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            cb.specify().columnMemberName();
            cb.specify().columnFormalizedDatetime();
            cb.query().setFormalizedDatetime_FromTo(fromLocalDateTime, toLocalDateTime, op -> op.compareAsDate());
            cb.query().setMemberName_LikeSearch(nameKeyword, op -> op.likeContain());
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            MemberStatus memberStatus = member.getMemberStatus().get(); // memberStatusCodeはmemberテーブルのNotNullのFKカラムなため、memberからみてmemberStatusは必ず存在する
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            LocalDate formalizedDate = convertLocalDateTimeToLocalDate(formalizedDatetime);

            log("name: {}, formalizedDatetime: {}, status: {}", member.getMemberName(), formalizedDatetime, memberStatus.getMemberStatusName());

            assertNotNull(memberStatus.getMemberStatusCode());
            assertNotNull(memberStatus.getMemberStatusName());
            // 会員ステータスの説明・表示順を取得していないことをアサート。
            assertException(NonSpecifiedColumnAccessException.class, () -> memberStatus.getDescription());
            assertException(NonSpecifiedColumnAccessException.class, () -> memberStatus.getDisplayOrder());

            // 正式会員日時が指定日ぴったりでもOK
            assertTrue(formalizedDate.isAfter(fromLocalDateMinusOneDay));
            assertTrue(formalizedDate.isBefore(toLocalDatePlusOneDay));
        });
    }

    // ===================================================================================
    //                                                                             Convert
    //                                                                           =========
    // こっちとても良い再利用メソッド by jflute
    private static LocalDateTime convertStrDateToLocalDateTime(String strDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return LocalDate.parse(strDate, dateTimeFormatter).atTime(LocalTime.MIN);
    }

    // こっち少し大げさかも？でも悪くはない再利用メソッド by jflute
    private static LocalDate convertLocalDateTimeToLocalDate(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }
}
