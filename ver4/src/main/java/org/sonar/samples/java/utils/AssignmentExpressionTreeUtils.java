package org.sonar.samples.java.utils;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;

public class AssignmentExpressionTreeUtils {

	public static String getLine(JavaFileScannerContext context, AssignmentExpressionTree tree) {
		int firstLine = tree.firstToken().line() - 1;
		int lastLine = tree.lastToken().line() - 1;
		int firstCol = tree.firstToken().column() - 1;
		int lastCol = tree.lastToken().column() + tree.lastToken().text().length();
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
		return clearLine(tree, line);
	}

	private static String clearLine(AssignmentExpressionTree tree, String str) {
		return str.replaceAll("\\s+", "").replaceFirst(tree.variable().firstToken().text() + "=", "");
	}

}