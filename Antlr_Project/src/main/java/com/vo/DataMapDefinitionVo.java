package com.vo;

public class DataMapDefinitionVo {

	private String oldTableId;			// (구)영문테이블ID-대문자	
	private String oldTableName;		// [1](구)한글테이블명
	private String oldColumnId;			// [2](구)영문필드ID-대문자
	private String oldColumnName;		// [3](구)한글필드명
	private String oldDataType;			// [4](구)데이터속성
	private String oldDataLength;		// [5](구)데이터길이

	private String newTableId;			// (신)영문테이블ID-대문자
	private String newTableName;		// [6](신)한글테이블명
	private String newColumnId;			// [7](신)영문필드ID-대문자
	private String newColumnName;		// [8](신)한글필드명
	private String newDataType;			// [9](신)데이터속성
	private String newDataLength;		// [10](신)데이터길이

	private boolean isConvert = false;	// [11]전환여부
	private String convertRule = "";	// [12]변환 규칙
	
	public String getOldTableId() {
		return oldTableId;
	}
	public void setOldTableId(String oldTableId) {
		this.oldTableId = oldTableId;
	}
	public String getOldTableName() {
		return oldTableName;
	}
	public void setOldTableName(String oldTableName) {
		this.oldTableName = oldTableName;
	}
	public String getOldColumnId() {
		return oldColumnId;
	}
	public void setOldColumnId(String oldColumnId) {
		this.oldColumnId = oldColumnId;
	}
	public String getOldColumnName() {
		return oldColumnName;
	}
	public void setOldColumnName(String oldColumnName) {
		this.oldColumnName = oldColumnName;
	}
	public String getOldDataType() {
		return oldDataType;
	}
	public void setOldDataType(String oldDataType) {
		this.oldDataType = oldDataType;
	}
	public String getOldDataLength() {
		return oldDataLength;
	}
	public void setOldDataLength(String oldDataLength) {
		this.oldDataLength = oldDataLength;
	}
	public String getNewTableId() {
		return newTableId;
	}
	public void setNewTableId(String newTableId) {
		this.newTableId = newTableId;
	}
	public String getNewTableName() {
		return newTableName;
	}
	public void setNewTableName(String newTableName) {
		this.newTableName = newTableName;
	}
	public String getNewColumnId() {
		return newColumnId;
	}
	public void setNewColumnId(String newColumnId) {
		this.newColumnId = newColumnId;
	}
	public String getNewColumnName() {
		return newColumnName;
	}
	public void setNewColumnName(String newColumnName) {
		this.newColumnName = newColumnName;
	}
	public String getNewDataType() {
		return newDataType;
	}
	public void setNewDataType(String newDataType) {
		this.newDataType = newDataType;
	}
	public String getNewDataLength() {
		return newDataLength;
	}
	public void setNewDataLength(String newDataLength) {
		this.newDataLength = newDataLength;
	}
	public boolean isConvert() {
		return isConvert;
	}
	public void setConvert(boolean isConvert) {
		this.isConvert = isConvert;
	}
	public String getConvertRule() {
		return convertRule;
	}
	public void setConvertRule(String convertRule) {
		this.convertRule = convertRule;
	}
}
