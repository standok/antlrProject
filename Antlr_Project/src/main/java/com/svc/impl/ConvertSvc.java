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

import com.biz.TokenInfoBiz;
import com.svc.IConvertSvc;
import com.svc.IParsingJavaSvr;
import com.svc.IParsingSqlSvr;
import com.svc.ISearchSvr;
import com.util.Log;
import com.util.LogManager;
import com.vo.JavaTokenInfo;
import com.vo.SqlTokenInfo;

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;
import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class ConvertSvc implements IConvertSvc {

	/**
	 * 설명 : 파일내용을 String으로 변환
	 *
	 * @param File
	 * @return
	 * @throws Exception, IOException
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
	 * 설명 : String을 File로 변환
	 *
	 * @param String
	 * @return
	 * @throws Exception, IOException
	 */
	@Override
	public void convertStringToFile(String str) throws Exception, IOException {

	}

	/**
	 * 설명 : StringBuilder로 변환된 Java 소스 파싱
	 *
	 * @param StringBuilder
	 * @return
	 * @throws Exception, IOException
	 */
	private void parsingJava(StringBuilder sb) throws Exception, IOException {

		IParsingJavaSvr parsingJavaSvc = new ParsingJavaSvr();

		Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(sb.toString()));
		CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
		Java8Parser parser = new Java8Parser(commonTokenStream);
		ParseTree tree = parser.compilationUnit();
		//Log.debug("result:"+tree.toStringTree(parser));
		//parser.setBuildParseTree(true);

		List<List<JavaTokenInfo>> javaSrcList = getJavaConList(parser, commonTokenStream);

		LogManager.getLogger("debug").debug("javaSrcList.size()======>"+javaSrcList.size());

		for(int i=0; i<javaSrcList.size(); i++) {
			List<JavaTokenInfo> javaTokenList = javaSrcList.get(i);
			Log.printInfomation(lexer, parser, javaTokenList);

			// SQL문만 가져오기
			List<JavaTokenInfo> parsingSqlList = parsingJavaSvc.getSqlInJava(javaTokenList);

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
	 * 설명 : CommonTokenStream에서 Token을 분류
	 *
	 * @param Java8Parser parser, CommonTokenStream commonTokenStream
	 * @return List<List<JavaTokenInfo>>
	 * @throws
	 */
	private List<List<JavaTokenInfo>> getJavaConList(Java8Parser parser, CommonTokenStream commonTokenStream) {

		List<List<JavaTokenInfo>> javaSrcList = new ArrayList<>();
		List<JavaTokenInfo> list = new ArrayList<>();
		JavaTokenInfo tokenInfo = new JavaTokenInfo();

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i = 0; i<commonTokenStream.size(); i++) {
			int symbolNo = commonTokenStream.get(i).getType();
			String tokenName = commonTokenStream.get(i).getText();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);

			// 제외요건
			if(symbolNo == Java8Parser.PACKAGE) continue;
			if(symbolNo == Java8Parser.IMPORT) continue;

			tokenInfo = new JavaTokenInfo();
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
	 *
	 * @param StringBuilder
	 * @return
	 * @throws Exception
	 */
	private void parsingSql(StringBuilder sb) throws Exception {

		IParsingSqlSvr parsingSvc = new ParsingSqlSvr();
		ISearchSvr searchSvr = new SearchSvr();

		PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sb.toString()));
		TokenStream tokenStream =new CommonTokenStream(lexer);
		PlSqlParser parser = new PlSqlParser(tokenStream);
		parser.setBuildParseTree(true);

		parser.data_manipulation_language_statements();

		List<List<SqlTokenInfo>> sqlConList = getSqlConList(parser, tokenStream);

		for(int i=0; i<sqlConList.size(); i++) {
			List<SqlTokenInfo> tokenList = sqlConList.get(i);
			Log.printInfomation(lexer, parser, tokenList);

			// SQL파싱 Svc 호출
			parsingSvc.parsingSql(tokenList, parser);

			List<SqlTokenInfo> queryTokenList = parsingSvc.getQueryTokenList();

			LogManager.getLogger("debug").debug("sqlConList ["+i+"] Output Result -------------------------");
			Log.logListToString(parser, queryTokenList);
			LogManager.getLogger("debug").debug("sqlConList ["+i+"] ---------------------------------------");

			searchSvr.searchTable(sqlConList, queryTokenList);
		}


	}

	/**
	 * 설명 : TokenStream에서 Token을 분류
	 *
	 * @param PlSqlParser parser, TokenStream tokenStream
	 * @return List<List<TokenInfo>>
	 * @throws
	 */
	private List<List<SqlTokenInfo>> getSqlConList(PlSqlParser parser, TokenStream tokenStream) {
		TokenInfoBiz tokenInfoBiz = new TokenInfoBiz();

		List<List<SqlTokenInfo>> sqlConList = new ArrayList<>();
		List<SqlTokenInfo> list = new ArrayList<>();

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i = 0; i<tokenStream.size(); i++) {
			int symbolNo = tokenStream.get(i).getType();
			String tokenName = tokenStream.get(i).getText();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);

			//Log.debug("tokenName=>"+tokenName);

			if(symbolNo == PlSqlParser.COMMENT) continue;
			if(symbolNo == PlSqlParser.SINGLE_LINE_COMMENT) continue;
			if(symbolNo == PlSqlParser.MULTI_LINE_COMMENT) continue;
			if(symbolNo == PlSqlParser.REMARK_COMMENT) continue;

			list.add(tokenInfoBiz.createTokenInfo(tokenName, symbolicId, symbolNo));

			if(symbolNo == PlSqlParser.SEMICOLON) {
				sqlConList.add(list);
				list = new ArrayList<>();
			}
		}

		return sqlConList;
	}
}
