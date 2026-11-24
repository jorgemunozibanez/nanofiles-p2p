package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;
	

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */

	private FileInfo[] ficheros;
	private long fileSize;
	private boolean getFile;
	private short longHash;
	private String hash;
	private short longSubhash;
	private String subhash;
	private short longName;
	private String name;
	private long offset;
	private int fragmentSize;
	private boolean last;
	private int longData;
	private byte[] fileData;



	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	
	//FILES_INFO (0x01)
	public PeerMessage(byte op, FileInfo[] files ) {
		this(op);
		if (opcode != PeerMessageOps.OPCODE_FILES_INFO) {
			throw new RuntimeException("PeerMessage: constructor called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.ficheros = files;
	}
	
	//GOT_FILE (0x10)
	public PeerMessage(byte op, boolean getfileEnt, short longSub, String subhashEnt) {
		this(op);
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE	) {
			throw new RuntimeException("PeerMessage: constructor called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.getFile = getfileEnt;
		this.longSubhash = longSub;
		this.subhash = subhashEnt;
	}

	//FILE_LOCATED (0x11)
	public PeerMessage(byte op, long sizeEnt, short lHash, String hashEnt, short longName, String nameEnt) {
		this(op);
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED) {
			throw new RuntimeException("PeerMessage: constructor called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.fileSize = sizeEnt;
		this.longHash = lHash;
		this.hash = hashEnt;
		this.longName = longName;
		this.name = nameEnt;
	}

	//GET_FRAGMENT (0x20)
	public PeerMessage(byte op, long offsetEnt, int fragSizeEnt, short lHash, String hashEnt) {
		this(op);
		if (opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: constructor called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.offset = offsetEnt;
		this.fragmentSize = fragSizeEnt;
		this.longHash = lHash;
		this.hash = hashEnt;
	}

	//SEND_CHUNK (0x31)
	public PeerMessage(byte op, boolean lastEnt, int lData, byte[] dataEnt) {
		this(op);
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: constructor called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.last = lastEnt;
		this.longData = lData;
		this.fileData = dataEnt;
	}

	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	public void setOpcode(byte opcodeEnt) {
		this.opcode = opcodeEnt;
	}
	
	//ficheros
	public FileInfo[] getFicheros() {
		if (opcode != PeerMessageOps.OPCODE_FILES_INFO) {
			throw new RuntimeException("PeerMessage: getFicheros called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		if (this.ficheros == null) return null;
	    return ficheros.clone();
	}

	public void setFicheros(FileInfo[] ficheros) {
		if (opcode != PeerMessageOps.OPCODE_FILES_INFO) {
			throw new RuntimeException("PeerMessage: getFicheros called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		} 
		this.ficheros = ficheros;
	}

	//fileSize
	public long getFileSize() {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED) {
			throw new RuntimeException("PeerMessage: getFileSize called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return fileSize;
	}


	public void setFileSize(long fileSize) {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED) {
			throw new RuntimeException("PeerMessage: setFileSize called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.fileSize = fileSize;
	}

	//hash
	public short getLongHash() {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: getLongHash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return longHash;
	}

	public void setLongHash(short longHash) {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: setLongHash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.longHash = longHash;
	}

	public String getHash() {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: getHash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return hash;
	}

	public void setHash(String hash) {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: setHash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.hash = hash;
	}

	//name
	public short getLongName() {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: getLongName called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return longName;
	}
	public void setLongName(short longName) {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: setLongName called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.longName = longName;
	}
	public String getName() {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: getName called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return name;
	}
	public void setName(String name) {
		if (opcode != PeerMessageOps.OPCODE_FILE_LOCATED && opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: setName called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.name = name;
	}
	
	//getfile
	public boolean getGetFile() {
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE) {
			throw new RuntimeException("PeerMessage: getGetFile called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return getFile;
	}

	public void setGetFile(boolean getFile) {
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE) {
			throw new RuntimeException("PeerMessage: setLongSubhash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.getFile = getFile;
	}

	//subhash
	public short getLongSubhash() {
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE) {
			throw new RuntimeException("PeerMessage: setGetFile called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return longSubhash;
	}

	public void setLongSubhash(short longSubhash) {
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE) {
			throw new RuntimeException("PeerMessage: setLongSubhash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.longSubhash = longSubhash;
	}

	public String getSubhash() {
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE) {
			throw new RuntimeException("PeerMessage: getSubhash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return subhash;
	}

	public void setSubhash(String subhash) {
		if (opcode != PeerMessageOps.OPCODE_GOT_FILE) {
			throw new RuntimeException("PeerMessage: setSubhash called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.subhash = subhash;
	}

	//offset
	public long getOffset() {
		if (opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: getOffset called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return offset;
	}

	public void setOffset(long offset) {
		if (opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: setOffset called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.offset = offset;
	}

	//fragmentSize
	public int getFragmentSize() {
		if (opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: getFragmentSize called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return fragmentSize;
	}

	public void setFragmentSize(int fragmentSize) {
		if (opcode != PeerMessageOps.OPCODE_GET_FRAGMENT) {
			throw new RuntimeException("PeerMessage: setFragmentSize called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.fragmentSize = fragmentSize;
	}

	//last
	public boolean isLast() {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: isLast called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return last;
	}

	public void setLast(boolean last) {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: setLast called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.last = last;
	}

	//fileData
	public int getLongData() {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: getLongData called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return longData;
	}

	public void setLongData(int longData) {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: setLongData called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.longData = longData;
	}

	public byte[] getFileData() {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: getFileData called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) {
			throw new RuntimeException("PeerMessage: setFileData called for unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
		this.fileData = fileData;
	}





	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
	    /*
	     * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
	     * socket a través del "dis" el resto de campos para ir extrayendo con los
	     * valores y establecer los atributos del un objeto DirMessage que contendrá
	     * toda la información del mensaje, y que será devuelto como resultado. NOTA:
	     * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
	     * entero, etc.
	     */
	    PeerMessage message = new PeerMessage();
	    byte opcode = dis.readByte();
	    message.setOpcode(opcode);

	    switch (opcode) {
	        case PeerMessageOps.OPCODE_PEERFILES: {
	            break;
	        }

	        case PeerMessageOps.OPCODE_FILES_INFO: {
	            int lbytes = dis.readShort();
	            byte[] bytes = new byte[lbytes];
	            dis.readFully(bytes);
	            message.setFicheros(FileInfo.deserializeList(bytes));
	            break;
	        }

	        case PeerMessageOps.OPCODE_EMPTY_LIST: {
	            break;
	        }

	        case PeerMessageOps.OPCODE_GOT_FILE: {
	            message.setGetFile(dis.readBoolean());
	            short longSubhash = dis.readShort();
	            message.setLongSubhash(longSubhash);
	            byte[] subhashBytes = new byte[longSubhash];
	            dis.readFully(subhashBytes);
	            message.setSubhash(new String(subhashBytes, 0, subhashBytes.length, StandardCharsets.UTF_8));
	            break;
	        }

	        case PeerMessageOps.OPCODE_FILE_LOCATED: {
	            message.setFileSize(dis.readLong());
	            short longHash = dis.readShort();
	            message.setLongHash(longHash);
	            byte[] hashBytes2 = new byte[longHash];
	            dis.readFully(hashBytes2);
	            message.setHash(new String(hashBytes2, 0, hashBytes2.length, StandardCharsets.UTF_8));
	            short longName = dis.readShort();
	            message.setLongName(longName);
	            byte[] hashBytes3 = new byte[longName];
	            dis.readFully(hashBytes3);
	            message.setName(new String(hashBytes3, 0, hashBytes3.length, StandardCharsets.UTF_8));
	            break;
	        }

	        case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
	            break;
	        }

	        case PeerMessageOps.OPCODE_MULTIPLE_FILES_FOUND: {
	            break;
	        }

	        case PeerMessageOps.OPCODE_GET_FRAGMENT: {
	            message.setOffset(dis.readLong());
	            message.setFragmentSize(dis.readInt());
	            short longHashFrag = dis.readShort();
	            message.setLongHash(longHashFrag);
	            byte[] hashFragBytes = new byte[longHashFrag];
	            dis.readFully(hashFragBytes);
	            message.setHash(new String(hashFragBytes, 0, hashFragBytes.length, StandardCharsets.UTF_8));
	            break;
	        }

	        case PeerMessageOps.OPCODE_SEND_CHUNK: {
	            message.setLast(dis.readBoolean());
	            int longData = dis.readInt();
	            message.setLongData(longData);
	            byte[] fileData = new byte[longData];
	            dis.readFully(fileData);
	            message.setFileData(fileData);
	            break;
	        }

	        default:
	            System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
	                    + PeerMessageOps.opcodeToOperation(opcode));
	            System.exit(-1);
	    }
	    return message;
	}
	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para escribir un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {


		case PeerMessageOps.OPCODE_PEERFILES: {
            break;
        }
		
        case PeerMessageOps.OPCODE_FILES_INFO: {
            byte[] bytes = FileInfo.serializeList(ficheros);
            short lbyte = (short) bytes.length;
            dos.writeShort(lbyte);
            dos.write(bytes);
            break;
        }
        
        case PeerMessageOps.OPCODE_EMPTY_LIST: {
            break;
        }
        
        case PeerMessageOps.OPCODE_GOT_FILE: {
        	dos.writeBoolean(getFile);
            dos.writeShort(longSubhash);
            dos.write(subhash.getBytes(StandardCharsets.UTF_8));
            break;
        }

        case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
            break;
        }
        
        case PeerMessageOps.OPCODE_MULTIPLE_FILES_FOUND: {
            break;
        }
        
        case PeerMessageOps.OPCODE_FILE_LOCATED: {
            dos.writeLong(fileSize);
            dos.writeShort(longHash);
            dos.write(hash.getBytes(StandardCharsets.UTF_8));
            dos.writeShort(longName);
            dos.write(name.getBytes(StandardCharsets.UTF_8));
            break;
        }
        
        case PeerMessageOps.OPCODE_GET_FRAGMENT: {
            dos.writeLong(offset);
            dos.writeInt(fragmentSize);
            dos.writeShort(longHash);
            dos.write(hash.getBytes(StandardCharsets.UTF_8));
            break;
        }
        
        case PeerMessageOps.OPCODE_SEND_CHUNK: {
            dos.writeBoolean(last);
            dos.writeInt(longData);
            dos.write(fileData, 0, longData);
            break;
        }


        
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}




}