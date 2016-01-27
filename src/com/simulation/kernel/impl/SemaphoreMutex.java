//Siyuan Zhou
package com.simulation.kernel.impl;

import com.simulation.core.Scheduler;
import com.simulation.process.MyProcess;

public class SemaphoreMutex extends Mutex {

	private int sema;

	public SemaphoreMutex(Scheduler scheduler, String name, int i) {
		super(scheduler, name);
		sema = i;
	}

	@Override
	public void addWaitingProcess(MyProcess proc) {
		if (proc == null)
			return;

		waitingQueue.add(proc);

		if (sema >= 0) {
			releaseSevice(proc);
			sema--;
		}
	}

	@Override
	public void releaseSevice(MyProcess p) {
		sema++;
		MyProcess newPossess = waitingQueue.poll();
		if (newPossess != null) {
			possess = newPossess;
			callback.callbackNotify(newPossess);
			sema--;
		}
	}
}
