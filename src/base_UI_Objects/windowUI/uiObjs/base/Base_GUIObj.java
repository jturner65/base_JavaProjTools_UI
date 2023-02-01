package base_UI_Objects.windowUI.uiObjs.base;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myVector;

//object on menu that can be modified via mouse input
public abstract class Base_GUIObj {
	protected static IRenderInterface p;
	
	//object to draw or not draw the prefix box
	protected base_UIObjDrawer boxDrawer; 

	public final int ID;
	//static variables - put obj constructor counters here
	private static int GUIObjID = 0;										//counter variable for gui objs

	protected final int objID;							//id in owning window
	protected myVector start, end;				//x,y coords of start corner, end corner (z==0) for clickable region
	protected final String name;
	protected String dispText;

	protected double val;
	protected double minVal, maxVal;
	
	protected final GUIObj_Type objType;
	
	//Flags structure to monitor/manage internal state
	protected int[] uiStateFlags;
	protected static final int 
		debugIDX 		= 0,
		showIDX			= 1,				//show this component
		valChangedIDX   = 2;				//object value is dirty/clean
	protected static final int numStateFlags = 3;	// # of internal state booleans
	
	//Flags structure to monitor/manage configurable behavior
	private int[] uiConfigFlags;
	protected static final int 
			//config flags
			usedByWinsIDX	= 0, 				//value is sent to window
			updateWhileModIDX = 1,				//value is sent to window on any change, not just release
			explicitUIDataUpdateIDX = 2;		//if true does not update UIDataUpdate structure on changes - must be explicitly sent to consumers 
	protected static final int numConfigFlags = 3;			// # of config flags		


	
	protected final int[] _cVal = new int[] {0,0,0};
	protected double modMult,						//multiplier for mod value
					xOff,yOff;						//Offset value
	public float[] initDrawTrans, boxDrawTrans;
	public int[] bxclr;
	
	public final float[] boxDim = new float[] {-2.5f, -2.5f, 5.0f, 5.0f};

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
		dispText = name.length() > 0 ? new String(""+name + " : ") : ("");
		start = new myVector(_xst,_yst,0); end =  new myVector(_xend,_yend,0);
		minVal=_minMaxMod[0]; maxVal = _minMaxMod[1]; modMult = _minMaxMod[2];
		val = _initVal;
		initVals = new double[4];
		for(int i=0;i<_minMaxMod.length;++i) {initVals[i]=_minMaxMod[i];}
		initVals[3] = _initVal;
		objType = _objType;
		initStateFlags();
		
		initConfigFlags();
		
		int numToInit = (_flags.length < numConfigFlags ? _flags.length : numConfigFlags);
		for(int i =0; i<numToInit;++i){ 	setConfigFlags(i,_flags[i]);	}	
		
		bxclr = new int[]{ThreadLocalRandom.current().nextInt(256),
				ThreadLocalRandom.current().nextInt(256),
				ThreadLocalRandom.current().nextInt(256),255};
		
		initDrawTrans= new float[]{(float)(start.x + xOff), (float)(start.y + yOff)};
		boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(-yOff*.25f)};			
	}
	
	private void initConfigFlags(){			uiConfigFlags = new int[1 + numConfigFlags/32]; for(int i = 0; i<numConfigFlags; ++i){setConfigFlags(i,false);}	}
	protected boolean getConfigFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiConfigFlags[idx/32] & bitLoc) == bitLoc;}	
	protected void setConfigFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiConfigFlags[flIDX] = (val ?  uiConfigFlags[flIDX] | mask : uiConfigFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case usedByWinsIDX				:{break;}
		case updateWhileModIDX			:{break;}
		case explicitUIDataUpdateIDX 	:{break;}
		}
	}//setFlag	
	
	private void initStateFlags(){			uiStateFlags = new int[1 + numStateFlags/32]; for(int i = 0; i<numStateFlags; ++i){setStateFlags(i,false);}	}
	protected boolean getStateFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiStateFlags[idx/32] & bitLoc) == bitLoc;}	
	protected void setStateFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiStateFlags[flIDX] = (val ?  uiStateFlags[flIDX] | mask : uiStateFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case debugIDX 					:{break;}
		case showIDX					:{break;}	//show this component
		case valChangedIDX 				:{break;}
		}
	}//setFlag	
	
	
	public final String getName(){return name;}
	public final double getVal(){return val;}	
	public final double getMinVal(){return minVal;}
	public final double getMaxVal(){return maxVal;}
	public final double getModStep(){return modMult;}	
	
	protected void setIsDirty(boolean isDirty) {setStateFlags(valChangedIDX, isDirty);}
	public boolean shouldUpdateConsumer() {return !getConfigFlags(explicitUIDataUpdateIDX);}
		
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
		boolean isDirty = getStateFlags(valChangedIDX);
		//only clear once processed
		if (isRelease){	setIsDirty(false);	}
		return isDirty && ((isRelease || getConfigFlags(updateWhileModIDX)) && getConfigFlags(usedByWinsIDX));
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
		//state
		for(int i =0;i<Base_GUIObj.numStateFlags; ++i){
			setStateFlags(i, Boolean.parseBoolean(toks[4].split("\\s")[i].trim()));
		}	
		//config
		for(int i =0;i<Base_GUIObj.numConfigFlags; ++i){
			setConfigFlags(i, Boolean.parseBoolean(toks[5].split("\\s")[i].trim()));
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
		sb.append(" |state flags: ");
		for(int i =0;i<Base_GUIObj.numStateFlags; ++i){
			sb.append(getStateFlags(i) ? " true" : " false");
		}
		sb.append(" |config flags: ");
		for(int i =0;i<Base_GUIObj.numConfigFlags; ++i){
			sb.append(getConfigFlags(i) ? " true" : " false");
		}
		return sb.toString().trim();		
		
	}//getStrFromUIObj
	
	public final boolean checkIn(float _clkx, float _clky){return (_clkx > start.x)&&(_clkx < end.x)&&(_clky > start.y)&&(_clky < end.y);}
	public final void draw(){
		p.pushMatState();
			p.translate(initDrawTrans[0],initDrawTrans[1],0);
			p.pushMatState();
				p.noStroke();
				p.setFill(bxclr,bxclr[3]);
				p.translate(boxDrawTrans[0],boxDrawTrans[1],0);
				p.drawRect(boxDim);
			p.popMatState();
			p.setFill(_cVal,255);
			p.setStroke(_cVal,255);			
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
		res += "Is Dirty :"+getStateFlags(valChangedIDX);
		return res;
	}
}//class Base_GUIObj

abstract class base_UIObjDrawer{
	protected static IRenderInterface p;
	public base_UIObjDrawer(IRenderInterface _p) {}
	public abstract void draw(Base_GUIObj _obj);
}//base_UIBoxDrawer

class UIObjDrawerBox extends base_UIObjDrawer{
	public UIObjDrawerBox(IRenderInterface p) {super(p);}
	@Override
	public final void draw(Base_GUIObj _obj) {
		p.pushMatState();
			p.noStroke();
			p.setFill(_obj.bxclr,_obj.bxclr[3]);
			p.translate(_obj.boxDrawTrans[0],_obj.boxDrawTrans[1],0);
			p.drawRect(_obj.boxDim);
		p.popMatState();		
	}
	
}//class UIBoxDrawer

class UIObjDrawerNoBox extends base_UIObjDrawer{
	public UIObjDrawerNoBox(IRenderInterface p) {super(p);}
	@Override
	public final void draw(Base_GUIObj _obj) {}
	
}//class UINoBoxDrawer