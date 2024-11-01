package com.biz;

import com.vo.JavaTokenInfo;
import com.vo.SqlTokenInfo;

public class TokenInfoBiz {

	public JavaTokenInfo createTokenInfo(String tokenName, String symbolicId, int symbolNo, String varName, String varType) {
		JavaTokenInfo javaTokenInfo = new JavaTokenInfo();
		javaTokenInfo.setTokenName(tokenName);
		javaTokenInfo.setSymbolicId(symbolicId);
		javaTokenInfo.setSymbolNo(symbolNo);
		javaTokenInfo.setVarName(varName);
		javaTokenInfo.setVarType(varType);
		return javaTokenInfo;
	}

	public SqlTokenInfo createSqlTokenInfo(String tokenName, String symbolicId, int symbolNo, String aliasName, String tableName) {
		SqlTokenInfo sqlTokenInfo = new SqlTokenInfo();
		sqlTokenInfo.setTokenName(tokenName);
		sqlTokenInfo.setSymbolicId(symbolicId);
		sqlTokenInfo.setSymbolNo(symbolNo);
		sqlTokenInfo.setAliasName(aliasName);
		sqlTokenInfo.setTableName(tableName);
		return sqlTokenInfo;
	}

	public SqlTokenInfo createTokenInfo(String tokenName, String aliasName, String tableName) {
		return this.createSqlTokenInfo(tokenName, "", 0, aliasName, tableName);
	}

	public SqlTokenInfo createTokenInfo(String tokenName, String symbolicId, int symbolNo) {
		return this.createSqlTokenInfo(tokenName, symbolicId, symbolNo, "", "");
	}
}
