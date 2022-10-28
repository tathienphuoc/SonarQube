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
 * 
 * @author tathienphuoc
 * @see CommonMessage
 * @since J2EE 1.6
 */
public final class CommonMessage {

	public static final String ABSENT_FILE_COMMENT = "Missing file comment";
	public static final String ABSENT_CLASS_COMMENT = "Missing class comment";
	public static final String ABSENT_METHOD_COMMENT = "Missing method comment";
	public static final String ABSENT_DESCRIPTION = "Missing description section";
	public static final String ABSENT_PARAM = "Missing parameter section";
	public static final String ABSENT_RETURN = "Missing return section";
	public static final String ABSENT_AUTHOR = "Missing author section";
	public static final String ABSENT_SEE = "Missing see section";
	public static final String ABSENT_SINCE = "Missing since section";
	public static final String ABSENT_EXCEPTION = "Missing exception section";
	public static final String ABSENT_EMPTY_LINE = "Missing empty line after description section";

	public static final String ABSENT_LOG_ERROR = "Please log error using org.apache.log4j.Logger library";
	public static final String ABSENT_THROW_NEW = "Please throw an Exception or remove throws";

	public static final String INVALID_COMMENT_FORMAT = "Comment must start with /** and end with */\"";// COMMON
	public static final String METHOD_NOT_SORTED = "Method comment is not sorted";
	public static final String INVALID_DESCRIPTION_FORMAT = "Description must end with <br>";// COMMON
	public static final String INVALID_PARAM_FORMAT = "\'PARAMETER\' must follow the format: @param [Type] [Name]";
	public static final String INVALID_RETURN_FORMAT = "\'RETURN\' must follow the format: @return [Type]";
	public static final String INVALID_EXCEPTION_FORMAT = "\'EXCEPTION\' must follow the format: @exception [Type] ( or @throws [Type] )";
	public static final String INVALID_AUTHOR_FORMAT = "\'AUTHOR\' must follow the format: @author [Name]";// COMMON
	public static final String INVALID_LINE_FORMAT = "Line must start with one asterisk and one whitespace";// common

	public static final String INVALID_PARAM = "Please check parameter section";
	public static final String INVALID_RETURN = "Please check return section";
	public static final String INVALID_EXCEPTION = "Please check exception section";

	public static final String TOO_MANY_FILE_COMMENTS = "Too many file comments";
	public static final String TOO_MANY_CLASS_COMMENTS = "Too many class comments";
	public static final String TOO_MANY_METHOD_COMMENTS = "Too many method comments";
	public static final String TOO_MANY_RETURN = "Too many return comments";
	public static final String TOO_MANY_PARAM = "Too many param comments";
	public static final String TOO_MANY_EXCEPTION = "Too many exception comments";
	public static final String TOO_MANY_AUTHOR = "Too many author comments";
	public static final String TOO_MANY_SEE = "Too many see comments";
	public static final String TOO_MANY_SINCE = "Too many since comments";

	public static final String DONT_COMPARE_TO_NULL = "Don't compare to null";
	public static final String CONSTANT_FIRST = "Please put the constant before the expression in a comparison";
	public static final String AVOID_NULL_POINTER = "%s: Please avoid NullPointerException";
	public static final String COMPARE_FLOAT_VALUE = "Don't use equality (==) to compare float values.";

	public static final String LACK_OF_PARAM = "Lack of param comment";
	public static final String LACK_OF_EXCEPTION = "Lack of exception comment";

	public static final String REMOVE_EMPTY_LINE = "Please remove empty line";// common
	public static final String REMOVE_EMPTY_LINE_WITHOUT_ASTERISK = "Please remove empty line without asterisk";// common

	public static final String REMOVE_SETTER = "Please remove setter method";// common
	public static final String REMOVE_GETTER = "Please remove getter method";// common

	public static final String REMOVE_DEBUG_STATEMENT = "Please remove printStackTrace()";

	public static final String MAYBE_NULL = "%s could be null";

	public static final String CHECK_RULE = "Please check rule description for more detail";

	public static final String NOT_SUPPORT = "WARNING: Not support. Can't handle this case";

}
