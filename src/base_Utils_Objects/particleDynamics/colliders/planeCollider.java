package base_Utils_Objects.particleDynamics.colliders;

import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.particleDynamics.colliders.base.CollisionType;
import base_Utils_Objects.particleDynamics.colliders.base.baseCollider;
import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.particleDynamics.solvers.SolverType;
import base_Utils_Objects.vectorObjs.myVectorf;

public class planeCollider extends baseCollider{
	//plane
	public myVectorf planeNormal;		//normal of this collider, if flat plane
	public myVectorf[] verts;	//vertices of this object, if flat plane
	public double[] peq;		//plane equation values
	
	public planeCollider(String _n, myVectorf _drawLoc, myVectorf[] _verts) {
		super(_n, _drawLoc, CollisionType.FLAT);
		verts = _verts;
		buildPlaneNorm();
		findPlaneEQ();
	}
	
	//determines the equation coefficients for a planar collider
	public void findPlaneEQ() {
		//Ax + By + Cz + D = 0
		peq = new double[4];
		peq[0] = planeNormal.x;		//A == norm.X
		peq[1] = planeNormal.y;		//B == norm.y
		peq[2] = planeNormal.z;		//C == norm.z
		peq[3] = -planeNormal._dot(verts[0]);//- ((peq[0] * verts[0].x) + (peq[1] * verts[0].y) + (peq[2] * verts[0].z));		//D
	}

	//build normal of planar object
	public void buildPlaneNorm() {
		myVectorf P0P1 = myVectorf._sub(verts[1], verts[0]);
		myVectorf P1P2 = myVectorf._sub(verts[1], verts[2]);
		//planeNormal = P0P1._cross(P1P2);
		planeNormal = P1P2._cross(P0P1);
		planeNormal._normalized();
	}//buildNorm

	@Override  
	//0 if no collision, 
	//1 if collision via breach - push back to legal position, address vel and frc concerns
	//2 if collision next timestep  
	//3 if need to counter force due to contact - within some epsilon of part radius distance from collider surface
	public int checkCollision(double deltaT, myParticle part) {
		myVectorf partLocVec = new myVectorf(verts[0],part.aPosition[part.curIDX]);			//vector from point on plane to particle position to get dist from plane
		//far from plane - no need to check further in collision detection
		double distFromPlane = partLocVec._dot(planeNormal) - myParticle.partRad; 				//distance edge of particle is from plane
		if (distFromPlane  > myParticle.tenPartRads){ return NoCol;}									//if further away from plane than 10 * particle radius then no collision this or next cycle
		if (distFromPlane  < -MyMathUtils.eps) { return BrchCol; }						      	//immediate collision - breached plane by more than eps*rad
		//dist between epsVal and 10 snoflake rads - possible collision next cycle
		double spdInPlaneNormDir = part.aVelocity[part.curIDX]._dot(planeNormal),					//velocity in direction of plane normal - speed toward plane is negative of this
				accInPlaneNormDir = part.aForceAcc[part.curIDX]._dot(planeNormal)/part.mass;		//acc in dir of plane normal - acc toward plane is negative of this
		if((spdInPlaneNormDir > 0) && (accInPlaneNormDir > 0)){ return NoCol;}						//not touching plane, moving toward plane or accelerating toward plane, so no collision possible this time step
		if (distFromPlane  < MyMathUtils.eps) { return CntctCol; }								//contact - address forces
		//by here, within col dist of plane, and moving toward, or tangent to, plane - predict motion
		double hfDelT2 = .5 * deltaT * deltaT,
				velPartInPlaneDir = spdInPlaneNormDir * deltaT,
				accPartInPlaneDir = hfDelT2 * accInPlaneNormDir,
				distWillMove = velPartInPlaneDir + accPartInPlaneDir;		
		if(distFromPlane < distWillMove){//particle is closer to plane than how far it is going to move next cycle - will breach after integration
			return NextCol;
		}
		return NoCol;

	}//checkCollision	
	
	//if particle has breached planar collider somehow, move particle along appropriate normal direction until not breached anymore, and check particle's velocity(and reverse if necessary)
	//1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact
	@Override
	public void handleCollision(myParticle part, int res) {
		myVectorf partPos = part.aPosition[part.curIDX];
		double distFromBreach = myVectorf._sub(partPos, verts[0])._dot(planeNormal);
		myVectorf partVel = part.aVelocity[part.curIDX], partFrc = part.aForceAcc[part.curIDX];		
		
		//1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact
		if ((res == 2) || (partVel._dot(planeNormal) < 0)) {//going to breach next time step, or have breached and velocity is still going down - swap velocity direction
			myVectorf[] partVelComp = MyMathUtils.getVecFrameNonNorm(partVel, planeNormal);
			//partVelComp[0] *= (-1 * Krest);//reverse direction of normal velocity
			partVel.set(  myVectorf._add(myVectorf._mult(partVelComp[0],(-1 * Krest)), partVelComp[1]));//should change dir of velocity
			// part.color = myVector(1,0,0);
			if (part.solveType == SolverType.VERLET) { handleVerletCol(part); }//handle reflection/velocity change by swapping old and new positions - need to scale by krest
		}//if breached and velocity going away from wall

		else if (res == 1) {           //immediate breach, swap position
			//p.color = myVector(0, 0, 0);
			if (distFromBreach > 0) {}//cout<<"breach error, not on wrong side of plane"<<endl;}
			else if (part.solveType == SolverType.VERLET) { handleVerletCol(part); }	//handle reflection/velocity change by swapping old and new positions - need to scale by krest			
			else {//forcibly move particle to just a bit on the right side of the collider
				distFromBreach *= -(2.001);
				myVectorf newPos = myVectorf._add(partPos, myVectorf._mult(planeNormal,(distFromBreach + MyMathUtils.eps)));   //reflect position up from plane by slightly more than breach amount
				//if(p.getSolveType() == GROUND){cout<<"dist from breach "<<distFromBreach<<" old position: "<<partPos<<" new position : "<<newPos<<endl;}
				//part.position.peekFirst().set( newPos);
				partPos.set( newPos);
				myVectorf[] partAccComp = MyMathUtils.getVecFrameNonNorm(partFrc, planeNormal);
				myVectorf frcTanDir = myVectorf._normalize(partAccComp[1]),
						//TODO fix this stuff - friction is not working correctly
						tanForce = myVectorf._mult(frcTanDir,-muFrict * (partAccComp[0]._dot(planeNormal)))
				;
				partFrc.set(partAccComp[0]) ;
				partVel.set(0,0,0);// = myVector(0,0,0);//partVelComp[0];//+partVelComp[1];//should change dir of velocity
			}
		}//if 1

		else if (res == 3) {          //contact
			if (part.solveType == SolverType.VERLET) { handleVerletCol(part); }//handle reflection/velocity change by swapping old and new positions - need to scale by krest
		}

		if ((res == 3) || (res == 2) || (res == 1)) {                             //any contact criteria - swap normal force direction
			myVectorf[] partAccComp = MyMathUtils.getVecFrameNonNorm(partFrc, planeNormal);
			partAccComp[0]._mult(-1);//reverse direction of normal acc
			//part.forceAcc.peekFirst().set(myVector._add(partAccComp[0], partAccComp[1]));
			partFrc.set(myVectorf._add(partAccComp[0], partAccComp[1]));
		}//tangent
	}//handlePlanarBreach

	public void handleVerletCol(myParticle p) {
		myVectorf tmpOldPos = p.aPosition[p.curIDX], 
				tmpNewPos = p.aOldPos[p.curIDX];         //swapped already
		myVectorf colPt = myVectorf._mult(myVectorf._add(tmpOldPos, tmpNewPos), .5);
		double krTmp = ((1 - Krest) * .25) + Krest;
		myVectorf tmpOldSub = myVectorf._sub(tmpOldPos, colPt), colNDotVec = myVectorf._mult(planeNormal, colPt._dot(planeNormal)),
				tmpNewSub = myVectorf._sub(tmpNewPos, colPt);
		
		tmpOldPos = myVectorf._mult(myVectorf._add(myVectorf._sub(myVectorf._mult(myVectorf._sub(tmpOldPos,colNDotVec),2), tmpOldSub), colPt),krTmp);
		tmpNewPos = myVectorf._add(myVectorf._sub(myVectorf._mult(myVectorf._sub(tmpOldPos,colNDotVec),2), tmpNewSub),colPt);
		p.aPosition[p.curIDX].set(tmpNewPos);
		p.aOldPos[p.curIDX].set(tmpOldPos);
	}
}//planeCollider
