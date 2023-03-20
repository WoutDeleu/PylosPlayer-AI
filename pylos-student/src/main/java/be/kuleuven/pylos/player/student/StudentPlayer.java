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
	// We simulate every move and calculate which move is better
	// 3 deep
	// After finding the right move we delete the whole tree
	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		Action a = findAction(game, board);
		// Todo execute action
		/* board methods
			* 	PylosLocation[] allLocations = board.getLocations();
			* 	PylosSphere[] allSpheres = board.getSpheres();
			* 	PylosSphere[] mySpheres = board.getSpheres(this);
			* 	PylosSphere myReserveSphere = board.getReserve(this); */

		/* game methods
			* game.moveSphere(myReserveSphere, allLocations[0]); */

	}

	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		Action a = findAction(game, board);
		// Todo execute action
		/* game methods
			* game.removeSphere(mySphere); */
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		Action a = findAction(game, board);
		// Todo execute action
		/* game methods
			* game.removeSphere(mySphere);
			* game.pass() */
	}

	public Action findAction(PylosGameIF game, PylosBoard board){
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);

		Action bestAction = new Action(Integer.MIN_VALUE); // Best action to take next
		ArrayList<Action> possibleActions = generatePossibleActions(board,game.getState(), this.PLAYER_COLOR); // Generate all possible actions

		// Depth == 1, further actions don't need to be saved, only the board state passes along
		for(Action a: possibleActions){
			// Todo simulate the action to the board
			int bestNextScore = minimax(game,this.PLAYER_COLOR,simulator,board, depth-1);
			if(bestAction.score < bestNextScore)bestAction = a;

		}
		return bestAction;
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
				int bestNextScore;
				// REMOVE
				if(a.state.equals(PylosGameState.REMOVE_FIRST) || a.state.equals(PylosGameState.REMOVE_SECOND)){
					// Change depth -1 to depth for the WANNES MANIER
					// Todo simulate the action to the board and change the game state
					bestNextScore = minimax(game,color,simulator,board, depth-1);
				}
				// MOVE
				else {
					// Todo simulate the action to the board and change the game state
					bestNextScore = minimax(game,this.OTHER.PLAYER_COLOR,simulator,board, depth-1);
				}
				if(bestScore < bestNextScore)bestScore = bestNextScore;
				// Todo Undo the simulated action
			}
		}

		// Enemy turn, they want to minimize our score
		else {
			bestScore = Integer.MAX_VALUE;
			ArrayList<Action> possibleActions = generatePossibleActions(board,game.getState(),color); // Generate all possible actions
			for(Action a: possibleActions){
				int bestNextScore;
				// REMOVE
				if(a.state.equals(PylosGameState.REMOVE_FIRST) || a.state.equals(PylosGameState.REMOVE_SECOND)){
					// Change depth -1 to depth for the WANNES MANIER
					// Todo simulate the action to the board and change the game state
					bestNextScore = minimax(game,color,simulator,board,depth-1);
				}
				// MOVE
				else {
					// Todo simulate the action to the board and change the game state
					bestNextScore = minimax(game,this.PLAYER_COLOR,simulator,board,depth-1);
				}
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