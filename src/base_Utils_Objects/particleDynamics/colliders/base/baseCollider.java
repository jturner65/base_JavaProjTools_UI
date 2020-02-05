package base_Utils_Objects.particleDynamics.colliders.base;

import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class baseCollider {
	public static int ID_gen = 0;
	public int ID;
	public String name;
	public CollisionType colType;
	
	public myVectorf drawLoc;			//drawn location of this collider - only different from center if needed for display purposes

	public double Krest,			//coefficent of restitution - how much bounce do we have :1 total bounce, 0 no bounce.  multiply this against part's Velperp
				muFrict;         //friction coefficient

	public static final int
		NoCol = 0,					//0 if no collision, 
		BrchCol = 1,				//1 if collision via breach - push back to legal position, address vel and frc concerns
		NextCol = 2,				//2 if collision next timestep  
		CntctCol = 3;				//3 if need to counter force due to contact - within some epsilon of part radius distance from collider surface		
	
	public static final double partRad = .04;
	
	public baseCollider(String _n, myVectorf _drawLoc, CollisionType _colType) {
   		ID = ID_gen++;
  		name = new String(_n);
  		drawLoc = _drawLoc;
  		colType = _colType;
	}
	
	//checks if particle location + deltaT partVel will cause a collision, or if particle is in contact without
	//a normal-dir component of velocity, in which case it will modify the force
	//0 vec if no collision, 1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact	
	public abstract int checkCollision(double deltaT, myParticle part);

	//if particle has breached collider somehow, move particle along appropriate normal direction until not breached anymore, and check particle's velocity(and reverse if necessary)
	//1 if collision via breach, 2 if collision next timestep, 3 if need to counter force due to contact
	public abstract void handleCollision(myParticle part, int res);
	
}//myCollider
