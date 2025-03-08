package base_UI_Objects.renderedObjs;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_UI_Objects.my_procApplet;
import base_UI_Objects.renderedObjs.base.Base_RenderObj;
import base_UI_Objects.renderedObjs.base.RenderObj_Clr;
import base_UI_Objects.renderedObjs.base.RenderObj_ClrPalette;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PImage;

/**
 * build a registered pre-rendered instantiatable object for each objRep - speeds up display by orders of magnitude
 * @author John Turner
 *
 */
public class Boat_RenderObj extends Base_RenderObj {
	/**
	 * if overall geometry has been made or not
	 */
	private static boolean[] madeForType = null;
	/**
	 * individual static object representation. Any animation will be owned by the instancing class
	 */
	private static PShape[] objReps = null;	
	/**
	 * precalc consts
	 */
	private static final int numOars = 5;
	//objRep geometry/construction variables
	private static final myPointf[][] boatVerts = new myPointf[5][12];						//seed points to build object 	
	private static myPointf[][] boatHullVerts;													//points of hull
	private static myPointf[] pts3, pts5, pts7;	
		
	//extra pshapes for this object
	//1 array for each type of objRep, 1 element for each animation frame of oar motion
	private static PShape[] oars;										
	//UV ara shaped like sail
	private static final myPointf[] uvAra = new myPointf[]{
			new myPointf(0,0,0),new myPointf(0,1,0),
			new myPointf(.375f,.9f,0),new myPointf(.75f,.9f,0),
			new myPointf(1,1,0),new myPointf(1,0,0),
			new myPointf(.75f,.1f,0),new myPointf(.375f,.1f,0)};
	//common initial transformation vector used in boat construction
	private static final myPointf transYup1 = new myPointf(0,1,0);
	
	private static double maxAnimCntr = 1000.0;
	/**
	 * colors for boat reps
	 */	
	private static RenderObj_ClrPalette clrPalette;
	
	public Boat_RenderObj(IRenderInterface _p, int _type, int _numTypes, int _numAnimFrames, RenderObj_ClrPalette _clrPalette, PImage[] _textures) {	
		super(_p, _type, _numTypes, _numAnimFrames, _clrPalette, _textures);
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
			objReps = new PShape[_numTypes];
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
	@Override
	public final double getMaxAnimCounter() {return maxAnimCntr;}
	@Override
	public final void setMaxAnimCounter(double _maxAnimCntr) {maxAnimCntr = _maxAnimCntr;}
	
	/**
	 * Instantiate objRep object
	 * @return
	 */
	@Override
	protected final PShape createObjRepForType() {
		return createBaseShape(PConstants.GROUP); 		
	}

	//inherited from myRenderObj
	//colors shared by all instances/flocks of this type of render obj
	@Override
	protected void setMainColorPalette(RenderObj_ClrPalette _clrPalette){
		clrPalette = new RenderObj_ClrPalette(_clrPalette);
	}			 

	@Override
	protected RenderObj_Clr getObjTypeColor() {
		return clrPalette.getInstanceColor(type);
	}
	
	/**
	 * Builds geometry for object class to be instanced - only perform once per object class (not per type/instance)
	 */
	@Override
	protected void initObjGeometry() {		
		float xVert, yVert, zVert;
		int numSlices = boatVerts[0].length, numSlicesSq = numSlices * numSlices;
		for(int j = 0; j < numSlices; ++j){
			zVert = j - 4;		
			float sf = (1 - ((zVert+3)*(zVert+3)*(zVert+3))/(numSlicesSq * numSlices * 1.0f));
			for(int i = 0; i < boatVerts.length; ++i){
				float ires1 = (1.5f*i - 3);
				xVert = ires1 * sf;
				yVert = (float) (((-1 * Math.sqrt(9 - (ires1*ires1)) ) * sf) + (3*(zVert-2)*(zVert-2))/(numSlicesSq * 1.0f));
				boatVerts[i][j] = new myVectorf(xVert, yVert, zVert);
			}//for i	
		}//for j	
		pts3 = buildSailPtAra(3);
		pts5 = buildSailPtAra(5);
		pts7 = buildSailPtAra(7);
		//build boat body arrays
		initBoatBody();	
		//create pshape groups of oars, for each frame of animation, shared across all instances
		oars = new PShape[numAnimFrames];
		double animRatio = maxAnimCntr/(1.0f*numAnimFrames);
		for(int a=0; a<numAnimFrames; ++a){
			oars[a] = createBaseShape(PConstants.GROUP);
			double animCntr = (a * animRatio);
			buildOars(a, clrPalette.getMainColor(), animCntr, 1, new myVectorf(0, 0.3f, 3));
			buildOars(a, clrPalette.getMainColor(), animCntr, -1, new myVectorf(0, 0.3f, 3)); 		
		}		
		
	}//initObjGeometry()	
	
	//set values for flock-type specific instance of boid render rep, (rep is built in base class call)
	@Override
	protected void initInstObjGeometryIndiv(){ 
		//sailTexture = ((myBoids3DWin)win).flkSails[type];
	}//initInstObjGeometry
	
	//fix rotation to match desired x-forward y-up orientation
	private void finalRotate(PShape _objRep) {
		_objRep.rotate(MyMathUtils.HALF_PI_F,0,0,1);
		_objRep.rotate(MyMathUtils.HALF_PI_F,0,1,0);	
	}
	
	@Override
	protected void buildObj(){
		PShape _objRep = objReps[type];
		//send color to use for masts and oars
		initBoatMasts(_objRep, clrPalette.getMainColor());
		int numZ = boatVerts[0].length-1, numX = boatVerts.length;
		int btPt = 0;		
		for(int j = 0; j < numZ; ++j){
			btPt = buildQuadShape(_objRep,transYup1, numX, btPt, boatHullVerts);
		}//for j
		for(int i = 0; i < numX; ++i){	
			buildBodyBottom(_objRep, boatVerts,i, numZ, numX);	
		}//for i	
		for(int j = 0; j < numZ; ++j){
			btPt = buildQuadShape(_objRep, transYup1, 1, btPt, boatHullVerts);
			btPt = buildQuadShape(_objRep, transYup1, 1, btPt, boatHullVerts);		
		}//for j		
		myVectorf transVec2 = new myVectorf(0,1.5f,0);
		//draw rear and front castle
		for(int j = 0; j < 27; ++j){
			btPt = buildQuadShape(_objRep, transVec2, 1, btPt, boatHullVerts);
		}
		finalRotate(_objRep);
	}//buildShape
	//end inherited from myRenderObj

	@Override //representation-specific drawing code (i.e. oars settings for boats)
	protected void drawMeIndiv(int animIDX){//which oars array instance of oars to show - oars move relative to speed of boid
		((my_procApplet) p).shape(objReps[type]);		
		((my_procApplet) p).shape(oars[animIDX]);
	}//drawMe
	
	private myPointf[] buildSailPtAra(float len){
		myPointf[] res = new myPointf[]{new myPointf(0,0,0),new myPointf(0,len,0),
				new myPointf(-1.5f,len*.9f,1.5f),new myPointf(-3f,len*.9f,1.5f),
				new myPointf(-4f,len,0), new myPointf(-4f,0,0),
				new myPointf(-3f,len*.1f,1.5f),new myPointf(-1.5f,len*.1f,1.5f)};
		return res;
	}

	/**
	 * build masts and oars(multiple orientations in a list to just show specific frames)
	 * @param clr
	 */
	private void initBoatMasts(PShape _objRep, RenderObj_Clr clr){	
		myPointf[] trans1Ara = new myPointf[]{new myPointf(0, 3.5f, -3),new myPointf(0, 1.25f, 1),new myPointf(0, 2.2f, 5),new myPointf(0, 1.8f, 7)},
				scale1Ara = new myPointf[]{new myPointf(.95f,.85f,1),new myPointf(1.3f,1.2f,1),new myPointf(1f,.9f,1),new myVectorf(1,1,1)};
		
		float[][] rot1Ara = new float[][]{new float[]{0,0,0,0},new float[]{0,0,0,0},new float[]{0,0,0,0},new float[]{MyMathUtils.THIRD_PI_F, 1, 0,0}};
		int idx = 0;
		for(int rep = 0; rep < 3; ++rep){buildSail(_objRep, false, pts7, pts5, (type%2==1), trans1Ara[idx],  scale1Ara[idx]);idx++; }
		buildSail(_objRep, true, pts3, pts3, true, trans1Ara[idx],  scale1Ara[idx]);   //
		
		float[] sailRotAra = new float[]{MyMathUtils.HALF_PI_F, 0,0,1};
		for(int j = 0; j<trans1Ara.length; ++j){//mainColor,
			if(j==3){//front sail
				_objRep.addChild(buildPole(0, clr, .1f, 7, false, trans1Ara[j],  scale1Ara[j], rot1Ara[j], new myVectorf(0,0,0), new float[]{0,0,0,0},new myVectorf(0,0,0), new float[]{0,0,0,0}));
				_objRep.addChild(buildPole(4, clr, .05f, 3,  true, trans1Ara[j],  scale1Ara[j], rot1Ara[j], new myVectorf(0, 5f, 0), sailRotAra,new myVectorf(1,-1.5f,0), new float[]{0,0,0,0}));
			}
			else{
				_objRep.addChild(buildPole(1,clr, .1f, 10, false,trans1Ara[j],  scale1Ara[j], rot1Ara[j], new myVectorf(0,0,0), new float[]{0,0,0,0}, new myVectorf(0,0,0), new float[]{0,0,0,0}));
				_objRep.addChild(buildPole(2,clr, .05f, 7, true, trans1Ara[j],  scale1Ara[j], rot1Ara[j], new myVectorf(0, 4.5f, 0), sailRotAra,new myVectorf(0,-3.5f,0), new float[]{0,0,0,0}));
				_objRep.addChild(buildPole(3,clr, .05f, 5, true, trans1Ara[j],  scale1Ara[j], rot1Ara[j], new myVectorf(0, 4.5f, 0), sailRotAra,new myVectorf(4.5f,-2.5f,0), new float[]{0,0,0,0}));
			}					
		}
	}//initBoatMasts	

	/**
	 * build oars to orient in appropriate position for animIdx frame of animation - want all numAnimFrm of animation to cycle
	 * @param animIdx
	 * @param clr
	 * @param animCntr
	 * @param dirMult
	 * @param transVec
	 */
	private void buildOars(int animIdx, RenderObj_Clr clr, double animCntr, float dirMult, myPointf transVec){ 
		float[] rotAra1 = new float[]{MyMathUtils.HALF_PI_F, 1, 0, 0},
				rotAra2, rotAra3;
		myPointf transVec1 = new myPointf(0,0,0);
		float disp = 0, d=-6, distMod = 10.0f/numOars;
		double ca = pi4thrds + .65f*Math.cos(animCntr*pi100th);
		for(int i =0; i<numOars;++i){
			double	sa = pi6ths + .65f*Math.sin(((animCntr + i/(1.0f*numOars)))*pi100th);
			transVec1.set(transVec.x+dirMult*1.5f, transVec.y, transVec.z+d+disp);
			rotAra2 = new float[]{(float) ca, 0,0,dirMult};
			rotAra3 = new float[]{(float) (sa*.5f), 1,0, 0};			
			oars[animIdx].addChild(buildPole(1,clr,.1f, 6, false, transVec1, new myPointf(1,1,1), rotAra1, myPointf.ZEROPT, rotAra2, myPointf.ZEROPT, rotAra3));	
			//fix orientation of oars
			finalRotate(oars[animIdx]);
			disp+=distMod;
		}			
	}//buildOars

	private void build1Sail(PShape _objRep, boolean renderSigil, myPointf[] pts, myPointf transVec, myPointf trans2Vec, myPointf scaleVec){
		myPointf trans2VecDisp = new myPointf(trans2Vec);
		trans2VecDisp._add(0, -3.5f, 0);
		PShape sh = createAndSetInitialTransform(
				transVec, scaleVec, new float[4], 
				new myPointf(0,4.5f,0), new float[] {MyMathUtils.HALF_PI_F, 0,0,1},
				trans2VecDisp, new float[4]);
		sh.beginShape(); 
		sh.fill(0xFFFFFFFF);	
		sh.noStroke();
		if(renderSigil) {
			setObjTexture(sh, 0);
		}
		for(int i=0;i<pts.length;++i){	sh.vertex(pts[i].x,pts[i].y,pts[i].z,uvAra[i].y,uvAra[i].x);}		
		sh.endShape();
		_objRep.addChild(sh);			
	}
	
	private void buildSail(PShape _objRep, boolean frontMast, myPointf[] pts1, myPointf[] pts2, boolean renderSigil, myPointf transVec, myPointf scaleVec){
		if(frontMast){
			PShape sh = createAndSetInitialTransform(
					transVec, scaleVec, new float[] {MyMathUtils.THIRD_PI_F, 1,0,0}, 
					new myPointf(0,5.0f,0), new float[] {MyMathUtils.HALF_PI_F, 0,0,1},
					new myPointf(1,-1.5f,0), new float[4]);	
			sh.beginShape(); 
			sh.fill(0xFFFFFFFF);	
			sh.noStroke();
			setObjTexture(sh, 0);
			for(int i=0;i<pts1.length;++i){	sh.vertex(pts1[i].x,pts1[i].y,pts1[i].z,uvAra[i].y,uvAra[i].x);}			
			sh.endShape();
			_objRep.addChild(sh);			
		}
		else {			
			build1Sail(_objRep, renderSigil, pts1, transVec, myVectorf.ZEROVEC, scaleVec);
			build1Sail(_objRep, !renderSigil, pts2, transVec,new myVectorf(4.5f,1,0), scaleVec);
		}
	}//drawSail
	
	
	private void buildBodyBottom(PShape _objRep, myPointf[][] boatVerts, int i, int lastIDX, int numX){
		PShape sh = createBaseShape();
		sh.translate(transYup1.x, transYup1.y, transYup1.z);
		sh.beginShape(PConstants.TRIANGLE);			
			getObjTypeColor().shPaintColors(sh);
			sh.vertex(boatVerts[i][lastIDX].x, boatVerts[i][lastIDX].y, 	boatVerts[i][lastIDX].z);	sh.vertex(0, 1, lastIDX-1);	sh.vertex(boatVerts[(i+1)%numX][lastIDX].x, boatVerts[(i+1)%numX][lastIDX].y, 	boatVerts[(i+1)%numX][lastIDX].z);	
		sh.endShape(PConstants.CLOSE);
		_objRep.addChild(sh);			

		sh = createBaseShape();
		sh.translate(transYup1.x, transYup1.y, transYup1.z);
		sh.beginShape(PConstants.QUAD);		
			getObjTypeColor().shPaintColors(sh);
			sh.vertex(boatVerts[i][0].x, boatVerts[i][0].y, boatVerts[i][0].z);sh.vertex(boatVerts[i][0].x * .75f, boatVerts[i][0].y * .75f, boatVerts[i][0].z -.5f);	sh.vertex(boatVerts[(i+1)%numX][0].x * .75f, boatVerts[(i+1)%numX][0].y * .75f, 	boatVerts[(i+1)%numX][0].z -.5f);sh.vertex(boatVerts[(i+1)%numX][0].x, boatVerts[(i+1)%numX][0].y, 	boatVerts[(i+1)%numX][0].z );
		sh.endShape(PConstants.CLOSE);
		_objRep.addChild(sh);			
		
		sh = createBaseShape();
		sh.translate(transYup1.x, transYup1.y, transYup1.z);
		sh.beginShape(PConstants.TRIANGLE);		
			getObjTypeColor().shPaintColors(sh);
			sh.vertex(boatVerts[i][0].x * .75f, boatVerts[i][0].y * .75f, boatVerts[i][0].z  -.5f);	sh.vertex(0, 0, boatVerts[i][0].z - 1);	sh.vertex(boatVerts[(i+1)%numX][0].x * .75f, boatVerts[(i+1)%numX][0].y * .75f, 	boatVerts[(i+1)%numX][0].z  -.5f);	
		sh.endShape(PConstants.CLOSE);		
		_objRep.addChild(sh);
	}
	
	//build objRep's body points
	private void initBoatBody(){
		int numZ = boatVerts[0].length, numX = boatVerts.length, idx, pIdx = 0, araIdx = 0;
		myPointf[] tmpPtAra;
		myPointf[][] resPtAra = new myPointf[104][];
		
		for(int j = 0; j < numZ-1; ++j){
			for(int i = 0; i < numX; ++i){
				tmpPtAra = new myPointf[4];pIdx = 0;	tmpPtAra[pIdx++] = new myPointf(boatVerts[i][j].x, 	boatVerts[i][j].y, 	boatVerts[i][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[(i+1)%numX][j].x, 		boatVerts[(i+1)%numX][j].y,			boatVerts[(i+1)%numX][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[(i+1)%numX][(j+1)%numZ].x,boatVerts[(i+1)%numX][(j+1)%numZ].y, boatVerts[(i+1)%numX][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[i][(j+1)%numZ].x,			boatVerts[i][(j+1)%numZ].y, 			boatVerts[i][(j+1)%numZ].z);
				resPtAra[araIdx++] = tmpPtAra;
			}//for i	
		}//for j		
		for(int j = 0; j < numZ-1; ++j){
			tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x, boatVerts[0][j].y, 	 boatVerts[0][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x, 					boatVerts[0][j].y +.5f,			 boatVerts[0][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x,			boatVerts[0][(j+1)%numZ].y + .5f, boatVerts[0][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x,			boatVerts[0][(j+1)%numZ].y, 	 boatVerts[0][(j+1)%numZ].z);				
			resPtAra[araIdx++] = tmpPtAra;
			tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x, boatVerts[numX-1][j].y, 	 boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x, 		 boatVerts[numX-1][j].y + .5f,			 boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x, boatVerts[numX-1][(j+1)%numZ].y +.5f, boatVerts[numX-1][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x, boatVerts[numX-1][(j+1)%numZ].y, 	 boatVerts[numX-1][(j+1)%numZ].z);				
			resPtAra[araIdx++] = tmpPtAra;
		}//for j
		//draw rear castle
		for(int j = 0; j < 3; ++j){
			tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x*.9f, 			boatVerts[0][j].y-.5f, 			 boatVerts[0][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x*.9f, 					boatVerts[0][j].y+2,			 boatVerts[0][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x*.9f,			boatVerts[0][(j+1)%numZ].y+2, boatVerts[0][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x*.9f,			boatVerts[0][(j+1)%numZ].y-.5f, 	 boatVerts[0][(j+1)%numZ].z);				
			resPtAra[araIdx++] = tmpPtAra;
			tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x*.9f, boatVerts[numX-1][j].y-.5f, boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x*.9f, 		 boatVerts[numX-1][j].y+2,			 boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x*.9f, boatVerts[numX-1][(j+1)%numZ].y+2, boatVerts[numX-1][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x*.9f, boatVerts[numX-1][(j+1)%numZ].y-.5f, 	 boatVerts[numX-1][(j+1)%numZ].z);				
			resPtAra[araIdx++] = tmpPtAra;
			tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x*.9f, 		boatVerts[0][j].y+1.5f,		boatVerts[0][j].z);	tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x*.9f, 		boatVerts[numX-1][j].y+1.5f,			boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x*.9f,boatVerts[numX-1][(j+1)%numZ].y+1.5f, 	boatVerts[numX-1][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x*.9f,		boatVerts[0][(j+1)%numZ].y+1.5f, 		boatVerts[0][(j+1)%numZ].z);					
			resPtAra[araIdx++] = tmpPtAra;
		}//for j
		tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][3].x*.9f, 		boatVerts[0][3].y+2,		boatVerts[0][3].z);	tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][3].x*.9f, boatVerts[0][3].y+2,boatVerts[numX-1][3].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][3].x*.9f, boatVerts[0][3].y-.5f,boatVerts[numX-1][3].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][3].x*.9f,		boatVerts[0][3].y-.5f, 	boatVerts[0][3].z);			
		resPtAra[araIdx++] = tmpPtAra;

		tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f, 	boatVerts[0][0].y-.5f, 	boatVerts[0][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f, 	boatVerts[0][0].y+2.5f,	boatVerts[0][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f,	boatVerts[0][0].y+2, 	boatVerts[0][1].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f,	boatVerts[0][0].y-1, 	boatVerts[0][1].z-1);			
		resPtAra[araIdx++] = tmpPtAra;

		tmpPtAra = new myPointf[4];pIdx = 0;
		tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y-.5f, 	boatVerts[numX-1][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y+2.5f, boatVerts[numX-1][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y+2, 	boatVerts[numX-1][1].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y-1, 	boatVerts[numX-1][1].z-1);			
		resPtAra[araIdx++] = tmpPtAra;
		tmpPtAra = new myPointf[4];	pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f, 		boatVerts[0][0].y+2.5f,		boatVerts[0][0].z - 1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y+2.5f,	boatVerts[numX-1][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y+2f,	boatVerts[numX-1][1].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f,		boatVerts[0][0].y+2f, 		boatVerts[0][1].z-1);			
		resPtAra[araIdx++] = tmpPtAra;
		tmpPtAra = new myPointf[4];	pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f, 		boatVerts[0][0].y-.5f,		boatVerts[0][0].z - 1);	tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y-.5f,boatVerts[numX-1][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y-1,boatVerts[numX-1][1].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f,		boatVerts[0][0].y-1, 	boatVerts[0][1].z-1);			
		resPtAra[araIdx++] = tmpPtAra;
		tmpPtAra = new myPointf[4];	pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f, 		boatVerts[0][0].y+2.5f,		boatVerts[0][0].z - 1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y+2.5f,boatVerts[numX-1][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[numX-1][0].y-.5f,boatVerts[numX-1][0].z-1);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f,		boatVerts[0][0].y-.5f, 	boatVerts[0][0].z-1);				
		resPtAra[araIdx++] = tmpPtAra;
		tmpPtAra = new myPointf[4];	pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f, boatVerts[0][0].y+2,		boatVerts[0][0].z);	tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[0][0].y+2,boatVerts[numX-1][0].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][0].x*.9f, boatVerts[0][0].y-.5f,boatVerts[numX-1][0].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][0].x*.9f,		boatVerts[0][0].y-.5f, 	boatVerts[0][0].z);	
		resPtAra[araIdx++] = tmpPtAra;
		//draw front castle
		for(int j = numZ-4; j < numZ-1; ++j){
			tmpPtAra = new myPointf[4];	pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x*.9f, 		boatVerts[0][j].y-.5f, 		boatVerts[0][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x*.9f, 		boatVerts[0][j].y+.5f,		 boatVerts[0][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x*.9f,			boatVerts[0][(j+1)%numZ].y+.5f, boatVerts[0][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x*.9f,			boatVerts[0][(j+1)%numZ].y-.5f, 	 boatVerts[0][(j+1)%numZ].z);				
			resPtAra[araIdx++] = tmpPtAra;
			tmpPtAra = new myPointf[4];pIdx = 0;	tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x*.9f, boatVerts[numX-1][j].y-.5f, 	boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x*.9f, boatVerts[numX-1][j].y+.5f, boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x*.9f, boatVerts[numX-1][(j+1)%numZ].y+.5f, boatVerts[numX-1][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x*.9f, boatVerts[numX-1][(j+1)%numZ].y-.5f, 	 boatVerts[numX-1][(j+1)%numZ].z);					
			resPtAra[araIdx++] = tmpPtAra;
			tmpPtAra = new myPointf[4];pIdx = 0;	tmpPtAra[pIdx++] = new myPointf(boatVerts[0][j].x*.9f, 		boatVerts[0][j].y+.5f,			boatVerts[0][j].z);	tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][j].x*.9f, boatVerts[numX-1][j].y+.5f, boatVerts[numX-1][j].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][(j+1)%numZ].x*.9f,boatVerts[numX-1][(j+1)%numZ].y+.5f, 	boatVerts[numX-1][(j+1)%numZ].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][(j+1)%numZ].x*.9f,		boatVerts[0][(j+1)%numZ].y+.5f, 		boatVerts[0][(j+1)%numZ].z);
			resPtAra[araIdx++] = tmpPtAra;
		}//for j
		idx = numZ-1;
		tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][ idx].x*.9f, 		boatVerts[0][ idx].y-.5f,	boatVerts[0][ idx].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][ idx].x*.9f, boatVerts[0][ idx].y-.5f,		boatVerts[0][ idx].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][ idx].x*.9f, boatVerts[0][ idx].y+.5f,		boatVerts[0][ idx].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][ idx].x*.9f,		boatVerts[0][ idx].y+.5f, 	boatVerts[0][ idx].z);			
		resPtAra[araIdx++] = tmpPtAra;
		idx = numZ-4;
		tmpPtAra = new myPointf[4];pIdx = 0;tmpPtAra[pIdx++] = new myPointf(boatVerts[0][ idx].x*.9f, 		boatVerts[0][idx].y-.5f,	boatVerts[0][ idx].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][idx].x*.9f, boatVerts[0][idx].y-.5f,		boatVerts[0][ idx].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[numX-1][idx].x*.9f, boatVerts[0][idx].y+.5f,		boatVerts[0][ idx].z);tmpPtAra[pIdx++] = new myPointf(boatVerts[0][idx].x*.9f,		boatVerts[0][idx].y+.5f, 	boatVerts[0][idx].z);			
		resPtAra[araIdx++] = tmpPtAra;
		boatHullVerts = resPtAra;
	}//initBoatBody	


}//class myBoatRndrObj
