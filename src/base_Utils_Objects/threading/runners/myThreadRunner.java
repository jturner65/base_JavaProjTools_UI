package base_Utils_Objects.threading.runners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import base_Utils_Objects.io.MessageObject;

/**
 * manage a runner that will launch a number of callables suitable 
 * for machine arch to manage multi-threaded calcs.  Instances of this class 
 * will manage instancing and invoking all threads to execute functionality 
 * in either MT or ST environment.
 * 
 * @author john
 *
 */
public abstract class myThreadRunner {
	//msg object to handle console/log IO
	protected final MessageObject msgObj;
	//whether or not this calculation can be executed in multi-thread
	protected final boolean canMultiThread;
	//the # of usable threads available for MT exec
	protected final int numUsableThreads;
	//ref to thread executor
	protected final ExecutorService th_exec;	
	//# of work units to perform 
	protected final int numWorkUnits;
	
	List<Future<Boolean>> ExMapperFtrs = new ArrayList<Future<Boolean>>();
	List<Callable<Boolean>> ExMappers = new ArrayList<Callable<Boolean>>();

	
	public myThreadRunner(MessageObject _msgObj, ExecutorService _th_exec, boolean _canMT, int _numThds, int _numWorkUnits) {
		msgObj = _msgObj;
		th_exec = _th_exec;
		canMultiThread = _canMT;
		numUsableThreads = _numThds;
		numWorkUnits = _numWorkUnits;
	}//myThreadRunner
		
	/**
	 * determine how many work elements should be assigned per thread 
	 * @param numVals total number of work elements to execute
	 * @param numThds total number of threads available
	 * @return number of work elements per thread to assign
	 */
	public final int calcNumPerThd(int numVals, int numThds) {	return (int) ((numVals -1)/(1.0*numThds)) + 1;	}//calcNumPerThd
	
	/**
	 * build callable object that will be invoked
	 * @param dataSt start idx in data
	 * @param dataEnd end idx in data
	 * @param pIdx thread/partition idx
	 * @return callable to be invoked 
	 */
	protected abstract void execPerPartition(List<Callable<Boolean>> ExMappers, int dataSt, int dataEnd, int pIdx, int ttlParts);
	
	/**
	 * execute this thread runner
	 */
	public final void runMe() {
		if(canMultiThread){
			int numPartitions = Math.round(numWorkUnits/(1.0f*getNumPerPartition()) + .5f);
			if(numPartitions < 1) {numPartitions = 1;}
			int numPerPartition = calcNumPerThd(numWorkUnits,numPartitions);
			
			ExMappers = new ArrayList<Callable<Boolean>>();
			ExMapperFtrs = new ArrayList<Future<Boolean>>();

			int dataSt = 0;
			int dataEnd = numPerPartition;
			for(int pIdx = 0; pIdx < numPartitions-1;++pIdx) {
				execPerPartition(ExMappers, dataSt, dataEnd, pIdx, numPartitions);
				dataSt = dataEnd;
				dataEnd +=numPerPartition;			
			}
			if(dataSt < numWorkUnits) {execPerPartition(ExMappers, dataSt, numWorkUnits, numPartitions-1, numPartitions);}	
			try {ExMapperFtrs = th_exec.invokeAll(ExMappers);for(Future<Boolean> f: ExMapperFtrs) { f.get(); }} catch (Exception e) { e.printStackTrace(); }
			
		} else {
			runMe_Indiv_ST();
		}
		runMe_Indiv_End();
	}//runMe()
	/**
	 * return approx desired # of work units to perform per partition (thread)
	 * @return
	 */
	protected abstract int getNumPerPartition();
//	/**
//	 * perform multi-threaded execution
//	 * @param numPartitions # of work partitions (== # of threads)
//	 * @param numPerPartition # of work units/data per thread
//	 */
//	protected abstract void runMe_Indiv_MT(int numPartitions, int numPerPartition);
	/**
	 * perform single threaded execution
	 */
	protected abstract void runMe_Indiv_ST();
	/**
	 * after either MT or ST execution, final execution to perform
	 */
	protected abstract void runMe_Indiv_End();

	
}//class myThreadRunner
