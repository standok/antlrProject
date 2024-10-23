package com.svc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.domain.DataMapDefinitionVo;

public interface IPoiSvr {
	public HashMap readExcelToMap(File file) throws IOException;
	public List<DataMapDefinitionVo> readExcelToList(File file) throws IOException;
	void writeExcel(Map<String, String> columnMap) throws IOException;
}
