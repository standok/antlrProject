package com.svc.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;

import com.biz.TokenInfoBiz;
import com.svc.IParsingSqlSvr;
import com.util.DataManager;
import com.util.Log;
import com.util.LogManager;
import com.util.StringUtil;
import com.vo.DataMapDefinitionVo;
import com.vo.SqlTokenInfoVo;

import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class ParsingSqlSvr implements IParsingSqlSvr {

	private List<List<SqlTokenInfoVo>> sqlConList = null;
	private List<SqlTokenInfoVo> queryTokenList = null;

	private PlSqlLexer lexer = null;
	private PlSqlParser parser = null;

	private int tokenIdx;
	private int depLv;	// 쿼리 깊이

	@Override
	public List<List<SqlTokenInfoVo>> getSqlConList() {
		return sqlConList;
	}

	@Override
	public List<SqlTokenInfoVo> getQueryTokenList() {
		return queryTokenList;
	}

	@Override
	public void printQueryTokenList() throws Exception {
		Log.logSqlListToString(queryTokenList);
	}

	/**
	 * 설명 : SQL 파일내용을 parsing해서 getSqlConList를 만든다.
	 *
	 * @param StringBuilder sb
	 * @return
	 * @throws Exception
	 */
	@Override
	public void parsingSql(StringBuilder sb) throws Exception {

		Log.printMethod("[START]");

		sqlConList = new ArrayList<>();

		lexer = new PlSqlLexer(CharStreams.fromString(sb.toString()));
		TokenStream tokenStream =new CommonTokenStream(lexer);
		parser = new PlSqlParser(tokenStream);
		parser.setBuildParseTree(true);

		parser.data_manipulation_language_statements();

		TokenInfoBiz tokenInfoBiz = new TokenInfoBiz();

		List<SqlTokenInfoVo> list = new ArrayList<>();

		Vocabulary vocabulary = parser.getVocabulary();

		// TokenStream -> sqlConList 변환
		for(int i = 0; i<tokenStream.size(); i++) {
			int tokenIndex = tokenStream.get(i).getTokenIndex();
			String tokenName = tokenStream.get(i).getText();
			int tokenType = tokenStream.get(i).getType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);
			int tokenLine = tokenStream.get(i).getLine();

			SqlTokenInfoVo sqlTokenInfoVo = new SqlTokenInfoVo();
			sqlTokenInfoVo.setTokenIndex(tokenIndex);
			sqlTokenInfoVo.setTokenName(tokenName);
			sqlTokenInfoVo.setTokenType(tokenType);
			sqlTokenInfoVo.setSymbolicName(symbolicName);
			sqlTokenInfoVo.setTokenLine(tokenLine);
			sqlTokenInfoVo.setAliasName("");
			sqlTokenInfoVo.setTableId("");

			list.add(sqlTokenInfoVo);

			if(tokenType == PlSqlParser.SEMICOLON) {
				Log.printInfomation(lexer, parser, list);

				sqlConList.add(list);

				list = new ArrayList<>();
			}
		}

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : Query를 parsing해서 queryTokenList를 만든다.
	 *
	 * @param List<SqlTokenInfoVo> tokenList
	 * @return
	 * @throws Exception
	 */
	@Override
	public void parsingQuery(List<SqlTokenInfoVo> tokenList) throws Exception {

		Log.printMethod("[START]");

		// parsing query set
		queryTokenList = tokenList;

		// parsing variable Initialization
		tokenIdx = 0;
		depLv = 0;

		// SQL로 부터 테이블 데이터 분류 시작
		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]");

			if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
			} else if (tokenType == PlSqlParser.UPDATE) {
				parsingUpdateSQL();
			} else if (tokenType == PlSqlParser.DELETE) {
				parsingDeleteSQL();
			} else if (tokenType == PlSqlParser.INSERT) {
				parsingInsertSQL();
//			} else if (tokenType == PlSqlParser.WITH) {
//				parsingWithAsSQL();
			} else {

			}
		}

		// 새로운 테이블로 데이터 일괄 변경
		changeQueryTokenData();

		Log.printMethod("[END]");
	}

	private void changeQueryTokenData() throws Exception {

		Log.printMethod("[START]");

		// queryTokenList new 컬럼, 테이블로 변경
		for(SqlTokenInfoVo tmp : queryTokenList) {
			if("Col".equals(tmp.getRolePosition())) {
				if( !StringUtil.isEmtpy(tmp.getTableId()) && !StringUtil.isEmtpy(tmp.getTokenName())) {
					String newColumnId = DataManager.getNewColumnId(tmp.getTableId(), tmp.getTokenName());
					if(!StringUtil.isEmtpy(newColumnId))  tmp.setTokenName(newColumnId);
				}
				else {
					tmp.setTokenName(tmp.getTokenName()+"(없음)");
				}
			} else if("Tab".equals(tmp.getRolePosition())) {
				if( !StringUtil.isEmtpy(tmp.getTokenName())) {
					String newTableId = DataManager.getNewTableId(tmp.getTokenName());
					if(!StringUtil.isEmtpy(newTableId)) tmp.setTokenName(newTableId);
				}
				else {
					tmp.setTokenName(tmp.getTokenName()+"(없음)");
				}
			} else {

			}
		}

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : Select Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingSelectSQL() throws Exception {

		Log.printMethod("[START]");

		tokenIdx++;

		int startTokenIdx = tokenIdx;

		Map<String, String> tableMap = new LinkedHashMap<>();

		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();

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

			// TODO: Select 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET
					//||tokenType == PlSqlParser.INTO
					||tokenType == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if(tokenType == PlSqlParser.MULTI_LINE_COMMENT
					&& queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.SELECT) {
				// 오라클 Hint 찾기
				if(isHint(tokenName)) queryTokenList.get(tokenIdx).setRolePosition("Hint");
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
			} else if (tokenType == PlSqlParser.FROM) {
				tableMap = parsingFromSQL();
			} else if (tokenType == PlSqlParser.WHERE) {
				parsingWhereSQL();
				break;	// TODO : 이걸 지워도 되지 않을까?
			} else if (tokenType == PlSqlParser.REGULAR_ID) {

				if(queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.RIGHT_PAREN
				 ||queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.CHAR_STRING) {
					LogManager.getLogger("debug").debug(logStr);
					continue;
				}

				// alias 확인
				if(queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.PERIOD) {
					tokenName = queryTokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}

				if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+1).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Col");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+2).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Col");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+queryTokenList.get(tokenIdx+2).getTokenName()+"]["+queryTokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"] *추가*");
					queryTokenList.get(tokenIdx).setAliasName(tokenName);
					queryTokenList.get(tokenIdx).setRolePosition("Col");
				}
			}
		}

		// token에 테이블 정보 입력하기
		setTokenTableId(startTokenIdx, tableMap);

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : From Query Parsing
	 *
	 * @param
	 * @return Map<String, String>
	 * @throws Exception
	 */
	private Map<String, String> parsingFromSQL() throws Exception {

		Log.printMethod("[START]");

		tokenIdx++;

		Map<String, String> tableMap = new LinkedHashMap<>();

		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();

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

			// TODO: From 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO
					||tokenType == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.COMMENT
					||tokenType == PlSqlParser.SINGLE_LINE_COMMENT
					||tokenType == PlSqlParser.MULTI_LINE_COMMENT
					||tokenType == PlSqlParser.REMARK_COMMENT
					||tokenType == PlSqlParser.COMMA) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
			} else {
				if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					tableMap.put(queryTokenList.get(tokenIdx+1).getTokenName(), tokenName);
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+1).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					tableMap.put(queryTokenList.get(tokenIdx+2).getTokenName(), tokenName);
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+2).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+queryTokenList.get(tokenIdx+2).getTokenName()+"]["+queryTokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx)+"]["+queryTokenList.get(tokenIdx).getTokenName()+"]["+queryTokenList.get(tokenIdx).getTokenType()+"] *추가*");
					tableMap.put(tokenName, tokenName);
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
				}
			}
		}

		Log.printMethod("[END]");
		return tableMap;
	}

	/**
	 * 설명 : Where Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingWhereSQL() throws Exception {
		Log.printMethod("[START]");

		tokenIdx++;

		int startDepLv = depLv;

		for(; tokenIdx<queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();
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

			// TODO: Where 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO
					) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (tokenType == PlSqlParser.REGULAR_ID) {
//				if(queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.RIGHT_PAREN) {
//					continue;
//				}
				if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+1).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Col");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+2).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Col");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+queryTokenList.get(tokenIdx+2).getTokenName()+"]["+queryTokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					if(queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.PERIOD) {
						tokenName = queryTokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
					}
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"] *추가*");
					queryTokenList.get(tokenIdx).setAliasName(tokenName);
					queryTokenList.get(tokenIdx).setRolePosition("Col");
				}
			}
		}

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : Update Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingUpdateSQL() throws Exception {

		Log.printMethod("[START]");

		tokenIdx++;

		int startTokenIdx = tokenIdx;

		Map<String, String> tableMap = new LinkedHashMap<>();

		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();

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

			// TODO: Update 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.DELETE
				||tokenType == PlSqlParser.INSERT
				||tokenType == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
//			} else if(tokenType == PlSqlParser.MULTI_LINE_COMMENT
//					&& queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.SELECT) {
//				// 오라클 Hint 찾기
//				if(isHint(tokenName)) queryTokenList.get(tokenIdx).setRolePosition("Hint");
			} else if (tokenType == PlSqlParser.SET) {
				parsingSetSQL();
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
//			} else if (tokenType == PlSqlParser.FROM) {
//				tableMap = parsingFromSQL();
			} else if (tokenType == PlSqlParser.WHERE) {
				parsingWhereSQL();
				break;	// TODO : 이걸 지워도 되지 않을까?
			} else if (tokenType == PlSqlParser.REGULAR_ID) {
				if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					tableMap.put(queryTokenList.get(tokenIdx+1).getTokenName(), tokenName);
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+1).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"] *추가*");
					tokenIdx++;
				} else if(queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.AS
						||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.ALIAS) {
					tableMap.put(queryTokenList.get(tokenIdx+2).getTokenName(), tokenName);
					queryTokenList.get(tokenIdx).setAliasName(queryTokenList.get(tokenIdx+2).getTokenName());
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+1)+"]["+queryTokenList.get(tokenIdx+1).getTokenName()+"]["+queryTokenList.get(tokenIdx+1).getTokenType()+"]");
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx+2)+"]["+queryTokenList.get(tokenIdx+2).getTokenName()+"]["+queryTokenList.get(tokenIdx+2).getTokenType()+"] *추가*");
					tokenIdx+=2;
				} else {
					LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+(tokenIdx)+"]["+queryTokenList.get(tokenIdx).getTokenName()+"]["+queryTokenList.get(tokenIdx).getTokenType()+"] *추가*");
					tableMap.put(tokenName, tokenName);
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
				}
			}
		}

		// token에 테이블 정보 입력하기
		setTokenTableId(startTokenIdx, tableMap);

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : Set Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingSetSQL() throws Exception {

		Log.printMethod("[START]");

		tokenIdx++;

		for(; tokenIdx<queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();
			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]";

			// SET -> WHERE 종료 토큰
			if (tokenType == PlSqlParser.WHERE) {
				tokenIdx--;
				break;
			}

			// TODO: Set 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
				||tokenType == PlSqlParser.DELETE
				||tokenType == PlSqlParser.INSERT
				||tokenType == PlSqlParser.SET
				||tokenType == PlSqlParser.INTO
				||tokenType == PlSqlParser.SEMICOLON
				) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (tokenType == PlSqlParser.REGULAR_ID) {
				if(queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.PERIOD) {
					tokenName = queryTokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}
				LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"] *추가*");
				queryTokenList.get(tokenIdx).setAliasName(tokenName);
				queryTokenList.get(tokenIdx).setRolePosition("Col");
			}
		}
		Log.printMethod("[END]");
	}

	/**
	 * 설명 : Delete Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingDeleteSQL() throws Exception {

		Log.printMethod("[START]");

		tokenIdx++;

		int startTokenIdx = tokenIdx;

		Map<String, String> tableMap = new LinkedHashMap<>();

		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();

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

			// TODO: Delete 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
//					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO
					||tokenType == PlSqlParser.SEMICOLON) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (tokenType == PlSqlParser.FROM) {
				tableMap = parsingFromSQL();
			} else if (tokenType == PlSqlParser.WHERE) {
				parsingWhereSQL();
				break;	// TODO : 이걸 지워도 되지 않을까?
			}
		}

		// token에 테이블 정보 입력하기
		setTokenTableId(startTokenIdx, tableMap);

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : Insert Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingInsertSQL() throws Exception {

		Log.printMethod("[START]");

		tokenIdx++;

		int startTokenIdx = tokenIdx;

		Map<String, String> tableMap = new LinkedHashMap<>();

		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();

			String logStr = "Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]";

			// INSERT 종료 토큰
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
				continue;
			}

			// TODO: Insert 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.DELETE
				||tokenType == PlSqlParser.UPDATE
				||tokenType == PlSqlParser.SET
//				||tokenType == PlSqlParser.WITH
				) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.COMMA
					||tokenType == PlSqlParser.PERIOD
					||tokenType == PlSqlParser.INTO
					||tokenType == PlSqlParser.VALUES
					||queryTokenList.get(tokenIdx+1).getTokenType() == PlSqlParser.PERIOD) {
				LogManager.getLogger("debug").debug(logStr);
				continue;
			} else if (tokenType == PlSqlParser.SELECT) {
				parsingSelectSQL();
			} else if (tokenType == PlSqlParser.VALUES) {
				break;
			} else if (tokenType == PlSqlParser.REGULAR_ID) {
				LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"] *추가*");
				if(depLv > 0) {
//					if(queryTokenList.get(tokenIdx-1).getTokenType() == PlSqlParser.PERIOD) {
//						tokenName = queryTokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
//					}
					queryTokenList.get(tokenIdx).setAliasName(tokenName);
					queryTokenList.get(tokenIdx).setRolePosition("Col");
				} else {
					tableMap.put(tokenName, tokenName);
					queryTokenList.get(tokenIdx).setRolePosition("Tab");
				}
			}
		}

		// token에 테이블 정보 입력하기
		setTokenTableId(startTokenIdx, tableMap);

		Log.printMethod("[END]");
	}

	/**
	 * 설명 : With Query Parsing
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	private void parsingWithAsSQL() throws Exception {

	}

	/**
	 * 설명 : queryTokenList를 String으로 변환
	 *
	 * @param boolean bufferYn
	 * @return StringBuilder
	 * @throws Exception, IOException
	 */
	@Override
	public StringBuilder getQueryToString(boolean bufferYn) throws Exception {

		Log.printMethod("[START]");

		int lastLine = 1;		// 마지막줄
		int depCnt = 0;			// 들여쓰기깊이
		String spaceStr = "";	// 공백
		StringBuilder str = new StringBuilder();	// 소스내용

		if(bufferYn) str.append("\"");

		LogManager.getLogger("debug").debug("queryTokenList 확인 =============================");

		for(int i = 0; i < queryTokenList.size(); i++) {

			String tokenName = queryTokenList.get(i).getTokenName();
			int tokenType = queryTokenList.get(i).getTokenType();
			String symbolicName = queryTokenList.get(i).getSymbolicName();
			int tokenLine = queryTokenList.get(i).getTokenLine();

			// TODO : tokenName 줄바꿈 지우기 (수정해야할려나?)
			tokenName = tokenName.replace("\n", "");

			// 들여쓰기 깊이 설정
			if(tokenType == PlSqlParser.LEFT_PAREN) {
				depCnt++;
			} else if(tokenType == PlSqlParser.RIGHT_PAREN) {
				depCnt--;
			}

			if(depCnt == 0) {
				if(tokenType == PlSqlParser.SELECT) {
					spaceStr = "     ";
				}
				if(tokenType == PlSqlParser.FROM) {
//					spaceStr = "  ";
					spaceStr = " ";
				}
				if(tokenType == PlSqlParser.WHERE) {
					spaceStr = " ";
				}
				if(tokenType == PlSqlParser.AND) {
					spaceStr = "   ";
				}
				if(tokenType == PlSqlParser.SET) {
					spaceStr = "   ";
				}
				if(tokenType == PlSqlParser.INTO) {
					spaceStr = "  ";
				}
				if(tokenType == PlSqlParser.SEMICOLON) {
					spaceStr = "";
				}
			} else {
				spaceStr = "";
			}

			LogManager.getLogger("debug").debug("[Token]["+i+"]==>"
					+"["+tokenName+"]"
					+"["+symbolicName+"]"
					+"["+lastLine+"]"
					+"["+tokenLine+"]"
					+"["+countLines(tokenName)+"]"
			 );

			if(tokenLine - lastLine > 0) {

				if(bufferYn) str.append("\"");

				// 줄바꿈
				for(int s = 0; s < tokenLine - lastLine; s++) {
					str.append("\n");
				}

				// 문장앞 공백 추가
				str.append(spaceStr);

				lastLine = tokenLine + countLines(tokenName) -1;
			}

			/*****************************************
			 * 앞/뒤 공백 입력
			 * TODO: 테스트하면서 추가
			 *****************************************/
			// 앞 공백
			if(tokenType == PlSqlParser.SINGLE_LINE_COMMENT) {
				if((i-1) >= 0 && queryTokenList.get(i-1).getTokenType() == PlSqlParser.REGULAR_ID) {
					str.append(" ");
				}
				str.append(tokenName);
			}
			// 뒤 공백
			else if(tokenType == PlSqlParser.SELECT
					|| tokenType == PlSqlParser.UPDATE
					|| tokenType == PlSqlParser.DELETE
					|| tokenType == PlSqlParser.INSERT
					|| tokenType == PlSqlParser.WITH
					|| tokenType == PlSqlParser.AS
					|| tokenType == PlSqlParser.SET
					|| tokenType == PlSqlParser.COMMA
					|| tokenType == PlSqlParser.RIGHT_PAREN
					|| tokenType == PlSqlParser.FROM
					|| tokenType == PlSqlParser.WHERE
					|| tokenType == PlSqlParser.EQUALS_OP
					|| tokenType == PlSqlParser.AND
					|| tokenType == PlSqlParser.NOT
					|| tokenType == PlSqlParser.IN
					|| tokenType == PlSqlParser.INTO
					|| tokenType == PlSqlParser.DECLARE
					|| tokenType == PlSqlParser.BEGIN
					|| tokenType == PlSqlParser.END
//					|| tokenType == PlSqlParser.NEW
				) {
				// 앞뒤 공백
				if(tokenType != PlSqlParser.COMMA
				   && tokenType != PlSqlParser.RIGHT_PAREN
					) {
					if((i-1) >= 0 && queryTokenList.get(i-1).getTokenType() == PlSqlParser.REGULAR_ID) {
						str.append(" ");
					}
				}
				str.append(tokenName + " ");
			} else if(tokenType == PlSqlParser.EOF) {
				// Skip
			} else if(tokenType == PlSqlParser.REGULAR_ID) {
				if(queryTokenList.size() != (i+1)
				   && queryTokenList.get(i+1).getTokenType() == PlSqlParser.REGULAR_ID) {
					str.append(tokenName + " ");
				} else if((i-1) > 0
						   && queryTokenList.get(i-1).getTokenType() == PlSqlParser.CHAR_STRING) {
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

	private void setTokenTableId(int startTokenIdx, Map<String, String> tableMap) throws Exception {

//		LogManager.getLogger("debug").debug("==tableMap==");
//		Log.logMapToString(tableMap);

		LogManager.getLogger("debug").debug("Column에 Table명 세팅["+startTokenIdx+"] ~ ["+tokenIdx+"]");

		// token에 테이블 정보 입력하기
		for(int i = startTokenIdx; i <= tokenIdx; i++) {
			if("Col".equals(queryTokenList.get(i).getRolePosition())) {

				String tokenName = queryTokenList.get(i).getTokenName();
				String aliasName = queryTokenList.get(i).getAliasName();
				String tableId = queryTokenList.get(i).getTableId();

				// 스칼라에서 조회된 컬럼은 제외
				if(!StringUtil.isEmtpy(tableId)) {
					continue;
				}

				int getCnt = 0;

				// 1. Alias로 테이블명 찾기
				if(aliasName.contains(".")) {
					String tableAliasName = aliasName.substring(0, aliasName.indexOf("."));
					if(tableMap.containsKey(tableAliasName)) {
						tableId = tableMap.get(tableAliasName);
						getCnt++;
					}
				} else {
					// 2. Excel 추출 데이터맵에서 테이블명 찾기
					for(String key : tableMap.keySet()) {

						String inqTableId = tableMap.get(key);

						DataMapDefinitionVo dataVo = DataManager.getDataMapDefinitionVo(inqTableId, tokenName);
						if(dataVo != null && !StringUtil.isEmtpy(dataVo.getOldTableId())) {
							tableId = dataVo.getOldTableId();
							getCnt++;
						}
//						else {
//							LogManager.getLogger("debug").debug("("+i+") null=>["+inqTableId+"]["+tableId+"]["+tokenName+"]");
//						}
					}
				}

				// [유효성체크]
				if(getCnt == 0) {
					queryTokenList.get(i).setConvertRule("테이블ID가 발견안됨");
				} else if(getCnt > 1) {
					queryTokenList.get(i).setConvertRule("query중복컬럼존재("+getCnt+")");
				} else {
					queryTokenList.get(i).setConvertRule("성공");
				}

				// 테이블명 세팅
				queryTokenList.get(i).setTableId(tableId);

				LogManager.getLogger("debug").debug("("+i+") parsingSelectSQL Column ["+tokenName+"]["+aliasName+"]["+tableId+"]["+queryTokenList.get(i).getConvertRule()+"]");
			}
		}
	}

	private boolean isHint(String tokenName) {
		if(tokenName.indexOf("/*+") > 0) {
			// 펀드시스템에서 사용안하는 Hint는 주석
			if(tokenName.indexOf("INDEX") > 0
//				||tokenName.indexOf("ALL_ROWS") > 0
//				||tokenName.indexOf("FIRST_ROWS") > 0
//				||tokenName.indexOf("CHOOSE") > 0
				||tokenName.indexOf("RULE") > 0
				||tokenName.indexOf("USE_") > 0
//				||tokenName.indexOf("INDEX_") > 0
//				||tokenName.indexOf("FULL(") > 0
//				||tokenName.indexOf("HASH(") > 0
//				||tokenName.indexOf("CLUSTER(") > 0
//				||tokenName.indexOf("HASH_AJ") > 0
//				||tokenName.indexOf("HASH_SJ") > 0
//				||tokenName.indexOf("ROWID(") > 0
//				||tokenName.indexOf("MERGE_AJ") > 0
//				||tokenName.indexOf("MERGE_SJ") > 0
//				||tokenName.indexOf("AND_EQUAL(") > 0
//				||tokenName.indexOf("USE_CONCAT") > 0
				||tokenName.indexOf("ORDERED") > 0
//				||tokenName.indexOf("STAR") > 0
//				||tokenName.indexOf("DRIVING_SITE(") > 0
				||tokenName.indexOf("PARALLEL(") > 0
//				||tokenName.indexOf("NOPARALLEL(") > 0
				||tokenName.indexOf("CACHE(") > 0
//				||tokenName.indexOf("NOCACHE(") > 0
				||tokenName.indexOf("MERGE(") > 0
//				||tokenName.indexOf("NOMERGE(") > 0
//				||tokenName.indexOf("PUSH_SUBQ") > 0
				) {
				return true;
			}
		}

		return false;
	}

	private int countLines(String str){
	   String[] lines = str.split("\r\n|\r|\n");
	   return  lines.length;
	}

}
