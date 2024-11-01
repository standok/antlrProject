package com.svc.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;

import com.biz.TokenInfoBiz;
import com.svc.IParsingSqlSvr;
import com.vo.SqlTokenInfo;

import util.antlr.PlSqlParser;

public class ParsingSqlSvr implements IParsingSqlSvr {

	private List<SqlTokenInfo> queryTokenList = null;
	private int tokenIdx;
	private int depLv;	// 쿼리 깊이

	@Override
	public List<SqlTokenInfo> getQueryTokenList() {
		return queryTokenList;
	}

	/**
	 * 설명 : SQL을 파싱해서 queryTokenList를 만든다.
	 *
	 * @param List<SqlTokenInfo> tokenList, PlSqlParser parser
	 * @return
	 * @throws Exception
	 */
	@Override
	public void parsingSql(List<SqlTokenInfo> tokenList, PlSqlParser parser) throws Exception {

		// 파싱 변수 초기화
		queryTokenList = new ArrayList<>();
		tokenIdx = 0;
		depLv = 0;

		// SQL로 부터 테이블 데이터 분류 시작
		for(; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]");

			if (symbolNo == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.UPDATE) {
//				parsingUpdateSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.DELETE) {
//				parsingDeleteSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.WITH) {
//				parsingWithAsSQL(tokenList, parser);
			} else {

			}
		}
	}

	/**
	 * 설명 : Select Query 파싱
	 *
	 * @param List<SqlTokenInfo> tokenList, PlSqlParser parser
	 * @return
	 * @throws Exception
	 */
	private void parsingSelectSQL(List<SqlTokenInfo> tokenList, PlSqlParser parser) throws Exception {

		LogManager.getLogger("debug").debug("parsingSelectSQL Start ===============================");

		tokenIdx++;

		TokenInfoBiz tokenInfoBiz = new TokenInfoBiz();

		Map<String, String> selectMap = new LinkedHashMap<>();
		Map<String, String> fromMap = new LinkedHashMap<>();
		Map<String, String> whereMap = new LinkedHashMap<>();

		for(; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();
			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]";

			// query depth level
			if(symbolNo == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(symbolNo == PlSqlParser.RIGHT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv--;
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
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (symbolNo == PlSqlParser.FROM) {
				fromMap = parsingFromSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.WHERE) {
				whereMap = parsingWhereSQL(tokenList, parser);
				break;
			} else if (symbolNo == PlSqlParser.REGULAR_ID) {

				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.RIGHT_PAREN) {
					LogManager.getLogger("debug").debug(logStr);
					continue;
				}

				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					selectMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"] *추가*");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					selectMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getSymbolNo()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"] *추가*");
					selectMap.put(tokenName, tokenName);
				}
			}
		}

//		LogManager.getLogger("debug").debug("==selectMap==");
//		Log.logMapToString(selectMap);
//		LogManager.getLogger("debug").debug("==fromMap==");
//		Log.logMapToString(fromMap);
//		LogManager.getLogger("debug").debug("==whereMap==");
//		Log.logMapToString(whereMap);

		// 리턴값 세팅
		for(String key : selectMap.keySet()) {
			String columnName = selectMap.get(key);

			if(columnName.contains(".")) {
				String tableAliasName = columnName.substring(0, columnName.indexOf("."));

				if(fromMap.containsKey(tableAliasName)) {
					String columnMainName = columnName.substring((columnName.indexOf(".")+1));
					String tableMainName = fromMap.get(tableAliasName);

					queryTokenList.add(tokenInfoBiz.createTokenInfo(columnMainName, key, tableMainName));
				}
			} else {
				for(String tableKey : fromMap.keySet()) {
					queryTokenList.add(tokenInfoBiz.createTokenInfo(columnName, key, fromMap.get(tableKey)));
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingSelectSQL END ===============================");
	}

	/**
	 * 설명 : From Query 파싱
	 *
	 * @param List<SqlTokenInfo> tokenList, PlSqlParser parser
	 * @return Map<String, String>
	 * @throws Exception
	 */
	private Map<String, String> parsingFromSQL(List<SqlTokenInfo> tokenList, PlSqlParser parser) throws Exception {

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

			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]";

			// query depth level
			if(symbolNo == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(symbolNo == PlSqlParser.RIGHT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv--;
				continue;
			}

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
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else {
				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					fromMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"] *추가*");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					fromMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getSymbolNo()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx)+"]["+tokenList.get(tokenIdx).getTokenName()+"]["+tokenList.get(tokenIdx).getSymbolNo()+"] *추가*");
					fromMap.put(tokenName, tokenName);
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingFromSQL END ===============================");
		return fromMap;
	}

	/**
	 * 설명 : Where Query 파싱
	 *
	 * @param List<SqlTokenInfo> tokenList, PlSqlParser parser
	 * @return Map<String, String>
	 * @throws Exception
	 */
	public Map<String, String> parsingWhereSQL(List<SqlTokenInfo> tokenList, PlSqlParser parser) throws Exception {

		LogManager.getLogger("debug").debug("parsingWhereSQL Start ===============================");

		tokenIdx++;

		Map<String, String> whereMap = new LinkedHashMap<String, String>();

		int startDepLv = depLv;

		for(; tokenIdx<tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();
			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"]";

			// WHERE 종료 토큰
			if (symbolNo == PlSqlParser.SEMICOLON) {
				LogManager.getLogger("debug").debug(logStr+"<종료>");
				break;
			}

			// query depth level
			if(symbolNo == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(symbolNo == PlSqlParser.RIGHT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv--;
				if(startDepLv == depLv+1) {
					break;	// where 진입 초기 깊이랑 같아지면 종료
				} else continue;
			}

			if(symbolNo == PlSqlParser.UPDATE
					||symbolNo == PlSqlParser.DELETE
					||symbolNo == PlSqlParser.SET
					||symbolNo == PlSqlParser.INTO
//					||symbolNo == PlSqlParser.SEMICOLON
					) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (symbolNo == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (symbolNo == PlSqlParser.COMMA
					||symbolNo == PlSqlParser.PERIOD
					||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.PERIOD) {
				continue;
			} else if (symbolNo == PlSqlParser.REGULAR_ID) {

//				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.RIGHT_PAREN) {
//					continue;
//				}

				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					whereMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"] *추가*");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					whereMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getSymbolNo()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getSymbolNo()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+symbolNo+"] *추가*");
					whereMap.put(tokenName, tokenName);
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingWhereSQL END ===============================");
		return whereMap;
	}

}
