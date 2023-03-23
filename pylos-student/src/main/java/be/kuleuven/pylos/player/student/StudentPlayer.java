package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	static int pruningCounter = 0;
	enum Figures { TRI, L, T}
	int depth = 3;
	boolean WANNES = false;

	/** Override Functions **/
	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		Action a = findAction(simulator, board);
		game.removeSphere(a.pylosSphere);
	}
	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		Action a = findAction(simulator, board);
		if(a.pass) game.pass();
		else game.removeSphere(a.pylosSphere);
	}
	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		Action bestAction = findAction(simulator, board);
		game.moveSphere(bestAction.pylosSphere, bestAction.location);
		/* board methods
		 * 	PylosLocation[] allLocations = board.getLocations();
		 * 	PylosSphere[] allSpheres = board.getSpheres();
		 * 	PylosSphere[] mySpheres = board.getSpheres(this);
		 * 	PylosSphere myReserveSphere = board.getReserve(this); */
	}

	/** AI Functions **/
	public ArrayList<Action> generatePossibleActions(PylosBoard board, PylosGameSimulator simulator) {
		ArrayList<Action> possibleActions = new ArrayList<>();
		PylosGameState state = simulator.getState();
		PylosPlayerColor color = simulator.getColor();

		if(state == PylosGameState.MOVE) {
			PylosSphere reserveSphere = board.getReserve(color);
			for (PylosLocation location : board.getLocations()) {
				// Move a sphere from the reserve to an available spot
				if (location.isUsable()) possibleActions.add(new Action(reserveSphere, location, state, color));
				// Move a sphere on the board to a higher location
				for (PylosSphere sphere : board.getSpheres(color)) {
					if(sphere.canMoveTo(location)) possibleActions.add(new Action(sphere, location, state, color));
				}
			}
		}
		else if(state == PylosGameState.REMOVE_FIRST) {
			// remove a sphere in a square
			for(PylosSphere sphere : board.getSpheres(color)){
				if(sphere.canRemove()) possibleActions.add(new Action(sphere, sphere.getLocation(), state, color));
			}
		}
		// Remove Second sphere or pass
		else {
			// Pass
			possibleActions.add(new Action(true));
			// remove a sphere in a square
			for(PylosSphere sphere : board.getSpheres(color)){
				if(sphere.canRemove()) possibleActions.add(new Action(sphere, state, color));
			}
		}
		assert possibleActions.size() != 0: "No possbible actions found";
		return possibleActions;
	}

	public Action findAction(PylosGameSimulator simulator, PylosBoard board) {
		pruningCounter = 0;
		Action bestAction = new Action(Integer.MIN_VALUE); // Best action to take next
		ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions

		// Pruning
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;

		for(Action action: possibleActions) {
			int score = findScore(action, simulator, board, depth, alpha, beta);
			if(bestAction.score < score) {
				bestAction = action;
				bestAction.score = score;
			}
			alpha = max(alpha,score);
		}
		//System.out.println(pruningCounter);
		return bestAction;
	}

	public int findScore(Action action, PylosGameSimulator simulator, PylosBoard board, int depth, int alpha, int beta) {
		// PASS
		if(action.pass) return simulatePass(simulator, board, depth, alpha, beta);
			// REMOVE FIRST
		else if(action.state.equals(PylosGameState.REMOVE_FIRST)) return simulateRemove(action, true, simulator, board, depth, alpha, beta);
			// REMOVE SECOND
		else if(action.state.equals(PylosGameState.REMOVE_SECOND)) return simulateRemove(action, false, simulator, board, depth, alpha, beta);
			// MOVE
		else return simulateMove(action, simulator, board, depth, alpha, beta);
	}
	public int minimax(PylosGameSimulator simulator, PylosBoard board, int depth, int alpha, int beta) {
		pruningCounter++;

		// End of the tree, or end of the game: evaluate the board and return the score
		if(depth==0 || simulator.getState().equals(PylosGameState.COMPLETED) || simulator.getState().equals(PylosGameState.DRAW)) {
			return evaluate(board);
		}

		int bestScore; // Best action to take next
		// Our turn, we want to maximize our score
		if(simulator.getColor() == this.PLAYER_COLOR) {
			bestScore = Integer.MIN_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions
			for(Action a : possibleActions) {
				int bestNextScore = findScore(a, simulator, board, depth, alpha, beta);
				bestScore = max(bestScore, bestNextScore);
				alpha = max(alpha, bestNextScore);
				if(beta <= alpha){
					break;
				}
			}
		}
		// Enemy turn, they want to minimize our score
		else {
			bestScore = Integer.MAX_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions
			for(Action a: possibleActions){
				int bestNextScore = findScore(a, simulator, board, depth, alpha, beta);
				bestScore = min(bestScore, bestNextScore);
				beta = min(beta, bestNextScore);
				if(beta <= alpha){
					break;
				}
			}
		}
		return bestScore;
	}

	/** EVALUATION **/
	public int evaluate(PylosBoard board) {
		// Calculate a value for the board state after a certain move
		int weight_reserves = 20;
		int weight_squares = 15;
		int weight_middleControl = 5;

		PylosPlayerColor playerColor = this.PLAYER_COLOR;
		PylosPlayerColor opponentColor = playerColor.other();

		// Amount of reserves remaining
		int reservesScore = board.getReservesSize(playerColor) -  board.getReservesSize(opponentColor);

		// Amount of Squares
		//int squareScore = getSquares(playerColor, board) - getSquares(opponentColor, board);

		// Amount of balls in center
		int centerScore = getCenters(playerColor, board) - getCenters(opponentColor, board);

		// Amount of T and L figures
		//int figureScore = getFiguresScore(playerColor, board) - getFiguresScore(opponentColor, board);

		// todo: As much balls high

		return weight_reserves*reservesScore /*+ weight_squares*squareScore */+ weight_middleControl*centerScore/* + figureScore*/;
	}
	private int getSquares(PylosPlayerColor playerColor, PylosBoard board) {
		int count = 0;
		PylosSquare[] squares = board.getAllSquares();
		for(PylosSquare square : squares) {
			if(square.isSquare(playerColor)) count++;
		}
		return count;
	}

	private int getCenters(PylosPlayerColor playerColor, PylosBoard board) {
		PylosSphere[] spheres = board.getSpheres(playerColor);
		int count = 0;
		for (PylosSphere sphere : spheres) {
			if(isCentered(sphere, board)) count++;
		}
		return count;
	}

	private boolean isCentered(PylosSphere sphere, PylosBoard board) {
		if(!sphere.isReserve()) {
			PylosLocation location = sphere.getLocation();
			int x = location.X;
			int y = location.Y;
			int size = board.SIZE;
			boolean centeredX = x < size-1 && x > 0;
			boolean centeredY = y < size-1 && y > 0;
			return centeredX && centeredY;
		}
		else return false;
	}


	/** FIGURES **/

	private int getFiguresScore(PylosPlayerColor playerColor, PylosBoard board) {
		int count_L = 0, count_T = 0, count_Tri= 0;
		ArrayList<PylosSphere> spheres = filterReserves(board.getSpheres(playerColor));
		for (PylosSphere possibleCenter : spheres) {
			// Sphere who could potentially form a figure
			ArrayList<PylosSphere> adjacentSpheres_L = new ArrayList<>();
			ArrayList<PylosSphere> adjacentSpheres_T = new ArrayList<>();
			ArrayList<PylosSphere> adjacentSpheres_Tri = new ArrayList<>();

			// Fill in the possible spheres
			for (PylosSphere otherSphere : spheres) {
				if (isAdjacent(2, possibleCenter.getLocation(), otherSphere.getLocation()))
					adjacentSpheres_L.add(otherSphere);
				if (isAdjacent(1, possibleCenter.getLocation(), otherSphere.getLocation())) {
					adjacentSpheres_T.add(otherSphere);
					adjacentSpheres_Tri.add(otherSphere);
				}
			}
			// Count the effective figures
			count_L += assemble(Figures.L, adjacentSpheres_L);
			count_T += assemble(Figures.T, adjacentSpheres_T);
			count_Tri += assemble(Figures.TRI, adjacentSpheres_Tri);
		}
		return calculateFigureScores(count_Tri, count_L, count_T);
	}

	private ArrayList<PylosSphere> filterReserves(PylosSphere[] spheres) {
		ArrayList<PylosSphere> spheresWithoutReserves = new ArrayList<>();
		for(PylosSphere sphere : spheres) {
			if(!sphere.isReserve()) spheresWithoutReserves.add(sphere);
		}
		return spheresWithoutReserves;
	}
	private int calculateFigureScores(int countTri, int countL, int countT) {
		int weight_T = 10;
		int weight_L = 10;
		int weight_Triangle = 2;

		int scoreTriangle = weight_Triangle * countTri;

		// Take in account 1 T has already 2 triangles in it
		int scoreT = (weight_T - weight_Triangle*2) * countT;

		// Take in account 1 L has already 1 triangle in it
		int scoreL = (weight_L-weight_Triangle) * countL;

		return scoreL + scoreT + scoreTriangle;
	}

	// Count the figures which effectively occur
	private int assemble(Figures f, ArrayList<PylosSphere> adjacentSpheres) {
		int count = 0;
		if(adjacentSpheres.size() < 2) return count;
		Set<Integer[]> processedID = new HashSet<>();
		for(PylosSphere sphere1 : adjacentSpheres) {
			for(PylosSphere sphere2 : adjacentSpheres) {
				switch(f) {
					case TRI:
						if(isCornered(sphere1.getLocation(), sphere2.getLocation()) && !hasProcessed2(processedID, new Integer[]{sphere1.ID, sphere2.ID})) {
							count++;
							processedID.add(new Integer[]{sphere1.ID, sphere2.ID});
						}
						break;
					case L:
						for(PylosSphere sphere3 : adjacentSpheres) {
							if (isLshaped(sphere1.getLocation(), sphere2.getLocation(), sphere3.getLocation()) && !hasProcessed3(processedID, new Integer[]{sphere1.ID, sphere2.ID, sphere3.ID})) {
								count++;
								processedID.add(new Integer[]{sphere1.ID, sphere2.ID, sphere3.ID});
							}
						}
						break;
					case T:
						for(PylosSphere sphere3 : adjacentSpheres) {
							if (isTshaped(sphere1.getLocation(), sphere2.getLocation(), sphere3.getLocation()) && !hasProcessed3(processedID, new Integer[]{sphere1.ID, sphere2.ID, sphere3.ID})) {
								count++;
								processedID.add(new Integer[]{sphere1.ID, sphere2.ID, sphere3.ID});
							}
						}
						break;
				}
			}
		}
		return count;
	}

	// todo : Deze 2 in 1 fucntie gieten
	private boolean hasProcessed2(Set<Integer[]> processedCorners, Integer[] current) {
		for(Integer[] combo : processedCorners) {
			if(combo[0] == current[0] && combo[1] == current[1]) return true;
			if(combo[0] == current[1] && combo[1] == current[0]) return true;
		}
		return false;
	}
	private boolean hasProcessed3(Set<Integer[]> processedCorners, Integer[] current) {
		for(Integer[] combo : processedCorners) {
			if(combo[0] == current[0] && combo[1] == current[1] && combo[2] == current[2]) return true;
			if(combo[0] == current[1] && combo[1] == current[2] && combo[2] == current[0]) return true;
			if(combo[0] == current[2] && combo[1] == current[0] && combo[2] == current[1]) return true;
		}
		return false;
	}

	private boolean isLshaped(PylosLocation location, PylosLocation location2, PylosLocation location3) {
		// Different orientations
		boolean corner1 = isCornered(location, location2);
		boolean corner2 = isCornered(location, location3);
		boolean corner3 = isCornered(location2, location3);

		boolean straight1 = isStraight(location, location2);
		boolean straight2 = isStraight(location, location3);
		boolean straight3 = isStraight(location2, location3);

		return (corner1 && (straight2 || straight3)) || (corner2 && (straight1 || straight3)) || (corner3 && (straight1 || straight2));
	}

	private boolean isTshaped(PylosLocation location, PylosLocation location2, PylosLocation location3) {
		// Different orientations
		boolean corner1 = isCornered(location, location2);
		boolean corner2 = isCornered(location, location3);
		boolean corner3 = isCornered(location2, location3);

		return (corner1 && corner2) || (corner2 && corner3) || (corner1 && corner3);
	}

	private boolean isCornered(PylosLocation location, PylosLocation location2) {
		int x = location.X;
		int y = location.Y;

		int x2 = location2.X;
		int y2 = location2.Y;

		// Different orientations
		boolean corner1 = (x2 == x+1 && y2 == y+1);
		boolean corner2 = (x2 == x+1 && y2 == y-1);
		boolean corner3 = (x2 == x-1 && y2 == y+1);
		boolean corner4 = (x2 == x-1 && y2 == y-1);

		return corner1 || corner2 || corner3 || corner4;
	}
	private boolean isStraight(PylosLocation location, PylosLocation location2) {
		int x = location.X;
		int y = location.Y;

		int x2 = location2.X;
		int y2 = location2.Y;

		// Different orientations
		boolean straight1 = (x2 == x && y2 == y+1);
		boolean straight2 = (x2 == x && y2 == y-1);
		boolean straight3 = (x2 == x+1 && y2 == y);
		boolean straight4 = (x2 == x-1 && y2 == y);

		return straight1 || straight2 || straight3 || straight4;
	}


	private boolean isAdjacent(int maxMargin, PylosLocation location, PylosLocation location2) {
		int x = location.X;
		int y = location.Y;
		int z = location.Z;

		int x2 = location2.X;
		int y2 = location2.Y;
		int z2 = location2.Z;

		boolean adj = false;
		for(int i=1; i<=maxMargin; i++) {
			// Different orientations
			boolean adj1 = (x2 == x && y2 == y+i && z2 == z);
			boolean adj2 = (x2 == x+i && y2 == y && z2 == z);
			boolean adj3 = (x2 == x && y2 == y-i && z2 == z);
			boolean adj4 = (x2 == x-i && y2 == y && z2 == z);
			adj = adj || adj1 || adj2 || adj3 || adj4;
		}
		return adj;
	}

	/** SIMULATION **/
	private int simulateRemove(Action action, boolean firstSphere, PylosGameSimulator simulator, PylosBoard board, int depth, int alpha, int beta) {
		int bestScore;
		// Get previous State
		PylosSphere sphere = action.pylosSphere;
		PylosGameState prevState = simulator.getState();
		PylosPlayerColor prevColor = simulator.getColor();
		PylosLocation prevLocation = action.pylosSphere.getLocation();
		// Simulate movement
		simulator.removeSphere(sphere);
		// Recursion - Simulate further movement
		if(WANNES) bestScore = minimax(simulator, board, depth-1, alpha, beta);
		else bestScore = minimax(simulator, board, depth, alpha, beta);
		// Reset board
		if(firstSphere) simulator.undoRemoveFirstSphere(sphere, prevLocation, prevState, prevColor);
		else simulator.undoRemoveSecondSphere(sphere, prevLocation, prevState, prevColor);
		return bestScore;
	}

	private int simulateMove(Action action, PylosGameSimulator simulator, PylosBoard board, int depth, int alpha, int beta) {
		// Get previous State
		PylosGameState prevState = simulator.getState();
		PylosPlayerColor prevColor = simulator.getColor();
		PylosLocation prevLocation = action.pylosSphere.getLocation();
		// Simulate movement
		simulator.moveSphere(action.pylosSphere, action.location);
		// Recursion - Simulate further movement
		int bestScore = minimax(simulator, board, depth-1, alpha, beta);
		// Reset board
		if(prevLocation == null) simulator.undoAddSphere(action.pylosSphere, prevState, prevColor);
		else simulator.undoMoveSphere(action.pylosSphere, prevLocation, prevState, prevColor);
		return bestScore;
	}

	private int simulatePass(PylosGameSimulator simulator, PylosBoard board, int depth, int alpha, int beta) {
		// Get previous State
		PylosGameState prevState = simulator.getState();
		PylosPlayerColor prevColor = simulator.getColor();
		// Simulate movement
		simulator.pass();
		// Recursion - Simulate further movement
		int bestScore = minimax(simulator, board, depth-1, alpha, beta);
		// Reset board
		simulator.undoPass(prevState, prevColor);
		return bestScore;
	}

	public class Action {
		int score;
		boolean pass;
		PylosSphere pylosSphere;
		PylosLocation location;
		PylosGameState state;
		PylosPlayerColor color;
		public Action(PylosSphere pylosSphere, PylosLocation location, PylosGameState state, PylosPlayerColor color) {
			this.pylosSphere = pylosSphere;
			this.location = location;
			this.state = state;
			this.color = color;
		}
		public Action(int score){
			this.score = score;
		}
		public Action(boolean pass){
			this.pass = pass;
		}

		public Action(PylosSphere pylosSphere, PylosGameState state, PylosPlayerColor color) {
			this.pylosSphere = pylosSphere;
			this.state = state;
			this.color = color;
		}
	}
}