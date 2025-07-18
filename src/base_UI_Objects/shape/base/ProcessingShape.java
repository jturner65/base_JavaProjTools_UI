package base_UI_Objects.shape.base;

import base_Render_Interface.shape.base.IShapeInterface;
import base_UI_Objects.renderer.ProcessingRenderer;
import processing.core.PShape;

public abstract class ProcessingShape extends PShape implements IShapeInterface {
    public static ProcessingRenderer ri;
    
    /**
     * GL_based shape built with glBegin-glEnd
     * @param _ri
     * @param family
     */
    public ProcessingShape(ProcessingRenderer _ri, int family) {
        super(_ri.g, family);
        ri=_ri;
    }

    /**
     * Constructor for primitive shapes - follows processing convention
     * @param _ri
     * @param kind kind of primitive shape
     * @param params essential parameters for primitive shape
     */
    public ProcessingShape(ProcessingRenderer _ri,  int kind, float[] params) {
        super(_ri.g, PRIMITIVE);
        super.setKind(kind);
        super.setParams(params);
        ri=_ri;
    }

    /**
     * Set normal for smooth shading
     * @param x
     * @param y
     * @param z
     */
    @Override
    public final void gl_normal(float x, float y, float z) {super.normal(x,y,z);}                                          // changes normal for smooth shading
    /**
     * Set a vertex for drawing shapes
     * @param P
     */
    @Override
    public final void gl_vertex(float x, float y, float z) {super.vertex(x,y,z);}                                             // vertex for shading or drawing
    /**
     * Set a vertex for drawing shapes with u, v coords
     * @param P
     */
    @Override
    public final void gl_vertex(float x, float y, float z, float u, float v) {super.vertex(x,y,z,u,v);} 
    /**
     * set fill color by value during shape building - only inside begin/end
     * @param clr 1st 3 values denote integer color vals
     * @param alpha 
     */
    @Override
    public final void gl_setFill(int r, int g, int b, int alpha) {super.fill(r,g,b,alpha);}
    /**
     * Set to have no fill color for a specified object (within gl_beginShape/gl_endShape) - only inside begin/end
     */
    @Override
    public final void gl_setNoFill() {super.noFill();}
    /**
     * set stroke color by value during shape building - only inside begin/end
     * @param clr rgba
     * @param alpha 
     */
    @Override
    public final void gl_setStroke(int r, int g, int b, int alpha) {super.stroke(r,g,b,alpha);}
    /**
     * Set to have no stroke color for a specified object (within gl_beginShape/gl_endShape) - only inside begin/end
     */
    @Override
    public final void gl_setNoStroke() {super.noStroke();}
    /**
     * Set stroke weight for a specified object (within gl_beginShape/gl_endShape) - only inside begin/end
     * @param wt
     */
    @Override
    public final void gl_setStrokeWt(float wt) {super.strokeWeight(wt);}
    
    ////////////////////////
    /// Commands for -outside- begin/end
    
    @Override
    public final void setFill(int r, int g, int b, int alpha) {super.setFill(getClrAsHex(r,g,b,alpha));}

    @Override
    public final void setNoFill() {super.setFill(false);}

    @Override
    public final void setStroke(int r, int g, int b, int alpha) {super.setStroke(getClrAsHex(r,g,b,alpha));}

    @Override
    public final void setNoStroke() {super.setStroke(false);}

    @Override
    public final void setStrokeWt(float stW) {super.setStrokeWeight(stW);}

    /**
     * Set ambient color to be passed hex color - only outside begin/end
     */
    @Override
    public void setAmbient(int _hexClr) {super.setAmbient(_hexClr);}
    /**
     * Set specular color to be passed hex color
     */
    @Override
    public void setSpecular(int _hexClr) {  super.setSpecular(_hexClr); }
    /**
     * Set emissive color to be passed hex color
     */
    @Override
    public void setEmissive(int _hexClr) { super.setEmissive(_hexClr);}
    /**
     * Set shininess to be passed float value
     */
    @Override
    public void setShininess(float shininess) { super.setShininess(shininess);}
    
    
    ////////////////////////
    //transformations    
    @Override
    public final void translate(float x, float y){super.translate(x,y);}
    @Override
    public final void translate(float x, float y, float z){super.translate(x,y,z);}

    @Override
    public final void rotate(float thet, float x, float y, float z) {super.rotate(thet, x, y, z);}

    @Override
    public final void scale(float x) {super.scale(x);}
    @Override
    public final void scale(float x,float y) {super.scale(x, y);}
    @Override
    public final void scale(float x,float y,float z) {super.scale(x,y,z);}   
    ////////////////////////
    // end transformations
    
    @Override
    public final void draw() {  ri.shape(this);   }

    @Override
    public final void setIsVisible(boolean isVis) {     super.setVisible(isVis);  }
    @Override
    public final boolean isVisible() {return super.isVisible();}

    @Override
    public final int getNumChildren() { return super.getChildCount();}

    @Override
    public final void addChildShape(IShapeInterface child) {              super.addChild(((PShape)child));}

    @Override
    public final void addChildShape(IShapeInterface child, int idx) {     super.addChild(((PShape)child), idx);  }

    
    /**
     * Get the child of this IMeshInterface object specified by the given layer
     * @param layer
     * @return
     */
    @Override
    public final IShapeInterface getChildShape(int layer) {return ((ProcessingShape)super.getChild(layer));}
    /**
     * Get the child of this IMeshInterface object with the given name
     * @param name
     * @return
     */
    @Override
    public final IShapeInterface getChildShape(String name) {return ((ProcessingShape)super.getChild(name));}

}
