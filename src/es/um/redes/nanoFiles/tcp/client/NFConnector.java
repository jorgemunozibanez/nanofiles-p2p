package es.um.redes.nanoFiles.tcp.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector.DownloadedFile;
import es.um.redes.nanoFiles.util.FileInfo;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream dis;
	private DataOutputStream dos;




	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
		
		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		 dis = new DataInputStream(socket.getInputStream());
		 dos = new DataOutputStream(socket.getOutputStream());
	}

	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		String subhash = "ashfdgt24df";
		try {
			PeerMessage mensaje = new PeerMessage(PeerMessageOps.OPCODE_GOT_FILE, true, (short) subhash.length(), subhash);
			mensaje.writeMessageToOutputStream(dos);
			PeerMessage mensaje2 = PeerMessage.readMessageFromInputStream(dis);
			if (mensaje.getOpcode() == mensaje2.getOpcode() && mensaje.getLongSubhash() == mensaje2.getLongSubhash()
					&& mensaje.getSubhash().equals(mensaje.getSubhash())) System.out.println("De puta madre");
			else System.out.println("De puta pena");
		} catch (IOException e) {
			System.err.println("NFConnector.test: Error en el flujo de bytes" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public FileInfo[] peerfiles () {
		try {
			PeerMessage mensajeEnvio = new PeerMessage(PeerMessageOps.OPCODE_PEERFILES);
			mensajeEnvio.writeMessageToOutputStream(dos);
			PeerMessage mensajeRespuesta = PeerMessage.readMessageFromInputStream(dis);
			byte opcode = mensajeRespuesta.getOpcode();
			if (opcode == PeerMessageOps.OPCODE_EMPTY_LIST) {
				return null;
			} else {
				return mensajeRespuesta.getFicheros();
			}
		} catch (IOException e) {
			System.err.println("NFConnector.gotFile: There was an error concerning the byte flow" + e.getMessage());
			e.printStackTrace();
			return null;
		} 
	}
	
	public DownloadedFile gotFile (boolean getFile, String subhash) {
		byte[] fileData = null;
		String filename = null;
		long filesize = -1;
		String filehash = null;
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //Para anidar data[]
		
		try {
			PeerMessage mensajeEnvio = new PeerMessage(PeerMessageOps.OPCODE_GOT_FILE, getFile, (short) subhash.length(), subhash);
			mensajeEnvio.writeMessageToOutputStream(dos);
			PeerMessage mensajeRespuesta = PeerMessage.readMessageFromInputStream(dis);
			byte opcode = mensajeRespuesta.getOpcode();
			if (opcode == PeerMessageOps.OPCODE_FILE_NOT_FOUND) {
				//Silencio.
			} else if (opcode == PeerMessageOps.OPCODE_MULTIPLE_FILES_FOUND) {
				filesize = -2;
			} else {
				filehash = mensajeRespuesta.getHash();
				filesize = mensajeRespuesta.getFileSize();
				filename = mensajeRespuesta.getName();
				if (getFile) {
					int totalChunks = 0;
					boolean last = false;
					PeerMessage chunk;
					while (!last) {
						chunk = PeerMessage.readMessageFromInputStream(dis);
						totalChunks++;
						last = chunk.isLast();
						buffer.write(chunk.getFileData(), 0, chunk.getLongData());
					}
					System.out.println("	Downloaded " + totalChunks + " chunks from server at " + serverAddr);
				}
			}
			fileData = buffer.toByteArray();
			return new DownloadedFile(filename, filesize, fileData, filehash);
		} catch (IOException e) {
			System.err.println("NFConnector.gotFile: There was an error concerning the byte flow" + e.getMessage());
			e.printStackTrace();
			return null;
		} 
	}
	
	public byte[] getFragment (long offset, int fragmentSize, String hash) {
		byte[] fileData = null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //Para anidar data[]
		
		try {
			PeerMessage mensajeEnvio = new PeerMessage(PeerMessageOps.OPCODE_GET_FRAGMENT, offset, fragmentSize, (short) hash.length(), hash);
			mensajeEnvio.writeMessageToOutputStream(dos);
			int totalChunks = 0;
			boolean last = false;
			PeerMessage chunk;
			while (!last) {
				chunk = PeerMessage.readMessageFromInputStream(dis);
				last = chunk.isLast();
				buffer.write(chunk.getFileData(), 0, chunk.getLongData());
				totalChunks++;
			}
			System.out.println("	Downloaded " + totalChunks + " chunks from server at " + serverAddr);
			fileData = buffer.toByteArray();
		} catch (IOException e) {
			System.err.println("NFConnector.gotFile: There was an error concerning the byte flow" + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		return fileData;
	}
	
	public void closeConnector () {
		try {
			if (socket != null) {
	            socket.close();
	        }
	    } catch (IOException e) {
	        System.err.println("NFConnector.closeConnector: There was an error closing peer connector: " + e.getMessage());
	    }
	}



	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
