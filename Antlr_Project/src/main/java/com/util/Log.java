package com.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Vocabulary;

import com.domain.JavaSourceTokenInfo;
import com.domain.TokenInfo;

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;
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
	public static void error(Exception e) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		e.printStackTrace(ps);
		ps.println();;
		System.out.println(bos.toString());
	}
	
	public static void logListToString(Java8Parser parser, List<JavaSourceTokenInfo> tokenList) {
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
			System.out.printf("	≒	%s[%s]", symbolicId , symbolNo);
			
			if(symbolNo == PlSqlParser.REGULAR_ID) System.out.print("*");
			System.out.println();
		}
		Log.debug("=======================================================");
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
			System.out.printf("	≒	%s[%s]", symbolicId , symbolNo);
			
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
	
	public static void printInfomation(Java8Lexer lexer, Java8Parser parser, Vocabulary vocabulary, List<JavaSourceTokenInfo> tokenList) {
		Log.debug("[Antlr] Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("[Antlr] Token/Syntax Count : ["+tokenList.size()+"]");
		Log.debug("");
		
		Log.debug("--------------------------------------------------------------");
		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[SymbolNo]");
		Log.debug("--------------------------------------------------------------");
		
		for(int i=0; i<tokenList.size(); i++) {
			int symbolNo = tokenList.get(i).getSymbolNo();
			String tokenName = tokenList.get(i).getTokenName();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);
			
			System.out.printf("%4d: %s", (i+1), tokenName);
			for(int j=0; j<24-(tokenName.length()-getKorCnt(tokenName)); j++) {
				System.out.print(" ");
			}
			System.out.printf(" ≒ %s[%s]", symbolicId , symbolNo);
			
			if(symbolNo == Java8Parser.Identifier) System.out.print("*");
			System.out.println();
		}
		
		Log.debug("=======================================================");
	}
	
	public static void printInfomation(PlSqlLexer lexer, PlSqlParser parser, Vocabulary vocabulary, List<TokenInfo> tokenList) {
		Log.debug("[Antlr] Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("[Antlr] Token/Syntax Count : ["+tokenList.size()+"]");
		Log.debug("");
		
		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[SymbolNo]");
		Log.debug("--------------------------------------------------------------");
		
		for(int i=0; i<tokenList.size(); i++) {
			int symbolNo = tokenList.get(i).getSymbolNo();
			String tokenName = tokenList.get(i).getTokenName();
			String symbolicId = vocabulary.getSymbolicName(symbolNo);
			
			System.out.printf("%4d: %s", (i+1), tokenName);
			for(int j=0; j<24-(tokenName.length()-getKorCnt(tokenName)); j++) {
				System.out.print(" ");
			}
			System.out.printf("	≒	%s[%s]", symbolicId , symbolNo);
			
			if(symbolNo == PlSqlParser.REGULAR_ID) System.out.print("*");
			System.out.println();
		}
		
		Log.debug("=======================================================");
	}
	
	public static int getKorCnt(String kor) {
		int cnt = 0;
		for(int i=0; i<kor.length(); i++) {
			if(kor.charAt(i)>='가' && kor.charAt(i) <= '힣') cnt++;			
		}
		return cnt;
	}
	
	public String convert(String word, int size) {
		String formatter = String.format("%%%ds",  size-getKorCnt(word));
		return String.format(formatter,  word);
	}
}
