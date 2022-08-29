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
	private static ArrayList<String> fileComments = new ArrayList<String>();
	private static ArrayList<String> regex = new ArrayList<String>();
	private static String className = "";
	static {
		regex.add("^\\*\\s{0,1}Copyright\\(c\\) \\d{4} CyberLogitec");
		regex.add("^\\*\\s{0,1}@FileName\\s{0,1}:\\s{0,1}");
		regex.add("^\\*\\s{0,1}@FileTitle\\s{0,1}:\\s{0,1}");
		regex.add("^\\*\\s{0,1}Open Issues\\s{0,1}:\\s{0,1}.*");
		regex.add("^\\*\\s{0,1}Change history\\s{0,1}:\\s{0,1}.*");
		regex.add("^\\*\\s{0,1}@LastModifyDate\\s{0,1}:\\s{0,1}(\\d{4}\\-\\d{2}\\-\\d{2}){0,1}$");
		regex.add("^\\*\\s{0,1}@LastModifier\\s{0,1}:\\s{0,1}.*");
		regex.add("^\\*\\s{0,1}@LastVersion\\s{0,1}:\\s{0,1}.*");

	}

	// Chỉ check các file HTMLAction,.....
	private static void initValue(JavaFileScannerContext context, ClassTree tree) throws Exception {
		List<String> fileLines = context.getFileLines();// check lại trường hợp mà comment có dòng rỗng, kh có gì cả,
														// chỉ là dòng trắng
		className = tree.symbol().declaration().simpleName().name(); 
		regex.set(1, regex.get(1) + className + "\\.java");
		regex.set(2, regex.get(2) + className);
		if (!fileLines.isEmpty() && fileLines.get(0).startsWith("/*==")) {
			for (int i = 1; i < fileLines.size(); i++) {
				String value = fileLines.get(i).trim();
				if (value.endsWith("*/")) {
					return;
				}
				fileComments.add(value);
			}
		} else {
			throw new Exception(CommonMessage.FILE_ABSENT_COMMENTS);
		}
	}

	public static String getDocErrMsgs(JavaFileScannerContext context, ClassTree tree) {
		try {
			initValue(context, tree);
		} catch (Exception e) {
			return e.getMessage();
		}
		if (regex.size() > fileComments.size()) {
			return CommonMessage.FILE_INVALID_COMMENTS_FORMAT;
		}
		for (int i = 0; i < regex.size(); i++) {
			if (!Pattern.compile(regex.get(i)).matcher(fileComments.get(i)).find()) {
				return CommonMessage.FILE_INVALID_COMMENTS_FORMAT;
			}
		}
		return "";
	}
}