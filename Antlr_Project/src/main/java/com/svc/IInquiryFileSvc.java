package com.svc;

import java.io.File;
import java.io.IOException;

public interface IInquiryFileSvc {

	/**
	 * 설명 : Java 파일내용 조회
	 *
	 * @param File
	 * @throws Exception, IOException
	 */
	public void inquiryJava(File file) throws IOException;

}
