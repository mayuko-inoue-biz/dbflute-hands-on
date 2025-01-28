package org.docksidestage.handson.exercise;

import java.time.LocalDate;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * DBFluteハンズオン02のクラス
 * @author mayukorin
 */
public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    /**
     * テストデータがあることをテスト
     */
    public void test_existsTestData() throws Exception {
        // ## Arrange ##

        // ## Act ##
        // [1on1でのふぉろー] 単語省略語のよもやま話
        // 省略語をあまり使わなくなってきた歴史がある。でも明らかにわかる省略語はある程度使われている。
        // 例外的にもう広まっている省略語、例えばFLGのお話。あと現場の省略語のお話。
        int memberCnt = memberBhv.selectCount(cb -> {
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertTrue(memberCnt > 0);
    }

    /**
     * 会員名称がSで始まる会員を検索
     */
    public void test_searchByMemberNamePrefixS() throws Exception {
        // ## Arrange ##
        // done mayukorin [いいね] 良い変数名、わかりやすい！ by jflute (2025/01/14)
        String memberNamePrefix = "S";

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberNamePrefix, op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        // ## Assert ##
        // done mayukorin [いいね] ちゃんと素通り防止できてますね！ by jflute (2025/01/14)
        // ちなみに、UnitTestでループするときはずっとそうなので、assH -> assertHasAnyElement()
        // という専用のメソッドあるので、そっちを使ってみてください。
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            // done mayukorin 細かいですが、member.getMemberName() が二度呼ばれてコードが横長なので... by jflute (2025/01/14)
            // 変数に抽出してみましょう。IntelliJのショートカットがあるはずです。
            // [1on1でのふぉろー] 書く時は勢いに乗って.get.getして呼び出して、書き終わった後に抽出するってのがコツ
            String memberName = member.getMemberName();
            log("memberName: {}, memberNamePrefixForSearch: {}", memberName, memberNamePrefix);
            assertTrue(memberName.startsWith(memberNamePrefix));
        });
    }

    /**
     * 会員IDが1の会員を検索
     */
    public void test_searchByMemberIdOne() throws Exception {
        // ## Arrange ##
        // done mayukorin 99999は一時的なお試しで、コミットはしないようにしましょう by jflute (2025/01/14)
        Integer memberId = 999999;

        // ## Act ##
        OptionalEntity<Member> searchedMemberOpt = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(memberId);
        });

        // ## Assert ##
        // done jflute 1on1でOptionalの概念 (2025/01/20)
        // done jflute 1on1でDBFluteのOptionalの話をする予定 (2025/01/20)
        // done mayukorin 丁寧で明示的で悪くはないですが、この場合 DBFlute の get() で例外の方がデバッグしやすいですね by jflute (2025/01/14)
        // assertTrue()を削除して99999で実行して例外メッセージを読んでみましょう。
        // 確かに、getの例外の方がデバックしやすかったです！  by m.inoue (2025/01/15)
        // [思い出]
        // assertTrue(member.isPresent()); // memberがnullのときに下のgetMemberId()でExceptionにならないようにisPresent()を追加したが、不要かも
        // done mayukorin せめて member.get() は変数に抽出しちゃった方がスッキリするかと思います by jflute (2025/01/14)
        //
        // 標準Optionalで問答無用get()すると、NoSuchElementException だけでデバッグ情報が何も無い。
        // なので、問答無用get()はせずorElseThrow()を使いましょうとよく言われます。
        // ただ、orElseThrow()を使ってもデバッグ情報を入れなければ何も変わらない
        //searchedMemberOpt.orElseThrow(() -> {
        //   return new IllegalStateException("No value present"); // これダメ
        //});
        // ↓↓↓
        //searchedMemberOpt.orElseThrow(() -> {
        //   return new IllegalStateException("No value present: memberId=" + memberId); // これならまだ良い 
        //});
        // じゃあ今回のように、「業務的には必ず存在していて、なければ例外で落ちてもいいようなケース」では、
        // 必ずorElseThrow()でデバッグ情報をしっかり入れた例外を生成するのか？
        // (特にDB周りだと、引数の検索条件次第で、必ず存在するケースか？存在しないかもしれないケースか？変わる)
        // (戻り値でOptionalだからといって、常に「存在しないかもしれないケース」とは限らない)
        //
        // 標準のOptionalの思想だと、その通り、毎回orElseThrow()ちゃんとやりましょうという話になる。
        // でも、けっこう面倒くさい。ので、チーム開発でみんな必ずやってくれるかどうか？
        // あと、デバッグ情報をちゃんと入れた例外メッセージを構築するかどうか？
        // あとあと、結局SQLの情報は出せない。(SQLの情報とかはフレームワークが持ってるから)
        // ちょっと怪しい...とjfluteさんは考えた。
        //
        // ということでDBFluteでは、DBFluteがOptionalを用意して...
        // フレームワーク情報(SQL)も入った例外メッセージを載せた独自のOptionalを提供するようにした。
        // それで、必ず存在するケースにおいては、別に問答無用get()しちゃっても構わないようにした。
        //
        // あと、get()だとわかりにくいのは確かにあるので、さらに alwaysPresent() というメソッドを提供した。
        // alwaysPresent() は、ifPresent() の get() 版みたいなもの。なければ例外。
        // もしくは、「get()をコールバックにしたもの」というニュアンス。
        //searchedMemberOpt.alwaysPresent(member -> {
        //    Integer memberId2 = member.getMemberId();
        //});
        //
        // ちなみに、必ず存在するケースでは、ifPresent()は向かない。
        //searchedMemberOpt.ifPresent(searchedMember -> {
        //    String searchedMemberName = searchedMember.getMemberName();
        //    Integer searchedMemberId = searchedMember.getMemberId();
        //    
        //    // done mayukorin テスト内でログを出すとき、assertの前に出す方が落ちたときにそのログを見てデバッグできます by jflute (2025/01/14)
        //    // 確かに！！教えていただき、ありがとうございます  by m.inoue (2025/01/15)
        //    log("searchedMemberName: {}, searchedMemberId: {}, memberIdForSearch", searchedMemberName, searchedMemberId, memberId);
        //    assertEquals(searchedMemberId, memberId);
        //});
        // ↑正常なときは問題ないけど、万が一のバグとかによって値が存在しなかったとき、素通りする。
        // 素通りして、次の処理が動いてしまって、業務的な不整合を起こしてしまう可能性がある。
        // 基本的に、「ダメなケースがだったら、すぐに落ちて欲しい」という考え方が現場にはよくある。
        // 中途半端にシステムが動くってのが怖い、という感覚。完璧な状態で次の処理に行って欲しい。
        // 必ず存在するケースでifを使うってことは、万が一のときに素通りをさせてしまう実装になる。
        // これで言うと、まだデバッグ情報の少ない問答無用get()の方が止まるからマシとも言える。
        //
        // だからこそ、DBFluteなら alwaysPresent() を用意した。
        // 万が一なかったときのデバッグ情報もリッチでデバッグしやすい。
        //
        // 一方でというだがしかし、Java10から標準のOptionalに、orElseThrow(引数なし)が追加された。
        // これって、get() と全く同じ挙動するメソッドで、デバッグ情報は結局存在しない。
        // 唯一違うところは、「なければ例外throwするよ」とメソッド名で表現してちょっとわかりやすいという面。
        // でも、もともと標準のOptionalは、問答無用get()はせずorElseThrow(引数あり例外)つどつどする想定だったような？
        // でもでも、Javaで追加されたということは、必ず存在するケースで問答無用getみたいなことしたいというニーズはあったのでは？
        // せめて、名前を改善して若干の免罪符があるかのようなメソッドでお茶を濁したのか？ちょっとここは想像ですが。
        //
        // さておき、Optionalを戻す人(メソッド)が、一番デバッグ情報を持っているわけなので、
        // その戻す人が、デフォルトの例外メッセージを作ってOptionalを戻す方が良いかな？
        // というのが、DBFluteのOptionalの思想。
        //
        // 加えて、DBFluteのOptionalを理解することで、逆に標準のOptionalの理解にもつながるでしょう。
        
        Member searchedMember = searchedMemberOpt.get();
        String searchedMemberName = searchedMember.getMemberName();
        Integer searchedMemberId = searchedMember.getMemberId();

        // done mayukorin テスト内でログを出すとき、assertの前に出す方が落ちたときにそのログを見てデバッグできます by jflute (2025/01/14)
        // 確かに！！教えていただき、ありがとうございます  by m.inoue (2025/01/15)
        log("searchedMemberName: {}, searchedMemberId: {}, memberIdForSearch", searchedMemberName, searchedMemberId, memberId);
        assertEquals(searchedMemberId, memberId);
    }

    /**
     * 生年月日がない会員を検索
     */
    public void test_searchMemberByNoBirthDate() throws Exception {
        // ## Arrange ##

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Desc();
        });

        // ## Assert ##
        // done mayukorin こっちも素通り防止を by jflute (2025/01/14)
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            // [1on1でのふぉろー] 変数は、右側から書いて、ショートカット抽出しちゃった方が宣言が楽 (オススメ)
            // IntelliJだと、.var もしくは option+Enter でヒントを聞く (だいたい一番上に出てくる)
            String memberName = member.getMemberName();
            LocalDate birthdate = member.getBirthdate();
            // done mayukorin 細かいですが、カラム名変数名としてはbirthdateでdは小文字なのでラベルも合わせましょう by jflute (2025/01/20)
            log("memberName: {}, birthdate: {}", memberName, birthdate);
            assertNull(birthdate);
        });
    }
}
