package base_UI_Objects.windowUI.simulation.sim;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.simulation.simExec.Base_UISimExec;
import base_Utils_Objects.sim.Base_Simulator;

/**
 * Class for UI-based simulations
 * @author John Turner
 *
 */
public abstract class Base_UISimulator extends Base_Simulator {
	
	//TODO : declare deltaT here ?
	
	/**
	 * Build this UI-enabled simulator - must be owned by a UI-enabled exec 
	 * @param _exec
	 * @param _name
	 */
	public Base_UISimulator(Base_UISimExec _exec, String _name) {
		super(_exec, _name);
	}

	/**
	 * Consume the newly set data values from sim exec
	 */
	protected final void useDataUpdateVals_Indiv() {
		//TODO add standard UI-based sim values to be updated here
		// i.e. record changes to deltaT here
		
		useUIDataUpdateVals();
	}
	
	/**
	 * Consume the newly set data values from sim exec for specifically UI-based implementations
	 */
	protected abstract void useUIDataUpdateVals();
	
	/**
	 * Evolve a simulation visualization
	 * @param scaledMillisSinceLastFrame
	 */
	public abstract void simStepVisualization(float scaledMillisSinceLastFrame);
	
	/**
	 * Render the pertinent data for this simulator
	 * animTimeMod is in seconds, time that has passed since last draw call
	 * @param ri
	 * @param animTimeMod
	 * @param win
	 */
	public abstract void drawMe(IRenderInterface ri, float animTimeMod, Base_DispWindow win);
	
	/**
	 * draw result information on right sidebar, if gui-based sim
	 * @param ri
	 * @param yOff
	 */
	public abstract void drawResultBar(IRenderInterface ri, float yOff);

}//class Base_UISimulator
