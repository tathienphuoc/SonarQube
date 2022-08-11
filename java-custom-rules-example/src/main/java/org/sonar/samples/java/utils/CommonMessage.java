package org.sonar.samples.java.utils;

public final class CommonMessage {
	public static final String ABSENT_METHOD_COMMENTS = "Missing method comments";
	public static final String ABSENT_DESCRIPTION = "Missing description section";
	public static final String ABSENT_PARAM = "Missing parameter section";
	public static final String ABSENT_RETURN = "Missing return section";
	public static final String ABSENT_EXCEPTION = "Missing exception section";
	public static final String ABSENT_EMPTY_LINE = "Missing empty line after description section";

	public static final String INVALID_METHOD_COMMENTS_FORMAT = "Method comment must start with /** and end with */";
	public static final String INVALID_ORDER_FORMAT = "Method comments format: \n\t* Description section \n\t* [Empty line] \n\t* Parameter section \n\t* Return section \n\t* Exception section";
	public static final String INVALID_PARAM_FORMAT = "\'PARAMETER\' must follow format: @param [Type] [Name]";
	public static final String INVALID_RETURN_FORMAT = "\'RETURN\' must follow format: @return [Type]";
	public static final String INVALID_EXCEPTION_FORMAT = "\'EXCEPTION\' must follow format: @exception [Type] ( or @throws [Type] ) ";

	public static final String INVALID_PARAM = "Checking parameter section";
	public static final String INVALID_RETURN = "Checking return section";
	public static final String INVALID_EXCEPTION = "Checking exception section";
	public static final String INVALID_LINE = "Line must start with * and one space";

	public static final String TOO_MANY_METHOD_COMMENTS = "Too many method comments";
	public static final String TOO_MANY_RETURN = "Too many return comment";
	public static final String TOO_MANY_PARAM = "Too many param comment";
	public static final String TOO_MANY_EXCEPTION = "Too many exception comment";
	
	public static final String LACK_OF_PARAM = "Lack of param comment";
	public static final String LACK_OF_EXCEPTION = "Lack of exception comment";

	public static final String REMOVE_EMPTY_LINE = "Remove empty lines";
}
