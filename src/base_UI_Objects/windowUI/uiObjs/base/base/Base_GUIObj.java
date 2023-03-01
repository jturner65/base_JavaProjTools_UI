package base_UI_Objects.windowUI.uiObjs.base.base;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.ornaments.base.Base_GUIPrefixObj;
import base_Math_Objects.vectorObjs.floats.myVectorf;

/**
 * Base class for interactable UI objects
 * @author John Turner
 *
 */
public abstract class Base_GUIObj {
	/**
	 * Interface to drawing/graphics engine
	 */
	protected static IRenderInterface ri;
	/**
	 * Internal object ID
	 */
	public final int ID;
	/**
	 * Keep count for next ID
	 */
	private static int GUIObjID = 0;
	/**
	 * Name/display label of object
	 */
	protected final String name;	
	/**
	 * UI Object ID from owning window (index in holding container)
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
	/**
	 * Text to display as a label
	 */
	protected String dispText;
	/**
	 * Type of this object
	 */
	protected final GUIObj_Type objType;	
	/**
	 * Flags structure to monitor/manage internal UI object state. No child class should access these directly
	 */
	private int[] uiStateFlags;
	protected static final int 
		debugIDX 		= 0,
		showIDX			= 1,					//show this component
		valChangedIDX   = 2;					//object value is dirty/clean
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

	/**
	 * Fill color value for main UI object
	 */
	protected int[] _fillClr = new int[] {0,0,0,255};
	/**
	 * Stroke color value for main UI object
	 */
	protected int[] _strkClr = new int[] {0,0,0,255};
	
	/**
	 * Object to either manage and display or not show an ornamental box in front of a UI element
	 */
	private final Base_GUIPrefixObj _ornament;
	
	/**
	 * Builds a UI object
	 * @param _p render interface
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _xst the x value of the upper-left corner of the object's hot zone
	 * @param _yst the y value of the upper-left corner of the object's hot zone
	 * @param _xend the x value of the lower-right corner of the object's hot zone
	 * @param _yend the y value of the lower-right corner of the object's hot zone
	 * @param _objType the type of UI object this is
	 * @param _flags any preset configuration flags
	 * @param _off
	 */
	public Base_GUIObj(IRenderInterface _p, int _objID, String _name, 
			double _xst, double _yst, double _xend, double _yend, 
			GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		ri=_p;
		objID = _objID;
		ID = GUIObjID++;
		name = _name;
		dispText = name.length() > 0 ? name + " : " : ("");
		//hotbox start and end x,y's
		start = new myVectorf(_xst,_yst,0); end =  new myVectorf(_xend,_yend,0);
		//type of object
		objType = _objType;
		//UI Object state
		initStateFlags();
		//UI Object configuration
		initConfigFlags();
		//build prefix ornament to display
		_ornament = _buildPrefixOrnament(_off);
		
		int numToInit = (_flags.length < numConfigFlags ? _flags.length : numConfigFlags);
		for(int i =0; i<numToInit;++i){ 	setConfigFlags(i,_flags[i]);	}
	}
	
	/**
	 * Instance class should build an ornament object based on whether it should or should not have one
	 * @return
	 */
	protected abstract Base_GUIPrefixObj _buildPrefixOrnament(double[] _off);
	
	private void initStateFlags(){			uiStateFlags = new int[1 + numStateFlags/32]; for(int i = 0; i<numStateFlags; ++i){setStateFlags(i,false);}	}
	protected boolean getStateFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiStateFlags[idx/32] & bitLoc) == bitLoc;}	
	protected void setStateFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiStateFlags[flIDX] = (val ?  uiStateFlags[flIDX] | mask : uiStateFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case debugIDX 					:{break;}
		case showIDX					:{break;}
		case valChangedIDX 				:{break;}
		}
	}//setFlag	
	
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

	
	
	public final String getName(){return name;}
	
	protected void setIsDirty(boolean isDirty) {setStateFlags(valChangedIDX, isDirty);}
	public boolean shouldUpdateConsumer() {return !getConfigFlags(explicitUIDataUpdateIDX);}
	
	/**
	 * Reset this object's value to its initial value
	 */
	public abstract void resetToInit();
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
	private int _animCount = 0;
	private boolean _cyanStroke = false;
	public final void drawDebug() {
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			++_animCount;
			if(_animCount>20) {_animCount = 0; _cyanStroke = !_cyanStroke;}
			if(_cyanStroke) {ri.setStroke(0, 255, 255,255);} else {	ri.setStroke(255, 0, 255,255);}
			ri.noFill();
			//Draw rectangle around this object denoting active zone
			ri.drawRect(start.x, start.y, end.x - start.x, end.y - start.y);
		ri.popMatState();
		draw();
	}
	
	/**
	 * Draw this UI Object, including any ornamentation if appropriate
	 */
	public final void draw() {
		ri.pushMatState();
			ri.translate(start.x,start.y,0);
			_ornament.drawPrefixObj(ri);
			ri.setFill(_fillClr,_fillClr[3]);
			ri.setStroke(_strkClr,_strkClr[3]);	
			//draw specifics for this UI object
			_drawObject_Indiv();
		ri.popMatState();
	}//draw

	protected abstract void _drawObject_Indiv();
	
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
		//Set UI object value from correct token
		setValueFromString(toks[3].split("\\s")[1].trim());	
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
	 * Set this UI object's value from a string
	 * @param str
	 */
	protected abstract void setValueFromString(String str);
	
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
		sb.append(getValueAsString());
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
	 * Get this UI object's value as a string
	 * @return
	 */
	protected abstract String getValueAsString();
	
	/**
	 * Get string data aarray representing the value this UI object holds
	 * @return
	 */
	protected abstract String[] getStrDataForVal();
	
	/**
	 * Retrive an array of string debug data
	 * @return
	 */
	public String[] getStrData(){
		String[] valResAra = getStrDataForVal();
		String[] tmpRes = new String[valResAra.length+4];
		int idx = 0;
		tmpRes[idx++] = "ID : "+ ID+" Obj ID : " + objID  + " Name : "+ name + " distText : " + dispText;
		tmpRes[idx++] = "Upper Left crnr click zone : ["+ start.x +","+start.y+"]| Lower Right crnr click zone : ["+ end.x +","+end.y+"]";
		tmpRes[idx++] = "Treat as Int  : " + (objType == GUIObj_Type.IntVal);
		for (String valStr : valResAra) {tmpRes[idx++] = valStr;}
		tmpRes[idx++] = "Is Dirty :"+getStateFlags(valChangedIDX);
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
