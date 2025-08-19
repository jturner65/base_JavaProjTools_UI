package base_UI_Objects.windowUI.uiObjs.menuObjs.numeric;

import java.util.ArrayList;
import java.util.Arrays;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * UI object that supports selecting between a list of available values.
 */
public class GUIObj_List extends GUIObj_Int {
    
    /**
     * List of different values to be displayed for this list-based object
     */
    protected String[] listVals = new String[]{"None"};
    
    /**
     * Original list of different values to be displayed for this list based-object
     */
    protected String[] origListVals;
    
    /**
     * 
     * Build a list-based UI object
     * @param _objID the index of the object in the managing container
     * @param _name the name/display label of the object
     * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
     * @param _initVal the initial value of this object
     * @param _objType Type of object (for child classes)
     * @param _flags any preset behavior flags
     * @param _listVals Initial list of values this object holds
     */
    public GUIObj_List(int _objID, GUIObj_Params objParams) {
        super(_objID, objParams);
        setListVals(objParams.getListVals(), true);
    }

    /**
     * Instance-specific reset
     */
    @Override
    protected void resetToDefault_Indiv() {
        //reset list values to be original list values
        listVals = new String[origListVals.length];
        System.arraycopy(origListVals, 0, listVals, 0, origListVals.length);
    }
    
    /**
     * Set a new modifier value to use for this object : Mod values for list-based objects will always be 1
     * @param _unused
     */
    @Override
    public void setNewMod(double _unused){    
        modMult = 1.0;
        formatStr = "%.0f";
    }
        
    /**
     * Get this UI object's value as a string
     * @return
     */
    @Override
    protected final String getValueAsString(double _val) {    return listVals[(int)forceBounds(_val)];}
    
    /**
     * return the string representation corresponding to the passed index in the list of this object's values, if any exist
     * @param idx index in list of value to retrieve
     * @return
     */
    public final String getListValStr(int idx) {    return getValueAsString(idx);}
    
    /**
     * Get all list values
     */
    public final String[] getListValues() {return listVals.clone();}
    
    /**
     * Set the passed list as the original/default list for this object
     * @param vals
     */
    private void _setNewDefaultList(String[] vals) {
        origListVals = new String[vals.length];
        System.arraycopy(vals, 0, origListVals, 0, vals.length);    
    }
    
    /**
     * Set this list object's list of values
     * @param vals The new list of values to set for this object
     * @param setAsDefault Whether these values should be set as the default values (i.e. reloaded on reset)
     * @return returns current val cast to int as idx
     */
    public final int setListVals(String[] vals, boolean setAsDefault) {
        if((null == vals) || (vals.length == 0)) {    listVals = new String[]{"List Not Initialized!"};    } 
        else {
            listVals = new String[vals.length];
            System.arraycopy(vals, 0, listVals, 0, vals.length);
            if (setAsDefault) {_setNewDefaultList(listVals);}
        }
        //Update new max value
        double curVal = getVal();
        setNewMax(listVals.length-1);
        curVal = setVal(curVal);
        updateRenderer();
        return (int) curVal;        
    }
    
    /**
     * Add a string to the end of this list vals
     * @param val
     * @param setAsDefault
     * @return
     */
    public final int addListVal(String val, boolean setAsDefault) {
        if(!val.isEmpty()) {
            ArrayList<String> tmpAra = new ArrayList<String>(Arrays.asList(listVals));
            tmpAra.add(val);
            listVals = tmpAra.toArray(new String[0]);        
            if(setAsDefault) {_setNewDefaultList(listVals);}
        }
        //Update new max value
        double curVal = listVals.length-1;
        setNewMax(listVals.length-1);
        curVal = setVal(curVal);
        updateRenderer();
        return (int) curVal;    
    }
    
    /**
     * Get the number of entries this list UI object supports
     */
    public final int getNumEntries() {return listVals.length;}
    
    /**
     * Return the index of the passed string in the array of values this object manages.
     * @param tok the string to find
     * @return
     */
    public final int getIDXofStringInArray(String tok) {
        for(int i=0;i<listVals.length;++i) {if(listVals[i].trim().equals(tok.trim())) {return i;}}
        return -1;
    }
    
    /**
     * set list to display passed token, if it exists, otherwise return -1
     * @param tok string in list to display
     * @return ara [idx of string in list, otherwise -1, 0 if ok, 1 if bad]
     */
    public final int[] setValInList(String tok) {
        int idx = getIDXofStringInArray(tok);
        if(idx >=0){        return new int[] {(int) setVal(idx), 0};}
        return new int[] {idx, 1};
    }
    
    /**
     * Get string data array representing the value this list-based UI object holds - overrides Base_GUIObj impl.
     * @return
     */
    @Override
    protected String[] getStrDataForVal() {
        String[] tmpRes = new String[(1 + listVals.length) + (1 + origListVals.length)];
        tmpRes[0] = "Current Value: `"+ getValueAsString() + "`|Index in list : " + getValueAsInt() + "| Current List of values:";
        int i;
        for(i=0;i<listVals.length;++i) {tmpRes[i+1] = "\tidx" + i + ":"+getListValStr(i);    }
        tmpRes[i++] = "Init Value : "+ getValueAsString(initVals[3]) + "`|Index in list : " + ((int) initVals[3]) + "| Original List of values:";
        for(int j=i;j<i+origListVals.length;++j) {int listIdx = (j-i);tmpRes[j+1] = "\tidx" + listIdx + ":"+origListVals[listIdx];    }
        return tmpRes;
    }
}//class myGUIObj_List
