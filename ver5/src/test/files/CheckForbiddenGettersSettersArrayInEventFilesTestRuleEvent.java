/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : ClassCommentUtils.java
*@FileTitle : ClassCommentUtils
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/
package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.midi.Soundbank;

import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;

public class CheckDuplicatedVariableNamesRuleTest {
	String a, b, c;
	private List<String> a1;
	private List<String> a2 = new ArrayList<String>();

	private ArrayList<String> b1;
	private ArrayList<String> b2 = new ArrayList<String>();

	private String[] c1;
	private String[] c2, c3;

	public C1[] getC1() {// Noncompliant
		return c1;
	}

	public String[] getB2() {
		return c1;
	}

}