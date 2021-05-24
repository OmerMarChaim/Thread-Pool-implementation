import java.io.File;

public class Searcher implements Runnable {
    private int id;
    private java.lang.String extension;
    private SynchronizedQueue<java.io.File> directoryQueue;
    private SynchronizedQueue<java.io.File> resultsQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    public Searcher(int id, java.lang.String extension,
                    SynchronizedQueue<java.io.File> directoryQueue,
                    SynchronizedQueue<java.io.File> resultsQueue,
                    SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    @Override
    public void run() {
        File directory = null;
        resultsQueue.registerProducer();

  //      System.out.println("we are at search run"+id);

        while (this.directoryQueue.getSize() > 0) {
       //     System.out.println("we are at search while "+ id);

            directory = directoryQueue.dequeue();
            if (directory != null) {

                searchInSpecificFolder(directory);
            }
        }
        resultsQueue.unregisterProducer();

    }

    private void searchInSpecificFolder(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    int lastDot = fileName.lastIndexOf('.');
                    String fileExtension = fileName.substring(lastDot + 1);

                    if (fileExtension.equals(this.extension)) {

                     //   System.out.println(fileName  +" we are at searchInSpecificFolder @@@@@@@@");

                        if (this.isMilestones) {
                            milestonesQueue.registerProducer();
                            String mileStoneMessage = String.format(" Searcher on thread id " + this.id + " file named " + fileName + "was found");
                            milestonesQueue.enqueue(mileStoneMessage);

                         //   System.out.println(mileStoneMessage);
                            milestonesQueue.unregisterProducer();
                        }
                        this.resultsQueue.enqueue(file);

                    }
                }
            }
        }
    }
}
