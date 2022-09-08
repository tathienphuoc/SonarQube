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
 * @author tathienphuoc
 * @see CommonMessage
 * @since J2EE 1.6
 */
public final class CommonMessage {
	public static final String FILE_ABSENT_COMMENTS = "[R4J][File comment] Missing file comments";
	
	public static final String CLASS_ABSENT_COMMENTS = "[R4J][Class comment] Missing class comments";
	public static final String CLASS_ABSENT_DESCRIPTION = "[R4J][Class comment] Missing description section";
	public static final String CLASS_ABSENT_AUTHOR = "[R4J][Class comment] Missing author section";
	public static final String CLASS_ABSENT_SEE = "[R4J][Class comment] Missing see section";
	public static final String CLASS_ABSENT_SINCE = "[R4J][Class comment] Missing since section";
	public static final String CLASS_ABSENT_EMPTY_LINE = "[R4J][Class comment] Missing empty line after description section";
	
	public static final String METHOD_ABSENT_COMMENTS = "[R4J][Method comment] Missing method comments";
	public static final String METHOD_ABSENT_DESCRIPTION = "[R4J][Method comment] Missing description section";
	public static final String METHOD_ABSENT_PARAM = "[R4J][Method comment] Missing parameter section";
	public static final String METHOD_ABSENT_RETURN = "[R4J][Method comment] Missing return section";
	public static final String METHOD_ABSENT_EXCEPTION = "[R4J][Method comment] Missing exception section";
	public static final String METHOD_ABSENT_EMPTY_LINE = "[R4J][Method comment] Missing empty line after description section";

	public static final String FILE_INVALID_COMMENTS_FORMAT = "[R4J][File comment] Please checks rule description for more detail";
	
	public static final String CLASS_INVALID_COMMENTS_FORMAT = "[R4J][Class comment] Please checks rule description for more detail";
	public static final String CLASS_INVALID_ORDER_FORMAT = "[R4J][Class comment] Class comments format: \n\t* Description section \n\t* [Empty line] \n\t* Author section \n\t* See section \n\t* Since section";
	public static final String CLASS_INVALID_DESCRIPTION_FORMAT = "[R4J][Class comment] Description must end with <br/>";
	public static final String CLASS_INVALID_AUTHOR_FORMAT = "[R4J][Class comment] \'AUTHOR\' must follow the format: @author [Name]";
	public static final String CLASS_INVALID_SEE_FORMAT = "[R4J][Class comment] \'SEE\' must follow the format: @see [FileName]";
	public static final String CLASS_INVALID_SINCE_FORMAT = "[R4J][Class comment] \'SINCE\' must follow the format: @since [Platform] [Version]";
	public static final String CLASS_INVALID_LINE_FORMAT = "[R4J][Class comment] Line must start with * and one space";

	public static final String METHOD_INVALID_COMMENTS_FORMAT = "[R4J][Method comment] Method comment must start with /** and end with */";
	public static final String METHOD_INVALID_ORDER_FORMAT = "[R4J][Method comment] Method comments format: \n\t* Description section \n\t* [Empty line] \n\t* Parameter section \n\t* Return section \n\t* Exception section";
	public static final String METHOD_INVALID_PARAM_FORMAT = "[R4J][Method comment] \'PARAMETER\' must follow the format: @param [Type] [Name]";
	public static final String METHOD_INVALID_RETURN_FORMAT = "[R4J][Method comment] \'RETURN\' must follow the format: @return [Type]";
	public static final String METHOD_INVALID_EXCEPTION_FORMAT = "[R4J][Method comment] \'EXCEPTION\' must follow the format: @exception [Type] ( or @throws [Type] ) ";
    public static final String METHOD_INVALID_LINE_FORMAT = "[R4J][Method comment] Line must start with * and one space";
	
	public static final String METHOD_INVALID_PARAM = "[R4J][Method comment] Checking parameter section";
	public static final String METHOD_INVALID_RETURN = "[R4J][Method comment] Checking return section";
	public static final String METHOD_INVALID_EXCEPTION = "[R4J][Method comment] Checking exception section";


	public static final String CLASS_TOO_MANY_COMMENTS = "[R4J][Class comment] Too many class comments";
	public static final String CLASS_TOO_MANY_AUTHOR = "[R4J][Class comment] Too many author comments";
	public static final String CLASS_TOO_MANY_SEE = "[R4J][Class comment] Too many see comments";
	public static final String CLASS_TOO_MANY_SINCE = "[R4J][Class comment] Too many since comments";

	public static final String METHOD_TOO_MANY_COMMENTS = "[R4J][Method comment] Too many method comments";
	public static final String METHOD_TOO_MANY_RETURN = "[R4J][Method comment] Too many return comments";
	public static final String METHOD_TOO_MANY_PARAM = "[R4J][Method comment] Too many param comments";
	public static final String METHOD_TOO_MANY_EXCEPTION = "[R4J][Method comment] Too many exception comments";
	
	
	public static final String METHOD_LACK_OF_PARAM = "[R4J][Method comment] Lack of param comment";
	public static final String METHOD_LACK_OF_EXCEPTION = "[R4J][Method comment] Lack of exception comment";

	public static final String CLASS_REMOVE_EMPTY_LINE = "[R4J][Class comment] Remove empty lines";
	public static final String METHOD_REMOVE_EMPTY_LINE = "[R4J][Method comment] Remove empty lines";
	
	public static final String NOT_SUPPORT = "WARNING: Not support. Can't handle this case";
}
