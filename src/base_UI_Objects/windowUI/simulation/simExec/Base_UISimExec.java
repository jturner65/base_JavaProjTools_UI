package base_UI_Objects.windowUI.simulation.simExec;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.simulation.sim.Base_UISimulator;
import base_UI_Objects.windowUI.simulation.ui.Base_UISimWindow;
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
	protected final Base_UISimWindow win;
	
	/**
	 * ref to render interface, if window-based, or null if console
	 */
	protected final IRenderInterface ri;
	
	/**
	 * flags relevant to managed simulator execution - idxs in SimPrivStateFlags
	 */
	public static final int
					//debug is idx 0
					buildVisObjsIDX		= 1,						//TODO whether or not to build visualization objects - turn off to bypass unnecessary stuff when using console only
					drawVisIDX			= 2;						//draw visualization - if false should ignore all rendering calls
	
	/**
	 * Number of defined sim flags. Implementations should start the new flag assignments with this value
	 */
	protected static final int numSimFlags = 3;
	
	
	public Base_UISimExec(Base_UISimWindow _win, String _name, int _maxSimLayouts) {
		super(_name, _maxSimLayouts);
		if(_win != null) {	win = _win;		ri = Base_UISimWindow.ri;} 
		else {				win = null;		ri = null;}
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
	 * 		idx 0 : start y location of text (will change as text is written)
	 * 		idx 1 : per-line y offset for grouped text
	 * 		idx 2 : per-line y offset for title-to-group text (small space)
	 * 		idx 3 : per-line y offset for text that is not grouped (slightly larger)	 
	 */
	public final void drawRightSideInfoBar(float modAmtMillis, float[] rtSideYVals) {
		if(ri == null) {return;}
		//Draw any header info. yOffset is starting y for rest of text		
		ri.pushMatState();
			long curTime = (Math.round(getNowTime()/1000.0f));
			ri.setFill(255,255,0,255);	
			ri.showText(currSim.getName() + " SIMULATION OUTPUT", 0, rtSideYVals[0]);rtSideYVals[0] += rtSideYVals[2];
			ri.setFill(255,255,255,255);
			ri.showText("Sim Time : " + String.format("%08d", curTime) + " secs ", 0, rtSideYVals[0]);
			ri.showText("Sim Clock Time : " + String.format("%04d", curTime/3600) + " : " + String.format("%02d", (curTime/60)%60 )+ " : " + String.format("%02d", (curTime%60)), 150, rtSideYVals[0]);
	//		rtSideYVals[0] += rtSideYVals[1];
	//		pa.showText("Wall Clock Time : " + String.format("%04d", curTime/3600) + " : " + String.format("%02d", (curTime/60)%60 )+ " : " + String.format("%02d", (curTime%60)), 0, rtSideYVals[0]);
			rtSideYVals[0] += rtSideYVals[3];
		
			((Base_UISimulator) currSim).drawResultBar(ri, rtSideYVals);
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
