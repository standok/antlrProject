package com.svc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;

import com.domain.JavaSourceTokenInfo;
import com.domain.TokenInfo;
import com.dto.TokenInfoCreateDto;
import com.svc.IParsingSvr;
import com.svc.ISearchSvr;
import com.svc.ITokenInfoSvc;
import com.util.Log;
import com.util.LogManager;

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;
import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class TokenInfoSvc implements ITokenInfoSvc {

	/**
	 * 설명 : 파일내용을 String으로 변환
	 */
	@Override
	public void convertFileToString(File file) throws Exception, IOException {

		String fileName = file.getName();

		InputStream in = new FileInputStream(file);
		InputStreamReader rd = new InputStreamReader(in, "UTF-8");
		BufferedReader br = new BufferedReader(rd);

		StringBuilder sb = new StringBuilder();
		String line;

		if(fileName.toUpperCase().endsWith(".JAVA")) {
			while((line=br.readLine()) != null) sb.append(line + "\n");
			parsingJava(sb);
		} else if(fileName.toUpperCase().endsWith(".SQL")) {
			while((line=br.readLine()) != null) sb.append(line.toUpperCase() + "\n");
			parsingSql(sb);
		}
	}

	/**
	 * 설명 : SQL 파싱
	 */
	private void parsingJava(StringBuilder sb) throws Exception, IOException {

		IParsingSvr parsingSvc = new ParsingSvr();
		ISearchSvr searchSvr = new SearchSvr();

		Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(sb.toString()));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Java8Parser parser = new Java8Parser(tokens);
		ParseTree tree = parser.compilationUnit();
		//Log.debug("result:"+tree.toStringTree(parser));
		//parser.setBuildParseTree(true);

		Vocabulary vocabulary = parser.getVocabulary();
		List<List<JavaSourceTokenInfo>> javaSrcList = addJavaList(lexer, parser, tokens, vocabulary);

		List<Integer> reservedWordList = new ArrayList<Integer>();

		reservedWordList.add(Java8Parser.PACKAGE);

		LogManager.getLogger("debug").debug("javaSrcList.size()======>"+javaSrcList.size());

		for(int i=0; i<javaSrcList.size(); i++) {
			List<JavaSourceTokenInfo> tokenList = javaSrcList.get(i);
			Log.printInfomation(lexer, parser, vocabulary, tokenList);

			// 변경할 라인 찾기

			// SQL문만 가져오기
			List<JavaSourceTokenInfo> parsingSqlList = parsingSvc.parsingJavaToSQL(tokenList, parser, reservedWordList);

			LogManager.getLogger("debug").debug(" << SQL 파싱 시작 ["+i+"]>>-------------------------");
			//Log.logListToString(parser, parsingSqlList);

			StringBuilder sbSql = new StringBuilder();
			for(int idx=0; idx<parsingSqlList.size(); idx++) {
				String tokenName = parsingSqlList.get(idx).getTokenName();
				int symbolNo = parsingSqlList.get(idx).getSymbolNo();

				if(symbolNo == Java8Parser.SEMI) {
					sbSql.append(tokenName.replace("\\n", ""));
					LogManager.getLogger("debug").debug("< Java소스에서 SQL 출력 ["+i+"] >");
					LogManager.getLogger("debug").debug(sbSql.toString());

					// SQL 파싱
					parsingSql(sbSql);

					// 초기화
					sbSql = new StringBuilder();
				} else {
					sbSql.append(tokenName.replace("\\n", "")+"\n");
				}
			}
		}
	}

	/**
	 * 설명 : 토큰 구분 결과를 List에 저장 후 반환 / 주석, 피룡없는 sql문 구별 기능 추가해야함.
	 */
	private List<List<JavaSourceTokenInfo>> addJavaList(Java8Lexer lexer, Java8Parser parser, CommonTokenStream tokenStream, Vocabulary vocabulary) {

		List<List<JavaSourceTokenInfo>> javaSrcList = new ArrayList<>();
		List<JavaSourceTokenInfo> list = new ArrayList<>();
		JavaSourceTokenInfo tokenInfo = new JavaSourceTokenInfo();

		for(int i = 0; i<tokenStream.size(); i++) {
			int symbolNo = tokenStream.get(i).getType();
			String tokenName = tokenStream.get(i).getText();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);

			// 제외요건
			if(symbolNo == Java8Parser.PACKAGE) continue;
			if(symbolNo == Java8Parser.IMPORT) continue;

			tokenInfo = new JavaSourceTokenInfo();
			tokenInfo.setSymbolNo(symbolNo);
			tokenInfo.setSymbolicId(symbolicId);
			tokenInfo.setTokenName(tokenName);

			list.add(tokenInfo);

		}
		javaSrcList.add(list);
		return javaSrcList;
	}

	/**
	 * 설명 : SQL 파싱
	 */
	private void parsingSql(StringBuilder sb) throws Exception {

		IParsingSvr parsingSvc = new ParsingSvr();
		ISearchSvr searchSvr = new SearchSvr();

		PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sb.toString()));
		TokenStream tokenStream =new CommonTokenStream(lexer);
		PlSqlParser parser = new PlSqlParser(tokenStream);
		parser.setBuildParseTree(true);

		parser.data_manipulation_language_statements();

		Vocabulary vocabulary = parser.getVocabulary();
		List<List<TokenInfo>> sqlList = addSqlList(lexer, parser, tokenStream, vocabulary);
		List<Integer> reservedWordList = initReservedWordList(parser);

		for(int i=0; i<sqlList.size(); i++) {
			List<TokenInfo> tokenList = sqlList.get(i);
			Log.printInfomation(lexer, parser, vocabulary, tokenList);

			// 실제 SQL을 파싱하는 로직
			parsingSvc.parsingSql(tokenList, parser, reservedWordList);
			List<TokenInfo> queryTokenList = parsingSvc.getQueryTokenList();

			LogManager.getLogger("debug").debug("["+i+"] Output Result-------------------------");
			Log.logListToString(parser, queryTokenList);
			LogManager.getLogger("debug").debug("["+i+"] ---------------------------------------");

			searchSvr.searchTable(sqlList, queryTokenList);
		}
	}

	/**
	 * 설명 : 토큰 구분 결과를 List에 저장 후 반환 / 주석, 피룡없는 sql문 구별 기능 추가해야함.
	 */
	private List<List<TokenInfo>> addSqlList(PlSqlLexer lexer, PlSqlParser parser, TokenStream tokenStream, Vocabulary vocabulary) {
		TokenInfoCreateDto tokenInfoCreateDto = new TokenInfoCreateDto();

		List<List<TokenInfo>> sqlList = new ArrayList<>();
		List<TokenInfo> list = new ArrayList<>();

		for(int i = 0; i<tokenStream.size(); i++) {
			int symbolNo = tokenStream.get(i).getType();
			String tokenName = tokenStream.get(i).getText();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);

			//Log.debug("tokenName=>"+tokenName);

			if(symbolNo == PlSqlParser.COMMENT) continue;
			if(symbolNo == PlSqlParser.SINGLE_LINE_COMMENT) continue;
			if(symbolNo == PlSqlParser.MULTI_LINE_COMMENT) continue;
			if(symbolNo == PlSqlParser.REMARK_COMMENT) continue;

			list.add(tokenInfoCreateDto.toEntity(tokenName, symbolicId, symbolNo));

			if(symbolNo == PlSqlParser.SEMICOLON) {
				sqlList.add(list);
				list = new ArrayList<>();
			}
		}

		return sqlList;
	}

	/**
	 * 설명 : 파라미터가 예약어에 속한다면 true 반환, 아니면 false 반환
	 */
	@Override
	public boolean checkReservedWord(int SymbolNo, List<Integer> reserveWordList) {
		return reserveWordList.contains(SymbolNo);
	}

	/**
	 * 설명 : 예약어에 속하는 단어 추가
	 * TODO : PlSqlParser.g4 파일에서 예약어를 구분하는 문장이 있을것이다. 확인 해야함.
	 */
	private List<Integer> initReservedWordList(PlSqlParser parser) {
		List<Integer> reservedWordList = new ArrayList<Integer>();

		//reservedWordList.add(PlSqlParser.SELECT);
		reservedWordList.add(PlSqlParser.UPDATE);
		reservedWordList.add(PlSqlParser.DELETE);
		reservedWordList.add(PlSqlParser.SET);
		//reservedWordList.add(PlSqlParser.WITH);
		reservedWordList.add(PlSqlParser.INTO);
		//reservedWordList.add(PlSqlParser.FROM);
		//reservedWordList.add(PlSqlParser.WHERE);
		reservedWordList.add(PlSqlParser.SEMICOLON);

		return reservedWordList;
	}
}
