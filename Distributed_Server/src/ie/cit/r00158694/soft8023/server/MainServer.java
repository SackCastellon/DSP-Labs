/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.server;

import ie.cit.r00158694.soft8023.common.UpdateEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MainServer {

	private static final int SERVER_PORT = 1234;
	private static final int REGISTRY_PORT = 4321;
	private static final String RESOURCE_MONITOR = "RESOURCE_MONITOR";

	private static ResourceMonitor resourceMonitor;

	private static boolean isRunning = true;

	public static void main(String[] args) {
		System.out.println("[Server] Server starting");
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
			private int i = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName(String.format("Client Handler %d", i++));
				return thread;
			}
		});
		try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
			System.out.println("[Server] Server started");

			resourceMonitor = ResourceMonitor.getInstance();
			Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
			registry.rebind(RESOURCE_MONITOR, resourceMonitor);

			while (isRunning) {
				System.out.println("[Server] Waiting for a new client");
				exec.execute(new ClientThread(server.accept()));
			}

			System.out.println("[Server] Server stopping");
			System.out.println("[Server] No longer waiting for new clients");
			exec.shutdown();
			System.out.println("[Server] Awaiting for termination of all client threads");
			boolean terminated = exec.awaitTermination(5, TimeUnit.SECONDS);
			if (terminated) System.out.println("[Server-Thread] All client threads terminated correctly");
			else System.out.println("[Server] Timeout before termination all client threads");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("[Server] Server stopped");
		}
	}

	private static class ClientThread implements Runnable {

		private final Socket client;

		ClientThread(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			System.out.printf("[%s] Client thread starting\n", Thread.currentThread().getName());

			final LinkedList<UpdateEvent> notifications = new LinkedList<>();

			String clientId = null;

			try (DataInputStream fromClient = new DataInputStream(client.getInputStream());
				 DataOutputStream toClient = new DataOutputStream(client.getOutputStream())) {
				System.out.printf("[%s] Client thread started\n", Thread.currentThread().getName());
				String clientName = fromClient.readUTF();
				System.out.printf("[%s] Client '%s' connected to server from %s\n", Thread.currentThread().getName(), clientName, client.getInetAddress());
				clientId = resourceMonitor.addClient(clientName, notifications);
				System.out.printf("[%s] Assigned ID #%s to client '%s'\n", Thread.currentThread().getName(), clientId, clientName);
				toClient.writeUTF(clientId);

				while (isRunning) {
					toClient.writeBoolean(true); // Tell the client to keep running
					boolean hasNotification = !notifications.isEmpty();
					toClient.writeBoolean(hasNotification); // Tell the client whether there is or not a notification
					if (hasNotification) {
						UpdateEvent notification = notifications.poll();
						toClient.writeUTF(notification.serialize()); // Send the notification to the client
					}
				}

				System.out.printf("[%s] Client thread stopping\n", Thread.currentThread().getName());
				toClient.writeBoolean(false); // Tell client to stop
				fromClient.read(); // Wait for client to disconnect TODO Check
			} catch (IOException e) {
//				e.printStackTrace();
				System.out.printf("[%s] Client disconnected\n", Thread.currentThread().getName());
			} finally {
				resourceMonitor.removeClient(clientId);
				System.out.printf("[%s] Client thread stopped\n", Thread.currentThread().getName());
			}
		}
	}
}
