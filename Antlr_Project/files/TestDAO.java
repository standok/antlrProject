package util.test;

import ibk.iis.component.accouht.dto.AccountPwdDTO;
import ibk.iis.component.accouht.dto.Account;

/**
* 프로그램ID : AccountDAO.java
* 날짜: 2007.09.11
*/

public class TestDAO extends CommonDAO {

	private String prgName = this.getClass().getName();


	/**
	 * 계좌번호조회
	 * @param acnm
	 * @return
	 * @throws Exception
	 */
	public String selectAcnmInfo(String acnm) throws Exception {
		String sThisMethod = prgName + ".selectEabnYon(String acnm)";

		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("SELECT /*+ INDEX(TB_IIS_ACIF_I_A_PK TB_IIS_ACIF_I_A)*/  \n");
		sql.append("       ACNM A_ACNM		-- 계좌번호        \n");
		sql.append("     , WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR		--전행고객번호    \n");
		sql.append("     , INTR_CNRC_NMBR	-- 내부계약번호    \n");
		sql.append("     , BNKB_NMBR    	-- 통장번호		\n");
		sql.append("     , ACNT_OPEN_BRCD    \n");
		sql.append("  FROM TB_IIS_ACIF_I_A	-- 계좌정보    \n");
		sql.append(" WHERE ACNM = ?         -- 계좌번호    \n");

		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;

		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호

		NeoResultSet rs = qm.executeQuery();

		String acnm = null;

		if(rs.next()) {
			acnm = rs.getString("A_ACNM");
		}
		return eabnYon;
	}


	/**
	 * e번호조회
	 * @param acnm
	 * @return
	 * @throws Exception
	 */
	public String selectEabnYon(String acnm) throws Exception {
		String sThisMethod = prgName + ".selectEabnYon(String acnm)";

		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("SELECT (SELECT PRIF.ACNM FROM TB_IIS_PRIF_I_A  PRIF WHERE ACIF.ACNM = PRIF.ACNM) V_AA  \n");
		sql.append("     , WHBN_CSTM_NMBR V_BB          -- 계좌번호    \n");
		sql.append("     , INTR_CNRC_NMBR V_CC         -- 계좌번호    \n");
		sql.append("  FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n");
		sql.append(" WHERE ACNM = ?         -- 계좌번호    \n");

		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;

		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16)); //계좌번호

		NeoResultSet rs = qm.executeQuery();

		String eabnYon = null;

		if(rs.next()) {
			eabnYon = rs.getString("ACNM");
		}
		return eabnYon;
	}

	/**
	 * 계좌목록조회
	 * @param acnm
	 * @return AcifDto[]
	 * @throws Exception
	 */
	public AcifDto[] selectAcnmInfoList(String acnm, String baseYmd) throws Exception {
		String sThisMethod = prgName + ".selectEabnYon(String acnm, String baseYmd)";

		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("SELECT /*+ INDEX(TB_IIS_ACIF_I_A_PK ACIF)*/  \n");
		sql.append("       ACIF.ACNM A_ACNM						-- 계좌번호        \n");
		sql.append("     , WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR		--전행고객번호    \n");
		sql.append("     , INTR_CNRC_NMBR						-- 내부계약번호    \n");
		sql.append("     , ACIF.BNKB_NMBR    					-- 통장번호		\n");
		sql.append("     , '").append(baseYmd).append("' 조회년도 			\n");
		sql.append("     , ACNT_OPEN_BRCD    								\n");
		sql.append("  FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n");
		sql.append(" WHERE ACNT_STTS_DSCD IN ('20', '40') \n");
		sql.append("   AND RDEX_TXTN_PRDC_DSCD NOT IN ('66', '67')     -- 계좌번호     \n");

		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;

//		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호

		NeoResultSet rs = qm.executeQuery();

		AcifDto[] boList = new AcifDto[rs.getRowCount()];
		AcifDto bo = null;
		int idx = 0;

		while(rs.next()) {
			bo = new AcifDto();
			bo.setAcnm(rs.getString("A_ACNM"));
			bo.setWhbnCstmNmbr(rs.getString("A_WHBN_CSTM_NMBR"));
			bo.setIntrCnrcNmbr(rs.getString("INTR_CNRC_NMBR"));
			bo.setBnkbNmbr(rs.getString("BNKB_NMBR"));
			bo.setAcntOpenBrcd(rs.getString("ACNT_OPEN_BRCD"));

			boList[idx] = bo;
			idx++;
		}
		return boList;
	}

	/**
	 * 연결계좌번호 업데이트
	 * @param acnm
	 * @return
	 * @throws Exception
	 */
	public int updateLinkAcn(String acnm, String linkAcn) throws Exception {
		String sThisMethod = prgName + ".updateLinkAcn(String acnm, String linkAcn)";

		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("UPDATE TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n");
		sql.append("   SET LNKN_ACNM = ?                      \n");
		sql.append(" WHERE ACIF.ACNM = ?         -- 계좌번호    \n");

		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;

		qm.setString(++paramCnt, linkAcn);
		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호

		return qm.executeQuery();
	}
	
	/**
	 * 계좌정보 테이블 입력
	 * @param account
	 * @return void
	 * @throws Exception
	 */
	public int insertAcctInfo(Account paramBo) throws Exception {
		String sThisMethod = prgName + ".insertAcctInfo(Account paramBo)";

		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("INSERT INTO TB_IIS_ACIF_I_A (   -- 계좌정보        \n");
		sql.append("     ACNM  					-- 계좌번호        \n");
		sql.append("     , WHBN_CSTM_NMBR           -- 전행고객번호           \n");
		sql.append("     , INTR_CNRC_NMBR  			-- 내부계약번호        \n");
		sql.append("     , BNKB_NMBR  				-- 통장번호        \n");
		sql.append("     , ACNT_ADRM 				-- 계좌부기명       \n");
		sql.append(") VALUES ( ?,?,?,?,?, ?                          \n");
		sql.append(") \n");

		int paramCnt = 0;
		
		QueryManager qm = new QueryManager(sql.toString());
		
		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호
		qm.setString(++paramCnt, paramBo.WhbnCstmNmbr());
		qm.setString(++paramCnt, paramBo.WhbnCstmNmbr());
		qm.setString(++paramCnt, paramBo.WhbnCstmNmbr());
		qm.setString(++paramCnt, paramBo.WhbnCstmNmbr());
		qm.setString(++paramCnt, paramBo.WhbnCstmNmbr());

		qm.executeUpdate();
	}
}