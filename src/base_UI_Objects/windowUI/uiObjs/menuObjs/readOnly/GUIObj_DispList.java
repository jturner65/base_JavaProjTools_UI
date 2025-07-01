package base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly;

import java.util.ArrayList;
import java.util.Arrays;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.readOnly.base.Base_ReadOnlyGUIObj;

public class GUIObj_DispList extends Base_ReadOnlyGUIObj {
    
    /**
     * List of different values to be displayed for this list-based object
     */
    protected String[] listVals = new String[]{"None"};
    
    /**
     * Original list of different values to be displayed for this list based-object
     */
    protected String[] origListVals;
    
    public GUIObj_DispList(int _objID, GUIObj_Params objParams) {
        super(_objID, objParams);
        setListVals(objParams.getListVals(), true);
    }

    /**
     * Set the passed list as the original/default list for this object
     * @param vals
     */
    private void setNewDefaultList(String[] vals) {
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
        if((null == vals) || (vals.length == 0)) {  listVals = new String[]{"List Not Initialized!"};   } 
        else {
            listVals = new String[vals.length];
            System.arraycopy(vals, 0, listVals, 0, vals.length);
            if (setAsDefault) {setNewDefaultList(listVals);}
        }
        //Update new max value
        double curVal = getVal();
        setNewMax(listVals.length-1);
        curVal = setVal(curVal);
        if(renderer != null) {      renderer.updateFromObject();        }
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
            if(setAsDefault) {setNewDefaultList(listVals);}
        }
        //Update new max value
        double curVal = listVals.length-1;
        setNewMax(listVals.length-1);
        curVal = setVal(curVal);
        if(renderer != null) {      renderer.updateFromObject();        }
        return (int) curVal;    
    }
    /**
     * Get this UI object's value as a string
     * @return
     */
    @Override
    protected final String getValueAsString(double _val) {  return listVals[(int)forceBounds(_val)];}
    
    /**
     * Get the number of entries this list UI object supports
     */
    public final int getNumEntries() {return listVals.length;}
    
}//class GUIObj_DispList
