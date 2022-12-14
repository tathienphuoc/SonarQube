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

import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.TryStatementTree;
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
 * @see CheckNullDereferencingRuleOld
 * @since J2EE 1.6
 */
@Rule(key = "CheckNullDereferencingRule")
public class CheckNullDereferencingRuleOld extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		NullDereferencingUtils.init(context);
		IfStatementTreeUtils.init(context);
		scan(context.getTree());
	}

	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		super.visitMethodInvocation(tree);
		int line = tree.firstToken().line();
		try {
			NullDereferencingUtils.check(tree);
			List<String> errMsgs = NullDereferencingUtils.getErrMsg();
			if (errMsgs.isEmpty()) {
				System.out.println(line + " ok");
			}
			for (int i = 0; i < errMsgs.size(); i++) {
				System.out.println(line + " " + errMsgs.get(i));
				context.reportIssue(this, tree, errMsgs.get(i));
			}
		} catch (Exception e) {
			System.out.println(line + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}

	}

	@Override
	public void visitIfStatement(IfStatementTree tree) {
		try {
			IfStatementTreeUtils.add(tree);
			super.visitIfStatement(tree);
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitBinaryExpression(BinaryExpressionTree tree) {// tree.leftOperand().firstToken().text()
		super.visitBinaryExpression(tree);

		System.out.println(tree.firstToken().line() + "  " + getBinaryExpression(tree.leftOperand())
				+ " visitBinaryExpression " + getBinaryExpression(tree.rightOperand()));
	}

	private String getBinaryExpression(ExpressionTree tree) {
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
		return line.replaceAll("\\s+", "").replaceAll("^\\(", "").replaceAll("\\)$", "");
	}

	@Override
	public void visitVariable(VariableTree tree) {
		super.visitVariable(tree);// neu da error tu cap 3 can check cap 4 kh ????

		try {
			String name = tree.simpleName().name();
			if (NullDereferencingUtils.containArgument(name)) {
				return;
			} else if (VariableTreeUtils.hasValue(tree)) {
				NullDereferencingUtils.addVariable(name);
			}
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitMethod(MethodTree tree) {
		try {
			NullDereferencingUtils.clear();

			if (tree.throwsClauses().stream().anyMatch(
					ex -> "NullPointerException".equals(ex.toString()) || "Exception".equals(ex.toString()))) {
				NullDereferencingUtils.enableThrowException();
			}
			tree.parameters().forEach(p -> NullDereferencingUtils.addArgument(p.simpleName().name()));

			super.visitMethod(tree);
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitTryStatement(TryStatementTree tree) {
		try {
			if (tree.catches().stream().anyMatch(c -> {
				String exception = c.parameter().symbol().declaration().firstToken().text();
				return "Exception".equals(exception) || "NullPointerException".equals(exception);
			})) {
				NullDereferencingUtils.addTryCatche(tree);
			}
			super.visitTryStatement(tree);
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
			if ("null".equals(value)) {
				NullDereferencingUtils.removeVariable(variableName);
			} else {
				NullDereferencingUtils.addVariable(variableName);
			}
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}
}

//^[a-zA-Z][a-zA-Z0-9_]*(\(.*\)){0,1}\.[a-zA-Z][a-zA-Z0-9_]*

//if getLine start with "" or (-> kh can check

//visit method 
//	clear set var, type
//	thing.callMethod()
//	just care about "thing"
//	Type : value (constant: "", new Object, int, ... things we know it's value), null (or value has null value, don't have value), method 
//	
//	(""+)
//	
//
//case 1:
//	method 
