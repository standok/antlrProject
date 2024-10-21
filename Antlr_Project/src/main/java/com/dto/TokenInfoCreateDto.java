package com.dto;

import com.domain.TokenInfo;

public class TokenInfoCreateDto {
	public TokenInfo toEntity(String tokenName, String aliasName, String tableName) {
		return new TokenInfo(tokenName, aliasName, tableName, "", 0);
	}
	public TokenInfo toEntity(String tokenName, String symbolicId, int symbolNo) {
		return new TokenInfo(tokenName, "", "", symbolicId, symbolNo);
	}
	public TokenInfo toEntity(String tokenName, String aliasName, String tableName, String symbolicId, int symbolNo) {
		return new TokenInfo(tokenName, aliasName, tableName, symbolicId, symbolNo);
	}
}
