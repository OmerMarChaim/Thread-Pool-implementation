import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class Scouter implements Runnable {
    private int id;
    private SynchronizedQueue<java.io.File> directoryQueue;
    private File root;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    public Scouter(int id, SynchronizedQueue<java.io.File> directoryQueue,
                   java.io.File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    @Override
    public void run() {

        this.directoryQueue.registerProducer();
      //  System.out.println("start scout in scouter");

        addFolderToDirectoryQueue(this.root);
        this.directoryQueue.unregisterProducer();

       // notifyAll();


    }

    private void addFolderToDirectoryQueue(File root) {
        Queue<File> tempQ = new LinkedList<File>();
        tempQ.add(root);
        while (!tempQ.isEmpty()) {
            File item = tempQ.remove();
            this.directoryQueue.enqueue(item);
            if (isMilestones) {
                milestonesQueue.registerProducer();
                String mileStoneMessage = String.format(" Scouter on thread id " + this.id +
                        " directory named " + item + " was scouted");
                milestonesQueue.enqueue(mileStoneMessage);
              //  System.out.println(mileStoneMessage);

                milestonesQueue.unregisterProducer();
            }

            File[] subFiles = item.listFiles();
            for (File subFile : subFiles) {
                if (subFile.isDirectory())
                    tempQ.add(subFile);
            }
        }

    }
}

