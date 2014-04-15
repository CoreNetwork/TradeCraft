package us.corenetwork.tradecraft.db;

import java.util.concurrent.LinkedBlockingQueue;

public class DbWorker implements Runnable {

	public static LinkedBlockingQueue<IDbTask> queue = new LinkedBlockingQueue<>();
	
	@Override
	public void run() {
		while(true)
		{
			IDbTask task = null;
			
			try {
				task = queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(task != null)
			{
				task.perform();
			}
		}
	}

	
}
