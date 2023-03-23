package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	int depth = 3;
	boolean WANNES = true;

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

		Action bestAction = new Action(Integer.MIN_VALUE); // Best action to take next
		ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions
		// Depth == 1, further actions don't need to be saved, only the board state passes alongd
		for(Action action: possibleActions) {
			int score = findScore(action, simulator, board, depth);
			if(bestAction.score < score) bestAction = action;
		}
		return bestAction;
	}

	public int findScore(Action action, PylosGameSimulator simulator, PylosBoard board, int depth) {
		// PASS
		if(action.pass) return simulatePass(simulator, board, depth);
		// REMOVE FIRST
		else if(action.state.equals(PylosGameState.REMOVE_FIRST)) return simulateRemove(action, true, simulator, board, depth);
		// REMOVE SECOND
		else if(action.state.equals(PylosGameState.REMOVE_SECOND)) return simulateRemove(action, false, simulator, board, depth);
		// MOVE
		else return simulateMove(action, simulator, board, depth);
	}
	public int minimax(PylosGameSimulator simulator, PylosBoard board, int depth) {
		// Todo: Pruning

		// End of the tree, or end of the game: evaluate the board and return the score
		if(depth==0 || simulator.getState().equals(PylosGameState.COMPLETED) || simulator.getState().equals(PylosGameState.DRAW)) {
			return evaluate(board);
		}

		int bestScore; // Best action to take next
		// Our turn, we want to maximize our score
		if(simulator.getColor() == this.PLAYER_COLOR) {
			bestScore = Integer.MIN_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions
			for(Action action : possibleActions) {
				int bestNextScore = findScore(action, simulator, board, depth);
				if(bestScore < bestNextScore) bestScore = bestNextScore;
			}
		}
		// Enemy turn, they want to minimize our score
		else {
			bestScore = Integer.MAX_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions
			for(Action a: possibleActions){
				int bestNextScore = findScore(a, simulator, board, depth);
				if(bestScore > bestNextScore) bestScore = bestNextScore;
			}
		}
		return bestScore;
	}

	/** EVALUATION **/
	public int evaluate(PylosBoard board) {
		// Calculate a value for the board state after a certain move
		int weight_reserves = 1000;
		int weight_squares = 0;
		int weight_middleControl = 0;

		PylosPlayerColor playerColor = this.PLAYER_COLOR;
		PylosPlayerColor opponentColor = playerColor.other();

		// Amount of reserves remaining
		int reservesScore = board.getReservesSize(playerColor) -  board.getReservesSize(opponentColor);

		// Amount of Squares
		int squareScore = getSquares(playerColor, board) - getSquares(opponentColor, board);

		// Amount of balls in center
		int centerScore = getCenters(playerColor, board) - getCenters(opponentColor, board);

		// Amount of T and L figures
		int figureScore = getFigures(playerColor, board) - getFigures(opponentColor, board);

		// todo: As much balls high

		return weight_reserves*reservesScore + weight_squares*squareScore + weight_middleControl*centerScore + figureScore;
	}
	private int getSquares(PylosPlayerColor playerColor, PylosBoard board) {
		int count = 0;
		PylosSquare[] squares = board.getAllSquares();
		for(PylosSquare square : squares) {
			if(square.isSquare(playerColor)) count++;
		}
		return count;
	}

	private int getTriangles(PylosPlayerColor playerColor, PylosBoard board) {
		int count = 0;
		ArrayList<PylosSphere> spheres = filterReserves(board.getSpheres(playerColor));
		for (PylosSphere possibleCenter : spheres) {
			ArrayList<PylosSphere> adjacentSpheres = new ArrayList<>();
			for (PylosSphere otherSphere : spheres) {
				if (isAdjacent_Triangle(possibleCenter.getLocation(), otherSphere.getLocation())) adjacentSpheres.add(otherSphere);
			}
			count += assembleTriangles(adjacentSpheres);
		}
		return count;
	}

	private int assembleTriangles(ArrayList<PylosSphere> adjacentSpheres) {
		int count = 0;
		if(adjacentSpheres.size() < 2) return count;
		Set<Integer[]> processedCorners = new HashSet<>();
		for(PylosSphere sphere1 : adjacentSpheres) {
			for(PylosSphere sphere2 : adjacentSpheres) {
				if(isCornered(sphere1.getLocation(), sphere2.getLocation()) && !hasProcessed(processedCorners, new Integer[]{sphere1.ID, sphere2.ID})) {
					count++;
					processedCorners.add(new Integer[]{sphere1.ID, sphere2.ID});
				}
			}
		}
		return count;
	}

	private boolean hasProcessed(Set<Integer[]> processedCorners, Integer[] current) {
		for(Integer[] combo : processedCorners) {
			if(combo[0] == current[0] && combo[1] == current[1]) return true;
			if(combo[0] == current[1] && combo[1] == current[0]) return true;
		}
		return false;
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

	private ArrayList<PylosSphere> filterReserves(PylosSphere[] spheres) {
		ArrayList<PylosSphere> spheresWithoutReserves = new ArrayList<>();
		for(PylosSphere sphere : spheres) {
			if(!sphere.isReserve()) spheresWithoutReserves.add(sphere);
		}
		return spheresWithoutReserves;
	}

	private boolean isAdjacent_Triangle(PylosLocation location, PylosLocation location2) {
		int x = location.X;
		int y = location.Y;
		int z = location.Z;

		int x2 = location2.X;
		int y2 = location2.Y;
		int z2 = location2.Z;

		// Different orientations
		boolean adj1 = (x2 == x && y2 == y+1 && z2 == z);
		boolean adj2 = (x2 == x+1 && y2 == y && z2 == z);
		boolean adj3 = (x2 == x && y2 == y-1 && z2 == z);
		boolean adj4 = (x2 == x-1 && y2 == y && z2 == z);

		return adj1 || adj2 || adj3 || adj4;
	}

	private int getFigures(PylosPlayerColor playerColor, PylosBoard board) {
		return getFigures_T(playerColor, board) + getFigures_L(playerColor, board) + getTriangles(playerColor, board);
	}

	private int getFigures_L(PylosPlayerColor playerColor, PylosBoard board) {
		ArrayList<PylosSphere> spheres = filterReserves(board.getSpheres(playerColor));
		int count = 0;
		for (PylosSphere possibleCenter : spheres) {
			ArrayList<PylosSphere> adjacentSpheres = new ArrayList<>();
			for (PylosSphere otherSphere : spheres) {
				if (isAdjacent_Triangle(possibleCenter.getLocation(), otherSphere.getLocation())) adjacentSpheres.add(otherSphere);
			}
			count += assembleTriangles(adjacentSpheres);
		}
		return count;
		// todo : L Filteren driehoeken
	}
	private int getFigures_T(PylosPlayerColor playerColor, PylosBoard board) {
		// todo: implement Figure_T
		// todo : T Filteren driehoeken (Bevat 2 driehoeken)
		return 0;
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


	/** SIMULATION **/
	private int simulateRemove(Action action, boolean firstSphere, PylosGameSimulator simulator, PylosBoard board, int depth) {
		int bestScore;
		// Get previous State
		PylosSphere sphere = action.pylosSphere;
		PylosGameState prevState = simulator.getState();
		PylosPlayerColor prevColor = simulator.getColor();
		PylosLocation prevLocation = action.pylosSphere.getLocation();
		// Simulate movement
		simulator.removeSphere(sphere);
		// Recursion - Simulate further movement
		if(WANNES) bestScore = minimax(simulator, board, depth-1);
		else bestScore = minimax(simulator, board, depth);
		// Reset board
		if(firstSphere) simulator.undoRemoveFirstSphere(sphere, prevLocation, prevState, prevColor);
		else simulator.undoRemoveSecondSphere(sphere, prevLocation, prevState, prevColor);
		return bestScore;
	}

	private int simulateMove(Action action, PylosGameSimulator simulator, PylosBoard board, int depth) {
		// Get previous State
		PylosGameState prevState = simulator.getState();
		PylosPlayerColor prevColor = simulator.getColor();
		PylosLocation prevLocation = action.pylosSphere.getLocation();
		// Simulate movement
		simulator.moveSphere(action.pylosSphere, action.location);
		// Recursion - Simulate further movement
		int bestScore = minimax(simulator, board, depth-1);
		// Reset board
		if(prevLocation == null) simulator.undoAddSphere(action.pylosSphere, prevState, prevColor);
		else simulator.undoMoveSphere(action.pylosSphere, prevLocation, prevState, prevColor);
		return bestScore;
	}

	private int simulatePass(PylosGameSimulator simulator, PylosBoard board, int depth) {
		// Get previous State
		PylosGameState prevState = simulator.getState();
		PylosPlayerColor prevColor = simulator.getColor();
		// Simulate movement
		simulator.pass();
		// Recursion - Simulate further movement
		int bestScore = minimax(simulator, board, depth-1);
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