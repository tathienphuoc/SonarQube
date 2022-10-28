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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.samples.java.utils.CommonMessage;

/**
 * This rule detects invalid class comment
 * 
 * @author tathienphuoc
 * @see CheckFloatingPointValues
 * @since J2EE 1.6
 */
@Rule(key = "CheckFloatingPointValues")
public class CheckFloatingPointValues extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;
	private Set<String> floatVars = new HashSet<>();
	private Map<String, Boolean> localFloatVars = new HashMap<>();
	private static String errMgs = String.format("[R4J][Floating Point Values][%s]", CommonMessage.COMPARE_FLOAT_VALUE);

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	@Override
	public void visitClass(ClassTree tree) {
		try {
			tree.members().forEach(m -> {
				if (m.getClass().getName().endsWith(".VariableTreeImpl")) {
					VariableTree variable = (VariableTree) m;
					if (isFloatType(variable))
						floatVars.add(variable.simpleName().name());
				}

			});
			super.visitClass(tree);
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitVariable(VariableTree tree) {
		try {
			localFloatVars.put(tree.simpleName().name(), isFloatType(tree));
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitMethod(MethodTree tree) {
		try {
			localFloatVars.clear();
			super.visitMethod(tree);
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	public boolean isFloatType(VariableTree variable) {
		return Arrays.asList("double", "Double", "float", "Float").contains(variable.symbol().type().name());
	}

	@Override
	public void visitBinaryExpression(BinaryExpressionTree tree) {

		try {
			super.visitBinaryExpression(tree);
			if (!"==".equals(tree.operatorToken().text())) {
				return;
			}
			String leftOperand = getOperand(tree.leftOperand());
			String rightOperand = getOperand(tree.rightOperand());

			String message = tree.firstToken().line() + " compare ngu " + leftOperand + rightOperand;
			if (isFloatValue(leftOperand) || isFloatValue(rightOperand)) { // co number la ngu
				System.out.println(message);
				context.reportIssue(this, tree, errMgs);
			} else {
				if (localFloatVars.containsKey(leftOperand)) {
					if (localFloatVars.get(leftOperand)) {
						System.out.println(message);
						context.reportIssue(this, tree, errMgs);
					}
				} else if (floatVars.contains(leftOperand)) {
					if (localFloatVars.containsKey(rightOperand)) {
						if (localFloatVars.get(rightOperand)) {
							System.out.println(message);
							context.reportIssue(this, tree, errMgs);
						}
					} else if (floatVars.contains(rightOperand)) {
						System.out.println(message);
						context.reportIssue(this, tree, errMgs);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}

	}

	private boolean isFloatValue(String str) {
		return str.matches("^[-+]?[0-9]*\\.[0-9]+$");
	}

	private String getOperand(ExpressionTree tree) {
		int firstLine = tree.firstToken().line() - 1;
		int lastLine = tree.lastToken().line() - 1;
		int firstCol = tree.firstToken().column() - 1;
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
		return line.replaceAll("\\s+", "").replaceAll("^[\\(=]", "").replaceAll("[=\\)]$", "");
	}

}
