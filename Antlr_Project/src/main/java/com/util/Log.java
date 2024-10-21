package com.util;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Vocabulary;

import com.domain.TokenInfo;

import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class Log {
	
	public static void debug(String msg) {
		//Logger.getLogger("debug").log("debug", msg);
		System.out.println(msg);
	}
	
	public static void error(String msg) {
		//Logger.getLogger("debug").log("error", msg);
		System.out.println(msg);
	}
	
	public static void logListToString(PlSqlParser parser, List<TokenInfo> tokenList) {
		Vocabulary vocabulary = parser.getVocabulary();
		
		Log.debug("=======================================================");
		for(int i=0; i<tokenList.size(); i++) {
			int symbolNo = tokenList.get(i).getSymbolNo();
			String tokenName = tokenList.get(i).getTokenName();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);
			
			System.out.printf("%4d: %s", (i+1), tokenName);
			for(int j=0; j<24-(tokenName.length()-getKorCnt(tokenName)); j++) {
				System.out.print(" ");
			}
			System.out.printf("	¡Ö	%s[%s]", symbolicId , symbolNo);
			
			if(symbolNo == PlSqlParser.REGULAR_ID) System.out.print("*");
			System.out.println();
		}
		Log.debug("=======================================================");
	}
	
	public static void logMapToString(Map<String, String> map) {
		
		Log.debug("=======================================================");
		int idx=0;
		for(String key : map.keySet()) {
			Log.debug("["+(++idx)+"] key : "+key+", Value : "+map.get(key));
		}
		Log.debug("=======================================================");
	}
	
	public static void printInfomation(PlSqlLexer lexer, PlSqlParser parser, Vocabulary vocabulary, List<TokenInfo> tokenList) {
		Log.debug("Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("Token/Syntax Count : ["+tokenList.size()+"]");
		
		Log.debug("    #:Token/Syntax             ¡Ö Symbolic Id[SymbolNo]");
		Log.debug("=======================================================");
		
		for(int i=0; i<tokenList.size(); i++) {
			int symbolNo = tokenList.get(i).getSymbolNo();
			String tokenName = tokenList.get(i).getTokenName();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);
			
			System.out.printf("%4d: %s", (i+1), tokenName);
			for(int j=0; j<24-(tokenName.length()-getKorCnt(tokenName)); j++) {
				System.out.print(" ");
			}
			System.out.printf("	¡Ö	%s[%s]", symbolicId , symbolNo);
			
			if(symbolNo == PlSqlParser.REGULAR_ID) System.out.print("*");
			System.out.println();
		}
		
		Log.debug("=======================================================");
	}
	
	public static int getKorCnt(String kor) {
		int cnt = 0;
		for(int i=0; i<kor.length(); i++) {
			if(kor.charAt(i)>='°¡' && kor.charAt(i) <= 'ÆR') cnt++;			
		}
		return cnt;
	}
	
	public String convert(String word, int size) {
		String formatter = String.format("%%%ds",  size-getKorCnt(word));
		return String.format(formatter,  word);
	}
}
