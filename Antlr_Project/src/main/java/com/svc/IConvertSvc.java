package com.svc;

import java.io.File;
import java.io.IOException;

public interface IConvertSvc {

	/**
	 * 설명 : 파일내용을 String으로 변환
	 *
	 * @param File
	 * @throws Exception, IOException
	 */
	public void convertFileToString(File file) throws Exception, IOException;

	/**
	 * 설명 : String을 File로 변환
	 *
	 * @param String
	 * @throws Exception, IOException
	 */
	public void convertStringToFile(String str) throws Exception, IOException;
}
