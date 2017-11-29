/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.client;

import ie.cit.r00158694.soft8023.common.SharedFile;

import java.rmi.RemoteException;

public class FullClient extends AbstractClient {

	public FullClient(String name) {
		super(name);
	}

	@Override
	public boolean addFile(SharedFile file) {
		try {
			return getMonitor().addFile(this.getId(), file);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteFile(String file) {
		try {
			return getMonitor().deleteFile(this.getId(), file);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean readFile(String file) {
		try {
			return getMonitor().readFile(this.getId(), file);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean releaseFile(String file) {
		try {
			return getMonitor().releaseFile(this.getId(), file);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
}
