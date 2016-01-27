//Siyuan Zhou
package com.simulation.core;

import java.util.Queue;
import java.util.Random;

import com.simulation.kernel.KernelService;
import com.simulation.process.Competitor;
import com.simulation.process.Consumer;
import com.simulation.process.IdleProcess;
import com.simulation.process.MyProcess;
import com.simulation.process.Producer;

/**
 * Process generator
 */
public class ProcessGen {
	private Random rand;

	private int MAXID = 900;
	private int MAXJOB = 30000;

	private int cpid = 0;

	private KernelService[] kServices;
	private KernelService[] resources;

	public ProcessGen(KernelService[] kServices, KernelService[] resources) {
		rand = new Random(System.currentTimeMillis());
		this.kServices = kServices;
		this.resources = resources;
	}

	/**
	 * Create an idle process when none task
	 * 
	 * @return
	 */
	public MyProcess intialProcess() {
		MyProcess proc = new IdleProcess(0, 0);
		proc.setStatus(PCB.STAT_IDLE);
		return proc;
	}

	/**
	 * Create new process in pairs. Return in two element array with the first
	 * is Producer and the second is Consumer
	 * 
	 * @return
	 */
	private MyProcess[] createCPPair() {
		int pairid = cpid++;
		MyProcess produceProc = new Producer(pairid, rand.nextInt(MAXID) + 1,
				MAXJOB);
		int[] ksInerupt = new int[4];
		ksInerupt[0] = 1000 + rand.nextInt(1000);
		ksInerupt[1] = 2000 + rand.nextInt(1000);
		ksInerupt[2] = 6000 + rand.nextInt(2000);
		ksInerupt[3] = 8000 + rand.nextInt(2000);

		produceProc.newKernelServiceDevice(kServices, ksInerupt);
		produceProc.setStatus(PCB.STAT_READY);

		MyProcess consumeProc = new Consumer(pairid, rand.nextInt(MAXID) + 1,
				MAXJOB);
		ksInerupt = new int[2];
		ksInerupt[0] = 1000 + rand.nextInt(1000);
		ksInerupt[1] = 5000 + rand.nextInt(1000);

		consumeProc.newKernelServiceDevice(kServices, ksInerupt);
		consumeProc.setStatus(PCB.STAT_READY);

		return new MyProcess[] { produceProc, consumeProc };

	}

	/**
	 * Create new process of Mutual Resource Users in pairs. Return in two
	 * element array with the process as resource competitor
	 * 
	 * @return
	 */
	private MyProcess[] createMRPair() {
		int pairid = cpid++;
		MyProcess cp1 = new Competitor(pairid, rand.nextInt(MAXID) + 1, MAXJOB);
		int[] ksInerupt = new int[4];
		ksInerupt[0] = 1000 + rand.nextInt(1000);
		ksInerupt[1] = 4000 + rand.nextInt(1000);
		ksInerupt[2] = 7000 + rand.nextInt(1000);
		ksInerupt[3] = 9000 + rand.nextInt(1000);

		cp1.newKernelServiceDevice(resources, ksInerupt);
		cp1.setStatus(PCB.STAT_READY);

		pairid = cpid++;
		MyProcess cp2 = new Competitor(pairid, rand.nextInt(MAXID) + 1, MAXJOB);
		ksInerupt = new int[4];
		ksInerupt[0] = 1000 + rand.nextInt(1000);
		ksInerupt[1] = 4000 + rand.nextInt(1000);
		ksInerupt[2] = 7000 + rand.nextInt(1000);
		ksInerupt[3] = 9000 + rand.nextInt(1000);

		cp2.newKernelServiceDevice(resources, ksInerupt);
		cp2.setStatus(PCB.STAT_READY);

		return new MyProcess[] { cp1, cp2 };
	}

	/**
	 * @param readyQueue
	 */
	public void buildRandomCPPair(Queue<MyProcess> readyQueue) {
		cpid = 0;
		if (readyQueue.size() < 10) {
			// Add a bunch of process
			for (int i = 0; i < 10; i++) {
				MyProcess[] procs = createCPPair();
				readyQueue.add(procs[0]);
				readyQueue.add(procs[1]);
			}

		}
	}

	/**
	 * @param readyQueue
	 */
	public void buildRandomMRPairNoDeadLock(Queue<MyProcess> readyQueue) {
		cpid = 0;
		if (readyQueue.size() < 2) {
			// Add a bunch of process
			for (int i = 0; i < 2; i++) {
				MyProcess[] procs = createMRPair();
				((Competitor) procs[0]).setReqSequence(new int[] { 1, 2 });
				((Competitor) procs[0]).setRelSequence(new int[] { 2, 1 });
				((Competitor) procs[1]).setReqSequence(new int[] { 1, 2 });
				((Competitor) procs[1]).setRelSequence(new int[] { 2, 1 });
				readyQueue.add(procs[0]);
				readyQueue.add(procs[1]);
			}

		}
	}

	/**
	 * @param readyQueue
	 */
	public void buildRandomMRPairDeadLock(Queue<MyProcess> readyQueue) {
		cpid = 0;
		if (readyQueue.size() < 2) {
			// Add a bunch of process
			for (int i = 0; i < 2; i++) {
				MyProcess[] procs = createMRPair();
				((Competitor) procs[0]).setReqSequence(new int[] { 1, 2 });
				((Competitor) procs[0]).setRelSequence(new int[] { 2, 1 });
				((Competitor) procs[1]).setReqSequence(new int[] { 2, 1 });
				((Competitor) procs[1]).setRelSequence(new int[] { 1, 2 });
				readyQueue.add(procs[0]);
				readyQueue.add(procs[1]);
			}

		}
	}

	/**
	 * @param readyQueue
	 * @param p
	 */
	public void rebuildRandom(Queue<MyProcess> readyQueue, MyProcess p) {
		// if (rand.nextInt() % 2 == 0) {
		// p.setStatus(PCB.STAT_CLOSE);
		// p.close();
		// } else {
		p.restart(MAXJOB);
		p.setStatus(PCB.STAT_READY);
		readyQueue.add(p);
		// }
	}

}
