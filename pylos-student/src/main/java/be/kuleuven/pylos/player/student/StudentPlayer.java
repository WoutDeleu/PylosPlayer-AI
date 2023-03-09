package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.player.PylosPlayer;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	// We simulate every move and calculate which move is better
	// 3 deep
	// After finding the right move we delete the whole tree

	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
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

	public int evaluateBoardState(PylosBoard board, Action move){
		//Calculate a value for the board state after a certain move
		return 0;
	}

	public Action findBestMove(PylosBoard board, PylosGameIF game, PylosPlayerColor color) {
		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(),color, board);
		Stack<Action> previousMoves = new Stack<>();
		Action bestMove;
		int bestMoveScore = 0;
		for(PylosLocation bl : board.getLocations()) {
			if(bl.isUsable()) {
				if(0< board.getReservesSize(this)) {
					// Todo:  move sphere from reserve
				}

				previousMoves.add(new Action(bl.getSphere()/*is deze juist?*/, bl,game.getState(),color));
				simulator.moveSphere();
				if(evaluateBoardState())
				//findBestMove(simulator,board);
			}
		}
		return bestMove;
	}

	public class Action {
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

	}

}
