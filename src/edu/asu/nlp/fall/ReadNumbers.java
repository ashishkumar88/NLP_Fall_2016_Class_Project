package edu.asu.nlp.fall;
import java.io.*;
import java.util.*;

public class ReadNumbers {
	private static String EMPTY_STRING = "";
	private static String PARAGRAPH_SEPARATOR = "---";
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream("LDC2003T05" + File.separator + "test.txt")));
		String line = EMPTY_STRING;
		int docIndex = 0;
		int lineIndex = 0;
		while ((line = br.readLine()) != null) {
			if (line.indexOf(PARAGRAPH_SEPARATOR) != -1) {
				docIndex++;
				System.out.println(docIndex + ","+lineIndex+",0");
				lineIndex = 0;
			} else {
				lineIndex++;
			}
		}
	}
}