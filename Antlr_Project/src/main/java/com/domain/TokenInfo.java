package com.domain;

public class TokenInfo {
	private String tokenName;		// Column 이름	- V_BASE_YMD
	private String aliasName;		// Alias 이름		- 기준일자
	private String tableName;		// Table 이름		- TB_IIS_ACIF_I_A
	private String symbolicId;		// Symbolic Id	- REGULAR_ID
	private int symbolNo;			// Symbolic No	- 2250
	
	public TokenInfo(String tokenName, String aliasName, String tableName, String symbolicId, int symbolNo) {
		super();
		this.tokenName = tokenName;
		this.aliasName = aliasName;
		this.tableName = tableName;
		this.symbolicId = symbolicId;
		this.symbolNo = symbolNo;
	}

	public String getTokenName() {
		return tokenName;
	}

	public String getAliasName() {
		return aliasName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getSymbolicId() {
		return symbolicId;
	}

	public int getSymbolNo() {
		return symbolNo;
	}
	
	@Override
	public String toString() {
		return "TokenInfo [tokenName=" + tokenName + ", aliasName=" + aliasName
				+ ", tableaname=" + tableName + ", symbolicId=" + symbolicId
				+ ", symbolNo=" + symbolNo + "]";
	}

}
