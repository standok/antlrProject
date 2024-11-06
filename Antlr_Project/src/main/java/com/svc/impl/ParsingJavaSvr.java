package com.svc.impl;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;

import com.svc.IParsingJavaSvr;
import com.util.Log;
import com.vo.JavaTokenInfoVo;

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;

public class ParsingJavaSvr implements IParsingJavaSvr {

	private List<JavaTokenInfoVo> javaTokenList = null;

	@Override
	public List<JavaTokenInfoVo> getJavaTokenList() {
		return javaTokenList;
	}

	/**
	 * 설명 : String을 Parsing해서 JavaTokenList 정보 저장
	 *
	 * @param StringBuilder
	 * @return
	 * @throws
	 */
	@Override
	public void parsingJava(StringBuilder sb) throws Exception {

		// 변수 초기화
		javaTokenList = new ArrayList<JavaTokenInfoVo>();

		Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(sb.toString()));
		CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
		Java8Parser parser = new Java8Parser(commonTokenStream);
		ParseTree tree = parser.compilationUnit();
		//Log.debug("result:"+tree.toStringTree(parser));
		//parser.setBuildParseTree(true);

		Vocabulary vocabulary = parser.getVocabulary();

		JavaTokenInfoVo vo = new JavaTokenInfoVo();

		for(int i = 0; i < commonTokenStream.size(); i++) {

			int tokenIndex = commonTokenStream.get(i).getTokenIndex();
			String tokenName = commonTokenStream.get(i).getText();
			int tokenType = commonTokenStream.get(i).getType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);
			int tokenLine = commonTokenStream.get(i).getLine();

//			Log.debug("ts("+i+").getTokenIndex()==========>"+commonTokenStream.get(i).getTokenIndex());
//			Log.debug("ts("+i+").getType()================>"+Integer.toString(commonTokenStream.get(i).getType()));
//			Log.debug("ts("+i+").getText()================>"+commonTokenStream.get(i).getText());
//			Log.debug("ts("+i+").getChannel()=============>"+commonTokenStream.get(i).getChannel());
//			Log.debug("ts("+i+").getCharPositionInLine()==>"+commonTokenStream.get(i).getCharPositionInLine());
//			Log.debug("ts("+i+").getLine()================>"+commonTokenStream.get(i).getLine());
//			Log.debug("ts("+i+").getStartIndex()==========>"+commonTokenStream.get(i).getStartIndex());
//			Log.debug("ts("+i+").getStopIndex()===========>"+commonTokenStream.get(i).getStopIndex());

			vo = new JavaTokenInfoVo();
			vo.setTokenIndex(tokenIndex);
			vo.setTokenName(tokenName);
			vo.setTokenType(tokenType);
			vo.setSymbolicName(symbolicName);
			vo.setTokenLine(tokenLine);

			javaTokenList.add(vo);
		}

		// Java 소스에서 전환대상을 추출
		boolean state = false;

		//SQL 정보 저장
		for(int listCnt = 1; listCnt < javaTokenList.size(); listCnt++) {

//			int tokenIndex = tokenList.get(listCnt).getTokenIndex();
			String tokenName = javaTokenList.get(listCnt).getTokenName();
			int tokenType = javaTokenList.get(listCnt).getTokenType();
//			int tokenLine = tokenList.get(listCnt).getTokenLine();

			if (tokenType == Java8Parser.Identifier) {
				// TODO: QueryManager 를 사용하지 않는 부분도 확인이 필요하다.
				if("QueryManager".equals(tokenName)
						&&"new".equals(javaTokenList.get(listCnt-1).getTokenName())) {
					javaTokenList.get(listCnt).setSqlLastLine(true);
					state = false;
				}
			} else if(tokenType == Java8Parser.StringLiteral) {

				tokenName = tokenName.replace("\"","");

				// TODO: 테스트하면서 추가
				if(tokenName.contains("SELECT")) state = true;
				if(tokenName.contains("UPDATE")) state = true;
				if(tokenName.contains("DELETE")) state = true;
				if(tokenName.contains("WITH AS")) state = true;

				//LogManager.getLogger("debug").debug("["+state+"]tokenName==>"+tokenName);

				if(state) {
					javaTokenList.get(listCnt).setConvert(true);
					javaTokenList.get(listCnt).setConvertRule("SQL변환");
				}
			}
		}

		// 파싱결과 출력
		Log.printInfomation(lexer, parser, javaTokenList);
	}
}
