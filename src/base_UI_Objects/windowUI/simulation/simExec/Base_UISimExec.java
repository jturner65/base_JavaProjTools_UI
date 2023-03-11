package base_UI_Objects.windowUI.simulation.simExec;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.simulation.sim.Base_UISimulator;
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
	protected final Base_DispWindow win;
	
	/**
	 * ref to render interface, if window-based, or null if console
	 */
	protected final IRenderInterface ri;	
	
	public Base_UISimExec(Base_DispWindow _win, String _name, int _maxSimLayouts) {
		super(_name, _maxSimLayouts);
		if(_win != null) {	win = _win;		ri = Base_DispWindow.ri;} 
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
	 * Draw the right side info bar for the currently executing simulator
	 * @param txtHeightOff
	 * @param modAmtMillis
	 */
	public abstract void drawRightSideInfoBar(float txtHeightOff, float modAmtMillis);	
	
	/**
	 * Advance current sim's visualizations, if any, and then step simulation
	 * @param modAmtMillis is milliseconds elapsed since last frame
	 * @param scaledMillisSinceLastFrame is milliseconds since last frame scaled to speed up simulation
	 * @return whether sim is complete or not
	 */
	@Override
	protected final boolean stepSimulation_Indiv(float modAmtMillis, float scaledMillisSinceLastFrame) {
		//move objects in visualization - ignored if not using vis (checked in sim)
		((Base_UISimulator) currSim).simStepVisualization(scaledMillisSinceLastFrame);
		return stepUISimulation_Indiv(modAmtMillis, scaledMillisSinceLastFrame);
	}
	/**
	 * Advance current simulation
	 * @param modAmtMillis is milliseconds elapsed since last frame
	 * @return whether sim is complete or not
	 */
	protected abstract boolean stepUISimulation_Indiv(float modAmtMillis, float scaledMillisSinceLastFrame);
	
}//Base_UISimExec
