package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;

public class MethodCommentUtils {

	private enum Type {
		INVALID_LINE(Pattern.compile("^[^\\*]|(\\*)\\S+")),
		DESCRIPTION(Pattern.compile("^\\*\\s+[^@][a-zA-Z0-9]+.*$")),
		PARAM(Pattern.compile("^\\* @param(\\s+\\S+){2}\\s*$")),
		INVALID_PARAM(Pattern.compile("\\*\\s*@param(\\s+\\S+)*\\s*$")),
		RETURN(Pattern.compile("^\\* @return\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_RETURN(Pattern.compile("\\*\\s*(@return).*$")),
		EXCEPTION(Pattern.compile("^\\* (@throws|@exception)\\s+[a-zA-Z]+\\s*$")),
		INVALID_EXCEPTION(Pattern.compile("\\*\\s*(@throws|@exception).*$")), EMPTY_LINE(Pattern.compile("^\\*\\s*$"));

//		INVALID_LINE(Pattern.compile("^[^\\*].*"));
		private Pattern regex;

		private String errMsg = "";

		static {
			INVALID_PARAM.errMsg = CommonMessage.INVALID_PARAM_FORMAT;
			INVALID_RETURN.errMsg = CommonMessage.INVALID_RETURN_FORMAT;
			INVALID_EXCEPTION.errMsg = CommonMessage.INVALID_EXCEPTION_FORMAT;
			INVALID_LINE.errMsg = CommonMessage.INVALID_LINE;
			EMPTY_LINE.errMsg = CommonMessage.REMOVE_EMPTY_LINE;
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

	private static EnumMap<Type, ArrayList<String>> methodComments = new EnumMap<>(Type.class);

	private static JavaFileScannerContext context;

	private static void initMethodComments() {
		for (Type t : Type.values()) {
			methodComments.put(t, new ArrayList<>());
		}
	}

	private static boolean allowEmptyLine() {
		return methodComments.get(Type.PARAM).isEmpty() && methodComments.get(Type.RETURN).isEmpty()
				&& methodComments.get(Type.EXCEPTION).isEmpty();
	}

	private static String getMethodComments(MethodTree tree) throws Exception {
		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
		if (methodComments.isEmpty()) {
			throw new Exception(CommonMessage.ABSENT_METHOD_COMMENTS);
		} else if (methodComments.size() > 1) {
			throw new Exception(CommonMessage.TOO_MANY_METHOD_COMMENTS);
		} else {
			String methodComment = methodComments.get(0).comment();
			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
				return methodComment;
			}
//			if (methodComment.startsWith("^\\/\\*\\*.*\\*\\/$")) {
//				return methodComment;
//			}
			throw new Exception(CommonMessage.INVALID_METHOD_COMMENTS_FORMAT);
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

	public static List<String> getFormatErrMsgs(MethodTree tree) {
		initMethodComments();
		List<String> commentLines;
		try {
			commentLines = cleanLines(getMethodComments(tree));
//			if (commentLines.isEmpty()) {
//				return Arrays.asList(CommonMessage.ABSENT_METHOD_COMMENTS);
//			}
		} catch (Exception e) {
			return Arrays.asList(e.getMessage());
		}

		List<String> errMsgs = new ArrayList<>();
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			methodComments.get(type).add(commentLines.get(i));
//			String nameType = type.name();
//			String value = commentLines.get(i);
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine()) {
					errMsgs.add(type.errMsg);
				}
			} else {
				if (!type.errMsg.isEmpty()) {
					errMsgs.add(type.errMsg);
				}
				if (!isOrderedLine(type)) {
					errMsgs.add(CommonMessage.INVALID_ORDER_FORMAT);
				}
			}
		}
//		if (methodComments.get(Type.EMPTY_LINE).isEmpty()) {
//			errMsgs.add(0,CommonMessage.ABSENT_EMPTY_LINE);
//		}
//		if (methodComments.get(Type.DESCRIPTION).isEmpty()) {
//			errMsgs.add(0, CommonMessage.ABSENT_DESCRIPTION);
//		} else if (methodComments.get(Type.EMPTY_LINE).isEmpty()) {
//			errMsgs.add(0, CommonMessage.ABSENT_EMPTY_LINE);
//		}
		return errMsgs;
	}

	public static String getLine(int lineNo) {
		return context.getFileLines().get(lineNo - 1);
	}

	public static List<String> getDocErrMsgs(JavaFileScannerContext context, MethodTree tree) {
		MethodCommentUtils.context = context;
		List<String> errMsgs = new ArrayList<>(getFormatErrMsgs(tree));
		if (!errMsgs.isEmpty()) {
			return errMsgs;
		}
		if (methodComments.get(Type.DESCRIPTION).isEmpty()) {
			errMsgs.add(0, CommonMessage.ABSENT_DESCRIPTION);
		} else if (methodComments.get(Type.EMPTY_LINE).isEmpty()) {
			errMsgs.add(0, CommonMessage.ABSENT_EMPTY_LINE);
		}
//		if (!validParam(tree)) {
//			errMsgs.add(CommonMessage.INVALID_PARAM);
//		}
		String paramErrMsg = getParamErrMsg(tree);
		String returnErrMsg = getReturnErrMsg(tree);
		String exceptErrMsg = getExceptErrMsg(tree);
		if (!paramErrMsg.isEmpty()) {
			errMsgs.add(paramErrMsg);
		}
		if (!returnErrMsg.isEmpty()) {
			errMsgs.add(returnErrMsg);
		}
		if (!exceptErrMsg.isEmpty()) {
			errMsgs.add(exceptErrMsg);
		}
//		if (!validReturn(tree)) {
//			errMsgs.add(CommonMessage.INVALID_RETURN);
//		}
//		if (!validExcept(tree)) {
//			errMsgs.add(CommonMessage.INVALID_EXCEPTION);
//		}
		return errMsgs;
	}

	public static String getParamErrMsg(MethodTree tree) {
//		String[] av = getLine(tree.firstToken().line()).replaceAll("\\s+", "").split("[()]");
		String declare = getLine(tree.firstToken().line()).replaceAll("\\s+", "").split("[()]")[1];
		List<String> params = methodComments.get(Type.PARAM);
		if (params.isEmpty() && !tree.parameters().isEmpty()) {
			return CommonMessage.ABSENT_PARAM;
		} else if (params.size() < tree.parameters().size()) {
			return CommonMessage.LACK_OF_PARAM;
		} else if (params.size() > tree.parameters().size()) {
			return CommonMessage.TOO_MANY_PARAM;
		} else if (params.isEmpty() && declare.isEmpty()) {
			return "";
		} else {
			String paramComments = String.join(",", params).replaceAll("\\* @param|\\s+", "");
			return paramComments.equals(declare) ? "" : CommonMessage.INVALID_PARAM;
		}
	}

//	public static String getParamErrMsg(MethodTree tree) {
//		Symbol.MethodSymbol symbol = tree.symbol();
//		List<String> params = methodComments.get(Type.PARAM);
//		if (params.isEmpty() && !tree.parameters().isEmpty()) {
//			return CommonMessage.ABSENT_PARAM;
//		} else if (params.size() < tree.parameters().size()) {
//			return CommonMessage.LACK_OF_PARAM;
//		} else if (params.size() > tree.parameters().size()) {
//			return CommonMessage.TOO_MANY_PARAM;
//		}
//		String type = "";
//		String name = "";
//		for (int i = 0; i < params.size(); i++) {
//
//			type = symbol.parameterTypes().get(i).fullyQualifiedName();
//			name = tree.parameters().get(i).simpleName().name();
//			String[] parts = params.get(i).split("\\s+");
////			tree.firstToken().line()
////			tree.lastToken()
////			if (!parts[3].equals(name) || !parts[2].equals(type)) {
////				return CommonMessage.INVALID_PARAM;
////			}
//			if (!parts[3].equals(name) || !type.contains(parts[2])) {
//				return CommonMessage.INVALID_PARAM;
//			}
//		}
//		return "";
//	}

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

//	public static boolean validReturn(MethodTree tree) {
//		org.sonar.plugins.java.api.semantic.Type rType = tree.returnType().symbolType();
//		if (methodComments.get(Type.RETURN).isEmpty()) {
//			return rType.isVoid();
//		} else if (methodComments.get(Type.RETURN).size() > 1) {
//			return false;
//		}
//		List<org.sonar.plugins.java.api.semantic.Type> typeArguments = rType.typeArguments();
//
//		String[] parts = methodComments.get(Type.RETURN).get(0).split("\\s+");
//
//		List<String> returnTypes = Arrays.asList(parts[2].replaceAll("\\s+", "").split("[<>,]"));
//		if (rType.isVoid() && returnTypes.size() == 1 && "Void".equalsIgnoreCase(returnTypes.get(0))) {
//			return true;
//		} else {
//			if (returnTypes.isEmpty() || !rType.fullyQualifiedName().contains(returnTypes.get(0))
//					|| returnTypes.size() != typeArguments.size() + 1) {// ??
//				return false;
//			}
//			for (int i = 0; i < typeArguments.size(); i++) {
//				if (!typeArguments.get(i).fullyQualifiedName().equals(returnTypes.get(i + 1))) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}

	public static String getReturnErrMsg(MethodTree tree) {
		org.sonar.plugins.java.api.semantic.Type rType = tree.returnType().symbolType();
		if (methodComments.get(Type.RETURN).isEmpty()) {
			return rType.isVoid() ? "" : CommonMessage.ABSENT_RETURN;
		} else if (methodComments.get(Type.RETURN).size() > 1) {
			return CommonMessage.TOO_MANY_RETURN;
		}
//		tree.firstToken().text();
//		tree.lastToken().text()
//		tree.defaultToken().text()
//		tree.symbol().name()
		String declare = getLine(tree.firstToken().line()).replaceAll("\\s+", "").split("[()]")[0];
		String returnComment = methodComments.get(Type.RETURN).get(0).replaceAll("\\* @return|\\s+", "")+tree.symbol().name();
		return declare.contains(returnComment) ? "" : CommonMessage.INVALID_RETURN;
		
//		List<org.sonar.plugins.java.api.semantic.Type> typeArguments = rType.typeArguments();
//
//		String returnType = methodComments.get(Type.RETURN).get(0).replaceAll("\\* @return", "").trim();
//
//		List<String> parts = Arrays.asList(returnType.split("[<>,]"));
//		if (rType.fullyQualifiedName().equals(returnType)) {
//			return "";
//		} else {
//			if (parts.size() != typeArguments.size() + 1 | !rType.fullyQualifiedName().contains(parts.get(0))) {
//				return CommonMessage.INVALID_RETURN;
//			}
//			for (int i = 1; i < typeArguments.size(); i++) {
//				if (!typeArguments.get(i).fullyQualifiedName().equals(parts.get(i + 1))) {
//					return CommonMessage.INVALID_RETURN;
//				}
//			}
//		}
//		return "";
	}
	
//	public static String getReturnErrMsg(MethodTree tree) {
//		org.sonar.plugins.java.api.semantic.Type rType = tree.returnType().symbolType();
//		if (methodComments.get(Type.RETURN).isEmpty()) {
//			return rType.isVoid() ? "" : CommonMessage.ABSENT_RETURN;
//		} else if (methodComments.get(Type.RETURN).size() > 1) {
//			return CommonMessage.TOO_MANY_RETURN;
//		}
//		List<org.sonar.plugins.java.api.semantic.Type> typeArguments = rType.typeArguments();
//
//		String returnType = methodComments.get(Type.RETURN).get(0).replaceAll("\\* @return", "").trim();
//
//		List<String> parts = Arrays.asList(returnType.split("[<>,]"));
//		if (rType.fullyQualifiedName().equals(returnType)) {
//			return "";
//		} else {
//			if (parts.size() != typeArguments.size() + 1 | !rType.fullyQualifiedName().contains(parts.get(0))) {
//				return CommonMessage.INVALID_RETURN;
//			}
//			for (int i = 1; i < typeArguments.size(); i++) {
//				if (!typeArguments.get(i).fullyQualifiedName().equals(parts.get(i + 1))) {
//					return CommonMessage.INVALID_RETURN;
//				}
//			}
//		}
//		return "";
//	}

	public static boolean validReturn(MethodTree tree) {
		org.sonar.plugins.java.api.semantic.Type rType = tree.returnType().symbolType();
		if (methodComments.get(Type.RETURN).isEmpty()) {
			return rType.isVoid();
		} else if (methodComments.get(Type.RETURN).size() > 1) {
			return false;
		}
		List<org.sonar.plugins.java.api.semantic.Type> typeArguments = rType.typeArguments();

//		String[] parts = methodComments.get(Type.RETURN).get(0).split("\\s+");

		String returnType = methodComments.get(Type.RETURN).get(0).replaceAll("\\* @return", "").trim();

		List<String> parts = Arrays.asList(returnType.split("[<>,]"));
		if (rType.fullyQualifiedName().equalsIgnoreCase(returnType)) {
			return true;
		} else {
//			if (returnTypes.isEmpty() || !rType.fullyQualifiedName().contains(returnTypes.get(0))
//					|| returnTypes.size() != typeArguments.size() + 1) {// ??
//				return false;
//			}
			if (parts.size() != typeArguments.size() + 1) {
				return false;
			}
			for (int i = 1; i < typeArguments.size(); i++) {
				if (!typeArguments.get(i).fullyQualifiedName().equals(parts.get(i + 1))) {
					return false;
				}
			}
		}
		return true;
	}

	public static String getExceptErrMsg(MethodTree tree) {
		List<String> throwsTypes = tree.throwsClauses().stream().map(el -> ((IdentifierTree) el).name())
				.collect(Collectors.toList());
		List<String> exceptionTypes = methodComments.get(Type.EXCEPTION).stream()
				.map(ex -> ex.replaceAll("\\* @exception|\\* @throws", "").trim()).collect(Collectors.toList());
		if (exceptionTypes.isEmpty() && !throwsTypes.isEmpty()) {
			return CommonMessage.ABSENT_EXCEPTION;
		} else if (exceptionTypes.size() < throwsTypes.size()) {
			return CommonMessage.LACK_OF_EXCEPTION;
		} else if (exceptionTypes.size() > throwsTypes.size()) {
			return CommonMessage.TOO_MANY_EXCEPTION;
		}
		for (int i = 0; i < throwsTypes.size(); i++) {
			if (!exceptionTypes.get(i).equals(throwsTypes.get(i))) {
				return CommonMessage.INVALID_EXCEPTION;
			}
		}
		return "";
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
//		if (javadoc == null) {
//			return Collections.emptyList();
//		}
		String trimmedJavadoc = javadoc.trim();
//		if (trimmedJavadoc.length() <= 4) {
//			// Empty or malformed javadoc. for instance: '/**/'
//			return Collections.emptyList();
//		}
		// remove start and end of Javadoc as well as stars
		String[] lines = trimmedJavadoc.substring(3, trimmedJavadoc.length() - 2).replaceAll("(?m)^\\s*", "").trim()
				.split("\\r?\\n");
//		List<String> cleanLines = Arrays.stream(lines).map(String::trim).collect(Collectors.toList());
//		if (cleanLines.size() == 1 && cleanLines.get(0).isEmpty()) {
//			return Collections.emptyList();
//		}
//		return cleanLines;
		return Arrays.stream(lines).map(String::trim).filter(l -> !l.isEmpty()).collect(Collectors.toList());
	}

}