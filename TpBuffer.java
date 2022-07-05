import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TpBuffer implements Runnable{
    //List of all families
    List<TokenFamily> Nodes = Arrays.asList(new TokenFamily[25]);
    //Queue for sending tokens to other families
    BlockingQueue queue = new LinkedBlockingQueue();
    //Queue for telling next family that they can go
    BlockingQueue<Integer> sendQueue = new LinkedBlockingQueue();
    //Count how many families have gone shopping
    int counter = 0;

    public synchronized void run() {
        while (counter < 25) {
            //Wait for all families to enter the address list
            while (Nodes.size() < 24) {}

            if (!queue.isEmpty()) {
                //Get first message from queue
                List message = (List) queue.peek();
                //Delete first message
                queue.remove();

                //Get recipient family via the list of families
                Integer recipient = (Integer) message.get(0);
                TokenFamily Recipient = Nodes.get(recipient-1);

                //Send message to family
                Recipient.receive(message);
            }

            if (!sendQueue.isEmpty()) {
                //Get first message from sendQueue
                Integer recipient = sendQueue.peek();
                //Delete first message
                sendQueue.remove();
                //Recipient = 0 after the final family has gone shopping - means program has finished
                if (recipient != 0) {
                    //Get recipient family and send message to them
                    TokenFamily Recipient = Nodes.get(recipient - 1);
                    Recipient.response();
                }
            }
        }
    }
}
