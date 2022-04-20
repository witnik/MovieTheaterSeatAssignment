# Movie Theater Seating Challenge


## Approach
* Using a Greedy Approach
* Starting from the bottom left corner and filling in seats from 0 to 20 and rows from 9 to 0.
* Trying to keep reservation parties together. Only to split them up if there is no space where they can all sit together.
* Consider each reservations in batch of 20. Assign each batch of maximum 20 people.
* Setup buffer only after all batches from one reservation has been assigned


## Assumptions

* The buffer row of one is the same seat in rows above and below the current reseravtions
* Parties within one reservation would want to sit together.
* People would prefer seats in the back.
* Parties within same group of people would not require buffer.

## Execution
Download source code from Git repository and unzip the file.

Open your terminal window / command prompt. Go to the folder where the unzipped file is saved.  Navigate to the folder "src".
Run the command:
```
  javac Main.java
 ```

Run the following command to start the application
```
  java Main <path to input file>
 ```


## Optimizations
* Incorportating a better view with sitting together as the first priority, possibly BFS to go through all options. Very time consuming though.
* Run time, for checking for consecutive seats for the party was a slow process in my algorithm this could be speed up by using a bit more space and putting pointers to the first open seat and how many consecutive seats are available
* If there is unsold seats could reconfiguring so that better viewing seat ( the middle) and larger safety buffers could be applied
* Unit testing for each functions
* Assign people with middle seats if possible.
