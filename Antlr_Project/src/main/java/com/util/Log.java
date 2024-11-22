package com.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Vocabulary;

import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;

import util.antlr.Java8Lexer;
import util.antlr.Java8Parser;
import util.antlr.PlSqlLexer;
import util.antlr.PlSqlParser;

public class Log extends LogManager {

	public static void debug(String msg) {
		getLogger("debug").debug(msg);
		System.out.println(msg);
	}

	public static void error(String msg) {
		getLogger("debug").debug(msg);
		System.out.println(msg);
	}
	public static void error(Exception e) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		e.printStackTrace(ps);
		ps.println();;
//		getLogger("error").error(bos.toString());
		getLogger("debug").debug(bos.toString());
		System.out.println(bos.toString());
	}

	public static void printMethod(String msg) {
		String printStr = "";
		StackTraceElement[] tmp = Thread.currentThread().getStackTrace();
		if(tmp.length > 2) {
			printStr = tmp[2].getClassName()+"."+tmp[2].getMethodName();
		}
		getLogger("debug").debug(printStr+" "+msg);
//		System.out.println(msg);
	}

	public static void logJavaListToString(List<JavaTokenInfoVo> tokenList) {
		Log.debug("=======================================================");
		for(int i=0; i<tokenList.size(); i++) {
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = tokenList.get(i).getSymbolicName();

			String lastStr = "";
			if(tokenType == PlSqlParser.REGULAR_ID) lastStr = "*";
			Log.debug(StringUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+StringUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}
		Log.debug("=======================================================");
	}

	public static void logSqlListToString(List<SqlTokenInfoVo> tokenList) {
		Log.debug("=======================================================");
		for(int i=0; i<tokenList.size(); i++) {
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = tokenList.get(i).getSymbolicName();
			String aliasName = tokenList.get(i).getAliasName();
			String tableId = tokenList.get(i).getTableId();

			String lastStr = "";
			if(tokenType == PlSqlParser.REGULAR_ID) lastStr = "*";
//			Log.debug(StringUtil.padString(Integer.toString(i+1), 4, " ", true)
//					 +":"+StringUtil.rightBytesPad(tokenName, 24)
//					 +" ≒ "+StringUtil.rightBytesPad(symbolicName+"["+tokenType+"]"+lastStr, 50)
//					 +">>"+"["+StringUtil.rightBytesPad(aliasName, 20)+"]/"
//					 +"/ "+"["+StringUtil.rightBytesPad(tableId, 20)+"]");
			Log.debug(StringUtil.padString(Integer.toString(tokenList.get(i).getTokenIndex()+1), 4, " ", true)
					+"/"+StringUtil.rightBytesPad(tokenList.get(i).getTokenName(), 24)
					+"/"+tokenType
					+"/"+symbolicName
					+"/"+tokenList.get(i).getTokenLine()
					+"/"+tokenList.get(i).getRolePosition()
					+"/"+tokenList.get(i).getAliasName()
					+"/"+tableId
					+"/"+tokenList.get(i).isConvert()
					+"/"+tokenList.get(i).getConvertRule());
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

	public static void printInfomation(Java8Lexer lexer, Java8Parser parser, List<JavaTokenInfoVo> tokenList) {
		Log.debug("[Antlr] Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("[Antlr] Token/Syntax Count : ["+tokenList.size()+"]");
		Log.debug("");

		Log.debug("--------------------------------------------------------------");
		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[TokenType]");
		Log.debug("--------------------------------------------------------------");

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i=0; i<tokenList.size(); i++) {
			int tokenIndex = tokenList.get(i).getTokenIndex();
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);

			String lastStr = "";
			if(tokenType == Java8Parser.Identifier) lastStr = "*";
			if(tokenList.get(i).isConvert()) lastStr += "<전환대상>";
			Log.debug(StringUtil.padString(Integer.toString(tokenIndex), 4, " ", true)
					 +":"+StringUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}

		Log.debug("=======================================================");
	}

	public static void printInfomation(PlSqlLexer lexer, PlSqlParser parser, List<SqlTokenInfoVo> tokenList) {
		Log.debug("[Antlr] Parser/Lexer Grammar : ["+parser.getGrammarFileName()+", "+lexer.getGrammarFileName()+"]");
		Log.debug("[Antlr] Token/Syntax Count : ["+tokenList.size()+"]");
		Log.debug("");

		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[TokenType]");
		Log.debug("--------------------------------------------------------------");

		Vocabulary vocabulary = parser.getVocabulary();

		for(int i=0; i<tokenList.size(); i++) {
			int tokenIndex = tokenList.get(i).getTokenIndex();
			String tokenName = tokenList.get(i).getTokenName();
			int tokenType = tokenList.get(i).getTokenType();
			String symbolicName = vocabulary.getSymbolicName(tokenType);

			String lastStr = "";
			if(tokenType == PlSqlParser.REGULAR_ID) lastStr = "*";
			if(tokenList.get(i).isConvert()) lastStr += "<전환대상>";
			Log.debug(StringUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+StringUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}

		Log.debug("=======================================================");
	}
}
