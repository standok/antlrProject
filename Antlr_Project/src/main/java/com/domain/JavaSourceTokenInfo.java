package com.domain;

public class JavaSourceTokenInfo {

	private String tokenName;		// Token Name   - import
	private String symbolicId;		// Symbolic Id	- IMPORT
	private int symbolNo;			// Symbolic No	- [25]
	private String varType;			// 변수타입
	private String varName;			// 변수명
	
	public String getTokenName() {
		return tokenName;
	}
	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}
	public String getSymbolicId() {
		return symbolicId;
	}
	public void setSymbolicId(String symbolicId) {
		this.symbolicId = symbolicId;
	}
	public int getSymbolNo() {
		return symbolNo;
	}
	public void setSymbolNo(int symbolNo) {
		this.symbolNo = symbolNo;
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
	
}
