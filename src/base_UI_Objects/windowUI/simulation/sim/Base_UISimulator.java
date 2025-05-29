package base_UI_Objects.windowUI.simulation.sim;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.simulation.simExec.Base_UISimExec;
import base_UI_Objects.windowUI.simulation.ui.Base_UISimWindow;
import base_Utils_Objects.sim.Base_Simulator;

/**
 * Class for UI-based simulations - adds handling for 
 * result rendering and simulation-pertinent interactive value modification
 * @author John Turner
 *
 */
public abstract class Base_UISimulator extends Base_Simulator {
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
	public final IRenderInterface ri;
	
	/**
	 * Build this UI-enabled simulator - must be owned by a UI-enabled exec 
	 * @param _exec
	 * @param _name
	 */
	public Base_UISimulator(Base_UISimExec _exec, String _name, int _simLayoutToUse) {
		super(_exec, _name, _simLayoutToUse);
		win=((Base_UISimExec)exec).win;
		ri = Base_DispWindow.ri;
		AppMgr = Base_UISimWindow.AppMgr;
	}

	/**
	 * Consume the newly set data values from sim exec
	 */
	protected final void useDataUpdateVals_Indiv() {
		//TODO add standard UI-based sim values to be updated here
		// i.e. record changes to deltaT here
		
		useUIDataUpdateVals();
	}
	
	@Override
	protected void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal) {
		switch(idx){
			//case debugSimIDX 			: {break;}		//idx 0 is debug already anyway		
			case Base_UISimExec.drawVisIDX				: {break;}		//draw visualization - if false should ignore all processing/papplet stuff	
			default :{
				if(!handlePrivSimFlags_Indiv(idx, val, oldVal)) {
					msgObj.dispErrorMessage("Simulator("+name+")", "handlePrivFlags_Indiv", "Unknown/unhandled simulation flag idx :"+idx+" attempting to be set to "+val+" from "+oldVal+". Aborting.");
				}
			}			
		}			
	}//handlePrivFlags_Indiv		
			
	protected abstract boolean handlePrivSimFlags_Indiv(int idx, boolean val, boolean oldVal);
	
	
	/**
	 * Set this simulator to draw or not draw visualization.
	 * @param val
	 */
	@Override
	public final void setSimDrawVis(boolean val) {setSimFlag(Base_UISimExec.drawVisIDX, val);}
	
	/**
	 * Evolve a simulation visualization; called Base_UISimExec - evolve visualization.
	 * Use timestep/frameTimeScale set in Base_Simulator 
	 */
	public abstract void simStepVisualization();
	
	/**
	 * Consume the newly set data values from sim exec for specifically UI-based implementations
	 */
	protected abstract void useUIDataUpdateVals();

	
	/**
	 * Render the pertinent data for this simulator
	 * @param animTimeMod in seconds, time that has passed since last draw call
	 */
	public final void drawMe(float animTimeMod) {
		//scale animation by frameTimeScale
		drawMe_Indiv(animTimeMod* frameTimeScale);
	}
	
	/**
	 * Render the pertinent data for the implementation simulator
	 * @param animTimeMod time since last draw call, scaled by frameTimeScale
	 */
	protected abstract void drawMe_Indiv(float animTimeMod);
	
	/**
	 * draw result information on right sidebar
	 * @param yVals float array holding : 
	 * 		idx 0 : start y value for next text
	 * 		idx 1 : per-line y offset for grouped text
	 * 		idx 2 : per-line y offset for title-to-group text (small space)
	 * 		idx 3 : per-line y offset for text that is not grouped (slightly larger)
	 * 		
	 */
	public final void drawResultBar( float[] yVals ) {
		//TODO: Add any simulation-agnostic information to display here
		yVals[0] = drawResultBar_Indiv(yVals);
		
	}//drawResultBar
	
	/**
	 * draw sim-specific result information on right sidebar, if gui-based sim
	 * @param ri
	 * @param yVals float array holding : 
	 * 		idx 0 : start y value for text
	 * 		idx 1 : per-line y offset for grouped text
	 * 		idx 2 : per-line y offset for title-to-group text (small space)
	 * 		idx 3 : per-line y offset for text that is not grouped (slightly larger)
	 * @return next yValue to draw text at
	 */
	protected abstract float drawResultBar_Indiv(float[] yVals);
}//class Base_UISimulator
