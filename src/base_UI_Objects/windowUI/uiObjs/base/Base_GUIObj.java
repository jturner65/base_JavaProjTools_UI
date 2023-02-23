package base_UI_Objects.windowUI.uiObjs.base;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.floats.myVectorf;

/**
 * UI object on menu that can be modified via mouse input
 * @author John Turner
 *
 */
public abstract class Base_GUIObj {
	/**
	 * Interface to drawing/graphics engine
	 */
	protected static IRenderInterface p;
	/**
	 * Internal object ID
	 */
	public final int ID;
	/**
	 * Keep count for next ID
	 */
	private static int GUIObjID = 0;
	
	protected final String name;
										
	/**
	 * UI Object ID from owning window
	 */
	protected final int objID;			
	/**
	 * x,y coords of top left corner for clickable region
	 */
	protected myVectorf start;
	/**
	 * x,y coords of bottom right corner for clickable region
	 */
	protected myVectorf end;
	protected String dispText;

	protected double val;
	protected double minVal, maxVal;
	
	protected final GUIObj_Type objType;
	
	/**
	 * Flags structure to monitor/manage internal UI object state. No child class should access these directly
	 */
	private int[] uiStateFlags;
	protected static final int 
		debugIDX 		= 0,
		showIDX			= 1,				//show this component
		valChangedIDX   = 2;				//object value is dirty/clean
	protected static final int numStateFlags = 3;	// # of internal state booleans
	
	/**
	 * Flags structure to monitor/manage configurable behavior. No child class should access these directly
	 */
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
		start = new myVectorf(_xst,_yst,0); end =  new myVectorf(_xend,_yend,0);
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
	public final void resetToInit() {		
		setNewMin(initVals[0]);
		setNewMax(initVals[1]);
		setNewMod(initVals[2]);
		setVal(initVals[3]);
	}
	/**
	 * Whether this object was intialized to update the owning window every time it was changed
	 * @param isRelease
	 * @return
	 */
	public final boolean shouldUpdateWin(boolean isRelease) {
		boolean isDirty = getStateFlags(valChangedIDX);
		//only clear once processed
		if (isRelease){	setIsDirty(false);	}
		return isDirty && ((isRelease || getConfigFlags(updateWhileModIDX)) && getConfigFlags(usedByWinsIDX));
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
	 * Verify passed coordinates are within this object's modifiable zone. If true then this object will be modified by UI actions
	 * @param _clkx
	 * @param _clky
	 * @return whether passed coords are within this object's modifiable zone
	 */
	public final boolean checkIn(float _clkx, float _clky){return (_clkx > start.x)&&(_clkx < end.x)&&(_clky > start.y)&&(_clky < end.y);}
	
	/**
	 * Draw this UI object encapsulated by a border representing the click region this UI element will respond to
	 * @param animTimeMod animation time modifier to enable this object to blink
	 */
	public final void drawDebug(float animTimeMod) {
		p.pushMatState();
			p.setStrokeWt(1.0f);
			if((int)(animTimeMod) % 2 == 0) {
				p.setStroke(0, 255, 255,255);
			} else {
				p.setStroke(255, 0, 255,255);				
			}
			p.noFill();
			//Draw rectangle around this object denoting active zone
			p.drawRect(start.x, start.y, end.x - start.x, end.y - start.y);
		p.popMatState();
		draw();
	}
	
	/**
	 * Draw this UI Object, including any ornamentation if appropriate
	 */
	public abstract void draw();
	
	protected abstract void _drawIndiv();
	
	/**
	 * set new display text for this UI object - doesn't change name
	 * @param _str
	 */
	public final void setNewDispText(String _str) {	dispText = new String(""+_str + " : ");	}
	
	/**
	 * Return the type of this object as defined in GUIObj_Type enum
	 * @return
	 */
	public final GUIObj_Type getObjType() {return objType;}
		
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
	
	
	/**
	 * Retrive an array of string debug data
	 * @return
	 */
	public String[] getStrData(){
		String[] tmpRes = new String[6];
		tmpRes[0] = "ID : "+ ID+" Obj ID : " + objID  + " Name : "+ name + " distText : " + dispText;
		tmpRes[1] = "Upper Left crnr click zone : ["+ start.x +","+start.y+"]| Lower Right crnr click zone : ["+ end.x +","+end.y+"]";
		tmpRes[2] = "Treat as Int  : " + (objType == GUIObj_Type.IntVal);
		tmpRes[3] = "Value : "+ val +" Max Val : "+ maxVal + " Min Val : " + minVal+ " Mod multiplier : " + modMult;
		tmpRes[4] = "Init Value : "+ initVals[3] +"|Init Max Val : "+ initVals[1] + "|Init Min Val : " + initVals[0]+ "|Init Mod : " + initVals[2];
		tmpRes[5] = "Is Dirty :"+getStateFlags(valChangedIDX);
		return tmpRes;
	}
	
	@Override
	public String toString() {
		String[] tmpRes = getStrData();
		String res = tmpRes[0]+"\n";
		for (int i=1;i<tmpRes.length;++i) {res += tmpRes[i]+"\n";}
		return res;
	}
}//class Base_GUIObj