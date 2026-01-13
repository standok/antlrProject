package com.svc;

import java.io.File;
import java.io.IOException;

import com.vo.SvcFileInfoVo;

public interface IInquiryFileSvc {

	/**
	 * 설명 : Java 파일내용 조회
	 *
	 * @param File
	 * @throws Exception, IOException
	 */
	public SvcFileInfoVo inquiryJava(File file) throws IOException;

}
