package base_Utils_Objects.interpolants.base;

import base_Utils_Objects.interpolants.Cubic_Interpolant;
import base_Utils_Objects.interpolants.InterpolantBehavior;
import base_Utils_Objects.interpolants.InterpolantTypes;
import base_Utils_Objects.interpolants.Linear_Interpolant;
import base_Utils_Objects.interpolants.Quintic_Interpolant;
import base_Utils_Objects.interpolants.Sine_Interpolant;

/**
 * this class manages an interpolant - a value to be varied between 0 and 1
 * to be used to interpolate, extrapolate, or animate, between some set of values
 * @author john
 *
 */
public abstract class baseInterpolant {
	
	/**
	 * basic interpolant - always between 0 and 1 and linearly evolved
	 */
	private float raw_t;
	/**
	 * interpolant to be used by consumer
	 */
	private float t;
	
	/**
	 * used to determine direction of modification for interpolant
	 */
	private float sign = 1.0f;
	
	/**
	 * for interpolants that stop at extremal locations
	 */
	private float stopTimeT = 0.0f;
	public final float stopTimerDur;
	public static final float _dfltStopTimerDur = 3.0f;
	private boolean isStopped;
	/**
	 * used to determine desired behavior of animation when hitting extremal values 
	 */
	private InterpolantBehavior animBehavior; 
	
	
	public baseInterpolant(float _t) {
		this(_t, _dfltStopTimerDur);
	}
	
	public baseInterpolant(float _t, float _stopTimerDur) {
		setValue(_t);
		setAnimBehavior(0);
		stopTimerDur = _stopTimerDur;	
		isStopped = false;
	}
	
	public final void setAnimBehavior(int _idx) {animBehavior = InterpolantBehavior.getVal(_idx);}
	
	public final void setValue(float _t) {
		raw_t=(_t<0 ? 0 : _t>1 ? 1 : _t);
		isStopped = false;	
		t=calcInterpolant(raw_t);
	}
	/**
	 * return processed fade/interpolant
	 * @return
	 */
	public float getValue() {return t;}
	
	/**
	 * function evolves rawT, only to be used for actual animation
	 * @param amt
	 */
	protected final void _evolveRawT (float amt) {
		switch(animBehavior) {
			case pingPong 				:
			case pingPongStop 			: 
			case oneWayFwdLoop 			:
			case oneWayFwdStopLoop		: {	raw_t += amt;	break;}
			case oneWayBkwdLoop 		: 
			case oneWayBkwdStopLoop		: {	raw_t -= amt;	break;}			
			default 					: {	raw_t += amt;}//unknown defaults to ping-pong
		}//switch			
	}
	
	public final float evolveInterpolant(float delta) {		
		//if(isStopped) {	System.out.println("Stopped :stopTimeT : " + stopTimeT + " | delta :"+delta + " | stopTimerDur : " + stopTimerDur);}
		switch(animBehavior) {
			case pingPong 		: {
				isStopped = false;
				raw_t += (sign * delta);	
				if(raw_t >= 1.0f) {raw_t = 1.0f;sign = -1.0f;} else if (raw_t <= 0.0f) {	raw_t = 0.0f;	sign = 1.0f;}		
				break;}
			case pingPongStop 		: {
				if(isStopped) {//manage timer for duration of stop
					stopTimeT += delta;
					isStopped = (stopTimeT < stopTimerDur);
				} else {		
					raw_t += (sign * delta);	
					if(raw_t >= 1.0f) {
						raw_t = 1.0f;   sign = -1.0f;
						stopTimeT = 0.0f;	isStopped = true;
					} else if (raw_t <= 0.0f) {
						raw_t = 0.0f;	sign = 1.0f;
						stopTimeT = 0.0f;	isStopped = true;
					} 
				}
				break;}
			case oneWayFwdLoop 		: {
				isStopped = false;
				raw_t += delta;	
				if(raw_t >= 1.0f) {raw_t = 0.0f;}
				break;}
			case oneWayBkwdLoop 	: {
				isStopped = false;
				raw_t  -= delta;	
				if (raw_t <= 0.0f) {	raw_t = 1.0f;}	
				break;}		
			
			case oneWayFwdStopLoop		: {
				if(isStopped) {//manage timer for duration of stop
					stopTimeT += delta;
					isStopped = (stopTimeT < stopTimerDur);
					if(!isStopped) {						raw_t = 0.0f;}//turning off stop
				} else {		
					raw_t += delta;	
					if(raw_t >= 1.0f) {
						raw_t = 1.0f;
						stopTimeT = 0.0f;	isStopped = true;
					}
				}
				break;}
			case oneWayBkwdStopLoop		: {
				if(isStopped) {//manage timer for duration of stop
					stopTimeT += delta;
					isStopped = (stopTimeT < stopTimerDur);
					if(!isStopped) {						raw_t = 1.0f;}//turning off stop
				} else {		
					raw_t  -= delta;	
					if (raw_t <= 0.0f) {
						raw_t = 0.0f;
						stopTimeT = 0.0f;	isStopped = true;
					}	
				}
				break;}
			
			default 		:	{//unknown defaults to ping-pong
				raw_t += (sign * delta);	
				if(raw_t > 1.0f) {raw_t = 1.0f;sign = -1.0f;} else if (raw_t < 0.0f) {	raw_t = 0.0f;	sign = 1.0f;}					
			}
		}//switch
//		raw_t += (sign * delta);			
//		if(raw_t > 1.0f) {raw_t = 1.0f;sign = -1.0f;} else if (raw_t < 0.0f) {	raw_t = 0.0f;	sign = 1.0f;}
		t=calcInterpolant(raw_t);
		return t;
	}
	/**
	 * build actual interpolant fade 
	 * @return
	 */
	public final float calcInterpolant(float _rawt) {
		if(_rawt<=0) {return 0.0f;}
		if(_rawt>=1.0f) {return 1.0f;}
		return calcInterpolant_Indiv(_rawt);
	}
	protected abstract float calcInterpolant_Indiv(float _rawt);
	
	/**
	 * build an interpolant of passed type, for type defined in baseInterpolant
	 * @param animType
	 * @return
	 */
	public static baseInterpolant buildInterpolant(InterpolantTypes animType, float _initT) {return buildInterpolant(animType, _initT, _dfltStopTimerDur);}
	public static baseInterpolant buildInterpolant(InterpolantTypes animType, float _initT, float _stopTime) {
		switch(animType) {
			case linear 				: {		return new Linear_Interpolant(_initT,_stopTime);		}
			case smoothVelocity			: {		return new Cubic_Interpolant(_initT,_stopTime);		}
			case smoothAccel 			: {		return new Quintic_Interpolant(_initT,_stopTime);		}
			case sine					: {		return new Sine_Interpolant(_initT,_stopTime);		}
			default : {
				System.out.println("baseInterpolant :: buildInterpolant :: Unknown interpolant type : " + animType.toString() + ".  Aborting.");
				return null;
			}
		}
	}//buildInterpolant
	

}//class baseInterpolant

