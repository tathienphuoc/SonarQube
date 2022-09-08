/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : CommonMessage.java
*@FileTitle : CommonMessage
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/
package org.sonar.samples.java.utils;

/**
 * Common messages
 * 
 
 @author tathienphuoc
 * @see CommonMessage_bk
 * @since J2EE 1.6
 */
public final class CommonMessage_bk {
	public static final String FILE_ABSENT_COMMENTS = "[R4J][File comment][Missing file comments]";
	
	public static final String CLASS_ABSENT_COMMENTS = "[R4J][Class comment][Missing class comments]";
	public static final String CLASS_ABSENT_DESCRIPTION = "[R4J][Class comment][Missing description section]";
	public static final String CLASS_ABSENT_AUTHOR = "[R4J][Class comment][Missing author section]";
	public static final String CLASS_ABSENT_SEE = "[R4J][Class comment][Missing see section]";
	public static final String CLASS_ABSENT_SINCE = "[R4J][Class comment][Missing since section]";
	public static final String CLASS_ABSENT_EMPTY_LINE = "[R4J][Class comment][Missing empty line after description section]";
	
//	public static final String METHOD_ABSENT_COMMENTS = "Missing method comments]";
	public static final String METHOD_ABSENT_COMMENTS = "Missing method comments";
	public static final String METHOD_ABSENT_DESCRIPTION = "Missing description section";
	public static final String METHOD_ABSENT_PARAM = "Missing parameter section";
	public static final String METHOD_ABSENT_RETURN = "Missing return section";
	public static final String METHOD_ABSENT_EXCEPTION = "Missing exception section";
	public static final String METHOD_ABSENT_EMPTY_LINE = "Missing empty line after description section";

	public static final String FILE_INVALID_COMMENTS_FORMAT = "[R4J][File comment][Please checks rule description for more detail]";
	
	public static final String CLASS_INVALID_COMMENTS_FORMAT = "[R4J][Class comment][Please checks rule description for more detail]";
	public static final String CLASS_INVALID_ORDER_FORMAT = "[R4J][Class comment][Class comments format: \n\t* Description section \n\t* [Empty line] \n\t* Author section \n\t* See section \n\t* Since section]";
//	public static final String CLASS_INVALID_DESCRIPTION_FORMAT = "[R4J][Class comment][Description must end with </br>]";
	public static final String INVALID_AUTHOR_FORMAT = "[\'AUTHOR\' must follow the format: @author [Name]]";//COMMON
	public static final String CLASS_INVALID_SEE_FORMAT = "[R4J][Class comment][\'SEE\' must follow the format: @see [FileName]]";
	public static final String CLASS_INVALID_SINCE_FORMAT = "[R4J][Class comment][\'SINCE\' must follow the format: @since [Platform] [Version]]";
//	public static final String CLASS_INVALID_LINE_FORMAT = "[R4J][Class comment][Line must start with one asterisk and one whitespace]";

	public static final String METHOD_INVALID_COMMENT_FORMAT = "Method comment must start with /** and end with */\"";
	public static final String METHOD_NOT_SORTED = "Method comment is not sorted";
	public static final String METHOD_INVALID_ORDER_FORMAT = "Method comments format: \n\t* Description section \n\t* [Empty line] \n\t* Parameter section \n\t* Return section \n\t* Exception section";
	public static final String INVALID_DESCRIPTION_FORMAT = "Description must end with <br>";//COMMON
	public static final String METHOD_INVALID_PARAM_FORMAT = "\'PARAMETER\' must follow the format: @param [Type] [Name]";
	public static final String METHOD_INVALID_RETURN_FORMAT = "\'RETURN\' must follow the format: @return [Type]";
	public static final String METHOD_INVALID_EXCEPTION_FORMAT = "\'EXCEPTION\' must follow the format: @exception [Type] ( or @throws [Type] )";
    public static final String INVALID_LINE_FORMAT = "Line must start with one asterisk and one whitespace";//common
	
	public static final String METHOD_INVALID_PARAM = "Please checks parameter section";
	public static final String METHOD_INVALID_RETURN = "Please checks return section";
	public static final String METHOD_INVALID_EXCEPTION = "Please checks exception section";


	public static final String CLASS_TOO_MANY_COMMENTS = "[R4J][Class comment][Too many class comments]";
	public static final String CLASS_TOO_MANY_AUTHOR = "[R4J][Class comment][Too many author comments]";
	public static final String CLASS_TOO_MANY_SEE = "[R4J][Class comment][Too many see comments]";
	public static final String CLASS_TOO_MANY_SINCE = "[R4J][Class comment][Too many since comments]";

	public static final String METHOD_TOO_MANY_COMMENTS = "Too many method comments";
	public static final String METHOD_TOO_MANY_RETURN = "Too many return comments";
	public static final String METHOD_TOO_MANY_PARAM = "Too many param comments";
	public static final String METHOD_TOO_MANY_EXCEPTION = "Too many exception comments";
	
	
	public static final String METHOD_LACK_OF_PARAM = "Lack of param comment";
	public static final String METHOD_LACK_OF_EXCEPTION = "Lack of exception comment";

//	public static final String CLASS_REMOVE_EMPTY_LINE = "[R4J][Class comment][Please remove empty line]";
	public static final String REMOVE_EMPTY_LINE = "Please remove empty line";//common
	public static final String REMOVE_EMPTY_LINE_WITHOUT_ASTERISK = "Please remove empty line without asterisk";//common
	public static final String METHOD_REMOVE_EXTRA_WHITESPACE = "Please remove extra whitespace and tab space";
	
	public static final String METHOD_CHECK_RULE = "Please checks rule description for more detail";
	
	public static final String NOT_SUPPORT = "WARNING: Not support. Can't handle this case";
}
