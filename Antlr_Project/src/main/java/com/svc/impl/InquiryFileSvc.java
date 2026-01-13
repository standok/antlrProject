package com.svc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.svc.IInquiryFileSvc;
import com.svc.IParsingJavaSvc;
import com.util.Log;
import com.vo.JavaTokenInfoVo;
import com.vo.SvcFileFuncInfoVo;
import com.vo.SvcFileInfoVo;

import util.antlr.Java8Parser;

public class InquiryFileSvc implements IInquiryFileSvc {

	/**
	 * 설명 : Java 파일내용 조회
	 *
	 * @param File
	 * @throws Exception, IOException
	 */
	@Override
	public SvcFileInfoVo inquiryJava(File file) throws IOException {

		String fileName = file.getName();

		InputStream in = null;
		InputStreamReader rd = null;
		BufferedReader br = null;

		StringBuilder sb = new StringBuilder();
		String line;

		SvcFileInfoVo rtnVo = null;

		try {
			in = new FileInputStream(file);
			rd = new InputStreamReader(in, "UTF-8");
			br = new BufferedReader(rd);

			while((line=br.readLine()) != null) sb.append(line + "\n");
			rtnVo = inquirySvcFileInfo(file.getPath(), sb);

		} catch (Exception e) {

		} finally {
			if(br != null) try{br.close();} catch(IOException ioe){}
			if(rd != null) try{rd.close();} catch(IOException ioe){}
			if(in != null) try{in.close();} catch(IOException ioe){}
		}

		return rtnVo;
	}

	/**
	 * 설명 : StringBuilder로 변환된 Java 소스 변환시작
	 *
	 * @param String filePath, StringBuilder sb
	 * @return
	 * @throws Exception, IOException
	 */
	private SvcFileInfoVo inquirySvcFileInfo(String filePath, StringBuilder sb) throws Exception, IOException {

		Log.printMethod("[START]");

		/**********************************
		 * TokenStream에서 필요한정보를 List로 변환한다
		 **********************************/
		IParsingJavaSvc parsingJavaSvc = new ParsingJavaSvc();
		parsingJavaSvc.parsingJava(sb, "1");	// 거래구분코드(1:조회)

		// 파싱한 정보를 가져온다.
		List<JavaTokenInfoVo> javaTokenList = parsingJavaSvc.getJavaTokenList();

		SvcFileInfoVo rtnVo = new SvcFileInfoVo();
		List<SvcFileFuncInfoVo> funcVoList = new ArrayList();
		SvcFileFuncInfoVo funcVo = null;

		boolean funcChk = false;

		for(int i = 0; i < javaTokenList.size(); i++) {
			String tokenName = javaTokenList.get(i).getTokenName();
			int tokenType = javaTokenList.get(i).getTokenType();
			int tokenLine = javaTokenList.get(i).getTokenLine();

			/*************************************
			 * ServiceID, 함수명을 조회하기 위한 단순조회
			 *************************************/
			if (tokenType == Java8Parser.INTERFACE) {
				rtnVo.setSorcNm(javaTokenList.get(i+1).getTokenName());
				funcVo = new SvcFileFuncInfoVo();
			}

			if (tokenType == Java8Parser.AT
					&& javaTokenList.get(i+1).getTokenType() == Java8Parser.Identifier
					&& javaTokenList.get(i+2).getTokenType() == Java8Parser.LPAREN
					&& javaTokenList.get(i+3).getTokenType() == Java8Parser.StringLiteral) {

				String tokenName1 = javaTokenList.get(i+1).getTokenName();
				String tokenName3 = javaTokenList.get(i+3).getTokenName();

				// 함수한글명
				if("LocalName".equals(tokenName1)) {
					funcVo.setFuncKrnNm(tokenName3.replace("\"", ""));
				}
				// 서비스ID
				if("ServiceIdMapping".equals(tokenName1)) {
					funcVo.setSvcId(tokenName3.replace("\"", ""));
					funcChk = true;	// 함수 확인
				}
			}

			if(funcChk) {
				if (tokenType == Java8Parser.PUBLIC
						&& javaTokenList.get(i+2).getTokenType() == Java8Parser.Identifier) {
					funcVo.setFuncNm(javaTokenList.get(i+2).getTokenName());
				}

				if (tokenType == Java8Parser.SEMI) {
					funcVoList.add(funcVo);
					funcChk = false;
					funcVo = new SvcFileFuncInfoVo();
				}
			}
		}

		// 함수List 저장
		rtnVo.setFuncInfoVoList(funcVoList);

		Log.printMethod("[END]");
		return rtnVo;
	}

}
