package base_UI_Objects.renderedObjs;

import base_Math_Objects.MyMathUtils;
import base_Render_Interface.IGraphicsAppInterface;
import base_Render_Interface.shape.IMeshInterface;
import base_Render_Interface.shape.IPrimShapeInterface;
import base_Render_Interface.shape.PrimitiveType;
import base_UI_Objects.renderedObjs.base.Base_RenderObj;
import base_UI_Objects.renderedObjs.base.RenderObj_Clr;
import base_UI_Objects.renderedObjs.base.RenderObj_ClrPalette;

/**
 * jellyfish pshape, with multiple component shapes that are animated
 * @author John Turner
 *
 */
public class JFish_RenderObj extends Base_RenderObj {
    /**
     * if overall geometry has been made or not
     */
    private static boolean[] madeForType = null;
    /**
     * individual static object representation. Any animation will be owned by the instancing class
     */
    private static IMeshInterface[] objReps = null;
    /**
     * Deformed spheres to make up the body
     */
    private static IMeshInterface[][] bodyAra = null;
    
    /**
     * colors for jellyfish reps
     */    
    private static RenderObj_ClrPalette clrPalette;
    
    private static double maxAnimCntr = 1000.0;

    
    public JFish_RenderObj(IGraphicsAppInterface _p, int _type, int _numTypes, int _numAnimFrames, RenderObj_ClrPalette _clrPalette)  {    
        super(_p, _type, _numTypes, _numAnimFrames, _clrPalette);
    }//ctor
    
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
            objReps = new IMeshInterface[_numTypes];
            for(int i=0;i<_numTypes;++i) {objReps[i]=(IMeshInterface) createObjRepForType();}
        }
        if (bodyAra == null) {
            bodyAra = new IMeshInterface[_numTypes][];
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
    
    @Override
    protected void setMainColorPalette(RenderObj_ClrPalette _clrPalette){
        clrPalette = new RenderObj_ClrPalette(_clrPalette);
    }            

    @Override
    protected RenderObj_Clr getObjTypeColor() {
        return clrPalette.getInstanceColor(type);
    }
    
    /**
     * builds geometry for object to be instanced - only perform once per object type 
     */
    @Override
    protected void initObjGeometry() {
        //make all bodies of a cycle of animation - make instances in buildObj
        float radAmt = (MyMathUtils.TWO_PI_F/(1.0f*numAnimFrames));
        //Precalc scaling animation
        float[][] scaleVec = new float[numAnimFrames][3];
        for(int a=0; a<numAnimFrames; ++a){//for each frame of animation    
            float sclMult = (float) ((Math.sin(a * radAmt) * .25f) +1.0f);
            scaleVec[a] = new float[] {sclMult, sclMult, 1.0f/(sclMult * sclMult)};
        }
        // Build deformed bodies
        for(int i=0;i<bodyAra.length;++i) {
            bodyAra[i] = new IMeshInterface[numAnimFrames];
            ri.setSphereDetail(20);
            for(int a=0; a<numAnimFrames; ++a){//for each frame of animation
                // each body element is a sphere with a certain scale setting to simulate deformation
                bodyAra[i][a] = ri.createBaseGroupMesh();
                IPrimShapeInterface indiv = ri.createBasePrim(PrimitiveType.SPHERE, 5.0f);                
                //sclMult = (float) ((Math.sin(a * radAmt) * .25f) +1.0f);
                //indiv.scale(sclMult, sclMult, 1.0f/(sclMult * sclMult));
                indiv.scale(scaleVec[a][0], scaleVec[a][1],scaleVec[a][2]);                
                //call shSetPaintColors since we need to use set<type> style functions of Pshape when outside beginShape-endShape
                clrPalette.getInstanceColor(i).shSetShapeColors(indiv);        
                bodyAra[i][a].addChildShape(indiv);
            }    
        }
        ri.setSphereDetail(5);            
    }
    //any instance specific, jelly-fish specific geometry setup goes here (textures, sizes, shapes, etc)
    @Override
    protected void initInstObjGeometryIndiv() {
        //build instance    
    }//initInstObjGeometry

    @Override
    protected void buildObj() {
        //build the boid's body geometry here - called at end of initInstObjGeometry
    }

    @Override
    protected void drawMeIndiv(int animIDX) {
        //draw animation index-specified deformed "jellyfish"
        objReps[type].draw();     
        bodyAra[type][animIDX].draw();
    }
    @Override
    public final double getMaxAnimCounter() {return maxAnimCntr;}
    @Override
    public final void setMaxAnimCounter(double _maxAnimCntr) {maxAnimCntr = _maxAnimCntr;}
}
