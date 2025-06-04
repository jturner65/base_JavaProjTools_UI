package base_UI_Objects;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.jogamp.newt.opengl.GLWindow;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.baseApp.Disp3DCanvas;
import base_UI_Objects.baseApp.GUI_AppStateFlags;
import base_UI_Objects.renderer.ProcessingRenderer;
//import base_UI_Objects.windowUI.background.base.Base_WinBackground;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.base.GUI_AppWinVals;
import base_UI_Objects.windowUI.sidebar.SidebarMenu;
import base_UI_Objects.windowUI.sidebar.SidebarMenuBtnConfig;
import base_Utils_Objects.appManager.Java_AppManager;


/**
 * this class manages all common functionality for a gui application, independent of renderer
 * @author john
 *
 */
public abstract class GUI_AppManager extends Java_AppManager {
	//////////////////////////////////
	// Rendering and viewing-based values
	/**
	 * rendering engine interface, providing expected methods.
	 */
	protected static IRenderInterface ri = null;
	/**
	 * Multiple of screen height that font size should be
	 */
	public static final float fontSizeScale = .0075f;
	/**
	 * point size of text; is some multiple of screen height. Should be able to support modification
	 */	
	private int _txtSize;
	/**
	 * 3d interaction stuff and mouse tracking
	 */
	private Disp3DCanvas _canvas;	
	/**
	 * Reference to GL Window underlying IRenderInterface surface
	 */
	public GLWindow window;	
	/**
	 * Width and height of application window
	 */
	private int _viewWidth, _viewHeight;
	/**
	 * Half-Width and Half-height of application window
	 */
	private int _viewWidthHalf, _viewHeightHalf;
	
	/**
	 * Aspect ratio for perspective display
	 */
	private float _aspectRatio;
	
	/**
	 * 9 element array holding camera loc, target, and orientation
	 */
	private float[] _camVals;	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Time and date
	
	/**
	 * time that this application started in millis
	 */
	private long _glblStartSimFrameTime,			//begin of draw(sim)
			_glblLastSimFrameTime,					//begin of last draw(sim)
			_memChkLastTimerMark,
			_memChkMillisTimer;						//# of milliseconds to count for memory update in title bar
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Display Window-based values
	/**
	 * max ratio of width to height to use for application window initialization for widescreen
	 */
	private final float _maxWinRatio =  1.77777778f;	
	
	/**
	 * individual display/HUD windows for gui/user interaction
	 */
	private Base_DispWindow[] _dispWinFrames = new Base_DispWindow[0];
	
	
	/**
	 * whether or not a particular background is 'dark' or 'light' ( backgrounds are dark if their average color is < 128)
	 */
	protected HashMap<Integer, Boolean> bgrndIsDarkAra;
	
	/**
	 * TODO backgrounds for windows, either spherical image or colors
	 */
	//private Base_WinBackground[] dispWinBkgrnds;
	
	/**
	 * Convenience ref to left side menu window.
	 */
	protected SidebarMenu sideBarMenu;
	
	/**
	 * set in instancing class - must be > 1
	 */
	public final int numDispWins;
	/**
	 * always idx 0 - first window is always right side menu
	 */
	private static final int dispMenuIDX = 0;
	/**
	 * which Base_DispWindow currently has focus
	 */
	private int _curFocusWin;	
	/**
	 * Width of left sidebar menu window
	 */
	protected float menuWidth;			

	private final float _menuWidthScale;
	private final float _hideWinWidthScale;
	private final float _hideWinHeightScale;	
	
	/**
	 * Width of window when hidden
	 */
	protected float hideWinWidth;
	/**
	 * Height of window when hidden
	 */
	protected float hideWinHeight;	
	/**
	 * Default menu width fraction
	 */
	private final float _dfltMenuWidthMult = .15f;
	/**
	 * Default hidden window width fraction
	 */
	private final float _dfltHideWinWidthMult = .03f;
	/**
	 * Default hidden window height fraction
	 */
	private final float _dfltHideWinHeightMult = .05f;	
	/**
	 * Default Popup window height fraction
	 */
	private final float _dfltPopUpWinOpenFraction = .80f;
	
	/**
	 * height, and fraction of full window height, a popup win should use, from bottom of screen, when open
	 */
	protected float popUpWinHeight;
	protected final float popUpWinOpenMult;
	
	/**
	 * Whether or not this application uses a sphere background for each window
	 */
	private boolean[] _useSkyboxBKGndAra;
	
	/**
	 * specify windows that cannot be shown simultaneously here and their flags
	 */
	public int[] winFlagsXOR, winDispIdxXOR;	
	
	/**
	 * Window initialization values, 1 per window, including left-side menu
	 */
	public GUI_AppWinVals[] winInitVals;

	/**
	 * flags explicitly pertaining to window visibility.  1 flag per window in application - flags defined in child class
	 */
	private int[] _winVisFlags;		

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// program control variables

	
	/**
	 * Application state flags
	 */
	private GUI_AppStateFlags _appStateFlags;
	
	/**
	 * flags used to control various elements of the entire application.
	 */
	private int[] _baseFlags;
	/**
	 * Base flag idxs
	 */
	private static final int 
			debugMode 			= 0,			//whether we are in dev debug mode or not	
			finalInitDone		= 1,			//used only to call final init in first draw loop, to avoid stupid timeout error processing 3.x's setup introduced
			saveAnim 			= 2,			//whether we are saving or not an anim screenie
			showCanvas			= 3,
			
	//simulation
			runSim				= 4,			//run simulation
			singleStep			= 5,			//run single sim step
	//UI
			showRtSideMenu		= 6,			//display the right side info menu for the current window, if it supports that display
			showStatusBar		= 7,			//whether or not to display status bar with frames per second and mem usage
			flipDrawnTraj  		= 8,			//whether or not to flip the direction of the drawn trajectory TODO this needs to be moved to window
			clearBKG 			= 9;			//whether or not background should be cleared for every draw.  defaults to true
	public final int numBaseFlags = 10;
	
	/**
	 * booleans in main program - need to have labels in idx order, even if not displayed
	 */
	private final String[] _truePFlagNames = {//needs to be in order of flags
			"Debug Mode",
			"Final init Done",
			"Save Anim",
			"Show Drawable Canvas",
			"Stop Simulation",
			"Single Step",
			"Displaying Side Menu",
			"Displaying Status Bar",
			"Reverse Drawn Trajectory",
			"Clearing Background"
			};
	
	private final String[] _falsePFlagNames = {//needs to be in order of flags
			"Debug Mode",	
			"Final init Done",
			"Save Anim", 
			"Show Drawable Canvas",
			"Run Simulation",
			"Single Step",
			"Display Side Menu",
			"Display Status Bar",
			"Reverse Drawn Trajectory",
			"Clear Background"
			};
	/**
	 * Colors to use to display true flags
	 */
	private int[][] _trueFlagColors;
	
	/**
	 * flags to actually display in menu as clickable text labels - order does matter
	 */
	private List<Integer> _flagsToShow = Arrays.asList( 
		debugMode, 			
		saveAnim,
		runSim,
		singleStep,
		showCanvas,
		showRtSideMenu,
		showStatusBar
		);
	
	private int _numFlagsToShow = _flagsToShow.size();
		
	public int animCounter = 0;
	/**
	 * whether or not to show start up instructions for code		
	 */
	public boolean showInfo = false;			
	
	//display-related size variables
	private int _2DGridDimX = 800, _2DGridDimY = 800;	
	private int _3DGridDimX = 800, _3DGridDimY = 800, _3DGridDimZ = 800;				//dimensions of 3d region
	private myPointf _3DHalfGridDim = new myPointf(_3DGridDimX*.5f, _3DGridDimY*.5f, _3DGridDimZ*.5f);
	
	/**
	 * boundary regions for enclosing cube - given as min and difference of min and max
	 */
	private float[][] _cubeBnds;	
	/**
	 * Unit direction vectors for cube walls
	 */
	private myVectorf[] _cubeDirAra;
	/**
	 * origins in wall planes for each index of _cubeDirAra normals.
	 */
	private myPointf[][] _origPerDirAra;
		
	/**
	 * 2D, 3D vector to focus point
	 */
	private myVectorf[] _sceneFcsValsAra;
	/**
	 * 2D, 3D vector to scene origin
	 */
	private myPointf[] _sceneOriginValsAra;
	
	//3D box stuff	
	private final myVectorf[] _boxNorms = new myVectorf[] {new myVectorf(1,0,0),new myVectorf(-1,0,0),new myVectorf(0,1,0),new myVectorf(0,-1,0),new myVectorf(0,0,1),new myVectorf(0,0,-1)};//normals to 3 d bounding boxes
	private myPointf[][] _boxWallPts;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// file IO variables
	protected final Path exePath = Paths.get(".").toAbsolutePath();
	//path string location
	protected final String exeDir = exePath.toString();
	//file location of current executable
	protected File currFileIOLoc = exePath.toFile();
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// mouse control variables

	//mouse wheel sensitivity
	public static final float mouseWhlSens = 1.0f;
	// distance within which to check if clicked from a point
	public double msClkEps = 40;				
	//scaling factors for mouse movement		
	public float msSclX, msSclY;											

	public final int[][] triColors = new int[][] {
		{IRenderInterface.gui_DarkMagenta,IRenderInterface.gui_DarkBlue,IRenderInterface.gui_DarkGreen,IRenderInterface.gui_DarkCyan}, 
		{IRenderInterface.gui_LightMagenta,IRenderInterface.gui_LightBlue,IRenderInterface.gui_LightGreen,IRenderInterface.gui_TransCyan}};
	
		
	// Maintain copy of memory map	
	protected Long[] currMemMap;
		
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// code
	
	public GUI_AppManager() {
		super(true);		
		_menuWidthScale = getMenuWidthMult();
		_hideWinWidthScale = getHideWinWidthMult();
		_hideWinHeightScale = getHideWinHeightMult();
		popUpWinOpenMult = getPopUpWinOpenMult();
		//Get number of windows
		numDispWins = getNumDispWindows();	
		//set up initial window-based configuration arrays
		//per-window initialization values
		winInitVals = new GUI_AppWinVals[numDispWins];		
		//display window initialization
		_dispWinFrames = new Base_DispWindow[numDispWins];
		//whether each 3D window uses Skybox or color background 
		_useSkyboxBKGndAra= new boolean[numDispWins];
		// set initial text size
		_txtSize = (int) (_displayHeight * fontSizeScale);
		// Initialize memory map
		currMemMap = getMemoryStatusMap();
		//initialize grid dim structs
		setGridDimStructs();
	}//	
		
	/**
	 * invoke the renderer main function for processing-based renderer - this is called 
	 * from instancing GUI_AppManager class
	 * TODO : remake this to be agnostic to render interface/applet
	 * @param <T>
	 * @param _appMgr
	 * @param passedArgs
	 */
	public static <T extends GUI_AppManager> void invokeProcessingMain(T _appMgr, String[] _passedArgs) {
		Java_AppManager.invokeMain(_appMgr, _passedArgs);
		ProcessingRenderer._invokedMain(_appMgr, _passedArgs);
	}
	
	/**
	 * Build runtime argument map, either from command-line arguments (for console applications) or from specifications in UI-based instancing AppManager
	 * @param passedArgs
	 */	
	@Override
	protected final HashMap<String, Object> buildCmdLineArgs(String[] passedArgs) {
		HashMap<String, Object> rawArgsMap = new HashMap<String, Object>();
		//just populate argsMap with values from passed args
		int argCount = 0;
		for(String arg : passedArgs) {	rawArgsMap.put("Arg_"+argCount++, arg);	}
		//possibly override arguments from arg parser within application
      	return rawArgsMap;
    }//handleRuntimeArgs
	
	public final void setIRenderInterface(IRenderInterface _pa) {
		if (null == ri) {ri=_pa;}
		ri.initRenderInterface();
	}	
	
	/**
	 * Used when initialized child windows for a particular window in instancing class.  
	 * @param _curWin
	 */
	protected final void setCurFocusWin(int _curWin) {	_curFocusWin = _curWin;}
	
	/**
	 * this will manage very large displays, while scaling window to smaller displays
	 * the goal is to preserve a reasonably close to 16:10 ratio window with big/widescreen displays
	 * @return int[] { desired application window width, desired application window height}
	 */
	public final int[] getIdealAppWindowDims() {		
		int winSizeCntrl = setAppWindowDimRestrictions();		
		switch(winSizeCntrl) {
			case 0 : {//don't care about window dimensions
				return new int[] {(int)(_displayWidth*.95f), (int)(_displayHeight*.92f)};
			}
			case 1 : {//make screen manageable for wide screen monitors
				float displayRatio = _displayWidth/(1.0f*_displayHeight);
				float newWidth = (displayRatio > _maxWinRatio) ?  _displayWidth * _maxWinRatio/displayRatio : _displayWidth;
				return new int[] {(int)(newWidth*.95f), (int)(_displayHeight*.92f)};
			}
			default :{//unsupported winSizeCntrl setting >= 2
				msgObj.dispConsoleWarningMessage("GUI_AppManager", "getIdealAppWindowDims","Unsupported value from setAppWindowDimRestrictions(). Defaulting to 0.");
				return new int[] {(int)(_displayWidth*.95f), (int)(_displayHeight*.92f)};
			}			
		}
	}//getIdealAppWindowDims
	
	/**
	 * set level of smoothing to use for rendering (depending on rendering used, this may be ignored)
	 */
	public abstract void setSmoothing();

	/**
	 * whether or not we want to restrict window size on widescreen monitors
	 * 
	 * @return 0 - use monitor size regardless
	 * 			1 - use smaller dim of monitor size or minimized ratio to determine window size (for wide screen monitors) 
	 * 			2+ - TBD
	 */
	protected abstract int setAppWindowDimRestrictions();
	
	/**
	 * Setup this application.  Called from render interface setup
	 * @param width width of the application window
	 * @param height height of the application window
	 */
	public final void setupApp(int width, int height) {
		//set initial window location
		ri.setLocation((int)((_displayWidth - width)*.5f), 0);
		//potentially override setup variables on per-project basis
		setupAppDims_Indiv();
		
		int[][] bkGrndColors = new int[numDispWins][];
		bgrndIsDarkAra = new HashMap<Integer, Boolean>();
		for(int i=0;i<numDispWins;++i) {
			_useSkyboxBKGndAra[i] = getUseSkyboxBKGnd(i);
			if (_useSkyboxBKGndAra[i]) {
				ri.loadBkgndSphere(i, getSkyboxFilename(i));
			}
			int[] bGroundClr = getBackgroundColor(i);
			bgrndIsDarkAra.put(i, ((bGroundClr[0]+bGroundClr[1]+bGroundClr[2])/3.0f < 128.0f));			
			ri.setRenderBackground(i, bGroundClr, bGroundClr[3]);
			bkGrndColors[i] = bGroundClr;
		}

		//Initialize application
		//Set window to point to main GL window
		window = ri.getGLWindow();
		//init internal state flags structure
		initBaseFlags();
		//Set dimensions for application based on applet window size and rebuild _canvas
		setAppWindowDims(width, height);
		//for every window, load either window color or window Skybox, depending on 
		//per-window specification
		//initialize the application state flags structure
		_appStateFlags = new GUI_AppStateFlags(this);			
		//called after window dims are set
		initBaseFlags_Indiv();
		//instancing class version
		initAllDispWindows();
		//All GUI_AppWinVals objects are built by here, now set 
		for (int i=0; i< winInitVals.length;++i) {
			winInitVals[i].setBackgrndColor(bkGrndColors[i]);
		}
		
		//set clearing the background to be true
		setBaseFlag(clearBKG,true);
		//init sim cycles count
		//visibility flags corresponding to windows
		initVisFlags();
		
		// set milli time tracking
		_glblStartSimFrameTime = timeMgr.getMillisFromProgStart();
		_glblLastSimFrameTime =  _glblStartSimFrameTime;
		_memChkMillisTimer = getMemStrUpdateMillis();
		_memChkLastTimerMark = _glblStartSimFrameTime/_memChkMillisTimer; 
				
		//call this in first draw loop also, if not setup yet
		initOnce();
	}
	
	/**
	 * Called in pre-draw initial setup, before first init
	 * potentially override setup variables on per-project basis.
	 * Do not use for setting background color or Skybox anymore.
	 *  	(Current settings in IRenderInterface implementation) 	
	 *  	strokeCap(PROJECT);
	 *  	textSize(txtSz);
	 *  	textureMode(NORMAL);			
	 *  	rectMode(CORNER);	
	 *  	sphereDetail(4);	 * 
	 */
	protected abstract void setupAppDims_Indiv();		

	
	/**
	 * Set the application window width and height and rebuild camera and _canvas, on init or resize (TODO)
	 * @param width
	 * @param height
	 */
	public final void setAppWindowDims(int width, int height) {
		_viewWidth = width;
		_viewHeight = height;			
		_viewWidthHalf = _viewWidth/2; 
		_viewHeightHalf = _viewHeight/2;
		
		_aspectRatio = _viewWidth/(1.0f*_viewHeight);
		msSclX = MyMathUtils.PI_F/_viewWidth;
		msSclY = MyMathUtils.PI_F/_viewHeight;

		menuWidth = _viewWidth * _menuWidthScale;	
		
		hideWinWidth = _viewWidth * _hideWinWidthScale;				//dims for hidden windows
		//popup/hidden window height to use when hidden 
		hideWinHeight = _viewHeight * _hideWinHeightScale;
		//popup window height when open
		popUpWinHeight = _viewHeight * popUpWinOpenMult;
		// set cam vals
		_camVals = new float[]{0, 0, (float) (_viewHeightHalf / Math.tan(MyMathUtils.PI/6.0)), 0, 0, 0, 0,1,0};		
		//build _canvas
		_canvas = new Disp3DCanvas(this, ri, _viewWidth, _viewHeight);	
		msgObj.dispInfoMessage("GUI_AppManager","setAppWindowDims","Base applet width : " + _viewWidth + " | height : " +  _viewHeight);
	}//setAppWindowWidth


	public final float getMenuWidth() {return menuWidth;}
	
	/**
	 * Get size of text as fraction of display height.
	 * @return
	 */
	public final int getTextSize() {return _txtSize;}
	
	///////////////////////////////////////
	// UI text/display dims based on text size
	/**
	 * size of interaction/close window box in pxls
	 * @return
	 */
	public final float getClkBoxDim() {
		return getTextSize();
	}
	
	/**
	 * Height of a line of text. Also used as a width of an average character
	 */
	public final float getTextHeightOffset() {
		return 1.25f * getTextSize();
	}
	
	/**
	 * Based on textSize but slightly smaller for purely label/read only text
	 * @return
	 */
	public final float getLabelTextHeightOffset() {
		return 1.1f * getTextSize();
	}
	
	/**
	 * Base right side text menu per-line height offset
	 */
	public final float getRtSideTxtHeightOffset(){
		return getLabelTextHeightOffset();// - 4.0f;
	}
	
	/**
	 * Right side menu y values
	 * 		idx 0 : current y value for text (changes with each frame)
	 * 		idx 1 : per-line y offset for grouped text
	 * 		idx 2 : per-line y offset for title-to-group text (small space)
	 * 		idx 3 : per-line y offset for text that is not grouped (slightly larger)
	 */
	public final float[] getRtSideYOffVals() {
		float rtSideTxtHeightOff = getRtSideTxtHeightOffset();
		return new float[] {0, rtSideTxtHeightOff, 1.2f * rtSideTxtHeightOff, 1.5f * rtSideTxtHeightOff};
	}

	/**
	 * X Dimension offset for text
	 */
	public final float getXOffset() {
		return getTextHeightOffset();
	}

	/**
	 * Half of X Dimension offset for text
	 */
	public final float getXOffsetHalf() {
		return 0.5f * getXOffset();
	}
	
	
	/**
	 * Offset for starting a new row in Y
	 */
	public final float getRowStYOffset() {
		return 0.15f * getTextHeightOffset();
	}
	
	/**
	 * The Y distance between 2 successive buttons
	 * @return
	 */
	public final float getBtnLabelYOffset() {
		return 2.0f * getTextHeightOffset(); 
	}

	/**
	 * array of x,y offsets for UI objects that have a prefix graphical element
	 * @return
	 */
	public final double[] getUIOffset() {
		return new double[] { getXOffset(), getTextHeightOffset() };
	}	
	
	///////////////////////////////////////
	//End UI text/display dims based on text size	
	
	/**
	 * Override if want to resize menu width fraction. Needs to be [0,1]
	 * @return
	 */
	protected float getMenuWidthMult_Custom() {return _dfltMenuWidthMult;}
	/**
	 * Override if want to resize hidden window width fraction. Needs to be [0,1]
	 * @return
	 */
	protected float getHideWinWidthMult_Custom() {return _dfltHideWinWidthMult;}
	/**
	 * Override if want to resize hidden window height fraction. Needs to be [0,1]
	 * @return
	 */
	protected float getHideWinHeightMult_Custom() {return _dfltHideWinHeightMult;}
	
	/**
	 * Override if want to resize popup window open size fraction. Needs to be [0,1]
	 * @return
	 */
	protected float getPopUpWinOpenMult_Custom() {	return _dfltPopUpWinOpenFraction;}	
	
	/**
	 * Override if we want to speed up or slow down the memory query for the title bar, in number of millis between updates
	 */
	protected long getMemStrUpdateMillis() {return 200L;}
	
	/**
	 * Whether or not the 3d window at winIdx of this application 
	 * should use a sphere Skybox background. Ignored for 2D
	 * @param winIdx the window idx
	 * @return
	 */
	protected abstract boolean getUseSkyboxBKGnd(int winIdx);
	
	/**
	 * Get the file name for the window skybox
	 * @param winIdx
	 * @return
	 */
	protected abstract String getSkyboxFilename(int winIdx);
	
	/**
	 * Get the background color to use for the window at winIdx
	 * @param winIdx
	 * @return
	 */
	protected abstract int[] getBackgroundColor(int winIdx);
	
	/**
	 * Returns the number of display windows in this application, including the sidebar menu
	 * @return
	 */
	protected abstract int getNumDispWindows();
	
	/**
	 * Provide multiplier for window width fraction. Needs to be [0,1]
	 * @return
	 */
	private final float getMenuWidthMult() {
		float retVal = getMenuWidthMult_Custom();
		return MyMathUtils.inRange(retVal, 0.0f, 1.0f) ? retVal : _dfltMenuWidthMult;
	}
	/**
	 * Provide multiplier for hidden window width fraction. Needs to be [0,1]
	 * @return
	 */
	private final float getHideWinWidthMult() {
		float retVal = getHideWinWidthMult_Custom();		
		return MyMathUtils.inRange(retVal, 0.0f, 1.0f) ? retVal : _dfltHideWinWidthMult;
	}
	/**
	 * Provide multiplier for hidden window height fraction. Needs to be [0,1]
	 * @return
	 */
	private final float getHideWinHeightMult() {
		float retVal = getHideWinHeightMult_Custom();		
		return MyMathUtils.inRange(retVal, 0.0f, 1.0f) ? retVal : _dfltHideWinHeightMult;
	}
	
	/**
	 * Provide multiplier for popup window open size fraction. Needs to be [0,1]
	 * @return
	 */
	private final float getPopUpWinOpenMult() {		
		float retVal = getPopUpWinOpenMult_Custom();		
		return MyMathUtils.inRange(retVal, 0.0f, 1.0f) ? retVal : _dfltPopUpWinOpenFraction;
	}
	
	/**
	 * Retrieves the default window dimensions and camera initial values
	 * @return
	 */	
	public float[][] getDefaultWinAndCameraDims(){
		return new float[][] {getDefaultWinDimOpen(), getDefaultWinDimClosed(), getInitCameraValues()};
	}
	/**
	 * Retrieves reasonable default window open dims
	 * @return
	 */
	public float[] getDefaultWinDimOpen() {return new float[]{menuWidth, 0, _viewWidth-menuWidth,  _viewHeight};}
	
	/**
	 * Retrieves reasonable default window closed dims
	 * @return
	 */
	public float[] getDefaultWinDimClosed() {return new float[]{menuWidth, 0, hideWinWidth,  _viewHeight};}

	/**
	 * Retrieves reasonable default pop-up window open dims
	 * @return
	 */
	public float[] getDefaultPopUpWinDimOpen() {return new float[]{menuWidth, _viewHeight-popUpWinHeight, _viewWidth-menuWidth,  popUpWinHeight};}
	
	/**
	 * Retrieves reasonable default pop-up window closed dims
	 * @return
	 */
	public float[] getDefaultPopUpWinDimClosed() {return new float[]{menuWidth, _viewHeight-hideWinHeight, hideWinWidth,  hideWinHeight};}

	/**
	 * this is called to determine which main flags to display in the window
	 */
	protected abstract void initBaseFlags_Indiv();
	
	/**
	 * this is called to build all the Base_DispWindows in the instancing class
	 */
	protected abstract void initAllDispWindows();

	/**
	 * 1 time initialization of programmatic things that won't change
	 */
	public final void initOnce() {
		//1-time init for program and windows
		//always default to showing left side input UI menu
		setWinVisFlag(dispMenuIDX, true);
		//app-specific 1-time init
		initOnce_Indiv();
		//initProgram is called every time reinitialization is desired
		initProgram();		

		//after all init is done
		setFinalInitDone(true);
	}//initOnce	

	protected abstract void initOnce_Indiv();	
	
	/**
	 * called every time re-initialized
	 */
	public final void initProgram() {
		for (int i=1; i<_dispWinFrames.length;++i) {	_dispWinFrames[i].reInitInfoStr();}		
		initProgram_Indiv();
	}//initProgram	
	protected abstract void initProgram_Indiv();
	
	//specify windows that cannot be shown simultaneously here
	protected void initXORWins(int[] _winFlags, int[] _winIdxs) {
		winFlagsXOR = _winFlags;
		//specify windows that cannot be shown simultaneously here
		winDispIdxXOR = _winIdxs;		
	}
	
	/**
	 * modify 3D grid dimensions to be cube of passed value per side
	 * @param _gVal
	 */
	protected void setDesired3DGridDims(int _gVal) {setDesired3DGridDims(_gVal,_gVal,_gVal);}
	/**
	 * modify 3D grid dimensions to be cube of passed value dims
	 * @param _gx desired x dim
	 * @param _gy desired y dim
	 * @param _gz desired z dim
	 */
	protected void setDesired3DGridDims(int _gx, int _gy, int _gz) {
		_3DGridDimX = _gx;_3DGridDimY = _gy;_3DGridDimZ = _gz;				//dimensions of 3d region
		setGridDimStructs();
	}
	
	/**
	 * Specify 2D grid dims
	 * @param _gx
	 * @param _gy
	 */
	protected void setDesired2DGridDims(int _gx, int _gy) {
		_2DGridDimX = _gx; _2DGridDimY = _gy;	
		setGridDimStructs();
	}
	
	private void setGridDimStructs() {
		_3DHalfGridDim.set(_3DGridDimX*.5f,_3DGridDimY*.5f,_3DGridDimZ*.5f );
		myPointf _3DTenGridDim = new myPointf(_3DGridDimX*10.0f, _3DGridDimY*10.0f, _3DGridDimZ*10.0f);
		
		_cubeBnds = new float[][]{//idx 0 is min, 1 is diffs
			new float[]{-_3DGridDimX/2.0f,-_3DGridDimY/2.0f,-_3DGridDimZ/2.0f},	//mins
			new float[]{_3DGridDimX,_3DGridDimY,_3DGridDimZ}};					//diffs
			
			
		_cubeDirAra = new myVectorf[] {
				new myVectorf(_cubeBnds[1][0], 0.f, 0.f),
				new myVectorf(0.0f, _cubeBnds[1][1], 0.0f),
				new myVectorf(0.0f, 0.f, _cubeBnds[1][2]),
		};
	
		_origPerDirAra = new myPointf[][] {
			 new myPointf[] {
			    		new myPointf(_cubeBnds[0]),																		//min x, min y, min z
			    		new myPointf(_cubeBnds[0][0], _cubeBnds[0][1] +_cubeBnds[1][1], _cubeBnds[0][2]),					//min x, max y, min z
			    		new myPointf(_cubeBnds[0][0], _cubeBnds[0][1], 				 _cubeBnds[0][2] +_cubeBnds[1][2]),	//min x, min y, max z
			    		new myPointf(_cubeBnds[0][0], _cubeBnds[0][1] +_cubeBnds[1][1], _cubeBnds[0][2] +_cubeBnds[1][2])	//min x, max y, max z
			    },
			new myPointf[] {
		    		new myPointf(_cubeBnds[0]),             																//min x, min y, min z  
		    		new myPointf(_cubeBnds[0][0] +_cubeBnds[1][0], 	_cubeBnds[0][1], _cubeBnds[0][2]),            	 	//max x, min y, min z  
		    		new myPointf(_cubeBnds[0][0], 					_cubeBnds[0][1], _cubeBnds[0][2] +_cubeBnds[1][2]),	//min x, min y, max z  
		    		new myPointf(_cubeBnds[0][0] +_cubeBnds[1][0], 	_cubeBnds[0][1], _cubeBnds[0][2] +_cubeBnds[1][2]) 	//max x, min y, max z  
		    }, 
			new myPointf[] {
		    		new myPointf(_cubeBnds[0]), //min x, min y, min z  
		    		new myPointf(_cubeBnds[0][0] +_cubeBnds[1][0],	_cubeBnds[0][1], 				_cubeBnds[0][2]), 	 //max x, min y, min z  
		    		new myPointf(_cubeBnds[0][0], 					_cubeBnds[0][1] +_cubeBnds[1][1], 	_cubeBnds[0][2]), //min x, max y, min z  
		    		new myPointf(_cubeBnds[0][0] +_cubeBnds[1][0], 	_cubeBnds[0][1] +_cubeBnds[1][1], 	_cubeBnds[0][2])  //max x, max y, min z  
		    }
			
		};
		
		//2D, 3D
		_sceneFcsValsAra = new myVectorf[]{						//set these values to be different targets of focus
				new myVectorf(-_2DGridDimX/2,-_2DGridDimY/1.75f,0),
				new myVectorf(0,0,0)
		};
		//2D, 3D
		_sceneOriginValsAra = new myPointf[]{				//set these values to be different display center translations -
			new myPointf(0,0,0),										// to be used to calculate mouse offset in world for pick
			new myPointf(-_3DGridDimX/2.0,-_3DGridDimY/2.0,-_3DGridDimZ/2.0)
		};
		
		_boxWallPts = new myPointf[][] {//pts to check if intersection with 3D bounding box happens
				new myPointf[] {new myPointf(_3DHalfGridDim.x,_3DTenGridDim.y,_3DTenGridDim.z), new myPointf(_3DHalfGridDim.x,-_3DTenGridDim.y,_3DTenGridDim.z), new myPointf(_3DHalfGridDim.x,_3DTenGridDim.y,-_3DTenGridDim.z)  },
				new myPointf[] {new myPointf(-_3DHalfGridDim.x,_3DTenGridDim.y,_3DTenGridDim.z), new myPointf(-_3DHalfGridDim.x,-_3DTenGridDim.y,_3DTenGridDim.z), new myPointf(-_3DHalfGridDim.x,_3DTenGridDim.y,-_3DTenGridDim.z) },
				new myPointf[] {new myPointf(_3DTenGridDim.x,_3DHalfGridDim.y,_3DTenGridDim.z), new myPointf(-_3DTenGridDim.x,_3DHalfGridDim.y,_3DTenGridDim.z), new myPointf(_3DTenGridDim.x,_3DHalfGridDim.y,-_3DTenGridDim.z) },
				new myPointf[] {new myPointf(_3DTenGridDim.x,-_3DHalfGridDim.y,_3DTenGridDim.z),new myPointf(-_3DTenGridDim.x,-_3DHalfGridDim.y,_3DTenGridDim.z),new myPointf(_3DTenGridDim.x,-_3DHalfGridDim.y,-_3DTenGridDim.z) },
				new myPointf[] {new myPointf(_3DTenGridDim.x,_3DTenGridDim.y,_3DHalfGridDim.z), new myPointf(-_3DTenGridDim.x,_3DTenGridDim.y,_3DHalfGridDim.z), new myPointf(_3DTenGridDim.x,-_3DTenGridDim.y,_3DHalfGridDim.z)  },
				new myPointf[] {new myPointf(_3DTenGridDim.x,_3DTenGridDim.y,-_3DHalfGridDim.z),new myPointf(-_3DTenGridDim.x,_3DTenGridDim.y,-_3DHalfGridDim.z),new myPointf(_3DTenGridDim.x,-_3DTenGridDim.y,-_3DHalfGridDim.z)  }};
	}
	
	/**
	 * build the appropriate side bar menu configuration for this application
	 * @param _winTitles array of per window titles
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _funcBtnNames array of arrays of names for each button
	 * @param _dbgBtnNames array of names for each debug button. If array is empty then no debug buttons will be handled.
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 */
	public final void buildSideBarMenu(String[] _winTitles, String[] _funcRowNames, String[][] _funcBtnNames, String[] _dbgBtnNames, boolean _inclWinNames, boolean _inclMseOvValues){
		 /** @param _winIdx the window's idx
		 * @param _strVals an array holding the window title(idx 0) and the window description(idx 1)
		 * @param _flags an array holding boolean values for idxs : 
		 * 		0 : dispWinIs3d, 
		 * 		1 : canDrawInWin; 
		 * 		2 : canShow3dbox (only supported for 3D); 
		 * 		3 : canMoveView
		 * @param _floatVals an array holding float arrays for 
		 * 				rectDimOpen(idx 0),
		 * 				rectDimClosed(idx 1),
		 * 				initCameraVals(idx 2)
		 * @param _intVals and array holding int arrays for
		 * 				winFillClr (idx 0),
		 * 				winStrkClr (idx 1),
		 * 				winTrajFillClr(idx 2),
		 * 				winTrajStrkClr(idx 3),
		 * 				rtSideFillClr(idx 4),
		 * 				rtSideStrkClr(idx 5)
		 * @param _sceneCenterVal center of scene, for drawing objects
		 * @param _initSceneFocusVal initial focus target for camera
		 */
		winInitVals[dispMenuIDX] = new GUI_AppWinVals(dispMenuIDX, new String[] {"UI Window", "User Controls"}, new boolean[4],
				new float[][] {new float[]{0,0, menuWidth, _viewHeight}, new float[]{0,0, hideWinWidth, _viewHeight},getInitCameraValues()},
				new int[][] {new int[]{255,255,255,255},new int[]{0,0,0,255},new int[]{0,0,0,255},new int[]{0,0,0,255},new int[]{0,0,0,200},new int[]{255,255,255,255}},
				_sceneOriginValsAra[0], _sceneFcsValsAra[0]);
		_winTitles[0] = "UI Window";
		SidebarMenuBtnConfig sideBarConfig = new SidebarMenuBtnConfig(ri, this, _funcRowNames, _funcBtnNames, _dbgBtnNames, _winTitles, _inclWinNames, _inclMseOvValues);
		_dispWinFrames[dispMenuIDX] = new SidebarMenu(ri, this, dispMenuIDX, sideBarConfig);	
		sideBarMenu = (SidebarMenu)(_dispWinFrames[dispMenuIDX]);
	}
	
	/**
	 * Return a default boolean array of window-related flags suitable for most window creation for both 2D and 3D.
	 * @param is3D whether the window is 3d or not
	 * @return an array of booleans with idx/vals : 
	 * 								is3D		!is3D
	 * 		0 : dispWinIs3d,		true		false
	 * 		1 : canDrawInWin		false		false
	 * 		2 : canShow3dbox		true		false
	 * 		3 : canMoveView 		true		true
	 * 
	 */
	protected boolean[] getDfltBoolAra(boolean is3D) {return new boolean[]{is3D,false,is3D,true};}
	
	/**
	 * call once for each display window before calling constructor. Sets essential values describing windows
	 * @param _winIdx The index in the various window-descriptor arrays for the dispWindow being set
	 * @param _title string title of this window
	 * @param _descr string description of this window
	 * @param _dispFlags Essential flags describing the nature of the dispWindow for idxs : 
	 * 		0 : dispWinIs3d, 
	 * 		1 : canDrawInWin; 
	 * 		2 : canShow3dbox (only supported for 3D); 
	 * 		3 : canMoveView
	 * @param _floatVals an array holding float arrays for 
	 * 				rectDimOpen(idx 0),
	 * 				rectDimClosed(idx 1),
	 * 				initCameraVals(idx 2)
	 * @param _intClrVals and array holding int arrays for
	 * 				winFillClr (idx 0),
	 * 				winStrkClr (idx 1),
	 * 				winTrajFillClr(idx 2),
	 * 				winTrajStrkClr(idx 3),
	 * 				rtSideFillClr(idx 4),
	 * 				rtSideStrkClr(idx 5)
	 * @param _sceneCenterVal center of scene, for drawing objects
	 * @param _initSceneFocusVal initial focus target for camera
	 */
	protected final void setInitDispWinVals(int _winIdx, String _title, String _descr, boolean[] _dispFlags,  
			float[][] _floatVals, int[][] _intClrVals, myPointf _sceneCenterVal, myVectorf _initSceneFocusVal) {
		winInitVals[_winIdx] = buildGUI_AppWinVals(_winIdx, _title, _descr, _dispFlags, 
				_floatVals, _intClrVals, _sceneCenterVal, _initSceneFocusVal);
	
	}//setInitDispWinVals
	/**
	 * Build a GUI_AppWinVals structure for the passed values. 	 
	 * @param _winIdx The index in the various window-descriptor arrays for the dispWindow being set
	 * @param _title string title of this window
	 * @param _descr string description of this window
	 * @param _dispFlags Essential flags describing the nature of the dispWindow for idxs : 
	 * 		0 : dispWinIs3d, 
	 * 		1 : canDrawInWin; 
	 * 		2 : canShow3dbox (only supported for 3D); 
	 * 		3 : canMoveView
	 * @param _floatVals an array holding float arrays for 
	 * 				rectDimOpen(idx 0),
	 * 				rectDimClosed(idx 1),
	 * 				initCameraVals(idx 2)
	 * @param _intClrVals and array holding int arrays for
	 * 				winFillClr (idx 0),
	 * 				winStrkClr (idx 1),
	 * 				winTrajFillClr(idx 2),
	 * 				winTrajStrkClr(idx 3),
	 * 				rtSideFillClr(idx 4),
	 * 				rtSideStrkClr(idx 5)
	 * @param _sceneCenterVal center of scene, for drawing objects
	 * @param _initSceneFocusVal initial focus target for camera
	 * @return the constructed GUI_AppWinVals
	 */
	public final GUI_AppWinVals buildGUI_AppWinVals(int _winIdx, String _title, String _descr, boolean[] _dispFlags,  
			float[][] _floatVals, int[][] _intClrVals, myPointf _sceneCenterVal, myVectorf _initSceneFocusVal) {
		return new GUI_AppWinVals(_winIdx, new String[] {_title, _descr}, _dispFlags,
				_floatVals, _intClrVals, _sceneCenterVal, _initSceneFocusVal); 
	}//buildGUI_AppWinVals
	/**
	 * call once for each display window before calling constructor. Sets essential values describing windows
	 * @param _winIdx The index in the various window-descriptor arrays for the dispWindow being set
	 * @param _title string title of this window
	 * @param _descr string description of this window
	 * @param _dispFlags Essential flags describing the nature of the dispWindow for idxs : 
	 * 		0 : dispWinIs3d, 
	 * 		1 : canDrawInWin; 
	 * 		2 : canShow3dbox (only supported for 3D); 
	 * 		3 : canMoveView
	 * @param _floatVals an array holding float arrays for 
	 * 				rectDimOpen(idx 0),
	 * 				rectDimClosed(idx 1),
	 * 				initCameraVals(idx 2)
	 * @param _intClrVals and array holding int arrays for
	 * 				winFillClr (idx 0),
	 * 				winStrkClr (idx 1),
	 * 				winTrajFillClr(idx 2),
	 * 				winTrajStrkClr(idx 3),
	 * 				rtSideFillClr(idx 4),
	 * 				rtSideStrkClr(idx 5)
	 */
	protected final void setInitDispWinVals(int _winIdx, String _title, String _descr, boolean[] _dispFlags,  
			float[][] _floatVals, int[][] _intClrVals) {
		winInitVals[_winIdx] = buildGUI_AppWinVals(_winIdx, _title, _descr, _dispFlags, _floatVals, _intClrVals);	
	}//setInitDispWinVals
	
	/**
	 * Build a GUI_AppWinVals structure for the passed values. 	 
	 * @param _winIdx The index in the various window-descriptor arrays for the dispWindow being set
	 * @param _title string title of this window
	 * @param _descr string description of this window
	 * @param _dispFlags Essential flags describing the nature of the dispWindow for idxs : 
	 * 		0 : dispWinIs3d, 
	 * 		1 : canDrawInWin; 
	 * 		2 : canShow3dbox (only supported for 3D); 
	 * 		3 : canMoveView
	 * @param _floatVals an array holding float arrays for 
	 * 				rectDimOpen(idx 0),
	 * 				rectDimClosed(idx 1),
	 * 				initCameraVals(idx 2)
	 * @param _intClrVals and array holding int arrays for
	 * 				winFillClr (idx 0),
	 * 				winStrkClr (idx 1),
	 * 				winTrajFillClr(idx 2),
	 * 				winTrajStrkClr(idx 3),
	 * 				rtSideFillClr(idx 4),
	 * 				rtSideStrkClr(idx 5)
	 * @return
	 */
	public final GUI_AppWinVals buildGUI_AppWinVals(int _winIdx, String _title, String _descr, boolean[] _dispFlags,  
			float[][] _floatVals, int[][] _intClrVals) {
		int scIdx = _dispFlags[0] ? 1 : 0;//whether or not is 3d
		return new GUI_AppWinVals(_winIdx, new String[] {_title, _descr}, _dispFlags,
				_floatVals, _intClrVals, _sceneOriginValsAra[scIdx], _sceneFcsValsAra[scIdx]); 
	}//buildGUI_AppWinVals
	
	/**
	 * This will build a 6 element array of color int arrays, based on the integer tags provided.
	 * @param _tags IRenderInterface constants reflecting specific colors
	 * 				winFillClr (idx 0),
	 * 				winStrkClr (idx 1),
	 * 				winTrajFillClr(idx 2),
	 * 				winTrajStrkClr(idx 3),
	 * 				rtSideFillClr(idx 4), (if not provided defaults to {0,0,0,200})
	 * 				rtSideStrkClr(idx 5)  (if not provided defaults to {255,255,255,255})
	 * @return
	 */
	protected int[][] buildWinColorArray(int[] _tags){
		int[][] res = new int[6][];		
		res[4] = new int[]{0,0,0,200};
		res[5] = new int[]{255,255,255,255};
		int minLen = res.length > _tags.length ? _tags.length : res.length;
		for(int i=0; i<minLen;++i) {res[i] = ri.getClr(_tags[i], 255);}	
		return res;
	}
	
	/**
	 * Get a reasonable initial camera setting, for windows with camera movement
	 * @return
	 */
	public float[] getInitCameraValues() {
		return new float[] {-0.06f*MyMathUtils.TWO_PI_F, -0.04f*MyMathUtils.TWO_PI_F, -200.0f};
	}
	
	/**
	 * Retrive the window that is currently in focus
	 * @return
	 */
	public final Base_DispWindow getCurFocusDispWindow() {return _dispWinFrames[_curFocusWin];}	
	
	/**
	 * Retrieve the window specified by the passed index
	 * @param winIdx
	 * @return
	 */
	public final Base_DispWindow getDispWindow(int winIdx) {
		if((winIdx<0) || (winIdx >= numDispWins)) {
			msgObj.dispConsoleErrorMessage("GUI_AppManager", "getDispWindow" , "Attempting to retrieve window idx :"+winIdx+" which is out of the range of allowed windows : [0,"+numDispWins+") so retrieving the window that currently has focus.");
			return _dispWinFrames[_curFocusWin];
		}
		return _dispWinFrames[winIdx];
	}
	
	/**
	 * Set the passed window
	 * @param wIdx
	 * @param newWindow
	 */
	public final void setDispWindow(int wIdx, Base_DispWindow newWindow) {	_dispWinFrames[wIdx] = newWindow;}
		
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// side bar menu stuff	
	
	/**
	 * clear menu side bar buttons when window-specific processing is finished
	 * @param _type
	 * @param col
	 * @param isSlowProc means original calling process lasted longer than mouse click release and so button state should be forced to be off
	 */
	public final void clearBtnState(int _type, int col, boolean isSlowProc) {
		int row = _type;
		sideBarMenu.getGuiBtnWaitForProc()[row][col] = false;
		if(isSlowProc) {sideBarMenu.getGuiBtnSt()[row][col] = 0;}		
	}//clearBtnState 
		
	/**
	 * Get a list of the maximum click coordinates across every window
	 * @return
	 */
	protected float[] getMaxUIClkCoords() {
		float[] res = new float[] {0.0f,0.0f,0.0f,0.0f}, tmpCoords;
		for (int winIDX : winDispIdxXOR) {
			tmpCoords = _dispWinFrames[winIDX].getUIClkCoords();
			for(int i=0;i<tmpCoords.length;++i) {
				if(res[i]<tmpCoords[i]) {res[i]=tmpCoords[i];}
			}
		}
		return res;
	}//getMaxUIClkCoords
	
	/**
	 * only send names of function and debug btns (if they exist) in 2d array
	 * @param btnNames
	 */
	public final void setAllMenuBtnNames(String[][] btnNames) {
		for(int _type = 0;_type<btnNames.length;++_type) {sideBarMenu.setAllFuncBtnLabels(_type,btnNames[_type]);}
	}
		
	/**
	 * Handle showing a particular window from a menu button click
	 * @param btn which button has been selected
	 * @param val whether button is turned on or off
	 */
	public final void handleShowWinFromMenuClick(int btn, int val){_handleShowWinInternal(btn, val, true);}					//display specific windows - multi-select/ always on if sel
	/**
	 * these tie using the UI buttons to modify the window in with using the boolean tags - PITA but currently necessary
	 * @param btn
	 * @param val
	 * @param callFlags whether turned on/off by button click, or via programatically being set 
	 */
	private final void _handleShowWinInternal(int btn, int val, boolean callFlags) {
		if(!callFlags){//called from setflags - only sets button state in UI to avoid infinite loop
			setMenuBtnState(SidebarMenu.btnShowWinIdx,btn, val);
		} else {//called from clicking on window buttons in UI
			//val is btn state before transition 
			boolean bVal = (val == 1?  false : true);
			//each entry in this array should correspond to a clickable window
			setWinVisFlag(winFlagsXOR[btn], bVal);
		}
	}
	
	/**
	 * process to handle file io	- TODO	
	 * @param _type
	 * @param btn
	 * @param val
	 */
	public final void handleFileCmd(int _type, int btn, int val){handleFileCmd(_type,btn, val, true);}					//display specific windows - multi-select/ always on if sel
	/**
	 * process to handle file io	- TODO
	 * @param _type
	 * @param btn
	 * @param val
	 * @param callFlags
	 */
	public final void handleFileCmd(int _type, int btn, int val, boolean callFlags){//{"Load","Save"},							//load an existing score, save an existing score - momentary	
		if(!callFlags){			setMenuBtnState(_type,btn, val);		}  else {
			switch(btn){
				case 0 : {
					//processing provided custom selectInput and selectOutput windows that had call 
					//back functionality.  this cannot be delineated in an interface, so this needs to be coded separately
					msgObj.dispConsoleErrorMessage("GUI_AppManager", "handleFileCmd", "Selecting input not implemented currently.");
					break;}
				case 1 : {
					msgObj.dispConsoleErrorMessage("GUI_AppManager", "handleFileCmd", "Selecting Output not implemented currently.");
					break;}
			}
			getSideBarMenuWindow().hndlMouseRel_Indiv();
		}
	}//handleFileCmd
	
	/**
	 * display specific windows - multi-select/ always on if sel
	 * @param row
	 * @param funcOffset
	 * @param col
	 * @param val
	 */
	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val){handleMenuBtnSelCmp(row, funcOffset, col, val, true);}					
	/**
	 * display specific windows - multi-select/ always on if sel
	 * @param row
	 * @param funcOffset
	 * @param col
	 * @param val
	 * @param callFlags
	 */
	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val, boolean callFlags){
		if(!callFlags){			setMenuBtnState(row,col, val);		} //if called programmatically, not via ui action
		else {					_dispWinFrames[_curFocusWin].clickSideMenuBtn(row, funcOffset, col);		}
	}//handleAddDelSelCmp		
		
	/**
	 * pass on to current display window the choice for mouse over display data
	 * @param btn
	 * @param val
	 */
	public final void handleMenuBtnMseOvDispSel(int btn,boolean val) {
		_dispWinFrames[_curFocusWin].handleSideMenuMseOvrDispSel(btn, val);
	}
	
	/**
	 * pass on to current display window the choice for debug selection - application should manage debug button state
	 * @param btn
	 * @param val
	 */
	public final void handleMenuBtnDebugSel(int btn,int val) {
		//set current window's debug functions based on selection in sidebar debug button menu
		_dispWinFrames[_curFocusWin].setThisWinMenuBtnDbgState(btn, val);	
	}	
	
	protected void setMenuBtnState(int row, int col, int val) {
		sideBarMenu.getGuiBtnSt()[row][col] = val;	
		if (val == 1) {
			sideBarMenu.setWaitForProc(row,col);//if programmatically (not through UI) setting button on, then set wait for proc value true
		}
	}//setMenuBtnState	
	
	public final void loadFromFile(File file){
		if (file == null) {
			msgObj.dispConsoleWarningMessage("GUI_AppManager", "loadFromFile", "Load was cancelled.");
		    return;
		} 		
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		_dispWinFrames[_curFocusWin].loadFromFile(file);
	
	}//loadFromFile
	
	public final void saveToFile(File file){
		if (file == null) {
			msgObj.dispConsoleWarningMessage("GUI_AppManager", "saveToFile", "Save was cancelled.");
		    return;
		} 
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		_dispWinFrames[_curFocusWin].saveToFile(file);
	}//saveToFile	
	

	public final String getAnimPicName() {
		//if(!flags[this.runSim]) {return;}//don't save until actually running simulation
		//idx 0 is directory, idx 1 is file name prefix
		String[] ssName = _dispWinFrames[_curFocusWin].getSaveFileDirName();
		if(ssName.length != 2) {setBaseFlag(saveAnim, false);return null;}
		//save(screenShotPath + prjNmShrt + ((animCounter < 10) ? "0000" : ((animCounter < 100) ? "000" : ((animCounter < 1000) ? "00" : ((animCounter < 10000) ? "0" : "")))) + animCounter + ".jpg");		
		String saveDirAndSubDir = ssName[0] + //"run_"+String.format("%02d", runCounter)  + 
				ssName[1] + File.separatorChar;	
		return saveDirAndSubDir + String.format("%06d", animCounter++) + ".jpg";		
	}

		
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// draw/display functions
	/**
	 * Draw background
	 * @param winIdx
	 */
	private final void drawBackground(int winIdx) {
		if(_useSkyboxBKGndAra[winIdx]) {	ri.drawBkgndSphere(winIdx);} 
		else {								ri.drawRenderBackground(winIdx);}
	}

	
	public boolean getShouldClearBKG() {return getBaseFlag(clearBKG);}
	
	/**
	 * return CWD of this application
	 * @return
	 */
	public String getApplicationPath() {
		String res = System.getProperty("user.dir");
		return res;
	}

	/**
	 * get difference between frames and set both glbl times
	 * @return
	 */
	private float getModAmtMillis() {
		_glblStartSimFrameTime = timeMgr.getMillisFromProgStart();
		float modAmtMillis = (_glblStartSimFrameTime - _glblLastSimFrameTime);
		_glblLastSimFrameTime = _glblStartSimFrameTime;
		return modAmtMillis;
	}
		
	/**
	 * primary sim and draw loop.  Called from draw in IRenderInterface class
	 */
	public final boolean mainSimAndDrawLoop() {
		//Finish final init if not done already
		if(!isFinalInitDone()) {initOnce(); return false;}	
		float modAmtMillis = getModAmtMillis();
		//simulation section
		execSimDuringDrawLoop(modAmtMillis);
		//drawing section																//initialize camera, lights and scene orientation and set up eye movement
		drawMainWinAndCanvas(modAmtMillis);		     									//draw UI overlay on top of rendered results			
		//set window title
		ri.setWindowTitle(getProjAndFrapsString());
		//Update timer mark for mem query update
		_memChkLastTimerMark = _glblStartSimFrameTime/_memChkMillisTimer;
		
		return true;
	}//mainSimAndDrawLoop
	
	/**
	 * Amount to divide memory query by to display in MBs
	 */
	private final float memDiv = 1024.0f * 1024.0f;
	protected final String buildMemString(boolean updateMem) {
		// Current Memory status - only update occasionally
		if (updateMem) {	currMemMap = getMemoryStatusMap();		}
		String memStatusStr = "Memory : ";
		for(int i=0;i< currMemMap.length; ++i) { 
			memStatusStr +=  memDispTextAbbrev[i]+": " + String.format("%7.2f", currMemMap[i]/memDiv)+" MB | ";
		}	
		return memStatusStr;
	}
	protected final String sep = "  |  ";
	protected final String getProjAndFrapsString() {
		return getPrjNmLong() + sep + "Frames : " + String.format("%4.1f",ri.getFrameRate()) + " fps";
	}
	
	protected final String getStatusBarString(boolean updateMem) {
		return getProjAndFrapsString() +sep + buildMemString(updateMem);
//		// Memory status if title not too long
//		if(title.length()<130){			title +=buildMemString(updateMem); } // about 111 long
//		return title;
	}// getStatusBarString
	 
	/**
	 * sim loop, called from IRenderInterface draw method
	 * @param modAmtMillis milliseconds since last frame started
	 */
	protected boolean execSimDuringDrawLoop(float modAmtMillis) {
		//simulation section
		if(isRunSim() ){
			//run simulation
			for(int i = 1; i<numDispWins; ++i){if((isShowingWindow(i)) && (_dispWinFrames[i].getIsRunnable())){_dispWinFrames[i].simulate(modAmtMillis);}}
			if(isSingleStep()){setSimIsRunning(false);}
			return true;
		}		//play in current window
		return false;
	}//execSimDuringDrawLoop
	
	/**
	 * setup for draw
	 */
	private void _drawSetup(){
		ri.setPerspective(MyMathUtils.THIRD_PI_F, _aspectRatio, .5f, _camVals[2]*100.0f);
		ri.enableLights(); 	
		_dispWinFrames[_curFocusWin].drawSetupWin(_camVals);
	}//drawSetup
	
	/**
	 * main draw loop
	 @param modAmtMillis milliseconds since last frame started
	 */
	private final void drawMainWinAndCanvas(float modAmtMillis){
		ri.pushMatState();
		_drawSetup();
		boolean is3DDraw = (_curFocusWin == -1) || (curDispWinIs3D()); 
		if(is3DDraw){	//allow for single window to have focus, but display multiple windows	
			//if refreshing screen, this clears screen, sets background
			if(getShouldClearBKG()) {
				drawBackground(_curFocusWin);				
				draw3D(modAmtMillis);
				if(curDispWinCanShow3dbox()){drawBoxBnds();}
				if(_dispWinFrames[_curFocusWin].chkDrawMseRet()){			_canvas.drawMseEdge(_dispWinFrames[_curFocusWin], is3DDraw);	}		
				if(doShowDrawawbleCanvas()) {ri.drawCanvas(getEyeToMse(), getCanvasDrawPlanePts(), winInitVals[_curFocusWin].canvasColor);}
			} else {
				draw3D(modAmtMillis);
			}
			ri.popMatState(); 
		} else {	//either/or 2d window
			//2d windows paint window box so background is always cleared
			_canvas.buildCanvas();
			_canvas.drawMseEdge(_dispWinFrames[_curFocusWin], is3DDraw);
			ri.popMatState(); 
			draw2D(modAmtMillis);
		}
		drawMePost_Indiv(modAmtMillis, is3DDraw);
		//Draw UI and on-screen elements
		drawUI(modAmtMillis);
	}//draw	
	
	
	/**
	 * Individual extending Application Manager post-drawMe functions
	 * @param modAmtMillis milliseconds since last frame started
	 * @param is3DDraw
	 */
	protected abstract void drawMePost_Indiv(float modAmtMillis, boolean is3DDraw);

	/**
	 * Draw 3d windows that are currently displayed
	 * @param modAmtMillis milliseconds since last frame started
	 */
	private final void draw3D(float modAmtMillis){
		for(int i =1; i<numDispWins; ++i){
			if((isShowingWindow(i)) && (_dispWinFrames[i].getIs3DWindow())){	_dispWinFrames[i].draw3D(modAmtMillis);}
		}
		//fixed xyz rgb axes for visualisation purposes and to show movement and location in otherwise empty scene
		drawAxes(100,3, new myPoint(-_viewWidth/2.0f+40,0.0f,0.0f), 200, false); 
		//build target _canvas
		_canvas.buildCanvas();
	}//draw3D
	
	/**
	 * Draw 2d windows that are currently displayed but not sidebar menu, which is drawn via drawUI()
	 * @param modAmtMillis milliseconds since last frame started
	 */	
	private final void draw2D(float modAmtMillis) {
		for(int i =1; i<numDispWins; ++i){if (isShowingWindow(i) && !(_dispWinFrames[i].getIs3DWindow())){_dispWinFrames[i].draw2D(modAmtMillis);}}
	}
	
	/**
	 * Draw UI components on screen surface
	 * @param modAmtMillis milliseconds since last frame started
	 */
	private final void drawUI(float modAmtMillis){	
		boolean shouldDrawOnscreenText = (isDebugMode() || showInfo);
		for(int i =1; i<numDispWins; ++i){
			_dispWinFrames[i].drawHeader(
					sideBarMenu.getDebugData(),
					(shouldDrawOnscreenText && (i==_curFocusWin)), 
					isDebugMode(), 
					modAmtMillis);}
		sideBarMenu.draw2D(modAmtMillis);
		sideBarMenu.drawHeader(new String[0], false, isDebugMode(), modAmtMillis);
		_dispWinFrames[_curFocusWin].updateConsoleStrs();	
		//build and set statusbar if should be used
		if(doShowStatusBar()) {
			drawWindowStatusBar(getStatusBarString(_memChkLastTimerMark != _glblStartSimFrameTime/_memChkMillisTimer));
		}		
	}//drawUI
	
	private final void drawWindowStatusBar(String statusBarString) { 
		ri.pushMatState();
		ri.setBeginNoDepthTest();
		ri.disableLights();
		// draw status bar
		ri.setFill(255,255,255,255);
		ri.setStrokeWt(1.0f);
		ri.setStroke(0,0,0,255);
		ri.translate(0, _viewHeight);
		ri.drawRect(0, -getTextHeightOffset(), _viewWidth, getTextHeightOffset());		
		ri.setColorValFill(IRenderInterface.gui_Black, 255);ri.setColorValStroke(IRenderInterface.gui_Black, 255);
		ri.showText(statusBarString, getRowStYOffset(), -getRowStYOffset(),0); 
		ri.enableLights();
		ri.setEndNoDepthTest();
		ri.popMatState();
	}
	
	/**
	 * draw bounding box for 3d
	 */
	private final void drawBoxBnds(){
		ri.pushMatState();
		ri.setStrokeWt(3f);
		ri.noFill();
		ri.setColorValStroke(IRenderInterface.gui_TransGray,255);		
		ri.drawBox3D(_3DGridDimX,_3DGridDimY,_3DGridDimZ);
		ri.popMatState();
	}	
	
	/**
	 * project passed point onto box surface based on location - to help visualize the location in 3d
	 * @param p
	 */
	public final void drawProjOnBox(myPoint p){
		myPoint prjOnPlane;
		ri.pushMatState();
		ri.translate(-p.x,-p.y,-p.z);
		for(int i  = 0; i< 6; ++i){				
			prjOnPlane = bndChkInCntrdBox3D(MyMathUtils.intersectPlane(p, _boxNorms[i], _boxWallPts[i][0],_boxWallPts[i][1],_boxWallPts[i][2]));				
			ri.showPtAsSphere(prjOnPlane,5,5,IRenderInterface.rgbClrs[i/2],IRenderInterface.rgbClrs[i/2]);				
		}
		ri.popMatState();
	}//drawProjOnBox
	/**
	 * project passed point onto box surface based on location - to help visualize the location in 3d
	 * @param p
	 */
	public final void drawProjOnBox(myPointf p){
		myPointf prjOnPlane;
		ri.pushMatState();
		ri.translate(-p.x,-p.y,-p.z);
		for(int i  = 0; i< 6; ++i){				
			prjOnPlane = bndChkInCntrdBox3D(MyMathUtils.intersectPlane(p, _boxNorms[i], _boxWallPts[i][0],_boxWallPts[i][1],_boxWallPts[i][2]));				
			ri.showPtAsSphere(prjOnPlane,5,5,IRenderInterface.rgbClrs[i/2],IRenderInterface.rgbClrs[i/2]);				
		}
		ri.popMatState();
	}//drawProjOnBox
	/**
	 * display menu text based on menu state - moved from menu class
	 * @param xOffHalf
	 * @param yOffHalf
	 */
	private final int[] btnGreyClr = new int[]{180,180,180};
	public final void dispMenuText(float xOffHalf, float yOffHalf) {
		for(int idx =0; idx<_numFlagsToShow; ++idx){
			int i = _flagsToShow.get(idx);
			if(getBaseFlag(i) ){		dispMenuTxtLat(_truePFlagNames[i], _trueFlagColors[i], true, xOffHalf,yOffHalf);}
			else {						dispMenuTxtLat(_falsePFlagNames[i], btnGreyClr, false, xOffHalf,yOffHalf);}					
		}
	}//dispMenuText
	
	/**
	 * draw a series of strings in a column for menu text
	 * @param txt string to draw
	 * @param clrAra color to set text to
	 * @param showSphere put a marking sphere next to text
	 * @param xOff x-dim offset
	 * @param yOff y-dim offset
	 */
	public final void dispMenuTxtLat(String txt, int[] clrAra, boolean showSphere, float xOff, float yOff){
		ri.setFill(clrAra, 255); 
		ri.translate(xOff,yOff);
		if(showSphere){ri.setStroke(clrAra, 255);		ri.drawSphere(5);	} 
		else {	ri.noStroke();		}
		ri.translate(-xOff,yOff);
		ri.showText(""+txt,2.0f*xOff,-yOff*.5f);	
	}

	/**
	 * draw state booleans at top of screen and their state
	 */
	public final void drawSideBarStateLights(float yOff){ _appStateFlags.drawSideBarStateLights(ri, yOff);}
	
	/**
	 * called by sidebar menu to display current window's UI components
	 */
	public final void drawWindowGuiObjs(float animTimeMod){
		if(_curFocusWin != -1){
			ri.pushMatState();
			_dispWinFrames[_curFocusWin].drawWindowGuiObjs(isDebugMode(), animTimeMod);					//draw what user-modifiable fields are currently available
			ri.popMatState();	
		}
	}//
	
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param alpha alpha value for how dark/faint axes should be
	 * @param centered whether axis should be centered at ctr or just in positive direction at ctr
	 */
	public final void drawAxes(double len, float stW, myPoint ctr, int alpha, boolean centered){//axes using current global orientation
		ri.pushMatState();
			ri.setStrokeWt(stW);
			ri.setStroke(255,0,0,alpha);
			if(centered){
				double off = len*.5f;
				ri.drawLine(ctr.x-off,ctr.y,ctr.z,ctr.x+off,ctr.y,ctr.z);ri.setStroke(0,255,0,alpha);ri.drawLine(ctr.x,ctr.y-off,ctr.z,ctr.x,ctr.y+off,ctr.z);ri.setStroke(0,0,255,alpha);ri.drawLine(ctr.x,ctr.y,ctr.z-off,ctr.x,ctr.y,ctr.z+off);} 
			else {		ri.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+len,ctr.y,ctr.z);ri.setStroke(0,255,0,alpha);ri.drawLine(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y+len,ctr.z);ri.setStroke(0,0,255,alpha);ri.drawLine(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y,ctr.z+len);}
		ri.popMatState();	
	}//	drawAxes
	public final void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int alpha, boolean drawVerts){//RGB -> XYZ axes
		ri.pushMatState();
		if(drawVerts){
			ri.showPtAsSphere(ctr,3,5,IRenderInterface.gui_Black,IRenderInterface.gui_Black);
			for(int i=0;i<_axis.length;++i){ri.showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),3,5,IRenderInterface.rgbClrs[i],IRenderInterface.rgbClrs[i]);}
		}
		ri.setStrokeWt(stW);
		for(int i=0; i<3;++i){	ri.setColorValStroke(IRenderInterface.rgbClrs[i],255);	ri.showVec(ctr,len, _axis[i]);	}
		ri.popMatState();	
	}//	drawAxes
	public final void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int[] clr, boolean drawVerts){//all axes same color
		ri.pushMatState();
			if(drawVerts){
				ri.showPtAsSphere(ctr,2,5,IRenderInterface.gui_Black,IRenderInterface.gui_Black);
				for(int i=0;i<_axis.length;++i){ri.showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),2,5,IRenderInterface.rgbClrs[i],IRenderInterface.rgbClrs[i]);}
			}
			ri.setStrokeWt(stW);ri.setStroke(clr[0],clr[1],clr[2],clr[3]);
			for(int i=0; i<3;++i){	ri.showVec(ctr,len, _axis[i]);	}
		ri.popMatState();	
	}//	drawAxes
	
	public final void drawAxes(double len, double stW, myPoint ctr, myVectorf[] _axis, int alpha){
		ri.pushMatState();
			ri.setStrokeWt((float)stW);
			ri.setStroke(255,0,0,alpha);
			ri.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[0].x)*len,ctr.y+(_axis[0].y)*len,ctr.z+(_axis[0].z)*len);
			ri.setStroke(0,255,0,alpha);
			ri.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[1].x)*len,ctr.y+(_axis[1].y)*len,ctr.z+(_axis[1].z)*len);	
			ri.setStroke(0,0,255,alpha);	
			ri.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[2].x)*len,ctr.y+(_axis[2].y)*len,ctr.z+(_axis[2].z)*len);
		ri.popMatState();	
	}//	drawAxes
	

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// _canvas functions
	
	public final myVector getDrawSNorm() {return _canvas.getDrawSNorm();}
	public final myVectorf getDrawSNorm_f() {return _canvas.getDrawSNorm_f();}
	public final myVector getEyeToMse() {return _canvas.getEyeToMse();}
	public final myVectorf getEyeToMse_f() {return _canvas.getEyeToMse_f();}

	public final myVector getUScrUpInWorld(){			myVector res = new myVector(ri.getWorldLoc(_viewWidthHalf, _viewHeightHalf,-.00001f),ri.getWorldLoc(_viewWidthHalf, _viewHeight,-.00001f));		return res._normalize();}	
	public final myVector getUScrRightInWorld(){		myVector res = new myVector(ri.getWorldLoc(_viewWidthHalf, _viewHeightHalf,-.00001f),ri.getWorldLoc(_viewWidth, _viewHeightHalf,-.00001f));		return res._normalize();}
	public final myVectorf getUScrUpInWorldf(){		myVectorf res = new myVectorf(ri.getWorldLoc(_viewWidthHalf, _viewHeightHalf,-.00001f),ri.getWorldLoc(_viewWidthHalf,_viewHeight,-.00001f));	return res._normalize();}	
	public final myVectorf getUScrRightInWorldf(){	myVectorf res = new myVectorf(ri.getWorldLoc(_viewWidthHalf, _viewHeightHalf,-.00001f),ri.getWorldLoc(_viewWidth, _viewHeightHalf,-.00001f));	return res._normalize();}
	public final myPoint getEyeLoc(){return ri.getWorldLoc(_viewWidthHalf, _viewHeightHalf,-.00001f);	}
	
	public final myPoint getMseLoc(){			return _canvas.getMseLoc();}
	public final myPointf getMseLoc_f(){		return _canvas.getMseLoc_f();}
	public final myPoint getOldMseLoc(){		return _canvas.getOldMseLoc();}	
	public final myVector getMseDragVec(){	return _canvas.getMseDragVec();}
	/**
	 * return a unit vector from the screen location of the mouse pointer in the world to the reticle location in the world - for ray casting onto objects the mouse is over
	 * @param glbTrans
	 * @return
	 */
	public final myVector getMse2DtoMse3DinWorld(myPoint glbTrans){
		int[] mse = ri.getMouse_Raw_Int();
		myVector res = new myVector(ri.getWorldLoc(mse[0], mse[1],-.00001f),getMseLoc(glbTrans) );		
		return res._normalize();
	}
	/**
	 * relative to passed origin
	 * @param glbTrans
	 * @return
	 */
	public final myPoint getMseLoc(myPoint glbTrans){			return _canvas.getMseLoc(glbTrans);	}
	/**
	 * relative to passed origin as float point
	 * @param glbTrans
	 * @return
	 */
	public final myPoint getMseLoc(myPointf glbTrans){			return _canvas.getMseLoc(new myPoint(glbTrans.x, glbTrans.y, glbTrans.z));	}
	/**
	 * move by passed translation
	 * @param glbTrans
	 * @return
	 */
	public final myPointf getTransMseLoc(myPointf glbTrans){	return _canvas.getTransMseLoc(glbTrans);	}
	/**
	 * dist from mouse to passed location
	 * @param glbTrans
	 * @return
	 */
	public final float getMseDist(myPointf glbTrans){			return _canvas.getMseDist(glbTrans);}
	public final myPoint getOldMseLoc(myPoint glbTrans){		return _canvas.getOldMseLoc(glbTrans);}
	
	/**
	 * get normalized ray from eye loc to mouse loc
	 * @return
	 */
	public final myVectorf getEyeToMouseRay_f() {				return _canvas.getEyeToMouseRay_f();	}	
	
	/**
	 * return display string holding sreen and world mouse and eye locations 
	 */
	public final String getMseEyeInfoString(String winCamDisp) {
		myPoint mseLocPt = ri.getMouse_Raw();
		return "mse loc on screen : " + mseLocPt + " mse loc in world :"+ _canvas.getMseLoc() +"  Eye loc in world :"+ _canvas.getEyeInWorld()+ winCamDisp;
	}
	
	/**
	 * Get label of sidebar menu button specified by row and column
	 * @param row
	 * @param col
	 * @return
	 */
	public final String getSidebarMenuButtonLabel(int row, int col) {
		return sideBarMenu.getSidebarMenuButtonLabel(row,col);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// showing functions
	/**
	 * This will properly format and display a string of text in white, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_White(String txt) {showOffsetText_RightSideMenu(255,255,255,255,txt);}
	/**
	 * This will properly format and display a string of text in black, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors. NOTE: generally the right side menu that
	 * uses this text has a black background. Be warned.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Black(String txt) {showOffsetText_RightSideMenu(0,0,0,255,txt);}
	/**
	 * This will properly format and display a string of text in white, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Gray(String txt) {showOffsetText_RightSideMenu(165,165,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Red, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Red(String txt) {showOffsetText_RightSideMenu(255,0,0,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Green(String txt) {showOffsetText_RightSideMenu(0,255,0,255,txt);}
		
	/**
	 * This will properly format and display a string of text in Blue, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Blue(String txt) {showOffsetText_RightSideMenu(0,0,255,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Cyan, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Cyan(String txt) {showOffsetText_RightSideMenu(0,255,255,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Magenta, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Magenta(String txt) {showOffsetText_RightSideMenu(255,0,255,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Yellow, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Yellow(String txt) {showOffsetText_RightSideMenu(255,255,0,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Orange, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Orange(String txt) {showOffsetText_RightSideMenu(255,165,0,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Pink, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Pink(String txt) {showOffsetText_RightSideMenu(255,0,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Aqua, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Aqua(String txt) {showOffsetText_RightSideMenu(0,165,255,255,txt);}
		
	/**
	 * This will properly format and display a string of text in Violet, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Violet(String txt) {showOffsetText_RightSideMenu(165,0,255,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Lime green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Lime(String txt) {showOffsetText_RightSideMenu(165,255,0,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Seafoam green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_Seafoam(String txt) {showOffsetText_RightSideMenu(0,255,165,255,txt);}

	////////////////////////////////////
	/// Light colors
	
	/**
	 * This will properly format and display a string of text in white, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightGray(String txt) {showOffsetText_RightSideMenu(210,210,210,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Red, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightRed(String txt) {showOffsetText_RightSideMenu(255,165,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightGreen(String txt) {showOffsetText_RightSideMenu(165,255,165,255,txt);}
		
	/**
	 * This will properly format and display a string of text in Blue, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightBlue(String txt) {showOffsetText_RightSideMenu(165,165,255,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Cyan, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightCyan(String txt) {showOffsetText_RightSideMenu(165,255,255,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Magenta, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightMagenta(String txt) {showOffsetText_RightSideMenu(255,165,255,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Yellow, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightYellow(String txt) {showOffsetText_RightSideMenu(255,255,165,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Orange, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightOrange(String txt) {showOffsetText_RightSideMenu(255,210,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Pink, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightPink(String txt) {showOffsetText_RightSideMenu(255,165,210,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Aqua, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightAqua(String txt) {showOffsetText_RightSideMenu(165,210,255,255,txt);}
		
	/**
	 * This will properly format and display a string of text in Violet, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightViolet(String txt) {showOffsetText_RightSideMenu(210,165,255,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Lime green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightLime(String txt) {showOffsetText_RightSideMenu(210,255,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Seafoam green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_LightSeafoam(String txt) {showOffsetText_RightSideMenu(165,255,210,255,txt);}


	////////////////////////////////////
	/// Dark colors
	
	/**
	 * This will properly format and display a string of text in white, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkGray(String txt) {showOffsetText_RightSideMenu(90,90,90,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Red, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkRed(String txt) {showOffsetText_RightSideMenu(165,0,0,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkGreen(String txt) {showOffsetText_RightSideMenu(0,165,0,255,txt);}
		
	/**
	 * This will properly format and display a string of text in Blue, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkBlue(String txt) {showOffsetText_RightSideMenu(0,0,165,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Cyan, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkCyan(String txt) {showOffsetText_RightSideMenu(0,165,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Magenta, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkMagenta(String txt) {showOffsetText_RightSideMenu(165,0,165,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Yellow, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkYellow(String txt) {showOffsetText_RightSideMenu(165,165,0,255,txt);}	
	
	/**
	 * This will properly format and display a string of text in Orange, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkOrange(String txt) {showOffsetText_RightSideMenu(165,90,0,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Pink, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkPink(String txt) {showOffsetText_RightSideMenu(165,0,90,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Aqua, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkAqua(String txt) {showOffsetText_RightSideMenu(0,90,165,255,txt);}
		
	/**
	 * This will properly format and display a string of text in Violet, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkViolet(String txt) {showOffsetText_RightSideMenu(90,0,165,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Lime green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkLime(String txt) {showOffsetText_RightSideMenu(90,165,0,255,txt);}
	
	/**
	 * This will properly format and display a string of text in Seafoam green, and will 
	 * translate the width of the text, so multiple strings can be displayed on 
	 * the same line with different colors.
	 * 
	 * @param txt
	 */
	public final void showMenuTxt_DarkSeafoam(String txt) {showOffsetText_RightSideMenu(0,165,90,255,txt);}

		
	/**
	 * This will properly format and display a string of text in the passed color, and will translate the width,
	 * so multiple strings can be displayed on the same line with different colors
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param alpha alpha transparency
	 * @param txt text to display
	 */
	public final void showOffsetText_RightSideMenu(int r, int g, int b, int alpha, String txt) {
		ri.setFill(r,g,b,alpha);ri.setStroke(r,g,b,alpha);
		ri.showText(txt,0.0f,0.0f,0.0f);
		ri.translate(ri.getTextWidth(txt)*1.1f, 0.0f,0.0f);		
	}
	
	/**
	 * this will properly format and display a string of text in the passed color, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr 4 int array, with last value being 
	 * @param txt
	 */
	public final void showOffsetText_RightSideMenu(int[] tclr, String txt) {
		ri.setFill(tclr,tclr[3]);ri.setStroke(tclr,tclr[3]);
		ri.showText(txt,0.0f,0.0f,0.0f);
		ri.translate(ri.getTextWidth(txt)*1.1f, 0.0f,0.0f);		
	}
	/**
	 * this will properly format and display a string of text, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr color constant as defined in IRenderInterface
	 * @param txt
	 */
	public final void showOffsetText_RightSideMenu(int tclr, String txt) {
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt,0.0f,0.0f,0.0f);
		ri.translate(ri.getTextWidth(txt)*1.1f, 0.0f,0.0f);		
	}
	
	/**
	 * 
	 * @param d
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText(float d, int[] tclr, String txt){
		ri.setFill(tclr,tclr[3]);ri.setStroke(tclr,tclr[3]);
		ri.showText(txt, d, d,d); 
	}
	/**
	 * 
	 * @param loc
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText(myPointf loc, int[] tclr, String txt){
		ri.setFill(tclr,tclr[3]);ri.setStroke(tclr,tclr[3]);
		ri.showText(txt, loc.x, loc.y, loc.z); 
	}	
	/**
	 * 
	 * @param d
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText2D(float d, int[] tclr, String txt){
		ri.setFill(tclr,tclr[3]);ri.setStroke(tclr,tclr[3]);
		ri.showText(txt, d, d,0); 
	}	
	/**
	 * 
	 * @param d
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText(float d, int tclr, String txt){
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt, d, d,d); 
	}
	/**
	 * 
	 * @param loc
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText(myPointf loc, int tclr, String txt){
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt, loc.x, loc.y, loc.z); 
	}	
	/**
	 * 
	 * @param d
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText2D(float d, int tclr, String txt){
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt, d, d,0); 
	}
	/**
	 * 
	 * @param P
	 * @param rad
	 * @param det
	 * @param fclr
	 * @param strkclr
	 * @param tclr
	 * @param txt
	 */
	public final void showBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		ri.pushMatState();  
		ri.translate(P.x,P.y,P.z);
		ri.setColorValFill(IRenderInterface.gui_White,150);
		ri.setColorValStroke(IRenderInterface.gui_Black,255);
		ri.drawRect(new float[] {0,6.0f,txt.length()*7.8f,-15});
		tclr = IRenderInterface.gui_Black;		
		ri.setFill(fclr,255); ri.setStroke(strkclr,255);			
		ri.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	/**
	 * translate to point, draw point and text
	 * @param P
	 * @param rad
	 * @param det
	 * @param fclr
	 * @param strkclr
	 * @param tclr
	 * @param txt
	 */
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		ri.pushMatState();  
		ri.setFill(fclr,255); 
		ri.setStroke(strkclr,255);		
		ri.translate(P.x,P.y,P.z); 
		ri.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	/**
	 * textP is location of text relative to point
	 * @param P
	 * @param rad
	 * @param det
	 * @param fclr
	 * @param strkclr
	 * @param tclr
	 * @param txtP
	 * @param txt
	 */
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		ri.pushMatState();  
		ri.translate(P.x,P.y,P.z); 
		ri.setFill(fclr,255); 
		ri.setStroke(strkclr,255);			
		ri.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(txtP,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	/**
	 * textP is location of text relative to point
	 * @param P
	 * @param rad
	 * @param fclr
	 * @param strkclr
	 * @param tclr
	 * @param txtP
	 * @param txt
	 */
	public final void showCrclNoBox_ClrAra(myPointf P, float rad, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		ri.pushMatState();  
		ri.translate(P.x,P.y,P.z); 
		if((fclr!= null) && (strkclr!= null)){ri.setFill(fclr,255); ri.setStroke(strkclr,255);}		
		ri.drawEllipse2D(0,0,rad,rad); 
		ri.drawEllipse2D(0,0,2,2);
		showOffsetText(txtP,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	/**
	 * show sphere of certain radius
	 * @param P
	 * @param rad
	 * @param det
	 * @param fclr
	 * @param strkclr
	 */
	public final void show_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr) {
		ri.pushMatState();   
		if((fclr!= null) && (strkclr!= null)){ri.setFill(fclr,255); ri.setStroke(strkclr,255);}
		ri.drawSphere(P, rad, det);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	///////////////////////////////
	// end draw/display functions
	
	/**
	 * returns the width of the visible display in pxls
	 * @return
	 */
	public final int getDisplayWidth() {return _displayWidth;}
	/**
	 * returns the height of the visible display in pxls
	 * @return
	 */
	public final int getDisplayHeight() {return _displayHeight;}
	
	/**
	 * Retrieve the 3d grid box dimensions
	 * @return
	 */
	public final float[] get3dGridDims() {return new float[] {_3DGridDimX, _3DGridDimY, _3DGridDimZ};}
	
	/**
	 * Retrieve the 3d grid box dimensions scaled by some multiplicative factor 
	 * (for rendering to stay consistent if some measurements change)
	 * @return
	 */
	public final float[] getScaled3dGridDims(float _scl) {return new float[] {_3DGridDimX*_scl, _3DGridDimY*_scl, _3DGridDimZ*_scl};}
	
	/**
	 * Return the boundaries for the enclosing 3d cube
	 * @return
	 */
	public final float[][] get3dCubeBnds(){return _cubeBnds;}
	
	/**
	 * Returns window dims : X,Y,Width,Height
	 */
	public final int[] getWindowLoc() {
		return new int[] {window.getX(),window.getY(), window.getWidth(), window.getHeight()};
	}
	
	/**
	 * set the height of each window that is above the popup window, to move up or down when it changes size
	 * @param popUpWinIDX
	 */
	public final void setWinsHeight(int popUpWinIDX){
		//skip first window - ui menu
		for(int i=0;i<winDispIdxXOR.length;++i){		_dispWinFrames[winDispIdxXOR[i]].setRectDimsY( _dispWinFrames[popUpWinIDX].getRectDim(1));	}						
	}
	/**
	 * allow only 1 window to display
	 * @param idx idx of window whose display is being modified
	 * @param val whether window idx is turned on(true) or off(false)
	 */
	public final void setWinFlagsXOR(int idx, boolean val){
		if(val){//turning one on
			//turn off not shown, turn on shown				
			for(int i=0;i<winDispIdxXOR.length;++i){//check windows that should be mutually exclusive during display
				if(winDispIdxXOR[i]!= idx){
					_dispWinFrames[winDispIdxXOR[i]].setShowWin(false);
					_handleShowWinInternal(i ,0,false); 
					forceWinVisFlag(winFlagsXOR[i], false);
				}//not this window
				else {//turning on this one
					_dispWinFrames[idx].setShowWin(true);
					_handleShowWinInternal(i ,1,false); 
					forceWinVisFlag(winFlagsXOR[i], true);
					_curFocusWin = winDispIdxXOR[i];
				}
			}
		} else {//if turning off a window - need a default uncloseable window - for now just turn on next window
			//idx is dispXXXIDX idx of allowable windows (1+ since idx 0 is sidebar menu), so use idx-1 for mod function			
			setWinFlagsXOR((idx % winFlagsXOR.length)+1, true);
		}			
	}//setWinFlagsXOR
	
	//////////////////////////
	// mouse and keypress
	
	//key is key pressed
	//keycode is actual physical key pressed == key if shift/alt/cntl not pressed.,so shift-1 gives key 33 ('!') but keycode 49 ('1')

	/**
	 * handle key pressed
	 * @param keyVal 0-9, with or without shift ((keyCode>=48) && (keyCode <=57))
	 * @param keyCode actual code of key having been pressed
	 */
	protected abstract void handleKeyPress(char key, int keyCode);
	
	/**
	 * check if shift, alt, or control are pressed
	 * @param keyCode
	 */
	public final void checkAndSetSACKeys(int keyCode) {						_appStateFlags.checkAndSetSACKeys(keyCode);}
	
	/**
	 * Check if keys are released
	 * @param keyIsCoded
	 * @param keyCode
	 */
	public final void checkKeyReleased(boolean keyIsCoded, int keyCode) {	_appStateFlags.checkKeyReleased(keyIsCoded, keyCode);}
	
	/**
	 * send key presses to all windows
	 * @param key
	 * @param keyCode
	 */
	public final void sendKeyPressToWindows(char key, int keyCode) {
		handleKeyPress(key,keyCode);	//handle all other (non-numeric) keys in instancing class
		for(int i=0; i<numDispWins; ++i){_dispWinFrames[i].setValueKeyPress(key, keyCode);}
		_appStateFlags.setValueKeyPressed(true);
	}
		
	/**
	 * return a list of labels to apply to mse-over display select buttons - an empty or null list will not display option
	 * @return
	 */
	public abstract String[] getMouseOverSelBtnLabels();
		
	/**
	 * find mouse "force" exerted upon a particular location - distance from mouse to passed location
	 * @param msClickForce
	 * @param _loc
	 * @param attractMode
	 * @return
	 */
	public myVectorf mouseForceAtLoc(float msClickForce, myPointf _loc, boolean attractMode){
		myPointf mouseFrcLoc = getTransMseLoc(new myPointf(_3DGridDimX/2.0f, _3DGridDimY/2.0f, _3DGridDimZ/2.0f));// new myPointf(c.dfCtr.x+_3DGridDimX/2.0f,c.dfCtr.y+_3DGridDimY/2.0f,c.dfCtr.z+_3DGridDimZ/2.0f);// new myVector(lstClkX,0,lstClkY);//translate click location to where the space where the boids are	
		myVectorf resFrc = new myVectorf(_loc, mouseFrcLoc);		
		float sqDist = resFrc.sqMagn;
		if(sqDist<MyMathUtils.EPS_F){sqDist=MyMathUtils.EPS_F;}
		float mag = (attractMode? 1 : -1) * msClickForce / sqDist;
		resFrc._scale(mag);
		return resFrc;	
	}//mouseForceAtLoc
	
	/**
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public final void mouseMoved(int mouseX, int mouseY){for(int i=0; i<numDispWins; ++i){if (_dispWinFrames[i].handleMouseMove(mouseX, mouseY)){return;}}}
	
	/**
	 * Mouse button pressed
	 * @param mouseX
	 * @param mouseY
	 * @param isLeftClick
	 * @param isRightClick
	 */
	public final void mousePressed(int mouseX, int mouseY,boolean isLeftClick, boolean isRightClick) {
		_appStateFlags.mousePressed(mouseX, mouseY, isLeftClick, isRightClick);
	}// mousePressed		
	
	/**
	 * Handle mouse being dragged from old position to new position
	 * @param mouseX current mouse x
	 * @param mouseY current mouse y
	 * @param pmouseX previous mouse x 
	 * @param pmouseY previous mouse y
	 * @param isLeftClick
	 * @param isRightClick
	 */
	public final void mouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, boolean isLeftClick, boolean isRightClick){
		_appStateFlags.mouseDragged(mouseX, mouseY, pmouseX, pmouseY, _canvas.getMseDragVec(), isLeftClick, isRightClick);
	}//mouseDragged()
	
	/**
	 * Handle mouse wheel
	 * @param ticks amount of wheel moves
	 */
	public final void mouseWheel(int ticks) {_appStateFlags.mouseWheel(ticks);}

	/**
	 * Handle mouse button release
	 */
	public final void mouseReleased(){_appStateFlags.mouseReleased();}//mouseReleased

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// state and control flag handling
	
	/**
	 * get the ui rect values of the "master" ui region (another window) -> this is so ui objects of one window can be made, clicked, and shown displaced from those of the parent window
	 * @param idx
	 * @return
	 */	
	public final float[] getUIRectVals(int idx) {
		switch(idx){
			//sidebar menu synthesizes its uiClickCoords in its constructor
			case dispMenuIDX 		: {return new float[0];}			
			default 	:{
				return getUIRectVals_Indiv(idx, sideBarMenu.getUIClkCoords());
			}
		}
	}//getUIRectVals
	
	/**
	 * Individual window handling
	 * @param idx window index
	 * @param menuRectVals rectValus from left side menu
	 * @return
	 */
	protected abstract float[] getUIRectVals_Indiv(int idx, float[] menuRectVals);
	
	/**
	 * init boolean state machine flags for visible flags
	 */
	private final void initVisFlags(){
		int numVisFlags = getNumVisFlags();
		_winVisFlags = new int[1 + numVisFlags/32];for(int i=0; i<numVisFlags;++i){forceWinVisFlag(i,false);}	
	}		
	
	/**
	 * return the number of visible window flags for this application
	 * @return
	 */
	public abstract int getNumVisFlags();
	
	/**
	 * set visibility flag value
	 * @param idx
	 * @param val
	 */
	public final void setWinVisFlag(int idx, boolean val ){
		int flIDX = idx/32, mask = 1<<(idx%32);
		_winVisFlags[flIDX] = (val ?  _winVisFlags[flIDX] | mask : _winVisFlags[flIDX] & ~mask);
		switch (idx){
			case dispMenuIDX 	: { sideBarMenu.setShowWin(val);    break;}
			default 			: {	setVisFlag_Indiv(idx, val);	}
		}
	}

	/**
	 * only process visibility-related state changes here (should only be the switch statement
	 * @param idx
	 * @param val
	 */
	protected abstract void setVisFlag_Indiv(int idx, boolean val);	

	/**
	 * get vis flag
	 * @param idx
	 * @return
	 */
	public final boolean getWinVisFlag(int idx){int bitLoc = 1<<(idx%32);return (_winVisFlags[idx/32] & bitLoc) == bitLoc;}		
	
	/**
	 * this will not execute the code in setVisFlag, which might cause a loop
	 * @param idx
	 * @param val
	 */
	public final void forceWinVisFlag(int idx, boolean val) {
		int flIDX = idx/32, mask = 1<<(idx%32);
		_winVisFlags[flIDX] = (val ?  _winVisFlags[flIDX] | mask : _winVisFlags[flIDX] & ~mask);
		//doesn't perform any other ops - to prevent looping
	}
	
	/**
	 * determine whether polled window is being shown currently
	 * @param i
	 * @return
	 */
	public final boolean isShowingWindow(int i){return getWinVisFlag(i);}//showUIMenu is first flag of window showing flags, visFlags are defined in instancing class

	/**
	 * base class flags init
	 */
	private final void initBaseFlags(){
		_baseFlags = new int[1 + numBaseFlags/32];
		_trueFlagColors = new int[numBaseFlags][3];		
		
		for(int i = 0; i < numBaseFlags; ++i){ 
			forceBaseFlag(i,false);
			 _trueFlagColors[i] = MyMathUtils.randomIntClrAra(150, 100, 150);					
		}
	}//initBaseFlags()
	
	/**
	 * 
	 * @param idx
	 * @param val
	 */
	protected final void setBaseFlag(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		_baseFlags[flIDX] = (val ?  _baseFlags[flIDX] | mask : _baseFlags[flIDX] & ~mask);
		switch(idx){
			case debugMode 			: { for(int i =1; i<_dispWinFrames.length;++i){_dispWinFrames[i].setIsGlobalDebugMode(val) ;}break;}//anything special for debugMode 	
			case finalInitDone		: { break;}//flag to handle long setup - processing seems to time out if setup takes too long, so this will continue setup in the first draw loop
			case saveAnim 			: { break;}//anything special for saveAnim 			
			case runSim				: { break;}// handleTrnsprt((val ? 2 : 1) ,(val ? 1 : 0),false); break;}		//anything special for runSim	
			case showRtSideMenu		: {	for(int i =1; i<_dispWinFrames.length;++i){_dispWinFrames[i].setRtSideInfoWinSt(val);}break;}	//set value for every window - to show or not to show info window
			case showStatusBar		: { break;}
			case flipDrawnTraj		: { for(int i =1; i<_dispWinFrames.length;++i){_dispWinFrames[i].rebuildAllDrawnTrajs();}break;}						//whether or not to flip the drawn melody trajectory, width-wise
			case singleStep 		: { break;}
			case clearBKG			: { break;}
		}				
	}//setBaseFlag
	/**
	 * force base flag - bypass any window setting
	 * @param idx
	 * @param val
	 */
	private void forceBaseFlag(int idx, boolean val) {		
		int flIDX = idx/32, mask = 1<<(idx%32);
		_baseFlags[flIDX] = (val ?  _baseFlags[flIDX] | mask : _baseFlags[flIDX] & ~mask);
	}
	//get baseclass flag
	protected final boolean getBaseFlag(int idx){int bitLoc = 1<<(idx%32);return (_baseFlags[idx/32] & bitLoc) == bitLoc;}	
	protected final void clearBaseFlags(int[] idxs){		for(int idx : idxs){setBaseFlag(idx,false);}	}	

	/**
	 * used to toggle the value of a flag
	 * @param idx
	 */
	public final void flipMainFlag(int i) {
		int flagIDX = _flagsToShow.get(i);
		setBaseFlag(flagIDX,!getBaseFlag(flagIDX));
	}	
	/**
	 * get the current state (T/F) of state flags (Such as if shift is pressed or not) specified by idx in _stateFlagsToShow List
	 * @param idx the actual idx of the state flag
	 * @return
	 */
	public final boolean getStateFlagState(int idx) {return getBaseFlag(idx);}
	/**
	 * get number of main flags to display in right side menu
	 * @return
	 */
	public final int getNumFlagsToShow() {return _numFlagsToShow;}
	
	public final boolean isDebugMode() {return getBaseFlag(debugMode);}
	
	public final boolean doShowDrawawbleCanvas() {return getBaseFlag(showCanvas);}
	
	public final boolean isFinalInitDone() {return getBaseFlag(finalInitDone);}
	public final boolean isRunSim() {return getBaseFlag(runSim);}
	public final boolean isSingleStep() {return getBaseFlag(singleStep);}
	public final boolean doSaveAnim() {return getBaseFlag(saveAnim);}
	public final boolean doFlipTraj() {return getBaseFlag(flipDrawnTraj);}
	public final boolean doShowRtSideMenu() {return getBaseFlag(showRtSideMenu);}
	public final boolean doShowStatusBar() {return getBaseFlag(showStatusBar);}
	
	public final boolean valueKeyIsPressed() {return _appStateFlags.valueKeyIsPressed();}
	public final boolean shiftIsPressed() {return _appStateFlags.shiftIsPressed();}
	public final boolean altIsPressed() {return _appStateFlags.altIsPressed();}
	public final boolean cntlIsPressed() {return _appStateFlags.cntlIsPressed();}
	public final boolean mouseIsClicked() {return _appStateFlags.mouseIsClicked();}
	public final boolean IsModView() {return _appStateFlags.IsModView();}
	public final boolean IsDrawing() {return _appStateFlags.IsDrawing();}
	
	//display window flags
	public final boolean dispWinCanDrawInWin(int wIdx) {return winInitVals[wIdx].canDrawInWin();}
	public final boolean dispWinCanShow3dbox(int wIdx) {return winInitVals[wIdx].canShow3dbox();}
	public final boolean dispWinCanMoveView(int wIdx) {return winInitVals[wIdx].canMoveView();}
	public final boolean dispWinIs3D(int wIdx) {return winInitVals[wIdx].dispWinIs3D();}
	
	public final boolean curDispWinCanDrawInWin() {return winInitVals[_curFocusWin].canDrawInWin();}
	public final boolean curDispWinCanShow3dbox() {return winInitVals[_curFocusWin].canShow3dbox();}
	public final boolean curDispWinCanMoveView() {return winInitVals[_curFocusWin].canMoveView();}
	public final boolean curDispWinIs3D() {return winInitVals[_curFocusWin].dispWinIs3D();}
	
	public final void setSimIsRunning(boolean val) {setBaseFlag(runSim,val);}
	public final void toggleSimIsRunning() {setBaseFlag(runSim, !getBaseFlag(runSim));}
	public final void setSimIsSingleStep(boolean val) {setBaseFlag(singleStep,val);}
	public final void setShowRtSideMenu(boolean val) {setBaseFlag(showRtSideMenu,val);}
	public final void setShowStatusBar(boolean val) {setBaseFlag(showStatusBar,val);}
	public final void setClearBackgroundEveryStep(boolean val) {setBaseFlag(clearBKG,val);}
		
	public final void setMouseClicked(boolean val) { _appStateFlags.setMouseClicked(val);}
	public final void setModView(boolean val) {_appStateFlags.setModView(val);}
	public final void setIsDrawing(boolean val) {_appStateFlags.setIsDrawing(val);}
	public final void setFinalInitDone(boolean val) {setBaseFlag(finalInitDone, val);}	
	public final void setSaveAnim(boolean val) {setBaseFlag(saveAnim, val);}
	public final void toggleSaveAnim() {setBaseFlag(saveAnim, !getBaseFlag(saveAnim));}
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Top-level UI flags to show

	protected final void setBaseFlagToShow_debugMode(boolean val) {_setBaseFlagToShow(debugMode, val);}
	protected final void setBaseFlagToShow_saveAnim(boolean val) {_setBaseFlagToShow(saveAnim, val);}
	protected final void setBaseFlagToShow_runSim(boolean val) {_setBaseFlagToShow(runSim, val);}
	protected final void setBaseFlagToShow_singleStep(boolean val) {_setBaseFlagToShow(singleStep, val);}
	protected final void setBaseFlagToShow_showRtSideMenu(boolean val) {_setBaseFlagToShow(showRtSideMenu, val);}	
	protected final void setBaseFlagToShow_showStatusBar(boolean val) {_setBaseFlagToShow(showStatusBar, val);}	
	protected final void setBaseFlagToShow_showDrawableCanvas(boolean val) {_setBaseFlagToShow(showCanvas, val);}	
	
	public final boolean getBaseFlagIsShown_debugMode() {return _getBaseFlagIsShown(debugMode);}
	public final boolean getBaseFlagIsShown_saveAnim() {return _getBaseFlagIsShown(saveAnim);}
	public final boolean getBaseFlagIsShown_runSim() {return _getBaseFlagIsShown(runSim);}
	public final boolean getBaseFlagIsShown_singleStep() {return _getBaseFlagIsShown(singleStep);}
	public final boolean getBaseFlagIsShown_showRtSideMenu() {return _getBaseFlagIsShown(showRtSideMenu);}
	public final boolean getBaseFlagIsShown_showStatusBar() {return _getBaseFlagIsShown(showStatusBar);}
	public final boolean getBaseFlagIsShown_showDrawableCanvas() {return _getBaseFlagIsShown(showCanvas);}
	

	//determine primary application flags that are actually being displayed or not displayed
	private final void _setBaseFlagToShow(int idx, boolean val) {
		HashMap<Integer, Integer> tmpMapOfFlags = new HashMap<Integer, Integer>();
		for(Integer flag : _flagsToShow) {			tmpMapOfFlags.put(flag, 0);		}
		if(val) {tmpMapOfFlags.put(idx, 0);	} else {tmpMapOfFlags.remove(idx);}
		_flagsToShow = new ArrayList<Integer>(tmpMapOfFlags.keySet());
		_numFlagsToShow = _flagsToShow.size();
	}//_setBaseFlagToShow
	
	private final boolean _getBaseFlagIsShown(int idx) {
		for(Integer flag : _flagsToShow) { if (flag == idx) {return true;}}
		return false;
	}
	
	/**
	 * gives multiplier based on whether shift, alt or cntl (or any combo) is pressed
	 * @return
	 */
	public final double clickValModMult(){return ((altIsPressed() ? .1 : 1.0) * (shiftIsPressed() ? 10.0 : 1.0));}	
	
	/**
	 * Specify criteria for modifying click without dragging (i.e. shift is pressed or alt is pressed) 
	 * @return
	 */
	public abstract boolean isClickModUIVal();

	public final SidebarMenu getSideBarMenuWindow() {return sideBarMenu;}
	public final String getCurFocusDispWindowName() {return getDispWindowName(_curFocusWin);}
	public final String getDispWindowName(int idx) { 
		if ((idx >= 0) && (idx < _dispWinFrames.length)){
			return _dispWinFrames[idx].getName();
		}
		return "None";
	}
	/**
	 * performs shuffle on list of strings. Moves from end of list, picks string i, finds random string j [0,i] and swaps if i!=j
	 * @param _list
	 * @param type
	 * @return
	 */
	public final String[] shuffleStrList(String[] _list, String type){
		String tmp = "";
		for(int i=(_list.length-1);i>0;--i){
			int j = (int)(MyMathUtils.randomDouble(0,(i+1)));
			if (i==j) {continue;}
			tmp = _list[i];
			_list[i] = _list[j];
			_list[j] = tmp;
		}
		msgObj.dispInfoMessage(getPrjNmShrt(), "shuffleStrList","String list of object " + type + " shuffled");
		return _list;
	}//shuffleStrList
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// calculations
	
	/**
	 * Return the points describing a plane orthogonal to the eye vector and bound to the 3d Cube bounds.
	 * @return
	 */
	public myPointf[] getCanvasDrawPlanePts() {
		return buildPlaneBoxBounds(_canvas.getCanvasCorners());
	}
		
	/**
	 * This will take given set of points and will calculate a set of points that make up the 
	 * perimeter of the plane within the specified bounds.
	 * @param pts
	 * @return
	 */
	public final myPointf[] buildPlaneBoxBounds(myPoint[] pts) {
		myVector tmpNorm = myVector._cross(new myVector(pts[0], pts[1]), new myVector(pts[0], pts[2]))._normalize();
		float[] eq = MyMathUtils.getPlanarEqFromPointAndNorm(tmpNorm, pts[0]);
		//works because plane is built with unit normal in equation
		return buildPlaneBoxBounds(eq);
	}
	
	/**
	 * This will take given set of points and will calculate a set of points that make up the 
	 * perimeter of the plane within the specified bounds.
	 * @param pts
	 * @return
	 */
	public final myPointf[] buildPlaneBoxBounds(myPointf[] pts) {
		myVectorf tmpNorm = myVectorf._cross(new myVectorf(pts[0], pts[1]), new myVectorf(pts[0], pts[2]))._normalize();
		float[] eq = MyMathUtils.getPlanarEqFromPointAndNorm(tmpNorm, pts[0]);
		//works because plane is built with unit normal in equation
		return buildPlaneBoxBounds(eq);
	}
	
	/**
	 * Find intersection of plane as described by passed coefficients with ray
	 * @param eq Plane equation coefficients
	 * @param RayOrig
	 * @param RayDir
	 * @return
	 */
	public myPointf rayintersectPlaneane(float[] eq, myPointf rayOrig, myVectorf rayDir) {		
		Float denomVal = eq[0]* rayDir.x +eq[1]* rayDir.y+ eq[2]* rayDir.z;
	    if (denomVal == 0.0f) {        return null;}
	    Float tVal = - (eq[0]* rayOrig.x +eq[1]* rayOrig.y+ eq[2]* rayOrig.z + eq[3]) / denomVal;
	    if (tVal >= 0.f && tVal <= 1.f) {   	return (myPointf._add(rayOrig,tVal, rayDir));}
	    return null;	
	}
	
	
	/**
	 * Given the equation this will derive the planar origin and then calculate a set of points that make up the perimeter
	 * of the plane within the defined bounding box.
	 * @param eq
	 * @return
	 */
	public final myPointf[] buildPlaneBoxBounds(float[] eq) {
		//works because plane is built with unit normal in equation
		myPointf planeOrigin = new myPointf(-eq[0]*eq[3],-eq[1]*eq[3],-eq[2]*eq[3]);
		//Find intersection between this object's plane and every edge of world axis aligned bound box.
	    ArrayList<myPointf> ptsAra = new ArrayList<myPointf>();
	    for(int i=0; i<_origPerDirAra.length;++i) {
			for(int j=0;j<_origPerDirAra[i].length;++j) {
			    myPointf p = rayintersectPlaneane(eq, _origPerDirAra[i][j], _cubeDirAra[i]);
			    if(null!=p) {ptsAra.add(p);}
			}
	    }	    
	    if(ptsAra.size() == 0) {    	
	    	return new myPointf[0];
	    }//no intersection
	    //sort in cw order around normal
		TreeMap<Float, myPointf> ptsMap = new TreeMap<Float, myPointf>();
		myVectorf baseVec = new myVectorf(planeOrigin, ptsAra.get(0));
		for(int i=0;i<ptsAra.size();++i) {
			myPointf pt = ptsAra.get(i);
			float res = (myVectorf._angleBetween_Xprod(new myVectorf(planeOrigin, pt),baseVec));
			ptsMap.put(res, pt);
		}		

	    return ptsMap.values().toArray(new myPointf[0]);
	}
	
	/**
	 * build a frame based on world orientation given two passed points
	 * @param A center point of endcap A
	 * @param B center point of endcap B
	 * @return vec array of {AB, ScreenNorm, ScreenTan}
	 */
	public myVector[] buildViewBasedFrame(myPoint A, myPoint B) {
		return MyMathUtils.buildFrameAroundNormal(A, B, getDrawSNorm());		
	}
	
	/**
	 * build a frame based on world orientation given two passed points
	 * @param A center point of endcap A
	 * @param B center point of endcap B
	 * @return float vec array of {AB, ScreenNorm, ScreenTan}
	 */
	public myVectorf[] buildViewBasedFrame(myPointf A, myPointf B) {
		return MyMathUtils.buildFrameAroundNormal(A, B, getDrawSNorm_f());
	}
		
	/**
	 * Derive the points of a cylinder of radius r around axis through A and B
	 * @param A center point of endcap A
	 * @param B center point of endcap B
	 * @param r desired radius of cylinder
	 * @return array of points for cylinder
	 */
	public myPoint[] buildCylVerts(myPoint A, myPoint B, double r) {
		return MyMathUtils.buildCylVerts(A,B,r, getDrawSNorm());
	}//build list of all cylinder vertices 
	
	/**
	 * Derive the points of a cylinder of radius r around axis through A and B
	 * @param A center point of endcap A
	 * @param B center point of endcap B
	 * @param r desired radius of cylinder
	 * @return array of points for cylinder
	 */
	public myPointf[] buildCylVerts(myPointf A, myPointf B, float r) {
		return MyMathUtils.buildCylVerts(A,B,r, getDrawSNorm_f());
	}//build list of all cylinder vertices 
	
	/**
	 * random location within coords[0] and coords[1] extremal corners of a cube - bnds is to give a margin of possible random values
	 * @param coords
	 * @param bnds
	 * @return
	 */
	public myVectorf getRandPosInCube(float[][] coords, float bnds){
		return new myVectorf(
				MyMathUtils.randomDouble(coords[0][0]+bnds,(coords[0][0] + coords[1][0] - bnds)),
				MyMathUtils.randomDouble(coords[0][1]+bnds,(coords[0][1] + coords[1][1] - bnds)),
				MyMathUtils.randomDouble(coords[0][2]+bnds,(coords[0][2] + coords[1][2] - bnds)));}		
			
	public final myPoint bndChkInBox2D(myPoint p){p.set(MyMathUtils.max(0,MyMathUtils.min(p.x,_2DGridDimX)),MyMathUtils.max(0,MyMathUtils.min(p.y,_2DGridDimY)),0);return p;}
	public final myPoint bndChkInBox3D(myPoint p){p.set(MyMathUtils.max(0,MyMathUtils.min(p.x,_3DGridDimX)), MyMathUtils.max(0,MyMathUtils.min(p.y,_3DGridDimY)),MyMathUtils.max(0,MyMathUtils.min(p.z,_3DGridDimZ)));return p;}	
	public final myPoint bndChkInCntrdBox3D(myPoint p){
		p.set(MyMathUtils.max(-_3DHalfGridDim.x,MyMathUtils.min(p.x,_3DHalfGridDim.x)), 
				MyMathUtils.max(-_3DHalfGridDim.y,MyMathUtils.min(p.y,_3DHalfGridDim.y)),
				MyMathUtils.max(-_3DHalfGridDim.z,MyMathUtils.min(p.z,_3DHalfGridDim.z)));return p;}	
	
	public final myPointf bndChkInBox2D(myPointf p){p.set(MyMathUtils.max(0,MyMathUtils.min(p.x,_2DGridDimX)),MyMathUtils.max(0,MyMathUtils.min(p.y,_2DGridDimY)),0);return p;}
	public final myPointf bndChkInBox3D(myPointf p){p.set(MyMathUtils.max(0,MyMathUtils.min(p.x,_3DGridDimX)), MyMathUtils.max(0,MyMathUtils.min(p.y,_3DGridDimY)),MyMathUtils.max(0,MyMathUtils.min(p.z,_3DGridDimZ)));return p;}	
	public final myPointf bndChkInCntrdBox3D(myPointf p){
		p.set(MyMathUtils.max(-_3DHalfGridDim.x,MyMathUtils.min(p.x,_3DHalfGridDim.x)), 
				MyMathUtils.max(-_3DHalfGridDim.y,MyMathUtils.min(p.y,_3DHalfGridDim.y)),
				MyMathUtils.max(-_3DHalfGridDim.z,MyMathUtils.min(p.z,_3DHalfGridDim.z)));return p;}	
	
	/**
	 * convert a world location within the bounded cube region to be a 4-int color array
	 * @param t
	 * @return
	 */
	public final int[] getClrFromCubeLoc(float[] t){
		return new int[]{(int)(255*(t[0]-_cubeBnds[0][0])/_cubeBnds[1][0]),(int)(255*(t[1]-_cubeBnds[0][1])/_cubeBnds[1][1]),(int)(255*(t[2]-_cubeBnds[0][2])/_cubeBnds[1][2]),255};
	}
	
	/**
	 * convert a world location within the bounded cube region to be a 4-int color array
	 * @param t
	 * @return
	 */
	public final int[] getClrFromCubeLoc(myPointf t){
		return new int[]{(int)(255*(t.x-_cubeBnds[0][0])/_cubeBnds[1][0]),(int)(255*(t.y-_cubeBnds[0][1])/_cubeBnds[1][1]),(int)(255*(t.z-_cubeBnds[0][2])/_cubeBnds[1][2]),255};
	}

	//set color based on passed point r= x, g = z, b=y
	public final void fillAndShowLineByRBGPt(myPoint p, float x,  float y, float w, float h){
		ri.setFill((int)p.x,(int)p.y,(int)p.z, 255);
		ri.setStroke((int)p.x,(int)p.y,(int)p.z, 255);
		ri.drawRect(x,y,w,h);
	}	
	
}//class GUI_AppManager
