package com.util;

import java.nio.charset.Charset;

public class ConverterUtil {

	public static String rightPad(String str, int size, char padChar) {
		return padString(str, size, String.valueOf(padChar), false);
	}

	public static String rightBytesPad(String str, int size) {
		return rightBytesPad(str, size, ' ');
	}

	public static String rightBytesPad(String str, int size, char padChar) {
		return padBytesString(str, size, String.valueOf(padChar), false);
	}

	/**
	 * 설명 : 문자열 앞뒤에 특정문자를 Padding한 문자열을 반환한다.
	 */
	public static String padString(String str, int size, String padStr, boolean isLeft) {

		String inputString = str;
		String padString = padStr;

		if(inputString == null) {
			return null;
		}
		int originalStrLength = inputString.length();

		if(size < originalStrLength) {
			return inputString;
		}

		int difference = size - originalStrLength;

		if (difference > 0) {
			if (padString == null || "".equals(padString)) {
				padString = " ";
			}

			String tempPad = null;
			int padStrLen = padString.length();
			int inputLen = inputString.length();

			if (padStrLen == 1) {

				char padChar = padString.charAt(0);
				char[] tempChars = new char[difference];

				for (int i = 0; i < tempChars.length; i++) {
					tempChars[i] = padChar;
				}

				tempPad = new String(tempChars);

			} else {

				StringBuilder tempBuilder = new StringBuilder(difference);

				do {
					for(int j = 0; j < padStrLen; j++) {
						tempBuilder.append(padString.charAt(j));
						if(inputLen + tempBuilder.length() >= size) {
							break;
						}
					}
				} while (difference > tempBuilder.length());

				tempPad = tempBuilder.toString();
			}

			if (isLeft) {
				inputString = tempPad + inputString;
			} else {
				inputString = inputString + tempPad;
			}
		}

		return inputString;
	}

	/**
	 * 설명 : 문자열 앞뒤에 특정문자를 Padding한 문자열을 반환한다.
	 */
	public static String padBytesString(String str, int size, String padStr, boolean isLeft) {

		String inputString = str;
		String padString = padStr;

		if(inputString == null) {
			return null;
		}
		int originalStrLength = 0;
		String charsetName = Charset.defaultCharset().displayName();// default charset of this Java virtual machine

		for (int i = 0; i < inputString.length(); i++) {
			originalStrLength += getByteLength(inputString.charAt(i), charsetName);
		}

		if(size < originalStrLength) {
			return inputString;
		}

		int difference = size - originalStrLength;

		if (difference > 0) {
			if (padString == null || "".equals(padString)) {
				padString = " ";
			}

			String tempPad = null;
			int padStrLen = 0;

			for (int i=0; i < padString.length(); i++) {
				padStrLen += getByteLength(padString.charAt(i), charsetName);
			}

			if (padStrLen == 1) {

				char padChar = padString.charAt(0);
				char[] tempChars = new char[difference];

				for (int i = 0; i < tempChars.length; i++) {
					tempChars[i] = padChar;
				}

				tempPad = new String(tempChars);

			} else {

				StringBuilder tempBuilder = new StringBuilder(difference);
				int len = 0;

				do {
					for(int i = 0; i < padStrLen; i++) {
						len = getByteLength(padString.charAt(i), charsetName);

						if(len <= difference) {
							difference -= len;
							tempBuilder.append(padString.charAt(i));
						} else break;
					}
				} while (difference > tempBuilder.length());

				tempPad = tempBuilder.toString();
			}

			if (isLeft) {
				inputString = tempPad + inputString;
			} else {
				inputString = inputString + tempPad;
			}
		}

		return inputString;
	}

	public static int getByteLength(char ch, String charset) {
		return String.valueOf(ch).getBytes(Charset.forName(charset)).length;
	}

}
