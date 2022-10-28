package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.plugins.java.api.tree.MethodInvocationTree;

public class ForbiddenStringEqualExpressionUtils {
	private static List<String> checkList = Arrays.asList("equals", "equalsIgnoreCase", "compareTo",
			"compareToIgnoreCase");
	private static String prefix = "[R4J][Forbidden string equal expression][%s]";
	private static List<String> errMsgs;

	public enum Type {
		EXPRESSION, CONSTANT, NULL;
	}

	public static void check(MethodInvocationTree tree) {
		errMsgs = new ArrayList<>();
		Type object;
		Type argument;
		if (!checkList.contains(tree.symbol().name()))
			return;
		object = tree.firstToken().text().startsWith("\"") ? Type.CONSTANT : Type.EXPRESSION;
		if (tree.arguments().get(0).symbolType().is("<nulltype>")) {
			argument = Type.NULL;
		} else {
			argument = tree.arguments().get(0).asConstant().isPresent() ? Type.CONSTANT : Type.EXPRESSION;
		}
		if (argument.equals(Type.NULL)) {
			errMsgs.add(CommonMessage.DONT_COMPARE_TO_NULL);
		} else if (object.equals(Type.EXPRESSION) && argument.equals(Type.CONSTANT)) {
			errMsgs.add(CommonMessage.CONSTANT_FIRST);
		}
	}

	public static List<String> getErrMsg() {
		return errMsgs.stream().map(errMsg -> String.format(prefix, errMsg)).collect(Collectors.toList());
	}
}
/*
 * 1. a.equals(b) a: AVOID_NULL_POINTER
 * 
 * 2. a.equals("String a") Noncompliant //constant must compare to variable
 * 
 * 5 "String a".equals(null) Noncompliant //don't compare to null
 * 
 * 6 a.equals(null) Noncompliant//don't compare to null, AVOID_NULL_POINTER
 */

//if(arg is null ) {
//	add not compare null
//}
//if(object is variable) {
//	if(variables not containt object) {
//		if(arg is constaint) {
//			add CONSTANT_FIRST
//		}else {			
//			add avoid null pointer
//		}
//	}
//}

/*
 * 1. a.equals(b) a: AVOID_NULL_POINTER
 * 
 * 3. "String a".equals(a) Compliant
 * 
 * 4. "String a".equals("String a") Compliant
 * 
 * 2. a.equals("String a") Noncompliant //constant must compare to variable
 * 
 * 5 "String a".equals(null) Noncompliant //don't compare to null
 * 
 * 6 a.equals(null) Noncompliant//don't compare to null AVOID_NULL_POINTER
 */