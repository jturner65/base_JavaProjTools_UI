package base_UI_Objects.windowUI.sidebar;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.baseApp.GUI_AppUIFlags;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.base.GUI_AppWinVals;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * displays sidebar menu of interaction and functionality
 * @author John Turner
 *
 */
public class SidebarMenu extends Base_DispWindow{
    /**
     * Number of main, app-wide booleans to show
     */
    private final int numMainFlagsToShow;
    
    private final int clkFlgsStY;

    /**
     * private child-class flags - window specific
     */
    public static final int
            //idx 0 is debug in Base_BoolFlags
            mseClickedInBtnsIDX     = 1,                    //the mouse was clicked in the button region of the menu and a click event was processed
            usesWinBtnDispIDX       = 2,                    //this menu displays the window title bar
            usesMseOvrBtnDispIDX    = 3,                    //this menu uses mouse-over display text
            usesDbgBtnDispIDX       = 4;                    //this menu displays debug side bar buttons

    //private flag based buttons - ui menu won't display these
    private static final int numBasePrivFlags = 5;
    
    //where buttons should start on side menu
    /**
     * index in arrays of button states/names for the show window buttons, if present
     */
    public static final int btnShowWinIdx = 0;    
    /**
     * configuration, build and rendering of side bar buttons
     */
    private SidebarMenuBtnConfig btnConfig;
    
    /**
     * Size of text height offset on creation TODO get rid of this value in favor of live updates
     */
    private final float txtHeightOffHalf;
    private final float xOffHalf;
    /**
     * Size of button label on creation TODO get rid of this value in favor of live updates
     */
    private final float initBtnLblYOff;
    
    /**
     * 
     * @param _ri
     * @param _AppMgr
     * @param _winIdx
     * @param _btnCfg
     */
    public SidebarMenu(IGraphicsAppInterface _ri, GUI_AppManager _AppMgr, int _winIdx, SidebarMenuBtnConfig _btnCfg) {
        super(_ri, _AppMgr, _winIdx);
        btnConfig=_btnCfg;
        
        clkFlgsStY = (int) AppMgr.getTextHeightOffset();
        
        xOffHalf = AppMgr.getXOffsetHalf();
        txtHeightOffHalf = 0.5f * btnConfig.initTextHeightOff;
        initBtnLblYOff = AppMgr.getBtnLabelYOffset();
        
        //these have to be set before setupGUIObjsAras is called from initThisWin
        numMainFlagsToShow = AppMgr.getNumFlagsToShow();
        //msgObj.dispConsoleDebugMessage("SidebarMenu", "ctor", "clkFlgsStY : " + clkFlgsStY+ "|initTextHeightOff : " + initTextHeightOff + "|initBtnLblYOff : "+initBtnLblYOff+"| initRowStYOff : "+initRowStYOff+"| minBtnClkY : "+minBtnClkY);
        // build uiClkCoords
        //all ui objects for all windows will follow this format and share the uiClkCoords[0] value    
        // UI region for Application-wide buttons, under application flags
        btnConfig.setAppButtonRegion(winInitVals.rectDim, (numMainFlagsToShow+3) * btnConfig.initTextHeightOff);
        // Change UI FlagReg - standard init;
    }//ctor
            
    /**
     * Override for SidebarMenu of setting initial state flags
     */
    @Override
    protected final void initDispFlags(GUI_AppUIFlags _notUsed) {
        //menu is never runnable
        dispFlags.setIsRunnable(false);
        //menu never has a rt side menu window
        dispFlags.setHasRtSideInfoDisp(false);
        //menu is not ever closeable 
        dispFlags.setIsCloseable(false);            
    }
    
    /**
     * init/reinit this window
     */
    @Override
    protected final void initMe() {    }    
    /**
     * This function implements the instantiation of a child window owned by this window, if such exists.
     * The implementation should be similar to how the main windows are implemented in GUI_AppManager::initAllDispWindows.
     * If no child window exists, this implementation of this function can be empty
     * 
     * @param GUI_AppWinVals the window control values for the child window.
     */
    @Override
    protected final void buildAndSetChildWindow_Indiv(GUI_AppWinVals _appVals) {}  
    /**
     * Get the click coordinates formed by the parent, or in this case, the initial coords under the global state display indicators
     * @return
     */
    @Override
    protected final float[] getParentWindowUIClkCoords() {
        // Application flags start at beginning y of window rect dimensions
        float uiClkCoordsYStart = winInitVals.rectDim[1] + .01f * winInitVals.rectDim[3];
        // UI click coords will be flag region + Application buttons (so include flag y start)
        float[] UIAppButtonRegion = btnConfig.getUIAppBtnRegion();
        return new float[] {UIAppButtonRegion[0], uiClkCoordsYStart, UIAppButtonRegion[2], UIAppButtonRegion[3]};
    }//getParentWindowUIClkCoords
    
    ////////////////////////////////////////////
    ///
    public final static int
        gIDX_FloatVal1      = 0;
    public static final int numBaseGUIObjs = 1;
    
    //current/initial values
    protected double floatVal1 = .1;

    
    /**
     * Build all UI objects to be shown in left side bar menu for this window. This is the first child class function called by initThisWin
     * @param tmpUIObjMap : map of GUIObj_Params, keyed by unique string, with values describing the UI object
     *             - The object IDX                   
     *          - A double array of min/max/mod values                                                   
     *          - The starting value                                                                      
     *          - The label for object                                                                       
     *          - The object type (GUIObj_Type enum)
     *          - A boolean array of behavior configuration values : (unspecified values default to false)
     *               idx 0: value is sent to owning window,  
     *               idx 1: value is sent on any modifications (while being modified, not just on release), 
     *               idx 2: changes to value must be explicitly sent to consumer (are not automatically sent),
     *          - A boolean array of renderer format values :(unspecified values default to false) - Behavior Boolean array must also be provided!
     *                 - Should be multiline
     *                 - One object per row in UI space (i.e. default for multi-line and btn objects is false, single line non-buttons is true)
     *                 - Force this object to be on a new row/line (For side-by-side layouts)
     *                 - Text should be centered (default is false)
     *                 - Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 - Should have ornament
     *                 - Ornament color should match label color 
     */
    @Override
    protected final void setupGUIObjsAras(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap) {
        //build list select box values
        //keyed by object idx (uiXXXIDX), entries are lists of values to use for list select ui objects
        //tmpUIObjMap.put("gIDX_FloatVal1", uiMgr.uiObjInitAra_Float(gIDX_FloatVal1, new double[]{0,1.0f,.0001f}, floatVal1, "Float Value 1"));        
    }
    
    /**
     * Build all UI buttons to be shown in left side bar menu for this window. This is for instancing windows to add to button region
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is true label
     *                 the second element is false label
     *                 the third element is integer flag idx 
     */
    @Override
    protected final void setupGUIBoolSwitchAras(int firstIdx,LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {
  
//        LinkedHashMap<String, GUIObj_Params> tmpUIGrpBuilderMap = new LinkedHashMap<String, GUIObj_Params>(); 
//        int grpIdx = 0;
//        // add all previously initialized existing boolean switch vals to a group
//        if(firstIdx > 0) {
//            tmpUIGrpBuilderMap.put("row_"+(grpIdx++)+"_init", uiMgr.buildUIObjGroupParams(tmpUIBoolSwitchObjMap));
//            tmpUIBoolSwitchObjMap.clear();
//        }
//        int idx=firstIdx;
//        // For all groups of application switch booleans
//        // get visible application booleans from app mgr
//        
//        
//        
//        
//        tmpUIBoolSwitchObjMap.put("AppSwitch_"+idx, uiMgr.buildDebugButton(idx++,"Debugging", "Enable Debug"));
//        //tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_SwitchMainBools(idx++, "button1_IDX", "Button 1 On", "Button 1 Off", button1_IDX));
//        
//
//        
//        
//        // For each group of non-switch booleans
//        
//        
//        
//        tmpUIBoolSwitchObjMap.putAll(tmpUIGrpBuilderMap);
    }
    
    /**
     * Set the function button labels based on the requirements of the application window
     * @param _funRowIDX
     * @param BtnLabels
     */
    public void setAllFuncBtnLabels(int _funRowIDX, String[] BtnLabels) {btnConfig.setAllFuncBtnLabels(_funRowIDX, BtnLabels);}
    
    /**
     * Return the label on the sidebar button specified by the passed row and column
     * @param row
     * @param col
     * @return
     */
    public final String getSidebarMenuButtonLabel(int row, int col) {        return btnConfig.getSidebarMenuButtonLabel(row, col);}
    
    public Boolean[][] getGuiBtnWaitForProc() {return  btnConfig.getGuiBtnWaitForProc();}
    public void setGuiBtnWaitForProc(Boolean[][] _guiBtnWaitForProc) {        btnConfig.setGuiBtnWaitForProc(_guiBtnWaitForProc);}
    public int[][] getGuiBtnSt() {        return btnConfig.getGuiBtnState();    }
    public void setGuiBtnSt(int[][] _guiBtnSt) {btnConfig.setGuiBtnState(_guiBtnSt);    }
    
    /**
     * Retrieve the total number of defined privFlags booleans (application-specific state bools and interactive buttons)
     */
    @Override
    public int getTotalNumOfPrivBools() {return numBasePrivFlags;}
    
    /**
     * Window UI for all sidebar data
     */
    @Override
    protected UIDataUpdater buildUIDataUpdateObject() {
        return new SidebarMenuData(this);
    }
    @Override
    protected final void updateCalcObjUIVals() {}
    @Override
    protected int[] getFlagIDXsToInitToTrue() {
        ArrayList<Integer> resAra = new ArrayList<Integer>();
        if(btnConfig._initBtnShowWin) {         resAra.add(usesWinBtnDispIDX);}
        if(btnConfig._initBtnMseFunc) {         resAra.add(usesMseOvrBtnDispIDX);}
        if(btnConfig._initBtnDBGSelCmp) {       resAra.add(usesDbgBtnDispIDX);}
        int[] res = new int[resAra.size()];
        for(int i=0;i<res.length;++i) {         res[i]=resAra.get(i);        }
        return res;
    }
    
    /**
     * Handle application-specific flag setting
     */
    @Override
    public final void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal){
        switch (idx) {//special actions for each flag
            case mseClickedInBtnsIDX    : {break;}            
            case usesWinBtnDispIDX      : {break;}
            case usesMseOvrBtnDispIDX   : {break;}
            case usesDbgBtnDispIDX      : {break;}
        }
    }

    /**
     * UI code-level Debug mode functionality. Called only from flags structure
     * @param val
     */
    @Override
    protected final void handleDispFlagsDebugMode_Indiv(boolean val) {}
    
    /**
     * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
     * @param val
     */
    @Override
    protected final void handlePrivFlagsDebugMode_Indiv(boolean val) {    }
    
    /**
     * turn off buttons that may be on and should be turned off - called at release of mouse - check for mouse loc before calling (in button region)?
     */
    private final void clearAllBtnStates(){
        if(uiMgr.getPrivFlag(mseClickedInBtnsIDX)) {
            btnConfig.clearAllBtnStates();
            uiMgr.setPrivFlag(mseClickedInBtnsIDX, false);
        }
    }//clearAllBtnStates
        
    /**
     * Set non-momentary buttons to be waiting for processing complete command
     * @param row
     * @param col
     */
    public final void setWaitForProc(int row, int col) {btnConfig.setWaitForProc(row, col);}
    @Override
    protected void setUI_IntValsCustom(int UIidx, int ival, int oldval) {}
    @Override
    protected void setUI_FloatValsCustom(int UIidx, float val, float oldval) {}
    @Override
    protected final void launchMenuBtnHndlr(int funcRow, int btn, String label) {    }
    @Override
    public final void handleSideMenuMseOvrDispSel(int btn, boolean val) {    }
    @Override
    protected final void handleSideMenuDebugSelEnable(int btn) {    }
    @Override
    protected final void handleSideMenuDebugSelDisable(int btn) {    }
    @Override
    protected boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld){        return false;    }
    @Override
    protected boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {    
        if(!winInitVals.pointInRectDim(mouseX, mouseY)){return false;}//not in this window's bounds, quit asap for speedz
        //TODO Awful - needs to be recalced, dependent on menu being on left
        int i = (int)((mouseY-(initBtnLblYOff + clkFlgsStY))/(btnConfig.initTextHeightOff));                    
        //msgObj.dispInfoMessage(className, "hndlMouseClick_Indiv", "Clicked on disp windows : i : " + i+"|uiClkCoords[1] = "+uiClkCoords[1]+" | UIAppButtonRegion[1] :"+UIAppButtonRegion[1]);
        
        if((i>=0) && (i<numMainFlagsToShow)){
            AppMgr.toggleAppFlag(i);return true;    
        } else if(btnConfig.checkInButtonRegion(mouseX, mouseY)) {
            boolean clkInBtnRegion = btnConfig.checkButtons(mouseX, mouseY, winInitVals.rectDim[2]);
            if(clkInBtnRegion) { uiMgr.setPrivFlag(mseClickedInBtnsIDX, true);}
            return clkInBtnRegion;
        }//in region where clickable buttons are - uiClkCoords[1] is bottom of buttons
        return false;
    }
    /**
     * regular UI obj handling handled elsewhere - custom UI handling necessary to call main window    
     * @param mouseX
     * @param mouseY
     * @param pmouseX
     * @param pmouseY
     * @param mouseClickIn3D
     * @param mseDragInWorld
     * @param mseBtn
     * @return
     */
    @Override
    protected boolean hndlMouseDrag_Indiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {        
        boolean res = AppMgr.getCurFocusDispWindow().sideBarMenu_CallWinMseDrag_Indiv(mouseX, mouseY,pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);    
        return res;    }
    @Override
    protected void hndlMouseRel_Indiv() {    clearAllBtnStates();}

    @Override
    protected boolean handleMouseWheel_Indiv(int ticks, float mult) {        return false;   }
  
    /**
     * For windows to draw on screen
     */
    @Override
    protected final void drawOnScreenStuffPriv(float modAmtMillis, boolean isGlblAppDebug) {}
    @Override
    protected final void drawRightSideInfoBarPriv(float modAmtMillis, boolean isGlblAppDebug) {}
    /**
     * Draw window/application-specific functionality
     * @param animTimeMod # of milliseconds since last frame divided by 1000
     */
    @Override
    protected final void drawMe(float animTimeMod, boolean isGlblAppDebug) {
        ri.pushMatState();
            ri.pushMatState();
                AppMgr.drawSideBarStateLights(animTimeMod, btnConfig.initTextHeightOff);                //lights that reflect various states
            ri.popMatState();        
            ri.pushMatState();
                //draw main booleans and their state
                ri.translate(xOffHalf,initBtnLblYOff);
                ri.setColorValFill(IGraphicsAppInterface.gui_Black,255);
                ri.showText("Application Control Flags",0,txtHeightOffHalf);
                ri.translate(0,clkFlgsStY);
                AppMgr.dispMenuText(xOffHalf,txtHeightOffHalf);
            ri.popMatState();    
            ri.pushMatState();            
                btnConfig.drawSideBarButtons(
                        isGlblAppDebug,
                        initBtnLblYOff,
                        xOffHalf,
                        AppMgr.getRowStYOffset(),
                        winInitVals.rectDim[2]);                        //draw buttons
            ri.popMatState();    
            ri.pushMatState();
                //draw what global user-modifiable fields are currently available
                uiMgr.drawGUIObjs(animTimeMod, isGlblAppDebug); 
            ri.popMatState();            
            ri.pushMatState();
                AppMgr.drawWindowGuiObjs(animTimeMod, isGlblAppDebug);            //draw objects for window(s) with primary focus
            ri.popMatState();    
        ri.popMatState();
    }//drawMe
    
    @Override
    public final void drawCustMenuObjs(float animTimeMod, boolean isGlblAppDebug){}    
    //no custom camera handling for menu , float rx, float ry, float dz are all now member variables of every window
    @Override
    protected final void setCamera_Indiv(float[] camVals){}
    @Override
    public void hndlFileLoad(File file, String[] vals, int[] stIdx) {
        hndlFileLoad_GUI(vals, stIdx);
    }
    @Override
    public ArrayList<String> hndlFileSave(File file) {
        ArrayList<String> res = hndlFileSave_GUI();

        return res;
    }
    /**
     * If the window is resized
     */
    @Override
    protected final void resizeMe(float scale) {
        
    }
    
    @Override
    protected String[] getSaveFileDirNamesPriv() {return new String[]{"menuDir","menuFile"};    }
    @Override
    protected final void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc){}//not a snap-to window
    @Override
    protected final void setVisScreenDimsPriv() {}
    @Override
    protected myPoint getMsePtAs3DPt(myPoint mseLoc){return new myPoint(mseLoc.x,mseLoc.y,0);}
    @Override
    protected final void endShiftKey_Indiv() {}
    @Override
    protected final void endAltKey_Indiv() {}
    @Override
    protected final void endCntlKey_Indiv() {}
    @Override
    protected final void closeMe() {}
    @Override
    protected final void showMe() {}    
    @Override
    protected final void setCustMenuBtnLabels() {}
    @Override
    protected boolean simMe(float modAmtSec) {return false;}
    @Override
    protected final void stopMe() {}
    @Override
    protected final void addSScrToWin_Indiv(int newWinKey){}
    @Override
    protected final void addTrajToScr_Indiv(int subScrKey, String newTrajKey){}
    @Override
    protected final void delSScrToWin_Indiv(int idx) {}    
    @Override
    protected final void delTrajToScr_Indiv(int subScrKey, String newTrajKey) {}        
    //no trajectory here
    @Override
    public final void processTraj_Indiv(DrawnSimpleTraj drawnTraj){}    
    @Override
    protected final void initDrwnTraj_Indiv(){}
    @Override
    public String toString(){
        String res = super.toString();
        return res;
    }

}//mySideBarMenu