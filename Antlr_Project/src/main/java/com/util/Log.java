package com.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Vocabulary;

import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;
import com.vo.SvcFileFuncInfoVo;
import com.vo.SvcFileInfoVo;

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

		getLogger("debug").debug("=======================================================");
		int idx=0;
		for(String key : map.keySet()) {
			getLogger("debug").debug("["+(++idx)+"] key : "+key+", Value : "+map.get(key));
		}
		getLogger("debug").debug("=======================================================");
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
//			if(tokenList.get(i).isConvert()) lastStr += "<전환대상>";
			Log.debug(StringUtil.padString(Integer.toString(i+1), 4, " ", true)
					 +":"+StringUtil.rightBytesPad(tokenName, 24)
					 +" ≒ "+symbolicName+"["+tokenType+"]"+lastStr);
		}

		Log.debug("=======================================================");
	}

	public static void printInfomation(List<SvcFileInfoVo> svcFileInfoList) {
		Log.debug("--------------------------------------------------------------");
		Log.debug("[Antlr] Svc File Count : ["+svcFileInfoList.size()+"]");
		Log.debug("");
//		Log.debug("   #: Token/Syntax             ≒ Symbolic Id[TokenType]");
		Log.debug("--------------------------------------------------------------");

		int idx = 1;

		for(int i = 0; i < svcFileInfoList.size(); i++) {
			String PkgNm = svcFileInfoList.get(i).getPkgNm();
			String sorcNm = svcFileInfoList.get(i).getSorcNm();
			List<SvcFileFuncInfoVo> svcFileFuncInfoVoList = svcFileInfoList.get(i).getFuncInfoVoList();

//			Log.debug(StringUtil.padString(Integer.toString(i+1), 4, " ", true)
//					 +":"+StringUtil.rightBytesPad(PkgNm, 30)
//					 +":"+StringUtil.rightBytesPad(sorcNm, 50));
			for(int k = 0; k < svcFileFuncInfoVoList.size(); k++) {
				String funcKrnNm = svcFileFuncInfoVoList.get(k).getFuncKrnNm();
				String funcNm = svcFileFuncInfoVoList.get(k).getFuncNm();
				String svcId = svcFileFuncInfoVoList.get(k).getSvcId();

//				Log.debug(StringUtil.padString(Integer.toString(k+1), 4, " ", true)
//						+") "+StringUtil.rightBytesPad(svcId, 12)
//						+" "+StringUtil.rightBytesPad(funcNm+"("+funcKrnNm+")", 100));
				Log.debug(StringUtil.padString(Integer.toString(idx++), 4, " ", true)
						+"|"+sorcNm //StringUtil.rightBytesPad(sorcNm, 50)
						+"|"+funcNm //StringUtil.rightBytesPad(funcNm, 100)
						+"|"+funcKrnNm //StringUtil.rightBytesPad(funcKrnNm, 100)
						+"|"+StringUtil.rightBytesPad(svcId, 12, '0'));
			}
		}

		Log.debug("=======================================================");
	}
}
