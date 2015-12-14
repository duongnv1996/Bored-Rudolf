package org.faudroids.boredrudolf.game;

/**
 * State of the current game.
 */
public enum GameState {

	// nothing happening
	STOPPED,
	// playing normally
	RUNNING,
	// still running, but will end soon
	SHUTDOWN_REQUESTED

}
