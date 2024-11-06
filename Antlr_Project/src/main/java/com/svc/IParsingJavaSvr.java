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
	 * @throws
	 */
	public void parsingJava(StringBuilder sb) throws Exception;

}
