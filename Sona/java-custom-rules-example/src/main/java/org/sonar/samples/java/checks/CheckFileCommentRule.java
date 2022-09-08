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

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.samples.java.utils.CommonMessage;
import org.sonar.samples.java.utils.FileCommentUtils;

/**
 * This rule detects invalid class comment
 * 
 * @author tathienphuoc
 * @see CheckFileCommentRule
 * @since J2EE 1.6
 */
@Rule(key = "CheckFileCommentRule")
public class CheckFileCommentRule extends BaseTreeVisitor implements JavaFileScanner {

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
	 * Override visitClass method from BaseTreeVisitor
	 * 
	 * @param ClassTree tree
	 */
	@Override
	public void visitClass(ClassTree tree) {
		try {
			String errMsg = FileCommentUtils.getDocErrMsgs(context, tree);
			if (!errMsg.isEmpty()) {
				context.reportIssue(this, tree, errMsg);
			}
		} catch (Exception e) {
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}
}