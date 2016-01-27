//Siyuan Zhou
package com.simulation.process;

import com.simulation.kernel.KernelService;

public class IdleProcess extends MyProcess {

	public IdleProcess(int pid, int execute) {
		super(pid, execute);
	}

	@Override
	public int doProcess(int quantum) {
		return 5;// done
	}

	@Override
	public void newKernelServiceDevice(KernelService[] writeService,
			int[] ksEvent) {
	}
	
	@Override
	public String toString() {
		return "Idle";
	}

}
