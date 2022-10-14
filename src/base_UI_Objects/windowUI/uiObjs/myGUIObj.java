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
	private final int objID;							//id in owning window
	private myVector start, end;				//x,y coords of start corner, end corner (z==0) for clickable region
	public final String name;
	private String dispText;

	private double val;
	private double minVal, maxVal;
	
	private final GUIObj_Type objType;
	
	private int[] uiFlags;
	private static final int 
			debugIDX 		= 0,
			showIDX			= 1,				//show this component
			//config flags
			usedByWinsIDX	= 2, 
			updateWhileModIDX = 3,
			objValChangedIDX = 4;				//object value is dirty/clean
	public static final int numFlags = 5;			
	
	private int[] _cVal;
	private double modMult,						//multiplier for mod value
					xOff,yOff;						//Offset value
	private float[] initDrawTrans, boxDrawTrans;
	private int[] bxclr;
	
	private final float[] boxDim = new float[] {-2.5f, -2.5f, 5.0f, 5.0f};
	
	private String[] listVals = new String[] {"None"};
	
	public myGUIObj(IRenderInterface _p, int _objID, String _name, myVector _start, myVector _end, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		p=_p;
		objID = _objID;
		ID = GUIObjID++;
		name = _name;
		xOff = _off[0];
		yOff = _off[1];
		//dispText = new String("UI Obj "+ID+" : "+name + " : ");
		dispText = new String(""+name + " : ");
		start = new myVector(_start); end = new myVector(_end);
		minVal=_minMaxMod[0]; maxVal = _minMaxMod[1]; modMult = _minMaxMod[2];
		val = _initVal;
		objType = _objType;
		initFlags();
		int numToInit = (_flags.length < numFlags-2 ? _flags.length : numFlags-2);
		for(int i =0; i<numToInit;++i){ 	setFlags(i+2,_flags[i]);	}	
		
		_cVal = new int[] {0,0,0};
		bxclr = new int[]{ThreadLocalRandom.current().nextInt(256),ThreadLocalRandom.current().nextInt(256),ThreadLocalRandom.current().nextInt(256),255};
		
		initDrawTrans= new float[]{(float)(start.x + xOff), (float)(start.y + yOff)};
		boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(-yOff*.25f)};		
	}	
	public myGUIObj(IRenderInterface _p, int _objID, String _name,double _xst, double _yst, double _xend, double _yend, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _Off) {
		this(_p,_objID,_name,new myVector(_xst,_yst,0), new myVector(_xend,_yend,0), _minMaxMod, _initVal, _objType, _flags, _Off);	
	}
	
	public void initFlags(){			uiFlags = new int[1 + numFlags/32]; for(int i = 0; i<numFlags; ++i){setFlags(i,false);}	}
	private boolean getFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiFlags[idx/32] & bitLoc) == bitLoc;}	
	private void setFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiFlags[flIDX] = (val ?  uiFlags[flIDX] | mask : uiFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case debugIDX 			:{break;}
		case showIDX			:{break;}	//show this component
		case usedByWinsIDX		:{break;}
		case updateWhileModIDX	:{break;}
		case objValChangedIDX 	:{break;}		
		}
	}//setFlag	
	
	public String getName(){return name;}
	public double getVal(){return val;}	
	public double getMinVal(){return minVal;}
	public double getMaxVal(){return maxVal;}
	public double getModStep(){return modMult;}	
	
	private void setIsDirty(boolean isDirty) {setFlags(objValChangedIDX, isDirty);}
		
	//Make sure val adheres to specified bounds
	private double forceBounds(double _val) {
		if (_val < minVal) {return minVal;}
		if (_val > maxVal) {return maxVal;}
		return _val;
	}
	public void setNewMax(double _newval){
		double oldVal = val;
		maxVal = _newval;
		val = forceBounds(val);	
		if (oldVal != val) {setIsDirty(true);}		
	}
	public void setNewMin(double _newval){	
		double oldVal = val;
		minVal = _newval;
		val = forceBounds(val);		
		if (oldVal != val) {setIsDirty(true);}		
	}
	public void setNewMod(double _newval){	
		if (_newval > (maxVal-minVal)) {
			_newval = (maxVal-minVal);
		}
		modMult = _newval;	
	}
	public double setVal(double _newVal){
		double oldVal = val;
		val = forceBounds(_newVal);	
		if (oldVal != val) {setIsDirty(true);}		
		return val;
	}	
	public double modVal(double mod){
		double oldVal = val;
		val += (mod*modMult);
		if(objType == GUIObj_Type.IntVal){val = Math.round(val);}
		val = forceBounds(val);
		if (oldVal != val) {setIsDirty(true);}		
		return val;		
	}

	public final boolean shouldUpdateWin(boolean isRelease) {
		boolean isDirty = getFlags(objValChangedIDX);
		//only clear once processed
		if (isRelease){	setIsDirty(false);	}
		return isDirty && ((isRelease || getFlags(updateWhileModIDX)) && getFlags(usedByWinsIDX));
	}
	
	public final int valAsInt(){return (int)(val) ;}
	public final float valAsFloat(){return (float)( val);}
	
	/**
	 * Set this UI object's value based on string tokens from file
	 * @param toks
	 */
	public final void setValFromStrTokens(String[] toks) {
		//String name = toks[1];
		//objtype == toks[2]
		double uiVal = Double.parseDouble(toks[3].split("\\s")[1].trim());	
		setVal(uiVal);
		for(int i =0;i<myGUIObj.numFlags; ++i){
			setFlags(i, Boolean.parseBoolean(toks[4].split("\\s")[i].trim()));
		}	
	}//setValFromStrTokens
	
	/**
	 * Builds a string to save the value from this UI Object
	 * @param idx
	 * @return
	 */
	public String getStrFromUIObj(int idx){
		StringBuilder sb = new StringBuilder(400);
		sb.append("ui_idx: ");
		sb.append(idx);
		sb.append(" |name: ");
		sb.append(name);
		sb.append(" |type: ");
		sb.append(objType.getVal());
		sb.append(" |value: ");
		sb.append(getVal());
		sb.append(" |flags: ");
		for(int i =0;i<myGUIObj.numFlags; ++i){
			sb.append(getFlags(i) ? " true" : " false");
		}
		return sb.toString().trim();		
		
	}//getStrFromUIObj
	
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
			if(objType == GUIObj_Type.FloatVal){		p.showText(dispText + String.format("%.5f",val), 0,0);}
			else{
				//disp ints and list values
				//String resStr = (objType == GUIObj_Type.ListVal) ?  win.getUIListValStr(objID, (int)val) : String.format("%.0f",val);
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
		if(objType == GUIObj_Type.ListVal) {	return listVals[(idx % listVals.length)];} 
		else {							return String.format("%.0f",val);		}
	}
	/**
	 * Set this list object's list of values
	 * @param _vals
	 * @return returns current val cast to int as idx
	 */
	public final int setListVals(String[] _vals) {
		if((null == _vals) || (_vals.length == 0)) {			
			String dfltEntry = (objType == GUIObj_Type.ListVal) ? "List Not Initialized!" : "None";
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
	
	public final GUIObj_Type getObjType() {return objType;}
	
	public final int getIDXofStringInArray(String tok) {for(int i=0;i<listVals.length;++i) {if(listVals[i].trim().equals(tok.trim())) {return i;}}return -1;}
	
	public String[] getStrData(){
		ArrayList<String> tmpRes = new ArrayList<String>();
		tmpRes.add("ID : "+ ID+" Obj ID : " + objID  + " Name : "+ name + " distText : " + dispText);
		tmpRes.add("Start loc : "+ start + " End loc : "+ end + " Treat as Int  : " + (objType == GUIObj_Type.IntVal));
		tmpRes.add("Value : "+ val +" Max Val : "+ maxVal + " Min Val : " + minVal+ " Mod multiplier : " + modMult);
		return tmpRes.toArray(new String[0]);
	}
}//class myGUIObj
