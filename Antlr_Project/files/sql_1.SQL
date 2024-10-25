SELECT /*+ INDEX(TB_IIS_ACIF_I_A_PK TB_IIS_ACIF_I_A)*/
       ACNM A_ACNM		-- 계좌번호
     , WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR		--전행고객번호
     , INTR_CNRC_NMBR	--내부계약번호
     , SUB_SNRC_NMBR	--
     , BNKB_NMBR
     , ACNT_OPEN_BRCD
  FROM TB_IIS_ACIF_I_A
;