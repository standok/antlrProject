package com.svc;

import java.util.List;

import com.domain.TokenInfo;

import util.antlr.PlSqlParser;

public interface IParsingSvr {
	List<TokenInfo> parsingSql(List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList);
}
