package com.vo;

import java.util.List;

public class SvcFileInfoVo {

	private String pkgNm;			// 패키지
	private String sorcNm;			// 소스명

	private List<SvcFileFuncInfoVo> funcInfoVoList = null;	// 함수List

	public String getPkgNm() {
		return pkgNm;
	}
	public void setPkgNm(String pkgNm) {
		this.pkgNm = pkgNm;
	}
	public String getSorcNm() {
		return sorcNm;
	}
	public void setSorcNm(String sorcNm) {
		this.sorcNm = sorcNm;
	}
	public List<SvcFileFuncInfoVo> getFuncInfoVoList() {
		return funcInfoVoList;
	}
	public void setFuncInfoVoList(List<SvcFileFuncInfoVo> funcInfoVoList) {
		this.funcInfoVoList = funcInfoVoList;
	}

}
