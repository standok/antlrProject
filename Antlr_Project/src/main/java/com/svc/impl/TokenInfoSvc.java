package com.svc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;

import com.domain.TokenInfo;
import com.dto.TokenInfoCreateDto;
import com.svc.IParsingSvr;
import com.svc.ISearchSvr;
import com.svc.ITokenInfoSvc;
import com.util.Log;

import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class TokenInfoSvc implements ITokenInfoSvc {
	
	/**
	 * ���� ���� �� ���� �ʱ�ȭ
	 * �Ľ� ���� ����
	 * TODO sql���� ������ ������ �� ó���ؾ���.
	 * @throws IOException
	 */
	public void parsing(File file) throws IOException {
		
		IParsingSvr parsingSvc = new ParsingSvr();
		ISearchSvr searchSvr = new SearchSvr();
		
		InputStream in = new FileInputStream(file);
		InputStreamReader rd = new InputStreamReader(in, "UTF-8");
		BufferedReader br = new BufferedReader(rd);
		
		StringBuilder sb = new StringBuilder();
		
		String line;
		while((line=br.readLine()) != null) sb.append(line.toUpperCase() + "\n");
		
		CharStream charStream = CharStreams.fromString(sb.toString());
		
		PlSqlLexer lexer = new PlSqlLexer(charStream);
		TokenStream tokenStream =new CommonTokenStream(lexer);
		PlSqlParser parser = new PlSqlParser(tokenStream);
		parser.setBuildParseTree(true);

		parser.data_manipulation_language_statements();
		
		Vocabulary vocabulary = parser.getVocabulary();
		List<List<TokenInfo>> sqlList = addSqlList(lexer, parser, tokenStream, vocabulary);
		
		List<Integer> reservedWordList = initReservedWordList(parser);
		
		Log.debug("sqlList.size()======>"+sqlList.size());
		
		List<TokenInfo> tokenList = sqlList.get(0);
		Log.printInfomation(lexer, parser, vocabulary, tokenList);
		
		// ���� SQL�� �Ľ��ϴ� ����
		List<TokenInfo> parsingResultList = parsingSvc.parsingSql(tokenList, parser, reservedWordList);
		
		Log.debug("Output Result -------------------------");
		Log.logListToString(parser, parsingResultList);
		Log.debug("---------------------------------------");
		
		searchSvr.searchTable(sqlList, parsingResultList);
		
	}
	
	/**
	 * ���� : ��ū ���� ����� List�� ���� �� ��ȯ / �ּ�, �Ƿ���� sql�� ���� ��� �߰��ؾ���.
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
	 * ���� : �Ķ���Ͱ� ���� ���Ѵٸ� true ��ȯ, �ƴϸ� false ��ȯ
	 */
	@Override
	public boolean checkReservedWord(int SymbolNo, List<Integer> reserveWordList) {
		return reserveWordList.contains(SymbolNo);
	}
	
	/**
	 * ���� : ���� ���ϴ� �ܾ� �߰�
	 * TODO : PlSqlParser.g4 ���Ͽ��� ���� �����ϴ� ������ �������̴�. Ȯ�� �ؾ���.
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
