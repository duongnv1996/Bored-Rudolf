package org.faudroids.loooooading.game;

import android.content.Context;
import android.content.SharedPreferences;

public class Score {

	private static final String PREFS_KEY_HIGHSCORE = "highscore";

	private final Context context;

	private int consumedSnowflakes = 0;
	private int consumedSnowflakesHighScore;

	public Score(Context context) {
		this.context = context;
		this.consumedSnowflakesHighScore = getPrefs().getInt(PREFS_KEY_HIGHSCORE, 0);
	}

	public void onSnowflakeConsumed() {
		++consumedSnowflakes;
		if (consumedSnowflakes > consumedSnowflakesHighScore) {
			consumedSnowflakesHighScore = consumedSnowflakes;
			SharedPreferences.Editor editor = getPrefs().edit();
			editor.putInt(PREFS_KEY_HIGHSCORE, consumedSnowflakesHighScore);
			editor.commit();
		}
	}

	public void onHitByBomb() {
		consumedSnowflakes -= 5;
	}

	public int getNumericScore() {
		return consumedSnowflakes;
	}

	public int getNumericHighScore() {
		return consumedSnowflakesHighScore;
	}

	public void reset() {
		this.consumedSnowflakes = 0;
	}

	private SharedPreferences getPrefs() {
		return context.getSharedPreferences("org.faudroids.loooooading.game.Score", Context.MODE_PRIVATE);
	}
}
