package actor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ActorThread<M> extends Thread {

    // TODO: one suitable attribute here
    private final BlockingQueue<M> queue = new LinkedBlockingQueue<M>();

    /** Called by another thread, to send a message to this thread. */
    public void send(M message) {
        queue.add(message);
    }
    
    /** Returns the first message in the queue, or blocks if none available. */
    protected M receive() throws InterruptedException {
        return  queue.take();
    }
    
    /** Returns the first message in the queue, or blocks up to 'timeout'
        milliseconds if none available. Returns null if no message is obtained
        within 'timeout' milliseconds. */
    protected M receiveWithTimeout(long timeout) throws InterruptedException {
        return queue.poll(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}