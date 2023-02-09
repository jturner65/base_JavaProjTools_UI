/**
 * 
 */
package base_UI_Objects.windowUI.uiObjs.menuObjs.base;

import java.util.concurrent.ThreadLocalRandom;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;

/**
 * Base class for main menu gui objects, that display with a prefix box ornament before UI Object text label
 * @author John Turner
 *
 */
public abstract class Base_MenuGUIObj extends Base_GUIObj {
	
	/**
	 * Color for prefix display box
	 */
	private final int[] bxclr;
	/**
	 * Transformation for prefix display box
	 */
	private final float[] boxDrawTrans;
	
	/**
	 * Dimensions for prefix display box ornament
	 */
	private final float[] boxDim = new float[] {-2.5f, -2.5f, 5.0f, 5.0f};

	/**
	 * Where to start drawing this UI object
	 */
	private float[] initDrawTrans;
	
	/**
	 * @param _p
	 * @param _objID
	 * @param _name
	 * @param _xst
	 * @param _yst
	 * @param _xend
	 * @param _yend
	 * @param _minMaxMod
	 * @param _initVal
	 * @param _objType
	 * @param _flags
	 * @param _off
	 */
	public Base_MenuGUIObj(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _minMaxMod, _initVal, _objType, _flags, _off);
		bxclr = new int[]{ThreadLocalRandom.current().nextInt(256),
				ThreadLocalRandom.current().nextInt(256),
				ThreadLocalRandom.current().nextInt(256),255};

		initDrawTrans = new float[]{(float)(start.x + xOff), (float)(start.y + yOff)};
		boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(-yOff*.25f)};
	}

	/**
	 * Draw this UI Object
	 */
	@Override
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
		p.popMatState();
	}//draw
}//class Base_MenuGUIObj
