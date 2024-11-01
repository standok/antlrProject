package com.svc.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;

import com.domain.JavaSourceTokenInfo;
import com.domain.TokenInfo;
import com.dto.TokenInfoCreateDto;
import com.svc.IParsingSvr;
import com.util.Log;

import util.antlr.Java8Parser;
import util.antlr.PlSqlParser;

public class ParsingSvr implements IParsingSvr {

	private List<TokenInfo> queryTokenList = null;
	private int tokenIdx;
	private int querylevel;	// 쿼리 깊이

	/**
	 * 설명 : java 소스에서 SQL문을 추출
	 */
	@Override
	public List<JavaSourceTokenInfo> parsingJavaToSQL(List<JavaSourceTokenInfo> tokenList, Java8Parser parser, List<Integer> reservedWordList) throws Exception {

		LogManager.getLogger("debug").debug("ParsingSvr.parsingJavaToSQL Start~!!");

		List<JavaSourceTokenInfo> javaTokenList = new ArrayList<>();
		JavaSourceTokenInfo javaSourceTokenInfo = new JavaSourceTokenInfo();

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
					javaSourceTokenInfo = new JavaSourceTokenInfo();
					javaSourceTokenInfo.setTokenName(";");
					javaSourceTokenInfo.setSymbolNo(Java8Parser.SEMI);
					javaTokenList.add(javaSourceTokenInfo);
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
					javaSourceTokenInfo = new JavaSourceTokenInfo();
					javaSourceTokenInfo.setTokenName(tokenName);
					javaSourceTokenInfo.setSymbolNo(symbolNo);
					javaTokenList.add(javaSourceTokenInfo);
				}
			}
		}

		return javaTokenList;
	}

	/**
	 * 설명 : SQL 파싱
	 */
	@Override
	public void parsingSql(List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) throws Exception {

		// 파싱 변수 초기화
		queryTokenList = new ArrayList<>();
		tokenIdx = 0;
		querylevel = 0;

		// SQL로 부터 테이블 데이터 분류 시작
		for(; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]");

			if (symbolNo == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.UPDATE) {

			} else if (symbolNo == PlSqlParser.DELETE) {

			} else if (symbolNo == PlSqlParser.WITH) {

			} else {

			}
		}
	}

	/**
	 * 설명 : SELECT SQL 파싱 후 queryTokenList 생성
	 */
	private void parsingSelectSQL(List<TokenInfo> tokenList, PlSqlParser parser) throws Exception {

		LogManager.getLogger("debug").debug("parsingSelectSQL Start ===============================");

		tokenIdx++;

		TokenInfoCreateDto tokenInfoCreateDto = new TokenInfoCreateDto();

		Map<String, String> selectMap = new LinkedHashMap<>();
		Map<String, String> fromMap = new LinkedHashMap<>();
		Map<String, String> whereMap = new LinkedHashMap<>();

		for(; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]");

			// SELECT문 종료
//			if(tokenIdx+1 == tokenList.size() && symbolNo == PlSqlParser.SEMICOLON) {
//				break;
//			}

			// query depth level
			if(symbolNo == PlSqlParser.UPDATE) {
				querylevel++;
				continue;
			}

			// TODO: SELECT문 오류 토큰 확인 (테스트하면서 추가)
			if(symbolNo == PlSqlParser.UPDATE
					||symbolNo == PlSqlParser.DELETE
					||symbolNo == PlSqlParser.SET
					//||symbolNo == PlSqlParser.INTO
					||symbolNo == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (symbolNo == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.COMMA
					||symbolNo == PlSqlParser.PERIOD
					||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.PERIOD) {
				continue;
			} else if (symbolNo == PlSqlParser.FROM) {
				fromMap = parsingFromSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.WHERE) {
				whereMap = parsingWhereSQL(tokenList, parser);
			} else {
				// 1.
				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				// 2.
				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					selectMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					selectMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getSymbolNo()+"]");
					tokenIdx+=2;
				} else {
					selectMap.put(tokenName, tokenName);
				}
			}
		}

		LogManager.getLogger("debug").debug("==selectMap==");
		Log.logMapToString(selectMap);
		LogManager.getLogger("debug").debug("==fromMap==");
		Log.logMapToString(fromMap);
		LogManager.getLogger("debug").debug("==whereMap==");
		Log.logMapToString(whereMap);

		// 리턴값 세팅
		for(String key : selectMap.keySet()) {
			String columnName = selectMap.get(key);

			if(columnName.contains(".")) {
				String tableAliasName = columnName.substring(0, columnName.indexOf("."));

				if(fromMap.containsKey(tableAliasName)) {
					String columnMainName = columnName.substring((columnName.indexOf(".")+1));
					String tableMainName = fromMap.get(tableAliasName);

					queryTokenList.add(tokenInfoCreateDto.toEntity(columnMainName, key, tableMainName));
				}
			} else {
				for(String tableKey : fromMap.keySet()) {
					queryTokenList.add(tokenInfoCreateDto.toEntity(columnName, key, fromMap.get(tableKey)));
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingSelectSQL END ===============================");
	}

	/**
	 * 설명 : 토큰 분석 후 from Map 생성
	 */
	private Map<String, String> parsingFromSQL(List<TokenInfo> tokenList, PlSqlParser parser) throws Exception {

		LogManager.getLogger("debug").debug("parsingFromSQL Start ===============================");

		tokenIdx++;

		Map<String, String> fromMap = new LinkedHashMap<>();

		for(; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();

			// FROM -> WHERE 종료 토큰
			if (symbolNo == PlSqlParser.WHERE) {
				tokenIdx--;
				break;
			}

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]");

			// TODO: SELECT문 오류 토큰 확인 (테스트하면서 추가)
			if(symbolNo == PlSqlParser.UPDATE
					||symbolNo == PlSqlParser.DELETE
					||symbolNo == PlSqlParser.SET
					||symbolNo == PlSqlParser.INTO
					||symbolNo == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (symbolNo == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.COMMA) {
				continue;
			} else {
				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					fromMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					fromMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getSymbolNo()+"]");
					tokenIdx+=2;
				} else {
					fromMap.put(tokenName, tokenName);
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingFromSQL END ===============================");
		return fromMap;
	}

	/**
	 * 설명 : 토큰 분석 후 where Map 생성
	 */
	public Map<String, String> parsingWhereSQL(List<TokenInfo> tokenList, PlSqlParser parser) throws Exception {

		LogManager.getLogger("debug").debug("parsingWhereSQL Start ===============================");

		tokenIdx++;

		Map<String, String> whereMap = new LinkedHashMap<String, String>();

		for(; tokenIdx<tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();

			// WHERE 종료 토큰
			if (symbolNo == PlSqlParser.SEMICOLON) {
				break;
			}

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]");

			if(symbolNo == PlSqlParser.UPDATE
					||symbolNo == PlSqlParser.DELETE
					||symbolNo == PlSqlParser.SET
					||symbolNo == PlSqlParser.INTO
//					||symbolNo == PlSqlParser.SEMICOLON
					) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (symbolNo == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.COMMA) {
				continue;
			} else {
				// 1.
				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				// 2.
				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					whereMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					whereMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+querylevel+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getSymbolNo()+"]");
					tokenIdx+=2;
				} else {
					whereMap.put(tokenName, tokenName);
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingWhereSQL END ===============================");
		return whereMap;
	}

	@Override
	public List<TokenInfo> getQueryTokenList() {
		return queryTokenList;
	}

	/**
	 * 설명 : SQL문 처리
	 */
	//	private List<TokenInfo> parsing(List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) {
	//
	//		Log.debug("ParsingSvr.parsing Start~!!");
	//
	//		List<TokenInfo> parsingResultList = new ArrayList<>();
	//		TokenInfoCreateDto tokenInfoCreateDto = new TokenInfoCreateDto();
	//
	//		Map<String, String> columnMap = new LinkedHashMap<>();
	//		Map<String, String> tableMap = new LinkedHashMap<>();
	//
	//		tokenIdx = 1;
	//		int tokenSize = tokenList.size();
	//
	//		//Column 정보 저장
	//		columnMap = addColumnMap(tokenSize, tokenList, parser, reservedWordList);
	//		Log.debug("==columnMap==");
	//		Log.logMapToString(columnMap);
	//
	//		//table 정보 저장
	//		if(tokenList.get(tokenIdx).getSymbolNo() == PlSqlParser.FROM) {
	//			tokenIdx++;
	//			tableMap = addTableMap(tokenSize, tokenList, parser, reservedWordList);
	//		}
	//		Log.debug("==tableMap==");
	//		Log.logMapToString(tableMap);
	//
	//		//where문 정보 저장
	//		String whereStr = addWhereStr(tokenSize, tokenList);
	//		Log.debug(whereStr);
	//
	//		for(String key : columnMap.keySet()) {
	//			String columnName = columnMap.get(key);
	//
	//			if(columnName.contains(".")) {
	//				String tableAliasName = columnName.substring(0, columnName.indexOf("."));
	//
	//				if(tableMap.containsKey(tableAliasName)) {
	//					String columnMainName = columnName.substring((columnName.indexOf(".")+1));
	//					String tableMainName = tableMap.get(tableAliasName);
	//
	//					parsingResultList.add(tokenInfoCreateDto.toEntity(columnMainName, key, tableMainName));
	//				}
	//			} else {
	//				for(String tableKey : tableMap.keySet()) {
	//					parsingResultList.add(tokenInfoCreateDto.toEntity(columnName, key, tableMap.get(tableKey)));
	//				}
	//			}
	//		}
	//		return parsingResultList;
	//	}
	//
	//	/**
	//	 * 설명 : 토큰 분석 후 Column Map 생성
	//	 */
	//	public Map<String, String> addColumnMap(int tokenSize, List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) {
	//		Map<String, String> aliasMap = new LinkedHashMap<>();
	//
	//		for(; tokenIdx<tokenSize; tokenIdx++) {
	//			String tokenName = tokenList.get(tokenIdx).getTokenName();
	//			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();
	//
	//			Log.debug("addColumnMap["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]");
	//
	//			if(tokenInfoSvr.checkReservedWord(symbolNo, reservedWordList)) {
	//				break;
	//			} else if (symbolNo == PlSqlParser.COMMA
	//					||symbolNo == PlSqlParser.PERIOD
	//					||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.PERIOD) {
	//				continue;
	//			} else {
	//				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.PERIOD) {
	//					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
	//				}
	//
	//				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
	//					aliasMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
	//					tokenIdx++;
	//				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
	//						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
	//					aliasMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
	//					tokenIdx+=2;
	//				} else {
	//					aliasMap.put(tokenName, tokenName);
	//				}
	//			}
	//		}
	//
	//		return aliasMap;
	//	}
	//
	//	/**
	//	 * 설명 : 토큰 분석 후 Table Map 생성
	//	 */
	//	public Map<String, String> addTableMap(int tokenSize, List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) {
	//		Map<String, String> aliasMap = new LinkedHashMap<String, String>();
	//
	//		for(; tokenIdx<tokenSize; tokenIdx++) {
	//			String tokenName = tokenList.get(tokenIdx).getTokenName();
	//			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();
	//
	//			if(tokenInfoSvr.checkReservedWord(symbolNo, reservedWordList)) {
	//				break;
	//			} else if (symbolNo == PlSqlParser.COMMA) {
	//				continue;
	//			} else {
	//				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
	//					aliasMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
	//					tokenIdx++;
	//				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
	//						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
	//					aliasMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
	//					tokenIdx+=2;
	//				} else {
	//					aliasMap.put(tokenName, tokenName);
	//				}
	//			}
	//		}
	//
	//		return aliasMap;
	//	}

}
