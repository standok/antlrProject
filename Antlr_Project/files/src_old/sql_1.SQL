CREATE OR REPLACE procedure DUIBKIIS."SP_CREATE_EXECUTE_ZIMS_IIS"
(
	ASV_RETURN OUT VARCHAR2	-- RETURN VALUE
)
IS
/**
 * <pre>
 * Program/File : SP_CREATE_EXECUTE_ZIMS_IIS
 * 설  명	    : 투자자 예치금 시행에 따른 예금 적수를 쌓는다
 * 작성자    : 
 * 작성일    : 2024.1122
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
	
	DECLARE V_LNKN_ACNM CHAR(16);
	DECLARE V_ACNM CHAR(16);
	DECLARE V_BASE_YMD DATE;
	DECLARE V_BASE_YEAR CHAR(4);

    SELECT /*+ INDEX(TB_IIS_ACIF_I_A_PK TB_IIS_ACIF_I_A)*/  
           ACNM A_ACNM		-- 계좌번호        
         , WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR		--전행고객번호    
         , INTR_CNRC_NMBR	-- 내부계약번호    
         , BNKB_NMBR    	-- 통장번호		
         , ACNT_OPEN_BRCD    
      FROM TB_IIS_ACIF_I_A	-- 계좌정보    
     WHERE ACNM = V_ACNM         -- 계좌번호    
    ;

    SELECT (SELECT PRIF.ACNM FROM TB_IIS_PRIF_I_A  PRIF WHERE ACIF.ACNM = PRIF.ACNM) V_AA  
         , WHBN_CSTM_NMBR V_BB          -- 계좌번호    
         , INTR_CNRC_NMBR V_CC         -- 계좌번호    
      FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    
     WHERE ACNM = V_ACNM         -- 계좌번호    
    ;

    SELECT /*+ INDEX(TB_IIS_ACIF_I_A_PK ACIF)*/  
           ACIF.ACNM A_ACNM						-- 계좌번호        
         , WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR		--전행고객번호    
         , INTR_CNRC_NMBR						-- 내부계약번호    
         , ACIF.BNKB_NMBR    					-- 통장번호		
         , V_BASE_YEAR 조회년도 			
         , ACNT_OPEN_BRCD    								
      FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    
     WHERE ACNT_STTS_DSCD IN ('20', '40') 
       AND RDEX_TXTN_PRDC_DSCD NOT IN ('66', '67')     -- 계좌번호     
    ;

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

    UPDATE TB_IIS_ACIF_I_A ACIF	-- 계좌정보    
       SET LNKN_ACNM = V_LNKN_ACNM
         , CNRC_AMNT = TO_NUMBER(10)
         , LAST_MDFC_YMD = TO_CHAR(SYSDATE, 'YYYYMMDDD')
     WHERE ACIF.ACNM = V_ACNM         -- 계좌번호    
    ;

    INSERT INTO TB_IIS_ACIF_I_A (   -- 계좌정보        
           ACNM  					-- 계좌번호        
         , WHBN_CSTM_NMBR           -- 전행고객번호           
         , INTR_CNRC_NMBR  			-- 내부계약번호        
         , BNKB_NMBR  				-- 통장번호        
         , ACNT_ADRM 				-- 계좌부기명       
    ) VALUES (
	     V_ACNM
	    ,V_WHBN_CSTM_NMBR
	    ,V_INTR_CNRC_NMBR
	    ,V_BNKB_NMBR
	    ,V_ACNT_ADRM
	)
    ;

END 