package base_UI_Objects.windowUI.uiObjs.ornaments;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.ornaments.base.Base_GUIPrefixObj;

/**
 * Class to hold and draw the individual ornamental box that is displayed
 * in front of UI elements
 * @author John Turner
 *
 */
public class GUI_PrefixObj extends Base_GUIPrefixObj{	
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
	
	public GUI_PrefixObj(double[] _off, int[] _fillColor) {
		super();
		bxclr = new int[_fillColor.length];
		System.arraycopy(_fillColor, 0, bxclr, 0, _fillColor.length);
		//get offset values
		double xOff = _off[0];
		double yOff = _off[1];		
		//relative location of center based on owning ui object's location
		initDrawTrans = new float[]{(float)(xOff), (float)(.75f* yOff)};
		//translation to get to upper corner
		boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(-yOff*.25f)};
	}
		
	@Override
	public void drawPrefixObj(IRenderInterface ri) {
		//outside push/pop because translating beyond the initial for both rectangle and UI object
		ri.translate(initDrawTrans[0],initDrawTrans[1],0);
		ri.pushMatState();
			ri.noStroke();
			ri.setFill(bxclr,bxclr[3]);
			ri.translate(boxDrawTrans[0],boxDrawTrans[1],0);
			ri.drawRect(boxDim);
		ri.popMatState();
	}
	
}//class GUI_PrefixOrnament