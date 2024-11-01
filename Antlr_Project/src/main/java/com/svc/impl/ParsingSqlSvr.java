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

		LogManager.getLogger("debug").debug("ParsingSqlSvr.parsingSql Start~!!");

		// 파싱 변수 초기화
		queryTokenList = new ArrayList<>();
		tokenIdx = 0;
		depLv = 0;

		// SQL로 부터 테이블 데이터 분류 시작
		for(; tokenIdx < tokenList.size(); tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int tokenType = tokenList.get(tokenIdx).getTokenType();

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]");

			if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.UPDATE) {
//				parsingUpdateSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.DELETE) {
//				parsingDeleteSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.WITH) {
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
			int tokenType = tokenList.get(tokenIdx).getTokenType();

			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]";

			// query depth level
			if(tokenType == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(tokenType == PlSqlParser.RIGHT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv--;
				continue;
			}

			// TODO: SELECT문 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.SET
					//||tokenType == PlSqlParser.INTO
					||tokenType == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (tokenType == PlSqlParser.FROM) {
				fromMap = parsingFromSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.WHERE) {
				whereMap = parsingWhereSQL(tokenList, parser);
				break;
			} else if (tokenType == PlSqlParser.REGULAR_ID) {

				if(tokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.RIGHT_PAREN) {
					LogManager.getLogger("debug").debug(logStr);
					continue;
				}

				if(tokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				if(tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					selectMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					selectMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"] *추가*");
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
		for(String aliasName : selectMap.keySet()) {
			String tokenName = selectMap.get(aliasName);

			if(tokenName.contains(".")) {
				String tableAliasName = tokenName.substring(0, tokenName.indexOf("."));

				if(fromMap.containsKey(tableAliasName)) {
					String columnMainName = tokenName.substring((tokenName.indexOf(".")+1));
					String tableMainName = fromMap.get(tableAliasName);

					queryTokenList.add(tokenInfoBiz.createSqlTokenInfo(columnMainName, aliasName, tableMainName));
				}
			} else {
				for(String tableKey : fromMap.keySet()) {
					queryTokenList.add(tokenInfoBiz.createSqlTokenInfo(tokenName, aliasName, fromMap.get(tableKey)));
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
			int tokenType = tokenList.get(tokenIdx).getTokenType();

			// FROM -> WHERE 종료 토큰
			if (tokenType == PlSqlParser.WHERE) {
				tokenIdx--;
				break;
			}

			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]";

			// query depth level
			if(tokenType == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(tokenType == PlSqlParser.RIGHT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv--;
				continue;
			}

			// TODO: SELECT문 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO
					||tokenType == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.COMMA) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else {
				if(tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					fromMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					fromMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx)+"]["+tokenList.get(tokenIdx).getTokenName()+"]["+tokenList.get(tokenIdx).getTokenType()+"] *추가*");
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
			int tokenType = tokenList.get(tokenIdx).getTokenType();
			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]";

			// WHERE 종료 토큰
			if (tokenType == PlSqlParser.SEMICOLON) {
				LogManager.getLogger("debug").debug(logStr+"<종료>");
				break;
			}

			// query depth level
			if(tokenType == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(tokenType == PlSqlParser.RIGHT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv--;
				if(startDepLv == depLv+1) {
					break;	// where 진입 초기 깊이랑 같아지면 종료
				} else continue;
			}

			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO
//					||tokenType == PlSqlParser.SEMICOLON
					) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL(tokenList, parser);
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				continue;
			} else if (tokenType == PlSqlParser.REGULAR_ID) {

//				if(tokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.RIGHT_PAREN) {
//					continue;
//				}

				if(tokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				if(tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					whereMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||tokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					whereMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+tokenList.get(tokenIdx+1).getTokenName()+"]["+tokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+tokenList.get(tokenIdx+2).getTokenName()+"]["+tokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"] *추가*");
					whereMap.put(tokenName, tokenName);
				}
			}
		}
		LogManager.getLogger("debug").debug("parsingWhereSQL END ===============================");
		return whereMap;
	}

}
