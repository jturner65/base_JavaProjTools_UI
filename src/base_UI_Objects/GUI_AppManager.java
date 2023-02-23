package base_UI_Objects;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.jogamp.newt.opengl.GLWindow;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_UI_Objects.windowUI.base.Base_DispWindow;
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
	 * 3d interaction stuff and mouse tracking
	 */
	protected Disp3DCanvas canvas;	
	/**
	 * Reference to GL Window underlying IRenderInterface surface
	 */
	public GLWindow window;
	
	/**
	 * physical display width and height this project is running on
	 */
	protected final int _displayWidth, _displayHeight;
	
	/**
	 * Width and height of application window
	 */
	private int viewWidth, viewHeight, viewWidthHalf, viewHeightHalf;
	
	/**
	 * Aspect ratio for perspective display
	 */
	protected float aspectRatio;
	
	/**
	 * counter for simulation cycles	
	 */
	protected int simCycles;
	
	/**
	 * 9 element array holding camera loc, target, and orientation
	 */
	public float[] camVals;	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Time and date
	
	/**
	 * time that this application started
	 */
	protected int glblStartSimFrameTime,			//begin of draw(sim)
			glblLastSimFrameTime;					//begin of last draw(sim)
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Display Window-based values
	/**
	 * max ratio of width to height to use for application window initialization for widescreen
	 */
	public final float maxWinRatio =  1.77777778f;	
	
	/**
	 * individual display/HUD windows for gui/user interaction
	 */
	protected Base_DispWindow[] dispWinFrames = new Base_DispWindow[0];
	/**
	 * set in instancing class - must be > 1
	 */
	protected final int numDispWins;
	/**
	 * always idx 0 - first window is always right side menu
	 */
	public static final int dispMenuIDX = 0;	
	/**
	 * which Base_DispWindow currently has focus
	 */
	public int curFocusWin;		
	/**
	 * need 1 per display window
	 */
	public String[] winTitles,winDescr;

	//whether or not the display windows will accept a drawn trajectory
	protected boolean[][] dispWinFlags;
	//idxs : 0 : canDrawInWin; 1 : canShow3dbox; 2 : canMoveView; 3 : dispWinIs3d
	protected static final int
				dispCanDrawInWinIDX 	= 0,
				dispCanShow3dboxIDX 	= 1,
				dispCanMoveViewIDX 		= 2,
				dispWinIs3dIDX 			= 3;
	private static int numDispWinBoolFlags = 4;
	
	public int[][] winFillClrs, winStrkClrs;
	
	public int[][] winTrajFillClrs = new int [][]{{0,0},{0,0}};		//set to color constants for each window
	public int[][] winTrajStrkClrs = new int [][]{{0,0},{0,0}};		//set to color constants for each window
	

	protected float menuWidth;			
	//side menu is 15% of screen grid2D_X, 
	protected final float menuWidthMult;
	protected float hideWinWidth;
	protected final float hideWinWidthMult;
	protected float hideWinHeight;
	protected final float hideWinHeightMult;	
	
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
	 * height, and fraction of window height, popup win should use, from bottom of screen, when open
	 */
	protected float popUpWinHeight;
	protected final float popUpWinOpenMult;
	
	/**
	 * Whether or not this application uses a sphere background for each window
	 */
	protected boolean[] useSkyboxBKGndAra;
	
	/**
	 * specify windows that cannot be shown simultaneously here and their flags
	 */
	public int[] winFlagsXOR, winDispIdxXOR;
	
	/**
	 * unblocked window dimensions - location and dim of window if window is open\closed
	 */
	public float[][] winRectDimOpen, winRectDimClose;

	/**
	 * flags explicitly pertaining to window visibility.  1 flag per window in application - flags defined in child class
	 */
	private int[] _visFlags;		

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// program control variables

	/**
	 * flags used to control various elements of the entire application.
	 */
	private int[] _baseFlags;
	//dev/debug flags
	private static final int 
			debugMode 			= 0,			//whether we are in debug mode or not	
			finalInitDone		= 1,			//used only to call final init in first draw loop, to avoid stupid timeout error processing 3.x's setup introduced
			saveAnim 			= 2,			//whether we are saving or not an anim screenie
	//interface flags	                   
			valueKeyPressed		= 3,
			shiftKeyPressed 	= 4,			//shift pressed
			altKeyPressed  		= 5,			//alt pressed
			cntlKeyPressed  	= 6,			//cntrl pressed
			mouseClicked 		= 7,			//mouse left button is held down	
			drawing				= 8, 			//currently drawing  showSOMMapUI
			modView	 			= 9,			//shift+mouse click+mouse move being used to modify the view
	//simulation
			runSim				= 10,			//run simulation
			singleStep			= 11,			//run single sim step
			showRtSideMenu		= 12,			//display the right side info menu for the current window, if it supports that display
			flipDrawnTraj  		= 13,			//whether or not to flip the direction of the drawn trajectory TODO this needs to be moved to window
			clearBKG 			= 14;			//whether or not background should be cleared for every draw.  defaults to true
	public final int numBaseFlags = 15;
	
	//booleans in main program - need to have labels in idx order, even if not displayed
	private final String[] truePFlagNames = {//needs to be in order of flags
			"Debug Mode",
			"Final init Done",
			"Save Anim", 	
			"Key Pressed",
			"Shift-Key Pressed",
			"Alt-Key Pressed",
			"Cntl-Key Pressed",
			"Click interact", 	
			"Drawing Curve",
			"Changing View",	
			"Stop Simulation",
			"Single Step",
			"Displaying Side Menu",
			"Reverse Drawn Trajectory",
			"Clearing Background"
			};
	
	private final String[] falsePFlagNames = {//needs to be in order of flags
			"Debug Mode",	
			"Final init Done",
			"Save Anim", 	
			"Key Pressed",
			"Shift-Key Pressed",
			"Alt-Key Pressed",
			"Cntl-Key Pressed",
			"Click interact", 	
			"Drawing Curve",
			"Changing View",	 	
			"Run Simulation",
			"Single Step",
			"Displaying Side Menu",
			"Reverse Drawn Trajectory",
			"Clear Background"
			};
	/**
	 * Colors to use to display flags
	 */
	private int[][] pFlagColors;
	
	/**
	 * flags to actually display in menu as clickable text labels - order does matter
	 */
	private List<Integer> flagsToShow = Arrays.asList( 
		debugMode, 			
		saveAnim,
		runSim,
		singleStep,
		showRtSideMenu
		);
	
	private int numFlagsToShow = flagsToShow.size();
	
	/**
	 * Display UI indicator that varius mod keys are pressed
	 */
	private final List<Integer> stateFlagsToShow = Arrays.asList( 
		shiftKeyPressed,			//shift pressed
		altKeyPressed,				//alt pressed
		cntlKeyPressed,				//cntrl pressed
		mouseClicked,				//mouse left button is held down	
		drawing, 					//currently drawing
		modView	 					//shift+mouse click+mouse move being used to modify the view					
			);
	public final int numStFlagsToShow = stateFlagsToShow.size();
	
	
	private final String[] StateBoolNames = {"Shift","Alt","Cntl","Click", "Draw","View"};
	//multiplier for displacement to display text label for stateboolnames
	private final float[] StrWdMult = new float[]{-3.0f,-3.0f,-3.0f,-3.2f,-3.5f,-2.5f};
	private int[][] stBoolFlagColors;	
	
	public int animCounter = 0;
	//whether or not to show start up instructions for code		
	public boolean showInfo=false;			
	
	//display-related size variables
	public int grid2D_X = 800, grid2D_Y = 800;	
	public int gridDimX = 800, gridDimY = 800, gridDimZ = 800;				//dimensions of 3d region
	public myVectorf gridHalfDim = new myVectorf(gridDimX*.5f,gridDimY*.5f,gridDimZ*.5f );

	//boundary regions for enclosing cube - given as min and difference of min and max
	public float[][] cubeBnds = new float[][]{//idx 0 is min, 1 is diffs
		new float[]{-gridDimX/2.0f,-gridDimY/2.0f,-gridDimZ/2.0f},//mins
		new float[]{gridDimX,gridDimY,gridDimZ}};			//diffs		
	
	//2D, 3D
	private myVector[] sceneFcsValsBase = new myVector[]{						//set these values to be different targets of focus
			new myVector(-grid2D_X/2,-grid2D_Y/1.75f,0),
			new myVector(0,0,0)
	};
	//2D, 3D
	private myPoint[] sceneCtrValsBase = new myPoint[]{				//set these values to be different display center translations -
		new myPoint(0,0,0),										// to be used to calculate mouse offset in world for pick
		new myPoint(-gridDimX/2.0,-gridDimY/2.0,-gridDimZ/2.0)
	};
	
	//3D box stuff
	public myVector[] boxNorms = new myVector[] {new myVector(1,0,0),new myVector(-1,0,0),new myVector(0,1,0),new myVector(0,-1,0),new myVector(0,0,1),new myVector(0,0,-1)};//normals to 3 d bounding boxes
	protected float hGDimX = gridDimX/2.0f, hGDimY = gridDimY/2.0f, hGDimZ = gridDimZ/2.0f;
	protected float tGDimX = gridDimX*10, tGDimY = gridDimY*10, tGDimZ = gridDimZ*20;
	public myPoint[][] boxWallPts = new myPoint[][] {//pts to check if intersection with 3D bounding box happens
			new myPoint[] {new myPoint(hGDimX,tGDimY,tGDimZ), new myPoint(hGDimX,-tGDimY,tGDimZ), new myPoint(hGDimX,tGDimY,-tGDimZ)  },
			new myPoint[] {new myPoint(-hGDimX,tGDimY,tGDimZ), new myPoint(-hGDimX,-tGDimY,tGDimZ), new myPoint(-hGDimX,tGDimY,-tGDimZ) },
			new myPoint[] {new myPoint(tGDimX,hGDimY,tGDimZ), new myPoint(-tGDimX,hGDimY,tGDimZ), new myPoint(tGDimX,hGDimY,-tGDimZ) },
			new myPoint[] {new myPoint(tGDimX,-hGDimY,tGDimZ),new myPoint(-tGDimX,-hGDimY,tGDimZ),new myPoint(tGDimX,-hGDimY,-tGDimZ) },
			new myPoint[] {new myPoint(tGDimX,tGDimY,hGDimZ), new myPoint(-tGDimX,tGDimY,hGDimZ), new myPoint(tGDimX,-tGDimY,hGDimZ)  },
			new myPoint[] {new myPoint(tGDimX,tGDimY,-hGDimZ),new myPoint(-tGDimX,tGDimY,-hGDimZ),new myPoint(tGDimX,-tGDimY,-hGDimZ)  }
	};

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
		
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// code
	
	public GUI_AppManager() {
		super(true);
		//get primary monitor size
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		_displayWidth = gd.getDisplayMode().getWidth();
		_displayHeight = gd.getDisplayMode().getHeight();	
		
		menuWidthMult = getMenuWidthMult();
		hideWinWidthMult = getHideWinWidthMult();
		hideWinHeightMult = getHideWinHeightMult();
		popUpWinOpenMult = getPopUpWinOpenMult();
		//Get number of windows
		numDispWins = getNumDispWindows();
		//Init window structures
		winRectDimOpen = new float[numDispWins][];
		winRectDimClose = new float[numDispWins][];
		//idxs : 0 : canDrawInWin; 1 : canShow3dbox; 2 : canMoveView; 3 : dispWinIs3d
		dispWinFlags = new boolean[numDispWins][numDispWinBoolFlags];
		winFillClrs = new int[numDispWins][4];
		winStrkClrs = new int[numDispWins][4];
		winTrajFillClrs = new int[numDispWins][4];		//set to color constants for each window
		winTrajStrkClrs = new int[numDispWins][4];	//set to color constants for each window
		//whether each 3D window uses Skybox or color background 
		useSkyboxBKGndAra= new boolean[numDispWins];
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
		my_procApplet._invokedMain(_appMgr, _passedArgs);
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
				float newWidth = (displayRatio > maxWinRatio) ?  _displayWidth * maxWinRatio/displayRatio : _displayWidth;
				return new int[] {(int)(newWidth*.95f), (int)(_displayHeight*.92f)};
			}
			default :{//unsupported winSizeCntrl setting >= 2
				System.out.println("Unsupported value from setAppWindowDimRestrictions(). Defaulting to 0.");
				return new int[] {(int)(_displayWidth*.95f), (int)(_displayHeight*.92f)};
			}			
		}
	}//getIdealAppWindowDims
		
	/**
	 * set level of smoothing to use for rendering (depending on rendering used, this may be ignored)
	 */
	protected abstract void setSmoothing();

	/**
	 * whether or not we want to restrict window size on widescreen monitors
	 * 
	 * @return 0 - use monitor size regardless
	 * 			1 - use smaller dim of monitor size or minimized ratio to determine window size (for wide screen monitors) 
	 * 			2+ - TBD
	 */
	protected abstract int setAppWindowDimRestrictions();
		
	public int getNumThreadsAvailable() {return Runtime.getRuntime().availableProcessors();}
	
	/**
	 * Setup this application.  Called from render interface setup
	 * @param width width of the application window
	 * @param height height of the application window
	 */
	public final void setupApp(int width, int height) {
		//potentially override setup variables on per-project basis
		setupAppDims_Indiv();
		
		//for every window, load either window color or window Skybox, depending on 
		//per-window specification
		for(int i=0;i<numDispWins;++i) {
			useSkyboxBKGndAra[i] = getUseSkyboxBKGnd(i);
			if (useSkyboxBKGndAra[i]) {
				ri.loadBkgndSphere(i, getSkyboxFilename(i));
			}
			int[] bGroundClr = getBackgroundColor(i);
			ri.setRenderBackground(i, bGroundClr, bGroundClr[3]);
		}
				
		//Initialize application
		//Set window to point to main GL window
		window = ri.getGLWindow();
		//init internal state flags structure
		initBaseFlags();
		//Set dimensions for application based on applet window size and rebuild canvas
		setAppWindowDims(width, height);
		
		//called after windows are built
		initBaseFlags_Indiv();
		//instancing class version
		initAllDispWindows();

		initPFlagColors();		
		//after all display windows are drawn
		finalDispWinInit();
		//set clearing the background to be true
		setBaseFlag(clearBKG,true);
		//init sim cycles count
		simCycles = 0;
		//visibility flags corresponding to windows
		initVisFlags();
		
		// set milli time tracking
		glblStartSimFrameTime = timeSinceStart();
		glblLastSimFrameTime =  glblStartSimFrameTime;	
		
		
		//call this in first draw loop also, if not setup yet
		initOnce();
	}
	
	/**
	 * Called in pre-draw initial setup, before first init
	 * potentially override setup variables on per-project basis.
	 * Do not use for setting background color or Skybox anymore.
	 *  	(Current settings in my_procApplet) 	
	 *  	strokeCap(PROJECT);
	 *  	textSize(txtSz);
	 *  	textureMode(NORMAL);			
	 *  	rectMode(CORNER);	
	 *  	sphereDetail(4);	 * 
	 */
	protected abstract void setupAppDims_Indiv();		

	
	/**
	 * Set the application window width and height and rebuild camera and canvas, on init or resize (TODO)
	 * @param width
	 * @param height
	 */
	public final void setAppWindowDims(int width, int height) {
		viewWidth = width;
		viewHeight = height;
		
		msgObj.dispInfoMessage("GUI_AppManager","setAppWindowDims","Base applet width : " + viewWidth + " | height : " +  viewHeight);
		
		viewWidthHalf = viewWidth/2; 
		viewHeightHalf = viewHeight/2;
		
		aspectRatio = viewWidth/(1.0f*viewHeight);
		msSclX = MyMathUtils.PI_F/viewWidth;
		msSclY = MyMathUtils.PI_F/viewHeight;

		menuWidth = viewWidth * menuWidthMult;						
		
		hideWinWidth = viewWidth * hideWinWidthMult;				//dims for hidden windows
		//popup/hidden window height to use when hidden 
		hideWinHeight = viewHeight * hideWinHeightMult;
		//popup window height when open
		popUpWinHeight = viewHeight * popUpWinOpenMult;
		// set cam vals
		camVals = new float[]{0, 0, (float) (viewHeightHalf / Math.tan(MyMathUtils.PI/6.0)), 0, 0, 0, 0,1,0};		
		//build canvas
		canvas = new Disp3DCanvas(this, ri, viewWidth, viewHeight);	
	}//setAppWindowWidth

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
	 * Retrieves reasonable default window open dims
	 * @return
	 */
	public float[] getDefaultWinDimOpen() {return new float[]{menuWidth, 0, viewWidth-menuWidth,  viewHeight};}
	
	/**
	 * Retrieves reasonable default window closed dims
	 * @return
	 */
	public float[] getDefaultWinDimClosed() {return new float[]{menuWidth, 0, hideWinWidth,  viewHeight};}

	/**
	 * Retrieves reasonable default pop-up window open dims
	 * @return
	 */
	public float[] getDefaultPopUpWinDimOpen() {return new float[]{menuWidth, viewHeight-popUpWinHeight, viewWidth-menuWidth,  popUpWinHeight};}
	
	/**
	 * Retrieves reasonable default pop-up window closed dims
	 * @return
	 */
	public float[] getDefaultPopUpWinDimClosed() {return new float[]{menuWidth, viewHeight-hideWinHeight, hideWinWidth,  hideWinHeight};}

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
		initOnce_Indiv();
		//initProgram is called every time reinitialization is desired
		initProgram();		

		//after all init is done
		setFinalInitDone(true);
	}//initOnce	

	protected abstract void initOnce_Indiv();	
	
	//called every time re-initialized
	private final void initVisProg(){	
		for (int i=1; i<dispWinFrames.length;++i) {
			dispWinFrames[i].reInitInfoStr();
		}
	}
		//called every time re-initialized
	public final void initProgram() {
		initVisProg();				//always first
		
		initProgram_Indiv();
	}//initProgram	
	protected abstract void initProgram_Indiv();
	
	/**
	 * Specify window titles and descriptions
	 * @param _winTtls
	 * @param _winDescs
	 */
	public final void setWinTitlesAndDescs(String[] _winTtls, String[] _winDescs) {
		//display window initialization
		dispWinFrames = new Base_DispWindow[numDispWins];	
		
		//need 1 per display window
		winTitles = _winTtls;
		winDescr = _winDescs;
	}//initWins
	
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
		gridDimX = _gx;gridDimY = _gy;gridDimZ = _gz;				//dimensions of 3d region
		gridHalfDim.set(gridDimX*.5f,gridDimY*.5f,gridDimZ*.5f );
		
		cubeBnds = new float[][]{//idx 0 is min, 1 is diffs
			new float[]{-gridDimX/2.0f,-gridDimY/2.0f,-gridDimZ/2.0f},//mins
			new float[]{gridDimX,gridDimY,gridDimZ}};			//diffs
		
		//2D, 3D
		sceneFcsValsBase = new myVector[]{						//set these values to be different targets of focus
				new myVector(-grid2D_X/2,-grid2D_Y/1.75f,0),
				new myVector(0,0,0)
		};
		//2D, 3D
		sceneCtrValsBase = new myPoint[]{				//set these values to be different display center translations -
			new myPoint(0,0,0),										// to be used to calculate mouse offset in world for pick
			new myPoint(-gridDimX/2.0,-gridDimY/2.0,-gridDimZ/2.0)
		};
		
		hGDimX = gridDimX/2.0f;
		hGDimY = gridDimY/2.0f;
		hGDimZ = gridDimZ/2.0f;
		tGDimX = gridDimX*10;
		tGDimY = gridDimY*10;
		tGDimZ = gridDimZ*20;
		boxWallPts = new myPoint[][] {//pts to check if intersection with 3D bounding box happens
				new myPoint[] {new myPoint(hGDimX,tGDimY,tGDimZ), new myPoint(hGDimX,-tGDimY,tGDimZ), new myPoint(hGDimX,tGDimY,-tGDimZ)  },
				new myPoint[] {new myPoint(-hGDimX,tGDimY,tGDimZ), new myPoint(-hGDimX,-tGDimY,tGDimZ), new myPoint(-hGDimX,tGDimY,-tGDimZ) },
				new myPoint[] {new myPoint(tGDimX,hGDimY,tGDimZ), new myPoint(-tGDimX,hGDimY,tGDimZ), new myPoint(tGDimX,hGDimY,-tGDimZ) },
				new myPoint[] {new myPoint(tGDimX,-hGDimY,tGDimZ),new myPoint(-tGDimX,-hGDimY,tGDimZ),new myPoint(tGDimX,-hGDimY,-tGDimZ) },
				new myPoint[] {new myPoint(tGDimX,tGDimY,hGDimZ), new myPoint(-tGDimX,tGDimY,hGDimZ), new myPoint(tGDimX,-tGDimY,hGDimZ)  },
				new myPoint[] {new myPoint(tGDimX,tGDimY,-hGDimZ),new myPoint(-tGDimX,tGDimY,-hGDimZ),new myPoint(tGDimX,-tGDimY,-hGDimZ)  }};
	}
	
	/**
	 * initialize menu window
	 * @param _showUIMenuIDX
	 */
	public final void buildInitMenuWin() {
		//init sidebar menu vals
		for(int i=0;i<dispWinFlags[dispMenuIDX].length;++i) {dispWinFlags[dispMenuIDX][i] = false;}
		//set up dims for menu
		winRectDimOpen[dispMenuIDX] =  new float[]{0,0, menuWidth, viewHeight};
		winRectDimClose[dispMenuIDX] =  new float[]{0,0, hideWinWidth, viewHeight};
		
		winFillClrs[dispMenuIDX] = new int[]{255,255,255,255};
		winStrkClrs[dispMenuIDX] = new int[]{0,0,0,255};
		
		winTrajFillClrs[dispMenuIDX] = new int[]{0,0,0,255};		//set to color constants for each window
		winTrajStrkClrs[dispMenuIDX] = new int[]{0,0,0,255};		//set to color constants for each window		
		winTitles[dispMenuIDX] = "UI Window";
		winDescr[dispMenuIDX] = "User Controls";
		
	}//setIniMenuWin
	
	/**
	 * call once for each display window before calling constructor. Sets essential values describing windows
	 * @param _winIDX The index in the various window-descriptor arrays for the dispWindow being set
	 * @param _dimOpen The array of x,y,W,H dimensions for the dispWindow being open
	 * @param _dimClosed The array of x,y,W,H dimensions for the dispWindow being closed
	 * @param _dispFlags Essential flags describing the nature of the dispWindow
	 * @param _fill Fill color to use for dispWindow
	 * @param _strk Stroke color to use for dispWindow
	 * @param _trajFill Trajetory's fill color to use for dispWindow
	 * @param _trajStrk Trajetory's stroke color to use for dispWindow
	 */
	public final void setInitDispWinVals(int _winIDX, float[] _dimOpen, float[] _dimClosed, boolean[] _dispFlags, int[] _fill, int[] _strk, int[] _trajFill, int[] _trajStrk) {
		winRectDimOpen[_winIDX] = new float[_dimOpen.length];
		System.arraycopy(_dimOpen, 0, winRectDimOpen[_winIDX], 0, _dimOpen.length);
		winRectDimClose[_winIDX] = new float[_dimClosed.length];
		System.arraycopy(_dimClosed, 0, winRectDimClose[_winIDX], 0, _dimClosed.length);
		dispWinFlags[_winIDX] = new boolean[ _dispFlags.length];
		System.arraycopy(_dispFlags, 0, dispWinFlags[_winIDX], 0, _dispFlags.length);
		winFillClrs[_winIDX] = new int[_fill.length];
		System.arraycopy(_fill, 0, winFillClrs[_winIDX], 0, _fill.length);
		winStrkClrs[_winIDX] = new int[_strk.length];
		System.arraycopy(_strk, 0, winStrkClrs[_winIDX], 0, _strk.length);
		winTrajFillClrs[_winIDX] = new int[_trajFill.length];		//set to color constants for each window
		System.arraycopy(_trajFill, 0, winTrajFillClrs[_winIDX], 0, _trajFill.length);
		winTrajStrkClrs[_winIDX] = new int[_trajStrk.length];	//set to color constants for each window		
		System.arraycopy(_trajStrk, 0, winTrajStrkClrs[_winIDX], 0, _trajStrk.length);
	
	}//setInitDispWinVals
	
	private void finalDispWinInit() {
		for(int i =0; i < numDispWins; ++i){
			int scIdx = dispWinFlags[i][dispWinIs3dIDX] ? 1 : 0;//whether or not is 3d
			dispWinFrames[i].finalInit(dispWinFlags[i][dispCanDrawInWinIDX],dispWinFlags[i][dispWinIs3dIDX], dispWinFlags[i][dispCanMoveViewIDX], sceneCtrValsBase[scIdx], sceneFcsValsBase[scIdx]);
			dispWinFrames[i].setTrajColors(winTrajFillClrs[i], winTrajStrkClrs[i]);
			dispWinFrames[i].setRtSideUIBoxClrs(new int[]{0,0,0,200},new int[]{255,255,255,255});
		}	
	
	}//finalDispWinInit	
	
	public Base_DispWindow getCurrentWindow() {return dispWinFrames[curFocusWin];}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// side bar menu stuff
	
	/**
	 * build the appropriate side bar menu configuration for this application
	 * @param wIdx
	 * @param fIdx
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _funcBtnNames array of arrays of names for each button
	 * @param _dbgBtnNames array of names for each debug button. If array is empty then no debug buttons will be handled.
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 * @return
	 */
	public final SidebarMenu buildSideBarMenu(int wIdx, int fIdx, String[] _funcRowNames, String[][] _funcBtnNames, String[] _dbgBtnNames, boolean _inclWinNames, boolean _inclMseOvValues){
		SidebarMenuBtnConfig sideBarConfig = new SidebarMenuBtnConfig(_funcRowNames, _funcBtnNames, _dbgBtnNames, _inclWinNames, _inclMseOvValues);
		return new SidebarMenu(ri, this, wIdx, fIdx, sideBarConfig);		
	}
	
	//set up initial colors for primary flags for display
	private void initPFlagColors(){
		pFlagColors = new int[numBaseFlags][3];
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		for (int i = 0; i < numBaseFlags; ++i) { pFlagColors[i] = new int[]{tr.nextInt(150),tr.nextInt(100),tr.nextInt(150)}; }		
		stBoolFlagColors = new int[numStFlagsToShow][3];
		stBoolFlagColors[0] = new int[]{255,0,0};
		stBoolFlagColors[1] = new int[]{0,255,0};
		stBoolFlagColors[2] = new int[]{0,0,255};		//new int[]{100+((int) random(150)),150+((int) random(100)),150+((int) random(150))};	
		for (int i = 3; i < numStFlagsToShow; ++i) { stBoolFlagColors[i] = new int[]{tr.nextInt(100,250),tr.nextInt(150,250),tr.nextInt(150,250)};		}
	}
	
	//clear menu side bar buttons when window-specific processing is finished
	//isSlowProc means original calling process lasted longer than mouse click release and so button state should be forced to be off
	public final void clearBtnState(int _type, int col, boolean isSlowProc) {
		int row = _type;
		SidebarMenu win = (SidebarMenu)dispWinFrames[dispMenuIDX];
		win.getGuiBtnWaitForProc()[row][col] = false;
		if(isSlowProc) {win.getGuiBtnSt()[row][col] = 0;}		
	}//clearBtnState 
	
	/**
	 * only send names of function and debug btns (if they exist) in 2d array
	 * @param btnNames
	 */
	public final void setAllMenuBtnNames(String[][] btnNames) {
		for(int _type = 0;_type<btnNames.length;++_type) {((SidebarMenu)dispWinFrames[dispMenuIDX]).setAllFuncBtnLabels(_type,btnNames[_type]);}
	}
	
	
	//these tie using the UI buttons to modify the window in with using the boolean tags - PITA but currently necessary
	public final void handleShowWin(int btn, int val){handleShowWin(btn, val, true);}					//display specific windows - multi-select/ always on if sel
	//these tie using the UI buttons to modify the window in with using the boolean tags - PITA but currently necessary
	public abstract void handleShowWin(int btn, int val, boolean callFlags);
	//process to handle file io	- TODO	
	public final void handleFileCmd(int _type, int btn, int val){handleFileCmd(_type,btn, val, true);}					//display specific windows - multi-select/ always on if sel
	public final void handleFileCmd(int _type, int btn, int val, boolean callFlags){//{"Load","Save"},							//load an existing score, save an existing score - momentary	
		if(!callFlags){			setMenuBtnState(_type,btn, val);		}  else {
			switch(btn){
				case 0 : {
					//processing provided custom selectInput and selectOutput windows that had call 
					//back functionality.  this cannot be delineated in an interface, so this needs to be coded separately
					System.out.println("Selecting input not implemented currently.");
					//selectInput("Select a file to load from : ", "loadFromFile", currFileIOLoc);
					break;}
				case 1 : {
					System.out.println("Selecting Output not implemented currently.");
					//selectOutput("Select a file to save to : ", "saveToFile", currFileIOLoc);
					break;}
			}
			getSideBarMenuWindow().hndlMouseRelIndiv();
		}
	}//handleFileCmd
	
	//display specific windows - multi-select/ always on if sel
	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val){handleMenuBtnSelCmp(row, funcOffset, col, val, true);}					
	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val, boolean callFlags){
		if(!callFlags){			setMenuBtnState(row,col, val);		} //if called programmatically, not via ui action
		else {					dispWinFrames[curFocusWin].clickSideMenuBtn(row, funcOffset, col);		}
	}//handleAddDelSelCmp		
		
	/**
	 * pass on to current display window the choice for mouse over display data
	 * @param btn
	 * @param val
	 */
	public final void handleMenuBtnMseOvDispSel(int btn,boolean val) {
		dispWinFrames[curFocusWin].handleSideMenuMseOvrDispSel(btn, val);
	}
	
	/**
	 * pass on to current display window the choice for debug selection - application should manage debug button state
	 * @param btn
	 * @param val
	 */
	public final void handleMenuBtnDebugSel(int btn,int val) {
		//set current window's debug state
		dispWinFrames[curFocusWin].setThisWinDebugState(btn, val);	
	}	
	
	protected void setMenuBtnState(int row, int col, int val) {
		((SidebarMenu)dispWinFrames[dispMenuIDX]).getGuiBtnSt()[row][col] = val;	
		if (val == 1) {
			//outStr2Scr("my_procApplet :: setMenuBtnState :: Note!!! Turning on button at row : " + row + "  col " + col + " without button's command.");
			((SidebarMenu)dispWinFrames[dispMenuIDX]).setWaitForProc(row,col);}//if programmatically (not through UI) setting button on, then set wait for proc value true 
	}//setMenuBtnState	
	
	public final void loadFromFile(File file){
		if (file == null) {
			System.out.println("AppMgr :: loadFromFile ::Load was cancelled.");
		    return;
		} 		
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		dispWinFrames[curFocusWin].loadFromFile(file);
	
	}//loadFromFile
	
	public final void saveToFile(File file){
		if (file == null) {
			System.out.println("AppMgr :: saveToFile ::Save was cancelled.");
		    return;
		} 
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		dispWinFrames[curFocusWin].saveToFile(file);
	}//saveToFile	
	

	public final String getAnimPicName() {
		//if(!flags[this.runSim]) {return;}//don't save until actually running simulation
		//idx 0 is directory, idx 1 is file name prefix
		String[] ssName = dispWinFrames[curFocusWin].getSaveFileDirName();
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
		if(useSkyboxBKGndAra[winIdx]) {	ri.drawBkgndSphere(winIdx);} 
		else {					ri.drawRenderBackground(winIdx);}
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
		glblStartSimFrameTime = timeSinceStart();
		float modAmtMillis = (glblStartSimFrameTime - glblLastSimFrameTime);
		glblLastSimFrameTime = timeSinceStart();
		return modAmtMillis;
	}
	
	/**
	 * primary sim and draw loop.  Called from draw in IRenderInterface class
	 */
	public boolean mainSimAndDrawLoop() {
		//Finish final init if not done already
		if(!isFinalInitDone()) {initOnce(); return false;}	
		float modAmtMillis = getModAmtMillis();
		//simulation section
		execSimDuringDrawLoop(modAmtMillis);
		//drawing section																//initialize camera, lights and scene orientation and set up eye movement
		drawMe(modAmtMillis);				
		//Draw UI and 
		drawUI(modAmtMillis);												//draw UI overlay on top of rendered results			
		//build window title
		ri.setWindowTitle(getPrjNmLong(), ("IDX : " + curFocusWin + " : " + getCurFocusDispWindowName()));
		return true;
	}//mainSimAndDrawLoop
	
	
	/**
	 * sim loop, called from IRenderInterface draw method
	 * @param modAmtMillis
	 */
	public boolean execSimDuringDrawLoop(float modAmtMillis) {
		//simulation section
		if(isRunSim() ){
			//run simulation
			for(int i =1; i<numDispWins; ++i){if((isShowingWindow(i)) && (dispWinFrames[i].dispFlags.getIsRunnable())){dispWinFrames[i].simulate(modAmtMillis);}}
			if(isSingleStep()){setSimIsRunning(false);}
			++simCycles;
			return true;
		}		//play in current window
		return false;
	}//execSimDuringDrawLoop
	/**
	 * setup 
	 */
	protected void drawSetup(){
		ri.setPerspective(MyMathUtils.THIRD_PI_F, aspectRatio, .5f, camVals[2]*100.0f);
		ri.enableLights(); 	
		dispWinFrames[curFocusWin].drawSetupWin(camVals);
	}//drawSetup
	
	/**
	 * main draw loop
	 */
	public final void drawMe(float modAmtMillis){
		ri.pushMatState();
		drawSetup();
		boolean is3DDraw = (curFocusWin == -1) || (curDispWinIs3D()); 
		if(is3DDraw){	//allow for single window to have focus, but display multiple windows	
			//if refreshing screen, this clears screen, sets background
			if(getShouldClearBKG()) {
				drawBackground(curFocusWin);				
				draw3D_solve3D(modAmtMillis);
				if(curDispWinCanShow3dbox()){drawBoxBnds();}
				if(dispWinFrames[curFocusWin].chkDrawMseRet()){			canvas.drawMseEdge(dispWinFrames[curFocusWin], is3DDraw);	}		
			} else {
				draw3D_solve3D(modAmtMillis);
			}
			ri.popMatState(); 
		} else {	//either/or 2d window
			//2d windows paint window box so background is always cleared
			canvas.buildCanvas();
			canvas.drawMseEdge(dispWinFrames[curFocusWin], is3DDraw);
			ri.popMatState(); 
			draw2D(modAmtMillis);
		}
		drawMePost_Indiv(modAmtMillis, is3DDraw);
	}//draw	
	
	
	/**
	 * Individual extending Application Manager post-drawMe functions
	 * @param modAmtMillis
	 * @param is3DDraw
	 */
	protected abstract void drawMePost_Indiv(float modAmtMillis, boolean is3DDraw);

	/**
	 * Draw 3d windows that are currently displayed
	 * @param modAmtMillis
	 */
	private final void draw3D_solve3D(float modAmtMillis){
		ri.pushMatState();
		for(int i =1; i<numDispWins; ++i){
			if((isShowingWindow(i)) && (dispWinFrames[i].getIs3DWindow())){	dispWinFrames[i].draw3D(modAmtMillis);}
		}
		ri.popMatState();
		//fixed xyz rgb axes for visualisation purposes and to show movement and location in otherwise empty scene
		drawAxes(100,3, new myPoint(-viewWidth/2.0f+40,0.0f,0.0f), 200, false); 
		//build target canvas
		canvas.buildCanvas();
	}//draw3D_solve3D
	
	/**
	 * Draw 2d windows that are currently displayed
	 * @param modAmtMillis
	 */
	
	public final void draw2D(float modAmtMillis) {
		for(int i =1; i<numDispWins; ++i){if (isShowingWindow(i) && !(dispWinFrames[i].getIs3DWindow())){dispWinFrames[i].draw2D(modAmtMillis);}}
	}
	
	/**
	 * draw bounding box for 3d
	 */
	public final void drawBoxBnds(){
		ri.pushMatState();
		ri.setStrokeWt(3f);
		ri.noFill();
		ri.setColorValStroke(IRenderInterface.gui_TransGray,255);		
		ri.drawBox3D(gridDimX,gridDimY,gridDimZ);
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
			prjOnPlane = bndChkInCntrdBox3D(intersectPl(p, boxNorms[i], boxWallPts[i][0],boxWallPts[i][1],boxWallPts[i][2]));				
			ri.showPtAsSphere(prjOnPlane,5,5,IRenderInterface.rgbClrs[i/2],IRenderInterface.rgbClrs[i/2]);				
		}
		ri.popMatState();
	}//drawProjOnBox

	/**
	 * display menu text based on menu state - moved from menu class
	 * @param xOffHalf
	 * @param yOffHalf
	 */
	public final void dispMenuText(float xOffHalf, float yOffHalf) {
		for(int idx =0; idx<numFlagsToShow; ++idx){
		int i = flagsToShow.get(idx);
			if(getBaseFlag(i) ){											dispMenuTxtLat(truePFlagNames[i],pFlagColors[i], true, xOffHalf,yOffHalf);			}
			else {	if(truePFlagNames[i].equals(falsePFlagNames[i])) {		dispMenuTxtLat(truePFlagNames[i],new int[]{180,180,180}, false, xOffHalf,yOffHalf);}	
					else {													dispMenuTxtLat(falsePFlagNames[i],new int[]{0,255-pFlagColors[i][1],255-pFlagColors[i][2]}, true, xOffHalf,yOffHalf);}		
			}
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
	 * display state flag indicator at top of window
	 * @param txt
	 * @param clrAra
	 * @param state
	 * @param stMult
	 * @param yOff
	 */
	protected void dispBoolStFlag(String txt, int[] clrAra, boolean state, float stMult, float yOff){
		if(state){
			ri.setFill(clrAra, 255); 
			ri.setStroke(clrAra, 255);
		} else {
			ri.setColorValFill(IRenderInterface.gui_DarkGray,255); 
			ri.noStroke();	
		}
		ri.drawSphere(5);
		//text(""+txt,-xOff,yOff*.8f);	
		ri.showText(""+txt,stMult*txt.length(),yOff*.8f);	
	}	
	
	/**
	 * draw state booleans at top of screen and their state
	 */
	public final void drawSideBarStateBools(float yOff){ //numStFlagsToShow
		ri.translate(110,10);
		float xTrans = (int)((getMenuWidth()-100) / (1.0f*numStFlagsToShow));
		for(int idx =0; idx<numStFlagsToShow; ++idx){
			dispBoolStFlag(StateBoolNames[idx],stBoolFlagColors[idx], getStateFlagState(idx),StrWdMult[idx], yOff);			
			ri.translate(xTrans,0);
		}
	}
	
	/**
	 * called by sidebar menu to display current window's UI components
	 */
	public final void drawWindowGuiObjs(float animTimeMod){
		if(curFocusWin != -1){
			ri.pushMatState();
			dispWinFrames[curFocusWin].drawWindowGuiObjs(animTimeMod);					//draw what user-modifiable fields are currently available
			ri.popMatState();	
		}
	}//	
	
	public final void drawUI(float modAmtMillis){					
		for(int i =1; i<numDispWins; ++i){dispWinFrames[i].drawHeader(modAmtMillis);}
		//menu always idx 0
		//normal(0,0,1);
		dispWinFrames[dispMenuIDX].draw2D(modAmtMillis);
		dispWinFrames[dispMenuIDX].drawHeader(modAmtMillis);
		if(isDebugMode()){
			dispWinFrames[curFocusWin].drawUIDebugMode(dispWinFrames[dispMenuIDX].getDebugData());		
		} else if(showInfo){
			dispWinFrames[curFocusWin].drawOnscreenText();
		}
		dispWinFrames[curFocusWin].updateConsoleStrs();		
	}//drawUI
	
	
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
		for(int i =0; i<3;++i){	ri.setColorValStroke(IRenderInterface.rgbClrs[i],255);	ri.showVec(ctr,len, _axis[i]);	}
		ri.popMatState();	
	}//	drawAxes
	public final void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int[] clr, boolean drawVerts){//all axes same color
		ri.pushMatState();
			if(drawVerts){
				ri.showPtAsSphere(ctr,2,5,IRenderInterface.gui_Black,IRenderInterface.gui_Black);
				for(int i=0;i<_axis.length;++i){ri.showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),2,5,IRenderInterface.rgbClrs[i],IRenderInterface.rgbClrs[i]);}
			}
			ri.setStrokeWt(stW);ri.setStroke(clr[0],clr[1],clr[2],clr[3]);
			for(int i =0; i<3;++i){	ri.showVec(ctr,len, _axis[i]);	}
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
	// canvas functions
	
	public final myVector getDrawSNorm() {return canvas.getDrawSNorm();}
	public final myVectorf getDrawSNorm_f() {return canvas.getDrawSNorm_f();}
	public final myVector getEyeToMse() {return canvas.getEyeToMse();}
	public final myVectorf getEyeToMse_f() {return canvas.getEyeToMse_f();}

	public final myVector getUScrUpInWorld(){			myVector res = new myVector(ri.getWorldLoc(viewWidthHalf, viewHeightHalf,-.00001f),ri.getWorldLoc(viewWidthHalf, viewHeight,-.00001f));		return res._normalize();}	
	public final myVector getUScrRightInWorld(){		myVector res = new myVector(ri.getWorldLoc(viewWidthHalf, viewHeightHalf,-.00001f),ri.getWorldLoc(viewWidth, viewHeightHalf,-.00001f));		return res._normalize();}
	public final myVectorf getUScrUpInWorldf(){		myVectorf res = new myVectorf(ri.getWorldLoc(viewWidthHalf, viewHeightHalf,-.00001f),ri.getWorldLoc(viewWidthHalf,viewHeight,-.00001f));	return res._normalize();}	
	public final myVectorf getUScrRightInWorldf(){	myVectorf res = new myVectorf(ri.getWorldLoc(viewWidthHalf, viewHeightHalf,-.00001f),ri.getWorldLoc(viewWidth, viewHeightHalf,-.00001f));	return res._normalize();}
	public final myPoint getEyeLoc(){return ri.getWorldLoc(viewWidthHalf, viewHeightHalf,-.00001f);	}
	
	public final myPoint getMseLoc(){			return canvas.getMseLoc();}
	public final myPointf getMseLoc_f(){		return canvas.getMseLoc_f();}
	public final myPoint getOldMseLoc(){		return canvas.getOldMseLoc();}	
	public final myVector getMseDragVec(){	return canvas.getMseDragVec();}
	/**
	 * return a unit vector from the screen location of the mouse pointer in the world to the reticle location in the world - for ray casting onto objects the mouse is over
	 * @param glbTrans
	 * @return
	 */
	public myVector getMse2DtoMse3DinWorld(myPoint glbTrans){
		int[] mse = ri.getMouse_Raw_Int();
		myVector res = new myVector(ri.getWorldLoc(mse[0], mse[1],-.00001f),getMseLoc(glbTrans) );		
		return res._normalize();
	}
	/**
	 * relative to passed origin
	 * @param glbTrans
	 * @return
	 */
	public myPoint getMseLoc(myPoint glbTrans){			return canvas.getMseLoc(glbTrans);	}
	/**
	 * move by passed translation
	 * @param glbTrans
	 * @return
	 */
	public myPointf getTransMseLoc(myPointf glbTrans){	return canvas.getTransMseLoc(glbTrans);	}
	/**
	 * dist from mouse to passed location
	 * @param glbTrans
	 * @return
	 */
	public float getMseDist(myPointf glbTrans){			return canvas.getMseDist(glbTrans);}
	public myPoint getOldMseLoc(myPoint glbTrans){		return canvas.getOldMseLoc(glbTrans);}
	
	/**
	 * get normalized ray from eye loc to mouse loc
	 * @return
	 */
	public myVectorf getEyeToMouseRay_f() {				return canvas.getEyeToMouseRay_f();	}	
	
	/**
	 * return display string holding sreen and world mouse and eye locations 
	 */
	public final String getMseEyeInfoString(String winCamDisp) {
		myPoint mseLocPt = ri.getMouse_Raw();
		return "mse loc on screen : " + mseLocPt + " mse loc in world :"+ canvas.getMseLoc() +"  Eye loc in world :"+ canvas.getEyeInWorld()+ winCamDisp;
	}
	
	/**
	 * Get label of sidebar menu button specified by row and column
	 * @param row
	 * @param col
	 * @return
	 */
	public final String getSidebarMenuButtonLabel(int row, int col) {
		return ((SidebarMenu) dispWinFrames[dispMenuIDX]).getSidebarMenuButtonLabel(row,col);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// showing functions
	
	/**
	 * this will properly format and display a string of text, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText_RightSideMenu(int[] tclr, float mult,  String txt) {
		ri.setFill(tclr,tclr[3]);ri.setStroke(tclr,tclr[3]);
		ri.showText(txt,0.0f,0.0f,0.0f);
		ri.translate(txt.length()*mult, 0.0f,0.0f);		
	}
	
	public final void showOffsetText(float d, int tclr, String txt){
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt, d, d,d); 
	}	
	public final void showOffsetText(myPointf loc, int tclr, String txt){
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt, loc.x, loc.y, loc.z); 
	}	
	public final void showOffsetText2D(float d, int tclr, String txt){
		ri.setColorValFill(tclr, 255);ri.setColorValStroke(tclr, 255);
		ri.showText(txt, d, d,0); 
	}
		
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
	
	//translate to point, draw point and text
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		ri.pushMatState();  
		ri.setFill(fclr,255); 
		ri.setStroke(strkclr,255);		
		ri.translate(P.x,P.y,P.z); 
		ri.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	//textP is location of text relative to point
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		ri.pushMatState();  
		ri.translate(P.x,P.y,P.z); 
		ri.setFill(fclr,255); 
		ri.setStroke(strkclr,255);			
		ri.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(txtP,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	//textP is location of text relative to point
	public final void showCrclNoBox_ClrAra(myPointf P, float rad, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		ri.pushMatState();  
		ri.translate(P.x,P.y,P.z); 
		if((fclr!= null) && (strkclr!= null)){ri.setFill(fclr,255); ri.setStroke(strkclr,255);}		
		ri.drawEllipse2D(0,0,rad,rad); 
		ri.drawEllipse2D(0,0,2,2);
		showOffsetText(txtP,tclr, txt);
		ri.popMatState();
	} // render sphere of radius r and center P)
	
	//show sphere of certain radius
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
		for(int i =0;i<winDispIdxXOR.length;++i){		dispWinFrames[winDispIdxXOR[i]].setRectDimsY( dispWinFrames[popUpWinIDX].getRectDim(1));	}						
	}
	/**
	 * allow only 1 window to display
	 * @param idx idx of window whose display is being modified
	 * @param val whether window idx is turned on(true) or off(false)
	 */
	public final void setWinFlagsXOR(int idx, boolean val){
		if(val){//turning one on
			//turn off not shown, turn on shown				
			for(int i =0;i<winDispIdxXOR.length;++i){//check windows that should be mutually exclusive during display
				if(winDispIdxXOR[i]!= idx){
					dispWinFrames[winDispIdxXOR[i]].setShow(false);
					handleShowWin(i ,0,false); 
					forceVisFlag(winFlagsXOR[i], false);
				}//not this window
				else {//turning on this one
					dispWinFrames[idx].setShow(true);
					handleShowWin(i ,1,false); 
					forceVisFlag(winFlagsXOR[i], true);
					curFocusWin = winDispIdxXOR[i];
					//setCamView();	//camera now handled by individual windows
					dispWinFrames[idx].setInitCamView();
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
	public final void checkAndSetSACKeys(int keyCode) {
		if(!shiftIsPressed()){setShiftPressed(keyCode  == 16);} //16 == KeyEvent.VK_SHIFT
		if(!cntlIsPressed()){setCntlPressed(keyCode  == 17);}//17 == KeyEvent.VK_CONTROL			
		if(!altIsPressed()){setAltPressed(keyCode  == 18);}//18 == KeyEvent.VK_ALT
	}
	
	public final void checkKeyReleased(boolean keyIsCoded, int keyCode) {
		if(getBaseFlag(valueKeyPressed)) {endValueKeyPressed();}
		if(keyIsCoded) {
			if((getBaseFlag(shiftKeyPressed)) && (keyCode == 16)){endShiftKey();}
			if((getBaseFlag(cntlKeyPressed)) && (keyCode == 17)){endCntlKey();}
			if((getBaseFlag(altKeyPressed)) && (keyCode == 18)){endAltKey();}
		}
	}
	
	/**
	 * send key presses to all windows
	 * @param key
	 * @param keyCode
	 */
	public final void sendKeyPressToWindows(char key, int keyCode) {
		handleKeyPress(key,keyCode);	//handle all other (non-numeric) keys in instancing class
		for(int i =0; i<numDispWins; ++i){dispWinFrames[i].setValueKeyPress(key, keyCode);}
		setValueKeyPressed(true);
	}
	public final void setShiftPressed(boolean val) {setBaseFlag(shiftKeyPressed,val);}
	public final void setAltPressed(boolean val) {setBaseFlag(altKeyPressed,val);}
	public final void setCntlPressed(boolean val) {setBaseFlag(cntlKeyPressed,val);}
	public final void setValueKeyPressed(boolean val){setBaseFlag(valueKeyPressed,val);}
	//modview tied to shift key
	private void endShiftKey(){			clearBaseFlags(new int []{shiftKeyPressed, modView});	for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endShiftKey();}}
	private void endAltKey(){			setAltPressed(false);				for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endAltKey();}}
	private void endCntlKey(){			setCntlPressed(false);				for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endCntlKey();}}
	private void endValueKeyPressed() {	setValueKeyPressed(false);			for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endValueKeyPress();}}

	/**
	 * get the ui rect values of the "master" ui region (another window) -> this is so ui objects of one window can be made, clicked, and shown displaced from those of the parent window
	 * @param idx
	 * @return
	 */
	public abstract float[] getUIRectVals(int idx);
	
	/**
	 * return a list of labels to apply to mse-over display select buttons - an empty or null list will not display option
	 * @return
	 */
	public abstract String[] getMouseOverSelBtnLabels();
	
	
	//find mouse "force" exerted upon a particular location - distance from mouse to passed location
	public myVectorf mouseForceAtLoc(float msClickForce, myPointf _loc, boolean attractMode){
		myPointf mouseFrcLoc = getTransMseLoc(new myPointf(gridDimX/2.0f, gridDimY/2.0f,gridDimZ/2.0f));// new myPointf(c.dfCtr.x+gridDimX/2.0f,c.dfCtr.y+gridDimY/2.0f,c.dfCtr.z+gridDimZ/2.0f);// new myVector(lstClkX,0,lstClkY);//translate click location to where the space where the boids are	
		myVectorf resFrc = new myVectorf(_loc, mouseFrcLoc);		
		float sqDist = resFrc.sqMagn;
		if(sqDist<MyMathUtils.EPS_F){sqDist=MyMathUtils.EPS_F;}
		float mag = (attractMode? 1 : -1) * msClickForce / sqDist;
		resFrc._scale(mag);
		return resFrc;	
	}//mouseForceAtLoc
	
	
	public final void mouseMoved(int mouseX, int mouseY){for(int i =0; i<numDispWins; ++i){if (dispWinFrames[i].handleMouseMove(mouseX, mouseY)){return;}}}
	public final void mousePressed(int mouseX, int mouseY,boolean isLeft, boolean isRight) {
		setBaseFlag(mouseClicked, true);
		if(isLeft){							myMouseClicked(mouseX, mouseY,0);} 
		else if (isRight) {						myMouseClicked(mouseX, mouseY,1);}
		//for(int i =0; i<numDispWins; ++i){	if (dispWinFrames[i].handleMouseClick(mouseX, mouseY,c.getMseLoc(sceneCtrVals[sceneIDX]))){	return;}}
	}// mousepressed		
	private void myMouseClicked(int mouseX, int mouseY, int mseBtn){ 	for(int i =0; i<numDispWins; ++i){if (dispWinFrames[i].handleMouseClick(mouseX, mouseY,mseBtn)){return;}}}
	
	public final void mouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, boolean isLeft, boolean isRight){
		myVector drag = canvas.getMseDragVec();
		
		if(isLeft){							myMouseDragged(mouseX, mouseY, pmouseX, pmouseY,drag,0);}
		else if (isRight) {						myMouseDragged(mouseX, mouseY, pmouseX, pmouseY,drag,1);}
	}//mouseDragged()
	private void myMouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, myVector drag, int mseBtn){	for(int i =0; i<numDispWins; ++i){if (dispWinFrames[i].handleMouseDrag(mouseX, mouseY, pmouseX, pmouseY,drag,mseBtn)) {return;}}}
	/**
	 * currently only for zooming
	 * @param ticks amount of wheel moves
	 */
	public final void mouseWheel(int ticks) {
		if(dispWinFrames.length < 1) {return;}
		if (dispWinFrames[curFocusWin].dispFlags.getCanChgView()) {// (canMoveView[curFocusWin]){	
			float mult = (getBaseFlag(shiftKeyPressed)) ? 50.0f * mouseWhlSens : 10.0f*mouseWhlSens;
			dispWinFrames[curFocusWin].handleViewChange(true,(mult * ticks),0);
		}
	}

	public final void mouseReleased(){
		clearBaseFlags(new int[]{mouseClicked, modView});
		for(int i =0; i<numDispWins; ++i){dispWinFrames[i].handleMouseRelease();}
		setBaseFlag(drawing, false);
		//c.clearMsDepth();
	}//mouseReleased

	//determine primary application flags that are actually being displayed or not displayed
	private final void _setBaseFlagToShow(int idx, boolean val) {
		HashMap<Integer, Integer> tmpMapOfFlags = new HashMap<Integer, Integer>();
		for(Integer flag : flagsToShow) {			tmpMapOfFlags.put(flag, 0);		}
		if(val) {tmpMapOfFlags.put(idx, 0);	} else {tmpMapOfFlags.remove(idx);}
		flagsToShow = new ArrayList<Integer>(tmpMapOfFlags.keySet());
		numFlagsToShow = flagsToShow.size();
	}//_setBaseFlagToShow
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// state and control flag handling
	/**
	 * init boolean state machine flags for visible flags
	 */
	private final void initVisFlags(){
		int numVisFlags = getNumVisFlags();
		_visFlags = new int[1 + numVisFlags/32];for(int i =0; i<numVisFlags;++i){forceVisFlag(i,false);}	
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
	public final void setVisFlag(int idx, boolean val ){
		int flIDX = idx/32, mask = 1<<(idx%32);
		_visFlags[flIDX] = (val ?  _visFlags[flIDX] | mask : _visFlags[flIDX] & ~mask);
		setVisFlag_Indiv(idx, val);
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
	public final boolean getVisFlag(int idx){int bitLoc = 1<<(idx%32);return (_visFlags[idx/32] & bitLoc) == bitLoc;}		
	
	/**
	 * this will not execute the code in setVisFlag, which might cause a loop
	 * @param idx
	 * @param val
	 */
	public final void forceVisFlag(int idx, boolean val) {
		int flIDX = idx/32, mask = 1<<(idx%32);
		_visFlags[flIDX] = (val ?  _visFlags[flIDX] | mask : _visFlags[flIDX] & ~mask);
		//doesn't perform any other ops - to prevent looping
	}
	
	/**
	 * determine whether polled window is being shown currently
	 * @param i
	 * @return
	 */
	public final boolean isShowingWindow(int i){return getVisFlag(i);}//showUIMenu is first flag of window showing flags, visFlags are defined in instancing class

	//base class flags init
	protected final void initBaseFlags(){_baseFlags = new int[1 + numBaseFlags/32];for(int i =0; i<numBaseFlags;++i){forceBaseFlag(i,false);}}		
	//set baseclass flags  //setBaseFlag(showIDX, 
	protected final void setBaseFlag(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		_baseFlags[flIDX] = (val ?  _baseFlags[flIDX] | mask : _baseFlags[flIDX] & ~mask);
		switch(idx){
			case debugMode 			: { break;}//anything special for debugMode 	
			case finalInitDone		: { break;}//flag to handle long setup - processing seems to time out if setup takes too long, so this will continue setup in the first draw loop
			case saveAnim 			: { break;}//anything special for saveAnim 			
			case altKeyPressed 		: { break;}//anything special for altKeyPressed 	
			case shiftKeyPressed 	: { break;}//anything special for shiftKeyPressed 	
			case cntlKeyPressed		: { break;}
			case mouseClicked 		: { break;}//anything special for mouseClicked 		
			case modView	 		: { break;}//anything special for modView	 	
			case drawing			: { break;}
			case runSim				: { break;}// handleTrnsprt((val ? 2 : 1) ,(val ? 1 : 0),false); break;}		//anything special for runSim	
			case showRtSideMenu		: {	for(int i =1; i<dispWinFrames.length;++i){dispWinFrames[i].setRtSideInfoWinSt(val);}break;}	//set value for every window - to show or not to show info window
			case flipDrawnTraj		: { for(int i =1; i<dispWinFrames.length;++i){dispWinFrames[i].rebuildAllDrawnTrajs();}break;}						//whether or not to flip the drawn melody trajectory, width-wise
			case singleStep 		: { break;}
			case clearBKG			: { break;}
		}				
	}//setBaseFlag
	//force base flag - bypass any window setting
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
		int flagIDX = flagsToShow.get(i);
		setBaseFlag(flagIDX,!getBaseFlag(flagIDX));
	}	
	/**
	 * get the current state (T/F) of state flags (Such as if shift is pressed or not) specified by idx in stateFlagsToShow List
	 * @param idx
	 * @return
	 */
	public final boolean getStateFlagState(int idx) {return getBaseFlag(stateFlagsToShow.get(idx));}
	/**
	 * get number of main flags to display in right side menu
	 * @return
	 */
	public final int getNumFlagsToShow() {return numFlagsToShow;}
	
	public final boolean isDebugMode() {return getBaseFlag(debugMode);}
	
	public final boolean isFinalInitDone() {return getBaseFlag(finalInitDone);}
	public final boolean isRunSim() {return getBaseFlag(runSim);}
	public final boolean isSingleStep() {return getBaseFlag(singleStep);}
	public final boolean doSaveAnim() {return getBaseFlag(saveAnim);}
	public final boolean doFlipTraj() {return getBaseFlag(flipDrawnTraj);}
	public final boolean doShowRtSideMenu() {return getBaseFlag(showRtSideMenu);}	
	
	public final boolean shiftIsPressed() {return getBaseFlag(shiftKeyPressed);}
	public final boolean altIsPressed() {return getBaseFlag(altKeyPressed);}
	public final boolean cntlIsPressed() {return getBaseFlag(cntlKeyPressed);}
	public final boolean mouseIsClicked() {return getBaseFlag(mouseClicked);}
	public final boolean IsModView() {return getBaseFlag(modView);}
	public final boolean IsDrawing() {return getBaseFlag(drawing);}
	
	//display window flags
	public final boolean dispWinCanDrawInWin(int wIdx) {return dispWinFlags[wIdx][dispCanDrawInWinIDX];}
	public final boolean dispWinCanShow3dbox(int wIdx) {return dispWinFlags[wIdx][dispCanShow3dboxIDX];}
	public final boolean dispWinCanMoveView(int wIdx) {return dispWinFlags[wIdx][dispCanMoveViewIDX];}
	public final boolean dispWinIs3D(int wIdx) {return dispWinFlags[wIdx][dispWinIs3dIDX];}
	
	public final boolean curDispWinCanDrawInWin() {return dispWinFlags[curFocusWin][dispCanDrawInWinIDX];}
	public final boolean curDispWinCanShow3dbox() {return dispWinFlags[curFocusWin][dispCanShow3dboxIDX];}
	public final boolean curDispWinCanMoveView() {return dispWinFlags[curFocusWin][dispCanMoveViewIDX];}
	public final boolean curDispWinIs3D() {return dispWinFlags[curFocusWin][dispWinIs3dIDX];}
	
	public final void setSimIsRunning(boolean val) {setBaseFlag(runSim,val);}
	public final void toggleSimIsRunning() {setBaseFlag(runSim, !getBaseFlag(runSim));}
	public final void setSimIsSingleStep(boolean val) {setBaseFlag(singleStep,val);}
	public final void setShowRtSideMenu(boolean val) {setBaseFlag(showRtSideMenu,val);}
	public final void setClearBackgroundEveryStep(boolean val) {setBaseFlag(clearBKG,val);}
		
	public final void setMouseClicked(boolean val) {setBaseFlag(mouseClicked,val);}
	public final void setModView(boolean val) {setBaseFlag(modView,val);}
	public final void setIsDrawing(boolean val) {setBaseFlag(drawing,val);}
	public final void setFinalInitDone(boolean val) {setBaseFlag(finalInitDone, val);}	
	public final void setSaveAnim(boolean val) {setBaseFlag(saveAnim, val);}
	public final void toggleSaveAnim() {setBaseFlag(saveAnim, !getBaseFlag(saveAnim));}
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// flags to show
	protected final void setBaseFlagToShow_debugMode(boolean val) {_setBaseFlagToShow(debugMode, val);}
	protected final void setBaseFlagToShow_saveAnim(boolean val) {_setBaseFlagToShow(saveAnim, val);}
	protected final void setBaseFlagToShow_runSim(boolean val) {_setBaseFlagToShow(runSim, val);}
	protected final void setBaseFlagToShow_singleStep(boolean val) {_setBaseFlagToShow(singleStep, val);}
	protected final void setBaseFlagToShow_showRtSideMenu(boolean val) {_setBaseFlagToShow(showRtSideMenu, val);}	
	
		
	public abstract double clickValModMult();
	public abstract boolean isClickModUIVal();

	public float getMenuWidth() {return menuWidth;}
	
	public Base_DispWindow getCurFocusDispWindow() {return dispWinFrames[curFocusWin];}	

	public SidebarMenu getSideBarMenuWindow() {return ((SidebarMenu)dispWinFrames[dispMenuIDX]);}
	public String getCurFocusDispWindowName() {return getDispWindowName(curFocusWin);}
	public String getDispWindowName(int idx) { 
		if ((idx >= 0) && (idx < dispWinFrames.length)){
			return dispWinFrames[idx].getName();
		}
		return "None";
	}
	/**
	 * performs shuffle on list of strings. Moves from end of list, picks string i, finds random string j [0,i] and swaps if i!=j
	 * @param _list
	 * @param type
	 * @return
	 */
	public String[] shuffleStrList(String[] _list, String type){
		String tmp = "";
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		for(int i=(_list.length-1);i>0;--i){
			int j = (int)(tr.nextDouble(0,(i+1)));
			if (i==j) {continue;}
			tmp = _list[i];
			_list[i] = _list[j];
			_list[j] = tmp;
		}
		getCurFocusDispWindow().getMsgObj().dispInfoMessage(getPrjNmShrt(), "shuffleStrList","String list of object " + type + " shuffled");
		return _list;
	}//shuffleStrList
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// calculations
	
	/**
	 * build a frame based on passed normal given two passed points
	 * @param A
	 * @param B
	 * @param I normal to build frame around
	 * @return vec array of {AB, Normal, Tangent}
	 */
	public myVector[] buildFrameAroundNormal(myPoint A, myPoint B, myVector norm) {
		myVector V = new myVector(A,B);
		myVector tan = norm._cross(V)._normalize(); 
		return new myVector[] {V,norm,tan};		
	}
	
	/**
	 * build a frame based on passed normal given two passed points
	 * @param A
	 * @param B
	 * @param I normal to build frame around
	 * @return vec array of {AB, Normal, Tangent}
	 */
	public myVectorf[] buildFrameAroundNormal(myPointf A, myPointf B, myVectorf norm) {
		myVectorf V = new myVectorf(A,B);
		myVectorf tan = norm._cross(V)._normalize(); 
		return new myVectorf[] {V,norm,tan};		
	}
	
	/**
	 * build a frame based on world orientation given two passed points
	 * @param A
	 * @param B
	 * @return vec array of {AB, ScreenNorm, ScreenTan}
	 */
	public myVector[] buildViewBasedFrame(myPoint A, myPoint B) {
		return buildFrameAroundNormal(A, B, getDrawSNorm());		
	}
	
	/**
	 * build a frame based on world orientation given two passed points
	 * @param A
	 * @param B
	 * @param I Screen normal
	 * @return float vec array of {AB, ScreenNorm, ScreenTan}
	 */
	public myVectorf[] buildViewBasedFrame(myPointf A, myPointf B) {
		return buildFrameAroundNormal(A, B, getDrawSNorm_f());
	}
		
	/**
	 * Derive the points of a cylinder of radius r around axis through A and B
	 * @param A center point of endcap
	 * @param B center point of endcap
	 * @param r desired radius of cylinder
	 * @return array of points for cylinder
	 */
	public myPoint[] buildCylVerts(myPoint A, myPoint B, double r) {
		myVector[] frame = buildFrameAroundNormal(A, B, getDrawSNorm());
		myPoint[] resList = new myPoint[2 * MyMathUtils.preCalcCosVals.length];
		double rca, rsa;
		int idx = 0;
		for(int i = 0; i<MyMathUtils.preCalcCosVals.length; ++i) {
			rca = r*MyMathUtils.preCalcCosVals[i];
			rsa = r*MyMathUtils.preCalcCosVals[i];
			resList[idx++] = myPoint._add(A,rca,frame[1],rsa,frame[2]); 
			resList[idx++] = myPoint._add(A,rca,frame[1],rsa,frame[2],1,frame[0]);				
		}
		return resList;
	}//build list of all cylinder vertices 
	
	/**
	 * Derive the points of a cylinder of radius r around axis through A and B
	 * @param A center point of endcap
	 * @param B center point of endcap
	 * @param r desired radius of cylinder
	 * @return array of points for cylinder
	 */
	public myPointf[] buildCylVerts(myPointf A, myPointf B, float r) {
		myVectorf[] frame = buildFrameAroundNormal(A, B, getDrawSNorm_f());
		myPointf[] resList = new myPointf[2 * MyMathUtils.preCalcCosVals.length];
		float rca, rsa;
		int idx = 0;
		for(int i = 0; i<MyMathUtils.preCalcCosVals.length; ++i) {
			rca = r*MyMathUtils.preCalcCosVals_f[i];
			rsa = r*MyMathUtils.preCalcSinVals_f[i];
			resList[idx++] = myPointf._add(A,rca,frame[1],rsa,frame[2]); 
			resList[idx++] = myPointf._add(A,rca,frame[1],rsa,frame[2],1,frame[0]);				
		}	
		return resList;
	}//build list of all cylinder vertices 
	
	/**
	 * Build a set of n points inscribed on a circle centered at p in plane I,J
	 * @param p center point
	 * @param r circle radius
	 * @param I, J axes of plane
	 * @param n # of points
	 * @return array of n equal-arc-length points centered around p
	 */
	public synchronized myPoint[] buildCircleInscribedPoints(myPoint p, double r, myVector I, myVector J, int n) {
		myPoint[] pts = new myPoint[n];
		pts[0] = new myPoint(p,r,myVector._unit(I));
		double a = (MyMathUtils.TWO_PI)/(1.0*n); 
		for(int i=1;i<n;++i){pts[i] = pts[i-1].rotMeAroundPt(a,J,I,p);}
		return pts;
	}
	/**
	 * Build a set of n points inscribed on a circle centered at p in plane I,J
	 * @param p center point
	 * @param r circle radius
	 * @param I, J axes of plane
	 * @param n # of points
	 * @return array of n equal-arc-length points centered around p
	 */
	public synchronized myPointf[] buildCircleInscribedPoints(myPointf p, float r, myVectorf I, myVectorf J, int n) {
		myPointf[] pts = new myPointf[n];
		pts[0] = new myPointf(p,r,myVectorf._unit(I));
		float a = (MyMathUtils.TWO_PI_F)/(1.0f*n);
		for(int i=1;i<n;++i){pts[i] = pts[i-1].rotMeAroundPt(a,J,I,p);}
		return pts;
	}
	
	
	public final myPoint PtOnSpiral(myPoint A, myPoint B, myPoint C, double t) {
		//center is coplanar to A and B, and coplanar to B and C, but not necessarily coplanar to A, B and C
		//so center will be coplanar to mp(A,B) and mp(B,C) - use mpCA midpoint to determine plane mpAB-mpBC plane?
		myPoint mAB = new myPoint(A,.5, B), mBC = new myPoint(B,.5, C), mCA = new myPoint(C,.5, A);
		myVector mI = myVector._unit(mCA,mAB), mTmp = myVector._cross(mI,myVector._unit(mCA,mBC)), mJ = myVector._unit(mTmp._cross(mI));	//I and J are orthonormal
		double a =spiralAngle(A,B,B,C), s =spiralScale(A,B,B,C);
		
		//myPoint G = spiralCenter(a, s, A, B, mI, mJ); 
		myPoint G = spiralCenter(A, mAB, B, mBC); 
		//return new myPoint(G, Math.pow(s,t), R(A,t*a,mI,mJ,G));
		return new myPoint(G, Math.pow(s,t), A.rotMeAroundPt(t*a,mI,mJ,G));
	}	
	public double spiralAngle(myPoint A, myPoint B, myPoint C, myPoint D) {return myVector._angleBetween(new myVector(A,B),new myVector(C,D));}
	public double spiralScale(myPoint A, myPoint B, myPoint C, myPoint D) {return myPoint._dist(C,D)/ myPoint._dist(A,B);}

	// spiral given 4 points, AB and CD are edges corresponding through rotation
	public final myPoint spiralCenter(myPoint A, myPoint B, myPoint C, myPoint D) {         // new spiral center
		myVector AB=new myVector(A,B), CD=new myVector(C,D), AC=new myVector(A,C);
		double m=CD.magn/AB.magn, n=CD.magn*AB.magn;		
		myVector rotAxis = myVector._unit(AB._cross(CD));		//expect ab and ac to be coplanar - this is the axis to rotate around to find f
		
		myVector rAB = myVector._rotAroundAxis(AB, rotAxis, MyMathUtils.HALF_PI_F);
		double c=AB._dot(CD)/n,	s=rAB._dot(CD)/n;
		double AB2 = AB._dot(AB), a=AB._dot(AC)/AB2, b=rAB._dot(AC)/AB2, x=(a-m*( a*c+b*s)), y=(b-m*(-a*s+b*c)), d=1+m*(m-2*c);  if((c!=1)&&(m!=1)) { x/=d; y/=d; };
		return new myPoint(new myPoint(A,x,AB),y,rAB);
	}

	public final myPointf PtOnSpiral(myPointf A, myPointf B, myPointf C, float t) {
		//center is coplanar to A and B, and coplanar to B and C, but not necessarily coplanar to A, B and C
		//so center will be coplanar to mp(A,B) and mp(B,C) - use mpCA midpoint to determine plane mpAB-mpBC plane?
		myPointf mAB = new myPointf(A,.5f, B), mBC = new myPointf(B,.5f, C), mCA = new myPointf(C,.5f, A);
		myVectorf mI = myVectorf._unit(mCA,mAB), mTmp = myVectorf._cross(mI,myVectorf._unit(mCA,mBC)), mJ = myVectorf._unit(mTmp._cross(mI));	//I and J are orthonormal
		float a =spiralAngle(A,B,B,C), s =spiralScale(A,B,B,C);
		
		//myPoint G = spiralCenter(a, s, A, B, mI, mJ); 
		myPointf G = spiralCenter(A, mAB, B, mBC); 
		return new myPointf(G, (float)Math.pow(s,t), A.rotMeAroundPt(t*a,mI,mJ,G));
	}	
	public float spiralAngle(myPointf A, myPointf B, myPointf C, myPointf D) {return myVectorf._angleBetween(new myVectorf(A,B),new myVectorf(C,D));}
	public float spiralScale(myPointf A, myPointf B, myPointf C, myPointf D) {return myPointf._dist(C,D)/ myPointf._dist(A,B);}
	
	// spiral given 4 points, AB and CD are edges corresponding through rotation
	public final myPointf spiralCenter(myPointf A, myPointf B, myPointf C, myPointf D) {         // new spiral center
		myVectorf AB=new myVectorf(A,B), CD=new myVectorf(C,D), AC=new myVectorf(A,C);
		float m=CD.magn/AB.magn, n=CD.magn*AB.magn;		
		myVectorf rotAxis = myVectorf._unit(AB._cross(CD));		//expect ab and ac to be coplanar - this is the axis to rotate around to find f
		
		myVectorf rAB = myVectorf._rotAroundAxis(AB, rotAxis, MyMathUtils.HALF_PI_F);
		float c=AB._dot(CD)/n,	s=rAB._dot(CD)/n;
		float AB2 = AB._dot(AB), a=AB._dot(AC)/AB2, b=rAB._dot(AC)/AB2, x=(a-m*( a*c+b*s)), y=(b-m*(-a*s+b*c)), d=1+m*(m-2*c);  if((c!=1)&&(m!=1)) { x/=d; y/=d; };
		return new myPointf(new myPointf(A,x,AB),y,rAB);
	}

	/**
	 * Return intersection point of vector T through point E in plane described by ABC
	 * @param E point within cast vector/ray
	 * @param T directional vector/ray
	 * @param A point describing plane
	 * @param B point describing plane
	 * @param C point describing plane
	 * @return
	 */
	public final myPoint intersectPl(myPoint E, myVector T, myPoint A, myPoint B, myPoint C) {
		//vector through point and planar point
		myVector EA=new myVector(E,A); 
		//planar vectors
		myVector AB=new myVector(A,B), AC=new myVector(A,C);
		//find planar norm
		myVector ACB = AC._cross(AB);
		//project 
		double t = (float)(EA._dot(ACB) / T._dot(ACB));		
		return (myPoint._add(E,t,T));		
	}//intersectPl
	
	/**
	 * if ray from E along V intersects sphere at C with radius r, return t when intersection occurs
	 * @param E
	 * @param V
	 * @param C
	 * @param r
	 * @return t value along vector V where first intersection occurs
	 */
	public double intersectPt(myPoint E, myVector V, myPoint C, double r) { 
		myVector Vce = new myVector(C,E);
		double ta = 2 * V._dot(V),
				b = 2 * V._dot(Vce), 
				c = Vce._dot(Vce) - (r*r),
				radical = (b*b) - 2 *(ta) * c;		//b^2 - 4ac
		if(radical < 0) return -1;
		double sqrtRad = Math.sqrt(radical);
		double t1 = (b + sqrtRad)/ta, t2 = (b - sqrtRad)/ta;
		if (t1 < t2) {return t1 > 0 ? t1 : t2;}	
		return t2 > 0 ? t2 : t1;	
		//return ((t1 > 0) && (t2 > 0) ? MyMathUtils.min(t1, t2) : ((t1 < 0 ) ? ((t2 < 0 ) ?-1 : t2) : t1) );
	}	
	
	private static final double third = 1.0/3.0;
	/**
	 * Find a random position in a sphere centered at 0 of radius rad, using spherical coords as rand axes
	 * @param rad
	 * @return
	 */
	public final myPointf getRandPosInSphere(double rad){ return getRandPosInSphere(rad, new myPointf());}
	/**
	 * Find a random position in a sphere centered at ctr of radius rad, using spherical coords as rand axes
	 * @param rad
	 * @param ctr
	 * @return
	 */
	public final myPointf getRandPosInSphere(double rad, myPointf ctr){
		myPointf pos = new myPointf();
		double u = ThreadLocalRandom.current().nextDouble(0,1),	
			cosTheta = ThreadLocalRandom.current().nextDouble(-1,1),
			phi = ThreadLocalRandom.current().nextDouble(0,MyMathUtils.TWO_PI_F),
			r = rad * Math.pow(u, third),
			rSinTheta = r * (Math.sqrt(1.0 - (cosTheta * cosTheta)));			
		pos.set(rSinTheta * Math.cos(phi), rSinTheta * Math.sin(phi),cosTheta*r);
		pos._add(ctr);
		return pos;
	}
	/**
	 * Find a random position on a sphere's surface centered at 0 of radius rad, using spherical coords as rand axes
	 * @param rad
	 * @return
	 */
	public final myPointf getRandPosOnSphere(double rad){ return getRandPosOnSphere(rad, new myPointf());}
	/**
	 * Find a random position on a sphere's surface centered at ctr of radius rad, using spherical coords as rand axes
	 * @param rad
	 * @param ctr
	 * @return
	 */
	public final myPointf getRandPosOnSphere(double rad, myPointf ctr){
		myPointf pos = new myPointf();
		double 	cosTheta = ThreadLocalRandom.current().nextDouble(-1,1),
				phi = ThreadLocalRandom.current().nextDouble(0,MyMathUtils.TWO_PI_F), 
				rSinTheta = rad* (Math.sqrt(1.0 - (cosTheta * cosTheta)));
		pos.set(rSinTheta * Math.cos(phi), rSinTheta * Math.sin(phi),cosTheta * rad);
		pos._add(ctr);
		return pos;
	}
	/**
	 * random location within coords[0] and coords[1] extremal corners of a cube - bnds is to give a margin of possible random values
	 * @param coords
	 * @param bnds
	 * @return
	 */
	public myVectorf getRandPosInCube(float[][] coords, float bnds){
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		return new myVectorf(
				tr.nextDouble(coords[0][0]+bnds,(coords[0][0] + coords[1][0] - bnds)),
				tr.nextDouble(coords[0][1]+bnds,(coords[0][1] + coords[1][1] - bnds)),
				tr.nextDouble(coords[0][2]+bnds,(coords[0][2] + coords[1][2] - bnds)));}		
	
	/** 
	 * convert from spherical coords to cartesian. Returns Array :
	 * 	idx0 : norm of vector through point from origin
	 * 	idx1 : point
	 * @param rad
	 * @param thet
	 * @param phi
	 * @param scaleZ scaling factor to make ellipsoid
	 * @return ara : norm, surface point == x,y,z of coords passed
	 */
	public myVectorf[] getXYZFromRThetPhi(double rad, double thet, double phi, double scaleZ) {
		double sinThet = Math.sin(thet);	
		myVectorf[] res = new myVectorf[2];
		res[1] = new myVectorf(sinThet * Math.cos(phi) * rad, sinThet * Math.sin(phi) * rad,Math.cos(thet)*rad*scaleZ);
		res[0] = myVectorf._normalize(res[1]);
		return res;
	}//
	
	
	/** 
	 * builds a list of N regularly placed vertices and normals for a sphere of radius rad centered at ctr
	 * @param rad radius of sphere
	 * @param N # of verts we want in result
	 * @param scaleZ scaling factor for ellipsoid
	 * @return list of points (as vectors) where each entry is a tuple of norm/point
	 */
	public myVectorf[][] getRegularSphereList(float rad, int N, float scaleZ) {
		ArrayList<myVectorf[]> res = new ArrayList<myVectorf[]>();
		//choose 1 point per dArea, where dArea is area of sphere parsed into N equal portions
		double lclA = 4*MyMathUtils.PI/N, lclD = Math.sqrt(lclA);
		int Mthet = (int) Math.round(MyMathUtils.PI/lclD), Mphi;
		double dThet = MyMathUtils.PI/Mthet, dPhi = lclA/dThet, thet, phi, twoPiOvDPhi = MyMathUtils.TWO_PI/dPhi;
		for(int i=0;i<Mthet;++i) {
			thet = dThet * (i + 0.5f);
			Mphi = (int) Math.round(twoPiOvDPhi * Math.sin(thet));
			for (int j=0;j<Mphi; ++j) { 
				phi = (MyMathUtils.TWO_PI*j)/Mphi;		
				res.add(getXYZFromRThetPhi(rad, thet, phi, scaleZ));
			}
		}
		return res.toArray(new myVectorf[0][]);
	}//getRegularSphereList	
	
	
	public final myPoint bndChkInBox2D(myPoint p){p.set(MyMathUtils.max(0,MyMathUtils.min(p.x,grid2D_X)),MyMathUtils.max(0,MyMathUtils.min(p.y,grid2D_Y)),0);return p;}
	public final myPoint bndChkInBox3D(myPoint p){p.set(MyMathUtils.max(0,MyMathUtils.min(p.x,gridDimX)), MyMathUtils.max(0,MyMathUtils.min(p.y,gridDimY)),MyMathUtils.max(0,MyMathUtils.min(p.z,gridDimZ)));return p;}	
	public final myPoint bndChkInCntrdBox3D(myPoint p){
		p.set(MyMathUtils.max(-hGDimX,MyMathUtils.min(p.x,hGDimX)), 
				MyMathUtils.max(-hGDimY,MyMathUtils.min(p.y,hGDimY)),
				MyMathUtils.max(-hGDimZ,MyMathUtils.min(p.z,hGDimZ)));return p;}	
	
	
	//very fast mechanism for setting an array of doubles to a specific val - takes advantage of caching
	public final void dAraFill(double[] ara, double val){
		int len = ara.length;
		if (len > 0){ara[0] = val; }
		for (int i = 1; i < len; i += i){  System.arraycopy(ara, 0, ara, i, ((len - i) < i) ? (len - i) : i);  }		
	}
	
	//convert a world location within the bounded cube region to be a 4-int color array
	public final int[] getClrFromCubeLoc(float[] t){
		return new int[]{(int)(255*(t[0]-cubeBnds[0][0])/cubeBnds[1][0]),(int)(255*(t[1]-cubeBnds[0][1])/cubeBnds[1][1]),(int)(255*(t[2]-cubeBnds[0][2])/cubeBnds[1][2]),255};
	}
	
	//convert a world location within the bounded cube region to be a 4-int color array
	public final int[] getClrFromCubeLoc(myPointf t){
		return new int[]{(int)(255*(t.x-cubeBnds[0][0])/cubeBnds[1][0]),(int)(255*(t.y-cubeBnds[0][1])/cubeBnds[1][1]),(int)(255*(t.z-cubeBnds[0][2])/cubeBnds[1][2]),255};
	}
	
	/**
	 * any instancing-class-specific colors - colorVal set to be higher than IRenderInterface.gui_OffWhite
	 * @param colorVal
	 * @param alpha
	 * @return
	 */
	public abstract int[] getClr_Custom(int colorVal, int alpha);

	
	/**
	 * display the current memory setup
	 */
	public final void checkMemorySetup() {
		Runtime runtime = Runtime.getRuntime();  
		long maxMem = runtime.maxMemory(), allocMem = runtime.totalMemory(), freeMem = runtime.freeMemory();
		getCurFocusDispWindow().getMsgObj().dispInfoMessage(getPrjNmShrt(), "checkMemorySetup","Free memory: " + freeMem / 1024.0f);  
		getCurFocusDispWindow().getMsgObj().dispInfoMessage(getPrjNmShrt(), "checkMemorySetup","Allocated memory: " + allocMem / 1024.0f);  
		getCurFocusDispWindow().getMsgObj().dispInfoMessage(getPrjNmShrt(), "checkMemorySetup","Max memory: " + maxMem /1024.0f);  
		getCurFocusDispWindow().getMsgObj().dispInfoMessage(getPrjNmShrt(), "checkMemorySetup","Total free memory: " +  (freeMem + (maxMem - allocMem)) / 1024.0f);   
	
	}//checkMemorySetup
	
	
	//		public final int  
	//		  black=0xff000000, 
	//		  white=0xffFFFFFF,
	//		  red=0xffFF0000, 
	//		  green=0xff00FF00, 
	//		  blue=0xff0000FF, 
	//		  yellow=0xffFFFF00, 
	//		  cyan=0xff00FFFF, 
	//		  magenta=0xffFF00FF,
	//		  grey=0xff818181, 
	//		  orange=0xffFFA600, 
	//		  brown=0xffB46005, 
	//		  metal=0xffB5CCDE, 
	//		  dgreen=0xff157901;
	//set color based on passed point r= x, g = z, b=y
	public final void fillAndShowLineByRBGPt(myPoint p, float x,  float y, float w, float h){
		ri.setFill((int)p.x,(int)p.y,(int)p.z, 255);
		ri.setStroke((int)p.x,(int)p.y,(int)p.z, 255);
		ri.drawRect(x,y,w,h);
	}	
	
}//class GUI_AppManager
