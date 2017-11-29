/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.concurrency.client;

import ie.cit.r00158694.soft8023.concurrency.monitor.ResourceMonitor;
import ie.cit.r00158694.soft8023.concurrency.monitor.SharedFile;

public class FullClient extends AbstractClient {

	public FullClient(String name, ResourceMonitor resourceMonitor) {
		super(name, resourceMonitor);
	}

	@Override
	public boolean addFile(SharedFile file) {
		return getResourceMonitor().addFile(this, file);
	}

	@Override
	protected boolean deleteFile(String file) {
		return getResourceMonitor().deleteFile(this, file);
	}

	@Override
	protected boolean readFile(String file) {
		return getResourceMonitor().readFile(this, file);
	}

	@Override
	public String toString() {
		return "AbstractClient: " + getClientName();
	}
}
