package com.svc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.domain.DataMapDefinitionVo;

public interface IPoiSvr {
	public List<DataMapDefinitionVo> readExcel(File file) throws IOException;
	void writeExcel(Map<String, String> columnMap) throws IOException;
}
