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

import java.util.HashSet;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.samples.java.utils.CommonMessage;

/**
 * This rule detects invalid class comment
 * 
 * @author tathienphuoc
 * @see DuplicatedVariableNamesRule
 * @since J2EE 1.6
 */
@Rule(key = "CheckForbiddenAssignmentStatementForPrivateArrayFieldRule")
public class CheckForbiddenAssignmentStatementForPrivateArrayFieldRule extends BaseTreeVisitor
		implements JavaFileScanner {

	private static String prefix = "[R4J][Forbidden Getters or Setters Array In Event Files][%s]";
	private static Set<String> variables = new HashSet<>();
	private JavaFileScannerContext context;

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
					if (variable.symbol().type().name().endsWith("[]")) {
						variables.add(variable.simpleName().name());
					}
				}

			});
			super.visitClass(tree);
		} catch (Exception e) {

			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitMethod(MethodTree tree) {
		try {
			int line = tree.firstToken().line();
			String methodName = tree.simpleName().name();
			if (tree.returnType() == null) {
				System.out.println(line + " ok");
			} else if (tree.returnType().symbolType().name().endsWith("[]")
					&& variables.stream().anyMatch(v -> methodName.equalsIgnoreCase("get" + v))) {
				System.out.println(line + " " + CommonMessage.REMOVE_GETTER);
				context.reportIssue(this, tree, String.format(prefix, CommonMessage.REMOVE_GETTER));
			} else if (tree.returnType().symbolType().isVoid()
					&& variables.stream().anyMatch(v -> methodName.equalsIgnoreCase("set" + v))) {
				context.reportIssue(this, tree, String.format(prefix, CommonMessage.REMOVE_SETTER));
				System.out.println(line + " " + CommonMessage.REMOVE_SETTER);
			} else {
				System.out.println(line + " ok");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

}