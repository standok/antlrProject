package com.svc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.vo.DataMapDefinitionVo;

public interface IPoiSvr {

	/**
	 * 설명 : Excel 파일내용을 읽고 Table정보를 Map으로 변경하여 리턴
	 *
	 * @param File file
	 * @return HashMap<String, HashMap>
	 * @throws IOException
	 */
	public HashMap readExcelToMap(File file) throws IOException;

	/**
	 * 설명 : Excel 파일내용을 읽고 Table정보를 List으로 변경하여 리턴
	 *
	 * @param File file
	 * @return List<DataMapDefinitionVo>
	 * @throws IOException
	 */
	public List<DataMapDefinitionVo> readExcelToList(File file) throws IOException;

}
