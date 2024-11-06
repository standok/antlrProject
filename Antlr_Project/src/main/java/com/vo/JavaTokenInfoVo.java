package com.vo;

public class JavaTokenInfoVo {

	private int tokenIndex;				// 토큰인덱스
	private String tokenName;			// 토큰명
	private int tokenType;				// 토큰타입
	private String symbolicName;		// 토큰타입명
	private int tokenLine;				// 토큰라인

	private String varType;				// 변수타입
	private String varName;				// 변수명

	private boolean isConvert = false;	// 전환여부
	private String convertRule = "";	// 변환 규칙

	private boolean isSqlLastLine = false;	// SQL 마지막 라인을 표시

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
	public String getVarType() {
		return varType;
	}
	public void setVarType(String varType) {
		this.varType = varType;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
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
	public boolean isSqlLastLine() {
		return isSqlLastLine;
	}
	public void setSqlLastLine(boolean isSqlLastLine) {
		this.isSqlLastLine = isSqlLastLine;
	}
}
