//Siyuan Zhou
package com.simulation.core;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.simulation.kernel.KernelService;
import com.simulation.kernel.impl.Mutex;
import com.simulation.kernel.impl.SemaphoreMutex;
import com.simulation.process.MyProcess;

/**
 * The processor of simulation
 *
 */
public class Scheduler {
	private final int QUANTUM = 300;
	private Queue<MyProcess> readyQueue;

	private KernelService[] kServices;
	private KernelService[] resouces;

	private ProcessGen procGenerator;

	private DeadLockMonitor dmonitor;

	public Scheduler() {
		readyQueue = new ConcurrentLinkedQueue<MyProcess>();

		// initial kernel service devices
		kServices = new KernelService[4];
		kServices[0] = new SemaphoreMutex(this, "write", 0);
		kServices[1] = new SemaphoreMutex(this, "read", -1);
		kServices[2] = new Mutex(this, "critical");

		// initial kernel service devices
		resouces = new KernelService[2];
		resouces[0] = new Mutex(this, "R1");
		resouces[1] = new Mutex(this, "R2");

		dmonitor = new DeadLockMonitor(resouces);

		procGenerator = new ProcessGen(kServices, resouces);
	}

	/**
	 * @param i
	 */
	public void startConsumerProducer(int i) {
		LogPrinter
				.out("==========================================================");
		LogPrinter.out("               Consumer Producer with " + i);
		LogPrinter
				.out("==========================================================");
		CriticalRegion.MAX_SHARE = i;
		// Add initial process
		readyQueue.clear();
		readyQueue.add(procGenerator.intialProcess());
		// Generate new process
		procGenerator.buildRandomCPPair(readyQueue);
		boolean over = false;
		while (!over) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Get one ready process from list
			MyProcess p = readyQueue.poll();

			if (p.getStatus() == PCB.STAT_IDLE) {
				readyQueue.add(p);
				continue;
			}

			p.setStatus(PCB.STAT_RUNNING);

			switch (p.doProcess(QUANTUM)) {
			case 0:// work done
				procGenerator.rebuildRandom(readyQueue, p);
				break;
			case 1: // write mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().writeMutex.addWaitingProcess(p);
				break;
			case 2: // write mutex unlock
				p.getPCB().writeMutex.releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 3: // critical mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().criticalMutex.addWaitingProcess(p);
				break;
			case 4:// critical mutex unlock
				p.getPCB().criticalMutex.releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 5:// read mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().readMutex.addWaitingProcess(p);
				break;
			case 6:// read mutex unlock
				p.getPCB().readMutex.releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 7:// time consuming
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case -1:
				over = true;
				break;
			}
		}
		LogPrinter
				.out("==========================================================");
		LogPrinter.out("               Consumer Producer over");
		LogPrinter
				.out("==========================================================");
	}

	/**
	 * @param i
	 */
	public void startShareResource(int i) {
		LogPrinter
				.out("==========================================================");
		LogPrinter.out("         Mutual Resource Users with no deadlock ");
		LogPrinter
				.out("==========================================================");
		
		resouces[0].clear();
		resouces[1].clear();
		dmonitor.setResouces(resouces);
		// Add initial process
		readyQueue.clear();
		readyQueue.add(procGenerator.intialProcess());
		// Generate new process
		procGenerator.buildRandomMRPairNoDeadLock(readyQueue);
		for (int k = 0; k <= i; k++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Get one ready process from list
			MyProcess p = readyQueue.poll();

			if (k % 100 == 0) {
				Set<Integer> pids = dmonitor.findDeadLock();
				if (pids.size() != 0) {
					String s = "DEADLOCK MONITOR: deadlock detected for processes";
					for (Integer pid : pids) {
						s += " PID" + pid + ",";
					}
					LogPrinter.out(s);
				} else
					LogPrinter.out("DEADLOCK MONITOR: no deadlock detected");
			}

			if (p.getStatus() == PCB.STAT_IDLE) {
				readyQueue.add(p);
				continue;
			}

			p.setStatus(PCB.STAT_RUNNING);

			switch (p.doProcess(QUANTUM)) {
			case 0:// work done
				procGenerator.rebuildRandom(readyQueue, p);
				break;
			case 1: // R1 mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().resourceMutex[0].addWaitingProcess(p);
				break;
			case 2: // R1 mutex unlock
				p.getPCB().resourceMutex[0].releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 3: // R2 mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().resourceMutex[1].addWaitingProcess(p);
				break;
			case 4:// R2 mutex unlock
				p.getPCB().resourceMutex[1].releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 5:// time consuming
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			}
		}
		LogPrinter
				.out("==========================================================");
		LogPrinter.out("    Mutual Resource Users with no deadlock over");
		LogPrinter
				.out("==========================================================");
	}

	/**
	 * 
	 */
	public void startShareResourceDeadLock(int i) {
		LogPrinter
				.out("==========================================================");
		LogPrinter.out("       Mutual Resource Users with deadlock");
		LogPrinter
				.out("==========================================================");
		CriticalRegion.MAX_SHARE = i;
		resouces[0].clear();
		resouces[1].clear();
		dmonitor.setResouces(resouces);
		// Add initial process
		readyQueue.clear();
		readyQueue.add(procGenerator.intialProcess());
		// Generate new process
		procGenerator.buildRandomMRPairDeadLock(readyQueue);
		for (int k = 0; k <= i; k++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (k % 100 == 0) {
				Set<Integer> pids = dmonitor.findDeadLock();
				if (pids.size() != 0) {
					String s = "DEADLOCK MONITOR: deadlock detected for processes";
					for (Integer pid : pids) {
						s += " PID" + pid + ",";
					}
					LogPrinter.out(s);
				} else
					LogPrinter.out("DEADLOCK MONITOR: no deadlock detected");
			}
			// Get one ready process from list
			MyProcess p = readyQueue.poll();

			if (p.getStatus() == PCB.STAT_IDLE) {
				readyQueue.add(p);
				continue;
			}

			p.setStatus(PCB.STAT_RUNNING);

			switch (p.doProcess(QUANTUM)) {
			case 0:// work done
				procGenerator.rebuildRandom(readyQueue, p);
				break;
			case 1: // R1 mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().resourceMutex[0].addWaitingProcess(p);
				break;
			case 2: // R1 mutex unlock
				p.getPCB().resourceMutex[0].releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 3: // R2 mutex lock
				p.setStatus(PCB.STAT_BLOCK);
				p.getPCB().resourceMutex[1].addWaitingProcess(p);
				break;
			case 4:// R2 mutex unlock
				p.getPCB().resourceMutex[1].releaseSevice(p);
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			case 5:// time consuming
				p.setStatus(PCB.STAT_READY);
				readyQueue.add(p);
				break;
			}
		}
		LogPrinter
				.out("==========================================================");
		LogPrinter.out("        Mutual Resource Users with deadlock over");
		LogPrinter
				.out("==========================================================");
	}

	/**
	 * Add the process into the ready queue
	 * 
	 * @param proc
	 */
	public void callbackNotify(MyProcess proc) {
		proc.setStatus(PCB.STAT_READY);
		readyQueue.add(proc);
	}

	public static void main(String[] args) {
		Scheduler myProcesser = new Scheduler();
		myProcesser.startConsumerProducer(10);
		myProcesser.startShareResource(1000);
		myProcesser.startShareResourceDeadLock(1000);
	}
}
