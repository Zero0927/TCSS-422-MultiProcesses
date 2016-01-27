//Siyuan Zhou
package com.simulation.process;

import com.simulation.core.CriticalRegion;
import com.simulation.core.LogPrinter;
import com.simulation.kernel.KernelService;

public class Producer extends MyProcess {
	private int processId;

	private int R_W = 0;

	private int internal = 0;

	public Producer(int i, int pid, int execute) {
		super(pid, execute);
		if (pid != 0)
			LogPrinter.out("PID " + pid + " New Produser " + i
					+ "- Created with job quantum " + execute);
		processId = i;
	}

	@Override
	public int doProcess(int quantum) {
		// End of the time slot
		int thisEnd = Math.min(quantum, pcb.liftTime - doneWorkTime);
		for (int i = doneWorkTime + 1; i <= doneWorkTime + thisEnd; i++) {
			// check kernel service event
			for (int j = 0; j < pcb.ksEvent.length; j++) {
				if (i >= pcb.ksEvent[j] && i % pcb.ksEvent[j] == 0) {
					doneWorkTime = i;
					if (j % 4 == 0 && R_W == 0) { // request the write mutex
						requestWriteMutex(i);
						return 1;
					} else if (j % 4 == 1 && R_W == 1) { // request the critical
															// mutex
						requestCriticalMutex(i);
						return 3;
					} else if (j % 4 == 2 && R_W == 3) { // release the critical
															// mutex
						releaseCriticalMutex(i);
						return 4;
					} else if (j % 4 == 3 && R_W == 4) { // release the read
															// mutex
						releaseReadMutex(i);
						return 6;
					}

				}
			}
		}

		// check time slot
		if (doneWorkTime + quantum >= pcb.liftTime) {
			return 0;
		} else {
			pcb.runThreads = (int) (System.currentTimeMillis() % 100);
			doneWorkTime += quantum;
			if (R_W == 2) {
				internal = CriticalRegion.shareInt;
				internal += 1;
				CriticalRegion.shareInt = internal;
				LogPrinter.out(String.format(
						"PID %d Producer %d Produce product %d - "
								+ "At quantium %d of %d", pcb.pid, processId,
						internal, doneWorkTime, pcb.liftTime));
				R_W++;
			}
			return 7;
		}
	}

	/**
	 * @param t
	 */
	private void requestWriteMutex(int t) {
		LogPrinter.out(String.format(
				"PID %d Producer %d Request Write MutexLock - "
						+ "At quantium %d of %d", pcb.pid, processId, t,
				pcb.liftTime));
		R_W++;
	}

	/**
	 * @param t
	 */
	private void releaseReadMutex(int t) {
		R_W = 0;
		LogPrinter.out(String.format(
				"PID %d Producer %d Release Read MutexLock - "
						+ "At quantium %d of %d", pcb.pid, processId, t,
				pcb.liftTime));

	}

	/**
	 * @param t
	 */
	private void releaseCriticalMutex(int t) {
		LogPrinter.out(String.format(
				"PID %d Producer %d Release Critical MutexLock - "
						+ "At quantium %d of %d", pcb.pid, processId, t,
				pcb.liftTime));
		R_W++;
	}

	/**
	 * @param t
	 */
	private void requestCriticalMutex(int t) {
		LogPrinter.out(String.format(
				"PID %d Producer %d Request Critical MutexLock - "
						+ "At quantium %d of %d", pcb.pid, processId, t,
				pcb.liftTime));
		R_W++;
	}

	@Override
	public void newKernelServiceDevice(KernelService[] kernelService,
			int[] ksEvent) {
		pcb.writeMutex = kernelService[0];
		pcb.readMutex = kernelService[1];
		pcb.criticalMutex = kernelService[2];
		pcb.ksEvent = ksEvent;
	}

	@Override
	public String toString() {
		return "PID " + pcb.pid + " Producer " + processId;
	}

}
