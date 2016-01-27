//Siyuan Zhou
package com.simulation.process;

import com.simulation.core.LogPrinter;
import com.simulation.core.PCB;
import com.simulation.kernel.KernelService;

/**
 * Class of process simulation
 */
public abstract class MyProcess {

	/**
	 * The pcb of process
	 */
	protected PCB pcb;

	/**
	 * The count of quanta has done
	 */
	protected int doneWorkTime;

	protected boolean requestMutex;

	/**
	 * Create a new process with it's total execute time
	 * 
	 * @param execute
	 */
	public MyProcess(int pid, int execute) {
		pcb = new PCB();
		pcb.pid = pid;
		pcb.liftTime = execute;
		doneWorkTime = 0;
	}

	/**
	 * Do work of process
	 * 
	 * @param quantum
	 * @return return 1 for lock, return 2 for unlock, return 3 for time
	 *         consuming over, return 4 for done of work
	 */
	public abstract int doProcess(int quantum);

	/**
	 * Add new kernel service event
	 * 
	 * @param[] kServices
	 * 
	 * @param ksEvent
	 */
	public abstract void newKernelServiceDevice(KernelService[] writeService, int[] ksEvent);

	/**
	 * Restart of this process
	 * 
	 * @param execute
	 */
	public void restart(int execute) {
		LogPrinter.out("PID " + pcb.pid + " - Restart with job " + execute);
		pcb.liftTime = execute;
		doneWorkTime = 0;
	}

	/**
	 * 
	 */
	public void close() {
		LogPrinter.out("PID " + pcb.pid + " - Destroy");
	}

	/**
	 * @return the pStatus
	 */
	public int getStatus() {
		return pcb.pStatus;
	}

	/**
	 * @param pStatus
	 *            the pStatus to set
	 */
	public void setStatus(int pStatus) {
		this.pcb.pStatus = pStatus;
	}

	/**
	 * @return the pid
	 */
	public int getPid() {
		return pcb.pid;
	}

	/**
	 * @return the liftTime
	 */
	public int getLiftTime() {
		return pcb.liftTime;
	}

	/**
	 * @return the liftTime
	 */
	public int getcursorTime() {
		return doneWorkTime;
	}

	/**
	 * Return the process control block
	 * 
	 * @return
	 */
	public PCB getPCB() {
		return pcb;
	}
}
