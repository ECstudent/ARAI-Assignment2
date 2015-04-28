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

	private String fileName;

	public SudokuReader(String fileName) {
		this.fileName = fileName;
	}

	public List<String> readPuzzles() throws IOException {
		Scanner sc = new Scanner(new FileReader(fileName));

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
