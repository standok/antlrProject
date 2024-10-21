package com.svc.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.domain.TokenInfo;
import com.dto.TokenInfoCreateDto;
import com.svc.IParsingSvr;
import com.svc.ITokenInfoSvc;
import com.util.Log;

import util.antlr.PlSqlParser;

public class ParsingSvr implements IParsingSvr {

	ITokenInfoSvc tokenInfoSvr = new TokenInfoSvc(); 
	
	int tokenIdx;
	
	/**
	 * 설명 : SELECT SQL문 처리 
	 */
	@Override
	public List<TokenInfo> parsingSql(List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) {
		
		Log.debug("ParsingSvr.parsingSql Start~!!");
		
		List<TokenInfo> parsingResultList = new ArrayList<>();
		TokenInfoCreateDto tokenInfoCreateDto = new TokenInfoCreateDto();
		
		Map<String, String> columnMap = new LinkedHashMap<>();
		Map<String, String> tableMap = new LinkedHashMap<>();
		
		String whereStr = "";
		
		tokenIdx = 1;
		int tokenSize = tokenList.size();
		
		//Column 정보 저장
		columnMap = addColumnMap(tokenSize, tokenList, parser, reservedWordList);
		Log.debug("==columnMap==");
		Log.logMapToString(columnMap);
		
		//table 정보 저장
		if(tokenList.get(tokenIdx).getSymbolNo() == PlSqlParser.FROM) {
			tokenIdx++;
			tableMap = addTableMap(tokenSize, tokenList, parser, reservedWordList);
		}
		Log.debug("==tableMap==");
		Log.logMapToString(tableMap);
		
		//where문 정보 저장
		whereStr = addWhereStr(tokenSize, tokenList); 
		Log.debug(whereStr);;
		
		for(String key : columnMap.keySet()) {
			String columnName = columnMap.get(key);
			
			if(columnName.contains(".")) {
				String tableAliasName = columnName.substring(0, columnName.indexOf("."));
				
				if(tableMap.containsKey(tableAliasName)) {
					String columnMainName = columnName.substring((columnName.indexOf(".")+1));
					String tableMainName = tableMap.get(tableAliasName);
					
					parsingResultList.add(tokenInfoCreateDto.toEntity(columnMainName, key, tableMainName));
				}
			} else {
				for(String tableKey : tableMap.keySet()) {
					parsingResultList.add(tokenInfoCreateDto.toEntity(columnName, key, tableMap.get(tableKey)));
				}
			}
		}
		
		Log.logListToString(parser, parsingResultList);
		
		return parsingResultList;
	}
	
	/**
	 * 설명 : 토큰 분석 후 Column Map 생성
	 */
	public Map<String, String> addColumnMap(int tokenSize, List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) {
		Map<String, String> aliasMap = new LinkedHashMap<>();
		
		for(; tokenIdx<tokenSize; tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();
			
			if(tokenInfoSvr.checkReservedWord(symbolNo, reservedWordList)) {
				break;
			} else if (symbolNo == PlSqlParser.COMMA
					 ||symbolNo == PlSqlParser.PERIOD
					 ||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.PERIOD) {
				continue;
			} else {
				if(tokenList.get(tokenIdx-1).getSymbolNo() == PlSqlParser.PERIOD) {
					tokenName = tokenList.get(tokenIdx-2).getTokenName() + "." + tokenName;
				}
				
				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					aliasMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);				
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS 
					    ||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					aliasMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					tokenIdx+=2;
				} else {
					aliasMap.put(tokenName, tokenName);
				}
			}
		}
		
		return aliasMap;
	}
	
	/**
	 * 설명 : 토큰 분석 후 Table Map 생성
	 */
	public Map<String, String> addTableMap(int tokenSize, List<TokenInfo> tokenList, PlSqlParser parser, List<Integer> reservedWordList) {
		Map<String, String> aliasMap = new LinkedHashMap<String, String>();
		
		for(; tokenIdx<tokenSize; tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			int symbolNo = tokenList.get(tokenIdx).getSymbolNo();
			
			if(tokenInfoSvr.checkReservedWord(symbolNo, reservedWordList)) {
				break;
			} else if (symbolNo == PlSqlParser.COMMA) {
				continue;
			} else {
				if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.REGULAR_ID) {
					aliasMap.put(tokenList.get(tokenIdx+1).getTokenName(), tokenName);				
					tokenIdx++;
				} else if(tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.AS 
					    ||tokenList.get(tokenIdx+1).getSymbolNo() == PlSqlParser.ALIAS) {
					aliasMap.put(tokenList.get(tokenIdx+2).getTokenName(), tokenName);
					tokenIdx+=2;
				} else {
					aliasMap.put(tokenName, tokenName);
				}
			}
		}
		
		return aliasMap;
	}
	
	/**
	 * 설명 : 토큰 분석 후 Where Map 생성
	 */
	public String addWhereStr(int tokenSize, List<TokenInfo> tokenList) {
		String whereStr = "";
		
		tokenIdx++;
		
		for(; tokenIdx<tokenSize; tokenIdx++) {
			String tokenName = tokenList.get(tokenIdx).getTokenName();
			whereStr += " " + tokenName;
		}
		
		return whereStr;
	}
			
}
