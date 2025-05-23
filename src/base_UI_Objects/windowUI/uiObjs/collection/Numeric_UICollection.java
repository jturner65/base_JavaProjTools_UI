package base_UI_Objects.windowUI.uiObjs.collection;

import java.util.TreeMap;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.collection.base.Base_UIObjectCollection;

/**
 * A collection object holding 1 or more (non-button) Base_NumericGUIObj objects
 */
public class Numeric_UICollection extends Base_UIObjectCollection {	
	
	public Numeric_UICollection(IRenderInterface _ri, float[] _uiClkCoords, TreeMap<Integer, String[]> tmpListObjVals, TreeMap<Integer, Object[]> tmpUIObjArray) {
		super(_ri, _uiClkCoords);
		guiObjsAra = new Base_GUIObj[tmpUIObjArray.size()]; // list of modifiable gui objects
		
		//build ui objects
		//uiClkCoords[3] = _buildGUIObjsForMenu(tmpUIObjArray, tmpListObjVals, uiClkCoords);	

	}

	@Override
	protected void drawDbgGUIObjsInternal(float animTimeMod) {
		ri.pushMatState();	
			for(int i =0; i<guiObjsAra.length; ++i){guiObjsAra[i].drawDebug();}
		ri.popMatState();			
	}


	@Override
	protected void drawGUIObjsInternal(int msClkObj, float animTimeMod) {
		ri.pushMatState();	
			//mouse highlight
			if (msClkObj != -1) {	guiObjsAra[msClkObj].drawHighlight();	}
			for(int i =0; i<guiObjsAra.length; ++i){guiObjsAra[i].draw();}
		ri.popMatState();	
	}

}//class Numeric_UICollection
