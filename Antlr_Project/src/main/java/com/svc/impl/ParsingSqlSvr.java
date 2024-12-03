package com.svc.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;

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
	private StringBuilder queryTokenListStr = null;
	private String queryLastLineStr = null;

	private PlSqlLexer lexer = null;
	private PlSqlParser parser = null;

	private int tokenIdx;
	private int depLv;	// 쿼리 깊이

	private final Set<Integer> DATA_TYPE = new HashSet<Integer>();
	private final Set<Integer> ORA_FUNC = new HashSet<Integer>();
	private final Set<Integer> BEGIN_CLAUSES = new HashSet<Integer>();
	private final Set<Integer> END_CLAUSES = new HashSet<Integer>();
	private final Set<Integer> LOGICAL = new HashSet<Integer>();
	private final Set<Integer> QUANTIFIERS = new HashSet<Integer>();
	private final Set<Integer> DML = new HashSet<Integer>();
	private final Set<Integer> MISC = new HashSet<Integer>();

	/**
	 * 생성자에서 하는일
	 * - 토큰 타입 정의 ( 펀드에 필요한것만 넣을것 )
	 */
	public ParsingSqlSvr() {

		/*******************************
		 * 데이터 타입
		 *******************************/
		DATA_TYPE.add(PlSqlParser.CHAR);
		DATA_TYPE.add(PlSqlParser.NUMBER);
		DATA_TYPE.add(PlSqlParser.VARCHAR);
		DATA_TYPE.add(PlSqlParser.VARCHAR2);
//		DATA_TYPE.add(PlSqlParser.NVARCHAR2);
//		DATA_TYPE.add(PlSqlParser.NCHAR);
//		DATA_TYPE.add(PlSqlParser.NVARCHAR);
		DATA_TYPE.add(PlSqlParser.LONG);
		DATA_TYPE.add(PlSqlParser.CLOB);
//		DATA_TYPE.add(PlSqlParser.NCLOB);
//		DATA_TYPE.add(PlSqlParser.BLOB);
//		DATA_TYPE.add(PlSqlParser.BFILE);
		DATA_TYPE.add(PlSqlParser.FLOAT);
//		DATA_TYPE.add(PlSqlParser.BINARY_FLOAT);
//		DATA_TYPE.add(PlSqlParser.BINARY_DOUBLE);
		DATA_TYPE.add(PlSqlParser.DATE);
		DATA_TYPE.add(PlSqlParser.TIMESTAMP);

		/*******************************
		 * 오라클 함수
		 *******************************/
		ORA_FUNC.add(PlSqlParser.TO_CHAR);
		ORA_FUNC.add(PlSqlParser.TO_NUMBER);
		ORA_FUNC.add(PlSqlParser.TO_DATE);
		ORA_FUNC.add(PlSqlParser.TO_CLOB);

		ORA_FUNC.add(PlSqlParser.MAX);
		ORA_FUNC.add(PlSqlParser.MIN);
		ORA_FUNC.add(PlSqlParser.AVG);
		ORA_FUNC.add(PlSqlParser.SUM);
		ORA_FUNC.add(PlSqlParser.MOD);
		ORA_FUNC.add(PlSqlParser.COUNT);

		ORA_FUNC.add(PlSqlParser.LOWER);
		ORA_FUNC.add(PlSqlParser.UPPER);
//		ORA_FUNC.add(PlSqlParser.INITCAP);
		ORA_FUNC.add(PlSqlParser.CONCAT);
		ORA_FUNC.add(PlSqlParser.SUBSTR);
		ORA_FUNC.add(PlSqlParser.LENGTH);
		ORA_FUNC.add(PlSqlParser.INSTR);
		ORA_FUNC.add(PlSqlParser.LPAD);
		ORA_FUNC.add(PlSqlParser.RPAD);
		ORA_FUNC.add(PlSqlParser.TRIM);
		ORA_FUNC.add(PlSqlParser.LTRIM);
		ORA_FUNC.add(PlSqlParser.RTRIM);
		ORA_FUNC.add(PlSqlParser.REPLACE);
		ORA_FUNC.add(PlSqlParser.NVL);
//		ORA_FUNC.add(PlSqlParser.NVL2);
//		ORA_FUNC.add(PlSqlParser.NULLIF);
//		ORA_FUNC.add(PlSqlParser.COALESCE);

		ORA_FUNC.add(PlSqlParser.ABS);
		ORA_FUNC.add(PlSqlParser.MOD);
		ORA_FUNC.add(PlSqlParser.CEIL);
		ORA_FUNC.add(PlSqlParser.FLOOR);
		ORA_FUNC.add(PlSqlParser.ROUND);
		ORA_FUNC.add(PlSqlParser.TRUNC);

		ORA_FUNC.add(PlSqlParser.SYSDATE);
//		ORA_FUNC.add(PlSqlParser.EXTRACT);
		ORA_FUNC.add(PlSqlParser.MONTHS_BETWEEN);
		ORA_FUNC.add(PlSqlParser.ADD_MONTHS);
		ORA_FUNC.add(PlSqlParser.NEXT_DAY);
		ORA_FUNC.add(PlSqlParser.LAST_DAY);

		BEGIN_CLAUSES.add(PlSqlParser.LEFT);
		BEGIN_CLAUSES.add(PlSqlParser.RIGHT);
		BEGIN_CLAUSES.add(PlSqlParser.INNER);
		BEGIN_CLAUSES.add(PlSqlParser.OUTER);
		BEGIN_CLAUSES.add(PlSqlParser.GROUP);
		BEGIN_CLAUSES.add(PlSqlParser.ORDER);

		END_CLAUSES.add(PlSqlParser.WHERE);
		END_CLAUSES.add(PlSqlParser.SET);
		END_CLAUSES.add(PlSqlParser.HAVING);
		END_CLAUSES.add(PlSqlParser.JOIN);
		END_CLAUSES.add(PlSqlParser.FROM);
		END_CLAUSES.add(PlSqlParser.BY);
		END_CLAUSES.add(PlSqlParser.JOIN);
		END_CLAUSES.add(PlSqlParser.INTO);
		END_CLAUSES.add(PlSqlParser.UNION);

		LOGICAL.add(PlSqlParser.AND);
		LOGICAL.add(PlSqlParser.OR);
		LOGICAL.add(PlSqlParser.WHEN);
		LOGICAL.add(PlSqlParser.ELSE);
		LOGICAL.add(PlSqlParser.END);

		QUANTIFIERS.add(PlSqlParser.IN);
		QUANTIFIERS.add(PlSqlParser.ALL);
		QUANTIFIERS.add(PlSqlParser.EXISTS);
		QUANTIFIERS.add(PlSqlParser.SOME);
		QUANTIFIERS.add(PlSqlParser.ANY);

		DML.add(PlSqlParser.INSERT);
		DML.add(PlSqlParser.UPDATE);
		DML.add(PlSqlParser.DELETE);
		DML.add(PlSqlParser.MERGE);

		MISC.add(PlSqlParser.SELECT);
		MISC.add(PlSqlParser.ON);
	}

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

		List<SqlTokenInfoVo> list = new ArrayList<>();

		Vocabulary vocabulary = parser.getVocabulary();

		boolean convert = false;
		int tokenIndent = 0;

		// TokenStream -> sqlConList 변환
		for(int i = 0; i<tokenStream.size(); i++) {
			int tokenIndex = tokenStream.get(i).getTokenIndex();
			String tokenName = tokenStream.get(i).getText();
			int tokenType = tokenStream.get(i).getType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);
			int tokenLine = tokenStream.get(i).getLine();
//			int startIndex = tokenStream.get(i).getCharPositionInLine();

//			Log.debug(tokenName+"->"+startIndex);

			if(DML.contains(tokenType) || (!convert && tokenType == PlSqlParser.SELECT)) {
				if(list.size() > 0) {
					sqlConList.add(list);
					list = new ArrayList<>();
				}
				convert = true;
			}

			SqlTokenInfoVo sqlTokenInfoVo = new SqlTokenInfoVo();
			sqlTokenInfoVo.setTokenIndex(tokenIndex);
			sqlTokenInfoVo.setTokenName(tokenName);
			sqlTokenInfoVo.setTokenType(tokenType);
			sqlTokenInfoVo.setSymbolicName(symbolicName);
			sqlTokenInfoVo.setTokenLine(tokenLine);
			sqlTokenInfoVo.setAliasName("");
			sqlTokenInfoVo.setTableId("");
			sqlTokenInfoVo.setConvert(convert);

			if(tokenType == PlSqlParser.END) {
				tokenIndent -= 4;
			}
			sqlTokenInfoVo.setTokenIndent(tokenIndent);

			list.add(sqlTokenInfoVo);

			if(tokenType == PlSqlParser.BEGIN) {
				tokenIndent += 4;
			}

			if(convert && tokenType == PlSqlParser.SEMICOLON) {
				Log.printInfomation(lexer, parser, list);

				sqlConList.add(list);
				list = new ArrayList<>();
				convert = false;
			}
		}

		if(list.size() > 0) sqlConList.add(list);

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
		queryTokenListStr = new StringBuilder();
		queryLastLineStr = "";

		// 전환대상 확인
		if(tokenList != null && !tokenList.get(0).isConvert()) {
			return;
		}

		// parsing variable Initialization
		tokenIdx = 0;
		depLv = 0;

		// query에 수정할 데이터가 있는지 확인
		boolean queryModifyYn = false;

		// SQL로 부터 테이블 데이터 분류 시작
		for(; tokenIdx < queryTokenList.size(); tokenIdx++) {
			String tokenName = queryTokenList.get(tokenIdx).getTokenName();
			int tokenType = queryTokenList.get(tokenIdx).getTokenType();
			String symbolicName = queryTokenList.get(tokenIdx).getSymbolicName();

			LogManager.getLogger("debug").debug("Token 데이터 분류 정보["+depLv+"]["+tokenIdx+"]["+tokenName+"]["+tokenType+"]");

			if (tokenType == PlSqlParser.SELECT) {
				queryModifyYn = true;
				parsingSelectSQL();
			} else if (tokenType == PlSqlParser.UPDATE) {
				queryModifyYn = true;
				parsingUpdateSQL();
			} else if (tokenType == PlSqlParser.DELETE) {
				queryModifyYn = true;
				parsingDeleteSQL();
			} else if (tokenType == PlSqlParser.INSERT) {
				queryModifyYn = true;
				parsingInsertSQL();
//			} else if (tokenType == PlSqlParser.WITH) {
//				parsingWithAsSQL();
			} else if (tokenType == PlSqlParser.SEMICOLON) {

			} else {
				Log.debug("미정의데이터==>["+symbolicName+"]");
			}
		}

		// 새로운 테이블로 데이터 일괄 변경
		if(queryModifyYn) modifyQueryTokenData();

		Log.printMethod("[END]");
	}

	private void modifyQueryTokenData() throws Exception {

		Log.printMethod("[START]");

		// queryTokenList new 컬럼, 테이블로 변경
		for(SqlTokenInfoVo tmp : queryTokenList) {
			if("Col".equals(tmp.getRolePosition())) {
				LogManager.getLogger("debug").debug("[Col]=>["+tmp.getTableId()+","+tmp.getTokenName()+"]");;
				if( !StringUtil.isEmtpy(tmp.getTableId()) && !StringUtil.isEmtpy(tmp.getTokenName())) {
					String newColumnId = DataManager.getNewColumnId(tmp.getTableId(), tmp.getTokenName());
					if(!StringUtil.isEmtpy(newColumnId))  tmp.setTokenName(newColumnId);
				}
//				else {
//					tmp.setTokenName(tmp.getTokenName()+"(없음)");
//				}
			} else if("Tab".equals(tmp.getRolePosition())) {
				LogManager.getLogger("debug").debug("[Tab]=>["+tmp.getTableId()+","+tmp.getTokenName()+"]");
				if( !StringUtil.isEmtpy(tmp.getTokenName())) {
					String newTableId = DataManager.getNewTableId(tmp.getTokenName());
					if(!StringUtil.isEmtpy(newTableId)) tmp.setTokenName(newTableId);
				}
//				else {
//					tmp.setTokenName(tmp.getTokenName()+"(없음)");
//				}
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
		int startDepLv = depLv;

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
				if(startDepLv == depLv) {
					tokenIdx--;
					LogManager.getLogger("debug").debug(logStr+"< Select 종료 >");
					break;	// where 진입 초기 깊이랑 같아지면 종료
				} else {
					LogManager.getLogger("debug").debug(logStr);
					depLv--;
					continue;
				}
			}

			// TODO: Select 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.SEMICOLON) {	// Select 종료 토큰
				tokenIdx--;
				LogManager.getLogger("debug").debug(logStr+"< Select 종료 >");
				break;
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
//				break;	// TODO : 이걸 지워도 되지 않을까?
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

		int startDepLv = depLv;

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
				if(startDepLv == depLv) {
					tokenIdx--;
					LogManager.getLogger("debug").debug(logStr+"< From 종료 >");
					break;	// where 진입 초기 깊이랑 같아지면 종료
				} else {
					LogManager.getLogger("debug").debug(logStr);
					depLv--;
					continue;
				}
			}

			// TODO: From 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.GROUP
				   ||tokenType == PlSqlParser.ORDER
				   ||tokenType == PlSqlParser.WHERE
				   ||tokenType == PlSqlParser.SEMICOLON) {	// FROM 종료 토큰
				tokenIdx--;
				LogManager.getLogger("debug").debug(logStr+"< From 종료 >");
				break;
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
				// 임시테이블 제외
				if("DUAL".equals(tokenName)) {
					LogManager.getLogger("debug").debug(logStr);
					continue;
				}

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

			// query depth level
			if(tokenType == PlSqlParser.LEFT_PAREN) {
				LogManager.getLogger("debug").debug(logStr);
				depLv++;
				continue;
			} else if(tokenType == PlSqlParser.RIGHT_PAREN) {
				if(startDepLv == depLv) {
					tokenIdx--;
					LogManager.getLogger("debug").debug(logStr+"< Where 종료 >");
					break;	// where 진입 초기 깊이랑 같아지면 종료
				} else {
					LogManager.getLogger("debug").debug(logStr);
					depLv--;
					continue;
				}
			}

			// TODO: Where 오류 토큰 확인 (테스트하면서 추가)
			if(tokenType == PlSqlParser.UPDATE
					||tokenType == PlSqlParser.DELETE
					||tokenType == PlSqlParser.INSERT
					||tokenType == PlSqlParser.SET
					||tokenType == PlSqlParser.INTO) {
				throw new Exception("무효한 토큰값이 들어옴");
			} else if (tokenType == PlSqlParser.GROUP
				   ||tokenType == PlSqlParser.ORDER
				   ||tokenType == PlSqlParser.SEMICOLON) { // WHERE 종료 토큰
				tokenIdx--;
				LogManager.getLogger("debug").debug(logStr+"< Where 종료 >");
				break;
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
				||tokenType == PlSqlParser.INSERT) {
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
				||tokenType == PlSqlParser.INTO) {
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
					||tokenType == PlSqlParser.INTO) {
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


	private void setTokenTableId(int startTokenIdx, Map<String, String> tableMap) throws Exception {

//		LogManager.getLogger("debug").debug("==tableMap==");
//		Log.logMapToString(tableMap);

//		LogManager.getLogger("debug").debug("Column에 Table명 세팅["+startTokenIdx+"] ~ ["+tokenIdx+"]");

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

		int secondBfTokenType = 0;	// 전전 토큰
		int firstBfTokenType = 0;	// 전 토큰타입
		String firstBfTokenName = "";	// 전 토큰명

		int lastLine = 0;		// 마지막줄

		boolean beginLine = true;

		StringBuilder str = new StringBuilder();	// 소스내용

		if(bufferYn) str.append("\"");

		LogManager.getLogger("debug").debug("queryTokenList 확인 =============================");

		for(int i = 0; i < queryTokenList.size(); i++) {

			String tokenName = queryTokenList.get(i).getTokenName();
			int tokenType = queryTokenList.get(i).getTokenType();
			String symbolicName = queryTokenList.get(i).getSymbolicName();
			int tokenLine = queryTokenList.get(i).getTokenLine();

			// 최종라인 세팅
			if( i == 0 ) lastLine = tokenLine - 1;

			LogManager.getLogger("debug").debug("[Token]["+i+"]==>"
					+"["+tokenName+"]"
					+"["+symbolicName+"]"
					+"["+lastLine+"]"
					+"["+tokenLine+"]"
					+"["+countLines(tokenName)+"]"
			 );

			// 줄바꿈과 문장 앞 공백 추가
			if(tokenLine - lastLine > 0) {

				if(bufferYn) str.append("\"");

				// 줄바꿈
				for(int s = 0; s < tokenLine - lastLine; s++) {
					str.append("\n");
				}

				lastLine = tokenLine + countLines(tokenName) -1;
				beginLine = true;
			}

			/*****************************************
			 * 앞/뒤 공백 입력
			 * TODO: 테스트하면서 추가
			 *****************************************/
			// 앞 공백
			if(tokenType == PlSqlParser.SINGLE_LINE_COMMENT) {
//				if((i-1) >= 0 && queryTokenList.get(i-1).getTokenType() == PlSqlParser.REGULAR_ID) {
//				if(!beginLine) {
//					str.append(" ");
//				}

				// 싱글 라인 코멘트일경우 줄바꿈문자 제거
				str.append(tokenName.replace("\n", ""));

//			} else if(tokenType == PlSqlParser.MULTI_LINE_COMMENT) {

			} else if(tokenType == PlSqlParser.EOF) {
				// Skip
			} else if(tokenType == PlSqlParser.REGULAR_ID) {
				if(firstBfTokenType == PlSqlParser.REGULAR_ID
					||firstBfTokenType == PlSqlParser.RIGHT_PAREN
					){
					str.append(" ");
				}
				str.append(tokenName);
			} else {
				// 앞 공백
				if(tokenType == PlSqlParser.COMMA
					|| tokenType == PlSqlParser.PERIOD
					|| tokenType == PlSqlParser.RIGHT_PAREN
					|| tokenType == PlSqlParser.CHAR_STRING) {
					// 추가안함
				} else {
					if(firstBfTokenType == PlSqlParser.REGULAR_ID) {
						str.append(" ");
					}
				}

				// 입력 + 뒷 공백
				if(tokenType == PlSqlParser.LEFT_PAREN
					||tokenType == PlSqlParser.RIGHT_PAREN
					||tokenType == PlSqlParser.UNSIGNED_INTEGER
					||tokenType == PlSqlParser.PERIOD
					||tokenType == PlSqlParser.SEMICOLON) {
					str.append(tokenName);
				} else if(DATA_TYPE.contains(tokenType)
						||ORA_FUNC.contains(tokenType)) {
					str.append(tokenName);
					if(tokenType == PlSqlParser.NUMBER
					  ||tokenType == PlSqlParser.REPLACE) str.append(" ");
				} else {
					str.append(tokenName + " ");
				}
			}

			// 다음작업을 위한 변수 세팅
			firstBfTokenName = tokenName;
			firstBfTokenType = tokenType;
			secondBfTokenType = firstBfTokenType;
			beginLine = false;
		}
//		LogManager.getLogger("debug").debug("<결과값확인> =============================");
//		LogManager.getLogger("debug").debug("\n"+str.toString());

		Log.printMethod("[END]");
		return str;
	}

	/**
	 * 설명 : queryTokenList를 String으로 변환
	 *
	 * @param boolean bufferYn
	 * @return StringBuilder
	 * @throws Exception, IOException
	 */
	@Override
	public StringBuilder getQueryToStringNew(boolean bufferYn) throws Exception {

		Log.printMethod("[START]");

//		boolean afterBeginBeforeEnd = false;
//		boolean afterOn = false;
		boolean afterBetween = false;

//		int inFunction = 0;

		int indentCnt = 0;
		int paragraphCnt = 0;				// 구문
		boolean afterByOrSetOrFromOrSelect = false;
		LinkedList<Integer> indentCntList = new LinkedList<Integer>();
		LinkedList<Integer> paragraphCntList = new LinkedList<Integer>();
		LinkedList<Boolean> afterByOrFromOrSelectList = new LinkedList<Boolean>();

		int parensSinceSelect = 0;		// select문 확인

//		int secondBfTokenType = 0;		// 전전 토큰
		int firstBfTokenType = 0;		// 전 토큰타입
		String firstBfTokenName = "";	// 전 토큰명

		int lastLine = 1;			// 마지막 줄

		// 초기화
		queryTokenListStr = new StringBuilder();
		queryLastLineStr = "";

		if(bufferYn) out("\"");

		LogManager.getLogger("debug").debug("queryTokenList 확인 =============================");

		for(int i = 0; i < queryTokenList.size(); i++) {

			String tokenName = queryTokenList.get(i).getTokenName();
			int tokenType = queryTokenList.get(i).getTokenType();
			String symbolicName = queryTokenList.get(i).getSymbolicName();
			int tokenLine = queryTokenList.get(i).getTokenLine();
			int tokenIndent = queryTokenList.get(i).getTokenIndent();

			// 첫 토큰일경우, indent 입력과 최종라인 세팅
			if( i == 0 ) {
				// 최종라인 세팅
				lastLine = tokenLine;
				// indent 입력
				for(int s = 0; s < tokenIndent; s++) {
					out(" ");
				}
			}

			LogManager.getLogger("debug").debug("[Token]["+i+"]==>"
					+"["+tokenName+"]"
					+"["+symbolicName+"]"
					+"["+lastLine+"]"
					+"["+tokenLine+"]"
					+"["+countLines(tokenName)+"]"
			 );


			// TODO : 멀티코멘트 고쳐야함










			/**************************************
			 * 줄바꿈과 문장 앞 공백 추가
			 **************************************/
			if(tokenLine - lastLine > 0) {

				if(bufferYn) out("\"");

				// 줄바꿈
				for(int s = 0; s < tokenLine - lastLine; s++) {
					out("\n");
				}

				// 문장앞 공백 추가
				int totalIndent = indentCnt;

				// 줄바꿈일때만 키워드 공백 추가
//				if(tokenType == PlSqlParser.COMMA || tokenType == PlSqlParser.REGULAR_ID) {
				if(tokenType == PlSqlParser.COMMA) {
					totalIndent += (paragraphCnt-1 > 0? paragraphCnt-1 : 0);
				} else if(tokenType == PlSqlParser.REGULAR_ID) {
					totalIndent += paragraphCnt + 1;
				} else if(tokenType == PlSqlParser.BEGIN || tokenType == PlSqlParser.END) {

				} else if(tokenType == PlSqlParser.PERIOD
						|| tokenType == PlSqlParser.RIGHT_PAREN
						|| tokenType == PlSqlParser.SEMICOLON) {

				} else {
					totalIndent += (paragraphCnt - countStr(tokenName) > 0? paragraphCnt - countStr(tokenName) : 0);
				}

//				/out(indentCnt+"/"+paragraphCnt+"/"+totalIndent);

				for(int s = 0; s < totalIndent; s++) {
					out(" ");
				}

				lastLine = tokenLine + countLines(tokenName) -1;
			}

			/**************************************
			 * Query 분류 작업
			 **************************************/
//			if ( afterByOrSetOrFromOrSelect && tokenType == PlSqlParser.COMMA) {
//				out(tokenName, false);
//			}

//			else if ( afterOn && tokenType == PlSqlParser.COMMA ) {
//				out(tokenName).append(" ");
//				indentCnt--;
//				newline();
//				afterOn = false;
//				afterByOrSetOrFromOrSelect = true;
//			}
//
//			else
			if(tokenType == PlSqlParser.LEFT_PAREN) {

				out(tokenName);

//				if ( isFunctionName( firstBfTokenName )
//					|| inFunction > 0
//					) {
//					inFunction++;
//				} else {
					// 기존 구문 저장
					indentCntList.addLast( indentCnt );
					paragraphCntList.addLast( paragraphCnt );
					afterByOrFromOrSelectList.addLast( afterByOrSetOrFromOrSelect );
					parensSinceSelect++;
//				}
			}

			else if(tokenType == PlSqlParser.RIGHT_PAREN) {

				if ( parensSinceSelect > 0 ) {
					parensSinceSelect--;
					indentCnt = indentCntList.removeLast();
					paragraphCnt = paragraphCntList.removeLast();
					afterByOrSetOrFromOrSelect = afterByOrFromOrSelectList.removeLast();
				} else {
					Log.error("Left/Right Paren 개수 불일치==>["+queryTokenListStr.toString()+"]");
					throw new Exception("오류가 발생했습니다.[Left/Right Paren 개수 불일치]");
				}

//				if ( inFunction > 0 ) {
//					inFunction--;
//				}
//				else {
//					if ( !afterByOrSetOrFromOrSelect ) {
//						indentCnt--;
//						newline();
//					}
//				}
				out(tokenName);
			}

			else if ( BEGIN_CLAUSES.contains( tokenType ) ) {
//				if ( !afterBeginBeforeEnd ) {
//					if ( afterOn ) {
//						indentCnt--;
//						afterOn = false;
//					}
//					indentCnt--;
//					newline();
//				}
				out(tokenName, false);

//				afterBeginBeforeEnd = true;
			}

			else if ( END_CLAUSES.contains( tokenType ) ) {
//				if ( !afterBeginBeforeEnd ) {
//					indentCnt--;
//					if ( afterOn ) {
//						indentCnt--;
//						afterOn = false;
//					}
//					newline();
//				}
				out(tokenName, false);;
//				if (tokenType != PlSqlParser.UNION) {
//					indentCnt++;
//				}
//				newline();
//				afterBeginBeforeEnd = false;
				afterByOrSetOrFromOrSelect = tokenType == PlSqlParser.BY
									|| tokenType == PlSqlParser.SET
									|| tokenType == PlSqlParser.FROM;
			}

			else if (tokenType == PlSqlParser.SELECT) {

				indentCnt = countStr(queryLastLineStr);
				paragraphCnt = countStr(tokenName);
				afterByOrSetOrFromOrSelect = true;

				out(tokenName, false);
			}

			else if ( DML.contains( tokenType ) ) {
				indentCnt = countStr(queryLastLineStr);
				paragraphCnt = countStr(tokenName);

				out(tokenName, false);
			}

			else if (tokenType == PlSqlParser.ON) {
				out(tokenName, false);
//				afterOn = true;
			}

			else if (afterBetween && tokenType == PlSqlParser.AND) {
				out(tokenName, false);
				afterBetween = false;
			}

			else if ( LOGICAL.contains( tokenType ) ) {
				out(tokenName, false);
//				paragraphCnt = countStr(tokenName);
			}

			else if (tokenType == PlSqlParser.CREATE) {
				out(tokenName, false);
//				paragraphCnt = countStr(tokenName);
			}

			// 싱글 라인 코멘트일경우 줄바꿈문자 제거
			else if(tokenType == PlSqlParser.SINGLE_LINE_COMMENT) {
				out(tokenName.replace("\n", ""));
			}
			else if(tokenType == PlSqlParser.MULTI_LINE_COMMENT) {
				out(tokenName);
				lastLine = tokenLine + countLines(tokenName) -1;
			}
			else if(tokenType == PlSqlParser.EOF) {
				// Skip
			}

			else if(tokenType == PlSqlParser.REGULAR_ID) {
				if(firstBfTokenType == PlSqlParser.RIGHT_PAREN) {
					out(" ");
				}
				if( i+1 < queryTokenList.size()) {
					if(queryTokenList.get(i+1).getTokenType() == PlSqlParser.PERIOD
						||queryTokenList.get(i+1).getTokenType() == PlSqlParser.COMMA
						||queryTokenList.get(i+1).getTokenType() == PlSqlParser.RIGHT_PAREN
						||queryTokenList.get(i+1).getTokenType() == PlSqlParser.SEMICOLON) {
						out(tokenName);
					} else {
						out(tokenName, false);
					}
				} else {
					out(tokenName, false);
				}
			}
			// 그 외
			else {
				if (tokenType == PlSqlParser.BETWEEN) {
					afterBetween = true;
				}
//				else if (tokenType == PlSqlParser.CASE) {
//					indentCnt++;
//				}

				// 앞 공백 ================================================
//				if(tokenType == PlSqlParser.COMMA
//					|| tokenType == PlSqlParser.PERIOD
//					|| tokenType == PlSqlParser.RIGHT_PAREN
//					|| tokenType == PlSqlParser.CHAR_STRING) {
//					// 추가안함
//				} else {
//					if(firstBfTokenType == PlSqlParser.REGULAR_ID) {
//						out(" ");
//					}
//				}

				// 입력 ==================================================
				out(tokenName);

				// 뒷 공백 ================================================
				if(tokenType == PlSqlParser.LEFT_PAREN
					||tokenType == PlSqlParser.RIGHT_PAREN
					||tokenType == PlSqlParser.UNSIGNED_INTEGER
					||tokenType == PlSqlParser.PERIOD
//					||tokenType == PlSqlParser.CHAR_STRING
					||tokenType == PlSqlParser.SEMICOLON
					) {

				} else if(DATA_TYPE.contains(tokenType) ||ORA_FUNC.contains(tokenType)) {
					if(tokenType == PlSqlParser.NUMBER ||tokenType == PlSqlParser.REPLACE) out(" ");
				} else {
					out(" ");
				}
			}

			// whitespace 추가(위에추가)
//			if(tokenType != PlSqlParser.PERIOD) {
//				out(" ");
//			}

			// 다음작업을 위한 변수 세팅
			firstBfTokenName = tokenName;
			firstBfTokenType = tokenType;
//			secondBfTokenType = firstBfTokenType;

			LogManager.getLogger("debug").debug("[tokenName]:["+tokenName+"]"
					   +"[queryLastLineStr]:["+queryLastLineStr+"]"
				       +"[indentCnt]:["+indentCnt+"]"
				       +"[parensSinceSelect]:["+parensSinceSelect+"]"
					   );
		}

//		Log.printMethod("<결과값확인> =============================");
//		Log.printMethod("\n"+queryTokenListStr.toString());

		Log.printMethod("[END]");
		return queryTokenListStr;
	}

	private void out(String str) throws Exception {
		queryTokenListStr.append(str);
		if("\n".equals(str)) {
			queryLastLineStr = "";
		} else {
			queryLastLineStr += str;
		}
	}

	private void out(String str, boolean isLeft) throws Exception {
		if(isLeft) {
			out(" ");
			out(str);
		} else {
			out(str);
			out(" ");
		}

	}

	private boolean isFunctionName(String tok) {
		final char begin = tok.charAt( 0 );
		final boolean isIdentifier = Character.isJavaIdentifierStart( begin ) || '"' == begin;
		return isIdentifier &&
				!LOGICAL.contains( tok ) &&
				!END_CLAUSES.contains( tok ) &&
				!QUANTIFIERS.contains( tok ) &&
				!DML.contains( tok ) &&
				!MISC.contains( tok );
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
				||tokenName.indexOf("LEADING(") > 0
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
				||tokenName.indexOf("NO_MERGE") > 0
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
	private int countStr(String str){
		// TODO : 한글처리는 아직 생각하지 말자
		return  str.length();
	}

}
