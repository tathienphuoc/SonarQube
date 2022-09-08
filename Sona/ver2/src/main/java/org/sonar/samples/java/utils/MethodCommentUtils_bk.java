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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;

/**
 * Utility Method Comment
 * 
 * @author tathienphuoc
 * @see MethodCommentUtils_bk
 * @since J2EE 1.6
 */
public class MethodCommentUtils_bk {
	private static JavaFileScannerContext context;
	private static EnumMap<Type, ArrayList<String>> methodComments = new EnumMap<>(Type.class);
	public static HashMap<String, Integer> errMsgs = new HashMap<>();
	// public static boolean isNotSorted = false;
	public static String declareMethod = "";
	public static MethodTree tree;
	public static String methodName;
	public static String prefix = "";

	public enum Type {

		DESCRIPTION, EMPTY_LINE, PARAM, RETURN, EXCEPTION, OTHER, EMPTY_LINE_WITHOUT_ASTERISK, INVALID_LINE_FORMAT,
		INVALID_PARAM_FORMAT, INVALID_DESCRIPTION_FORMAT, INVALID_RETURN_FORMAT, INVALID_EXCEPTION_FORMAT,
		INVALID_EXTRA_WHITESPACE_FORMAT;

		public String errMsg = "";
		public String regex="";
		
		static {
			EMPTY_LINE.errMsg = CommonMessage.REMOVE_EMPTY_LINE;
			EMPTY_LINE_WITHOUT_ASTERISK.errMsg = CommonMessage.REMOVE_EMPTY_LINE_WITHOUT_ASTERISK;
			INVALID_LINE_FORMAT.errMsg = CommonMessage.INVALID_LINE_FORMAT;
			INVALID_DESCRIPTION_FORMAT.errMsg = CommonMessage.INVALID_DESCRIPTION_FORMAT;
			INVALID_PARAM_FORMAT.errMsg = CommonMessage.INVALID_PARAM_FORMAT;
			INVALID_RETURN_FORMAT.errMsg = CommonMessage.INVALID_RETURN_FORMAT;
			INVALID_EXCEPTION_FORMAT.errMsg = CommonMessage.INVALID_EXCEPTION_FORMAT;
			
			DESCRIPTION.regex="^\\*\\s+[^@\\s]+.*$";
		}

		public static boolean validFormat(String line, Type type) {
			if (type != PARAM && type != RETURN) {
				return false;
			}
			line = line.substring(type == PARAM ? 8 : 9).replaceAll("\\s+", " ").trim();
			if (line.isEmpty()) {
				return false;
			}
			int lastIndex = -1;
			Matcher matcher = Pattern.compile("[<>,\\[\\]?]").matcher(line);
			while (matcher.find()) {
				lastIndex = matcher.end() - 1;
			}
			long countWhitespace;
			if (lastIndex == -1) {// primary type
				countWhitespace = line.chars().filter(c -> c == ' ').count();
				if (type == PARAM) {// int name
					return countWhitespace == 1;
				} else {// int
					return countWhitespace == 0;
				}
			} else {// String[] a
				if (type == PARAM) {
					return line.substring(lastIndex + 1).chars().filter(c -> c == ' ').count() == 1;
				} else {
					return lastIndex == line.length() - 1;
				}
			}
		}

		public static Type getType(String line) {// line==null is necessary?
			if (line.isEmpty()) {// line==null is necessary?
				return EMPTY_LINE_WITHOUT_ASTERISK;
			} else if (line.equals("*")) {// *
				return EMPTY_LINE;
			}
			
			int beginIndex = line.indexOf("@");
			int lastIndex = line.indexOf(" ",2);
			String annotation="";
			if(beginIndex!=-1) {
				annotation=lastIndex == -1 ? line.substring(beginIndex) : line.substring(beginIndex, lastIndex);
			}
			
			if (Pattern.compile("^(\\*){0,1}(\\s{2,}){0,1}@").matcher(line).find()) {// annotation vi pham
				switch (annotation) {
				case "@param":
					methodComments.get(INVALID_PARAM_FORMAT).add(line);
					break;
				case "@return":
					methodComments.get(INVALID_RETURN_FORMAT).add(line);
					break;
				case "@throws":case "@exception":
					methodComments.get(INVALID_RETURN_FORMAT).add(line);
					break;
				default:
					if(!annotation.startsWith("@")) {
						methodComments.get(INVALID_RETURN_FORMAT).add(line);
					}
					break;
				}
				return INVALID_LINE_FORMAT;
			}else if(Pattern.compile("^(\\*){0,1}(\\s{2,}){0,1}[^@]").matcher(line).find()) {//des
				return line.endsWith("<br>")?DESCRIPTION:INVALID_DESCRIPTION_FORMAT;
			}else {
				switch (annotation) {
				case "@param":
					return validFormat(line, PARAM) ? PARAM : INVALID_PARAM_FORMAT;
				case "@return":
					return validFormat(line, RETURN) ? RETURN : INVALID_RETURN_FORMAT;
				case "@throws":case "@exception":
					if (line.split("\\s+").length == 3) {
						return EXCEPTION;
					} else {
						return INVALID_EXCEPTION_FORMAT;
					}
				default:
					break;
				}
			}
			return OTHER;
		}
	}

	/**
	 * Get method comments
	 * 
	 * @param MethodTree tree
	 * @return String
	 * @throws Exception
	 */
	private static String getMethodComments() throws Exception {
		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
		if (methodComments.isEmpty()) {
			throw new Exception(CommonMessage.ABSENT_METHOD_COMMENT);
		} else if (methodComments.size() > 1) {
			throw new Exception(CommonMessage.TOO_MANY_METHOD_COMMENTS);
		} else {
			String methodComment = methodComments.get(0).comment();

			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
				return methodComment;
			}
			throw new Exception(CommonMessage.INVALID_COMMENT_FORMAT);
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

	public static boolean hasFormatErr() throws Exception {
		List<String> commentLines = cleanLines(getMethodComments());
		int firstAnotationIndex = -1;
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			methodComments.get(type).add(commentLines.get(i));
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine()) {
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
				if (!isOrderedLine(type)) {
					errMsgs.put(CommonMessage.METHOD_NOT_SORTED,
							errMsgs.getOrDefault(CommonMessage.METHOD_NOT_SORTED, 0) + 1);
				}
			}
		}

		String desErrMsg = getFormatErrMsg(Type.DESCRIPTION);
		String paramErrMsg = getFormatErrMsg(Type.PARAM);
		String returnErrMsg = getFormatErrMsg(Type.RETURN);
		String exceptErrMsg = getFormatErrMsg(Type.EXCEPTION);
//		String emptyLineErrMsg = getFormatErrMsg(Type.EMPTY_LINE);
		if (!desErrMsg.isEmpty()) {
			errMsgs.put(desErrMsg, 1);
		}
//		if (!emptyLineErrMsg.isEmpty()) {
//			errMsgs.put(emptyLineErrMsg, 1);
//		}else if(!Type.getType(commentLines.get(firstAnotationIndex-1)).equals(Type.EMPTY_LINE)){
//			errMsgs.put(CommonMessage.METHOD_ABSENT_EMPTY_LINE, 1);
//		}

		if (firstAnotationIndex != -1
				&& !Type.getType(commentLines.get(firstAnotationIndex - 1)).equals(Type.EMPTY_LINE)) {
			errMsgs.put(CommonMessage.ABSENT_EMPTY_LINE, 1);
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

		return !errMsgs.isEmpty();
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

		return sb.toString().replaceAll("\\s+", "");
	}
//	public static String getDeclare() {
//		StringBuilder sb = new StringBuilder();
//		int lineNo = tree.modifiers().lastToken().line()-1;
//		List<String> fileLines=context.getFileLines();
//		while(lineNo<fileLines.size()) {
//			String line=fileLines.get(lineNo);
//			int endIndex=line.indexOf("{");
//			if(endIndex!=-1) {
//				line=line.substring(0,endIndex);
//			}
//			sb.append(line);
//			lineNo++;
//		}
//		return sb.toString().replaceAll("\\s+", ""); 
//	}

	public static String getFormatErrMsg(Type type) {
		int commentCount, declareCount;
		switch (type) {
		case DESCRIPTION:
			return methodComments.get(Type.DESCRIPTION).isEmpty()
					&& methodComments.get(Type.INVALID_DESCRIPTION_FORMAT).isEmpty()
							? CommonMessage.ABSENT_DESCRIPTION
							: "";
//		case EMPTY_LINE:
//			commentCount = methodComments.get(Type.PARAM).size() + methodComments.get(Type.INVALID_PARAM_FORMAT).size()
//					+ methodComments.get(Type.RETURN).size() + methodComments.get(Type.INVALID_RETURN_FORMAT).size()
//					+ methodComments.get(Type.EXCEPTION).size()
//					+ methodComments.get(Type.INVALID_EXCEPTION_FORMAT).size();
//			return methodComments.get(Type.EMPTY_LINE).isEmpty() && commentCount != 0
//					? CommonMessage.METHOD_ABSENT_EMPTY_LINE
//					: "";
		case PARAM:
			commentCount = methodComments.get(Type.PARAM).size() + methodComments.get(Type.INVALID_PARAM_FORMAT).size();
			declareCount = tree.parameters().size();
			if (commentCount == declareCount) {
				return "";
			} else if (commentCount > declareCount) {
				return CommonMessage.TOO_MANY_PARAM;
			} else if (commentCount == 0 && declareCount != 0) {
				return CommonMessage.ABSENT_PARAM;
			} else {
				return CommonMessage.LACK_OF_PARAM;
			}

		case RETURN:
			commentCount = methodComments.get(Type.RETURN).size()
					+ methodComments.get(Type.INVALID_RETURN_FORMAT).size();
			if (tree.returnType() == null || commentCount > 1) {
				return commentCount == 0 ? "" : CommonMessage.TOO_MANY_RETURN;
			} else if (tree.returnType().symbolType().isVoid() && commentCount == 0) {
				return "";
			} else if (commentCount == 0) {
				return CommonMessage.ABSENT_RETURN;
			} else {// 1==1
				return "";
			}

//		case RETURN:
//			commentCount = methodComments.get(Type.RETURN).size()
//					+ methodComments.get(Type.INVALID_RETURN_FORMAT).size();
//			if(tree.returnType()==null) {
//				return commentCount==0?"":CommonMessage.METHOD_TOO_MANY_RETURN;
//			}else if(tree.returnType().symbolType().isVoid()) {
//				return commentCount<2?"":CommonMessage.METHOD_TOO_MANY_RETURN;	
//			}else if(commentCount==0){
//				return CommonMessage.METHOD_ABSENT_RETURN;
//			}else if(commentCount>1) {
//				return CommonMessage.METHOD_TOO_MANY_RETURN;
//			}else {
//				return "";
//			}

		case EXCEPTION:
			commentCount = methodComments.get(Type.EXCEPTION).size()
					+ methodComments.get(Type.INVALID_EXCEPTION_FORMAT).size();
			declareCount = tree.throwsClauses().size();
			if (commentCount == declareCount) {
				return "";
			} else if (commentCount > declareCount) {
				return CommonMessage.TOO_MANY_EXCEPTION;
			} else if (commentCount == 0 && declareCount != 0) {
				return CommonMessage.ABSENT_EXCEPTION;
			} else {
				return CommonMessage.LACK_OF_EXCEPTION;
			}
		default:
			return "";
		}
	}

	public static List<String> generateErrMsgs() {
		List<String> msgs = new ArrayList<String>();
		for (Entry<String, Integer> entry : errMsgs.entrySet()) {
			if (entry.getValue() == 1) {
				msgs.add(String.format("%s[%s]", prefix, entry.getKey()));
			} else {
				msgs.add(String.format("%s[%s][+%d locations]", prefix, entry.getKey(), entry.getValue()));
			}
		}
//		return msgs.size() > 5 ? Arrays.asList(String.format("[%s]", CommonMessage.METHOD_CHECK_RULE))
//				: msgs;
		return msgs;
	}

	public static List<String> getDocErrMsgs(JavaFileScannerContext context, MethodTree methodTree) {
		initMethodComments(context, methodTree);
		try {
			if (!hasFormatErr()) {
				if (!validParamSection()) {
					errMsgs.put(CommonMessage.INVALID_PARAM, 1);
				}
				if (!validReturnSection()) {
					errMsgs.put(CommonMessage.INVALID_RETURN, 1);
				}
				if (!validExceptSection()) {
					errMsgs.put(CommonMessage.INVALID_EXCEPTION, 1);
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
			errMsgs.put(e.getMessage(), 1);
		}
		return generateErrMsgs();
	}

	private static void initMethodComments(JavaFileScannerContext javaFileScannerContext, MethodTree methodTree) {
//		isNotSorted = false;
		context = javaFileScannerContext;
		tree = methodTree;
		methodComments.clear();
		errMsgs.clear();
		declareMethod = getDeclare();
		methodName = tree.symbol().declaration().simpleName().name();
		prefix = String.format("[R4J][Method comment][%s]", methodName);
		for (Type t : Type.values()) {
			methodComments.put(t, new ArrayList<>());
		}
	}

	private static boolean allowEmptyLine() {
		return methodComments.get(Type.PARAM).isEmpty() && methodComments.get(Type.RETURN).isEmpty()
				&& methodComments.get(Type.EXCEPTION).isEmpty();
	}

	public static boolean validParamSection() {
		String comments = String.join(",", methodComments.get(Type.PARAM).stream()
				.map(p -> p.substring(8).replaceAll("\\* @param|\\s+", "")).collect(Collectors.toList()));
		String declare = declareMethod.split("[()]")[1];
		return declare.equals(comments);
	}

	public static boolean validReturnSection() {
		if (methodComments.get(Type.RETURN).isEmpty()) {
			return true;
		}
		String comment = methodComments.get(Type.RETURN).get(0).replaceAll("\\* @return|\\s+", "") + methodName;
		String declare = declareMethod.split("[()]")[0];
		return declare.endsWith(comment);
	}

	public static boolean validExceptSection() {
		String comments = String.join(",", methodComments.get(Type.EXCEPTION).stream()
				.map(p -> p.replaceAll("\\* @exception|\\* @throws|\\s+", "")).collect(Collectors.toList()));
		String declare = declareMethod.split("[()]")[2];
		return !declare.startsWith("throws") || declare.startsWith(comments, 6);
	}

	private static List<String> cleanLines(String javadoc) {
		String trimmedJavadoc = javadoc.trim();
		String[] lines = trimmedJavadoc.substring(3, trimmedJavadoc.length() - 2).trim().split("\\r?\\n");
		return Arrays.stream(lines).map(String::trim).collect(Collectors.toList());
	}

}