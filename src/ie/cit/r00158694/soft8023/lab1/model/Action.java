/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

public enum Action {

	ADD("action.added"), REMOVE("action.removed"), READ("action.startedPlaying"), RELEASE("action.stoppedPlaying");

	private final String key;

	Action(String key) {this.key = key;}

	/**
	 * Returns the localization key used to translate this action text.
	 *
	 * @return The localization key
	 */
	public String getKey() { return key; }
}
