/*
 * Copyright (C) 2012-2022 SonarSource SA - mailto:info AT sonarsource DOT com
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package org.sonar.samples.java.checks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.samples.java.utils.MethodCommentUtils;

@Rule(key = "CheckMethodCommentRule")
public class CheckMethodCommentRule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;
	
	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	@Override
	public void visitMethod(MethodTree tree) {

		List<String> errMsgs = MethodCommentUtils.getDocErrMsg(context,tree);
		for(int i=0;i<errMsgs.size();i++) {
			context.reportIssue(this, tree, errMsgs.get(i));
			System.out.println( errMsgs.get(i));
		}
		if(errMsgs.isEmpty()) {
			System.out.println("ok");
		}
	}
}