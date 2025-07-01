package base_UI_Objects.simulationUI.ui;

import java.util.LinkedHashMap;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.simulationUI.simExec.Base_UISimExec;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

public abstract class Base_UISimWindow extends Base_DispWindow {
    /**
     * Executor for simulation functionality, owned by this Sim window.
     */
    protected Base_UISimExec simExec;    
    
    ///////////
    //ui vals

    public final static int
        gIDX_LayoutToUse            = 0,                //which layout/simulation world should be used
        gIDX_TimeStep                = 1,                //delta t time step for simulation integration                 
        gIDX_FrameTimeScale            = 2,                //scaled modAmtMillis to determine how much time the simulation should execute 
        gIDX_ExpLength                = 3,                //length of time for experiment, in minutes
        gIDX_NumExpTrials            = 4;                //number of experimental trials to perform
    
    /**
     * Number of gui objects defined in base window. Subsequent IDXs in child class should start here
     */
    protected static final int numBaseSimGUIObjs = 5;    
    
    /**
     * Initial scaling time to speed up simulation == amount to multiply modAmtMillis by
     */
    protected final float initFrameTimeScale = 1000.0f;    
    
    /**
     * Initial delta t time for timestepping/integrating simulation calculations
     */
    protected final float initDeltaT = 0.01f;
    
    /**
     * private child-class flags - window specific
     */
    public static final int 
            //debugAnimIDX         = 0,                        //debug
            resetSimIDX            = 1,                        //whether or not to reset sim
            drawVisIDX             = 2,                        //draw visualization - if false SIM exec and sim should ignore all processing/papplet stuff
            conductExpIDX        = 3,                        //conduct experiment with current settings
            conductSweepExpIDX  = 4;                        //sweep through experimental value through number of trials

    /**
     * Number of boolean flags defined in base window. Subsequent IDXs of boolean flags in child class should start here
     */
    public static final int numBaseSimPrivFlags = 5;

    public Base_UISimWindow(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx) {
        super(_p, _AppMgr, _winIdx);
    }
    
    /**
     * Get sweep experiment name for buttons
     * @return
     */
    protected abstract String getSweepFieldName();

    @Override
    protected final void initMe() {//all ui objects set by here
        //initialize sim exec
        initSimExec();        
        //implementation class specifics 
        initMeSim();
    }//initMe()
    
    /**
     * Initialize the sim exec this window manages and set up its simulations
     */
    private final void initSimExec() {
        simExec = buildSimulationExecutive(getName(), getSimLayoutToUseList().length);
        //Initialize the simulation executive
        boolean riExists = (ri != null);
        //specify whether visualizations should be available to be drawn based on wither a render interface exists
        //(windowed sim vs console sim)
        simExec.initDoDrawVis(riExists);
        //initialize the sim exec with implementation-specific details
        initSimExec(riExists);
        //Create all sims the sim exec manages
        simExec.createAllSims();
        //set which simulator we should use        
        setSimToUse(0);    
    }//initSimExec
        
    /**
     * Simulation implementation specific initialization.
     */
    protected abstract void initMeSim();
    
    /**
     * Initialize the simulation executive during initMe() after simExec was created
     * @param showVis whether or not we should render the visualizations for this simulation
     */
    protected abstract void initSimExec(boolean showVis);

    /**
     * Build the executive managing the simulations owned by this window
     * @param _simName base name of simulation the sim exec manages
     * @param _numSimulations
     * @return
     */
    protected abstract Base_UISimExec buildSimulationExecutive(String _simName, int _numSimulations);

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
    protected final void handlePrivFlagsDebugMode_Indiv(boolean val) {    
        simExec.setExecDebug(val);        
    }

    @Override
    protected final void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal) {
        switch(idx){
            case resetSimIDX            : {
                if(val) {simExec.resetSimExec(true); addPrivSwitchToClear(resetSimIDX);}break;}
            case drawVisIDX                :{
                simExec.setDoDrawViz(val);break;}
            case conductExpIDX            : {
                //if wanting to conduct exp need to stop current experimet, reset environment, and then launch experiment
                if(val) {
                    simExec.setConductSweepExperiment(true);
                    simExec.initializeTrials(getUIDataUpdater().getIntValue(gIDX_ExpLength), getUIDataUpdater().getIntValue(gIDX_NumExpTrials));
                    AppMgr.setSimIsRunning(true);
                    addPrivSwitchToClear(conductExpIDX);
                } 
                break;}
            case conductSweepExpIDX            : {
                //if wanting to conduct exp need to stop current experiment, reset environment, and then launch experiment
                if(val) {
                    simExec.setConductSweepExperiment(false);
                    simExec.initializeTrials(getUIDataUpdater().getIntValue(gIDX_ExpLength), getUIDataUpdater().getIntValue(gIDX_NumExpTrials));
                    AppMgr.setSimIsRunning(true);
                    addPrivSwitchToClear(conductSweepExpIDX);
                } 
                break;}                
            default :{
                if (!handleSimPrivFlags_Indiv(idx, val, oldVal)){
                    msgObj.dispErrorMessage(className, "handlePrivFlags_Indiv", "Unknown/unhandled flag idx :"+idx+" attempting to be set to "+val+" from "+oldVal+". Aborting.");
                }                
            }
        }
    }//handlePrivFlags_Indiv
    
    /**
     * Instance-specific boolean flags to handle
     * @param idx
     * @param val
     * @param oldVal
     * @return
     */
    protected abstract boolean handleSimPrivFlags_Indiv(int idx, boolean val, boolean oldVal);    
    
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
        String[] simLayoutToUseList = getSimLayoutToUseList();
        
        tmpUIObjMap.put("gIDX_LayoutToUse", uiMgr.uiObjInitAra_List(gIDX_LayoutToUse, 0.0, "Sim Layout To Use", simLayoutToUseList));                          
        tmpUIObjMap.put("gIDX_TimeStep", uiMgr.uiObjInitAra_Float(gIDX_TimeStep, new double[]{0.00001f,10.0f,0.00001f}, 1.0*initDeltaT, "Sim Time Step"));  
        tmpUIObjMap.put("gIDX_FrameTimeScale", uiMgr.uiObjInitAra_Float(gIDX_FrameTimeScale, new double[]{1.0f,10000.0f,1.0f}, 1.0*initFrameTimeScale, "Sim Speed Multiplier"));  
        tmpUIObjMap.put("gIDX_ExpLength", uiMgr.uiObjInitAra_Int(gIDX_ExpLength, new double[]{1.0f, 1440, 1.0f}, 720.0, "Experiment Duration"));    
        tmpUIObjMap.put("gIDX_NumExpTrials", uiMgr.uiObjInitAra_Int(gIDX_NumExpTrials, new double[]{1.0f, 100, 1.0f}, 1.0, "# Experimental Trials"));
        
        setupGUIObjsAras_Sim(tmpUIObjMap);
    }
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
    protected abstract void setupGUIObjsAras_Sim(LinkedHashMap<String, GUIObj_Params> tmpUIObjMap);

    /**
     * Build UI button objects to be shown in left side bar menu for this window.  This is the first child class function called by initThisWin
     * @param firstIdx : the first index to use in the map/as the objIdx
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is the object index
     *                 the second element is true label
     *                 the third element is false label
     *                 the final element is integer flag idx 
     */
    @Override
    protected final void setupGUIBoolSwitchAras(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap) {        

        //By here all non-button objects should be created already
        //TODO use the same obj map as tmpUIObjMap
        int idx=firstIdx;
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.buildDebugButton(idx++,"Visualization Debug", "Enable Debug"));  
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "Reset_Sim", "Resetting Simulation", "Reset Simulation", resetSimIDX));  
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "Draw_Vis", "Drawing Vis", "Render Visualization",  drawVisIDX));  
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "Run_Experiment", "Experimenting", "Run Experiment", conductExpIDX));  
        tmpUIBoolSwitchObjMap.put("Button_"+idx, uiMgr.uiObjInitAra_Switch(idx++, "Sweep_Expermient", "Sweep "+ getSweepFieldName()+" Experiment", "Run "+getSweepFieldName()+" Experiment", conductSweepExpIDX));  

        setupGUIBoolSwitchAras_Sim(idx, tmpUIBoolSwitchObjMap);
    }//setupGUIObjsAras

    /**
     * Build all UI buttons to be shown in left side bar menu for this window. This is for instancing windows to add to button region
     * @param firstIdx : the first index to use in the map/as the objIdx
     * @param tmpUIBoolSwitchObjMap : map of GUIObj_Params to be built containing all flag-backed boolean switch definitions, keyed by sequential value == objId
     *                 the first element is the object index
     *                 the second element is true label
     *                 the third element is false label
     *                 the final element is integer flag idx 
     */
    protected abstract void setupGUIBoolSwitchAras_Sim(int firstIdx, LinkedHashMap<String, GUIObj_Params> tmpUIBoolSwitchObjMap);
    
    /**
     * Return the list to use for sim layout
     * @return
     */
    protected abstract String[] getSimLayoutToUseList();

    /**
     * Set which simulation implementation to use
     * @param ival
     */
    protected abstract void setSimToUse(int ival);
    
    /**
     * Called if int-handling guiObjs_Numeric[UIidx] (int or list) has new data which updated UI adapter. 
     * Intended to support custom per-object handling by owning window.
     * Only called if data changed!
     * @param UIidx Index of gui obj with new data
     * @param ival integer value of new data
     * @param oldVal integer value of old data in UIUpdater
     */    
    @Override
    protected final void setUI_IntValsCustom(int UIidx, int ival, int oldVal) {
        switch(UIidx){    
            case gIDX_LayoutToUse : {
                setSimToUse(ival);
                break;}
            case gIDX_ExpLength         : {break;}//determines experiment length                
            case gIDX_NumExpTrials         : {break;}//# of trials for experiments
            default : {
                if (!setUI_SimIntValsCustom(UIidx, ival, oldVal)) {
                    msgObj.dispWarningMessage(className, "setUI_IntValsCustom", "No int-defined gui object mapped to idx :"+UIidx);
                }
            }
        }        
    }//setUI_IntValsCustom
    
    /**
     * Handle instance-specific integer ui value setting
     * @param UIidx
     * @param ival
     * @param oldVal
     * @return
     */
    protected abstract boolean setUI_SimIntValsCustom(int UIidx, int ival, int oldVal);    
    /**
     * Called if float-handling guiObjs_Numeric[UIidx] has new data which updated UI adapter.  
     * Intended to support custom per-object handling by owning window.
     * Only called if data changed!
     * @param UIidx Index of gui obj with new data
     * @param val float value of new data
     * @param oldVal integer value of old data in UIUpdater
     */
    @Override
    protected final void setUI_FloatValsCustom(int UIidx, float val, float oldVal) {
        switch(UIidx){
            case gIDX_TimeStep                    :{
                simExec.setTimeStep(val);
                break;}
            case gIDX_FrameTimeScale             :{
                simExec.setTimeScale(val);
                break;}
            default : {
                if (!setUI_SimUIFloatValsCustom(UIidx, val, oldVal)) {
                    msgObj.dispWarningMessage(className, "setUI_FloatValsCustom", "No float-defined gui object mapped to idx :"+UIidx);
                }
            }
        }        
    }    
    /**
     * Handle instance-specific float ui value setting
     * @param UIidx
     * @param ival
     * @param oldVal
     * @return
     */
    protected abstract boolean setUI_SimUIFloatValsCustom(int UIidx, float ival, float oldVal);    
    
    /**
     * modAmtMillis is time passed per frame in milliseconds
     */
    @Override
    protected final boolean simMe(float modAmtMillis) {//run simulation
        boolean done = simExec.stepSimulation(modAmtMillis);
        if(done) {uiMgr.setPrivFlag(conductExpIDX, false);}
        return simMePostExec_Indiv(modAmtMillis, done);    
    }//simMe
    /**
     * Implementation-specific sim step functionality, after simExec is called
     * @param modAmtMillis
     * @param done
     * @return
     */
    protected abstract boolean simMePostExec_Indiv(float modAmtMillis, boolean done);
    
    @Override
    protected final void drawRightSideInfoBarPriv(float modAmtMillis) {
        float[] rtSideYOffVals = AppMgr.getRtSideYOffVals();
        ri.pushMatState();
        //display current simulation variables and data on right side menu
        simExec.drawRightSideInfoBar(modAmtMillis, rtSideYOffVals);
        //Reset y start value for next frame
        rtSideYOffVals[0] = 0;
        ri.popMatState();        
    }//drawRightSideInfoBarPriv
    
    /**
     * animTimeMod is in seconds.
     */
    @Override
    protected final void drawMe(float animTimeMod) {
        // draw current sim - TODO move to Base_DispWindow?
        simExec.drawMe(animTimeMod);
    }//drawMe    
    
    //draw custom 2d constructs below interactive component of menu
    @Override
    public final void drawCustMenuObjs(float animTimeMod){
        ri.pushMatState();    
        //draw any custom menu stuff here
        drawSimCustMenuObjs(animTimeMod);
        ri.popMatState();    
    }//drawCustMenuObjs    
    /**
     * draw any custom menu objects for sidebar menu based on simulation
     */
    protected abstract void drawSimCustMenuObjs(float animTimeMod);


}//class Base_UISimWindow
