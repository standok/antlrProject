package com.svc.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;

import com.svc.IParsingJavaSvr;
import com.vo.JavaTokenInfo;

import util.antlr.Java8Parser;

public class ParsingJavaSvr implements IParsingJavaSvr {

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

		List<JavaTokenInfo> javaTokenList = new ArrayList<>();
		JavaTokenInfo javaTokenInfo = new JavaTokenInfo();

		int tokenIdx = 1;
		int tokenSize = tokenList.size();
		boolean state = false;

		//SQL 정보 저장
		for(; tokenIdx<tokenSize; tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();

			if (symbolNo == Java8Parser.Identifier) {
				// TODO: QueryManager 를 사용하지 않는 부분도 확인이 필요하다.
				if("QueryManager".equals(tokenName)
						&&"new".equals(tokenList.get(tokenIdx-1).getTokenName())) {
					javaTokenInfo = new JavaTokenInfo();
					javaTokenInfo.setTokenName(";");
					javaTokenInfo.setSymbolNo(Java8Parser.SEMI);
					javaTokenList.add(javaTokenInfo);
					state = false;
				}
			} else if(symbolNo == Java8Parser.StringLiteral) {

				tokenName = tokenName.replace("\"","");

				if(tokenName.contains("SELECT")) state = true;
				if(tokenName.contains("UPDATE")) state = true;
				if(tokenName.contains("DELETE")) state = true;
				if(tokenName.contains("WITH AS")) state = true;

				//LogManager.getLogger("debug").debug("["+state+"]tokenName==>"+tokenName);

				if(state) {
					javaTokenInfo = new JavaTokenInfo();
					javaTokenInfo.setTokenName(tokenName);
					javaTokenInfo.setSymbolNo(symbolNo);
					javaTokenList.add(javaTokenInfo);
				}
			}
		}

		return javaTokenList;
	}
}
