package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {

	// We simulate every move and calculate which move is better
	// 3 deep
	// After finding the right move we delete the whole tree

	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		Stack<Action> previousMoves = new Stack<>();
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		Action bestMove = findBestMove(previousMoves, null, board, game, this.PLAYER_COLOR, simulator);

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
		/* game methods
			* game.removeSphere(mySphere); */
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		/* game methods
			* game.removeSphere(mySphere);
			* game.pass() */
	}

	public int evaluate(PylosBoard board){
		// Calculate a value for the board state after a certain move
		// Todo evaluate the board state and give back a score
		return 0;
	}

	public ArrayList<Action> generatePossibleActions(Action a){
		// Keep in mind the color of the past action and the next kind of move
		// Todo generate all possible moves from the boardstate of this action (MOVE & REMOVES)
		return null;
	}

	public Action findBestMove(Action previousAction, PylosGameIF game, PylosPlayerColor color,
							   PylosGameSimulator simulator, int depth) {
		// Todo simulate the action
		// Todo save previous actions
		// Todo add pruning

		// End of the tree, or end of the game: evaluate the board and return the score
		if(depth==0 || game.isFinished()){
			previousAction.score = evaluate(previousAction.board);

			// Todo Undo the simulated action

			return previousAction;
		}

		Action bestAction; // Best action to take next
		ArrayList<Action> possibleActions = generatePossibleActions(previousAction);

		// Our turn, we want to maximize our score
		if(color==this.PLAYER_COLOR){
			bestAction = new Action(Integer.MIN_VALUE);
			for(Action a: possibleActions){
				Action bestNextAction;
				// REMOVE
				if(a.state.equals(PylosGameState.REMOVE_FIRST) || a.state.equals(PylosGameState.REMOVE_SECOND)){
					// Change depth -1 to depth for the WANNES MANIER
					bestNextAction = findBestMove(a,game,color,simulator,depth-1);
				}
				// MOVE
				else {
					bestNextAction = findBestMove(a,game,this.OTHER.PLAYER_COLOR,simulator,depth-1);
				}
				if(bestAction.score < bestNextAction.score)bestAction = bestNextAction;
			}
		}

		// Enemy turn, they want to minimize our score
		else {
			bestAction = new Action(Integer.MAX_VALUE);
			for(Action a: possibleActions){
				Action bestNextAction;
				// REMOVE
				if(a.state.equals(PylosGameState.REMOVE_FIRST) || a.state.equals(PylosGameState.REMOVE_SECOND)){
					// Change depth -1 to depth for the WANNES MANIER
					bestNextAction = findBestMove(a,game,color,simulator,depth-1);
				}
				// MOVE
				else {
					bestNextAction = findBestMove(a,game,this.PLAYER_COLOR,simulator,depth-1);
				}
				if(bestAction.score > bestNextAction.score)bestAction = bestNextAction;
			}
		}
		return bestAction;


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

	private void moveFromReserve(PylosGameSimulator simulator, Stack<Action> previousMoves, PylosLocation bl, PylosBoard board, PylosGameState state, PylosPlayerColor color) {

		PylosSphere pylosSphere = board.getReserve(this);
		simulator.moveSphere(pylosSphere, bl);
		previousMoves.add(new Action(pylosSphere, bl, state, color));
	}

	public void takeBack() {

	}
	public void possibleTakeBack(Stack<Action> previousMoves, Action bestMove, PylosBoard board, PylosGameIF game,
								 PylosPlayerColor color, PylosGameSimulator simulator) {
		// CHECK IF POSSIBLE SQUARE1
		if(game.removeSphere() == 0) {

		}

		// recursion happens here
	}

	public class Action {
		int score;
		PylosSphere pylosSphere;
		PylosLocation location;
		PylosGameState state;
		PylosPlayerColor color;
		PylosBoard board;

		public Action(PylosSphere pylosSphere, PylosLocation location, PylosGameState state, PylosPlayerColor color, PylosBoard board) {
			this.pylosSphere = pylosSphere;
			this.location = location;
			this.state = state;
			this.color = color;
			this.board = board;
		}
		public Action(int score){
			this.score = score;
		}

	}


}