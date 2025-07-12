package base_UI_Objects.renderedObjs.base;

import base_Render_Interface.IGraphicsAppInterface;

/**
 * A full palette of colors to use with a render object
 * @author John Turner
 *
 */
public class RenderObj_ClrPalette {
    /**
     * Rendering functionality interface
     */
    protected static IGraphicsAppInterface ri;
    
    /**
     * This color is used across each different types of a specific render object
     */
    private RenderObj_Clr mainColor;    
    
    /**
     * This array of colors is used, indexed by render object type, for each instance of a specific render object 
     */
    private RenderObj_Clr[] allTypeColors;
    
    public RenderObj_ClrPalette(IGraphicsAppInterface _ri, int _numTypes) {
        ri=_ri;
        allTypeColors = new RenderObj_Clr[_numTypes];
    }
    
    public RenderObj_ClrPalette(RenderObj_ClrPalette _otr) {
        mainColor = RenderObj_Clr.makeRenderObjColor(_otr.mainColor); 
        allTypeColors = new RenderObj_Clr[_otr.allTypeColors.length];
        for (int i=0;i<allTypeColors.length;++i) {
            allTypeColors[i] = RenderObj_Clr.makeRenderObjColor(_otr.allTypeColors[i]); 
        }
    }
    
    /**
     * Retrieve the main color for this palette
     * @return
     */
    public final RenderObj_Clr getMainColor() {return mainColor;}
    
    /**
     * Retrieve the instance-specific color for this palette
     * @param _idx
     * @return
     */
    public final RenderObj_Clr getInstanceColor(int _idx) {return allTypeColors[_idx];}
    
    /**
     * Build a RenderObj_Clr object, with passed values for various color settings. Stroke is a scaled value of fill.
     * @param idx idx of color type array to build. If -1 then this is main color
     * @param fill fill color for object
     * @param stroke stroke color for object
     * @param emit emissive color
     * @param spec specular color
     * @param amb ambient color
     * @param stWt stroke weight
     * @param shn shininess 
     * @return
     */
    public void setColor(int idx, int[] fill,  int[] stroke, int[] emit, int[] spec, int[] amb, float stWt, float shn){
        if (idx < 0) {
            mainColor = RenderObj_Clr.makeRenderObjColor(ri, new int[][]{fill, stroke, emit, spec, amb}, stWt, shn);
        } else if (idx < allTypeColors.length){
            allTypeColors[idx] = RenderObj_Clr.makeRenderObjColor(ri, new int[][]{fill, stroke, emit, spec, amb}, stWt, shn);    
        } else {
            System.out.println("RenderObj_ClrPalette::setColor : !!!!! Error attempting to set color @ idx " +idx + " in array of length "+ allTypeColors.length+". Aborting!");
        }
        
    }//setColor    
    
    public void scaleMainFillColor(float _scaleVal) {mainColor.scaleFillColor(_scaleVal);}
    public void scaleInstanceFillColor(int _idx, float _scaleVal) {allTypeColors[_idx].scaleFillColor(_scaleVal);}
    public void scaleAllFillColors(float _scaleVal) {
        mainColor.scaleFillColor(_scaleVal);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].scaleFillColor(_scaleVal);}
    }
    
    
    public void scaleMainStrokeColor(float _scaleVal) {mainColor.scaleStrokeColor(_scaleVal);}
    public void scaleInstanceStrokeColor(int _idx, float _scaleVal) {allTypeColors[_idx].scaleStrokeColor(_scaleVal);}
    public void scaleAllStrokeColors(float _scaleVal) {
        mainColor.scaleStrokeColor(_scaleVal);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].scaleStrokeColor(_scaleVal);}
    }
    
    public void scaleMainEmissiveColor(float _scaleVal) {mainColor.scaleEmissiveColor(_scaleVal);}
    public void scaleInstanceEmissiveColor(int _idx, float _scaleVal) {allTypeColors[_idx].scaleEmissiveColor(_scaleVal);}
    public void scaleAllEmissiveColors(float _scaleVal) {
        mainColor.scaleEmissiveColor(_scaleVal);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].scaleEmissiveColor(_scaleVal);}
    }
    
    public void scaleMainSpecularColor(float _scaleVal) {mainColor.scaleSpecularColor(_scaleVal);}
    public void scaleInstanceSpecularColor(int _idx, float _scaleVal) {allTypeColors[_idx].scaleSpecularColor(_scaleVal);}
    public void scaleAllSpecularColors(float _scaleVal) {
        mainColor.scaleSpecularColor(_scaleVal);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].scaleSpecularColor(_scaleVal);}
    }
        
    public void scaleMainAmbientColor(float _scaleVal) {mainColor.scaleAmbientColor(_scaleVal);}
    public void scaleInstanceAmbientColor(int _idx, float _scaleVal) {allTypeColors[_idx].scaleAmbientColor(_scaleVal);}
    public void scaleAllAmbientColors(float _scaleVal) {
        mainColor.scaleAmbientColor(_scaleVal);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].scaleAmbientColor(_scaleVal);}
    }
    
    /**
     * Set fill color alpha
     * @param _clr
     */
    public void setMainFillColorAlpha(int _alpha) {mainColor.setFillClrAlpha(_alpha);}
    /**
     * Set fill color alpha for type color @ idx
     * @param _idx
     * @param _alpha
     */
    public void setInstanceFillColorAlpha(int _idx, int _alpha) {allTypeColors[_idx].setFillClrAlpha(_alpha);}
    /**
     * Set all fill color alpha
     * @param _alpha
     */
    public void setAllFillClrAlpha(int _alpha){
        mainColor.setFillClrAlpha(_alpha);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].setFillClrAlpha(_alpha);}
    }
    
    /**
     * Set stroke color alpha
     * @param _alpha
     */
    public void setMainStrokeColorAlpha(int _alpha) {mainColor.setStrokeClrAlpha(_alpha);}
    /**
     * Set stroke color alpha for type color @ idx
     * @param _idx
     * @param _alpha
     */
    public void setInstanceStrokeColorAlpha(int _idx, int _alpha) {allTypeColors[_idx].setStrokeClrAlpha(_alpha);}
    /**
     * Set all stroke color alpha
     * @param _alpha
     */
    public void setAllStrokeClrAlpha(int _alpha){
        mainColor.setStrokeClrAlpha(_alpha);
        for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].setStrokeClrAlpha(_alpha);}
    }
        
    public void enableFill(){mainColor.enableFill();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].enableFill();}}
    public void enableStroke(){mainColor.enableStroke();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].enableStroke();}}
    public void enableEmissive(){mainColor.enableEmissive();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].enableEmissive();}}
    public void enableSpecular(){mainColor.enableSpecular();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].enableSpecular();}}
    public void enableAmbient(){mainColor.enableAmbient();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].enableAmbient();}}
    public void enableShine(){mainColor.enableShine();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].enableShine();}}
    
    public void disableFill(){mainColor.disableFill();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].disableFill();}}
    public void disableStroke(){mainColor.disableStroke();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].disableStroke();}}
    public void disableEmissive(){mainColor.disableEmissive();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].disableEmissive();}}
    public void disableSpecular(){mainColor.disableSpecular();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].disableSpecular();}}
    public void disableAmbient(){mainColor.disableAmbient();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].disableAmbient();}}
    public void disableShine(){mainColor.disableShine();for (int i=0;i<allTypeColors.length;++i) {allTypeColors[i].disableShine();}}

        
        
}//class RenderObj_ClrPalette
