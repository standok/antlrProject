package com.vo;

public class SqlTokenInfoVo {

	private int tokenIndex;				// 토큰인덱스
	private String tokenName;			// 토큰명
	private int tokenType;				// 토큰타입
	private String symbolicName;		// 토큰타입명
	private int tokenLine;				// 토큰라인

	private String rolePosition;		// 역활위치(Col: 컬럼, Tab:테이블)
	private String aliasName;			// alias명
	private String tableName;			// 테이블명

	private boolean isConvert = false;	// 전환여부
	private String convertRule = "";	// 변환 규칙

	public int getTokenIndex() {
		return tokenIndex;
	}
	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}
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
	public int getTokenLine() {
		return tokenLine;
	}
	public void setTokenLine(int tokenLine) {
		this.tokenLine = tokenLine;
	}
	public String getRolePosition() {
		return rolePosition;
	}
	public void setRolePosition(String rolePosition) {
		this.rolePosition = rolePosition;
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
	public boolean isConvert() {
		return isConvert;
	}
	public void setConvert(boolean isConvert) {
		this.isConvert = isConvert;
	}
	public String getConvertRule() {
		return convertRule;
	}
	public void setConvertRule(String convertRule) {
		this.convertRule = convertRule;
	}
}
