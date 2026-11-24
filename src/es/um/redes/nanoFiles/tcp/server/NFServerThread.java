package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class NFServerThread extends Thread {

	Socket socket;
	List<Socket> activeSockets;
	
	/*
	 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServer.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */

	public NFServerThread(Socket socket, List<Socket> listaActivos) {
		this.socket = socket;
		this.activeSockets = listaActivos;
	}

	@Override
	public void run() {
		System.out.println("New client connected from " + socket.getRemoteSocketAddress());
		try {
			NFServer.serveFilesToClient(socket);
		} finally {
			System.out.println("Disconnected client " + socket.getRemoteSocketAddress());
			activeSockets.remove(socket);
			try {
                if (socket != null) {
                    socket.close(); 
                }
            } catch (IOException e) {
                // Silencio.
            }
		}
	}
}
