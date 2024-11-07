package com.biz;

import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;

public class TokenInfoBiz {

	public JavaTokenInfoVo createJavaTokenInfoVo(int tokenIndex, String tokenName, int tokenType, String symbolicName, int tokenLine, String varName, String varType) {
		JavaTokenInfoVo javaTokenInfoVo = new JavaTokenInfoVo();
		javaTokenInfoVo.setTokenIndex(tokenIndex);
		javaTokenInfoVo.setTokenName(tokenName);
		javaTokenInfoVo.setTokenType(tokenType);
		javaTokenInfoVo.setSymbolicName(symbolicName);
		javaTokenInfoVo.setTokenLine(tokenLine);
		javaTokenInfoVo.setVarName(varName);
		javaTokenInfoVo.setVarType(varType);
		return javaTokenInfoVo;
	}

	public JavaTokenInfoVo createJavaTokenInfoVo(int tokenIndex, String tokenName, int tokenType, int tokenLine) {
		return this.createJavaTokenInfoVo(tokenIndex, tokenName, tokenType, "", tokenLine, "", "");
	}

	public SqlTokenInfoVo createSqlTokenInfoVo(int tokenIndex, String tokenName, int tokenType, String symbolicName, int tokenLine, String aliasName, String tableName) {
		SqlTokenInfoVo sqlTokenInfoVo = new SqlTokenInfoVo();
		sqlTokenInfoVo.setTokenName(tokenName);
		sqlTokenInfoVo.setTokenType(tokenType);
		sqlTokenInfoVo.setSymbolicName(symbolicName);
		sqlTokenInfoVo.setAliasName(aliasName);
		sqlTokenInfoVo.setTableName(tableName);
		return sqlTokenInfoVo;
	}

	public SqlTokenInfoVo createSqlTokenInfoVo(String tokenName, String aliasName, String tableName) {
		return this.createSqlTokenInfoVo(0, tokenName, 0, "", 0, aliasName, tableName);
	}

	public SqlTokenInfoVo createSqlTokenInfoVo(int tokenIndex, String tokenName, int tokenType, String symbolicName, int tokenLine) {
		return this.createSqlTokenInfoVo(tokenIndex, tokenName, tokenType, symbolicName, tokenLine, "", "");
	}
}
