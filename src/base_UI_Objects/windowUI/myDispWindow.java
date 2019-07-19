package base_UI_Objects.windowUI;

import java.io.File;
import java.util.*;

import base_UI_Objects.IRenderInterface;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.drawnObjs.myDrawnSmplTraj;
import base_Utils_Objects.*;
import base_Utils_Objects.io.MessageObject;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.*;

/**
 * abstract class to hold base code for a menu/display window (2D for gui, etc), 
 * to handle displaying and controlling the window, and calling the implementing 
 * class for the specifics
 * @author john
 *
 */
public abstract class myDispWindow {
	public static my_procApplet pa;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;
	public static int winCnt = 0;
	public int ID;	
	public String name, winText;		
	public int[] fillClr, strkClr, rtSideUIFillClr, rtSideUIStrkClr;
	public float[] rectDim, closeBox, rectDimClosed, mseClickCrnr;	
	//current visible screen width and height
	public float[] curVisScrDims;

	public static final float xOff = 20 , yOff = 18.0f * (my_procApplet.txtSz/12.0f), btnLblYOff = 2 * yOff, rowStYOff = yOff*.15f;
	private static final float maxBtnWidthMult = .95f;
	public static final int topOffY = 40;			//offset values to render boolean menu on side of screen - offset at top before drawing
	public static final float clkBxDim = 10;//size of interaction/close window box in pxls
	
	public int pFlagIdx;					//the flags idx in the PApplet that controls this window - use -1 for none	
	private int[] dispFlags;	
	public static final int 
				showIDX 			= 0,			//whether or not to show this window
				is3DWin 			= 1,
				canChgView			= 2,			//view can change
				isRunnable 			= 3,			//runs a simulation
				closeable 			= 4,			//window is able to be closed
				hasScrollBars 		= 5,			//this window has scroll bars (both vert and horizontal)
				procMouseMove 		= 6,
				mouseSnapMove		= 7,			//mouse locations for this window are discrete multiples - if so implement inherited function to calculate mouse snap location
				uiObjMod			= 8,			//a ui object in this window has been modified
				useRndBtnClrs		= 9,	
				useCustCam			= 10,			//whether or not to use a custom camera for this window
				drawMseEdge			= 11,			//whether or not to draw the mouse location/edge from eye/projection onto box
				drawRightSideMenu	= 12,			//whether this window has a right-side info menu overlay
				showRightSideMenu	= 13,			//whether this window is currently showing right side info menu, or if it is minimized
				clearPrivBtns		= 14;			//momentary priv buttons have been set, need to be cleared next frame
						
	public static final int numDispFlags = 15;
	
	//private window-specific flags and UI components (buttons)
	public int[] privFlags;
	public String[] truePrivFlagNames; //needs to be in order of flags	
	public String[] falsePrivFlagNames;//needs to be in order of flags
	
		//for boolean buttons based on child-class window specific values
	public int[][] privFlagColors;
	public int[] privModFlgIdxs;										//only modifiable idx's will be shown as buttons - this needs to be in order of flag names
	public float[][] privFlagBtns;									//clickable dimensions for these buttons
	public int numClickBools;
	//array of priv buttons to be cleared next frame - should always be empty except when buttons need to be cleared
	protected ArrayList<Integer> privBtnsToClear;
	
//	//edit circle quantities for visual cues when grab and smoothen trajectories
//	public static final int[] editCrcFillClrs = new int[] {my_procApplet.gui_FaintMagenta, my_procApplet.gui_FaintGreen};			
//	public static final float[] editCrcRads = new float[] {20.0f,40.0f};			
//	public static final float[] editCrcMods = new float[] {1f,2f};			
//	public final myPoint[] editCrcCtrs = new myPoint[] {new myPoint(0,0,0),new myPoint(0,0,0)};			
//	public float[] editCrcCurRads = new float[] {0,0};	
	
	//UI objects in this window
	//GUI Objects
	public myGUIObj[] guiObjs;	
	public int msClkObj, msOvrObj;												//myGUIObj object that was clicked on  - for modification, object mouse moved over
	public int msBtnClcked;														//mouse button clicked
	public float[] uiClkCoords;												//subregion of window where UI objects may be found
	public static final double uiWidthMult = 9;							//multipler of size of label for width of UI components, when aligning components horizontally
	
	public double[][] guiMinMaxModVals;					//min max mod values
	public double[] guiStVals;							//starting values
	public String[] guiObjNames;							//display labels for UI components	
	//idx 0 is treat as int, idx 1 is obj has list vals, idx 2 is object gets sent to windows
	public boolean[][] guiBoolVals;						//array of UI flags for UI objects
	
	//offset to bottom of custom window menu 
	protected float custMenuOffset;
	
	//box holding x,y,w,h values of black rectangle to form around menu for display variables on right side of screen, if present
	private float[] UIRtSideRectBox;
	//closed window box
	private float[] closedUIRtSideRecBox;
	
	///////////////////////////////////////
	// traj stuff
	
	/**
	 * object to manage any drawn trajectories
	 */
	protected myTrajManager trajMgr;

	///////////
	//display and camera related variables - managed per window
	//public static final float TWO_PI =(float) (Math.PI*2.0f), HALF_PI =(float) (Math.PI/2.0f);
	protected float dz=0, rx= (-0.06f*MyMathUtils.twoPi_f), ry=-0.04f*MyMathUtils.twoPi_f;		// distance to camera. Manipulated with wheel or when,view angles manipulated when space pressed but not mouse	
	public final float camInitialDist = -200,		//initial distance camera is from scene - needs to be negative
			camInitRy = ry,
			camInitRx = rx;

	protected myVector focusTar;							//target of focus - used in translate to set where the camera is looking - allow for modification
	protected myVector sceneFcsVal;							//set this value  to be default target of focus	- don't programmatically change, keep to use as reset
	protected myPoint sceneCtrVal;							//set this value to be different display center translations -to be used to calculate mouse offset in world for pick
	
	//to control how much is shown in the window - if stuff extends off the screen and for 2d window
	public myScrollBars[] scbrs;
	
	private final int[] trueBtnClr = new int[]{220,255,220,255}, falseBtnClr = new int[]{255,215,215,255};
	
	//path to save screenshots for this dispwindow
	protected final String ssPathBase;
	
	//these ints hold the index of which custom functions or debug functions should be launched.  
	//these are set when the sidebar menu is clicked and these processes are requested, and they are set to -1 when these processes are launched.  this is so the buttons can be turned on before the process starts
	//this is sub-optimal solution - needs an index per sidebar button on each row; using more than necessary, otherwise will crash if btn idx >= curCustBtn.length
	protected int[] curCustBtn = new int[] {-1,-1,-1,-1,-1,-1,-1,-1};
	protected int curCustBtnType = -1;//type/row of current button selected
	//this is set to true when curCustXXX vals are set to != -1; this is used as a 1-frame buffer to allow the UI to turn on the source buttons of these functions
	private boolean custClickSetThisFrame = false, custFuncDoLaunch = false;
	
	public myDispWindow(my_procApplet _p, String _n, int _flagIdx, int[] fc,  int[] sc, float[] rd, float[] rdClosed, String _winTxt, boolean _canDrawTraj) {
		pa=_p;
		msgObj = MessageObject.buildMe(pa);
		ID = winCnt++;
		name = _n;
		pFlagIdx = _flagIdx;
		//base screenshot path
		String tmpNow = pa.now.toInstant().toString();
		tmpNow = tmpNow.replace(':','_');
		ssPathBase = pa.sketchPath() +File.separatorChar +name+"_"+tmpNow + File.separatorChar;
		initClrDims( fc, sc, rd, rdClosed);
		winText = _winTxt;
		msClkObj = -1;
		msOvrObj = -1;
	}//ctor
	
	public void initThisWin(boolean _canDrawTraj, boolean _trajIsFlat, boolean _isMenu){
		if(_canDrawTraj) {trajMgr = new myTrajManager(pa, this, _canDrawTraj,_trajIsFlat);} else {trajMgr = null;}
		initFlags();
		if(!_isMenu){
			initUIBox();				//set up ui click region to be in sidebar menu below menu's entries - do not do here for sidebar menu itself
		}
		
		setupGUIObjsAras();				//setup all ui objects and record final y value in sidebar menu for UI Objects in this window
		
		privBtnsToClear = new ArrayList<Integer>();
		initAllPrivBtns();
		initMe();
		
		setClosedBox();
		mseClickCrnr = new float[2];		//this is offset for click to check buttons in x and y - since buttons for all menus will be in menubar, this should be the upper left corner of menubar - upper left corner of rect 
		mseClickCrnr[0] = 0;
		mseClickCrnr[1] = 0;		
		if((!_isMenu) && (getFlags(hasScrollBars))){scbrs = new myScrollBars[4];	for(int i =0; i<scbrs.length;++i){scbrs[i] = new myScrollBars(pa, this);}}
	}//initThisWin	
	
	//final initialization stuff, after window made, but necessary to make sure window displays correctly
	public void finalInit(boolean thisIs3D, boolean viewCanChange, myPoint _ctr, myVector _baseFcs) {
		setFlags(is3DWin, thisIs3D);
		setFlags(canChgView, viewCanChange);
		sceneFcsVal = new myVector(_baseFcs);
		sceneCtrVal = new myPoint(_ctr);
		focusTar = new myVector(_baseFcs);		
		if(null!=trajMgr) {trajMgr.finalTrajValsInit(_ctr, _baseFcs);}
	}
	
	//set up initial trajectories - 2d array, 1 per UI Page, 1 per modifiable construct within page.
	public void initDrwnTrajs(){
		if(null!=trajMgr) {		trajMgr.initDrwnTrajs();	initDrwnTrajIndiv();				}
	}
	
	//init fill and stroke colors and dims of rectangular area open and closed - only called from ctor
	private void initClrDims(int[] fc,  int[] sc, float[] rd, float[] rdClosed) {
		fillClr = new int[4];rtSideUIFillClr= new int[4]; rtSideUIStrkClr= new int[4]; strkClr = new int[4];	 
		rectDim = new float[4];	rectDimClosed = new float[4]; closeBox = new float[4]; uiClkCoords = new float[4];		
		for(int i =0;i<4;++i){
			fillClr[i] = fc[i];strkClr[i]=sc[i];
			rtSideUIFillClr[i] = fc[i];rtSideUIStrkClr[i]=sc[i];			
			rectDim[i]=rd[i];rectDimClosed[i]=rdClosed[i];
		}			
		
		float boxWidth = 1.1f*rectDim[0];
		UIRtSideRectBox = new float[] {rectDim[2]-boxWidth,0,boxWidth, rectDim[3]};		
		closedUIRtSideRecBox = new float[] {rectDim[2]-20,0,20,rectDim[3]};

		curVisScrDims = new float[] {closedUIRtSideRecBox[0],rectDim[3]};
	}//initClrDims	
	
	protected void setVisScreenWidth(float visScrWidth) {setVisScreenDims(visScrWidth,curVisScrDims[1]);}
	protected void setVisScreenHeight(float visScrHeight) {setVisScreenDims(curVisScrDims[0],visScrHeight);}
	//based on current visible screen width, set map and calc analysis display locations
	protected void setVisScreenDims(float visScrWidth, float visScrHeight) {
		curVisScrDims[0] = visScrWidth;
		curVisScrDims[1] = visScrHeight;
		setVisScreenDimsPriv();
	}//calcAndSetMapLoc
	
	//set right side data display fill/stroke colors
	public void setRtSideUIBoxClrs(int[] fc,  int[] sc) {
		for(int i =0;i<4;++i){rtSideUIFillClr[i] = fc[i];rtSideUIStrkClr[i]=sc[i];}				
	}		

	//build UI clickable region
	protected void initUIClickCoords(float x1, float y1, float x2, float y2){uiClkCoords[0] = x1;uiClkCoords[1] = y1;uiClkCoords[2] = x2; uiClkCoords[3] = y2;}
	protected void initUIClickCoords(float[] cpy){	uiClkCoords[0] = cpy[0];uiClkCoords[1] = cpy[1];uiClkCoords[2] = cpy[2]; uiClkCoords[3] = cpy[3];}
	//public void initFlags(){dispFlags = new boolean[numDispFlags];for(int i =0; i<numDispFlags;++i){dispFlags[i]=false;}}		
	//base class flags init
	public void initFlags(){dispFlags = new int[1 + numDispFlags/32];for(int i =0; i<numDispFlags;++i){setFlags(i,false);}}		
	//child-class flag init
	protected void initPrivFlags(int numPrivFlags){privFlags = new int[1 + numPrivFlags/32]; for(int i = 0; i<numPrivFlags; ++i){setPrivFlags(i,false);}}
	//set up initial colors for sim specific flags for display
	protected void initPrivFlagColors(){
		privFlagColors = new int[truePrivFlagNames.length][4];
		for (int i = 0; i < privFlagColors.length; ++i) { privFlagColors[i] = new int[]{(int) pa.random(150),(int) pa.random(100),(int) pa.random(150), 255}; }			
	}
	
	/**
	 * set up child class button rectangles
	 */
	protected void initUIBox(){		
		float [] menuUIClkCoords = pa.getUIRectVals(ID); 
		initUIClickCoords(menuUIClkCoords[0],menuUIClkCoords[3],menuUIClkCoords[2],menuUIClkCoords[3]);			
	}
	
	/**
	 * calculate button length
	 */
	private static final float ltrLen = 5.0f;private static final int btnStep = 5;
	private float calcBtnLength(String tStr, String fStr){return btnStep * (int)(((PApplet.max(tStr.length(),fStr.length())+4) * ltrLen)/btnStep);}
	
	private void setBtnDims(int idx, float oldBtnLen, float btnLen) {privFlagBtns[idx]= new float[] {(float)(uiClkCoords[0])+oldBtnLen, (float) uiClkCoords[3], btnLen, yOff };}
	
	/**
	 * set up child class boolean button rectangles using initialized truePrivFlagNames and falsePrivFlagNames
	 * @param yDisp displacement for button to be drawn
	 * @param numBtns number of buttons to make
	 */
	protected void initPrivBtnRects(float yDisp, int numBtns){
		//msgObj.dispInfoMessage("myDispWindow","initPrivBtnRects","initPrivBtnRects in :"+ name + "st value for uiClkCoords[3]");
		float maxBtnLen = maxBtnWidthMult * pa.getMenuWidth(), halfBtnLen = .5f*maxBtnLen;
		//pa.pr("maxBtnLen : " + maxBtnLen);
		privFlagBtns = new float[numBtns][];
		this.uiClkCoords[3] += yOff;
		float oldBtnLen = 0;
		boolean lastBtnHalfStLine = false, startNewLine = true;
		for(int i=0; i<numBtns; ++i){						//clickable button regions - as rect,so x,y,w,h - need to be in terms of sidebar menu 
			float btnLen = calcBtnLength(truePrivFlagNames[i].trim(),falsePrivFlagNames[i].trim());
			//either button of half length or full length.  if half length, might be changed to full length in next iteration.
			//pa.pr("initPrivBtnRects: i "+i+" len : " +btnLen+" cap 1: " + truePrivFlagNames[i].trim()+"|"+falsePrivFlagNames[i].trim());
			if(btnLen > halfBtnLen){//this button is bigger than halfsize - it needs to be made full size, and if last button was half size and start of line, make it full size as well
				btnLen = maxBtnLen;
				if(lastBtnHalfStLine){//make last button full size, and make button this button on another line
					privFlagBtns[i-1][2] = maxBtnLen;
					this.uiClkCoords[3] += yOff;
				}
				setBtnDims(i, 0, btnLen);
				//privFlagBtns[i]= new float[] {(float)(uiClkCoords[0]-xOff), (float) uiClkCoords[3], btnLen, yOff };				
				this.uiClkCoords[3] += yOff;
				startNewLine = true;
				lastBtnHalfStLine = false;
			} else {//button len should be half width unless this button started a new line
				btnLen = halfBtnLen;
				if(startNewLine){//button is starting new line
					lastBtnHalfStLine = true;
					setBtnDims(i, 0, btnLen);
					//privFlagBtns[i]= new float[] {(float)(uiClkCoords[0]-xOff), (float) uiClkCoords[3], btnLen, yOff };
					startNewLine = false;
				} else {//should only get here if 2nd of two <1/2 width buttons in a row
					lastBtnHalfStLine = false;
					setBtnDims(i, oldBtnLen, btnLen);
					//privFlagBtns[i]= new float[] {(float)(uiClkCoords[0]-xOff)+oldBtnLen, (float) uiClkCoords[3], btnLen, yOff };
					this.uiClkCoords[3] += yOff;
					startNewLine = true;					
				}
			}			
			oldBtnLen = btnLen;
		}
		if(lastBtnHalfStLine){//set last button full length if starting new line
			privFlagBtns[numBtns-1][2] = maxBtnLen;			
		}
		this.uiClkCoords[3] += yOff;
		initPrivFlagColors();
	}//initPrivBtnRects
	/**
	 * find index in flag name arrays of passed boolean IDX
	 * @param idx
	 * @return
	 */
	protected int getFlagAraIdxOfBool(int idx) {
		for(int i=0;i<privModFlgIdxs.length;++i) {if(idx == privModFlgIdxs[i]) {return i;}	}		
		return -1;//not found
	}	
	/**
	 * set baseclass flags  //setFlags(showIDX, 
	 * @param idx
	 * @param val
	 */
	public void setFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		dispFlags[flIDX] = (val ?  dispFlags[flIDX] | mask : dispFlags[flIDX] & ~mask);
		switch(idx){
			case showIDX 			: {	
				setClosedBox();
				if(!val){		closeMe();}	//not showing window :specific instancing window implementation stuff to do when hidden/transitioning to another window (i.e. suspend stuff running outside draw loop, or release memory of unnecessary stuff)
				else {			showMe();}	//specific instance window functionality to do when window is shown
				break;}	
			case is3DWin 			: {	break;}	
			case closeable 			: {	break;}	
			case hasScrollBars 		: {	break;}	
			case procMouseMove 		: {	break;}	
			case mouseSnapMove		: {	break;}	
			case uiObjMod			: {	break;}			
			case useRndBtnClrs		: { break;}
			case useCustCam			: { break;}
			case drawMseEdge		: { break;}
			case clearPrivBtns		: { break;}
			case drawRightSideMenu  : { break;}	//can drawn right side menu
			case showRightSideMenu  : { 		//modify the dimensions of the visible window based on whether the side bar menu is shown
				if(getFlags(drawRightSideMenu)) {
					float visWidth = (val ?  UIRtSideRectBox[0] : closedUIRtSideRecBox[0]);		//to match whether the side bar menu is open or closed
					setVisScreenWidth(visWidth);
				}
				break;}		
		}				
	}//setFlags

	/**
	 * set the right side menu state for this window - if it is actually present, show it
	 * @param visible
	 */
	public void setRtSideInfoWinSt(boolean visible) {if(getFlags(drawRightSideMenu)) {setFlags(showRightSideMenu,visible);}}		
	/**
	 * get baseclass flag
	 * @param idx
	 * @return
	 */
	public boolean getFlags(int idx){int bitLoc = 1<<(idx%32);return (dispFlags[idx/32] & bitLoc) == bitLoc;}	
	/**
	 * check list of flags
	 * @param idxs
	 * @return
	 */
	public boolean getAllFlags(int [] idxs){int bitLoc; for(int idx =0;idx<idxs.length;++idx){bitLoc = 1<<(idx%32);if ((dispFlags[idx/32] & bitLoc) != bitLoc){return false;}} return true;}
	public boolean getAnyFlags(int [] idxs){int bitLoc; for(int idx =0;idx<idxs.length;++idx){bitLoc = 1<<(idx%32);if ((dispFlags[idx/32] & bitLoc) == bitLoc){return true;}} return false;}
	
	/**
	 * set/get child class flags
	 * @param idx
	 * @param val
	 */
	public abstract void setPrivFlags(int idx, boolean val);
	public boolean getPrivFlags(int idx){int bitLoc = 1<<(idx%32);return (privFlags[idx/32] & bitLoc) == bitLoc;}	
	public boolean getAllPrivFlags(int [] idxs){int bitLoc; for(int idx =0;idx<idxs.length;++idx){bitLoc = 1<<(idx%32);if ((privFlags[idx/32] & bitLoc) != bitLoc){return false;}} return true;}
	public boolean getAnyPrivFlags(int [] idxs){int bitLoc; for(int idx =0;idx<idxs.length;++idx){bitLoc = 1<<(idx%32);if ((privFlags[idx/32] & bitLoc) == bitLoc){return true;}} return false;}
	//set a list of indexes in private flags array to be a specific value
	public void setAllPrivFlags(int[] idxs, boolean val) { for(int idx =0;idx<idxs.length;++idx) {setPrivFlags(idxs[idx],val);}}

	/**
	 * this will set the height of the rectangle enclosing this window - this will be called when a 
	 * window pushes up or pulls down this window - this resizes any drawn trajectories in this 
	 * window, and calls the instance class's code for resizing
	 * @param height
	 */
	public void setRectDimsY(float height){
		float oldVal = getFlags(showIDX) ? rectDim[3] : rectDimClosed[3];
		rectDim[3] = height;
		rectDimClosed[3] = height;
		float scale  = height/oldVal;			//scale of modification - rescale the size and location of all components of this window by this
		if(null!=trajMgr) {		trajMgr.setTrajRectDimsY(height, scale);}
		if(getFlags(hasScrollBars)){for(int i =0; i<scbrs.length;++i){scbrs[i].setSize();}}
		resizeMe(scale);
	}
	
	/**
	 * build myGUIObj objects for interaction - call from setupMenuClkRegions of window, uiClkCoords 
	 * needs to be derived before this is called by child class - maxY val(for vertical stack) or 
	 * maxX val(for horizontal stack) will be derived here
	 * @param guiObjNames
	 * @param guiStVals
	 * @param guiMinMaxModVals
	 * @param guiBoolVals
	 * @param off
	 * @param listVals : keyed by object IDX i guiObjs, value is String array of list values
	 */
	protected void buildGUIObjs(String[] guiObjNames, double[] guiStVals, double[][] guiMinMaxModVals, boolean[][] guiBoolVals, double[] off, TreeMap<Integer, String[]> listVals){
		//myGUIObj tmp; 
//			if(getFlags(uiObjsAreVert]){		//vertical stack of UI components - clickable region x is unchanged, y changes with # of objects
		float stClkY = uiClkCoords[1];
		int numListObjs = 0;
		for(int i =0; i< guiObjs.length; ++i){
			//guiObjs[i] = buildGUIObj(i,guiObjNames[i],guiStVals[i], guiMinMaxModVals[i], guiBoolVals[i], new double[]{uiClkCoords[0], stClkY, uiClkCoords[2], stClkY+yOff},off);
			if(guiBoolVals[i][1]) {++numListObjs;}
			guiObjs[i] = new myGUIObj(pa, this,i, guiObjNames[i], uiClkCoords[0], stClkY, uiClkCoords[2], stClkY+yOff, guiMinMaxModVals[i], guiStVals[i], guiBoolVals[i], off);
			stClkY += yOff;
		}
		uiClkCoords[3] = stClkY;
		if(numListObjs != listVals.size()) {
			msgObj.dispWarningMessage("myDispWindow", "buildGUIObjs", "Error!!!! # of specified list select UI objects ("+numListObjs+") does not match # of passed lists ("+listVals.size()+") - some or all of specified list objects will not display properly.");
		}
		//build lists of data for all list UI objects
		for(Integer listIDX : listVals.keySet()) {	guiObjs[listIDX].setListVals(listVals.get(listIDX));}		
	}//
	
	//this returns a formatted string holding the UI data
	protected String getStrFromUIObj(int idx){
		StringBuilder sb = new StringBuilder(400);
		sb.append("ui_idx: ");
		sb.append(idx);
		sb.append(" |name: ");
		sb.append(guiObjs[idx].name);
		sb.append(" |value: ");
		sb.append(guiObjs[idx].getVal());
		sb.append(" |flags: ");
		for(int i =0;i<guiObjs[idx].numFlags; ++i){
			sb.append(" ");
			sb.append((guiObjs[idx].getFlags(i) ? "true" : "false"));
		}
		return sb.toString().trim();		
		
	}//getStrFromUIObj
		
	//this sets the value of a gui object from the data held in a string
	protected void setValFromFileStr(String str){
		String[] toks = str.trim().split("\\|");
		//window has no data values to load
		if(toks.length==0){return;}
		int uiIdx = Integer.parseInt(toks[0].split("\\s")[1].trim());
		//String name = toks[3];
		double uiVal = Double.parseDouble(toks[2].split("\\s")[1].trim());	
		guiObjs[uiIdx].setVal(uiVal);
		for(int i =0;i<guiObjs[uiIdx].numFlags; ++i){
			guiObjs[uiIdx].setFlags(i, Boolean.parseBoolean(toks[3].split("\\s")[i].trim()));
		}	
		setUIWinVals(uiIdx);//update window's values with UI construct's values
	}//setValFromFileStr

	public void loadFromFile(File file){
		if (file == null) {
			msgObj.dispWarningMessage("myDispWindow","loadFromFile","Load was cancelled.");
		    return;
		} 
		String[] res = pa.loadStrings(file.getAbsolutePath());
		int[] stIdx = {0};//start index for a particular window - make an array so it can be passed by ref and changed by windows
		hndlFileLoad(file, res,stIdx);
	}//loadFromFile
	
	public String[] getSaveFileDirName() {
		String[] vals = getSaveFileDirNamesPriv();
		String[] res = new String[] {
			ssPathBase + vals[0] + File.separatorChar, vals[1]	
		};
		return res;
	}
	
	
	public void saveToFile(File file){
		if (file == null) {
			msgObj.dispWarningMessage("myDispWindow","loadFromFile","Save was cancelled.");
		    return;
		} 
		ArrayList<String> res = new ArrayList<String>();

		res.addAll(hndlFileSave(file));	

		pa.saveStrings(file.getAbsolutePath(), res.toArray(new String[0]));  
	}//saveToFile	
	
	//manage loading pre-saved UI component values, if useful for this window's load/save (if so call from child window's implementation
	protected void hndlFileLoad_GUI(String[] vals, int[] stIdx) {
		++stIdx[0];
		//set values for ui sliders
		while(!vals[stIdx[0]].contains(name + "_custUIComps")){
			if(vals[stIdx[0]].trim() != ""){	setValFromFileStr(vals[stIdx[0]]);	}
			++stIdx[0];
		}
		++stIdx[0];		
		
	}//hndlFileLoad_GUI
	//manage saving this window's UI component values.  if needed call from child window's implementation
	protected ArrayList<String> hndlFileSave_GUI(){
		ArrayList<String> res = new ArrayList<String>();
		res.add(name);
		for(int i=0;i<guiObjs.length;++i){	res.add(getStrFromUIObj(i));}		
		//bound for custom components
		res.add(name + "_custUIComps");
		//add blank space
		res.add("");
		return res;
	}//
	//////////////////////
	//camera stuff
	
	//resets camera view and focus target
	public void setInitCamView(){
		rx = camInitRx;
		ry = camInitRy;
		dz = camInitialDist;	
		resetViewFocus();
	}//setCamView()	

	public void setCamera(float[] camVals){
		if(getFlags(useCustCam)){setCameraIndiv (camVals);}//individual window camera handling
		else {
			pa.camera(camVals[0],camVals[1],camVals[2],camVals[3],camVals[4],camVals[5],camVals[6],camVals[7],camVals[8]);      
			//if(this.flags[this.debugMode]){outStr2Scr("rx :  " + rx + " ry : " + ry + " dz : " + dz);}
			// puts origin of all drawn objects at screen center and moves forward/away by dz
			pa.translate(camVals[0],camVals[1],(float)dz); 
		    setCamOrient();	
		}
	}//setCamera

	//used to handle camera location/motion
	public void setCamOrient(){pa.rotateX(rx);pa.rotateY(ry); pa.rotateX(MyMathUtils.halfPi_f);		}//sets the rx, ry, pi/2 orientation of the camera eye	
	//used to draw text on screen without changing mode - reverses camera orientation setting
	public void unSetCamOrient(){pa.rotateX(-MyMathUtils.halfPi_f); pa.rotateY(-ry);   pa.rotateX(-rx); }//reverses the rx,ry,pi/2 orientation of the camera eye - paints on screen and is unaffected by camera movement
	//return display string for camera location
	public String getCamDisp() {return " camera rx :  " + rx + " ry : " + ry + " dz : " + dz ; }
	
	//initial draw stuff for each frame draw
	public void drawSetupWin(float[] camVals) {
		setCamera(camVals);
		//move to focus target
		pa.translate(focusTar.x,focusTar.y,focusTar.z);
	}
	
	//recenter view on original focus target
	public void resetViewFocus() {focusTar.set(sceneFcsVal);}
	//////////////////////
	//end camera stuff
	
	public float calcOffsetScale(double val, float sc, float off){float res =(float)val - off; res *=sc; return res+=off;}
	public double calcDBLOffsetScale(double val, float sc, double off){double res = val - off; res *=sc; return res+=off;}
	//returns passed current passed dimension from either rectDim or rectDimClosed
	public float getRectDim(int idx){return ( getFlags(showIDX) ? rectDim[idx] : rectDimClosed[idx]);	}

	public void setClosedBox(){
		if( getFlags(showIDX)){	closeBox[0] = rectDim[0]+rectDim[2]-clkBxDim;closeBox[1] = rectDim[1];	closeBox[2] = clkBxDim;	closeBox[3] = clkBxDim;} 
		else {					closeBox[0] = rectDimClosed[0]+rectDimClosed[2]-clkBxDim;closeBox[1] = rectDimClosed[1];	closeBox[2] = clkBxDim;	closeBox[3] = clkBxDim;}
	}	
	
	//draw a series of strings in a column
	protected void dispMenuTxtLat(String txt, int[] clrAra, boolean showSphere){
		pa.setFill(clrAra, 255); 
		pa.translate(xOff*.5f,yOff*.5f);
		if(showSphere){pa.setStroke(clrAra, 255);		pa.sphere(5);	} 
		else {	pa.noStroke();		}
		pa.translate(-xOff*.5f,yOff*.5f);
		pa.text(""+txt,xOff,-yOff*.25f);	
	}
	protected void dispBoolStFlag(String txt, int[] clrAra, boolean state, float stMult){
		if(state){
			pa.setFill(clrAra, 255); 
			pa.setStroke(clrAra, 255);
		} else {
			pa.setColorValFill(pa.gui_DarkGray,255); 
			pa.noStroke();	
		}
		pa.sphere(5);
		//pa.text(""+txt,-xOff,yOff*.8f);	
		pa.text(""+txt,stMult*txt.length(),yOff*.8f);	
	}
	
	//draw a series of strings in a row
	protected void dispBttnAtLoc(String txt, float[] loc, int[] clrAra){
		pa.setFill(clrAra, clrAra[3]);
		pa.setColorValStroke(my_procApplet.gui_Black,255);
		pa.drawRect(loc);		
		pa.setColorValFill(my_procApplet.gui_Black,255);
		//pa.translate(-xOff*.5f,-yOff*.5f);
		pa.text(""+txt,loc[0] + (txt.length() * .3f),loc[1]+loc[3]*.75f);
		//pa.translate(width, 0);
	}
	
	//whether or not to draw the mouse reticle/rgb(xyz) projection/edge to eye
	public boolean chkDrawMseRet(){		return getFlags(drawMseEdge);	}

	
	//draw ui objects
	public void drawGUIObjs(){	
		pa.pushMatrix();pa.pushStyle();	
		for(int i =0; i<guiObjs.length; ++i){guiObjs[i].draw();}
		pa.popStyle();pa.popMatrix();
	}
	
	//draw all boolean-based buttons for this window
	public void drawClickableBooleans() {	
		pa.pushMatrix();pa.pushStyle();	
		pa.setColorValFill(pa.gui_Black,255);
		if(getFlags(useRndBtnClrs)){
			for(int i =0; i<privModFlgIdxs.length; ++i){//prlFlagRects dispBttnAtLoc(String txt, float[] loc, int[] clrAra)
				if(getPrivFlags(privModFlgIdxs[i]) ){								dispBttnAtLoc(truePrivFlagNames[i],privFlagBtns[i],privFlagColors[i]);			}
				else {	if(truePrivFlagNames[i].equals(falsePrivFlagNames[i])) {	dispBttnAtLoc(truePrivFlagNames[i],privFlagBtns[i],new int[]{180,180,180, 255});}	
						else {														dispBttnAtLoc(falsePrivFlagNames[i],privFlagBtns[i],new int[]{0,255-privFlagColors[i][1],255-privFlagColors[i][2], 255});}		
				}
			}		
		} else {
			for(int i =0; i<privModFlgIdxs.length; ++i){//prlFlagRects dispBttnAtLoc(String txt, float[] loc, int[] clrAra)
				if(getPrivFlags(privModFlgIdxs[i]) ){								dispBttnAtLoc(truePrivFlagNames[i],privFlagBtns[i],trueBtnClr);			}
				else {																dispBttnAtLoc(falsePrivFlagNames[i],privFlagBtns[i],falseBtnClr);	}
			}	
		}
		pa.popStyle();pa.popMatrix();
	}//drawClickableBooleans
	
	//draw any custom menu objects for sidebar menu
	public abstract void drawCustMenuObjs();
	
	public abstract void initAllPrivBtns();
	
	
	//draw box to hide window
	protected void drawMouseBox(){
		if( getFlags(showIDX)){
		    pa.setColorValFill(my_procApplet.gui_LightGreen ,255);
			pa.drawRect(closeBox);
			pa.setFill(strkClr, strkClr[3]);
			pa.text("Close" , closeBox[0]-35, closeBox[1]+10);
		} else {
		    pa.setColorValFill(my_procApplet.gui_DarkRed,255);
			pa.drawRect(closeBox);
			pa.setFill(strkClr, strkClr[3]);
			pa.text("Open", closeBox[0]-35, closeBox[1]+10);			
		}
	}
	public void drawSmall(){
		pa.pushMatrix();				pa.pushStyle();	
		//msgObj.dispInfoMessage("myDispWindow","drawSmall","Hitting hint code draw small");
		pa.hint(PConstants.DISABLE_DEPTH_TEST);
		pa.noLights();		
		pa.setStroke(strkClr, strkClr[3]);
		pa.setFill(fillClr, fillClr[3]);
		//main window drawing
		pa.drawRect(rectDimClosed);		
		pa.setFill(strkClr, strkClr[3]);
		if(winText.trim() != ""){
			pa.text(winText.split(" ")[0], rectDimClosed[0]+10, rectDimClosed[1]+25);
		}		
		//close box drawing
		if(getFlags(closeable)){drawMouseBox();}
		pa.hint(PConstants.ENABLE_DEPTH_TEST);
		pa.popStyle();pa.popMatrix();		
	}
	
	public void drawHeader(float modAmtMillis){
		if(!getFlags(showIDX)){return;}
		pa.pushMatrix();				pa.pushStyle();			
		//msgObj.dispInfoMessage("myDispWindow","drawHeader","Hitting hint code drawHeader");
		pa.hint(PConstants.DISABLE_DEPTH_TEST);
		pa.noLights();		
		pa.setStroke(strkClr, strkClr[3]);
		pa.setFill(strkClr, strkClr[3]);
		if(winText.trim() != ""){	pa.ml_text(winText,  rectDim[0]+10,  rectDim[1]+10);}
		if(null!=trajMgr){	trajMgr.drawNotifications();	}				//if this window accepts a drawn trajectory, then allow it to be displayed
		if(getFlags(closeable)){drawMouseBox();}
		//TODO if scroll bars are ever going to actually be supported, need to separate them from drawn trajectories
		if(getFlags(hasScrollBars) && (null!=trajMgr)){scbrs[trajMgr.curDrnTrajScrIDX].drawMe();}
		//draw rightSideMenu stuff, if this window supports it
		if(getFlags(drawRightSideMenu)) {drawRtSideInfoBar(modAmtMillis);	}
		pa.lights();	
		pa.hint(PConstants.ENABLE_DEPTH_TEST);
		pa.popStyle();pa.popMatrix();	
		//last thing per draw - clear btns that have been set to clear after 1 frame of display
		if (getFlags(clearPrivBtns)) {clearAllPrivBtns();setFlags(clearPrivBtns,false);}
//		//if buttons have been set to clear, clear them next draw - put this in mouse release?
//		if (privBtnsToClear.size() > 0){setFlags(clearPrivBtns, true);	}		
	}//drawHeader
	
	//draw right side "menu" used to display simualtion/calculation variables and results
	private void drawRtSideInfoBar(float modAmtMillis) {
		pa.pushMatrix();pa.pushStyle();
		//move to upper right corner of sidebar menu - cannot draw over leftside menu, use drawCustMenuObjs() instead to put UI objects there
		//this side window is for information display
		pa.translate(rectDim[0],0,0);			
		//draw onscreen stuff for main window
		drawOnScreenStuffPriv(modAmtMillis);
		//draw right side info display if relelvant
		if(getFlags(drawRightSideMenu)) {
			pa.setFill(rtSideUIFillClr, rtSideUIFillClr[3]);//transparent black
			if(getFlags(showRightSideMenu)) {				
				pa.drawRect(UIRtSideRectBox);
				//move to manage internal text display in owning window
				pa.translate(UIRtSideRectBox[0]+5,UIRtSideRectBox[1]+yOff-4,0);
				pa.setFill(new int[] {255,255,255},255);	
				 //instancing class implements this function
				drawRightSideInfoBarPriv(modAmtMillis); 
			} else {
				//shows narrow rectangular reminder that window is there								 
				pa.drawRect(closedUIRtSideRecBox);
			}
		}
		pa.popStyle();pa.popMatrix();			
	}//drawRtSideInfoBar
	
	public void draw3D(float modAmtMillis){
		if(!getFlags(showIDX)){return;}
		//stAnimTime = pa.millis();
		float animTimeMod = (modAmtMillis/1000.0f);//in seconds
		//lastAnimTime = pa.millis();
		pa.pushMatrix();				pa.pushStyle();			
		pa.setFill(fillClr, fillClr[3]);
		pa.setStroke(strkClr,strkClr[3]);
		//draw traj stuff if exists and appropriate
		if(null!=trajMgr){		trajMgr.drawTraj_3d(animTimeMod, myPoint._add(sceneCtrVal,focusTar));}				//if this window accepts a drawn trajectory, then allow it to be displayed
		//draw instancing win-specific stuff
		drawMe(animTimeMod);			//call instance class's draw
		pa.popStyle();pa.popMatrix();		
	}//draw3D
	
	public void drawTraj3D(float animTimeMod,myPoint trans){
		msgObj.dispWarningMessage("myDispWindow","drawTraj3D","I should be overridden in 3d instancing class");
//			pa.pushMatrix();pa.pushStyle();	
//			if(null != tmpDrawnTraj){tmpDrawnTraj.drawMe(animTimeMod);}
//			TreeMap<String,ArrayList<myDrawnNoteTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
//			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
//				for(int i =0; i<tmpTreeMap.size(); ++i){
//					ArrayList<myDrawnNoteTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
//					if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){tmpAra.get(j).drawMe(animTimeMod);}}
//				}	
//			}
//			pa.popStyle();pa.popMatrix();		
	}//drawTraj3D
	
	public void draw2D(float modAmtMillis){
		if(!getFlags(showIDX)){drawSmall();return;}
		//stAnimTime = pa.millis();
		float animTimeMod = (modAmtMillis/1000.0f);
		//lastAnimTime = pa.millis();
		pa.pushMatrix();				pa.pushStyle();	
		//msgObj.dispInfoMessage("myDispWindow","draw2D","Hitting hint code draw2D");
		pa.hint(PConstants.DISABLE_DEPTH_TEST);
		pa.setStroke(strkClr,strkClr[3]);
		pa.setFill(fillClr,fillClr[3]);
		//main window drawing
		pa.drawRect(rectDim);
		//draw traj stuff if exists and appropriate
		if(null!=trajMgr){		trajMgr.drawTraj_2d(animTimeMod);}				//if this window accepts a drawn trajectory, then allow it to be displayed
		//draw instancing win-specific stuff
		drawMe(animTimeMod);			//call instance class's draw
		pa.hint(PConstants.ENABLE_DEPTH_TEST);
		pa.popStyle();pa.popMatrix();
	}
	
	public void simulate(float modAmtMillis){
		boolean simDone = simMe(modAmtMillis);
		if(simDone) {endSim();}
	}//
	
	//if ending simulation, call this function
	private void endSim() {	pa.setSimIsRunning(false);}//endSim
	
	//call after single draw - will clear window-based priv buttons that are momentary
	protected void clearAllPrivBtns() {
		if(privBtnsToClear.size() == 0) {return;}
		//only clear button if button is currently set to true, otherwise concurrent modification error
		for (Integer idx : privBtnsToClear) {this.setPrivFlags(idx, false);}
		privBtnsToClear.clear();
	}//clearPrivBtns()
	//add a button to clear after next draw
	protected void addPrivBtnToClear(int idx) {
		privBtnsToClear.add(idx);
	}
	
	//stuff to do when shown/hidden
	public void setShow(boolean val){
		setFlags(showIDX,val);
		setClosedBox();
		if(!getFlags(showIDX)){		closeMe();}//not showing window : specific instancing window implementation stuff to do when hidden
		else {						showMe();}//specific instance window functionality to do when window is shown		
	}
	
	protected void toggleWindowState(){
		//msgObj.dispInfoMessage("myDispWindow","toggleWindowState","Attempting to close window : " + this.name);
		setFlags(showIDX,!getFlags(showIDX));
		pa.setBaseFlag(pFlagIdx, getFlags(showIDX));		//value has been changed above by close box
	}
	
	protected boolean checkClsBox(int mouseX, int mouseY){
		boolean res = false;
		if(pa.ptInRange(mouseX, mouseY, closeBox[0], closeBox[1], closeBox[0]+closeBox[2], closeBox[1]+closeBox[3])){toggleWindowState(); res = true;}				
		return res;		
	}
	//check if mouse location is in UI buttons, and handle button click if so
	protected boolean checkUIButtons(int mouseX, int mouseY){
		boolean mod = false;
		int mx, my;
		//keep checking -see if clicked in UI buttons (flag-based buttons)
		for(int i = 0;i<privFlagBtns.length;++i){
			mx = (int)(mouseX - mseClickCrnr[0]); my = (int)(mouseY - mseClickCrnr[1]);
			mod = msePtInRect(mx, my, privFlagBtns[i]); 
			//msgObj.dispInfoMessage("myDispWindow","checkUIButtons","Handle mouse click in window : "+ ID + " : (" + mouseX+","+mouseY+") : "+mod + ": btn rect : "+privFlagBtns[i][0]+","+privFlagBtns[i][1]+","+privFlagBtns[i][2]+","+privFlagBtns[i][3]);
			if (mod){ 
				setPrivFlags(privModFlgIdxs[i],!getPrivFlags(privModFlgIdxs[i])); 
				return mod;
			}			
		}
		return mod;
	}//checkUIButtons	
	
	//change view based on mouse click/drag behavior and whether we are moving or zooming
	//use delX for zoom
	public void handleViewChange(boolean doZoom, float delX, float delY ) {
		if(doZoom) {	dz-=delX;	} 
		else {			rx-=delX; ry+=delY;} 		
	}//handleViewChange()
	
	//modify the viewing target by finding -TODO doesn't behave appropriately if camera is rotated around origin
	public void handleViewTargetChange(float delY, float delX) {
		//find screen up unit vector, screen right unit vector in world space, dot focus tar with that, move in that direction
		setCamOrient();
		
		if(Math.abs(delY) > 0) {
			myVectorf scrUp = pa.c.getUScrUpInWorldf();//, upVec = myVectorf._cross(scrRt, scrUp);
			myVectorf up = new myVectorf(scrUp.x* -delY,scrUp.y* -delY,scrUp.z* -delY);
			focusTar._add(up);
		}		
		if(Math.abs(delX) > 0) {
			myVectorf scrRt = pa.c.getUScrRightInWorldf();
			myVectorf rt = new myVectorf(scrRt.x* delX,scrRt.y* delX,scrRt.z* delX);
			focusTar._add(rt);
		}
		unSetCamOrient();
	}//handleViewTargetChange
	
	//protected myPoint getMsePoint(myPoint pt){return getFlags(myDispWindow.is3DWin) ? getMsePtAs3DPt((int)pt.x, (int)pt.y) : pt;}
	protected myPoint getMsePoint(myPoint pt){return getFlags(myDispWindow.is3DWin) ? getMsePtAs3DPt(pt) : pt;}		//get appropriate representation of mouse location in 3d if 3d window
	protected myPoint getMsePoint(int mouseX, int mouseY){return getFlags(myDispWindow.is3DWin) ? getMsePtAs3DPt(new myPoint(mouseX,mouseY,0)) : new myPoint(mouseX,mouseY,0);}
	public boolean handleMouseMove(int mouseX, int mouseY){
		if(!getFlags(showIDX)){return false;}
		if((getFlags(showIDX))&& (msePtInUIRect(mouseX, mouseY))){//in clickable region for UI interaction
			for(int j=0; j<guiObjs.length; ++j){if(guiObjs[j].checkIn(mouseX, mouseY)){	msOvrObj=j;return true;	}}
		}
		myPoint mouseClickIn3D = pa.c.getMseLoc(sceneCtrVal);
		if(hndlMouseMoveIndiv(mouseX, mouseY, mouseClickIn3D)){return true;}
		msOvrObj = -1;
		return false;
	}//handleMouseMove
	
	public boolean msePtInRect(int x, int y, float[] r){return ((x > r[0])&&(x <= r[0]+r[2])&&(y > r[1])&&(y <= r[1]+r[3]));}	
	public boolean msePtInUIRect(int x, int y){return ((x > uiClkCoords[0])&&(x <= uiClkCoords[2])&&(y > uiClkCoords[1])&&(y <= uiClkCoords[3]));}	

	public boolean handleMouseClick(int mouseX, int mouseY, int mseBtn){
		boolean mod = false;
		if((getFlags(showIDX))&& (msePtInUIRect(mouseX, mouseY))){//in clickable region for UI interaction
			for(int j=0; j<guiObjs.length; ++j){
				if(guiObjs[j].checkIn(mouseX, mouseY)){	
					msBtnClcked = mseBtn;
					if(pa.isClickModUIVal()){//allows for click-mod
						setUIObjValFromClickAlone(j);
						setFlags(uiObjMod,true);
					} 				
					msClkObj=j;
					return true;	
				}
			}
		}			
		if(getFlags(closeable)){mod = checkClsBox(mouseX, mouseY);}							//check if trying to close or open the window via click, if possible
		if(!getFlags(showIDX)){return mod;}
		if(!mod){
			myPoint mouseClickIn3D = pa.c.getMseLoc(sceneCtrVal);
			mod = hndlMouseClickIndiv(mouseX, mouseY,mouseClickIn3D, mseBtn);
		}			//if nothing triggered yet, then specific instancing window implementation stuff
		if((!mod) && (msePtInRect(mouseX, mouseY, this.rectDim)) && (null!=trajMgr)){ 
			mod = trajMgr.handleMouseClick_Traj(pa.altIsPressed(), getMsePoint(mouseX, mouseY));
		}			//click + alt for traj drawing : only allow drawing trajectory if it can be drawn and no other interaction has occurred
		return mod;
	}//handleMouseClick
	//vector for drag in 3D
	public boolean handleMouseDrag(int mouseX, int mouseY,int pmouseX, int pmouseY, myVector mseDragInWorld, int mseBtn){
		boolean mod = false;
		if(!getFlags(showIDX)){return mod;}
		//check if modding view
		if ((pa.shiftIsPressed()) && getFlags(canChgView) && (msClkObj==-1)) {//modifying view angle/zoom
			pa.setModView(true);	
			if(mseBtn == 0){			handleViewChange(false,pa.msSclY*(mouseY-pmouseY), pa.msSclX*(mouseX-pmouseX));}	
			else if (mseBtn == 1) {		handleViewChange(true,(mouseY-pmouseY), 0);}	//moveZoom(mouseY-pmouseY);}//dz-=(
			return true;
		} else if ((pa.cntlIsPressed()) && getFlags(canChgView) && (msClkObj==-1)) {//modifying view focus
			pa.setModView(true);
			handleViewTargetChange((mouseY-pmouseY), (mouseX-pmouseX));
			return true;
		} else {//modify UI elements
		
			//any generic dragging stuff - need flag to determine if trajectory is being entered		
			//modify object that was clicked in by mouse motion
			if(msClkObj!=-1){	guiObjs[msClkObj].modVal((mouseX-pmouseX)+(mouseY-pmouseY)*-(pa.shiftIsPressed() ? 50.0f : 5.0f));setFlags(uiObjMod, true); return true;}		
			
			if(null!=trajMgr) {
				mod = trajMgr.handleMouseDrag_Traj(mouseX, mouseY, pmouseX, pmouseY, mseDragInWorld, mseBtn);
			}
		
			if(!mod) {
				if((!pa.ptInRange(mouseX, mouseY, rectDim[0], rectDim[1], rectDim[0]+rectDim[2], rectDim[1]+rectDim[3]))){return false;}	//if not drawing or editing a trajectory, force all dragging to be within window rectangle
				//msgObj.dispInfoMessage("myDispWindow","handleMouseDrag","before handle indiv drag traj for window : " + this.name);
				myPoint mouseClickIn3D = pa.c.getMseLoc(sceneCtrVal);
				mod = hndlMouseDragIndiv(mouseX, mouseY,pmouseX, pmouseY,mouseClickIn3D,mseDragInWorld,mseBtn);		//handle specific, non-trajectory functionality for implementation of window
			}
		}
		return mod;
	}//handleMouseDrag
	
	/**
	 * set all window values for UI objects
	 */
	protected void setAllUIWinVals() {for(int i=0;i<guiObjs.length;++i){if(guiObjs[i].getFlags(myGUIObj.usedByWinsIDX)){setUIWinVals(i);}}}
	//set UI value for object based on non-drag modification such as click - either at initial click or when click is released
	private void setUIObjValFromClickAlone(int j) {
		float mult = msBtnClcked * -2.0f + 1;	//+1 for left, -1 for right btn	
		//msgObj.dispInfoMessage("myDispWindow","setUIObjValFromClickAlone","Mult : " + (mult *pa.clickValModMult()));
		guiObjs[j].modVal(mult * pa.clickValModMult());
	}//setUIObjValFromClickAlone
	
	public void handleMouseRelease(){
		if(!getFlags(showIDX)){return;}
		if(getFlags(uiObjMod)){
			setAllUIWinVals();
			setFlags(uiObjMod, false);
			msClkObj = -1;	
		}//some object was clicked - pass the values out to all windows
		else if(msClkObj != -1) {//means object was clicked in but not drag modified through drag or shift-clic - use this to modify by clicking
			setUIObjValFromClickAlone(msClkObj);
			setAllUIWinVals();
			setFlags(uiObjMod, false);
			msClkObj = -1;	
		}
		if(null!=trajMgr) {trajMgr.handleMouseRelease_Traj();}
		msClkObj = -1;	
		//if buttons have been put in clear queue (set to clear), set flag to clear them next draw
		if (privBtnsToClear.size() > 0){setFlags(clearPrivBtns, true);	}		
		hndlMouseRelIndiv();//specific instancing window implementation stuff

		if(null!=trajMgr) {trajMgr.clearTmpDrawnTraj();}
	}//handleMouseRelease	
	
	//release shift/control/alt keys
	public void endShiftKey(){
		if(!getFlags(showIDX)){return;}
		if(null!=trajMgr) {trajMgr.endShiftKey();}
		endShiftKeyI();
	}
	public void endAltKey(){
		if(!getFlags(showIDX)){return;}
		if(null!=trajMgr) {trajMgr.endAltKey();}
		endAltKeyI();
	}	
	public void endCntlKey(){
		if(!getFlags(showIDX)){return;}
		if(null!=trajMgr) {trajMgr.endCntlKey();}
		endCntlKeyI();
	}	
	
	//finds closest point to p in sPts - put dist in d
	public final int findClosestPt(myPoint p, double[] d, myPoint[] _pts){
		int res = -1;
		double mindist = 99999999, _d;
		for(int i=0; i<_pts.length; ++i){_d = myPoint._dist(p,_pts[i]);if(_d < mindist){mindist = _d; d[0]=_d;res = i;}}	
		return res;
	}

	public void rebuildAllDrawnTrajs(){
		if(null!=trajMgr) {trajMgr.rebuildAllDrawnTrajs();}
	}//rebuildAllDrawnTrajs
	
	//debug data to display on screen
	//get string array for onscreen display of debug info for each object
	public String[] getDebugData(){
		ArrayList<String> res = new ArrayList<String>();
		List<String>tmp;
		for(int j = 0; j<guiObjs.length; j++){tmp = Arrays.asList(guiObjs[j].getStrData());res.addAll(tmp);}
		return res.toArray(new String[0]);	
	}
	
	//setup the launch of UI-driven custom functions or debugging capabilities, which will execute next frame
	
	//set colors of the trajectory for this window
	public void setTrajColors(int[] _tfc, int[] _tsc){if(null!=trajMgr) {trajMgr.setTrajColors(_tfc, _tsc);}};//trajFillClrCnst = _tfc;trajStrkClrCnst = _tsc;initTmpTrajStuff(getFlags(trajPointsAreFlat));}
	//get key used to access arrays in traj array
	protected String getTrajAraKeyStr(int i){if(null==trajMgr) {return "";} return trajMgr.getTrajAraKeyStr(i);}
	protected int getTrajAraIDXVal(String str){if(null==trajMgr) {return -1;} return trajMgr.getTrajAraIDXVal(str);  }
	
	public void clearAllTrajectories(){	if(null!=trajMgr) {		trajMgr.clearAllTrajectories();}}//clearAllTrajectories
	
	//add another screen to this window - need to handle specific trajectories - always remake traj structure
	public void addSubScreenToWin(int newWinKey){						if(null!=trajMgr) {		trajMgr.modTrajStructs(newWinKey, "",false);			addSScrToWinIndiv(newWinKey);}}
	public void addTrajToSubScreen(int subScrKey, String newTrajKey){	if(null!=trajMgr) {		trajMgr.modTrajStructs(subScrKey, newTrajKey,false);	addTrajToScrIndiv(subScrKey, newTrajKey);}}
	public void delSubScreenToWin(int delWinKey){						if(null!=trajMgr) {		trajMgr.modTrajStructs(delWinKey, "",true);				delSScrToWinIndiv(delWinKey);}}
	public void delTrajToSubScreen(int subScrKey, String newTrajKey){	if(null!=trajMgr) {		trajMgr.modTrajStructs(subScrKey, newTrajKey,true);		delTrajToScrIndiv(subScrKey,newTrajKey);}}
		
	//updates values in UI with programatic changes 
	public boolean setWinToUIVals(int UIidx, double val){return val == guiObjs[UIidx].setVal(val);}	
	//UI controlled auxiliary/debug functionality	
	public final void clickSideMenuBtn(int _typeIDX, int btnNum) {	curCustBtnType = _typeIDX; curCustBtn[_typeIDX] = btnNum; custClickSetThisFrame = true;}
	
	//check if either custom function or debugging has been launched and process if so, skip otherwise.latched by a frame so that button can be turned on
	public final void checkCustMenuUIObjs() {
		if (custClickSetThisFrame) { custClickSetThisFrame = false;custFuncDoLaunch=true;return;}	//was set last frame and processed, so clear all flags
		if (!custFuncDoLaunch) {return;}//has been launched don't relaunch
		launchMenuBtnHndlr();
		custFuncDoLaunch=false;
	}//checkCustMenuUIObjs

	//type is row of buttons (1st idx in curCustBtn array) 2nd idx is btn
	protected abstract void launchMenuBtnHndlr() ;
	
	//call from custFunc/custDbg functions being launched in threads
	//these are launched in threads to allow UI to respond to user input
	public void resetButtonState() {resetButtonState(true);}
	public void resetButtonState(boolean isSlowProc) {
		if (curCustBtnType == -1) {return;}
		if (curCustBtn[curCustBtnType] == -1) {return;}
		pa.clearBtnState(curCustBtnType,curCustBtn[curCustBtnType], isSlowProc);
		curCustBtn[curCustBtnType] = -1;
	}//resetButtonState
	
	//return relevant name information for files and directories to be used to build screenshots/saved files	
	protected abstract String[] getSaveFileDirNamesPriv();
	
	protected abstract void initDrwnTrajIndiv();
	protected abstract void addSScrToWinIndiv(int newWinKey);
	protected abstract void addTrajToScrIndiv(int subScrKey, String newTrajKey);
	protected abstract void delSScrToWinIndiv(int idx);
	protected abstract void delTrajToScrIndiv(int subScrKey, String newTrajKey);
	/**
	 * return appropriate 3d representation of mouse location - in 2d this will just be mseLoc x, mse Loc y, 0
	 * @param mseLoc x and y are int values of mouse x and y location
	 * @return
	 */
	protected abstract myPoint getMsePtAs3DPt(myPoint mseLoc);	
	//set window-specific variables that are based on current visible screen dimensions
	protected abstract void setVisScreenDimsPriv();
	//implementing class' necessary functions - implement for each individual window
	protected abstract boolean hndlMouseMoveIndiv(int mouseX, int mouseY, myPoint mseClckInWorld);
	protected abstract boolean hndlMouseClickIndiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn);
	protected abstract boolean hndlMouseDragIndiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn);
	protected abstract void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc);	
	
	protected abstract void hndlMouseRelIndiv();
	
	protected abstract void endShiftKeyI();
	protected abstract void endAltKeyI();
	protected abstract void endCntlKeyI();
	
	protected abstract void setCustMenuBtnNames();
	
	//ui init routines
	/**
	 * Initialize all UI objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
	 */
	protected abstract void setupGUIObjsAras();	
	protected abstract void setUIWinVals(int UIidx);
	
	protected abstract void processTrajIndiv(myDrawnSmplTraj drawnTraj);
	
	//file io used from selectOutput/selectInput - 
	//take loaded params and process
	public abstract void hndlFileLoad(File file, String[] vals, int[] stIdx);
	//accumulate array of params to save
	public abstract ArrayList<String> hndlFileSave(File file);	
	
	protected abstract void initMe();
	protected abstract void resizeMe(float scale);	
	protected abstract void showMe();
	protected abstract void closeMe();	
	protected abstract boolean simMe(float modAmtSec);
	protected abstract void stopMe();
	protected abstract void setCameraIndiv(float[] camVals);
	protected abstract void drawMe(float animTimeMod);	
	protected abstract void drawRightSideInfoBarPriv(float modAmtMillis);
	protected abstract void drawOnScreenStuffPriv(float modAmtMillis);
	
	public String toString(){
		String res = "Window : "+name+" ID: "+ID+" Fill :("+fillClr[0]+","+fillClr[1]+","+fillClr[2]+","+fillClr[3]+
				") | Stroke :("+fillClr[0]+","+fillClr[1]+","+fillClr[2]+","+fillClr[3]+") | Rect : ("+
				String.format("%.2f",rectDim[0])+","+String.format("%.2f",rectDim[1])+","+String.format("%.2f",rectDim[2])+","+String.format("%.2f",rectDim[3])+")\n";	
		return res;
	}
}//dispWindow


