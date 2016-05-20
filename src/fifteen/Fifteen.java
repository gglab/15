/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fifteen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
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
    private static final String FILE_BFS_KEY = "fileToSave_BFS";
    private static final String FILE_DFS_KEY = "fileToSave_DFS";
    private static final String FILE_ASTAR1_KEY = "fileToSave_A*1";
    private static final String FILE_ASTAR2_KEY = "fileToSave_A*2";
    private static final String DIRECTION_KEY = "direction";

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(PROPERTIES_FILE_NAME));
        } catch (IOException ex) {
            System.out.println(new File(PROPERTIES_FILE_NAME).getAbsolutePath());
            Logger.getLogger(Fifteen.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private static final File SOLUTION_FILE_BFS = new File(properties.getProperty(FILE_BFS_KEY, "Solution_BFS.txt"));
    private static final File SOLUTION_FILE_DFS = new File(properties.getProperty(FILE_DFS_KEY, "Solution_DFS.txt"));
    private static final File SOLUTION_FILE_ASTAR1 = new File(properties.getProperty(FILE_ASTAR1_KEY, "Solution_aStarManhattan.txt"));
    private static final File SOLUTION_FILE_ASTAR2 = new File(properties.getProperty(FILE_ASTAR2_KEY, "Solution_aStarHamming.txt"));

    private static final int ROWS = Integer.parseInt(properties.getProperty(ROWS_KEY, "2"));
    private static final int COLS = Integer.parseInt(properties.getProperty(COLS_KEY, "2"));
    private static boolean isManhattan = true;
    private static final String STATE = properties.getProperty(STATE_KEY, "0 1 2 3");
    private static final List<Move> DIRECTIONS = getDirections(properties.getProperty(DIRECTION_KEY, "GPDL"));
    private static final boolean IS_RANDOM = RANDOM.equals(STATE);
    public static final Fifteen SOLUTION = new Fifteen(true);

    int[][] board = new int[ROWS][COLS];
    int c;
    int r;
    int cost = 0;
    int value = 0;
    Move m;

    public Fifteen(final boolean solved) {
        m = null;
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
        m = null;
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

        if (COLS % 2 == 0) {
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
        if (isManhattan) {
            return getManhattanDistance();
        } else {
            return getHammingDistance();
        }
    }

    private int getHammingDistance() {
        int i = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] != SOLUTION.board[row][col]) {
                    i++;
                }
            }
        }
        return i;
    }

    private int getManhattanDistance() {
        int result = 0;
        int i = 1;
        int[] position;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (i == ROWS * COLS) {
                    continue;
                }
                position = getPosition(i);
                result += Math.abs(row - position[0]) + Math.abs(col - position[1]);
                i++;
            }
        }
        position = getPosition(0);
        result += Math.abs(ROWS - 1 - position[0]) + Math.abs(COLS - 1 - position[1]);
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
        } else if (!fifteen.isSolvable(fifteen.parseState())) {
            throw new Exception("Puzzle is not solvable!");
        }
        System.out.println("Puzzle is solvable!");
        System.out.println("Puzzle to solve:");
        System.out.println(fifteen);
        System.out.println("----------");
        isManhattan = true;
        System.out.println("Manhattan huristic method");
        fifteen.value = fifteen.getValue();
        saveSolution(Fifteen.aStar(fifteen), SOLUTION_FILE_ASTAR1);
        System.out.println("----------");
        isManhattan = false;
        System.out.println("Hamming heuristic method");
        fifteen.value = fifteen.getValue();
        saveSolution(Fifteen.aStar(fifteen), SOLUTION_FILE_ASTAR2);
        System.out.println("----------");
        saveSolution(Fifteen.bfs(fifteen), SOLUTION_FILE_BFS);
        System.out.println("----------");
        saveSolution(Fifteen.dfs(fifteen), SOLUTION_FILE_DFS);
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

    boolean move(final Move direction) {
        final int x = board[r][c];
        try {
            switch (direction) {
                //do góry    
                case G:
                    board[r][c] = board[r - 1][c];
                    board[r - 1][c] = x;
                    r--;
                    return true;
                //prawo
                case P:
                    board[r][c] = board[r][c + 1];
                    board[r][c + 1] = x;
                    c++;
                    return true;

                //dół
                case D:
                    board[r][c] = board[r + 1][c];
                    board[r + 1][c] = x;
                    r++;
                    return true;
                //lewo
                case L:
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

    public boolean isMoveLegal(final Move direction) {
        switch (direction) {
            //do góry    
            case G:
                if (r != 0) {
                    return true;
                }
                break;
            //prawo
            case P:
                if (c != COLS - 1) {
                    return true;
                }
                break;
            //dół
            case D:
                if (r != ROWS - 1) {
                    return true;
                }
                break;
            //lewo
            case L:
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
            if (isMoveLegal(Move.getMove(move))) {
                move(Move.getMove(move));
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

    public static LinkedList<Fifteen> dfs(final Fifteen fifteen) {
        final long startTime = System.currentTimeMillis();
        Fifteen permutation;
        HashMap<Fifteen, Fifteen> predecessor = new HashMap<Fifteen, Fifteen>();
        predecessor.put(fifteen, null);
        Stack<Fifteen> stack = new Stack<Fifteen>();
        stack.push(fifteen);
        Fifteen current;
        do {
            current = stack.pop();
            if (current.equals(SOLUTION)) {
                final long end = System.currentTimeMillis() - startTime;
                System.out.println("Solved using DFS in " + end + " ms");
                LinkedList<Fifteen> solution = new LinkedList<Fifteen>();
                Fifteen prev = current;
                StringBuffer sb = new StringBuffer();
                while (prev != null) {
                    solution.addFirst(prev);
                    if (prev.m != null) {
                        sb.insert(0, prev.m);
                    }
                    prev = predecessor.get(prev);
                }
                System.out.println("Moves to solve: " + solution.size());
                System.out.println(sb);
                return solution;
            }
            for (Move m : DIRECTIONS) {
                permutation = new Fifteen(current, false);
                if (permutation.isMoveLegal(m)) {
                    permutation.move(m);
                    permutation.m = m;
                    if (!predecessor.containsKey(permutation)) {
                        predecessor.put(permutation, current);
                        stack.push(permutation);
                    }
                }
            }
        } while (stack.size() > 0);
        return null;
    }

    public static LinkedList<Fifteen> bfs(final Fifteen fifteen) {
        final long startTime = System.currentTimeMillis();
        Fifteen permutation;
        HashMap<Fifteen, Fifteen> predecessor = new HashMap<Fifteen, Fifteen>();
        predecessor.put(fifteen, null);
        Queue<Fifteen> queue = new LinkedList<Fifteen>();
        queue.add(fifteen);
        Fifteen current;
        do {
            current = queue.poll();
            if (current.equals(SOLUTION)) {
                final long end = System.currentTimeMillis() - startTime;
                System.out.println("Solved using BFS in " + end + " ms");
                LinkedList<Fifteen> solution = new LinkedList<Fifteen>();
                Fifteen prev = current;
                StringBuffer sb = new StringBuffer();
                while (prev != null) {
                    solution.addFirst(prev);
                    if (prev.m != null) {
                        sb.insert(0, prev.m);
                    }
                    prev = predecessor.get(prev);
                }
                System.out.println("Moves to solve: " + solution.size());
                System.out.println(sb);
                return solution;
            }
            for (Move m : DIRECTIONS) {
                permutation = new Fifteen(current, false);
                if (permutation.isMoveLegal(m)) {
                    permutation.move(m);
                    permutation.m = m;
                    if (!predecessor.containsKey(permutation)) {
                        predecessor.put(permutation, current);
                        queue.add(permutation);
                    }
                }
            }
        } while (queue.size() > 0);
        return null;
    }

    public static LinkedList<Fifteen> aStar(Fifteen fifteen) {
        final long startTime = System.currentTimeMillis();
        Fifteen permutation;
        HashMap<Fifteen, Fifteen> predecessor = new HashMap<Fifteen, Fifteen>();
        predecessor.put(fifteen, null);
        PriorityQueue<Fifteen> queue = new PriorityQueue<Fifteen>();
        fifteen.value = fifteen.getValue();
        queue.add(fifteen);
        Fifteen current;
        do {
            current = queue.poll();
            if (current.equals(SOLUTION)) {
                final long end = System.currentTimeMillis() - startTime;
                System.out.println("Solved using A* in " + end + " ms");
                LinkedList<Fifteen> solution = new LinkedList<Fifteen>();
                Fifteen prev = current;
                StringBuffer sb = new StringBuffer();
                while (prev != null) {
                    solution.addFirst(prev);
                    if (prev.m != null) {
                        sb.insert(0, prev.m);
                    }
                    prev = predecessor.get(prev);
                }
                System.out.println("Moves to solve: " + solution.size());
                System.out.println(sb);
                return solution;
            }
            for (Move m : DIRECTIONS) {
                permutation = new Fifteen(current, true);
                if (permutation.isMoveLegal(m)) {
                    permutation.move(m);
                    permutation.m = m;
                    permutation.cost++;
                    permutation.value = permutation.getValue();
                    if (!predecessor.containsKey(permutation)) {
                        predecessor.put(permutation, current);
                        queue.add(permutation);
                    }
                }
            }
        } while (queue.size() > 0);
        return null;
    }

    private static void saveSolution(final Iterable collection, final File file) {
        try {
            final PrintWriter writer = new PrintWriter(file);
            for (Iterator<Fifteen> iterator = collection.iterator(); iterator.hasNext();) {
                Fifteen temp = (Fifteen) iterator.next();
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        writer.print(SEPARATOR + temp.board[i][j] + SEPARATOR);
                    }
                    writer.println();
                }
                writer.println("--------------------------------------------------");
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Fifteen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static List<Move> getDirections(final String directions) {
        //System.out.println(directions);
        List<Move> result = new ArrayList<Move>(4);
        if (directions.equalsIgnoreCase(RANDOM)) {
            Random r = new Random(0);
            for (int i = 0; i < 4; i++) {
                result.add(Move.getMove(r.nextInt(4)));
            }
            return result;
        }
        for (int i = 0; i < 4; i++) {
            result.add(Move.valueOf(directions.substring(i, i + 1)));
        }
        return result;
    }
}

enum Move {

    G(0), P(1), D(2), L(3);
    private int value;

    private Move(int value) {
        this.value = value;
    }

    public static Move getMove(int value) {
        return Move.values()[value];
    }
}
