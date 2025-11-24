package com.svc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.svc.IInquiryFileSvc;
import com.svc.IParsingJavaSvc;
import com.util.Log;
import com.vo.JavaTokenInfoVo;

import util.antlr.Java8Parser;

public class InquiryFileSvc implements IInquiryFileSvc {

	/**
	 * 설명 : Java 파일내용 조회
	 *
	 * @param File
	 * @throws Exception, IOException
	 */
	@Override
	public void inquiryJava(File file) throws IOException {

		String fileName = file.getName();

		InputStream in = null;
		InputStreamReader rd = null;
		BufferedReader br = null;

		StringBuilder sb = new StringBuilder();
		String line;

		try {
			in = new FileInputStream(file);
			rd = new InputStreamReader(in, "UTF-8");
			br = new BufferedReader(rd);

			while((line=br.readLine()) != null) sb.append(line + "\n");
			inquiryJava1(file.getPath(), sb);

		} catch (Exception e) {

		} finally {
			if(br != null) try{br.close();} catch(IOException ioe){}
			if(rd != null) try{rd.close();} catch(IOException ioe){}
			if(in != null) try{in.close();} catch(IOException ioe){}
		}
	}

	/**
	 * 설명 : StringBuilder로 변환된 Java 소스 변환시작
	 *
	 * @param String filePath, StringBuilder sb
	 * @return
	 * @throws Exception, IOException
	 */
	private void inquiryJava1(String filePath, StringBuilder sb) throws Exception, IOException {

		Log.printMethod("[START]");

		/**********************************
		 * TokenStream에서 필요한정보를 List로 변환한다
		 **********************************/
		IParsingJavaSvc parsingJavaSvc = new ParsingJavaSvc();
		parsingJavaSvc.parsingJava(sb, "1");	// 거래구분코드(1:조회)

		// 파싱한 정보를 가져온다.
		List<JavaTokenInfoVo> javaTokenList = parsingJavaSvc.getJavaTokenList();

		for(int i = 0; i < javaTokenList.size(); i++) {
			String tokenName = javaTokenList.get(i).getTokenName();
			int tokenType = javaTokenList.get(i).getTokenType();
			int tokenLine = javaTokenList.get(i).getTokenLine();

			/*************************************
			 * ServiceID, 함수명을 조회하기 위한 단순조회
			 *************************************/
			if (tokenType == Java8Parser.INTERFACE) {

			}

			if (tokenType == Java8Parser.AT) {
				String serviceId = "";

			}


		}

		Log.printMethod("[END]");
	}

}
