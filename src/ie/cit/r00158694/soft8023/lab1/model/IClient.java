/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.function.Consumer;

public interface IClient {

	/**
	 * Sets the consumer that will be executed every time the client receives an update.
	 *
	 * @param onUpdate The consumer
	 */
	void setOnUpdate(Consumer<UpdateEvent> onUpdate);

	/**
	 * The method that will receive the {@link UpdateEvent}.
	 *
	 * @param event The update event
	 */
	void update(UpdateEvent event);

	/**
	 * Returns the name of the client.
	 *
	 * @return The client name
	 */
	String getClientName();
}
