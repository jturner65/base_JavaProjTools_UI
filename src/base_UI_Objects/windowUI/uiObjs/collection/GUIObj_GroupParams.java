package base_UI_Objects.windowUI.uiObjs.collection;

import java.util.LinkedHashMap;

import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.renderer.base.GUIObjRenderer_Flags;

/**
 * This class is for aggregating a grouping of GUIObj_Params for a row or grid of UI Objects
 */

public class GUIObj_GroupParams extends GUIObj_Params{
    
    /**
     * Collection of obj params making up a single collection - can be of individual obj params or of collections (i.e. for a grid of objects)
     */
    private LinkedHashMap<String, GUIObj_Params> _paramsGroup;

    /**
     * Convenience ref for first entry, to denote that it should start on a new row
     */
    private GUIObj_Params firstEntry = null;
    
    /**
     * Number of objects to set per line when building the hotspots and renderers
     */
    private int _numObjsPerLine;
    
    /**
     * 
     * @param _objIdx
     */
    public GUIObj_GroupParams() {
        super(-1, "non-display object collection", GUIObj_Type.IntVal, -1,  new boolean[0], new GUIObjRenderer_Flags() , new boolean[0]);
        _paramsGroup = new LinkedHashMap<String, GUIObj_Params>();
    }
    
    public GUIObj_GroupParams(GUIObj_GroupParams otr) {
        super(otr);
        _paramsGroup = new LinkedHashMap<String, GUIObj_Params>();
        if(_isAGroupOfObjs) {
            for (var mapEntry : otr._paramsGroup.entrySet()) {
                var objKey = mapEntry.getKey();
                var objVal = mapEntry.getValue();
                _paramsGroup.put(objKey, new GUIObj_Params(objVal));
            }
        }        
    }
    
    /**
     * Add a single object to the end of the current collection of 
     * @param params
     */
    public final void addObjectsToCollection(String _key, GUIObj_Params _params) {        
        _paramsGroup.put(_key, _params);
        if(firstEntry == null) {    firstEntry = _params;}
        _isAGroupOfObjs = true;    
        _numObjsPerLine = _paramsGroup.size();
    }
    
    /**
     * Change the value for this group. Having the value be high can help keep things on a single line and use up unused space.
     * @param numObjs
     */
    public final void setNumObjsPerLine(int numObjs) {
        _numObjsPerLine = numObjs;
    }
    
    /**
     * Reset to be only the number of objects in the group.
     */
    public final void resetNumObjsPerLine() {
        _numObjsPerLine = _paramsGroup.size();
    }
    
    public final int getNumObjsPerLine() {return _numObjsPerLine;}
    
    /**
     * Retrieve the map holding the group of parameters, held in insertion order
     * @return
     */
    public final LinkedHashMap<String, GUIObj_Params> getParamsGroupMap(){
        // condition renderer flags for last entry
        if(!firstEntry.isAGroupOfObjs()) {        firstEntry.setIsFirstObjOnRow();}    
        return _paramsGroup;}
}// class GUIObj_GroupParams
