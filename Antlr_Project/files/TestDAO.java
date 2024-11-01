package ibk.iis.dao.account;

import ibk.iis.component.accouht.dto.AccountPwdDTO;
import ibk.iis.component.accouht.dto.Account;

/**
* 프로그램ID : AccountDAO.java
* 날짜: 2007.09.11
*/

public class TestDAO extends CommonDAO {
	
	private String prgName = this.getClass().getName();
	
	/**
	 * e번호조회
	 * @param acnm
	 * @return 
	 * @throws Exception
	 */
	public String selectEabnYon(String acnm) throw Exception {
		String sThisMethod = prgName + ".selectEabnYon(String acnm)";
		
		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("SELECT (SELECT PRIF.ACNM FROM TB_IIS_PRIF_I_A  PRIF WHERE ACIF.ACNM = PRIF.ACNM) ACNM  \n");
		sql.append("  FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n");
		sql.append(" WHERE ACNM = ?         -- 계좌번호    \n");
		
		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;
		
		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호
		
		NeoResultSet rs = qm.executeQuery();
		
		String eabnYon = null;
		
		if (rs.next()) {
			eabnYon = rs.getString("ACNM");
		}
		return eabnYon;
	}
	
	/**
	 * e번호조회
	 * @param acnm
	 * @return 
	 * @throws Exception
	 */
	public String selectAcnmInfo(String acnm) throw Exception {
		String sThisMethod = prgName + ".selectEabnYon(String acnm)";
		
		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("SELECT /*+ INDEX(TB_IIS_ACIF_I_A_PK ACIF)*/  \n");
		sql.append("       ACIF.ACNM A_ACNM		-- 계좌번호        \n");
		sql.append("     , ACIF.WHBN_CSTM_NMBR A_WHBN_CSTM_NMBR		--전행고객번호    \n");
		sql.append("     , ACIF.SUB_SNRC_NMBR	--    \n");
		sql.append("     , ACIF.BNKB_NMBR    \n");
		sql.append("     , ACNT_OPEN_BRCD    \n");		
		sql.append("  FROM TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n");
		sql.append(" WHERE ACIF.ACNM = ?         -- 계좌번호    \n");
		
		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;
		
		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호
		
		NeoResultSet rs = qm.executeQuery();
		
		String acnm = null;
		
		if (rs.next()) {
			acnm = rs.getString("A_ACNM");
		}
		return eabnYon;
	}
	
	/**
	 * 연결계좌번호 업데이트
	 * @param acnm
	 * @return 
	 * @throws Exception
	 */
	public int updateLinkAcn(String acnm, String linkAcn) throw Exception {
		String sThisMethod = prgName + ".updateLinkAcn(String acnm, String linkAcn)";
		
		StringBuffer sql = new StringBuffer();
		sql.append("/* "+sThisMethod +"*/                \n");
		sql.append("UPDATE TB_IIS_ACIF_I_A ACIF	-- 계좌정보    \n");
		sql.append("   SET LINK_ACN = ?                      \n");
		sql.append(" WHERE ACIF.ACNM = ?         -- 계좌번호    \n");
		
		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;
		
		qm.setString(++paramCnt, linkAcn);
		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호

		return qm.executeQuery();
	}

}