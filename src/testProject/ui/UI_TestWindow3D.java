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
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import testProject.uiData.UITestDataUpdater_3D;

public class UI_TestWindow3D extends Base_DispWindow {
	/**
	 * idxs - need one per ui object
	 */
	public final static int
		gIDX_TimeStep 		= 0,
		gIDX_NumFlocks		= 1,
		gIDX_BoidType		= 2,
		gIDX_ModNumBoids	= 3,
		gIDX_BoidToObs		= 4;

	public static final int numBaseGUIObjs = 5;											//# of gui objects for ui

	protected String[] listOfNames = new String[]{"Name 1", "Name 2", "Name 3", "Name 4", "Name 5"};
	//current/initial values
	protected double curTimeStep = .1;
	protected int numFlocks = 1;
	protected final int maxNumFlocks = listOfNames.length;			//max # of flocks we'll support
	
	
	public UI_TestWindow3D(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx) {
		super(_p, _AppMgr, _winIdx);
		super.initThisWin(false);
	}

	@Override
	protected void initDispFlags() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initMe() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected UIDataUpdater buildUIDataUpdateObject() {
		return new UITestDataUpdater_3D(this);
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
	public int initAllPrivBtns(ArrayList<Object[]> tmpBtnNamesArray) {
		// TODO Auto-generated method stub
		return 0;
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
	protected void endShiftKeyI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endAltKeyI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endCntlKeyI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setCustMenuBtnLabels() {
		// TODO Auto-generated method stub

	}
	/**
	 * Build all UI objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
	 * @param tmpUIObjArray : map of object data, keyed by UI object idx, with array values being :                    
	 *           the first element double array of min/max/mod values                                                   
	 *           the 2nd element is starting value                                                                      
	 *           the 3rd elem is label for object                                                                       
	 *           the 4th element is object type (GUIObj_Type enum)
	 *           the 5th element is boolean array of : (unspecified values default to false)
	 *           	{value is sent to owning window, 
	 *           	value is sent on any modifications (while being modified, not just on release), 
	 *           	changes to value must be explicitly sent to consumer (are not automatically sent)}    
	 * @param tmpListObjVals
	 */
	@Override
	protected final void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals){	
		//build list select box values
		//keyed by object idx (uiXXXIDX), entries are lists of values to use for list select ui objects
		
		tmpListObjVals.put(gIDX_BoidType, listOfNames);
			
		tmpUIObjArray.put(gIDX_TimeStep,  new Object[]{new double[]{0,1.0f,.0001f}, curTimeStep, "Float Value 1", GUIObj_Type.FloatVal, new boolean[]{true}});   				//uiTrainDataFrmtIDX                                                                        
		tmpUIObjArray.put(gIDX_NumFlocks, new Object[]{new double[]{1,10,1.0f}, 1.0, "Int Value 1", GUIObj_Type.IntVal, new boolean[]{true}});   				//uiTrainDataFrmtIDX                                                                        
		tmpUIObjArray.put(gIDX_BoidType,  new Object[]{new double[]{0,listOfNames.length-1,1.1f}, 0.0, "List of Names", GUIObj_Type.ListVal, new boolean[]{true}} );   				//uiTrainDataFrmtIDX                                                                        
		tmpUIObjArray.put(gIDX_ModNumBoids, new Object[]{new double[]{-50,50,1.0f}, 0.0, "Int Value 2", GUIObj_Type.IntVal, new boolean[]{true}});   				//uiTrainDataFrmtIDX                                                                        
		tmpUIObjArray.put(gIDX_BoidToObs, new Object[]{new double[]{0,1000,1.0f}, 0.0, "Int Value 3", GUIObj_Type.IntVal, new boolean[]{true}} );   				//uiTrainDataFrmtIDX
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
	protected boolean simMe(float modAmtSec) {
		// TODO Auto-generated method stub
		return false;
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
	protected void drawMe(float animTimeMod) {
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

}//class UI_TestWindow3D
