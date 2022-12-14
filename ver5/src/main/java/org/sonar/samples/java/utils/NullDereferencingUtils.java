package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.TryStatementTree;

public class NullDereferencingUtils {
	private static JavaFileScannerContext context;
//	private static HashMap<String, Type> variables;
	private static Set<String> variables;
	private static List<List<Integer>> tryCatches;
	private static Set<String> arguments;
	private static boolean throwException;
	private static String prefix = "[R4J][Null dereferencing][%s]";
	private static List<String> errMsgs;

	public enum Type {
//		clear set var, type
//		thing.callMethod()
//		just care about "thing"
//		Type : value (constant: "", new Object, int, ... things we know it's value), 
		// null (or value has null value, don't have value),
		// method
		VALUE, NULL, METHOD, IGNORE, VARIABLE;

	}

	public static void init(JavaFileScannerContext javaFileScannerContext) {
		context = javaFileScannerContext;
		variables = new HashSet<>();
		arguments = new HashSet<>();
		tryCatches = new ArrayList<>();
		errMsgs = new ArrayList<>();
		throwException = false;
	}

	public static void enableThrowException() {
		throwException = true;
	}

	public static boolean handleNullPointerException() {
		return throwException;
	}

	public static Type getType(String str) {
		if (str.contains("(")) {
			return Type.METHOD;
		} else if (str.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
			return Type.VARIABLE;
		}
		return Type.IGNORE;
	}

	public static void clear() {
		variables.clear();
		arguments.clear();
		tryCatches.clear();
		IfStatementTreeUtils.clear();
		throwException = false;
	}

	public static void addTryCatche(TryStatementTree tree) {
		tryCatches.add(Arrays.asList(tree.firstToken().line(), tree.firstToken().column(), tree.lastToken().line(),
				tree.lastToken().column()));
	}

	public static boolean hasTryCatch(MethodInvocationTree tree) {
		return tryCatches.stream().anyMatch(t -> {
			int firstLine = tree.firstToken().line();
			int firstCol = tree.firstToken().column();
			int lastLine = tree.lastToken().line();
			int lastCol = tree.lastToken().column();
			return (firstLine > t.get(0) && lastLine < t.get(2)) || (firstLine == t.get(0) && firstCol > t.get(1))
					|| (lastLine == t.get(2) && lastCol < t.get(1));
		});
	}

	public static void addVariable(String name) {
		arguments.remove(name);
		variables.add(name);
	}

	public static void removeVariable(String name) {
		variables.remove(name);
	}

	public static void addArgument(String name) {
		arguments.add(name);
	}

	public static void removeArgument(String name) {
		arguments.remove(name);
	}

	public static boolean containArgument(String name) {
		return arguments.contains(name);
	}

	public static void check(MethodInvocationTree tree) {
		errMsgs.clear();
		String str = getLine(tree, true);
		String text = getLine(tree, false);
		if (handleNullPointerException() || hasTryCatch(tree) /*|| IfStatementTreeUtils.hanleNullPointerException(str)*/) {
			return;
		}
		try {
			if (arguments.contains(str)) {
				errMsgs.add(text + " " + String.format(CommonMessage.MAYBE_NULL, str));
			} else if (str.matches("[a-zA-Z][a-zA-Z0-9_]*") && !variables.contains(str)) {
				errMsgs.add(text + " " + String.format(CommonMessage.AVOID_NULL_POINTER, str));
			}
		} catch (Exception e) {
			e.printStackTrace();
			errMsgs.add(text + "error:" + e.getMessage());
		}
	}

	private static String getLine(MethodInvocationTree tree, boolean flag) {
		int firstLine = tree.firstToken().line() - 1;
		int lastLine = tree.lastToken().line() - 1;
		int firstCol = tree.firstToken().column();
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
		return flag ? cleanLine(line.replaceAll("\\s+", "")) : line.replaceAll("\\s+", "");
	}

	private static String cleanLine(String str) {
		Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\(.*\\)){0,1}\\.[a-zA-Z][a-zA-Z0-9_]*");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return str.substring(0, str.lastIndexOf(".", matcher.end()));
		}
		return "";
	}

	public static List<String> getErrMsg() {
		return errMsgs.stream().map(errMsg -> String.format(prefix, errMsg)).collect(Collectors.toList());
	}
}
