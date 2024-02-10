import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class StartClient {

	public static void main(String args[]) throws NotBoundException, IOException {

		RMIRegistry server = (RMIRegistry) Naming.lookup("RMIRegistry");

		Scanner teclado = new Scanner(System.in);
		System.out.println("Introduzca el nombre de la carpeta local. Si no, se le asignará una carpeta por defecto");
		String ruta = teclado.nextLine();

		if (ruta.isBlank()) {
			ruta = "DirectorioLocal";
		}

		System.out.println("La carpeta local será : \n " + ruta + "\n");

		ClientImplementation cliente = new ClientImplementation(ruta);
		
		System.out.println("Se empezará con la sincronizacion de archivos. Pulse enter para continuar");
		
		teclado.nextLine();
		
		HashSet<String> listaLocal = cliente.getLista();
		HashSet<String> listaServer = server.getLista();

		System.out.println("Lista de archivos locales \n" + listaLocal + "\n Lista de archivos remotos \n" + listaServer + "\n");
		ArrayList<HashSet<String>> listas = cliente.comparar(listaLocal, listaServer);

		HashSet<String> paraSubir = listas.get(1);
		HashSet<String> paraBajar = listas.get(0);

		System.out.println(" \n Archivos a bajar definitivos \n " + paraBajar + "\n");
		System.out.println("\n Archivos a subir  definitivos \n " + paraSubir + "\n");
		
		
		if (!paraBajar.isEmpty()) {
			System.out.println("Se descargarán los archivos. ¿Está de acuerdo? S/N");

			if (teclado.nextLine().contains("S")) {

				for (String archivo : paraBajar) {
					cliente.descargarArchivo(archivo);

				}
			}
		}
		if (!paraSubir.isEmpty()) {
			System.out.println("¿Quiere subir los archivos listados?S/N");
			if (teclado.nextLine().contains("S")) {

				for (String archivo : paraSubir) {
					cliente.subirArchivo(archivo);

				}
			}

		}
		System.out.println("Proceso finalizado");
		teclado.close();
	}

}
