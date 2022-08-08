package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		DESCRIPTION(Pattern.compile("^\\*\\s+[^@][a-zA-Z0-9]+.*$")),
		AUTHOR(Pattern.compile("^\\* @author\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_AUTHOR(Pattern.compile("\\*\\s*(@author).*$")), SEE(Pattern.compile("^\\* @see\\s+[a-zA-Z]+\\S*\\s*$")),
		INVALID_SEE(Pattern.compile("\\*\\s*(@see).*$")), SINCE(Pattern.compile("^\\* @since(\\s+\\S+){2}\\s*$")),
		INVALID_SINCE(Pattern.compile("\\*\\s*@since(\\s+\\S+){2,}\\s*$")), EMPTY_LINE(Pattern.compile("^\\*\\s*$")),
		INVALID_LINE(Pattern.compile("^[^\\*].*"));

		private Pattern regex;

		private String errMsg = "";

		static {
			INVALID_AUTHOR.errMsg = "INVALID_AUTHOR";
			INVALID_SEE.errMsg = "INVALID_SEE";
			INVALID_SINCE.errMsg = "INVALID_SINCE";
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

	private static HashMap<Type, List<String>> classComments = new HashMap<>();

	private static void initClassComments() {
		for (Type t : Type.values()) {
			classComments.put(t, new ArrayList<String>());
		}
	}

	private static boolean allowEmptyLine() {
		return classComments.get(Type.AUTHOR).isEmpty() && classComments.get(Type.SEE).isEmpty()
				&& classComments.get(Type.SINCE).isEmpty();
	}

	private static String getClassComments(ClassTree tree) throws Exception {
		List<SyntaxTrivia> classComments = tree.firstToken().trivias();
		if (classComments.isEmpty()) {
			return "";
		} else if (classComments.size() > 1) {
			throw new Exception("Too many class comment");
		} else {
			String classComment = classComments.get(0).comment();
			if (classComment.startsWith("/**") && classComment.endsWith("*/")) {
				return classComment;
			}
			throw new Exception("Class comment must be start with //** and end with *//");
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

	public static String getFormatErrMsg(Tree tree) {
		initClassComments();
		List<String> commentLines;
		try {
			commentLines = cleanLines(getClassComments((ClassTree) tree));
		} catch (Exception e) {
			return e.getMessage();
		}
		StringBuilder errMsg = new StringBuilder();
		if (commentLines.isEmpty()) {
			errMsg.append("Class comments doesn't exist");
			return errMsg.toString();
		}
		for (int i = 0; i < commentLines.size(); i++) {
			Type type = Type.getType(commentLines.get(i));
			classComments.get(type).add(commentLines.get(i));
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
		return errMsg.toString();
	}

	public static String getDocErrMsg(Tree tree) {
		StringBuilder errMsg = new StringBuilder();
		errMsg.append(getFormatErrMsg(tree));
		if (classComments.get(Type.DESCRIPTION).isEmpty() || classComments.get(Type.DESCRIPTION).get(0).isEmpty()) {
			errMsg.append("Where is my des\n");
		}
		if (classComments.get(Type.EMPTY_LINE).isEmpty()) {
			errMsg.append("Need at least one empty line after des");
		}
		if (classComments.get(Type.AUTHOR).isEmpty()) {
			errMsg.append("no author \n");
		}
		if (classComments.get(Type.SEE).isEmpty()) {
			errMsg.append("no see \n");
		}
		if (classComments.get(Type.SINCE).isEmpty()) {
			errMsg.append("no SINCE \n");
		}
		return errMsg.toString();
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