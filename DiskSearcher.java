import java.io.File;
import java.sql.Time;
import java.time.Instant;
import java.time.Duration;


public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static final int MILESTONES_QUEUE_CAPACITY = 4096;


    public static void main(String[] args) {
        long startTime = System.nanoTime();
        long start = System.currentTimeMillis();
      //  StopWatch stopWatch = new StopWatch();




        System.out.println("help , please enter command as needed");
        boolean milestoneQueueFlag = Boolean.parseBoolean(args[0]);
        String fileExtension = args[1];
        File rootDirectory = new File(args[2]);
        File destinationDirectory = new File(args[3]);
        int numOfSearchers = Integer.parseInt(args[4]);
        int numOfCopiers = Integer.parseInt(args[5]);
        Thread[] searchers = new Thread[numOfSearchers];
        Thread[] copiers = new Thread[numOfCopiers];

        // initialize queues
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);
        SynchronizedQueue<String> milestonesQueue = null;
        if (milestoneQueueFlag)
            milestonesQueue = new SynchronizedQueue<>(MILESTONES_QUEUE_CAPACITY);

        ///1. Start a single scouter thread
        // (and make sure it writes to the milestonesQueue)
        Scouter scouter = new Scouter(0, directoryQueue, rootDirectory, milestonesQueue, milestoneQueueFlag);
        Thread scouterThread = new Thread(scouter);
        scouterThread.start();
//
        ///4. Wait for scouter to finish.
        try {
            scouterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /// 2. Start a group of searcher threads (number of searchers as specified in arguments,
        // and make sure it writes to the milestonesQueue)
        for (int i = 0; i < numOfSearchers; i++) {

            Searcher searcher = new Searcher(i, fileExtension, directoryQueue, resultsQueue, milestonesQueue, milestoneQueueFlag);
            Thread searcherThread = new Thread(searcher);
            searchers[i] = searcherThread;
            searcherThread.start();
        }
        ///3. Start a group of copier threads (number of copiers as specified in arguments,
        // and make sure it writes to the milestonesQueue)
        for (int i = 0; i < numOfCopiers; i++) {

            Copier copier = new Copier(i, destinationDirectory, resultsQueue, milestonesQueue, milestoneQueueFlag);
            Thread copierThread = new Thread(copier);
            copiers[i] = copierThread;
            copierThread.start();
        }
//        //4. Wait for scouter to finish.
//        try {
//            scouterThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        //5. Wait for searcher and copier threads to finish.

        waitForArrOfThreads(searchers);
        waitForArrOfThreads(copiers);
      printQueue(milestonesQueue);
        long end = System.currentTimeMillis();
        System.out.println("this is start"+start);
        System.out.println("this is end"+end);

        System.out.println(end-start);

    }

    private static void printQueue(SynchronizedQueue<java.lang.String> iQueue) {

        while (iQueue.getSize()>0 )
        {
            System.out.println(iQueue.dequeue());
        }
    }

    private static void waitForArrOfThreads(Thread[] arrOfThreads) {
        for (Thread thread : arrOfThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
