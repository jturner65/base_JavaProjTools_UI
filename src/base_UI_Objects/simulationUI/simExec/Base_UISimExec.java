package base_UI_Objects.simulationUI.simExec;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.simulationUI.sim.Base_UISimulator;
import base_UI_Objects.simulationUI.ui.Base_UISimWindow;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Utils_Objects.simExec.Base_SimExec;

/**
 * Class to hold method defs for UI-based simulation. Allows for non-UI-based simulations as well by verifying ri existence
 * @author John Turner
 *
 */
public abstract class Base_UISimExec extends Base_SimExec {
    /**
     * Owning window, or null if console application
     */
    public final Base_UISimWindow win;
    /**
     * Owning application
     */
    public static GUI_AppManager AppMgr;
    
    /**
     * ref to render interface, if window-based, or null if console
     */
    protected final IRenderInterface ri;
    
    /**
     * flags relevant to managed simulator execution - idxs in SimPrivStateFlags
     */
    public static final int
                    //debug is idx 0
                    buildVisObjsIDX        = 1,                        //TODO whether or not to build visualization objects - turn off to bypass unnecessary stuff when using console only
                    drawVisIDX            = 2;                        //draw visualization - if false should ignore all rendering calls
    
    /**
     * Number of defined sim flags. Implementations should start the new flag assignments with this value
     */
    protected static final int numSimFlags = 3;
    
    
    public Base_UISimExec(Base_UISimWindow _win, String _name, int _maxSimLayouts) {
        super(_name, _maxSimLayouts);
        if(_win != null) {    win = _win;        AppMgr = Base_DispWindow.AppMgr;  ri = Base_UISimWindow.ri;} 
        else {                win = null;         AppMgr = null;        ri = null;}
    }    
    
    /**
     * Implementation-specific initialization
     */
    protected final void initSimExec_Indiv(){
        //Initialize render objects if rendering is available
        if (hasRenderInterface()) {
            buildRenderObjs();
        } else {
            clearRenderObjs();
        }
        
        initUISimExec_Indiv();
    }//initSimExec_Indiv
    
    /**
     * Set whether the visualizations should be available to be drawn, 
     * usually based on whether a render interface exists or not.
     * @param showVis
     */
    public final void initDoDrawVis(boolean showVis) {
        initMasterDataAdapter(drawVisIDX, showVis);
    }
    
    /**
     * Implementation-specific ui-based sim exec initialization
     */
    protected abstract void initUISimExec_Indiv();
    
    /**
     * Initialize/build the rendered objects to use for the simulation rendering, if they exist
     */
    protected abstract void buildRenderObjs();
    
    /**
     * Clear out structures holding rendered objects for simulation, if any exist
     */
    protected abstract void clearRenderObjs();
    
    
    @Override
    public final boolean hasRenderInterface() {return ri!=null;}
    
    /**
     * Returns this application's render interface. Will be null if a console application 
     * @return
     */
    public final IRenderInterface getRenderInterface() {return ri;}    
    
    /**
     * Draw the current simulation results
     * @param animTimeMod
     */
    public abstract void drawMe(float animTimeMod);
    
    /**
     * Draw the right side info for current sim exec
     * @param modAmtMillis from sim engine - millis since last frame
     * @param rtSideYVals Array of right side menu y values
     *         idx 0 : start y location of text (will change as text is written)
     *         idx 1 : per-line y offset for grouped text
     *         idx 2 : per-line y offset for title-to-group text (small space)
     *         idx 3 : per-line y offset for text that is not grouped (slightly larger)     
     */
    public final void drawRightSideInfoBar(float modAmtMillis, float[] rtSideYVals) {
        if(AppMgr == null) {return;}
        //Draw any header info. txtHeight is starting y for rest of text        
        ri.pushMatState();
            long curTime = (Math.round(getNowTime()/1000.0f));
            AppMgr.showOffsetText(0,IRenderInterface.gui_Yellow, currSim.getName() + " SIMULATION OUTPUT");
            rtSideYVals[0] +=rtSideYVals[1]; ri.translate(0.0f,rtSideYVals[1], 0.0f);
            ri.pushMatState();
            AppMgr.showMenuTxt_White("Sim Time : ");
            AppMgr.showMenuTxt_Green( String.format("%08d", curTime) + " secs ");
            AppMgr.showMenuTxt_White("Sim Clock Time : ");
            AppMgr.showMenuTxt_Green( String.format("%04d", curTime/3600) + " : " + String.format("%02d", (curTime/60)%60 )+ " : " + String.format("%02d", (curTime%60)));
            ri.popMatState();
            rtSideYVals[0] +=rtSideYVals[3]; ri.translate(0.0f,rtSideYVals[3], 0.0f);
    
            ((Base_UISimulator) currSim).drawResultBar(rtSideYVals);
        ri.popMatState();
    }//drawRightSideInfoBar
    
    /**
     * Advance current sim's visualizations, if any, and then step simulation
     * @param modAmtMillis is milliseconds elapsed since last frame
     * @param scaledMillisSinceLastFrame is milliseconds since last frame scaled to speed up simulation
     * @return whether sim is complete or not
     */
    @Override
    protected final boolean stepSimulation_Indiv(float modAmtMillis) {
        boolean done = stepUISimulation_Indiv(modAmtMillis); 
        //move objects in visualization - ignored if not using vis (checked in sim)
        if(getSimFlag(Base_UISimExec.drawVisIDX)) {
            ((Base_UISimulator) currSim).simStepVisualization();
        } else {            
            msgObj.dispConsoleWarningMessage("SimExec("+name+") for "+currSim.getName(), "stepSimulation_Indiv", "Not stepping visualization.");    
        }
        return done;
    }
    /**
     * Advance current simulation
     * @param modAmtMillis is milliseconds elapsed since last frame
     * @return whether sim is complete or not
     */
    protected abstract boolean stepUISimulation_Indiv(float modAmtMillis);    
}//Base_UISimExec
