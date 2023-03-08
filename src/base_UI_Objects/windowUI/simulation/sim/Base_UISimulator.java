package base_UI_Objects.windowUI.simulation.sim;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Utils_Objects.sim.Base_Simulator;
import base_Utils_Objects.simExec.Base_SimExec;

public abstract class Base_UISimulator extends Base_Simulator {

	public Base_UISimulator(Base_SimExec _exec, String _name) {
		super(_exec, _name);
	}

	/**
	 * animTimeMod is in seconds, time that has passed since last draw call
	 * @param pa
	 * @param animTimeMod
	 * @param win
	 */
	public abstract void drawMe(IRenderInterface pa, float animTimeMod, Base_DispWindow win);
	
	/**
	 * draw result information on right sidebar, if gui-based sim
	 * @param ri
	 * @param yOff
	 */
	public abstract void drawResultBar(IRenderInterface ri, float yOff);

}//class Base_UISimulator
