package com.svc;

import java.io.IOException;

public interface ICreateFileSvc {

	public void createJavaFile(String filePath, StringBuilder str) throws IOException;

	public void createSqlFile(String filePath, StringBuilder str) throws IOException;
}
