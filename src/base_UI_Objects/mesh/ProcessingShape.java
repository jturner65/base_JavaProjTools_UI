package base_UI_Objects.mesh;

import base_Render_Interface.GL_PrimStyle;
import base_Render_Interface.IMeshInterface;
import base_UI_Objects.renderer.ProcessingRenderer;
import processing.core.PConstants;
import processing.core.PShape;

public class ProcessingShape extends PShape implements IMeshInterface {
    public static ProcessingRenderer ri;
    
    public ProcessingShape(ProcessingRenderer _ri) {
        super();
        ri=_ri;
    }

    public ProcessingShape(ProcessingRenderer _ri, int family) {
        super(_ri.g, family);
        ri=_ri;
    }
    
    public ProcessingShape(ProcessingRenderer _ri,  int kind, float... params) {
        super(_ri.g, kind, params);
        ri=_ri;
    }

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
    public final void gl_beginShape(GL_PrimStyle primType) {
        switch (primType) {
            case GL_POINTS : {          super.beginShape(PConstants.POINTS);         return;}
            case GL_LINES : {           super.beginShape(PConstants.LINES);          return;}
            case GL_LINE_LOOP : {       super.beginShape(PConstants.LINE_LOOP);      return;}           
            case GL_LINE_STRIP : {      super.beginShape(PConstants.LINE_STRIP);     return;}           
            case GL_TRIANGLES : {       super.beginShape(PConstants.TRIANGLES);      return;}
            case GL_TRIANGLE_STRIP : {  super.beginShape(PConstants.TRIANGLE_STRIP); return;}
            case GL_TRIANGLE_FAN : {    super.beginShape(PConstants.TRIANGLE_FAN);   return;}
            case GL_QUADS : {           super.beginShape(PConstants.QUADS);          return;}
            case GL_QUAD_STRIP : {      super.beginShape(PConstants.QUAD_STRIP);     return;}
            case POINT : {              super.beginShape(PConstants.POINT);          return;}
            case LINE : {               super.beginShape(PConstants.LINE);           return;}
            case TRIANGLE : {           super.beginShape(PConstants.TRIANGLE);       return;}
            case QUAD : {               super.beginShape(PConstants.QUAD);           return;}
            case RECT : {               super.beginShape(PConstants.RECT);           return;}
            case ELLIPSE : {            super.beginShape(PConstants.ELLIPSE);        return;}
            case ARC : {                super.beginShape(PConstants.ARC);            return;}
            case SPHERE : {             super.beginShape(PConstants.SPHERE);         return;}
            case BOX : {                super.beginShape(PConstants.BOX);            return;}
            default : {                 super.beginShape(PConstants.POLYGON);        return;}        
        }
    }//gl_beginShape

    @Override
    public void gl_endShape(boolean isClosed) {
        super.endShape(isClosed ? PConstants.CLOSE : PConstants.OPEN);
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
     * set fill color by value during shape building
     * @param clr 1st 3 values denot integer color vals
     * @param alpha 
     */
    @Override
    public final void gl_setFill(int r, int g, int b, int alpha) {super.fill(r,g,b,alpha);}
    /**
     * Set to have no fill color for a specified object (within gl_beginShape/gl_endShape)
     */
    @Override
    public final void gl_setNoFill() {super.noFill();}
    /**
     * set stroke color by value during shape building
     * @param clr rgba
     * @param alpha 
     */
    @Override
    public final void gl_setStroke(int r, int g, int b, int alpha) {super.stroke(r,g,b,alpha);}
    /**
     * Set to have no stroke color for a specified object (within gl_beginShape/gl_endShape)
     */
    @Override
    public final void gl_setNoStroke() {super.noStroke();}
    /**
     * Set stroke weight for a specified object (within gl_beginShape/gl_endShape)
     * @param wt
     */
    @Override
    public final void gl_setStrokeWt(float wt) {super.strokeWeight(wt);}

    @Override
    public void setFill(int r, int g, int b, int alpha) {super.fill(r,g,b,alpha);}

    @Override
    public void setNoFill() {super.noFill();}

    @Override
    public void setStroke(int r, int g, int b, int alpha) {super.stroke(r,g,b,alpha);}

    @Override
    public void setNoStroke() {super.noStroke();}

    @Override
    public void setStrokeWt(float stW) {super.strokeWeight(stW);}

    @Override
    public void draw() {  ri.shape(this);   }

    @Override
    public void setIsVisible(boolean isVis) {     super.setVisible(isVis);  }
    @Override
    public boolean isVisible() {return super.isVisible();}

    @Override
    public int getNumChildren() { return super.getChildCount();}

    @Override
    public void addChildMesh(IMeshInterface child) {              super.addChild(((ProcessingShape)child));}

    @Override
    public void addChildMesh(IMeshInterface child, int idx) {     super.addChild(((ProcessingShape)child), idx);  }
 
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
    
    /**
     * Get the child of this IMeshInterface object specified by the given layer
     * @param layer
     * @return
     */
    @Override
    public IMeshInterface getChildMesh(int layer) {return ((ProcessingShape)super.getChild(layer));}
    /**
     * Get the child of this IMeshInterface object with the given name
     * @param name
     * @return
     */
    @Override
    public IMeshInterface getChildMesh(String name) {return ((ProcessingShape)super.getChild(name));}

}// class ProcessingShape
