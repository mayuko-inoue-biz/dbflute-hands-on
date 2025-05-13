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
