package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ine on 5/05/2015.
 */
public class StudentPlayerRandomFit extends PylosPlayer{

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
		/* add a reserve sphere to a feasible random location */
        PylosSphere reserveSphere = board.getReserve(this);
        ArrayList<PylosLocation> usableLocations = new ArrayList<>();
        for (PylosLocation bl : board.getLocations()) {
            if (bl.isUsable()) {
                usableLocations.add(bl);
            }
        }
        PylosLocation randomLocation = usableLocations.get(getRandom().nextInt(usableLocations.size()-1));
        game.moveSphere(reserveSphere,randomLocation);
    }

    @Override
    public void doRemove(PylosGameIF game, PylosBoard board) {
		/* removeSphere a random sphere */
        ArrayList<PylosLocation> usedLocations = new ArrayList<>();
        for (PylosLocation bl : board.getLocations()) {
            if (bl.isUsed() && !bl.hasAbove() && this.PLAYER_COLOR == bl.getSphere().PLAYER_COLOR) {
                usedLocations.add(bl);
            }
        }
        PylosSphere randomSphere = usedLocations.get(getRandom().nextInt(usedLocations.size()-1)).getSphere();
        game.removeSphere(randomSphere);
    }

    @Override
    public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
        /* always pass */
        game.pass();
    }
}
