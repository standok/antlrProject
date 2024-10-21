package com.domain;

public class DataMapDefinitionVo {

	private String oldTableId;			// (��)�������̺�ID-�빮��	
	private String oldTableName;		// [1](��)�ѱ����̺��
	private String oldColumnId;			// [2](��)�����ʵ�ID-�빮��
	private String oldColumnName;		// [3](��)�ѱ��ʵ��
	private String oldDataType;			// [4](��)�����ͼӼ�
	private String oldDataLength;		// [5](��)�����ͱ���

	private String newTableId;			// (��)�������̺�ID-�빮��
	private String newTableName;		// [6](��)�ѱ����̺��
	private String newColumnId;			// [7](��)�����ʵ�ID-�빮��
	private String newColumnName;		// [8](��)�ѱ��ʵ��
	private String newDataType;			// [9](��)�����ͼӼ�
	private String newDataLength;		// [10](��)�����ͱ���

	private boolean isConvert = false;	// [11]��ȯ����
	private String convertRule = "";	// [12]��ȯ ��Ģ
	
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
