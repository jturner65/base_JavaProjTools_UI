package base_UI_Objects.windowUI.uiObjs;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.myGUIObj;

public class myGUIObj_List extends myGUIObj {
	
	protected String[] listVals = new String[] {"None"};

	public myGUIObj_List(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, double[] _minMaxMod, double _initVal, boolean[] _flags, double[] _off) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _minMaxMod, _initVal, GUIObj_Type.ListVal, _flags, _off);
	}
	
	@Override
	public final double modVal(double mod){
		double oldVal = val;
		val += (mod*modMult);
		val = Math.round(val);
		val = forceBounds(val);
		if (oldVal != val) {setIsDirty(true);}		
		return val;		
	}
	
	/**
	 * return the string representation corresponding to the passed index in the list of this object's values, if any exist
	 * @param idx index in list of value to retrieve
	 * @return
	 */
	public final String getListValStr(int idx) {
		return listVals[(idx % listVals.length)];
	}// getListValStr

	@Override
	protected void _drawIndiv() {
		p.showText(dispText + listVals[(((int)val) % listVals.length)], 0,0);		
	}
	
	
	/**
	 * Set this list object's list of values
	 * @param _vals
	 * @return returns current val cast to int as idx
	 */
	public final int setListVals(String[] _vals) {
		if((null == _vals) || (_vals.length == 0)) {	listVals = new String[] {"List Not Initialized!"};	} 
		else {
			listVals = new String[_vals.length];
			System.arraycopy(_vals, 0, listVals, 0, listVals.length);			
		}
		double curVal = getVal();
		setNewMax(listVals.length-1);
		curVal = setVal(curVal);
		return (int) curVal;
		
	}//setListVals
	
	public final int getIDXofStringInArray(String tok) {
		for(int i=0;i<listVals.length;++i) {
			if(listVals[i].trim().equals(tok.trim())) {return i;}
		}return -1;
	}//getIDXofStringInArray
	
	
	/**
	 * set list to display passed token, if it exists, otherwise return -1
	 * @param tok string in list to display
	 * @return ara [idx of string in list, otherwise -1, 0 if ok, 1 if bad]
	 */
	public final int[] setValInList(String tok) {
		int idx = getIDXofStringInArray(tok);
		if(idx >=0){		return new int[] {(int) setVal(idx), 0};}
		return new int[] {idx, 1};
	}


}//class myGUIObj_List
