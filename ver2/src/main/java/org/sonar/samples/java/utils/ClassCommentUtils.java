/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : ClassCommentUtils.java
*@FileTitle : ClassCommentUtils
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;

/**
 * Utility Class Comment
 * 
 * @author tathienphuoc
 * @see ClassCommentUtils
 * @since J2EE 1.6
 */
public class ClassCommentUtils {
	private static List<String> classComments;
	private static EnumMap<Type, ArrayList<String>> commentLineTypes;
	private static HashMap<String, Integer> errMsgs;
	private static ClassTree tree;
	private static String prefix;

	public enum Type {
		DESCRIPTION, EMPTY_LINE, AUTHOR, SEE, SINCE, OTHER, EMPTY_LINE_WITHOUT_ASTERISK, INVALID_LINE_FORMAT,
		INVALID_AUTHOR_FORMAT, INVALID_DESCRIPTION_FORMAT;

		String errMsg = "";
		private Pattern regex;

		static {
			EMPTY_LINE.errMsg = CommonMessage.REMOVE_EMPTY_LINE;
			EMPTY_LINE_WITHOUT_ASTERISK.errMsg = CommonMessage.REMOVE_EMPTY_LINE_WITHOUT_ASTERISK;
			INVALID_LINE_FORMAT.errMsg = CommonMessage.INVALID_LINE_FORMAT;
			INVALID_DESCRIPTION_FORMAT.errMsg = CommonMessage.INVALID_DESCRIPTION_FORMAT;
			INVALID_AUTHOR_FORMAT.errMsg = CommonMessage.INVALID_AUTHOR_FORMAT;

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
		case "@author":
			commentLineTypes.get(Type.AUTHOR).add(line);
			break;
		case "@see":
			commentLineTypes.get(Type.SEE).add(line);
			break;
		case "@since":
			commentLineTypes.get(Type.SINCE).add(line);
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
		case "@author":
			return validAuthorFormat(line) ? Type.AUTHOR : Type.INVALID_AUTHOR_FORMAT;
		case "@see":
			return Type.SEE;
		case "@since":
			return Type.SINCE;
		default:
			return Type.OTHER;
		}
	}

	public static boolean validAuthorFormat(String line) {
		return line.split("\\s+").length > 1;
	}

	public static String getAnnotation(String line) {
		line = line.charAt(0) == '*' ? line.substring(1).trim() : line;
		int endIndex = line.indexOf(" ");
		return line.substring(0, endIndex != -1 ? endIndex : line.length());
	}

	private static void initClassComments(ClassTree classTree) throws Exception {

		tree = classTree;
		errMsgs = new HashMap<>();

		prefix = String.format("[R4J][Class comment][%s]", tree.symbol().declaration().simpleName().name());

		classComments = getClassComments();
		commentLineTypes = getCommentLineTypes();

	}

	private static List<String> getClassComments() throws Exception {
		List<SyntaxTrivia> methodComments = tree.firstToken().trivias();
		if (methodComments.isEmpty()) {
			throw new Exception(CommonMessage.ABSENT_CLASS_COMMENT);
		} else if (methodComments.size() > 1) {
			throw new Exception(CommonMessage.TOO_MANY_CLASS_COMMENTS);
		} else {
			String methodComment = methodComments.get(0).comment();

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
		for (int i = 0; i < classComments.size(); i++) {
			String line = classComments.get(i);
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
//		if (firstAnotationIndex != -1 && !getType(classComments.get(firstAnotationIndex - 1)).equals(Type.EMPTY_LINE)) {
//			errMsgs.put(CommonMessage.ABSENT_EMPTY_LINE, 1);
//		}	
		if (firstAnotationIndex >0 && !getType(classComments.get(firstAnotationIndex - 1)).equals(Type.EMPTY_LINE)) {
			errMsgs.put(CommonMessage.ABSENT_EMPTY_LINE, 1);
		}
		return lineTypes;
	}

	private static boolean isOrderedLine(Type type, EnumMap<Type, ArrayList<String>> lineTypes) {
		switch (type) {
		case DESCRIPTION:
		case INVALID_DESCRIPTION_FORMAT:
			return allowEmptyLine(lineTypes);
		case AUTHOR:
		case INVALID_AUTHOR_FORMAT:
			return lineTypes.get(Type.SEE).isEmpty() && lineTypes.get(Type.SINCE).isEmpty();
		case SEE:
			return lineTypes.get(Type.SINCE).isEmpty();
		default:
			return true;
		}
	}

	private static boolean allowEmptyLine(EnumMap<Type, ArrayList<String>> lineTypes) {
		return lineTypes.get(Type.AUTHOR).isEmpty() && lineTypes.get(Type.SEE).isEmpty()
				&& lineTypes.get(Type.SINCE).isEmpty();
	}

	public static void check(ClassTree classTree) {
		try {
			initClassComments(classTree);

			String desErrMsg = validSection(Type.DESCRIPTION);
			String authorErrMsg = validSection(Type.AUTHOR);
			String seeErrMsg = validSection(Type.SEE);
			String sinceErrMsg = validSection(Type.SINCE);

			if (!desErrMsg.isEmpty()) {
				errMsgs.put(desErrMsg, 1);
			}
			if (!authorErrMsg.isEmpty()) {
				errMsgs.put(authorErrMsg, 1);
			}
			if (!seeErrMsg.isEmpty()) {
				errMsgs.put(seeErrMsg, 1);
			}
			if (!sinceErrMsg.isEmpty()) {
				errMsgs.put(sinceErrMsg, 1);
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

	public static String validAuthorSection() {
		int commentCount = commentLineTypes.get(Type.AUTHOR).size()
				+ commentLineTypes.get(Type.INVALID_AUTHOR_FORMAT).size();

		if (commentCount == 0) {
			return CommonMessage.ABSENT_AUTHOR;
		} else if (commentCount == 1) {
			return "";
		} else {
			return CommonMessage.TOO_MANY_AUTHOR;
		}
	}

	public static String validSeeSection() {
		int commentCount = commentLineTypes.get(Type.SEE).size();

		if (commentCount == 0) {
			return CommonMessage.ABSENT_SEE;
		} else if (commentCount == 1) {
			return "";
		} else {
			return CommonMessage.TOO_MANY_SEE;
		}
	}

	public static String validSinceSection() {
		int commentCount = commentLineTypes.get(Type.SINCE).size();

		if (commentCount == 0) {
			return CommonMessage.ABSENT_SINCE;
		} else if (commentCount == 1) {
			return "";
		} else {
			return CommonMessage.TOO_MANY_SINCE;
		}
	}

	public static String validSection(Type type) {
		switch (type) {
		case DESCRIPTION:
			return validDesSection();
		case AUTHOR:
			return validAuthorSection();
		case SEE:
			return validSeeSection();

		case SINCE:
			return validSinceSection();
		default:
			return "";
		}
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
//			return msgs.size() > 5 ? Arrays.asList(String.format("[%s]", CommonMessage.METHOD_CHECK_RULE))
//					: msgs;
		return msgs;
	}
}