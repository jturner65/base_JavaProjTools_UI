package base_UI_Objects.windowUI.simulation.uiData;

import java.util.Map;

import base_UI_Objects.windowUI.simulation.simExec.Base_UISimExec;
import base_Utils_Objects.sim.Base_SimDataAdapter;

/**
 * Base class to communicate parameters between sim exec and simulation
 * @author John Turner
 *
 */
public abstract class Base_UISimDataAdapter extends Base_SimDataAdapter {

	public Base_UISimDataAdapter(Base_UISimExec _simExec) {
		super(_simExec);
	}

	public Base_UISimDataAdapter(Base_UISimExec _simExec, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals,
			Map<Integer, Boolean> _bVals) {
		super(_simExec, _iVals, _fVals, _bVals);
	}

	public Base_UISimDataAdapter(Base_UISimDataAdapter _otr) {
		super(_otr);
	}

	/**
	 * Implementation-specific set draw vis idx to be val
	 * @param val
	 */
	@Override
	public final boolean checkAndSetSimDrawVis(boolean val) {
		return checkAndSetBoolValue(Base_UISimExec.drawVisIDX, val);		
	}
	@Override
	public final boolean getSimDrawVis() {
		return getBoolValue(Base_UISimExec.drawVisIDX);
	}
	
}//class Base_UISimDataAdapter
