public class PermissionBased {
    public static void main(String args[]) {
        PmBuffer buffer = new PmBuffer();

        for (int i = 1; i<26; i++) {
            //Create thread with the number being the name of the family
            Family family = new Family(buffer);
            Thread f = new Thread(family, String.valueOf(i));
            f.start();
        }

        //Create thread to deliver messages between the families
        Thread pm = new Thread(buffer, "Messenger");
        pm.start();
    }
}
