package arai.csp;

import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		SudokuReader sr = new SudokuReader("sudoku_training.txt");

		try {
			List<String> puzzles = sr.readPuzzles();
			for (String puzzle : puzzles) {
				// time
				// csp.solve(puzzle);
				// time
				// time
				// bf.solve(puzzle);
				// time
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
