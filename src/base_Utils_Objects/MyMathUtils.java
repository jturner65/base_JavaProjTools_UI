package base_Utils_Objects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;

/**
 * mathematical functions and constants that might be of use in some applications
 * @author john
 *
 */
public class MyMathUtils {
	public static final double 
		Pi = Math.PI,
		halfPi = .5*Pi,
		twoPi = 2.0*Pi,
		threeQtrPI = .75 * Pi,
		fifthPI = .2 * Pi,
		sqrt2 = Math.sqrt(2.0),
		invSqrt2 = .5 * sqrt2,
		sqrt3 = Math.sqrt(3.0),
		invSqrt3 = 1.0/sqrt3,
		eps = 1e-8,
		log2 = Math.log(2.0),
		log10 = Math.log(10.0);

	public static final float 
		Pi_f = (float)Pi,
		halfPi_f = (float) halfPi,
		twoPi_f = (float) twoPi,
		threeQtrPI_f = (float) threeQtrPI,
		fifthPI_f = (float) fifthPI,
		sqrt2_f = (float) sqrt2,
		invSqrt2_f = (float) invSqrt2,
		sqrt3_f = (float) sqrt3,
		invSqrt3_f = (float) invSqrt3,
		eps_f = (float) eps,
		log2_f = (float)log2,
		log10_f = (float)log10;
	

    // numbers greater than 10^MAX_DIGITS_10 or e^MAX_DIGITS_EXP are considered unsafe ('too big') for floating point operations
    protected static final int MAX_DIGITS_EXP = 677;
    protected static final int MAX_DIGITS_10 = 294; // ~ MAX_DIGITS_EXP/LN(10)
    protected static final int MAX_DIGITS_2 = 977; // ~ MAX_DIGITS_EXP/LN(2)
	

	//shouldn't be instanced
	private MyMathUtils() {	}
	
	/**
	 * find distance of P to line determined by AB
	 * @param P
	 * @param A
	 * @param B
	 * @return
	 */
	public synchronized static double distToLine(myPoint P, myPoint A, myPoint B) {
		myVector AB = new myVector(A,B),AP = new myVector(A,P);
		AB._normalize();
		double udv = AB._dot(AP); //project AP onto line		
		double resSq = AP.sqMagn - (udv*udv);		//AB mag is 1 so can be ignored
		if(resSq < 0) {return 0;}
		return Math.sqrt(resSq); 
	};		//MAY RETURN NAN IF point P is on line
		
	public synchronized static float distToLine(myPointf P, myPointf A, myPointf B) {
		myVectorf AB = new myVectorf(A,B),AP = new myVectorf(A,P);
		AB._normalize();
		double udv = AB._dot(AP); //project AP onto line		
		double resSq = AP.sqMagn - (udv*udv);		//AB mag is 1 so can be ignored
		if(resSq < 0) {return 0;}
		return (float) Math.sqrt(resSq); 
	};		//MAY RETURN NAN IF point P is on line
		
	/**
	 * return the projection point of P on line determined by AB between A and B
	 * @param P point to investigate
	 * @param A,B line seg endpoints
	 * @return 
	 */	
	public synchronized static myPoint projectionOnLine(myPoint P, myPoint A, myPoint B) {
		myVector AB = new myVector(A,B), AP = new myVector(A,P);
		return new myPoint(A,AB._dot(AP)/(AB._dot(AB)),AB);
	}
	public synchronized static myPointf projectionOnLine(myPointf P, myPointf A, myPointf B) {
		myVectorf AB = new myVectorf(A,B), AP = new myVectorf(A,P);
		return new myPointf(A,AB._dot(AP)/(AB._dot(AB)),AB);
	}
	/**
	 * return true if P orthogonally projects onto line determined by AB between A and B
	 * @param P point to investigate
	 * @param A,B line seg endpoints
	 * @return
	 */
	public synchronized static boolean projectsBetween(myPoint P, myPoint A, myPoint B) {
		myVector AP = new myVector(A,P), AB = new myVector(A,B);
		if(AP._dot(AB) <= 0) {return false;}		//if not greater than 0 than won't project onto AB - past A away from segment
		myVector BP = new myVector(B,P), BA = new myVector(B,A);		
		return BP._dot(BA)>0 ; 						//if not greater than 0 than won't project onto AB - past B away from segment
	}
	public synchronized static boolean projectsBetween(myPointf P, myPointf A, myPointf B) {
		myVectorf AP = new myVectorf(A,P), AB = new myVectorf(A,B);
		if(AP._dot(AB) <= 0) {return false;}		//if not greater than 0 than won't project onto AB - past A away from segment
		myVectorf BP = new myVectorf(B,P), BA = new myVectorf(B,A);		
		return BP._dot(BA)>0 ; 						//if not greater than 0 than won't project onto AB - past B away from segment
	}
	
	//public static final int O_FWD = 0, O_RHT = 1,  O_UP = 2;
	/**
	 * build axis angle orientation from passed orientation matrix
	 * @param orientation array of 3 vectors corresponding to orientation vectors
	 * @param O_FWD idx of forward orientation
	 * @param O_RHT idx of right orientation
	 * @param O_UP idx of up orientation
	 * @return axis-angle representation of orientation
	 */
	public static float[] toAxisAngle(myVectorf[] orientation, int O_FWD, int O_RHT, int O_UP) {
		float rt2 = .5f*sqrt2_f;//p.fsqrt2; 
		float angle,x=rt2,y=rt2,z=rt2,s;
		float fyrx = -orientation[O_FWD].y+orientation[O_RHT].x,
			uxfz = -orientation[O_UP].x+orientation[O_FWD].z,
			rzuy = -orientation[O_RHT].z+orientation[O_UP].y;
		float epsValCalcSq = eps_f*eps_f;
		if (((fyrx*fyrx) < epsValCalcSq) && ((uxfz*uxfz) < epsValCalcSq) && ((rzuy*rzuy) < epsValCalcSq)) {			//checking for rotational singularity
			// angle == 0
			float fyrx2 = orientation[O_FWD].y+orientation[O_RHT].x,
				fzux2 = orientation[O_FWD].z+orientation[O_UP].x,
				rzuy2 = orientation[O_RHT].z+orientation[O_UP].y,
				fxryuz3 = orientation[O_FWD].x+orientation[O_RHT].y+orientation[O_UP].z-3;
			if (((fyrx2*fyrx2) < 1)	&& (fzux2*fzux2 < 1) && ((rzuy2*rzuy2) < 1) && ((fxryuz3*fxryuz3) < 1)) {	return new float[]{0,1,0,0}; }
			// angle == pi
			angle = Pi_f;
			float fwd2x = (orientation[O_FWD].x+1)/2.0f,rht2y = (orientation[O_RHT].y+1)/2.0f,up2z = (orientation[O_UP].z+1)/2.0f,
				fwd2y = fyrx2/4.0f, fwd2z = fzux2/4.0f, rht2z = rzuy2/4.0f;
			if ((fwd2x > rht2y) && (fwd2x > up2z)) { // orientation[O_FWD].x is the largest diagonal term
				if (fwd2x< eps_f) {	x = 0;} else {			x = (float) Math.sqrt(fwd2x);y = fwd2y/x;z = fwd2z/x;} 
			} else if (rht2y > up2z) { 		// orientation[O_RHT].y is the largest diagonal term
				if (rht2y< eps_f) {	y = 0;} else {			y = (float) Math.sqrt(rht2y);x = fwd2y/y;z = rht2z/y;}
			} else { // orientation[O_UP].z is the largest diagonal term so base result on this
				if (up2z< eps_f) {	z = 0;} else {			z = (float) Math.sqrt(up2z);	x = fwd2z/z;y = rht2z/z;}
			}
			return new float[]{angle,x,y,z}; // return 180 deg rotation
		}
		//no singularities - handle normally
		myVectorf tmp = new myVectorf(rzuy, uxfz, fyrx);
		s = tmp.magn;
		if (s < eps_f){ s=1; }
		tmp._scale(s);//changes mag to s
			// prevent divide by zero, should not happen if matrix is orthogonal -- should be caught by singularity test above
		angle = (float) -Math.acos(( orientation[O_FWD].x + orientation[O_RHT].y + orientation[O_UP].z - 1)/2.0);
		
		//consume this as follows : 
		//p.rotate(O_axisAngle[0],O_axisAngle[1],O_axisAngle[2],O_axisAngle[3]);
		
	   return new float[]{angle,tmp.x,tmp.y,tmp.z};
	}//toAxisAngle
	
	
	
	/**
	 * Calculate normal to planed described by triangle ABC, non-normalized (proportional to area)
	 * @param A, B, C verts of triangle
	 * @return
	 */
	public myVector normToPlane(myPoint A, myPoint B, myPoint C) {
		return myVector._cross(new myVector(A,B),new myVector(A,C)); 
	};   // normal to triangle (A,B,C), not normalized (proportional to area)

	public myVectorf normToPlane(myPointf A, myPointf B, myPointf C) {
		return myVectorf._cross(new myVectorf(A,B),new myVectorf(A,C)); 
	};   // normal to triangle (A,B,C), not normalized (proportional to area)

	/**
	 * calculates the determinant of a Matrix
	 * @param M n x n matrix - don't over do it
	 * @return
	 */
	public static double detMat(float[][] M){ 
		double sum=0, s;
		if(M.length==1){	return(M[0][0]); }
		for(int i=0;i < M.length;i++){ 														
			float[][] minor= new float[M.length-1][M.length-1];
			for(int b=0;b<M.length;++b){
				if(b==i) {continue;}
				int bIdx = (b<i)? b : b-1;
				for(int a=1;a<M.length;++a){
					minor[a-1][bIdx] = M[a][b];
				}
			}	
			s = (i%2==0) ? 1.0f : -1.0f;
			sum += s * M[0][i] * (detMat(minor)); 										
		}
		return(sum); //returns determinant value. once stack is finished, returns final determinant.
	}//detMat

	/**
	 * calculates the determinant of a Matrix
	 * @param M n x n matrix - don't over do it
	 * @return
	 */
	public static double detMat(double[][] M){ 
		double sum=0, s;
		if(M.length==1){	return(M[0][0]); }
		for(int i=0;i < M.length;i++){ 														
			double[][] minor= new double[M.length-1][M.length-1];
			for(int b=0;b<M.length;++b){
				if(b==i) {continue;}
				int bIdx = (b<i)? b : b-1;
				for(int a=1;a<M.length;++a){
					minor[a-1][bIdx] = M[a][b];
				}
			}	
			s = (i%2==0) ? 1.0f : -1.0f;
			sum += s * M[0][i] * (detMat(minor)); 										
		}
		return(sum); //returns determinant value. once stack is finished, returns final determinant.
	}//detMat

	
	
	
	/**
	 * quake inv sqrt calc - about 30% faster than 
	 * @param x
	 * @return
	 */
    public synchronized static float invSqrtFloat(float x){
        float xhalf = x * 0.5f;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x *= (1.5f - (xhalf * x * x));   // newton iter
        return x;
    }
    /**
     * double version of quake sqrt approx - 2x as fast as Math.sqrt
     * @param x
     * @return
     */
    public static double invSqrtDouble(double x){    	
        double xhalf = x * 0.5;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6eb50c7b537a9L - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5 - (xhalf * x * x));   // newton iter
        return x;
    }
    
    /**
     * recursive factorial formulation
     * @param x
     * @return
     */
    public static int fact(int x) {
    	if(x < 2) {return 1;} if (x==2) {return 2;}
    	return x * fact(x-1);
    }
    
    /**
     * n choose k == n!/(k! * (n-k)!) - ways to choose k items from a set of size n
     * @param n top - size of pop
     * @param k bottom - size of choice
     * @return
     */
    public static long choose(long n, long k) {		//entry point
    	if(k>n) {return 0;} if (k==n) {return 1;}
    	if(n-k < k) {		return  _choose(n,n-k);   	}
    	return _choose(n,k);
    }
    private static long _choose(long n, long k) {		//multiplicative formulation
    	long res = 1;
    	for(int i=1;i<=k;++i) {    		res *= (n+1-i);res /=i;    	}
    	return res;
    }

    /**
     * n choose k == n!/(k! * (n-k)!) - ways to choose k items from a set of size n
     * @param n top - size of pop
     * @param k bottom - size of choice
     * @return
     */
    public static BigInteger choose_BigInt(long n, long k) {		//entry point
    	if(k>n) {return BigInteger.ZERO;} if (k==n) {return BigInteger.ONE;}
    	if(n-k < k) {		return  _choose_BigInt(n,n-k);   	}
    	return _choose_BigInt(n,k);
    }
    private static BigInteger _choose_BigInt(long n, long k) {		//multiplicative formulation
    	BigInteger res = BigInteger.ONE;
    	for(int i=1;i<=k;++i) {    
    		res = res.multiply(BigInteger.valueOf(n+1-i));
    		//res *= (n+1-i);
    		//res /=i;    	
    		res = res.divide(BigInteger.valueOf(i));
    	}
    	return res;
    }

    /**
     * Computes the natural logarithm of a BigInteger. 
     * Works for really big integers (practically unlimited), even when the argument 
     * falls outside the double range
     * Returns Nan if argument is negative, NEGATIVE_INFINITY if zero.
     * @param val Argument
     * @return Natural logarithm, as in Math.log()
     */
    public static double logBigInteger(BigInteger val) {
        if (val.signum() < 1) { return val.signum() < 0 ? Double.NaN : Double.NEGATIVE_INFINITY;}
        int blex = val.bitLength() - MAX_DIGITS_2; // any value in 60..1023 works ok here
        if (blex > 0) {
            val = val.shiftRight(blex);
            double res = Math.log(val.doubleValue());
            return res + blex * log2;
        } else {        	return Math.log(val.doubleValue());        }
    }

    /**
     * Computes the natural logarithm of a BigDecimal. 
     * Works for really big (or really small) arguments, even outside the double range.
     * Returns Nan if argument is negative, NEGATIVE_INFINITY if zero.
    *
     * @param val Argument
     * @return Natural logarithm, as in <tt>Math.log()</tt>
     */
    public static double logBigDecimal(BigDecimal val) {
        if (val.signum() < 1) { return val.signum() < 0 ? Double.NaN : Double.NEGATIVE_INFINITY;}
        int digits = val.precision() - val.scale(); 
        if (digits < MAX_DIGITS_10 && digits > -MAX_DIGITS_10) {return Math.log(val.doubleValue());}
        else {            return logBigInteger(val.unscaledValue()) - val.scale() * log10;}
    }

    /**
     * Computes the exponential function, returning a BigDecimal (precision ~ 16).       
     * Works for very big and very small exponents, even when the result 
     * falls outside the double range
     *
     * @param exponent Any finite value (infinite or Nan throws IllegalArgumentException)
     * @return The value of e (base of the natural logarithms) raised to the given exponent, as in Math.exp()
     */
    public static BigDecimal expBig(double exponent) {
        if (!Double.isFinite(exponent)) {throw new IllegalArgumentException("Infinite not accepted: " + exponent);}
        // e^b = e^(b2+c) = e^b2 2^t with e^c = 2^t 
        double bc = MAX_DIGITS_EXP;
        if (exponent < bc && exponent > -bc) {return new BigDecimal(Math.exp(exponent), MathContext.DECIMAL64);}
        boolean neg = false;
        if (exponent < 0) {            neg = true;            exponent = -exponent;        }
        double b2 = bc, c = exponent - bc;
        int t = (int) Math.ceil(c / log10);
        c = t * log10;
        b2 = exponent - c;
        if (neg) {          b2 = -b2;         t = -t;   }
        return new BigDecimal(Math.exp(b2), MathContext.DECIMAL64).movePointRight(t);
    }

    /**
     * Same as Math.pow(a,b) but returns a BigDecimal (precision ~ 16). 
     * Works even for outputs that fall outside the double range
     * 
     * The only limit is that b * log(a) does not overflow the double range 
     * 
     * @param a Base. Should be non-negative 
     * @param b Exponent. Should be finite (and non-negative if base is zero)
     * @return Returns the value of the first argument raised to the power of the second argument.
     */
    public static BigDecimal powBig(double a, double b) {
        if (!(Double.isFinite(a) && Double.isFinite(b)))
            throw new IllegalArgumentException(Double.isFinite(b) ? "base not finite: a=" + a : "exponent not finite: b=" + b);
        if (b == 0) {  return BigDecimal.ONE;}
        if (b == 1) {  return BigDecimal.valueOf(a);}
        if (a == 0) {
            if (b >= 0) { return BigDecimal.ZERO;}
            else {        throw new IllegalArgumentException("0**negative = infinite");}
        }
        if (a < 0) { throw new IllegalArgumentException("negative base a=" + a);}
        double x = b * Math.log(a);
        if (Math.abs(x) < MAX_DIGITS_EXP) { return BigDecimal.valueOf(Math.pow(a, b));}
        else {          				    return expBig(x);}
    }
    
	/**
	 * calculate the normal, tangent, binormal components of passed vector compared to the passed normal (needs to be normalized)
	 * @param vec
	 * @param norm
	 * @return
	 */
	public static myVectorf[] getVecFrameNonNorm(myVectorf vec, myVectorf norm) {
		myVectorf[] result = new myVectorf[3];//(2, myVector(0, 0, 0));
		result[0] = myVectorf._mult(norm,(norm._dot(vec)));//norm dir
		result[1] = myVectorf._sub(vec, result[0]);		//tan dir
		result[2] = myVectorf._cross(result[0], result[1]);
		return result;
	}
    
	/**
	 * calculate the normal, tangent, binormal components of passed vector compared to the passed normal
	 * @param vec
	 * @param norm
	 * @return
	 */
	public static myVectorf[] getVecFrameNormalized(myVectorf vec, myVectorf norm) {
		myVectorf[] nn_result = getVecFrameNonNorm(vec, norm), result = new myVectorf[nn_result.length];
		for(int i=0;i<result.length;++i) {
			result[i]=nn_result[i]._normalized();
		}
		return result;
	}
    
	/**
	 * calculate the normal, tangent, binormal components of passed vector compared to the passed normal (needs to be normalized)
	 * @param vec
	 * @param norm
	 * @return
	 */
	public static myVector[] getVecFrameNonNorm(myVector vec, myVector norm) {
		myVector[] result = new myVector[3];//(2, myVector(0, 0, 0));
		result[0] = myVector._mult(norm,(norm._dot(vec)));//norm dir
		result[1] = myVector._sub(vec, result[0]);		//tan dir
		result[2] = myVector._cross(result[0], result[1]);
		return result;
	}
    
	/**
	 * calculate the normal, tangent, binormal components of passed vector compared to the passed normal
	 * @param vec
	 * @param norm
	 * @return
	 */
	public static myVector[] getVecFrameNormalized(myVector vec, myVector norm) {
		myVector[] nn_result = getVecFrameNonNorm(vec, norm), result = new myVector[nn_result.length];
		for(int i=0;i<result.length;++i) {
			result[i]=nn_result[i]._normalized();
		}
		return result;
	}
	
    /**
     * return max value of any comparable type
     */
    public static <T extends Comparable<T>> T max(T x, T y) {      return (x.compareTo(y) > 0) ? x : y;    }
    /**
     * return min value of any comparable type
     */
    public static <T extends Comparable<T>> T min(T x, T y) {      return (x.compareTo(y) < 0) ? x : y;    }
   
    /**
     * return max value of any comparable type of 3 values
     */
    public static <T extends Comparable<T>> T max(T x, T y, T z) {    	return max(max(x,y),z);    }
    /**
     * return min value of any comparable type
     */
    public static <T extends Comparable<T>> T min(T x, T y, T z) {    	return min(min(x,y),z);     }
   
     
}//math utils

