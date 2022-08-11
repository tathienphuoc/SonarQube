package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.Tree;

public class ClassCommentUtils {

	private enum Type {
		INVALID_LINE(Pattern.compile("^[^\\*]|(\\*)\\S+")), DESCRIPTION(Pattern.compile("^\\*\\s+[^@][a-zA-Z0-9]+.*$")),
		AUTHOR(Pattern.compile("^\\* @author\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_AUTHOR(Pattern.compile("\\*\\s*(@author).*$")), SEE(Pattern.compile("^\\* @see\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_SEE(Pattern.compile("\\*\\s*(@see).*$")), SINCE(Pattern.compile("^\\* @since(\\s+\\S+){2}\\s*$")),
		INVALID_SINCE(Pattern.compile("\\*\\s*@since(\\s+\\S+){2,}\\s*$")), EMPTY_LINE(Pattern.compile("^\\*\\s*$"));

		private Pattern regex;

		private String errMsg = "";

		static {

			INVALID_AUTHOR.errMsg = CommonMessage.INVALID_AUTHOR_FORMAT;
			INVALID_SEE.errMsg = CommonMessage.INVALID_SEE_FORMAT;
			INVALID_SINCE.errMsg = CommonMessage.INVALID_SINCE_FORMAT;
			INVALID_LINE.errMsg = CommonMessage.INVALID_LINE;
			EMPTY_LINE.errMsg = CommonMessage.REMOVE_EMPTY_LINE;
		}

//		public String getErrMsg(int lineNo) {
//			return "Line " + lineNo + ": " + this.errMsg;
//		}

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

	private static EnumMap<Type, ArrayList<String>> classComments = new EnumMap<>(Type.class);

	private static void initClassComments() {
		for (Type t : Type.values()) {
			classComments.put(t, new ArrayList<>());
		}
	}

	private static boolean allowEmptyLine() {
		return classComments.get(Type.AUTHOR).isEmpty() && classComments.get(Type.SEE).isEmpty()
				&& classComments.get(Type.SINCE).isEmpty();
	}

//	private static String getClassComments(ClassTree tree) throws Exception {
//		List<SyntaxTrivia> classComments = tree.firstToken().trivias();
//		if (classComments.isEmpty()) {
//			return "";
//		} else if (classComments.size() > 1) {
//			throw new Exception(CommonMessage.TOO_MANY_CLASS_COMMENTS);
//		} else {
//			String classComment = classComments.get(0).comment();
//			if (classComment.startsWith("/**") && classComment.endsWith("*/") && classComment.length() > 4) {
//				return classComment;
//			}
//			throw new Exception(CommonMessage.INVALID_METHOD_COMMENTS_FORMAT);
//		}
//	}

	private static String getClassComments(ClassTree tree) throws Exception {
		List<SyntaxTrivia> classComments = tree.firstToken().trivias();
		if (classComments.isEmpty()) {
			throw new Exception(CommonMessage.ABSENT_CLASS_COMMENTS);
		} else if (classComments.size() > 1) {
			throw new Exception(CommonMessage.TOO_MANY_CLASS_COMMENTS);
		} else {
			String methodComment = classComments.get(0).comment();
			if (methodComment.startsWith("/**") && methodComment.endsWith("*/") && methodComment.length() > 4) {
				return methodComment;
			}
//			if (methodComment.startsWith("^\\/\\*\\*.*\\*\\/$")) {
//				return methodComment;
//			}
			throw new Exception(CommonMessage.INVALID_CLASS_COMMENTS_FORMAT);
		}
	}

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

	public static List<String> getFormatErrMsgs(Tree tree) {
		initClassComments();
		List<String> commentLines;
		try {
			commentLines = cleanLines(getClassComments((ClassTree) tree));
		} catch (Exception e) {
			return Arrays.asList(e.getMessage());
		}
		List<String> errMsgs = new ArrayList<>();
//		if (commentLines.isEmpty()) {
//			errMsg.append(CommonMessage.ABSENT_CLASS_COMMENTS);
//			return errMsg.toString();
//		}
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			classComments.get(type).add(commentLines.get(i));
			if (type.equals(Type.EMPTY_LINE)) {
				if (!allowEmptyLine()) {
//					errMsg.append("\n" + type.getErrMsg(i + 1) + commentLines.get(i) + "\n");
					errMsgs.add(type.errMsg);
				}
			} else {
				if (!type.errMsg.isEmpty()) {
//					errMsg.append("\n" + type.getErrMsg(i + 1) + commentLines.get(i) + "\n");
					errMsgs.add(type.errMsg);
				}
				if (!isOrderedLine(type)) {
//					errMsg.append("\nLine " + (i + 1) + commentLines.get(i) + " invalid order\n");
					errMsgs.add(CommonMessage.INVALID_CLASS_ORDER_FORMAT);
				}
			}
		}
		return errMsgs;
	}

	public static List<String> getDocErrMsgs(Tree tree) {
		List<String> errMsgs = new ArrayList<>(getFormatErrMsgs(tree));
//		errMsg.append(getFormatErrMsg(tree));
		if (!errMsgs.isEmpty()) {
			return errMsgs;
		}
		if (classComments.get(Type.DESCRIPTION).isEmpty()) {
			errMsgs.add(0, CommonMessage.ABSENT_DESCRIPTION);
		} else if (classComments.get(Type.EMPTY_LINE).isEmpty()) {
			errMsgs.add(0, CommonMessage.ABSENT_EMPTY_LINE);
		}
		if (classComments.get(Type.AUTHOR).isEmpty()) {
//			errMsg.append("no author \n");
			errMsgs.add(CommonMessage.ABSENT_AUTHOR);
		} else if (classComments.get(Type.AUTHOR).size() > 1) {
			errMsgs.add(CommonMessage.TOO_MANY_AUTHOR);
		}
		if (classComments.get(Type.SEE).isEmpty()) {
//			errMsg.append("no see \n");
			errMsgs.add(CommonMessage.ABSENT_SEE);
		} else if (classComments.get(Type.SEE).size() > 1) {
			errMsgs.add(CommonMessage.TOO_MANY_SEE);
		}
		if (classComments.get(Type.SINCE).isEmpty()) {
//			errMsg.append("no SINCE \n");
			errMsgs.add(CommonMessage.ABSENT_SINCE);
		} else if (classComments.get(Type.SINCE).size() > 1) {
			errMsgs.add(CommonMessage.TOO_MANY_SINCE);
		}

		return errMsgs;
	}

//	private static List<String> cleanLines(@Nullable String javadoc) {
//		if (javadoc == null) {
//			return Collections.emptyList();
//		}
//		String trimmedJavadoc = javadoc.trim();
//		if (trimmedJavadoc.length() <= 4) {
//			// Empty or malformed javadoc. for instance: '/**/'
//			return Collections.emptyList();
//		}
//		// remove start and end of Javadoc as well as stars
//		String[] lines = trimmedJavadoc.substring(3, trimmedJavadoc.length() - 2).replaceAll("(?m)^\\s*", "").trim()
//				.split("\\r?\\n");
//		return Arrays.stream(lines).map(String::trim).collect(Collectors.toList());
//	}
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