package com.svc;

import java.util.List;

import com.vo.JavaTokenInfoVo;

public interface IParsingJavaSvr {

	public List<JavaTokenInfoVo> getJavaTokenList();

	/**
	 * 설명 : String을 Parsing해서 JavaTokenList 정보 저장
	 *
	 * @param StringBuilder
	 * @return
	 * @throws Exception
	 */
	public void parsingJava(StringBuilder sb) throws Exception;

	/**
	 * 설명 : JavaTokenList의 SQL 소스를 변경한다.
	 *
	 * @param StringBuilder sbSql, int startLine, int endLine
	 * @return
	 * @throws Exception
	 */
	public void modSqlInJavaConList(StringBuilder sbSql, int startLine, int endLine) throws Exception;

	/**
	 * 설명 : javaTokenList를 String으로 변환
	 *
	 * @param boolean bufferYn
	 * @return StringBuilder
	 * @throws Exception
	 */
	public StringBuilder getJavaToString(boolean bufferYn) throws Exception;

}
