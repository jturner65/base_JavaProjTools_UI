package base_UI_Objects;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_UI_Objects.windowUI.base.myDispWindow;
import base_UI_Objects.windowUI.sidebar.mySideBarMenu;
import base_UI_Objects.windowUI.sidebar.mySidebarMenuBtnConfig;
import base_Utils_Objects.io.MessageObject;

/**
 * this class manages all common functionality for a gui application, independent of renderer
 * @author john
 *
 */
public abstract class GUI_AppManager {
	//rendering engine
	public static IRenderInterface pa = null;
	//msg object interface
	protected MessageObject msg;
	//3d interaction stuff and mouse tracking
	protected my3DCanvas canvas;												
	/**
	 * max ratio of width to height to use for application window initialization
	 */
	public float maxWinRatio =  1.77777778f;
	/**
	 * physical display width and height this project is running on
	 */
	protected static int _displayWidth, _displayHeight;
	
	//individual display/HUD windows for gui/user interaction
	protected myDispWindow[] dispWinFrames = new myDispWindow[0] ;
	//set in instancing class - must be > 1
	protected int numDispWins;
	//always idx 0 - first window is always right side menu
	public static final int dispMenuIDX = 0;	
	//which myDispWindow currently has focus
	public int curFocusWin;		
	//need 1 per display window
	public String[] winTitles,winDescr;

	//whether or not the display windows will accept a drawn trajectory
	protected boolean[][] dispWinFlags;
	//idxs : 0 : canDrawInWin; 1 : canShow3dbox; 2 : canMoveView; 3 : dispWinIs3d
	protected static int
		dispCanDrawInWinIDX 	= 0,
		dispCanShow3dboxIDX 	= 1,
		dispCanMoveViewIDX 		= 2,
		dispWinIs3dIDX 			= 3;
	private static int numDispWinBoolFlags = 4;
	
	public int[][] winFillClrs, winStrkClrs;
	
	public int[][] winTrajFillClrs = new int [][]{{0,0},{0,0}};		//set to color constants for each window
	public int[][] winTrajStrkClrs = new int [][]{{0,0},{0,0}};		//set to color constants for each window
	

	//specify windows that cannot be shown simultaneously here and their flags
	public int[] winFlagsXOR, winDispIdxXOR;
	
	//unblocked window dimensions - location and dim of window if window is open\closed
	public float[][] winRectDimOpen, winRectDimClose;

	//9 element array holding camera loc, target, and orientation
	public float[] camVals;		
		
	//used to manage current time
	public Calendar now;
	//data being printed to console - show on screen
	//public ArrayDeque<String> consoleStrings;		
	
	protected int simCycles;												// counter for draw cycles		
	
	//enable drawing dbug info onto screen	
	private ArrayList<String> DebugInfoAra;									

	//visualization variables
	//flags explicitly pertaining to window visibility
	private int[] _visFlags;		
	// boolean flags used to control various elements of the program 
	private int[] baseFlags;
	//dev/debug flags
	private final int 
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
			flipDrawnTraj  		= 13,			//whether or not to flip the direction of the drawn trajectory
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
			"Displaying UI Menu",
			"Reverse Drawn Trajectory"
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
			"Displaying UI Menu",
			"Reverse Drawn Trajectory"
			};
	private int[][] pFlagColors;
	
	
	//public final int numDebugVisFlags = 6;
	//flags to actually display in menu as clickable text labels - order does matter
	private List<Integer> flagsToShow = Arrays.asList( 
		debugMode, 			
		saveAnim,
		runSim,
		singleStep,
		showRtSideMenu
		);
	
	private int numFlagsToShow = flagsToShow.size();
	
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
	
	//3dbox stuff
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

	protected float menuWidth;			
	//side menu is 15% of screen grid2D_X, 
	protected float menuWidthMult = .15f;
	protected float hideWinWidth;
	protected float hideWinWidthMult = .03f;
	protected float hidWinHeight;
	protected float hideWinHeightMult = .05f;

	protected String exeDir = Paths.get(".").toAbsolutePath().toString();
	//file location of current executable
	protected File currFileIOLoc = Paths.get(".").toAbsolutePath().toFile();
	//mouse wheel sensitivity
	public static final float mouseWhlSens = 1.0f;
	// distance within which to check if clicked from a point
	public double msClkEps = 40;				
	//scaling factors for mouse movement		
	public float msSclX, msSclY;											

	public final int[][] triColors = new int[][] {
		{IRenderInterface.gui_DarkMagenta,IRenderInterface.gui_DarkBlue,IRenderInterface.gui_DarkGreen,IRenderInterface.gui_DarkCyan}, 
		{IRenderInterface.gui_LightMagenta,IRenderInterface.gui_LightBlue,IRenderInterface.gui_LightGreen,IRenderInterface.gui_TransCyan}};

	//////////////////////////////
	// code
	
	public GUI_AppManager() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		_displayWidth = gd.getDisplayMode().getWidth();
		_displayHeight = gd.getDisplayMode().getHeight();	
	}//	
	
	public void setIRenderInterface(IRenderInterface _pa) {
		if (null == pa) {pa=_pa;}
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
	 * 			1 - use smaller dim to be determine window 
	 * 			2+ - TBD
	 */
	protected abstract int setAppWindowDimRestrictions();
		
	public int getNumThreadsAvailable() {return Runtime.getRuntime().availableProcessors();}

	/**
	 * called once, at beginning of IRenderInterface object initVisOnce()
	 * @param width
	 * @param height
	 */
	public void firstInit(int width, int height) {
		msSclX = MyMathUtils.Pi_f/width;
		msSclY = MyMathUtils.Pi_f/height;
		
		int numThreadsAvail = getNumThreadsAvailable();
		//init internal state flags structure
		initBaseFlags();			

		now = Calendar.getInstance();

		//consoleStrings = new ArrayDeque<String>();				//data being printed to console		
		pa.outStr2Scr("# threads : "+ numThreadsAvail);
		
		menuWidth = width * menuWidthMult;						//grid2D_X of menu region	
		hideWinWidth = width * hideWinWidthMult;				//dims for hidden windows
		hidWinHeight = height * hideWinHeightMult;
		//build canvas
		canvas = new my3DCanvas(this, pa, width, height);	
	}
	
	/**
	 * called once, after windows are built from IRenderInteface object initVisOnce()
	 */
	public void endInit() {		
		initMainFlags_Indiv();
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
		
		// set cam vals
		camVals = new float[]{0, 0, (float) ((pa.getHeight()/2.0) / Math.tan(MyMathUtils.Pi/6.0)), 0, 0, 0, 0,1,0};		
	}
	/**
	 * called in pre-draw initial setup, before first init
	 */
	protected abstract void setup_Indiv();	
	/**
	 * this is called to determine which main flags to display in the window
	 */
	protected abstract void initMainFlags_Indiv();
	
	/**
	 * this is called to build all the myDispWindows in the instancing class
	 */
	protected abstract void initAllDispWindows();

	//1 time initialization of programmatic things that won't change
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
	public final void initVisProg(){	
		reInitInfoStr();
		initVisProg_Indiv();
	}
	protected abstract void initVisProg_Indiv();
		//called every time re-initialized
	public final void initProgram() {
		initVisProg();				//always first
		
		initProgram_Indiv();
	}//initProgram	
	protected abstract void initProgram_Indiv();
	
	/**
	 * reset debug info array 
	 */
	public final void reInitInfoStr(){		DebugInfoAra = new ArrayList<String>();		DebugInfoAra.add("");	}	

	/**
	 * set up window structures - called from instanced class of IRenderInterface
	 * @param _numWins
	 * @param _winTtls
	 * @param _winDescs
	 */
	public void initWins(int _numWins, String[] _winTtls, String[] _winDescs) {
		numDispWins = _numWins;	//must be set here!
		winRectDimOpen = new float[numDispWins][];
		winRectDimClose = new float[numDispWins][];
		//idxs : 0 : canDrawInWin; 1 : canShow3dbox; 2 : canMoveView; 3 : dispWinIs3d
		dispWinFlags = new boolean[numDispWins][numDispWinBoolFlags];
		//need 1 per display window
		winTitles = _winTtls;
		winDescr = _winDescs;
		winFillClrs = new int[numDispWins][4];
		winStrkClrs = new int[numDispWins][4];
		winTrajFillClrs = new int[numDispWins][4];		//set to color constants for each window
		winTrajStrkClrs = new int[numDispWins][4];	//set to color constants for each window
		//display window initialization
		dispWinFrames = new myDispWindow[numDispWins];			
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
	
	//initialize menu window
	public void buildInitMenuWin(int _showUIMenuIDX) {
		//init sidebar menu vals
		for(int i=0;i<dispWinFlags[dispMenuIDX].length;++i) {dispWinFlags[dispMenuIDX][i] = false;}
		//set up dims for menu
		winRectDimOpen[dispMenuIDX] =  new float[]{0,0, menuWidth, pa.getHeight()};
		winRectDimClose[dispMenuIDX] =  new float[]{0,0, hideWinWidth, pa.getHeight()};
		
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
	public void setInitDispWinVals(int _winIDX, float[] _dimOpen, float[] _dimClosed, boolean[] _dispFlags, int[] _fill, int[] _strk, int[] _trajFill, int[] _trajStrk) {
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
	
	public myDispWindow getCurrentWindow() {return dispWinFrames[curFocusWin];}
	
	//////////////////////////////////
	// side bar menu stuff
	
	/**
	 * build the appropriate side bar menu configuration for this application
	 * @param wIdx
	 * @param fIdx
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _numBtnsPerFuncRow array of # of buttons per row of functional buttons - size must match # of entries in _funcRowNames array
	 * @param _numDbgBtns # of debug buttons
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 * @return
	 */
	public final mySideBarMenu buildSideBarMenu(int wIdx, int fIdx, String[] _funcRowNames, int[] _numBtnsPerFuncRow, int _numDbgBtns, boolean _inclWinNames, boolean _inclMseOvValues){
		mySidebarMenuBtnConfig sideBarConfig = new mySidebarMenuBtnConfig(_funcRowNames, _numBtnsPerFuncRow, _numDbgBtns, _inclWinNames, _inclMseOvValues);
		return new mySideBarMenu(pa, this, winTitles[wIdx], fIdx, winFillClrs[wIdx], winStrkClrs[wIdx], winRectDimOpen[wIdx], winRectDimClose[wIdx], winDescr[wIdx], sideBarConfig);		
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
		mySideBarMenu win = (mySideBarMenu)dispWinFrames[dispMenuIDX];
		win.getGuiBtnWaitForProc()[row][col] = false;
		if(isSlowProc) {win.getGuiBtnSt()[row][col] = 0;}		
	}//clearBtnState 
	
	/**
	 * only send names of function and debug btns (if they exist) in 2d array
	 * @param btnNames
	 */
	public final void setAllMenuBtnNames(String[][] btnNames) {
		for(int _type = 0;_type<btnNames.length;++_type) {((mySideBarMenu)dispWinFrames[dispMenuIDX]).setAllFuncBtnNames(_type,btnNames[_type]);}
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
	
	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val){handleMenuBtnSelCmp(row, funcOffset, col, val, true);}					//display specific windows - multi-select/ always on if sel
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
		((mySideBarMenu)dispWinFrames[dispMenuIDX]).getGuiBtnSt()[row][col] = val;	
		if (val == 1) {
			//outStr2Scr("my_procApplet :: setMenuBtnState :: Note!!! Turning on button at row : " + row + "  col " + col + " without button's command.");
			((mySideBarMenu)dispWinFrames[dispMenuIDX]).setWaitForProc(row,col);}//if programmatically (not through UI) setting button on, then set wait for proc value true 
	}//setMenuBtnState	
	
	public void loadFromFile(File file){
		if (file == null) {
			pa.outStr2Scr("AppMgr :: loadFromFile ::Load was cancelled.");
		    return;
		} 		
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		dispWinFrames[curFocusWin].loadFromFile(file);
	
	}//loadFromFile
	
	public void saveToFile(File file){
		if (file == null) {
			pa.outStr2Scr("AppMgr :: saveToFile ::Save was cancelled.");
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
	
	
//	protected void updateConsoleStrs(){
//		++drawCount;
//		if(drawCount % cnslStrDecay == 0){drawCount = 0;	consoleStrings.poll();}			
//	}//updateConsoleStrs

		
	///////////////////////////////
	// draw/display functions
	
	protected abstract void setBkgrnd();//{	background(_bground[0],_bground[1],_bground[2],_bground[3]);}//setBkgrnd
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
	 * determine whether polled window is being shown currently
	 * @param i
	 * @return
	 */
	public final boolean isShowingWindow(int i){return getVisFlag(i);}//showUIMenu is first flag of window showing flags, visFlags are defined in instancing class
	
	protected abstract String getPrjNmLong();
	protected abstract String getPrjNmShrt();
	
	/**
	 * sim loop, called from IRenderInterface draw method
	 * @param modAmtMillis
	 */
	public boolean execSimDuringDrawLoop(float modAmtMillis) {
		//simulation section
		if(isRunSim() ){
			//run simulation
			//drawCount++;									//needed here to stop draw update so that pausing sim retains animation positions - moved to IRenderInterface caller, if return is true	
			for(int i =1; i<numDispWins; ++i){if((isShowingWindow(i)) && (dispWinFrames[i].getFlags(myDispWindow.isRunnable))){dispWinFrames[i].simulate(modAmtMillis);}}
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
		pa.setPerspective(MyMathUtils.Pi_f/3.0f, (1.0f*pa.getWidth())/(1.0f*pa.getHeight()), .5f, camVals[2]*100.0f);
		pa.enableLights(); 	
		dispWinFrames[curFocusWin].drawSetupWin(camVals);
	}//drawSetup
	
	/**
	 * main draw loop
	 */
	public final void drawMe(float modAmtMillis){
		pa.pushMatState();
		drawSetup();
		boolean is3DDraw = (curFocusWin == -1) || (curDispWinIs3D()); 
		if(is3DDraw){	//allow for single window to have focus, but display multiple windows	
			//if refreshing screen, this clears screen, sets background
			if(getShouldClearBKG()) {
				setBkgrnd();				
				draw3D_solve3D(modAmtMillis, -canvas.getViewDimW()/2.0f+40);
				canvas.buildCanvas();
				if(curDispWinCanShow3dbox()){drawBoxBnds();}
				if(dispWinFrames[curFocusWin].chkDrawMseRet()){			canvas.drawMseEdge(dispWinFrames[curFocusWin], is3DDraw);	}		
			} else {
				draw3D_solve3D(modAmtMillis, -canvas.getViewDimW()/2.0f+40);
				canvas.buildCanvas();
			}
			pa.popMatState(); 
		} else {	//either/or 2d window
			//2d windows paint window box so background is always cleared
			canvas.buildCanvas();
			canvas.drawMseEdge(dispWinFrames[curFocusWin], is3DDraw);
			pa.popMatState(); 
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
	 * @param viewDimW
	 */
	public final void draw3D_solve3D(float modAmtMillis, float viewDimW){
		pa.pushMatState();
		for(int i =1; i<numDispWins; ++i){
			if((isShowingWindow(i)) && (dispWinFrames[i].getIs3DWindow())){	dispWinFrames[i].draw3D(modAmtMillis);}
		}
		pa.popMatState();
		//fixed xyz rgb axes for visualisation purposes and to show movement and location in otherwise empty scene
		drawAxes(100,3, new myPoint(viewDimW,0.0f,0.0f), 200, false); 		
	}//draw3D_solve3D
	
	/**
	 * Draw 2d windows that are currently displayed
	 * @param modAmtMillis
	 */
	
	public final void draw2D(float modAmtMillis) {
		for(int i =1; i<numDispWins; ++i){if (isShowingWindow(i) && !(dispWinFrames[i].getIs3DWindow())){dispWinFrames[i].draw2D(modAmtMillis);}}
	}
	
	//vector and point functions to be compatible with earlier code from jarek's class or previous projects	
	//draw bounding box for 3d
	public final void drawBoxBnds(){
		pa.pushMatState();
		pa.setStrokeWt(3f);
		pa.noFill();
		pa.setColorValStroke(IRenderInterface.gui_TransGray,255);		
		pa.drawBox3D(gridDimX,gridDimY,gridDimZ);
		pa.popMatState();
	}	
	
	//project passed point onto box surface based on location - to help visualize the location in 3d
	public final void drawProjOnBox(myPoint p){
		//myPoint[]  projOnPlanes = new myPoint[6];
		myPoint prjOnPlane;
		//public final myPoint intersectPl(myPoint E, myVector T, myPoint A, myPoint B, myPoint C) { // if ray from E along T intersects triangle (A,B,C), return true and set proposal to the intersection point
		pa.pushMatState();
		pa.translate(-p.x,-p.y,-p.z);
		for(int i  = 0; i< 6; ++i){				
			prjOnPlane = bndChkInCntrdBox3D(intersectPl(p, boxNorms[i], boxWallPts[i][0],boxWallPts[i][1],boxWallPts[i][2]));				
			pa.showPtAsSphere(prjOnPlane,5,5,IRenderInterface.rgbClrs[i/2],IRenderInterface.rgbClrs[i/2]);				
		}
		pa.popMatState();
	}//drawProjOnBox

	/**
	 * display menu text based on menu state - moved from menu class
	 * @param xOffHalf
	 * @param yOffHalf
	 */
	public void dispMenuText(float xOffHalf, float yOffHalf) {
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
	public void dispMenuTxtLat(String txt, int[] clrAra, boolean showSphere, float xOff, float yOff){
		pa.setFill(clrAra, 255); 
		pa.translate(xOff,yOff);
		if(showSphere){pa.setStroke(clrAra, 255);		pa.drawSphere(5);	} 
		else {	pa.noStroke();		}
		pa.translate(-xOff,yOff);
		pa.showText(""+txt,2.0f*xOff,-yOff*.5f);	
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
			pa.setFill(clrAra, 255); 
			pa.setStroke(clrAra, 255);
		} else {
			pa.setColorValFill(IRenderInterface.gui_DarkGray,255); 
			pa.noStroke();	
		}
		pa.drawSphere(5);
		//text(""+txt,-xOff,yOff*.8f);	
		pa.showText(""+txt,stMult*txt.length(),yOff*.8f);	
	}	
	
	/**
	 * draw state booleans at top of screen and their state
	 */
	public void drawSideBarStateBools(float yOff){ //numStFlagsToShow
		pa.translate(110,10);
		float xTrans = (int)((getMenuWidth()-100) / (1.0f*numStFlagsToShow));
		for(int idx =0; idx<numStFlagsToShow; ++idx){
			dispBoolStFlag(StateBoolNames[idx],stBoolFlagColors[idx], getStateFlagState(idx),StrWdMult[idx], yOff);			
			pa.translate(xTrans,0);
		}
	}
	
	/**
	 * called by sidebar menu to display current window's UI components
	 */
	public final void drawWindowGuiObjs(){
		if(curFocusWin != -1){
			pa.pushMatState();
			dispWinFrames[curFocusWin].drawGUIObjs();					//draw what user-modifiable fields are currently available
			dispWinFrames[curFocusWin].drawClickableBooleans();					//draw what user-modifiable fields are currently available
			dispWinFrames[curFocusWin].drawCustMenuObjs();					//customizable menu objects for each window
			//also launch custom function here
			dispWinFrames[curFocusWin].checkCustMenuUIObjs();			
			pa.popMatState();	
		}
	}//	
	
	public final void drawUI(float modAmtMillis){					
		for(int i =1; i<numDispWins; ++i){dispWinFrames[i].drawHeader(modAmtMillis);}
		//menu always idx 0
		//normal(0,0,1);
		dispWinFrames[0].draw2D(modAmtMillis);
		dispWinFrames[0].drawHeader(modAmtMillis);
		if(isDebugMode()){
			pa.pushMatState();			
			reInitInfoStr();
			addInfoStr(0,getMseEyeInfoString(dispWinFrames[curFocusWin].getCamDisp()));
			String[] res = ((mySideBarMenu)dispWinFrames[dispMenuIDX]).getDebugData();		//get debug data for each UI object
			int numToPrint = MyMathUtils.min(res.length,80);
			for(int s=0;s<numToPrint;++s) {	addInfoStr(res[s]);}				//add info to string to be displayed for debug
			drawInfoStr(1.0f, dispWinFrames[curFocusWin].strkClr); 	
			pa.popMatState();		
		}
		else if(showInfo){
			pa.pushMatState();			
			reInitInfoStr();	
			String[] res = pa.getConsoleStrings().toArray(new String[0]);
			int dispNum = MyMathUtils.min(res.length, 80);
			for(int i=0;i<dispNum;++i){addInfoStr(res[i]);}
		    drawInfoStr(1.1f, dispWinFrames[curFocusWin].strkClr); 
		    pa.popMatState();
		}
	}//drawUI
	
	
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param alpha alpha value for how dark/faint axes should be
	 * @param centered whether axis should be centered at ctr or just in positive direction at ctr
	 */
	public void drawAxes(double len, float stW, myPoint ctr, int alpha, boolean centered){//axes using current global orientation
		pa.pushMatState();
			pa.setStrokeWt(stW);
			pa.setStroke(255,0,0,alpha);
			if(centered){
				double off = len*.5f;
				pa.drawLine(ctr.x-off,ctr.y,ctr.z,ctr.x+off,ctr.y,ctr.z);pa.setStroke(0,255,0,alpha);pa.drawLine(ctr.x,ctr.y-off,ctr.z,ctr.x,ctr.y+off,ctr.z);pa.setStroke(0,0,255,alpha);pa.drawLine(ctr.x,ctr.y,ctr.z-off,ctr.x,ctr.y,ctr.z+off);} 
			else {		pa.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+len,ctr.y,ctr.z);pa.setStroke(0,255,0,alpha);pa.drawLine(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y+len,ctr.z);pa.setStroke(0,0,255,alpha);pa.drawLine(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y,ctr.z+len);}
		pa.popMatState();	
	}//	drawAxes
	public void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int alpha, boolean drawVerts){//RGB -> XYZ axes
		pa.pushMatState();
		if(drawVerts){
			pa.showPtAsSphere(ctr,3,5,IRenderInterface.gui_Black,IRenderInterface.gui_Black);
			for(int i=0;i<_axis.length;++i){pa.showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),3,5,IRenderInterface.rgbClrs[i],IRenderInterface.rgbClrs[i]);}
		}
		pa.setStrokeWt((float)stW);
		for(int i =0; i<3;++i){	pa.setColorValStroke(IRenderInterface.rgbClrs[i],255);	pa.showVec(ctr,len, _axis[i]);	}
		pa.popMatState();	
	}//	drawAxes
	public void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int[] clr, boolean drawVerts){//all axes same color
		pa.pushMatState();
			if(drawVerts){
				pa.showPtAsSphere(ctr,2,5,IRenderInterface.gui_Black,IRenderInterface.gui_Black);
				for(int i=0;i<_axis.length;++i){pa.showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),2,5,IRenderInterface.rgbClrs[i],IRenderInterface.rgbClrs[i]);}
			}
			pa.setStrokeWt(stW);pa.setStroke(clr[0],clr[1],clr[2],clr[3]);
			for(int i =0; i<3;++i){	pa.showVec(ctr,len, _axis[i]);	}
		pa.popMatState();	
	}//	drawAxes
	public void drawAxes(double len, double stW, myPoint ctr, myVectorf[] _axis, int alpha){
		pa.pushMatState();
			pa.setStrokeWt((float)stW);
			pa.setStroke(255,0,0,alpha);
			pa.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[0].x)*len,ctr.y+(_axis[0].y)*len,ctr.z+(_axis[0].z)*len);
			pa.setStroke(0,255,0,alpha);
			pa.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[1].x)*len,ctr.y+(_axis[1].y)*len,ctr.z+(_axis[1].z)*len);	
			pa.setStroke(0,0,255,alpha);	
			pa.drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[2].x)*len,ctr.y+(_axis[2].y)*len,ctr.z+(_axis[2].z)*len);
		pa.popMatState();	
	}//	drawAxes
	

	/////////////////
	// canvas functions
	
	public final myVector getDrawSNorm() {return canvas.getDrawSNorm();}
	public final myVectorf getDrawSNorm_f() {return canvas.getDrawSNorm_f();}
	public final myVector getEyeToMse() {return canvas.getEyeToMse();}
	public final myVectorf getEyeToMse_f() {return canvas.getEyeToMse_f();}
	public myVector getUScrUpInWorld(){		return canvas.getUScrUpInWorld();}	
	public myVector getUScrRightInWorld(){		return canvas.getUScrRightInWorld();}
	public myVectorf getUScrUpInWorldf(){		return canvas.getUScrUpInWorldf();}	
	public myVectorf getUScrRightInWorldf(){	return canvas.getUScrRightInWorldf();}
	
	public myPoint getMseLoc(){			return canvas.getMseLoc();}
	public myPointf getMseLoc_f(){		return canvas.getMseLoc_f();	}
	public myPoint getEyeLoc(){			return canvas.getEyeLoc();	}
	public myPoint getOldMseLoc(){		return canvas.getOldMseLoc();	}	
	public myVector getMseDragVec(){	return canvas.getMseDragVec();}
	
	//relative to passed origin
	public myPoint getMseLoc(myPoint glbTrans){			return canvas.getMseLoc(glbTrans);	}
	//move by passed translation
	public myPointf getTransMseLoc(myPointf glbTrans){	return canvas.getTransMseLoc(glbTrans);	}
	//dist from mouse to passed location
	public float getMseDist(myPointf glbTrans){			return canvas.getMseDist(glbTrans);}
	public myPoint getOldMseLoc(myPoint glbTrans){		return canvas.getOldMseLoc(glbTrans);}
	
	//get normalized ray from eye loc to mouse loc
	public myVectorf getEyeToMouseRay_f() {				return canvas.getEyeToMouseRay_f();	}	
	
	/**
	 * return display string holding sreen and world mouse and eye locations 
	 */
	public final String getMseEyeInfoString(String winCamDisp) {
		myPoint mseLocPt = pa.getMouse_Raw();
		return "mse loc on screen : " + mseLocPt + " mse loc in world :"+ canvas.mseLoc +"  Eye loc in world :"+ canvas.eyeInWorld+ winCamDisp;
	}
	

	///////////////////////////////
	// showing functions
	
	/**
	 * this will properly format and display a string of text, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText_RightSideMenu(int[] tclr, float mult,  String txt) {
		pa.setFill(tclr,tclr[3]);pa.setStroke(tclr,tclr[3]);
		pa.showText(txt,0.0f,0.0f,0.0f);
		pa.translate(txt.length()*mult, 0.0f,0.0f);		
	}
	
	public final void showOffsetText(float d, int tclr, String txt){
		pa.setColorValFill(tclr, 255);pa.setColorValStroke(tclr, 255);
		pa.showText(txt, d, d,d); 
	}	
	public final void showOffsetText(myPointf loc, int tclr, String txt){
		pa.setColorValFill(tclr, 255);pa.setColorValStroke(tclr, 255);
		pa.showText(txt, loc.x, loc.y, loc.z); 
	}	
	public final void showOffsetText2D(float d, int tclr, String txt){
		pa.setColorValFill(tclr, 255);pa.setColorValStroke(tclr, 255);
		pa.showText(txt, d, d,0); 
	}
	public final void showOffsetTextAra(float d, int tclr, String[] txtAra){
		pa.setColorValFill(tclr, 255);pa.setColorValStroke(tclr, 255);
		float y = d;
		for (String txt : txtAra) {
			pa.showText(txt, d, y, d);
			y+=10;
		}
	}
		
	public final void showBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		pa.pushMatState();  
		pa.translate(P.x,P.y,P.z);
		pa.setColorValFill(IRenderInterface.gui_White,150);
		pa.setColorValStroke(IRenderInterface.gui_Black,255);
		pa.drawRect(new float[] {0,6.0f,txt.length()*7.8f,-15});
		tclr = IRenderInterface.gui_Black;		
		pa.setFill(fclr,255); pa.setStroke(strkclr,255);			
		pa.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		pa.popMatState();
	} // render sphere of radius r and center P)
	
	//translate to point, draw point and text
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		pa.pushMatState();  
		pa.setFill(fclr,255); 
		pa.setStroke(strkclr,255);		
		pa.translate(P.x,P.y,P.z); 
		pa.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		pa.popMatState();
	} // render sphere of radius r and center P)
	
	//textP is location of text relative to point
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		pa.pushMatState();  
		pa.translate(P.x,P.y,P.z); 
		pa.setFill(fclr,255); 
		pa.setStroke(strkclr,255);			
		pa.drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(txtP,tclr, txt);
		pa.popMatState();
	} // render sphere of radius r and center P)
	
	//textP is location of text relative to point
	public final void showCrclNoBox_ClrAra(myPointf P, float rad, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		pa.pushMatState();  
		pa.translate(P.x,P.y,P.z); 
		if((fclr!= null) && (strkclr!= null)){pa.setFill(fclr,255); pa.setStroke(strkclr,255);}		
		pa.drawEllipse2D(0,0,rad,rad); 
		pa.drawEllipse2D(0,0,2,2);
		showOffsetText(txtP,tclr, txt);
		pa.popMatState();
	} // render sphere of radius r and center P)
	
	//show sphere of certain radius
	public final void show_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr) {
		pa.pushMatState();   
		if((fclr!= null) && (strkclr!= null)){pa.setFill(fclr,255); pa.setStroke(strkclr,255);}
		pa.drawSphere(P, rad, det);
		pa.popMatState();
	} // render sphere of radius r and center P)
	
	
	public final int addInfoStr(String str){return addInfoStr(DebugInfoAra.size(), str);}
	public final int addInfoStr(int idx, String str){	
		int lstIdx = DebugInfoAra.size();
		if(idx >= lstIdx){		for(int i = lstIdx; i <= idx; ++i){	DebugInfoAra.add(i,"");	}}
		setInfoStr(idx,str);	return idx;
	}
	public final void setInfoStr(int idx, String str){DebugInfoAra.set(idx,str);	}
	public final void drawInfoStr(float sc, int clr){drawInfoStr(sc, pa.getClr(clr,255));}
	public final void drawInfoStr(float sc, int[] fillClr){//draw text on main part of screen
		pa.pushMatState();		
			pa.setFill(fillClr,fillClr[3]);
			pa.translate((menuWidth),0);
			pa.scale(sc,sc);
			for(int i = 0; i < DebugInfoAra.size(); ++i){		pa.showText((getBaseFlag(debugMode)?(i<10?"0":"")+i+":     " : "") +"     "+DebugInfoAra.get(i)+"\n\n",0,(10+(12*i)));	}
		pa.popMatState();
	}		
	
	//print out multiple-line text to screen
	public final void ml_text(String str, float x, float y){
		String[] res = str.split("\\r?\\n");
		float disp = 0;
		for(int i =0; i<res.length; ++i){
			pa.showText(res[i],x, y+disp);		//add console string output to screen display- decays over time
			disp += 12;
		}
	}

	///////////////////////////////
	// end draw/display functions
	//build a date with each component separated by token
	public String getDateTimeString(){return getDateTimeString(true, false,".");}
	public String getDateTimeString(boolean useYear, boolean toSecond, String token){
		String result = "";
		int val;
		if(useYear){val = now.get(Calendar.YEAR);		result += ""+val+token;}
		val = now.get(Calendar.MONTH)+1;				result += (val < 10 ? "0"+val : ""+val)+ token;
		val = now.get(Calendar.DAY_OF_MONTH);			result += (val < 10 ? "0"+val : ""+val)+ token;
		val = now.get(Calendar.HOUR_OF_DAY);					result += (val < 10 ? "0"+val : ""+val)+ token;
		val = now.get(Calendar.MINUTE);					result += (val < 10 ? "0"+val : ""+val);
		if(toSecond){val = now.get(Calendar.SECOND);	result += token + (val < 10 ? "0"+val : ""+val);}
		return result;
	}
	//utilities
	public String getDateString(){return getDateString(true, "-");}
	public String getDateString(boolean useYear, String token){
		String result = "";
		int val;
		if(useYear){val = now.get(Calendar.YEAR);		result += ""+val+token;}
		val = now.get(Calendar.MONTH)+1;				result += (val < 10 ? "0"+val : ""+val)+ token;
		val = now.get(Calendar.DAY_OF_MONTH);			result += (val < 10 ? "0"+val : ""+val)+ token;
		return result;
	}//getDateString
	
	public String getTimeString(){return getTimeString(true, "-");}
	public String getTimeString(boolean toSecond, String token){
		String result = "";
		int val;
		val = now.get(Calendar.HOUR_OF_DAY);					result += (val < 10 ? "0"+val : ""+val)+ token;
		val = now.get(Calendar.MINUTE);					result += (val < 10 ? "0"+val : ""+val);
		if(toSecond){val = now.get(Calendar.SECOND);	result += token + (val < 10 ? "0"+val : ""+val);}
		return result;
	}//getDateString
	
	//////////////////////////
	//
	
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
	
	//set the height of each window that is above the popup window, to move up or down when it changes size
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
	public void sendKeyPressToWindows(char key, int keyCode) {
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
	 * return a list of names to apply to mse-over display select buttons - an empty or null list will not display option
	 * @return
	 */
	public abstract String[] getMouseOverSelBtnNames();
	
	
	//find mouse "force" exerted upon a particular location - distance from mouse to passed location
	public myVectorf mouseForceAtLoc(float msClickForce, myPointf _loc, boolean attractMode){
		myPointf mouseFrcLoc = getTransMseLoc(new myPointf(gridDimX/2.0f, gridDimY/2.0f,gridDimZ/2.0f));// new myPointf(c.dfCtr.x+gridDimX/2.0f,c.dfCtr.y+gridDimY/2.0f,c.dfCtr.z+gridDimZ/2.0f);// new myVector(lstClkX,0,lstClkY);//translate click location to where the space where the boids are	
		myVectorf resFrc = new myVectorf(_loc, mouseFrcLoc);		
		float sqDist = resFrc.sqMagn;
		if(sqDist<MyMathUtils.eps_f){sqDist=MyMathUtils.eps_f;}
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
		if (dispWinFrames[curFocusWin].getFlags(myDispWindow.canChgView)) {// (canMoveView[curFocusWin]){	
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

		
	private void _setMainFlagToShow(int idx, boolean val) {
		TreeMap<Integer, Integer> tmpMapOfFlags = new TreeMap<Integer, Integer>();
		for(Integer flag : flagsToShow) {			tmpMapOfFlags.put(flag, 0);		}
		if(val) {tmpMapOfFlags.put(idx, 0);	} else {tmpMapOfFlags.remove(idx);}
		flagsToShow = new ArrayList<Integer>(tmpMapOfFlags.keySet());
		numFlagsToShow = flagsToShow.size();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////	
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

	//base class flags init
	protected void initBaseFlags(){baseFlags = new int[1 + numBaseFlags/32];for(int i =0; i<numBaseFlags;++i){forceBaseFlag(i,false);}}		
	//set baseclass flags  //setBaseFlag(showIDX, 
	protected final void setBaseFlag(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		baseFlags[flIDX] = (val ?  baseFlags[flIDX] | mask : baseFlags[flIDX] & ~mask);
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
		baseFlags[flIDX] = (val ?  baseFlags[flIDX] | mask : baseFlags[flIDX] & ~mask);
	}
	//get baseclass flag
	protected final boolean getBaseFlag(int idx){int bitLoc = 1<<(idx%32);return (baseFlags[idx/32] & bitLoc) == bitLoc;}	
	protected final void clearBaseFlags(int[] idxs){		for(int idx : idxs){setBaseFlag(idx,false);}	}	

	/**
	 * used to toggle the value of a flag
	 * @param idx
	 */
	public void flipMainFlag(int i) {
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
	public int getNumFlagsToShow() {return numFlagsToShow;}
	
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
	///////////////////////
	// flags to show
	protected void setMainFlagToShow_debugMode(boolean val) {_setMainFlagToShow(debugMode, val);}
	protected void setMainFlagToShow_saveAnim(boolean val) {_setMainFlagToShow(saveAnim, val);}
	protected void setMainFlagToShow_runSim(boolean val) {_setMainFlagToShow(runSim, val);}
	protected void setMainFlagToShow_singleStep(boolean val) {_setMainFlagToShow(singleStep, val);}
	protected void setMainFlagToShow_showRtSideMenu(boolean val) {_setMainFlagToShow(showRtSideMenu, val);}	
	
		
	public abstract double clickValModMult();
	public abstract boolean isClickModUIVal();

	public float getMenuWidth() {return menuWidth;}
	
	public myDispWindow getCurFocusDispWindow() {return dispWinFrames[curFocusWin];}	

	public mySideBarMenu getSideBarMenuWindow() {return ((mySideBarMenu)dispWinFrames[dispMenuIDX]);}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// calculations
	
	/**
	 * build a frame based on world orientation given two passed points
	 * @param A
	 * @param B
	 * @return vec array of {AB, ScreenNorm, ScreenTan}
	 */
	public myVector[] buildViewBasedFrame(myPoint A, myPoint B) {
		myVector V = new myVector(A,B);
		myVector I = canvas.getDrawSNorm();//U(Normal(V));
		myVector J = I._cross(V)._normalize(); 
		return new myVector[] {V,I,J};		
	}
	
	/**
	 * build a frame based on world orientation given two passed points
	 * @param A
	 * @param B
	 * @return float vec array of {AB, ScreenNorm, ScreenTan}
	 */
	public myVectorf[] buildViewBasedFrame_f(myPointf A, myPointf B) {
		myVectorf V = new myVectorf(A,B);
		myVectorf I = canvas.getDrawSNorm_f();//U(Normal(V));
		myVectorf J = I._cross(V)._normalize(); 
		return new myVectorf[] {V,I,J};		
	}
	
	/**
	 * Build a set of n points inscribed on a circle centered at p in plane I,J
	 * @param p center point
	 * @param r circle radius
	 * @param I, J axes of plane
	 * @param n # of points
	 * @return array of n equal-arc-length points centered around p
	 */
	public synchronized myPoint[] buildCircleInscribedPoints(myPoint p, float r, myVector I, myVector J,int n) {
		myPoint[] pts = new myPoint[n];
		pts[0] = new myPoint(p,r,myVector._unit(I));
		float a = (MyMathUtils.twoPi_f)/(1.0f*n); 
		for(int i=1;i<n;++i){pts[i] = pts[i-1].rotMeAroundPt(a,J,I,p);}
		return pts;
	}
	public synchronized myPointf[] buildCircleInscribedPoints(myPointf p, float r, myVectorf I, myVectorf J,int n) {
		myPointf[] pts = new myPointf[n];
		pts[0] = new myPointf(p,r,myVectorf._unit(I));
		float a = (MyMathUtils.twoPi_f)/(1.0f*n);
		for(int i=1;i<n;++i){pts[i] = pts[i-1].rotMeAroundPt(a,J,I,p);}
		return pts;
	}
	
	
	public final myPoint PtOnSpiral(myPoint A, myPoint B, myPoint C, double t) {
		//center is coplanar to A and B, and coplanar to B and C, but not necessarily coplanar to A, B and C
		//so center will be coplanar to mp(A,B) and mp(B,C) - use mpCA midpoint to determine plane mpAB-mpBC plane?
		myPoint mAB = new myPoint(A,.5f, B), mBC = new myPoint(B,.5f, C), mCA = new myPoint(C,.5f, A);
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
		
		myVector rAB = myVector._rotAroundAxis(AB, rotAxis, MyMathUtils.halfPi_f);
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
		
		myVectorf rAB = myVectorf._rotAroundAxis(AB, rotAxis, MyMathUtils.halfPi_f);
		float c=AB._dot(CD)/n,	s=rAB._dot(CD)/n;
		float AB2 = AB._dot(AB), a=AB._dot(AC)/AB2, b=rAB._dot(AC)/AB2, x=(a-m*( a*c+b*s)), y=(b-m*(-a*s+b*c)), d=1+m*(m-2*c);  if((c!=1)&&(m!=1)) { x/=d; y/=d; };
		return new myPointf(new myPointf(A,x,AB),y,rAB);
	}

	
	
	public boolean intersectPl(myPoint E, myVector T, myPoint A, myPoint B, myPoint C, myPoint X) { // if ray from E along T intersects triangle (A,B,C), return true and set proposal to the intersection point
		myVector EA=new myVector(E,A), AB=new myVector(A,B), AC=new myVector(A,C); 		double t = (float)(myVector._mixProd(EA,AC,AB) / myVector._mixProd(T,AC,AB));		X.set(myPoint._add(E,t,T));		return true;
	}	
	public final myPoint intersectPl(myPoint E, myVector T, myPoint A, myPoint B, myPoint C) { // if ray from E along T intersects triangle (A,B,C), return true and set proposal to the intersection point
		myVector EA=new myVector(E,A), AB=new myVector(A,B), AC=new myVector(A,C); 		
		double t = (float)(myVector._mixProd(EA,AC,AB) / myVector._mixProd(T,AC,AB));		
		return (myPoint._add(E,t,T));		
	}	
	// if ray from E along V intersects sphere at C with radius r, return t when intersection occurs
	public double intersectPt(myPoint E, myVector V, myPoint C, double r) { 
		myVector Vce = new myVector(C,E);
		double CEdCE = Vce._dot(Vce), VdV = V._dot(V), VdVce = V._dot(Vce), b = 2 * VdVce, c = CEdCE - (r*r),
				radical = (b*b) - 4 *(VdV) * c;
		if(radical < 0) return -1;
		double t1 = (b + Math.sqrt(radical))/(2*VdV), t2 = (b - Math.sqrt(radical))/(2*VdV);			
		return ((t1 > 0) && (t2 > 0) ? Math.min(t1, t2) : ((t1 < 0 ) ? ((t2 < 0 ) ? -1 : t2) : t1) );
		
	}	
	
	//performs shuffle
	public String[] shuffleStrList(String[] _list, String type){
		String tmp = "";
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		for(int i=(_list.length-1);i>0;--i){
			int j = (int)(tr.nextDouble(0,(i+1))) ;
			tmp = _list[i];
			_list[i] = _list[j];
			_list[j] = tmp;
		//	outStr2Scr("From i : " + i + " to j : " + j);
		}
		pa.outStr2Scr("String list of Sphere " + type + " shuffled");
		return _list;
	}//shuffleStrList
	
	private static final double third = 1.0/3.0;
	public final myVectorf getRandPosInSphere(double rad, myVectorf ctr){
		myVectorf pos = new myVectorf();
		do{
			double u = ThreadLocalRandom.current().nextDouble(0,1), r = rad * Math.pow(u, third),
					cosTheta = ThreadLocalRandom.current().nextDouble(-1,1), sinTheta =  Math.sin(Math.acos(cosTheta)),
					phi = ThreadLocalRandom.current().nextDouble(0,MyMathUtils.twoPi_f);
			pos.set(sinTheta * Math.cos(phi), sinTheta * Math.sin(phi),cosTheta);
			pos._mult(r);
			pos._add(ctr);
		} while (pos.z < 0);
		return pos;
	}
	
	public final myVectorf getRandPosOnSphere(double rad, myVectorf ctr){
		myVectorf pos = new myVectorf();
		//do{
			double 	cosTheta = ThreadLocalRandom.current().nextDouble(-1,1), sinTheta =  Math.sin(Math.acos(cosTheta)),
					phi = ThreadLocalRandom.current().nextDouble(0,MyMathUtils.twoPi_f);
			pos.set(sinTheta * Math.cos(phi), sinTheta * Math.sin(phi),cosTheta);
			pos._mult(rad);
			pos._add(ctr);
		//} while (pos.z < 0);
		return pos;
	}
	//random location within coords[0] and coords[1] extremal corners of a cube - bnds is to give a margin of possible random values
	public myVectorf getRandPosInCube(float[][] coords, float bnds){
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		return new myVectorf(
				tr.nextDouble(coords[0][0]+bnds,(coords[0][0] + coords[1][0] - bnds)),
				tr.nextDouble(coords[0][1]+bnds,(coords[0][1] + coords[1][1] - bnds)),
				tr.nextDouble(coords[0][2]+bnds,(coords[0][2] + coords[1][2] - bnds)));}		
	
	public final myPoint bndChkInBox2D(myPoint p){p.set(Math.max(0,Math.min(p.x,grid2D_X)),Math.max(0,Math.min(p.y,grid2D_Y)),0);return p;}
	public final myPoint bndChkInBox3D(myPoint p){p.set(Math.max(0,Math.min(p.x,gridDimX)), Math.max(0,Math.min(p.y,gridDimY)),Math.max(0,Math.min(p.z,gridDimZ)));return p;}	
	public final myPoint bndChkInCntrdBox3D(myPoint p){
		p.set(Math.max(-hGDimX,Math.min(p.x,hGDimX)), 
				Math.max(-hGDimY,Math.min(p.y,hGDimY)),
				Math.max(-hGDimZ,Math.min(p.z,hGDimZ)));return p;}	
	
	
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
	public void checkMemorySetup() {
		Runtime runtime = Runtime.getRuntime();  
		long maxMem = runtime.maxMemory(), allocMem = runtime.totalMemory(), freeMem = runtime.freeMemory();  
		pa.outStr2Scr("Free memory: " + freeMem / 1024);  
		pa.outStr2Scr("Allocated memory: " + allocMem / 1024);  
		pa.outStr2Scr("Max memory: " + maxMem /1024);  
		pa.outStr2Scr("Total free memory: " +  (freeMem + (maxMem - allocMem)) / 1024);   
	
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
	public void fillAndShowLineByRBGPt(myPoint p, float x,  float y, float w, float h){
		pa.setFill((int)p.x,(int)p.y,(int)p.z, 255);
		pa.setStroke((int)p.x,(int)p.y,(int)p.z, 255);
		pa.drawRect(x,y,w,h);
		//show(p,r,-1);
	}	
	
}//class GUI_AppManager
