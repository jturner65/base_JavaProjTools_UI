package base_UI_Objects.renderedObjs;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.renderedObjs.base.Base_RenderObj;
import base_UI_Objects.renderedObjs.base.RenderObj_Clr;
import base_UI_Objects.renderedObjs.base.RenderObj_ClrPalette;
import processing.core.PConstants;
import processing.core.PShape;

/**
 * jellyfish pshape, with multiple component shapes that are animated
 * @author John Turner
 *
 */
public class JFish_RenderObj extends Base_RenderObj {
	//static vals set in here because we want boid "species"-wide color settings
	//if overall geometry has been made or not
	private static boolean made;
	
	private static PShape[][] bodyAra = new PShape[5][];
	//private int numTentacles = 5;
	
	/**
	 * colors for jellyfish reps
	 */	
	private static RenderObj_ClrPalette clrPalette;
	
	private static double maxAnimCntr = 1000.0;

	
	public JFish_RenderObj(IRenderInterface _p, int _type, int _numAnimFrames, RenderObj_ClrPalette _clrPalette)  {	
		super(_p, _type, _numAnimFrames, _clrPalette);
	}//ctor
	
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
		return createBaseShape(PConstants.GROUP); 		
	}

	
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
			bodyAra[i] = new PShape[numAnimFrames];
			p.setSphereDetail(20);
			for(int a=0; a<numAnimFrames; ++a){//for each frame of animation			
				bodyAra[i][a] = createBaseShape(PConstants.GROUP);
				PShape indiv = createBaseShape(PConstants.SPHERE, 5.0f);				
				//sclMult = (float) ((Math.sin(a * radAmt) * .25f) +1.0f);
				//indiv.scale(sclMult, sclMult, 1.0f/(sclMult * sclMult));
				indiv.scale(scaleVec[a][0], scaleVec[a][1],scaleVec[a][2]);				
				//call shSetPaintColors since we need to use set<type> style functions of Pshape when outside beginShape-endShape
				clrPalette.getInstanceColor(i).shSetShapeColors(indiv);		
				bodyAra[i][a].addChild(indiv);
			}	
		}
		p.setSphereDetail(5);			
	}
	//any instance specific, jelly-fish specific geometry setup goes here (textures, sizes, shapes, etc)
	@Override
	protected void initInstObjGeometryIndiv() {
		//build instance	
	}//initInstObjGeometry

	@Override
	protected void buildObj(PShape _objRep) {
		//build the boid's body geometry here - called at end of initInstObjGeometry
	}

	@Override
	protected void drawMeIndiv(int animIDX) {
		//draw animation index-specified deformed "jellyfish"
		((my_procApplet) p).shape(bodyAra[type][animIDX]);
	}
	@Override
	public final double getMaxAnimCounter() {return maxAnimCntr;}
	@Override
	public final void setMaxAnimCounter(double _maxAnimCntr) {maxAnimCntr = _maxAnimCntr;}
}
