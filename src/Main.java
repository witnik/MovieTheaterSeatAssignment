import java.io.*;
import java.util.*;

public class Main {
    /**
     * Assuming a fixed theater layout, having 10 rows and 20 seats per row.
     **/
    public static void main(String[] args) {
       	String path = args[0];
        // String path = "input.txt";
        int rows = 10;
        int cols = 20;
        int rowBuffer = 1;
        int colBuffer = 3;

        SeatAssigner assigner = new SeatAssigner(rows, cols, rowBuffer, colBuffer);
        List<String> failedRequests = new ArrayList<>();
        try {
            File file = new File(path);
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String req = sc.nextLine();
                if(!assigner.reserve(req)) {
                    failedRequests.add(req);
                }
            }
            sc.close();
            String filepath = assigner.writeToFile("../tickets.txt");
            assigner.printSeatingMap();

            System.out.println();
            System.out.println("The file is stored at: " + filepath);
            System.out.println();

            if(failedRequests.size() > 0) {
                System.out.println("List of Request that can not be complete / invalid:");
                for (String req : failedRequests) {
                    System.out.print(req.split(" ")[0] + " ");
                }
                System.out.println();
                System.out.println();
            }

            assigner.printStatus();

        }
        catch(IOException e) {
            System.out.println("Encountered Exception: " + e.toString());
        }
    }
}