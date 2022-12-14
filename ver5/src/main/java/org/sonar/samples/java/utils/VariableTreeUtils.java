package org.sonar.samples.java.utils;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.VariableTree;

public class VariableTreeUtils {
	private static JavaFileScannerContext context;

	private static String getLine(VariableTree tree) {
		int firstLine = tree.firstToken().line() - 1;
		int lastLine = tree.lastToken().line() - 1;
		int firstCol = tree.firstToken().column() - 1;
		int lastCol = tree.lastToken().column();
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

//	private static String clearLine(VariableTree tree, String str) {
//		str = str.replaceAll("\\s+", "").replaceFirst(";*" + tree.firstToken().text(), "");
//		int startIndex = str.indexOf("," + tree.simpleName().name());
////		return startIndex == -1 ? str : str.substring(startIndex + 1);
//		return str.substring(startIndex == -1 ? 2 : startIndex + 3);
//	}

	private static String clearLine(VariableTree tree, String str) {
		str = str.replaceAll("\\s+", "").replaceFirst(";*" + tree.firstToken().text() + tree.simpleName().name(), "");
//		int startIndex = str.indexOf("," + tree.simpleName().name());
//		return startIndex == -1 ? str : str.substring(startIndex + 1);
		return str.substring(1);
	}

	public static String getValue(JavaFileScannerContext javaFileScannerContext, VariableTree tree) {
		context = javaFileScannerContext;
		if (tree.initializer() != null) {
//			tree.initializer().symbolType().name()
//			String x = getLine(tree);
			return getLine(tree);
//			return (String) tree.initializer().asConstant().orElse(getLine(tree));
		}
		return "return_null";
	}

	public static boolean hasValue(VariableTree tree) {
		if (tree.type().symbolType().fullyQualifiedName().endsWith("Exception"))
			return true;
		return tree.initializer() != null && !("<nulltype>".equals(tree.initializer().symbolType().name()));
	}
}