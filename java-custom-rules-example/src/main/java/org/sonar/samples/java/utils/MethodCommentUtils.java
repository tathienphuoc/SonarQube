package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.Tree;

public class MethodCommentUtils {

	private enum Type {
		DESCRIPTION(Pattern.compile("^\\*\\s+[^@][a-zA-Z0-9]+.*$")),
		PARAM(Pattern.compile("^\\* @param(\\s+[a-zA-Z]+){2}\\s*$")),
		INVALID_PARAM(Pattern.compile("\\*\\s*@param(\\s+[a-zA-Z]+){2,}\\s*$")),
		RETURN(Pattern.compile("^\\* @return\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_RETURN(Pattern.compile("\\*\\s*(@return).*$")),
		EXCEPTION(Pattern.compile("^\\* (@throws|@exception)\\s+[a-zA-Z]+\\s*$")),
		INVALID_EXCEPTION(Pattern.compile("\\*\\s*(@throws|@exception).*$")), EMPTY_LINE(Pattern.compile("^\\*\\s*$")),
		INVALID_LINE(Pattern.compile("^[^\\*].*"));

		private Pattern regex;

		private String errMsg = "";

		static {
			INVALID_PARAM.errMsg = "INVALID_PARAM";
			INVALID_RETURN.errMsg = "INVALID_RETURN";
			INVALID_EXCEPTION.errMsg = "INVALID_EXCEPTION";
			INVALID_LINE.errMsg = "INVALID_LINE";
			EMPTY_LINE.errMsg = "INVALID EMPTY_LINE";
		}

		public String getErrMsg(int lineNo) {
			return "Line " + lineNo + ": " + this.errMsg;
		}

		Type(Pattern s) {
			this.regex = s;
		}

		public Boolean matches(String text) {
			return regex.matcher(text).find();
		}

		public static Type getType(String line) {
			for (Type t : values()) {
				if (t.matches(line)) {
					return t;
				}
			}
			return DESCRIPTION;
		}
	}

	private static HashMap<Type, List<String>> methodComments = new HashMap<>();

	private static void initMethodComments() {
		for (Type t : Type.values()) {
			methodComments.put(t, new ArrayList<String>());
		}
	}

	private static boolean allowEmptyLine() {
		return methodComments.get(Type.PARAM).isEmpty() && methodComments.get(Type.RETURN).isEmpty()
				&& methodComments.get(Type.EXCEPTION).isEmpty();
	}

	private static String getMethodComments(MethodTree tree) throws Exception {
		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
		if (methodComments.isEmpty()) {
			return "";
		} else if (methodComments.size() > 1) {
			throw new Exception("Too many method comment");
		} else {
			String methodComment = methodComments.get(0).comment();
			if (methodComment.startsWith("/**") && methodComment.endsWith("*/")) {
				return methodComment;
			}
			throw new Exception("Method comment must be start with //** and end with *//");
		}
	}

	private static boolean isOrderedLine(Type type) {
		switch (type) {
		case DESCRIPTION:
			return allowEmptyLine();
		case PARAM:
			return methodComments.get(Type.RETURN).isEmpty() && methodComments.get(Type.EXCEPTION).isEmpty();
		case RETURN:
			return methodComments.get(Type.EXCEPTION).isEmpty();
		default:
			return true;
		}
	}

	public static String getFormatErrMsg(Tree tree) {
		initMethodComments();
		List<String> commentLines;
		try {
			commentLines = cleanLines(getMethodComments((MethodTree) tree));
		} catch (Exception e) {
			return e.getMessage();
		}
		StringBuilder errMsg = new StringBuilder();
		if (commentLines.isEmpty()) {
			errMsg.append("Method comments doesn't exist");
			return errMsg.toString();
		}
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			methodComments.get(type).add(commentLines.get(i));
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine()) {
					errMsg.append("\n" + type.getErrMsg(i + 1) + commentLines.get(i) + "\n");
				}
			} else {
				if (!type.errMsg.isEmpty()) {
					errMsg.append("\n" + type.getErrMsg(i + 1) + commentLines.get(i) + "\n");
				}
				if (!isOrderedLine(type)) {
					errMsg.append("\nLine " + (i + 1) + commentLines.get(i) + " invalid order\n");
				}
			}
		}
		if (methodComments.get(Type.DESCRIPTION).isEmpty()) {
			errMsg.insert(0, "Where is my des\n");
		}
		return errMsg.toString();
	}

	public static String getDocErrMsg(Tree tree) {
		String formatErrMsg = getFormatErrMsg(tree);
		if (!formatErrMsg.isEmpty()) {
			return formatErrMsg;
		}
		MethodTree method = (MethodTree) tree;
		StringBuilder errMsg = new StringBuilder();
		if (methodComments.get(Type.EMPTY_LINE).isEmpty()) {
			errMsg.append("Need at least one empty line after des");
		}
		if (!validParam(method)) {
			errMsg.append(Type.INVALID_PARAM.errMsg + "\n");
		}
		if (!validReturn(method)) {
			errMsg.append(Type.INVALID_RETURN.errMsg + "\n");
		}
		if (!validExcept(method)) {
			errMsg.append(Type.INVALID_EXCEPTION.errMsg + "\n");
		}
		return errMsg.toString();
	}

	public static boolean validParam(MethodTree tree) {
		Symbol.MethodSymbol symbol = tree.symbol();
		List<String> params = methodComments.get(Type.PARAM);
		if (params.size() != tree.parameters().size()) {
			return false;
		}
		String type = "";
		String name = "";
		for (int i = 0; i < params.size(); i++) {

			type = symbol.parameterTypes().get(i).fullyQualifiedName();
			name = tree.parameters().get(i).simpleName().name();
			String[] parts = params.get(i).split("\\s+");
			if (!parts[3].equals(name) || !parts[2].equals(type)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validReturn(MethodTree tree) {
		org.sonar.plugins.java.api.semantic.Type rType = tree.returnType().symbolType();
		if (methodComments.get(Type.RETURN).isEmpty()) {
			return rType.isVoid();
		} else if (methodComments.get(Type.RETURN).size() > 1) {
			return false;
		}
		List<org.sonar.plugins.java.api.semantic.Type> typeArguments = rType.typeArguments();

		String[] parts = methodComments.get(Type.RETURN).get(0).split("\\s+");

		List<String> returnTypes = Arrays.asList(parts[2].replaceAll("\\s+", "").split("[<>,]"));
		if (rType.isVoid() && returnTypes.size() == 1 && "Void".equalsIgnoreCase(returnTypes.get(0))) {
			return true;
		} else {
			if (returnTypes.isEmpty() || !rType.fullyQualifiedName().contains(returnTypes.get(0))
					|| returnTypes.size() != typeArguments.size() + 1) {// ??
				return false;
			}
			for (int i = 0; i < typeArguments.size(); i++) {
				if (!typeArguments.get(i).fullyQualifiedName().equals(returnTypes.get(i + 1))) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean validExcept(MethodTree tree) {
		List<String> throwsTypes = tree.throwsClauses().stream().map(el -> ((IdentifierTree) el).name())
				.collect(Collectors.toList());
		List<String> exceptionTypes = methodComments.get(Type.EXCEPTION).stream()
				.map(ex -> ex.replaceAll("\\* @exception|\\* @throws", "").trim()).collect(Collectors.toList());
		if (exceptionTypes.size() != throwsTypes.size()) {
			return false;
		}
		for (int i = 0; i < throwsTypes.size(); i++) {
			if (!exceptionTypes.get(i).equals(throwsTypes.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static List<String> cleanLines(@Nullable String javadoc) {
		if (javadoc == null) {
			return Collections.emptyList();
		}
		String trimmedJavadoc = javadoc.trim();
		if (trimmedJavadoc.length() <= 4) {
			// Empty or malformed javadoc. for instance: '/**/'
			return Collections.emptyList();
		}
		// remove start and end of Javadoc as well as stars
		String[] lines = trimmedJavadoc.substring(3, trimmedJavadoc.length() - 2).replaceAll("(?m)^\\s*", "").trim()
				.split("\\r?\\n");
		return Arrays.stream(lines).map(String::trim).collect(Collectors.toList());
	}

}