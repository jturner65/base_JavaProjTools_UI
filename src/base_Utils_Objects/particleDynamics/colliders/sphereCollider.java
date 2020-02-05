package base_Utils_Objects.particleDynamics.colliders;

import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.particleDynamics.colliders.base.CollisionType;
import base_Utils_Objects.particleDynamics.colliders.base.baseCollider;
import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;

public class sphereCollider extends baseCollider{
	public myVectorf center,			//actual location of this collider, w/respect to particles - used for sphere
	radius;						//radius around center for ellipsoid (in each direction for x,y,z)

	public double avgRadius;		//average radius, for rouch col calculations
	public double[] minMaxRadius;		//minimum dist from center to still be inside (in case of ellipse, to minimize calculations) min idx 0, max idx 1
	
	
	public boolean  intRefl;			//internal reflections? for sphere (collide on inside)
	//myCollider(string _n, const Eigen::Vector3d& _dr, const Eigen::Vector3d& _ctr, const Eigen::Vector3d& _rad, bool _inRefl) :							//sphere collider
	//	ID(++ID_gen), name(_n), colType(SPHERE), drawLoc(_dr), center(_ctr), radius(_rad), minMaxRadius(2), planeNormal(), verts(), peq(4), intRefl(_inRefl), Krest(1) {
	//	initCollider();
	//}
	
	public sphereCollider(String _n, myVectorf _drawLoc, myVectorf _ctr, myVectorf _rad, boolean _intRefl) {
		super( _n, _drawLoc, CollisionType.SPHERE);
		intRefl = _intRefl;
		center = _ctr; radius = _rad;
		avgRadius = (radius.x + radius.y + radius.z) * 1.0/3.0;
		findMinMaxRadius();
	}
	
	
	
	//finds minimum and maximum value of radius for ellipsoid sphere, to speed up calculations of collisions
	public void findMinMaxRadius() {
		minMaxRadius = new double[5];
		minMaxRadius[0] = MyMathUtils.min(radius.z, radius.x, radius.y);
		minMaxRadius[1] = MyMathUtils.max(radius.z, radius.x, radius.y);
		minMaxRadius[2] = minMaxRadius[0] * minMaxRadius[0];	//sq min rad
		minMaxRadius[3] = minMaxRadius[1] * minMaxRadius[1];	//sq max rad
		minMaxRadius[4] = minMaxRadius[0] - myParticle.tenPartRads;		//min dist to ignore particle collision - min radius - 10x particle radius
	}	
	
	@Override
	//0 vec if no collision, 1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact
	public int checkCollision(double deltaT, myParticle part) {
		myVectorf partLocVec = new myVectorf(center,part.aPosition[part.curIDX]), vecToCtr = myVectorf._normalize(partLocVec);			//vector from center to particle position to get dist sphere wall
		//far from plane - no need to check further in collision detection
		double distFromCtr = partLocVec.magn + myParticle.partRad;
		if (distFromCtr < minMaxRadius[4]){ return NoCol;}						// more than 10x part radii from sphere surface - no collision assumed possible
		double distFromCtrDiff  = distFromCtr - minMaxRadius[1];			//compare to largest dimension of ellipsoid - if still positive, then definite breach
		if (distFromCtrDiff > MyMathUtils.eps_f) { return BrchCol; }						//immediate collision - breached plane by more than eps + snowflakerad
	
		double spdInRadNormDir = part.aVelocity[part.curIDX]._dot(vecToCtr),				//velocity in direction of particle-from-center vector
				accInRadNormDir = part.aForceAcc[part.curIDX]._dot(vecToCtr)/part.mass;		//acc in direction of particle-from-center vector
	
		if((spdInRadNormDir > 0) && (accInRadNormDir > 0)){ return NoCol;}						//not moving toward plane or accelerating toward plane, so no collision possible this time step
		//by here, within col dist of plane, and moving toward, or tangent to, plane - predict motion
		double velPartInRadNormDir = spdInRadNormDir * deltaT,
				accPartInRadNormDir = .5 * deltaT * deltaT * accInRadNormDir,
				distWillMove = velPartInRadNormDir + accPartInRadNormDir;
		
		myVectorf v2ctrORad = myVectorf._elemDiv(vecToCtr, radius);
		
		double a = v2ctrORad._dot(v2ctrORad), b = v2ctrORad._dot(center), c = center._dot(center) -1, ta = 2*a, discr1 = Math.pow(((b*b) - (2*ta*c)),.5), 
				t1 = (-1*b + discr1)/(ta), t2 = (-1*b - discr1)/(ta);
		//set the t value of the intersection to be the minimum of these two values (which would be the edge closest to the eye/origin of the ray)
		double t = Math.max(t1,t2);
		
		if(distFromCtr + distWillMove > t){//will move further from center than location of intersection of vector from center
			return NextCol;
		}
		return NoCol;
	
	//	//calc t so that normalized partLocVec collides with sphere/ellipsoid wall.  if t > len(partLocVec) then no collision
	//	
	//	
	//	
	//	
	//	
	//	myVector partPos = part.aPosition[part.curIDX],				
	//			partLocCtr = new myVector(center,partPos);
	//	//check if partloc is very far from wall
	//	if(((partLocCtr.magn < .95f * minMaxRadius[0]) && intRefl) //less than 90% of the sphere's minimum radius or 111% of max radius
	//		|| ((partLocCtr.magn > 1.056f * minMaxRadius[1]) && !intRefl)){return NoCol;}
	//	
	//	double hfDelT2 = .5 * deltaT * deltaT;
	//	myVector multPartFAcc = myVector._mult(part.aForceAcc[part.curIDX], hfDelT2),
	//			partVelPoint = myVector._add(myVector._mult(part.aVelocity[part.curIDX],deltaT), multPartFAcc),
	//			partMovePoint = myVector._add(partPos, partVelPoint);	//potential movement point for next turn of movement, to see if next turn of movement will hit wall
	//
	//	myVector partMvCtr = new myVector(center,partMovePoint);
	//		if (((partMvCtr.magn < (minMaxRadius[0] + partRad)) && (!intRefl)) ||					//current location is breach 
	//		((partMvCtr.magn > (minMaxRadius[1] - partRad)) && (intRefl))) {
	//			if (((partLocCtr.magn < (minMaxRadius[0] + partRad)) && (!intRefl)) ||					//current location is breach 
	//				((partLocCtr.magn > (minMaxRadius[1] - partRad)) && (intRefl))) {
	//				return BrchCol;
	//			}
	//			else {
	//				return NextCol;
	//			}
	//		}
	//	//find point on surface of sphere inline with center and partlocation
	//	myVector sNormPartP = getSphereNormal(partPos);				//normal through current point and center, in direction of collision surface
	//	myVector partSpherePnt =  myVector._add(center, myVector._mult(sNormPartP,-snoGlobe.snowGlobRad));			//point on ellipsoid surface colinear with center and particle move point
	//	double dist2wall = myVector._sub(partSpherePnt, partPos).magn, distFromWallChk = dist2wall - partVelPoint.magn;
	//	if (distFromWallChk > pa.epsValCalc) { return NoCol; }
	//	else if (distFromWallChk > -pa.epsValCalc) { return CntctCol; }
	//	else { return BrchCol; }
	}//checkCollision		
	
	public myVectorf getSphereNormal(myVectorf _loc) {//get normal at a particular location - no matter where inside or outside of sphere, normal built from this point and center will point in appropriate dir
		//if sphere, normal will be either pointing out or in, colinear with line from center to _loc
		myVectorf normDir = myVectorf._sub(center,_loc);//either point into center if internal reflections or point out of center if not
		double mult = ((intRefl) ? 1 : -1);
		normDir._mult(mult);
		normDir._normalize();
		return normDir;
	}//getNormal
	
	@Override
	//if particle has breached planar collider somehow, move particle along appropriate normal direction until not breached anymore, and check particle's velocity(and reverse if necessary)
	//1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact
	public void handleCollision(myParticle part, int res) {
		myVectorf partPos = part.aPosition[part.curIDX], partVel = part.aVelocity[part.curIDX], sphereNormal = getSphereNormal(partPos);
	
		if (res == 2) {//if close to intersection with sphere boundary
			myVectorf[] partVelComp = MyMathUtils.getVecFrameNonNorm(partVel, sphereNormal);
			//partVelComp[0] *= (-1 * Krest);//reverse direction of normal velocity
			partVel.set(myVectorf._add(myVectorf._mult(partVelComp[0],(-1 * Krest)), partVelComp[1]));//should change dir of velocity, decrease tangent velocity for friction
		}//if about to hit collider
	
		else if (res == 1) {//1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact
			double distFromBreach = myVectorf._sub(partPos, center).magn - (avgRadius - partRad);
			//cout<<"dist from breach "<<distFromBreach<<endl;
			if (((intRefl) && ((distFromBreach) < 0)) || ((!intRefl) && ((distFromBreach) > 0))) {}//cout<<"breach error, not on wrong side of sphere"<<endl;}
			else {//forcibly move particle to just a bit on the right side of the collider, reverse velocity
				distFromBreach *= (1.1);//move slightly more than breach amount
				myVectorf newPos = myVectorf._add(partPos,myVectorf._mult(sphereNormal,distFromBreach));//move back into sphere
				partPos.set(newPos);
				myVectorf[] partVelComp = MyMathUtils.getVecFrameNonNorm(partVel, sphereNormal);
				//partVelComp[0] *= -1;//reverse direction of normal velocity
				partVel.set(myVectorf._mult(myVectorf._add(partVelComp[0],partVelComp[1]),-1));//should change dir of velocity, for sphere zeroing tangent velocity
			}
		}//if 1
		else if (res == 3) //diminish all force and velocity in normal dir 
		{//tangent, get forceAcc and add -(forcecomponent in normal dir)
			myVectorf[] partAccComp = MyMathUtils.getVecFrameNonNorm(part.aForceAcc[part.curIDX], sphereNormal);
			partAccComp[0]._mult( -1 * Krest);//reverse direction of normal accel
			part.applyForce(myVectorf._add(partAccComp[0], partAccComp[1]));
		}//tangent
	}//handlePlanarBreach	

}//sphereCollider