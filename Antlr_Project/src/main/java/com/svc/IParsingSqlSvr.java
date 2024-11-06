package com.svc;

import java.util.List;

import com.vo.SqlTokenInfoVo;

import util.antlr.PlSqlParser;

public interface IParsingSqlSvr {

	public List<SqlTokenInfoVo> getQueryTokenList();

	/**
	 * 설명 : SQL을 파싱해서 queryTokenList를 만든다.
	 *
	 * @param List<SqlTokenInfoVo> tokenList, PlSqlParser parser
	 * @return
	 * @throws Exception
	 */
	public void parsingSql(List<SqlTokenInfoVo> tokenList, PlSqlParser parser) throws Exception;

}
