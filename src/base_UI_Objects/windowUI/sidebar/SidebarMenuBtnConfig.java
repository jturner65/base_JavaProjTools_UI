package base_UI_Objects.windowUI.sidebar;

import java.util.ArrayList;

import base_Math_Objects.MyMathUtils;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;

/**
 * Struct to hold desired configuration of sidebar buttons
 * @author john
 *
 */
public class SidebarMenuBtnConfig {
	/**
	 * Gui-based application manager
	 */
	public static GUI_AppManager AppMgr;
	
	/**
	 * Render interface
	 */
	public static IRenderInterface ri;

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
	private int funcBtnIDXOffset=0;
	
	public boolean _initBtnShowWin = false, 	// specify a row of buttons to select multiple windows
			_initBtnMseFunc= false, 			// specify mouse-over buttons
			_initBtnDBGSelCmp = false;			// specify debug button group present (toggle instead of instant buttons)
	/**
	 * names for each row of buttons - idx 1 is name of row
	 */
	private String[] guiBtnRowNames;
	/**
	 * names for each row of buttons - idx 1 is name of row
	 */
	private String[][] guiBtnLabels;
	/**
	 * Width of labels for each button
	 */
	private Float[][] guiBtnLabelWidths;
	
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
	private int[][] guiBtnState;
	
	public final int[] guiBtnStateFillClr = new int[]{					//button colors based on state
			IRenderInterface.gui_White,								//disabled color for buttons
			IRenderInterface.gui_LightGray,							//not clicked button color
			IRenderInterface.gui_LightBlue,							//clicked button color
		};
	public final int[] guiBtnStateTxtClr = new int[]{					//text color for buttons
			IRenderInterface.gui_LightGray,							//disabled color for buttons
			IRenderInterface.gui_Black,								//not clicked button color
			IRenderInterface.gui_Black,								//clicked button color
		};	
	
	/**
	 * Y coord where gui button rows end
	 */
	public final float guiBtnRowsEndY;

	/**
	 * Where buttons should start on 
	 */
	/**
	 * Size of text height offset on creation TODO get rid of this value in favor of live updates
	 */
	private float initTextHeightOff;

	/**
	 * Offset for starting a new row in Y at creation TODO get rid of this value in favor of live updates
	 */
	private float initRowStYOff;	
	
	/**
	 * This is the UI clickable region for the application-wide buttons, under the application flags.
	 */
	private float[] UIAppButtonRegion;
	
	/**
	 * Configuration for important side bar features, 
	 * including to set row names for each row of ui action buttons getMouseOverSelBtnLabels()
	 * @param _AppMgr The Application manager
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _funcBtnLabels array of arrays of labels for each button
	 * @param _debugBtnLabels list of labels for debug buttons
	 * @param _winTitles the titles of all the windows in this application
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 */
	public SidebarMenuBtnConfig(IRenderInterface _ri, GUI_AppManager _AppMgr, String[] _funcRowNames, String[][] _funcBtnLabels, String[] _debugBtnLabels, String[] _winTitles, boolean _inclWinNames, boolean _inclMseOvValues) {
		ri=_ri; AppMgr = _AppMgr;
		initTextHeightOff = AppMgr.getTextHeightOffset();
		initRowStYOff = AppMgr.getRowStYOffset();
		// build all side bar menu buttons with format specific to instancing application
		// returns the y location of where the UI clickable region for the display window should start
		guiBtnRowsEndY = buildBtnData(_funcRowNames, _funcBtnLabels, _debugBtnLabels, _winTitles, _inclWinNames, _inclMseOvValues);
	}

	public void setAppButtonRegion(float[] rectDim, float yOffset) {
		UIAppButtonRegion = new float[4];		
		UIAppButtonRegion[0] = rectDim[0] + .01f * rectDim[2];
		UIAppButtonRegion[1] = yOffset;
		UIAppButtonRegion[2] = rectDim[0] + .99f * rectDim[2];
		UIAppButtonRegion[3] = yOffset + initRowStYOff +guiBtnRowsEndY;		
	}
	

	/**
	 * Set values for ui action buttons, based on specifications of @class mySidebarMenuBtnConfig
	 * Parameters user defined in main as window is specified, individual button names can be overridden in individual app windows
	 * @param funcRowNames array of names for each row of functional buttons 
	 * @param funcBtnLabels array of arrays of labels for each button
	 * @param debugBtnLabels list of labels for debug buttons
	 * @param winTitles the titles of all the windows in this application
	 * @param inclWinNames include the names of all the instanced windows
	 * @param inclMseOvValues include a row for possible mouse over values
	 * @return
	 */
	public float buildBtnData(
			String[] funcRowNames, 
			String[][] funcBtnLabels, 
			String[] debugBtnLabels, 
			String[] winTitles, 
			boolean inclWinNames, 
			boolean inclMseOvValues) {
		int[] numBtnsPerFuncRow = new int[funcRowNames.length];
		for (int i=0;i<funcRowNames.length;++i) {
			numBtnsPerFuncRow[i] = funcBtnLabels[i].length;
		}		
		ArrayList<String> tmpGuiBtnRowNames = new ArrayList<String>();
		ArrayList<String[]> tmpBtnLabels = new ArrayList<String[]>();
		ArrayList<Float[]> tmpBtnLabelWidths = new ArrayList<Float[]>();
		ArrayList<String[]> tmpDfltBtnLabels = new ArrayList<String[]>();
		
		ArrayList<Boolean[]> tmpBtnIsInst = new ArrayList<Boolean[]>();
		ArrayList<Boolean[]> tmpBtnWaitForProc = new ArrayList<Boolean[]>();
		int numFuncBtnArrayNames = funcRowNames.length;
		//if debug btn names array is not empty in config, add debug button names row to funcBtnIDXAra
		int debugBtnRowIDX = (debugBtnLabels.length > 0) 
				? numFuncBtnArrayNames++ : -1;
		
		funcBtnIDXAra = new int[numFuncBtnArrayNames];
		
		btnDBGSelCmpIdx = -1;
		btnMseOvrFuncIdx = -1;
		funcBtnIDXOffset = 0;
		
		String[] titleArray = new String[winTitles.length-1];
		Float[] titleLengths = new Float[winTitles.length-1];
		if((inclWinNames)&&(titleArray.length != 0)) {
			for(int i=0;i<titleArray.length;++i) {titleArray[i] =winTitles[i+1];titleLengths[i]=ri.getTextWidth(titleArray[i]);}
			tmpBtnLabels.add(titleArray);
			tmpBtnLabelWidths.add(titleLengths);
			tmpDfltBtnLabels.add(titleArray);

			tmpBtnIsInst.add(buildDfltBtnFlagAra(titleArray.length));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(titleArray.length));
			
			tmpGuiBtnRowNames.add("Display Window");

			_initBtnShowWin = true;
			++funcBtnIDXOffset;
		} else {			_initBtnShowWin= false;		}
		
		String[] mseOvrBtnLabels = AppMgr.getMouseOverSelBtnLabels();
		if((inclMseOvValues) && (mseOvrBtnLabels!=null) && (mseOvrBtnLabels.length > 0)) {
			tmpBtnLabels.add(mseOvrBtnLabels);
			Float[] mseOvrBtnLblLengths = new Float[mseOvrBtnLabels.length];
			for(int i=0;i<mseOvrBtnLblLengths.length;++i ) {mseOvrBtnLblLengths[i] = ri.getTextWidth(mseOvrBtnLabels[i]);}
			tmpBtnLabelWidths.add(mseOvrBtnLblLengths);			
			tmpDfltBtnLabels.add(mseOvrBtnLabels);
		
			tmpBtnIsInst.add(buildDfltBtnFlagAra(mseOvrBtnLabels.length));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(mseOvrBtnLabels.length));

			tmpGuiBtnRowNames.add("Mouse Over Info To Display");
			btnMseOvrFuncIdx = funcBtnIDXOffset;  //
			++funcBtnIDXOffset;
			_initBtnMseFunc= true;
		} else {			_initBtnMseFunc= false;}
		
 		for(int i=0;i<numBtnsPerFuncRow.length;++i) {
			String s = funcRowNames[i];
			String[] btnNameAra = buildBtnNameAra(numBtnsPerFuncRow[i],"Func"); 
			Float[] btnNameLengths = new Float[btnNameAra.length];
			tmpBtnLabels.add(btnNameAra);
			for(int j=0;j<btnNameLengths.length;++j ) {btnNameLengths[j] = ri.getTextWidth(btnNameAra[j]);}
			tmpBtnLabelWidths.add(btnNameLengths);			
			tmpDfltBtnLabels.add(buildBtnNameAra(numBtnsPerFuncRow[i],"Func"));
			
			tmpBtnIsInst.add(buildDfltBtnFlagAra(numBtnsPerFuncRow[i]));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(numBtnsPerFuncRow[i]));
			funcBtnIDXAra[i]=i+funcBtnIDXOffset;
			tmpGuiBtnRowNames.add(s);
		}
		if(debugBtnRowIDX >= 0) {
			String[] btnNameAra = buildBtnNameAra(debugBtnLabels.length,"Debug");
			Float[] btnNameLengths = new Float[btnNameAra.length];
			tmpBtnLabels.add(btnNameAra);
			for(int j=0;j<btnNameLengths.length;++j ) {btnNameLengths[j] = ri.getTextWidth(btnNameAra[j]);}
			tmpBtnLabelWidths.add(btnNameLengths);			
			tmpDfltBtnLabels.add(buildBtnNameAra(debugBtnLabels.length,"Debug"));
			
			tmpBtnIsInst.add(buildDfltBtnFlagAra(debugBtnLabels.length));
			tmpBtnWaitForProc.add(buildDfltBtnFlagAra(debugBtnLabels.length));
			tmpGuiBtnRowNames.add("DEBUG");
			btnDBGSelCmpIdx = tmpGuiBtnRowNames.size()-1;
			funcBtnIDXAra[debugBtnRowIDX]=debugBtnRowIDX+funcBtnIDXOffset;
			_initBtnDBGSelCmp = true;
		} else {	_initBtnDBGSelCmp = false;	}
		
		guiBtnRowNames = tmpGuiBtnRowNames.toArray(new String[0]);		
		guiBtnLabels = tmpBtnLabels.toArray(new String[0][]);
		guiBtnLabelWidths = tmpBtnLabelWidths.toArray(new Float[0][]);
		defaultUIBtnLabels = tmpDfltBtnLabels.toArray(new String[0][]);
		guiBtnInst = tmpBtnIsInst.toArray(new Boolean[0][]);
		guiBtnWaitForProc = tmpBtnWaitForProc.toArray(new Boolean[0][]);
		
		guiBtnState = new int[guiBtnRowNames.length][];
		for(int i=0;i<guiBtnState.length;++i) {guiBtnState[i] = new int[guiBtnLabels[i].length];}
		
		//set button names
		for(int _type = 0;_type<funcBtnLabels.length;++_type) {setAllFuncBtnLabels(_type,funcBtnLabels[_type]);}
		//set debug button names from valus specified in btnConfig, if any provided
		if (_initBtnDBGSelCmp) {
			setAllFuncBtnLabels(debugBtnRowIDX,debugBtnLabels);
		}	
		// y span of the buttons
		return (guiBtnRowNames.length * 2.0f) * initTextHeightOff;
	}//buildBtnData
	
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
	
	/**
	 * call this from each new window to set function btn labels, if specified, when window gets focus
	 * @param rowIdx
	 * @param BtnLabels
	 */
	public void setAllFuncBtnLabels(int _funRowIDX, String[] BtnLabels) {
		int rowIdx = funcBtnIDXAra[_funRowIDX];		
		String[] replAra = ((null==BtnLabels) || (BtnLabels.length != guiBtnLabels[rowIdx].length)) ? defaultUIBtnLabels[rowIdx] : BtnLabels;
		for(int i=0;i<guiBtnLabels[rowIdx].length;++i) {
			guiBtnLabels[rowIdx][i]=replAra[i]; 
			guiBtnLabelWidths[rowIdx][i] = ri.getTextWidth(guiBtnLabels[rowIdx][i]);
		}
	}//setFunctionButtonNames
	
	/**
	 * turn off buttons that may be on and should be turned off - called at release of mouse - check for mouse loc before calling (in button region)?
	 */
	public final void clearAllBtnStates(){	
		//guiBtnWaitForProc should only be set for non-momentary buttons when they are pushed and cleared when whatever they are do is complete
		for(int row=0; row<guiBtnRowNames.length;++row){for(int col =0; col<guiBtnLabels[row].length;++col){				
			if((guiBtnState[row][col]==1) && (guiBtnInst[row][col]  || !guiBtnWaitForProc[row][col])){	guiBtnState[row][col] = 0;}//btn is on, and either is momentary or it is not waiting for processing
		}}		
	}//clearAllBtnStates
	
	/**
	 * clear the passed row of buttons except for the indicated btn - to enable single button per row radio-style buttons
	 * @param row row of buttons to clear
	 * @param btnToKeepOn btn whose state should stay on
	 */
	public final void clearRowExceptPassedBtn(int row, int btnToKeepOn) {
		for(int col =0; col<guiBtnLabels[row].length;++col){	guiBtnState[row][col] = 0;}
		guiBtnState[row][btnToKeepOn]=1;
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
	 * Return the label on the sidebar button specified by the passed row and column
	 * @param row
	 * @param col
	 * @return
	 */
	public final String getSidebarMenuButtonLabel(int row, int col) {
		if(row >= guiBtnLabels.length) {		return "Row " +row+" too high for max rows : "+guiBtnLabels.length;}
		if(col >= guiBtnLabels[row].length) {	return "Col " +col+" too high for max cols @ row " + row+" : "+guiBtnLabels[row].length;}
		return guiBtnLabels[row][col];}
	
	/**
	 * handle click on button region of menubar
	 * @param row
	 * @param col
	 */
	private final void handleButtonClick(int row, int col){
		int val = guiBtnState[row][col];//initial state, before being changed
		guiBtnState[row][col] = (guiBtnState[row][col] + 1)%2;//change state
		//if not momentary buttons, set wait for proc to true
		setWaitForProc(row,col);
		// click in window select button region
		if((row == SidebarMenu.btnShowWinIdx) && _initBtnShowWin) {AppMgr.handleShowWin(col, val);}
		// click in mouse-over button region
		else if((row == btnMseOvrFuncIdx) && _initBtnMseFunc) {
			if(val==0) {clearRowExceptPassedBtn(row,col);}
			AppMgr.handleMenuBtnMseOvDispSel(col, val==0);			
		}
		// click in debug region
		else if((row == btnDBGSelCmpIdx) && _initBtnDBGSelCmp) {AppMgr.handleMenuBtnDebugSel(col, val);}
		// click in main button region
		else {AppMgr.handleMenuBtnSelCmp(row, funcBtnIDXOffset, col, val);}		
	}	
	
	/**
	 * check if buttons clicked. TODO Create a button class that will manage its own hotspot
	 * @param mseX
	 * @param mseY
	 * @return
	 */
	public boolean checkButtons(int mseX, int mseY, float winWidth){
		double stY = UIAppButtonRegion[1] + initRowStYOff, endY = 0, 
				stX = 0, endX, widthX; //btnLblYOff			
		for(int row=0; row<guiBtnRowNames.length;++row){
			endY = stY + initTextHeightOff;	
			widthX = winWidth/(1.0f * guiBtnLabels[row].length);
			stX = 0;	endX = widthX;
			for(int col =0; col<guiBtnLabels[row].length;++col){	
				if((MyMathUtils.ptInRange(mseX, mseY,stX, stY, endX, endY)) && (guiBtnState[row][col] != -1)){
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
	
	public boolean checkInButtonRegion(float mouseX, float mouseY) {
		return MyMathUtils.ptInRange(mouseX, mouseY, UIAppButtonRegion[0], UIAppButtonRegion[1], UIAppButtonRegion[2], UIAppButtonRegion[3]);
	}
	
	public Boolean[][] getGuiBtnWaitForProc() {return guiBtnWaitForProc;}
	public void setGuiBtnWaitForProc(Boolean[][] _guiBtnWaitForProc) {		guiBtnWaitForProc = _guiBtnWaitForProc;}
	public int[][] getGuiBtnState() {		return guiBtnState;	}
	public void setGuiBtnState(int[][] _guiBtnState) {	guiBtnState = _guiBtnState;	}	
	public float[] getUIAppBtnRegion() {return UIAppButtonRegion;}
	/**
	 * draw UI buttons that control functions, debug and global load/save stubs
	 */
	public void drawSideBarButtons(float btnLblYOff, float xOffHalf, float rowStYOff, float xWidth){
		ri.translate(xOffHalf,(float)UIAppButtonRegion[1]);
		ri.setFill(new int[]{0,0,0}, 255);
		float halfWay;
		for(int row=0; row<guiBtnRowNames.length;++row){
			ri.showText(guiBtnRowNames[row],0,-initTextHeightOff*.15f);
			ri.translate(0,rowStYOff);
			float xWidthOffset = xWidth/(1.0f * guiBtnLabels[row].length);
			ri.pushMatState();
			ri.setStrokeWt(1.0f);
			ri.setColorValStroke(IRenderInterface.gui_Black,255);
			ri.noFill();
			ri.translate(-xOffHalf, 0);
			for(int col =0; col<guiBtnLabels[row].length;++col){
				halfWay = (xWidthOffset - guiBtnLabelWidths[row][col])/2.0f;
				ri.setColorValFill(guiBtnStateFillClr[guiBtnState[row][col]+1],255);
				ri.drawRect(new float[] {0,0,xWidthOffset, initTextHeightOff});	
				ri.setColorValFill(guiBtnStateTxtClr[guiBtnState[row][col]+1],255);
				ri.showText(guiBtnLabels[row][col], halfWay, initTextHeightOff*.75f);
				ri.translate(xWidthOffset, 0);
			}
			ri.popMatState();						
			ri.translate(0,btnLblYOff);
		}
	}//drawSideBarButtons	
	
}//class mySidebarMenuBtnConfig
