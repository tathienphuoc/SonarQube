/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : CheckMethodCommentRule.java
*@FileTitle : CheckMethodCommentRule
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
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.samples.java.utils.CommonMessage;
import org.sonar.samples.java.utils.MethodCommentUtils;

/**
 * This rule detects invalid method comment
 * 
 * @author tathienphuoc
 * @see CheckClassCommentRule
 * @since J2EE 1.6
 */
@Rule(key = "CheckMethodCommentRule")
public class CheckMethodCommentRule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;

	/**
	 * Implement scanFile method from JavaFileScanner
	 * 
	 * @param JavaFileScannerContext context
	 */
	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	/**
	 * Override visitMethod method from BaseTreeVisitor
	 * 
	 * @param MethodTree tree
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		try {
			MethodCommentUtils.check(context, tree);
			List<String> errMsgs = MethodCommentUtils.getErrMsg();
			if (errMsgs.isEmpty()) {
				System.out.println("ok");
			}
			for (int i = 0; i < errMsgs.size(); i++) {
				System.out.println(errMsgs.get(i));
				context.reportIssue(this, tree, errMsgs.get(i));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}
}