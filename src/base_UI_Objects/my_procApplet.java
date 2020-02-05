package base_UI_Objects;


import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import base_UI_Objects.windowUI.base.myDispWindow;
import base_UI_Objects.windowUI.sidebar.mySideBarMenu;
import base_UI_Objects.windowUI.sidebar.mySidebarMenuBtnConfig;
import base_Utils_Objects.*;
import base_Utils_Objects.vectorObjs.*;

import processing.event.MouseEvent;

public abstract class my_procApplet extends processing.core.PApplet implements IRenderInterface {
	
	protected int glblStartSimFrameTime,			//begin of draw
		glblLastSimFrameTime,					//begin of last draw
		glblStartProgTime;					//start of program
	
	public int drawnTrajEditWidth = 10; //TODO make ui component			//width in cntl points of the amount of the drawn trajectory deformed by dragging
	
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

	//used to manage current time
	public Calendar now;
	//data being printed to console - show on screen
	public ArrayDeque<String> consoleStrings;							

	public int[] rgbClrs = new int[]{gui_Red,gui_Green,gui_Blue};
	
	protected final int cnslStrDecay = 10;			//how long a message should last before it is popped from the console strings deque (how many frames)
	
	public final float frate = 120;			//frame rate - # of playback updates per second
	
	//size of printed text (default is 12)
	public static final int txtSz = 11;
	//mouse wheel sensitivity
	public static final float mouseWhlSens = 1.0f;

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
			
	public final float
	//PopUpWinOpenFraction = .40f,				//fraction of screen not covered by popwindow
		wScale = frameRate/5.0f,					//velocity drag scaling	
		trajDragScaleAmt = 100.0f;					//amt of displacement when dragging drawn trajectory to edit
	
	// path and filename to save pictures for animation
	public int animCounter, runCounter;	
	public final int scrMsgTime = 50;									//5 seconds to delay a message 60 fps (used against draw count)
	
	protected int drawCount,simCycles;												// counter for draw cycles		
	protected float menuWidth;			//side menu is 15% of screen grid2D_X, 

	protected float menuWidthMult = .15f;
	protected float hideWinWidth;
	protected float hideWinWidthMult = .03f;
	protected float hidWinHeight;
	protected float hideWinHeightMult = .05f;
	
	private ArrayList<String> DebugInfoAra;										//enable drawing dbug info onto screen	
	
	//animation control variables	
	public final float maxAnimCntr = PI*1000.0f, baseAnimSpd = 1.0f;
	public float msSclX, msSclY;											//scaling factors for mouse movement		
	public my3DCanvas c;												//3d interaction stuff and mouse tracking
	
	protected float dz=0, rx=-0.06f*TWO_PI, ry=-0.04f*TWO_PI;		// distance to camera. Manipulated with wheel or when,view angles manipulated when space pressed but not mouse	
	public final float camInitialDist = -200,		//initial distance camera is from scene - needs to be negative
			camInitRy = ry,
			camInitRx = rx;
	public float[] camVals;		
	
	public double eps = .000000001;				//calc epsilon
	public float feps = .000001f;
	
	public double msClkEps = 40;				// distance within which to check if clicked from a point
	
	
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
	
	
	//whether or not to show start up instructions for code		
	public boolean showInfo=false;			
	
	protected String exeDir = Paths.get(".").toAbsolutePath().toString();
	//file location of current executable
	protected File currFileIOLoc = Paths.get(".").toAbsolutePath().toFile();
	//replace old displayWidth, displayHeight variables being deprecated in processing
	protected static int _displayWidth, _displayHeight;
	////////////////////////
	// code
	
	///////////////////////////////////
	/// inits
	///////////////////////////////////

	//needs main to run project - do not modify this code in any way 
	//needs to be called by instancing class
	protected final static void _invokedMain(String[] appletArgs, String[] passedArgs) {	
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		_displayWidth = gd.getDisplayMode().getWidth();
		_displayHeight = gd.getDisplayMode().getHeight();
	    if (passedArgs != null) {processing.core.PApplet.main(processing.core.PApplet.concat(appletArgs, passedArgs)); } else {processing.core.PApplet.main(appletArgs);		    }
	}//main	

	//processing being run in eclipse uses settings for variable size dimensions
	public final void settings(){	
		int[] desDims = getIdealAppWindowDims();
		size(desDims[0], desDims[1],P3D);	
		//allow user to set smoothing
		setSmoothing();
		//noSmooth();
	}	
	
	/**
	 * this will manage very large displays, while scaling window to smaller displays
	 * the goal is to preserve a reasonably close to 16:10 ratio window with big/widescreen displays
	 * @return int[] { desired application window width, desired application window height}
	 */
	protected final int[] getIdealAppWindowDims() {		
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
	
	protected abstract void setSmoothing();

	/**
	 * whether or not we want to restrict window size on widescreen monitors
	 * 
	 * @return 0 - use monitor size regardless
	 * 			1 - use smaller dim to be determine window 
	 * 			2+ - TBD
	 */
	protected abstract int setAppWindowDimRestrictions();
	
	/**
	 * returns the width of the visible display in pxls
	 * @return
	 */
	protected final int getDisplayWidth() {return _displayWidth;}
	/**
	 * returns the height of the visible display in pxls
	 * @return
	 */
	protected final int getDisplayHeight() {return _displayHeight;}
	/**
	 * returns application window width in pxls
	 * @return
	 */
	protected final int getWidth() {return width;}
	/**
	 * returns application window height in pxls
	 * @return
	 */
	protected final int getHeight() {return height;}
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
	
	
	
	public final void setup() {
		colorMode(RGB, 255, 255, 255, 255);
//		frameRate(frate);
		setup_indiv();
		initVisOnce();						//always first
		//call this in first draw loop?
		initOnce();		
		//needs to be the last thing called in setup, to avoid timeout 5000ms issue
		frameRate(frate);
	}//setup()
	
	protected abstract void setup_indiv();
	
	public int getNumThreadsAvailable() {return Runtime.getRuntime().availableProcessors();}
		//1 time initialization of visualization things that won't change
	public final void initVisOnce(){	
		int numThreadsAvail = getNumThreadsAvailable();
		//init internal state flags structure
		initBaseFlags();			

		now = Calendar.getInstance();
		//mouse scrolling scale
		msSclX = (float) (Math.PI/width);
		msSclY = (float) (Math.PI/height);

		consoleStrings = new ArrayDeque<String>();				//data being printed to console		
		outStr2Scr("# threads : "+ numThreadsAvail);
		outStr2Scr("Current sketchPath " + sketchPath());
		
		menuWidth = width * menuWidthMult;						//grid2D_X of menu region	
		hideWinWidth = width * hideWinWidthMult;				//dims for hidden windows
		hidWinHeight = height * hideWinHeightMult;
		c = new my3DCanvas(this);			
		strokeCap(SQUARE);//makes the ends of stroke lines squared off		

		initMainFlags_Priv();
		
		//instancing class version
		initVisOnce_Priv();
		//initialize all visible flag colors and state flag colors
		initPFlagColors();
		
		//after all display windows are drawn
		finalDispWinInit();
		initVisFlags();

		//this is to determine which main flags to display on window
		setBaseFlag(clearBKG,true);
		//camVals = new float[]{width/2.0f, height/2.0f, (height/2.0f) / tan(PI/6.0f), width/2.0f, height/2.0f, 0, 0, 1, 0};
		camVals = new float[]{0, 0, (height/2.0f) / tan(PI/6.0f), 0, 0, 0, 0,1,0};
		
		textSize(txtSz);
		
		textureMode(NORMAL);			
		rectMode(CORNER);	
		sphereDetail(4);
		simCycles = 0;
		glblStartProgTime = millis();
		glblStartSimFrameTime = glblStartProgTime;
		glblLastSimFrameTime =  glblStartProgTime;	

		initCamView();
		simCycles = 0;
	}//	initVisOnce
	
	public int timeSinceStart() {return millis() - glblStartProgTime;}
	
	/**
	 * this is called to determine which main flags to display in the window
	 */
	protected abstract void initMainFlags_Priv();
	
	
	protected abstract void initVisOnce_Priv();

	//1 time initialization of programmatic things that won't change
	public final void initOnce() {
		//1-time init for program and windows
		initOnce_Priv();
		//initProgram is called every time reinitialization is desired
		initProgram();		

		//after all init is done
		setFinalInitDone(true);
	}//initOnce	
//	protected abstract void initDispWins();
	protected abstract void initOnce_Priv();	
	
		//called every time re-initialized
	public final void initVisProg(){	
		drawCount = 0;			
		reInitInfoStr();
	}
	protected abstract void initVisProg_Indiv();
		//called every time re-initialized
	public final void initProgram() {
		initVisProg();				//always first
		
		initProgram_Indiv();
	}//initProgram	
	protected abstract void initProgram_Indiv();
	
	private void _setMainFlagToShow(int idx, boolean val) {
		TreeMap<Integer, Integer> tmpMapOfFlags = new TreeMap<Integer, Integer>();
		for(Integer flag : flagsToShow) {			tmpMapOfFlags.put(flag, 0);		}
		if(val) {tmpMapOfFlags.put(idx, 0);	} else {tmpMapOfFlags.remove(idx);}
		flagsToShow = new ArrayList<Integer>(tmpMapOfFlags.keySet());
		numFlagsToShow = flagsToShow.size();
	}
	
	protected void setMainFlagToShow_debugMode(boolean val) {_setMainFlagToShow(debugMode, val);}
	protected void setMainFlagToShow_saveAnim(boolean val) {_setMainFlagToShow(saveAnim, val);}
	protected void setMainFlagToShow_runSim(boolean val) {_setMainFlagToShow(runSim, val);}
	protected void setMainFlagToShow_singleStep(boolean val) {_setMainFlagToShow(singleStep, val);}
	protected void setMainFlagToShow_showRtSideMenu(boolean val) {_setMainFlagToShow(showRtSideMenu, val);}
	
	
	public final void initCamView(){	dz=camInitialDist;	ry=camInitRy;	rx=camInitRx - ry;	}
	public final void reInitInfoStr(){		DebugInfoAra = new ArrayList<String>();		DebugInfoAra.add("");	}	
	
	public float getMenuWidth() {return menuWidth;}
	
	public myDispWindow getCurFocusDispWindow() {return dispWinFrames[curFocusWin];}	
	
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
	protected final mySideBarMenu buildSideBarMenu(int wIdx, int fIdx, String[] _funcRowNames, int[] _numBtnsPerFuncRow, int _numDbgBtns, boolean _inclWinNames, boolean _inclMseOvValues){
		mySidebarMenuBtnConfig sideBarConfig = new mySidebarMenuBtnConfig(_funcRowNames, _numBtnsPerFuncRow, _numDbgBtns, _inclWinNames, _inclMseOvValues);
		return new mySideBarMenu(this, winTitles[wIdx], fIdx, winFillClrs[wIdx], winStrkClrs[wIdx], winRectDimOpen[wIdx], winRectDimClose[wIdx], winDescr[wIdx], sideBarConfig);		
	}
	
	//set up initial colors for primary flags for display
	private void initPFlagColors(){
		pFlagColors = new int[numBaseFlags][3];
		for (int i = 0; i < numBaseFlags; ++i) { pFlagColors[i] = new int[]{(int) random(150),(int) random(100),(int) random(150)}; }		
		stBoolFlagColors = new int[numStFlagsToShow][3];
		stBoolFlagColors[0] = new int[]{255,0,0};
		stBoolFlagColors[1] = new int[]{0,255,0};
		stBoolFlagColors[2] = new int[]{0,0,255};		
		for (int i = 3; i < numStFlagsToShow; ++i) { stBoolFlagColors[i] = new int[]{100+((int) random(150)),150+((int) random(100)),150+((int) random(150))};		}
	}
		
	/**
	 * get number of main flags to display in right side menu
	 * @return
	 */
	public int getNumFlagsToShow() {return numFlagsToShow;}
	
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
		setFill(clrAra, 255); 
		translate(xOff,yOff);
		if(showSphere){setStroke(clrAra, 255);		sphere(5);	} 
		else {	noStroke();		}
		translate(-xOff,yOff);
		text(""+txt,2.0f*xOff,-yOff*.5f);	
	}
	/**
	 * draw state booleans at top of screen and their state
	 */
	public void drawSideBarStateBools(float yOff){ //numStFlagsToShow
		translate(110,10);
		float xTrans = (int)((getMenuWidth()-100) / (1.0f*numStFlagsToShow));
		for(int idx =0; idx<numStFlagsToShow; ++idx){
			dispBoolStFlag(StateBoolNames[idx],stBoolFlagColors[idx], getStateFlagState(idx),StrWdMult[idx], yOff);			
			translate(xTrans,0);
		}
	}	
	
	////////////////////////////
	// end side bar menu-specific stuff
	
	//set up window structures
	protected void initWins(int _numWins, String[] _winTtls, String[] _winDescs) {
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
	
	//initialize menu window
	protected void buildInitMenuWin(int _showUIMenuIDX) {
		//init sidebar menu vals
		for(int i=0;i<dispWinFlags[dispMenuIDX].length;++i) {dispWinFlags[dispMenuIDX][i] = false;}
		//set up dims for menu
		winRectDimOpen[dispMenuIDX] =  new float[]{0,0, menuWidth, height};
		winRectDimClose[dispMenuIDX] =  new float[]{0,0, hideWinWidth, height};
		
		winFillClrs[dispMenuIDX] = new int[]{255,255,255,255};
		winStrkClrs[dispMenuIDX] = new int[]{0,0,0,255};
		
		winTrajFillClrs[dispMenuIDX] = new int[]{0,0,0,255};		//set to color constants for each window
		winTrajStrkClrs[dispMenuIDX] = new int[]{0,0,0,255};		//set to color constants for each window		
		winTitles[dispMenuIDX] = "UI Window";
		winDescr[dispMenuIDX] = "User Controls";
		
	}//setIniMenuWin
	
	//call once for each display window before calling constructor
	protected void setInitDispWinVals(int _winIDX, float[] _dimOpen, float[] _dimClosed, boolean[] _dispFlags, int[] _fill, int[] _strk, int[] _trajFill, int[] _trajStrk) {
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
	
//	
//			dispCanDrawInWinIDX 	= 0,
//			dispCanShow3dboxIDX 	= 1,
//			dispCanMoveViewIDX 		= 2,
//			dispWinIs3dIDX 			= 3;
		
	protected void finalDispWinInit() {
		for(int i =0; i < numDispWins; ++i){
			int scIdx = dispWinFlags[i][dispWinIs3dIDX] ? 1 : 0;//whether or not is 3d
			dispWinFrames[i].finalInit(dispWinFlags[i][dispCanDrawInWinIDX],dispWinFlags[i][dispWinIs3dIDX], dispWinFlags[i][dispCanMoveViewIDX], sceneCtrValsBase[scIdx], sceneFcsValsBase[scIdx]);
			dispWinFrames[i].setTrajColors(winTrajFillClrs[i], winTrajStrkClrs[i]);
			dispWinFrames[i].setRtSideUIBoxClrs(new int[]{0,0,0,200},new int[]{255,255,255,255});
		}	

	}//finalDispWinInit	
	
	public myDispWindow getCurrentWindow() {return dispWinFrames[curFocusWin];}

	///////////////////////////////////////////
	// draw routines
	
	protected abstract void setBkgrnd();//{	background(_bground[0],_bground[1],_bground[2],_bground[3]);}//setBkgrnd	
	
	protected boolean isShowingWindow(int i){return getVisFlag(i);}//showUIMenu is first flag of window showing flags, visFlags are defined in instancing class
	
	//get difference between frames and set both glbl times
	protected final float getModAmtMillis() {
		glblStartSimFrameTime = millis();
		float modAmtMillis = (glblStartSimFrameTime - glblLastSimFrameTime);
		glblLastSimFrameTime = millis();
		return modAmtMillis;
	}
	
	
	@Override
	/**
	 * main draw loop - override if handling draw differently
	 */
	public void draw(){
		if(!isFinalInitDone()) {initOnce(); return;}	
		float modAmtMillis = getModAmtMillis();
		//simulation section
		if(isRunSim() ){
			//run simulation
			drawCount++;									//needed here to stop draw update so that pausing sim retains animation positions	
			for(int i =1; i<numDispWins; ++i){if((isShowingWindow(i)) && (dispWinFrames[i].getFlags(myDispWindow.isRunnable))){dispWinFrames[i].simulate(modAmtMillis);}}
			if(isSingleStep()){setSimIsRunning(false);}
			simCycles++;
		}		//play in current window

		//drawing section
		pushMatrix();pushStyle();
		drawSetup();																//initialize camera, lights and scene orientation and set up eye movement		
		if((curFocusWin == -1) || (curDispWinIs3D())){	//allow for single window to have focus, but display multiple windows	
			//if refreshing screen, this clears screen, sets background
			if(getBaseFlag(clearBKG)) {
				setBkgrnd();				
				draw3D_solve3D(modAmtMillis);
				c.buildCanvas();
				if(curDispWinCanShow3dbox()){drawBoxBnds();}
				if(dispWinFrames[curFocusWin].chkDrawMseRet()){			c.drawMseEdge(dispWinFrames[curFocusWin]);	}		
			} else {
				draw3D_solve3D(modAmtMillis);
				c.buildCanvas();
			}
			popStyle();popMatrix(); 
		} else {	//either/or 2d window
			//2d windows paint window box so background is always cleared
			c.buildCanvas();
			c.drawMseEdge(dispWinFrames[curFocusWin]);
			popStyle();popMatrix(); 
			for(int i =1; i<numDispWins; ++i){if (isShowingWindow(i) && !(dispWinFrames[i].getFlags(myDispWindow.is3DWin))){dispWinFrames[i].draw2D(modAmtMillis);}}
		}
		drawUI(modAmtMillis);																	//draw UI overlay on top of rendered results			
		if (doSaveAnim()) {	savePic();}
		updateConsoleStrs();
		surface.setTitle(getPrjNmLong() + " : " + (int)(frameRate) + " fps|cyc curFocusWin : " + curFocusWin);
	}//draw	
	protected abstract String getPrjNmLong();
	protected abstract String getPrjNmShrt();

	
	protected final void draw3D_solve3D(float modAmtMillis){
		//System.out.println("drawSolve");
		pushMatrix();pushStyle();
		for(int i =1; i<numDispWins; ++i){
			if((isShowingWindow(i)) && (dispWinFrames[i].getFlags(myDispWindow.is3DWin))){	dispWinFrames[i].draw3D(modAmtMillis);}
		}
		popStyle();popMatrix();
		//fixed xyz rgb axes for visualisation purposes and to show movement and location in otherwise empty scene
		drawAxes(100,3, new myPoint(-c.getViewDimW()/2.0f+40,0.0f,0.0f), 200, false); 		
	}//draw3D_solve3D
	
	protected final void drawUI(float modAmtMillis){					
		//for(int i =1; i<numDispWins; ++i){if ( !(dispWinFrames[i].dispFlags[myDispWindow.is3DWin])){dispWinFrames[i].draw(sceneCtrVals[sceneIDX]);}}
		//dispWinFrames[0].draw(sceneCtrVals[sceneIDX]);
		for(int i =1; i<numDispWins; ++i){dispWinFrames[i].drawHeader(modAmtMillis);}
		//menu always idx 0
		normal(0,0,1);
		dispWinFrames[0].draw2D(modAmtMillis);
		dispWinFrames[0].drawHeader(modAmtMillis);
		drawOnScreenData();				//debug and on-screen data
	}//drawUI	
	
	/**
	 * Draw Axes at ctr point
	 * @param len length of axis
	 * @param stW stroke weight (line thickness)
	 * @param ctr ctr point to draw axes
	 * @param alpha alpha value for how dark/faint axes should be
	 * @param centered whether axis should be centered at ctr or just in positive direction at ctr
	 */
	@Override
	public final void drawAxes(double len, float stW, myPoint ctr, int alpha, boolean centered){//axes using current global orientation
		pushMatrix();pushStyle();
			strokeWeight(stW);
			stroke(255,0,0,alpha);
			if(centered){
				double off = len*.5f;
				line(ctr.x-off,ctr.y,ctr.z,ctr.x+off,ctr.y,ctr.z);stroke(0,255,0,alpha);line(ctr.x,ctr.y-off,ctr.z,ctr.x,ctr.y+off,ctr.z);stroke(0,0,255,alpha);line(ctr.x,ctr.y,ctr.z-off,ctr.x,ctr.y,ctr.z+off);} 
			else {		line(ctr.x,ctr.y,ctr.z,ctr.x+len,ctr.y,ctr.z);stroke(0,255,0,alpha);line(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y+len,ctr.z);stroke(0,0,255,alpha);line(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y,ctr.z+len);}
		popStyle();	popMatrix();	
	}//	drawAxes
	@Override
	public final void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int alpha, boolean drawVerts){//RGB -> XYZ axes
		pushMatrix();pushStyle();
		if(drawVerts){
			show(ctr,3,gui_Black,gui_Black, false);
			for(int i=0;i<_axis.length;++i){show(myPoint._add(ctr, myVector._mult(_axis[i],len)),3,rgbClrs[i],rgbClrs[i], false);}
		}
		strokeWeight(stW);
		for(int i =0; i<3;++i){	setColorValStroke(rgbClrs[i],255);	showVec(ctr,len, _axis[i]);	}
		popStyle();	popMatrix();	
	}//	drawAxes
	@Override
	public final void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int[] clr, boolean drawVerts){//all axes same color
		pushMatrix();pushStyle();
			if(drawVerts){
				show(ctr,2,gui_Black,gui_Black, false);
				for(int i=0;i<_axis.length;++i){show(myPoint._add(ctr, myVector._mult(_axis[i],len)),2,rgbClrs[i],rgbClrs[i], false);}
			}
			strokeWeight(stW);stroke(clr[0],clr[1],clr[2],clr[3]);
			for(int i =0; i<3;++i){	showVec(ctr,len, _axis[i]);	}
		popStyle();	popMatrix();	
	}//	drawAxes
	@Override
	public void drawAxes(double len, double stW, myPoint ctr, myVectorf[] _axis, int alpha){
		pushMatrix();pushStyle();
			strokeWeight((float)stW);
			stroke(255,0,0,alpha);
			line(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[0].x)*len,ctr.y+(_axis[0].y)*len,ctr.z+(_axis[0].z)*len);
			stroke(0,255,0,alpha);
			line(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[1].x)*len,ctr.y+(_axis[1].y)*len,ctr.z+(_axis[1].z)*len);	
			stroke(0,0,255,alpha);	
			line(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[2].x)*len,ctr.y+(_axis[2].y)*len,ctr.z+(_axis[2].z)*len);
		popStyle();	popMatrix();	
	}//	drawAxes
	
	private final void showVec( myPoint ctr, double len, myVector v){line(ctr.x,ctr.y,ctr.z,ctr.x+(v.x)*len,ctr.y+(v.y)*len,ctr.z+(v.z)*len);}
	
	public final int addInfoStr(String str){return addInfoStr(DebugInfoAra.size(), str);}
	public final int addInfoStr(int idx, String str){	
		int lstIdx = DebugInfoAra.size();
		if(idx >= lstIdx){		for(int i = lstIdx; i <= idx; ++i){	DebugInfoAra.add(i,"");	}}
		setInfoStr(idx,str);	return idx;
	}
	public final void setInfoStr(int idx, String str){DebugInfoAra.set(idx,str);	}
	public final void drawInfoStr(float sc, int clr){drawInfoStr(sc, getClr(clr,255));}
	public final void drawInfoStr(float sc, int[] fillClr){//draw text on main part of screen
		pushMatrix();		pushStyle();
			fill(fillClr[0],fillClr[1],fillClr[2],fillClr[3]);
			translate((menuWidth),0);
			scale(sc,sc);
			for(int i = 0; i < DebugInfoAra.size(); ++i){		text((getBaseFlag(debugMode)?(i<10?"0":"")+i+":     " : "") +"     "+DebugInfoAra.get(i)+"\n\n",0,(10+(12*i)));	}
		popStyle();	popMatrix();
	}		
	
	
	//vector and point functions to be compatible with earlier code from jarek's class or previous projects	
	//draw bounding box for 3d
	public final void drawBoxBnds(){
		pushMatrix();	pushStyle();
		strokeWeight(3f);
		noFill();
		setColorValStroke(gui_TransGray,255);		
		box(gridDimX,gridDimY,gridDimZ);
		popStyle();	popMatrix();
	}		
	//drawsInitial setup for each draw
	public final void drawSetup(){
		perspective(PI/3.0f, (1.0f*width)/(1.0f*height), .5f, camVals[2]*100.0f);
		lights(); 	
	    dispWinFrames[curFocusWin].drawSetupWin(camVals);
	}//drawSetup		
	

	//called by sidebar menu to display current window's UI components
	public final void drawWindowGuiObjs(){
		if(curFocusWin != -1){
			pushMatrix();pushStyle();
			dispWinFrames[curFocusWin].drawGUIObjs();					//draw what user-modifiable fields are currently available
			dispWinFrames[curFocusWin].drawClickableBooleans();					//draw what user-modifiable fields are currently available
			dispWinFrames[curFocusWin].drawCustMenuObjs();					//customizable menu objects for each window
			//also launch custom function here
			dispWinFrames[curFocusWin].checkCustMenuUIObjs();			
			popStyle();	popMatrix();	
		}
	}//
	
	//use current display window's fill color for text color
	private void drawOnScreenData(){
		if(isDebugMode()){
			pushMatrix();pushStyle();			
			reInitInfoStr();
			addInfoStr(0,"mse loc on screen : " + new myPoint(mouseX, mouseY,0) + " mse loc in world :"+c.mseLoc +"  Eye loc in world :"+ c.eyeInWorld+ dispWinFrames[curFocusWin].getCamDisp());//" camera rx :  " + rx + " ry : " + ry + " dz : " + dz);
			String[] res = ((mySideBarMenu)dispWinFrames[dispMenuIDX]).getDebugData();		//get debug data for each UI object
			int numToPrint = min(res.length,80);
			for(int s=0;s<numToPrint;++s) {	addInfoStr(res[s]);}				//add info to string to be displayed for debug
			drawInfoStr(1.0f, dispWinFrames[curFocusWin].strkClr); 	
			popStyle();	popMatrix();		
		}
		else if(showInfo){
			pushMatrix();pushStyle();			
			reInitInfoStr();	
			String[] res = consoleStrings.toArray(new String[0]);
			int dispNum = min(res.length, 80);
			for(int i=0;i<dispNum;++i){addInfoStr(res[i]);}
		    drawInfoStr(1.1f, dispWinFrames[curFocusWin].strkClr); 
			popStyle();	popMatrix();	
		}
	}//drawOnScreenData
	
	protected void updateConsoleStrs(){
		++drawCount;
		if(drawCount % cnslStrDecay == 0){drawCount = 0;	consoleStrings.poll();}			
	}//updateConsoleStrs
	
	//////////////////////////////////
	// end draw routines

	//////////////////////////////
	// state and control flag handling
	//init boolean state machine flags for visible flags
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

	//get vis flag
	public final boolean getVisFlag(int idx){int bitLoc = 1<<(idx%32);return (_visFlags[idx/32] & bitLoc) == bitLoc;}	
	//this will not execute the code in setVisFlag, which might cause a loop
	public final void forceVisFlag(int idx, boolean val) {
		int flIDX = idx/32, mask = 1<<(idx%32);
		_visFlags[flIDX] = (val ?  _visFlags[flIDX] | mask : _visFlags[flIDX] & ~mask);
		//doesn't perform any other ops - to prevent looping
	}

	//base class flags init
	private void initBaseFlags(){baseFlags = new int[1 + numBaseFlags/32];for(int i =0; i<numBaseFlags;++i){forceBaseFlag(i,false);}}		
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
	 * display state flag indicator at top of window
	 * @param txt
	 * @param clrAra
	 * @param state
	 * @param stMult
	 * @param yOff
	 */
	protected void dispBoolStFlag(String txt, int[] clrAra, boolean state, float stMult, float yOff){
		if(state){
			setFill(clrAra, 255); 
			setStroke(clrAra, 255);
		} else {
			setColorValFill(IRenderInterface.gui_DarkGray,255); 
			noStroke();	
		}
		sphere(5);
		//text(""+txt,-xOff,yOff*.8f);	
		text(""+txt,stMult*txt.length(),yOff*.8f);	
	}
	
	
	
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
	
	public final void setShiftPressed(boolean val) {setBaseFlag(shiftKeyPressed,val);}
	public final void setAltPressed(boolean val) {setBaseFlag(altKeyPressed,val);}
	public final void setCntlPressed(boolean val) {setBaseFlag(cntlKeyPressed,val);}
	public final void setValueKeyPressed(boolean val){setBaseFlag(valueKeyPressed,val);}
	
	public final void setMouseClicked(boolean val) {setBaseFlag(mouseClicked,val);}
	public final void setModView(boolean val) {setBaseFlag(modView,val);}
	public final void setIsDrawing(boolean val) {setBaseFlag(drawing,val);}
	public final void setFinalInitDone(boolean val) {setBaseFlag(finalInitDone, val);}	
	public final void setSaveAnim(boolean val) {setBaseFlag(saveAnim, val);}
	public final void toggleSaveAnim() {setBaseFlag(saveAnim, !getBaseFlag(saveAnim));}
	
//////////////////////////////////////////////////////
/// user interaction
//////////////////////////////////////////////////////	
	//key is key pressed
	//keycode is actual physical key pressed == key if shift/alt/cntl not pressed.,so shift-1 gives key 33 ('!') but keycode 49 ('1')
	public final void keyPressed(){
		if(key==CODED) {
			if(!shiftIsPressed()){setShiftPressed(keyCode  == 16);} //16 == KeyEvent.VK_SHIFT
			if(!cntlIsPressed()){setCntlPressed(keyCode  == 17);}//17 == KeyEvent.VK_CONTROL			
			if(!altIsPressed()){setAltPressed(keyCode  == 18);}//18 == KeyEvent.VK_ALT
		} else {	
			sendKeyPressToWindows(key,keyCode);	//handle all other (non-numeric) keys			
		}
	}//keyPressed()

//	//handle pressing keys 0-9 (with or without shift,alt, cntl)
//	if ((keyCode>=48) && (keyCode <=57)) { 	handleNumberKeyPress(((int)key),keyCode);}
//	else {									
	/**
	 * handle key pressed
	 * @param keyVal 0-9, with or without shift ((keyCode>=48) && (keyCode <=57))
	 * @param keyCode actual code of key having been pressed
	 */
	protected abstract void handleKeyPress(char key, int keyCode);
	
	protected void saveSS(String prjNmShrt) {save(getScreenShotSaveName(prjNmShrt));}
	
	public final void keyReleased(){
		if(getBaseFlag(valueKeyPressed)) {endValueKeyPressed();}
		if(key==CODED) {
			if((getBaseFlag(shiftKeyPressed)) && (keyCode == 16)){endShiftKey();}
			if((getBaseFlag(cntlKeyPressed)) && (keyCode == 17)){endCntlKey();}
			if((getBaseFlag(altKeyPressed)) && (keyCode == 18)){endAltKey();}
		}
	}		
	
	private void sendKeyPressToWindows(char key, int keyCode) {
		handleKeyPress(key,keyCode);	//handle all other (non-numeric) keys in instancing class
		for(int i =0; i<numDispWins; ++i){dispWinFrames[i].setValueKeyPress(key, keyCode);}
		setValueKeyPressed(true);
	}
	//modview tied to shift key
	private void endShiftKey(){			clearBaseFlags(new int []{shiftKeyPressed, modView});	for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endShiftKey();}}
	private void endAltKey(){			setAltPressed(false);				for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endAltKey();}}
	private void endCntlKey(){			setCntlPressed(false);				for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endCntlKey();}}
	private void endValueKeyPressed() {	setValueKeyPressed(false);			for(int i =0; i<numDispWins; ++i){dispWinFrames[i].endValueKeyPress();}}
	
	public abstract double clickValModMult();
	public abstract boolean isClickModUIVal();

	public final void mouseMoved(){for(int i =0; i<numDispWins; ++i){if (dispWinFrames[i].handleMouseMove(mouseX, mouseY)){return;}}}
	public final void mousePressed() {
		setBaseFlag(mouseClicked, true);
		if(mouseButton == LEFT){							myMouseClicked(0);} 
		else if (mouseButton == RIGHT) {						myMouseClicked(1);}
		//for(int i =0; i<numDispWins; ++i){	if (dispWinFrames[i].handleMouseClick(mouseX, mouseY,c.getMseLoc(sceneCtrVals[sceneIDX]))){	return;}}
	}// mousepressed		
	private void myMouseClicked(int mseBtn){ 	for(int i =0; i<numDispWins; ++i){if (dispWinFrames[i].handleMouseClick(mouseX, mouseY,mseBtn)){return;}}}
	
	public final void mouseDragged(){
		if(mouseButton == LEFT){							myMouseDragged(0);}
		else if (mouseButton == RIGHT) {						myMouseDragged(1);}
	}//mouseDragged()
	private void myMouseDragged(int mseBtn){	for(int i =0; i<numDispWins; ++i){if (dispWinFrames[i].handleMouseDrag(mouseX, mouseY, pmouseX, pmouseY,c.getMseDragVec(),mseBtn)) {return;}}}
	
	//only for zooming
	public final void mouseWheel(MouseEvent event) {
		if(dispWinFrames.length < 1) {return;}
		if (dispWinFrames[curFocusWin].getFlags(myDispWindow.canChgView)) {// (canMoveView[curFocusWin]){	
			float mult = (getBaseFlag(shiftKeyPressed)) ? 50.0f * mouseWhlSens : 10.0f*mouseWhlSens;
			dispWinFrames[curFocusWin].handleViewChange(true,(mult * event.getCount()),0);
		}
	}

	public final void mouseReleased(){
		clearBaseFlags(new int[]{mouseClicked, modView});
		for(int i =0; i<numDispWins; ++i){dispWinFrames[i].handleMouseRelease();}
		setBaseFlag(drawing, false);
		//c.clearMsDepth();
	}//mouseReleased
	
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
				if(winDispIdxXOR[i]!= idx){dispWinFrames[winDispIdxXOR[i]].setShow(false);handleShowWin(i ,0,false); forceVisFlag(winFlagsXOR[i], false);}//not this window
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

	//get the ui rect values of the "master" ui region (another window) -> this is so ui objects of one window can be made, clicked, and shown displaced from those of the parent windwo
	public abstract float[] getUIRectVals(int idx);
	
	/**
	 * return a list of names to apply to mse-over display select buttons - an empty or null list will not display option
	 * @return
	 */
	public abstract String[] getMouseOverSelBtnNames();
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
				case 0 : {selectInput("Select a file to load from : ", "loadFromFile", currFileIOLoc);break;}
				case 1 : {selectOutput("Select a file to save to : ", "saveToFile", currFileIOLoc);break;}
			}
			((mySideBarMenu)dispWinFrames[dispMenuIDX]).hndlMouseRelIndiv();
		}
	}//handleFileCmd
	//turn off specific function button that might have been kept on during processing - btn must be in range of size of guiBtnSt[mySideBarMenu.btnAuxFuncIdx]
	//isSlowProc means function this was waiting on is a slow process and escaped the click release in the window (i.e. if isSlowProc then we must force button to be off)
	//public final void clearFuncBtnSt(int btn, boolean isSlowProc) {clearBtnState(mySideBarMenu.btnAuxFuncIdx,btn, isSlowProc);}

	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val){handleMenuBtnSelCmp(row, funcOffset, col, val, true);}					//display specific windows - multi-select/ always on if sel
	public final void handleMenuBtnSelCmp(int row, int funcOffset, int col, int val, boolean callFlags){
		if(!callFlags){			setMenuBtnState(row,col, val);		} 
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
	 * pass on to current display window the choice for debug selection 
	 * @param btn
	 * @param val
	 */
	public final void handleMenuBtnDebugSel(int btn,int val) {
		dispWinFrames[curFocusWin].handleSideMenuDebugSel(btn, val);
	}
	
	
	protected void setMenuBtnState(int row, int col, int val) {
		((mySideBarMenu)dispWinFrames[dispMenuIDX]).getGuiBtnSt()[row][col] = val;	
		if (val == 1) {
			//outStr2Scr("my_procApplet :: setMenuBtnState :: Note!!! Turning on button at row : " + row + "  col " + col + " without processing button's command.");
			((mySideBarMenu)dispWinFrames[dispMenuIDX]).setWaitForProc(row,col);}//if programmatically (not through UI) setting button on, then set wait for proc value true 
	}//setMenuBtnState	
	
	public void loadFromFile(File file){
		if (file == null) {
			outStr2Scr("my_procApplet :: setMenuBtnState ::Load was cancelled.");
		    return;
		} 		
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		dispWinFrames[curFocusWin].loadFromFile(file);
	
	}//loadFromFile
	
	public void saveToFile(File file){
		if (file == null) {
			outStr2Scr("my_procApplet :: setMenuBtnState ::Save was cancelled.");
		    return;
		} 
		//reset to match navigation in file IO window
		currFileIOLoc = file;
		dispWinFrames[curFocusWin].saveToFile(file);
	}//saveToFile	
	
	
	//2d range checking of point
	public final boolean ptInRange(double x, double y, double minX, double minY, double maxX, double maxY){return ((x > minX)&&(x <= maxX)&&(y > minY)&&(y <= maxY));}	

	public final void setCamOrient_Glbl(){rotateX(rx);rotateY(ry); rotateX(PI/(2.0f));		}//sets the rx, ry, pi/2 orientation of the camera eye	
	public final void unSetCamOrient_Glbl(){rotateX(-PI/(2.0f)); rotateY(-ry);   rotateX(-rx); }//reverses the rx,ry,pi/2 orientation of the camera eye - paints on screen and is unaffected by camera movement

	//save screenshot
	public final void savePic(){	
		//if(!flags[this.runSim]) {return;}//don't save until actually running simulation
		//idx 0 is directory, idx 1 is file name prefix
		String[] ssName = dispWinFrames[curFocusWin].getSaveFileDirName();
		if(ssName.length != 2) {setBaseFlag(saveAnim, false);return;}
		//save(screenShotPath + prjNmShrt + ((animCounter < 10) ? "0000" : ((animCounter < 100) ? "000" : ((animCounter < 1000) ? "00" : ((animCounter < 10000) ? "0" : "")))) + animCounter + ".jpg");		
		String saveDirAndSubDir = ssName[0] + //"run_"+String.format("%02d", runCounter)  + 
				ssName[1] + File.separatorChar;		
		save(saveDirAndSubDir + String.format("%06d", animCounter) + ".jpg");		
		animCounter++;		
	}
	
	public void line(double x1, double y1, double z1, double x2, double y2, double z2){line((float)x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2 );}
	public void line(myPoint p1, myPoint p2){line((float)p1.x,(float)p1.y,(float)p1.z,(float)p2.x,(float)p2.y,(float)p2.z);}
	public void line(myPointf p1, myPointf p2){line(p1.x,p1.y,p1.z,p2.x,p2.y,p2.z);}
	public void line(myPointf a, myPointf b, int stClr, int endClr){
		beginShape();
		this.strokeWeight(1.0f);
		this.setColorValStroke(stClr, 255);
		this.vertex((float)a.x,(float)a.y,(float)a.z);
		this.setColorValStroke(endClr,255);
		this.vertex((float)b.x,(float)b.y,(float)b.z);
		endShape();
	}
	
	//print out multiple-line text to screen
	public final void ml_text(String str, float x, float y){
		String[] res = str.split("\\r?\\n");
		float disp = 0;
		for(int i =0; i<res.length; ++i){
			text(res[i],x, y+disp);		//add console string output to screen display- decays over time
			disp += 12;
		}
	}
	//print out a string ara with perLine # of strings per line
	public final void outStr2ScrAra(String[] sAra, int perLine){
		for(int i=0;i<sAra.length; i+=perLine){
			String s = "";
			for(int j=0; j<perLine; ++j){	s+= sAra[i+j]+ "\t";}
			outStr2Scr(s,true);}
	}
	//print out string in display window
	public final void outStr2Scr(String str){outStr2Scr(str,true);}
	//print informational string data to console, and to screen
	public final void outStr2Scr(String str, boolean showDraw){
		if(trim(str) != ""){	System.out.println(str);}
		String[] res = str.split("\\r?\\n");
		if(showDraw){
			for(int i =0; i<res.length; ++i){
				consoleStrings.add(res[i]);		//add console string output to screen display- decays over time
			}
		}
	}
	
	public String getScreenShotSaveName(String prjNmShrt) {
		return sketchPath() +File.separatorChar+prjNmShrt+"_"+getDateString()+File.separatorChar+prjNmShrt+"_img"+getTimeString() + ".jpg";
	}
	
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
	
	
	//handle user-driven file load or save - returns a filename + filepath string
	public String FileSelected(File selection){
		if (null==selection){return null;}
		return selection.getAbsolutePath();		
	}//FileSelected
	

	public String getFName(String fNameAndPath){
		String[] strs = fNameAndPath.split("/");
		return strs[strs.length-1];
	}
	
	//load a file as text strings
	public String[] loadFileIntoStringAra(String fileName, String dispYesStr, String dispNoStr){
		String[] strs = null;
		try{
			strs = loadStrings(fileName);
			System.out.println(dispYesStr+"\tLength : " + strs.length);
		} catch (Exception e){System.out.println("!!"+dispNoStr);return null;}
		return strs;		
	}//loadFileIntoStrings

	public final void displayHeader() { // Displays title and authors face on screen
	    float stVal = 17;
	    int idx = 1;	
	    translate(0,10,0);
	    fill(0); text("Shift-Click-Drag to change view.",width-190, stVal*idx++); noFill(); 
	    fill(0); text("Shift-RClick-Drag to zoom.",width-160, stVal*idx++); noFill();
	    fill(0); text("John Turner",width-75, stVal*idx++); noFill();	
	}
	
	//project passed point onto box surface based on location - to help visualize the location in 3d
	public final void drawProjOnBox(myPoint p){
		//myPoint[]  projOnPlanes = new myPoint[6];
		myPoint prjOnPlane;
		//public final myPoint intersectPl(myPoint E, myVector T, myPoint A, myPoint B, myPoint C) { // if ray from E along T intersects triangle (A,B,C), return true and set proposal to the intersection point
		pushMatrix();
		translate(-p.x,-p.y,-p.z);
		for(int i  = 0; i< 6; ++i){				
			prjOnPlane = bndChkInCntrdBox3D(intersectPl(p, boxNorms[i], boxWallPts[i][0],boxWallPts[i][1],boxWallPts[i][2]));				
			show(prjOnPlane,5,rgbClrs[i/2],rgbClrs[i/2], false);				
		}
		popMatrix();
	}//drawProjOnBox
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
	
	//find mouse "force" exerted upon a particular location - distance from mouse to passed location
	public myVectorf mouseForceAtLoc(float msClickForce, myPointf _loc, boolean attractMode){
		myPointf mouseFrcLoc = c.getTransMseLoc(new myPointf(gridDimX/2.0f, gridDimY/2.0f,gridDimZ/2.0f));// new myPointf(c.dfCtr.x+gridDimX/2.0f,c.dfCtr.y+gridDimY/2.0f,c.dfCtr.z+gridDimZ/2.0f);// new myVector(lstClkX,0,lstClkY);//translate click location to where the space where the boids are	
		myVectorf resFrc = new myVectorf(_loc, mouseFrcLoc);		
		float sqDist = resFrc.sqMagn;
		if(sqDist<MyMathUtils.eps_f){sqDist=MyMathUtils.eps_f;}
		float mag = (attractMode? 1 : -1) * msClickForce / sqDist;
		resFrc._scale(mag);
		return resFrc;	
	}//mouseForceAtLoc
	
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
		outStr2Scr("String list of Sphere " + type + " shuffled");
		return _list;
	}//shuffleStrList
	
	//random location within coords[0] and coords[1] extremal corners of a cube - bnds is to give a margin of possible random values
	public myVectorf getRandPosInCube(float[][] coords, float bnds){
		ThreadLocalRandom tr = ThreadLocalRandom.current();
		return new myVectorf(
				tr.nextDouble(coords[0][0]+bnds,(coords[0][0] + coords[1][0] - bnds)),
				tr.nextDouble(coords[0][1]+bnds,(coords[0][1] + coords[1][1] - bnds)),
				tr.nextDouble(coords[0][2]+bnds,(coords[0][2] + coords[1][2] - bnds)));}		
	public final myPoint getScrLocOf3dWrldPt(myPoint pt){	return new myPoint(screenX((float)pt.x,(float)pt.y,(float)pt.z),screenY((float)pt.x,(float)pt.y,(float)pt.z),screenZ((float)pt.x,(float)pt.y,(float)pt.z));}
	
	public final myPoint bndChkInBox2D(myPoint p){p.set(Math.max(0,Math.min(p.x,grid2D_X)),Math.max(0,Math.min(p.y,grid2D_Y)),0);return p;}
	public final myPoint bndChkInBox3D(myPoint p){p.set(Math.max(0,Math.min(p.x,gridDimX)), Math.max(0,Math.min(p.y,gridDimY)),Math.max(0,Math.min(p.z,gridDimZ)));return p;}	
	public final myPoint bndChkInCntrdBox3D(myPoint p){
		p.set(Math.max(-hGDimX,Math.min(p.x,hGDimX)), 
				Math.max(-hGDimY,Math.min(p.y,hGDimY)),
				Math.max(-hGDimZ,Math.min(p.z,hGDimZ)));return p;}	
	
	@Override
	public final void translate(myPoint p){translate((float)p.x,(float)p.y,(float)p.z);}
	@Override
	public final void translate(myPointf p){translate(p.x,p.y,p.z);}
	@Override
	public final void translate(double x, double y, double z){translate((float)x,(float)y,(float)z);}
	//public final void translate(double x, double y){translate((float)x,(float)y);}
	@Override
	public final void rotate(float thet, myPoint axis){rotate(thet, (float)axis.x,(float)axis.y,(float)axis.z);}
	@Override
	public final void rotate(float thet, myPointf axis){rotate(thet, axis.x,axis.y,axis.z);}
	@Override
	public final void rotate(float thet, double x, double y, double z){rotate(thet, (float)x,(float)y,(float)z);}
	//************************************************************************
	//**** SPIRAL
	//************************************************************************
	//3d rotation - rotate P by angle a around point G and axis normal to plane IJ
	public final myPoint R(myPoint P, double a, myVector I, myVector J, myPoint G) {
		double x= myVector._dot(new myVector(G,P),myVector._unit(I)), y=myVector._dot(new myVector(G,P),myVector._unit(J)); 
		double c=Math.cos(a), s=Math.sin(a); 
		double iXVal = x*c-x-y*s, jYVal= x*s+y*c-y;			
		return myPoint._add(P,iXVal,I,jYVal,J); 
	}; 
	public final myPointf R(myPointf P, float a, myVectorf I, myVectorf J, myPointf G) {
		double x= myVectorf._dot(new myVectorf(G,P),myVectorf._unit(I)), y=myVectorf._dot(new myVectorf(G,P),myVectorf._unit(J)); 
		double c=Math.cos(a), s=Math.sin(a); 
		float iXVal = (float) (x*c-x-y*s), jYVal= (float) (x*s+y*c-y);			
		return myPointf._add(P,iXVal,I,jYVal,J); 
	}; 
		
	public final myPoint R(myPoint Q, myPoint C, myPoint P, myPoint R) { // returns rotated version of Q by angle(CP,CR) parallel to plane (C,P,R)
		myVector I0=myVector._unit(C,P), I1=myVector._unit(C,R), V=new myVector(C,Q); 
		double c=myPoint._dist(I0,I1), s=Math.sqrt(1.-(c*c)); 
		if(Math.abs(s)<0.00001) return Q;		
		myVector J0=myVector._add(myVector._mult(I1,1./s),myVector._mult(I0,-c/s));  
		myVector J1=myVector._add(myVector._mult(I0,-s),myVector._mult(J0,c));  
		double x=V._dot(I0), y=V._dot(J0);  
		return myPoint._add(Q,x,myVector._sub(I1,I0),y,myVector._sub(J1,J0)); 
	} 	
		
	public final myPointf R(myPointf Q, myPointf C, myPointf P, myPointf R) { // returns rotated version of Q by angle(CP,CR) parallel to plane (C,P,R)
		myVectorf I0=myVectorf._unit(C,P), I1=myVectorf._unit(C,R), V=new myVectorf(C,Q); 
		double c=myPointf._dist(I0,I1), s=Math.sqrt(1.-(c*c)); 
		if(Math.abs(s)<0.00001) return Q;		
		myVectorf J0=myVectorf._add(myVectorf._mult(I1,1./s),myVectorf._mult(I0,-c/s));  
		myVectorf J1=myVectorf._add(myVectorf._mult(I0,-s),myVectorf._mult(J0,c));  
		float x=V._dot(I0), y=V._dot(J0);  
		return myPointf._add(Q,x,myVectorf._sub(I1,I0),y,myVectorf._sub(J1,J0)); 
	} 	
		
			
	public myCntlPt R(myCntlPt P, double a, myVector I, myVector J, myPoint G) {
		double x= myVector._dot(new myVector(G,P),myVector._unit(I)), y=myVector._dot(new myVector(G,P),myVector._unit(J)); 
		double c=Math.cos(a), s=Math.sin(a); 
		double iXVal = x*c-x-y*s, jYVal= x*s+y*c-y;		
		return new myCntlPt( myPoint._add(P,iXVal,I,jYVal,J), P.r, P.w); 
	};
		
	public myCntlPtf R(myCntlPtf P, float a, myVectorf I, myVectorf J, myPointf G) {
		float x= myVectorf._dot(new myVectorf(G,P),myVectorf._unit(I)), y=myVectorf._dot(new myVectorf(G,P),myVectorf._unit(J)); 
		float c=(float) Math.cos(a), s=(float) Math.sin(a); 
		float iXVal = x*c-x-y*s, jYVal= x*s+y*c-y;		
		return new myCntlPtf( myPointf._add(P,iXVal,I,jYVal,J), P.r, P.w); 
	};
				
	public final myPoint PtOnSpiral(myPoint A, myPoint B, myPoint C, double t) {
		//center is coplanar to A and B, and coplanar to B and C, but not necessarily coplanar to A, B and C
		//so center will be coplanar to mp(A,B) and mp(B,C) - use mpCA midpoint to determine plane mpAB-mpBC plane?
		myPoint mAB = new myPoint(A,.5f, B), mBC = new myPoint(B,.5f, C), mCA = new myPoint(C,.5f, A);
		myVector mI = myVector._unit(mCA,mAB), mTmp = myVector._cross(mI,myVector._unit(mCA,mBC)), mJ = myVector._unit(mTmp._cross(mI));	//I and J are orthonormal
		double a =spiralAngle(A,B,B,C), s =spiralScale(A,B,B,C);
		
		//myPoint G = spiralCenter(a, s, A, B, mI, mJ); 
		myPoint G = spiralCenter(A, mAB, B, mBC); 
		return new myPoint(G, Math.pow(s,t), R(A,t*a,mI,mJ,G));
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
		return new myPointf(G, (float)Math.pow(s,t), R(A,t*a,mI,mJ,G));
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
	
	
	public final void cylinder_NoFill(myPoint A, myPoint B, float r, int c1, int c2) {
		myPoint P = A;
		myVector V = new myVector(A,B);
		myVector I = c.getDrawSNorm();//U(Normal(V));
		myVector J = I._cross(V)._normalize(); 
		float da = TWO_PI/36;
		noFill();
		beginShape(QUAD_STRIP);
			for(float a=0; a<=TWO_PI+da; a+=da) {
				stroke(c1); 
				//gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J)); 
				stroke(c2); 
				gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J,1,V));}
		endShape();
	}

	
	public final void cylinder_NoFill(myPointf A, myPointf B, float r, int c1, int c2) {
		myPointf P = A;
		myVectorf V = new myVectorf(A,B);
		myVectorf I = c.getDrawSNorm_f();//U(Normal(V));
		myVectorf J = I._cross(V)._normalize(); 
		float da = TWO_PI/36;
		noFill();
		beginShape(QUAD_STRIP);
			for(float a=0; a<=TWO_PI+da; a+=da) {
				stroke(c1); 
				//gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPointf._add(P,r*cos(a),I,r*sin(a),J)); 
				stroke(c2); 
				gl_vertex(myPointf._add(P,r*cos(a),I,r*sin(a),J,1,V));}
		endShape();
	}

	
	
	public final void cylinder(myPoint A, myPoint B, float r, int c1, int c2) {
		myPoint P = A;
		myVector V = new myVector(A,B);
		myVector I = c.getDrawSNorm();//U(Normal(V));
		myVector J = I._cross(V)._normalize(); 
		float da = TWO_PI/36;
		beginShape(QUAD_STRIP);
			for(float a=0; a<=TWO_PI+da; a+=da) {
				fill(c1); 
				//gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J)); 
				fill(c2); 
				gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J,1,V));}
		endShape();
	}

	public final void cylinder(myPointf A, myPointf B, float r, int c1, int c2) {
		myVectorf V = new myVectorf(A,B);
		myVectorf I = c.getDrawSNorm_f();//U(Normal(V));
		myVectorf J = I._cross(V)._normalize(); 
		float da = TWO_PI/36;
		beginShape(QUAD_STRIP);
			for(float a=0; a<=TWO_PI+da; a+=da) {
				fill(c1); 
				//gl_vertex(myPoint._add(P,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPointf._add(A,r*cos(a),I,r*sin(a),J)); 
				fill(c2); 
				gl_vertex(myPointf._add(A,r*cos(a),I,r*sin(a),J,1,V));}
		endShape();
	}

	//draw a circle - JT
	
	/**
	 * Build a set of n points inscribed on a circle centered at p in plane I,J
	 * @param p center point
	 * @param r circle radius
	 * @param I, J axes of plane
	 * @param n # of points
	 * @return array of n equal-arc-length points centered around p
	 */
	public myPoint[] buildCircleInscribedPoints(myPoint p,  float r, myVector I, myVector J,int n) {
		myPoint[] pts = new myPoint[n];
		pts[0] = new myPoint(p,r,myVector._unit(I));
		float a = (twoPi_f)/(1.0f*n);
		for(int i=1;i<n;++i){pts[i] = R(pts[i-1],a,J,I,p);}
		return pts;
	}
	public myPointf[] buildCircleInscribedPoints(myPointf p,  float r, myVectorf I, myVectorf J,int n) {
		myPointf[] pts = new myPointf[n];
		pts[0] = new myPointf(p,r,myVectorf._unit(I));
		float a = (twoPi_f)/(1.0f*n);
		for(int i=1;i<n;++i){pts[i] = R(pts[i-1],a,J,I,p);}
		return pts;
	}
	
	/**
	 * draw a circle centered at P with specified radius r in plane proscribed by passed axes using n number of points
	 * @param P center
	 * @param r radius
	 * @param I x axis
	 * @param J y axis
	 * @param n # of points to use
	 */
	@Override
	public final void drawCircle(myPoint P, float r, myVector I, myVector J, int n) {
		myPoint[] pts = buildCircleInscribedPoints(P,r,I,J,n);
		pushMatrix(); pushStyle();noFill(); show(pts);popStyle();popMatrix();
	}; 
	
	public final void drawCircle(myPointf P, float r, myVectorf I, myVectorf J, int n) {
		myPointf[] pts = buildCircleInscribedPoints(P,r,I,J,n);
		pushMatrix(); pushStyle();noFill(); show(pts);popStyle();popMatrix();
	}; 
	
	public final void circle(myPoint p, float r){ellipse((float)p.x, (float)p.y, r, r);}
	
	public final void circle(myPointf p, float r){ellipse(p.x, p.y, r, r);}
	public void circle(float x, float y, float r1, float r2){ellipse(x,y, r1, r2);}
	/**
	 * draw a 6 pointed star centered at p inscribed in circle radius r
	 */
	public final void star(myPointf p, float r) {
		myPointf[] pts = buildCircleInscribedPoints(p,r,myVectorf.FORWARD,myVectorf.RIGHT,6);
		triangle(pts[0], pts[2],pts[4]);
		triangle(pts[1], pts[3],pts[5]);
	}
	public final void triangle(myPointf a, myPointf b, myPointf c) {triangle(a.x,a.y, b.x, b.y, c.x, c.y);}
	
	public void noteArc(float[] dims, int[] noteClr){
		noFill();
		setStroke(noteClr, noteClr[3]);
		strokeWeight(1.5f*dims[3]);
		arc(0,0, dims[2], dims[2], dims[0] - HALF_PI, dims[1] - HALF_PI);
	}
	//draw a ring segment from alphaSt in radians to alphaEnd in radians
	public void noteArc(myPoint ctr, float alphaSt, float alphaEnd, float rad, float thickness, int[] noteClr){
		noFill();
		setStroke(noteClr,noteClr[3]);
		strokeWeight(thickness);
		arc((float)ctr.x, (float)ctr.y, rad, rad, alphaSt - HALF_PI, alphaEnd- HALF_PI);
	}
	
	
	public void bezier(myPoint A, myPoint B, myPoint C, myPoint D) {bezier((float)A.x,(float)A.y,(float)A.z,(float)B.x,(float)B.y,(float)B.z,(float)C.x,(float)C.y,(float)C.z,(float)D.x,(float)D.y,(float)D.z);} // draws a cubic Bezier curve with control points A, B, C, D
	public void bezier(myPoint [] C) {bezier(C[0],C[1],C[2],C[3]);} // draws a cubic Bezier curve with control points A, B, C, D
	public myPoint bezierPoint(myPoint[] C, float t) {return new myPoint(bezierPoint((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierPoint((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierPoint((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
	public myVector bezierTangent(myPoint[] C, float t) {return new myVector(bezierTangent((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierTangent((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierTangent((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
	
	
	public final myPoint Mouse() {return new myPoint(mouseX, mouseY,0);}                                          			// current mouse location
	public myVector MouseDrag() {return new myVector(mouseX-pmouseX,mouseY-pmouseY,0);};                     			// vector representing recent mouse displacement
	
	//public final int color(myPoint p){return color((int)p.x,(int)p.z,(int)p.y);}	//needs to be x,z,y for some reason - to match orientation of color frames in z-up 3d geometry
	public final int color(myPoint p){return color((int)p.x,(int)p.y,(int)p.z);}	
	public final int color(myPointf p){return color((int)p.x,(int)p.y,(int)p.z);}	
	/**
	 * vertex with texture coordinates
	 * @param P vertex location
	 * @param u,v txtr coords
	 */
	public void vTextured(myPointf P, float u, float v) {vertex((float)P.x,(float)P.y,(float)P.z,(float)u,(float)v);};                         

	@Override
	public final void gl_normal(myVector V) {normal((float)V.x,(float)V.y,(float)V.z);}                                          // changes normal for smooth shading
	@Override
	public final void gl_vertex(myPoint P) {vertex((float)P.x,(float)P.y,(float)P.z);}                                           // vertex for shading or drawing
	@Override
	public final void gl_normal(myVectorf V) {normal(V.x,V.y,V.z);}                                          // changes normal for smooth shading
	@Override
	public final void gl_vertex(myPointf P) {vertex(P.x,P.y,P.z);}                                           // vertex for shading or drawing
	
	@Override
	public final void drawSphere(float rad) {sphere(rad);}
	@Override
	public final void setSphereDetail(int det) {sphereDetail(det);}
	
	/**
	 * draw a 2 d ellipse 
	 * @param a 4 element array : x,y,x rad, y rad
	 */
	@Override
	public void drawEllipse(float[] a) {ellipse(a[0],a[1],a[2],a[3]);}
	
	
	/////////////
	// show functions 
	public final void show(myPointf P, float rad, int det, int[] fclr, int[] sclr) {
		pushMatrix(); pushStyle(); 
		if((fclr!= null) && (sclr!= null)){setFill(fclr,255); setStroke(sclr,255);}
		sphereDetail(det);
		translate(P.x,P.y,P.z); 
		sphere(rad); 
		popStyle(); popMatrix();
	}// render sphere of radius r and center P)
	public final void show(myPoint P, double r,int fclr, int sclr, boolean flat) {//TODO make flat circles for points if flat
		pushMatrix(); pushStyle(); 
		if((fclr!= -1) && (sclr!= -1)){setColorValFill(fclr,255); setColorValStroke(sclr,255);}
		if(!flat){
			translate((float)P.x,(float)P.y,(float)P.z); 
			sphereDetail(5);
			sphere((float)r);
		} else {
			translate((float)P.x,(float)P.y,0); 
			this.circle(0,0,(float)r,(float)r);				
		}
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	public final void show(myPoint P, double rad, int fclr, int sclr, int tclr, String txt) {
		pushMatrix(); pushStyle(); 
		if((fclr!= -1) && (sclr!= -1)){setColorValFill(fclr,255); setColorValStroke(sclr,255);}
		sphereDetail(5);
		translate((float)P.x,(float)P.y,(float)P.z); 
		setColorValFill(tclr,255);setColorValStroke(tclr,255);
		showOffsetText(1.2f * (float)rad,tclr, txt);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	public final void show(myPoint P, double r, int fclr, int sclr) {
		pushMatrix(); pushStyle(); 
		if((fclr!= -1) && (sclr!= -1)){setColorValFill(fclr,255); setColorValStroke(sclr,255);}
		sphereDetail(5);
		translate((float)P.x,(float)P.y,(float)P.z); 
		sphere((float)r); 
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	//public final void show(myPoint P, double r){show(P,r, gui_Black, gui_Black, false);}
	public final void show(myPoint P, String s) {text(s, (float)P.x, (float)P.y, (float)P.z); } // prints string s in 3D at P
	public final void show(myPoint P, String s, myVector D) {text(s, (float)(P.x+D.x), (float)(P.y+D.y), (float)(P.z+D.z));  } // prints string s in 3D at P+D
	public final void show(myPoint P, double r, String s, myVector D){show(P,r, gui_Black, gui_Black, false);pushStyle();setColorValFill(gui_Black,255);show(P,s,D);popStyle();}
	public final void show(myPoint P, double r, String s, myVector D, int clr, boolean flat){show(P,r, clr, clr, flat);pushStyle();setColorValFill(clr,255);show(P,s,D);popStyle();}
	public final void show(myPoint[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};                     
	public final void show(myPoint[] ara, myVector norm) {beginShape();gl_normal(norm); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};   
	public final void showVec( myPointf ctr, float len, myVectorf v){line(ctr.x,ctr.y,ctr.z,ctr.x+(v.x)*len,ctr.y+(v.y)*len,ctr.z+(v.z)*len);}
	
	/**
	 * this will properly format and display a string of text, and will translate the width, so multiple strings can be displayed on the same line with different colors
	 * @param tclr
	 * @param txt
	 */
	public final void showOffsetText_RightSideMenu(int[] tclr, float mult,  String txt) {
		setFill(tclr,tclr[3]);setStroke(tclr,tclr[3]);
		text(txt,0.0f,0.0f,0.0f);
		translate(txt.length()*mult, 0.0f,0.0f);		
	}
	
	public final void showOffsetText(float d, int tclr, String txt){
		setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
		text(txt, d, d,d); 
	}	
	public final void showOffsetText(myPointf loc, int tclr, String txt){
		setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
		text(txt, loc.x, loc.y, loc.z); 
	}	
	public final void showOffsetText2D(float d, int tclr, String txt){
		setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
		text(txt, d, d,0); 
	}
	public final void showOffsetTextAra(float d, int tclr, String[] txtAra){
		setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
		float y = d;
		for (String txt : txtAra) {
			text(txt, d, y, d);
			y+=10;
		}
	}
		
	public final void show(myPointf P, float r,int fclr, int sclr, boolean flat) {//TODO make flat circles for points if flat
		pushMatrix(); pushStyle(); 
		if((fclr!= -1) && (sclr!= -1)){setColorValFill(fclr,255); setColorValStroke(sclr,255);}
		if(!flat){
			drawSphere(P, r, 5);
		} else {
			translate(P.x,P.y,0); 
			circle(0,0,r,r);				
		}
		popStyle(); popMatrix();
	} // render sphere of radius r and center P)	
	public final void showBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		pushMatrix(); pushStyle(); 
		translate(P.x,P.y,P.z);
		fill(255,255,255,150);
		stroke(0,0,0,255);
		rect(0,6.0f,txt.length()*7.8f,-15);
		tclr = gui_Black;		
		setFill(fclr,255); setStroke(strkclr,255);			
		drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	//translate to point, draw point and text
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, String txt) {
		pushMatrix(); pushStyle(); 
		setFill(fclr,255); setStroke(strkclr,255);		
		translate(P.x,P.y,P.z); 
		drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(1.2f * rad,tclr, txt);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	//textP is location of text relative to point
	public final void showNoBox_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		pushMatrix(); pushStyle(); 
		translate(P.x,P.y,P.z); 
		setFill(fclr,255); setStroke(strkclr,255);			
		drawSphere(myPointf.ZEROPT, rad, det);
		showOffsetText(txtP,tclr, txt);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	//textP is location of text relative to point
	public final void showCrclNoBox_ClrAra(myPointf P, float rad, int[] fclr, int[] strkclr, int tclr, myPointf txtP, String txt) {
		pushMatrix(); pushStyle(); 
		translate(P.x,P.y,P.z); 
		setFill(fclr,255); setStroke(strkclr,255);			
		ellipse(0,0,rad,rad); 
		ellipse(0,0,2,2);
		showOffsetText(txtP,tclr, txt);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	
	//show sphere of certain radius
	public final void show_ClrAra(myPointf P, float rad, int det, int[] fclr, int[] strkclr) {
		pushMatrix(); pushStyle(); 
		if((fclr!= null) && (strkclr!= null)){setFill(fclr,255); setStroke(strkclr,255);}
		drawSphere(P, rad, det);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	/////////////
	// show functions using color idxs 
	public final void showBox(myPointf P, float rad, int det, int[] clrs, String[] txtAra, float[] rectDims) {
		pushMatrix(); pushStyle(); 			
			setColorValFill(clrs[0],255); 
			setColorValStroke(clrs[1],255);
			translate(P.x,P.y,P.z);
			drawSphere(myPointf.ZEROPT, rad, det);			
			
			pushMatrix(); pushStyle();
			//make sure box doesn't extend off screen
				transToStayOnScreen(P,rectDims);
				fill(255,255,255,150);
				stroke(0,0,0,255);
				strokeWeight(2.5f);
				drawRect(rectDims);
				translate(rectDims[0],0,0);
				showOffsetTextAra(1.2f * rad, clrs[2], txtAra);
			popStyle(); popMatrix();
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	public final void show(myPointf P, float rad, int det, int[] clrs) {//only call with set fclr and sclr - idx0 == fill, idx 1 == strk
		pushMatrix(); pushStyle(); 
		setColorValFill(clrs[0],255); 
		setColorValStroke(clrs[1],255);
		drawSphere(P, rad, det);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	public final void show(myPointf P, float rad, int det, int[] clrs, String[] txtAra) {//only call with set fclr and sclr - idx0 == fill, idx 1 == strk, idx2 == txtClr
		pushMatrix(); pushStyle(); 
		setColorValFill(clrs[0],255); 
		setColorValStroke(clrs[1],255);
		drawSphere(P, rad, det);
		translate(P.x,P.y,P.z); 
		showOffsetTextAra(1.2f * rad, clrs[2], txtAra);
		popStyle(); popMatrix();} // render sphere of radius r and center P)
	
	/////////////
	//base show functions
	public final void show(myPointf P, float rad, int det){			
		pushMatrix(); pushStyle(); 
		fill(0,0,0,255); 
		stroke(0,0,0,255);
		drawSphere(P, rad, det);
		popStyle(); popMatrix();
	}
	
	public final void show(myPoint P, double rad, int det){			
		pushMatrix(); pushStyle(); 
		fill(0,0,0,255); 
		stroke(0,0,0,255);
		drawSphere(P, rad, det);
		popStyle(); popMatrix();
	}
	
	public final void drawSphere(myPointf P, float rad, int det) {
		pushMatrix(); pushStyle(); 
		sphereDetail(det);
		translate(P.x,P.y,P.z); 
		sphere(rad); 
		popStyle(); popMatrix();
	}
	public final void drawSphere(myPoint P, double rad, int det) {
		pushMatrix(); pushStyle(); 
		sphereDetail(det);
		translate(P.x,P.y,P.z); 
		sphere((float) rad); 
		popStyle(); popMatrix();
	}
	
	public final void show(myPointf P, String s) {text(s, P.x, P.y, P.z); } // prints string s in 3D at P
	public final void show(myPointf P, String s, myVectorf D) {text(s, (P.x+D.x), (P.y+D.y),(P.z+D.z));  } // prints string s in 3D at P+D
	public final void show(myPointf P, float r, String s, myVectorf D){show(P,r, gui_Black, gui_Black, false);pushStyle();setColorValFill(gui_Black,255);show(P,s,D);popStyle();}
	public final void show(myPointf P, float r, String s, myVectorf D, int clr, boolean flat){show(P,r, clr, clr, flat);pushStyle();setColorValFill(clr,255);show(P,s,D);popStyle();}
	public final void show(myPointf[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};                     
	public final void show(myPointf[] ara, myVectorf norm) {beginShape();gl_normal(norm); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};                     
	
	public final void showNoClose(myPoint[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape();};                     
	public final void showNoClose(myPointf[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape();};                     
	///end show functions
	
	public final void curveVertex(myPoint P) {curveVertex((float)P.x,(float)P.y);};                                           // curveVertex for shading or drawing
	public final void curveVertex(myPointf P) {curveVertex(P.x,P.y);};                                           // curveVertex for shading or drawing
	public final void curve(myPoint[] ara) {if(ara.length == 0){return;}beginShape(); curveVertex(ara[0]);for(int i=0;i<ara.length;++i){curveVertex(ara[i]);} curveVertex(ara[ara.length-1]);endShape();};                      // volume of tet 	
	public final void curve(myPointf[] ara) {if(ara.length == 0){return;}beginShape(); curveVertex(ara[0]);for(int i=0;i<ara.length;++i){curveVertex(ara[i]);} curveVertex(ara[ara.length-1]);endShape();};                      // volume of tet 	
	
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
	/**
	 * draw a rectangle in 2D using the passed values as x,y,w,h
	 * @param a 4 element array : x,y,w,h
	 */
	@Override
	public final void drawRect(float[] a){rect(a[0],a[1],a[2],a[3]);}				//rectangle from array of floats : x, y, w, h
	
	
	/**
	 * this will translate the passed box dimensions to keep them on the screen
	 * using p as start point and rectDims[2] and rectDims[3] as width and height
	 * @param P starting point
	 * @param rectDims box dimensions 
	 */
	@Override
	public final void transToStayOnScreen(myPointf P, float[] rectDims) {
		float xLocSt = P.x + rectDims[0], xLocEnd = xLocSt + rectDims[2];
		float yLocSt = P.y + rectDims[1], yLocEnd = yLocSt + rectDims[3];
		float transX = 0.0f, transY = 0.0f;
		if (xLocSt < 0) {	transX = -1.0f * xLocSt;	} else if (xLocEnd > width) {transX = width - xLocEnd - 20;}
		if (yLocSt < 0) {	transY = -1.0f * yLocSt;	} else if (yLocEnd > height) {transY = height - yLocEnd - 20;}
		translate(transX,transY);		
	}
	
	
	
	/////////////////////		
	///color utils
	/////////////////////
	//		public final int  // set more colors using Menu >  Tools > Color Selector
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
		fill((int)p.x,(int)p.y,(int)p.z);
		stroke((int)p.x,(int)p.y,(int)p.z);
		rect(x,y,w,h);
		//show(p,r,-1);
	}	
	public final myPoint WrldToScreen(myPoint wPt){return new myPoint(screenX((float)wPt.x,(float)wPt.y,(float)wPt.z),screenY((float)wPt.x,(float)wPt.y,(float)wPt.z),screenZ((float)wPt.x,(float)wPt.y,(float)wPt.z));}
	public final int[][] triColors = new int[][] {{gui_DarkMagenta,gui_DarkBlue,gui_DarkGreen,gui_DarkCyan}, {gui_LightMagenta,gui_LightBlue,gui_LightGreen,gui_TransCyan}};
	

	@Override
	public final void setFill(int[] clr, int alpha){fill(clr[0],clr[1],clr[2], alpha);}
	@Override
	public final void setStroke(int[] clr, int alpha){stroke(clr[0],clr[1],clr[2], alpha);}
	/**
	 * set stroke weight
	 */
	@Override
	public final void setStrokeWt(float stW) {	strokeWeight(stW);}

	public final void setColorValFill(int colorVal, int alpha){
		if(colorVal == gui_TransBlack) {
			fill(0x00010100);//	have to use hex so that alpha val is not lost    
		} else {
			setFill(getClr(colorVal, alpha), alpha);
		}	
	}//setcolorValFill
	
	public final void setColorValStroke(int colorVal, int alpha){
		setStroke(getClr(colorVal, alpha), alpha);		
	}//setcolorValStroke	
	
	public final void setColorValFillAmb(int colorVal, int alpha){
		if(colorVal == gui_TransBlack) {
			fill(0x00010100);//	have to use hex so that alpha val is not lost    
			ambient(0,0,0);
		} else {
			int[] fillClr = getClr(colorVal, alpha);
			setFill(fillClr, alpha);
			ambient(fillClr[0],fillClr[1],fillClr[2]);
		}		
	}//setcolorValFill
	
	//returns one of 30 predefined colors as an array (to support alpha)
	public final int[] getClr(int colorVal, int alpha){
		switch (colorVal){
			case gui_Black			         : { return new int[] {0,0,0,alpha};}
			case gui_Gray   		         : { return new int[] {120,120,120,alpha}; }
			case gui_White  		         : { return new int[] {255,255,255,alpha}; }
			case gui_Yellow 		         : { return new int[] {255,255,0,alpha}; }
			case gui_Cyan   		         : { return new int[] {0,255,255,alpha};} 
			case gui_Magenta		         : { return new int[] {255,0,255,alpha};}  
			case gui_Red    		         : { return new int[] {255,0,0,alpha};} 
			case gui_Blue			         : { return new int[] {0,0,255,alpha};}
			case gui_Green			         : { return new int[] {0,255,0,alpha};}  
			case gui_DarkGray   	         : { return new int[] {80,80,80,alpha};}
			case gui_DarkRed    	         : { return new int[] {120,0,0,alpha};}
			case gui_DarkBlue  	 	         : { return new int[] {0,0,120,alpha};}
			case gui_DarkGreen  	         : { return new int[] {0,120,0,alpha};}
			case gui_DarkYellow 	         : { return new int[] {120,120,0,alpha};}
			case gui_DarkMagenta	         : { return new int[] {120,0,120,alpha};}
			case gui_DarkCyan   	         : { return new int[] {0,120,120,alpha};}	   
			case gui_LightGray   	         : { return new int[] {200,200,200,alpha};}
			case gui_LightRed    	         : { return new int[] {255,110,110,alpha};}
			case gui_LightBlue   	         : { return new int[] {110,110,255,alpha};}
			case gui_LightGreen  	         : { return new int[] {110,255,110,alpha};}
			case gui_LightYellow 	         : { return new int[] {255,255,110,alpha};}
			case gui_LightMagenta	         : { return new int[] {255,110,255,alpha};}
			case gui_LightCyan   	         : { return new int[] {110,255,255,alpha};}
			case gui_FaintGray 		         : { return new int[] {110,110,110,alpha};}
			case gui_FaintRed 	 	         : { return new int[] {110,0,0,alpha};}
			case gui_FaintBlue 	 	         : { return new int[] {0,0,110,alpha};}
			case gui_FaintGreen 	         : { return new int[] {0,110,0,alpha};}
			case gui_FaintYellow 	         : { return new int[] {110,110,0,alpha};}
			case gui_FaintCyan  	         : { return new int[] {0,110,110,alpha};}
			case gui_FaintMagenta  	         : { return new int[] {110,0,110,alpha};}    	
			case gui_TransBlack  	         : { return new int[] {0,0,0,alpha/2};}  	
			case gui_TransGray  	         : { return new int[] {110,110,110,alpha/2};}
			case gui_TransLtGray  	         : { return new int[] {180,180,180,alpha/2};}
			case gui_TransRed  	         	 : { return new int[] {110,0,0,alpha/2};}
			case gui_TransBlue  	         : { return new int[] {0,0,110,alpha/2};}
			case gui_TransGreen  	         : { return new int[] {0,110,0,alpha/2};}
			case gui_TransYellow  	         : { return new int[] {110,110,0,alpha/2};}
			case gui_TransCyan  	         : { return new int[] {0,110,110,alpha/2};}
			case gui_TransMagenta  	         : { return new int[] {110,0,110,alpha/2};}	
			case gui_TransWhite  	         : { return new int[] {220,220,220,alpha/2};}	
			case gui_OffWhite				 : { return new int[] {255,255,235,alpha};}
			default         		         : { return getClr_Custom(colorVal,alpha);}    
		}//switch
	}//getClr
	/**
	 * any instancing-class-specific colors - colorVal set to be higher than IRenderInterface.gui_OffWhite
	 * @param colorVal
	 * @param alpha
	 * @return
	 */
	protected abstract int[] getClr_Custom(int colorVal, int alpha);
	
	
	public final int getRndClrInt(){return (int)random(0,23);}		//return a random color flag value from below
	public final int[] getRndClr(int alpha){return new int[]{(int)random(0,255),(int)random(0,255),(int)random(0,255),alpha};	}
	public final int[] getRndClr2(){return new int[]{(int)random(50,255),(int)random(25,200),(int)random(80,255),255};	}
	public final int[] getRndClr2(int alpha){return new int[]{(int)random(50,255),(int)random(25,200),(int)random(80,255),alpha};	}
	public final int[] getRndClr(){return getRndClr(255);	}		
	public final Integer[] getClrMorph(int a, int b, double t){return getClrMorph(getClr(a,255), getClr(b,255), t);}    
	public final Integer[] getClrMorph(int[] a, int[] b, double t){
		if(t==0){return new Integer[]{a[0],a[1],a[2],a[3]};} else if(t==1){return new Integer[]{b[0],b[1],b[2],b[3]};}
		return new Integer[]{(int)(((1.0f-t)*a[0])+t*b[0]),(int)(((1.0f-t)*a[1])+t*b[1]),(int)(((1.0f-t)*a[2])+t*b[2]),(int)(((1.0f-t)*a[3])+t*b[3])};
	}


	

}//my_procApplet
