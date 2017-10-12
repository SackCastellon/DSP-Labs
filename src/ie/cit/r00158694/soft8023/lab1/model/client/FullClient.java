/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model.client;

import ie.cit.r00158694.soft8023.lab1.model.monitor.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.model.monitor.SharedFile;

public class FullClient extends Client {

	public FullClient(String name, ResourceMonitor resourceMonitor) { super(name, resourceMonitor); }

	@Override
	public boolean addFile(SharedFile file) { return getResourceMonitor().addFile(this, file); }

	@Override
	public boolean deleteFile(String file) { return getResourceMonitor().deleteFile(this, file); }

	@Override
	protected boolean readFile(String file) { return getResourceMonitor().readFile(this, file); }
}
