import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PmBuffer implements Runnable {
    //List of all families
    List<Family> Nodes = Arrays.asList(new Family[25]);
    //Queue for asking permission
    BlockingQueue queue = new LinkedBlockingQueue();
    //Queue for sending permission
    BlockingQueue sendQueue = new LinkedBlockingQueue();
    //Boolean to stop everything once everyone has finished (stopcounter == 25)
    boolean stop = false;
    int stopcounter = 0;

    //Send message to its recipient
    public synchronized void run() {
        while (!stop) {
            //Initial message - find recipient and send them the id of the sender
            if (!queue.isEmpty()) {
                //Get first message from the queue
                List message = (List) queue.peek();
                //Delete first message from the queue
                queue.remove();

                Integer recipient = (Integer) message.get(0);
                Integer sender = (Integer) message.get(1);

                //Get the family the message is being sent to via the list of families
                Family Recipient = Nodes.get(recipient-1);

                try {
                    Recipient.receive(sender);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Reply message to initial message - only returns true or false to initial sender
            //Similar to what happens with the above queue
            if (!sendQueue.isEmpty()) {
                List message = (List) sendQueue.peek();
                sendQueue.remove();

                if (message.get(0) != null) {
                    //Get family message is being sent to
                    Integer recipient = (Integer) message.get(0);
                    boolean response = (boolean) message.get(1);
                    Family Recipient = Nodes.get(recipient - 1);

                    //Send message
                    Recipient.response(response);
                }
            }

            //Sleep statement to pace messages being sent to the families
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Once every family has gone shopping, stop the program
            if (stopcounter == 25) {
                stop = true;
            }
        }
    }
}
