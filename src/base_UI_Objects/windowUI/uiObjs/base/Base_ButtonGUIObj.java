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
	 * Build a boolean/multi-state button
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _enumType The enum class this button uses. Passed as <enum type name>.class
	 * @param _initialState the initial state setting for this object
	 * @param _objType the type of UI object this is
	 * @param _flags any preset behavior flags
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public Base_ButtonGUIObj(int _objID, String _name, Class<E>_enumType, E _initialState, GUIObj_Type _objType, boolean[] _flags, int[] strkClr, int[] fillClr) {		
		super(_objID, _name, _objType, _flags, strkClr, fillClr);
		state = _initialState;
		initialState = state;
		enumType = _enumType;
	}//ctor
	
	@Override
	public void resetToInit() {
		state=initialState;
	}

	@Override
	protected void setValueFromString(String enumStr) {
		state = Enum.valueOf(enumType, enumStr);
	}

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