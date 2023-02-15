package base_UI_Objects.renderedObjs;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.renderedObjs.base.Base_RenderObj;
import base_UI_Objects.renderedObjs.base.RenderObj_Clr;
import base_UI_Objects.renderedObjs.base.RenderObj_ClrPalette;
import processing.core.PConstants;
import processing.core.PShape;

public class Sphere_RenderObj extends Base_RenderObj {
	//if overall geometry has been made or not
	private static boolean made;
	
	/**
	 * colors for sphere reps
	 */	
	private static RenderObj_ClrPalette clrPalette;

	private static double maxAnimCntr = 1000.0;
	
	public Sphere_RenderObj(IRenderInterface _p, int _type, RenderObj_ClrPalette _clrPalette) {
		//sphere has no animation
		super(_p, _type, 1, _clrPalette);
	}//ctor
	
	@Override
	protected void setMainColorPalette(RenderObj_ClrPalette _clrPalette) {
		clrPalette = new RenderObj_ClrPalette(_clrPalette);
//		mainColor = makeColor(sphrFillClrs[baseObjIDX], sphrFillClrs[baseObjIDX], new int[]{0,0,0,0}, specClr, clrStrkDiv[baseObjIDX], strkWt, shn);
//		mainColor.disableStroke();
//		mainColor.disableAmbient();
//		// have all flock colors available initially to facilitate first-time creation
//		for (int i=0;i<allTypeColors.length;++i) {
//			allTypeColors[i] =  makeColor(sphrFillClrs[i], sphrFillClrs[i], new int[]{0,0,0,0}, specClr,clrStrkDiv[i], strkWt, shn);
//			allTypeColors[i].disableStroke();
//			allTypeColors[i].disableAmbient();
//		}	}
	}
	@Override
	protected RenderObj_Clr getObjTypeColor() {
		return clrPalette.getInstanceColor(type);
	}
	/**
	 * Get per-species boolean defining whether or not species-wide geometry has been completed. 
	 * Each species should (class inheriting from this class) should have its own static 'made' boolean,
	 * which this provides access to.
	 */
	@Override
	protected boolean getObjMade() {return made;}

	/**
	 * Set per-species boolean defining whether or not species-wide geometry has been completed. 
	 * Each species should (class inheriting from this class) should have its own static 'made' boolean,
	 * which this provides access to.
	 */
	@Override
	protected void setObjMade(boolean isMade) {made = isMade;}
	
	/**
	 * Instantiate objRep object
	 * @return
	 */
	@Override
	protected final PShape createObjRepForType() {
		int tmpDet = p.getSphereDetail();
		p.setSphereDetail(5);
		PShape obj = createBaseShape(PConstants.SPHERE, 5.0f);
		p.setSphereDetail(tmpDet);
		return obj;
	}
	
	//no custom geometry for sphere
	@Override
	protected void initObjGeometry() {	}
	//since this is a sphere, override default to create a different object type (instead of group)

	@Override
	protected void initInstObjGeometryIndiv(){
		//call shSetShapeColors since we need to use set<type> style functions of Pshape when outside beginShape-endShape
		setObjRepClrsFromObjClr();
	}

	//no need for specific object-building function for spheres
	@Override
	protected void buildObj(PShape _objRep) {	}
	//nothing special (per-frame) for sphere render object
	@Override
	protected void drawMeIndiv(int idx) {}
	@Override
	public final double getMaxAnimCounter() {return maxAnimCntr;}
	@Override
	public final void setMaxAnimCounter(double _maxAnimCntr) {maxAnimCntr = _maxAnimCntr;}
}//class mySphereRndrObj
