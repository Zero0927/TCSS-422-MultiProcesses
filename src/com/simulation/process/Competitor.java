//Siyuan Zhou
package com.simulation.process;

import com.simulation.core.LogPrinter;
import com.simulation.kernel.KernelService;

public class Competitor extends MyProcess {

	private int compId;

	private int status = 0;

	/**
	 * request sequence
	 */
	private int[] reqSequence;
	/**
	 * release sequence
	 */
	private int[] relSequence;

	public Competitor(int id, int pid, int execute) {
		super(pid, execute);
		compId = id;
		if (pid != 0)
			LogPrinter.out("PID " + pid + " New Competitor " + id
					+ "- Created with job quantum " + execute);
	}

	/**
	 * @return the reqSequence
	 */
	public int[] getReqSequence() {
		return reqSequence;
	}

	/**
	 * @param reqSequence
	 *            the reqSequence to set
	 */
	public void setReqSequence(int[] reqSequence) {
		this.reqSequence = reqSequence;
	}

	/**
	 * @return the relSequence
	 */
	public int[] getRelSequence() {
		return relSequence;
	}

	/**
	 * @param relSequence
	 *            the relSequence to set
	 */
	public void setRelSequence(int[] relSequence) {
		this.relSequence = relSequence;
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
					if (j % 4 == 0 && status == 0) { // request R1
						requestRMutex(i, reqSequence[j % 2]);
						return (2 * reqSequence[j % 2] - 1);
					} else if (j % 4 == 1 && status == 1) {// request R2
						requestRMutex(i, reqSequence[j % 2]);
						return (2 * reqSequence[j % 2] - 1);
					} else if (j % 4 == 2 && status == 3) {// release R1
						releaseRMutex(i, relSequence[j % 2]);
						return reqSequence[j % 2] * 2;
					} else if (j % 4 == 3 && status == 4) {// release R2
						releaseRMutex(i, relSequence[j % 2]);
						return reqSequence[j % 2] * 2;
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
			if (status == 2) {
				LogPrinter.out(String.format(
						"PID %d Competitor %d doing some works - "
								+ "At quantium %d of %d", pcb.pid, compId,
						doneWorkTime, pcb.liftTime));
				status++;
			}
			return 5;
		}
	}

	private void releaseRMutex(int i, int r) {
		LogPrinter.out(String.format(
				"PID %d Competitor %d release resource %d - "
						+ "At quantium %d of %d", pcb.pid, compId, r,
				doneWorkTime, pcb.liftTime));
		status++;
		if (status == 5) {
			status = 0;
		}

	}

	private void requestRMutex(int i, int r) {
		LogPrinter.out(String.format(
				"PID %d Competitor %d request resource %d - "
						+ "At quantium %d of %d", pcb.pid, compId, r,
				doneWorkTime, pcb.liftTime));
		status++;

	}

	@Override
	public void newKernelServiceDevice(KernelService[] resourceService,
			int[] ksEvent) {
		pcb.resourceMutex = resourceService;
		pcb.ksEvent = ksEvent;
	}

	@Override
	public String toString() {
		return "PID " + pcb.pid + " Competitor " + compId;
	}

}
