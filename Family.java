import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Family implements Runnable{
    PmBuffer buffer;
    //Is the family currently shopping
    boolean currentlyShopping = false;
    //List of ids from other families that need a reply for if they can go shopping
    ArrayList senders = new ArrayList();
    //Variables for recording how many true or false the family has received before they go shopping
    //They need 24 true to go, but any false and they have to restart
    int replyYes = 0;
    int replyNo = 0;
    //Boolean for if the family has finished their goal - if so, skip the main while loop
    boolean finishedShopping = false;

    public Family(PmBuffer b) {
        buffer = b;
    }

    public synchronized void run() {
        //Reset replyYes and replyNo whenever this function is recalled
        replyYes = 0;
        replyNo = 0;
        //Add family to list of families - index is the number of the family -1
        buffer.Nodes.set(Integer.parseInt(Thread.currentThread().getName())-1, this);

        //Set id as current families number
        Integer id = Integer.parseInt(Thread.currentThread().getName());

        //Ask all other families for permission
        for (int i = 1; i < 26; i++) {
            if (id != i) {
                ArrayList<Integer> message = new ArrayList<>();
                message.add(i);
                message.add(id);
                buffer.queue.add(message);
            }
        }
        //Main loop
        while(!finishedShopping) {
            //Reply to first message
            send();

            if (replyNo >= 1) {
                //If any replyNo, reset
                run();
            } else if (replyYes >= 24) {
                //If everyone replied true, go shopping - print statements
                System.out.println(" ");
                System.out.println("Family " + Thread.currentThread().getName() + " has entered the critical section");
                System.out.println("Yes:" + replyYes + " No:" + replyNo + " Family " + Thread.currentThread().getName());

                //Send message to everyone that they cannot go shopping now
                for (int i=1; i<26; i++) {
                    ArrayList message = new ArrayList();
                    message.add(i);
                    message.add(false);
                    buffer.sendQueue.add(message);
                }

                //If another family goes shopping while this family is sending the no messages, reset and do not go shopping
                if (replyNo >= 1) {
                    run();
                }

                //Set family to currently be shopping
                currentlyShopping = true;
                System.out.println("Family " + Thread.currentThread().getName() + " is now going shopping");

                //Clear send buffer, so false is sent to everyone else, and they can reset without wasting time
                buffer.sendQueue.clear();

                //Send message to everyone that they cannot go shopping now
                for (int i=1; i<26; i++) {
                    ArrayList message = new ArrayList();
                    message.add(i);
                    message.add(false);
                    buffer.sendQueue.add(message);
                }
                //Set their shopping to now be finished (exit main while loop)
                finishedShopping = true;
                //Family leaves critical section
                System.out.println("Family " + Thread.currentThread().getName() + " has left the critical section");
            }

            //Without sleep statement program works inconsistently
            //With sleep, program speed increases and works consistently
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Print that the family has finished shopping, and set currentlyShopping variable to false
            if (finishedShopping) {
                if (!buffer.stop) {
                    System.out.println("Family " + Thread.currentThread().getName() + " has finished shopping");
                    currentlyShopping = false;
                }
            }
        }
        //increment counter in the buffer (this family has finished shopping)
        buffer.stopcounter += 1;
        //Reply true to everyone else until everyone is finished
        while(!buffer.stop) {
            send();
        }
    }

    //Receive message from other family and add their id to the senders list
    public void receive(Integer sender) throws InterruptedException {
        this.senders.add(sender);
    }

    //Reply to first person in the senders list
    public void send() {
        ArrayList message = new ArrayList<>();
        if (!senders.isEmpty()) {
            //Sometimes if program goes too fast, sender can be added as null instead of their id
            if (senders.get(0) == null) {
                senders.remove(0);
            }
            //Add id for where to send this message
            message.add(senders.get(0));
            //Add if the other family is allowed to go shopping now
            if (currentlyShopping) {
                message.add(false);
            } else {
                message.add(true);
            }
            //remove id from list, and send message through the buffer
            senders.remove(0);
            buffer.sendQueue.add(message);
        }
    }

    //Increment replyYes or replyNo based on the response from other families
    public void response(boolean response) {
        if (response) {
            replyYes += 1;
        } else {
            replyNo += 1;
        }
    }
}
