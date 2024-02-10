

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;

public interface RMIRegistry extends Remote{
	
	
	HashSet<String> getLista() throws RemoteException, IOException;
	void subirArchivoAlServer(byte[] mydata, String serverpath, int length) throws RemoteException, FileNotFoundException, IOException;
	byte[] descargarArchivoDelServer(String serverpath) throws RemoteException;
	ArrayList<byte[]> getHash(String archivo) throws RemoteException;
	long getUltimaModificacion(String archivo) throws RemoteException;
	
}
