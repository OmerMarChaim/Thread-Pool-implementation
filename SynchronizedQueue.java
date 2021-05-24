/**
 *
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 *
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

    private T[] buffer;
    private int producers;
    private Object queueLock;
    private int inCount;
    private int outCount;
    private int queueCapacity;

    // TODO: Add more private members here as necessary

    /**
     * Constructor. Allocates a buffer (an array) with the given capacity and
     * resets pointers and counters.
     *
     * @param capacity Buffer capacity
     */
    @SuppressWarnings("unchecked")
    public SynchronizedQueue(int capacity) {
        this.buffer = (T[]) (new Object[capacity]);
        this.producers = 0;
        this.queueLock = new Object();
        this.inCount = 0;
        this.outCount = 0;
        this.queueCapacity = capacity;

    }

    /**
     * Dequeues the first item from the queue and returns it.
     * If the queue is empty but producers are still registered to this queue,
     * this method blocks until some item is available.
     * If the queue is empty and no more items are planned to be added to this
     * queue (because no producers are registered), this method returns null.
     *
     * @return The first item, or null if there are no more items
     * @see #registerProducer()
     * @see #unregisterProducer()
     */
    public T dequeue() {
        synchronized (this.queueLock) {
            while (producers != 0 && this.inCount <= this.outCount) {
                try {
                    this.queueLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T item = null;
            if (this.inCount > this.outCount) {
                item = this.buffer[this.outCount % this.queueCapacity];
                this.outCount++;
            }
            this.queueLock.notifyAll();
            return item;
        }
    }


    /**
     * Enqueues an item to the end of this queue. If the queue is full, this
     * method blocks until some space becomes available.
     *
     * @param item Item to enqueue
     */
    public void enqueue(T item) {

        synchronized (this.queueLock) {

            while (this.inCount - this.outCount >= this.queueCapacity) {
                try {
                    this.queueLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.buffer[this.inCount % this.queueCapacity] = item;
            this.inCount++;

            this.queueLock.notifyAll();
        }
    }


    /**
     * Returns the capacity of this queue
     *
     * @return queue capacity
     */
    public int getCapacity() {
        return this.queueCapacity;
    }

    /**
     * Returns the current size of the queue (number of elements in it)
     *
     * @return queue size
     */
    public int getSize() {
        return this.inCount - this.outCount;
    }

    /**
     * Registers a producer to this queue. This method actually increases the
     * internal producers counter of this queue by 1. This counter is used to
     * determine whether the queue is still active and to avoid blocking of
     * consumer threads that try to dequeue elements from an empty queue, when
     * no producer is expected to add any more items.
     * Every producer of this queue must call this method before starting to
     * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
     * finishes to enqueue all items.
     *
     * @see #dequeue()
     * @see #unregisterProducer()
     */
    public void registerProducer() {
        synchronized (this.queueLock) {
            this.producers++;

        }
    }

    /**
     * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
     *
     * @see #dequeue()
     * @see #registerProducer()
     */
    public void unregisterProducer() {
        synchronized (this.queueLock) {
            this.producers--;
            this.queueLock.notifyAll();
        }
    }


}


