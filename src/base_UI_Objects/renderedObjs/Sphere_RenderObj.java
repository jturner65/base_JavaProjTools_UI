package base_UI_Objects.renderedObjs;

import base_Render_Interface.IGraphicsAppInterface;
import base_Render_Interface.shape.PrimitiveType;
import base_Render_Interface.shape.base.IShapeInterface;
import base_UI_Objects.renderedObjs.base.Base_RenderObj;
import base_UI_Objects.renderedObjs.base.RenderObj_Clr;
import base_UI_Objects.renderedObjs.base.RenderObj_ClrPalette;

public class Sphere_RenderObj extends Base_RenderObj {
    /**
     * if overall geometry has been made or not
     */
    private static boolean[] madeForType = null;
    /**
     * individual static object representation. Any animation will be owned by the instancing class
     */
    private static IShapeInterface[] objReps = null;
    
    /**
     * colors for sphere reps
     */    
    private static RenderObj_ClrPalette clrPalette;

    private static double maxAnimCntr = 1000.0;
    
    public Sphere_RenderObj(IGraphicsAppInterface _p, int _type, int _numTypes, RenderObj_ClrPalette _clrPalette) {
        //sphere has no animation
        super(_p, _type, _numTypes, 1, _clrPalette);
    }//ctor
    
    @Override
    protected void setMainColorPalette(RenderObj_ClrPalette _clrPalette) {
        clrPalette = new RenderObj_ClrPalette(_clrPalette);
//        mainColor = makeColor(sphrFillClrs[baseObjIDX], sphrFillClrs[baseObjIDX], new int[]{0,0,0,0}, specClr, clrStrkDiv[baseObjIDX], strkWt, shn);
//        mainColor.disableStroke();
//        mainColor.disableAmbient();
//        // have all flock colors available initially to facilitate first-time creation
//        for (int i=0;i<allTypeColors.length;++i) {
//            allTypeColors[i] =  makeColor(sphrFillClrs[i], sphrFillClrs[i], new int[]{0,0,0,0}, specClr,clrStrkDiv[i], strkWt, shn);
//            allTypeColors[i].disableStroke();
//            allTypeColors[i].disableAmbient();
//        }    }
    }
    @Override
    protected RenderObj_Clr getObjTypeColor() {
        return clrPalette.getInstanceColor(type);
    }
    /**
     * Initialize the Object Made array of per-type booleans for instancing species
     * @param _type
     */
    @Override
    protected  void initObjMadeForTypeAndObjReps(int _numTypes) {
        if(madeForType == null) {
            madeForType = new boolean[_numTypes];
            for(int i=0;i<_numTypes;++i) {madeForType[i]=false;}
        }
        if(objReps == null) {
            objReps = new IShapeInterface[_numTypes];
            for(int i=0;i<_numTypes;++i) {objReps[i]=createObjRepForType();}
        }
    }
    
    /**
     * Get per-species per subtype boolean defining whether or not species-wide geometry has been completed. 
     * Each species should (class inheriting from this class) should have its own static 'made' boolean,
     * which this provides access to.
     */
    @Override
    protected boolean getObjMadeForType(int _type) {return madeForType[_type];}

    /**
     * Set per-species boolean defining whether or not species-wide geometry has been completed. 
     * Each species should (class inheriting from this class) should have its own static 'made' boolean,
     * which this provides access to.
     */
    @Override
    protected void setObjMadeForType(boolean isMade, int _type) {madeForType[_type] = isMade;}
    
    /**
     * Instantiate objRep object
     * @return
     */
    @Override
    protected final IShapeInterface createObjRepForType() {
        int tmpDet = ri.getSphereDetail();
        ri.setSphereDetail(5);
        var obj = ri.createBasePrim(PrimitiveType.SPHERE, 5.0f);
        ri.setSphereDetail(tmpDet);
        return obj;
    }
    
    //no custom geometry for sphere
    @Override
    protected void initObjGeometry() {    }
    //since this is a sphere, override default to create a different object type (instead of group)

    @Override
    protected void initInstObjGeometryIndiv(){
        //call shSetShapeColors since we need to use set<type> style functions of Pshape when outside beginShape-endShape
        objectColor.shSetShapeColors(objReps[type]);
    }

    //no need for specific object-building function for spheres
    @Override
    protected void buildObj() {    }
    //nothing special (per-frame) for sphere render object
    @Override
    protected void drawMeIndiv(int idx) {
       objReps[type].draw();
    }
    @Override
    public final double getMaxAnimCounter() {return maxAnimCntr;}
    @Override
    public final void setMaxAnimCounter(double _maxAnimCntr) {maxAnimCntr = _maxAnimCntr;}
}//class mySphereRndrObj
