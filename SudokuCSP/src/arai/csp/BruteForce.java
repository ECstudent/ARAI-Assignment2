package arai.csp;

import java.util.HashMap;

public class BruteForce {

	public static final int NUMBER_OF_BOXES = 81;

	// True if the puzzle has been solved
	private boolean solved = false;

	// A String array containing all variables
	private String[] variables;
	
	private String[] endSolution;

	// The Sudoku puzzle to be solved, unassigned variables are denoted with a
	// point ('.').
	private String puzzle;
	
	private void createVariableList() {
		variables = new String[NUMBER_OF_BOXES];

		for (int i = 0; i < NUMBER_OF_BOXES; i++) {
			if (puzzle.charAt(i) == '.') {
				variables[i] = "0";
			} else {
				variables[i] = "" + puzzle.charAt(i);
			}
		}
	}

	public void solve(String puzzle) {
		this.puzzle = puzzle;
		
		createVariableList();

		String[] solution = recursiveBruteForce(variables, 0);
		
		// print output sudoku
		for (int i = 0; i < NUMBER_OF_BOXES; i++){
			System.out.print(solution[i]);
			if ((i+1)%3 == 0 && (i+1)%9 != 0){
				System.out.print("|");
			}
			if ((i+1)%27 == 0 && i < 70){
				System.out.println("");
				System.out.print("-----------");
			}
			if ((i+1)%9 == 0){
				System.out.println("");
			}
		}
		
	}

	public String[] recursiveBruteForce(String[] varList, int depth){
//		// output incoming sudoku
//		System.out.println("");
//		System.out.print("INCOMING AT DEPTH: ");
//		System.out.println(depth);
//		for (int i = 0; i < NUMBER_OF_BOXES; i++){
//			System.out.print(varList[i]);
//			if ((i+1)%3 == 0 && (i+1)%9 != 0){
//				System.out.print("|");
//			}
//			if ((i+1)%27 == 0 && i < 70){
//				System.out.println("");
//				System.out.print("-----------");
//			}
//			if ((i+1)%9 == 0){
//				System.out.println("");
//			}
//		}
		
		String[] solution = new String[NUMBER_OF_BOXES];
		for (int i = 0; i < NUMBER_OF_BOXES; i++){
			solution[i] = varList[i];
		}
		
		if (depth >= NUMBER_OF_BOXES){
			this.endSolution = varList;
			this.solved = true;
			solution = this.endSolution;
			return solution;
		}		
		
		if ((varList[depth]) != "0"){
			solution = recursiveBruteForce(varList, (depth+1));
		} else {
			for (int i = 1; i <= 9; i++){
				if(this.solved){
//					System.out.println("-->SOLVED");
					return this.endSolution;
				}
				if (possibleSetting(varList, i, depth)){
					varList[depth] = Integer.toString(i);
					solution = recursiveBruteForce(varList, (depth+1));
				}
				if(this.solved){
//					System.out.println("-->SOLVED");
					return this.endSolution;
				}
			}
			varList[depth] = "0";
			solution[depth] = "0";
		}
		// output returning sudoku
//		System.out.println("------------>RETURNING---------->");		
//		System.out.print("FROM DEPTH: ");
//		System.out.println(depth);
//		for (int i = 0; i < NUMBER_OF_BOXES; i++){
//			System.out.print(varList[i]);
//			if ((i+1)%3 == 0 && (i+1)%9 != 0){
//				System.out.print("|");
//			}
//			if ((i+1)%27 == 0 && i < 70){
//				System.out.println("");
//				System.out.print("-----------");
//			}
//			if ((i+1)%9 == 0){
//				System.out.println("");
//			}
//		}
		return solution;
	}
	
	public boolean possibleSetting(String[] varList, int number, int position){
		if(inRow(varList, number, position)){
			return false;
		}
		if(inColumn(varList, number, position)){
			return false;
		}
		if(inRegion(varList, number, position)){
			return false;
		}
		
		return true;
	}
	
	public boolean inRow(String[] varList, int number, int position){
		int rowNo = position/9;
		int startPosition = rowNo * 9;
		for (int i = startPosition; i < startPosition+9; i++){
			if(varList[i].equals(""+number)){
				return true;
			}
		}
				
		return false;
	}
	
	public boolean inColumn(String[] varList, int number, int position){
		int columnNo = position % 9;
		
		for (int i = 0; i < 9; i++){
			int checkPlace = columnNo + i * 9;
			if(varList[checkPlace].equals(""+number)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean inRegion(String[] varList, int number, int position){
		int rowNo = position/9;
		int columnNo = position % 9;
		int regionTopRow = (rowNo / 3) * 3;
		int regionLeftColumn = (columnNo / 3) * 3;
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				int curColumn = (regionLeftColumn + j);
				int curRow = (regionTopRow + i);
				int checkPlace = curRow * 9 + curColumn;
				if(varList[checkPlace].equals(""+number)){
					return true;
				}				
			}
		}
		
		return false;
	}
	
}






