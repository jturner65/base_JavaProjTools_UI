package base_UI_Objects.windowUI.sidebar;

import java.io.*;
import java.util.*;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;

/**
 * displays sidebar menu of interaction and functionality
 * @author John Turner
 *
 */
public class SidebarMenu extends Base_DispWindow{
	/**
	 * Number of main, app-wide booleans to show
	 */
	private final int numMainFlagsToShow;
	
	private final int clkFlgsStY;

	/**
	 * private child-class flags - window specific
	 */
	public static final int
			//idx 0 is debug in Base_BoolFlags
			mseClickedInBtnsIDX 		= 1,					//the mouse was clicked in the button region of the menu and a click event was processed
			usesWinBtnDispIDX			= 2,					//this menu displays the window title bar
			usesMseOvrBtnDispIDX		= 3,					//this menu uses mouse-over display text
			usesDbgBtnDispIDX			= 4;					//this menu displays debug side bar buttons
	//private flag based buttons - ui menu won't display these
	private static final int numPrivFlags = 5;
	
	//where buttons should start on side menu
	/**
	 * index in arrays of button states/names for the show window buttons, if present
	 */
	public static final int btnShowWinIdx = 0;	
	/**
	 * configuration, build and rendering of side bar buttons
	 */
	private SidebarMenuBtnConfig btnConfig;
	
	/**
	 * Size of text height offset on creation TODO get rid of this value in favor of live updates
	 */
	private float initTextHeightOff;
	/**
	 * Size of button label on creation TODO get rid of this value in favor of live updates
	 */
	private float initBtnLblYOff;
	
	/**
	 * 
	 * @param _ri
	 * @param _AppMgr
	 * @param _winIdx
	 * @param _c
	 */
	public SidebarMenu(IRenderInterface _ri, GUI_AppManager _AppMgr, int _winIdx, SidebarMenuBtnConfig _c) {
		super(_ri, _AppMgr, _winIdx);
		btnConfig=_c;
		
		clkFlgsStY = (int) AppMgr.getClkBoxDim();
		initTextHeightOff = AppMgr.getTextHeightOffset();
		initBtnLblYOff = AppMgr.getBtnLabelYOffset();
		
		//these have to be set before setupGUIObjsAras is called from initThisWin
		numMainFlagsToShow = AppMgr.getNumFlagsToShow();
		//msgObj.dispConsoleDebugMessage("SidebarMenu", "ctor", "clkFlgsStY : " + clkFlgsStY+ "|initTextHeightOff : " + initTextHeightOff + "|initBtnLblYOff : "+initBtnLblYOff+"| initRowStYOff : "+initRowStYOff+"| minBtnClkY : "+minBtnClkY);
		// build uiClkCoords
		//all ui objects for all windows will follow this format and share the uiClkCoords[0] value
		
		// UI region for Application-wide buttons, under application flags
		btnConfig.setAppButtonRegion(winInitVals.rectDim, (numMainFlagsToShow+3) * initTextHeightOff + clkFlgsStY);
		

		// Application flags start at beginning y of window rect dimensions
		float uiClkCoordsYStart = winInitVals.rectDim[1] + .01f * winInitVals.rectDim[3];
		// UI click coords will be flag region + Application buttons (so include flag y start)
		float[] UIAppButtonRegion = btnConfig.getUIAppBtnRegion();
		initUIClickCoords(UIAppButtonRegion[0], uiClkCoordsYStart, UIAppButtonRegion[2], UIAppButtonRegion[3]);
		// Change UI FlagReg
		//standard init
		super.initThisWin(true);
	}//ctor
			
	/**
	 * init/reinit this window
	 */
	@Override
	protected final void initMe() {	}	
	
	
	//initialize structure to hold modifiable menu regions
	//called from super.initThisWin
	@Override
	protected final void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals){}//setupGUIObjsAras
	public void setAllFuncBtnLabels(int _funRowIDX, String[] BtnLabels) {btnConfig.setAllFuncBtnLabels(_funRowIDX, BtnLabels);}
	
	/**
	 * Return the label on the sidebar button specified by the passed row and column
	 * @param row
	 * @param col
	 * @return
	 */
	public final String getSidebarMenuButtonLabel(int row, int col) {		return btnConfig.getSidebarMenuButtonLabel(row, col);}
	
	public Boolean[][] getGuiBtnWaitForProc() {return  btnConfig.getGuiBtnWaitForProc();}
	public void setGuiBtnWaitForProc(Boolean[][] _guiBtnWaitForProc) {		btnConfig.setGuiBtnWaitForProc(_guiBtnWaitForProc);}
	public int[][] getGuiBtnSt() {		return btnConfig.getGuiBtnSt();	}
	public void setGuiBtnSt(int[][] _guiBtnSt) {btnConfig.setGuiBtnSt(_guiBtnSt);	}
	
	@Override
	//initialize all private-flag based UI buttons here - called by base class
	protected final int initAllUIButtons(ArrayList<Object[]> tmpBtnLabelsArray){return numPrivFlags;}//
	
	@Override
	protected final void initDispFlags() {}
	
	//window UI object not used for sidebar menu
	@Override
	protected UIDataUpdater buildUIDataUpdateObject() {return null;}
	@Override
	protected final void updateCalcObjUIVals() {}
	@Override
	protected int[] getFlagIDXsToInitToTrue() {
		ArrayList<Integer> resAra = new ArrayList<Integer>();
		if(btnConfig._initBtnShowWin) {		resAra.add(usesWinBtnDispIDX);}
		if(btnConfig._initBtnMseFunc) {		resAra.add(usesMseOvrBtnDispIDX);}
		if(btnConfig._initBtnDBGSelCmp) {	resAra.add(usesDbgBtnDispIDX);}
		int[] res = new int[resAra.size()];
		for(int i=0;i<res.length;++i) {			res[i]=resAra.get(i);		}
		return res;
	}
	
	/**
	 * Handle application-specific flag setting
	 */
	@Override
	public final void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal){
		switch (idx) {//special actions for each flag
			case mseClickedInBtnsIDX 	: {break;}			
			case usesWinBtnDispIDX	 	: {break;}
			case usesMseOvrBtnDispIDX 	: {break;}
			case usesDbgBtnDispIDX	 	: {break;}
		}
	}

	/**
	 * UI code-level Debug mode functionality. Called only from flags structure
	 * @param val
	 */
	@Override
	protected final void handleDispFlagsDebugMode_Indiv(boolean val) {}
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	@Override
	protected final void handlePrivFlagsDebugMode_Indiv(boolean val) {	}
	
	/**
	 * turn off buttons that may be on and should be turned off - called at release of mouse - check for mouse loc before calling (in button region)?
	 */
	private final void clearAllBtnStates(){
		if(privFlags.getFlag(mseClickedInBtnsIDX)) {
			btnConfig.clearAllBtnStates();
			privFlags.setFlag(mseClickedInBtnsIDX, false);
		}
	}//clearAllBtnStates
		
	/**
	 * set non-momentary buttons to be waiting for processing complete comand
	 * @param row
	 * @param col
	 */
	public final void setWaitForProc(int row, int col) {btnConfig.setWaitForProc(row, col);}
	@Override
	protected void setUI_IntValsCustom(int UIidx, int ival, int oldval) {}
	@Override
	protected void setUI_FloatValsCustom(int UIidx, float val, float oldval) {}
	@Override
	protected final void launchMenuBtnHndlr(int funcRow, int btn, String label) {	}
	@Override
	public final void handleSideMenuMseOvrDispSel(int btn, boolean val) {	}
	@Override
	protected final void handleSideMenuDebugSelEnable(int btn) {	}
	@Override
	protected final void handleSideMenuDebugSelDisable(int btn) {	}
	@Override
	protected boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld){		return false;	}
	@Override
	protected boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {	
		if(!winInitVals.pointInRectDim(mouseX, mouseY)){return false;}//not in this window's bounds, quit asap for speedz
		//TODO Awful - needs to be recalced, dependent on menu being on left
		int i = (int)((mouseY-(initBtnLblYOff + clkFlgsStY))/(initTextHeightOff));					
		//msgObj.dispInfoMessage(className, "hndlMouseClick_Indiv", "Clicked on disp windows : i : " + i+"|uiClkCoords[1] = "+uiClkCoords[1]+" | UIAppButtonRegion[1] :"+UIAppButtonRegion[1]);
		
		if((i>=0) && (i<numMainFlagsToShow)){
			AppMgr.flipMainFlag(i);return true;	
		} else if(btnConfig.checkInButtonRegion(mouseX, mouseY)) {//MyMathUtils.ptInRange((float)mouseX, (float)mouseY, UIAppButtonRegion[0], UIAppButtonRegion[1], UIAppButtonRegion[2], UIAppButtonRegion[3])){
			boolean clkInBtnRegion = btnConfig.checkButtons(mouseX, mouseY, winInitVals.rectDim[2]);
			if(clkInBtnRegion) { privFlags.setFlag(mseClickedInBtnsIDX, true);}
			return clkInBtnRegion;
		}//in region where clickable buttons are - uiClkCoords[1] is bottom of buttons
		return false;
	}
	@Override
	public boolean hndlMouseDrag_Indiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {//regular UI obj handling handled elsewhere - custom UI handling necessary to call main window		
		//boolean res = pa.getCurFocusDispWindow().hndlMouseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
		boolean res = AppMgr.getCurFocusDispWindow().sideBarMenu_CallWinMseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);	
		return res;	}
	@Override
	public void hndlMouseRel_Indiv() {	clearAllBtnStates();}

	private void drawSideBarBooleans(float btnLblYOff, float xOffHalf, float txtHeightOffHalf){
		//draw main booleans and their state
		ri.translate(xOffHalf,btnLblYOff);
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		ri.showText("Boolean Flags",0,txtHeightOffHalf);
		ri.translate(0,clkFlgsStY);
		AppMgr.dispMenuText(xOffHalf,txtHeightOffHalf);
	}//drawSideBarBooleans	
	
	@Override
	protected final void drawOnScreenStuffPriv(float modAmtMillis) {}
	@Override//for windows to draw on screen
	protected final void drawRightSideInfoBarPriv(float modAmtMillis) {}
	/**
	 * Draw window/application-specific functionality
	 * @param animTimeMod # of milliseconds since last frame dividied by 1000
	 */
	@Override
	protected final void drawMe(float animTimeMod) {
		float txtHeightOffHalf = 0.5f * initTextHeightOff;
		float xOffHalf = AppMgr.getXOffsetHalf();
		ri.pushMatState();
			ri.pushMatState();
				AppMgr.drawSideBarStateLights(initTextHeightOff);				//lights that reflect various states
			ri.popMatState();		
			ri.pushMatState();
				drawSideBarBooleans(
						AppMgr.getBtnLabelYOffset(), 
						xOffHalf, 
						txtHeightOffHalf);				//toggleable booleans 
			ri.popMatState();	
			ri.pushMatState();			
				btnConfig.drawSideBarButtons(
						AppMgr.getBtnLabelYOffset(),
						xOffHalf,
						AppMgr.getRowStYOffset(),
						winInitVals.rectDim[2]);						//draw buttons
			ri.popMatState();	
			ri.pushMatState();
				drawGUIObjs(AppMgr.isDebugMode(), animTimeMod);					//draw what global user-modifiable fields are currently available 
			ri.popMatState();			
			ri.pushMatState();
				AppMgr.drawWindowGuiObjs(animTimeMod);			//draw objects for window with primary focus
			ri.popMatState();	
		ri.popMatState();
	}
	
	@Override
	public final void drawCustMenuObjs(float animTimeMod){}	
	//no custom camera handling for menu , float rx, float ry, float dz are all now member variables of every window
	@Override
	protected final void setCamera_Indiv(float[] camVals){}
	@Override
	public void hndlFileLoad(File file, String[] vals, int[] stIdx) {
		hndlFileLoad_GUI(vals, stIdx);
	}
	@Override
	public ArrayList<String> hndlFileSave(File file) {
		ArrayList<String> res = hndlFileSave_GUI();

		return res;
	}
	@Override
	protected String[] getSaveFileDirNamesPriv() {return new String[]{"menuDir","menuFile"};	}
	@Override
	protected final void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc){}//not a snap-to window
	@Override
	protected final void setVisScreenDimsPriv() {}
	@Override
	protected myPoint getMsePtAs3DPt(myPoint mseLoc){return new myPoint(mseLoc.x,mseLoc.y,0);}
	@Override
	protected final void endShiftKey_Indiv() {}
	@Override
	protected final void endAltKey_Indiv() {}
	@Override
	protected final void endCntlKey_Indiv() {}
	@Override
	protected final void closeMe() {}
	@Override
	protected final void showMe() {}
	@Override
	protected final void resizeMe(float scale) {}	
	@Override
	protected final void setCustMenuBtnLabels() {}
	@Override
	protected boolean simMe(float modAmtSec) {return false;}
	@Override
	protected final void stopMe() {}
	@Override
	protected final void addSScrToWin_Indiv(int newWinKey){}
	@Override
	protected final void addTrajToScr_Indiv(int subScrKey, String newTrajKey){}
	@Override
	protected final void delSScrToWin_Indiv(int idx) {}	
	@Override
	protected final void delTrajToScr_Indiv(int subScrKey, String newTrajKey) {}		
	//no trajectory here
	@Override
	public final void processTraj_Indiv(DrawnSimpleTraj drawnTraj){}	
	@Override
	protected final void initDrwnTraj_Indiv(){}
	@Override
	public String toString(){
		String res = super.toString();
		return res;
	}
}//mySideBarMenu