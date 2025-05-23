package base_UI_Objects.windowUI.uiObjs.base.base;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * Base class for interactive UI objects
 * @author John Turner
 *
 */
public abstract class Base_GUIObj {
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
	 * Text to display as a label
	 */
	protected String label;
	/**
	 * Original label given to object (for reset)
	 */
	protected final String origLabel;
	/**
	 * Type of this object
	 */
	protected final GUIObj_Type objType;	
	/**
	 * Flags structure to monitor/manage internal UI object state. No child class should access these directly
	 */
	private int[] uiStateFlags;
	private static final int 
		debugIDX 			= 0,
		showIDX				= 1,					// show this component
		objHasFocusIDX		= 2,					// object currently has focus - set true upon click entry, false on click release
		valChangedIDX   	= 3,					// object value is dirty/clean
		rendererSetIDX 		= 4;					// whether or not the renderer has been built and assigned
	private static final int numStateFlags = 5;	// # of internal state booleans
	
	/**
	 * Flags structure to monitor/manage configurable behavior. No child class should access these directly
	 */
	private int[] uiConfigFlags;
	private static final int 
		//config flags
		usedByWinsIDX			= 0, 				// value is sent to window
		updateWhileModIDX 		= 1,				// value is sent to window on any change, not just release
		explicitUIDataUpdateIDX = 2,				// if true does not update UIDataUpdate structure on changes - must be explicitly sent to consumers
		objectIsReadOnlyIDX		= 3;			 	// ui object is not user-modifiable, just read only
	private static final int numConfigFlags = 4;			// # of config flags		
	
	/**
	 * Renderer for this object
	 */
	protected Base_GUIObjRenderer renderer;
	
	/**
	 * Builds a UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _objType the type of UI object this is
	 * @param _flags any preset behavior flags
	 * @param _off offset before text
	 */
	public Base_GUIObj(int _objID, String _name, GUIObj_Type _objType, boolean[] _flags){
		objID = _objID;
		ID = GUIObjID++;
		name = _name;
		setLabelFromName();
		origLabel = label;
		//type of object
		objType = _objType;
		//UI Object state
		initStateFlags();
		//UI Object configuration
		initConfigFlags();
		
		int numToInit = (_flags.length < numConfigFlags ? _flags.length : numConfigFlags);
		for(int i =0; i<numToInit;++i){ 	setConfigFlags(i,_flags[i]);	}
	}
	
	/**
	 * Returns whether this UI object is ready to be used
	 */
	public boolean checkUIObjectStatus(){ 
		return getStateFlags(rendererSetIDX) && checkUIObjectStatus_Indiv();
	}
	
	/**
	 * Instance-specific check for whether this UI object is ready to be used
	 */
	protected abstract boolean checkUIObjectStatus_Indiv();
	
	private void initStateFlags(){			uiStateFlags = new int[1 + numStateFlags/32]; for(int i = 0; i<numStateFlags; ++i){setStateFlags(i,false);}	}
	private boolean getStateFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiStateFlags[idx/32] & bitLoc) == bitLoc;}	
	private void setStateFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiStateFlags[flIDX] = (val ?  uiStateFlags[flIDX] | mask : uiStateFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case debugIDX 				:{break;}
		case showIDX				:{break;}
		case objHasFocusIDX			:{break;}
		case valChangedIDX 			:{break;}
		case rendererSetIDX			:{break;}
		}
	}//setFlag	
	
	private void initConfigFlags(){			uiConfigFlags = new int[1 + numConfigFlags/32]; for(int i = 0; i<numConfigFlags; ++i){setConfigFlags(i,false);}	}
	private boolean getConfigFlags(int idx){	int bitLoc = 1<<(idx%32);return (uiConfigFlags[idx/32] & bitLoc) == bitLoc;}	
	private void setConfigFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		uiConfigFlags[flIDX] = (val ?  uiConfigFlags[flIDX] | mask : uiConfigFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case usedByWinsIDX				:{break;}
		case updateWhileModIDX			:{break;}
		case explicitUIDataUpdateIDX 	:{break;}	
		case objectIsReadOnlyIDX		:{break;}
		}
	}//setFlag	
		
	public void setHasFocus() {setStateFlags(objHasFocusIDX, true);}
	public void clearFocus() {setStateFlags(objHasFocusIDX, false);}
	public boolean getHasFocus() {return getStateFlags(objHasFocusIDX);}
	
	protected void setIsDirty(boolean isDirty) {setStateFlags(valChangedIDX, isDirty);}
	public boolean getIsDirty() {return getStateFlags(valChangedIDX);}
	public boolean shouldUpdateConsumer() {return !getConfigFlags(explicitUIDataUpdateIDX);}
	protected boolean isUsedByWindow() {return getConfigFlags(usedByWinsIDX);}
	
	/**
	 * Reset this object's value to its initial value
	 */
	public abstract void resetToInit();
	/**
	 * Whether this object was initialized to update the owning window every time it was changed
	 * @param isRelease
	 * @return
	 */
	public final boolean shouldUpdateWin(boolean isRelease) {
		boolean isDirty = getIsDirty();
		//only clear once processed
		if (isRelease){	setIsDirty(false);	}
		return isDirty && ((isRelease || getConfigFlags(updateWhileModIDX)) && isUsedByWindow());
	}
	
	/**
	 * Verify passed coordinates are within this object's modifiable zone. 
	 * If true then this object will be modified by UI actions
	 * Renderer Manages hotspot. -sigh- might be better if this object owned this data.
	 * @param _clkx
	 * @param _clky
	 * @return whether passed coords are within this object's modifiable zone
	 */
	public final boolean checkIn(float _clkx, float _clky){return renderer.checkIn(_clkx, _clky);}
	
	/**
	 * Draw this UI object encapsulated by a border representing the click region this UI element will respond to
	 * @param animTimeMod animation time modifier to enable this object to blink
	 */
	public final void drawDebug() {			renderer.drawDebug();}
	
	/**
	 * Draw this UI Object, including any ornamentation if appropriate
	 */
	public final void draw() {				renderer.draw();}//draw
	
	/**
	 * Draw a highlight box around this object representing the click region this UI element will respond to
	 */
	public final void drawHighlight() { 	renderer.drawHighlight();}
		
	/**
	 * Return the type of this object as defined in GUIObj_Type enum
	 * @return
	 */
	public final GUIObj_Type getObjType() {return objType;}
	
	/**
	 * Recalculate the renderer-managed interactive hotspot for this object
	 * @param newStart the currently appropriate start location (upper left corner) for this hotspot
	 * @param lineHeight the height of a line of text
	 * @param menuStartX the x location on the screen for the beginning of the "menu" (i.e. UI region). 
	 * For multi-line UI objects that may need to pop down to the next line
	 * @param menuWidth the width of the "menu" (i.e. UI region). Used to test if a multi-line UI object will
	 * fit in the desired menu area.
	 * @return the next object's new start location
	 */
	public final myPointf reCalcHotSpot(myPointf newStart, float lineHeight, float menuStartX, float menuWidth) {
		return renderer.reCalcHotSpot(newStart, lineHeight, menuStartX, menuWidth);
	}

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
	 * Assign the renderer to use to render this UI object (i.e. single line or multi-line)
	 * @param _renderer
	 */
	public final void setRenderer(Base_GUIObjRenderer _renderer) {
		renderer = _renderer;
		setStateFlags(rendererSetIDX, true);
	}
		
	/**
	 * Return this object's label
	 */
	public String getLabel() {return label;}
	
	/**
	 * set new display text for this UI object - doesn't change name
	 * @param _str
	 */
	public abstract void setLabel(String _str);
	
	/**
	 * What to display if this UI object is single line
	 * @return
	 */
	public final String getUIDispAsSingleLine() {
		return getLabel() + getValueAsString();		
	}
	
	/**
	 * What to display if this UI object is multi line
	 * @return
	 */
	public final String[] getUIDispAsMultiLine() {
		return new String[] {getLabel(), getValueAsString()};		
	}	
	
	/**
	 * Standard label = name + ':' + value (added by instancing class)
	 * TODO Change this format for multi-line UI objs?
	 * @return
	 */
	public void setLabelFromName() {
		setLabel(name);
		//name.trim().length() > 0 ?(name + " : ") : ("");
	}

	/**
	 * Return the constant name assigned to this object on creation
	 */
	public final String getName(){return name;}
	
	
	public final int getObjID() {return objID;}
	/**
	 * Set this UI object's value from a string
	 * @param str
	 */
	protected abstract void setValueFromString(String str);

	/**
	 * Get this UI object's value as a string
	 * @return
	 */
	public abstract String getValueAsString();
	
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
		tmpRes[idx++] = "ID : "+ ID+" Obj ID : " + objID  + " Name : "+ name + " label : `" + getLabel()+"`";
		tmpRes[idx++] = renderer.getHotBoxLocString();
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
