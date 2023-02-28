/**
 * 
 */
package base_UI_Objects.windowUI.uiObjs.base;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;

/**
 * Base class for UI objects that manage numeric or numerically-backed data (like a list/drop down)
 * @author John Turner
 *
 */
public abstract class Base_NumericGUIObj extends Base_GUIObj {	

	/**
	 * Initial values for min, max, mod and val
	 */
	protected double[] initVals;	
	/**
	 * Multiplier for modification
	 */
	protected double modMult;
	/**
	 * Value for this numeric object
	 */
	protected double val;
	/**
	 * Min and max values allowed for this numeric object
	 */
	protected double minVal, maxVal;

	/**
	 * @param _p
	 * @param _objID
	 * @param _name
	 * @param _xst
	 * @param _yst
	 * @param _xend
	 * @param _yend
	 * @param _minMaxMod
	 * @param _initVal
	 * @param _objType
	 * @param _flags
	 * @param _off
	 */
	public Base_NumericGUIObj(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _objType, _flags);
		
		minVal=_minMaxMod[0]; maxVal = _minMaxMod[1]; modMult = _minMaxMod[2];
		val = _initVal;
		initVals = new double[4];
		for(int i=0;i<_minMaxMod.length;++i) {initVals[i]=_minMaxMod[i];}
		initVals[3] = _initVal;	
	}

	
	public final double getVal(){return val;}	
	public final double getMinVal(){return minVal;}
	public final double getMaxVal(){return maxVal;}
	public final double getModStep(){return modMult;}	

		
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
	 * Set a new modifier value to use for this object
	 * @param _newval
	 */
	public final void setNewMod(double _newval){	
		if (_newval > (maxVal-minVal)) {
			_newval = (maxVal-minVal);
		}
		modMult = _newval;	
	}
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
	 * Modify this object by passed mod value, scaled by modMult
	 * @param mod
	 * @return
	 */
	public abstract double modVal(double mod);

	/**
	 * Reset this UI component to its initialization values
	 */
	@Override
	public final void resetToInit() {		
		setNewMin(initVals[0]);
		setNewMax(initVals[1]);
		setNewMod(initVals[2]);
		setVal(initVals[3]);
	}
	
	/**
	 * Set this UI object's value from a string
	 * @param str
	 */
	protected final void setValueFromString(String str) {
		double uiVal = Double.parseDouble(str);	
		setVal(uiVal);
	}
	
	
	/**
	 * Get this object's value as an int
	 * @return
	 */
	public final int valAsInt(){return (int)(val) ;}
	/**
	 * Get this object's value as a float
	 * @return
	 */
	public final float valAsFloat(){return (float)( val);}
	
	/**
	 * Get this UI object's value as a string
	 * @return
	 */
	protected final String getValueAsString() {
		return ""+ getVal();
	}
	
	/**
	 * Get string data aarray representing the value this UI object holds
	 * @return
	 */
	protected final String[] getStrDataForVal() {
		String[] tmpRes = new String[2];
		tmpRes[0] = "Value : "+ val +" Max Val : "+ maxVal + " Min Val : " + minVal+ " Mod multiplier : " + modMult;
		tmpRes[1] = "Init Value : "+ initVals[3] +"|Init Max Val : "+ initVals[1] + "|Init Min Val : " + initVals[0]+ "|Init Mod : " + initVals[2];
		return tmpRes;
	}
	
}//class Base_NumericGUIObj
