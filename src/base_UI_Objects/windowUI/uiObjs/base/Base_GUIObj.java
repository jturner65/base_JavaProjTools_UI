package base_UI_Objects.windowUI.uiObjs.base;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myVector;

//object on menu that can be modified via mouse input
public abstract class Base_GUIObj {
	public final int ID;
	//static variables - put obj constructor counters here
	private static int GUIObjID = 0;										//counter variable for gui objs

	protected IRenderInterface p;
	protected final int objID;							//id in owning window
	protected myVector start, end;				//x,y coords of start corner, end corner (z==0) for clickable region
	protected final String name;
	protected String dispText;

	protected double val;
	protected double minVal, maxVal;
	
	protected final GUIObj_Type objType;
	
	protected int[] uiFlags;
	protected static final int 
			debugIDX 		= 0,
			showIDX			= 1,				//show this component
			valChangedIDX   = 2,				//object value is dirty/clean
			//config flags
			usedByWinsIDX	= 3, 
			updateWhileModIDX = 4,
			explicitUIDataUpdateIDX = 5;		//does not update UIDataUpdate structure on changes - must be explicitly sent to consumers 
	public static final int numFlags = 6;			
	protected static final int numPrivFlags = 3;	// # of internal state booleans
	protected int[] _cVal;
	protected double modMult,						//multiplier for mod value
					xOff,yOff;						//Offset value
	protected float[] initDrawTrans, boxDrawTrans;
	protected int[] bxclr;
	
	protected final float[] boxDim = new float[] {-2.5f, -2.5f, 5.0f, 5.0f};

	//Initial values for min, max, mod and val
	protected double[] initVals;
	
	public Base_GUIObj(IRenderInterface _p, int _objID, String _name, 
			double _xst, double _yst, double _xend, double _yend, 
			double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		p=_p;
		objID = _objID;
		ID = GUIObjID++;
		name = _name;
		xOff = _off[0];
		yOff = _off[1];
		//dispText = new String("UI Obj "+ID+" : "+name + " : ");
		dispText = new String(""+name + " : ");
		start = new myVector(_xst,_yst,0); end =  new myVector(_xend,_yend,0);
		minVal=_minMaxMod[0]; maxVal = _minMaxMod[1]; modMult = _minMaxMod[2];
		val = _initVal;
		initVals = new double[4];
		for(int i=0;i<_minMaxMod.length;++i) {initVals[i]=_minMaxMod[i];}
		initVals[3] = _initVal;
		objType = _objType;
		initFlags();
		int numToInit = (_flags.length < numFlags-numPrivFlags ? _flags.length : numFlags-numPrivFlags);
		for(int i =0; i<numToInit;++i){ 	setFlags(i+numPrivFlags,_flags[i]);	}	
		
		_cVal = new int[] {0,0,0};
		bxclr = new int[]{ThreadLocalRandom.current().nextInt(256),ThreadLocalRandom.current().nextInt(256),ThreadLocalRandom.current().nextInt(256),255};
		
		initDrawTrans= new float[]{(float)(start.x + xOff), (float)(start.y + yOff)};
		boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(-yOff*.25f)};			
	}
	
	public void initFlags(){			uiFlags = new int[1 + numFlags/32]; for(int i = 0; i<numFlags; ++i){setFlags(i,false);}	}
	private boolean getFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiFlags[idx/32] & bitLoc) == bitLoc;}	
	private void setFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiFlags[flIDX] = (val ?  uiFlags[flIDX] | mask : uiFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case debugIDX 					:{break;}
		case showIDX					:{break;}	//show this component
		case valChangedIDX 				:{break;}		
		case usedByWinsIDX				:{break;}
		case updateWhileModIDX			:{break;}
		case explicitUIDataUpdateIDX 	:{break;}
		}
	}//setFlag	
	
	public final String getName(){return name;}
	public final double getVal(){return val;}	
	public final double getMinVal(){return minVal;}
	public final double getMaxVal(){return maxVal;}
	public final double getModStep(){return modMult;}	
	
	protected void setIsDirty(boolean isDirty) {setFlags(valChangedIDX, isDirty);}
	public boolean shouldUpdateConsumer() {return !getFlags(explicitUIDataUpdateIDX);}
		
	//Make sure val adheres to specified bounds
	protected double forceBounds(double _val) {
		if (_val < minVal) {return minVal;}
		if (_val > maxVal) {return maxVal;}
		return _val;
	}
	public final void setNewMax(double _newval){
		double oldVal = val;
		maxVal = _newval;
		val = forceBounds(val);	
		if (oldVal != val) {setIsDirty(true);}		
	}
	public final void setNewMin(double _newval){	
		double oldVal = val;
		minVal = _newval;
		val = forceBounds(val);		
		if (oldVal != val) {setIsDirty(true);}		
	}
	public final void setNewMod(double _newval){	
		if (_newval > (maxVal-minVal)) {
			_newval = (maxVal-minVal);
		}
		modMult = _newval;	
	}
	public final double setVal(double _newVal){
		double oldVal = val;
		val = forceBounds(_newVal);	
		if (oldVal != val) {setIsDirty(true);}		
		return val;
	}	
	public abstract double modVal(double mod);
//	{
//		double oldVal = val;
//		val += (mod*modMult);
//		if(objType == GUIObj_Type.IntVal){val = Math.round(val);}
//		val = forceBounds(val);
//		if (oldVal != val) {setIsDirty(true);}		
//		return val;		
//	}
	/**
	 * Reset this UI component to its initialization values
	 */
	public final void resetToInit() {		
		setNewMin(initVals[0]);
		setNewMax(initVals[1]);
		setNewMod(initVals[2]);
		setVal(initVals[3]);
	}

	public final boolean shouldUpdateWin(boolean isRelease) {
		boolean isDirty = getFlags(valChangedIDX);
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
		for(int i =0;i<Base_GUIObj.numFlags; ++i){
			setFlags(i, Boolean.parseBoolean(toks[4].split("\\s")[i].trim()));
		}	
	}//setValFromStrTokens
	
	/**
	 * Builds a string to save the value from this UI Object
	 * @param idx
	 * @return
	 */
	public final String getStrFromUIObj(int idx){
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
		for(int i =0;i<Base_GUIObj.numFlags; ++i){
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
			//draw specifics for this UI object
			_drawIndiv();
//			if(objType == GUIObj_Type.FloatVal){		p.showText(dispText + String.format("%.5f",val), 0,0);}
//			else{
//				//disp ints and list values
//				//String resStr = (objType == GUIObj_Type.ListVal) ?  win.getUIListValStr(objID, (int)val) : String.format("%.0f",val);
//				p.showText(dispText + getListValStr((int)val), 0,0);
//			}
		p.popMatState();
	}//draw
	
	protected abstract void _drawIndiv();
	
	/**
	 * set new display text for this UI object - doesn't change name
	 * @param _str
	 */
	public final void setNewDispText(String _str) {	dispText = new String(""+_str + " : ");	}

	
	public final GUIObj_Type getObjType() {return objType;}
		
	public String[] getStrData(){
		ArrayList<String> tmpRes = new ArrayList<String>();
		tmpRes.add("ID : "+ ID+" Obj ID : " + objID  + " Name : "+ name + " distText : " + dispText);
		tmpRes.add("Start loc : "+ start + " End loc : "+ end + " Treat as Int  : " + (objType == GUIObj_Type.IntVal));
		tmpRes.add("Value : "+ val +" Max Val : "+ maxVal + " Min Val : " + minVal+ " Mod multiplier : " + modMult);
		return tmpRes.toArray(new String[0]);
	}
	
	@Override
	public String toString() {
		String res = "ID : "+ ID+" Obj ID : " + objID  + " Name : "+ name + " distText : " + dispText+"\n";
		res += "Start loc : "+ start + " End loc : "+ end + " Treat as Int  : " + (objType == GUIObj_Type.IntVal)+"\n";
		res += "Value : "+ val +"|Max Val : "+ maxVal + "|Min Val : " + minVal+ "|Mod : " + modMult+"\n";
		res += "Init Value : "+ initVals[3] +"|Init Max Val : "+ initVals[1] + "|Init Min Val : " + initVals[0]+ "|Init Mod : " + initVals[2]+"\n";
		res += "Is Dirty :"+getFlags(valChangedIDX);
		return res;
	}
}//class Base_GUIObj
