package com.vo;

public class SqlTokenInfo {

	private String tokenName;		// 토큰명
	private int tokenType;			// 토큰타입
	private String symbolicName;	// 토큰타입명

	private String aliasName;		// alias명
	private String tableName;		// 테이블명

	public String getTokenName() {
		return tokenName;
	}
	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}
	public int getTokenType() {
		return tokenType;
	}
	public void setTokenType(int tokenType) {
		this.tokenType = tokenType;
	}
	public String getSymbolicName() {
		return symbolicName;
	}
	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


}
