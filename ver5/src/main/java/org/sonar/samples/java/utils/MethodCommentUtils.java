package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;

public class MethodCommentUtils {
	private static JavaFileScannerContext context;
	private static List<String> methodComments;
	private static EnumMap<Type, ArrayList<String>> commentLineTypes;
	private static HashMap<String, Integer> errMsgs;
	private static String declareMethod;
	private static MethodTree tree;
	private static String methodName;
	private static String prefix;

	public enum Type {
		DESCRIPTION, EMPTY_LINE, PARAM, RETURN, EXCEPTION, OTHER, EMPTY_LINE_WITHOUT_ASTERISK, INVALID_LINE_FORMAT,
		INVALID_PARAM_FORMAT, INVALID_DESCRIPTION_FORMAT, INVALID_RETURN_FORMAT, INVALID_EXCEPTION_FORMAT;

		String errMsg = "";
		private Pattern regex;

		static {
			EMPTY_LINE.errMsg = CommonMessage.REMOVE_EMPTY_LINE;
			EMPTY_LINE_WITHOUT_ASTERISK.errMsg = CommonMessage.REMOVE_EMPTY_LINE_WITHOUT_ASTERISK;
			INVALID_LINE_FORMAT.errMsg = CommonMessage.INVALID_LINE_FORMAT;
			INVALID_DESCRIPTION_FORMAT.errMsg = CommonMessage.INVALID_DESCRIPTION_FORMAT;
			INVALID_PARAM_FORMAT.errMsg = CommonMessage.INVALID_PARAM_FORMAT;
			INVALID_RETURN_FORMAT.errMsg = CommonMessage.INVALID_RETURN_FORMAT;
			INVALID_EXCEPTION_FORMAT.errMsg = CommonMessage.INVALID_EXCEPTION_FORMAT;

			DESCRIPTION.regex = Pattern.compile("^\\*\\s+[^@\\s]+.*$");
			INVALID_LINE_FORMAT.regex = Pattern.compile("^(?!\\* \\S+).*$");
		}

	}

	public static boolean match(Type type, String line) {
		return type.regex.matcher(line).find();
	}

	public static Type getType(String line) {// line==null is necessary?
		if (line.isEmpty()) {// line==null is necessary?
			return Type.EMPTY_LINE_WITHOUT_ASTERISK;
		} else if (line.equals("*")) {// *
			return Type.EMPTY_LINE;
		} else if (match(Type.DESCRIPTION, line)) {
			return line.endsWith("<br>") ? Type.DESCRIPTION : Type.INVALID_DESCRIPTION_FORMAT;
		}
		if (match(Type.INVALID_LINE_FORMAT, line)) {// invalid line fomrat
			handleInvalidLineFormat(line);
			return Type.INVALID_LINE_FORMAT;
		} else {
			return getLineFormat(line);
		}
	}

	public static void handleInvalidLineFormat(String line) {
		String annotation = getAnnotation(line);
		switch (annotation) {
		case "@param":
			commentLineTypes.get(Type.PARAM).add(line);
			break;
		case "@return":
			commentLineTypes.get(Type.RETURN).add(line);
			break;
		case "@exception":
		case "@throws":
			commentLineTypes.get(Type.EXCEPTION).add(line);
			break;
		default:
			if (!annotation.startsWith("@")) {
				commentLineTypes.get(Type.DESCRIPTION).add(line);
			}
			break;
		}
	}

	public static Type getLineFormat(String line) {
		String annotation = getAnnotation(line);
		switch (annotation) {
		case "@param":
			return validParamFormat(line) ? Type.PARAM : Type.INVALID_PARAM_FORMAT;
		case "@return":
			return validReturnFormat(line) ? Type.RETURN : Type.INVALID_RETURN_FORMAT;
		case "@exception":
		case "@throws":
			return validExceptFormat(line) ? Type.EXCEPTION : Type.INVALID_EXCEPTION_FORMAT;
		default:
			return Type.OTHER;
		}
	}

	public static boolean validParamFormat(String line) {
		line = line.substring(8).replaceAll("\\s+", " ").trim();
		int lastIndex = line.lastIndexOf(" ");
		if (line.isEmpty() || lastIndex == -1) {
			return false;
		}
		line = line.substring(0, lastIndex - 1);
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ' && line.charAt(i - 1) != ',') {
				return false;
			}
		}
		return true;
	}

	public static boolean validReturnFormat(String line) {
		line = line.substring(9).replaceAll("\\s+", " ").trim();
		if (line.isEmpty()) {
			return false;
		}
		if (!line.contains(" ")) {// primary type
			return true;
		}
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ' && line.charAt(i - 1) != ',') {
				return false;
			}
		}
		return true;
	}

	public static boolean validExceptFormat(String line) {
		return line.split("\\s+").length == 3;
	}

	public static String getAnnotation(String line) {
		line = line.charAt(0) == '*' ? line.substring(1).trim() : line;
		int endIndex = line.indexOf(" ");
		return line.substring(0, endIndex != -1 ? endIndex : line.length());
	}

	private static void initMethodComments(JavaFileScannerContext javaFileScannerContext, MethodTree methodTree)
			throws Exception {
		context = javaFileScannerContext;
		tree = methodTree;

		errMsgs = new HashMap<>();
		declareMethod = getDeclare();

		methodName = tree.symbol().declaration().simpleName().name();
		prefix = String.format("[R4J][Method comment][%s]", methodName);

		methodComments = getMethodComments();
		commentLineTypes = getCommentLineTypes();

	}

	public static String getLine(int lineNo) {
		return context.getFileLines().get(lineNo - 1);
	}

	public static String getDeclare() {
		int lineNo = tree.modifiers().lastToken().line();
		StringBuilder sb = new StringBuilder(getLine(lineNo));
		while (sb.indexOf("{") == -1 && sb.indexOf(";") == -1) {
			sb.append(getLine(++lineNo));
		}
		return sb.toString().replaceAll("\\s+|@[^) ]*(\\)| )", "");
	}

//	private static List<String> getMethodComments() throws Exception {
//		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
//		if (methodComments.isEmpty()) {
//			throw new Exception(CommonMessage.ABSENT_METHOD_COMMENT);
//		} else if (methodComments.size() > 1) {
//			throw new Exception(CommonMessage.TOO_MANY_METHOD_COMMENTS);
//		} else {
//			String methodComment = methodComments.get(0).comment();
//
//			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
//				String[] lines = methodComment.substring(3, methodComment.length() - 2).trim().split("\\r?\\n");
//				return Arrays.stream(lines).map(String::trim).collect(Collectors.toList());
//			}
//			throw new Exception(CommonMessage.INVALID_COMMENT_FORMAT);
//		}
//	}
	
	private static List<String> getMethodComments() throws Exception {
		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
		if (methodComments.isEmpty()) {
			throw new Exception(CommonMessage.ABSENT_METHOD_COMMENT);
		} else {
			String methodComment = methodComments.get(methodComments.size()-1).comment();

			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
				String[] lines = methodComment.substring(3, methodComment.length() - 2).trim().split("\\r?\\n");
				return Arrays.stream(lines).map(String::trim).collect(Collectors.toList());
			}
			throw new Exception(CommonMessage.INVALID_COMMENT_FORMAT);
		}
	}

	public static EnumMap<Type, ArrayList<String>> getCommentLineTypes() {
		EnumMap<Type, ArrayList<String>> lineTypes = new EnumMap<>(Type.class);
		for (Type t : Type.values()) {
			lineTypes.put(t, new ArrayList<>());
		}
		int firstAnotationIndex = -1;
		for (int i = 0; i < methodComments.size(); i++) {
			String line = methodComments.get(i);
			Type type = getType(line);
			lineTypes.get(type).add(line);
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine(lineTypes)) {
					errMsgs.put(type.errMsg, errMsgs.getOrDefault(type.errMsg, 0) + 1);
				}
			} else {
				if (!type.equals(Type.DESCRIPTION) && !type.equals(Type.INVALID_DESCRIPTION_FORMAT)
						&& firstAnotationIndex == -1) {
					firstAnotationIndex = i;
				}
				if (!type.errMsg.isEmpty()) {
					errMsgs.put(type.errMsg, errMsgs.getOrDefault(type.errMsg, 0) + 1);
				}

			}
			if (!isOrderedLine(type, lineTypes)) {
				errMsgs.put(CommonMessage.METHOD_NOT_SORTED,
						errMsgs.getOrDefault(CommonMessage.METHOD_NOT_SORTED, 0) + 1);
			}
		}
		if (firstAnotationIndex > 0 && !getType(methodComments.get(firstAnotationIndex - 1)).equals(Type.EMPTY_LINE)) {
			errMsgs.put(CommonMessage.ABSENT_EMPTY_LINE, 1);
		}
		return lineTypes;
	}

	private static boolean isOrderedLine(Type type, EnumMap<Type, ArrayList<String>> lineTypes) {
		switch (type) {
		case DESCRIPTION:
		case INVALID_DESCRIPTION_FORMAT:
			return allowEmptyLine(lineTypes);
		case PARAM:
		case INVALID_PARAM_FORMAT:
			return lineTypes.get(Type.RETURN).isEmpty() && lineTypes.get(Type.EXCEPTION).isEmpty();
		case RETURN:
		case INVALID_RETURN_FORMAT:
			return lineTypes.get(Type.EXCEPTION).isEmpty();
		default:
			return true;
		}
	}

	private static boolean allowEmptyLine(EnumMap<Type, ArrayList<String>> lineTypes) {
		return lineTypes.get(Type.PARAM).isEmpty() && lineTypes.get(Type.RETURN).isEmpty()
				&& lineTypes.get(Type.EXCEPTION).isEmpty();
	}

	public static void check(JavaFileScannerContext javaFileScannerContext, MethodTree methodTree) {
		try {
			initMethodComments(javaFileScannerContext, methodTree);
			String desErrMsg = commentLineTypes.get(Type.INVALID_DESCRIPTION_FORMAT).isEmpty()
					? validSection(Type.DESCRIPTION)
					: "";
			String paramErrMsg = commentLineTypes.get(Type.INVALID_PARAM_FORMAT).isEmpty() ? validSection(Type.PARAM)
					: "";
			String returnErrMsg = commentLineTypes.get(Type.INVALID_RETURN_FORMAT).isEmpty() ? validSection(Type.RETURN)
					: "";
			String exceptErrMsg = commentLineTypes.get(Type.INVALID_EXCEPTION_FORMAT).isEmpty()
					? validSection(Type.EXCEPTION)
					: "";

			if (!desErrMsg.isEmpty()) {
				errMsgs.put(desErrMsg, 1);
			}
			if (!paramErrMsg.isEmpty()) {
				errMsgs.put(paramErrMsg, 1);
			}
			if (!returnErrMsg.isEmpty()) {
				errMsgs.put(returnErrMsg, 1);
			}
			if (!exceptErrMsg.isEmpty()) {
				errMsgs.put(exceptErrMsg, 1);
			}

		} catch (Exception e) {
			errMsgs.put(e.getMessage(), 1);
//			e.printStackTrace();
		}

	}

	public static String validDesSection() {
		return commentLineTypes.get(Type.DESCRIPTION).isEmpty()
				&& commentLineTypes.get(Type.INVALID_DESCRIPTION_FORMAT).isEmpty() ? CommonMessage.ABSENT_DESCRIPTION
						: "";
	}

	public static String validParamSection() {
		int commentCount = commentLineTypes.get(Type.PARAM).size()
				+ commentLineTypes.get(Type.INVALID_PARAM_FORMAT).size();
		int declareCount = tree.parameters().size();
		if (commentCount == declareCount) {
			return validParamComments() ? "" : CommonMessage.INVALID_PARAM;
		} else if (commentCount > declareCount) {
			return CommonMessage.TOO_MANY_PARAM;
		} else {
			return commentCount == 0 ? CommonMessage.ABSENT_PARAM : CommonMessage.LACK_OF_PARAM;
		}
	}

	public static String validReturnSection() {
		int commentCount = commentLineTypes.get(Type.RETURN).size()
				+ commentLineTypes.get(Type.INVALID_RETURN_FORMAT).size();
		if (tree.returnType() == null || commentCount > 1) {
			return commentCount == 0 ? "" : CommonMessage.TOO_MANY_RETURN;
		} else if (tree.returnType().symbolType().isVoid() && commentCount == 0) {
			return "";
		} else if (commentCount == 0) {
			return CommonMessage.ABSENT_RETURN;
		} else {// 1==1
			return validReturnComment() ? "" : CommonMessage.INVALID_RETURN;
		}
	}

	public static String validExceptSection() {
		int commentCount = commentLineTypes.get(Type.EXCEPTION).size()
				+ commentLineTypes.get(Type.INVALID_EXCEPTION_FORMAT).size();
		int declareCount = tree.throwsClauses().size();
		if (commentCount == declareCount) {
			return validExceptComments() ? "" : CommonMessage.INVALID_EXCEPTION;
		} else if (commentCount > declareCount) {
			return CommonMessage.TOO_MANY_EXCEPTION;
		} else {
			return commentCount == 0 ? CommonMessage.ABSENT_EXCEPTION : CommonMessage.LACK_OF_EXCEPTION;
		}
	}

	public static String validSection(Type type) {
		switch (type) {
		case DESCRIPTION:
			return validDesSection();
		case PARAM:
			return validParamSection();
		case RETURN:
			return validReturnSection();

		case EXCEPTION:
			return validExceptSection();
		default:
			return "";
		}
	}

	public static boolean validParamComments() {
		String comments = String.join(",", commentLineTypes.get(Type.PARAM).stream()
				.map(p -> p.substring(8).replaceAll("\\* @param|\\s+", "")).collect(Collectors.toList()));
		String declare = declareMethod.split("[()]")[1];
		return declare.equals(comments);
	}

	public static boolean validReturnComment() {
		if (commentLineTypes.get(Type.RETURN).isEmpty()) {
			return true;
		}
		String comment = commentLineTypes.get(Type.RETURN).get(0).replaceAll("\\* @return|\\s+", "") + methodName;
		String declare = declareMethod.split("[()]")[0];
		return declare.endsWith(comment);
	}

	public static boolean validExceptComments() {
		String comments = String.join(",", commentLineTypes.get(Type.EXCEPTION).stream()
				.map(p -> p.replaceAll("\\* @exception|\\* @throws|\\s+", "")).collect(Collectors.toList()));
		String declare = declareMethod.split("[()]")[2];
		return !declare.startsWith("throws") || declare.startsWith(comments, 6);
	}

	public static List<String> getErrMsg() {
		List<String> msgs = new ArrayList<>();
		for (Entry<String, Integer> entry : errMsgs.entrySet()) {
			if (entry.getValue() == 1) {
				msgs.add(String.format("%s[%s]", prefix, entry.getKey()));
			} else {
				msgs.add(String.format("%s[%s][+%d locations]", prefix, entry.getKey(), entry.getValue()));
			}
		}
		return msgs;
	}
}