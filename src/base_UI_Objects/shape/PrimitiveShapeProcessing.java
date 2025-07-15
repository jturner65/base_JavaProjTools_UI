package base_UI_Objects.shape;

import base_Render_Interface.shape.GL_PrimitiveType;
import base_Render_Interface.shape.IPrimShapeInterface;
import base_Render_Interface.shape.PrimitiveType;
import base_UI_Objects.renderer.ProcessingRenderer;
import base_UI_Objects.shape.base.ProcessingShape;
import processing.core.PConstants;

/**
 * Wrapper of a processing shape class object for simple primitives (i.e. Point, Line, rect, sphere, etc)
 */
public class PrimitiveShapeProcessing extends ProcessingShape implements IPrimShapeInterface {
    
    /**
     * Creates a primitive, described by kind with details specified by params
     * @param _ri
     * @param kind
     * @param params
     */
    public PrimitiveShapeProcessing(ProcessingRenderer _ri,  PrimitiveType kind, float[] params) {
        super(_ri, getProcFamilyFromPrimType(kind), params);
    }
    /**
     * Not used for primitives
     */
    @Override
    public void gl_beginShape(GL_PrimitiveType primType) { }
    
    /**
     * Not used for primitives
     */
    @Override
    public void gl_endShape(boolean isClosed) {}
    
    /**
     * Return the processing integer constant corresponding to the passed GL_PrimitiveType
     * @param meshType
     * @return
     */
    public static final int getProcFamilyFromPrimType(PrimitiveType meshType) {
        switch (meshType) {
            case POINT :        { return PConstants.POINT;        }
            case LINE :         { return PConstants.LINE;         }
            case TRIANGLE :     { return PConstants.TRIANGLE;     }
            case QUAD :         { return PConstants.QUAD;         }
            case RECT :         { return PConstants.RECT;         }
            case ELLIPSE :      { return PConstants.ELLIPSE;      }
            case ARC :          { return PConstants.ARC;          }
            case SPHERE :       { return PConstants.SPHERE;       }
            case BOX :          { return PConstants.BOX;          }
            default :           { return PConstants.POINT;        }
        }         
    }
    
}//class PrimitiveShapeProcessing
