package com.biz;

import com.vo.JavaTokenInfo;
import com.vo.SqlTokenInfo;

public class TokenInfoBiz {

	public JavaTokenInfo createJavaTokenInfo(String tokenName, int tokenType, String symbolicName, String varName, String varType) {
		JavaTokenInfo javaTokenInfo = new JavaTokenInfo();
		javaTokenInfo.setTokenName(tokenName);
		javaTokenInfo.setTokenType(tokenType);
		javaTokenInfo.setSymbolicName(symbolicName);
		javaTokenInfo.setVarName(varName);
		javaTokenInfo.setVarType(varType);
		return javaTokenInfo;
	}

	public JavaTokenInfo createJavaTokenInfo(String tokenName, int tokenType) {
		return this.createJavaTokenInfo(tokenName, tokenType, "", "", "");
	}

	public SqlTokenInfo createSqlTokenInfo(String tokenName, int tokenType, String symbolicName, String aliasName, String tableName) {
		SqlTokenInfo sqlTokenInfo = new SqlTokenInfo();
		sqlTokenInfo.setTokenName(tokenName);
		sqlTokenInfo.setTokenType(tokenType);
		sqlTokenInfo.setSymbolicName(symbolicName);
		sqlTokenInfo.setAliasName(aliasName);
		sqlTokenInfo.setTableName(tableName);
		return sqlTokenInfo;
	}

	public SqlTokenInfo createSqlTokenInfo(String tokenName, String aliasName, String tableName) {
		return this.createSqlTokenInfo(tokenName, 0, "", aliasName, tableName);
	}

	public SqlTokenInfo createSqlTokenInfo(String tokenName, int tokenType, String symbolicName) {
		return this.createSqlTokenInfo(tokenName, tokenType, symbolicName, "", "");
	}
}
