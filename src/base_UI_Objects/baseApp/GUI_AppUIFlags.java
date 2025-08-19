package base_UI_Objects.baseApp;

import java.util.ArrayList;
import java.util.HashMap;

import base_Math_Objects.MyMathUtils;
import base_UI_Objects.GUI_AppManager;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * Manage various application-wide boolean control flags. 
 * Some of these may not be shown in a particular application
 * These flags are mostly linked to UI fields and are propagated to all windows.
 */
public class GUI_AppUIFlags extends Base_BoolFlags {
    public static GUI_AppManager appMgr;
    
    public static final int
        saveAnim            = _numBaseFlags,            //whether we are saving or not an anim screenie
        showCanvas          = _numBaseFlags + 1,        //whether we should show the 'drawable canvas', the plane upon which drawn trajectory points are placed.    
        //simulation             
        runSim              = _numBaseFlags + 2,        //run simulation
        singleStep          = _numBaseFlags + 3,        //run single sim step
        //UI                                          
        showRtSideInfoDisp  = _numBaseFlags + 4,        //display the right side info display for the current window, if it supports that display
        showStatusBar       = _numBaseFlags + 5;        //whether or not to display status bar with frames per second and mem usage
    
     /**
     * # of UI-based control flags being managed
     */
    private static final int _numPrivFlags = _numBaseFlags+6;
 
    /**
     * booleans in main program - need to have labels in idx order, even if not displayed
     */
    private final String[] _truePFlagNames = {//needs to be in order of flags
            "Debug Mode",
            "Save Anim",
            "Showing Drawable Canvas",
            "Stop Simulation",
            "Single Step",
            "Showing Side Info Window",
            "Showing Status Bar"
            };
    
    private final String[] _falsePFlagNames = {//needs to be in order of flags
            "Debug Mode",
            "Save Anim", 
            "Show Drawable Canvas",
            "Run Simulation",
            "Single Step",
            "Show Side Info Window",
            "Show Status Bar"
            };
    
    
    /**
     * Colors to use to display true flags
     */
    private int[][] _trueFlagColors = new int[_numPrivFlags][3];
    
    /**
     * flags to actually display in menu as clickable text labels - order does matter
     */
    private HashMap<Integer, Integer> _flagsToShow; 
    
    /**
     * Temporary list of flags in order.
     */
    private ArrayList<Integer> _flagsInOrderKILL_ME_PLEASE;

    /**
     * Number of boolean application flags being shown
     */
    private int _numFlagsToShow;
    
    /**
     * Constructor
     * @param _appMgr Application manager that owns this flags structure
     */
    public GUI_AppUIFlags(GUI_AppManager _appMgr) {
        super(_numPrivFlags);
        appMgr = _appMgr;
        _flagsToShow = new HashMap<Integer, Integer>();
        for(int i=0;i<_numPrivFlags;++i) {         _flagsToShow.put(i, i);   }
        _numFlagsToShow = _flagsToShow.size();
        _flagsInOrderKILL_ME_PLEASE = new ArrayList<Integer>( _flagsToShow.keySet());
        for(int i = 0; i < _numPrivFlags; ++i){ 
            _trueFlagColors[i] = MyMathUtils.randomIntClrAra(150, 100, 150);   
        }
    }

    /**
     * Copy constructor
     * @param _otr
     */
    public GUI_AppUIFlags(GUI_AppUIFlags _otr) {        
        super(_otr);
        _flagsToShow = new HashMap<Integer, Integer>(_otr._flagsToShow);
        _numFlagsToShow = _flagsToShow.size();
        _flagsInOrderKILL_ME_PLEASE = new ArrayList<Integer>( _flagsToShow.keySet());
        for(int i = 0; i < _numPrivFlags; ++i){
            _otr._trueFlagColors[i] = new int[3];
            System.arraycopy(_otr._trueFlagColors[i], 0, _trueFlagColors[i], 0, 3);
        }
    }

    /**
     * Set which flags should be shown
     * @param showDebug whether debug button should be shown
     * @param showSaveAnim whether the save anim button should be shown
     * @param showRunSim whether the run sim button should be shown
     * @param showSingleStep whether the single-step sim button should be shown
     * @param showShowRtSideDisp whether the show-right-side-info-disp button should be shown
     * @param showShowStatusBar whether the show-status-bar button should be shown
     * @param showShowCanvas whether the show-canvas button should be shown
     */
    public final void setAllFlagsToShow(
            boolean showDebug, 
            boolean showSaveAnim, 
            boolean showRunSim, 
            boolean showSingleStep, 
            boolean showShowRtSideDisp,
            boolean showShowStatusBar,
            boolean showShowCanvas) {
        _flagsToShow = new HashMap<Integer, Integer>();
        if (showDebug){_flagsToShow.put(debugIDX, debugIDX);}         
        if (showSaveAnim){_flagsToShow.put(saveAnim, saveAnim);}      
        if (showRunSim){_flagsToShow.put(runSim, runSim);}        
        if (showSingleStep){_flagsToShow.put(singleStep, singleStep);}    
        if (showShowRtSideDisp){_flagsToShow.put(showRtSideInfoDisp, showRtSideInfoDisp);}
        if (showShowStatusBar){_flagsToShow.put(showStatusBar, showStatusBar);}
        if (showShowCanvas){_flagsToShow.put(showCanvas, showCanvas);}
        _numFlagsToShow = _flagsToShow.size();
        _flagsInOrderKILL_ME_PLEASE = new ArrayList<Integer>( _flagsToShow.keySet());        
    }//setAllFlagsToShow
    
    
    private final int[] btnGreyClr = new int[]{180,180,180};
    public final void dispMenuText(float xOffHalf, float yOffHalf) {
        for(Integer key : _flagsToShow.keySet()) {
        //for(int idx =0; idx<_numFlagsToShow; ++idx){
            //int i = _flagsToShow.get(idx);
            if(getFlag(key) ){        appMgr.dispMenuTxtLat(_truePFlagNames[key], _trueFlagColors[key], true, xOffHalf,yOffHalf);}
            else {                    appMgr.dispMenuTxtLat(_falsePFlagNames[key], btnGreyClr, false, xOffHalf,yOffHalf);}                    
        }
    }//dispMenuText
        
    /**
     * Toggle the flag from given UI input. TODO This will all go away once the side menu is properly synthesizing the UI
     * @param idx
     */
    public final void toggleFlagByIDX(int idx) {
        int flagIDX = _flagsInOrderKILL_ME_PLEASE.get(idx);
        toggleFlag(flagIDX);
    }
    
    
    /**
     * Return number of flags to show
     * @return
     */
    public final int getNumFlagsToShow() {return _numFlagsToShow;}

    /**
     * Whether or not the 'Debug mode' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_debugMode() {return   _flagsToShow.containsKey(debugIDX);}
    /**
     * Whether or not the 'Save anim' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_saveAnim() {return _flagsToShow.containsKey(saveAnim);}
    /**
     * Whether or not the 'Run sim' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_runSim() {return _flagsToShow.containsKey(runSim);}
    /**
     * Whether or not the 'Single step' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_singleStep() {return _flagsToShow.containsKey(singleStep);}
    /**
     * Whether or not the 'Show Right Side Info Window' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_showRtSideInfoDisp() {return _flagsToShow.containsKey(showRtSideInfoDisp);}
    /**
     * Whether or not the 'Show Status Bar' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_showStatusBar() {return _flagsToShow.containsKey(showStatusBar);}
    /**
     * Whether or not the 'Show Drawable Canvas' boolean is shown
     * @return
     */
    public final boolean getBaseFlagIsShown_showDrawableCanvas() {return _flagsToShow.containsKey(showCanvas);}
    
    
    ///////////////////////////////
    /// getter setter shortcuts
    
    /**
     * Toggle the sim is running flag
     */
    public final void toggleSimIsRunning() {toggleFlag(runSim);}
    /**
     * Set whether or not to start sim execution
     * @param val
     */
    public final void setRunSim(boolean val) {setFlag(runSim, val);}
    /**
     * Get whether or not to start sim execution
     * @return
     */
    public final boolean doRunSim() {return getFlag(runSim);}
    /**
     * Set whether or not a single step of simulation should be executed
     * @param val
     */
    public final void setDoSingleStep(boolean val) { setFlag(singleStep, val);}    
    /**
     * Get whether or not to do a single step of simulation
     * @return
     */
    public final boolean doSingleStep() {return getFlag(singleStep);}
    
    /**
     * Set whether or not to show the status bar
     * @param val
     */
    public final void setShowStatusBar(boolean val) {       setFlag(showStatusBar, val);}
    /**
     * Get whether or not to show the status bar
     * @return
     */
    public final boolean doShowStatusBar() {return getFlag(showStatusBar);}
    
    /**
     * Set whether or not to show the drawable canvas
     * @param val
     */
    public final void setShowDrawableCanvas(boolean val) {  setFlag(showCanvas, val);}    
    /**
     * Get whether or not to show the drawable canvas
     * @return
     */
    public final boolean doShowDrawawbleCanvas() {return getFlag(showCanvas);}
    
    /**
     * Toggle the save anim flag
     */
    public final void toggleSaveAnim() {toggleFlag(saveAnim);}
    /**
     * Set whether or not to save a picture of current anim
     * @param val
     */
    public final void setSaveAnim(boolean val) {setFlag(saveAnim, val);}
   
    
    @Override
    protected void handleSettingDebug(boolean val) {        appMgr.setAllWinIsDebugMode(val);   }
    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
        switch(idx){
            case saveAnim           : { break;}
            case showCanvas         : { break;}
            case runSim             : { break;}
            case showRtSideInfoDisp : { appMgr.setAllWinShowRtSideInfoDisp(val);break;}
            case showStatusBar      : { break;}
            case singleStep         : { break;}
        }// switch
    }//handleFlagSet_Indiv

}//class GUI_AppUIFlags
