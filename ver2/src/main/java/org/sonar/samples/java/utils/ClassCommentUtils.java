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
import java.util.List;
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

	private enum Type {
		INVALID_LINE(Pattern.compile("^[^\\*]|(\\*)\\S+")), DESCRIPTION(Pattern.compile("^\\*\\s+[^@][a-zA-Z0-9]+.*$")),
		AUTHOR(Pattern.compile("^\\* @author\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_AUTHOR(Pattern.compile("\\*\\s*(@author).*$")), SEE(Pattern.compile("^\\* @see\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_SEE(Pattern.compile("\\*\\s*(@see).*$")), SINCE(Pattern.compile("^\\* @since(\\s+\\S+){2}\\s*$")),
		INVALID_SINCE(Pattern.compile("\\*\\s*(@since).*$")), EMPTY_LINE(Pattern.compile("^\\*\\s*$"));

		private Pattern regex;

		private String errMsg = "";

		static {

			INVALID_AUTHOR.errMsg = CommonMessage.CLASS_INVALID_AUTHOR_FORMAT;
			INVALID_SEE.errMsg = CommonMessage.CLASS_INVALID_SEE_FORMAT;
			INVALID_SINCE.errMsg = CommonMessage.CLASS_INVALID_SINCE_FORMAT;
			INVALID_LINE.errMsg = CommonMessage.CLASS_INVALID_LINE_FORMAT;
			EMPTY_LINE.errMsg = CommonMessage.CLASS_REMOVE_EMPTY_LINE;
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

	private static EnumMap<Type, ArrayList<String>> classComments = new EnumMap<>(Type.class);

	/**
	 * Initialize class comments variable
	 * 
	 */
	private static void initClassComments() {
		for (Type t : Type.values()) {
			classComments.put(t, new ArrayList<>());
		}
	}

	/**
	 * Check if method comment allows empty line or not
	 * 
	 * @return boolean
	 */
	private static boolean allowEmptyLine() {
		return classComments.get(Type.AUTHOR).isEmpty() && classComments.get(Type.SEE).isEmpty()
				&& classComments.get(Type.SINCE).isEmpty();
	}

	/**
	 * Get class comments
	 * 
	 * @param ClassTree tree
	 * @return String
	 * @throws Exception
	 */
	private static String getClassComments(ClassTree tree) throws Exception {
		List<SyntaxTrivia> classComments = tree.firstToken().trivias();
		if (classComments.isEmpty()) {
			throw new Exception(CommonMessage.CLASS_ABSENT_COMMENTS);
		} else if (classComments.size() > 1) {
			throw new Exception(CommonMessage.CLASS_TOO_MANY_COMMENTS);
		} else {
			String methodComment = classComments.get(0).comment();
			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
				return methodComment;
			}
			throw new Exception(CommonMessage.CLASS_INVALID_COMMENTS_FORMAT);
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
		case AUTHOR:
			return classComments.get(Type.SEE).isEmpty() && classComments.get(Type.SINCE).isEmpty();
		case SEE:
			return classComments.get(Type.SINCE).isEmpty();
		default:
			return true;
		}
	}

	/**
	 * Get error messages of format in comments 
	 * 
	 * @param ClassTree tree
	 * @return List<String>
	 */
	public static List<String> getFormatErrMsgs(ClassTree tree) {
		initClassComments();
		List<String> commentLines;
		try {
			commentLines = cleanLines(getClassComments(tree));
		} catch (Exception e) {
			return Arrays.asList(e.getMessage());
		}
		List<String> errMsgs = new ArrayList<>();
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			classComments.get(type).add(commentLines.get(i));
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine()) {
					errMsgs.add(type.errMsg);
				}
			} else {
				if (!type.errMsg.isEmpty()) {
					errMsgs.add(type.errMsg);
				}
				if (!isOrderedLine(type)) {
					errMsgs.add(CommonMessage.CLASS_INVALID_ORDER_FORMAT);
				}
			}
		}
		return errMsgs;
	}

	/**
	 * Get error messages of invalid comments 
	 * 
	 * @param ClassTree tree
	 * @return List<String>
	 */
	public static List<String> getDocErrMsgs(ClassTree tree) {
		List<String> errMsgs = new ArrayList<>(getFormatErrMsgs(tree));
		if (!errMsgs.isEmpty()) {
			return errMsgs;
		}
		if (classComments.get(Type.DESCRIPTION).isEmpty()) {
			errMsgs.add(0, CommonMessage.CLASS_ABSENT_DESCRIPTION);
		} else if (classComments.get(Type.EMPTY_LINE).isEmpty()) {
			errMsgs.add(0, CommonMessage.CLASS_ABSENT_EMPTY_LINE);
		}
		if (classComments.get(Type.AUTHOR).isEmpty()) {
			errMsgs.add(CommonMessage.CLASS_ABSENT_AUTHOR);
		} else if (classComments.get(Type.AUTHOR).size() > 1) {
			errMsgs.add(CommonMessage.CLASS_TOO_MANY_AUTHOR);
		}
		if (classComments.get(Type.SEE).isEmpty()) {
			errMsgs.add(CommonMessage.CLASS_ABSENT_SEE);
		} else if (classComments.get(Type.SEE).size() > 1) {
			errMsgs.add(CommonMessage.CLASS_TOO_MANY_SEE);
		}
		if (classComments.get(Type.SINCE).isEmpty()) {
			errMsgs.add(CommonMessage.CLASS_ABSENT_SINCE);
		} else if (classComments.get(Type.SINCE).size() > 1) {
			errMsgs.add(CommonMessage.CLASS_TOO_MANY_SINCE);
		}

		return errMsgs;
	}
	/**
	 * Remove unwanted characters in comments and return lines of comment<br/>
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