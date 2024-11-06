package com.svc.impl;

import java.util.List;

import org.apache.log4j.LogManager;

import com.svc.ICreateFileSvr;
import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;

import util.antlr.Java8Parser;

public class CreateFileSvr implements ICreateFileSvr {


	@Override
	public void createJavaFile(List<JavaTokenInfoVo> javaTokenList) {

		LogManager.getLogger("debug").debug("CreateFileSvr.createJavaFile Start~!!");

		int lastLine = 1;	// 마지막줄
		int depCnt = 1;		// 들여쓰기깊이
		StringBuilder str = new StringBuilder();	// 소스내용

		str.append("\t");

		LogManager.getLogger("debug").debug("javaConList 확인 =============================");

		for(int i = 0; i < javaTokenList.size(); i++) {

			String tokenName = javaTokenList.get(i).getTokenName();
			int tokenType = javaTokenList.get(i).getTokenType();
//			String symbolicName = vocabulary.getSymbolicName(tokenType);
			int tokenLine = javaTokenList.get(i).getTokenLine();

			// 들여쓰기 깊이 설정
			if(tokenType == Java8Parser.LBRACE) {
				depCnt++;
			} else if(tokenType == Java8Parser.RBRACE) {
				depCnt--;
			}

			if(tokenLine - lastLine > 0) {

				// 줄바꿈
				for(int s = 0; s < tokenLine - lastLine; s++) {
					str.append("\n");
				}

				// 들여쓰기
				for(int d = 0; d < depCnt; d++) {
					str.append("\t");
				}

				lastLine = tokenLine + countLines(tokenName) -1;
			}

//			LogManager.getLogger("debug").debug("[Token]["+i+"]==>"
//					+"["+tokenName+"]"
//					+"["+symbolicName+"]"
//					+"["+lastLine+"]"
//					+"["+tokenLine+"]"
//					+"["+countLines(tokenName)+"]"
//					);

			/*****************************************
			 * 앞/뒤 공백 입력
			 * TODO: 테스트하면서 추가
			 *****************************************/
			// 앞뒤 공백
			if(tokenType == Java8Parser.ASSIGN
				||tokenType == Java8Parser.ADD
				||tokenType == Java8Parser.SUB
				||tokenType == Java8Parser.MOD

				||tokenType == Java8Parser.ADD
				||tokenType == Java8Parser.ADD
				||tokenType == Java8Parser.ADD
				) {
				str.append(" " + tokenName + " ");
			}
			// 앞 공백
			else if(tokenType == Java8Parser.LBRACE) {
				str.append(" " + tokenName);
			}
			// 뒤 공백
			else if(tokenType == Java8Parser.PACKAGE
					|| tokenType == Java8Parser.IMPORT
					|| tokenType == Java8Parser.CLASS
					|| tokenType == Java8Parser.PUBLIC
					|| tokenType == Java8Parser.PRIVATE
					|| tokenType == Java8Parser.EXTENDS
//					|| tokenType == Java8Parser.LPAREN
//					|| tokenType == Java8Parser.RPAREN
//					|| tokenType == Java8Parser.LBRACE
//					|| tokenType == Java8Parser.RBRACE
					|| tokenType == Java8Parser.THROW
					|| tokenType == Java8Parser.NEW

					|| tokenType == Java8Parser.NEW
					|| tokenType == Java8Parser.NEW
					|| tokenType == Java8Parser.NEW
					|| tokenType == Java8Parser.NEW
					|| tokenType == Java8Parser.NEW
					|| tokenType == Java8Parser.NEW
				) {
				str.append(tokenName + " ");
			} else if(tokenType == Java8Parser.EOF) {
				// Skip
			} else if(tokenType == Java8Parser.Identifier) {
				if("String".equals(tokenName)) {
					str.append(tokenName + " ");
				} else {
					str.append(tokenName);
				}
			} else {
				str.append(tokenName);
			}
		}
		LogManager.getLogger("debug").debug("<결과값확인> =============================");
		LogManager.getLogger("debug").debug("\n"+str.toString());
	}

	@Override
	public void createSqlFile(List<List<SqlTokenInfoVo>> sqlConList, List<SqlTokenInfoVo> sqlTokenList) {

	}

	private static int countLines(String str){
	   String[] lines = str.split("\r\n|\r|\n");
	   return  lines.length;
	}

	private void searchTable(List<List<SqlTokenInfoVo>> sqlConList, List<SqlTokenInfoVo> sqlTokenList) {

	}

	public boolean checkColumnInTable(List<SqlTokenInfoVo> tokenList, SqlTokenInfoVo tokenInfo) {
		boolean checkColumn = false;
		boolean checkTable = false;

		for(SqlTokenInfoVo tokenInfo2 : tokenList) {
			if(tokenInfo2.getTokenName().equals(tokenInfo.getTokenName())) checkColumn = true;
			if(tokenInfo2.getTableName().equals(tokenInfo.getTableName())) checkTable = true;
		}
		return (checkColumn && checkTable);
	}
}
