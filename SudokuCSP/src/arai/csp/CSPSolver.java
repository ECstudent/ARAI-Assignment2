package arai.csp;

import java.util.HashMap;
import java.util.Map;

public class CSPSolver {

	public static final int NUMBER_OF_BOXES_IN_ROW = 9;
	public static final int NUMBER_OF_BOXES_IN_CLM = 9;
	public static final int NUMBER_OF_BOXES_IN_REG = 9;
	public static final int NUMBER_OF_REGIONS_IN_ROW = 3;
	public static final int NUMBER_OF_REGIONS_IN_CLM = 3;
	public static final int NUMBER_OF_BOXES = 81;

	public static final String SPLIT_ALL = "(?!^)";

	public static final int CHECK_ALL = -1;

	public static final int ROW = 0;
	public static final int COLUMN = 1;
	public static final int REGION = 2;

	// Used for displaying the puzzle as it is being solved.
	private long printDelay = 0;

	// A String array containing the domains for all variables
	private String[] variables;

	// An int array containing the number of assigned occurrences for each value
	private int[] occurrences;

	// Holds the coordinates of all variables
	// The value is formatted as "rowcolumnregion", e.g., "142" would be the
	// first row, third column and second region
	// Note: Region numbers are calculated left-to-right, top-to-bottom.
	private Map<Integer, String> locs;

	/**
	 * Initialize the variables and domains.
	 * 
	 * Compute the row and column of each variable.
	 */
	private void initVariables(String puzzle) {
		variables = new String[NUMBER_OF_BOXES];
		locs = new HashMap<Integer, String>();

		occurrences = new int[9];

		int indexRow = 0;
		int indexClm = 1;
		int indexReg = 0;
		int regCount = -NUMBER_OF_REGIONS_IN_ROW;

		for (int index = 0; index < NUMBER_OF_BOXES; index++, indexClm++) {
			if (puzzle.charAt(index) == '.') {
				variables[index] = "123456789";
			} else {
				variables[index] = "" + puzzle.charAt(index);
				occurrences[Integer.valueOf("" + puzzle.charAt(index))]++;
			}

			if (index % NUMBER_OF_REGIONS_IN_ROW == 0) {
				indexReg++;
				if (index % NUMBER_OF_BOXES_IN_ROW == 0) {
					indexRow++;
					indexClm = 1;
					if (index
							% (NUMBER_OF_BOXES_IN_ROW * NUMBER_OF_REGIONS_IN_ROW) == 0)
						regCount += NUMBER_OF_REGIONS_IN_ROW;
					indexReg = 1 + regCount;
				}
			}
			locs.put(index, indexRow + "" + indexClm + "" + indexReg);
		}
	}

	/**
	 * Check the row, column or region of the given variable to reduce its
	 * domain.
	 * 
	 * If an element of the given variable is already present in another
	 * variable with domain length 1 (assigned) in the same row, column or
	 * region, then remove that element from its domain.
	 * 
	 * Most basic reasoning method for solving Sudoku puzzles. May not be
	 * sufficient for more difficult puzzles.
	 * 
	 * @param type
	 *            The area in the puzzle to check its constraints for. Can be
	 *            either ROW, COLUMN or REGION (0, 1 or 2).
	 * @param index
	 *            The index of the current variable in the variables array.
	 * @param tempVariables
	 */
	private void basicConstraints(int type, int index, String[] tempVariables) {
		int loc = Integer.valueOf(locs.get(index).split(SPLIT_ALL)[type]);

		// Check the actual constraints...
		// If a possible value in the variables domain has already been
		// assigned to another variable in the same ROW, COLUMN or REGION,
		// then it can't possibly be assigned to the current variable and
		// must, therefore, be removed from its domain.
		for (String value : tempVariables[index].split(SPLIT_ALL)) {
			for (int box = 0; box < NUMBER_OF_BOXES; box++) {
				// We only need to check with boxes that have assigned values.
				if (tempVariables[box].length() != 1)
					continue;

				int boxLoc = Integer
						.valueOf(locs.get(box).split(SPLIT_ALL)[type]);

				// Ignore itself and other boxes not in the same ROW, COLUMN or
				// REGION.
				if (boxLoc != loc || index == box)
					continue;

				// Remove the value if it has already been assigned to another
				// variable.
				if (tempVariables[box].contains(value))
					tempVariables[index] = tempVariables[index].replace(value,
							"");
			}
		}
	}

	/**
	 * Used to find a solution for a Sudoku puzzle using a CSP approach.
	 * 
	 * Includes initialization of variables and domains.
	 * 
	 * @param puzzle
	 *            The Sudoku puzzle to be solved.
	 */
	public void solve(String puzzle) {
		long time1 = System.nanoTime();
		initVariables(puzzle);
		System.out.print("t1:");
		System.out.println((System.nanoTime() - time1) / 1000);
		// System.out.println(solution());

		// printDelay = System.nanoTime();

		time1 = System.nanoTime();
		dfSearch(variables.clone(), CHECK_ALL);

		System.out.print("dfsearch:");
		System.out.println((System.nanoTime() - time1) / 1000);
		// System.out.println(solution());
	}

	/**
	 * Use a depth-first, recursive approach to solve the puzzle.
	 * 
	 * At every level constraint propagation takes place as far as possible
	 * (this can be further extended upon as described in constraintProp()).
	 * 
	 * After constraint propagation, we need to check if an empty domain is
	 * present for any of the variables. This indicates that (at least) one of
	 * the previous assigned variables was incorrect.
	 * 
	 * If there is no empty domain found (yet), we need to check if the puzzle
	 * is solved (all variables have correct assignments). If it is solved, copy
	 * the final assignments to the original variable array and end the search.
	 * 
	 * If it is not (yet) solved, use a heuristic to determine which variable
	 * needs to be assigned a value first. An ordered list is created to loop
	 * over (see method varSelection()). Whenever an assigned variable leads to
	 * an empty domain (i.e., it is incorrect), try the next value, etc. If all
	 * of the possible variable assignments lead to an empty domain, go "up" one
	 * level and try the next variable value there, etc.
	 * 
	 * @param tempVariables
	 * @param assigned
	 *            Has a value of -1 if the constraints for all boxes need to be
	 *            checked. Otherwise has a value between 0 and NUMBER_OF_BOXES -
	 *            1.
	 * @return
	 */
	private boolean dfSearch(String[] tempVariables, int assigned) {
		long time1 = System.nanoTime();
		constraintProp(tempVariables, assigned);

		System.out.print("cprop:");
		System.out.println((System.nanoTime() - time1) / 1000);

		// if (printDelay + 100 < System.nanoTime()) {
		// System.out.println(getCurrentAssignments(tempVariables));
		// printDelay = System.nanoTime();
		// }

		if (hasEmptyDomain(tempVariables))
			return false;

		if (!isSolved(tempVariables)) {
			int varIndex = singleVarSelection(tempVariables);
			String[] sortedValues = sortDomainByOccurrence(tempVariables[varIndex].split(SPLIT_ALL));
			for (int index = 0; index < sortedValues.length; index++) {
				tempVariables[varIndex] = sortedValues[index];				
				occurrences[Integer.valueOf(sortedValues[index])]++;
				if (dfSearch(tempVariables.clone(), varIndex))
					return true;
				occurrences[Integer.valueOf(sortedValues[index])]--;
			}
			return false;
		} else {
			variables = tempVariables.clone();
		}

		return true;
	}

	/**
	 * Heuristic used for variable selection.
	 * 
	 * The variable with the shortest domain is selected for assignment.
	 * Elements in the variables domain are chosen left-to-right (no heuristic).
	 * 
	 * @param tempVariables
	 * @return
	 */
	private int singleVarSelection(String[] tempVariables) {

		int varIndex = 0;

		int shortest = Integer.MAX_VALUE;

		for (int i = 0; i < NUMBER_OF_BOXES; i++) {
			if (tempVariables[i].length() > 1
					&& tempVariables[i].length() < shortest) {
				shortest = tempVariables[i].length();
				varIndex = i;
			}
		}

		return varIndex;
	}

	/**
	 * NOT DONE NEEDS FIXING
	 * @param unsorted
	 * @return
	 */
	
	private String[] sortDomainByOccurrence(String[] unsorted) {
		String[] sorted = new String[unsorted.length];
		
		int[] occurrenceCount = new int[unsorted.length];
		
		for (int i = 0; i < unsorted.length; i++) {
			int value = Integer.valueOf(unsorted[i]);
			int occurrence = occurrences[value];
			
		}
		
		return sorted;
	}
	
	/**
	 * Loop through all boxes and check all three main constraints. Continue
	 * until no variable domains can be further reduced. This indicates either
	 * that the puzzle has been solved or that more advanced solving techniques
	 * are necessary to complete it.
	 */
	private void constraintProp(String[] tempVariables, int assigned) {
		boolean reduced = false;

		for (int index = 0; index < NUMBER_OF_BOXES; index++) {
			if (tempVariables[index].length() == 1)
				continue;

			// If not all boxes need to be checked, then enter this if-box to
			// check if the current box is in the same row/column/region. If it
			// is, check it, if not, then move to the next box, etc.
			if (assigned != CHECK_ALL) {
				String[] loc = locs.get(assigned).split(SPLIT_ALL);
				String[] boxLoc = locs.get(index).split(SPLIT_ALL);

				int row = Integer.valueOf(loc[ROW]);
				int boxRow = Integer.valueOf(boxLoc[ROW]);
				int clm = Integer.valueOf(loc[COLUMN]);
				int boxClm = Integer.valueOf(boxLoc[COLUMN]);
				int reg = Integer.valueOf(loc[REGION]);
				int boxReg = Integer.valueOf(boxLoc[REGION]);

				if (row != boxRow && clm != boxClm && reg != boxReg)
					continue;
			}

			int varLength = tempVariables[index].length();

			basicConstraints(ROW, index, tempVariables);
			basicConstraints(COLUMN, index, tempVariables);
			basicConstraints(REGION, index, tempVariables);
			// more (advanced) constraints...

			if (!reduced && tempVariables[index].length() < varLength)
				reduced = true;
		}

		if (reduced)
			constraintProp(tempVariables, assigned);
	}

	/**
	 * Check if the puzzle has been fully solved. A puzzle has been solved if
	 * for all variables the domain has been reduced to a length of 1.
	 * 
	 * @param tempVariables
	 * @return
	 */
	public boolean isSolved(String[] tempVariables) {
		for (String var : tempVariables) {
			if (var.length() == 1)
				continue;
			return false;
		}
		return true;
	}

	/**
	 * Check if the current assignment contains a variable with no possible
	 * assignments (an empty domain).
	 * 
	 * @param tempVariables
	 * @return
	 */
	public boolean hasEmptyDomain(String[] tempVariables) {
		for (String var : tempVariables) {
			if (!var.isEmpty())
				continue;
			return true;
		}
		return false;
	}

	/**
	 * Retrieve the solution to the current puzzle, if any.
	 * 
	 * @return
	 */
	public String solution() {
		String solution = "";

		if (isSolved(variables)) {
			solution += "Solution is shown below.\n";
		} else {
			solution += "Not (yet) solved. See original puzzle below.\n";
		}

		for (int index = 0; index < NUMBER_OF_BOXES; index++) {
			if (index == 0) {
				solution += variables[index].length() > 1 ? "?"
						: variables[index].length() < 1 ? "."
								: variables[index];
				continue;
			}
			if (index % NUMBER_OF_REGIONS_IN_ROW == 0) {
				solution += "|";
				if (index % NUMBER_OF_BOXES_IN_ROW == 0) {
					solution += "\n";
					if (index
							% (NUMBER_OF_BOXES_IN_ROW * NUMBER_OF_REGIONS_IN_ROW) == 0) {
						solution += "--- --- ---\n";
					}
				}
			}
			solution += variables[index].length() > 1 ? "?" : variables[index]
					.length() < 1 ? "." : variables[index];
		}

		return solution + "|\n";
	}

	/**
	 * Retrieve the solution to the current puzzle, if any.
	 * 
	 * @return
	 */
	public String getCurrentAssignments(String[] tempVariables) {
		String solution = "Current Assignments...\n";

		for (int index = 0; index < NUMBER_OF_BOXES; index++) {
			if (index == 0) {
				solution += tempVariables[index] + " ";
				continue;
			}
			if (index % NUMBER_OF_REGIONS_IN_ROW == 0) {
				solution += "|";
				if (index % NUMBER_OF_BOXES_IN_ROW == 0) {
					solution += "\n";
					if (index
							% (NUMBER_OF_BOXES_IN_ROW * NUMBER_OF_REGIONS_IN_ROW) == 0) {
						solution += "--- --- ---\n";
					}
				}
			}
			solution += tempVariables[index] + " ";
		}

		return solution + "|\n";
	}
}
