/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : CheckClassCommentRule.java
*@FileTitle : CheckClassCommentRule
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/
package org.sonar.samples.java.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.samples.java.utils.AssignmentExpressionTreeUtils;
import org.sonar.samples.java.utils.CommonMessage;
import org.sonar.samples.java.utils.IfStatementTreeUtils;
import org.sonar.samples.java.utils.NullDereferencingUtils;
import org.sonar.samples.java.utils.VariableTreeUtils;

/**
 * This rule detects invalid class comment
 * 
 * @author tathienphuoc
 * @see CheckNullDereferencingRule
 * @since J2EE 1.6
 */
@Rule(key = "CheckNullDereferencingRule")
public class CheckNullDereferencingRule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;
	private static List<String> IfConditions = new ArrayList<>();
	private static HashMap<String,Boolean> arguments = new HashMap<>();
	private static String prefix = "[R4J][Null dereferencing][%s]";

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		IfStatementTreeUtils.init(context);
		scan(context.getTree());
	}

	private static void clear() {
		IfConditions.clear();
		arguments.clear();
	}

	@Override
	public void visitMethod(MethodTree tree) {
		try {

			clear();
			IfStatementTreeUtils.clear();
			super.visitMethod(tree);
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		try {
			super.visitMethodInvocation(tree);
			if (needToCheck(tree)) {
				if (IfStatementTreeUtils.hanleNullPointerException(tree.firstToken().text(), tree.firstToken().line(),
						tree.firstToken().column()))
					System.out.println(tree.firstToken().line() + " need to check and check is ok");
				else {

					context.reportIssue(this, tree, String.format(prefix, getLine(tree)));
//					System.out.println(tree.firstToken().line() + " need to check and no no");
					System.out.println(tree.firstToken().line() + " "+String.format(prefix, getLine(tree)));
				}
			} else {
				System.out.println(tree.firstToken().line() + " don't need to check");
			}
			System.out.println();
			System.out.println();
			System.out.println();
		}

		catch (Exception e) {
//			e.printStackTrace();
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	
	private boolean needToCheck(MethodInvocationTree tree) {
		String txt = getLine(tree);
		String name=tree.firstToken().text();
		System.out.println(txt);
		boolean match = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*\\.").matcher(txt).find();
//		System.out.println("visitMethodInvocation " + tree.firstToken().line() + " " + txt + match + " "
//				+ tree.firstToken().text());
		return match && arguments.containsKey(name)&&!arguments.get(name);

	}

	private boolean isMethod(String txt) {
		return Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*\\(.*\\)$").matcher(txt).find();
	}
	
	@Override
	public void visitVariable(VariableTree tree) {
		super.visitVariable(tree);// neu da error tu cap 3 can check cap 4 kh ????

		try {
			String name = tree.simpleName().name();
			if(VariableTreeUtils.hasValue(tree)) {
				arguments.put(name, true);
			}else {
				arguments.put(name, false);
			}
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}
	
	
	@Override
	public void visitAssignmentExpression(AssignmentExpressionTree tree) {
		super.visitAssignmentExpression(tree);
		try {
			String value = AssignmentExpressionTreeUtils.getLine(context, tree);
			String variableName = tree.variable().firstToken().text();
			if ("null".equals(value)||isMethod(value)) {
//				NullDereferencingUtils.removeVariable(variableName);
				arguments.put(variableName, false);
			} else {
//				NullDereferencingUtils.addVariable(variableName);
				arguments.put(variableName, true);
			}
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}
	
	private String getLine(MethodInvocationTree tree) {
		int firstLine = tree.firstToken().line() - 1;
		int lastLine = tree.lastToken().line() - 1;
		int firstCol = tree.firstToken().column();
		int lastCol = tree.lastToken().column() + tree.lastToken().text().length();
		String line;
		if (firstLine == lastLine) {
			line = context.getFileLines().get(firstLine).substring(firstCol, lastCol);
		} else {
			StringBuilder str = new StringBuilder(context.getFileLines().get(firstLine).substring(firstCol));
			firstLine++;
			while (firstLine < lastLine) {
				str.append(context.getFileLines().get(firstLine));
				firstLine++;
			}
			line = str.append(context.getFileLines().get(lastLine).subSequence(0, lastCol)).toString();
		}
		return line.replaceAll("\\s+", "");
	}

	@Override
	public void visitIfStatement(IfStatementTree tree) {
		try {
			IfStatementTreeUtils.add(tree);
			super.visitIfStatement(tree);
		} catch (Exception e) {
			e.printStackTrace();
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

}