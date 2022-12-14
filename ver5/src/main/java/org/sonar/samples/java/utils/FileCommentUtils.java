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
import java.util.List;
import java.util.regex.Pattern;

import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.ClassTree;

/**
 * Utility Method Comment
 * 
 * @author tathienphuoc
 * @see FileCommentUtils
 * @since J2EE 1.6
 */
public class FileCommentUtils {
	private static ArrayList<String> fileComments;
	private static ArrayList<String> regex = new ArrayList<String>();
	private static String className = "";
	private static String prefix = "[R4J][File comment][%s]";
	private static String checkRuleMsg = String.format(prefix, CommonMessage.CHECK_RULE);
	static {
		regex.add("^\\*\\s{0,1}Copyright\\(c\\) \\d{4} CyberLogitec");
		regex.add("^\\*\\s{0,1}@FileName\\s{0,1}:\\s{0,1}\\S*$");
		regex.add("^\\*\\s{0,1}@FileTitle\\s{0,1}:\\s{0,1}\\S*$");
		regex.add("^\\*\\s{0,1}Open Issues\\s{0,1}:\\s{0,1}.*");
		regex.add("^\\*\\s{0,1}Change history\\s{0,1}:\\s{0,1}.*");
		regex.add("^\\*\\s{0,1}@LastModifyDate\\s{0,1}:\\s{0,1}");
		regex.add("^\\*\\s{0,1}@LastModifier\\s{0,1}:\\s{0,1}.*");
		regex.add("^\\*\\s{0,1}@LastVersion\\s{0,1}:\\s{0,1}.*");

	}

	// Chỉ check các file HTMLAction,.....
	private static void initFileComments(JavaFileScannerContext context, ClassTree tree) throws Exception {
		List<String> fileLines = context.getFileLines();// check lại trường hợp mà comment có dòng rỗng, kh có gì cả,
														// chỉ là dòng trắng
		className = tree.symbol().declaration().simpleName().name();
		fileComments = new ArrayList<>();
		if (!fileLines.isEmpty() && fileLines.get(0).startsWith("/*==")) {
			for (int i = 1; i < fileLines.size(); i++) {
				String value = fileLines.get(i).trim();
				if (value.endsWith("*/")) {
					return;
				}
				fileComments.add(value);
			}
		} else {
			throw new Exception(CommonMessage.ABSENT_FILE_COMMENT);
		}
	}

	public static String getErrMsgs(JavaFileScannerContext context, ClassTree tree) {
		try {
			initFileComments(context, tree);
		} catch (Exception e) {
			return String.format(prefix, e.getMessage());
		}
		if (regex.size() > fileComments.size()) {
			return checkRuleMsg;
		}
		for (int i = 0; i < regex.size(); i++) {
			if (!Pattern.compile(regex.get(i)).matcher(fileComments.get(i)).find()) {
				return checkRuleMsg;
			}
		}
		return fileComments.get(1).endsWith(className + ".java") && fileComments.get(2).endsWith(className)
				&& isValidDateFormat(
						fileComments.get(5).replaceFirst("^\\*\\s{0,1}@LastModifyDate\\s{0,1}:\\s{0,1}", "")) ? ""
								: checkRuleMsg;
	}

	private static boolean isValidDateFormat(String str) {
		return str.isEmpty() || Arrays.asList("-", " ", "\\/", "\\.").stream()
				.anyMatch(el -> str.matches("^\\d{4}" + el + "\\d{2}" + el + "\\d{2}$"));
	}
}