package com.svc;

import java.util.List;

import com.domain.TokenInfo;

public interface ISearchSvr {
	void searchTable(List<List<TokenInfo>> sqlList, List<TokenInfo> tokenList);
}
