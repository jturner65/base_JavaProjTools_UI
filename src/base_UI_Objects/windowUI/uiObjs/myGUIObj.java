package base_UI_Objects.windowUI.uiObjs;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myVector;

//object on menu that can be modified via mouse input
public class myGUIObj {
	public int ID;
	//static variables - put obj constructor counters here
	private static int GUIObjID = 0;										//counter variable for gui objs

	private IRenderInterface p;
	private int winID;							//id in owning window
	private myVector start, end;				//x,y coords of start corner, end corner (z==0) for clickable region
	public final String name;
	private String dispText;

	private double val;
	private double minVal, maxVal;
	
	private int[] uiFlags;
	private static final int 
			debugIDX 		= 0,
			showIDX			= 1,				//show this component
			//config flags
			treatAsIntIDX	= 2,
			hasListValsIDX	= 3,
			usedByWinsIDX	= 4, 
			updateWhileModIDX = 5;
	public static final int numFlags = 6;			
	
	private int[] _cVal;
	private double modMult,						//multiplier for mod value
					xOff,yOff;						//Offset value
	private float[] initDrawTrans, boxDrawTrans;
	private int[] bxclr;
	
	private final float[] boxDim = new float[] {-2.5f, -2.5f, 5.0f, 5.0f};
	
	private String[] listVals = new String[] {"None"};
	
	public myGUIObj(IRenderInterface _p, int _winID, String _name, myVector _start, myVector _end, double[] _minMaxMod, double _initVal, boolean[] _flags, double[] _off) {
		p=_p;
		winID = _winID;
		ID = GUIObjID++;
		name = _name;
		xOff = _off[0];
		yOff = _off[1];
		//dispText = new String("UI Obj "+ID+" : "+name + " : ");
		dispText = new String(""+name + " : ");
		start = new myVector(_start); end = new myVector(_end);
		minVal=_minMaxMod[0]; maxVal = _minMaxMod[1]; modMult = _minMaxMod[2];
		val = _initVal;
		initFlags();
		int numToInit = (_flags.length < numFlags-2 ? _flags.length : numFlags-2);
		for(int i =0; i<numToInit;++i){ 	setFlags(i+2,_flags[i]);	}
		_cVal = new int[] {0,0,0};
		bxclr = new int[]{ThreadLocalRandom.current().nextInt(256),ThreadLocalRandom.current().nextInt(256),ThreadLocalRandom.current().nextInt(256),255};
		
		initDrawTrans= new float[]{(float)(start.x + xOff), (float)(start.y + yOff)};
		boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(-yOff*.25f)};		
	}	
	public myGUIObj(IRenderInterface _p, int _winID, String _name,double _xst, double _yst, double _xend, double _yend, double[] _minMaxMod, double _initVal, boolean[] _flags, double[] _Off) {
		this(_p,_winID,_name,new myVector(_xst,_yst,0), new myVector(_xend,_yend,0), _minMaxMod, _initVal, _flags, _Off);	
	}
	
	public void initFlags(){			uiFlags = new int[1 + numFlags/32]; for(int i = 0; i<numFlags; ++i){setFlags(i,false);}	}
	public boolean getFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiFlags[idx/32] & bitLoc) == bitLoc;}	
	public void setFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiFlags[flIDX] = (val ?  uiFlags[flIDX] | mask : uiFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case debugIDX 			:{break;}
		case showIDX			:{break;}	//show this component
		case treatAsIntIDX		:{break;}
		case hasListValsIDX		:{break;}
		case usedByWinsIDX		:{break;}
		case updateWhileModIDX	:{break;}
		
		}
	}//setFlag	
	
	public String getName() {return name;}
	public double getVal(){return val;}	
	public double getMinVal() {return minVal;}
	public double getMaxVal() {return maxVal;}
	
	//Make sure val adheres to specified bounds
	private double forceBounds(double _val) {
		if (_val <= minVal) {return minVal;}
		if (_val >= maxVal) {return maxVal;}
		return _val;
	}
	public void setNewMax(double _newval){	
		maxVal = _newval;
		val = forceBounds(val);		
	}
	public void setNewMin(double _newval){	
		minVal = _newval;
		val = forceBounds(val);		
	}
	public void setNewMod(double _newval){	
		if (_newval > (maxVal-minVal)) {
			_newval = (maxVal-minVal);
		}
		modMult = _newval;	
	}
	public double setVal(double _newVal){
		val = forceBounds(_newVal);		
		return val;
	}	
	public double modVal(double mod){
		val += (mod*modMult);
		if(getFlags(treatAsIntIDX)){val = Math.round(val);}
		val = forceBounds(val);
		return val;		
	}

	public final boolean shouldUpdateWin(boolean isRelease) {
		return ((isRelease || getFlags(updateWhileModIDX)) && getFlags(usedByWinsIDX));
	}
	
	public final int valAsInt(){return (int)(val) ;}
	public final float valAsFloat(){return (float)( val);}
	
	public final boolean checkIn(float _clkx, float _clky){return (_clkx > start.x)&&(_clkx < end.x)&&(_clky > start.y)&&(_clky < end.y);}
	public final void draw(){
		p.pushMatState();
			p.translate(initDrawTrans[0],initDrawTrans[1],0);
			p.setFill(_cVal,255);
			p.setStroke(_cVal,255);
			p.pushMatState();
				p.noStroke();
				p.setFill(bxclr,bxclr[3]);
				p.translate(boxDrawTrans[0],boxDrawTrans[1],0);
				p.drawRect(boxDim);
			p.popMatState();
			if(!getFlags(treatAsIntIDX)){		p.showText(dispText + String.format("%.5f",val), 0,0);}
			else{
				//String resStr = getFlags(hasListValsIDX) ?  win.getUIListValStr(winID, (int)val) : String.format("%.0f",val);
				p.showText(dispText + getListValStr((int)val), 0,0);
			}
		p.popMatState();
	}
	/**
	 * return the string representation corresponding to the passed index in the list of this object's values, if any exist
	 * @param idx index in list of value to retrieve
	 * @return
	 */
	public final String getListValStr(int idx) {
		if(getFlags(hasListValsIDX)) {	return listVals[(idx % listVals.length)];} 
		else {							return String.format("%.0f",val);		}
	}
	/**
	 * Set this list object's list of values
	 * @param _vals
	 * @return returns current val cast to int as idx
	 */
	public final int setListVals(String[] _vals) {
		if((null == _vals) || (_vals.length == 0)) {			
			String dfltEntry = (getFlags(hasListValsIDX)) ? "List Not Initialized!" : "None";
			listVals = new String[] {dfltEntry};			
		} 
		else {
			listVals = new String[_vals.length];
			System.arraycopy(_vals, 0, listVals, 0, listVals.length);			
		}
		double curVal = getVal();
		setNewMax(listVals.length-1);
		curVal = setVal(curVal);
		return (int) curVal;
		
	}//setListVals
	
	/**
	 * set new display text for this UI object - doesn't change name
	 * @param _str
	 */
	public final void setNewDispText(String _str) {
		dispText = new String(""+_str + " : ");		
	}
	
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
	
	public final int getIDXofStringInArray(String tok) {for(int i=0;i<listVals.length;++i) {if(listVals[i].trim().equals(tok.trim())) {return i;}}return -1;}
	
	public String[] getStrData(){
		ArrayList<String> tmpRes = new ArrayList<String>();
		tmpRes.add("ID : "+ ID+" Win ID : " + winID  + " Name : "+ name + " distText : " + dispText);
		tmpRes.add("Start loc : "+ start + " End loc : "+ end + " Treat as Int  : " + uiFlags[treatAsIntIDX]);
		tmpRes.add("Value : "+ val +" Max Val : "+ maxVal + " Min Val : " + minVal+ " Mod multiplier : " + modMult);
		return tmpRes.toArray(new String[0]);
	}
}//class myGUIObj