package org.faudroids.loooooading.game;


import java.util.EnumSet;
import java.util.Set;

/**
 * State machine, not very smart at the moment ...
 */
public enum PlayerState {

	// not doing anything
	DEFAULT {
		@Override
		public Set<PlayerState> possibleNextState() {
			return EnumSet.of(LOOKING_UP, EATING);
		}
	},

	// mouth open
	LOOKING_UP {
		@Override
		public Set<PlayerState> possibleNextState() {
			return EnumSet.of(DEFAULT, EATING);
		}
	},

	// not moving, eating the snowflake
	EATING {
		@Override
		public Set<PlayerState> possibleNextState() {
			return EnumSet.of(DEFAULT, LOOKING_UP);
		}
	};

	public Set<PlayerState> possibleNextState() {
		return EnumSet.noneOf(PlayerState.class);
	}
}
