package base_UI_Objects.windowUI.base;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.UIObjectManager;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.drawnTrajectories.TrajectoryManager;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.ScrollBars;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_Utils_Objects.io.file.FileIOManager;
import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.io.messaging.MsgCodes;

/**
 * abstract class to hold base code for a menu/display window (2D for gui, etc), 
 * to handle displaying and controlling the window, and calling the implementing 
 * class for the specifics
 * @author john
 *
 */
public abstract class Base_DispWindow implements IUIManagerOwner{
	/**
	 * Render interface
	 */
	public static IRenderInterface ri;
	/**
	 * Gui-based application manager
	 */
	public static GUI_AppManager AppMgr;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;

	/**
	 * Manager of all UI objects in this window
	 */
	protected UIObjectManager uiMgr;

	/**
	 * Window initialization values - open and closed dims, colors
	 */
	protected final GUI_AppWinVals winInitVals;
	
	/**
	 * file IO object to manage IO
	 */
	protected FileIOManager fileIO;
	/**
	 * class name of instancing class
	 */
	protected final String className;
	
	public final int ID;
	//Counter of how many windows are built in the application. Used to specify unique ID for each new window
	private static int winCnt = 0;
	
	/**
	 * x,y location and width,height of clickable close/open box in upper right corner of closeable windows
	 */
	private float[] closeBox;	
	/**
	 * current visible screen width and height
	 */
	public float[] curVisScrDims;
	
	/**
	 * enable drawing debug info onto app canvas	
	 */
	private ArrayList<String> DebugInfoAra;	
	//count of draw cycles for consoleString decay
	private int drawCount = 0;
		
	//how long a message should last before it is popped from the console strings deque (how many frames)
	private static final int cnslStrDecay = 10;
	
	/**
	 * the window list idx in the App Manager that controls this window - use -1 for none.
	 * (means this window is owned and managed by another window)
	 */
	public int dispFlagWinIDX;	
	
	/**
	 * Flags controlling the state of this window
	 */
	protected WinDispStateFlags dispFlags;
	
	/**
	 * Base_GUIObj that was clicked on for modification
	 */
	protected boolean msClickInUIObj;
	
	/**
	 * Boolean array of default behavior boolean values, if formatting is not otherwise specified
	 *  idx 0: value is sent to owning window,  
	 *  idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *  idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 */
	protected final boolean[] dfltUIBehaviorVals = new boolean[]{true, false, false};
	/**
	 * Boolean array of default UI format values, if formatting is not otherwise specified : 
	 *  idx 0: whether multi-line(stacked) or not                                                  
	 *  idx 1: if true, build prefix ornament                                                      
	 *  idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 */
	protected final boolean[] dfltUIFmtVals =  new boolean[] {false, true, false};
	
	/**
	 * box holding x,y,w,h values of black rectangle to form around menu for display variables on right side of screen, if present
	 */
	private float[] UIRtSideRectBox;
	/**
	 * closed window box
	 */
	private float[] closedUIRtSideRecBox;	
		
	//key pressed
	protected char keyPressed = ' ';
	protected int keyCodePressed = 0;	
	
	///////////////////////////////////////
	// traj stuff
	
	/**
	 * object to manage any drawn trajectories
	 */
	protected TrajectoryManager trajMgr;

	///////////
	//display and camera related variables - managed per window
	/**
	 * Camera x rotation for this window
	 */
	private float rx;
	/**
	 * Camera y rotation
	 */
	private float ry;		
	/**
	 * Distance to camera. Manipulated with wheel or shift-rt mse btn
	 */
	private float dz;	

	/**
	 * target of focus - used in translate to set where the camera is looking - allow for modification
	 */
	private myVectorf focusTar;							
	/**
	 * Set this value to be different display center translations -to be used to calculate mouse offset in world for pick
	 * and also as origin for 
	 */
	private myPointf sceneOriginVal;							
	
	//to control how much is shown in the window - if stuff extends off the screen and for 2d window
	protected ScrollBars[] scbrs;
		
	/**
	 * directory with proper timestamp from when window was made
	 */
	protected final String ssFolderDir;
	/**
	 * path to save screenshots for this dispwindow
	 */
	protected final String ssPathBase;

	/**
	 * These ints hold the index of which custom functions or debug functions should be launched. 
	 * These are set when the sidebar menu is clicked and these processes are requested, and they 
	 * are set to -1 when these processes are launched. this is so the buttons can be turned on
	 * before the process starts.
	 * Using this is sub-optimal solution - needs an index per sidebar button on each row; using
	 * more than necessary, otherwise will crash if btn idx >= curCustBtn.length
	 */
	protected int[] curCustBtn = new int[] {-1,-1,-1,-1,-1,-1,-1,-1};
	//type/row of current button selected
	protected int curCstBtnRow = -1;
	//offset to where buttons begin, if using windows and/or mse control
	protected int curCstFuncBtnOffset = 0;	
	/**
	 * this is set to true when curCustXXX vals are set to != -1;
	 */
	private boolean custClickSetThisFrame = false; 
	/**
	 * this is used as a 1-frame buffer to allow the UI to turn on the source buttons of these functions
	 */
	private boolean custFuncDoLaunch = false;
	
	/**
	 * Build this window
	 * @param _p
	 * @param _AppMgr
	 * @param _winIdx
	 * @param _winInitVals
	 */
	private Base_DispWindow(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx, GUI_AppWinVals _winInitVals) {
		ri=_p;
		AppMgr = _AppMgr;
		msgObj = AppMgr.msgObj;
		className = this.getClass().getSimpleName();
		ID = winCnt++;
		dispFlagWinIDX = _winIdx;
		winInitVals = _winInitVals;	
		fileIO = new FileIOManager(msgObj, winInitVals.winName);
		//base screenshot path based on launch time
		ssFolderDir = winInitVals.winName+"_"+getNowDateTimeString();
		ssPathBase = AppMgr.getApplicationPath() +File.separatorChar + ssFolderDir + File.separatorChar;
	
		closeBox = new float[4];
		float boxWidth = 1.1f*winInitVals.rectDim[0];
		UIRtSideRectBox = new float[] {winInitVals.rectDim[2]-boxWidth,0,boxWidth, winInitVals.rectDim[3]};		
		closedUIRtSideRecBox = new float[] {winInitVals.rectDim[2]-20,0,20,winInitVals.rectDim[3]};
		curVisScrDims = new float[] {closedUIRtSideRecBox[0],winInitVals.rectDim[3]};
		
		msClickInUIObj = false;
		reInitInfoStr();
		sceneOriginVal = new myPointf(winInitVals.sceneOriginVal);
		focusTar = new myVectorf(winInitVals.initSceneFocusVal);
		//initialize the camera
		setInitCamView();
		uiMgr = new UIObjectManager(ri, this, AppMgr, msgObj);
	}//ctor
	
	/**
	 * Independent window based constructor - use this for all windows that are built and managed
	 * independently of AppMgr
	 * @param _p
	 * @param _AppMgr
	 * @param _winIdx
	 */
	public Base_DispWindow(IRenderInterface _p, GUI_AppManager _AppMgr, GUI_AppWinVals _winInitVals) {
		this(_p, _AppMgr, -1, _winInitVals);	
	}//ctor
	
	/**
	 * AppMgr based constructor - use this for all windows that are registered with and directly displayed by AppMgr
	 * @param _p
	 * @param _AppMgr
	 * @param _winIdx
	 */
	public Base_DispWindow(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx) {
		this(_p, _AppMgr, _winIdx, _AppMgr.winInitVals[_winIdx]);	
	}//ctor
	

	/**
	 * Must be called by inheriting class constructor!
	 * @param _isMenu whether this window is the side-bar menu window or not
	 */
	public final void initThisWin(boolean _isMenu){
		dispFlags = new WinDispStateFlags(this);	
		// build all ui objects
		uiMgr.initAllGUIObjects();
		//run instancing window-specific initialization after all ui objects are built
		initMe();
		//set any custom button names if necessary
		setCustMenuBtnLabels();
		//pass all flag states to initialized structures in instancing window handler
		uiMgr.refreshPrivFlags();		
		_setClosedBox();		
		if((!_isMenu) && (dispFlags.getHasScrollBars())){scbrs = new ScrollBars[4];	for(int i =0; i<scbrs.length;++i){scbrs[i] = new ScrollBars(ri, this);}}
		dispFlags.setIs3DWin(winInitVals.dispWinIs3D());
		dispFlags.setCanChgView(winInitVals.canMoveView());
		if(winInitVals.canDrawInWin()) {
			trajMgr = new TrajectoryManager(this,!winInitVals.dispWinIs3D());
			trajMgr.setTrajColors(winInitVals.trajFillClr, winInitVals.trajStrkClr);
		} else {
			trajMgr = null;
		}
	}//initThisWin
	
	/**
	 * UIObjectManager will call this.
	 */
	@Override
	public void initOwnerStateDispFlags() {		initStateDispFlags();	}
	
	/**
	 * Set initial state and initialize gui objects. This is overridden by SidebarMenu
	 */
	protected final void initStateDispFlags() {
		//Initialize dispFlags settings based on AppMgr
		//Does this window include a runnable sim (launched by main menu flag)
		dispFlags.setIsRunnable(AppMgr.getBaseFlagIsShown_runSim());
		//Is this window capable of showing right side menu
		dispFlags.setHasRtSideMenu(AppMgr.getBaseFlagIsShown_showRtSideMenu());
		//initialize/override any state/display flags
		initDispFlags();					
	}//initStateDispFlags
	
	
	/**
	 * Initialize window's application-specific logic
	 */
	protected abstract void initMe();
	/**
	 * Initialize any UI control flags appropriate for window application
	 */
	protected abstract void initDispFlags();

	/**
	 * Build appropriate UIDataUpdater instance for application. ui manager calls this
	 * @return
	 */	
	@Override
	public UIDataUpdater buildOwnerUIDataUpdateObject() {return buildUIDataUpdateObject();}	
	/**
	 * Retrieve the Owner's UIDataUpdater
	 * @return
	 */
	@Override
	public UIDataUpdater getUIDataUpdater() {return uiMgr.getUIDataUpdater();}
	
	/**
	 * Build appropriate UIDataUpdater instance for application
	 * @return
	 */
	protected abstract UIDataUpdater buildUIDataUpdateObject();
	
	/**
	 * Build all UI objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
	 * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
	 * 			- The object IDX                   
	 *          - A double array of min/max/mod values                                                   
	 *          - The starting value                                                                      
	 *          - The label for object                                                                       
	 *          - The object type (GUIObj_Type enum)
	 *          - A boolean array of behavior configuration values : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *          - A boolean array of renderer format values :(unspecified values default to false) - Behavior Boolean array must also be provided!
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color 
	 */
	@Override
	public final void setupOwnerGUIObjsAras(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap) {
		// build the Non button UI objects
		setupGUIObjsAras(tmpUIObjMap);
	}
	/**
	 * Build UI button objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
	 * @param firstIdx : the first index to use in the map/as the objIdx
	 * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
	 * 				the first element is the object index
	 * 				the second element is true label
	 * 				the third element is false label
	 * 				the final element is integer flag idx 
	 */
	@Override
	public final void setupOwnerGUIBoolSwitchAras(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {
		// build the button region, using the existing size of the non-button map as a start index
		setupGUIBoolSwitchAras(firstIdx, tmpUIBoolSwitchObjMap);
	}
		
	/**
	 * Build all UI objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
	 * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
	 * 			- The object IDX                   
	 *          - A double array of min/max/mod values                                                   
	 *          - The starting value                                                                      
	 *          - The label for object                                                                       
	 *          - The object type (GUIObj_Type enum)
	 *          - A boolean array of behavior configuration values : (unspecified values default to false)
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 *          - A boolean array of renderer format values :(unspecified values default to false) - Behavior Boolean array must also be provided!
	 * 				- Should be multiline
	 * 				- One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
	 * 				- Force this object to be on a new row/line (For side-by-side layouts)
	 * 				- Text should be centered (default is false)
	 * 				- Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 				- Should have ornament
	 * 				- Ornament color should match label color 
	 */
	protected abstract void setupGUIObjsAras(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap);
	
	/**
	 * Build UI button objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
	 * @param firstIdx : the first index to use in the map/as the objIdx
	 * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
	 * 				the first element is the object index
	 * 				the second element is true label
	 * 				the third element is false label
	 * 				the final element is integer flag idx 
	 */
	protected abstract void setupGUIBoolSwitchAras(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap);		
	
	/**
	 * Called by privFlags bool struct, to update uiUpdateData when boolean flags have changed
	 * @param idx
	 * @param val
	 */
	@Override
	public final void checkSetBoolAndUpdate(int idx, boolean val) {uiMgr.checkSetBoolAndUpdate(idx, val);	}

	/**
	 * This will check if boolean value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	protected final boolean checkAndSetBoolValue(int idx, boolean value) {return uiMgr.checkAndSetBoolValue(idx, value);}
	/**
	 * This will check if Integer value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	protected final boolean checkAndSetIntVal(int idx, int value) {return uiMgr.checkAndSetIntVal(idx, value);}
	/**
	 * This will check if float value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	protected final boolean checkAndSetFloatVal(int idx, float value) {return uiMgr.checkAndSetFloatVal(idx, value);}
	
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	@Override
	public final void updateBoolValFromExecCode(int idx, boolean value) {uiMgr.updateBoolValFromExecCode(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	@Override
	public final void updateIntValFromExecCode(int idx, int value) {uiMgr.updateIntValFromExecCode(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	@Override
	public final void updateFloatValFromExecCode(int idx, float value) {uiMgr.updateFloatValFromExecCode(idx, value);}
	
	@Override
	public void updateOwnerCalcObjUIVals() {updateCalcObjUIVals();}	
	/**
	 * This function is called on ui value update, to pass new ui values on to window-owned consumers
	 */
	protected abstract void updateCalcObjUIVals();
		
	/**
	 * Set up initial trajectories - 2d array, 1 per UI Page, 1 per modifiable construct within page.
	 */
	public final void initDrwnTrajs(){
		if(null!=trajMgr) {		trajMgr.initDrwnTrajs();	initDrwnTraj_Indiv();				}
	}
	protected abstract void initDrwnTraj_Indiv();
		
	protected final void setVisScreenWidth(float visScrWidth) {setVisScreenDims(visScrWidth,curVisScrDims[1]);}
	protected final void setVisScreenHeight(float visScrHeight) {setVisScreenDims(curVisScrDims[0],visScrHeight);}
	/**
	 * based on current visible screen width, set map and calc analysis display locations
	 * @param visScrWidth
	 * @param visScrHeight
	 */
	protected final void setVisScreenDims(float visScrWidth, float visScrHeight) {
		curVisScrDims[0] = visScrWidth;
		curVisScrDims[1] = visScrHeight;
		setVisScreenDimsPriv();
	}//calcAndSetMapLoc
	/**
	 * Set implementation window-specific variables that are based on current visible screen dimensions curVisScrDims
	 */
	protected abstract void setVisScreenDimsPriv();
	
	/**
	 * UI Manager access to this function to retrieve appropriate initial uiClkCoords.
	 * @return
	 */
	@Override
	public final float[] getOwnerParentWindowUIClkCoords() {		return getParentWindowUIClkCoords();	}
	/**
	 * Get the click coordinates formed by the parent
	 * @return
	 */
	protected float[] getParentWindowUIClkCoords() {
		float [] menuUIClkCoords = AppMgr.getUIRectVals(ID);		
		return new float[] {menuUIClkCoords[0],menuUIClkCoords[3],menuUIClkCoords[2],menuUIClkCoords[3]};
	}
	
	
	/**
	 * Set the right side menu state for this window - if it is actually present, show it
	 * @param visible
	 */
	public final void setRtSideInfoWinSt(boolean visible) {dispFlags.setRtSideInfoWinSt(visible);}
	
	/**
	 * Set whether or not the global debug mode has been activated
	 * @param dbg
	 */
	public final void setIsGlobalDebugMode(boolean dbg) {dispFlags.setIsDebug(dbg);}

	/**
	 * UI code-level Debug mode functionality. Called only from flags structure from GUI_AppManager debug button. Enables debug mode in all windows!
	 * @param enable
	 */
	public final void handleDispFlagsDebugMode(boolean enable) {
		_dispDbgMsg("handleDispFlagsDebugMode", "Start UI Code-specific Debug, called from base window Debug flags with value "+ enable +".");
		handleDispFlagsDebugMode_Indiv(enable);
		_dispDbgMsg("handleDispFlagsDebugMode", "End UI Code-specific Debug, called from base window Debug flags with value "+ enable +".");
	}
	protected abstract void handleDispFlagsDebugMode_Indiv(boolean val);

	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from UI manager
	 * @param enable
	 */
	@Override
	public final void handleOwnerPrivFlagsDebugMode(boolean enable) {
		handlePrivFlagsDebugMode_Indiv(enable);
	}
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	protected abstract void handlePrivFlagsDebugMode_Indiv(boolean val);

	/**
	 * Switch structure only that handles priv flags being set or cleared. Called from UI Manager
	 * @param idx
	 * @param val new value for this index
	 * @param oldVal previous value for this index
	 */
	@Override
	public void handleOwnerPrivFlags(int idx, boolean val, boolean oldVal) {
		handlePrivFlags_Indiv(idx, val, oldVal);
	}
	protected abstract void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal);

	
	/**
	 * Custom handling of when show is set or cleared. Called from dispFlags handling
	 * @param val
	 */
	public void handleShowWinFromFlags(boolean val) {
		_setClosedBox();
		if(!val){		closeMe();}	//not showing window :specific instancing window implementation stuff to do when hidden/transitioning to another window (i.e. suspend stuff running outside draw loop, or release memory of unnecessary stuff)
		else {			showMe();}	//specific instance window functionality to do when window is shown
	}//handleShowWin	
	protected abstract void closeMe();	
	protected abstract void showMe();
	
	/**
	 * Custom handling of when showRightSideMenu is set or cleared
	 * @param val
	 */
	public void handleShowRtSideMenu(boolean val) {
		float visWidth = (val ?  UIRtSideRectBox[0] : closedUIRtSideRecBox[0]);		//to match whether the side bar menu is open or closed
		setVisScreenWidth(visWidth);
	}
	
	/**
	 * Set initial values for private flags for instancing window - set before initMe is called
	 */	
	public int[] getOwnerFlagIDXsToInitToTrue() {return getFlagIDXsToInitToTrue();}
	/**
	 * Set initial values for private flags for instancing window - set before initMe is called
	 */
	protected abstract int[] getFlagIDXsToInitToTrue();
	
	/**
	 * this will set the height of the rectangle enclosing this window - this will be called when a 
	 * window pushes up or pulls down this window - this resizes any drawn trajectories in this 
	 * window, and calls the instance class's code for resizing
	 * @param height
	 * 
	 * TODO DEPRECATE THIS
	 */
	public final void setRectDimsY(float height){
		float oldVal = dispFlags.getShowWin() ? winInitVals.rectDim[3] : winInitVals.rectDim[3];
		winInitVals.rectDim[3] = height;
		float scale  = height/oldVal;			//scale of modification - rescale the size and location of all components of this window by this
		if(null!=trajMgr) {		trajMgr.setTrajRectDimsY(height, scale);}
		if(dispFlags.getHasScrollBars()){for(int i =0; i<scbrs.length;++i){scbrs[i].setSize();}}
		resizeMe(scale);
	}
	protected abstract void resizeMe(float scale);	

	/**
	 * Returns string holding reasonable string name for a subdir for this application. Includes name of window and timestamp when window was instanced
	 * @return
	 */
	public final String getAppFileSubdirName() {		return ssFolderDir;	}
	
	/**
	 * This returns a date-time string properly formatted to be used in file names or file paths. Time is when called
	 * @return
	 */
	public final String getNowDateTimeString() {
		String tmpNow = msgObj.getCurrWallTime();
		tmpNow = tmpNow.replace(':','_');
		tmpNow = tmpNow.replace('-','_');
		tmpNow = tmpNow.replace('|','_');
		return tmpNow;
	}
	
	/**
	 * Called if int-handling guiObjs[UIidx] (int or list) has new data which updated UI adapter. 
	 * Intended to support custom per-object handling by owning window.
	 * Only called if data changed!
	 * @param UIidx Index of gui obj with new data
	 * @param ival integer value of new data
	 * @param oldVal integer value of old data in UIUpdater
	 */
	@Override
	public void setUI_OwnerIntValsCustom(int UIidx, int ival, int oldVal) {
		setUI_IntValsCustom(UIidx, ival, oldVal);
	}
	
	/**
	 * Called if float-handling guiObjs[UIidx] has new data which updated UI adapter. 
	 * Intended to support custom per-object handling by owning window.
	 * Only called if data changed!
	 * @param UIidx Index of gui obj with new data
	 * @param val float value of new data
	 * @param oldVal float value of old data in UIUpdater
	 */
	@Override
	public void setUI_OwnerFloatValsCustom(int UIidx, float val, float oldVal) {
		setUI_FloatValsCustom(UIidx, val, oldVal);
	}
	
	/**
	 * Called if int-handling guiObjs[UIidx] (int or list) has new data which updated UI adapter. 
	 * Intended to support custom per-object handling by owning window.
	 * Only called if data changed!
	 * @param UIidx Index of gui obj with new data
	 * @param ival integer value of new data
	 * @param oldVal integer value of old data in UIUpdater
	 */
	protected abstract void setUI_IntValsCustom(int UIidx, int ival, int oldVal);
	
	/**
	 * Called if float-handling guiObjs[UIidx] has new data which updated UI adapter. 
	 * Intended to support custom per-object handling by owning window.
	 * Only called if data changed!
	 * @param UIidx Index of gui obj with new data
	 * @param val float value of new data
	 * @param oldVal float value of old data in UIUpdater
	 */
	protected abstract void setUI_FloatValsCustom(int UIidx, float val, float oldVal);
		
	//////////////////////
	//camera stuff
	
	/**
	 * resets camera view and focus target to initial values
	 */
	public final void setInitCamView(){
		rx = winInitVals.initCameraVals[0];
		ry = winInitVals.initCameraVals[1];
		dz = winInitVals.initCameraVals[2];	
		resetViewFocus();
	}//setCamView()	
	public final float getCamRotX() {return rx;}
	public final float getCamRotY() {return ry;}
	public final float getCamDist() {return dz;}	
	
	protected final void setCameraBase(float[] camVals) {
		ri.setCameraWinVals(camVals);  
		//if(this.flags[this.debugMode]){_dispWarnMsg("setCameraBase","rx :  " + rx + " ry : " + ry + " dz : " + dz);}
		// puts origin of all drawn objects at screen center and moves forward/away by dz
		ri.translate(camVals[0],camVals[1],(float)dz); 
	    setCamOrient();	
	}
	public final void setCamera(float[] camVals){
		if(dispFlags.getUseCustCam()){setCamera_Indiv(camVals);}//individual window camera handling
		else {						setCameraBase(camVals);	}
	}//setCamera
	protected abstract void setCamera_Indiv(float[] camVals);

	/**
	 * used to handle camera location/motion
	 */
	public final void setCamOrient(){ri.setCamOrient(rx,ry); }//sets the rx, ry, pi/2 orientation of the camera eye	
	/**
	 * used to draw text on screen without changing mode - reverses camera orientation setting
	 */
	public final void unSetCamOrient(){ri.unSetCamOrient(rx,ry); }//reverses the rx,ry,pi/2 orientation of the camera eye - paints on screen and is unaffected by camera movement
	/**
	 * return display string for camera location
	 * @return
	 */
	public final String getCamDisp() {return " camera rx :  " + rx + " ry : " + ry + " dz : " + dz ; }
	
	/**
	 * recenter view on original focus target
	 */
	protected final void resetViewFocus() {focusTar.set(winInitVals.initSceneFocusVal);}
	//////////////////////
	//end camera stuff
	
	/**
	 * returns current passed idx's dimension from either rectDim or rectDimClosed
	 * @param idx
	 * @return
	 */
	public final float getRectDim(int idx){return ( dispFlags.getShowWin() ? winInitVals.rectDim[idx] : winInitVals.rectDimClosed[idx]);	}

	/**
	 * Gets rectDim array for this window
	 * @return
	 */
	public final float[] getRectDims() { return winInitVals.rectDim;}
	/**
	 * Gets rectDimClosed array for this window
	 * @return
	 */
	public final float[] getRectDimClosed() {return winInitVals.rectDimClosed;}
	
	private final void _setClosedBox(){
		float clkBxDim = AppMgr.getClkBoxDim();
		if( dispFlags.getShowWin()){	
			closeBox[0] = winInitVals.rectDim[0]+winInitVals.rectDim[2]-clkBxDim;
			closeBox[1] = winInitVals.rectDim[1];	
		} else {
			closeBox[0] = winInitVals.rectDimClosed[0]+winInitVals.rectDimClosed[2]-clkBxDim;
			closeBox[1] = winInitVals.rectDimClosed[1];	
		}
		closeBox[2] = clkBxDim;	
		closeBox[3] = clkBxDim;
	}	

	/**
	 * Whether or not to draw the mouse reticle/rgb(xyz) projection/edge to eye
	 * @return
	 */
	public final boolean chkDrawMseRet(){		return dispFlags.getDrawMseEdge();	}
	
	//////////////////////
	//draw functions

	/**
	 * Initial draw stuff for each frame draw
	 * @param camVals
	 */
	public final void drawSetupWin(float[] camVals) {
		setCamera(camVals);
		//move to focus target
		ri.translate(focusTar.x,focusTar.y,focusTar.z);
	}

	/**
	 * Draw this window's gui objects in sidebar menu
	 * @param animTimeMod
	 */
	public final void drawWindowGuiObjs(boolean isDebug, float animTimeMod) {
		//draw UI Objs
		uiMgr.drawGUIObjs(isDebug, animTimeMod);
		//draw any custom menu objects for sidebar menu after buttons
		ri.pushMatState();
			//draw any custom menu stuff here
			drawCustMenuObjs(animTimeMod);
		ri.popMatState();
		//also launch custom function here if any are specified
		checkCustMenuUIObjs();		
	}//drawWindowGuiObjs	

	/**
	 * Draw a series of strings in a row
	 * @param txt
	 * @param loc
	 * @param clrAra
	 */
	protected final void dispBttnAtLoc(String txt, float[] loc, int[] clrAra){
		ri.setFill(clrAra, clrAra[3]);
		ri.setColorValStroke(IRenderInterface.gui_Black,255);
		ri.drawRect(loc);		
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		//ri.translate(-xOff*.5f,-yOff*.5f);
		ri.showText(""+txt,loc[0] + (txt.length() * .3f),loc[1]+loc[3]*.75f);
		//ri.translate(width, 0);
	}
	
	/**
	 * draw any custom menu objects for sidebar menu
	 */
	protected abstract void drawCustMenuObjs(float animTimeMod);
	
	/**
	 * draw box to hide window
	 */
	private final void drawMouseBox(){
		int boxFillClrIDX;
		String dispTxt;
		if(dispFlags.getShowWin()){
			boxFillClrIDX = IRenderInterface.gui_LightGreen;
			dispTxt = "Close";
		} else {
			boxFillClrIDX = IRenderInterface.gui_DarkRed;
			dispTxt = "Open";
		}
	    ri.setColorValFill(boxFillClrIDX,255);
		ri.drawRect(closeBox);
		winInitVals.setWinFillWithStroke(ri);	
		ri.showText(dispTxt, closeBox[0]-35, closeBox[1]+10);			
	}

	/**
	 * Draw the window minimized. Will show a bit of the window description along with the close/open box if available.
	 */
	private final void drawSmall(){
		ri.pushMatState();
		ri.setBeginNoDepthTest();
		ri.disableLights();		
		winInitVals.setWinFillAndStroke(ri);
		//main window drawing
		winInitVals.drawRectDimClosed(ri);
		winInitVals.setWinFillWithStroke(ri);
		//close box drawing
		if(dispFlags.getIsCloseable()){drawMouseBox();}
		if(winInitVals.winDescr.trim() != ""){	
			ri.showText(winInitVals.winDescr.split(" ")[0], winInitVals.rectDim[0]+AppMgr.getXOffsetHalf(), winInitVals.rectDim[1]+AppMgr.getTextHeightOffset()); 
		}
		ri.setEndNoDepthTest();
		ri.popMatState();		
	}

	/**
	 * called by drawUI in IRenderInterface
	 * @param modAmtMillis
	 */
	public final void drawHeader(String[] res, boolean shouldDrawOnScreenText, boolean isDebug, float modAmtMillis){
		if(!dispFlags.getShowWin()){return;}
		ri.pushMatState();		
		ri.setBeginNoDepthTest();
		ri.disableLights();	
		winInitVals.setWinStroke(ri);
		winInitVals.setWinFillWithStroke(ri);
		if(dispFlags.getIsCloseable()){drawMouseBox();}
		// Move to beginning of screen display
		ri.translate(winInitVals.rectDim[0], winInitVals.rectDim[1]);
		if(winInitVals.winDescr.trim() != ""){	
			ri.showText(winInitVals.winDescr, AppMgr.getXOffsetHalf(), winInitVals.rectDim[1]+AppMgr.getTextHeightOffset()); 
		}	
		if(null!=trajMgr){	trajMgr.drawNotifications(ri, AppMgr.getXOffsetHalf(), getTextHeightOffset() *.5f);	}				//if this window accepts a drawn trajectory, then allow it to be displayed
		//TODO if scroll bars are ever going to actually be supported, need to separate them from drawn trajectories
		if(dispFlags.getHasScrollBars() && (null!=trajMgr)){scbrs[trajMgr.curDrnTrajScrIDX].drawMe();}
		float yOffset = AppMgr.getTextHeightOffset();
		//draw stuff on screen, including rightSideMenu stuff, if this window supports it
		ri.translate(0.0f,yOffset);			
		//draw onscreen stuff for main window
		drawOnScreenStuffPriv(modAmtMillis);
		//draw right side info display if relevant
		if(dispFlags.getHasRtSideMenu()) {
			ri.pushMatState();
				ri.translate(0, -yOffset);
				drawRightSideMenu(modAmtMillis);
			ri.popMatState();	
		}
		if (shouldDrawOnScreenText) {		
			ri.pushMatState();
				ri.translate(AppMgr.getXOffsetHalf(),0.0f);
				drawOnScreenDebugText(res, isDebug);
			ri.popMatState();
		}
		ri.enableLights();	
		ri.setEndNoDepthTest();		
		ri.popMatState();
		//cleanup after drawing
		postDraw();
	}//drawHeader
	protected abstract void drawOnScreenStuffPriv(float modAmtMillis);
	
	/**
	 * This is called after all UI and other draw functionality has occurred.
	 */
	public final void postDraw() {
		//last thing per draw - clear btns that have been set to clear after 1 frame of display
		 uiMgr.postDraw(dispFlags.getClearPrivBtns());
		 dispFlags.setClearPrivBtns(false);
	}
	
	/**
	 * Separating bar for menu
	 * @param uiClkRect
	 */
	protected void drawSepBar(double uiClkRect) {
		ri.pushMatState();
			ri.translate(0,uiClkRect + (.5f*AppMgr.getClkBoxDim()),0);
			ri.setFill(0,0,0,255);
			ri.setStrokeWt(1.0f);
			ri.setStroke(0,0,0,255);
			ri.drawLine(0,0,0,AppMgr.getMenuWidth(),0,0);
		ri.popMatState();				
	}//
	
	/**
	 * Draw UI data debug info
	 * @param res UI debug data from dispMenu window
	 * @param whether or not the global debug is enabled
	 */
	private final void drawOnScreenDebugText(String[] res, boolean isDebug) {
		ri.pushMatState();			
			reInitInfoStr();
			if(isDebug) {
				addInfoStr(0,AppMgr.getMseEyeInfoString(getCamDisp()));
			} else {
				res = msgObj.getConsoleStringsAsArray();
			}
			int numToPrint = MyMathUtils.min(res.length,80);
			for(int s=0;s<numToPrint;++s) {	addInfoStr(res[s]);}				//add info to string to be displayed for debug
			drawInfoStr(1.0f, winInitVals.strkClr); 	
		ri.popMatState();		
	}//drawOnScreenText
	
	/**
	 * Draw Right side menu text
	 * @param modAmtMillis milliseconds since last frame started
	 */
	private void drawRightSideMenu(float modAmtMillis) {
		ri.setFill(winInitVals.rtSideFillClr, winInitVals.rtSideFillClr[3]);//transparent black
		if(dispFlags.getShowRtSideMenu()) {
			ri.drawRect(UIRtSideRectBox);
			//move to manage internal text display in owning window
			ri.translate(UIRtSideRectBox[0]+5,UIRtSideRectBox[1]+AppMgr.getRtSideTxtHeightOffset(),0);
			ri.setFill(255,255,255,255);	
			 //instancing class implements this function
			drawRightSideInfoBarPriv(modAmtMillis); 
		} else {
			//shows narrow rectangular reminder that window is there								 
			ri.drawRect(closedUIRtSideRecBox);
		}		
	}//drawRightSideMenu
	/**
	 * Draw implementation-specific text in right side menu. Use the following
	 * 			"float[] rtSideYOffVals = AppMgr.getRtSideYOffVals();"
	 * to access appropriate right-side menu specific values
	 * @param modAmtMillis
	 */
	protected abstract void drawRightSideInfoBarPriv(float modAmtMillis);
	
	/**
	 * Draw 3d windows that are currently displayed
	 * @param modAmtMillis milliseconds since last frame started
	 */
	public final void draw3D(float modAmtMillis){
		if(!dispFlags.getShowWin()){return;}
		float animTimeMod = modAmtMillis/1000.0f;
		ri.pushMatState();
		// Set current fill and stroke colors
		winInitVals.setWinFillAndStroke(ri);
		//draw instancing win-specific stuff
		drawMe(animTimeMod);			//call instance class's draw
		//draw traj stuff if exists and appropriate - if this window 
		//accepts a drawn trajectory, then allow it to be displayed
		if(null!=trajMgr){		trajMgr.drawTraj_3d(ri, animTimeMod, myPointf._add(sceneOriginVal,focusTar));}				
		ri.popMatState();		
	}//draw3D
	/**
	 * Draw window/application-specific functionality
	 * @param animTimeMod # of milliseconds since last frame dividied by 1000
	 */
	protected abstract void drawMe(float animTimeMod);	

	/**
	 *  Convenience for 2D windows to move origin to view center
	 */
	protected final void moveTo2DRectCenter() {
		ri.translate(winInitVals.rectDim[0] + (winInitVals.rectDim[2]*.5f), winInitVals.rectDim[1] + (winInitVals.rectDim[3]*.5f));
	}	
	
	/**
	 * Draw 2d windows that are currently displayed
	 * @param modAmtMillis milliseconds since last frame started
	 */	
	public final void draw2D(float modAmtMillis){
		if(!dispFlags.getShowWin()){drawSmall();return;}
		ri.pushMatState();
		//_dispDbgMsg("draw2D","Hitting hint code draw2D");
		ri.setBeginNoDepthTest();
		ri.disableLights();
		// Set current fill and stroke colors
		winInitVals.setWinFillAndStroke(ri);
		//main window drawing
		winInitVals.drawRectDim(ri);
		//draw instancing win-specific stuff
		drawMe(modAmtMillis/1000.0f);			//call instance class's draw
		//draw traj stuff if exists and appropriate
		if(null!=trajMgr){		trajMgr.drawTraj_2d(ri);}				//if this window accepts a drawn trajectory, then allow it to be displayed
		ri.enableLights();
		ri.setEndNoDepthTest();
		ri.popMatState();
	}

	
	public void drawTraj3D(float animTimeMod, myPointf trans){
		_dispWarnMsg("drawTraj3D","I should be overridden in 3d instancing class");
//			ri.pushMatState();	
//			if(null != tmpDrawnTraj){tmpDrawnTraj.drawMe(animTimeMod);}
//			TreeMap<String,ArrayList<myDrawnNoteTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
//			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
//				for(int i =0; i<tmpTreeMap.size(); ++i){
//					ArrayList<myDrawnNoteTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
//					if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){tmpAra.get(j).drawMe(animTimeMod);}}
//				}	
//			}
//			ri.popMatState();		
	}//drawTraj3D	
	
	/**
	 * Decay this window's console strings
	 */
	public final void updateConsoleStrs(){
		++drawCount;
		if(drawCount % cnslStrDecay == 0){drawCount = 0;	msgObj.updateConsoleStrs();}			
	}//updateConsoleStrs

	/**
	 * reset debug info array 
	 */
	public final void reInitInfoStr(){		DebugInfoAra = new ArrayList<String>();		DebugInfoAra.add("");	}	

	
	private final int addInfoStr(String str){return addInfoStr(DebugInfoAra.size(), str);}
	private final int addInfoStr(int idx, String str){	
		int lstIdx = DebugInfoAra.size();
		if(idx >= lstIdx){		for(int i = lstIdx; i <= idx; ++i){	DebugInfoAra.add(i,"");	}}
		setInfoStr(idx,str);	return idx;
	}
	private final void setInfoStr(int idx, String str){DebugInfoAra.set(idx,str);	}
	private final void drawInfoStr(float sc, int[] fillClr){//draw text on main part of screen
		float yOff = getTextHeightOffset();
		ri.pushMatState();		
			ri.setFill(fillClr,fillClr[3]);
			ri.scale(sc,sc);
			for(int i = 0; i < DebugInfoAra.size(); ++i){		
				ri.showText((AppMgr.isDebugMode()?(i<10?"0":"")+i+":     " : "") +"     "+DebugInfoAra.get(i)+"\n\n",0,(yOff+(yOff*i)));	}
		ri.popMatState();
	}		
	
	//////////////////
	// Simulation
	/**
	 * Execute a simulation
	 * @param modAmtMillis
	 */
	public final void simulate(float modAmtMillis){
		boolean simDone = simMe(modAmtMillis);
		if(simDone) {endSim();}
	}//
	/**
	 * Implemenation-specific functionality for running a simulation
	 * @param modAmtSec
	 * @return
	 */
	protected abstract boolean simMe(float modAmtSec);
	
	/**
	 * if ending simulation, call this function
	 */
	private void endSim() {	
		AppMgr.setSimIsRunning(false);
		stopMe();
	}//endSim	
	/**
	 * Implementation-specific functionality for ending simulation
	 */
	protected abstract void stopMe();
		
	/**
	 * clear button next frame - to act like momentary switch. will also clear UI object
	 * @param idx
	 */
	protected final void clearSwitchNextFrame(int idx) {uiMgr.clearSwitchNextFrame(idx);}
		
	/**
	 * add a button to clear after next draw
	 * @param idx index of button to clear
	 */
	protected final void addPrivSwitchToClear(int idx) {uiMgr.addPrivSwitchToClear(idx);}
	
	/**
	 * Access private flag values
	 * @param idx
	 * @return
	 */
	public final boolean getPrivFlag(int idx) {				return uiMgr.getPrivFlag(idx);}
	
	/**
	 * Set private flag values
	 * @param idx
	 * @param val
	 */
	public final void setPrivFlag(int idx, boolean val) {		uiMgr.setPrivFlag(idx, val);}
	
	/**
	 * Whether this window manages a simulator or some other runnable construct
	 * @return
	 */
	public final boolean getIsRunnable() {return dispFlags.getIsRunnable();}
	
	/**
	 * Set this window's flag to show or hide
	 * @param val
	 */
	public final void setShowWin(boolean val) {dispFlags.setShowWin(val);}
	
	protected final void toggleWindowState(){
		//_dispDbgMsg("toggleWindowState","Attempting to close window : " + this.name);
		dispFlags.toggleShowWin();
		if(dispFlagWinIDX != -1) {AppMgr.setWinVisFlag(dispFlagWinIDX, dispFlags.getShowWin());}	//value has been changed above by close box	
	}
	
	/**
	 * Check for click in closeable box
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	protected final boolean checkClsBox(int mouseX, int mouseY){
		//if(MyMathUtils.ptInRange(mouseX, mouseY, closeBox[0], closeBox[1], closeBox[0]+closeBox[2], closeBox[1]+closeBox[3])){toggleWindowState(); res = true;}				
		if(msePtInRect(mouseX, mouseY, closeBox)){toggleWindowState(); return true;}				
		return false;		
	}
	
	/**
	 * change view based on mouse click/drag behavior and whether we are moving or zooming use delX for zoom
	 * @param doZoom
	 * @param delX
	 * @param delY
	 */
	protected final void handleViewChange(boolean doZoom, float delX, float delY ) {
		if(doZoom) {	dz-=delX;	} 
		else {			rx-=delX; ry+=delY;} 		
	}//handleViewChange()
	
	/**
	 * modify the viewing target by finding -TODO doesn't behave appropriately if camera is rotated around origin
	 * @param delY
	 * @param delX
	 */
	public final void handleViewTargetChange(float delY, float delX) {
		//find screen up unit vector, screen right unit vector in world space, dot focus tar with that, move in that direction
		setCamOrient();
		if(delY != 0.0f) {
			myVectorf scrUp = AppMgr.getUScrUpInWorldf();//, upVec = myVectorf._cross(scrRt, scrUp);
			myVectorf up = new myVectorf(scrUp.x* delY,scrUp.y* delY,scrUp.z* delY);
			focusTar._add(up);
		}		
		if(delX!= 0.0f) {
			myVectorf scrRt = AppMgr.getUScrRightInWorldf();
			myVectorf rt = new myVectorf(scrRt.x* delX,scrRt.y* delX,scrRt.z* delX);
			focusTar._add(rt);
		}
		unSetCamOrient();
	}//handleViewTargetChange	
	
	/**
	 * Whether or not this window displays in 3D
	 * @return
	 */
	public final boolean getIs3DWindow() {return dispFlags.getIs3DWin();}
	
	/**
	 * get appropriate representation of mouse location in 3d if 3d window
	 * @param pt
	 * @return
	 */
	protected final myPoint getMsePoint(myPoint pt){
		return dispFlags.getIs3DWin() ? getMsePtAs3DPt(pt) : pt;}		//get appropriate representation of mouse location in 3d if 3d window
	public final myPoint getMsePoint(int mouseX, int mouseY){return dispFlags.getIs3DWin() ? getMsePtAs3DPt(new myPoint(mouseX,mouseY,0)) : new myPoint(mouseX,mouseY,0);}
	/**
	 * return appropriate 3d representation of mouse location - in 2d this will just be mseLoc x, mse Loc y, 0
	 * @param mseLoc x and y are int values of mouse x and y location
	 * @return
	 */
	protected abstract myPoint getMsePtAs3DPt(myPoint mseLoc);

	/**
	 * Return the coordinates of the clickable region for this window's UI
	 * @return
	 */
	@Override
	public float[] getUIClkCoords() {return uiMgr.getUIClkCoords();}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Start Mouse and keyboard handling

	/**
	 * Check if point x,y is between r[0], r[1] and r[0]+r[2], r[1]+r[3]
	 * @param x
	 * @param y
	 * @param r rectangle - idx 0,1 is upper left corner, idx 2,3 is width, height
	 * @return
	 */
	public final boolean msePtInRect(int x, int y, float[] r){return ((x >= r[0])&&(x <= r[0]+r[2])&&(y >= r[1])&&(y <= r[1]+r[3]));}
	
	public final boolean msePtInUIClckCoords(int x, int y){return uiMgr.msePtInUIClckCoords(x, y);	}	
	
	public final boolean pointInRectDim(int x, int y){return winInitVals.pointInRectDim(x, y);	}
	public final boolean pointInRectDim(myPoint pt){return winInitVals.pointInRectDim(pt);}	
	public final boolean pointInRectDim(myPointf pt){return winInitVals.pointInRectDim(pt);}
	public final float getTextHeightOffset() {return AppMgr.getTextHeightOffset();}	
	
	/**
	 * Handle mouse interaction via a mouse click
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param mseBtn which button is pressed : 0 is left, 1 is right
	 * @return whether a UI object was clicked in
	 */
	@Override
	public final boolean handleMouseClick(int mouseX, int mouseY, int mseBtn){
		boolean mod = false;
		//check if trying to close or open the window via click, if possible
		if(dispFlags.getIsCloseable()){mod = checkClsBox(mouseX, mouseY);}		
		boolean showWin = dispFlags.getShowWin();
		if(!showWin){return mod;}
		boolean[] retVals = new boolean[] {false,false};
		msClickInUIObj = uiMgr.handleMouseClick(mouseX, mouseY, mseBtn, AppMgr.isClickModUIVal(), retVals);
		if (retVals[1]){dispFlags.setUIObjMod(true);}
		if (retVals[0]){return true;}
		
		//if nothing triggered yet, then specific instancing window implementation stuff
		if(!mod){
			//Get 3d point if appropriate
			myPoint mouseClickIn3D = AppMgr.getMseLoc(sceneOriginVal);
			mod = hndlMouseClick_Indiv(mouseX, mouseY, mouseClickIn3D, mseBtn);
		}			
		//if still nothing then check for trajectory handling
		if((!mod) && (winInitVals.pointInRectDim(mouseX, mouseY)) && (trajMgr != null)){ 
			mod = trajMgr.handleMouseClick_Traj(AppMgr.altIsPressed(), getMsePoint(mouseX, mouseY));
		}			//click + alt for traj drawing : only allow drawing trajectory if it can be drawn and no other interaction has occurred
		return mod;
	}//handleMouseClick
	/**
	 * Implementing class' necessary functions for mouse click
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param mseClckInWorld
	 * @param mseBtn
	 * @return whether a custom UI object was clicked in
	 */
	protected abstract boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn);

	/**
	 * Handle mouse interaction via the mouse moving over a UI object
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @return whether a UI object has the mouse pointer moved over it
	 */
	@Override
	public final boolean handleMouseMove(int mouseX, int mouseY){
		if(!dispFlags.getShowWin()){return false;}
		
		boolean uiObjMseOver = uiMgr.handleMouseMove(mouseX, mouseY);
		if (uiObjMseOver){return true;}
		myPoint mouseClickIn3D = AppMgr.getMseLoc(sceneOriginVal);
		if(hndlMouseMove_Indiv(mouseX, mouseY, mouseClickIn3D)){return true;}
		return false;
	}//handleMouseMove
	/**
	 * Implementing class' necessary function for mouse movement
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param mseClckInWorld
	 * @return whether a custom UI object has the mouse pointer moved over it
	 */
	protected abstract boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld);	
			
	/**
	 * Handle mouse interaction via the mouse wheel
	 * @param ticks
	 * @param mult amount to modify view based on sensitivity and whether shift is pressed or not
	 * @return whether a UI object has been modified via the mouse wheel
	 */
	@Override
	public final boolean handleMouseWheel(int ticks, float mult) {
		if (msClickInUIObj) {
			//modify object that was clicked in by mouse motion
			boolean retVals[] = uiMgr.handleMouseWheel(ticks, mult);
			if (retVals[1]){dispFlags.setUIObjMod(true);}
			if (retVals[0]){return true;}
			
			
		} else if (dispFlags.getCanChgView()) {handleViewChange(true,(mult * ticks),0);}
		return true;
	}//handleMouseWheel
	
	/**
     * Handle mouse interaction via the mouse wheel for implementing class
     * @param ticks
     * @param mult amount to modify view based on sensitivity and whether shift is pressed or not
     * @return whether a UI object has been modified via the mouse wheel
     */
	protected abstract boolean handleMouseWheel_Indiv(int ticks, float mult);

	/**
	 * Handle mouse interaction via the clicked mouse drag
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param pmouseX previous mouse x on screen
	 * @param pmouseY previous mouse y on screen
	 * @param mseDragInWorld vector of mouse drag in the world, for interacting with trajectories
	 * @param mseBtn what mouse btn is pressed
	 * @return whether a UI object has been modified via a drag action
	 */
	@Override
	public final boolean handleMouseDrag(int mouseX, int mouseY,int pmouseX, int pmouseY, myVector mseDragInWorld, int mseBtn){
		boolean mod = false;
		if(!dispFlags.getShowWin()){return mod;}
		boolean shiftPressed = AppMgr.shiftIsPressed();
		int delX = (mouseX-pmouseX), delY = (mouseY-pmouseY);
		//check if modding view
		if (shiftPressed && dispFlags.getCanChgView() && (!msClickInUIObj)) {//modifying view angle/zoom
			AppMgr.setModView(true);	
			if(mseBtn == 0){			handleViewChange(false,AppMgr.msSclY*delY, AppMgr.msSclX*delX);}	
			else if (mseBtn == 1) {		handleViewChange(true,delY, 0);}	
			return true;
		} else if ((AppMgr.cntlIsPressed()) && dispFlags.getCanChgView() && (!msClickInUIObj)) {//modifying view focus
			AppMgr.setModView(true);
			handleViewTargetChange(delY, delX);
			return true;
		} else {//modify UI elements		
			//any generic dragging stuff - need flag to determine if trajectory is being entered		
			//modify object that was clicked in by mouse motion
			
			boolean retVals[] = uiMgr.handleMouseDrag(delX, delY, mseBtn, shiftPressed);
			if (retVals[1]){dispFlags.setUIObjMod(true);}
			if (retVals[0]){return true;}

			if(null!=trajMgr) {	mod = trajMgr.handleMouseDrag_Traj(mouseX, mouseY, pmouseX, pmouseY, mseDragInWorld, mseBtn);		}
			if(!mod) {
				if(!winInitVals.pointInRectDim(mouseX, mouseY)){return false;}	//if not drawing or editing a trajectory, force all dragging to be within window rectangle	
				//_dispDbgMsg("handleMouseDrag","before handle indiv drag traj for window : " + this.name);
				myPoint mouseClickIn3D = AppMgr.getMseLoc(sceneOriginVal);
				mod = hndlMouseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY,mouseClickIn3D,mseDragInWorld,mseBtn);		//handle specific, non-trajectory functionality for implementation of window
			}
		}
		return mod;
	}//handleMouseDrag
	
	/**
	 * Sidebar menu calling main window's implementation code for drag
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param pmouseX previous mouse x on screen
	 * @param pmouseY previous mouse y on screen
	 * @param mouseClickIn3D
	 * @param mseDragInWorld vector of mouse drag in the world, for interacting with trajectories
	 * @param mseBtn what mouse btn is pressed
	 * @return
	 */
	public final boolean sideBarMenu_CallWinMseDrag_Indiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
		return hndlMouseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
	}
	/**
	 * Implementing class' necessary function for mouse drag
	 * @param mouseX current mouse x on screen
	 * @param mouseY current mouse y on screen
	 * @param pmouseX previous mouse x on screen
	 * @param pmouseY previous mouse y on screen
	 * @param mouseClickIn3D
	 * @param mseDragInWorld vector of mouse drag in the world, for interacting with trajectories
	 * @param mseBtn what mouse btn is pressed
	 * @return
	 */
	protected abstract boolean hndlMouseDrag_Indiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn);
	
	/**
	 * Handle selection of mouse-over-text option buttons in menu, specifying desired mouse over text to display in sim window
	 * @param btn
	 * @param val
	 */
	public abstract void handleSideMenuMseOvrDispSel(int btn,boolean val);		

	/**
	 * Handle mouse interactive when the mouse button is released - in general consider this the end of a mouse-driven interaction
	 */
	@Override
	public final void handleMouseRelease(){
		if(!dispFlags.getShowWin()){return;}
		boolean objModified = dispFlags.getUIObjMod();
		dispFlags.setUIObjMod(false);

		//if buttons have been put in clear queue (set to clear), set flag to clear them next draw
		if(uiMgr.handleMouseRelease(objModified)) {			dispFlags.setClearPrivBtns(true);		}
		msClickInUIObj = false;
		
		if(null!=trajMgr) {trajMgr.handleMouseRelease_Traj(getMsePoint(ri.getMouse_Raw()));}
		//specific instancing window implementation stuff for release
		hndlMouseRel_Indiv();
		if(null!=trajMgr) {trajMgr.clearTmpDrawnTraj();}
	}//handleMouseRelease	
	/**
	 * Implementing class' necessary function for mouse release
	 */
	protected abstract void hndlMouseRel_Indiv();
			
	/**
	 * Handle releasing the alt key
	 */
	public final void endAltKey(){
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endAltKey(getMsePoint(ri.getMouse_Raw()));}
		endAltKey_Indiv();
	}	
	protected abstract void endAltKey_Indiv();
	/**
	 * Handle releasing the cntl key
	 */
	public final void endCntlKey(){
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endCntlKey(getMsePoint(ri.getMouse_Raw()));}
		endCntlKey_Indiv();
	}
	protected abstract void endCntlKey_Indiv();
	/**
	 * Handle releasing the shift key
	 */
	public final void endShiftKey(){
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endShiftKey(getMsePoint(ri.getMouse_Raw()));}
		endShiftKey_Indiv();
	}
	protected abstract void endShiftKey_Indiv();
	/**
	 * Set the value of the key and keycode pressed, passed by GUI_AppMgr
	 * @param _key
	 * @param _keyCode
	 */
	public final void setValueKeyPress(char _key, int _keyCode) {	
		if(!dispFlags.getShowWin()){return;}
		keyPressed = _key; 
		keyCodePressed = _keyCode;
	}
	// INDIV Version for setValueKey?
	
	/**
	 * Clear the values of the key and keycode that was pressed. Called by GUI_AppMgr
	 */
	public final void endValueKeyPress() {
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endValueKeyPress();}
		keyPressed = ' ';
		keyCodePressed = 0;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// End Mouse and keyboard handling
	
	/**
	 * finds closest point to p in sPts - put dist in d, returns index
	 * @param p
	 * @param d
	 * @param _pts
	 * @return
	 */
	public final int findClosestPt(myPoint p, double[] d, myPoint[] _pts){
		int res = -1;
		double mindist = 99999999, _d;
		for(int i=0; i<_pts.length; ++i){_d = myPoint._dist(p,_pts[i]);if(_d < mindist){mindist = _d; d[0]=_d;res = i;}}	
		return res;
	}
	/**
	 * 
	 */
	public final void rebuildAllDrawnTrajs(){
		if(null!=trajMgr) {trajMgr.rebuildAllDrawnTrajs();}
	}//rebuildAllDrawnTrajs
	
	/**
	 * debug data to display on screen get string array for onscreen display of debug info for each object
	 * @return
	 */
	public final String[] getDebugData(){
		 String[] res = uiMgr.getDebugData();
		 return res;
	}
	
	/**
	 * Get the number of usable threads the system supports.
	 * @return
	 */
	public final int getNumThreadsAvailable() {return GUI_AppManager.getNumThreadsAvailable();}
	
	
	//setup the launch of UI-driven custom functions or debugging capabilities, which will execute next frame
	//get key used to access arrays in traj array
	protected final String getTrajAraKeyStr(int i){if(null==trajMgr) {return "";} return trajMgr.getTrajAraKeyStr(i);}
	protected final int getTrajAraIDXVal(String str){if(null==trajMgr) {return -1;} return trajMgr.getTrajAraIDXVal(str);  }
	
	public final void clearAllTrajectories(){	if(null!=trajMgr) {		trajMgr.clearAllTrajectories();}}//clearAllTrajectories
	
	/**
	 * Return the height of the clickable close/open box in upper right hand corner of closable windows
	 * @return
	 */
	public final float getCloseBoxHeight() {return closeBox[3];}
		
	/**
	 * Handle this window's debug state/function based on what button was selected from side-bar debug menu
	 * @param btn which button
	 * @param val whether on or off
	 */
	public final void setThisWinMenuBtnDbgState(int btn,int val) {
		if(val==0) {//turning on
			_dispDbgMsg("setThisWinMenuBtnDbgState","Click Debug functionality on in " + winInitVals.winName + " : btn : " + btn);
			handleSideMenuDebugSelEnable(btn);
			_dispDbgMsg("setThisWinMenuBtnDbgState", "End Debug functionality on selection.");
		} else {
			_dispDbgMsg("setThisWinMenuBtnDbgState","Click Debug functionality off in " + winInitVals.winName + " : btn : " + btn);
			handleSideMenuDebugSelDisable(btn);			
			_dispDbgMsg("setThisWinMenuBtnDbgState", "End Debug functionality off selection.");
		}
	}//setThisWinMenuBtnDbgState
	/**
	 * handle desired debug functionality enable based on buttons selected from side bar menu
	 * @param btn
	 * @param val
	 */
	protected abstract void handleSideMenuDebugSelEnable(int btn);
	/**
	 * handle desired debug functionality disable based on buttons selected from side bar menu
	 * @param btn
	 * @param val
	 */
	protected abstract void handleSideMenuDebugSelDisable(int btn);	
	
	/**
	 * check if either custom function or debugging has been launched and process if so, skip otherwise.latched by a frame so that button can be turned on
	 */
	private final void checkCustMenuUIObjs() {
		//was set last frame and processed, so latch to launch request. 
		//this will enable buttons to be displayed even if their processing only takes a single cycle
		if (custClickSetThisFrame) { custClickSetThisFrame = false;custFuncDoLaunch=true;return;}	 
		//no function has been requested to launch
		if (!custFuncDoLaunch) {return;}
		//launch special menu button handling
		int row = curCstBtnRow-curCstFuncBtnOffset;
		int col = curCustBtn[curCstBtnRow];
		String label = AppMgr.getSidebarMenuButtonLabel(curCstBtnRow,col);
		_dispDbgMsg("launchMenuBtnHndlr", "Begin requested action : Click '" + label +"' (Row:"+(row+1)+"|Col:"+col+") in " + winInitVals.winName);
		launchMenuBtnHndlr(row, col, label);
		_dispDbgMsg("launchMenuBtnHndlr", "End requested action (multithreaded actions may still be working) : Click '" + label +"' (Row:"+(row+1)+"|Col:"+col+") in " + winInitVals.winName);
		custFuncDoLaunch=false;
	}//checkCustMenuUIObjs

	/**
	 * UI controlled auxiliary/debug functionality	
	 * @param _row
	 * @param _funcOffset
	 * @param btnNum
	 */
	public final void clickSideMenuBtn(int _row, int _funcOffset, int btnNum) {	curCstBtnRow = _row; curCstFuncBtnOffset = _funcOffset; curCustBtn[_row] = btnNum; custClickSetThisFrame = true;}
	
	/**
	 * type is row of buttons (1st idx in curCustBtn array) 2nd idx is btn
	 * @param funcRow idx for button row
	 * @param btn idx for button within row (column)
	 * @param label label for this button (for display purposes)
	 */
	protected abstract void launchMenuBtnHndlr(int funcRow, int btn, String label) ;
	
	/**
	 * Call from custFunc/custDbg functions being launched in threads.
	 * these are launched in threads to allow UI to respond to user input
	 */
	public final void resetButtonState() {resetButtonState(true);}
	/**
	 * call from custFunc/custDbg functions being launched in threads.
	 * these are launched in threads to allow UI to respond to user input
	 * @param isSlowProc
	 */
	public final void resetButtonState(boolean isSlowProc) {
		if (curCstBtnRow == -1) {return;}
		if (curCustBtn[curCstBtnRow] == -1) {return;}
		AppMgr.clearBtnState(curCstBtnRow,curCustBtn[curCstBtnRow], isSlowProc);
		curCustBtn[curCstBtnRow] = -1;
	}//resetButtonState	
	
	protected abstract void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc);		
	
	/**
	 * Modify the application-wide ui button labels based on context
	 */
	protected abstract void setCustMenuBtnLabels();
	
	public abstract void processTraj_Indiv(DrawnSimpleTraj drawnTraj);
	
	/**
	 * 
	 * @param file
	 */
	public final void loadFromFile(File file){
		if (file == null) {
			_dispWarnMsg("loadFromFile","Load was cancelled.");
		    return;
		} 
		String[] res = fileIO.loadFileIntoStringAra(file.getAbsolutePath(), "Variable File Load successful", "Variable File Load Failed.");
		int[] stIdx = {0};//start index for a particular window - make an array so it can be passed by ref and changed by windows
		hndlFileLoad(file, res,stIdx);
	}//loadFromFile
	
	/**
	 * file io used from selectOutput/selectInput - take loaded params and process
	 * @param file
	 * @param vals
	 * @param stIdx
	 */
	public abstract void hndlFileLoad(File file, String[] vals, int[] stIdx);
	
	/**
	 * manage loading pre-saved UI component values, if useful for this window's load/save (if so call from child window's implementation
	 * @param vals
	 * @param stIdx
	 */
	protected final void hndlFileLoad_GUI(String[] vals, int[] stIdx) {		uiMgr.hndlFileLoad_GUI(winInitVals.winName, vals, stIdx);	}//hndlFileLoad_GUI
	
	/**
	 * 
	 * @return
	 */
	public final String[] getSaveFileDirName() {
		String[] vals = getSaveFileDirNamesPriv();
		if((null==vals) || (vals.length != 2)) {return new String[0];}
		String[] res = new String[]{
			ssPathBase + vals[0] + File.separatorChar, vals[1]	
		};
		return res;
	}	
	/**
	 * return relevant name information for files and directories to be used to build screenshots/saved files	
	 * @return
	 */
	protected abstract String[] getSaveFileDirNamesPriv();	
	/**
	 * 
	 * @param file
	 */
	public final void saveToFile(File file){
		if (file == null) {
			_dispWarnMsg("saveToFile","Save was cancelled.");
		    return;
		} 
		ArrayList<String> res = new ArrayList<String>();

		res.addAll(hndlFileSave(file));	

		fileIO.saveStrings(file.getAbsolutePath(), res);  
	}//saveToFile	
	
	/**
	 * accumulate array of params to save
	 * @param file
	 * @return
	 */
	public abstract ArrayList<String> hndlFileSave(File file);	
	
	/**
	 * manage saving this window's UI component values. if needed call from child window's implementation
	 * @return
	 */
	protected final ArrayList<String> hndlFileSave_GUI(){		return uiMgr.hndlFileSave_GUI(winInitVals.winName);	}//
	
	protected void _dispMessage(String funcName, String message, MsgCodes useCode) {		msgObj.dispMessage(className, funcName, message, useCode);}
	
	/**
	 * Shorthand to display general information
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispInfoMsg(String funcName, String message) {		msgObj.dispInfoMessage(className, funcName, message);}
	
	/**
	 * Shorthand to display a debug message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispDbgMsg(String funcName, String message) {		msgObj.dispDebugMessage(className, funcName, message);}
	
	/**
	 * Shorthand to display a warning message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispWarnMsg(String funcName, String message) {		msgObj.dispWarningMessage(className, funcName, message);}
	
	/**
	 * Shorthand to display an error message
	 * @param funcName the calling method
	 * @param message the message to display
	 */
	protected void _dispErrMsg(String funcName, String message) {		msgObj.dispErrorMessage(className, funcName, message);}
	
	/**
	 * Retrieve MessageObject for logging and message display
	 * @return
	 */
	public final MessageObject getMsgObj() {return msgObj;}
	@Override
	public final String getName() {return winInitVals.winName;}
	
	@Override
	public final int getID() {return ID;}
	@Override
	public final String getClassName() {return className;}
	/**
	 * String representation of the pertinent values for this window
	 */
	public String toString(){		String res = winInitVals.toString();		return res;	}
	
	//add another screen to this window - need to handle specific trajectories - always remake traj structure
	public final void addSubScreenToWin(int newWinKey){						if(null!=trajMgr) {		trajMgr.modTrajStructs(newWinKey, "",false);			addSScrToWin_Indiv(newWinKey);}}
	protected abstract void addSScrToWin_Indiv(int newWinKey);
	public final void addTrajToSubScreen(int subScrKey, String newTrajKey){	if(null!=trajMgr) {		trajMgr.modTrajStructs(subScrKey, newTrajKey,false);	addTrajToScr_Indiv(subScrKey, newTrajKey);}}
	protected abstract void addTrajToScr_Indiv(int subScrKey, String newTrajKey);
	public final void delSubScreenToWin(int delWinKey){						if(null!=trajMgr) {		trajMgr.modTrajStructs(delWinKey, "",true);				delSScrToWin_Indiv(delWinKey);}}
	protected abstract void delSScrToWin_Indiv(int idx);
	public final void delTrajToSubScreen(int subScrKey, String newTrajKey){	if(null!=trajMgr) {		trajMgr.modTrajStructs(subScrKey, newTrajKey,true);		delTrajToScr_Indiv(subScrKey,newTrajKey);}}
	protected abstract void delTrajToScr_Indiv(int subScrKey, String newTrajKey);
		
}//Base_DispWindow
