package com.svc;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ITokenInfoSvc {
	void parsing(File file) throws IOException;
	boolean checkReservedWord(int SymbolNo, List<Integer> reserveWordList);
}
