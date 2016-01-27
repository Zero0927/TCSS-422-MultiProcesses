//Siyuan Zhou
package com.simulation.core;

import java.util.HashSet;
import java.util.Set;

import com.simulation.kernel.KernelService;
import com.simulation.process.MyProcess;

public class DeadLockMonitor {
	private KernelService[] resouces;

	public DeadLockMonitor(KernelService[] resouces) {
		this.resouces = resouces;
	}

	/**
	 * @return the resouces
	 */
	public KernelService[] getResouces() {
		return resouces;
	}

	/**
	 * @param resouces
	 *            the resouces to set
	 */
	public void setResouces(KernelService[] resouces) {
		this.resouces = resouces;
	}

	/**
	 * @return
	 */
	public Set<Integer> findDeadLock() {
		Set<Integer> checkPID = new HashSet<Integer>();
		Set<Integer> possessPID;
		Set<Integer> rstPID = new HashSet<Integer>();
		for (int i = 0; i < resouces.length; i++) {
			possessPID = new HashSet<Integer>();
			MyProcess proc = resouces[i].possessProc();
			if (proc == null || checkPID.contains(proc.getPid()))
				continue;
			checkPID.add(proc.getPid());
			possessPID.add(proc.getPid());

			KernelService r = findWaiting(proc);
			while (r != null) {
				proc = r.possessProc();
				if (proc == null)
					break;
				checkPID.add(proc.getPid());
				if (possessPID.contains(proc.getPid())) {
					rstPID.addAll(possessPID);
					rstPID.add(proc.getPid());
					return rstPID;
				}
				possessPID.add(proc.getPid());
				r = findWaiting(proc);
			}
		}
		return rstPID;
	}

	/**
	 * @param proc
	 * @return
	 */
	private KernelService findWaiting(MyProcess proc) {
		for (int i = 0; i < resouces.length; i++) {
			if (resouces[i].waitingProcs().contains(proc))
				return resouces[i];
		}
		return null;
	}
}
