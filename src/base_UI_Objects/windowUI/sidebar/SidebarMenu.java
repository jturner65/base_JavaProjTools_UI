package base_UI_Objects.windowUI.sidebar;

import java.io.*;
import java.util.*;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
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
	
	//GUI Buttons
	public float minBtnClkY;			
	//where buttons should start on side menu
	/**
	 * index in arrays of button states/names for the show window buttons, if present
	 */
	public static final int btnShowWinIdx = 0;
	/**
	 * Index in button rows corresponding to the mouse-over display control buttons
	 */
	public static int btnMseOvrFuncIdx; 
	/**
	 * Index in button rows corresponding to the debug function buttons
	 */
	public static int btnDBGSelCmpIdx; 
	/**
	 * array of the idxs in the guiBtnArrays where the user-definable/modifiable functions reside
	 */
	protected int[] funcBtnIDXAra;
	/**
	 * offset in arrays where actual function buttons start
	 */
	public int funcBtnIDXOffset=0;
	
	private boolean _initBtnShowWin = false, 
			_initBtnMseFunc= false, 
			_initBtnDBGSelCmp = false;

	private String[] guiBtnRowNames;
	/**
	 * names for each row of buttons - idx 1 is name of row
	 */
	private String[][] guiBtnLabels;
	/**
	 * default names, to return to if not specified by user
	 */
	private String[][] defaultUIBtnLabels;
	/**
	 * whether buttons are momentary or not (on only while being clicked)
	 */
	private Boolean[][] guiBtnInst;
	/**
	 * whether buttons are waiting for processing to complete (for non-momentary buttons)
	 */
	private Boolean[][] guiBtnWaitForProc;
	
	/**
	 * whether buttons are disabled(-1), enabled but not clicked/on (0), or enabled and on/clicked(1)
	 */
	private int[][] guiBtnSt;
	
	public final int[] guiBtnStFillClr = new int[]{		//button colors based on state
			IRenderInterface.gui_White,								//disabled color for buttons
			IRenderInterface.gui_LightGray,								//not clicked button color
			IRenderInterface.gui_LightBlue,									//clicked button color
		};
	public final int[] guiBtnStTxtClr = new int[]{					//text color for buttons
			IRenderInterface.gui_LightGray,							//disabled color for buttons
			IRenderInterface.gui_Black,								//not clicked button color
			IRenderInterface.gui_Black,								//clicked button color
		};	
	//row and column of currently clicked-on button (for display highlight as pressing)
	public int[] curBtnClick = new int[]{-1,-1};
	
	/**
	 * configuration of side bar buttons
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
	 * Offset for starting a new row in Y at creation TODO get rid of this value in favor of live updates
	 */
	private float initRowStYOff;
	
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
		initRowStYOff = AppMgr.getRowStYOffset();
		
		// build all side bar menu buttons with format specific to instancing application
		// returns the y location of where the UI clickable region for the display window should start
		float guiBtnRowsEndY = buildBtnData();
		//these have to be set before setupGUIObjsAras is called from initThisWin
		numMainFlagsToShow = AppMgr.getNumFlagsToShow();

		minBtnClkY = (numMainFlagsToShow+3) * initTextHeightOff + clkFlgsStY;										//start of buttons from under boolean flags
		//msgObj.dispConsoleDebugMessage("SidebarMenu", "ctor", "clkFlgsStY : " + clkFlgsStY+ "|initTextHeightOff : " + initTextHeightOff + "|initBtnLblYOff : "+initBtnLblYOff+"| initRowStYOff : "+initRowStYOff+"| minBtnClkY : "+minBtnClkY);
		// build uiClkCoords
		//all ui objects for all windows will follow this format and share the uiClkCoords[0] value
		float uiClickStX = winInitVals.rectDim[0] + .01f * winInitVals.rectDim[2];
		float uiClickStY = winInitVals.rectDim[1] + .01f * winInitVals.rectDim[3];
		float uiClickEndX = winInitVals.rectDim[0] + .99f * winInitVals.rectDim[2];
		initUIClickCoords(uiClickStX, uiClickStY,uiClickEndX, minBtnClkY + guiBtnRowsEndY);
		//make a little space below debug buttons
		uiClkCoords[3] += initRowStYOff;
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
	
	
	/**
	 * call this from each new window to set function btn labels, if specified, when window gets focus
	 * @param rowIdx
	 * @param BtnLabels
	 */
	public void setAllFuncBtnLabels(int _funRowIDX, String[] BtnLabels) {
		int rowIdx = funcBtnIDXAra[_funRowIDX];		
		String[] replAra = ((null==BtnLabels) || (BtnLabels.length != guiBtnLabels[rowIdx].length)) ? defaultUIBtnLabels[rowIdx] : BtnLabels;
		for(int i=0;i<guiBtnLabels[rowIdx].length;++i) {guiBtnLabels[rowIdx][i]=replAra[i];}
	}//setFunctionButtonNames
	
	/**
	 * Set values for ui action buttons, based on specifications of @class mySidebarMenuBtnConfig
	 * Parameters user defined in main as window is specified, individual button names can be overridden in individual app windows
	 */
	private float buildBtnData() {
		ArrayList<String> tmpGuiBtnRowNames = new ArrayList<String>();
		ArrayList<String[]> tmpBtnLabels = new ArrayList<String[]>();
		ArrayList<String[]> tmpDfltBtnLabels = new ArrayList<String[]>();
		
		ArrayList<Boolean[]> tmpBtnIsInst = new ArrayList<Boolean[]>();
		ArrayList<Boolean[]> tmpBtnWaitForProc = new ArrayList<Boolean[]>();
		int numFuncBtnArrayNames = btnConfig.funcRowNames.length;
		//if debug btn names array is not empty in config, add debug button names row to funcBtnIDXAra
		int debugBtnRowIDX = (btnConfig.debugBtnLabels.length > 0) 
				? numFuncBtnArrayNames++ : -1;
		
		funcBtnIDXAra = new int[numFuncBtnArrayNames];
		
		btnDBGSelCmpIdx = -1;
		btnMseOvrFuncIdx = -1;
		funcBtnIDXOffset = 0;
		
		String[] titleArray = new String[btnConfig.winTitles.length-1];		
		if((btnConfig.inclWinNames)&&(titleArray.length != 0)) {
			for(int i=0;i<titleArray.length;++i) {titleArray[i] = btnConfig.winTitles[i+1];}
			tmpBtnLabels.add(titleArray);
			tmpDfltBtnLabels.add(titleArray);

			tmpBtnIsInst.add(buildDfltBtnFlagAra(titleArray.length));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(titleArray.length));
			
			tmpGuiBtnRowNames.add("Display Window");

			_initBtnShowWin = true;
			++funcBtnIDXOffset;
		} else {			_initBtnShowWin= false;		}
		
		String[] mseOvrBtnLabels = AppMgr.getMouseOverSelBtnLabels();	
		if((btnConfig.inclMseOvValues) && (mseOvrBtnLabels!=null) && (mseOvrBtnLabels.length > 0)) {
			tmpBtnLabels.add(mseOvrBtnLabels);
			tmpDfltBtnLabels.add(mseOvrBtnLabels);
		
			tmpBtnIsInst.add(buildDfltBtnFlagAra(mseOvrBtnLabels.length));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(mseOvrBtnLabels.length));

			tmpGuiBtnRowNames.add("Mouse Over Info To Display");
			btnMseOvrFuncIdx = funcBtnIDXOffset;  //btnConfig.
			++funcBtnIDXOffset;
			_initBtnMseFunc= true;
		} else {			_initBtnMseFunc= false;}
		
 		for(int i=0;i<btnConfig.numBtnsPerFuncRow.length;++i) {
			String s = btnConfig.funcRowNames[i];
			tmpBtnLabels.add(buildBtnNameAra(btnConfig.numBtnsPerFuncRow[i],"Func"));
			tmpDfltBtnLabels.add(buildBtnNameAra(btnConfig.numBtnsPerFuncRow[i],"Func"));
			
			tmpBtnIsInst.add(buildDfltBtnFlagAra(btnConfig.numBtnsPerFuncRow[i]));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(btnConfig.numBtnsPerFuncRow[i]));
			funcBtnIDXAra[i]=i+funcBtnIDXOffset;
			tmpGuiBtnRowNames.add(s);
		}
		if(debugBtnRowIDX >= 0) {
			tmpBtnLabels.add(buildBtnNameAra(btnConfig.debugBtnLabels.length,"Debug"));
			tmpDfltBtnLabels.add(buildBtnNameAra(btnConfig.debugBtnLabels.length,"Debug"));
			
			tmpBtnIsInst.add(buildDfltBtnFlagAra(btnConfig.debugBtnLabels.length));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(btnConfig.debugBtnLabels.length));
			tmpGuiBtnRowNames.add("DEBUG");
			btnDBGSelCmpIdx = tmpGuiBtnRowNames.size()-1;
			funcBtnIDXAra[debugBtnRowIDX]=debugBtnRowIDX+funcBtnIDXOffset;
			_initBtnDBGSelCmp = true;
		} else {	_initBtnDBGSelCmp = false;	}
		
		guiBtnRowNames = tmpGuiBtnRowNames.toArray(new String[0]);		
		guiBtnLabels = tmpBtnLabels.toArray(new String[0][]);
		defaultUIBtnLabels = tmpDfltBtnLabels.toArray(new String[0][]);
		guiBtnInst = tmpBtnIsInst.toArray(new Boolean[0][]);
		guiBtnWaitForProc = tmpBtnWaitForProc.toArray(new Boolean[0][]);
		
		guiBtnSt = new int[guiBtnRowNames.length][];
		for(int i=0;i<guiBtnSt.length;++i) {guiBtnSt[i] = new int[guiBtnLabels[i].length];}
		
		//set button names
		for(int _type = 0;_type<btnConfig.funcBtnLabels.length;++_type) {setAllFuncBtnLabels(_type,btnConfig.funcBtnLabels[_type]);}
		//set debug button names from valus specified in btnConfig, if any provided
		if (_initBtnDBGSelCmp) {
			setAllFuncBtnLabels(debugBtnRowIDX, btnConfig.debugBtnLabels);
		}	
		// y span of the buttons
		return (guiBtnRowNames.length * 2.0f) * initTextHeightOff;
	}//buildBtnData
	
	/**
	 * Return the label on the sidebar button specified by the passed row and column
	 * @param row
	 * @param col
	 * @return
	 */
	public final String getSidebarMenuButtonLabel(int row, int col) {
		if(row >= guiBtnLabels.length) {		return "Row " +row+" too high for max rows : "+guiBtnLabels.length;}
		if(col >= guiBtnLabels[row].length) {	return "Col " +col+" too high for max cols @ row " + row+" : "+guiBtnLabels[row].length;}
		return guiBtnLabels[row][col];}
	
	public Boolean[][] getGuiBtnWaitForProc() {return guiBtnWaitForProc;}
	public void setGuiBtnWaitForProc(Boolean[][] _guiBtnWaitForProc) {		guiBtnWaitForProc = _guiBtnWaitForProc;}
	public int[][] getGuiBtnSt() {		return guiBtnSt;	}
	public void setGuiBtnSt(int[][] _guiBtnSt) {	guiBtnSt = _guiBtnSt;	}

	private String[] buildBtnNameAra(int numBtns, String prfx) {
		String[] res = new String[numBtns];
		for(int i=0;i<numBtns;++i) {res[i]=""+prfx+" "+(i+1);}
		return res;
	}
	private Boolean[] buildDfltBtnFlagAra(int numBtns) {
		Boolean[] tmpAra1 = new Boolean[numBtns];
		for(int i=0;i<tmpAra1.length;++i) {tmpAra1[i]=false;}
		return tmpAra1;
	}
	
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
		if(_initBtnShowWin) {		resAra.add(usesWinBtnDispIDX);}
		if(_initBtnMseFunc) {		resAra.add(usesMseOvrBtnDispIDX);}
		if(_initBtnDBGSelCmp) {		resAra.add(usesDbgBtnDispIDX);}
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
	 * check if buttons clicked. TODO Create a button class that will manage its own hotspot
	 * @param mseX
	 * @param mseY
	 * @return
	 */
	private boolean checkButtons(int mseX, int mseY){
		double stY = minBtnClkY + initRowStYOff, endY = 0, 
				stX = 0, endX, widthX; //btnLblYOff			
		for(int row=0; row<guiBtnRowNames.length;++row){
			endY = stY + initTextHeightOff;	
			widthX = winInitVals.rectDim[2]/(1.0f * guiBtnLabels[row].length);
			stX = 0;	endX = widthX;
			for(int col =0; col<guiBtnLabels[row].length;++col){	
				if((MyMathUtils.ptInRange(mseX, mseY,stX, stY, endX, endY)) && (guiBtnSt[row][col] != -1)){
					handleButtonClick(row,col);
					return true;
				}					
				stX += widthX;	endX += widthX; 
			}
			//add initTextHeightOff for button row offset
			stY = endY + initTextHeightOff + initRowStYOff;		
		}
		return false;
	}//handleButtonClick	
	
	/**
	 * turn off buttons that may be on and should be turned off - called at release of mouse - check for mouse loc before calling (in button region)?
	 */
	private final void clearAllBtnStates(){
		if(privFlags.getFlag(mseClickedInBtnsIDX)) {
			//guiBtnWaitForProc should only be set for non-momentary buttons when they are pushed and cleared when whatever they are do is complete
			for(int row=0; row<guiBtnRowNames.length;++row){for(int col =0; col<guiBtnLabels[row].length;++col){				
				if((guiBtnSt[row][col]==1) && (guiBtnInst[row][col]  || !guiBtnWaitForProc[row][col])){	guiBtnSt[row][col] = 0;}//btn is on, and either is momentary or it is not waiting for processing
			}}
			privFlags.setFlag(mseClickedInBtnsIDX, false);
		}
	}//clearAllBtnStates
	
	/**
	 * clear the passed row of buttons except for the indicated btn - to enable single button per row radio-style buttons
	 * @param row row of buttons to clear
	 * @param btnToKeepOn btn whose state should stay on
	 */
	protected final void clearRowExceptPassedBtn(int row, int btnToKeepOn) {
		for(int col =0; col<guiBtnLabels[row].length;++col){	guiBtnSt[row][col] = 0;}
		guiBtnSt[row][btnToKeepOn]=1;
	}
	
	/**
	 * set non-momentary buttons to be waiting for processing complete comand
	 * @param row
	 * @param col
	 */
	public final void setWaitForProc(int row, int col) {
		if(!guiBtnInst[row][col]) {	guiBtnWaitForProc[row][col] = true;}		
	}
	/**
	 * handle click on button region of menubar
	 * @param row
	 * @param col
	 */
	private final void handleButtonClick(int row, int col){
		int val = guiBtnSt[row][col];//initial state, before being changed
		guiBtnSt[row][col] = (guiBtnSt[row][col] + 1)%2;//change state
		//if not momentary buttons, set wait for proc to true
		setWaitForProc(row,col);
		if((row == btnShowWinIdx) && privFlags.getFlag(usesWinBtnDispIDX)) {AppMgr.handleShowWin(col, val);}
		else if((row == btnMseOvrFuncIdx) && privFlags.getFlag(usesMseOvrBtnDispIDX)) {
			if(val==0) {clearRowExceptPassedBtn(row,col);}
			AppMgr.handleMenuBtnMseOvDispSel(col, val==0);			
		}
		else if((row == btnDBGSelCmpIdx) && privFlags.getFlag(usesDbgBtnDispIDX)) {AppMgr.handleMenuBtnDebugSel(col, val);}
		else {AppMgr.handleMenuBtnSelCmp(row, funcBtnIDXOffset, col, val);}		
	}

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
		if(!pointInRectDim(mouseX, mouseY)){return false;}//not in this window's bounds, quit asap for speedz
		int i = (int)((mouseY-(initBtnLblYOff + clkFlgsStY))/(initTextHeightOff));					//TODO Awful - needs to be recalced, dependent on menu being on left
		//msgObj.dispInfoMessage(className, "hndlMouseClick_Indiv", "Clicked on disp windows : i : " + i+"|uiClkCoords[1] = "+uiClkCoords[1]+" | minBtnClkY :"+minBtnClkY);
		
		if((i>=0) && (i<numMainFlagsToShow)){
			AppMgr.flipMainFlag(i);return true;	
		} else if(MyMathUtils.ptInRange(mouseX, mouseY, uiClkCoords[0], minBtnClkY, uiClkCoords[2], uiClkCoords[3])){
			boolean clkInBtnRegion = checkButtons(mouseX, mouseY);
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
	/**
	 * draw UI buttons that control functions, debug and global load/save stubs
	 */
	private void drawSideBarButtons(float btnLblYOff, float xOffHalf, float rowStYOff){
		ri.translate(xOffHalf,(float)minBtnClkY);
		ri.setFill(new int[]{0,0,0}, 255);
		for(int row=0; row<guiBtnRowNames.length;++row){
			ri.showText(guiBtnRowNames[row],0,-initTextHeightOff*.15f);
			ri.translate(0,rowStYOff);
			float xWidthOffset = winInitVals.rectDim[2]/(1.0f * guiBtnLabels[row].length), halfWay;
			ri.pushMatState();
			ri.setStrokeWt(1.0f);
			ri.setColorValStroke(IRenderInterface.gui_Black,255);
			ri.noFill();
			ri.translate(-xOffHalf, 0);
			for(int col =0; col<guiBtnLabels[row].length;++col){
				halfWay = (xWidthOffset - ri.textWidth(guiBtnLabels[row][col]))/2.0f;
				ri.setColorValFill(guiBtnStFillClr[guiBtnSt[row][col]+1],255);
				ri.drawRect(new float[] {0,0,xWidthOffset, initTextHeightOff});	
				ri.setColorValFill(guiBtnStTxtClr[guiBtnSt[row][col]+1],255);
				ri.showText(guiBtnLabels[row][col], halfWay, initTextHeightOff*.75f);
				ri.translate(xWidthOffset, 0);
			}
			ri.popMatState();						
			ri.translate(0,btnLblYOff);
		}
	}//drawSideBarButtons	
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
				drawSideBarButtons(
						AppMgr.getBtnLabelYOffset(),
						xOffHalf,
						AppMgr.getRowStYOffset());						//draw buttons
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