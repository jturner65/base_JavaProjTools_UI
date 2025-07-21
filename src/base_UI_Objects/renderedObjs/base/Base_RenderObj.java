package base_UI_Objects.renderedObjs.base;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.renderer.ProcessingRenderer;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;

/**
 * Base class describing a rendered mesh object
 * @author John Turner
 */
public abstract class Base_RenderObj {
    /**
     * Rendering functionality
     */
    protected static IGraphicsAppInterface ri;

    protected static final float
        pi4thrds = 4*MyMathUtils.THIRD_PI_F, 
        pi100th = .01f*MyMathUtils.PI_F, 
        pi6ths = .5f*MyMathUtils.THIRD_PI_F;
    
    /**
     * Type of object this represents
     */
    protected int type;

    /**
     * color defined for this particular object - also query for UI menu color
     */
    protected RenderObj_Clr objectColor;
    
    /**
     * # of animation frames to use for this rendered object
     */
    public int numAnimFrames = 90;    
    
    protected PImage[] textures;
    
    /**
     * # of types of this kind of render object
     */
    protected int numTypes;
    
    //private final String className;
    
    /**
     * Class to allow for prebuilding complex rendered representations of objects as pshapes
     * @param _p
     * @param _type
     * @param _numTypes
     * @param _numAnimFrames # of frames for a single cycle of repeated animation
     * @param _clrPalette
     * @param _textures
     */
    public Base_RenderObj(IGraphicsAppInterface _p, int _type, int _numTypes, int _numAnimFrames, RenderObj_ClrPalette _clrPalette, PImage[] _textures) {
        ri=_p; type = _type; numTypes=_numTypes; numAnimFrames = _numAnimFrames;
        textures = _textures;//className = this.getClass().getSimpleName();
        initObjMadeForTypeAndObjReps(numTypes);
        setObjMadeForType(initGeometry(_clrPalette),type);
    }
    
    public Base_RenderObj(IGraphicsAppInterface _p, int _type, int _numTypes, int _numAnimFrames, RenderObj_ClrPalette _clrPalette) {
        this(_p, _type, _numTypes, _numAnimFrames, _clrPalette, new PImage[0]);
    }
    
    /**
     * build geometry of object, including 1-time species-specific setup
     * @return true, denoting 1-time setup is complete
     */
    protected final boolean initGeometry(RenderObj_ClrPalette _clrPalette){
        //base colors for all objects of this species
        setMainColorPalette(_clrPalette);
        //set per-type color 
        objectColor = getObjTypeColor();
        //global setup for instance class object type
        if(!getObjMadeForType(type)){
            //perform this only once for all types of render objects

            //set up species-wide render object geometry
            initObjGeometry(); 
            //any per-type (child class) setup required    
            initInstObjGeometryIndiv();
            //create object/objRep for this type
            buildObj();            
        }//if not made yet initialize geometry to build this object
        //individual per-instance/type setup - need to not be static since window can change
        initInstObjGeometry();        
        return true;        
    }
    
    /**
     * Initialize the Object Made array of per-type booleans for instancing species
     * @param _type
     */
    protected abstract void initObjMadeForTypeAndObjReps(int _numTypes);
    /**
     * Get per-species per subtype boolean defining whether or not species-wide geometry has been completed. 
     * Each species should (class inheriting from this class) should have its own static 'made' boolean,
     * which this provides access to.
     */
    protected abstract boolean getObjMadeForType(int _type);
    /**
     * Set per-species boolean defining whether or not species-wide geometry has been completed. 
     * Each species should (class inheriting from this class) should have its own static 'made' boolean,
     * which this provides access to.
     */
    protected abstract void setObjMadeForType(boolean isMade, int _type);

    /**
     * initialize base colors for this object - only perform once per object type/species  
     */
    protected abstract void setMainColorPalette(RenderObj_ClrPalette _clrPalette);
    
    /**
     * Builds geometry for species of object to be instanced - only perform once per object type/species 
     */
    protected abstract void initObjGeometry();
    
    /**
     * Set a texture to use for the passed shape via texture array index. Call between beginShape and endShape
     */
    protected void setObjTexture(PShape sh, int idx) {
        if((textures==null) || (idx >= textures.length)) {return;}
        //sh.texture(textures[idx]);
    }
    /**
     * Builds instance of rendered object
     */
    protected final void initInstObjGeometry(){
//        objRep = createObjRepForType();//createBaseShape(getMainMeshType()); 
//        //set per-type color 
//        objectColor = getObjTypeColor();
//        //any per-type (child class) setup required    
//        initInstObjGeometryIndiv();
//        //create object/objRep
//        buildObj(objRep);            
    }//    initInstObjGeometry
    
    /**
     * Instantiate objRep object as group
     * @return
     */
    protected PShape createObjRepForType() {
        return createBaseGroupShape();         
    }

    /**
     * builds specific instance of object render rep, including colors, textures, etc.
     */
    protected abstract void initInstObjGeometryIndiv();        
    
    /**
     * retrieve renderObject_clr for an instance of this object
     */
    protected abstract RenderObj_Clr getObjTypeColor();    
    
    /**
     * build the instance of a particular object
     */
    protected abstract void buildObj();
    
    /**
     * Create a shape intended to be the parent of a group of shapes/meshes
     * @return
     */
    protected PShape createBaseGroupShape() {
        return ((ProcessingRenderer) ri).createShape(PConstants.GROUP);
    }
    
    /**
     * Create and return a processing shape (PShape) with passed arg list. 
     * TODO : replace with agnostic mesh someday.
     * @param meshType PConstants-defined constant specifying the type of shape to create
     * @param args a (possibly empty) list of arguments
     * @return a Processing PShape with specified criteria
     */    
    protected PShape createBaseShape(int meshType, float... args) {
        return createBaseShape(false, meshType, args);
    }
    /**
     * Create and return a processing shape (PShape) with no args. 
     * TODO : replace with agnostic mesh someday.
     * @return a Processing PShape with specified criteria
     */    
    protected PShape createBaseShape() {
        return createBaseShape(true, -1);
    }
    
    /**
     * Create and return a processing shape (PShape) with passed arg list. 
     * TODO : replace with agnostic mesh someday.
     * @param isEmpty If no meshType is provided
     * @param meshType PConstants-defined constant specifying the type of shape to create
     * @param args a (possibly empty) list of arguments
     * @return a Processing PShape with specified criteria
     */    
    private PShape createBaseShape(boolean isEmpty, int meshType, float... args) {
        if (isEmpty) {
            return ((ProcessingRenderer) ri).createShape();
        }
        if (args.length == 0) {
            return ((ProcessingRenderer) ri).createShape(meshType);
        }
        return ((ProcessingRenderer) ri).createShape(meshType, args);
    }
    
    /**
     * build quad shape from object points using object base color
     * @param transVec
     * @param numX
     * @param btPt
     * @param objRndr
     * @return
     */
    protected int buildQuadShape(PShape _objRep, myPointf transVec, int numX, int btPt, myPointf[][] objRndr){
        PShape sh = createBaseShape();
        sh.translate(transVec.x,transVec.y,transVec.z);
        sh.beginShape(PConstants.QUADS);
            objectColor.shPaintColors(sh);
            for(int i = 0; i < numX; ++i){
                shgl_vertex(sh,objRndr[btPt][0]);shgl_vertex(sh,objRndr[btPt][1]);shgl_vertex(sh,objRndr[btPt][2]);shgl_vertex(sh,objRndr[btPt][3]);btPt++;
            }//for i                
        sh.endShape(PConstants.CLOSE);
        _objRep.addChild(sh);        
        return btPt;
    }
        
    /**
     * Create a PShape transform matrix. Necessary because post 3.3.3 processing got rid of pshape matrix stack
     * @param transVec First applied transform - initial translation
     * @param scaleVec Scale applied after translate
     * @param rotAra First applied rotation
     * @param trans2Vec 2nd Applied translation
     * @param rotAra2 2nd Applied rotation
     * @param trans3Vec 3rd Applied translation
     * @param rotAra3 3rd Applied rotation
     * @return PShape created and transformed using passed transforms
     */
    private PMatrix3D buildMatrixTransform(myPointf transVec, myPointf scaleVec, float[] rotAra, myPointf trans2Vec, float[] rotAra2, myPointf trans3Vec, float[] rotAra3) {
        PMatrix3D mat = new PMatrix3D();
        mat.translate(transVec.x, transVec.y, transVec.z);
        mat.scale(scaleVec.x,scaleVec.y,scaleVec.z);
        mat.rotate(rotAra[0],rotAra[1],rotAra[2],rotAra[3]);
        mat.translate(trans2Vec.x, trans2Vec.y, trans2Vec.z);
        mat.rotate(rotAra2[0],rotAra2[1],rotAra2[2],rotAra2[3]);
        mat.translate(trans3Vec.x, trans3Vec.y, trans3Vec.z);
        mat.rotate(rotAra3[0],rotAra3[1],rotAra3[2],rotAra3[3]);    
        return mat;
    }
    
    /**
     * Create a PShape and set its initial transformations.
     * @param transVec First applied transform - initial translation
     * @param scaleVec Scale applied after translate
     * @param rotAra First applied rotation
     * @param trans2Vec 2nd Applied translation
     * @param rotAra2 2nd Applied rotation
     * @param trans3Vec 3rd Applied translation
     * @param rotAra3 3rd Applied rotation
     * @return PShape created and transformed using passed transforms
     */
    protected PShape createAndSetInitialTransform(myPointf transVec, myPointf scaleVec, float[] rotAra, myPointf trans2Vec, float[] rotAra2, myPointf trans3Vec, float[] rotAra3){    
        PShape sh = createBaseShape();
        PMatrix3D mat = buildMatrixTransform(transVec, scaleVec, rotAra, trans2Vec, rotAra2, trans3Vec, rotAra3);
        sh.applyMatrix(mat);
        return sh;
    }
    
    /**
     * build a pole
     * @param poleNum
     * @param clr
     * @param rad
     * @param height
     * @param drawBottom
     * @param transVec
     * @param scaleVec
     * @param rotAra
     * @param trans2Vec
     * @param rotAra2
     * @param trans3Vec
     * @param rotAra3
     * @return
     */
    protected PShape buildPole(
            int poleNum, RenderObj_Clr clr, 
            float rad, float height, 
            boolean drawBottom, 
            myPointf transVec, myPointf scaleVec, float[] rotAra, 
            myPointf trans2Vec, float[] rotAra2, 
            myPointf trans3Vec, float[] rotAra3){
        float theta, rsThet, rcThet, rsThet2, rcThet2;
        int numTurns = 6;
        float twoPiOvNumTurns = MyMathUtils.TWO_PI_F/numTurns;
        PShape shRes = createBaseGroupShape(), sh;
        //pre-calc rad-theta-sin and rad-theta-cos
        float[] rsThetAra = new float[1 + numTurns];
        float[] rcThetAra = new float[1 + numTurns];
        for(int i = 0; i <numTurns; ++i){
            theta = i * twoPiOvNumTurns;
            rsThetAra[i] = (float) (rad*Math.sin(theta));
            rcThetAra[i] = (float) (rad*Math.cos(theta));
        }
        //wrap around value - theta == 0
        rsThetAra[numTurns] = 0.0f;
        rcThetAra[numTurns] = rad;
        
        for(int i = 0; i <numTurns; ++i){
            rsThet = rsThetAra[i];
            rcThet = rcThetAra[i];
            rsThet2 = rsThetAra[i+1];
            rcThet2 = rcThetAra[i+1];

            sh = createAndSetInitialTransform(transVec, scaleVec, rotAra, trans2Vec, rotAra2, trans3Vec, rotAra3);
            sh.beginShape(PConstants.QUADS);                      
                clr.shPaintColors(sh);
                shgl_vertexf(sh,rsThet, 0, rcThet );
                shgl_vertexf(sh,rsThet, height,rcThet);
                shgl_vertexf(sh,rsThet2, height,rcThet2);
                shgl_vertexf(sh,rsThet2, 0, rcThet2);
            sh.endShape(PConstants.CLOSE);    
            shRes.addChild(sh);
            //caps
            sh = createAndSetInitialTransform(transVec, scaleVec, rotAra, trans2Vec, rotAra2, trans3Vec, rotAra3);
            sh.beginShape(PConstants.TRIANGLES);                      
                clr.shPaintColors(sh);
                shgl_vertexf(sh,rsThet, height, rcThet );
                shgl_vertexf(sh,0, height, 0 );
                shgl_vertexf(sh,rsThet2, height, rcThet2 );
            sh.endShape(PConstants.CLOSE);
            shRes.addChild(sh);
            
            if(drawBottom){
                sh = createAndSetInitialTransform(transVec, scaleVec, rotAra, trans2Vec, rotAra2, trans3Vec, rotAra3);                
                sh.beginShape(PConstants.TRIANGLES);
                    clr.shPaintColors(sh);
                    shgl_vertexf(sh,rsThet, 0, rcThet );
                    shgl_vertexf(sh,0, 0, 0 );
                    shgl_vertexf(sh,rsThet2, 0, rcThet2);
                sh.endShape(PConstants.CLOSE);
                shRes.addChild(sh);
            }
        }//for i
        return shRes;
    }//buildPole    
    
    //set background for menu color, darkening a bit so that bright colors are still visible on white background
    public void setMenuColor(){
        objectColor.setScaledFillClr(.9f);
    }
    
    /**
     * Get maximum ticks for animation, if object is animated
     * @return
     */
    public abstract double getMaxAnimCounter();
    public abstract void setMaxAnimCounter(double _maxAnimCntr);
    
    /**
     * instance a shape and draw it with given animation phase
     * @param animPhase
     * @param objID
     */
    public final void drawMe(double animPhase, int objID){
        //ProcessingRenderer.AppMgr.drawAxes(10.0, 2.0f, new myPoint(), 255, false);
        drawMeIndiv((int)(animPhase * numAnimFrames));
    }
    /**
     * draw object
     * @param animIDX index in animation array to use
     */
    protected abstract void drawMeIndiv(int animPhase);
    
    //public void shgl_vTextured(PShape sh, myPointf P, float u, float v) {sh.vertex(P.x,P.y,P.z,u,v);}                          // vertex with texture coordinates
    public void shgl_vertexf(PShape sh, float x, float y, float z){sh.vertex(x,y,z);}     // vertex for shading or drawing
    public void shgl_vertex(PShape sh, myPointf P){sh.vertex(P.x,P.y,P.z);}     // vertex for shading or drawing
    public void shgl_normal(PShape sh, myVectorf V){sh.normal(V.x,V.y,V.z);    } // changes normal for smooth shading

}//abstract class myRenderObj

