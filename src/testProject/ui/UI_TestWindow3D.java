package testProject.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.baseApp.GUI_AppUIFlags;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.base.GUI_AppWinVals;
import base_UI_Objects.windowUI.drawnTrajectories.DrawnSimpleTraj;
import base_UI_Objects.windowUI.uiData.UIDataUpdater;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_Utils_Objects.io.messaging.MsgCodes;
import testProject.uiData.UITestDataUpdater_3D;

public class UI_TestWindow3D extends Base_DispWindow {
    /**
     * idxs - need one per ui object
     */
    public final static int
        gIDX_FloatVal1      = 0,
        gIDX_IntVal1        = 1,
        gIDX_ListVal1       = 2,
        gIDX_IntVal2        = 3,
        gIDX_IntVal3        = 4;

    public static final int numBaseGUIObjs = 5;                                            //# of gui objects for ui
    
    /**
     * private child-class flags - window specific
     */
    public static final int 
            //debug is 0
            button1_IDX     = 1,
            button2_IDX     = 2,
            button3_IDX     = 3,
            button4_IDX     = 4,
            button5_IDX     = 5,
            button6_IDX     = 6,
            button7_IDX     = 7,
            button8_IDX     = 8,
            button9_IDX     = 9;

    protected static final int numBasePrivFlags = 10;
    

    protected String[] listOfNames = new String[]{"Name 1", "Name 2", "Name 3", "Name 4", "Name 5"};
    //current/initial values
    protected double floatVal1 = .1;
    
    protected myPointf startLoc;
    protected myPointf endLoc;
    
    public UI_TestWindow3D(IGraphicsAppInterface _p, GUI_AppManager _AppMgr, int _winIdx) {
        super(_p, _AppMgr, _winIdx);
        
    }
    /**
     * Initialize any UI control flags appropriate for window application
     * @param appUIFlags Snapshot of the initial flags structure for the application. 
     * Will not reflect future changes, so should not be retained
     */
    @Override
    protected void initDispFlags(GUI_AppUIFlags _notUsed) {}

    @Override
    protected void initMe() {
        startLoc = new myPointf(MyMathUtils.SQRT_3_F, MyMathUtils.SQRT_3_F, MyMathUtils.SQRT_3_F);
        endLoc = new myPointf();
        setEndAndScalePoints();
    }
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
     * Retrieve the total number of defined privFlags booleans (application-specific state bools and interactive buttons)
     */
    @Override
    public int getTotalNumOfPrivBools() {return numBasePrivFlags;}

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
     *                 idx 2 : Text should be centered (default is false)
     *                 idx 3 : Object should be rendered with outline (default for btns is true, for non-buttons is false)
     *                 idx 4 : Should have ornament
     *                 idx 5 : Ornament color should match label color 
     */
    @Override
    protected final void setupGUIObjsAras(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap){    
        //build list select box values
        //keyed by object idx (uiXXXIDX), entries are lists of values to use for list select ui objects
        tmpUIObjMap.put("gIDX_FloatVal1", uiMgr.uiObjInitAra_Float(gIDX_FloatVal1, new double[]{0,1.0f,.0001f}, floatVal1, "Float Value 1"));                   //uiTrainDataFrmtIDX                                                                        
        tmpUIObjMap.put("gIDX_IntVal1", uiMgr.uiObjInitAra_Int(gIDX_IntVal1, new double[]{1,10,1.0f}, 1.0, "Int Value 1"));                   //uiTrainDataFrmtIDX                                                                        
        tmpUIObjMap.put("gIDX_ListVal1", uiMgr.uiObjInitAra_List(gIDX_ListVal1, 0.0, "List Of Names", listOfNames));                   //uiTrainDataFrmtIDX                                                                        
        tmpUIObjMap.put("gIDX_IntVal2", uiMgr.uiObjInitAra_Int(gIDX_IntVal2, new double[]{-50,50,1.0f}, 0.0, "Int Value 2"));                   //uiTrainDataFrmtIDX                                                                        
        tmpUIObjMap.put("gIDX_IntVal3", uiMgr.uiObjInitAra_Int(gIDX_IntVal3, new double[]{0,1000,1.0f}, 0.0, "Int Value 3"));                   //uiTrainDataFrmtIDX
    }
    
    /**
     * Build UI button objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
     * @param firstIdx : the first index to use in the map/as the objIdx
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is true label
     *                 the second element is false label
     *                 the third element is integer flag idx 
     */
    @Override
    protected final void setupGUIBoolSwitchAras(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {
        int idx=firstIdx;
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.buildDebugButton(idx++,"Debugging", "Enable Debug"));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button1_IDX","Button 1 On", "Button 1 Off", button1_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button2_IDX","Button 2 On", "Button 2 Off", button2_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button3_IDX","Button 3 On", "Button 3 Off", button3_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button4_IDX","Button 4 On", "Button 4 Off", button4_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button5_IDX","Button 5 On", "Button 5 Off", button5_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button6_IDX","Button 6 On", "Button 6 Off", button6_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button7_IDX","Button 7 On", "Button 7 Off", button7_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button8_IDX","Button 8 On", "Button 8 Off", button8_IDX));
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "button9_IDX","Button 9 On", "Button 9 Off", button9_IDX));    
    }    
    
    @Override
    protected void drawMe(float animTimeMod, boolean isGlblAppDebug) {
        ri.pushMatState();    
            ri.drawCylinder_NoFill(startLoc,endLoc, 20.0f, IGraphicsAppInterface.gui_Red, IGraphicsAppInterface.gui_Green);
        ri.popMatState();    
    }
    
    protected void setEndAndScalePoints() {
        float scale = 100.0f / (1.000000001f * startLoc.dist(0,0,0));
        startLoc._mult(scale);        
        endLoc.set(-startLoc.x, -startLoc.y, -startLoc.z);
    }
    
    protected float nextVal_f() {return MyMathUtils.randomFloat() *2.0f - 1.0f;}
    protected void setPtNewRandVal(myPointf V) {V.set(nextVal_f(),nextVal_f(),nextVal_f());}
    protected double nextVal() {return MyMathUtils.randomDouble(-1.0, 1.0);}
    protected void setPtNewRandVal(myPoint V) {V.set(nextVal(),nextVal(),nextVal());}
    
    protected void calcNewEndPoints() {
        startLoc.set(nextVal_f(),nextVal_f(),nextVal_f());        
        setEndAndScalePoints();
    }
    
    @Override
    protected UIDataUpdater buildUIDataUpdateObject() {
        return new UITestDataUpdater_3D(this);
    }

    @Override
    protected void updateCalcObjUIVals() {}

    @Override
    protected void handleDispFlagsDebugMode_Indiv(boolean enable) {
        if(enable) {
            int numMsgCodes = MsgCodes.getNumVals();
            for(int i=0; i<numMsgCodes; ++i) {
                MsgCodes code = MsgCodes.getEnumByIndex(i);
                _dispMessage("handleDispFlagsDebugMode_Indiv", "Message code " +i +" with color for Code : " + code.name(), code);
            }
        }
    }

    @Override
    protected void handlePrivFlagsDebugMode_Indiv(boolean val) {}

    @Override
    protected void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal) {}

    @Override
    protected int[] getFlagIDXsToInitToTrue() {return null;}

    @Override
    protected void setUI_IntValsCustom(int UIidx, int ival, int oldVal) {}

    @Override
    protected void setUI_FloatValsCustom(int UIidx, float val, float oldVal) {}

    @Override
    protected void drawCustMenuObjs(float animTimeMod, boolean isGlblAppDebug) {}

    @Override
    public void handleSideMenuMseOvrDispSel(int btn, boolean val) {}

    @Override
    protected void handleSideMenuDebugSelEnable(int btn) {}

    @Override
    protected void handleSideMenuDebugSelDisable(int btn) {}

    @Override
    protected void launchMenuBtnHndlr(int funcRow, int btn, String label) {}

    @Override
    protected String[] getSaveFileDirNamesPriv() {return null;}

    @Override
    protected void initDrwnTraj_Indiv() {}

    @Override
    protected void addSScrToWin_Indiv(int newWinKey) {}

    @Override
    protected void addTrajToScr_Indiv(int subScrKey, String newTrajKey) {}

    @Override
    protected void delSScrToWin_Indiv(int idx) {}

    @Override
    protected void delTrajToScr_Indiv(int subScrKey, String newTrajKey) {}

    @Override
    protected myPoint getMsePtAs3DPt(myPoint mseLoc) {return null;}

    @Override
    protected void setVisScreenDimsPriv() {}

    @Override
    protected boolean hndlMouseMove_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld) {return false;}

    @Override
    protected boolean hndlMouseClick_Indiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {return false;}

    @Override
    protected boolean hndlMouseDrag_Indiv(int mouseX, int mouseY, int pmouseX, int pmouseY, myPoint mouseClickIn3D,
            myVector mseDragInWorld, int mseBtn) {return false;}

    @Override
    protected void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc) {}

    @Override
    protected void hndlMouseRel_Indiv() {}

    @Override
    protected void endShiftKey_Indiv() {}

    @Override
    protected void endAltKey_Indiv() {}

    @Override
    protected void endCntlKey_Indiv() {}

    @Override
    protected void setCustMenuBtnLabels() {}
    
    @Override
    public void processTraj_Indiv(DrawnSimpleTraj drawnTraj) {}

    @Override
    public void hndlFileLoad(File file, String[] vals, int[] stIdx) {}

    @Override
    public ArrayList<String> hndlFileSave(File file) {return null;}

    @Override
    protected void resizeMe(float scale) {}

    @Override
    protected void showMe() {}

    @Override
    protected void closeMe() {}

    @Override
    protected boolean simMe(float modAmtSec) {calcNewEndPoints();    return false;}

    @Override
    protected void stopMe() {}

    @Override
    protected void setCamera_Indiv(float[] camVals) {}

    @Override
    protected void drawRightSideInfoBarPriv(float modAmtMillis, boolean isGlblAppDebug) {}

    @Override
    protected void drawOnScreenStuffPriv(float modAmtMillis, boolean isGlblAppDebug) {}

    @Override
    protected boolean handleMouseWheel_Indiv(int ticks, float mult) {
        // TODO Auto-generated method stub
        return false;
    }
}//class UI_TestWindow3D
