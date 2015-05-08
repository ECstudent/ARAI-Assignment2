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

	public static final int NUMBER_OF_PEERS_PER_ROW = NUMBER_OF_BOXES_IN_ROW - 1;
	public static final int NUMBER_OF_PEERS_PER_BOX = 20;

	public static final String SPLIT_ALL = "(?!^)";

	public static final int CHECK_ALL = -1;

	public static final int ROW = 0;
	public static final int COLUMN = 1;
	public static final int REGION = 2;

	// Checks if the puzzle has been solved.
	private boolean isSolved = false;

	// Checks if any of the variables has an empty domain
	private boolean hasEmptyDomain = false;

	// Used for displaying the puzzle as it is being solved.
	private long printDelay = 0;

	// A String array containing the domains for all variables
	private String[] variables;

	// An int array containing the number of assigned occurrences for each value
	private int[] occurrences;

	// First index signifies the variable (zero-indexed). The second index
	// refers
	// to either the ROW, COLUMN or REGION. The last index holds the index of
	// its peers.
	private int[][][] peers;

	// Contains all peers of each box without duplicates (otherwise region peers
	// cause
	// overlap with row and column peers)
	private int[][] peersAll;

	public CSPSolver() {
		setPeers();
	}

	/**
	 * Set the peers (boxes in the same ROW, COLUMN or REGION) for each variable
	 * (box).
	 * 
	 */
	private void setPeers() {
		int peer = 0;
		int peerAll = 0;
		int rowNo = 0;
		int clmNo = 0;

		peers = new int[NUMBER_OF_BOXES][3][NUMBER_OF_PEERS_PER_ROW];
		peersAll = new int[NUMBER_OF_BOXES][NUMBER_OF_PEERS_PER_BOX];

		for (int i = 0; i < NUMBER_OF_BOXES; i++)
			for (int j = 0; j < NUMBER_OF_PEERS_PER_BOX; j++)
				peersAll[i][j] = -1;

		for (int i = 0; i < NUMBER_OF_BOXES; i++) {
			// Set ROWS
			peerAll = 0;
			peer = 0;
			rowNo = i / NUMBER_OF_BOXES_IN_ROW;
			int posRow = rowNo * NUMBER_OF_BOXES_IN_ROW;
			for (int j = posRow; j < posRow + NUMBER_OF_BOXES_IN_ROW; j++) {
				// Exclude self from peers
				if (j == i)
					continue;
				peers[i][ROW][peer++] = j;
				peersAll[i][peerAll++] = j;
			}
			// Set COLUMNS
			peer = 0;
			clmNo = i % NUMBER_OF_BOXES_IN_CLM;
			for (int j = 0; j < NUMBER_OF_BOXES_IN_CLM; j++) {
				int posClm = clmNo + (j * NUMBER_OF_BOXES_IN_CLM);
				// Exclude self from peers
				if (posClm == i)
					continue;
				peers[i][COLUMN][peer++] = posClm;
				peersAll[i][peerAll++] = posClm;
			}
			// Set REGIONS
			peer = 0;
			int regionRow = (rowNo / 3) * 3;
			int regionColumn = (clmNo / 3) * 3;
			for (int j = 0; j < (NUMBER_OF_BOXES_IN_REG / 3); j++) {
				for (int k = 0; k < (NUMBER_OF_BOXES_IN_REG / 3); k++) {
					int curClm = (regionColumn + k);
					int curRow = (regionRow + j);
					int posReg = (curRow * NUMBER_OF_BOXES_IN_REG) + curClm;
					// Exclude self from peers
					if (posReg == i)
						continue;
					peers[i][REGION][peer++] = posReg;
					// Avoid duplicates in peersAll array
					if (curRow != rowNo && curClm != clmNo)
						peersAll[i][peerAll++] = posReg;
				}
			}
		}
	}

	/**
	 * Initialize the variables and domains.
	 * 
	 * Compute the row and column of each variable.
	 */
	private void initVariables(String puzzle) {
		variables = new String[NUMBER_OF_BOXES];
		// occurrences = new int[10];

		for (int index = 0; index < NUMBER_OF_BOXES; index++) {
			if (puzzle.charAt(index) == '.') {
				variables[index] = "123456789";
			} else {
				variables[index] = "" + puzzle.charAt(index);
				// occurrences[Integer.valueOf("" + puzzle.charAt(index))]++;
			}
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
		// Check the actual constraints...
		// If a possible value in the variables domain has already been
		// assigned to another variable in the same ROW, COLUMN or REGION,
		// then it can't possibly be assigned to the current variable and
		// must, therefore, be removed from its domain.
		for (String value : tempVariables[index].split(SPLIT_ALL)) {
			for (int i = 0; i < NUMBER_OF_PEERS_PER_ROW; i++) {
				String peerValues = tempVariables[peers[index][type][i]];

				if (peerValues.length() == 1 && peerValues.contains(value)) {
					tempVariables[index] = tempVariables[index].replace(value,
							"");
				}
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

		isSolved = false;
		hasEmptyDomain = false;

		// long time1 = System.nanoTime();
		initVariables(puzzle);
		// System.out.print("t1:");
		// System.out.println((System.nanoTime() - time1) / 1000);
		// System.out.println(solution());

		// printDelay = System.nanoTime();

		// time1 = System.nanoTime();
		dfSearch(variables.clone(), CHECK_ALL);
		// System.out.print("dfsearch:");
		// System.out.println((System.nanoTime() - time1) / 1000);
		System.out.println(solution());
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

		// if (printDelay + 100000000 < System.nanoTime()) {
		 System.out.println(getCurrentAssignments(tempVariables));
		// printDelay = System.nanoTime();
		// }

		if (hasEmptyDomain) {
			hasEmptyDomain = false;
			System.out.println("returned1");
			return false;
		}

		if (!isSolved) {
			int varIndex = singleVarSelection(tempVariables);
			String domain = tempVariables[varIndex];
			// String[] sortedValues =
			// sortDomainByOccurrence(tempVariables[varIndex]
			// .split(SPLIT_ALL));
			// for (String value : sortedValues) {
			while(domain.length() > 0) {
				int value = getLeastOccurringValue(varIndex,
						domain.split(SPLIT_ALL), tempVariables);
				tempVariables[varIndex] = "" + value;
				// occurrences[Integer.valueOf(value)]++;
				if (dfSearch(tempVariables.clone(), varIndex)) {
					System.out.println("returned2");
					return true;
				}
				// occurrences[Integer.valueOf(value)]--;
				domain = domain.replace("" + value, "");
			}
			System.out.println("returned3");
			return false;
		} else {
			variables = tempVariables.clone();
		}
		System.out.println("returned4");
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
	 * Sorts the domain of the assigned variable by the number of times the
	 * value in the domain has been assigned in the full puzzle starting with
	 * the most occurrences.
	 * 
	 * @param unsorted
	 * @return
	 */
	private String[] sortDomainByOccurrence(String[] unsorted) {
		String tempValue = "";
		int tempOccur = 0;

		String[] sorted = new String[unsorted.length];
		int[] occurrenceCount = new int[unsorted.length];

		for (int i = 0; i < sorted.length; i++)
			sorted[i] = "";

		for (int i = 0; i < unsorted.length; i++) {
			int value = Integer.valueOf(unsorted[i]);
			int occurrence = occurrences[value];
			for (int j = 0; j < unsorted.length; j++) {
				if (occurrence >= occurrenceCount[j]) {
					// Sort the occurrences of elements...
					tempOccur = occurrenceCount[j];
					occurrenceCount[j] = occurrence;
					occurrence = tempOccur;
					// to sort the elements in the domain
					tempValue = sorted[j];
					sorted[j] = "" + value;
					if (tempValue.equals(""))
						break;
					value = Integer.valueOf(tempValue);
				}
			}
		}

		return sorted;
	}

	/**
	 * Implementation of least constraining value selection.
	 * 
	 * @param varIndex
	 * @param values
	 * @return
	 */
	private int getLeastOccurringValue(int varIndex, String[] values,
			String[] tempVariables) {
		int leastI = Integer.MAX_VALUE;
		int leastV = 0;
		int valueCount = 0;

		for (int i = 0; i < values.length; i++) {
			valueCount = 0;
			for (int peer = 0; peer < NUMBER_OF_PEERS_PER_BOX; peer++) {
				if (tempVariables[peersAll[varIndex][peer]].contains(values[i]))
					valueCount++;
			}
			if (valueCount < leastI) {
				leastI = valueCount;
				leastV = Integer.valueOf(values[i]);
			}
		}
		return leastV;
	}

	/**
	 * Loop through all boxes and check all three main constraints. Continue
	 * until no variable domains can be further reduced. This indicates either
	 * that the puzzle has been solved or that more advanced solving techniques
	 * are necessary to complete it.
	 */
	private void constraintProp(String[] tempVariables, int assigned) {
		boolean reduced = false;
		boolean solved = true;
		boolean peersSolved = true;

		if (assigned == CHECK_ALL) {
			peersSolved = false;
			for (int index = 0; index < NUMBER_OF_BOXES; index++) {
				int varLength = tempVariables[index].length();

				// Only has one value in its domain
				if (varLength == 1)
					continue;

				// Occurrence of empty domain, terminate propagation
				if (varLength == 0) {
					hasEmptyDomain = true;
					return;
				}

				// Not all variables have been assigned values
				solved = false;

				basicConstraints(ROW, index, tempVariables);
				basicConstraints(COLUMN, index, tempVariables);
				basicConstraints(REGION, index, tempVariables);
				// more (advanced) constraints...

				if (!reduced && tempVariables[index].length() < varLength)
					reduced = true;
			}
		} else {
			for (int index = 0; index < NUMBER_OF_PEERS_PER_BOX; index++) {
				int peer = peersAll[assigned][index];
				int varLength = tempVariables[peer].length();

				// Only has one value in its domain
				if (varLength == 1)
					continue;

				// Occurrence of empty domain, terminate propagation
				if (varLength == 0) {
					hasEmptyDomain = true;
					return;
				}

				// Not all peers have been assigned values
				peersSolved = false;
				solved = false;

				basicConstraints(ROW, peer, tempVariables);
				basicConstraints(COLUMN, peer, tempVariables);
				basicConstraints(REGION, peer, tempVariables);
				// more (advanced) constraints...

				if (!reduced && tempVariables[peer].length() < varLength)
					reduced = true;
			}
		}

		// All peers have been assigned values
		// Check all boxes to see of the full puzzle has been solved
		if (peersSolved) {
			for (int index = 0; index < NUMBER_OF_BOXES; index++) {
				if (tempVariables[index].length() == 1)
					continue;

				// Not all puzzles have been solved
				solved = false;
				break;
			}
		}

		if (reduced)
			constraintProp(tempVariables, assigned);
		else
			isSolved = solved;
	}

	/**
	 * Retrieve the solution to the current puzzle, if any.
	 * 
	 * @return
	 */
	public String solution() {
		String solution = "";

		if (isSolved) {
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
