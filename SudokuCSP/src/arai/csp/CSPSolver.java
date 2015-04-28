package arai.csp;

import java.util.HashMap;
import java.util.Map;

public class CSPSolver {

	public static final int NUMBER_OF_BOXES = 81;

	// True if the puzzle has been solved
	private boolean solved = false;

	// A String array containing the domains for all variables
	private String[] variables;

	// The Sudoku puzzle to be solved, unassigned variables are denoted with a
	// point ('.').
	private String puzzle;

	// Used to compute the row and column of each variable
	private final int[] MULTIPLE_OF_NINE = new int[] { 9, 18, 27, 36, 45, 54,
			63, 72, 81 };

	private Map<Integer, Integer> locRow;
	private Map<Integer, Integer> locClm;

	/**
	 * Initialize the variables and domains.
	 * 
	 * Compute the row and column of each variable.
	 */
	private void initVariables() {
		variables = new String[NUMBER_OF_BOXES];
		locRow = new HashMap<Integer, Integer>();
		locClm = new HashMap<Integer, Integer>();

		for (int i = 0; i < NUMBER_OF_BOXES; i++) {
			if (puzzle.charAt(i) == '.') {
				variables[i] = "123456789";
			} else {
				variables[i] = "" + puzzle.charAt(i);
			}
			String loc = computeLoc(i);
			locRow.put(i, Integer.valueOf(loc.split(",")[0]));
			locClm.put(i, Integer.valueOf(loc.split(",")[1]));
		}
	}

	/**
	 * Compute the location of the given variable in the Sudoku puzzle.
	 * 
	 * @param index
	 *            The index of the current variable in the variables array.
	 * @return A String containing the row and column of the variable, separated
	 *         by a comma.
	 */
	private String computeLoc(int index) {
		int column = 0;
		int row = 0;

		for (int i = 0; i < MULTIPLE_OF_NINE.length; i++) {
			int temp = MULTIPLE_OF_NINE[i] - (index + 1);

			if (temp < 0)
				continue;

			column = 9 - temp;
			row = i + 1;
			break;
		}

		return row + "," + column;
	}

	/**
	 * Check the row of the given variable to reduce its domain.
	 * 
	 * If an element of the given variable is already present in another
	 * variable with domain length 1 (assigned) in the same row, then remove
	 * that element from its domain.
	 * 
	 * Most basic reasoning method for solving Sudoku puzzles. May not be
	 * sufficient for more difficult puzzles.
	 * 
	 * @param index
	 *            The index of the current variable in the variables array.
	 */
	private void constraintRow(int index) {
		int row = locRow.get(index);
		for (int boxInRow = 0; boxInRow < locRow.size(); boxInRow++) {
			if (locRow.get(boxInRow) == row && index != boxInRow) {
				for (String value : variables[index].split("")) {
					if (variables[boxInRow].length() == 1
							&& variables[boxInRow].contains(value)) {
						variables[index].replace(value, "");
					}
				}
			}
		}
	}

	/**
	 * Check the column of the given variable to reduce its domain.
	 * 
	 * If an element of the given variable is already present in another
	 * variable with domain length 1 (assigned) in the same column, then remove
	 * that element from its domain.
	 * 
	 * Most basic reasoning method for solving Sudoku puzzles. May not be
	 * sufficient for more difficult puzzles.
	 * 
	 * @param index
	 *            The index of the current variable in the variables array.
	 */
	private void constraintColumn(int index) {
		int clm = locClm.get(index);
		for (int boxInClm = 0; boxInClm < locClm.size(); boxInClm++) {
			if (locClm.get(boxInClm) == clm && index != boxInClm) {
				for (String value : variables[index].split("")) {
					if (variables[boxInClm].length() == 1
							&& variables[boxInClm].contains(value)) {
						variables[index].replace(value, "");
					}
				}
			}
		}
	}

	private void constraintRegion(int index) {

	}

	/**
	 * Used to find a solution for a Sudoku puzzle using a CSP approach.
	 * 
	 * @param puzzle
	 *            The Sudoku puzzle to be solved.
	 */
	public void solve(String puzzle) {
		this.puzzle = puzzle;

		initVariables();

		reduceDomains();

		if (!isSolved()) {
			// heuristic to assign value to variable
			// e.g. first variables with smallest domains
		}
	}

	/**
	 * Loop through all boxes and check all three main constraints. Continue
	 * until no variable domains can be further reduced. This indicates either
	 * that the puzzle has been solved or that more advanced solving techniques
	 * are necessary to complete it.
	 */
	private void reduceDomains() {
		boolean reduced = false;

		for (int index = 0; index < NUMBER_OF_BOXES; index++) {
			if (variables[index].length() == 1)
				continue;

			int var_length = variables[index].length();

			constraintRow(index);
			constraintColumn(index);
			constraintRegion(index);
			// more constraints...

			if (variables[index].length() < var_length)
				reduced = true;
		}

		if (reduced) {
			reduceDomains();
		}
	}

	/**
	 * Check if the puzzle has been fully solved. A puzzle has been solved if
	 * for all variables the domain has been reduced to a length of 1.
	 * 
	 * @return
	 */
	public boolean isSolved() {
		for (String var : variables) {
			if (var.length() == 1)
				continue;
			return false;
		}
		return true;
	}

	/**
	 * Retrieve the solution to the given puzzle, if any.
	 * 
	 * @return
	 */
	public String solution() {
		if (isSolved()) {
			String solution = "";
			for (String s : variables) {
				solution += s;
			}
			return solution;
		} else {
			return null;
		}
	}
}
