/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : MethodCommentUtils.java
*@FileTitle : MethodCommentUtils
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/
package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.TypeTree;

/**
 * Utility Method Comment
 * 
 * @author tathienphuoc
 * @see MethodCommentUtils
 * @since J2EE 1.6
 */
public class MethodCommentUtils {
	private static EnumMap<Type, ArrayList<String>> methodComments = new EnumMap<>(Type.class);

	private static JavaFileScannerContext context;

	private enum Type {
		INVALID_LINE(Pattern.compile("^[^\\*]|(\\*)\\S+")), DESCRIPTION(Pattern.compile("^\\*\\s+[^@][a-zA-Z0-9]+.*$")),
		PARAM(Pattern.compile("^\\* @param(\\s+\\S+){2}\\s*$")),
		INVALID_PARAM(Pattern.compile("\\*\\s*@param(\\s+\\S+)*\\s*$")),
		RETURN(Pattern.compile("^\\* @return\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_RETURN(Pattern.compile("\\*\\s*(@return).*$")),
		EXCEPTION(Pattern.compile("^\\* (@throws|@exception)\\s+[a-zA-Z]+\\s*$")),
		INVALID_EXCEPTION(Pattern.compile("\\*\\s*(@throws|@exception).*$")), EMPTY_LINE(Pattern.compile("^\\*\\s*$"));

		private Pattern regex;

		private String errMsg = "";

		static {
			INVALID_PARAM.errMsg = CommonMessage.METHOD_INVALID_PARAM_FORMAT;
			INVALID_RETURN.errMsg = CommonMessage.METHOD_INVALID_RETURN_FORMAT;
			INVALID_EXCEPTION.errMsg = CommonMessage.METHOD_INVALID_EXCEPTION_FORMAT;
			INVALID_LINE.errMsg = CommonMessage.METHOD_INVALID_LINE_FORMAT;
			EMPTY_LINE.errMsg = CommonMessage.METHOD_REMOVE_EMPTY_LINE;
		}

		/**
		 * Constructor
		 * 
		 * @param Pattern s
		 */
		Type(Pattern s) {
			this.regex = s;
		}

		/**
		 * Check if string matches pattern
		 * 
		 * @param String text
		 * @return Boolean
		 */
		public Boolean matches(String text) {
			return regex.matcher(text).find();
		}

		/**
		 * Get Type of string
		 * 
		 * @param String line
		 * @return Type
		 */
		public static Type getType(String line) {
			for (Type t : values()) {
				if (t.matches(line)) {
					return t;
				}
			}
			return DESCRIPTION;
		}
	}

	/**
	 * Get method comments
	 * 
	 * @param MethodTree tree
	 * @return String
	 * @throws Exception
	 */
	private static String getMethodComments(MethodTree tree) throws Exception {
		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
		if (methodComments.isEmpty()) {
			throw new Exception(CommonMessage.METHOD_ABSENT_COMMENTS);
		} else if (methodComments.size() > 1) {
			throw new Exception(CommonMessage.METHOD_TOO_MANY_COMMENTS);
		} else {
			String methodComment = methodComments.get(0).comment();
			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
				return methodComment;
			}
			throw new Exception(CommonMessage.METHOD_INVALID_COMMENTS_FORMAT);
		}
	}

	/**
	 * Check order of lines in comments
	 * 
	 * @param Type type
	 * @return boolean
	 */
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

	/**
	 * Get error messages of format in comments 
	 * 
	 * @param MethodTree tree
	 * @return List<String>
	 */
	public static List<String> getFormatErrMsgs(MethodTree tree) {
		initMethodComments();
		List<String> commentLines;
		try {
			commentLines = cleanLines(getMethodComments(tree));
		} catch (Exception e) {
			return Arrays.asList(e.getMessage());
		}

		List<String> errMsgs = new ArrayList<>();
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			methodComments.get(type).add(commentLines.get(i));
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine()) {
					errMsgs.add(type.errMsg);
				}
			} else {
				if (!type.errMsg.isEmpty()) {
					errMsgs.add(type.errMsg);
				}
				if (!isOrderedLine(type)) {
					errMsgs.add(CommonMessage.METHOD_INVALID_ORDER_FORMAT);
				}
			}
		}
		return errMsgs;
	}

	/**
	 * Get line by line number
	 * 
	 * @param int lineNo
	 * @return String
	 */
	public static String getLine(int lineNo) {
		return context.getFileLines().get(lineNo - 1);
	}

	/**
	 * Get error messages of invalid comments 
	 * 
	 * @param JavaFileScannerContext context
	 * @param MethodTree tree
	 * @return List<String>
	 */
	public static List<String> getDocErrMsgs(JavaFileScannerContext context, MethodTree tree) {
		MethodCommentUtils.context = context;
		List<String> errMsgs = new ArrayList<>(getFormatErrMsgs(tree));
		if (!errMsgs.isEmpty()) {
			return errMsgs;
		}
		if (methodComments.get(Type.DESCRIPTION).isEmpty()) {
			errMsgs.add(0, CommonMessage.METHOD_ABSENT_DESCRIPTION);
		} else if (methodComments.get(Type.EMPTY_LINE).isEmpty()) {
			errMsgs.add(0, CommonMessage.METHOD_ABSENT_EMPTY_LINE);
		}
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
		return errMsgs;
	}
	
	/**
	 * Initialize method comments variable
	 * 
	 */
	private static void initMethodComments() {
		for (Type t : Type.values()) {
			methodComments.put(t, new ArrayList<>());
		}
	}

	/**
	 * Check if method comment allows empty line or not
	 * 
	 * @return boolean
	 */
	private static boolean allowEmptyLine() {
		return methodComments.get(Type.PARAM).isEmpty() && methodComments.get(Type.RETURN).isEmpty()
				&& methodComments.get(Type.EXCEPTION).isEmpty();
	}

	/**
	 * Get errors message of invalid parameter comments 
	 * 
	 * @param MethodTree tree
	 * @return String
	 */
	public static String getParamErrMsg(MethodTree tree) {
		String declare = getLine(tree.firstToken().line()).replaceAll("\\s+", "").split("[()]")[1];
		List<String> params = methodComments.get(Type.PARAM);
		if (params.isEmpty() && !tree.parameters().isEmpty()) {
			return CommonMessage.METHOD_ABSENT_PARAM;
		} else if (params.size() < tree.parameters().size()) {
			return CommonMessage.METHOD_LACK_OF_PARAM;
		} else if (params.size() > tree.parameters().size()) {
			return CommonMessage.METHOD_TOO_MANY_PARAM;
		} else if (params.isEmpty() && declare.isEmpty()) {
			return "";
		} else {
			String paramComments = String.join(",", params).replaceAll("\\* @param|\\s+", "");
			return paramComments.equals(declare) ? "" : CommonMessage.METHOD_INVALID_PARAM;
		}
	}

	/**
	 * Get error messages of invalid return comment
	 * 
	 * @param MethodTree tree
	 * @return String
	 */
	public static String getReturnErrMsg(MethodTree tree) {
		if(tree.returnType()==null) {
			return methodComments.get(Type.RETURN).isEmpty()?"": CommonMessage.METHOD_INVALID_RETURN;
		}
		org.sonar.plugins.java.api.semantic.Type rType = tree.returnType().symbolType();
		if (methodComments.get(Type.RETURN).isEmpty()) {
			return rType.isVoid() ? "" : CommonMessage.METHOD_ABSENT_RETURN;
		} else if (methodComments.get(Type.RETURN).size() > 1) {
			return CommonMessage.METHOD_TOO_MANY_RETURN;
		}
		String declare = getLine(tree.firstToken().line()).replaceAll("\\s+", "").split("[()]")[0];
		String returnComment = methodComments.get(Type.RETURN).get(0).replaceAll("\\* @return|\\s+", "")
				+ tree.symbol().name();
		return declare.contains(returnComment) ? "" : CommonMessage.METHOD_INVALID_RETURN;
	}

	/**
	 * Get error messages of invalid exception comments 
	 * 
	 * @param MethodTree tree
	 * @return String
	 */
	public static String getExceptErrMsg(MethodTree tree) {
		List<String> throwsTypes = tree.throwsClauses().stream().map(el -> ((IdentifierTree) el).name())
				.collect(Collectors.toList());
		List<String> exceptionTypes = methodComments.get(Type.EXCEPTION).stream()
				.map(ex -> ex.replaceAll("\\* @exception|\\* @throws", "").trim()).collect(Collectors.toList());
		if (exceptionTypes.isEmpty() && !throwsTypes.isEmpty()) {
			return CommonMessage.METHOD_ABSENT_EXCEPTION;
		} else if (exceptionTypes.size() < throwsTypes.size()) {
			return CommonMessage.METHOD_LACK_OF_EXCEPTION;
		} else if (exceptionTypes.size() > throwsTypes.size()) {
			return CommonMessage.METHOD_TOO_MANY_EXCEPTION;
		}
		for (int i = 0; i < throwsTypes.size(); i++) {
			if (!exceptionTypes.get(i).equals(throwsTypes.get(i))) {
				return CommonMessage.METHOD_INVALID_EXCEPTION;
			}
		}
		return "";
	}

	/**
	 * Remove unwanted characters in comments and return lines of comment
	 * 
	 * @param String javadoc
	 * @return List<String>
	 */
	private static List<String> cleanLines(String javadoc) {
		String trimmedJavadoc = javadoc.trim();
		String[] lines = trimmedJavadoc.substring(3, trimmedJavadoc.length() - 2).replaceAll("(?m)^\\s*", "").trim()
				.split("\\r?\\n");
		return Arrays.stream(lines).map(String::trim).filter(l -> !l.isEmpty()).collect(Collectors.toList());
	}

}