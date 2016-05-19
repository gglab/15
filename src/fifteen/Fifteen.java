/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fifteen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author glabg
 */
public class Fifteen implements Comparable<Fifteen> {

    private static final String PROPERTIES_FILE_NAME = "fifteen.properties";
    private static final String ROWS_KEY = "rows";
    private static final String COLS_KEY = "cols";
    private static final String STATE_KEY = "initial";
    private static final String STATE_SEPARATOR = " ";
    private static final String RANDOM = "random";
    private static final String SEPARATOR = "\t";
    private static final String NEWLINE = "\n\r";
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(PROPERTIES_FILE_NAME));
        } catch (IOException ex) {
            System.out.println(new File(PROPERTIES_FILE_NAME).getAbsolutePath());
            Logger.getLogger(Fifteen.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private static final int ROWS = Integer.parseInt(properties.getProperty(ROWS_KEY, "2"));
    private static final int COLS = Integer.parseInt(properties.getProperty(COLS_KEY, "2"));
    private static final String STATE = properties.getProperty(STATE_KEY, "0 1 2 3");
    private static final boolean IS_RANDOM = RANDOM.equals(STATE);
    public static final Fifteen SOLUTION = new Fifteen(true);

    int[][] board = new int[ROWS][COLS];
    int c;
    int r;
    int cost = 0;
    int value = 0;

    public Fifteen(final boolean solved) {
        if (solved || IS_RANDOM) {
            initBoard();
        } else if (!IS_RANDOM) {
            final int[] initialState = parseState();
            int n = 0;
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    board[i][j] = initialState[n];
                    if (initialState[n] == 0) {
                        r = i;
                        c = j;
                    }
                    n++;
                }
            }
        }
    }

    public Fifteen(final Fifteen fifteen, final boolean isHeuristic) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                this.board[i][j] = fifteen.board[i][j];
            }
        }
        this.r = fifteen.r;
        this.c = fifteen.c;
        if (isHeuristic) {
            cost = fifteen.cost;
            value = fifteen.value;
        }
    }

    public boolean isSolvable(final int[] puzzle) {
        int parity = 0;
        int row = 0; 
        int blankRow = 0;

        for (int i = 0; i < puzzle.length; i++) {
            if (i % ROWS == 0) { 
                row++;
            }
            if (puzzle[i] == 0) { 
                blankRow = row; 
                continue;
            }
            for (int j = i + 1; j < puzzle.length; j++) {
                if (puzzle[i] > puzzle[j] && puzzle[j] != 0) {
                    parity++;
                }
            }
        }

        if (ROWS % 2 == 0) { 
            if (blankRow % 2 == 0) { 
                return parity % 2 == 0;
            } else { 
                return parity % 2 != 0;
            }
        } else { 
            return parity % 2 == 0;
        }
    }

    private int[] getPosition(final int i) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] == i) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    private int getValue() {
        int result = 0;
        int i = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (i == 16) {
                    continue;
                }
                int[] position = getPosition(i);
                result += Math.abs(row - position[0]) + Math.abs(col - position[0]);
            }
        }
        int[] position = getPosition(0);
        result += Math.abs(ROWS - 1 - position[0]) + Math.abs(COLS - 1 - position[0]);
        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Fifteen fifteen = new Fifteen(IS_RANDOM);
//        System.out.println(fifteen);
        if (IS_RANDOM) {
            fifteen.shuffle();
        }else if(!fifteen.isSolvable(fifteen.parseState())){
            throw new Exception("Puzzle is not solvable!");
        }
        System.out.println("Puzzle is solvable!");
        System.out.println("Puzzle to solve:");
        System.out.println(fifteen);
        fifteen.value = fifteen.getValue();
        Fifteen.aStar(fifteen);
        System.out.println("Puzzle to solve:");
        System.out.println(fifteen);
        Fifteen.bfs(fifteen);
        System.out.println("Puzzle to solve:");
        System.out.println(fifteen);
        Fifteen.dfs(fifteen);

    }

    private void initBoard() {
        int n = 1;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = n;
                n++;
            }
        }
        r = ROWS - 1;
        c = COLS - 1;
        board[r][c] = 0;
    }

    private int[] parseState() {
        return Stream.of(STATE.split(STATE_SEPARATOR)).mapToInt(Integer::parseInt).toArray();
    }

    boolean move(final int direction) {
        final int x = board[r][c];
        try {
            switch (direction) {
                //do góry    
                case 0:
                    board[r][c] = board[r - 1][c];
                    board[r - 1][c] = x;
                    r--;
                    return true;
                //prawo
                case 1:
                    board[r][c] = board[r][c + 1];
                    board[r][c + 1] = x;
                    c++;
                    return true;

                //dół
                case 2:
                    board[r][c] = board[r + 1][c];
                    board[r + 1][c] = x;
                    r++;
                    return true;
                //lewo
                case 3:
                    // if (c != 0) {
                    board[r][c] = board[r][c - 1];
                    board[r][c - 1] = x;
                    c--;
                    return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }

    public boolean isMoveLegal(final int direction) {
        switch (direction) {
            //do góry    
            case 0:
                if (r != 0) {
                    return true;
                }
                break;
            //prawo
            case 1:
                if (c != COLS - 1) {
                    return true;
                }
                break;
            //dół
            case 2:
                if (r != ROWS - 1) {
                    return true;
                }
                break;
            //lewo
            case 3:
                if (c != 0) {
                    return true;
                }
                break;
        }
        return false;
    }

    void shuffle() {
        Random r = new Random(0);
        int move;
        for (int i = 0; i < 100; ++i) {
            move = r.nextInt(4);
            if (isMoveLegal(move)) {
                move(r.nextInt(4));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                sb.append(SEPARATOR).append(board[i][j]).append(SEPARATOR);
            }
            sb.append(NEWLINE);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Fifteen temp = (Fifteen) obj;
        if (!Arrays.deepEquals(board, temp.board)) {
            return false;
        }
        if (c != temp.c) {
            return false;
        }
        if (r != temp.r) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int p = 101;
        int result = 1;
        result = p * result + Arrays.deepHashCode(board);
        result = p * result + r;
        result = p * result + c;
        return result;
    }

    @Override
    public int compareTo(final Fifteen fifteen) {
        final int thisValue = cost + value;
        final int otherValue = fifteen.cost + fifteen.value;
        if (thisValue == otherValue) {
            return 0;
        } else if (thisValue < otherValue) {
            return -1;
        } else {
            return 1;
        }
    }

    public static Fifteen dfs(final Fifteen fifteen) {
        Fifteen permutation;
        Set<Fifteen> visited = new HashSet<Fifteen>();
        Stack<Fifteen> stack = new Stack<Fifteen>();
        stack.push(fifteen);
        Fifteen current;
        do {
            current = stack.pop();
            if (current.equals(SOLUTION)) {
                System.out.println("Solved using DFS!");
                System.out.println("Visited: " + visited.size());
                System.out.println("Moves to solve: " + stack.size());
                System.out.println(current);
                return current;
            }
            for (int i = 0; i < 4; i++) {
                permutation = new Fifteen(current, false);
                if (permutation.isMoveLegal(i)) {
                    permutation.move(i);
                    if (visited.add(permutation)) {
                        stack.push(permutation);
                    }
                }
            }
        } while (stack.size() > 0);
        return null;
    }

    public static Fifteen bfs(final Fifteen fifteen) {
        Fifteen permutation;
        Set<Fifteen> visited = new HashSet<Fifteen>();
        Queue<Fifteen> queue = new LinkedList<Fifteen>();
        queue.add(fifteen);
        Fifteen current;
        do {
            current = queue.poll();
            if (current.equals(SOLUTION)) {
                System.out.println("Solved using BFS!");
                System.out.println("Visited: " + visited.size());
                System.out.println("Moves to solve: " + queue.size());
                System.out.println(current);
                return current;
            }
            for (int i = 0; i < 4; i++) {
                permutation = new Fifteen(current, false);
                if (permutation.isMoveLegal(i)) {
                    permutation.move(i);
                    if (visited.add(permutation)) {
                        queue.add(permutation);
                    }
                }
            }
        } while (queue.size() > 0);
        return null;
    }

    public static Fifteen aStar(Fifteen fifteen) {
        Fifteen permutation;
        Set<Fifteen> visited = new HashSet<Fifteen>();
        PriorityQueue<Fifteen> queue = new PriorityQueue<Fifteen>();
        fifteen.value = fifteen.getValue();
        queue.add(fifteen);
        Fifteen current;
        do {
            current = queue.poll();
//            System.out.println("Visited: " + visited.size());
//            System.out.println("Moves to solve: " + queue.size());
//            System.out.println("cost: " + current.cost);
//            System.out.println("value: " + current.value);
//            System.out.println(current);
            if (current.equals(SOLUTION)) {
                System.out.println("Solved using A*!");
                System.out.println("Visited: " + visited.size());
                System.out.println("Moves to solve: " + queue.size());
                System.out.println(current);
                return current;
            }
            for (int i = 0; i < 4; i++) {
                permutation = new Fifteen(current, true);
                if (permutation.isMoveLegal(i)) {
                    permutation.move(i);
                    permutation.cost++;
                    permutation.value = permutation.getValue();
                    if (visited.add(permutation)) {
                        queue.add(permutation);
                    }
                }
            }
        } while (queue.size() > 0);
        System.out.println("Visited: " + visited.size());
        System.out.println("Moves to solve: " + queue.size());
        System.out.println("cost: " + current.cost);
        System.out.println("value: " + current.value);
        System.out.println(current);
        return current;
    }
}
