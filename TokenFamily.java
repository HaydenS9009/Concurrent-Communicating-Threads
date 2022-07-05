import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TokenFamily implements Runnable{
    TpBuffer buffer;
    //Set maximum number that a token can be
    int tokens = 100+1; //Change this number (100) to how high number the tokens can go
    //Current families token
    int token = (int) (Math.random()*tokens);
    //Current families id
    int id;
    //List for messages to be received into
    ArrayList<List> messages = new ArrayList();
    //variables for the following family
    int followerId;
    int followerToken = tokens+1;
    //Has this family finished shopping
    boolean finishedShopping = false;
    //Let this family go shopping
    boolean go = false;
    //Variable to record lowest token from all the tokens
    int lowestToken = token;
    //Is the lowest token number used more than once
    boolean lowestequal = false;

    public TokenFamily(TpBuffer b) { buffer = b;}

    public synchronized void run() {
        //Insert family to list of families in the buffer - index = name of family thread - 1
        buffer.Nodes.set(Integer.parseInt(Thread.currentThread().getName())-1, this);
        //Get id name from the name of the family thread
        id = Integer.parseInt(Thread.currentThread().getName());

        //Print which token the family received
        System.out.println("Family " + Thread.currentThread().getName() + " has token " + token);

        //Send initial messages to every family through the buffer - (other family id, current family id, current family token number)
        for (int i = 1; i < 26; i++) {
            if (id != i) {
               ArrayList<Integer> message = new ArrayList<>();
               message.add(i);
               message.add(id);
               message.add(token);
               buffer.queue.add(message);
            }
        }

        //Wait for all messages to arrive to this family
        while (messages.size() != 24) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Once this Family has received all other families tokens,
        //Work out whether this family starts, and which family comes next
        for (List x : messages) {
            Integer otherToken = (Integer) x.get(2);
            Integer otherId = (Integer) x.get(1);

            //If otherToken is lower than the current lowest token, replace lowest token with otherToken
            if (otherToken < lowestToken) {
                lowestToken = otherToken;
            }

            //find the lowest token above current families token
            if (otherToken > token) {
                if (otherToken < followerToken) {
                    followerToken = otherToken;
                    followerId = otherId;
                }
                //If the otherToken == followerToken, only replace the id if it is lower than the current followerId
                if (otherToken == followerToken) {
                    if (otherId < followerId) {
                        followerId = otherId;
                    }
                }
            //If another families token is the same as this families token
            } else if (otherToken == token) {
                lowestequal = true;
                //If token of another family is the same as this family
                if (followerToken == otherToken) {
                    //Update the id only if it is lower than the follower id, but higher than the current families id
                    if (otherId < followerId && otherId > id) {
                        followerId = otherId;
                    }
                } else if (otherId > id) {
                    followerId = otherId;
                    followerToken = otherToken;
                }
            }
        }

        //If this family has the lowest token, go first
        if (token == lowestToken) {
            //If the lowest token is held by multiple families, the lowest id goes first
            if (token == followerToken) {
                if (id < followerId) {
                    try {
                        goShopping(followerId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //If this families token is the lowest, and no other families token equals this token, go shopping
                if (!lowestequal) {
                    try {
                        goShopping(followerId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //If previous family has gone, and sent a message that this family can go, then go shopping
        while (!finishedShopping) {
            if (go) {
                try {
                    goShopping(followerId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Receive messages form the buffer - just appends the message to messages list
    //Message includes other families id and token
    public void receive(List message) {
        this.messages.add(message);
    }

    //Prints statements for family to go shopping, sends message for the next family to go shopping once they are finished
    public synchronized void goShopping(Integer followerId) throws InterruptedException {
        System.out.println(" ");
        System.out.println("Family " + Thread.currentThread().getName() + " is now going shopping with token " + token);
        //Set that this family has finished shopping
        this.finishedShopping = true;
        System.out.println("Family " + Thread.currentThread().getName() + " has finished shopping");

        //Increment counter - once it hits 25 (when all families have gone shopping), the program stops
        buffer.counter += 1;

        //Final family does not send a message to anyone
        if (followerToken != tokens+1) {
            System.out.println("Send message to following family: " + followerId);
            buffer.sendQueue.add(followerId);
        }
    }

    //Receive message from previous family - message is just permission to now go shopping, so just changes go boolean to true.
    public void response() {
        this.go = true;
    }
}