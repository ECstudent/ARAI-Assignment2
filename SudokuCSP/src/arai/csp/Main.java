package arai.csp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		SudokuReader sr = new SudokuReader("sudoku_training.txt");

		CSPSolver cspSolver = new CSPSolver();
		// BruteForce bruteSolver = new BruteForce();

		List<Long> avgCsp = new ArrayList<Long>();
		List<Long> avgBru = new ArrayList<Long>();

		try {
			List<String> puzzles = sr.readPuzzles();

			int count = 0;

			for (String puzzle : puzzles) {
				if (++count > 100)
					break;

				// run and time CSP Solver
				long cspStart = System.currentTimeMillis();
				cspSolver.solve(puzzle);
				long cspEnd = System.currentTimeMillis();
				long cspDiff = cspEnd - cspStart;

				// run and time brute force solver
				long bruteStart = System.currentTimeMillis();
				new BruteForce().solve(puzzle);
				long bruteEnd = System.currentTimeMillis();
				long bruteDiff = bruteEnd - bruteStart;

				avgCsp.add(cspDiff);
				avgBru.add(bruteDiff);

				// time
				System.out.println(count + " " + cspDiff + " " + bruteDiff);
			}

			long sumCsp = 0;
			long sumBru = 0;

			for (Long l : avgCsp)
				sumCsp += l;

			for (Long l : avgBru)
				sumBru += l;

			// averages
			System.out.println("  " + " " + (sumCsp / avgCsp.size()) + " "
					+ (sumBru / avgBru.size()));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
