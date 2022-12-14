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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ThrowStatementTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.samples.java.utils.CommonMessage;
/**
 * This rule detects invalid class comment
 * 
 * @author tathienphuoc
 * @see CheckICWE209Rule
 * @since J2EE 1.6
 */
@Rule(key = "CheckICWE209Rule")
public class CheckICWE209Rule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;
	private boolean hasImportLogger = false;
	private boolean hasThrows = false;
	private Set<String> logVars = new HashSet<>();
	private Map<String, Boolean> localLogVars = new HashMap<>();
	private List<Integer> throwLines = new ArrayList<>();
	private List<Integer> throwCols = new ArrayList<>();
	private List<Integer> logErrLines = new ArrayList<>();
	private List<Integer> logErrCols = new ArrayList<>();
	private static String formatErr = "[R4J][ICWE-209][%s]";
	private String errVar = "";

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	private String getImportLine(ImportTree tree) {
		int firstLine = tree.qualifiedIdentifier().firstToken().line() - 1;
		int lastLine = tree.qualifiedIdentifier().lastToken().line() - 1;
		int firstCol = tree.qualifiedIdentifier().firstToken().column() - 1;
		int lastCol = tree.qualifiedIdentifier().lastToken().column()
				+ tree.qualifiedIdentifier().lastToken().text().length();
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
	public void visitImport(ImportTree tree) {
		try {
			if ("org.apache.log4j.Logger".equals(getImportLine(tree))) {
				hasImportLogger = true;
			}
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	public boolean isLogVar(VariableTree variable) {
		return hasImportLogger
				&& Arrays.asList("Logger", "org.apache.log4j.Logger").contains(variable.symbol().type().name());
	}

	@Override
	public void visitClass(ClassTree tree) {
		try {
			if (tree.superClass() != null
					&& Arrays.asList("ServiceCommandSupport", "BasicCommandSupport", "DBDAOSupport")
							.contains(tree.superClass().toString())) {
				logVars.add("log");
			}
			if (hasImportLogger) {
				tree.members().forEach(m -> {
					if (m.getClass().getName().endsWith(".VariableTreeImpl")) {
						VariableTree variable = (VariableTree) m;
						if (isLogVar(variable))
							logVars.add(variable.simpleName().name());
					}

				});
			}
			super.visitClass(tree);
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitVariable(VariableTree tree) {
		try {
			localLogVars.put(tree.simpleName().name(), isLogVar(tree));
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitMethod(MethodTree tree) {
		try {
			localLogVars.clear();
			hasThrows = !tree.throwsClauses().isEmpty();
			super.visitMethod(tree);
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitThrowStatement(ThrowStatementTree tree) {
		try {
			throwLines.add(tree.firstToken().line());
			throwCols.add(tree.firstToken().column());
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		try {
			String varName = tree.firstToken().text();
			String methodInvocationLine = getMethodInvocationLine(tree);
			if (varName.equals(errVar) && methodInvocationLine.startsWith(varName + ".printStackTrace")) {
				System.out.println(tree.firstToken().line() + " "
						+ String.format(formatErr, CommonMessage.REMOVE_DEBUG_STATEMENT));
				context.reportIssue(this, tree, CommonMessage.REMOVE_DEBUG_STATEMENT);
			}
			if (((logVars.contains(varName) && !localLogVars.containsKey(varName))
					|| (localLogVars.containsKey(varName) && localLogVars.get(varName)))
					&& methodInvocationLine.startsWith(varName + ".error")) {
				logErrLines.add(tree.firstToken().line());
				logErrCols.add(tree.firstToken().column());
			}
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

	private String getMethodInvocationLine(MethodInvocationTree tree) {
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
		return line.replaceAll("\\s+", "");
	}

	@Override
	public void visitCatch(CatchTree tree) {
		try {
			errVar = tree.parameter().simpleName().name();
			super.visitCatch(tree);
			int line = tree.firstToken().line();
			int col = tree.firstToken().column();
			if (hasThrows) {
				for (int i = 0; i < throwLines.size(); i++) {
					if (line < throwLines.get(i) || (line == throwLines.get(i) && col < throwCols.get(i))) {
						return;
					}
				}
				System.out.println(
						tree.firstToken().line() + " " + String.format(formatErr, CommonMessage.ABSENT_THROW_NEW));
				context.reportIssue(this, tree, String.format(formatErr, CommonMessage.ABSENT_THROW_NEW));
			} else {
				for (int i = 0; i < logErrLines.size(); i++) {
					if (line < logErrLines.get(i) || (line == logErrLines.get(i) && col < logErrCols.get(i))) {
						return;
					}
				}
				System.out.println(
						tree.firstToken().line() + " " + String.format(formatErr, CommonMessage.ABSENT_LOG_ERROR));
				context.reportIssue(this, tree, String.format(formatErr, CommonMessage.ABSENT_LOG_ERROR));
			}
		} catch (Exception e) {
			System.out.println(tree.firstToken().line() + " " + e.getMessage());
			context.reportIssue(this, tree, CommonMessage.NOT_SUPPORT);
		}
	}

}
