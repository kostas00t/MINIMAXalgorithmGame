/*/**
 * Artificial Intelligence - MYY602
 * Semester Project - Exercise 2
 * KONSTANTINOS KIKIDIS - 4387
 * CHRISTOS KROKIDAS - 4399
 * KONSTANTINOS TSAMPIRAS - 4508
 */

/*/**
 * "A": Player A (computer), "B": Player B (human/computer(see main)), "-": Empty/White position, "*": Filled/Black position
 * The program asks the user to enter the grid size of the board, eg 5 creates a 5x5 board, works fast with for
 * less than 7, the grid size should be less than 10, to maintain board alignment (but works for any size). 
 * Then it asks for the starting positions of players A and B, if 0 entered at any point, we take the default 
 * positions (Up and Centered for A and Down and Centered for B). Then it asks for some predefined "blacked" boxes,
 * enter 0 to stop at any time. After that, the game starts, with the computer playing first. We have defined a
 * max step size of 4, but that can be changed, from line 41 we could have it as user-defined, with one more input
 * from user and passing it as parameter to starting Board Constructor. During gameplay the computer calculates the
 * best possible move by analyzing all possible boards, and stoping when he finds the position that is more likely to
 * win, it can achieve that, by counting the empty spaces nearby for himself and for the human.
 */
 

import java.util.LinkedList;
import java.util.Scanner;
import java.util.ArrayList;

public class Board {
    // A Board Object has:
    private String[][] board; 	// a 2d-array representation of the board,
	private int[] a_pos;		// the positions of A
	private int[] b_pos;		// and B,
	private Board parent;		// the parent board (which board generated the current board, null if it's the starting board),
    private int MAX_STEP_SIZE;	// the max step size (how far a player can go), and
    private int n;				// the dimension of the board.


	// Constructor for the first/starting board
    public Board(int n, int ax, int ay, int bx, int by)	{

        this.n = n; 
		this.MAX_STEP_SIZE = 4; // THIS CAN BE CHANGED FROM HERE
		this.parent = null;
		a_pos = new int[2];
		b_pos = new int[2];

		// First board creation, empty spots represented by "-"
		board = new String[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				board[i][j] = "-";
			}
		}

		// Putting the players on the board and assigning their positions
		// Default positions
		if (ax == 0 || ay == 0 || bx == 0 || by == 0) {
			board[0][n/2] = "A";
			board[n-1][n/2] = "B";

			a_pos[0] = 0;
			a_pos[1] = n/2;

			b_pos[0] = n-1;
			b_pos[1] = n/2;
		
		// Custom positions
		} else if (ax <= n || ay <= n || bx <= n || by <= n) {
			board[ax-1][ay-1] = "A";
			board[bx-1][by-1] = "B";

			a_pos[0] = ax-1;
			a_pos[1] = ay-1;

			b_pos[0] = bx-1;
			b_pos[1] = by-1;

		}
	}

	// Constructor for every new board except the first one
	public Board(Board parentBoard, int n) {

		this.n = n;
		this.MAX_STEP_SIZE = parentBoard.MAX_STEP_SIZE;
		this.parent = parentBoard;

		// Copy board from parent board
		board = new String[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				board[i][j] = parentBoard.board[i][j];
			}
		}

		// Copy positions from parent board
		a_pos = new int[2];
		a_pos[0] = parentBoard.a_pos[0];
		a_pos[1] = parentBoard.a_pos[1];

		b_pos = new int[2];
		b_pos[0] = parentBoard.b_pos[0];
		b_pos[1] = parentBoard.b_pos[1];
	}

	// Method to find the opponent of a player (for A is B, and for B is A)
	public String opp(String player) {
		if (player.equals("A")) {
			return "B";
   		} else {
			return "A";
   		}
	}

	// Prints the board (works pretty for less than 10, after that alignment breaks)
    public void printBoard() {
		System.out.print("    ");
		for (int h = 1; h < n+1; h++) {
			System.out.print(h + " ");
		}
		System.out.println();
		for (int i = 0; i < n; i++) {
			System.out.print((i+1) + " [ ");
			for (int j = 0; j < n; j++) {
				System.out.print(board[i][j] + " ");
			}
			System.out.println("]");
		}
		System.out.println();
	}

    // Check if a position is filled already
	public boolean isFilled(int x, int y) {
		// Mitigation for an outOfBounds error
		// if x or y given is equal to n, treat it as filled
		if (x == getN() || y == getN()) {
			return true;
		}
		// If there's a "-" then it's empty
		if (board[x][y].equals("-")) {
			return false;
		}
		return true;
	}

	// Check if a position is empty (reversed isFilled answer)
	public boolean isEmpty(int x, int y) {
		return !isFilled(x, y);
	}

	// Check if provided position is outOfBounds
	public boolean outOfBounds(int x, int y) {
		if (x > n || x < 0 || y > n || y < 0) {
			return true;
		}
		return false;
	}

	// Check if the position entered is valid
	public boolean isValid(String player, int x, int y, ArrayList<Integer[]> moves)	{

		// If outside of array, invalid position
		if (outOfBounds(x, y)) {
			return false;
		}

		// Row and column of players' current position
		int currentRow, currentColumn;
		if (player.equals("A")) { 	// player "A"
			currentRow = a_pos[0];
			currentColumn = a_pos[1];
		} else {					// player "B"
			currentRow = b_pos[0];
			currentColumn = b_pos[1];
		}

		// Distance on x and y axis, of new and old position
		int yAxisDistance = Math.abs(currentRow - x);
		int xAxisDistance = Math.abs(currentColumn - y);

		// If exceeded max step size
		if (yAxisDistance > MAX_STEP_SIZE || xAxisDistance > MAX_STEP_SIZE) {
			System.out.println("Over the max distance, choose a position less than " + MAX_STEP_SIZE + " away.");
			return false;
		}

		// If newPosition = oldPosition
		if (yAxisDistance == 0 && xAxisDistance == 0) {
			System.out.println("Need to move to another spot!");
			return false;
		}

		// Moving UP
		if ((x < currentRow) && (y == currentColumn)) {
			for (int i = 1; i <= yAxisDistance; i++) {
				if (isFilled(currentRow-i, y)) {		// Check if positions above our target position are filled
					moves.clear();
					return false;						// Filled position above found, invalid position, can't go there
				}
				Integer[] pos = {currentRow-i, y};		// Collect the positions required to go there (if invaild pos, clear moves)
				moves.add(pos);
			}
			return true;
		}

		// Same as above, just in other directions
		// Moving DOWN
		else if ( (x > currentRow) && (y == currentColumn)) {
			for (int i = 1; i <= yAxisDistance; i++) {
				if (isFilled(currentRow+i, y)) {
					moves.clear();
					return false;
				}
				Integer[] pos = {currentRow+i, y};
				moves.add(pos);
			}
			return true;
		}

		// Moving LEFT
		else if ( (x == currentRow) && (y < currentColumn)) {
			for (int i = 1; i <= xAxisDistance; i++) {
				if (isFilled(x, currentColumn-i)) {
					moves.clear();
					return false;
				}
				Integer[] pos = {x, currentColumn-i};
				moves.add(pos);
			}
			return true;
		}

		// Moving RIGHT
		else if ( (x == currentRow) && (y > currentColumn)) {
			for (int i = 1; i <= xAxisDistance; i++) {
				if (isFilled(x, currentColumn+i)) {
					moves.clear();
					return false;
				}
				Integer[] pos = {x, currentColumn+i};
				moves.add(pos);
			}
			return true;
		}

		// Moving Diagonally
		else if (yAxisDistance == xAxisDistance) {			// If x and y axis distances are the same, we move diagonally, check as above

			// UP and LEFT
			if (x < currentRow && y < currentColumn) {
				for (int i = 1; i <= xAxisDistance; i++) {
					if (isFilled(currentRow-i, currentColumn-i)) {
						moves.clear();
						return false;
					}
					Integer[] pos = {currentRow-i,currentColumn-i};
					moves.add(pos);
				}
				return true;
			}

			// UP and RIGHT
			else if (x < currentRow && y > currentColumn) {
				for (int i = 1; i <= xAxisDistance; i++) {
					if (isFilled(currentRow-i, currentColumn+i)) {
						moves.clear();
						return false;
					}
					Integer[] pos = {currentRow-i,currentColumn+i};
					moves.add(pos);
				}
				return true;
			}

			// DOWN and RIGHT
			else if (x > currentRow && y > currentColumn) {
				for (int i = 1; i <= xAxisDistance; i++) {
					if (isFilled(currentRow+i, currentColumn+i)) {
						moves.clear();
						return false;
					}
					Integer[] pos = {currentRow+i,currentColumn+i};
					moves.add(pos);
				}
				return true;
			}
			
			// DOWN and LEFT
			else if (x > currentRow && y < currentColumn) {
				for (int i = 1; i <= xAxisDistance; i++) {
					if (isFilled(currentRow+i, currentColumn-i)) {
						moves.clear();
						return false;
					}
					Integer[] pos = {currentRow+i,currentColumn-i};
					moves.add(pos);
				}
				return true;
			}
		}
		return false; // Neither Diagonal or x-Axis or y-Axis movement
	}

    // Check if a player has lost, by checking if there's no empty blocks surrounding it
	public boolean lose(String player) {

		int currentRow, currentColumn;
		if (player.equals("A")) {		// player "A"
			currentRow = a_pos[0];
			currentColumn = a_pos[1];
		} else {						// player "B"
			currentRow = b_pos[0];
			currentColumn = b_pos[1];
		}

		// Check surrounding/neighboring positions
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (i != 0 || j != 0) {
					if (!outOfBounds(currentRow + i, currentColumn + j)) {
						if (isEmpty(currentRow + i, currentColumn + j)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	// Move to new position
	public void move(String player, int x, int y, ArrayList<Integer[]> moves) {

		// Decrement x and y by 1 to play nice with the arrays, we have to occupy each step till we go to our target
		x--;
		y--;

		if (isValid(player, x, y, moves)) {

			if (player.equals("A")) {
				// Replace A's pos with "*"
				board[a_pos[0]][a_pos[1]] = "*";
				for (Integer[] i : moves) {
					board[i[0]][i[1]] = "*";
				}

				// board now has new position of A
				board[x][y] = "A";

				// update A's registered position
				a_pos[0] = x;
				a_pos[1] = y;

			} else if (player.equals("B")) {		// Same for "B"

				board[b_pos[0]][b_pos[1]] = "*";
				for (Integer[] i : moves) {
					board[i[0]][i[1]] = "*";
				}

				board[x][y] = "B";

				b_pos[0] = x;
				b_pos[1] = y;

			} else {
				System.out.println("error: invalid player");
			}
		} else {
			System.out.println("Not valid");
		}
	}

	// Move to new position with a new board (for generateMoves)
	public Board moveNew(String player, int x, int y, ArrayList<Integer[]> moves) {
		x--;
		y--;
		Board newBoard = null;
		//implement isValid() here!
		if (isValid(player, x, y, moves)) {
			newBoard = new Board(this, this.getN());
			// Add 1 because we decrement again in move.
			newBoard.move(player, x+1, y+1, moves);
		}
		return newBoard;
	}

	// Generate computers' possible moves
	public LinkedList<Board> generateMoves(String player, ArrayList<Integer[]> moves) {
		LinkedList<Board> m = new LinkedList<Board>();
		int currentRow, currentColumn;
		if (player.equals("A")) { 	// player "A"
			currentRow = a_pos[0];
			currentColumn = a_pos[1];
		} else {					// player "B"
			currentRow = b_pos[0];
			currentColumn = b_pos[1];
		}
		// Increment currentRow and currentColumn because of moveNew decrements by 1
		currentRow++;
		currentColumn++;
		// Check nearby cells till our max distance
		for (int i = -MAX_STEP_SIZE; i < MAX_STEP_SIZE+1; i++) {
			for (int j = -MAX_STEP_SIZE; j < MAX_STEP_SIZE+1; j++) {
				if (i != 0 || j != 0) {
					Board b = this.moveNew(player, currentRow+i, currentColumn+j, moves);
					if (b != null) {
						m.add(b);
						moves.clear();
					}
				}
			}

		}
		return m;
	}

	// For the computer, print the move it did (extended for computer-vs-computer case)
    public void printMove(String player) {
		int currentRow, currentColumn;
		if (player.equals("A")) { 	// player "A"
			currentRow = a_pos[0];
			currentColumn = a_pos[1];
		} else {					// player "B"
			currentRow = b_pos[0];
			currentColumn = b_pos[1];
		}
		// transform to User offset
		currentRow++;
		currentColumn++;
		System.out.println("Computer moves: (" + currentRow + ", " + currentColumn + ")");
	}


	// How many empty spaces we have surrounding the current position, that's how computer selects the best move
	public int eval(String player) {

		// Case 1: if player has lost, return negative MAX
		if (lose(player)) {
			return -Integer.MAX_VALUE;

		// Case 2: if opp has lost, return positive MAX
		} else if (lose(opp(player))) {
			return Integer.MAX_VALUE;

		// No-one has lost (yet), how many empty spaces we have surrounding the current position (max 2 blocks away)
		} else {
			int empty_spaces = 0;
			int currentRow, currentColumn;
			if (player.equals("A")) { 	/* "A" location */
				currentRow = a_pos[0];
				currentColumn = a_pos[1];
			} else {					/* "B" location */
				currentRow = b_pos[0];
				currentColumn = b_pos[1];
			}
			for (int i = -2; i < 3; i++) {
				for (int j = -2; j < 3; j++) {
					// Skip outOfBounds or current pos
					if ((i == 0 && j == 0) || outOfBounds(currentRow+i, currentColumn+j)) {
						;
					}
					// If empty, increment count
					else if (isEmpty(currentRow+i, currentColumn+j)) {
						empty_spaces++;
					}
				}
			}
			return empty_spaces;
		}
	}

	// Calculate the best move, pair a Board with an Integer value
	public Pair<Integer, Board> calculateBestMove(int depth, int MyBest, int HerBest, String player, ArrayList<Integer[]> moves) {

		Pair<Integer, Board> p;
		// Maximum depth reached, eval player
		if (depth == 0) {
			p = new Pair<Integer, Board>(eval(player), null);
			return p;
		// Check if player lost, assing board with -MAX_VALUE
		} else if (lose(player)) {
			p = new Pair<Integer, Board>(-Integer.MAX_VALUE, this);
			return p;
		// Check if opponent lost, assing board with MAX_VALUE
		} else if (lose(opp(player))) {
			p = new Pair<Integer, Board>(Integer.MAX_VALUE, this);
			return p;
		// Every other case
		} else {

			// Get a list with the possible moves
			LinkedList<Board> moveList = generateMoves(player, moves);
			int bestScore = MyBest;
			Board bestMove = null;

			// While we have possible moves to make
			while (moveList.size() > 0) {
				// Take the first move and recurse call calculateBestMove, with decremented depth by 1, inversed & reversed score values, and as the opponent
				Pair<Integer, Board> Try = moveList.getFirst().calculateBestMove(depth-1, -HerBest, -MyBest, opp(player), moves);
				// Get the score of the board
				int tryScore = -Try.getL();
				// Basically, find the best board (with the best score) and the move to achieve that
				if (tryScore >= bestScore) {
					bestScore = tryScore;
					bestMove = moveList.getFirst();
				}
				moveList.removeFirst();
			}
			// return a new pair, the best board with the best move
			p = new Pair<Integer, Board>(bestScore, bestMove);
			return p;
		}
	}

	// Getters
	public String[][] getBoard() {
		return board;
	}
	public int[] getA_pos() {
		return a_pos;
	}
	public int[] getB_pos() {
		return b_pos;
	}
	public Board getParent() {
		return parent;
	}
	public int getMaxStepSize() {
		return MAX_STEP_SIZE;
	}
	public int getN() {
		return n;
	}

    public static void main(String[] args) {
		System.out.print("Enter grid size, (eg 5): ");
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();

		// Get starting positions
		int ax = -1; int ay = -1; int bx = -1; int by = -1;
		while (ax < 0 || ax > n || ay < 0 || ay > n || bx < 0 || bx > n || by < 0 || by > n) {
			System.out.print("Enter player A row starting position (0 for defaults): ");
			ax = input.nextInt();
			if (ax == 0) {
				ax = 0; ay = 0; bx = 0; by = 0;
				break;
			}
			System.out.print("Enter player A column starting position (0 for defaults): ");		
			ay = input.nextInt();
			if (ay == 0) {
				ax = 0; ay = 0; bx = 0; by = 0;
				break;
			}
			System.out.print("Enter player B row starting position (0 for defaults): ");	
			bx = input.nextInt();
			if (bx == 0) {
				ax = 0; ay = 0; bx = 0; by = 0;
				break;
			}
			System.out.print("Enter player B column starting position (0 for defaults): ");	
			by = input.nextInt();
			if (by == 0) {
				ax = 0; ay = 0; bx = 0; by = 0;
				break;
			}

			if (ax < 0 || ax > n || ay < 0 || ay > n || bx < 0 || bx > n || by < 0 || by > n) {
				System.out.println("One of the parameters (negative or over grid size) was invalid, retry");
			}

		}

		// Initialization
        Board board = new Board(n, ax, ay, bx, by);
        board.printBoard();
		String p1 = "B"; 		// Human (or computer, see code below)
		String com = "A";		// Computer
		ArrayList<Integer[]> moves = new ArrayList<Integer[]>();
		Pair<Integer, Board> p;

		// To pre-mark positions as unavailable
		System.out.println("Select pre-filled locations, enter 0 to Stop");
		while (true) {
			System.out.print("Enter row: ");
			int row = input.nextInt();
			if (row == 0) {
				break;
			}
			System.out.print("Enter col: ");
			int col = input.nextInt();
			if (col == 0) {
				break;
			}

			if (board.outOfBounds(row, col)) {
				System.out.println("Invalid grid position, out of bounds");
			} else if (board.isFilled(row-1, col-1)) {
				System.out.println("Invalid grid position, already filled");
			} else {
				board.board[row-1][col-1] = "*";
				board.printBoard();
			}
		}

		// Game start
		while (true) {
			System.out.println("Computer is choosing move... ");

			// com = Computer
			// Calculate the best move
			p = board.calculateBestMove(n-1, -Integer.MAX_VALUE, Integer.MAX_VALUE, com, moves);
			// Get the board with the best move
			board = p.getR();
			// Print the board after computer's move
			board.printBoard();
			// Print the move the computer did
			board.printMove(com);
			// Clear the moves ArrayList
			moves.clear();

			// Check if computer has lost
			if (board.lose(com)) {
				System.out.println(p1 + " wins!");
				return;
			}

			// Edge case, catch case for n = X, 0 pre-marked location, user pos (X,X), (eg, for X=5) we had a problem without it
			if (board.eval(p1) == -Integer.MAX_VALUE || board.eval(com) == Integer.MAX_VALUE) {
				System.out.println(com + " wins!");
				return;
			}


			// p1 = Computer
			// Comment the code below (lines 645-659) an uncomment here (lines 637-642) for computer-vs-computer game
/* 			p = board.calculateBestMove(n-1, -Integer.MAX_VALUE, Integer.MAX_VALUE, p1, moves);
			board = p.getR();
			board.printBoard();
			board.printMove(p1);
			moves.clear();
*/

			// p1 = Human
			System.out.print("Select row: ");
			int row = input.nextInt();
			System.out.print("Select col: ");
			int col = input.nextInt();
			while (!board.isValid(p1, row-1, col-1, moves)) {
				System.out.println("Not a valid move, please try again");
				System.out.print("Select row: ");
				row = input.nextInt();
				System.out.print("Select col: ");
				col = input.nextInt();
			}
			board.move(p1, row, col, moves);
			moves.clear();
			board.printBoard();


			// Check if human (or computer, see comment above) has lost
			if (board.lose(p1)) {
				System.out.println(com + " wins!");
				return;
			}
		}
    }
}

// Helper class
class Pair<L,R> {

    private L l;
    private R r;

    public Pair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    public L getL() {
		return l;
	}
    public R getR() {
		return r;
	}

    public void setL(L l) {
		this.l = l;
	}
    public void setR(R r) {
		this.r = r;
	}
}
