package base_UI_Objects.windowUI.baseUI;

import java.util.HashMap;
import java.util.Map;

import base_UI_Objects.windowUI.base.myDispWindow;

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
	protected Map<Integer, Integer> intValues;
	/**
	 * map to hold UI-driven float values, using the UI object idx's as keys
	 */
	protected Map<Integer, Float> floatValues;
	/**
	 * map to hold UI-driven boolean values, using the UI object flags' idx's as keys 
	 */
	protected Map<Integer, Boolean> boolValues;	
	
	public base_UpdateFromUIData(myDispWindow _win) { win=_win;	initMaps();}
	public base_UpdateFromUIData(myDispWindow _win, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals, Map<Integer, Boolean> _bVals) {
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
		intValues = new HashMap<Integer, Integer>();
		floatValues = new HashMap<Integer, Float>(); 
		boolValues = new HashMap<Integer, Boolean>();
	}
	
	public final void setAllVals(base_UpdateFromUIData _otr) {
		setAllVals(_otr.intValues,_otr.floatValues,_otr.boolValues);		
	}
	
	public final void setAllVals(Map<Integer, Integer> _intValues, Map<Integer, Float> _floatValues,Map<Integer, Boolean> _boolValues) {
		if(_intValues!=null) {for (Map.Entry<Integer, Integer> entry : _intValues.entrySet()) {intValues.put(entry.getKey(), entry.getValue());}}
		if(_floatValues!=null) {for (Map.Entry<Integer, Float> entry : _floatValues.entrySet()) {floatValues.put(entry.getKey(), entry.getValue());}}
		if(_boolValues!=null) {for (Map.Entry<Integer, Boolean> entry : _boolValues.entrySet()) {boolValues.put(entry.getKey(), entry.getValue());}}
	}
	
	public final boolean compareIntValue(Integer idx, Integer value) {	return (intValues.get(idx) != null) && (intValues.get(idx).equals(value));	}
	public final boolean compareFloatValue(Integer idx, Float value) {	return (floatValues.get(idx) != null) && (floatValues.get(idx).equals(value));	}
	public final boolean compareBoolValue(Integer idx, Boolean value) {	return (boolValues.get(idx) != null) && (boolValues.get(idx).equals(value));	}
	
	public final void setIntValue(Integer idx, Integer value){	intValues.put(idx,value);  }
	public final void setFloatValue(Integer idx, Float value){	floatValues.put(idx,value);}
	public final void setBoolValue(Integer idx, Boolean value){	boolValues.put(idx,value);}
	
	
	protected <T extends Comparable<T>> boolean checkMapIsChanged(HashMap<Integer,Integer> idxsToIgnore, Map<Integer, T> thisMap, Map<Integer, T> thatMap ) {
		if(idxsToIgnore.size() == 0) {
			for (Map.Entry<Integer, T> entry : thisMap.entrySet()) {
				Integer key = entry.getKey();
				//ignore key if specified to ignore or if other map does not have value
				if (thatMap.get(key) == null){
					continue;
				}
				if (entry.getValue() != thatMap.get(key)) {return true;}
			}			
		} else {			
			for (Map.Entry<Integer, T> entry : thisMap.entrySet()) {
				Integer key = entry.getKey();
				//ignore key if specified to ignore or if other map does not have value
				if ((idxsToIgnore.get(key) != null) ||(thatMap.get(key) == null)){
					continue;
				}
				if (entry.getValue() != thatMap.get(key)) {return true;}
			}		
		}			
		return false;
	}//checkMapIsChanged
	
	/**
	 * Rebuild simulation if any simulator-dependent variables have changed. These are variables that are sent to the cuda kernel
	 * @param _otr
	 * @return
	 */
	protected boolean haveValuesChanged(base_UpdateFromUIData _otr, 
			HashMap<Integer,Integer> IntIdxsToIgnore, 
			HashMap<Integer,Integer> FloatIdxsToIgnore, 
			HashMap<Integer,Integer> BoolIdxsToIgnore) {	
		if (checkMapIsChanged(IntIdxsToIgnore, intValues, _otr.intValues)) {		return true;}
		if (checkMapIsChanged(FloatIdxsToIgnore, floatValues, _otr.floatValues)) {	return true;}
		if (checkMapIsChanged(BoolIdxsToIgnore, boolValues, _otr.boolValues)) {		return true;}		
		return false;
	}//haveValuesChanged
	
	
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
	 * Getters
	 */
	public final boolean getFlag(int idx) {return boolValues.get(idx);}
	public final int getIntValue(Integer idx, Integer value){	return intValues.get(idx);  }
	public final float getFloatValue(Integer idx, Float value){	return floatValues.get(idx);  }
	
	/**
	 * Updaters - these will update the owning window's data values as well
	 */
	public final void updateBoolValuee(int idx, boolean value) {
		setBoolValue(idx, value);
		win.updateBoolValFromExecCode(idx, value);
	}
	
	public final void updateIntValue(int idx, Integer value) {
		setIntValue(idx,value);
		win.updateIntValFromExecCode(idx, value);
	}
	
	public final void updateFloatValue(int idx, Float value) {
		setFloatValue(idx,value);
		win.updateFloatValFromExecCode(idx, value);
	}
	
	
	@Override
	public String toString() {
		String res = "Owning Window Name: "+win.name+" | Tracked values : "+intValues.size() +" Integers, " +floatValues.size() +" Floats, " +boolValues.size() + " Booleans\n";
		if (intValues.size() > 0) {
			res +="Int Values: (" +intValues.size() +")\n";
			for (Map.Entry<Integer, Integer> entry : intValues.entrySet()) {
				res += "\tKey : "+entry.getKey()+" | Value : "+entry.getValue()+"\n";
			}
		} else {		res+="No Integer values present/being tracked";	}
		if (floatValues.size() > 0) {
			res+="Float Values: (" +floatValues.size() +")\n";
			for (Map.Entry<Integer, Float> entry : floatValues.entrySet()) {
				res += "\tKey : "+entry.getKey()+" | Value : "+entry.getValue()+"\n";
			}
		} else {		res+="No Float values present/being tracked";	}
		if (boolValues.size() > 0) {	
			res+="Boolean Values: (" +boolValues.size() +")\n";
			for (Map.Entry<Integer, Boolean> entry : boolValues.entrySet()) {
				res += "\tKey : "+entry.getKey()+" | Value : "+entry.getValue()+"\n";
			}	
		} else {		res+="No Boolean values present/being tracked";	}
		
		return res;
	}
}//class base_UpdateFromUIData
