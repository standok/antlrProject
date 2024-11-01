BEGIN
	
	DECLARE V_BASE_YMD DATE;
	DECLARE V_BASE_YM CHAR(6);

SELECT EABN_YON			-- e번호
  FROM TB_IIS_ACIF_I_A	-- 계좌정보
 WHERE ACNM = ?         -- 계좌번호
;

SELECT ACIF.ACNM A_ACNM
     , ACIF.WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR
     , ACIF.SUB_SNRC_NMBR	--    \n
     , ACIF.BNKB_NMBR    \n
     , ACNT_OPEN_BRCD    \n
  FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n
 WHERE ACIF.ACNM = ?         -- 계좌번호    \n
;

END 