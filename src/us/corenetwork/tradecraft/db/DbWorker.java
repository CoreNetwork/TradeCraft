package us.corenetwork.tradecraft.db;

import java.util.concurrent.LinkedBlockingQueue;

public class DbWorker implements Runnable {

	public static LinkedBlockingQueue<IDbTask> queue = new LinkedBlockingQueue<>();
	private static volatile boolean running = true;

    public static void stopFurtherRequests()
    {
        running = false;
        queue.add(new IDbTask()
        {
            @Override
            public void perform()
            {
            }
        }); //Add dummy task to stop worker thread from blocking
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
