package testProject.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_Utils_Objects.tools.flags.Base_BoolFlags;
import testProject.uiData.UITestDataUpdater_2D;

public class UI_TestWindow2D extends Base_DispWindow {
	/**
	 * idxs - need one per ui object
	 */
	public final static int
		gIDX_FloatVal1 		= 0,
		gIDX_IntVal1		= 1,
		gIDX_ListVal1		= 2,
		gIDX_IntVal2    	= 3,
		gIDX_IntVal3		= 4,
		gIDX_ListVal2		= 5,
		gIDX_FloatVal2 		= 6,
		gIDX_FloatVal3 		= 7;

	public static final int numBaseGUIObjs = 8;											//# of gui objects for ui

	//current/initial values
	protected double floatVal1 = .3;
	protected double floatVal2 = 1.8;
	protected double floatVal3 = 42.5;
	
	protected String[] listOfNames1 = new String[]{
			"L1 Name 1", "L1 Name 2", "L1 Name 3", 
			"L1 Name 4", "L1 Name 5"};
	protected String[] listOfNames2 = new String[]{
			"L2 Name 1", "L2 Name 2", "L2 Name 3", 
			"L2 Name 4", "L2 Name 5", "L2 Name 6", 
			"L2 Name 7", "L2 Name 8", "L2 Name 9"};
	
	/**
	 * private child-class flags - window specific
	 */
	public static final int 
			//debug is 0
			button1_IDX 	= 1,
			button2_IDX		= 2,
			button3_IDX 	= 3,
			button4_IDX		= 4,
			button5_IDX		= 5,
			button6_IDX 	= 6,
			button7_IDX 	= 7,
			nonButton1_IDX  = 8,
			nonButton2_IDX  = 9,
			nonButton3_IDX  = 10;
	

	protected static final int numBasePrivFlags = 11;
	
	protected myPoint circleCenter;
	
	public UI_TestWindow2D(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx) {
		super(_p, _AppMgr, _winIdx);
		super.initThisWin(false);
		// Initially center circle in display screen
		circleCenter = new myPoint();
	}

	@Override
	protected void initDispFlags() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initMe() {
		// TODO Auto-generated method stub

	}	
	
	/**
	 * Retrieve the total number of defined privFlags booleans (application-specific state bools and interactive buttons)
	 */
	@Override
	public int getTotalNumOfPrivBools() {return numBasePrivFlags;}

	/**
	 * Build all UI objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
	 * @param tmpUIObjArray : map of object data, keyed by UI object idx, with array values being :                    
	 *           the first element double array of min/max/mod values                                                   
	 *           the 2nd element is starting value                                                                      
	 *           the 3rd elem is label for object                                                                       
	 *           the 4th element is object type (GUIObj_Type enum)
	 *           the 5th element is boolean array of : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *           the 6th element is a boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @param tmpListObjVals : map of string arrays, keyed by UI object idx, with array values being each element in the list
	 * @param tmpBtnNamesArray : map of Object arrays to be built containing all button definitions, keyed by sequential value == objId
	 * 				the first element is true label
	 * 				the second element is false label
	 */
	@Override
	protected final void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals, TreeMap<Integer, Object[]> tmpBtnNamesArray){	
		//build list select box values
		//keyed by object idx (uiXXXIDX), entries are lists of values to use for list select ui objects
		
		tmpListObjVals.put(gIDX_ListVal1, listOfNames1);
		tmpListObjVals.put(gIDX_ListVal2, listOfNames2);
			
		tmpUIObjArray.put(gIDX_FloatVal1, uiMgr.uiObjInitAra_Float(new double[]{0,1.0f,.0001f}, floatVal1, "Float Value 1"));   	                                                            
		tmpUIObjArray.put(gIDX_IntVal1, uiMgr.uiObjInitAra_Int(new double[]{1,10,1.0f}, 1.0, "Int Value 1"));   						                                                        
		tmpUIObjArray.put(gIDX_ListVal1, uiMgr.uiObjInitAra_List(new double[]{0,listOfNames1.length-1,1.1f}, 0.0, "List of Names 1"));                                                                     
		tmpUIObjArray.put(gIDX_IntVal2, uiMgr.uiObjInitAra_Int(new double[]{-50,50,1.0f}, 0.0, "Int Value 2"));   					                                                        
		tmpUIObjArray.put(gIDX_IntVal3, uiMgr.uiObjInitAra_Int(new double[]{0,1000,1.0f}, 0.0, "Int Value 3"));   					
		tmpUIObjArray.put(gIDX_FloatVal2, uiMgr.uiObjInitAra_Float(new double[]{0,10.0f,.0001f}, floatVal2, "Float Value 2"));   	                                                            
		tmpUIObjArray.put(gIDX_ListVal2, uiMgr.uiObjInitAra_List(new double[]{0,listOfNames2.length-1,1.1f}, 0.0, "List of Names 2"));                                                                     
		tmpUIObjArray.put(gIDX_FloatVal3, uiMgr.uiObjInitAra_Float(new double[]{1.0f,100.0f,.0001f}, floatVal3, "Float Value 3"));   	                                                            

		int idx=0;
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Debugging", "Enable Debug"}, Base_BoolFlags.debugIDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 1 On", "Button 1 Off"}, button1_IDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 2 On", "Button 2 Off"}, button2_IDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 3 On", "Button 3 Off"}, button3_IDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 4 On", "Button 4 Off"}, button4_IDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 5 On", "Button 5 Off"}, button5_IDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 6 On", "Button 6 Off"}, button6_IDX));
		tmpBtnNamesArray.put(idx++, uiMgr.uiObjInitAra_Btn(new String[]{"Button 7 On", "Button 7 Off"}, button7_IDX));

	}	
	
	@Override
	protected UIDataUpdater buildUIDataUpdateObject() {
		return new UITestDataUpdater_2D(this);
	}


	@Override
	protected void drawMe(float animTimeMod) {
		ri.pushMatState();
			moveTo2DRectCenter();
			ri.setFill(new int[] {255,255,255}, 255);
			ri.setStroke(new int[] {255,255,255}, 255);
			ri.setStrokeWt(3.0f);
			ri.drawEllipse2D(circleCenter, 100);
		ri.popMatState();	
	}
	
	@Override
	protected boolean simMe(float modAmtSec) {
		//calc new circle center
		return false;
	}
	
	@Override
	protected void updateCalcObjUIVals() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleDispFlagsDebugMode_Indiv(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handlePrivFlagsDebugMode_Indiv(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal) {
		// TODO Auto-generated method stub

	}

	@Override
	protected int[] getFlagIDXsToInitToTrue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setUI_IntValsCustom(int UIidx, int ival, int oldVal) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setUI_FloatValsCustom(int UIidx, float val, float oldVal) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawCustMenuObjs(float animTimeMod) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleSideMenuMseOvrDispSel(int btn, boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleSideMenuDebugSelEnable(int btn) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleSideMenuDebugSelDisable(int btn) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void launchMenuBtnHndlr(int funcRow, int btn, String label) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String[] getSaveFileDirNamesPriv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initDrwnTraj_Indiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addSScrToWin_Indiv(int newWinKey) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addTrajToScr_Indiv(int subScrKey, String newTrajKey) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void delSScrToWin_Indiv(int idx) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void delTrajToScr_Indiv(int subScrKey, String newTrajKey) {
		// TODO Auto-generated method stub

	}

	@Override
	protected myPoint getMsePtAs3DPt(myPoint mseLoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setVisScreenDimsPriv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean hndlMouseDrag_Indiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D,
			myVector mseDragInWorld, int mseBtn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void hndlMouseRel_Indiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endShiftKey_Indiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endAltKey_Indiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endCntlKey_Indiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setCustMenuBtnLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTraj_Indiv(DrawnSimpleTraj drawnTraj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hndlFileLoad(File file, String[] vals, int[] stIdx) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<String> hndlFileSave(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void resizeMe(float scale) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void showMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void closeMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stopMe() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setCamera_Indiv(float[] camVals) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawRightSideInfoBarPriv(float modAmtMillis) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawOnScreenStuffPriv(float modAmtMillis) {
		// TODO Auto-generated method stub

	}

}//class UI_TestWindow2D
