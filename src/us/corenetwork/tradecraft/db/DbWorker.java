package us.corenetwork.tradecraft.db;

import java.util.concurrent.LinkedBlockingQueue;

public class DbWorker implements Runnable {

	public static LinkedBlockingQueue<IDbTask> queue = new LinkedBlockingQueue<>();
	private static boolean running = true;

    public static void stopFurtherRequests()
    {
        running = false;
    }

	@Override
	public void run() {
		while(running || queue.size() > 0)
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
