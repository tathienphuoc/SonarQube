package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.IfStatementTree;

public class IfStatementTreeUtils {
	private static JavaFileScannerContext context;
	public static List<String> IfConditions;
	public static List<ArrayList<Integer>> positions;

	public static void init(JavaFileScannerContext javaFileScannerContext) {
		context = javaFileScannerContext;
		IfConditions = new ArrayList<>();
		positions = new ArrayList<>();
	}

	public static void clear() {
		IfConditions.clear();
		positions.clear();
	}

	public static void add(IfStatementTree tree) {
		IfConditions.add(getLine(tree));
		positions.add(new ArrayList<Integer>(Arrays.asList(tree.closeParenToken().line(),
				tree.closeParenToken().column(), tree.lastToken().line(), tree.lastToken().column())));
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

//	public static boolean hanleNullPointerException(String variableName) {
//		Pattern firstPattern = Pattern.compile("[|(&]" + variableName + "[=!]=null[|)&]");
//		Pattern secondPattern = Pattern.compile("[|(&]null[=!]=" + variableName + "[|)&]");
//		return IfConditions.stream()
//				.anyMatch(el -> firstPattern.matcher(el).find() || secondPattern.matcher(el).find());
//	}

	public static boolean hanleNullPointerException(String variableName, int line, int column) {
		Pattern firstPattern = Pattern.compile("[|(&]" + variableName + "[=!]=null[|)&]");
		Pattern secondPattern = Pattern.compile("[|(&]null[=!]=" + variableName + "[|)&]");
//		return IfConditions.stream()
//				.anyMatch(el -> firstPattern.matcher(el).find() || secondPattern.matcher(el).find());
		for (int i = 0; i < IfConditions.size(); i++) {
			String condition = IfConditions.get(i);
			ArrayList<Integer> position = positions.get(i);
			if (line == 35) {
				int a = 10;
			}
			if ((firstPattern.matcher(condition).find() || secondPattern.matcher(condition).find())
					&& ((line == position.get(0) && column > position.get(1))
							|| (line == position.get(2) && column < position.get(3))
							|| (line > position.get(0) && line < position.get(2)))) {
				return true;
			}
		}
		return false;
	}
}