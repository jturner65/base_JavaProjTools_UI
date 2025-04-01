package base_UI_Objects.windowUI.base;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.*;
import base_Math_Objects.vectorObjs.floats.*;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.UIObjectManager;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.drawnTrajectories.TrajectoryManager;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.ScrollBars;
import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_DispValue;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Float;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Int;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_List;
import base_UI_Objects.windowUI.uiObjs.renderer.MultiLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.SingleLineGUIObjRenderer;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;
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
	 * enable drawing debug info onto app canvas	
	 */
	private ArrayList<String> DebugInfoAra;	
	//count of draw cycles for consoleString decay
	private int drawCount = 0;
		
	//how long a message should last before it is popped from the console strings deque (how many frames)
	private final int cnslStrDecay = 10;
	
	/**
	 * file IO object to manage IO
	 */
	protected FileIOManager fileIO;
	/**
	 * class name of instancing class
	 */
	protected final String className;
	
	public final int ID;	
	private static int winCnt = 0;

	/**
	 * Window initialization values - open and closed dims, colors
	 */
	protected final GUI_AppWinVals winInitVals;
	
	public float[] closeBox;	
	//current visible screen width and height
	public float[] curVisScrDims;
	
	
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
	 * UI Application-specific flags and UI components (buttons)
	 */	
	protected WinAppPrivStateFlags privFlags;
	
	/**
	 * Button labels for true value buttons
	 */
	private String[] truePrivFlagLabels; //needs to be in order of flags
	/**
	 * Button labels for false value buttons
	 */
	private String[] falsePrivFlagLabels;//needs to be in order of flags	
	/**
	 * Colors for boolean buttons set to True based on child-class window specific values
	 */
	private int[][] privFlagTrueColors;
	/**
	 * Colors for boolean buttons set to False based on child-class window specific values
	 */
	private int[][] privFlagFalseColors;
	
	/**
	 * only modifiable idx's will be shown as buttons - this needs to be in order of flag names
	 */
	private int[] privModFlgIdxs;
	
	/**
	 * Click dimensions for each button
	 */
	private float[][] privFlagBtns;									
	/**
	 * array of priv buttons to be cleared next frame - 
	 * should always be empty except when buttons need to be cleared
	 */
	private ArrayList<Integer> privBtnsToClear;
	
	//UI objects in this window
	//GUI Objects
	private Base_NumericGUIObj[] guiObjs_Numeric;	
	
	/**
	 * Base_GUIObj that was clicked on for modification
	 */
	protected int msClkObj;
	
	/**
	 * object mouse moved over
	 */
	protected int msOvrObj;		
	
	/**
	 * mouse button clicked - consumed for individual click mod
	 */
	protected int msBtnClcked;														//mouse button clicked

	/**
	 * subregion of window where UI objects may be found
	 * Idx 0,1 : Upper left corner x,y
	 * Idx 2,3 : Lower right corner x,y
	 */
	protected float[] uiClkCoords;												//
											
	/**
	 * array lists of idxs for float-based UI objects.
	 */
	private ArrayList<Integer> guiFloatValIDXs;
	
	/**
	 * array lists of idxs for integer/list-based objects
	 */
	private ArrayList<Integer> guiIntValIDXs;
	/**
	 * array lists of idxs for label/read-only objects
	 */	
	private ArrayList<Integer> guiLabelValIDXs;
	
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
	 * offset to bottom of custom window menu 
	 */
	protected float custMenuOffset;
	
	/**
	 * box holding x,y,w,h values of black rectangle to form around menu for display variables on right side of screen, if present
	 */
	private float[] UIRtSideRectBox;
	/**
	 * closed window box
	 */
	private float[] closedUIRtSideRecBox;	
	/**
	 * structure to facilitate communicating UI changes with functional code
	 */
	private UIDataUpdater uiUpdateData;
		
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
	private myVector focusTar;							
	/**
	 * set this value to be different display center translations -to be used to calculate mouse offset in world for pick
	 * and also as origin for 
	 */
	private myPoint sceneOriginVal;							
	
	//to control how much is shown in the window - if stuff extends off the screen and for 2d window
	protected ScrollBars[] scbrs;
	
	/**
	 * Non random true button color
	 */
	private final int[] trueBtnClr = new int[]{220,255,220,255};
	/**
	 * Non-random false button color
	 */
	private final int[] falseBtnClr = new int[]{255,215,215,255};
	
	/**
	 * False button color to use if button labels are the same and using random colors
	 */
	private static final int[] baseBtnFalseClr = new int[]{180,180,180, 255};
	
	/**
	 * directory with proper timestamp from when window was made
	 */
	protected final String ssFolderDir;
	/**
	 * path to save screenshots for this dispwindow
	 */
	protected final String ssPathBase;
	
	//these ints hold the index of which custom functions or debug functions should be launched.  
	//these are set when the sidebar menu is clicked and these processes are requested, and they are set to -1 when these processes are launched.  this is so the buttons can be turned on before the process starts
	//this is sub-optimal solution - needs an index per sidebar button on each row; using more than necessary, otherwise will crash if btn idx >= curCustBtn.length
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
		uiMgr = new UIObjectManager(ri, this, AppMgr, msgObj);
		
		className = this.getClass().getSimpleName();
		ID = winCnt++;
		dispFlagWinIDX = _winIdx;
		winInitVals = _winInitVals;	
		fileIO = new FileIOManager(msgObj, winInitVals.winName);
		//base screenshot path based on launch time
		ssFolderDir = winInitVals.winName+"_"+getNowDateTimeString();
		ssPathBase = AppMgr.getApplicationPath() +File.separatorChar + ssFolderDir + File.separatorChar;

		closeBox = new float[4]; uiClkCoords = new float[4];
		float boxWidth = 1.1f*winInitVals.rectDim[0];
		UIRtSideRectBox = new float[] {winInitVals.rectDim[2]-boxWidth,0,boxWidth, winInitVals.rectDim[3]};		
		closedUIRtSideRecBox = new float[] {winInitVals.rectDim[2]-20,0,20,winInitVals.rectDim[3]};
		curVisScrDims = new float[] {closedUIRtSideRecBox[0],winInitVals.rectDim[3]};
		
		msClkObj = -1;
		msOvrObj = -1;
		reInitInfoStr();
		sceneOriginVal = new myPoint(winInitVals.sceneOriginVal);
		focusTar = new myVector(winInitVals.initSceneFocusVal);
		//initialize the camera
		setInitCamView();
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
		privBtnsToClear = new ArrayList<Integer>();
		
		//set up ui click region to be in sidebar menu below menu's entries - do not do here for sidebar menu itself
		if(!_isMenu){
			//Initialize dispFlags settings based on AppMgr
			//Does this window include a runnable sim (launched by main menu flag)
			dispFlags.setIsRunnable(AppMgr.getBaseFlagIsShown_runSim());
			//Is this window capable of showing right side menu
			dispFlags.setHasRtSideMenu(AppMgr.getBaseFlagIsShown_showRtSideMenu());
			//initialize/override any state/display flags
			initDispFlags();
			//build uiClkCoords for this object
			initUIBox();						
		} else {
			//menu is not ever closeable 
			dispFlags.setIsCloseable(false);
		}				
		// build all UI objects using specifications from instancing window
		_initAllGUIObjs(_isMenu, uiClkCoords);
		
		//run instancing window-specific initialization after all ui objects are built
		initMe();
		//set menu offset for custom UI objects
		custMenuOffset = uiClkCoords[3] + AppMgr.getClkBoxDim();
		//set any custom button names if necessary
		setCustMenuBtnLabels();
		//pass all flag states to initialized structures in instancing window handler
		privFlags.refreshAllFlags();
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
	 * Build appropriate UIDataUpdater instance for application
	 * @return
	 */	
	@Override
	public UIDataUpdater buildOwnerUIDataUpdateObject() {return buildUIDataUpdateObject();}
	
	/**
	 * Retrieve the Owner's UIDataUpdater
	 * @return
	 */
	@Override
	public UIDataUpdater getUIDataUpdater() {return uiUpdateData;}
	
	/**
	 * Build appropriate UIDataUpdater instance for application
	 * @return
	 */
	protected abstract UIDataUpdater buildUIDataUpdateObject();
	
	private void _initAllGUIObjs(boolean isMenu, float[] uiClkRect) {
		//initialize arrays to hold idxs of int and float items being created.
		guiFloatValIDXs = new ArrayList<Integer>();
		guiIntValIDXs = new ArrayList<Integer>();
		guiLabelValIDXs = new ArrayList<Integer>();
		if (!isMenu) {
			// list box values - keyed by list obj IDX, value is string array of list obj values
			TreeMap<Integer, String[]> tmpListObjVals = new TreeMap<Integer, String[]>();
			// ui object values - keyed by object idx, value is object array of describing values
			TreeMap<Integer, Object[]> tmpUIObjArray = new TreeMap<Integer, Object[]>();
			//  set up all gui objects for this window
			//setup all ui objects and record final y value in sidebar menu for UI Objects in this window
			setupGUIObjsAras(tmpUIObjArray,tmpListObjVals);					
			//initialized for sidebar menu as well as for display windows
			guiObjs_Numeric = new Base_NumericGUIObj[tmpUIObjArray.size()]; // list of modifiable gui objects
			//build ui objects
			uiClkRect[3] = _buildGUIObjsForMenu(tmpUIObjArray, tmpListObjVals, uiClkRect);	
		} else {
			//no guiObjs for menu
			guiObjs_Numeric = new Base_NumericGUIObj[0];
		}
		//build UI boolean buttons
		ArrayList<Object[]> tmpBtnNamesArray = new ArrayList<Object[]>();
		//  set up all window-specific boolean buttons for this window
		// this must return -all- priv buttons, not just those that are interactive (some may be hidden to manage functional booleans)
		int _numPrivFlags = initAllUIButtons(tmpBtnNamesArray);
		//initialize all private buttons based on values put in arraylist
		uiClkRect[3] = _buildAllPrivButtons(tmpBtnNamesArray, uiClkRect);
		// init specific sim flags
		privFlags = new WinAppPrivStateFlags(this,_numPrivFlags);
		// set instance-specific initial flags
		int[] trueFlagIDXs = getFlagIDXsToInitToTrue();
		//set local value for flags that should be initialized to true (without passing to instancing class handler yet)		
		if(null!=trueFlagIDXs) {_initPassedPrivFlagsToTrue(trueFlagIDXs);}	
		// build instance-specific UI update communication object if exists
		_buildUIUpdateStruct();
		
	}//_initAllGUIObjs
	
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
	 * @param tmpListObjVals
	 */
	@Override
	public void setupOwnerGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals) {
		setupGUIObjsAras(tmpUIObjArray,tmpListObjVals);					
	}
	
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
	 * @param tmpListObjVals
	 */	
	protected abstract void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);		
	
	/**
	 * Build the object array that describes a label object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default UI format boolean values. Label objects' behavior is restricted
	 * @return
	 */
	protected final Object[] uiObjInitAra_Label(double initVal, String name) {
		return uiObjInitAra_Label(initVal, name, dfltUIFmtVals);
	}		
	
	/**
	 * Build the object array that describes a integer object
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolFmtVals boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @return
	 */
	protected final Object[] uiObjInitAra_Label(double initVal, String name, boolean[] boolFmtVals) {
		return new Object[] {new double[0], initVal, name, GUIObj_Type.LabelVal, new boolean[] {false,false,false}, boolFmtVals};	
	}
	
	/**
	 * Build the object array that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_Int(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}	
		
	/**
	 * Build the object array that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_Int(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_Int(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}	
	
	/**
	 * Build the object array that describes a integer object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * @param boolFmtVals boolean array of format values :(unspecified values default to false)
	 *           	idx 0: whether multi-line(stacked) or not                                                  
	 *              idx 1: if true, build prefix ornament                                                      
	 *              idx 2: if true and prefix ornament is built, make it the same color as the text fill color.
	 * @return
	 */
	protected final Object[] uiObjInitAra_Int(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		return new Object[] {minMaxMod, initVal, name, GUIObj_Type.IntVal,boolVals, boolFmtVals};	
	}
	
	/**
	 * Build the object array that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_Float(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the object array that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_Float(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_Float(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the object array that describes a float object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_Float(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		return new Object[] {minMaxMod, initVal, name, GUIObj_Type.FloatVal,boolVals, boolFmtVals};	
	}

	/**
	 * Build the object array that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * NOTE : this method uses the default behavior and UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_List(double[] minMaxMod, double initVal, String name) {
		return uiObjInitAra_List(minMaxMod, initVal, name, dfltUIBehaviorVals, dfltUIFmtVals);
	}
	
	/**
	 * Build the object array that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_List(double[] minMaxMod, double initVal, String name, boolean[] boolVals) {
		return uiObjInitAra_List(minMaxMod, initVal, name, boolVals, dfltUIFmtVals);
	}	
	
	/**
	 * Build the object array that describes a list object
	 * @param minMaxMod 3-element double array holding the min and max vals and the base mod value
	 * @param initVal initial value for the object
	 * @param name name of the object
	 * @param boolVals boolean array specifying behavior (unspecified values are set to false): 
	 *           	idx 0: value is sent to owning window,  
	 *           	idx 1: value is sent on any modifications (while being modified, not just on release), 
	 *           	idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
	 * NOTE : this method uses the default UI format boolean values
	 * @return
	 */
	protected final Object[] uiObjInitAra_List(double[] minMaxMod, double initVal, String name, boolean[] boolVals, boolean[] boolFmtVals) {
		return new Object[] {minMaxMod, initVal, name, GUIObj_Type.ListVal,boolVals, boolFmtVals};	
	}	
	
	/**
	 * build ui objects from maps, keyed by ui object idx, with value being data
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
	 *           }    
	 * @param tmpListObjVals : map of list object data
	 * @param uiClkRect : 4-element array of upper corner x,y lower corner x,y coordinates for ui rectangular region
	 * @return y coordinate for end of ui region
	 */
	private float _buildGUIObjsForMenu(
			TreeMap<Integer, Object[]> tmpUIObjArray, 
			TreeMap<Integer, String[]> tmpListObjVals, 
			float[] uiClkRect) {
		int numGUIObjs = tmpUIObjArray.size();
		
		double[][] guiMinMaxModVals = new double[numGUIObjs][3];			//min max mod values
		double[] guiStVals = new double[numGUIObjs];						//starting values
		String[] guiObjNames = new String[numGUIObjs];						//display labels for UI components	
		//TODO Get guiColors from user input 
		int[][][] guiColors = new int[numGUIObjs][2][4];		
		
		//idx 0 is value is sent to owning window, 
		//idx 1 is value is sent on any modifications, 
		//idx 2 is if true, then changes to value are not sent to UIDataUpdater structure automatically
		boolean[][] guiBoolVals = new boolean[numGUIObjs][];				//array of UI flags for UI objects
		// idx 0: whether multi-line(stacked) or not
		// idx 1: if true, build prefix ornament
		// idx 2: if true and prefix ornament is built, make it the same color as the text fill color. 
		boolean[][] guiFormatBoolVals = new boolean[numGUIObjs][];		
				
		GUIObj_Type[] guiObjTypes = new GUIObj_Type[numGUIObjs];
		float textHeightOffset = AppMgr.getTextHeightOffset();
	
		for (int i = 0; i < numGUIObjs; ++i) {
			Object[] obj = tmpUIObjArray.get(i);
			boolean[] formatAra;
			if (obj.length == 6) {
				// object has been built with extended format array specified				
				formatAra = (boolean[])obj[5];
			} else {
				// Not specified, use default values - {false (single line), true (use prefix), false (don't use label color for prefix)}
				formatAra = new boolean[] {false, true,false};
			}
			guiMinMaxModVals[i] = (double[]) obj[0];
			guiStVals[i] = (Double)(obj[1]);
			guiObjNames[i] = (String)obj[2];
			guiObjTypes[i] = (GUIObj_Type)obj[3];
			//TODO Get guiColors from user input/configuration
			guiColors[i] = new int[][] {
						{0,0,0,255}, //stroke
						{0,0,0,255}, // fill
					};

			boolean[] tmpAra = (boolean[])obj[4];
			guiBoolVals[i] = new boolean[(tmpAra.length < 5 ? 5 : tmpAra.length)];
			int idx = 0;
			for (boolean val : tmpAra) {guiBoolVals[i][idx++] = val;}
			guiFormatBoolVals[i] = new boolean[(formatAra.length < 3 ? 3 : formatAra.length)];
			idx = 0;
			for (boolean val : formatAra) {	guiFormatBoolVals[i][idx++] = val;}
		}		
		//build all objects using these values 
		_buildAllObjects(guiObjNames, guiMinMaxModVals, 
				guiStVals, guiBoolVals, guiFormatBoolVals, 
				guiObjTypes, guiColors, tmpListObjVals, AppMgr.getUIOffset(), uiClkRect[2]);
		//Objects are created by here and assigned renderers
		// Assign hotspots
		myPointf newStPt = new myPointf(uiClkRect[0], uiClkRect[1], 0);
		for (int i = 0; i < guiObjs_Numeric.length; ++i) {
			// Get next newStPt as we calculate the hotspot region for every UI object
			newStPt = guiObjs_Numeric[i].reCalcHotSpot(newStPt, textHeightOffset, uiClkRect[0], uiClkRect[2]);			
		}
		//Make a smaller padding amount for final row
		uiClkRect[3] =  newStPt.y - .5f*textHeightOffset;
		// return final y coordinate
		return uiClkRect[3];
	}//_buildGUIObjsForMenu
	
	
	/**
	 * Build the renderer for a UI object 
	 * @param _owner
	 * @param _start
	 * @param _end
	 * @param _off
	 * @param _menuWidth max width of menu
	 * @param _colors : index 0 is stroke, index 1 is fill
	 * @param guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 				idx 0 : Should be multiline
	 * 				idx 1 : Should have ornament
	 * 				idx 2 : Ornament color should match label color 
	 * @return
	 */
	private Base_GUIObjRenderer buildRenderer(
			Base_GUIObj _owner, 
			double[] _off,
			float _menuWidth,
			int[][] _colors, 
			boolean[] guiFormatBoolVals) {
		
		int[] _strkClr = _colors[0];
		int[] _fillClr= _colors[1]; 
		if (guiFormatBoolVals[0]) {
			return new MultiLineGUIObjRenderer(ri, _owner, _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);
		} else {
			return new SingleLineGUIObjRenderer(ri, _owner, _off, _menuWidth, _strkClr, _fillClr, guiFormatBoolVals[1], guiFormatBoolVals[2]);			
		}
	}
	
	/**
	 * This will build objects sequentially using the values provided
	 * @param guiObjNames name of each object
	 * @param corners 2-element point array of upper left and lower right corners for object
	 * @param guiMinMaxModVals array of 3 element arrays of min and max value and base modifier
	 * @param guiStVals array of per-object initial values
	 * @param guiBoolVals array of boolean flags describing each object's behavior
	 * @param guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 				idx 0 : Should be multiline
	 * 				idx 1 : Should have ornament
	 * 				idx 2 : Ornament color should match label color 
	 * @param guiObjTypes array of per-object types
	 * @param guiColors 2-element array of int colors, idx0 == stroke, idx1 == fill
	 * @param tmpListObjVals map keyed by object idx where the value is a string array of elements to put in a list object
	 * @param UI_Off Either the ui offset to use for a prefixing ornament before the object's label, or null
	 * @param menuWidth Width of left side menu bar 
	 */
	private void _buildAllObjects(
			String[] guiObjNames, 
			double[][] guiMinMaxModVals, 
			double[] guiStVals, 
			boolean[][] guiBoolVals,
			boolean[][] guiFormatBoolVals,
			GUIObj_Type[] guiObjTypes, 
			int[][][] guiColors,
			TreeMap<Integer, String[]> tmpListObjVals, 
			double[] UI_off,
			float menuWidth) {
		int numListObjs = 0;
		for(int i =0; i< guiObjNames.length; ++i){
			switch(guiObjTypes[i]) {
				case IntVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Int(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i]);
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiIntValIDXs.add(i);
					break;}
				case ListVal : {
					++numListObjs;
					guiObjs_Numeric[i] = new MenuGUIObj_List(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i], tmpListObjVals.get(i));
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiIntValIDXs.add(i);
					break;}
				case FloatVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Float(i, guiObjNames[i], guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i]);
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiFloatValIDXs.add(i);
					break;}
				case LabelVal :{
					guiObjs_Numeric[i] = new MenuGUIObj_DispValue(i, guiObjNames[i], guiStVals[i]);					
					var renderer = buildRenderer(guiObjs_Numeric[i], UI_off, menuWidth, guiColors[i], guiFormatBoolVals[i]);
					guiObjs_Numeric[i].setRenderer(renderer);
					guiLabelValIDXs.add(i);
					break;}
				case Button  :{
					//TODO
					msgObj.dispWarningMessage(className, "_buildAllObjects", "Attempting to instantiate unknown UI object ID for a " + guiObjTypes[i].toStrBrf());
					break;
				}
				default : {
					msgObj.dispWarningMessage(className, "_buildAllObjects", "Attempting to instantiate unknown UI object for a " + guiObjTypes[i].toStrBrf());
					break;				
				}
			}
		}
		if(numListObjs != tmpListObjVals.size()) {
			msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Error!!!! # of specified list select UI objects ("+numListObjs+") does not match # of passed lists ("+tmpListObjVals.size()+") - some or all of specified list objects will not display properly.");
		}	
	}//_buildAllObjects	
	
	/**
	 * 
	 * @param idx
	 * @param len
	 * @param calFunc
	 * @param desc
	 * @return
	 */
	private boolean _validateUIObjectIdx(int idx, int len, String calFunc, String desc) {
		if (!MyMathUtils.inRange(idx, 0, len)){
			msgObj.dispErrorMessage(className, calFunc, 
				"Attempting to access illegal Numeric UI object to "+desc+" (idx :"+idx+" is out of range). Aborting.");
			return false;
		}		
		return true;
	}
	
	/**
	 * 
	 * @param obj
	 * @param calFunc
	 * @return
	 */
	private boolean _validateIdxIsListObj(Base_GUIObj obj, String calFunc, String desc) {
		if (obj.getObjType() != GUIObj_Type.ListVal) {
			msgObj.dispErrorMessage(className, calFunc, 
					"Attempting to access illegal List UI object to "+desc+" (object :"+obj.getName()+" is not a list object). Aborting.");
			return false;
		}
		return true;
	}
	
	/**
	 * Sets the passed UI object's new max value
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes
	 * @param maxVal
	 */
	public void setNewUIMaxVal(int idx, double maxVal) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIMaxVal", "set its max value")) {guiObjs_Numeric[idx].setNewMax(maxVal);}	
	}
	
	
	/**
	 * Sets the passed UI object's new min value
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes
	 * @param minVal
	 */
	public void setNewUIMinVal(int idx, double minVal) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIMinVal", "set its min value")) {guiObjs_Numeric[idx].setNewMin(minVal);}
	}
	
	/**
	 * Force a value to be set in the numeric UI object at the passed IDX
	 * @param idx index in numeric UI object array to access. If out of range, aborts without performing any changes and returns 0
	 * @param val
	 * @return the new value that was set, after having been bounded
	 */
	public double setNewUIValue(int idx, double val) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIValue", "set its value")) {guiObjs_Numeric[idx].setVal(val);}
			return 0;
		}		
	
	/**
	 * Set the display text of the passed UI Object, either numeric or boolean
	 * @param idx
	 * @param isNumeric
	 * @param str
	 */
	public void setNewUIDispText(int idx, boolean isNumeric, String str) {
		if (isNumeric) {
			if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIDispText", "set its display text")) {guiObjs_Numeric[idx].setLabel(str);}
			return;
		} else {
			//TODO support boolean UI objects
			if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setNewUIDispText", "set its display text")) {guiObjs_Numeric[idx].setLabel(str);}
			return;
		}
	}
	/**
	 * Specify a string to display in the idx'th List UI Object, if it exists, and is a list object
	 * @param idx
	 * @param val
	 * @return
	 */
	public int[] setDispUIListVal(int idx, String val) {		
		if ((!_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setDispUIListVal", "display passed value")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[idx], "setDispUIListVal", "display passed value"))){return new int[0];}
		return ((MenuGUIObj_List) guiObjs_Numeric[idx]).setValInList(val);
	}
	
	/**
	 * Set all the values in the idx'th List UI Object, if it exists, and is a list object
	 * @param idx
	 * @param values
	 * @return
	 */
	public int setAllUIListValues(int idx, String[] values) {		
		if ((!_validateUIObjectIdx(idx, guiObjs_Numeric.length, "setAllUIListValues", "add all list values")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[idx], "setAllUIListValues", "add all list values"))){return -1;}
		return ((MenuGUIObj_List) guiObjs_Numeric[idx]).setListVals(values);
	}
	
	/**
	 * Retrieve the min value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns max value
	 * @return
	 */
	public double getMinUIValue(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getMinUIValue","get its min value")) {return guiObjs_Numeric[idx].getMinVal();}
		return Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the max value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns min value
	 * @return
	 */
	public double getMaxUIValue(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getMaxUIValue","get its max value")){return guiObjs_Numeric[idx].getMaxVal();}
		return -Double.MAX_VALUE;
	}
	
	/**
	 * Retrieve the mod step value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns 0
	 * @return
	 */
	public double getModStep(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getModStep", "get its mod value")) {return guiObjs_Numeric[idx].getModStep();}
		return 0;
	}
	
	/**
	 * Retrieve the value of a numeric UI object
	 * @param idx index in numeric UI object array to access. If out of range, returns 0
	 * @return
	 */
	public double getUIValue(int idx) {
		if (_validateUIObjectIdx(idx, guiObjs_Numeric.length, "getUIValue", "get its value")) {return guiObjs_Numeric[idx].getVal();}
		return 0;
	}
	
	/**
	 * Get the string representation of the passed integer listIdx from the UI Object at UIidx
	 * @param UIidx
	 * @param listIdx
	 * @return
	 */
	public String getListValStr(int UIidx, int listIdx) {		
		if ((!_validateUIObjectIdx(UIidx, guiObjs_Numeric.length, "getListValStr", "get a list value at specified idx")) || 
				(!_validateIdxIsListObj(guiObjs_Numeric[UIidx], "getListValStr", "get a list value at specified idx"))){return "";}
		return ((MenuGUIObj_List) guiObjs_Numeric[UIidx]).getListValStr(listIdx);
	}
	
	/**
	 * This has to be called after UI structs are built and set - this creates and populates the 
	 * structure that serves to communicate UI data to consumer from UI Window.
	 */
	private void _buildUIUpdateStruct() {
		//set up UI->to->Consumer class communication object - only make instance of object here, 
		//initialize it after private flags are built and initialized
		uiUpdateData = buildUIDataUpdateObject();
		if (uiUpdateData == null) {return;}
		TreeMap<Integer, Integer> intValues = new TreeMap<Integer, Integer>();    
		for (Integer idx : guiIntValIDXs) {				intValues.put(idx, guiObjs_Numeric[idx].valAsInt());}		
		TreeMap<Integer, Float> floatValues = new TreeMap<Integer, Float>();
		for (Integer idx : guiFloatValIDXs) {			floatValues.put(idx, guiObjs_Numeric[idx].valAsFloat());}
		TreeMap<Integer, Boolean> boolValues = new TreeMap<Integer, Boolean>();
		for(Integer i=0; i < privFlags.numFlags;++i) {		boolValues.put(i, privFlags.getFlag(i));}	
		uiUpdateData.setAllVals(intValues, floatValues, boolValues); 
	}//_buildUIUpdateStruct
	
	/**
	 * Called by privFlags bool struct, to update uiUpdateData when boolean flags have changed
	 * @param idx
	 * @param val
	 */
	@Override
	public final void checkSetBoolAndUpdate(int idx, boolean val) {
		if((uiUpdateData != null) && uiUpdateData.checkAndSetBoolValue(idx, val)) {
			updateOwnerCalcObjUIVals();
		}
	}

	/**
	 * This will check if boolean value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	protected final boolean checkAndSetBoolValue(int idx, boolean value) {return uiUpdateData.checkAndSetBoolValue(idx, value);}
	/**
	 * This will check if Integer value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	protected final boolean checkAndSetIntVal(int idx, int value) {return uiUpdateData.checkAndSetIntVal(idx, value);}
	/**
	 * This will check if float value is different than previous value, and if so will change it
	 * @param idx of value
	 * @param val value to verify
	 * @return whether new value was modified
	 */
	protected final boolean checkAndSetFloatVal(int idx, float value) {return uiUpdateData.checkAndSetFloatVal(idx, value);}
	
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateBoolValFromExecCode(int idx, boolean value) {privFlags.setFlag(idx, value);uiUpdateData.setBoolValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateIntValFromExecCode(int idx, int value) {guiObjs_Numeric[idx].setVal(value);uiUpdateData.setIntValue(idx, value);}
	/**
	 * These are called externally from execution code object to synchronize ui values that might change during execution
	 * @param idx of particular type of object
	 * @param value value to set
	 */
	public final void updateFloatValFromExecCode(int idx, float value) {guiObjs_Numeric[idx].setVal(value);uiUpdateData.setFloatValue(idx, value);}
	
	@Override
	public void updateOwnerCalcObjUIVals() {
		updateCalcObjUIVals();
	}
	
	/**
	 * This function is called on ui value update, to pass new ui values on to window-owned consumers
	 */
	protected abstract void updateCalcObjUIVals();
		
	/**
	 * set up initial trajectories - 2d array, 1 per UI Page, 1 per modifiable construct within page.
	 */
	public final void initDrwnTrajs(){
		if(null!=trajMgr) {		trajMgr.initDrwnTrajs();	initDrwnTraj_Indiv();				}
	}
	
	
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

	//build UI clickable region
	protected final void initUIClickCoords(float x1, float y1, float x2, float y2){uiClkCoords[0] = x1;uiClkCoords[1] = y1;uiClkCoords[2] = x2; uiClkCoords[3] = y2;}
	protected final void initUIClickCoords(float[] cpy){	uiClkCoords[0] = cpy[0];uiClkCoords[1] = cpy[1];uiClkCoords[2] = cpy[2]; uiClkCoords[3] = cpy[3];}
	/**
	 * set up initial colors for sim specific flags for display
	 */
	private void initPrivFlagColors(){
		privFlagTrueColors = new int[truePrivFlagLabels.length][4];
		privFlagFalseColors = new int[privFlagTrueColors.length][4];
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		for (int i = 0; i < privFlagTrueColors.length; ++i) { 
			privFlagTrueColors[i] = new int[]{tr.nextInt(150),tr.nextInt(100),tr.nextInt(150), 255};
			if(truePrivFlagLabels[i].equals(falsePrivFlagLabels[i])) {
				privFlagFalseColors[i] = baseBtnFalseClr;
			} else {
				privFlagFalseColors[i] = new int[]{0,255-privFlagTrueColors[i][1],255-privFlagTrueColors[i][2], 255};
			}
		}			
	}
	
	/**
	 * set labels of boolean buttons for both true state and false state. Will be updated on next draw
	 * @param idx idx of button label to set
	 * @param tLbl new 
	 * @param fLbl
	 */
	protected void setButtonLabels(int idx, String tLbl, String fLbl) {truePrivFlagLabels[idx] = tLbl;falsePrivFlagLabels[idx] = fLbl;}
	
	/**
	 * set up child class button rectangles. Override-able for nested windows
	 */
	protected void initUIBox(){		
		float [] menuUIClkCoords = AppMgr.getUIRectVals(ID); 
		initUIClickCoords(menuUIClkCoords[0],menuUIClkCoords[3],menuUIClkCoords[2],menuUIClkCoords[3]);			
	}
	
	/**
	 * calculate button length
	 */
	//private static final float ltrLen = 5.0f;private static final int btnStep = 5;
	//private float _calcBtnLength(String tStr, String fStr){return btnStep * (int)(((MyMathUtils.max(tStr.length(), fStr.length())+4) * ltrLen)/btnStep);}
	private float _calcBtnLength(String tStr, String fStr){return MyMathUtils.max(ri.textWidth(tStr), ri.textWidth(fStr));}
	
	private void _setBtnDims(int idx, float xStart, float yEnd, float oldBtnLen, float btnLen) {privFlagBtns[idx]= new float[] {xStart+oldBtnLen, yEnd, btnLen, AppMgr.getTextHeightOffset() };}
	
	/**
	 * Take populated arraylist of object arrays describing private buttons and use these to initialize actual button arrays
	 * @param tmpBtnNamesArray arraylist of object arrays, each entry in object array holding a true string, a false string and an integer idx for the button
	 */	
	private float _buildAllPrivButtons(ArrayList<Object[]> tmpBtnNamesArray, float[] uiClkRect) {
		// finalize setup for UI toggle buttons - convert to arrays
		truePrivFlagLabels = new String[tmpBtnNamesArray.size()];
		falsePrivFlagLabels = new String[truePrivFlagLabels.length];
		privModFlgIdxs = new int[truePrivFlagLabels.length];
		for (int i = 0; i < truePrivFlagLabels.length; ++i) {
			Object[] tmpAra = tmpBtnNamesArray.get(i);
			truePrivFlagLabels[i] = (String) tmpAra[0];
			falsePrivFlagLabels[i] = (String) tmpAra[1];
			privModFlgIdxs[i] = (int) tmpAra[2];
		}
		return _buildPrivBtnRects(0, truePrivFlagLabels.length, uiClkRect);
	}//_buildAllPrivButtons
	
	/**
	 * set up boolean button rectangles using initialized truePrivFlagLabels and falsePrivFlagLabels
	 * @param yDisp displacement for button to be drawn
	 * @param numBtns number of buttons to make
	 */
	private float _buildPrivBtnRects(float yDisp, int numBtns, float[] uiClkRect){
		privFlagBtns = new float[numBtns][];
		if (numBtns == 0) {	return uiClkRect[3];	}
		float maxBtnLen = 0.95f * AppMgr.getMenuWidth(), halfBtnLen = .5f*maxBtnLen;
		uiClkRect[3] += getTextHeightOffset();
		float oldBtnLen = 0;
		boolean lastBtnHalfStLine = false, startNewLine = true;
		for(int i=0; i<numBtns; ++i){						//clickable button regions - as rect,so x,y,w,h - need to be in terms of sidebar menu 
			float btnLen = _calcBtnLength(truePrivFlagLabels[i].trim(),falsePrivFlagLabels[i].trim());
			//either button of half length or full length.  if half length, might be changed to full length in next iteration.
			//msgObj.dispDebugMessage(className, "_buildPrivBtnRects","i: "+i+" len : " +btnLen+" cap 1: " + truePrivFlagLabels[i].trim()+"|"+falsePrivFlagLabels[i].trim());
			if(btnLen > halfBtnLen){//this button is bigger than halfsize - it needs to be made full size, and if last button was half size and start of line, make it full size as well
				btnLen = maxBtnLen;
				if(lastBtnHalfStLine){//make last button full size, and make button this button on another line
					privFlagBtns[i-1][2] = maxBtnLen;
					uiClkRect[3] += getTextHeightOffset();
				}
				_setBtnDims(i, uiClkRect[0], uiClkRect[3], 0, btnLen);
				//privFlagBtns[i]= new float[] {(float)(uiClkRect[0]-winInitVals.getXOffset()), (float) uiClkRect[3], btnLen, yOff };				
				uiClkRect[3] += getTextHeightOffset();
				startNewLine = true;
				lastBtnHalfStLine = false;
			} else {//button len should be half width unless this button started a new line
				btnLen = halfBtnLen;
				if(startNewLine){//button is starting new line
					lastBtnHalfStLine = true;
					_setBtnDims(i, uiClkRect[0], uiClkRect[3], 0, btnLen);
					startNewLine = false;
				} else {//should only get here if 2nd of two <1/2 width buttons in a row
					lastBtnHalfStLine = false;
					_setBtnDims(i, uiClkRect[0], uiClkRect[3], oldBtnLen, btnLen);
					uiClkRect[3] += getTextHeightOffset();
					startNewLine = true;					
				}
			}			
			oldBtnLen = btnLen;
		}
		if(lastBtnHalfStLine){//set last button full length if starting new line
			privFlagBtns[numBtns-1][2] = maxBtnLen;
			uiClkRect[3] += getTextHeightOffset();
		}
		uiClkRect[3] += AppMgr.getRowStYOffset();
		initPrivFlagColors();
		return uiClkRect[3];
	}//_buildPrivBtnRects
	
	/**
	 * find index in flag name arrays of passed boolean IDX
	 * @param idx
	 * @return
	 */
	protected final int getFlagAraIdxOfBool(int idx) {
		for(int i=0;i<privModFlgIdxs.length;++i) {if(idx == privModFlgIdxs[i]) {return i;}	}		
		return -1;//not found
	}	
	
	/**
	 * set the right side menu state for this window - if it is actually present, show it
	 * @param visible
	 */
	public final void setRtSideInfoWinSt(boolean visible) {dispFlags.setRtSideInfoWinSt(visible);}	

	/**
	 * UI code-level Debug mode functionality. Called only from flags structure
	 * @param val
	 */
	public final void handleDispFlagsDebugMode(boolean val) {
		msgObj.dispDebugMessage(className, "handleDispFlagsDebugMode", "Start UI Code-specific Debug, called from base window Debug flags with value "+ val +".");
		handleDispFlagsDebugMode_Indiv(val);
		msgObj.dispDebugMessage(className, "handleDispFlagsDebugMode", "End UI Code-specific Debug, called from base window Debug flags with value "+ val +".");
	}
	protected abstract void handleDispFlagsDebugMode_Indiv(boolean val);

	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	@Override
	public final void handlePrivFlagsDebugMode(boolean val) {
		msgObj.dispDebugMessage(className, "handlePrivFlagsDebugMode", "Start App-specific Debug, called from App-specific Debug flags with value "+ val +".");
		handlePrivFlagsDebugMode_Indiv(val);
		msgObj.dispDebugMessage(className, "handlePrivFlagsDebugMode", "End App-specific Debug, called from App-specific Debug flags with value "+ val +".");
	}
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	protected abstract void handlePrivFlagsDebugMode_Indiv(boolean val);

	/**
	 * Switch structure only that handles priv flags being set or cleared. Called from WinAppPrivStateFlags structure
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
	
	/**
	 * Custom handling of when showRightSideMenu is set or cleared
	 * @param val
	 */
	public void handleShowRtSideMenu(boolean val) {
		float visWidth = (val ?  UIRtSideRectBox[0] : closedUIRtSideRecBox[0]);		//to match whether the side bar menu is open or closed
		setVisScreenWidth(visWidth);
	}
	
	/**
	 * set initial values for private flags for instancing window - set before initMe is called
	 */	
	public int[] getOwnerFlagIDXsToInitToTrue() {return getFlagIDXsToInitToTrue();}
	/**
	 * set initial values for private flags for instancing window - set before initMe is called
	 */
	protected abstract int[] getFlagIDXsToInitToTrue();
	
	/**
	 * sets flag values without calling instancing window flag handler - only for init!
	 * @param idxs
	 * @param val
	 */
	private void _initPassedPrivFlagsToTrue(int[] idxs) { 
		privFlags.setAllFlagsToTrue(idxs);
	}
	
	/**
	 * this will set the height of the rectangle enclosing this window - this will be called when a 
	 * window pushes up or pulls down this window - this resizes any drawn trajectories in this 
	 * window, and calls the instance class's code for resizing
	 * @param height
	 */
	public final void setRectDimsY(float height){
		float oldVal = dispFlags.getShowWin() ? winInitVals.rectDim[3] : winInitVals.rectDim[3];
		winInitVals.rectDim[3] = height;
		winInitVals.rectDim[3] = height;
		float scale  = height/oldVal;			//scale of modification - rescale the size and location of all components of this window by this
		if(null!=trajMgr) {		trajMgr.setTrajRectDimsY(height, scale);}
		if(dispFlags.getHasScrollBars()){for(int i =0; i<scbrs.length;++i){scbrs[i].setSize();}}
		resizeMe(scale);
	}
	
	/**
	 * Returns string holding reasonable string name for a subdir for this application. Includes name of window and timestamp when window was instanced
	 * @return
	 */
	public final String getAppFileSubdirName() {
		return ssFolderDir;
	}
	
	/**
	 * This returns a date-time string properly formatted to be used in file names or file paths.  Time is when called
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
	 * Set UI values by object type, sending value to 
	 * @param UIidx
	 */
	protected final void setUIWinVals(int UIidx) {
		//Determine whether int (int or list) or float
		GUIObj_Type objType = guiObjs_Numeric[UIidx].getObjType();
		switch (objType) {
			case IntVal : {
				int ival = guiObjs_Numeric[UIidx].valAsInt();
				int origVal = uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(guiObjs_Numeric[UIidx].shouldUpdateConsumer()) {updateOwnerCalcObjUIVals();}
					//Special per-obj int handling, if pertinent
					setUI_IntValsCustom(UIidx, ival, origVal);
				}
				break;}
			case ListVal : {
				int ival = guiObjs_Numeric[UIidx].valAsInt();
				int origVal = uiUpdateData.getIntValue(UIidx);
				if(checkAndSetIntVal(UIidx, ival)) {
					if(guiObjs_Numeric[UIidx].shouldUpdateConsumer()) {updateOwnerCalcObjUIVals();}
					//Special per-obj int (list idx)-related handling, if pertinent
					setUI_IntValsCustom(UIidx, ival, origVal);
				}
				break;}
			case FloatVal : {
				float val = guiObjs_Numeric[UIidx].valAsFloat();
				float origVal = uiUpdateData.getFloatValue(UIidx);
				if(checkAndSetFloatVal(UIidx, val)) {
					if(guiObjs_Numeric[UIidx].shouldUpdateConsumer()) {updateOwnerCalcObjUIVals();}
					//Special per-obj float handling, if pertinent
					setUI_FloatValsCustom(UIidx, val, origVal);
				}
				break;}
			case LabelVal : {
				msgObj.dispWarningMessage(className, "setUIWinVals", "Attempting to process the value `" + guiObjs_Numeric[UIidx].getValueAsString()+"` from the `" + guiObjs_Numeric[UIidx].getName()+ "` label object.");				
				break;}
			case Button : {
				msgObj.dispWarningMessage(className, "setUIWinVals", "Attempting to set a value for an unsupported Button UI object : " + objType.toStrBrf());
				break;}
			default : {
				msgObj.dispWarningMessage(className, "setUIWinVals", "Attempting to set a value for an unknown UI object for a " + objType.toStrBrf());
				break;}
			
		}//switch on obj type
	}//setUIWinVals
	
	/**
	 * Reset guiObj given by passed index to starting value
	 * @param uiIdx
	 */
	protected final void resetUIObj(int uiIdx) {guiObjs_Numeric[uiIdx].resetToInit();setUIWinVals(uiIdx);}	
	
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
	
	
	/**
	 * Reset all values to be initial values. 
	 * @param forceVals If true, this will bypass setUIWinVals, if false, will call set vals, to propagate changes to window vars 
	 */
	public final void resetUIVals(boolean forceVals){
		for(int i=0; i<guiObjs_Numeric.length;++i){				guiObjs_Numeric[i].resetToInit();		}
		if (!forceVals) {
			setAllUIWinVals();
		}
	}//resetUIVals
		
	/**
	 * this sets the value of a gui object from the data held in a string
	 * @param str
	 */
	protected final void setValFromFileStr(String str){
		String[] toks = str.trim().split("\\|");
		//window has no data values to load
		if(toks.length==0){return;}
		int uiIdx = Integer.parseInt(toks[0].split("\\s")[1].trim());
		guiObjs_Numeric[uiIdx].setValFromStrTokens(toks);
		setUIWinVals(uiIdx);//update window's values with UI construct's values
	}//setValFromFileStr
	
	/**
	 * 
	 * @param file
	 */
	public final void loadFromFile(File file){
		if (file == null) {
			msgObj.dispWarningMessage("Base_DispWindow","loadFromFile","Load was cancelled.");
		    return;
		} 
		String[] res = fileIO.loadFileIntoStringAra(file.getAbsolutePath(), "Variable File Load successful", "Variable File Load Failed.");
		int[] stIdx = {0};//start index for a particular window - make an array so it can be passed by ref and changed by windows
		hndlFileLoad(file, res,stIdx);
	}//loadFromFile
	
	/**
	 * 
	 * @return
	 */
	public final String[] getSaveFileDirName() {
		String[] vals = getSaveFileDirNamesPriv();
		if((null==vals) || (vals.length != 2)) {return new String[0];}
		String[] res = new String[] {
			ssPathBase + vals[0] + File.separatorChar, vals[1]	
		};
		return res;
	}
	
	
	public final void saveToFile(File file){
		if (file == null) {
			msgObj.dispWarningMessage("Base_DispWindow","saveToFile","Save was cancelled.");
		    return;
		} 
		ArrayList<String> res = new ArrayList<String>();

		res.addAll(hndlFileSave(file));	

		fileIO.saveStrings(file.getAbsolutePath(), res);  
	}//saveToFile	
	
	/**
	 * manage loading pre-saved UI component values, if useful for this window's load/save (if so call from child window's implementation
	 * @param vals
	 * @param stIdx
	 */
	protected final void hndlFileLoad_GUI(String[] vals, int[] stIdx) {
		++stIdx[0];
		//set values for ui sliders
		while(!vals[stIdx[0]].contains(winInitVals.winName + "_custUIComps")){
			if(vals[stIdx[0]].trim() != ""){	setValFromFileStr(vals[stIdx[0]]);	}
			++stIdx[0];
		}
		++stIdx[0];				
	}//hndlFileLoad_GUI
	
	/**
	 * manage saving this window's UI component values.  if needed call from child window's implementation
	 * @return
	 */
	protected final ArrayList<String> hndlFileSave_GUI(){
		ArrayList<String> res = new ArrayList<String>();
		res.add(winInitVals.winName);
		for(int i=0;i<guiObjs_Numeric.length;++i){	res.add(guiObjs_Numeric[i].getStrFromUIObj(i));}		
		//bound for custom components
		res.add(winInitVals.winName + "_custUIComps");
		//add blank space
		res.add("");
		return res;
	}//
	
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
		//if(this.flags[this.debugMode]){msgObj.dispWarningMessage("Base_DispWindow","setCameraBase","rx :  " + rx + " ry : " + ry + " dz : " + dz);}
		// puts origin of all drawn objects at screen center and moves forward/away by dz
		ri.translate(camVals[0],camVals[1],(float)dz); 
	    setCamOrient();	
	}
	public final void setCamera(float[] camVals){
		if(dispFlags.getUseCustCam()){setCamera_Indiv(camVals);}//individual window camera handling
		else {						setCameraBase(camVals);	}
	}//setCamera

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
	
	//draw a series of strings in a row
	protected final void dispBttnAtLoc(String txt, float[] loc, int[] clrAra){
		ri.setFill(clrAra, clrAra[3]);
		ri.setColorValStroke(IRenderInterface.gui_Black,255);
		ri.drawRect(loc);		
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		//pa.translate(-xOff*.5f,-yOff*.5f);
		ri.showText(""+txt,loc[0] + (txt.length() * .3f),loc[1]+loc[3]*.75f);
		//pa.translate(width, 0);
	}
	
	/**
	 * whether or not to draw the mouse reticle/rgb(xyz) projection/edge to eye
	 * @return
	 */
	public final boolean chkDrawMseRet(){		return dispFlags.getDrawMseEdge();	}
	
	//////////////////////
	//draw functions
	
	/**
	 * Entry point for draw functionality TODO
	 */
	public final void draw(float[] camvals, float modAmtMillis) {
		
		
	}
	
	
	/**
	 * initial draw stuff for each frame draw
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
		drawGUIObjs(isDebug, animTimeMod);
		//draw all boolean-based buttons for this window
		drawAppFlagButtons(dispFlags.getUseRndBtnClrs());
		//draw any custom menu objects for sidebar menu
		drawCustMenuObjs(animTimeMod);
		//also launch custom function here if any are specified
		checkCustMenuUIObjs();		
	}//drawWindowGuiObjs

	/**
	 * Draw application-specific flag buttons
	 * @param useRandBtnClrs
	 */
	private final void drawAppFlagButtons(boolean useRandBtnClrs) {
		ri.pushMatState();	
		ri.setColorValFill(IRenderInterface.gui_Black,255);
		String label;
		int[] clr;
		if(useRandBtnClrs){
			for(int i =0; i<privModFlgIdxs.length; ++i){
				if(privFlags.getFlag(privModFlgIdxs[i])){
					label = truePrivFlagLabels[i];
					clr = privFlagTrueColors[i];		
				} else {
					label = falsePrivFlagLabels[i];
					clr = privFlagFalseColors[i];
				}	
				dispBttnAtLoc(label,privFlagBtns[i],clr);	
			}
		} else {
			for(int i =0; i<privModFlgIdxs.length; ++i){
				if(privFlags.getFlag(privModFlgIdxs[i])){
					label = truePrivFlagLabels[i];
					clr = trueBtnClr;
				} else {																
					label = falsePrivFlagLabels[i];
					clr = falseBtnClr;
				}
				dispBttnAtLoc(label,privFlagBtns[i],clr);	
			}	
		}		
		ri.popMatState();	
	}//drawAppFlagButtons
	
	/**
	 * Draw UI Objs
	 * @param animTimeMod for potential future animated UI Objects
	 */
	protected final void drawGUIObjs(boolean isDebug, float animTimeMod) {
		ri.pushMatState();	
		if (isDebug) { 	
			for(int i =0; i<guiObjs_Numeric.length; ++i){guiObjs_Numeric[i].drawDebug();}
			ri.setStrokeWt(2.0f);
			ri.setNoFill();
			ri.setColorValStroke(this.ID * 10, 255);
			ri.drawRect(uiClkCoords[0], uiClkCoords[1], uiClkCoords[2]-uiClkCoords[0], uiClkCoords[3]-uiClkCoords[1]);
		}
		else {			for(int i =0; i<guiObjs_Numeric.length; ++i){guiObjs_Numeric[i].draw();}}
		ri.popMatState();	
	}
	
	/**
	 * draw any custom menu objects for sidebar menu
	 */
	protected abstract void drawCustMenuObjs(float animTimeMod);
	
	/**
	 * Build button descriptive arrays : each object array holds true label, false label, and idx of button in owning child class
	 * this must return count of -all- booleans managed by privFlags, not just those that are interactive buttons (some may be 
	 * hidden to manage booleans that manage or record state)
	 * @param tmpBtnNamesArray ArrayList of Object arrays to be built containing all button definitions. 
	 * @return count of -all- booleans to be managed by privFlags
	 */
	@Override
	public int initAllOwnerUIButtons(ArrayList<Object[]> tmpBtnNamesArray) {
		return initAllUIButtons(tmpBtnNamesArray);
	}
	protected abstract int initAllUIButtons(ArrayList<Object[]> tmpBtnNamesArray);	
	
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
	
	/**
	 * This is called after all UI and other draw functionality has occurred.
	 */
	public final void postDraw() {
		//last thing per draw - clear btns that have been set to clear after 1 frame of display
		if (dispFlags.getClearPrivBtns()) {clearAllPrivBtns();dispFlags.setClearPrivBtns(false);}
//		//if buttons have been set to clear, clear them next draw - put this in mouse release?
//		if (privBtnsToClear.size() > 0){dispFlags.setClearPrivBtns(true);	}				
	}
	
	/**
	 * separating bar for menu
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
	 * Draw 3d windows that are currently displayed
	 * @param modAmtMillis milliseconds since last frame started
	 */
	public final void draw3D(float modAmtMillis){
		if(!dispFlags.getShowWin()){return;}
		float animTimeMod = modAmtMillis/1000.0f;
		ri.pushMatState();	
		winInitVals.setWinFillAndStroke(ri);
		//draw instancing win-specific stuff
		drawMe(animTimeMod);			//call instance class's draw
		//draw traj stuff if exists and appropriate - if this window 
		//accepts a drawn trajectory, then allow it to be displayed
		if(null!=trajMgr){		trajMgr.drawTraj_3d(ri, animTimeMod, myPoint._add(sceneOriginVal,focusTar));}				
		ri.popMatState();		
	}//draw3D

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
		//msgObj.dispDebugMessage("Base_DispWindow","draw2D","Hitting hint code draw2D");
		ri.setBeginNoDepthTest();
		ri.disableLights();
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

	
	public void drawTraj3D(float animTimeMod, myPoint trans){
		msgObj.dispWarningMessage("Base_DispWindow","drawTraj3D","I should be overridden in 3d instancing class");
//			pa.pushMatState();	
//			if(null != tmpDrawnTraj){tmpDrawnTraj.drawMe(animTimeMod);}
//			TreeMap<String,ArrayList<myDrawnNoteTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
//			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
//				for(int i =0; i<tmpTreeMap.size(); ++i){
//					ArrayList<myDrawnNoteTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
//					if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){tmpAra.get(j).drawMe(animTimeMod);}}
//				}	
//			}
//			pa.popMatState();		
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

	
	public final int addInfoStr(String str){return addInfoStr(DebugInfoAra.size(), str);}
	public final int addInfoStr(int idx, String str){	
		int lstIdx = DebugInfoAra.size();
		if(idx >= lstIdx){		for(int i = lstIdx; i <= idx; ++i){	DebugInfoAra.add(i,"");	}}
		setInfoStr(idx,str);	return idx;
	}
	public final void setInfoStr(int idx, String str){DebugInfoAra.set(idx,str);	}
	public final void drawInfoStr(float sc, int clr){drawInfoStr(sc, ri.getClr(clr,255));}
	public final void drawInfoStr(float sc, int[] fillClr){//draw text on main part of screen
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
	 * if ending simulation, call this function
	 */
	private void endSim() {	AppMgr.setSimIsRunning(false);}//endSim
	
	/**
	 * call after single draw - will clear window-based priv buttons that are momentary
	 */
	protected final void clearAllPrivBtns() {
		if(privBtnsToClear.size() == 0) {return;}
		//only clear button if button is currently set to true, otherwise concurrent modification error
		for (Integer idx : privBtnsToClear) {if (privFlags.getFlag(idx)) {privFlags.setFlag(idx, false);}}
		privBtnsToClear.clear();
	}//clearPrivBtns()
		
	/**
	 * clear button next frame - to act like momentary switch.  will also clear UI object
	 * @param idx
	 */
	protected final void clearBtnNextFrame(int idx) {addPrivBtnToClear(idx);		checkAndSetBoolValue(idx, false);}
		
	/**
	 * add a button to clear after next draw
	 * @param idx index of button to clear
	 */
	protected final void addPrivBtnToClear(int idx) {
		privBtnsToClear.add(idx);
	}
	
	/**
	 * Access private flag values
	 * @param idx
	 * @return
	 */
	public final boolean getPrivFlag(int idx) {
		return privFlags.getFlag(idx);
	}
	
	public final void setPrivFlag(int idx, boolean val) {
		privFlags.setFlag(idx, val);
	}
	
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
		//msgObj.dispDebugMessage("Base_DispWindow","toggleWindowState","Attempting to close window : " + this.name);
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
	 * check if mouse location is in UI buttons, and handle button click if so
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	private boolean checkUIButtons(int mouseX, int mouseY){
		if(0==privFlagBtns.length) {return false;}
		boolean mod = false;
		//keep checking -see if clicked in UI buttons (flag-based buttons)
		for(int i = 0;i<privFlagBtns.length;++i){
			mod = msePtInRect(mouseX, mouseY, privFlagBtns[i]); 
			//msgObj.dispDebugMessage("Base_DispWindow","checkUIButtons","Handle mouse click in window : "+ ID + " : (" + mouseX+","+mouseY+") : "+mod + ": btn rect : "+privFlagBtns[i][0]+","+privFlagBtns[i][1]+","+privFlagBtns[i][2]+","+privFlagBtns[i][3]);
			if (mod){ 
				privFlags.toggleFlag(privModFlgIdxs[i]);
				//setPrivFlags(privModFlgIdxs[i],!getPrivFlags(privModFlgIdxs[i])); 
				return mod;
			}			
		}
		return mod;
	}//checkUIButtons	
	
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
	
	protected final myPoint getMsePoint(myPoint pt){return dispFlags.getIs3DWin() ? getMsePtAs3DPt(pt) : pt;}		//get appropriate representation of mouse location in 3d if 3d window
	public final myPoint getMsePoint(int mouseX, int mouseY){return dispFlags.getIs3DWin() ? getMsePtAs3DPt(new myPoint(mouseX,mouseY,0)) : new myPoint(mouseX,mouseY,0);}
	
	/**
	 * Return the coordinates of the clickable region for this window's UI
	 * @return
	 */
	public float[] getUIClkCoords() {return uiClkCoords;}
	
	/**
	 * Check inside all objects to see if passed mouse x,y is within hotspot
	 * @param mouseX
	 * @param mouseY
	 * @return idx of object that mouse resides in, or -1 if none
	 */
	private final int _checkInAllObjs(int mouseX, int mouseY) {
		for(int j=0; j<guiObjs_Numeric.length; ++j){if(guiObjs_Numeric[j].checkIn(mouseX, mouseY)){ return j;}}
		return -1;
	}	
	
	/**
	 * Handle mouse move over window
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public final boolean handleMouseMove(int mouseX, int mouseY){
		if(!dispFlags.getShowWin()){return false;}
		if(msePtInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			int idx = _checkInAllObjs(mouseX, mouseY);
			if(idx >= 0) {	msOvrObj=idx;return true;	}
		}
		myPoint mouseClickIn3D = AppMgr.getMseLoc(sceneOriginVal);
		if(hndlMouseMove_Indiv(mouseX, mouseY, mouseClickIn3D)){return true;}
		msOvrObj = -1;
		return false;
	}//handleMouseMove
	
	/**
	 * Check if point x,y is between r[0], r[1] and r[0]+r[2], r[1]+r[3]
	 * @param x
	 * @param y
	 * @param r rectangle - idx 0,1 is upper left corner, idx 2,3 is width, height
	 * @return
	 */
	public final boolean msePtInRect(int x, int y, float[] r){return ((x >= r[0])&&(x <= r[0]+r[2])&&(y >= r[1])&&(y <= r[1]+r[3]));}
	
	public final boolean msePtInUIClckCoords(int x, int y){
		return ((x > uiClkCoords[0])&&(x <= uiClkCoords[2])
				&&(y > uiClkCoords[1])&&(y <= uiClkCoords[3]));
	}	
	
	public final boolean pointInRectDim(int x, int y){return winInitVals.pointInRectDim(x, y);	}
	public final boolean pointInRectDim(myPoint pt){return winInitVals.pointInRectDim(pt);}	
	public final boolean pointInRectDim(myPointf pt){return winInitVals.pointInRectDim(pt);}
	public final float getTextHeightOffset() {return AppMgr.getTextHeightOffset();}
	
	/**
	 * Handle ticks from mouse wheel
	 * @param ticks
	 * @param mult amount to modify view based on sensitivity and whether shift is pressed or not
	 */
	public final void handleMouseWheel(int ticks, float mult) {
		if (dispFlags.getCanChgView()) {handleViewChange(true,(mult * ticks),0);}
	}//handleMouseWheel	
	
	/**
	 * handle a mouse click
	 * @param mouseX x location on screen
	 * @param mouseY y location on screen
	 * @param mseBtn which button is pressed : 0 is left, 1 is right
	 * @return
	 */
	public final boolean handleMouseClick(int mouseX, int mouseY, int mseBtn){
		boolean mod = false;
		//msgObj.dispConsoleDebugMessage(className, "handleMouseClick", String.format("Mouse click location @ [%d , %d]", mouseX, mouseY));
		//check if trying to close or open the window via click, if possible
		if(dispFlags.getIsCloseable()){mod = checkClsBox(mouseX, mouseY);}		
		boolean showWin = dispFlags.getShowWin();
		if(!showWin){return mod;}
		// this window is showing
		if(msePtInUIClckCoords(mouseX, mouseY)){//in clickable region for UI interaction
			int idx = _checkInAllObjs(mouseX, mouseY);
			if(idx >= 0) {
				//found in list of UI objects
				msBtnClcked = mseBtn;
				if(AppMgr.isClickModUIVal()){//allows for click-mod
					setUIObjValFromClickAlone(idx);
					dispFlags.setUIObjMod(true);
				} 				
				msClkObj=idx;
				return true;	
			}
		}			
		if(!mod) {			mod = checkUIButtons(mouseX, mouseY);	}
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
	 * vector for drag in 3D
	 * @param mouseX
	 * @param mouseY
	 * @param pmouseX
	 * @param pmouseY
	 * @param mseDragInWorld
	 * @param mseBtn
	 * @return
	 */
	public final boolean handleMouseDrag(int mouseX, int mouseY,int pmouseX, int pmouseY, myVector mseDragInWorld, int mseBtn){
		boolean mod = false;
		if(!dispFlags.getShowWin()){return mod;}
		boolean shiftPressed = AppMgr.shiftIsPressed();
		//check if modding view
		if (shiftPressed && dispFlags.getCanChgView() && (msClkObj==-1)) {//modifying view angle/zoom
			AppMgr.setModView(true);	
			if(mseBtn == 0){			handleViewChange(false,AppMgr.msSclY*(mouseY-pmouseY), AppMgr.msSclX*(mouseX-pmouseX));}	
			else if (mseBtn == 1) {		handleViewChange(true,(mouseY-pmouseY), 0);}	
			return true;
		} else if ((AppMgr.cntlIsPressed()) && dispFlags.getCanChgView() && (msClkObj==-1)) {//modifying view focus
			AppMgr.setModView(true);
			handleViewTargetChange((mouseY-pmouseY), (mouseX-pmouseX));
			return true;
		} else {//modify UI elements		
			//any generic dragging stuff - need flag to determine if trajectory is being entered		
			//modify object that was clicked in by mouse motion
			if(msClkObj!=-1){	
				guiObjs_Numeric[msClkObj].modVal((mouseX-pmouseX)+(mouseY-pmouseY)*-(shiftPressed ? 50.0f : 5.0f));
				dispFlags.setUIObjMod(true); 
				if(guiObjs_Numeric[msClkObj].shouldUpdateWin(false)){setUIWinVals(msClkObj);}
				return true;
			}		
			
			if(null!=trajMgr) {	mod = trajMgr.handleMouseDrag_Traj(mouseX, mouseY, pmouseX, pmouseY, mseDragInWorld, mseBtn);		}
			if(!mod) {
				if(!winInitVals.pointInRectDim(mouseX, mouseY)){return false;}	//if not drawing or editing a trajectory, force all dragging to be within window rectangle	
				//msgObj.dispDebugMessage("Base_DispWindow","handleMouseDrag","before handle indiv drag traj for window : " + this.name);
				myPoint mouseClickIn3D = AppMgr.getMseLoc(sceneOriginVal);
				mod = hndlMouseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY,mouseClickIn3D,mseDragInWorld,mseBtn);		//handle specific, non-trajectory functionality for implementation of window
			}
		}
		return mod;
	}//handleMouseDrag
	
	/**
	 * set all window values for UI objects
	 */
	protected final void setAllUIWinVals() {for(int i=0;i<guiObjs_Numeric.length;++i){if(guiObjs_Numeric[i].shouldUpdateWin(true)){setUIWinVals(i);}}}
	/**
	 * set UI value for object based on non-drag modification such as click - either at initial click or when click is released
	 * @param j
	 */
	private void setUIObjValFromClickAlone(int j) {
		float mult = msBtnClcked * -2.0f + 1;	//+1 for left, -1 for right btn	
		//msgObj.dispDebugMessage("Base_DispWindow","setUIObjValFromClickAlone","Mult : " + (mult *AppMgr.clickValModMult()));
		guiObjs_Numeric[j].modVal(mult * AppMgr.clickValModMult());
	}//setUIObjValFromClickAlone
	
	/**
	 * 
	 */
	public final void handleMouseRelease(){
		if(!dispFlags.getShowWin()){return;}
		if(dispFlags.getUIObjMod()){
			setAllUIWinVals();
			dispFlags.setUIObjMod(false);
			msClkObj = -1;	
		}//some object was clicked - pass the values out to all windows
		else if(msClkObj != -1) {
			//means object was clicked in but not drag modified through drag or shift-clic - use this to modify by clicking
			setUIObjValFromClickAlone(msClkObj);
			setAllUIWinVals();
			dispFlags.setUIObjMod(false);
			msClkObj = -1;	
		}
		
		if(null!=trajMgr) {trajMgr.handleMouseRelease_Traj(getMsePoint(ri.getMouse_Raw()));}
		msClkObj = -1;	
		//if buttons have been put in clear queue (set to clear), set flag to clear them next draw
		if (privBtnsToClear.size() > 0){dispFlags.setClearPrivBtns(true);	}
		
		hndlMouseRel_Indiv();//specific instancing window implementation stuff

		if(null!=trajMgr) {trajMgr.clearTmpDrawnTraj();}
	}//handleMouseRelease	
	
	//release shift/control/alt keys
	public final void endShiftKey(){
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endShiftKey(getMsePoint(ri.getMouse_Raw()));}
		endShiftKey_Indiv();
	}
	public final void endAltKey(){
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endAltKey(getMsePoint(ri.getMouse_Raw()));}
		endAltKey_Indiv();
	}	
	public final void endCntlKey(){
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endCntlKey(getMsePoint(ri.getMouse_Raw()));}
		endCntlKey_Indiv();
	}	
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
	/**
	 * Clear the values of the key and keycode that was pressed.  Called by GUI_AppMgr
	 */
	public final void endValueKeyPress() {
		if(!dispFlags.getShowWin()){return;}
		if(null!=trajMgr) {trajMgr.endValueKeyPress();}
		keyPressed = ' ';
		keyCodePressed = 0;
	}
	
	//finds closest point to p in sPts - put dist in d, returns index
	public final int findClosestPt(myPoint p, double[] d, myPoint[] _pts){
		int res = -1;
		double mindist = 99999999, _d;
		for(int i=0; i<_pts.length; ++i){_d = myPoint._dist(p,_pts[i]);if(_d < mindist){mindist = _d; d[0]=_d;res = i;}}	
		return res;
	}

	public final void rebuildAllDrawnTrajs(){
		if(null!=trajMgr) {trajMgr.rebuildAllDrawnTrajs();}
	}//rebuildAllDrawnTrajs
	
	/**
	 * debug data to display on screen get string array for onscreen display of debug info for each object
	 * @return
	 */
	public final String[] getDebugData(){
		ArrayList<String> res = new ArrayList<String>();
		List<String>tmp;
		for(int j = 0; j<guiObjs_Numeric.length; j++){tmp = Arrays.asList(guiObjs_Numeric[j].getStrData());res.addAll(tmp);}
		return res.toArray(new String[0]);	
	}
	
	//setup the launch of UI-driven custom functions or debugging capabilities, which will execute next frame
	
	//get key used to access arrays in traj array
	protected final String getTrajAraKeyStr(int i){if(null==trajMgr) {return "";} return trajMgr.getTrajAraKeyStr(i);}
	protected final int getTrajAraIDXVal(String str){if(null==trajMgr) {return -1;} return trajMgr.getTrajAraIDXVal(str);  }
	
	public final void clearAllTrajectories(){	if(null!=trajMgr) {		trajMgr.clearAllTrajectories();}}//clearAllTrajectories
	
	//add another screen to this window - need to handle specific trajectories - always remake traj structure
	public final void addSubScreenToWin(int newWinKey){						if(null!=trajMgr) {		trajMgr.modTrajStructs(newWinKey, "",false);			addSScrToWin_Indiv(newWinKey);}}
	public final void addTrajToSubScreen(int subScrKey, String newTrajKey){	if(null!=trajMgr) {		trajMgr.modTrajStructs(subScrKey, newTrajKey,false);	addTrajToScr_Indiv(subScrKey, newTrajKey);}}
	public final void delSubScreenToWin(int delWinKey){						if(null!=trajMgr) {		trajMgr.modTrajStructs(delWinKey, "",true);				delSScrToWin_Indiv(delWinKey);}}
	public final void delTrajToSubScreen(int subScrKey, String newTrajKey){	if(null!=trajMgr) {		trajMgr.modTrajStructs(subScrKey, newTrajKey,true);		delTrajToScr_Indiv(subScrKey,newTrajKey);}}
		
	//updates values in UI with programatic changes 
	public final boolean setWinToUIVals(int UIidx, double val){return val == guiObjs_Numeric[UIidx].setVal(val);}	
	//UI controlled auxiliary/debug functionality	
	public final void clickSideMenuBtn(int _row, int _funcOffset, int btnNum) {	curCstBtnRow = _row; curCstFuncBtnOffset = _funcOffset; curCustBtn[_row] = btnNum; custClickSetThisFrame = true;}
		
	public final void setThisWinDebugState(int btn,int val) {
		if(val==0) {//turning on
			msgObj.dispMessage(className, "handleSideMenuDebugSelEnable","Click Debug functionality on in " + winInitVals.winName + " : btn : " + btn, MsgCodes.debug1);
			handleSideMenuDebugSelEnable(btn);
			msgObj.dispMessage(className, "handleSideMenuDebugSelEnable", "End Debug functionality on selection.",MsgCodes.debug1);
		} else {
			msgObj.dispMessage(className, "handleSideMenuDebugSelDisable","Click Debug functionality off in " + winInitVals.winName + " : btn : " + btn, MsgCodes.debug1);
			handleSideMenuDebugSelDisable(btn);			
			msgObj.dispMessage(className, "handleSideMenuDebugSelDisable", "End Debug functionality off selection.",MsgCodes.debug1);
		}
	}
	
	
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
		msgObj.dispDebugMessage(className, "launchMenuBtnHndlr", "Begin requested action : Click '" + label +"' (Row:"+(row+1)+"|Col:"+col+") in " + winInitVals.winName);
		launchMenuBtnHndlr(row, col, label);
		msgObj.dispDebugMessage(className,"launchMenuBtnHndlr", "End requested action (multithreaded actions may still be working) : Click '" + label +"' (Row:"+(row+1)+"|Col:"+col+") in " + winInitVals.winName);
		custFuncDoLaunch=false;
	}//checkCustMenuUIObjs

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
	
	/**
	 * Handle selection of mouse-over-text option buttons in menu, specifying desired mouse over text to display in sim window
	 * @param btn
	 * @param val
	 */
	public abstract void handleSideMenuMseOvrDispSel(int btn,boolean val);	
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
	 * type is row of buttons (1st idx in curCustBtn array) 2nd idx is btn
	 * @param funcRow idx for button row
	 * @param btn idx for button within row (column)
	 * @param label label for this button (for display purposes)
	 */
	protected abstract void launchMenuBtnHndlr(int funcRow, int btn, String label) ;
	
	//return relevant name information for files and directories to be used to build screenshots/saved files	
	protected abstract String[] getSaveFileDirNamesPriv();
	
	protected abstract void initDrwnTraj_Indiv();
	protected abstract void addSScrToWin_Indiv(int newWinKey);
	protected abstract void addTrajToScr_Indiv(int subScrKey, String newTrajKey);
	protected abstract void delSScrToWin_Indiv(int idx);
	protected abstract void delTrajToScr_Indiv(int subScrKey, String newTrajKey);
	/**
	 * return appropriate 3d representation of mouse location - in 2d this will just be mseLoc x, mse Loc y, 0
	 * @param mseLoc x and y are int values of mouse x and y location
	 * @return
	 */
	protected abstract myPoint getMsePtAs3DPt(myPoint mseLoc);	
	//set window-specific variables that are based on current visible screen dimensions
	protected abstract void setVisScreenDimsPriv();
	//implementing class' necessary functions - implement for each individual window
	protected abstract boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld);
	protected abstract boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn);
	
	public final boolean sideBarMenu_CallWinMseDrag_Indiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {
		return hndlMouseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
	}
	
	protected abstract boolean hndlMouseDrag_Indiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn);
	protected abstract void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc);	
	
	protected abstract void hndlMouseRel_Indiv();
	
	protected abstract void endShiftKey_Indiv();
	protected abstract void endAltKey_Indiv();
	protected abstract void endCntlKey_Indiv();
	
	/**
	 * Modify the application-wide ui button labels based on context
	 */
	protected abstract void setCustMenuBtnLabels();
	
	public abstract void processTraj_Indiv(DrawnSimpleTraj drawnTraj);
	
	/**
	 * file io used from selectOutput/selectInput - take loaded params and process
	 * @param file
	 * @param vals
	 * @param stIdx
	 */
	public abstract void hndlFileLoad(File file, String[] vals, int[] stIdx);
	/**
	 * accumulate array of params to save
	 * @param file
	 * @return
	 */
	public abstract ArrayList<String> hndlFileSave(File file);	
	
	/**
	 * Initialize any UI control flags appropriate for window application
	 */
	protected abstract void initDispFlags();
	/**
	 * Initialize window's application-specific logic
	 */
	protected abstract void initMe();
	protected abstract void resizeMe(float scale);	
	protected abstract void showMe();
	protected abstract void closeMe();	
	protected abstract boolean simMe(float modAmtSec);
	protected abstract void stopMe();
	protected abstract void setCamera_Indiv(float[] camVals);
	/**
	 * Draw window/application-specific functionality
	 * @param animTimeMod # of milliseconds since last frame dividied by 1000
	 */
	protected abstract void drawMe(float animTimeMod);	
	protected abstract void drawRightSideInfoBarPriv(float modAmtMillis);
	protected abstract void drawOnScreenStuffPriv(float modAmtMillis);
	/**
	 * Retrieve MessageObject for logging and message display
	 * @return
	 */
	public final MessageObject getMsgObj() {return msgObj;}
	
	public final String getName() {return winInitVals.winName;}
	
	@Override
	public final String getClassName() {return className;}
	
	public final int getID() {return ID;}
	
	public String toString(){
		String res = winInitVals.toString();	
		return res;
	}
}//Base_DispWindow
