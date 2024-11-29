CREATE OR REPLACE procedure DUIBKIIS."SP_CREATE_EXECUTE_ZIMS_IIS"
(
	ASV_RETURN OUT VARCHAR2	-- RETURN VALUE
)
IS
/**
 * <pre>
 * Program/File : SP_CREATE_EXECUTE_ZIMS_IIS
 * 설  명	    : 투자자 예치금 시행에 따른 예금 적수를 쌓는다
 * 작성자    : 서지웅 과장
 * 작성일    : 2024.11.22
 * 버  전    : 1.0
 * 
 * 수정이력   : 
 * 
 * 기타사항   : 
 * <pre>
 */

TRGT_CNT      NUMBER    := 0;
SUCCEED_CNT   NUMBER    := 0;

V_YYYYMMDD   VARCHAR(10);
V_HHMMDD   VARCHAR(10);
V_ACNM     CHAR(16);

BEGIN
	

	SELECT (SELECT (SELECT V.ACNM
    	              FROM TB_IIS_ACIF_I_A V
	                 WHERE V.ACNM = A.ACNM) ACNM
	          FROM TB_IIS_BAH_H_A A
	         WHERE ROWNUM = 1
	       ) ACNM
	     , FDLD.FUND_CD
	  FROM TB_IIS_FDLD_I_A FDLD
	     , (SELECT (SELECT '016001' FROM DUAL) FUND_CD
	             , 'MMF' FUND_NM
	          FROM DUAL
	       ) SUB
	     , (SELECT '016001' FUND_CD, 'MMF' FUND_NM FROM DUAL
	       ) SUB2
	 WHERE FDLD.FUND_CD = SUB.FUND_CD
	   AND FDLD.FUND_CD = SUB2.FUND_CD
	   AND ROWNUM = 1
	;


END 