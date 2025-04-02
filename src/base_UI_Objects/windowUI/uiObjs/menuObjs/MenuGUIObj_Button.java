package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

public class MenuGUIObj_Button extends MenuGUIObj_List {
	
	/**
	 * Build a boolean/multi-state button as a variant of a listbox
	 * This button will have no label and instead should 
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _initialState the initial state setting for this object
	 * @param _flags any preset configuration flags
	 * @param _stateLabels list of labels for each state
	 */
	public MenuGUIObj_Button(int _objID, String _name, int _initialState, boolean[] _flags, String[] _stateLabels) {		
		super(_objID, _name, new double[] {0, _stateLabels.length, 1}, _initialState, GUIObj_Type.Button, _flags, _stateLabels);		
	}//ctor
	
	/**
	 * Return this button's state
	 * @return
	 */
	public final int getButtonState() {return this.getValueAsInt();}
	
	
	/**
	 * Make sure val adheres to specified bounds, looping around if necessary
	 * @param _val
	 * @return
	 */
	@Override
	protected double forceBounds(double _val) {
		int numStates = getNumStates();
		while(_val < 0) { _val += numStates;}
		_val %= numStates;
		return _val;
	}
	
	/**
	 * Return how many states this button supports
	 */
	public final int getNumStates() {return getNumEntries();}
	
	/**
	 * Get all the state labels for this button
	 * @return
	 */
	public final String[] getStateLabels() {return getListValues();}
	
	/**
	 * Dragging is disabled on buttons
	 */
	@Override
	public final double dragModVal(double _notUsed) {return getVal();}
	
	/**
	 * Modify this object by passed mod value, multiplied by scale. This is for a single click
	 * @param mod
	 * @param scale
	 * @return
	 */
	@Override
	public final double clickModVal(double mod, double scale) {
		double modVal = (mod *scale*modMult);
		return setVal(modValAssign(getVal() + (modVal > 0 ? 1 : -1)));
	}

}//class Base_ButtonGUIObj