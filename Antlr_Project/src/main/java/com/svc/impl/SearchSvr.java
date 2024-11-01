package com.svc.impl;

import java.util.ArrayList;
import java.util.List;

import com.svc.ISearchSvr;
import com.vo.SqlTokenInfo;

public class SearchSvr implements ISearchSvr {

	@Override
	public void searchTable(List<List<SqlTokenInfo>> sqlList, List<SqlTokenInfo> tokenList) {

		List<String> mainTableName = new ArrayList<>();

		for(SqlTokenInfo tokenInfo : tokenList) {

			for(int i=sqlList.size()-1; i>=0; i--) {
				if(checkColumnInTable(tokenList, tokenInfo)) {
//					mainTableName.add(null);
				}
			}
		}
	}

	public boolean checkColumnInTable(List<SqlTokenInfo> tokenList, SqlTokenInfo tokenInfo) {
		boolean checkColumn = false;
		boolean checkTable = false;

		for(SqlTokenInfo tokenInfo2 : tokenList) {
			if(tokenInfo2.getTokenName().equals(tokenInfo.getTokenName())) checkColumn = true;
			if(tokenInfo2.getTableName().equals(tokenInfo.getTableName())) checkTable = true;
		}
		return (checkColumn && checkTable);
	}
}
