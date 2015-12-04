package org.faudroids.loooooading.game;


import java.util.EnumSet;
import java.util.Set;

/**
 * State machine, not very smart at the moment ...
 *
 * Especially the possibleNextState method is pointless at the moment.
 * Once there are more states it might make sense ...
 */
public enum PlayerState {

	// not doing anything
	DEFAULT {
		@Override
		public Set<PlayerState> possibleNextState() {
			return EnumSet.allOf(PlayerState.class);
		}
	},

	// mouth open
	LOOKING_UP {
		@Override
		public Set<PlayerState> possibleNextState() {
			return EnumSet.allOf(PlayerState.class);
		}
	},

	// not moving, eating the snowflake
	EATING {
		@Override
		public Set<PlayerState> possibleNextState() {
			return EnumSet.allOf(PlayerState.class);
		}
	};

	public Set<PlayerState> possibleNextState() {
		return EnumSet.noneOf(PlayerState.class);
	}
}
