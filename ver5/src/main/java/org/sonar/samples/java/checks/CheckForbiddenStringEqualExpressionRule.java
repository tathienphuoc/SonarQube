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
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.samples.java.utils.CommonMessage;
import org.sonar.samples.java.utils.ForbiddenStringEqualExpressionUtils;

/**
 * This rule detects invalid class comment
 * 
 * @author tathienphuoc
 * @see CheckForbiddenStringEqualExpressionRule
 * @since J2EE 1.6
 */
@Rule(key = "CheckForbiddenStringEqualExpressionRule")
public class CheckForbiddenStringEqualExpressionRule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		super.visitMethodInvocation(tree);
		int line = tree.firstToken().line();
		try {
			ForbiddenStringEqualExpressionUtils.check(tree);
			List<String> errMsgs = ForbiddenStringEqualExpressionUtils.getErrMsg();
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
}