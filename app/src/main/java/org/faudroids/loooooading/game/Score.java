package org.faudroids.loooooading.game;

public class Score {

	private int consumedSnowflakes = 0;

	public void onSnowflakeConsumed() {
		++consumedSnowflakes;
	}

	public int toNumericScore() {
		return consumedSnowflakes;
	}

	public void reset() {
		this.consumedSnowflakes = 0;
	}

}
