package be.kuleuven.pylos.main;

import be.kuleuven.pylos.battle.Battle;
import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameObserver;
import be.kuleuven.pylos.game.PylosGameSimulator;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerObserver;
import be.kuleuven.pylos.player.codes.PylosPlayerBestFit;
import be.kuleuven.pylos.player.codes.PylosPlayerMiniMax;
import be.kuleuven.pylos.player.codes.PylosPlayerRandomFit;
import be.kuleuven.pylos.player.student.StudentPlayer;
import be.kuleuven.pylos.player.student.StudentPlayerRandomFit;
import be.kuleuven.pylos.player.student.StudentPlayerBestFit;

import java.util.Random;

public class PylosMain {

	public PylosMain() {

	}

	public void startPerformanceBattles() {
		Random random = new Random(0);
		PylosPlayer[] players = new PylosPlayer[]{/*new PylosPlayerBestFit(),*/ new PylosPlayerMiniMax(2)};
		//PylosPlayer[] players = new PylosPlayer[]{new StudentPlayerBestFit()};

		int[] wins = new int[players.length];
		for (int i = 0; i < players.length; i++) {
			PylosPlayer player = new StudentPlayer();
			PylosPlayer playerDark = players[i];
			double[] results = Battle.play(player, playerDark, 100);
			wins[i] = (int) Math.round(results[0] * 100);
		}

		for (int win : wins) {
			System.out.print(win + "\t");
		}
	}

	public void startSingleGame() {

		Random random = new Random(0);

		StudentPlayer randomPlayerStudent = new StudentPlayer();
		StudentPlayerRandomFit randomPlayerStudent2 = new StudentPlayerRandomFit();
		//PylosPlayer randomPlayerStudent = new StudentPlayerRandomFit();

		PylosBoard pylosBoard = new PylosBoard();
		PylosGame pylosGame = new PylosGame(pylosBoard, randomPlayerStudent, randomPlayerStudent2, random, PylosGameObserver.CONSOLE_GAME_OBSERVER, PylosPlayerObserver.NONE);

		pylosGame.play();
	}

	public void startBattle() {
		StudentPlayer playerStudent = new StudentPlayer();
		PylosPlayer randomPlayerPylos = new PylosPlayerRandomFit();
		Battle.play(playerStudent, randomPlayerPylos, 100);
	}

	public static void main(String[] args) {
		/* !!! vm argument !!! -ea */

		//new PylosMain().startSingleGame();
		//new PylosMain().startBattle();
		new PylosMain().startPerformanceBattles();
	}
}
