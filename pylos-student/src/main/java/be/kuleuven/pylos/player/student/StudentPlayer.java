package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	/** Override Functions **/
	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		Action a = findAction(simulator, board);
		simulator.removeSphere(a.pylosSphere);
	}
	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		Action a = findAction(simulator, board);
		if(a.pass) simulator.pass();
		else simulator.removeSphere(a.pylosSphere);
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
	int depth = 3;
	boolean WANNES = true;


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
				if(sphere.canRemove()) possibleActions.add(new Action(sphere, state, color));
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
		return possibleActions;
	}

	public Action findAction(PylosGameSimulator simulator, PylosBoard board) {

		Action bestAction = new Action(Integer.MIN_VALUE); // Best action to take next
		ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions

		// Depth == 1, further actions don't need to be saved, only the board state passes along
		for(Action action: possibleActions) {
			int bestNextScore = findScore(action, simulator, board, depth);
			if(bestAction.score < bestNextScore) bestAction = action;
		}
		return bestAction;
	}

	public int findScore(Action action, PylosGameSimulator simulator, PylosBoard board, int depth) {
		int bestScore;
		// PASS
		if(action.pass) {
			return simulatePass(simulator, board, depth);
		}
		// REMOVE
		else if(action.state.equals(PylosGameState.REMOVE_FIRST)) {
			// If the player removed his first sphere, he can remove action second one
			return simulateRemove(action, true, simulator, board, depth);

		}
		// MOVE
		else {
			// Todo simulate the action to the board and change the game state
			if(action.state.equals(PylosGameState.MOVE) && detectSquare(board, simulator.getColor())) {
				// After creating action square the player can remove spheres instantly
				System.out.println("Woop");
				bestScore = minimax(simulator,board, depth-1);
			}
			// If the player passes, removes action second square or moves action sphere without creating action square,
			// the turn goes to the opponent
			else{
				bestScore = simulatePass(simulator, board, depth);
			}
		}
		return bestScore;
	}
	public int minimax(PylosGameSimulator simulator, PylosBoard board, int depth) {
		// Todo(extra) add pruning

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
				// Todo Undo the simulated action
			}
		}

		// Enemy turn, they want to minimize our score
		else {
			bestScore = Integer.MAX_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board, simulator); // Generate all possible actions
			for(Action a: possibleActions){
				int bestNextScore = findScore(a, simulator, board, depth);
				if(bestScore > bestNextScore) bestScore = bestNextScore;
				// Todo Undo the simulated action
			}
		}

		return bestScore;
	}

	public int evaluate(PylosBoard board){
		// Calculate a value for the board state after a certain move
		// Todo evaluate the board state and give back a score
		return 0;
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
		simulator.undoMoveSphere(action.pylosSphere, prevLocation, prevState, prevColor);
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


	/** Util **/
	public boolean detectSquare(PylosBoard board, PylosPlayerColor color) {
		// Check if there's a square on the board from this player's color
//		for(PylosSquare ps : board.getAllSquares()){
//			if(ps.isSquare(color))return true;
//		}
		return false;
	}

	private void moveFromReserve(PylosGameSimulator simulator, Stack<Action> previousMoves, PylosLocation bl, PylosBoard board, PylosGameState state, PylosPlayerColor color) {

		PylosSphere pylosSphere = board.getReserve(this);
		simulator.moveSphere(pylosSphere, bl);
	}

	public void takeBack() {

	}
	public void possibleTakeBack(Stack<Action> previousMoves, Action bestMove, PylosBoard board, PylosGameIF game,
								 PylosPlayerColor color, PylosGameSimulator simulator) {
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