package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	int depth = 3;
	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		Action a = findAction(game, board);
		game.moveSphere(a.pylosSphere, a.location);
		/* board methods
			* 	PylosLocation[] allLocations = board.getLocations();
			* 	PylosSphere[] allSpheres = board.getSpheres();
			* 	PylosSphere[] mySpheres = board.getSpheres(this);
			* 	PylosSphere myReserveSphere = board.getReserve(this); */
	}

	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		Action a = findAction(game, board);
		game.removeSphere(a.pylosSphere);
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		Action a = findAction(game, board);
		if(a.pylosSphere != null)game.removeSphere(a.pylosSphere);
		else game.pass();
	}

	public Action findAction(PylosGameIF game, PylosBoard board){
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);

		Action bestAction = new Action(Integer.MIN_VALUE); // Best action to take next
		ArrayList<Action> possibleActions = generatePossibleActions(board,game.getState(), this.PLAYER_COLOR); // Generate all possible actions

		// Depth == 1, further actions don't need to be saved, only the board state passes along
		for(Action a: possibleActions){
			int bestNextScore = findNextScore(a, game, this.PLAYER_COLOR, simulator, board, depth);
			if(bestAction.score < bestNextScore)bestAction = a;
			// Todo Undo the simulated action
		}
		return bestAction;
	}

	public int findNextScore(Action a, PylosGameIF game, PylosPlayerColor color,
						 PylosGameSimulator simulator, PylosBoard board, int depth){
		int bestNextScore;
		boolean pass = false;
		// REMOVE
		if(a.state.equals(PylosGameState.REMOVE_FIRST) && !pass){
			// If the player removed his first sphere, he can remove a second one

			// Change depth to depth -1 for the WANNES MANIER
			// Todo simulate the action to the board and change the game state
			bestNextScore = minimax(game,color,simulator,board, depth-1);
		}
		// MOVE
		else {
			// Todo simulate the action to the board and change the game state

			if(a.state.equals(PylosGameState.MOVE) && detectSquare(board, color)){
				// After creating a square the player can remove spheres instantly
				bestNextScore = minimax(game,color,simulator,board, depth-1);
			}
			// If the player passes, removes a second square or moves a sphere without creating a square,
			// the turn goes to the opponent
			if(color == PylosPlayerColor.DARK) color = PylosPlayerColor.LIGHT;
			else color = PylosPlayerColor.DARK;
			bestNextScore = minimax(game,color,simulator,board, depth-1);
		}
		return bestNextScore;
	}

	public int minimax(PylosGameIF game, PylosPlayerColor color,
							   PylosGameSimulator simulator, PylosBoard board, int depth) {
		// Todo(extra) add pruning

		// End of the tree, or end of the game: evaluate the board and return the score
		if(depth==0 || game.isFinished()){
			return evaluate(board);
		}

		int bestScore; // Best action to take next

		// Our turn, we want to maximize our score
		if(color==this.PLAYER_COLOR){
			bestScore = Integer.MIN_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board,game.getState(),color); // Generate all possible actions
			for(Action a: possibleActions){
				int bestNextScore = findNextScore(a, game, this.PLAYER_COLOR, simulator, board, depth);
				if(bestScore < bestNextScore)bestScore = bestNextScore;
				// Todo Undo the simulated action
			}
		}

		// Enemy turn, they want to minimize our score
		else {
			bestScore = Integer.MAX_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board,game.getState(),color); // Generate all possible actions
			for(Action a: possibleActions){
				int bestNextScore = findNextScore(a, game, this.PLAYER_COLOR, simulator, board, depth);
				if(bestScore > bestNextScore)bestScore = bestNextScore;
				// Todo Undo the simulated action
			}
		}

		return bestScore;


//		for(PylosLocation bl : board.getLocations()) {
//			if(bl.isUsable()) {
//				PylosSphere pylosSphere;
//				// Move sphere from reserve
//				if (0 < board.getReservesSize(this)) {
//
//					moveFromReserve(simulator, previousMoves, bl, board, game.getState(), color);
//					possibleTakeBack(previousMoves, bestMove, board, game,
//							color, simulator);
//					// Recursion
//					// kan zijn dat het nog altijd uw beurt is!!!!!
//
//					// reset board
//				}
//				// Move sphere on the board
//				if () {
//					// Do move
//
//					// Recursion
//					// kan zijn dat het nog altijd uw beurt is!!!!!
//
//					// reset board
//				}
//			}
//			// Remove sphere
//			if() {
//				// Do move
//
//				// Recursion
//				// kan zijn dat het nog altijd uw beurt is!!!!!
//
//				// reset board
//			}
//		}
//
//		return bestMove;
	}

	public int evaluate(PylosBoard board){
		// Calculate a value for the board state after a certain move
		// Todo evaluate the board state and give back a score
		return 0;
	}

	public ArrayList<Action> generatePossibleActions(PylosBoard board, PylosGameState state, PylosPlayerColor color){
		// Keep in mind if the current state is REMOVE, only a select amount of actions are possible (remove2, pass)
		// Todo generate all possible actions from this board + state + color (MOVES & REMOVES)
		return null;
	}

	public boolean detectSquare(PylosBoard board, PylosPlayerColor color){
		// Todo create function
		// Check if there's a square on the board from this player's color
		return false;
	}

	private void moveFromReserve(PylosGameSimulator simulator, Stack<Action> previousMoves, PylosLocation bl, PylosBoard board, PylosGameState state, PylosPlayerColor color) {

		PylosSphere pylosSphere = board.getReserve(this);
		simulator.moveSphere(pylosSphere, bl);
		//previousMoves.add(new Action(pylosSphere, bl, state, color));
	}

	public void takeBack() {

	}
	public void possibleTakeBack(Stack<Action> previousMoves, Action bestMove, PylosBoard board, PylosGameIF game,
								 PylosPlayerColor color, PylosGameSimulator simulator) {
		// CHECK IF POSSIBLE SQUARE1
//		if(game.removeSphere() == 0) {
//
//		}

		// recursion happens here
	}

	public class Action {
		int score;
		PylosSphere pylosSphere;
		PylosLocation location;
		PylosGameState state;
		PylosPlayerColor color;


		public Action(PylosSphere pylosSphere, PylosLocation location, PylosGameState state, PylosPlayerColor color, PylosBoard board) {
			this.pylosSphere = pylosSphere;
			this.location = location;
			this.state = state;
			this.color = color;
		}
		public Action(int score){
			this.score = score;
		}

	}


}