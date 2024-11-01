package com.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Vocabulary;

import com.vo.JavaTokenInfo;
import com.vo.SqlTokenInfo;

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;
import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class Log extends LogManager {

	public static void debug(String msg) {
//		Logger.getLogger("debug").debug(msg);
		LogManager.getLogger("debug").debug(msg);
		System.out.println(msg);
	}

	public static void error(String msg) {
//		Logger.getLogger("error").error(msg);
		LogManager.getLogger("debug").debug(msg);
		System.out.println(msg);
	}
	public static void error(Exception e) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		e.printStackTrace(ps);
		ps.println();;
//		Logger.getLogger("error").error(bos.toString());
		LogManager.getLogger("debug").debug(bos.toString());
		System.out.println(bos.toString());
	}

	public static void logListToString(Java8Parser parser, List<JavaTokenInfo> tokenList) {
		Vocabulary vocabulary = parser.getVocabulary();

		Log.debug("=======================================================");
		for(int i=0; i<tokenList.size(); i++) {
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);

			String lastStr = "";
			if(tokenType == PlSqlParser.REGULAR_ID) lastStr = "*";
			Log.debug(ConverterUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+ConverterUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}
		Log.debug("=======================================================");
	}

	public static void logListToString(List<SqlTokenInfo> tokenList) {
		Log.debug("=======================================================");
		for(int i=0; i<tokenList.size(); i++) {
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = tokenList.get(i).getSymbolicName();

			String lastStr = "";
			if(tokenType == PlSqlParser.REGULAR_ID) lastStr = "*";
			Log.debug(ConverterUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+ConverterUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
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

	public static void printInfomation(Java8Lexer lexer, Java8Parser parser, List<JavaTokenInfo> tokenList) {
		Log.debug("[Antlr] Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("[Antlr] Token/Syntax Count : ["+tokenList.size()+"]");
		Log.debug("");

		Log.debug("--------------------------------------------------------------");
		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[TokenType]");
		Log.debug("--------------------------------------------------------------");

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i=0; i<tokenList.size(); i++) {
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);

			String lastStr = "";
			if(tokenType == Java8Parser.Identifier) lastStr = "*";
			Log.debug(ConverterUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+ConverterUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}

		Log.debug("=======================================================");
	}

	public static void printInfomation(PlSqlLexer lexer, PlSqlParser parser, List<SqlTokenInfo> tokenList) {
		Log.debug("[Antlr] Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("[Antlr] Token/Syntax Count : ["+tokenList.size()+"]");
		Log.debug("");

		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[TokenType]");
		Log.debug("--------------------------------------------------------------");

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i=0; i<tokenList.size(); i++) {
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);

			String lastStr = "";
			if(tokenType == PlSqlParser.REGULAR_ID) lastStr = "*";
			Log.debug(ConverterUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+ConverterUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}

		Log.debug("=======================================================");
	}
}
