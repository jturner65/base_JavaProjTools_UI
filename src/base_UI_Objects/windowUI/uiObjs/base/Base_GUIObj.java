package base_UI_Objects.windowUI.uiObjs.base;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * Base class for interactive UI objects
 * @author John Turner
 *
 */
public abstract class Base_GUIObj implements Comparable<Base_GUIObj>{
    /**
     * Internal object ID
     */
    public final int ID;
    /**
     * Keep count for next ID
     */
    private static int GUIObjID = 0;
    /**
     * Unique permanent name of object
     */
    protected final String name;    
    /**
     * UI Object ID from owning window (index in holding container)
     */
    protected final int objID;
    
    // Text to display as a label, without colon if appropriate    
    protected String label;
    // version of label with colon suffix
    protected String _dispLabel;
    
    /**
     * Original label given to object (for reset)
     */
    protected final String origLabel;
    /**
     * Type of this object
     */
    protected final GUIObj_Type objType;    
    /**
     * String format to display value
     */
    protected String formatStr;    
    /**
     * Original string format
     */
    private final String origFormatStr;
    /**
     * Initial values for min, max, mod and val
     */
    protected double[] initVals;    
    /**
     * Value for this numeric object
     */
    private double val;
    /**
     * Min and max values allowed for this numeric object
     */
    private double minVal, maxVal;
    /**
     * Multiplier for modification
     */
    protected double modMult;
        
    /**
     * Flags structure to monitor/manage internal UI object state. No child class should access these directly
     */
    private int[] uiStateFlags;
    private static final int 
        debugIDX           = 0,
        showIDX            = 1,                    // show this component
        objIsClickedIDX    = 2,                    // object currently has focus - set true upon click entry, false on click release
        valChangedIDX      = 3,                    // object value is dirty/clean
        rendererSetIDX     = 4;                    // whether or not the renderer has been built and assigned
    private static final int numStateFlags = 5;    // # of internal state booleans
    
    /**
     * The configuration flags that govern how this renderer will be constructed
     */
    private GUIObjConfig_Flags cfgFlags;
        
    /**
     * Renderer for this object
     */
    private Base_GUIObjRenderer renderer;
    
    /**
     * Builds a UI object
     * @param _objID the index of the object in the managing container
     * @param _name the name/display label of the object
     * @param _objType the type of UI object this is
     * @param _flags any preset behavior flags
     * @param _off offset before text
     */
    public Base_GUIObj(int _objID, GUIObj_Params objParams){
        objID = _objID;
        ID = GUIObjID++;
        //UI Object configuration flags
        cfgFlags = objParams.getUIObjConfigFlags();
        name = objParams.getName();
        setLabel(objParams.label);
        origLabel = label;
        //type of object
        objType = objParams.objType;
        //UI Object state
        initStateFlags();
        
        minVal=objParams.minMaxMod[0]; maxVal = objParams.minMaxMod[1]; setNewMod(objParams.minMaxMod[2]);
        //setNewMod sets formatStr
        origFormatStr = formatStr;
        val = objParams.initVal;
        initVals = new double[4];
        for(int i=0;i<objParams.minMaxMod.length;++i) {initVals[i]=objParams.minMaxMod[i];}
        initVals[3] = objParams.initVal;
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
    
    private void initStateFlags(){            uiStateFlags = new int[1 + numStateFlags/32]; for(int i = 0; i<numStateFlags; ++i){setStateFlags(i,false);}    }
    private boolean getStateFlags(int idx){    int bitLoc = 1<<(idx%32);return (uiStateFlags[idx/32] & bitLoc) == bitLoc;}    
    private void setStateFlags(int idx, boolean val){
        int flIDX = idx/32, mask = 1<<(idx%32);
        uiStateFlags[flIDX] = (val ?  uiStateFlags[flIDX] | mask : uiStateFlags[flIDX] & ~mask);
        switch (idx) {//special actions for each flag
        case debugIDX                 :{break;}
        case showIDX                  :{break;}
        case objIsClickedIDX          :{break;}
        case valChangedIDX            :{break;}
        case rendererSetIDX           :{break;}
        }
    }//setStateFlags    
        
    public void setIsClicked() {setStateFlags(objIsClickedIDX, true);}
    public void clearIsClicked() {setStateFlags(objIsClickedIDX, false);}
    public boolean getIsClicked() {return getStateFlags(objIsClickedIDX);}
    protected void setIsDirty(boolean isDirty) {setStateFlags(valChangedIDX, isDirty);}
    public boolean getIsDirty() {return getStateFlags(valChangedIDX);}
    
    ////////////
    // config flags access
    /**
     * Set whether this value is actually used by the owning window
     * @return
     */
    protected void setIsUsedByWindow(boolean val) {         cfgFlags.setIsUsedByWindow(val);}
    /**
     * Get whether this value is actually used by the owning window
     * @return
     */
    public boolean isUsedByWindow() {                       return cfgFlags.isUsedByWindow();}
    /**
     * Set whether this value is sent to window on any change, not just release
     * @return
     */
    protected void setUpdateWindowWhileMod(boolean val) {   cfgFlags.setUpdateWindowWhileMod(val);}
    /**
     * Get whether this value is sent to window on any change, not just release
     * @return
     */
    protected boolean isUpdateWindowWhileMod() {            return cfgFlags.isUpdateWindowWhileMod();}
    /**
     * Set whether this value, if true, does not update UIDataUpdate structure on changes - must be explicitly sent to consumers
     * @return
     */
    protected void setDontUpdateOwner(boolean val) {        cfgFlags.setDontUpdateOwner(val);}
    
    /**
     * Whether this object should update the consuming window or not
     * @return
     */
    public boolean shouldUpdateConsumer() {return !cfgFlags.getDontUpdateOwner();}
    /**
     * Get whether this value, if true, does not update UIDataUpdate structure on changes - must be explicitly sent to consumers
     * @return
     */
    public boolean getDontUpdateOwner() {                   return cfgFlags.getDontUpdateOwner();}
    /**
     * Set whether this object is read only
     * @return
     */
    protected void setIsReadOnly(boolean val) {             cfgFlags.setIsReadOnly(val);}
    /**
     * Get whether this object is read only
     * @return
     */
    public boolean isReadOnly() {                           return cfgFlags.isReadOnly();}
    
    /**
     * Set whether this object is value-range instead of just a single value (uses min and max instead of val)
     * @return
     */
    protected void setIsValueRange(boolean val) {           cfgFlags.setIsValueRange(val);}
    /**
     * Get whether this object is value-range instead of just a single value (uses min and max instead of val)
     * @return
     */
    public boolean isValueRange() {                         return cfgFlags.isValueRange();}   
    
    /**
     * Reset this UI component to its initialization values
     */
    public final void resetToInit() {        
        setNewMin(initVals[0]);
        setNewMax(initVals[1]);
        setNewMod(initVals[2]);
        returnToInitVal();
        setLabel(origLabel);
        formatStr = origFormatStr;
        resetToInit_Indiv();
    }//resetToInit

    /**
     * Return this object to its initial value
     */
    public final void returnToInitVal() {setVal(initVals[3]);}
    
    /**
     * Instance-specific reset - most instance classes have nothing to do here. Override for classes that require it.
     */
    protected void resetToInit_Indiv() {}
    /**
     * Get this UI component's current value
     * @return
     */
    public final double getVal(){return val;}
    /**
     * Get this UI component's current minimum value
     * @return
     */
    public final double getMinVal(){return minVal;}
    /**
     * Get this UI component's current maximum value
     * @return
     */
    public final double getMaxVal(){return maxVal;}
    /**
     * Get this UI component's current per-input modification amount
     * @return
     */
    public final double getModStep(){return modMult;}    
    /**
     * Get the difference between this UI component's current maximum and minimum values
     * @return
     */
    public final double getMinMaxDiff() {return maxVal - minVal;}
    
    /**
     * Make sure val adheres to specified bounds. May be overridden if object should act differently at boundaries (i.e. torroid)
     * @param _val
     * @return
     */
    protected double forceBounds(double _val) {
        if (_val < minVal) {return minVal;}
        if (_val > maxVal) {return maxVal;}
        return _val;
    }
    
    /**
     * Make sure _val adheres to specified bounds, wrapping around torroidally if necessary
     * @param _val
     * @return
     */
    protected double forceBoundsTorroidal(double _val) {
        double diff = maxVal - minVal;
        if (_val <= maxVal) {
            if(_val >= minVal) {                return _val;    }        //in correct range         
            // val is less than minVal, so move it by distVal-sized blocks until it is not
            while (val < minVal) {val += diff;}    
        }
        // val is greater than maxVal, so mod it to be in window [minVal,maxVal]
        // by shifting to [0,maxVal-minVal] first and then shifting back
        _val = ((_val - minVal) % diff) + minVal;
        return _val;
    }
    
    /**
     * Set new maximum value for this object, which will also force current value to adhere to bounds.
     * NOTE: Forces maxVal to be == to minVal if _newVal is < minVal. Don't be stupid
     * @param _newval
     */
    public final void setNewMax(double _newVal){
        double oldVal = val;
        maxVal = (_newVal > minVal ? _newVal : minVal);
        val = forceBounds(val);    
        if (oldVal != val) {setIsDirty(true);}        
    }
    
    /**
     * Set a new minimum bound for this object, which will also force current value to adhere to bounds.
     * NOTE: Forces minVal to be == to maxVal _newVal is > maxVal. Don't be stupid.
     * @param _newval
     */
    public final void setNewMin(double _newVal){    
        double oldVal = val;
        minVal = (_newVal < maxVal ? _newVal : maxVal);
        val = forceBounds(val);        
        if (oldVal != val) {setIsDirty(true);setFormatString(minVal);}        
    }
    
    /**
     * Set a new modifier value `modMult` to use for this object, and use the modifier to
     * derive the `formatStr` used to display this object's data.
     * @param _newval
     */
    public abstract void setNewMod(double _newval);
    
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
     * Modify this object by passed mod value, scaled by modMult. This is called during drag
     * @param mod
     * @return
     */
    public double dragModVal(double mod) {return setVal(modValAssign(val + (mod*modMult)));}
    
    /**
     * Modify this object by passed mod value, multiplied by scale. This is for a single click
     * @param mod
     * @param scale
     * @return
     */
    public double clickModVal(double mod, double scale) {
        return setVal(modValAssign(val + (mod *scale*modMult)));
    }
    
    /**
     * Object-specific handling of modified value. (int/list objects will round, display-only will force to be original val)
     * @param _val
     * @return
     */
    protected abstract double modValAssign(double _val);
    /**
     * Whether this object was initialized to update the owning window every time it was changed
     * @param isRelease
     * @return
     */
    public final boolean shouldUpdateWin(boolean _isRelease) {
        boolean isDirty = getIsDirty();
        //only clear once processed
        if (_isRelease){    setIsDirty(false);    }
        return isDirty && ((_isRelease || cfgFlags.isUpdateWindowWhileMod()) && cfgFlags.isUsedByWindow());
    }
    
    /**
     * Set the format string to be sufficient to display the passed value
     * @param _fmtVal value to use to determine format string
     */
    protected final void setFormatString(double _fmtVal) {
        if(_fmtVal >=1) {           formatStr = "%.0f"; } 
        else {
            int formatVal = (int) Math.ceil(-Math.log10(_fmtVal*1.00001))+1;
            formatStr = "%."+formatVal+"f";
        }
    }
    
    /**
     * Tell the renderer to update its info about this object. Should be called whenever major
     * changes to the object happen, such as a list box getting new values (not on regular UI interaction)
     */
    public final void updateRenderer() {       if(renderer != null) {      renderer.updateFromObject();        }   }
    
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
    public final void drawDebug() {            renderer.drawDebug(getIsClicked() || isReadOnly());}
    
    /**
     * Draw this UI Object, including any ornamentation if appropriate
     */
    public final void draw() {                renderer.draw(getIsClicked() || isReadOnly());}//draw
    
    /**
     * Draw a highlight box around this object representing the click region this UI element will respond to
     */
    public final void drawHighlight() {     renderer.drawHighlight();}    
    
    /**
     * Upper left corner of hotspot for this gui object
     * @return
     */
    public final myPointf getStart() {return renderer.getStart();}
    /**
     * Lower right corner of hotspot for this gui object
     * @return
     */
    public final myPointf getEnd() {return renderer.getEnd();}    
    
    /**
     * Set the upper left coordinates and width and height
     * @param _points : idx 0 is upper left corner, idx 1 is lower right corner
     */
    public final void setHotSpot(myPointf[] _points) {
        renderer.setStart(_points[0]);
        renderer.setEnd(_points[1]);
    }
        
    /**
     * Return the type of this object as defined in GUIObj_Type enum
     * @return
     */
    public final GUIObj_Type getObjType() {return objType;}
    
    /**
     * Whether this gui object is multi-line or single line
     * @return
     */
    public final boolean isMultiLine() {return renderer.getIsMultiLine();}
    
    /**
     * Return the max width feasible for this UI object's text (based on possible values + label length if any)
     * @return
     */
    public float getMaxTextWidth() {    return renderer.getMaxTextWidth();}
    
    /**
     * Return the height of the text this object will be displaying
     * @return
     */
    public float getMaxTextHeight(float yHeight) {         return renderer.getNumTextLines() * yHeight;}
    
    /**
     * Return whether or not this object should fill the available menu space
     * @return
     */
    public final boolean getIsOneObjPerRow() { return renderer.getIsOneObjPerRow();}
    
    /**
     * Return whether or not this object should fill the available menu space
     * @return
     */
    public final boolean getForceStartNewLine() { return renderer.getForceStartNewLine();}
    
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
        boolean[] vals = new boolean[GUIObjConfig_Flags.numConfigFlags];
        
        
        for(int i =0;i<vals.length; ++i){
            vals[i] = Boolean.parseBoolean(toks[5].split("\\s")[i].trim());
        }   
        cfgFlags = new GUIObjConfig_Flags(vals);
    }//setValFromStrTokens
    
    /**
     * Builds a string to save the value from this UI Object
     * @return
     */
    public final String getStrFromUIObj(){
        StringBuilder sb = new StringBuilder(400);
        sb.append("ui_idx: ");
        sb.append(objID);
        sb.append(" |name: ");
        sb.append(name);
        sb.append(" |label: ");
        sb.append(label);
        sb.append(" |type: ");
        sb.append(objType.getVal());
        sb.append(" |value: ");
        sb.append(getValueAsString());
        sb.append(" |state flags: ");
        for(int i =0;i<Base_GUIObj.numStateFlags; ++i){
            sb.append(getStateFlags(i) ? " true" : " false");
        }
        sb.append(" |config flags: ");
        for(int i =0;i<GUIObjConfig_Flags.numConfigFlags; ++i){
            sb.append(cfgFlags.getFlag(i) ? " true" : " false");
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
     * set new display text for this UI object - doesn't change name
     * @param _str
     */
    public final void setLabel(String _str) {    
        label = _str;
        setDispLabel();
     }
    //add colon to label for display
    protected void setDispLabel() {_dispLabel = label + (label.length()>0 ? (isValueRange() ? " Range : " : " : ") : "");}

    /**
     * Set this UI object's value from a string
     * @param str
     */
    protected final void setValueFromString(String str) {
        double uiVal = Double.parseDouble(str);    
        setVal(uiVal);
    }
    
    private final String _getUIValueString() {
        if(isValueRange()) {
            return getRangeAsString();
        }
        return getValueAsString();
    }
    
    /**
     * What to display if this UI object is single line
     * @return
     */
    public final String getUIDispAsSingleLine() {return _dispLabel + _getUIValueString();}
    
    /**
     * What to display if this UI object is multi line
     * @return
     */
    public final String[] getUIDispAsMultiLine() {return new String[]{_dispLabel, _getUIValueString()};}       
    /**
     * Get Min/Max range as a string
     * @return
     */
    public final String getRangeAsString() {
        return "[" + 
                getValueAsString(minVal)+", "+ 
                getValueAsString(maxVal)+"]";}
    
    /**
     * Return the constant name assigned to this object on creation
     */
    public final String getName(){return name;}
    
    /**
     * The object ID passed to this object on creation. Corresponds to integer key in the containing map
     * @return
     */
    public final int getObjID() {return objID;}

    /**
     * Get this object's value as an int
     * @return
     */
    public final int getValueAsInt(){return (int)(val) ;}
    /**
     * Get this object's value as a float
     * @return
     */
    public final float getValueAsFloat(){return (float)( val);}
    
    /**
     * Get this UI object's value as a string with appropriate format
     * @return
     */
    public final String getValueAsString() {return getValueAsString(val);}
    
    /**
     * Get this UI object's value as a string - overridden by classes that do not use val directly
     * @return
     */
    protected String getValueAsString(double _val) {    return String.format(formatStr,_val);}
    
    /**
     * Get string data aarray representing the value this UI object holds
     * @return
     */
    protected String[] getStrDataForVal() {
        String[] tmpRes = new String[2];
        tmpRes[0] = "Value : "+ getValueAsString() +" Max Val : " + getValueAsString(maxVal) 
        + " Min Val : " + getValueAsString(minVal)+ " Mod multiplier : " +getValueAsString(modMult);
        tmpRes[1] = "Init Value : "+ getValueAsString(initVals[3]) +"|Init Max Val : "
        + getValueAsString(initVals[1]) + "|Init Min Val : " + getValueAsString(initVals[0])
        + "|Init Mod : " + getValueAsString(initVals[2]);
        return tmpRes;
    }

    
    /**
     * Retrive an array of string debug data
     * @return
     */
    public String[] getStrData(){
        String[] valResAra = getStrDataForVal();
        String[] tmpRes = new String[valResAra.length+4];
        int idx = 0;
        tmpRes[idx++] = "ID : "+ ID+"| Obj ID : " + objID  + "| Name : "+ name + "|Label : `" + label+"`";
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
    
    /**
     * For comparisons : requires ID to always be unique
     * @param anotherObj
     * @return
     */
    @Override
    public int compareTo(Base_GUIObj obj) {        return (ID > obj.ID ? 1 : (ID < obj.ID ? -1 : 0));    }
    
}//class Base_GUIObj
