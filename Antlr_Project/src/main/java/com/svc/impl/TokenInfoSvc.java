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

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;
import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class TokenInfoSvc implements ITokenInfoSvc {
	
	/**
	 * 설명 : 파일 오픈
	 */
	public void parsing(File file) throws IOException {
		
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
	public void parsingJava(StringBuilder sb) throws IOException {
		
		IParsingSvr parsingSvc = new ParsingSvr();
		ISearchSvr searchSvr = new SearchSvr();
		
		Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(sb.toString()));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Java8Parser parser = new Java8Parser(tokens);
		ParseTree tree = parser.compilationUnit();
		//Log.debug("result:"+tree.toStringTree(parser));
		//parser.setBuildParseTree(true);
		
		Vocabulary vocabulary = parser.getVocabulary();
		List<List<JavaSourceTokenInfo>> javaMethodList = addJavaList(lexer, parser, tokens, vocabulary);
		
		List<Integer> reservedWordList = new ArrayList<Integer>();
		
		reservedWordList.add(Java8Parser.PACKAGE);
		
		Log.debug("javaMethodList.size()======>"+javaMethodList.size());
		
		for(int i=0; i<javaMethodList.size(); i++) {
			List<JavaSourceTokenInfo> tokenList = javaMethodList.get(i);
			Log.printInfomation(lexer, parser, vocabulary, tokenList);
			
			// 변경할 라인 찾기
			
			
			// 실제 SQL을 파싱하는 로직
			List<JavaSourceTokenInfo> parsingResultList = parsingSvc.parsingJava(tokenList, parser, reservedWordList);
			
			Log.debug("["+i+"] Output Result-------------------------");
//			Log.logListToString(parser, parsingResultList);
			Log.debug("["+i+"] ---------------------------------------");
			
//			searchSvr.searchTable(javaMethodList, parsingResultList);
		}
	}
	
	/**
	 * 설명 : 토큰 구분 결과를 List에 저장 후 반환 / 주석, 피룡없는 sql문 구별 기능 추가해야함.
	 */
	public List<List<JavaSourceTokenInfo>> addJavaList(Java8Lexer lexer, Java8Parser parser, CommonTokenStream tokenStream, Vocabulary vocabulary) {
		
		List<List<JavaSourceTokenInfo>> javaMethodList = new ArrayList<>();
		List<JavaSourceTokenInfo> list = new ArrayList<>();
		JavaSourceTokenInfo tokenInfo = new JavaSourceTokenInfo();
		
		for(int i = 0; i<tokenStream.size(); i++) {
			int symbolNo = tokenStream.get(i).getType();
			String tokenName = tokenStream.get(i).getText();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);
			
			//Log.debug("["+i+"]tokenName ="+tokenName+", symbolicId ="+symbolicId+"["+symbolNo+"]");
			
			if(symbolNo == Java8Parser.PACKAGE) continue;
			
			tokenInfo = new JavaSourceTokenInfo();
			tokenInfo.setSymbolNo(symbolNo);
			tokenInfo.setSymbolicId(symbolicId);
			tokenInfo.setTokenName(tokenName);
			
			list.add(tokenInfo);
//			Log.logListToString(parser, list);
			
//			if(symbolNo == Java8Parser.SEMI) {
//				javaMethodList.add(list);
//				list = new ArrayList<>();
//			}
		}
		javaMethodList.add(list);
		return javaMethodList;
	}
	
	/**
	 * 설명 : SQL 파싱
	 */
	public void parsingSql(StringBuilder sb) throws IOException {

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
		
		Log.debug("sqlList.size()======>"+sqlList.size());
		
		for(int i=0; i<sqlList.size(); i++) {
			List<TokenInfo> tokenList = sqlList.get(i);
			Log.printInfomation(lexer, parser, vocabulary, tokenList);
			
			// 실제 SQL을 파싱하는 로직
			List<TokenInfo> parsingResultList = parsingSvc.parsingSql(tokenList, parser, reservedWordList);
			
			Log.debug("["+i+"] Output Result-------------------------");
			Log.logListToString(parser, parsingResultList);
			Log.debug("["+i+"] ---------------------------------------");
			
			searchSvr.searchTable(sqlList, parsingResultList);
		}		
	}
	
	/**
	 * 설명 : 토큰 구분 결과를 List에 저장 후 반환 / 주석, 피룡없는 sql문 구별 기능 추가해야함.
	 */
	public List<List<TokenInfo>> addSqlList(PlSqlLexer lexer, PlSqlParser parser, TokenStream tokenStream, Vocabulary vocabulary) {
		TokenInfoCreateDto tokenInfoCreateDto = new TokenInfoCreateDto();
		
		List<List<TokenInfo>> sqlList = new ArrayList<>();
		List<TokenInfo> list = new ArrayList<>();
		
		for(int i = 0; i<tokenStream.size(); i++) {
			int symbolNo = tokenStream.get(i).getType();
			String tokenName = tokenStream.get(i).getText();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);
			
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
	public List<Integer> initReservedWordList(PlSqlParser parser) {
		List<Integer> reservedWordList = new ArrayList<Integer>();
		
		reservedWordList.add(PlSqlParser.SELECT);
		reservedWordList.add(PlSqlParser.INTO);
		reservedWordList.add(PlSqlParser.FROM);
		reservedWordList.add(PlSqlParser.WHERE);
		reservedWordList.add(PlSqlParser.SEMICOLON);
	
		return reservedWordList;
	}
}
