package com.svc.impl;

import java.util.ArrayList;
import java.util.List;

import com.domain.TokenInfo;
import com.svc.ISearchSvr;
import com.util.Log;

public class SearchSvr implements ISearchSvr {

	@Override
	public void searchTable(List<List<TokenInfo>> sqlList, List<TokenInfo> tokenList) {
		
		List<String> mainTableName = new ArrayList<>();
		
		for(TokenInfo tokenInfo : tokenList) {
			Log.debug(tokenInfo.toString());
			for(int i=sqlList.size()-1; i>=0; i--) {
				if(checkColumnInTable(tokenList, tokenInfo)) {
//					mainTableName.add(null);
				}
			}
		}
	}

	public boolean checkColumnInTable(List<TokenInfo> tokenList, TokenInfo tokenInfo) {
		boolean checkColumn = false;
		boolean checkTable = false;
		
		for(TokenInfo tokenInfo2 : tokenList) {
			if(tokenInfo2.getTokenName().equals(tokenInfo.getTokenName())) checkColumn = true;
			if(tokenInfo2.getTableName().equals(tokenInfo.getTableName())) checkTable = true;
		}
		return (checkColumn && checkTable);
	}
}
