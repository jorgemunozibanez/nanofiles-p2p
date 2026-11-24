package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	// Servidor TCP local para compartir ficheros con otros peers
	private NFServer fileServer = null;

	private boolean serverRunning = false;

	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
		} else {
			/*
			 * TODO: (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
			 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
			 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
			 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
			 * y tratadas en este método. Si se produce una excepción de entrada/salida
			 * (error del que no es posible recuperarse), se debe informar sin abortar el
			 * programa
			 * 
			 */
			try {
				fileServer = new NFServer();
				int puerto = getServerPort();
				if (puerto > 0) {
					System.out.println("FileServer is listening on port " + puerto);
					Thread t1 = new Thread(fileServer);
					t1.start();
					serverRunning = true;
				}
				
			} catch (IOException e) {
				System.err.println("Something went wrong while initiating the Server: " + e.getMessage());
		        }
		}
		return serverRunning;
	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			nfConnector.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para listar los ficheros de un peer concreto vía TCP e imprimirlos por
	 * pantalla.
	 * 
	 * @param La dirección del peer cuyos ficheros se quiere listar
	 * @return Verdadero si se ha obtenido exitosamente el listado de fichero del
	 *         peer
	 */
	protected boolean listPeerFiles(InetSocketAddress peerAddr) {
		boolean success = false;
		NFConnector nfConnector = null;
		try {
			nfConnector = new NFConnector(peerAddr);
			FileInfo[] trackedFiles = nfConnector.peerfiles();
			
			if (trackedFiles == null) {
				System.out.println(
						"* There are no files tracked by the peer at " + peerAddr);
			} else {
				System.out.println(
						"* These are the files tracked by the peer at " + peerAddr);
				FileInfo.printToSysout(trackedFiles);
			}
		} catch (UnknownHostException e) {
			System.err.println("Server not found: " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
			return false;
		} finally {
			if (nfConnector != null) nfConnector.closeConnector();
		}
		return success;
	}

	/**
	 * Descarga un fichero identificado por subcadena de hash desde uno o varios
	 * peers. Si se pasa "*" como nickname, usa el directorio para localizar los
	 * peers que tienen el hash.
	 */
	protected boolean downloadFromPeers(NFControllerLogicDir dirLogic, String targetPeerNickname,
			String targetHashSubstring) {
		// TODO: localizar peers con el hash solicitado (o uno concreto) y delegar en
		// downloadFileFromServers
		boolean success = false;
		InetSocketAddress[] targetPeers;
		if ("*".equals(targetPeerNickname)) {
			InetSocketAddress servidor = null;
			if (serverRunning) servidor = fileServer.getServerSocketAddress();
			Map<FileInfo, List<InetSocketAddress>> hashsYFich = dirLogic.peersThatShareFile(targetHashSubstring, servidor);
			if (hashsYFich == null) {
			 System.err.println("* Failed to download file given by hash substring " + targetHashSubstring);
				return false;
			}
			Set<FileInfo> hashs = hashsYFich.keySet();
			if (hashs.isEmpty()) {
				System.out.println("No peer owns a file whose hash contains the specified substring");
				System.err.println("* Failed to download file given by hash substring " + targetHashSubstring);
			} else if (hashs.size() > 1) {
				System.out.println("There are multiple files whose hash contains the specified substring.");
				System.out.println("Please try again with a more specific hash substring");
				System.err.println("* Failed to download file given by hash substring " + targetHashSubstring);
			} else {
				targetPeers = hashsYFich.values().iterator().next().toArray(new InetSocketAddress[0]);
				success = downloadFileFromServers(targetPeers, hashs.iterator().next());
			}
		} else {
			Map<String, InetSocketAddress> peers = dirLogic.fetchPeerList();
			InetSocketAddress serverAddr = peers.get(targetPeerNickname);
			NFConnector nfConnector = null;
			try {
				nfConnector = new NFConnector(serverAddr);
				DirectoryConnector.DownloadedFile dl = nfConnector.gotFile(true, targetHashSubstring);
				if (dl == null || dl.filesize < 0) {
					if (dl.filesize == -1) System.out.println("This peer does not have a file whose hash contains the specified substring");
					else if (dl.filesize == -2) {
						System.out.println("There are multiple files whose hash contains the specified substring.");
						System.out.println("Please try again with a more specific hash substring");
					}
					System.err.println("* Failed to download file given by hash substring " + targetHashSubstring);
					return false;
				}
				
				System.out.println("Tamaño esperado: " + dl.filesize + " | Tamaño descargado: " + dl.data.length);
				try {
					java.nio.file.Path dest = es.um.redes.nanoFiles.util.FileNameUtil.chooseAvailableName(dl.filename);
					java.nio.file.Files.write(dest, dl.data);
					String checksum = es.um.redes.nanoFiles.util.FileDigest.computeFileChecksumString(dest.toString());
					System.out.println("* Downloaded remote file to " + toDisplayPath(dest) + " (" + dl.data.length
							+ " bytes)");
					if (dl.filehash != null) {
						if (dl.filehash.equals(checksum)) {
							System.out.println("* Checksum verified: computed value matches expected hash (" + checksum + ")");
						} else {
							System.err.println("* WARNING: computed checksum (" + checksum + ") does not match expected hash ("
									+ dl.filehash + ")");
						}
					} else {
						System.out.println("* Computed SHA-256: " + checksum);
					}
					success = true;
				} catch (java.io.IOException e) {
					System.err.println("* Failed to write downloaded file: " + e.getMessage());
					return false;
				} 
				
			} catch (UnknownHostException e) {
				System.err.println("Server not found: " + e.getMessage());
				return false;
			} catch (IOException e) {
				System.err.println("I/O error: " + e.getMessage());
				return false;
			} finally {
				if (nfConnector != null) nfConnector.closeConnector();
			}
		}
		return success;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList   La lista de direcciones de los servidores a los
	 *                            que se conectará
	 * @param targetHashSubstring Hash del fichero a descargar
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, FileInfo infoFichero) {
		boolean downloaded = false;
		int numPeers = serverAddressList.length;
		if (numPeers == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		// TODO: crear conectores TCP solo a los servidores que confirmen el hash
		// pedido, obtener nombre remoto, reservar nombre local sin colisiones, alternar
		// descarga de chunks y verificar hash final. Cerrar los sockets al terminar.
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //Para anidar data[]
		long tamano = infoFichero.fileSize;
		String hash = infoFichero.fileHash;
		
		NFConnector nfConnector = null;
		long fragmentoBase = tamano / numPeers;
		long resto = tamano % numPeers;
		long offset = 0;
		byte[] fragmentData;
		
		try {
			for (int i = 0; i < numPeers-1; i++) {
				nfConnector = new NFConnector(serverAddressList[i]);
				fragmentData = nfConnector.getFragment(offset, (int) fragmentoBase, hash);
				buffer.write(fragmentData, 0, fragmentData.length);
				nfConnector.closeConnector();
				offset += fragmentoBase;
			}
			nfConnector = new NFConnector(serverAddressList[numPeers-1]);
			fragmentData = nfConnector.getFragment(offset, (int) (fragmentoBase + resto), hash);
			buffer.write(fragmentData, 0, fragmentData.length);
			nfConnector.closeConnector();
			
			byte[] fileData = buffer.toByteArray();
			System.out.println("Tamaño esperado: " + tamano + " | Tamaño descargado: " + fileData.length);
			try {
				java.nio.file.Path dest = es.um.redes.nanoFiles.util.FileNameUtil.chooseAvailableName(infoFichero.fileName);
				java.nio.file.Files.write(dest, fileData);
				String checksum = es.um.redes.nanoFiles.util.FileDigest.computeFileChecksumString(dest.toString());
				System.out.println("* Downloaded remote file to " + toDisplayPath(dest) + " (" + fileData.length
						+ " bytes)");
				if (infoFichero.fileHash != null) {
					if (infoFichero.fileHash.equals(checksum)) {
						System.out.println("* Checksum verified: computed value matches expected hash (" + checksum + ")");
					} else {
						System.err.println("* WARNING: computed checksum (" + checksum + ") does not match expected hash ("
								+ infoFichero.fileHash + ")");
					}
				} else {
					System.out.println("* Computed SHA-256: " + checksum);
				}
				downloaded = true;
			} catch (java.io.IOException e) {
				System.err.println("* Failed to write downloaded file: " + e.getMessage());
				return false;
			} 
			
		} catch (UnknownHostException e) {
			System.err.println("Server not found: " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
			return false;
		} finally {
			if (nfConnector != null) nfConnector.closeConnector();
		}


		return downloaded;
	}

	private String toDisplayPath(java.nio.file.Path path) {
		java.nio.file.Path abs = path.toAbsolutePath().normalize();
		java.nio.file.Path cwd = java.nio.file.Paths.get("").toAbsolutePath().normalize();
		if (abs.startsWith(cwd)) {
			return cwd.relativize(abs).toString();
		}
		return path.toString();
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		int port = fileServer.getPort();
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros
		 */
		if (port < 0) port = 0;

		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		serverRunning = false;
		fileServer.stopServer();
	}

	protected boolean serving() {
		return serverRunning;

	}

}
