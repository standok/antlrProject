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

import com.biz.TokenInfoBiz;
import com.svc.IConvertSvc;
import com.svc.ICreateFileSvr;
import com.svc.IParsingJavaSvr;
import com.svc.IParsingSqlSvr;
import com.util.Log;
import com.util.LogManager;
import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;

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
			convertJava(sb);
		} else if(fileName.toUpperCase().endsWith(".SQL")) {
			while((line=br.readLine()) != null) sb.append(line.toUpperCase() + "\n");
			convertSql(sb);
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
	 * 설명 : StringBuilder로 변환된 Java 소스 변환시작
	 *
	 * @param StringBuilder
	 * @return
	 * @throws Exception, IOException
	 */
	private void convertJava(StringBuilder sb) throws Exception, IOException {

		/**********************************
		 * TokenStream에서 필요한정보를 List로 변환한다
		 **********************************/
		IParsingJavaSvr parsingJavaSvc = new ParsingJavaSvr();
		parsingJavaSvc.parsingJava(sb);

		List<JavaTokenInfoVo> javaConList = parsingJavaSvc.getJavaTokenList();

		LogManager.getLogger("debug").debug(" << SQL 파싱 시작 >>-------------------------");

		StringBuilder sbSql = new StringBuilder();

		int sqlCnt = 1;

		for(int idx = 0; idx < javaConList.size(); idx++) {
			String tokenName = javaConList.get(idx).getTokenName();
//			int tokenType = javaConList.get(idx).getTokenType();

			// SQL(전환대상)만 조회
			if(javaConList.get(idx).isConvert()) {
				sbSql.append(tokenName.replace("\"","").replace("\\n", "")+"\n");
			}

			// 마지막줄에서 SQL 파싱
			if(javaConList.get(idx).isSqlLastLine()) {

				// 구분자 추가
				sbSql.append(";");

				LogManager.getLogger("debug").debug("< Java소스에서 SQL 출력 ["+(sqlCnt++)+"] >");
				LogManager.getLogger("debug").debug(sbSql.toString());

				// SQL 파싱
				convertSql(sbSql);

				// 변수 초기화
				sbSql = new StringBuilder();

				// SQL 변환작업 시작
				//

			}
		}

		/**********************************
		 * 새로운 Java 파일 생성
		 **********************************/
		ICreateFileSvr createFileSvr = new CreateFileSvr();
		createFileSvr.createJavaFile(javaConList);

	}

	/**
	 * 설명 : SQL convert
	 *
	 * @param StringBuilder
	 * @return
	 * @throws Exception
	 */
	private void convertSql(StringBuilder sb) throws Exception {

		IParsingSqlSvr parsingSqlSvc = new ParsingSqlSvr();

		PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sb.toString()));
		TokenStream tokenStream =new CommonTokenStream(lexer);
		PlSqlParser parser = new PlSqlParser(tokenStream);
		parser.setBuildParseTree(true);

		parser.data_manipulation_language_statements();

//		Vocabulary vocabulary = parser.getVocabulary();

		List<List<SqlTokenInfoVo>> sqlConList = getSqlConList(parser, tokenStream);

		for(int i=0; i<sqlConList.size(); i++) {
			List<SqlTokenInfoVo> tokenList = sqlConList.get(i);
			Log.printInfomation(lexer, parser, tokenList);

			// SQL파싱 Svc 호출
			parsingSqlSvc.parsingSql(tokenList, parser);

			// SQL에서 변경할 queryToken 리스트를 가져온다
			List<SqlTokenInfoVo> queryTokenList = parsingSqlSvc.getQueryTokenList();

			LogManager.getLogger("debug").debug("queryTokenList Output Result ["+i+"] ---------------------------------------");
			Log.logSqlListToString(queryTokenList);
			LogManager.getLogger("debug").debug("queryTokenList Output Result ["+i+"] ---------------------------------------");
		}
	}

	/**
	 * 설명 : TokenStream에서 Token을 분류
	 *
	 * @param PlSqlParser parser, TokenStream tokenStream
	 * @return List<List<TokenInfo>>
	 * @throws
	 */
	private List<List<SqlTokenInfoVo>> getSqlConList(PlSqlParser parser, TokenStream tokenStream) {
		TokenInfoBiz tokenInfoBiz = new TokenInfoBiz();

		List<List<SqlTokenInfoVo>> sqlConList = new ArrayList<>();
		List<SqlTokenInfoVo> list = new ArrayList<>();

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i = 0; i<tokenStream.size(); i++) {
			int tokenType = tokenStream.get(i).getType();
			String tokenName = tokenStream.get(i).getText();
			String symbolicName = vocabulary.getSymbolicName(tokenType);

			// TODO: 주석 제거 -> 차후 이부분을 주석하고 처리해야 깔끔할듯
			if(tokenType == PlSqlParser.COMMENT) continue;
			if(tokenType == PlSqlParser.SINGLE_LINE_COMMENT) continue;
			if(tokenType == PlSqlParser.MULTI_LINE_COMMENT) continue;
			if(tokenType == PlSqlParser.REMARK_COMMENT) continue;

			list.add(tokenInfoBiz.createSqlTokenInfoVo(tokenName, tokenType, symbolicName));

			if(tokenType == PlSqlParser.SEMICOLON) {
				sqlConList.add(list);
				list = new ArrayList<>();
			}
		}

		return sqlConList;
	}
}
