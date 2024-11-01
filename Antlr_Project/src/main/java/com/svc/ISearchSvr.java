package com.svc;

import java.util.List;

import com.vo.SqlTokenInfo;

public interface ISearchSvr {
	void searchTable(List<List<SqlTokenInfo>> sqlList, List<SqlTokenInfo> tokenList);
}
