import org.w3c.dom.events.EventException;

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
class MyClass {
	/**
	 * Loading UI <br>
	 * 
	 * @param LongRangeSkdGPVVO longRangeSkdGPVVO
	 * @param int               addCallPost
	 * @param int               addVvdPos
	 * @param LongRangeSkdVO    addPort
	 * @param boolean           addCancel
	 * @return List<LongRangeSkdVO>
	 * @exception EventException
	 */
	private List<LongRangeSkdVO> loadSimDataList(@DefaultValue("") LongRangeSkdGPVVO longRangeSkdGPVVO, int addCallPost,
			int addVvdPos, LongRangeSkdVO addPort, boolean addCancel) throws EventException;

	private void test() {// Noncompliant

	}
}
