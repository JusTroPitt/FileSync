
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;

public class ClientImplementation {

	private static Path path;
	private static RMIRegistry server;

	public ClientImplementation(String pathLocal) throws RemoteException, IOException, NotBoundException {
		// pathLocal sería la "carpeta madre" del cliente
		path = Paths.get(pathLocal);

		if (path.toFile().exists()) {
			System.out.println("La carpeta se ha introducido correctamente");
		} else {
			boolean x = path.toFile().mkdir();

			if (x) {
				System.out.println("La carpeta se ha creado correctamente");

			}

			else{
				System.out.println("No se ha podido crear la carpeta. Será creada una carpeta por defecto en su lugar");
				path = Paths.get("DirectorioLocal");
			}

		}

		System.out.println("La ruta absoluta o nombre de la carpeta es: \n" + path.toFile().getAbsolutePath() + "\n");
		server = (RMIRegistry) Naming.lookup("RMIRegistry");
	}

	public HashSet<String> getLista() throws IOException {
		HashSet<String> listado = new HashSet<>();
		File[] archivos = path.toFile().listFiles((File a) -> a.isFile());
		for (File f : archivos) {
			listado.add(f.getName());

		}

		File[] directorios = path.toFile().listFiles((File a) -> a.isDirectory());
		if (directorios.length > 0 || !directorios.equals(null)) {
			for (File f : directorios) {
				String pathDirectorio = f.getName();
				HashSet<String> archivosDirectorio = listarDirectorios(pathDirectorio);
				// System.out.println(path + "\n" + pathDirectorio.toString());
				listado.addAll(archivosDirectorio);
			}
		}

		return listado;
	}


	private HashSet<String> listarDirectorios(String directorio) throws IOException {
		Path pathV = Paths.get(path + "/" + directorio);
		//System.out.println(pathV);
		HashSet<String> listado = new HashSet<>();
		File[] archivos = pathV.toFile().listFiles((File a) -> a.isFile());
		for (File f : archivos) {
			listado.add(directorio + "/" + f.getName());

		}

		File[] directorios = pathV.toFile().listFiles((File a) -> a.isDirectory());

		if( directorios.length > 0 || !directorios.equals(null)){
		//System.out.println(directorios.length);

			for (File f : directorios) {
				//System.out.println(f.getName());
				String pathDirectorio = directorio + "/" + f.getName();
				//System.out.println(pathDirectorio);

				HashSet<String> archivosDirectorio = listarDirectorios(pathDirectorio);

				listado.addAll(archivosDirectorio);
				//System.out.println(archivosDirectorio);
			}

		}
		return listado;
	}

	public ArrayList<HashSet<String>> comparar(HashSet<String> listaLocal, HashSet<String> listaServer)
			throws IOException {

		HashSet<String> paraBajar = new HashSet<>(listaServer);
		paraBajar.removeAll(listaLocal);
		System.out.println(" \n Ficheros a bajar de forma preliminar: \n" + paraBajar + "\n");
				

		HashSet<String> paraSubir = new HashSet<>(listaLocal);
		paraSubir.removeAll(listaServer);
		System.out.println("\n Ficheros a subir de forma preliminar: \n" + paraSubir + "\n");
		

		HashSet<String> paraRevisar = new HashSet<>(listaServer);
		paraRevisar.retainAll(listaLocal);
		System.out.println("\n Ficheros a revisar: \n" + paraRevisar + "\n");
	
		
		ArrayList<HashSet<String>> listas = new ArrayList<>();
		listas.add(0, paraBajar);
		listas.add(1, paraSubir);
		listas.add(2, paraRevisar);


		return getListasDefinitivas(listas);
	}

	private static  ArrayList<byte[]> hash(String archivo) throws RemoteException {

		Path pathArchivo = Paths.get(path.toString() + "/" + archivo);

		FileChannel fn = null;
		ArrayList<byte[]> h = new ArrayList<>();
		try {
			// Leemos el archivo e indicamos que el buffer tenga una capacidad de 512kB
			fn = (FileChannel) Files.newByteChannel(pathArchivo);
			ByteBuffer buffer = ByteBuffer.allocate(512 * 1024);
			// Indicamosue queremos un hash en md5
			MessageDigest msd = MessageDigest.getInstance("MD5");
			// Creamos un bucle que termine cuando no lea nada mas en el archivo
			while (fn.read(buffer) != -1) {
				byte[] a = new byte[buffer.position()];
				buffer.flip();
				buffer.get(a);
				byte[] b = msd.digest(a);
				// Hacemos la conversion de bytes a hexadecimal
				StringBuilder result = new StringBuilder();
				for (byte aByte : b) {
					result.append(String.format("%02x", aByte));

				}
				// añadimos a nuestra variable h el resultado en bytes
				h.add(result.toString().getBytes());
				// Limpiamos el buffer
				buffer.clear();

			}
			// Si queremos hacer la comprobacion lo hacemos con la linea de abajo comentada
			// System.out.println(new String(h.get(0)));
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			try {
				fn.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return h;

	}

	private static boolean esElMismoHash(ArrayList<byte[]> hashServer, ArrayList<byte[]> hashLocal) throws IOException {
		String sHashServer = null;
		String sHashLocal = null;
		for (byte[] q : hashServer) {
			sHashServer = new String(q, StandardCharsets.UTF_8);

		}
		for (byte[] q : hashLocal) {
			sHashLocal = new String(q, StandardCharsets.UTF_8);
		}
		// System.out.println(sHashServer + "\n");
		// System.out.println(sHashLocal + "\n");
		if (sHashServer.contentEquals(sHashLocal)) {

			return true;
		} else {
			return false;
		}

	}

	private static boolean tengoElMasReciente(String archivo) throws RemoteException {
		Path pathArchivo = Paths.get(path + "/" + archivo);
		long lastServer = server.getUltimaModificacion(archivo);
		long lastLocal = pathArchivo.toFile().lastModified();
		//System.out.println(lastLocal);
		//System.out.println(lastServer);
		if (lastServer < lastLocal) {
			return true;
		} else {
			return false;
		}

	}

	private static ArrayList<HashSet<String>> getListasDefinitivas(ArrayList<HashSet<String>> listas) throws IOException {

		// 0 es subir, 1 es ,2 es comparar
		HashSet<String> paraBajar = listas.get(0);
		HashSet<String> paraSubir = listas.get(1);
		HashSet<String> paraComparar = listas.get(2);
		ArrayList<HashSet<String>> listasFinales = new ArrayList<>();
		for (String s : paraComparar) {
			ArrayList<byte[]> hashLocal = hash(s);
			ArrayList<byte[]> hashRemoto = server.getHash(s);

			if (!esElMismoHash(hashLocal, hashRemoto)) {
				if (tengoElMasReciente(s)) {

					paraSubir.add(s);
				} else {

					paraBajar.add(s);
				}

			}
		}
		listasFinales.add(0, paraBajar);
		listasFinales.add(1, paraSubir);
		//System.out.println("Para subir \n" + paraSubir );
		//System.out.println("Para bajar \n" + paraBajar );
		return listasFinales;

	}
	public void subirArchivo(String archivo) throws RemoteException,FileNotFoundException,IOException{

		Path pathArchivo= Paths.get(path + "/" + archivo);


		File clientpathfile = new File(pathArchivo.toString());
		byte [] datos=new byte[(int) clientpathfile.length()];
		FileInputStream in=new FileInputStream(clientpathfile);
			System.out.println("Subiendo " + clientpathfile.getName() +  " al servidor ");
		 in.read(datos, 0, datos.length);
		 server.subirArchivoAlServer(datos, archivo, (int) clientpathfile.length());

		 in.close();
	}
	public void descargarArchivo(String archivo) throws RemoteException,FileNotFoundException,IOException {

		Path pathArchivo= Paths.get(path + "/" + archivo);

		byte [] datos = server.descargarArchivoDelServer(archivo);
		System.out.println("Descargando " + archivo);
		File clientpathfile = new File(pathArchivo.toString());
		clientpathfile.getParentFile().mkdirs();
		FileOutputStream out=new FileOutputStream(clientpathfile);
		out.write(datos);
		out.flush();
    	out.close();
    	System.out.println(archivo + " descargado");
	}
}
