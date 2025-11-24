package com.svc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.svc.IConvertSvc;
import com.svc.ICreateFileSvc;
import com.svc.IParsingJavaSvc;
import com.svc.IParsingSqlSvc;
import com.util.Log;
import com.util.LogManager;
import com.util.StringUtil;
import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;

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

		InputStream in = null;
		InputStreamReader rd = null;
		BufferedReader br = null;

		StringBuilder sb = new StringBuilder();
		String line;

		try {
			in = new FileInputStream(file);
			rd = new InputStreamReader(in, "UTF-8");
			br = new BufferedReader(rd);

			if(fileName.toUpperCase().endsWith(".JAVA")) {
				while((line=br.readLine()) != null) sb.append(line + "\n");
				convertJava(file.getPath(), sb);
			} else if(fileName.toUpperCase().endsWith(".SQL")) {
				while((line=br.readLine()) != null) sb.append(line.toUpperCase() + "\n");
				convertSql(file.getPath(), sb);
			}
		} catch (Exception e) {

		} finally {
			if(br != null) try{br.close();} catch(IOException ioe){}
			if(rd != null) try{rd.close();} catch(IOException ioe){}
			if(in != null) try{in.close();} catch(IOException ioe){}
		}
	}

	/**
	 * 설명 : StringBuilder로 변환된 Java 소스 변환시작
	 *
	 * @param String filePath, StringBuilder sb
	 * @return
	 * @throws Exception, IOException
	 */
	private void convertJava(String filePath, StringBuilder sb) throws Exception, IOException {

		Log.printMethod("[START]");

		/**********************************
		 * TokenStream에서 필요한정보를 List로 변환한다
		 **********************************/
		IParsingJavaSvc parsingJavaSvc = new ParsingJavaSvc();
		parsingJavaSvc.parsingJava(sb, "2");	// 거래구분코드(2:SQL변환)

		// 파싱한 정보를 가져온다.
		List<JavaTokenInfoVo> javaTokenList = parsingJavaSvc.getJavaTokenList();

		StringBuilder sbSql = new StringBuilder();

		int sqlCnt = 0;				// SQL 건수
		int startLine = 0;			// SQL 첫번째 라인
		int lastLine = 0;			// SQL 줄바꿈 라인 & 마지막 라인

		// sqlToken 생성
		for(int i = 0; i < javaTokenList.size(); i++) {
			String tokenName = javaTokenList.get(i).getTokenName();
//			int tokenType = javaTokenList.get(i).getTokenType();
			int tokenLine = javaTokenList.get(i).getTokenLine();
			String convertRule = javaTokenList.get(i).getConvertRule();

			// SQL(전환대상)만 조회
			if(javaTokenList.get(i).isConvert()) {

				// 최초 SQL 라인(줄바꿈용)
				if(lastLine == 0) lastLine = tokenLine;

				// 줄바꿈
				if(tokenLine - lastLine > 0) {
					for(int s = 0; s < tokenLine - lastLine; s++) {
						sbSql.append("\n");
					}
					lastLine = tokenLine;
				}

				tokenName = tokenName.replace("\"","").replace("\\n", "");

				if("동적변수".equals(convertRule)) tokenName = "#"+tokenName+"#";

				// SQL StringBuilder 입력
				sbSql.append(tokenName);

				// SQL 최초 시작 라인 저장
				if(startLine == 0) startLine = i;
			}

			// 마지막줄에서 SQL 파싱
			if(javaTokenList.get(i).isSqlLastLine()) {

				sqlCnt++;

				// 구분자 추가
				sbSql.append("\n").append(";");

				/**********************************
				 * SQL parsing
				 **********************************/
				Log.debug("=======================================================");
				Log.debug("< ["+(sqlCnt)+"]번째 SQL 컬럼, 테이블명 변경 [START] >");
				Log.debug("=======================================================");
				Log.debug(sbSql.toString());
				Log.debug("=======================================================");

				sbSql = convertSql("JAVA", sbSql);

				Log.debug("=======================================================");
				Log.debug("< ["+(sqlCnt)+"]번째 SQL 컬럼, 테이블명 변경 [END]  >");
				Log.debug(sbSql.toString());
				Log.debug("=======================================================");

				/**********************************
				 * 변경후 SQL를 javaTokenList에 삽입
				 **********************************/
				parsingJavaSvc.modSqlInJavaConList(sbSql, startLine, i);

				// 변수 초기화
				startLine = 0;
				lastLine = 0;
				sbSql = new StringBuilder();
			}
		}

		/**********************************
		 * 새로운 Java 파일 생성
		 **********************************/
		ICreateFileSvc createFileSvc = new CreateFileSvc();
		createFileSvc.createJavaFile(filePath, parsingJavaSvc.getJavaToString(false));

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : SQL convert
	 *
	 * @param String filePath, StringBuilder sb
	 * @return
	 * @throws Exception
	 */
	private StringBuilder convertSql(String filePath, StringBuilder sb) throws Exception {
		Log.printMethod("[START]");

		StringBuilder rtnStr = new StringBuilder();

		/**********************************
		 * TokenStream에서 필요한정보를 List로 변환한다
		 **********************************/
		IParsingSqlSvc parsingSqlSvc = new ParsingSqlSvc();
		parsingSqlSvc.parsingSql(sb);

		// 파싱한 정보를 가져온다.
		List<List<SqlTokenInfoVo>> sqlConList = parsingSqlSvc.getSqlConList();
		LogManager.getLogger("debug").debug("sqlConList.size()=>"+sqlConList.size());

		// SQL Parsing 처리
		for(int i = 0; i < sqlConList.size(); i++) {

			if(i > 0) rtnStr.append("\n\n");

			List<SqlTokenInfoVo> queryList = sqlConList.get(i);

			// parsing 및 데이터 변환
			parsingSqlSvc.parsingQuery(queryList);

			// SQL파싱 Svc 호출
			rtnStr.append(parsingSqlSvc.getQueryToString(false));
		}

		// 파일이 들어온경우 새로운 파일로 변환
		if( ! StringUtil.isEmtpy(filePath) && !"JAVA".equals(filePath)) {
			/**********************************
			 * 새로운 Java 파일 생성
			 **********************************/
			ICreateFileSvc createFileSvc = new CreateFileSvc();
			createFileSvc.createSqlFile(filePath, rtnStr);
		}

		Log.printMethod("[END]");
		return rtnStr;
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

}
