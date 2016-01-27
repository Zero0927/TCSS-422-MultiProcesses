//Siyuan Zhou
package com.simulation.kernel.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.simulation.core.Scheduler;
import com.simulation.kernel.KernelService;
import com.simulation.process.MyProcess;

public class Mutex implements KernelService {

	/**
	 * 
	 */
	protected Scheduler callback;

	private String mutexName;

	protected MyProcess possess;

	protected Queue<MyProcess> waitingQueue;

	public Mutex(Scheduler scheduler, String name) {
		mutexName = name;
		callback = scheduler;
		waitingQueue = new ConcurrentLinkedQueue<MyProcess>();
	}

	@Override
	public void addWaitingProcess(MyProcess proc) {
		if (proc == null)
			return;
		waitingQueue.add(proc);
		if (possess == null)
			releaseSevice(proc);
	}

	@Override
	public String kernelServiceName() {
		return mutexName;
	}

	@Override
	public void releaseSevice(MyProcess p) {
		possess = null;
		MyProcess newPossess = waitingQueue.poll();
		if (newPossess != null){
			possess = newPossess;
			callback.callbackNotify(newPossess);
		}
	}

	@Override
	public void enterSevice(MyProcess p) {
		if (p != null)
			possess = p;
	}

	@Override
	public boolean isPossess() {
		return possess != null;
	}

	@Override
	public MyProcess possessProc() {
		return possess;
	}

	@Override
	public Queue<MyProcess> waitingProcs() {
		return waitingQueue;
	}

	@Override
	public void clear() {
		waitingQueue.clear();
		possess = null;
	}
}
