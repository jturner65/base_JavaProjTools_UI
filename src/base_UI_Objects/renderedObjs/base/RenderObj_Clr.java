package base_UI_Objects.renderedObjs.base;

import base_Math_Objects.MyMathUtils;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.renderer.ProcessingRenderer;
import processing.core.PShape;

/**
 * class that will hold the relevant information for a particular color 
 * configuration for rendering an object, with functions to render 
 * to a PShape, as well as a passed IRenderInterface
 * @author John Turner
 */
public class RenderObj_Clr{
	protected static IRenderInterface p;	
	protected final static int[] tmpInit = new int[]{255,255,255,255};
	//alpha values for fill and stroke colors
	protected int[] alphas;
	//all values for various colors as hex
	protected int[] hexColors;
	protected float shininess, strkWt;
	
	protected int[] flags;		//bit flags for color
	public static final int 
				fillIDX 		= 0,
				strokeIDX 		= 1,
				emitIDX 		= 2,
				specIDX 		= 3,
				ambIDX 			= 4,
	//idxs above for colors array of arrays
				shnIDX			= 5;
	protected static final int numColorFlags = 5;
	protected static final int numFlags = 6;	
	
	/**
	 * binary digit idxs represent clr types that have alpha channel
	 */
	protected final int hasAlphaBits = 0B00011;
	
	/**
	 * Copy factory
	 * @param _otr
	 * @return
	 */
	public static RenderObj_Clr makeRenderObjColor(RenderObj_Clr _otr) {
		RenderObj_Clr clr = new RenderObj_Clr(_otr);
		return clr;
	}
	
	/**
	 * Factory to build render obj color object
	 * @param _p render interface
	 * @param _clrs 5 element array of int arrays, where idxs correspond to : 
	 * 	0 : fillIDX :  fill color 4 element int array
	 * 	1 : strokeIDX :  stroke color 4 element int array
	 * 	2 : emitIDX :  emissive color 3 element int array
	 * 	3 : specIDX :  ambient color 3 element int array
	 * 	4 : ambIDX :  specular color 3 element int array
	 * @param _scales 5 element array of float scaling values for above idxs
	 * @param stWt stroke weight
	 * @param shn shininess
	 * @return RenderObj_Clr object
	 */
	public static RenderObj_Clr makeRenderObjColor(IRenderInterface _p, int[][] _clrs, float[] _scales, float _stWt, float _shn) {
		RenderObj_Clr clr = new RenderObj_Clr(_p);
		clr.setFillClrVal(_clrs[fillIDX]);
		clr.setStrokeClrVal(_clrs[strokeIDX]);
		clr.setEmissiveClrVal(_clrs[emitIDX]);
		clr.setSpecularClrVal(_clrs[specIDX]);
		clr.setAmbientClrVal(_clrs[ambIDX]);
		clr.setStrokeWt(_stWt);
		clr.setShininess(_shn);	
		clr.scaleAllColors(_scales);
		return clr;
	}
	/**
	 * Factory to build render obj color object
	 * @param _p render interface
	 * @param _clrs 5 element array of int arrays, where idxs correspond to : 
	 * 	0 : fillIDX :  fill color 4 element int array
	 * 	1 : strokeIDX :  stroke color 4 element int array
	 * 	2 : emitIDX :  emissive color 3 element int array
	 * 	3 : specIDX :  ambient color 3 element int array
	 * 	4 : ambIDX :  specular color 3 element int array
	 * @param stWt stroke weight
	 * @param shn shininess
	 * @return RenderObj_Clr object
	 */
	public static RenderObj_Clr makeRenderObjColor(IRenderInterface _p, int[][] _clrs, float _stWt, float _shn) {
		//build unit scaling vector
		float[] scales = new float[_clrs.length];
		for(int i=0;i<scales.length;++i) {scales[i]=1.0f;}		
		return RenderObj_Clr.makeRenderObjColor(_p, _clrs, scales, _stWt, _shn);
	}
	
	////////////////
	/**
	 * private constructors
	 * @param _p
	 */
	private RenderObj_Clr(IRenderInterface _p){
		p=_p;
		shininess = 1.0f;
		strkWt = 1.0f;
		hexColors = new int[numColorFlags];
		alphas = new int[numColorFlags];
		//RGBA (alpha ignored as appropriate) - init all as white
		for(int i=0; i<numColorFlags;++i) {
			//initialize colors to be either ARGB or RGB white
			setColorsFromArray(tmpInit, i);
		}
		//init all flags as true
		initFlags();
	}
	
	/**
	 * privatge copy constructor
	 * @param _otr
	 */
	private RenderObj_Clr(RenderObj_Clr _otr){
		shininess = _otr.shininess;
		strkWt = _otr.strkWt;
		hexColors = new int[numColorFlags];
		alphas = new int[numColorFlags];
		System.arraycopy(_otr.hexColors, 0, hexColors, 0, numColorFlags);
		System.arraycopy(_otr.alphas, 0, alphas, 0, numColorFlags);		
		//copy all flags
		initFlags(_otr.flags);		
	}
	////////////////
	
	/**
	 * Whether the passed color idx has an alpha channel
	 */
	public boolean idxHasAlpha(int _idx) {
		int alphaBitLoc = 1<<(_idx%32);
		return ((hasAlphaBits & alphaBitLoc) == alphaBitLoc);
	}
	
	private void setColorsFromArray(int[] _srcClr, int _idx) {
		boolean _hasAlpha = idxHasAlpha(_idx);
		if(_hasAlpha) {			alphas[_idx] = _srcClr[3];} 
		else {					alphas[_idx] = -1;}
		hexColors[_idx] = p.getClrAsHex(_srcClr, alphas[_idx]);	
	}
	/**
	 * Set fill color
	 * @param _clr
	 */
	public void setFillClrVal(int[] _clr){setColorsFromArray(_clr, fillIDX);}
	/**
	 * Set stroke color
	 * @param _clr
	 */
	public void setStrokeClrVal(int[] _clr){setColorsFromArray(_clr, strokeIDX);}	
	
	/**
	 * Set fill color
	 * @param _clr
	 */
	public void setFillClrAlpha(int _alpha){
		int[] _clr = this.getFillClrAra();
		_clr[3] = _alpha;
		setColorsFromArray(_clr, fillIDX);}
	/**
	 * Set stroke color
	 * @param _clr
	 */
	public void setStrokeClrAlpha(int _alpha){
		int[] _clr = this.getStrokeClrAra();
		_clr[3] = _alpha;
		setColorsFromArray(_clr, strokeIDX);}
	
	/**
	 * Set emissive color
	 * @param _clr
	 */
	public void setEmissiveClrVal(int[] _clr){setColorsFromArray(_clr, emitIDX);}
	/**
	 * Set Specular color
	 * @param _clr
	 */
	public void setSpecularClrVal(int[] _clr){setColorsFromArray(_clr, specIDX);}
	/**
	 * Set Ambient color
	 * @param _clr
	 */
	public void setAmbientClrVal(int[] _clr){setColorsFromArray(_clr, ambIDX);}
	/**
	 * Set stroke weight
	 * @param _val
	 */	
	public void setStrokeWt(float _val) {strkWt = _val;}
	/**
	 * Set shininess value
	 * @param _val
	 */
	public void setShininess(float _val) {shininess = _val;}
	
	/**
	 * scale specified color by passed multiplier
	 * @param _idx
	 * @param _mult
	 */
	private void _scalePassedColor(int _idx, float _mult) {
		if (_mult == 1.0f) {return;}
		int[] _clr;
		if (_mult <= 0) {
			_clr = new int[] {0,0,0,0};
		} else {
			_clr = p.getClrFromHex(hexColors[_idx]);
			for(int i=0;i<_clr.length;++i) {_clr[i] = MyMathUtils.max(MyMathUtils.min((int) (_clr[i] * _mult), 255), 0);}
		}
		setColorsFromArray(_clr, _idx);
	}//_scalePassedColor
	
	/**
	 * Scale all the colors in this object by the float values passed
	 * @param _scales
	 */
	public void scaleAllColors(float[] _scales) {
		int scaleCount = (numColorFlags > _scales.length ? _scales.length : numColorFlags);
		for(int i=0;i<scaleCount;++i) {
			if(_scales[i] == 1.0f) {continue;}
			_scalePassedColor(i, _scales[i]);
		}
	}
	
	/**
	 * Scale this object's fill color by passed multiplier, restricting to legal color values
	 * @param _mult
	 */
	public void scaleFillColor(float _mult) {_scalePassedColor(fillIDX, _mult);}
	/**
	 * Scale this object's stroke color by passed multiplier, restricting to legal color values
	 * @param _mult
	 */
	public void scaleStrokeColor(float _mult) {_scalePassedColor(strokeIDX, _mult);}
	/**
	 * Scale this object's emissive color by passed multiplier, restricting to legal color values
	 * @param _mult
	 */
	public void scaleEmissiveColor(float _mult) {_scalePassedColor(emitIDX, _mult);}
	/**
	 * Scale this object's specular color by passed multiplier, restricting to legal color values
	 * @param _mult
	 */
	public void scaleSpecularColor(float _mult) {_scalePassedColor(specIDX, _mult);}
	/**
	 * Scale this object's ambient color by passed multiplier, restricting to legal color values
	 * @param _mult
	 */
	public void scaleAmbientColor(float _mult) {_scalePassedColor(ambIDX, _mult);}
	

	/**
	 * Return stroke weight
	 * @return
	 */
	public float getStrkWt(){return strkWt;}
	/**
	 * Return shininess
	 * @return
	 */
	public float getShininess(){return shininess;}
	
	/**
	 * Get int array of fill color, with alpha
	 * @return
	 */
	public int[] getFillClrAra() {return p.getClrFromHex(hexColors[fillIDX]);}
	
	/**
	 * Get int array of stroke color, with alpha
	 * @return
	 */
	public int[] getStrokeClrAra() {return p.getClrFromHex(hexColors[strokeIDX]);}
	
	/**
	 * Get 3-element int array of emissive color
	 * @return
	 */
	public int[] getEmissiveClrAra() {return p.getClrFromHex(hexColors[emitIDX]);}
	
	/**
	 * Get 3-element int array of specular color
	 * @return
	 */
	public int[] getSpecularClrAra() {return p.getClrFromHex(hexColors[specIDX]);}
	
	/**
	 * Get 3-element int array of ambient color
	 * @return
	 */
	public int[] getAmbientClrAra() {return p.getClrFromHex(hexColors[ambIDX]);}
	
	/**
	 * Get hex fill color
	 * @return
	 */
	public int getHexFillClr() {return hexColors[fillIDX];}
	/**
	 * Get hex stroke color
	 * @return
	 */
	public int getHexStrokeClr() {return hexColors[strokeIDX];}
	/**
	 * Get hex emissive color
	 * @return
	 */
	public int getHexEmissiveClr() {return hexColors[emitIDX];}
	/**
	 * Get hex ambient color
	 * @return
	 */
	public int getHexAmbientClr() {return hexColors[ambIDX];}
	/**
	 * Get hex specular color
	 * @return
	 */
	public int getHexSpecularClr() {return hexColors[specIDX];}

	/**
	 * instance all activated colors in passed PShape for constructed PShape, set all colors.  This is called 
	 * between "beginShape/endShape"
	 * @param sh the shape to receive these colors
	 */
	public void shPaintColors(PShape sh){
		if(getFlags(fillIDX)){sh.fill(hexColors[fillIDX], alphas[fillIDX]);}
		else {		sh.noFill();}
		if(getFlags(strokeIDX)){
			sh.strokeWeight(strkWt);
			sh.stroke(hexColors[strokeIDX],alphas[strokeIDX]);
		} else {			sh.noStroke();		}
		if(getFlags(specIDX)){sh.specular(hexColors[specIDX]);}
		if(getFlags(emitIDX)){sh.emissive(hexColors[emitIDX]);}
		if(getFlags(ambIDX)){sh.ambient(hexColors[ambIDX]);}
		if(getFlags(shnIDX)){sh.shininess(shininess);}
	}

	
	/**
	 * instance all activated colors in passed PShape for constructed PShape, set all colors.  Not between "beginShape/endShape"
	 * @param sh the shape to receive these colors
	 */
	public void shSetShapeColors(PShape sh){
		if(getFlags(fillIDX)){sh.setFill(hexColors[fillIDX]);}
		else {		sh.setFill(false);}
		if(getFlags(strokeIDX)){
			sh.setStrokeWeight(strkWt);			
			sh.setStroke(hexColors[strokeIDX]);
		} else {	sh.setStroke(false);}
		if(getFlags(specIDX)){sh.setSpecular(hexColors[specIDX]);}
		if(getFlags(emitIDX)){sh.setEmissive(hexColors[emitIDX]);}
		if(getFlags(ambIDX)){sh.setAmbient(hexColors[ambIDX]);}
		if(getFlags(shnIDX)){sh.setShininess(shininess);}
	}
	
	/**
	 * instance all activated colors globally
	 */
	public void paintColors(){
		if(getFlags(fillIDX)){p.setFill(hexColors[fillIDX]);}
		else {		p.setNoFill();}
		if(getFlags(strokeIDX)){
			p.setStrokeWt(strkWt);
			p.setStroke(hexColors[strokeIDX]);
		} else {			p.noStroke();		}
		if(getFlags(specIDX)){((ProcessingRenderer) p).specular(hexColors[specIDX]);}
		if(getFlags(emitIDX)){((ProcessingRenderer) p).emissive(hexColors[emitIDX]);}
		if(getFlags(ambIDX)){((ProcessingRenderer) p).ambient(hexColors[ambIDX]);}
		if(getFlags(shnIDX)){((ProcessingRenderer) p).shininess(shininess);}
	}
	
	/**
	 * apply this color's fill color globally, scaled by mult
	 * @param mult
	 */
	public void setScaledFillClr(float mult){
		int[] clrAra = p.getClrFromHex(hexColors[fillIDX]);
		p.setFill((int)(mult*clrAra[0]),(int)(mult*clrAra[1]),(int)(mult*clrAra[2]),255);
	}
	
	public void setFlags(int idx, boolean val){setPrivFlag(idx, val);}
	public boolean getFlags(int idx){return getPrivFlag(flags, idx);}
	
	public void enableFill(){setPrivFlag(fillIDX, true);}
	public void enableStroke(){setPrivFlag(strokeIDX, true);}
	public void enableEmissive(){setPrivFlag(emitIDX, true);}
	public void enableSpecular(){setPrivFlag(specIDX, true);}
	public void enableAmbient(){setPrivFlag(ambIDX, true);}
	public void enableShine(){setPrivFlag(shnIDX, true);}
	
	public void disableFill(){setPrivFlag(fillIDX, false);}
	public void disableStroke(){setPrivFlag(strokeIDX, false);}
	public void disableEmissive(){setPrivFlag(emitIDX, false);}
	public void disableSpecular(){setPrivFlag(specIDX, false);}
	public void disableAmbient(){setPrivFlag(ambIDX, false);}
	public void disableShine(){setPrivFlag(shnIDX, false);}
	
	/**
	 * Copy flags from other flag array
	 * @param _flags
	 */
	private void initFlags(int[] _flags) {flags = new int[_flags.length]; for(int i=0;i<flags.length;++i) {flags[i]=_flags[i];}}
	
	private void initFlags(){flags = new int[1 + numFlags/32];for(int i =0; i<numFlags;++i){setFlags(i,true);}}
	
	protected void setPrivFlag(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		flags[flIDX] = (val ?  flags[flIDX] | mask : flags[flIDX] & ~mask);
		switch(idx){
			case fillIDX 	: { break;}	
			case strokeIDX 	: {	break;}	
			case emitIDX 	: {	break;}	
			case specIDX 	: {	break;}	
			case ambIDX 	: {	break;}	
		}				
	}//setFlags
	protected static boolean getPrivFlag(int[] flags, int idx){int bitLoc = 1<<(idx%32);return (flags[idx/32] & bitLoc) == bitLoc;}	
}//myRndrObjClr
