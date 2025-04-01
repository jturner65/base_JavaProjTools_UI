package base_UI_Objects.windowUI.uiObjs.base;

import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Base class for UI objects that manage boolean/clickable multi-state data
 * 
 * Instancing class should also define enum that governs the states of this button.
 * @author John Turner
 *
 */
public abstract class Base_ButtonGUIObj<E extends Enum<E>> extends Base_GUIObj {
	
	/**
	 * Current enum value of this object;
	 */
	protected E state;
	
	/**
	 * Initial enum of this object, for reset;
	 */
	protected final E initialState;
	/**
	 * The type of the num governing the value represented by this object
	 */
	protected Class<E> enumType;

	/**
	 * List of labels to display for each enum state.
	 */
	protected final String[] stateLabels;
	
	/**
	 * Build a boolean/multi-state button
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _enumType The enum class this button uses. Passed as <enum type name>.class
	 * @param _initialState the initial state setting for this object
	 * @param _flags any preset behavior flags
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public Base_ButtonGUIObj(int _objID, String _name, Class<E>_enumType, E _initialState, boolean[] _flags, String[] _stateLabels) {		
		super(_objID, _name, GUIObj_Type.Button, _flags);
		state = _initialState;
		initialState = state;
		enumType = _enumType;
		stateLabels = _stateLabels;
	}//ctor
	
	@Override
	public void resetToInit() {	state=initialState;}

	/**
	 * Set label to display for this button. 
	 * @param _str ignored - value will be the string in stateLabels array corresponding to the current state
	 */
	@Override
	public final void setLabel(String _str) {label = stateLabels[state.ordinal()];}
		
	/**
	 * Return this object's label - update the label based on the current state
	 */
	@Override
	public String getLabel() {
		setLabel("");
		return label;
	}

	public final String[] getStateLabels() {		return stateLabels;	}
	
	@Override
	protected final void setValueFromString(String enumStr) {
		state = Enum.valueOf(enumType, enumStr);
	}

	public final int getStateOrdinal() {	return state.ordinal();}
	
	
	
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

}//class Base_ButtonGUIObj