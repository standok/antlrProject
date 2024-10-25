package com.svc;

import java.util.List;

import com.domain.JavaSourceTokenInfo;
import com.domain.TokenInfo;

import util.antlr.Java8Parser;
import util.antlr.PlSqlParser;

public interface IParsingSvr {
	List<JavaSourceTokenInfo> parsingJavaToSQL(List<JavaSourceTokenInfo> tokenList, Java8Parser parser, List<Integer> reservedWordList);
	List<TokenInfo> parsingSql(List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList);
}
