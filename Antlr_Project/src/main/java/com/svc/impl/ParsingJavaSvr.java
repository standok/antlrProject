package com.svc.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;

import com.biz.TokenInfoBiz;
import com.svc.IParsingJavaSvr;
import com.vo.JavaTokenInfo;

import util.antlr.Java8Parser;

public class ParsingJavaSvr implements IParsingJavaSvr {

	private int tokenIdx;

	/**
	 * 설명 : Java 소스에서 SQL문을 추출
	 *
	 * @param List<JavaTokenInfo> tokenList, Java8Parser parser
	 * @return List<JavaTokenInfo>
	 * @throws Exception
	 */
	@Override
	public List<JavaTokenInfo> getSqlInJava(List<JavaTokenInfo> tokenList) throws Exception {

		LogManager.getLogger("debug").debug("ParsingJavaSvr.getSqlInJava Start~!!");

		TokenInfoBiz tokenInfoBiz = new TokenInfoBiz();

		List<JavaTokenInfo> javaTokenList = new ArrayList<>();

		boolean state = false;

		//SQL 정보 저장
		for(tokenIdx = 1; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int tokenType = tokenList.get(tokenIdx).getTokenType();

			if (tokenType == Java8Parser.Identifier) {
				// TODO: QueryManager 를 사용하지 않는 부분도 확인이 필요하다.
				if("QueryManager".equals(tokenName)
						&&"new".equals(tokenList.get(tokenIdx-1).getTokenName())) {
					javaTokenList.add(tokenInfoBiz.createJavaTokenInfo(";", Java8Parser.SEMI));
					state = false;
				}
			} else if(tokenType == Java8Parser.StringLiteral) {

				tokenName = tokenName.replace("\"","");

				if(tokenName.contains("SELECT")) state = true;
				if(tokenName.contains("UPDATE")) state = true;
				if(tokenName.contains("DELETE")) state = true;
				if(tokenName.contains("WITH AS")) state = true;

				//LogManager.getLogger("debug").debug("["+state+"]tokenName==>"+tokenName);

				if(state) {
					javaTokenList.add(tokenInfoBiz.createJavaTokenInfo(tokenName, tokenType));
				}
			}
		}

		return javaTokenList;
	}
}
