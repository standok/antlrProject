package com.svc;

import java.util.List;

import com.vo.JavaTokenInfo;

public interface IParsingJavaSvr {

	/**
	 * 설명 : Java 소스에서 SQL문을 추출
	 *
	 * @param List<JavaTokenInfo> tokenList
	 * @return List<JavaTokenInfo>
	 * @throws Exception
	 */
	public List<JavaTokenInfo> getSqlInJava(List<JavaTokenInfo> tokenList) throws Exception;
}
