package arai.csp;

import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		SudokuReader sr = new SudokuReader("sudoku_training.txt");

		try {
			List<String> puzzles = sr.readPuzzles();
			int count = 0;
			for (String puzzle : puzzles) {
				count++;
				if (count >= 25) {
					break;
				}
				// run and time CSP Solver
				long cspStart = System.currentTimeMillis( );
				//new CSPSolver().solve(puzzle);
				long cspEnd = System.currentTimeMillis( );
		        long cspDiff = cspEnd - cspStart;

		        // run and time brute force solver
				long bruteStart = System.currentTimeMillis( );
				new BruteForce().solve(puzzle);
				long bruteEnd = System.currentTimeMillis( );
		        long bruteDiff = bruteEnd - bruteStart;
		        System.out.println(cspDiff + " " + bruteDiff);
				// time
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
