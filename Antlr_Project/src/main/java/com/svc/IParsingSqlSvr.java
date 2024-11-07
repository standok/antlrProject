package com.svc;

import java.util.List;

import com.vo.SqlTokenInfoVo;

public interface IParsingSqlSvr {

	public List<List<SqlTokenInfoVo>> getSqlConList();
	public List<SqlTokenInfoVo> getQueryTokenList();

	/**
	 * 설명 : SQL 파일내용을 파싱해서 getSqlConList를 만든다.
	 *
	 * @param StringBuilder sb
	 * @return
	 * @throws Exception
	 */
	public void parsingSql(StringBuilder sb) throws Exception;

	/**
	 * 설명 : Query를 파싱해서 queryTokenList를 만든다.
	 *
	 * @param List<SqlTokenInfoVo> tokenList
	 * @return
	 * @throws Exception
	 */
	public void parsingQuery(List<SqlTokenInfoVo> tokenList) throws Exception;

}
