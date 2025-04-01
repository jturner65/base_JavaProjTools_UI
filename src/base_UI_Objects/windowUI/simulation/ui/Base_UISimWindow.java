package base_UI_Objects.windowUI.simulation.ui;

import java.util.ArrayList;
import java.util.TreeMap;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.simulation.simExec.Base_UISimExec;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

public abstract class Base_UISimWindow extends Base_DispWindow {
	/**
	 * Executor for simulation functionality, owned by this Sim window.
	 */
	protected Base_UISimExec simExec;	
	
	///////////
	//ui vals

	public final static int
		gIDX_LayoutToUse			= 0,				//which layout/simulation world should be used
		gIDX_TimeStep				= 1,				//delta t time step for simulation integration				 
		gIDX_FrameTimeScale			= 2,				//scaled modAmtMillis to determine how much time the simulation should execute 
		gIDX_ExpLength				= 3,				//length of time for experiment, in minutes
		gIDX_NumExpTrials			= 4;				//number of experimental trials to perform
	
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
			//debugAnimIDX 		= 0,						//debug
			resetSimIDX			= 1,						//whether or not to reset sim
			drawVisIDX 			= 2,						//draw visualization - if false SIM exec and sim should ignore all processing/papplet stuff
			conductExpIDX		= 3,						//conduct experiment with current settings
			conductSweepExpIDX  = 4;						//sweep through experimental value through number of trials

	/**
	 * Number of boolean flags defined in base window. Subsequent IDXs of boolean flags in child class should start here
	 */
	public static final int numBaseSimPrivFlags = 5;

	public Base_UISimWindow(IRenderInterface _p, GUI_AppManager _AppMgr, int _winIdx) {
		super(_p, _AppMgr, _winIdx);
	}
	

	@Override
	protected final int initAllUIButtons(ArrayList<Object[]> tmpBtnNamesArray) {
		// add an entry for each button, in the order they are wished to be displayed
		// true tag, false tag, btn IDX
		tmpBtnNamesArray.add(new Object[] {"Visualization Debug", "Enable Debug", Base_BoolFlags.debugIDX});  
		tmpBtnNamesArray.add(new Object[] {"Resetting Simulation", "Reset Simulation",   resetSimIDX});  
		tmpBtnNamesArray.add(new Object[] {"Drawing Vis", "Render Visualization",  drawVisIDX});  
		tmpBtnNamesArray.add(new Object[] {"Experimenting", "Run Experiment", conductExpIDX});  
		tmpBtnNamesArray.add(new Object[] {"Sweep "+ getSweepFieldName()+" Experiment", "Run "+getSweepFieldName()+" Experiment", conductSweepExpIDX});  

		
		return initSimPrivBtns(tmpBtnNamesArray);
	}
		
	protected abstract int initSimPrivBtns(ArrayList<Object[]> tmpBtnNamesArray);	

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
			case resetSimIDX			: {
				if(val) {simExec.resetSimExec(true); addPrivBtnToClear(resetSimIDX);}break;}
			case drawVisIDX				:{
				simExec.setDoDrawViz(val);break;}
			case conductExpIDX			: {
				//if wanting to conduct exp need to stop current experimet, reset environment, and then launch experiment
				if(val) {
					simExec.setConductSweepExperiment(true);
					simExec.initializeTrials(getUIDataUpdater().getIntValue(gIDX_ExpLength), getUIDataUpdater().getIntValue(gIDX_NumExpTrials));
					AppMgr.setSimIsRunning(true);
					addPrivBtnToClear(conductExpIDX);
				} 
				break;}
			case conductSweepExpIDX			: {
				//if wanting to conduct exp need to stop current experiment, reset environment, and then launch experiment
				if(val) {
					simExec.setConductSweepExperiment(false);
					simExec.initializeTrials(getUIDataUpdater().getIntValue(gIDX_ExpLength), getUIDataUpdater().getIntValue(gIDX_NumExpTrials));
					AppMgr.setSimIsRunning(true);
					addPrivBtnToClear(conductSweepExpIDX);
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
	

	@Override
	protected final void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray,
			TreeMap<Integer, String[]> tmpListObjVals) {
		String[] simLayoutToUseList = getSimLayoutToUseList();
		tmpListObjVals.put(gIDX_LayoutToUse, simLayoutToUseList);	
		
		tmpUIObjArray.put(gIDX_LayoutToUse, uiObjInitAra_List(new double[]{0,simLayoutToUseList.length-1, 1.0f}, 0.0, "Sim Layout To Use"));          				
		tmpUIObjArray.put(gIDX_TimeStep, uiObjInitAra_Float(new double[]{0.00001f,10.0f,0.00001f}, 1.0*initDeltaT, "Sim Time Step"));  
		tmpUIObjArray.put(gIDX_FrameTimeScale, uiObjInitAra_Float(new double[]{1.0f,10000.0f,1.0f}, 1.0*initFrameTimeScale, "Sim Speed Multiplier"));  
		tmpUIObjArray.put(gIDX_ExpLength, uiObjInitAra_Int(new double[]{1.0f, 1440, 1.0f}, 720.0, "Experiment Duration"));    
		tmpUIObjArray.put(gIDX_NumExpTrials, uiObjInitAra_Int(new double[]{1.0f, 100, 1.0f}, 1.0, "# Experimental Trials"));  
		
		setupGUIObjsAras_Sim(tmpUIObjArray, tmpListObjVals);
	}//setupGUIObjsAras
	
	/**
	 * Set up gui objects specifically for sim windows
	 * @param tmpUIObjArray
	 * @param tmpListObjVals
	 */
	protected abstract void setupGUIObjsAras_Sim(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals);
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
			case gIDX_ExpLength 		: {break;}//determines experiment length				
			case gIDX_NumExpTrials 		: {break;}//# of trials for experiments
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
			case gIDX_TimeStep					:{
				simExec.setTimeStep(val);
				break;}
			case gIDX_FrameTimeScale 			:{
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
		if(done) {privFlags.setFlag(conductExpIDX, false);}
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
		//all sub menu drawing within push mat call
		ri.translate(0,custMenuOffset+AppMgr.getTextHeightOffset());		
		//draw any custom menu stuff here
		drawSimCustMenuObjs(animTimeMod);
		ri.popMatState();	
	}//drawCustMenuObjs	
	/**
	 * draw any custom menu objects for sidebar menu based on simulation
	 */
	protected abstract void drawSimCustMenuObjs(float animTimeMod);


}//class Base_UISimWindow
