package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
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
        // done mayukorin 修行++のadjust...を使って10/1ぴったりのデータを作って実行してみましょう by jflute (2025/02/03)
        String fromDateStr = "2005/10/01";
        LocalDateTime fromLocalDateTime = convertStrToLocalDateTime(fromDateStr);
        LocalDate fromLocalDateMinusOneDay = convertLocalDateTimeToLocalDate(fromLocalDateTime).minusDays(1);

        String toDateStr = "2005/10/03";
        LocalDateTime toLocalDateTime = convertStrToLocalDateTime(toDateStr);
        LocalDate toLocalDatePlusOneDay = convertLocalDateTimeToLocalDate(toLocalDateTime).plusDays(1);

        String nameKeyword = "vi";

        adjustMember_FormalizedDatetime_FirstOnly(fromLocalDateTime, nameKeyword); // 10/1ジャストの正式会員日時を持つ会員データを作成。

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
    //                                                                    Platinumストレッチ
    //                                                                        ============
    /**
     * 正式会員になってから一週間以内の購入を検索 <br>
     * o 会員と会員ステータス、会員セキュリティ情報も一緒に取得 <br>
     * o 商品と商品ステータス、商品カテゴリ、さらに上位の商品カテゴリも一緒に取得 <br>
     * o 上位の商品カテゴリ名が取得できていることをアサート <br>
     * o 購入日時が正式会員になってから一週間以内であることをアサート <br>
     * ※修行++: 実装できたら、こんどはスーパークラスのメソッド adjustPurchase_PurchaseDatetime_...() を呼び出し、調整されたデータによって検索結果が一件増えるかどうか確認してみましょう。 もし増えないなら、なぜ増えないのか？しっかり分析して、コード上のコメントで分析結果を書き出してみましょう。 そして、一週間以内という解釈を無理のない程度に変えて、増えるようにしてみましょう。 もともと増えたのであれば、なぜ増えたのか？が把握できていたらOKです。
     */
    public void test_searchPurchaseByFormalizedDatetime() throws Exception {
        // ## Arrange ##
        // done mayukorin こちらも、補足に書いてあった adjust...() をやってみましょう by jflute (2025/02/12)
        // 検索結果が増えなかった
        // 現在のコードの1週間後の定義：正式会員になった日から、分単位で1週間後。
        // そのため、正式会員になった日から日単位では1週間後だが、分単位では1週間後以降の場合は、検索結果に含まれない。
        // adjust で更新したデータは上のケースに当てはまる（handyDate.addDay(7).moveToDayTerminal().moveToSecondJust()）。
        adjustPurchase_PurchaseDatetime_fromFormalizedDatetimeInWeek();

        // ## Act ##
        // done jflute 1on1にて一週間の定義について議論 (2025/02/12)
        // [1on1でのふぉろー]
        // いまの実装は、「7×24 + 1秒」になってる？はず。以下の図だと、Kを含むスタイル。
        //
        // 10/3                    10/10     10/11
        //  13h                      0h  13h   0h
        //   |                       |    |    |
        //   |       D               | I  |    | P
        // A |                       |H  J|L   |O
        //   |C                  E   G    K    N
        //   B                      F|    |   M|
        //   |                       |         |
        //
        // done mayukorin 修行++: "M" まで含むスタイルに変えてみてください ("N" は含まないように) by jflute (2025/02/17)
        // 2件から3件に増えました！
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberSecurityAsOne();
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Product().withProductCategory().withProductCategorySelf();
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .greaterEqual(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime());
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .lessThan(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime())
                    .convert(op -> op.truncTime().addDay(8));
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            LocalDateTime purchaseDatetime = purchase.getPurchaseDatetime();
            // done mayukorin [いいね] その通り！ by jflute (2025/02/12)
            // 紐づく member・memberStatus・product・productStatus・productCategory は必ず存在する。NotNullのFKカラムなため。
            Member member = purchase.getMember().get();
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            MemberStatus memberStatus = member.getMemberStatus().get();
            MemberSecurity memberSecurity = member.getMemberSecurityAsOne().get();
            Product product = purchase.getProduct().get();
            ProductStatus productStatus = product.getProductStatus().get();
            ProductCategory productCategory = product.getProductCategory().get();
            OptionalEntity<ProductCategory> optParentProductCategory = productCategory.getProductCategorySelf();

            log("purchaseDatetime: {}, purchasedProduct: {}, productStatus: {}, productCategory: {}, optParentProductCategory: {}, memberFormalizedDatetime: {}, memberStatus: {}, securityVersionNumber: {}",
                    purchaseDatetime, product.getProductName(), productStatus.getProductStatusName(), productCategory.getProductCategoryName(), optParentProductCategory, member.getFormalizedDatetime(), memberStatus.getMemberStatusName(), memberSecurity.getVersionNo());

            assertFalse(purchaseDatetime.isBefore(formalizedDatetime)); // 購入日時が正式会員日時以降であることをアサート（正式会員日時ピッタリも含む）
            assertTrue(purchaseDatetime.isBefore(convertLocalDateTimeToLocalDate(formalizedDatetime).plusDays(8).atStartOfDay())); // 購入日時が、M以内であることをアサート
            assertNotNull(optParentProductCategory.get().getProductCategoryName());
        });
    }

    /**
     * 1974年までに生まれた、もしくは不明の会員を検索 <br>
     * o 画面からの検索条件で1974年がリクエストされたと想定 <br>
     * o Arrange で String の "1974/01/01" を一度宣言してから日付クラスに変換 <br>
     * o その日付クラスの値を、(日付移動などせず)そのまま使って検索条件を実現 <br>
     * o 会員ステータス名称、リマインダ質問と回答、退会理由入力テキストを取得する(ログ出力) ※1 <br>
     * o 若い順だが生年月日が null のデータを最初に並べる <br>
     * o 生年月日が指定された条件に合致することをアサート (1975年1月1日なら落ちるように) <br>
     * o Arrangeで "きわどいデータ" ※2 を作ってみましょう (Behavior の updateNonstrict() ※3 を使って) <br>
     * o 検索で含まれるはずの "きわどいデータ" が検索されてることをアサート (アサート自体の保証のため) <br>
     * o 生まれが不明の会員が先頭になっていることをアサート <br>
     * o ※1: ログについて、値がない項目は "none" を出力。if文使わないように。ヒント: Java8なら map() <br>
     * o ※2: 1974年12月31日生まれの人、1975年1月1日生まれの人。前者は検索に含まれて、後者は含まれない。 テストデータに存在しない、もしくは、存在に依存するのがためらうほどのピンポイントのデータは、自分で作っちゃうというのも一つの手。 (エクササイズ 6 や 7 でやっていた adjust がまさしくそれ: 同じように adjustXxx() という感じでprivateメソッドにしましょう) <br>
     * o ※3: 1 から 9 までの任意の会員IDを選び updateNonstrict() してみましょう。 まあ、一桁代の会員IDが存在すること自体への依存は割り切りで。最低限それだけのテストデータで用意されていないとお話にならないってことで。 <br>
     * ※今後、"きわどいデータ" を作ってアサートを確かなものにするかどうかは自分の判断で。 <br>
     */
    public void test_searchMemberByBirthdate() throws Exception {
        // ## Arrange ##
        String birthYearForSearch = "1974/01/01";
        // done mayukorin なんか二ます空いてる by jflute (2025/02/17)
        LocalDate birthYearForSearchLocalDate = convertStrToLocalDate(birthYearForSearch);

        // "きわどいデータ"を作る
        LocalDate birthdayBarelyIncludedSearchResult = convertStrToLocalDate("1974/12/31");
        LocalDate birthdayBarelyNotIncludedSearchResult = convertStrToLocalDate("1975/01/01");
        adjustMember_Birthdate_byMemberId(birthdayBarelyIncludedSearchResult, 1);
        adjustMember_Birthdate_byMemberId(birthdayBarelyNotIncludedSearchResult, 2);
    
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.setupSelect_MemberWithdrawalAsOne();
            // done mayukorin orIsNull()パーフェクト！ by jflute (2025/02/12)
            cb.query().setBirthdate_FromTo(null, birthYearForSearchLocalDate, op -> op.allowOneSide().compareAsYear().orIsNull());
            cb.query().addOrderBy_Birthdate_Desc().withNullsFirst();
        });

        // ## Assert ##
        assertHasAnyElement(members);

        // done mayukorin [見比べ課題] 模範の実装を見比べてみて、頭の中で他のやり方も学んでみてください by jflute (2025/02/17)
        // 「生まれが不明の会員が先頭になっていること」をasseretすれば良いだけだから、先頭のmemberのbirthdateがnullかどうかだけ、見れば良いだけなのですね！
        int currentOrderNumber = 0;
        int lastNullBirthdateMemberOrderNumber = 0;
        int firstNonNullBirthdateMemberOrderNumber = 0;
        // done mayukorin [いいね] UnitTestの素通り防止のためにちゃんと存在確認をしてるの素晴らしい by jflute (2025/02/12)
        // done mayukorin [tips] isIncluded をもっと短く has.. にすることもできるかなと by jflute (2025/02/12)
        boolean hasBirthdayBarelyIncludedSearchResult = false;

        for (Member member : members) {
            currentOrderNumber++;
            LocalDate birthdate = member.getBirthdate();
            MemberStatus memberStatus = member.getMemberStatus().get();
            MemberSecurity memberSecurity = member.getMemberSecurityAsOne().get();
            OptionalEntity<MemberWithdrawal> optMemberWithdrawalAsOne = member.getMemberWithdrawalAsOne();
            // done mayukorin 横長すぎるのでちょっと改行して欲しいところですね by jflute (2025/02/12)
            // done mayukorin map()のところ、独立改行があると嬉しいかなと by jflute (2025/02/17)
            log("member: {}, birthdate: {}, status: {}, reminder question: {}, reminder answer:{}, withdrawal reason: {}",
                    member.getMemberName(), birthdate, memberStatus.getMemberStatusName(),
                    memberSecurity.getReminderQuestion(), memberSecurity.getReminderAnswer(),
                    optMemberWithdrawalAsOne.map(mw -> mw.getWithdrawalReasonInputText()).orElse("none"));

            assertTrue(birthdate == null || birthdate.isBefore(birthYearForSearchLocalDate.plusYears(1)));

            if (birthdate == null) { // 誕生日が不明の会員だったら
                lastNullBirthdateMemberOrderNumber = currentOrderNumber;
            } else { // 誕生日が不明ではない会員だったら
                if (firstNonNullBirthdateMemberOrderNumber == 0) { // 誕生日が不明ではない会員が初めて検索結果に登場したら
                    firstNonNullBirthdateMemberOrderNumber = currentOrderNumber;
                }
                if (birthdate.equals(birthdayBarelyIncludedSearchResult)) { // きわどい誕生日の会員が検索結果に登場したら
                    hasBirthdayBarelyIncludedSearchResult = true;
                }
            }
        }
        // done mayukorin ラベルの変数名 by jflute (2025/02/17)
        log("hasBirthdayBarelyIncludedSearchResult: {}, lastNullBirthdateMemberOrderNumber: {}, firstNonNullBirthdateMemberOrderNumber: {}", hasBirthdayBarelyIncludedSearchResult, lastNullBirthdateMemberOrderNumber, firstNonNullBirthdateMemberOrderNumber);

        assertTrue(hasBirthdayBarelyIncludedSearchResult); // かろうじて検索結果に含まれるはずの誕生日がきわどい会員がちゃんと検索結果に含まれていることをアサート
        assertTrue(lastNullBirthdateMemberOrderNumber < firstNonNullBirthdateMemberOrderNumber); // 生まれが不明の会員が、全員生まれが不明でない会員よりも前に登場することをアサート
    }

    /**
     * 2005年6月に正式会員になった会員を先に並べて生年月日のない会員を検索 <br>
     * o 画面からの検索条件で2005年6月がリクエストされたと想定 <br>
     * o Arrange で String の "2005/06/01" を一度宣言してから日付クラスに変換 <br>
     * o その日付クラスの値を、(日付移動などせず)そのまま使って検索条件を実現 <br>
     * o 第二ソートキーは会員IDの降順 <br>
     * o 検索された会員の生年月日が存在しないことをアサート <br>
     * o 2005年6月に正式会員になった会員が先に並んでいることをアサート (先頭だけじゃなく全体をチェック) <br>
     */
    public void test_searchMemberByFormalizedDatetimeByMonth() throws Exception {
        // done m.inoue きわどいデータは後でやってみる (2025/02/14)
        // ## Arrange ##
        String fromMonthStr = "2005/06/01";
        LocalDate fromMonthLocalDate = convertStrToLocalDate(fromMonthStr);

        // "きわどいデータ"を作る
        LocalDateTime formalizedDatetimeBarelyIncludedJun = convertStrToLocalDateTime("2005/06/01");
        LocalDateTime formalizedDatetimeBarelyNotIncludedJun = convertStrToLocalDateTime("2005/05/31");
        adjust_Member_FormalizedDatetimeAndNullBirthdate_byMemberId(formalizedDatetimeBarelyIncludedJun, 1);
        adjust_Member_FormalizedDatetimeAndNullBirthdate_byMemberId(formalizedDatetimeBarelyNotIncludedJun, 2);

        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_FormalizedDatetime_Asc().withManualOrder(op -> {
                // done mayukorin 識別するなら、もっと大げさに識別したほうが安全かなと e.g. innerOp  by jflute (2025/02/17)
                op.when_FromTo(fromMonthLocalDate, fromMonthLocalDate, innerOp -> innerOp.compareAsMonth());
            });
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(members);
       
        // done mayukorin [見比べ課題] 模範の実装と見比べて学んでみてください by jflute (2025/02/17)
        // 模範だと、2005年6月じゃない人が初めて登場してからそれ以降に2005年6月の人が登場しないかどうか、passedBorderで見てるのですね！
        int currentOrderNumber = 0;
        int lastJunFormalizedMemberOrderNumber = 0;
        int firstNotJunFormalizedMemberOrderNumber = 0;

        for (Member member : members) {
            currentOrderNumber++;
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();

            // done mayukorin member.getFormalizedDatetime()出してくれると嬉しい by jflute (2025/02/17)
            // done mayukorin カラム名は birthdate なので、ラベルも合わせたほうがいいかなと by jflute (2025/02/17)
            log("memberId: {}, formalizedDatetime: {}, birthdate: {}, ", member.getMemberId(), formalizedDatetime, member.getBirthdate());

            assertNull(member.getBirthdate());

            // done mayukorin [いいね] if/else に人間向きの条件説明があって読むの早くなって嬉しい by jflute (2025/02/17)
            if (formalizedDatetime != null && formalizedDatetime.getMonthValue() == fromMonthLocalDate.getMonthValue()) { // 2005年6月に正式会員になった会員だったら
                lastJunFormalizedMemberOrderNumber = currentOrderNumber;
                if (formalizedDatetime.equals(formalizedDatetimeBarelyIncludedJun)) { // ギリギリ2005年6月に正式会員になった会員だったら
                    assertEquals(0, firstNotJunFormalizedMemberOrderNumber); // その会員は、2005年6月に正式会員になってない会員よりも先に登場することをアサート
                }
            } else { // 2005年6月に正式会員になった会員ではなかったら
                if (firstNotJunFormalizedMemberOrderNumber == 0) { // 上の会員が初めて検索結果に登場したら
                    firstNotJunFormalizedMemberOrderNumber = currentOrderNumber;
                }
                if (formalizedDatetime != null && formalizedDatetime.equals(formalizedDatetimeBarelyNotIncludedJun)) { // ギリギリ2005年6月に正式会員になっていない会員だったら
                    assertTrue(currentOrderNumber > lastJunFormalizedMemberOrderNumber); // その会員は、2005年6月に正式会員になった会員よりも後に登場することをアサート
                }
            }
        }
        log("lastJunFormalizedMemberOrderNumber: {}, firstNotJunFormalizedMemberOrderNumber: {}", lastJunFormalizedMemberOrderNumber, firstNotJunFormalizedMemberOrderNumber);

        assertTrue(lastJunFormalizedMemberOrderNumber < firstNotJunFormalizedMemberOrderNumber); // 2005年6月に正式会員になった会員が、そうではない会員よりも前に登場することをアサート
    }

    // [1on1での雑談]
    //        ・推論の種類
    //       　・逆行推論
    //       　・演繹推論
    //       　・帰納推論
    //       ・逆行推論結構大事
    // done mayukorin [読み物課題] 問題分析と問題解決を分けることがハマらない第一歩 by jflute (2025/02/17)
    // https://jflute.hatenadiary.jp/entry/20170712/analysissolving
    // 私も問題解決いきなりしようとしてた気がします。身にしみます...

    // ===================================================================================
    //                                                                         ページング検索
    //                                                                           =========
    /**
     * 全ての会員をページング検索 <br>
     * o 会員ステータス名称も取得 <br>
     * o 会員IDの昇順で並べる <br>
     * o ページサイズは 3、ページ番号は 1 で検索すること <br>
     * o 会員ID、会員名称、会員ステータス名称をログに出力 <br>
     * o SQLのログでカウント検索時と実データ検索時の違いを確認 <br>
     * o 総レコード件数が会員テーブルの全件であることをアサート <br>
     * o 総ページ数が期待通りのページ数(計算で導出)であることをアサート <br>
     * o 検索結果のページサイズ、ページ番号が指定されたものであることをアサート <br>
     * o 検索結果が指定されたページサイズ分のデータだけであることをアサート <br>
     * o pageRangeを 3 にして PageNumberList を取得し、[1, 2, 3, 4]であることをアサート <br>
     * o 前のページが存在しないことをアサート
     * o 次のページが存在することをアサート
     */
    public void test_pagingSearch() throws Exception {
        // ## Arrange ##
        // done jflute 完璧なので1on1にてページング自体の少しフォローをするだけ (2025/02/28)
        int pageSize = 3;
        int pageNumber = 1;
    
        // ## Act ##
        PagingResultBean<Member> members = memberBhv.selectPage(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().addOrderBy_MemberId_Asc();
            cb.paging(pageSize, pageNumber);
        });

        // ## Assert ##
        int expectedMemberCount = memberBhv.selectCount(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().addOrderBy_MemberId_Asc();
        });
        int expectedPageCount = (expectedMemberCount + (pageSize-1))/pageSize;
        int pageRangeSize = 3;

        members.forEach(member -> {
            MemberStatus memberStatus = member.getMemberStatus().get();
            log("memberId: {}, name: {}, status: {}", member.getMemberId(), member.getMemberName(), memberStatus.getMemberStatusName());
        });

        assertEquals(expectedMemberCount, members.getAllRecordCount());
        assertEquals(expectedPageCount, members.getAllPageCount());
        assertEquals(pageSize, members.getPageSize());
        assertEquals(pageNumber, members.getCurrentPageNumber());
        assertEquals(pageSize, members.size());

        List<Integer> pageNumberList = members.pageRange(op -> op.rangeSize(pageRangeSize)).createPageNumberList();
        assertEquals(newArrayList(1, 2, 3, 4), pageNumberList);

        assertFalse(members.existsPreviousPage());
        assertTrue(members.existsNextPage());
    }

    // done mayukorin [読み物課題] times の "正常性バイアス" の話に派生して、少し "仮説キープ力" のお話を by jflute (2025/02/28)
    // 自分の中でデマを広げさせない: https://jflute.hatenadiary.jp/entry/20110619/nodema

    // ===================================================================================
    //                                                                          カーソル検索
    //                                                                           =========
    /**
     * 会員ステータスの表示順カラムで会員を並べてカーソル検索 <br>
     * o 会員ステータスの "表示順" カラムの昇順で並べる <br>
     * o 会員ステータスのデータも取得 <br>
     * o その次には、会員の会員IDの降順で並べる <br>
     * o 会員ステータスが取れていることをアサート <br>
     * o 会員が会員ステータスごとに固まって並んでいることをアサート <br>
     * o 検索したデータをまるごとメモリ上に持ってはいけない <br>
     * o (要は、検索結果レコード件数と同サイズのリストや配列の作成はダメ) <br>
     */
    public void test_cursorSearch() throws Exception {
        // ## Arrange ##
    
        // ## Act, Assert ##
        List<String> memberStatusList = new ArrayList<>();

        // TODO jflute 1on1にて、カーソル検索の使い所の話をする (2025/03/07)
        memberBhv.selectCursor(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        }, member -> {
            MemberStatus memberStatus = member.getMemberStatus().get();

            log("memberId: {}, name: {}, status: {}, displayOrder: {}", member.getMemberId(), member.getMemberName(), memberStatus.getMemberStatusName(), memberStatus.getDisplayOrder());

            assertNotNull(memberStatus.getMemberStatusName());

            // TODO mayukorin getMemberStatusCode()は変数に取って欲しいかな by jflute (2025/03/07)
            if (memberStatusList.isEmpty()) { // 最初のmember
                memberStatusList.add(memberStatus.getMemberStatusCode());
            } else if (!Objects.equals(memberStatus.getMemberStatusCode(),
                                memberStatusList.get(memberStatusList.size() - 1))) { // 1つ前のmemberとstatusが違っていたら
                assertFalse(memberStatusList.contains(memberStatus.getMemberStatusCode()));
                memberStatusList.add(memberStatus.getMemberStatusCode());
            }
        });
    }
    
    public void test_innerJoinAutoDetect() throws Exception {
        // ## Arrange ##
        // TODO jflute 1on1にて、外部結合と内部結合、ConditionBeanは概念的には外部結合話をする (2025/03/07)
    
        // ## Act ##
        // outer join
        memberBhv.selectList(cb -> {
           cb.setupSelect_MemberWithdrawalAsOne();
        });

        // memberWithdrawalに条件をつけたら、inner joinにしてくれた（検索条件アプローチ）
        memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
            cb.query().queryMemberWithdrawalAsOne().setWithdrawalReasonInputText_LikeSearch("a", op -> op.likeContain());
        });

        // inner join（構造的アプローチ）
        memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
        });
        // ## Assert ##
    }
    
    // TODO jflute 1on1にて、MySQLのトランザクション分離レベルの話をする (2025/03/07)

    // ===================================================================================
    //                                                                             Convert
    //                                                                           =========
    private static LocalDate convertStrToLocalDate(String strDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return LocalDate.parse(strDate, dateTimeFormatter);
    }

    // こっちとても良い再利用メソッド by jflute
    private static LocalDateTime convertStrToLocalDateTime(String strDate) {
        return convertStrToLocalDate(strDate).atTime(LocalTime.MIN);
    }

    // こっち少し大げさかも？でも悪くはない再利用メソッド by jflute
    private static LocalDate convertLocalDateTimeToLocalDate(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }

    // ===================================================================================
    //                                                                  Generate Test Data
    //                                                                           =========
    private void adjustMember_Birthdate_byMemberId(LocalDate birthdate, Integer memberId) {
        assertNotNull(memberId);
        // done mayukorin 事前selectせずに、new Member()でset/setして更新で大丈夫です by jflute (2025/02/28)
        Member member = new Member();
        member.setMemberId(memberId);
        member.setBirthdate(birthdate);
        memberBhv.updateNonstrict(member);
    }

    private void adjust_Member_FormalizedDatetimeAndNullBirthdate_byMemberId(LocalDateTime formalizedDatetime, Integer memberId) {
        assertNotNull(memberId);

        Member member = new Member();
        member.setMemberId(memberId);
        member.setFormalizedDatetime(formalizedDatetime);
        member.setBirthdate(null);

        memberBhv.updateNonstrict(member);
    }
}
