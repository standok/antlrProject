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
		sql.append("SELECT EABN_YON			-- e번호    \n");
		sql.append("  FROM TB_IIS_ACIF_I_A	-- 계좌정보    \n");
		sql.append(" WHERE ACNM = ?         -- 계좌번호    \n");
		
		QueryManager qm = new QueryManager(sql.toString());
		int paramCnt = 0;
		
		qm.setString(++paramCnt, ConverterUtil.fillPadValue(acnm, ' ', false, 16));	//계좌번호
		
		NeoResultSet rs = qm.executeQuery();
		
		String eabnYon = null;
		
		if (rs.next()) {
			eabnYon = rs.getString("EABN_YON");
		}
		return eabnYon;
	}
	
}