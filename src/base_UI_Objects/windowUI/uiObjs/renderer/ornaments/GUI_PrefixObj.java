package base_UI_Objects.windowUI.uiObjs.renderer.ornaments;

import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.windowUI.uiObjs.renderer.ornaments.base.Base_GUIPrefixObj;

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
    private float[] boxDrawTrans;
    
    /**
     * Dimensions for prefix display box ornament
     */
    private final float[] boxDim = new float[] {-2.5f,-2.5f, 5.0f, 5.0f};

    /**
     * Where to start drawing this UI object
     */
    private float initDrawTransX;
    /**
     * 
     * @param _off
     * @param _fillColor
     */
    public GUI_PrefixObj(double[] _off, int[] _fillColor) {
        super();
        bxclr = new int[_fillColor.length];
        System.arraycopy(_fillColor, 0, bxclr, 0, _fillColor.length);
        //get offset values
        double xOff = _off[0];
        double yOff = _off[1];        
        //relative location of center based on owning ui object's location
        // This translation has to be retained so that actual object can be shifted appropriately
        initDrawTransX = (float)(xOff);
        //translation to get to upper corner - override once the owning renderer has a center point for y
        boxDrawTrans = new float[]{(float)(-xOff * .5f), (float)(yOff*.5f)};        
    }
        
    @Override
    public void drawPrefixObj(IGraphicsAppInterface ri) {
        //outside push/pop because translating beyond the initial for both rectangle and UI object
        ri.translate(initDrawTransX,0,0);
        ri.pushMatState();
            ri.setNoStroke();
            ri.setFill(bxclr,bxclr[3]);
            ri.translate(boxDrawTrans[0],boxDrawTrans[1],0);
            ri.drawRect(boxDim);
        ri.popMatState();
    }

    @Override
    public float getWidth() {return initDrawTransX;}

    @Override
    public float getHeight() {return boxDim[3];    }

    @Override
    public void setYCenter(float _y) {
        boxDrawTrans[1]=_y;        
    }
    
}//class GUI_PrefixOrnament