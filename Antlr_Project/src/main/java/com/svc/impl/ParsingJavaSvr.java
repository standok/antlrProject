package com.svc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;

import com.svc.IParsingJavaSvr;
import com.util.Log;
import com.util.LogManager;
import com.util.StringUtil;
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

		Log.printMethod("[START]");

		// variable Initialization
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
		String bufferName = "";

		//SQL 정보 저장
		for(int i = 0; i < javaTokenList.size(); i++) {

//			int tokenIndex = tokenList.get(i).getTokenIndex();
			String tokenName = javaTokenList.get(i).getTokenName();
			int tokenType = javaTokenList.get(i).getTokenType();
//			int tokenLine = tokenList.get(i).getTokenLine();

			if (tokenType == Java8Parser.Identifier) {

				// SQL StringBuffer 소스의 동적변수 찾기
				if(state) {
					if((i-2 > 0)
						&& "append".equals(javaTokenList.get(i-2).getTokenName())
						&&(i+3 < javaTokenList.size())
						&& "append".equals(javaTokenList.get(i+3).getTokenName())) {
						javaTokenList.get(i).setConvert(true);
						javaTokenList.get(i).setConvertRule("동적변수");
						javaTokenList.get(i).setConvertBufferName(bufferName);
					}
				}

				// TODO: QueryManager 를 사용하지 않는 부분도 확인이 필요하다.
				if("QueryManager".equals(tokenName)
						&&"new".equals(javaTokenList.get(i-1).getTokenName())) {
					javaTokenList.get(i).setSqlLastLine(true);
					state = false;
					bufferName = "";
				}

				// SQL StringBuffer 변수명 찾기
				if("StringBuffer".equals(tokenName)
					&& Java8Parser.Identifier == javaTokenList.get(i+1).getTokenType()
					&& Java8Parser.ASSIGN == javaTokenList.get(i+2).getTokenType()
					) {
					bufferName = javaTokenList.get(i+1).getTokenName();
				}

			} else if(tokenType == Java8Parser.StringLiteral) {

				tokenName = tokenName.replace("\"","");

				// TODO: 테스트하면서 추가
				if(tokenName.contains("SELECT")) state = true;
				if(tokenName.contains("UPDATE")) state = true;
				if(tokenName.contains("DELETE")) state = true;
				if(tokenName.contains("INSERT")) state = true;
				if(tokenName.contains("WITH AS")) state = true;

				//LogManager.getLogger("debug").debug("["+state+"]tokenName==>"+tokenName);

				if(state) {
					javaTokenList.get(i).setConvert(true);
					javaTokenList.get(i).setConvertRule("SQL변환");
					javaTokenList.get(i).setConvertBufferName(bufferName);
				}
			}
		}

		// parsing결과 출력
		Log.printInfomation(lexer, parser, javaTokenList);

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : JavaConList의 SQL 소스를 변경한다.
	 *
	 * @param StringBuilder sbSql, int startLine, int endLine
	 * @return
	 * @throws Exception
	 */
	@Override
	public void modSqlInJavaConList(StringBuilder sbSql, int startLine, int endLine) throws Exception {
		Log.printMethod("[START]");

		if(StringUtil.isEmtpy(sbSql.toString())) {
			throw new Exception("SQL이 입력되지 않았습니다.");
		} else {
			// StringBuilder 의 내용을 Token단위로 분류한다
			StringTokenizer st = new StringTokenizer(sbSql.toString(), "#\n");
			int countTokens = st.countTokens();
			String sbSqlArr[] = new String[countTokens];

			//for문으로 얻기
			for(int i = 0; i < countTokens; i++) {
				sbSqlArr[i] = st.nextToken();
			}

			int convCnt  = 0;
			for(int i = startLine; i <= endLine; i++) {
				String tokenName = javaTokenList.get(i).getTokenName();
				int tokenType = javaTokenList.get(i).getTokenType();

				// SQL(전환대상)만 조회
				if(javaTokenList.get(i).isConvert()) {

					String afTokenName = sbSqlArr[convCnt++];

					if(tokenType == Java8Parser.StringLiteral) {
						if(tokenName.indexOf("\\n") > 0) {
							afTokenName = "\""+afTokenName+"\\n\"";
						} else {
							afTokenName = "\""+afTokenName+"\"";
						}
					}
					javaTokenList.get(i).setTokenName(afTokenName);
//					LogManager.getLogger("debug").debug(StringUtil.padString(Integer.toString(convCnt), 3, " ", true)
//							+"("+ javaTokenList.get(i).getTokenIndex()+")"
//							+" : "+ StringUtil.rightBytesPad(tokenName, 50)
//							+" ====> "+ StringUtil.rightBytesPad(afTokenName, 50)
//							);
				}
			}

			LogManager.getLogger("debug").debug("※ 쿼리 배열건수=["+countTokens+"], 전환대상건수=["+convCnt+"]");
		}

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : javaTokenList를 String으로 변환
	 *
	 * @param boolean bufferYn
	 * @return StringBuilder
	 * @throws Exception
	 */
	@Override
	public StringBuilder getJavaToString(boolean bufferYn) throws Exception {
		Log.printMethod("[START]");

		int lastLine = 1;	// 마지막줄
		int indent = 0;		// 들여쓰기깊이
		StringBuilder str = new StringBuilder();	// 소스내용
		boolean startCharYn = false; 				//

		for(int i = 0; i < javaTokenList.size(); i++) {

			String tokenName = javaTokenList.get(i).getTokenName();
			int tokenType = javaTokenList.get(i).getTokenType();
//			String symbolicName = vocabulary.getSymbolicName(tokenType);
			int tokenLine = javaTokenList.get(i).getTokenLine();

			// 들여쓰기 깊이 설정
			if(tokenType == Java8Parser.LBRACE) {
				indent++;
			} else if(tokenType == Java8Parser.RBRACE) {
				indent--;
			}


			if(tokenLine - lastLine > 0) {

				// 줄바꿈
				for(int s = 0; s < tokenLine - lastLine; s++) {
					str.append("\n");
				}

				// 들여쓰기
				for(int d = 0; d < indent; d++) {
					str.append("\t");
				}

				lastLine = tokenLine + countLines(tokenName) -1;

				startCharYn = true;
			} else {
				startCharYn = false;
			}

//			LogManager.getLogger("debug").debug("[Token]["+i+"]==>"
//					+"["+tokenName+"]"
//					+"["+symbolicName+"]"
//					+"["+lastLine+"]"
//					+"["+tokenLine+"]"
//					+"["+countLines(tokenName)+"]"
//					);

			/*****************************************
			 * 앞/뒤 공백 입력
			 * TODO: 테스트하면서 추가
			 *****************************************/
			// 뒤 공백
			if(tokenType == Java8Parser.PACKAGE
					||tokenType == Java8Parser.IMPORT
					||tokenType == Java8Parser.CLASS
					||tokenType == Java8Parser.PUBLIC
					||tokenType == Java8Parser.PRIVATE
					||tokenType == Java8Parser.THROW
					||tokenType == Java8Parser.THROWS
					||tokenType == Java8Parser.RETURN
					||tokenType == Java8Parser.COMMA
					||tokenType == Java8Parser.INT
					||tokenType == Java8Parser.NEW
					||tokenType == Java8Parser.NEW
					||tokenType == Java8Parser.NEW

					||tokenType == Java8Parser.EXTENDS
					||tokenType == Java8Parser.ASSIGN
					||tokenType == Java8Parser.ADD
					||tokenType == Java8Parser.SUB
					||tokenType == Java8Parser.MOD
				) {

				// 앞뒤 공백
				if(tokenType == Java8Parser.EXTENDS
					||tokenType == Java8Parser.ASSIGN
					||tokenType == Java8Parser.ADD
					||tokenType == Java8Parser.SUB
					||tokenType == Java8Parser.MOD
					) {
					str.append(" ");
				}

				str.append(tokenName + " ");

			} else if(tokenType == Java8Parser.LINE_COMMENT) {
				if(!startCharYn) {
					str.append("\t");
				}

				str.append(tokenName);

			} else if(tokenType == Java8Parser.LBRACE) {	// {
				if(!startCharYn) {
					if((i-1 > 0) && javaTokenList.get(i-1).getTokenType() == Java8Parser.Identifier) {
						str.append(" ");
					}
				}

				str.append(tokenName);

			} else if(tokenType == Java8Parser.RPAREN) {	// )
				if( (i-1 > 0)
				 && javaTokenList.get(i-1).getTokenType() != Java8Parser.LPAREN
				 && (i+1 < javaTokenList.size())
				 && (javaTokenList.get(i+1).getTokenType() != Java8Parser.SEMI
				     &&javaTokenList.get(i+1).getTokenType() != Java8Parser.RPAREN
				     &&javaTokenList.get(i+1).getTokenType() != Java8Parser.COMMA
				     &&javaTokenList.get(i+1).getTokenType() != Java8Parser.DOT
				     )) { // (
					str.append(tokenName + " ");
				} else {
					str.append(tokenName);
				}

			} else if(tokenType == Java8Parser.RBRACK) {	// ]
				if((i-1 > 0) && javaTokenList.get(i-1).getTokenType() == Java8Parser.LBRACK) {	// [
					str.append(tokenName + " ");
				} else {
					str.append(tokenName);
				}
			} else if(tokenType == Java8Parser.EOF) {
				// Skip
			} else if(tokenType == Java8Parser.Identifier) {
//				if("String".equals(tokenName)) {
//					str.append(tokenName + " ");
//				} else
				if((i-1 > 0) && javaTokenList.get(i-1).getTokenType() == Java8Parser.Identifier) {
					str.append(" " + tokenName);
				} else {
					str.append(tokenName);
				}
			} else {
				str.append(tokenName);
			}
		}
//		LogManager.getLogger("debug").debug("<결과값확인> =============================");
//		LogManager.getLogger("debug").debug("\n"+str.toString());

		Log.printMethod("[END]");
		return str;
	}

	private static int countLines(String str){
	   String[] lines = str.split("\r\n|\r|\n");
	   return  lines.length;
	}
}
