package base_UI_Objects.windowUI.uiObjs.base;

import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Base class for UI objects that manage numeric or numerically-backed data (like a list/drop down)
 * @author John Turner
 *
 */
public abstract class Base_NumericGUIObj extends Base_GUIObj {	
	/**
	 * String format to display value
	 */
	protected String formatStr;	
	/**
	 * Original string format
	 */
	private final String origFormatStr;
	/**
	 * Initial values for min, max, mod and val
	 */
	protected double[] initVals;	
	/**
	 * Value for this numeric object
	 */
	private double val;
	/**
	 * Min and max values allowed for this numeric object
	 */
	private double minVal, maxVal;
	/**
	 * Multiplier for modification
	 */
	protected double modMult;
	
	/**
	 * Build a numeric value-based UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and maximum values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _objType the type of UI object this is
	 * @param _flags any preset behavior flags
	 */
	public Base_NumericGUIObj(int _objID, String _name, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags) {
		super(_objID, _name, _objType, _flags);
		
		minVal=_minMaxMod[0]; maxVal = _minMaxMod[1]; setNewMod(_minMaxMod[2]);
		origFormatStr = formatStr;
		val = _initVal;
		initVals = new double[4];
		for(int i=0;i<_minMaxMod.length;++i) {initVals[i]=_minMaxMod[i];}
		initVals[3] = _initVal;	
	}//ctor

	/**
	 * Return this object to its initial value
	 */
	public final void returnToInitVal() {setVal(initVals[3]);}
	
	/**
	 * Reset this UI component to its initialization values
	 */
	@Override
	public final void resetToInit() {		
		setNewMin(initVals[0]);
		setNewMax(initVals[1]);
		setNewMod(initVals[2]);
		returnToInitVal();
		label = origLabel;
		formatStr = origFormatStr;
		resetToInit_Indiv();
	}
	/**
	 * Instance-specific reset - most instance classes have nothing to do here. Override for classes that require it.
	 */
	protected void resetToInit_Indiv() {}
	/**
	 * 
	 * @return
	 */
	public final double getVal(){return val;}
	/**
	 * 
	 * @return
	 */
	public final double getMinVal(){return minVal;}
	/**
	 * 
	 * @return
	 */
	public final double getMaxVal(){return maxVal;}
	/**
	 * 
	 * @return
	 */
	public final double getModStep(){return modMult;}	
	
	public final double getMinMaxDiff() {return maxVal - minVal;}
	
	/**
	 * Make sure val adheres to specified bounds
	 * @param _val
	 * @return
	 */
	protected double forceBounds(double _val) {
		if (_val < minVal) {return minVal;}
		if (_val > maxVal) {return maxVal;}
		return _val;
	}
	
	/**
	 * Set new maximum value for this object, which will also force current value to adhere to bounds.
	 * NOTE: Does not currently verify that new max is > current min. Don't be stupid.
	 * @param _newval
	 */
	public final void setNewMax(double _newval){
		double oldVal = val;
		maxVal = _newval;
		val = forceBounds(val);	
		if (oldVal != val) {setIsDirty(true);}		
	}
	
	/**
	 * Set a new minimum bound for this object, which will also force current value to adhere to bounds.
	 * NOTE: Does not currently verify that new min is < current max. Don't be stupid.
	 * @param _newval
	 */
	public final void setNewMin(double _newval){	
		double oldVal = val;
		minVal = _newval;
		val = forceBounds(val);		
		if (oldVal != val) {setIsDirty(true);}		
	}
	/**
	 * Set a new modifier value `modMult` to use for this object, and use the modifier to
	 * derive the `formatStr` used to display this object's data.
	 * @param _newval
	 */
	public abstract void setNewMod(double _newval);
	
	/**
	 * Set the value explicitly that we want to have for this object, subject to bounds.
	 * @param _newVal
	 * @return
	 */
	public final double setVal(double _newVal){
		double oldVal = val;
		val = forceBounds(_newVal);	
		if (oldVal != val) {setIsDirty(true);}		
		return val;
	}	
	/**
	 * Modify this object by passed mod value, scaled by modMult. This is called during drag
	 * @param mod
	 * @return
	 */
	public double dragModVal(double mod) {return setVal(modValAssign(val + (mod*modMult)));}
	
	/**
	 * Modify this object by passed mod value, multiplied by scale. This is for a single click
	 * @param mod
	 * @param scale
	 * @return
	 */
	public double clickModVal(double mod, double scale) {
		return setVal(modValAssign(val + (mod *scale*modMult)));
	}
	
	/**
	 * Object-specific handling of modified value. (int/list objects will round, display-only will force to be original val)
	 * @param _val
	 * @return
	 */
	protected abstract double modValAssign(double _val);
	
	/**
	 * set new display text for this UI object - doesn't change name
	 * @param _str
	 */
	@Override
	public final void setLabel(String _str) {	label = (_str.length() > 0 ? _str + " : " : "");	}
	
	/**
	 * Set this UI object's value from a string
	 * @param str
	 */
	@Override
	protected final void setValueFromString(String str) {
		double uiVal = Double.parseDouble(str);	
		setVal(uiVal);
	}
		
	/**
	 * Get this object's value as an int
	 * @return
	 */
	public final int getValueAsInt(){return (int)(val) ;}
	/**
	 * Get this object's value as a float
	 * @return
	 */
	public final float getValueAsFloat(){return (float)( val);}
	
	/**
	 * Get this UI object's value as a string with appropriate format
	 * @return
	 */
	@Override
	public final String getValueAsString() {return getValueAsString(val);}
	
	/**
	 * Get this UI object's value as a string - overridden by classes that do not use val directly
	 * @return
	 */
	protected String getValueAsString(double _val) {	return String.format(formatStr,_val);}


	@Override
	protected final boolean checkUIObjectStatus_Indiv() {
		// TODO add any object instance-specific checks that might be required here
		return true;
	}
	
	/**
	 * Get string data array representing the value this UI object holds
	 * @return
	 */
	@Override
	protected String[] getStrDataForVal() {
		String[] tmpRes = new String[2];
		tmpRes[0] = "Value : "+ getValueAsString() +" Max Val : " + getValueAsString(maxVal) 
		+ " Min Val : " + getValueAsString(minVal)+ " Mod multiplier : " +getValueAsString(modMult);
		tmpRes[1] = "Init Value : "+ getValueAsString(initVals[3]) +"|Init Max Val : "
		+ getValueAsString(initVals[1]) + "|Init Min Val : " + getValueAsString(initVals[0])
		+ "|Init Mod : " + getValueAsString(initVals[2]);
		return tmpRes;
	}
	
}//class Base_NumericGUIObj
