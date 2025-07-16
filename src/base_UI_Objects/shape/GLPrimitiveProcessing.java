package base_UI_Objects.shape;

import base_Render_Interface.shape.GL_PrimitiveType;
import base_Render_Interface.shape.IMeshInterface;
import base_UI_Objects.renderer.ProcessingRenderer;
import base_UI_Objects.shape.base.ProcessingShape;
import processing.core.PConstants;
import processing.core.PShape;

/**
 * Wrapper of a processing shape class object for glBegin-built constructs
 */
public class GLPrimitiveProcessing extends ProcessingShape implements IMeshInterface {
    public static ProcessingRenderer ri;
    
    /**
     * creates a processing group shape
     * @param _ri
     */
    public GLPrimitiveProcessing(ProcessingRenderer _ri) {       super(_ri, PShape.GROUP);}
    
    /**
     * For Geometry
     * @param _ri
     */
    public GLPrimitiveProcessing(ProcessingRenderer _ri, int _type) {super(_ri, _type);}
    /**
     * Creates a processing shape based on gl primitives (built with begin)
     * @param _ri
     * @param family
     */
    public GLPrimitiveProcessing(ProcessingRenderer _ri, GL_PrimitiveType meshType) {     super(_ri, getProcFamilyFromGLPrimType(meshType));  }

    /**
     * Beginning a shape without specification will be interpreted as a polygon
     */
    @Override
    public final void gl_beginShape() {        super.beginShape(PConstants.POLYGON);   }
    /**
     * type needs to be -1 for blank, otherwise should be specified in PConstants
     * 
     * (from PConstants) - these are allowed elements in glBegin function
          static final int POINTS          = 3;   // vertices
          static final int LINES           = 5;   // beginShape(), createShape()
          static final int LINE_STRIP      = 50;  // beginShape()
          static final int LINE_LOOP       = 51;
          static final int TRIANGLES       = 9;   // vertices
          static final int TRIANGLE_STRIP  = 10;  // vertices
          static final int TRIANGLE_FAN    = 11;  // vertices
          
          static final int QUADS           = 17;  // vertices
          static final int QUAD_STRIP      = 18;  // vertices
          
          static final int POLYGON         = 20;  // 
     * 
     * 
     */
    @Override
    public final void gl_beginShape(GL_PrimitiveType primType) {
        super.beginShape(getProcFamilyFromGLPrimType(primType));
    }//gl_beginShape

    @Override
    public void gl_endShape(boolean isClosed) {
        super.endShape(isClosed ? PConstants.CLOSE : PConstants.OPEN);
    }

    /**
     * Set ambient color to be passed hex color - only inside begin/end
     */
    @Override
    public void gl_setAmbient(int _hexClr) {    super.ambient(_hexClr);}
    /**
     * Set specular color to be passed hex color - only inside begin/end
     */
    @Override
    public void gl_setSpecular(int _hexClr) {   super.specular(_hexClr);}
    /**
     * Set emissive color to be passed hex color - only inside begin/end
     */
    @Override
    public void gl_setEmissive(int _hexClr) {   super.emissive(_hexClr);}
    /**
     * Set shininess to be passed float value - only inside begin/end
     */
    @Override
    public void gl_setShininess(float shine) {  super.shininess(shine);}
    
    
    
    /**
     * Return the processing integer constant corresponding to the passed GL_PrimitiveType
     * @param meshType
     * @return
     */
    public static final int getProcFamilyFromGLPrimType(GL_PrimitiveType meshType) {
        switch (meshType) {
            case GL_POINTS :        { return PConstants.POINTS;        }
            case GL_LINES :         { return PConstants.LINES;         }
            case GL_LINE_LOOP :     { 
              //Processing does not support line loop, so treat as polygon
                return PConstants.POLYGON;     }           
            case GL_LINE_STRIP :    { 
              //Processing does not support line_strip, treat as lines
                return PConstants.LINES;    }           
            case GL_TRIANGLES :     { return PConstants.TRIANGLES;     }
            case GL_TRIANGLE_STRIP :{ return PConstants.TRIANGLE_STRIP;}
            case GL_TRIANGLE_FAN :  { return PConstants.TRIANGLE_FAN;  }
            case GL_QUADS :         { return PConstants.QUADS;         }
            case GL_QUAD_STRIP :    { return PConstants.QUAD_STRIP;    }
            case GL_POLYGON :       { return PConstants.POLYGON;       }
            default :               { return PConstants.POLYGON;       }        
        }         
    }

    @Override
    public boolean isGLPrim() {return true;}


}// class GLPrimitiveProcessing
