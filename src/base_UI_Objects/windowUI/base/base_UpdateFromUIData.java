package base_UI_Objects.windowUI.base;

import java.util.TreeMap;

/**
 * structure holding UI-derived/modified data used to update execution code
 * @author john
 */

public abstract class base_UpdateFromUIData {
	/**
	 * Owning UI Window
	 */
	protected myDispWindow win;
	
	/**
	 * map to hold UI-driven int values, using the UI object idx's as keys
	 */
	protected TreeMap<Integer, Integer> intValues;
	/**
	 * map to hold UI-driven float values, using the UI object idx's as keys
	 */
	protected TreeMap<Integer, Float> floatValues;
	/**
	 * map to hold UI-driven boolean values, using the UI object flags' idx's as keys 
	 */
	protected TreeMap<Integer, Boolean> boolValues;

	public base_UpdateFromUIData(myDispWindow _win) { win=_win;	initMaps();}
	public base_UpdateFromUIData(myDispWindow _win, TreeMap<Integer, Integer> _iVals, TreeMap<Integer, Float> _fVals,TreeMap<Integer, Boolean> _bVals) {
		win=_win;
		initMaps();
		setAllVals(_iVals, _fVals, _bVals);
	}
	
	public base_UpdateFromUIData(base_UpdateFromUIData _otr) {
		win=_otr.win;
		initMaps();
		setAllVals(_otr);
	}
	
	protected final void initMaps() {
		intValues = new TreeMap<Integer, Integer>();
		floatValues = new TreeMap<Integer, Float>(); 
		boolValues = new TreeMap<Integer, Boolean>();
	}
	
	public final void setAllVals(base_UpdateFromUIData _otr) {
		setAllVals(_otr.intValues,_otr.floatValues,_otr.boolValues);		
	}
	
	public final void setAllVals(TreeMap<Integer, Integer> _intValues, TreeMap<Integer, Float> _floatValues,TreeMap<Integer, Boolean> _boolValues) {
		if(_intValues!=null) {for(Integer key : _intValues.keySet()) {intValues.put(key, _intValues.get(key));}}
		if(_floatValues!=null) {for(Integer key : _floatValues.keySet()) {floatValues.put(key, _floatValues.get(key));}}
		if(_boolValues!=null) {for(Integer key : _boolValues.keySet()) {boolValues.put(key, _boolValues.get(key));}}
	}
	
	public final boolean compareIntValue(Integer idx, Integer value) {	return (intValues.get(idx) != null) && (intValues.get(idx).equals(value));	}
	public final boolean compareFloatValue(Integer idx, Float value) {	return (floatValues.get(idx) != null) && (floatValues.get(idx).equals(value));	}
	public final boolean compareBoolValue(Integer idx, Boolean value) {	return (boolValues.get(idx) != null) && (boolValues.get(idx).equals(value));	}
	
	public final void setIntValue(Integer idx, Integer value){	intValues.put(idx,value);  }
	public final void setFloatValue(Integer idx, Float value){	floatValues.put(idx,value);}
	public final void setBoolValue(Integer idx, Boolean value){	boolValues.put(idx,value);}
	
	/**
	 * this will check if bool value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 * @return whether new value was set
	 */	
	public final boolean checkAndSetBoolValue(int idx, boolean value) {if(!compareBoolValue(idx, value)) {boolValues.put(idx,value); return true;}return false;}
	/**
	 * this will check if int value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 * @return whether new value was set
	 */	
	public final boolean checkAndSetIntVal(int idx, int value) {if(!compareIntValue(idx, value)) {intValues.put(idx,value); return true;}return false;}
	/**
	 * this will check if float value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 * @return whether new value was set
	 */	
	public final boolean checkAndSetFloatVal(int idx, float value) {if(!compareFloatValue(idx, value)) {floatValues.put(idx,value);return true;}return false;}
	
	
	
	/**
	 * accessors
	 */
	public final boolean getFlags(int idx) {return boolValues.get(idx);}
	public final int getIntValue(Integer idx, Integer value){	return intValues.get(idx);  }
	public final float getFloatValue(Integer idx, Float value){	return floatValues.get(idx);  }
	

}//class base_UpdateFromUIData
