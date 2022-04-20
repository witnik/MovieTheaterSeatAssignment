import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * This is the seat assigner class that will deal with seat assigning
 */
public class SeatAssigner {
    final private int rows;
    final private int cols;
    private int capacity;
    final private int rowBuffer;
    final private int colBuffer;
    private boolean[][] seats;
    private int[] seatsRemain;
    private Map<String, List<String>> tickets;
    private int assignTogether;
    private int assignSeparate;
    private int noAssign;
    private int invalidReq;
    private int totalReq;

    /**
     * SeatAssigner constructor
     * @param rows rows this theater have
     * @param cols cols this theeater have
     * @param rowBuffer rows to skip
     * @param colBuffer cols to skip
     */
    public SeatAssigner(int rows, int cols, int rowBuffer, int colBuffer) {
        this.rows = rows;
        this.cols = cols;
        this.capacity = rows * cols;
        this.rowBuffer = rowBuffer;
        this.colBuffer = colBuffer;
        this.seats = new boolean[rows][cols];
        this.seatsRemain = new int[rows];
        this.tickets = new TreeMap<>();
        this.assignTogether = 0;
        this.assignSeparate = 0;
        this.noAssign = 0;
        this.invalidReq = 0;
        this.totalReq = 0;

        for(int i = 0; i < rows; i++) {
            Arrays.fill(this.seats[i], false);
        }
        Arrays.fill(this.seatsRemain, cols);
    }

    /**
     * function where input line will be handled and checked for validity
     * @param request  input line from the input file
     * @return  whether the request is being successfully processed or not
     */
    public boolean reserve(String request) {
        this.totalReq++;
        String[] reservation = request.split(" ");
        String numReservation = reservation[0];
        int numSeats = Integer.parseInt(reservation[1]);

        if(numSeats <= 0) {
            System.out.println(numReservation + "'s Reservation Seats is invalid as: " + numSeats);
            this.invalidReq++;
            return false;
        }

        if(numSeats > capacity) {
            System.out.println(numReservation + "'s Reservation Seats exceeds capacity");
            this.noAssign++;
            return false;
        }

        assignSeat(numReservation, numSeats);

        return true;
    }

    /**
     * assign seats based on the request.
     * @param numReservation reservation number
     * @param numSeats  number of seats requested
     */
    private void assignSeat(String numReservation, int numSeats) {
        List<String> seatsTogether = new ArrayList<>();
        List<List<String>> seatsSeparate = new ArrayList<>();
        boolean separate = false;

        if(numSeats > this.cols) {
            separate = true;
        }

        while(numSeats > 0) {
            int seatCount = Math.min(numSeats, this.cols);
            numSeats -= this.cols;
            for(int i = this.rows - 1; i >= 0; i--) {
                // check if people can sit together in this row
                int startIdx = sitTogether(i, seatCount);
                if(startIdx != -1) {
//                    System.out.println("Sit together for " + numReservation + " at " + i);
                    seatsTogether.add(assignTogether(numReservation, seatCount, i, startIdx));
                    seatCount = 0;
                    break;
                }
            }

            // check if seats have already been assigned by sitting people together
            if(seatCount != 0) {
                seatsSeparate.add(assignSeparate(numReservation, seatCount));
                separate = true;
            }
        }

        // assign buffers after assigning all seats
        seatBuffer(seatsTogether);
        for(List<String> seatSep: seatsSeparate) {
            seatBuffer(seatSep);
        }

        if(separate) {
            this.assignSeparate++;
        }
        else {
            this.assignTogether++;
        }
    }

    /**
     * Helper function to check whether the number of people will be able to sit together in this row
     * @param row number of the row to check
     * @param numSeats  number of seats requested
     * @return  the index of the starting position people will start to sit. -1 if unable to fit
     */
    private int sitTogether(int row, int numSeats) {
        int idx = 0;
        int count = 0;
        while(idx < this.cols) {
            if(this.seats[row][idx]) {
                count = 0;
                idx++;
                continue;
            }
            count++;
            if(count == numSeats) {
                return idx - numSeats + 1;
            }
            idx++;
        }

        return -1;
    }

    /**
     * helper function to sit people together in the given row
     * @param numReservation    reservation number
     * @param seatCount     number of seats requested
     * @param row   the row to sit people
     * @param startIdx  the index to start sitting
     * @return    a string representing information about who has been seated
     */
    private String assignTogether(String numReservation, int seatCount, int row, int startIdx) {
        Arrays.fill(this.seats[row], startIdx, startIdx + seatCount, true);
        this.capacity -= seatCount;
        this.seatsRemain[row] -= seatCount;
        for(int i = startIdx; i < seatCount + startIdx; i++) {
            String ticket = (char)(row + 'a') + Integer.toString(i + 1);
            this.tickets.computeIfAbsent(numReservation, k -> new ArrayList<>()).add(ticket);
        }

        // representation of seats being assigned to return for the buffer assignment later
        return startIdx + " " + (seatCount + startIdx) + " " + row;
    }

    /**
     * helper function to sit people separately across the theater
     * @param numReservation    reservation number
     * @param seatCount     number of seats requested
     * @return  a list of string represetations of information of the seat arrangements being made
     */
    private List<String> assignSeparate(String numReservation, int seatCount) {
        List<String> bufferReps = new ArrayList<>();
        for(int i = this.rows - 1; i >= 0; i--) {
            // if the row are full, skip to the next one
            if(this.seatsRemain[i] == 0) {
                continue;
            }

            int j = 0;
            while(j < this.cols) {
                if(this.seats[i][j]) {
                    j++;
                    continue;
                }
                // sliding window to find range of available seats
                int startIdx = j;
                while(j < this.cols && !this.seats[i][j] && seatCount > 0) {
                    String ticket = (char)(i + 'a') + Integer.toString(j + 1);
                    this.tickets.computeIfAbsent(numReservation, k -> new ArrayList<>()).add(ticket);
                    j++;
                    seatCount--;
                }
                int endIdx = j;
                Arrays.fill(this.seats[i], startIdx, endIdx, true);
                this.capacity -= (endIdx - startIdx);
                this.seatsRemain[i] -= (endIdx - startIdx);

                String bufferRep = startIdx + " " + endIdx + " " + i;
                bufferReps.add(bufferRep);
                if(seatCount == 0) {
                    return bufferReps;
                }
            }
        }

        System.out.println("unreachable code");
        return bufferReps;
    }

    /**
     * Helper function to assign buffer seats that will be used to separate groups and protect people's safety
     * @param bufferReps     list of String representation of the range of seats assigned
     */
    private void seatBuffer(List<String> bufferReps) {
        for(String rep: bufferReps) {
            String[] reps = rep.split(" ");
            int startIdx = Integer.parseInt(reps[0]);
            int endIdx = Integer.parseInt(reps[1]);
            int row = Integer.parseInt(reps[2]);
            int numSeats = endIdx - startIdx;

            // assign row buffer
            for(int r = 1; r <= this.rowBuffer; r++) {
                for (int i = startIdx; i < endIdx; i++) {
                    if (row - r >= 0) {
                        if (!this.seats[row - r][i]) {
                            this.seats[row - r][i] = true;
                            this.seatsRemain[row - r] -= 1;
                            this.capacity -= 1;
                        }
                    }

                    if (row + r < this.rows) {
                        if (!this.seats[row + r][i]) {
                            this.seats[row + r][i] = true;
                            this.seatsRemain[row + r] -= 1;
                            this.capacity -= 1;
                        }
                    }
                }
            }

            // assign col buffer
            for(int i = 1; i <= this.colBuffer; i++) {
                if(startIdx - i >= 0) {
                    if(!this.seats[row][startIdx - i]) {
                        this.seats[row][startIdx - i] = true;
                        this.seatsRemain[row] -= 1;
                        this.capacity -= 1;
                    }
                }
                if(endIdx + i - 1 < this.cols) {
                    if(!this.seats[row][endIdx + i - 1]) {
                        this.seats[row][endIdx + i - 1] = true;
                        this.seatsRemain[row] -= 1;
                        this.capacity -= 1;
                    }
                }
            }
        }
    }

    /**
     * getter function for tickets
     * @return  tickets variable
     */
    public Map<String, List<String>> getTickets() {
        return this.tickets;
    }

    /**
     * function to write the tickets produced to a file named "tickets.txt"
     * @param path  file path to write to
     * @return  the absolute path to the file
     */
    public String writeToFile(String path) {
        BufferedWriter writer;
        try{
            writer = new BufferedWriter(new FileWriter(path));
            for(Map.Entry<String, List<String>> e: this.tickets.entrySet()) {
                String ticket = e.getKey();
                for(String v: e.getValue()) {
                    ticket += " " + v;
                }
                writer.write(ticket + "\n");
            }
            writer.close();
        }
        catch(Exception e) {
            System.out.println("A problem occured while writing to file: " + e.toString());
            return "";
        }

        Path filepath = Paths.get(path);
        return "" + filepath.toAbsolutePath();
    }

    /**
     * Print current seating map
     */
    public void printSeatingMap() {
        for(boolean[] arr: this.seats) {
            for(boolean s: arr) {
                System.out.print(s ? "X" : "_");
            }
            System.out.println();
        }
    }

    /**
     * Print status of the seats assignment
     */
    public void printStatus() {
        System.out.println("Total number of reservation: " + this.totalReq);
        System.out.println("Number of reservation satisfiedly fulfilled: " + this.assignTogether);
        System.out.println("Number of reservation partially fulfilled: " + this.assignSeparate);
        System.out.println("Number of reservation could not fulfilled: " + this.noAssign);
        System.out.println("Number of invalid reservation: " + this.invalidReq);
    }
}
