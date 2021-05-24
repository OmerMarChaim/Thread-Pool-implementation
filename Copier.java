import java.io.*;

public class Copier implements Runnable {
    public static final int COPY_BUFFER_SIZE = 4096;

    private int id;
    private SynchronizedQueue<java.io.File> resultsQueue;
    private File destination;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    public Copier(int id, java.io.File destination,
                  SynchronizedQueue<java.io.File> resultsQueue,
                  SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.resultsQueue = resultsQueue;
        this.destination = destination;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    @Override
    public void run() {
        File sourceFile = null;
        sourceFile = this.resultsQueue.dequeue();
        File destinationFile = null;
        while (sourceFile != null) {
            destinationFile = new File(destination.getAbsolutePath() + File.separator + sourceFile.getName());
            copyFiles(sourceFile, destinationFile);
          //  System.out.println("we are at copirt run "+id);
            if (isMilestones) {
                milestonesQueue.registerProducer();
                String mileStoneMessage = String.format(" Copier on thread id " + this.id +
                        " file named " + sourceFile.getName() + " was copied");
                milestonesQueue.enqueue(mileStoneMessage);
                //print
              //  System.out.println(mileStoneMessage);
                milestonesQueue.unregisterProducer();
            }
            sourceFile = this.resultsQueue.dequeue();

        }

    }

    private void copyFiles(File sourceFile, File destinationFile) {
        try {
            byte[] buffer = new byte[COPY_BUFFER_SIZE];
            InputStream is = new FileInputStream(sourceFile);
            OutputStream os = new FileOutputStream(destinationFile);
            int length = 0;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
