package util.test;

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

	public SqlTokenInfoVo createSqlTokenInfoVo(int tokenIndex, String tokenName, int tokenType, String symbolicName, int tokenLine, String aliasName, String tableId) {
		SqlTokenInfoVo sqlTokenInfoVo = new SqlTokenInfoVo();
		sqlTokenInfoVo.setTokenIndex(tokenIndex);
		sqlTokenInfoVo.setTokenName(tokenName);
		sqlTokenInfoVo.setTokenType(tokenType);
		sqlTokenInfoVo.setSymbolicName(symbolicName);
		sqlTokenInfoVo.setTokenLine(tokenLine);
		sqlTokenInfoVo.setAliasName(aliasName);
		sqlTokenInfoVo.setTableId(tableId);
		return sqlTokenInfoVo;
	}

	public SqlTokenInfoVo createSqlTokenInfoVo(String tokenName, String aliasName, String tableId) {
		return this.createSqlTokenInfoVo(0, tokenName, 0, "", 0, aliasName, tableId);
	}

	public SqlTokenInfoVo createSqlTokenInfoVo(int tokenIndex, String tokenName, int tokenType, String symbolicName, int tokenLine) {
		return this.createSqlTokenInfoVo(tokenIndex, tokenName, tokenType, symbolicName, tokenLine, "", "");
	}
}
