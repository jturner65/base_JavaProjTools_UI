package testProject;

import java.util.HashMap;

import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.sidebar.SidebarMenu;
import base_Utils_Objects.io.messaging.MsgCodes;
import testProject.ui.UI_TestWindow2D;
import testProject.ui.UI_TestWindow3D;

/**
 * This project is for testing UI development
 * @author John Turner
 *
 */
public class UI_TestProject extends GUI_AppManager {

	public final String prjNmLong = "UI Test Dev Project";
	public final String prjNmShrt = "UI_TestProject";
	public final String projDesc = "Test application for UI/GUI App Library";
	
	/**
	 * 
	 */
	private final int
		showUIMenu = 0,
		show3DWin = 1,
		show2DWin = 2;
	public final int numVisFlags = 3;
		
	/**
	 * idx's in dispWinFrames for each window - 0 is always left side menu window
	 */
	private static final int disp3DResIDX = 1,
							disp2DResIDX = 2;	
	
	/**
	 * Whether to use skybox or not
	 */
	private final boolean[] useSkybox = new boolean[] {false,true,false};
	
	/**
	 * Background color
	 */
	public final int[] bground = new int[]{244,244,255,255};
	

	/**
	 * @param args
	 */
	public static void main(String[] passedArgs) {
		UI_TestProject me = new UI_TestProject();		
		UI_TestProject.invokeProcessingMain(me, passedArgs);
	}


	public UI_TestProject() {super();}
	/**
	 * Set various relevant runtime arguments in argsMap
	 * @param _passedArgs command-line arguments
	 */
	@Override
	protected HashMap<String,Object> setRuntimeArgsVals(HashMap<String, Object> _passedArgsMap) {
		return  _passedArgsMap;
	}

	@Override
	protected void setSmoothing() {		ri.setSmoothing(0);		}
	/**
	 * whether or not we want to restrict window size on widescreen monitors
	 * 
	 * @return 0 - use monitor size regardless
	 * 			1 - use smaller dim to be determine window 
	 * 			2+ - TBD
	 */
	@Override
	protected int setAppWindowDimRestrictions() {	return 1;}	

	@Override
	protected boolean getUseSkyboxBKGnd(int winIdx) {return useSkybox[winIdx];}

	@Override
	protected String getSkyboxFilename(int winIdx) {return "bkgrndTex.jpg";}

	@Override
	protected int[] getBackgroundColor(int winIdx) {return bground;}

	@Override
	protected int getNumDispWindows() {	return numVisFlags;	}
	
	@Override
	public String getPrjNmShrt() {return prjNmShrt;}
	@Override
	public String getPrjNmLong() {return prjNmLong;}
	@Override
	public String getPrjDescr() {return projDesc;}
	
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
	@Override
	protected void setupAppDims_Indiv() {}

	@Override
	protected void initBaseFlags_Indiv() {
		setBaseFlagToShow_debugMode(true);
		setBaseFlagToShow_saveAnim(true); 
		setBaseFlagToShow_runSim(true);
		setBaseFlagToShow_singleStep(true);
		setBaseFlagToShow_showRtSideMenu(true);		
	}//initBaseFlags_Indiv

	@Override
	protected void initAllDispWindows() {
		showInfo = true;
		String[] _winTitles = new String[]{"","UI Test Window 3D","UI Test Window 2D"},
				_winDescr = new String[] {"", "Multi Flock Predator/Prey Boids 3D Simulation","Multi Flock Predator/Prey Boids 2D Simulation"};
		setWinTitlesAndDescs(_winTitles, _winDescr);
		//call for menu window
		buildInitMenuWin();
		//instanced window dimensions when open and closed - only showing 1 open at a time
		float[] _dimOpen  = getDefaultWinDimOpen(), 
				_dimClosed  = getDefaultWinDimClosed();	
		int wIdx = dispMenuIDX,fIdx=showUIMenu;
		//new mySideBarMenu(this, winTitles[wIdx], fIdx, winFillClrs[wIdx], winStrkClrs[wIdx], winRectDimOpen[wIdx], winRectDimClose[wIdx], winDescr[wIdx]);
		//Builds sidebar menu button config
		//application-wide menu button bar titles and button names
		String[] menuBtnTitles = new String[]{"Functions 1","Functions 2","Functions 3"};
		String[][] menuBtnNames = new String[][] { // each must have literals for every button defined in side bar menu, or ignored
			{"Func 00", "Func 01", "Func 02"},				//row 1
			{"Func 10", "Func 11", "Func 12", "Func 13"},	//row 2
			{"Func 10", "Func 11", "Func 12", "Func 13"}	//row 3
			};
		String [] dbgBtns = {"Debug 0", "Debug 1", "Debug 2", "Debug 3","Debug 4"};
		dispWinFrames[wIdx] = buildSideBarMenu(wIdx, fIdx,menuBtnTitles, menuBtnNames, dbgBtns, true, false);
		
		//define windows
		//idx 0 is menu, and is ignored	
		//setInitDispWinVals : use this to define the values of a display window
		//int _winIDX, 
		//float[] _dimOpen, float[] _dimClosed  : dimensions opened or closed
		//boolean[] _dispFlags 					: 
		//   flags controlling display of window :  idxs : 0 : canDrawInWin; 1 : canShow3dbox; 2 : canMoveView; 3 : dispWinIs3d
		//int[] _fill, int[] _strk, 			: window fill and stroke colors
		//int _trajFill, int _trajStrk)			: trajectory fill and stroke colors, if these objects can be drawn in window (used as alt color otherwise)
		//			//display window initialization	
		wIdx = disp3DResIDX; fIdx = show3DWin;
		setInitDispWinVals(wIdx, _dimOpen, _dimClosed,new boolean[]{false,true,true,true}, new int[]{255,255,255,255},new int[]{0,0,0,255},new int[]{180,180,180,255},new int[]{100,100,100,255}); 
		dispWinFrames[wIdx] = new UI_TestWindow3D(ri, this, wIdx, fIdx);
		wIdx = disp2DResIDX; fIdx = show2DWin;
		setInitDispWinVals(wIdx, _dimOpen, _dimClosed,new boolean[]{false,false,true,false}, new int[]{50,40,20,255}, new int[]{255,255,255,255},new int[]{180,180,180,255},new int[]{100,100,100,255});
		dispWinFrames[wIdx] = new UI_TestWindow2D(ri, this, wIdx, fIdx);

		//specify windows that cannot be shown simultaneously here
		initXORWins(new int[]{show3DWin,show2DWin}, new int[]{disp3DResIDX, disp2DResIDX});
		
	}//initAllDispWindows

	@Override
	protected void initOnce_Indiv() {
		setVisFlag(showUIMenu, true);					//show input UI menu	
		setVisFlag(show3DWin, true);
	}
	@Override
	protected void initProgram_Indiv() {	}

	@Override
	public String[] getMouseOverSelBtnLabels() {
		return new String[0];
	}
	
	@Override
	protected void handleKeyPress(char key, int keyCode) {
		switch (key){
			case '1' : {break;}
			case '2' : {break;}
			case '3' : {break;}
			case '4' : {break;}
			case '5' : {break;}							
			case '6' : {break;}
			case '7' : {break;}
			case '8' : {break;}
			case '9' : {break;}
			case '0' : { break;}							
			case ' ' : {toggleSimIsRunning(); break;}							//run sim
			case 'f' : {dispWinFrames[curFocusWin].setInitCamView();break;}//reset camera
			case 'a' :
			case 'A' : {toggleSaveAnim();break;}						//start/stop saving every frame for making into animation
			case 's' :
			case 'S' : {break;}		
			default : {	}
		}//switch	
	}
	
	//////////////////////////////////////////
	/// graphics and base functionality utilities and variables
	//////////////////////////////////////////

	/**
	 * Individual extending Application Manager post-drawMe functions
	 * @param modAmtMillis
	 * @param is3DDraw
	 */
	@Override
	protected void drawMePost_Indiv(float modAmtMillis, boolean is3DDraw) {}

	@Override
	//gives multiplier based on whether shift, alt or cntl (or any combo) is pressed
	public double clickValModMult(){return ((altIsPressed() ? .1 : 1.0) * (shiftIsPressed() ? 10.0 : 1.0));}	

	@Override
	public boolean isClickModUIVal() {
		//TODO change this to manage other key settings for situations where multiple simultaneous key presses are not optimal or convenient
		return altIsPressed() || shiftIsPressed();		
	}
	@Override
	public float[] getUIRectVals(int idx) {
			//this.pr("In getUIRectVals for idx : " + idx);
		switch(idx){
			case dispMenuIDX 		: {return new float[0];}			//idx 0 is parent menu sidebar
			case disp3DResIDX 		: {return dispWinFrames[dispMenuIDX].uiClkCoords;}
			case disp2DResIDX 		: {return dispWinFrames[dispMenuIDX].uiClkCoords;}
			default :  return dispWinFrames[dispMenuIDX].uiClkCoords;
		}
	}

	@Override
	public void handleShowWin(int btn, int val, boolean callFlags) {
		if(!callFlags){//called from setflags - only sets button state in UI to avoid infinite loop
			setMenuBtnState(SidebarMenu.btnShowWinIdx,btn, val);
		} else {//called from clicking on buttons in UI
			//val is btn state before transition 
			boolean bVal = (val == 1?  false : true);
			//each entry in this array should correspond to a clickable window
			setVisFlag(winFlagsXOR[btn], bVal);
		}
	}
	
	
	/**
	 * return the number of visible window flags for this application
	 * @return
	 */
	@Override
	public int getNumVisFlags() {return numVisFlags;}
	@Override
	//address all flag-setting here, so that if any special cases need to be addressed they can be
	protected void setVisFlag_Indiv(int idx, boolean val ){
		switch (idx){
			case showUIMenu 	: { dispWinFrames[dispMenuIDX].dispFlags.setShowWin(val);    break;}											//whether or not to show the main ui window (sidebar)			
			case show3DWin		: {setWinFlagsXOR(disp3DResIDX, val); break;}
			case show2DWin		: {setWinFlagsXOR(disp2DResIDX, val); break;}
			default : {break;}
		}
	}//setFlags  

	@Override
	public int[] getClr_Custom(int colorVal, int alpha) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Set minimum level of message object console messages to display for this application. If null then all messages displayed
	 * @return
	 */
	@Override
	protected final MsgCodes getMinConsoleMsgCodes() {return null;}
	/**
	 * Set minimum level of message object log messages to save to log for this application. If null then all messages saved to log.
	 * @return
	 */
	@Override
	protected final MsgCodes getMinLogMsgCodes() {return null;}

}//UI_TestProject
