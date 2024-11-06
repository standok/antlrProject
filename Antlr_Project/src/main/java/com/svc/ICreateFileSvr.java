package com.svc;

import java.util.List;

import com.vo.JavaTokenInfoVo;
import com.vo.SqlTokenInfoVo;

public interface ICreateFileSvr {
	void createJavaFile(List<JavaTokenInfoVo> javaTokenList);
	void createSqlFile(List<List<SqlTokenInfoVo>> sqlConList, List<SqlTokenInfoVo> sqlTokenList);
}
