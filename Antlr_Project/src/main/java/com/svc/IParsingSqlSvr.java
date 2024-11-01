package com.svc;

import java.util.List;

import com.vo.SqlTokenInfo;

import util.antlr.PlSqlParser;

public interface IParsingSqlSvr {

	public List<SqlTokenInfo> getQueryTokenList();

	/**
	 * 설명 : SQL을 파싱해서 queryTokenList를 만든다.
	 *
	 * @param List<SqlTokenInfo> tokenList, PlSqlParser parser
	 * @return
	 * @throws Exception
	 */
	public void parsingSql(List<SqlTokenInfo> tokenList, PlSqlParser parser) throws Exception;

}
