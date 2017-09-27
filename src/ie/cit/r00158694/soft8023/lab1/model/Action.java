/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

public enum Action {
	ADDED("action.added"), REMOVED("action.removed"), STARTED_PLAYING("action.startedPlaying"), STOPPED_PLAYING("action.stoppedPlaying");

	private final String key;

	Action(String key) {this.key = key;}

	public String getKey() { return key; }
}
