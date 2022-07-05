public class TokenPassing {
    public static void main(String args[]) {
        TpBuffer buffer = new TpBuffer();

        for (int i = 1; i<26; i++) {
            //Create thread with the number being the name of the family
            TokenFamily family = new TokenFamily(buffer);
            Thread t = new Thread(family, String.valueOf(i));
            t.start();
        }

        //Create thread to deliver messages between the families
        Thread tp = new Thread(buffer, "Messenger");
        tp.start();
    }
}
