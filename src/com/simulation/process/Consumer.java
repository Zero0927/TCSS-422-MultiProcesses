//Siyuan Zhou
package com.simulation.process;

import com.simulation.core.CriticalRegion;
import com.simulation.core.LogPrinter;
import com.simulation.kernel.KernelService;

public class Consumer extends MyProcess {

	private int processId;

	private int rStatus = 0;

	public Consumer(int i, int pid, int execute) {
		super(pid, execute);
		if (pid != 0)
			LogPrinter.out("PID " + pid + " New Consumer " + i
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
					if (j % 2 == 0 && rStatus == 0) { // request the read mutex
						requestReadMutex(i);
						return 5;
					} else if (rStatus == 2) { // release the write mutex
						releaseWriteMutex(i);
						return 2;
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
			if (rStatus == 1) {
				LogPrinter.out(String.format(
						"PID %d Consumer %d Consume product %d - "
								+ "At quantium %d of %d", pcb.pid, processId,
						CriticalRegion.shareInt, doneWorkTime, pcb.liftTime));
				rStatus++;
				if (CriticalRegion.shareInt == CriticalRegion.MAX_SHARE)
					return -1;
			}
			return 7;
		}
	}

	/**
	 * @param t
	 */
	private void releaseWriteMutex(int t) {
		LogPrinter.out(String.format(
				"PID %d Consumer %d Release Write MutexLock - "
						+ "At quantium %d of %d", pcb.pid, processId, t,
				pcb.liftTime));
		rStatus = 0;
	}

	/**
	 * @param t
	 */
	private void requestReadMutex(int t) {
		LogPrinter.out(String.format(
				"PID %d Consumer %d Request Read MutexLock - "
						+ "At quantium %d of %d", pcb.pid, processId, t,
				pcb.liftTime));
		rStatus++;
	}

	@Override
	public void newKernelServiceDevice(KernelService[] kernelService,
			int[] ksEvent) {
		pcb.writeMutex = kernelService[0];
		pcb.readMutex = kernelService[1];
		pcb.ksEvent = ksEvent;
	}

	@Override
	public String toString() {
		return "PID " + pcb.pid + " Consumer " + processId;
	}
}
