package arai.csp;

/**
 * This class takes in a sudoku files and reads all the clauses that are
 * specific to that puzzle.  It then outputs the information line at the top
 * in the form of:  "p cnf 999 " + totalClauseCount"  It will then list all
 * of the clauses that are specific to the puzzle passed into the constructor.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileReader;

public class SudokuReader {

	private static final int BOXES_IN_SUDOKU = 81;

	// This is the number of clauses that each sudoku puzzles all have in common
	private Scanner sc;
	private String puzzle = null;

	public SudokuReader(Scanner sc) {
		this.sc = sc;
	}

	public List<String> readPuzzles() throws IOException {
		Scanner sc = new Scanner(new FileReader("sudoku_training.txt"));

		String line = null;

		List<String> puzzles = new ArrayList<String>();

		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (line.length() == 81)
				puzzles.add(line);
		}

		sc.close();

		return puzzles;
	}

}
