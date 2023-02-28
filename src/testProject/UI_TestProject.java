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
	 * idx's in dispWinFrames for each window - 0 is always left side menu window
	 * Side menu is dispMenuIDX == 0
	 */
	private static final int disp3DResIDX = 1,
							disp2DResIDX = 2;
	/**
	 * # of visible windows including side menu (always at least 1 for side menu)
	 */
	private static final int numVisWins = 3;
	
	/**
	 * Whether to use skybox or not
	 */
	private final boolean[] useSkybox = new boolean[] {false,false,false};
	
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
	protected int getNumDispWindows() {	return numVisWins;	}
	
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

		//instanced window dimensions when open and closed - only showing 1 open at a time
		float[][] _floatDims  = new float[][] {getDefaultWinDimOpen(), getDefaultWinDimClosed(), getInitCameraValues()};	

		//Builds sidebar menu button config - application-wide menu button bar titles and button names
		String[] menuBtnTitles = new String[]{"Functions 1","Functions 2","Functions 3"};
		String[][] menuBtnNames = new String[][] { // each must have literals for every button defined in side bar menu, or ignored
			{"Func 00", "Func 01", "Func 02"},				//row 1
			{"Func 10", "Func 11", "Func 12", "Func 13"},	//row 2
			{"Func 10", "Func 11", "Func 12", "Func 13"}	//row 3
			};
		String [] dbgBtns = {"Debug 0", "Debug 1", "Debug 2", "Debug 3","Debug 4"};
		//Builds sidebar menu
		buildSideBarMenu(_winTitles, menuBtnTitles, menuBtnNames, dbgBtns, true, false);
		
		//define windows
		/**
		 *  _winIdx The index in the various window-descriptor arrays for the dispWindow being set
		 *  _title string title of this window
		 *  _descr string description of this window
		 *  _dispFlags Essential flags describing the nature of the dispWindow for idxs : 
		 * 		0 : dispWinIs3d, 
		 * 		1 : canDrawInWin; 
		 * 		2 : canShow3dbox (only supported for 3D); 
		 * 		3 : canMoveView
		 *  _floatVals an array holding float arrays for 
		 * 				rectDimOpen(idx 0),
		 * 				rectDimClosed(idx 1),
		 * 				initCameraVals(idx 2)
		 *  _intClrVals and array holding int arrays for
		 * 				winFillClr (idx 0),
		 * 				winStrkClr (idx 1),
		 * 				winTrajFillClr(idx 2),
		 * 				winTrajStrkClr(idx 3),
		 * 				rtSideFillClr(idx 4),
		 * 				rtSideStrkClr(idx 5)
		 *  _sceneCenterVal center of scene, for drawing objects (optional)
		 *  _initSceneFocusVal initial focus target for camera (optional)
		 */
		
		int wIdx = disp3DResIDX;
		setInitDispWinVals(wIdx, _winTitles[wIdx], _winDescr[wIdx], new boolean[]{true,false,true,true}, _floatDims,		
			new int[][] {new int[]{255,255,255,255},new int[]{0,0,0,255},
				new int[]{180,180,180,255},new int[]{100,100,100,255},
				new int[]{0,0,0,200},new int[]{255,255,255,255}});
		dispWinFrames[wIdx] = new UI_TestWindow3D(ri, this, wIdx);
		wIdx = disp2DResIDX;
		setInitDispWinVals(wIdx, _winTitles[wIdx], _winDescr[wIdx], new boolean[]{false,false,false,true}, _floatDims,
				new int[][] {new int[]{50,40,20,255}, new int[]{255,255,255,255},
					new int[]{180,180,180,255}, new int[]{100,100,100,255},
					new int[]{0,0,0,200},new int[]{255,255,255,255}});
		dispWinFrames[wIdx] = new UI_TestWindow2D(ri, this, wIdx);

		//specify windows that cannot be shown simultaneously here
		initXORWins(new int[]{disp3DResIDX,disp2DResIDX}, new int[]{disp3DResIDX, disp2DResIDX});
		
	}//initAllDispWindows

	@Override
	protected void initOnce_Indiv() {
		setVisFlag(disp3DResIDX, true);
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
	/**
	 * Individual window handling
	 * @param idx window index
	 * @param menuRectVals rectValus from left side menu
	 * @return
	 */
	@Override
	protected final float[] getUIRectVals_Indiv(int idx, float[] menuRectVals) {
			//this.pr("In getUIRectVals for idx : " + idx);
		switch(idx){
			case disp3DResIDX 		: {return menuRectVals;}
			case disp2DResIDX 		: {return menuRectVals;}
			default :  return menuRectVals;
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
	public int getNumVisFlags() {return numVisWins;}
	@Override
	//address all flag-setting here, so that if any special cases need to be addressed they can be
	protected void setVisFlag_Indiv(int idx, boolean val ){
		switch (idx){
			case disp3DResIDX		: {setWinFlagsXOR(disp3DResIDX, val); break;}
			case disp2DResIDX		: {setWinFlagsXOR(disp2DResIDX, val); break;}
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
