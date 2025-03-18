package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.TreeMap;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.GUI_AppWinVals;
import base_UI_Objects.windowUI.uiObjs.base.*;
import base_UI_Objects.windowUI.uiObjs.base.base.*;
import base_UI_Objects.windowUI.uiObjs.menuObjs.*;
import base_Utils_Objects.io.messaging.MessageObject;

/**
 * This class will manage all aspects of UI object creation, placement, rendering and interaction.
 * TODO Build an interface with necessary methods for this construct for its owner to interact with.
 * @author John Turner
 *
 */
public class Base_uiObjectManager {
	/**
	 * Used to render objects
	 */
	public static IRenderInterface ri;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;
	/**
	 * Owning construct initialization values
	 */
	protected final GUI_AppWinVals winInitVals;
	
	/**
	 * subregion of window where UI objects may be found
	 */
	public float[] uiClkCoords;	
		
	/**
	* array lists of idxs for float-based UI objects
	*/
	private ArrayList<Integer> guiFloatValIDXs;
	/**
	* array lists of idxs for integer/list-based objects
	*/
	private ArrayList<Integer> guiIntValIDXs;	
	
	//UI objects in this window
	//GUI Objects
	private Base_NumericGUIObj[] guiObjs_Numeric;	
	
	public Base_uiObjectManager(GUI_AppWinVals _winInitVals, IRenderInterface _ri, MessageObject _msgObj, float[] _uiClkCoords) {
		ri = _ri;
		msgObj = _msgObj;
		winInitVals = _winInitVals;
		uiClkCoords = _uiClkCoords;
	}

	// UI object creation
	//build UI clickable region
	public final void initUIClickCoords(float x1, float y1, float x2, float y2){uiClkCoords[0] = x1;uiClkCoords[1] = y1;uiClkCoords[2] = x2; uiClkCoords[3] = y2;}
	public final void initUIClickCoords(float[] cpy){	uiClkCoords[0] = cpy[0];uiClkCoords[1] = cpy[1];uiClkCoords[2] = cpy[2]; uiClkCoords[3] = cpy[3];}
	
	/**
	 * build ui objects from maps, keyed by ui object idx, with value being data
	 * @param tmpUIObjArray : map of object data, keyed by UI object idx, with array values being :                    
	 *           the first element double array of min/max/mod values                                                   
	 *           the 2nd element is starting value                                                                      
	 *           the 3rd elem is label for object                                                                       
	 *           the 4th element is object type (GUIObj_Type enum)
	 *           the 5th element is boolean array of : (unspecified values default to false)
	 *           	{value is sent to owning window, 
	 *           	value is sent on any modifications (while being modified, not just on release), 
	 *           	changes to value must be explicitly sent to consumer (are not automatically sent)}    
	 * @param tmpListObjVals
	 */
	public float _buildGUIObjsForMenu(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals) {
		int numGUIObjs = tmpUIObjArray.size();
		
		double[][] guiMinMaxModVals = new double[numGUIObjs][3];			//min max mod values
		double[] guiStVals = new double[numGUIObjs];						//starting values
		String[] guiObjNames = new String[numGUIObjs];						//display labels for UI components	
		//idx 0 is value is sent to owning window, 
		//idx 1 is value is sent on any modifications, 
		//idx 2 is if true, then changes to value are not sent to UIDataUpdater structure automatically
		boolean[][] guiBoolVals = new boolean[numGUIObjs][];				//array of UI flags for UI objects
				
		GUIObj_Type[] guiObjTypes = new GUIObj_Type[numGUIObjs];
		myPointf[][] corners = new myPointf[numGUIObjs][2];
		myPointf stPt = new myPointf(uiClkCoords[0], uiClkCoords[1], 0);
		myPointf endPt = new myPointf(uiClkCoords[2], uiClkCoords[1]+winInitVals.getTextHeightOffset(), 0);
			
		for (int i = 0; i < numGUIObjs; ++i) {
			Object[] obj = tmpUIObjArray.get(i);
			guiMinMaxModVals[i] = (double[]) obj[0];
			guiStVals[i] = (Double)(obj[1]);
			guiObjNames[i] = (String)obj[2];
			guiObjTypes[i] = (GUIObj_Type)obj[3];
			if(guiObjTypes[i] == GUIObj_Type.FloatVal) {
				guiFloatValIDXs.add(i);
			} if(guiObjTypes[i] == GUIObj_Type.Button) {
				msgObj.dispConsoleErrorMessage(
						"Base_uiObjectManager", "_buildGUIObjsForMenu", 
						"Attempting to add unsupported gui obj type : `"+guiObjTypes[i].toStrBrf() + "` at idx : "+i);
			
			} else {
				//int and list values are considered ints
				guiIntValIDXs.add(i);
			}
			corners[i] = new myPointf[] {new myPointf(stPt), new myPointf(endPt)};
			boolean[] tmpAra = (boolean[])obj[4];
			guiBoolVals[i] = new boolean[(tmpAra.length < 5 ? 5 : tmpAra.length)];
			int idx = 0;
			for (boolean val : tmpAra) {
				guiBoolVals[i][idx++] = val;
			}
			//move box down by text height
			stPt._add(0, winInitVals.getTextHeightOffset(), 0);
			endPt._add(0, winInitVals.getTextHeightOffset(), 0);
		}
		//Make a smaller padding amount for final row
		uiClkCoords[3] =  stPt.y - .5f*winInitVals.getTextHeightOffset();
		
		//build all objects using these values 
		_buildAllObjects(guiObjNames, corners, guiMinMaxModVals, guiStVals, guiBoolVals, guiObjTypes, tmpListObjVals, winInitVals.getUIOffset());

		// return final y coordinate
		return uiClkCoords[3];
	}//_buildGUIObjsFromMaps
	
	/**
	 * This will build objects sequentially using the values provided
	 * @param guiObjNames name of each object
	 * @param corners 2-element point array of upper left and lower right corners for object
	 * @param guiMinMaxModVals array of 3 element arrays of min and max value and base modifier
	 * @param guiStVals array of per-object initial values
	 * @param guiBoolVals array of boolean flags describing each object's configuration
	 * @param guiObjTypes array of per-object types
	 * @param tmpListObjVals map keyed by object idx where the value is a string array of elements to put in a list object
	 * @parram UI_Off Either the ui offset to use for a prefixing ornament before the object's label, or null
	 */
	public void _buildAllObjects(
			String[] guiObjNames, 
			myPointf[][] corners, 
			double[][] guiMinMaxModVals, 
			double[] guiStVals, 
			boolean[][] guiBoolVals, 
			GUIObj_Type[] guiObjTypes, 
			TreeMap<Integer, String[]> tmpListObjVals, 
			double[] UI_off) {
		int numListObjs = 0;
		for(int i =0; i< guiObjNames.length; ++i){
			switch(guiObjTypes[i]) {
				case IntVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Int(ri, i, guiObjNames[i], corners[i][0], corners[i][1], guiMinMaxModVals[i], 
							guiStVals[i], guiBoolVals[i], UI_off);					
					break;}
				case ListVal : {
					++numListObjs;
					guiObjs_Numeric[i] = new MenuGUIObj_List(ri, i, guiObjNames[i], corners[i][0], corners[i][1], guiMinMaxModVals[i], 
							guiStVals[i], guiBoolVals[i], UI_off, tmpListObjVals.get(i));
					break;}
				case FloatVal : {
					guiObjs_Numeric[i] = new MenuGUIObj_Float(ri, i, guiObjNames[i], corners[i][0], corners[i][1], guiMinMaxModVals[i], 
							guiStVals[i], guiBoolVals[i], UI_off);					
					break;}
				case Button  :{
					//TODO
					msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Attempting to instantiate unknown UI object ID for a " + guiObjTypes[i].toStrBrf());
					break;
				}
				default : {
					msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Attempting to instantiate unknown UI object for a " + guiObjTypes[i].toStrBrf());
					break;				
				}
			}
		}
		if(numListObjs != tmpListObjVals.size()) {
			msgObj.dispWarningMessage("Base_uiObjectManager", "_buildAllObjects", "Error!!!! # of specified list select UI objects ("+numListObjs+") does not match # of passed lists ("+tmpListObjVals.size()+") - some or all of specified list objects will not display properly.");
		}	
	}//_buildAllObjects	
	
	
	// UI object interaction
	
	
	// UI object rendering
	
	
}//class uiObjectManager
