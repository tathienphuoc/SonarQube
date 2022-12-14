package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.IfStatementTree;

public class IfStatementTreeUtilsOld {
	private static JavaFileScannerContext context;
	public static List<String> IfConditions;

	public static void init(JavaFileScannerContext javaFileScannerContext) {
		context = javaFileScannerContext;
		IfConditions = new ArrayList<>();
	}

	public static void clear() {
		IfConditions.clear();
	}

	public static void add(IfStatementTree tree) {
		IfConditions.add(getLine(tree));
	}

	public static String getLine(IfStatementTree tree) {

		int firstLine = tree.openParenToken().line() - 1;
		int lastLine = tree.closeParenToken().line() - 1;
		int firstCol = tree.openParenToken().column();
		int lastCol = tree.closeParenToken().column() + 1;

		String line;
		if (firstLine == lastLine) {
			line = context.getFileLines().get(firstLine).substring(firstCol, lastCol);
		} else {
			StringBuilder str = new StringBuilder(context.getFileLines().get(firstLine).substring(firstCol));
			firstLine++;
			while (firstLine < lastLine) {
				str.append(context.getFileLines().get(firstLine));
				firstLine++;
			}
			line = str.append(context.getFileLines().get(lastLine).subSequence(0, lastCol)).toString();
		}
		return line.replaceAll("\\s+", "");
	}

	public static boolean hanleNullPointerException(String variableName) {
		Pattern firstPattern = Pattern.compile("[|(&]" + variableName + "[=!]=null[|)&]");
		Pattern secondPattern = Pattern.compile("[|(&]null[=!]=" + variableName + "[|)&]");
		return IfConditions.stream()
				.anyMatch(el -> firstPattern.matcher(el).find() || secondPattern.matcher(el).find());
	}
}