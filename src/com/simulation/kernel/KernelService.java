//Siyuan Zhou
package com.simulation.kernel;

import java.util.Queue;

import com.simulation.process.MyProcess;

/**
 * Interface of kernel service
 *
 */
public interface KernelService {
	/**
	 * Add process of the io waiting queue
	 * 
	 * @param proc
	 */
	public void addWaitingProcess(MyProcess proc);

	/**
	 * Get the mutex name
	 * 
	 * @return
	 */
	public String kernelServiceName();

	/**
	 * Enter the service and possess it
	 * 
	 * @param p
	 */
	public void enterSevice(MyProcess p);
	
	/**
	 * Release the kernel service
	 * 
	 * @param p
	 */
	public void releaseSevice(MyProcess p);
	 
	/**
	 * Check possess of this service
	 * @return
	 */
	public boolean isPossess();
	
	/**
	 * 
	 * @return
	 */
	public MyProcess possessProc();
	
	/**
	 * 
	 * @return
	 */
	public Queue<MyProcess> waitingProcs();

	/**
	 * Clear the service
	 */
	public void clear();
}
