-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Member addresses should be only one at any time.
-- 有効期間が一日だけ重複しているパターンも検出（begin_dateとend_dateが同一のパターンを検出すれば良い）
-- - - - - - - - - - -/
select adr.MEMBER_ADDRESS_ID
     , adr.MEMBER_ID
     , adr.VALID_BEGIN_DATE
     , adr.VALID_END_DATE
     , adr.ADDRESS
  from MEMBER_ADDRESS adr
 where exists (select subadr.MEMBER_ADDRESS_ID
                from MEMBER_ADDRESS subadr
               where subadr.MEMBER_ID = adr.MEMBER_ID
                 and subadr.VALID_BEGIN_DATE > adr.VALID_BEGIN_DATE
                 and subadr.VALID_BEGIN_DATE <= adr.VALID_END_DATE
              );
-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- 正式会員日時を持ってる仮会員がいないこと
-- - - - - - - - - - -/
select MEMBER_ID
     , MEMBER_NAME
     , MEMBER_STATUS_CODE
     , FORMALIZED_DATETIME
  from MEMBER
 where MEMBER_STATUS_CODE = 'PRV'
   and FORMALIZED_DATETIME is not null;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- まだ生まれていない会員がいないこと
-- - - - - - - - - - -/
select MEMBER_ID
     , MEMBER_NAME
     , MEMBER_STATUS_CODE
     , FORMALIZED_DATETIME
  from MEMBER
 where BIRTHDATE > NOW();

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- 退会会員が退会情報を持っていることをアサート
-- - - - - - - - - - -/
select mem.MEMBER_ID
     , mem.MEMBER_NAME
     , mem.MEMBER_STATUS_CODE
     , mem.FORMALIZED_DATETIME
  from MEMBER mem
 where mem.MEMBER_STATUS_CODE = 'WDL'
   and not exists (select withdrawal.MEMBER_ID
                     from MEMBER_WITHDRAWAL withdrawal
                    where withdrawal.MEMBER_ID = mem.MEMBER_ID
                  );
