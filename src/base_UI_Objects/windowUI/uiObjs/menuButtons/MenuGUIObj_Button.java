package base_UI_Objects.windowUI.uiObjs.menuButtons;

import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Base class for UI objects that manage boolean/clickable multi-state data
 * 
 * Instancing class should also define enum that governs the states of this button.
 * @author John Turner
 *
 */
public class MenuGUIObj_Button extends Base_GUIObj {
	
	/**
	 * Current state value of this object;
	 */
	protected int state;
	
	/**
	 * Initial state value of this object, for reset;
	 */
	protected final int initialState;
	
	/**
	 * Total number of states this button supports - defined as the number of labels provided.
	 */
	protected final int numStates;

	/**
	 * List of labels to display for each state.
	 */
	protected final String[] stateLabels;
	
	/**
	 * Build a boolean/multi-state button
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _initialState the initial state setting for this object
	 * @param _flags any preset configuration flags
	 * @param _stateLabels list of labels for each state
	 */
	public MenuGUIObj_Button(int _objID, String _name, int _initialState, boolean[] _flags, String[] _stateLabels) {		
		super(_objID, _name, GUIObj_Type.Button, _flags);
		numStates = _stateLabels.length;
		//Restrict initial state
		state = (_initialState >= 0 ? (_initialState < numStates ? _initialState : numStates-1) : 0);
		initialState = state;
		stateLabels = new String[numStates];
		for(int i=0;i<numStates;++i) {	stateLabels[i]= _stateLabels[i];}
		
	}//ctor
	
	@Override
	public void resetToInit() {	state=initialState;}

	/**
	 * Make sure val adheres to specified bounds
	 * @param _val
	 * @return
	 */
	protected int forceBounds(int _val) {
		while (_val < 0) {_val += numStates;}
		if (_val >= numStates) {_val %= numStates;}
		return _val;
	}
	
	/**
	 * Set the value explicitly that we want to have for this object, subject to bounds.
	 * @param _newVal
	 * @return
	 */
	public final int setState(int _newVal){
		int oldVal = state;
		state = forceBounds(_newVal);	
		if (oldVal != state) {setIsDirty(true);}
		setLabel("");
		return state;
	}
	
	/**
	 * Modify this button state by passed mod value - either up 1 or down 1, cycling around if necessary
	 * @param mod either + or - 1.
	 * @return
	 */
	public final int modState(int mod) {	
		mod = (mod > 0 ? 1 : -1);		
		return setState((state + mod + numStates) % numStates);	}
	
	/**
	 * Set label to display for this button. 
	 * @param _unused ignored - value will be the string in stateLabels array corresponding to the current state
	 */
	@Override
	public final void setLabel(String _unused) {label = stateLabels[state];}
		
	/**
	 * Return this object's current label - update the label based on the current state
	 */
	@Override
	public String getLabel() {
		setLabel("");
		return label;
	}
	
	public final String[] getStateLabels() {		return stateLabels;	}
	
	@Override
	protected final void setValueFromString(String _val) {	state = Integer.parseInt(_val);}

	public final int getState() {return state;}
	
	
	@Override
	public String getValueAsString() {
		return "" + state;
	}

	@Override
	protected String[] getStrDataForVal() {
		String[] tmpRes = new String[1];
		tmpRes[0]="Label :"+getLabel() + " State : "+ state;
		return tmpRes;
	}

	@Override
	protected boolean checkUIObjectStatus_Indiv() {	return true;}

}//class Base_ButtonGUIObj