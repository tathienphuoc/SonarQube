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
	private static ArrayList<String> startWiths = new ArrayList<String>();
	private static String className="";
	static {
		startWiths.add("*Copyright(c)");
		startWiths.add("*@FileName : ");
		startWiths.add("*@FileTitle : ");
		startWiths.add("*Open Issues :");
		startWiths.add("*Change history :");
		startWiths.add("*@LastModifyDate :");
		startWiths.add("*@LastModifier :");
		startWiths.add("*@LastVersion :");
	}
	//Chỉ check các file HTMLAction,.....
	private static void initValue(JavaFileScannerContext context, ClassTree tree) throws Exception{
		List<String> fileLines=	context.getFileLines();//check lại trường hợp mà comment có dòng rỗng, kh có gì cả, chỉ là dòng trắng
		className=tree.simpleName().name();
		if(!fileLines.isEmpty()&&fileLines.get(0).startsWith("/*==")) {
			for(int i=1;i<fileLines.size();i++) {
				String value=fileLines.get(i);
				if(value.endsWith("*/")){
					return;
				}
				fileComments.add(value);
			}
		}else {
			throw new Exception(CommonMessage.FILE_ABSENT_COMMENTS);
		}
	}

	public static String getDocErrMsgs(JavaFileScannerContext context, ClassTree tree) {
		try {
			initValue(context,tree);
		} catch (Exception e) {
			return e.getMessage();
		}
		if(startWiths.size()>fileComments.size()) {
			return CommonMessage.FILE_INVALID_COMMENTS_FORMAT;
		}
		for(int i=0;i<startWiths.size();i++) {
			if(!fileComments.get(i).startsWith(startWiths.get(i))) {
				return CommonMessage.FILE_INVALID_COMMENTS_FORMAT;
			}
		}
		String fileName=startWiths.get(1)+className+".java";
		String fileTitle=startWiths.get(2)+className;
		return fileName.equals(fileComments.get(1).replaceAll("\\s+$", ""))&&fileTitle.equals(fileComments.get(2).replaceAll("\\s+$", ""))?"":CommonMessage.FILE_INVALID_COMMENTS_FORMAT;
	}
}