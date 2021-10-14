package Analizador;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Lexico {
private static int cont = 0; //Contador para evitar que la cadena salga de rango
private static int num = 0; //Almacena el numero
private static String lexema = ""; //Almacena la cadena
private static int linea = 2;
private static List<Token> tokens = new ArrayList<Token>(); //Lista de tokens

private static List<String> TemporalTS = new ArrayList<String>(); //TS temporal para saber las ids
private static int id = 0;

//public static void main(String[] args) throws Exception {
public static List<Token> listaTokens(String input) throws Exception {
	
	File archivo = new File(input);
	FileReader fr;
	int estado = 0;
	
	try {
		//Lee el archivo entrada
		fr = new FileReader (archivo);
		BufferedReader br = new BufferedReader(fr);
		
		//Bucle que recorre todo el fichero
		int value = 0;
	    while (value != -1) {
	    	
	    	//System.out.println(tokens);
	    	
	    	if(estado != -1) //El estado -1 es el estado 0 sin leer
	    		value = br.read();
	    	else
	    		estado = 0;
	    	
	    	//Busca el estado correspondiente
	    	switch(estado) {
	    		case 0:
	    			estado = automata0((char) value);
	    			break;
	    		case 1:
	    			estado = automata1((char) value);
	    			break;
	    		case 4:
	    			estado = automata4((char) value);
	    			break;
	    		case 6:
	    			estado = automata6((char) value);
	    			break;
	    		case 8:
	    			estado = automata8((char) value);
	    			break;
	    		case 11:
	    			estado = automata11((char) value);
	    			break;
	    		case 20:
	    			estado = automata20((char) value);
	    			break;
	    		case 21:
	    			estado = automata21((char) value);
	    			break;
	    		case -1:
	    			break;
	    		default:
	    			throw new Exception("Error lexico [linea "+linea/2+"]: El estado no existe"); //No deberia saltar
	    	}
	    }
	    
	    //Cierra el archivo
	    br.close();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	//ListIterator<Token> tokensIt = tokens.listIterator();
	
	//while(tokensIt.hasNext()) {
	//	System.out.println(tokensIt.next());
	
	return tokens;
}
	

//Estado inicio
public static int automata0(char caracter) throws Exception {
	
	//Comprueba si es un numero
	if(caracter>=48 && caracter<=57) {
		//Añade el numero
		num = caracter-48;
		return 4;
	}
	
	//Comprueba si es una letra mayusc o minusc, es un id
	if((caracter>=65 && caracter<=90)||(caracter>=97 && caracter<=122)) {
		//Añade la letra
		lexema = ""+caracter;
		cont = 0;
		return 11;
	}
	
	switch(caracter) {
		case 9:
		case 32:
			return 0; //Caso <del>, no hace nada
		case 13:
		case 10:
			linea++;
			return 0;
		case '=':
			return 1;
		case 39: //Es una comilla, crea la cadena
			lexema = "";
			cont = 0;
			return 6;
		case '+':
			return 8;
		case '{':
			//System.out.println("<AbrirLlave,>");
			tokens.add(new Token("AbrirLlave", linea));
			return 0;
		case '}':
			//System.out.println("<CerrarLlave,>");
			tokens.add(new Token("CerrarLlave", linea));
			return 0;
		case '(':
			//System.out.println("<AbrirPar,>");
			tokens.add(new Token("AbrirPar", linea));
			return 0;
		case ')':
			//System.out.println("<CerrarPar,>");
			tokens.add(new Token("CerrarPar", linea));
			return 0;
		case ';':
			//System.out.println("<PuntoComa,>");
			tokens.add(new Token("PuntoComa", linea));
			return 0;
		case ',':
			//System.out.println("<Coma,>");
			tokens.add(new Token("Coma", linea));
			return 0;
		case '!':
			//System.out.println("<Not,>");
			tokens.add(new Token("Not", linea));
			return 0;
		case '/':
			return 20;
		case (char) -1: //Salto de linea
			return 0;
		default:
			throw new Exception("Error lexico [linea "+linea/2+"]: Caracter *"+caracter+"* no valido");
	}
}

//Estado para  = y ==
public static int automata1(char caracter) {
	if(caracter=='=') {
		//System.out.println("<Igualdad,>");
		tokens.add(new Token("Igualdad", linea));
		return 0;
	}
	else {
		//System.out.println("<Asignacion,>");
		tokens.add(new Token("Asignacion", linea));
		return -1;
	}
}

//Estado para generar numeros
public static int automata4(char caracter) throws Exception {
	//Si es un numero, lo añade
	if(caracter>=48 && caracter<=57) {
		num = num*10 + caracter-48;
		return 4;
	} else {
		//Si no es un numero, termina de crear el token,
		//comprobando que este en el rango
		if(Math.abs(num)>32767)
			throw new Exception("Error lexico [linea "+linea/2+"]: Entero fuera de rango");
		//System.out.println("<Entero,"+num+">");
		tokens.add(new Token("Entero", num, linea));
		return -1;
	}
}

//Estado para generar cadenas
public static int automata6(char caracter) throws Exception {
	//Si es ', genera el token, comprobando que este en el rango
	if(caracter==39) {
		if(cont>64)
			throw new Exception("Error lexico [linea "+linea/2+"]: Cadena *"+lexema+"* fuera de rango");
		//System.out.println("<Cadena,"+(char)34+lexema+(char)34+">");
		tokens.add(new Token("Cadena",(char)34+lexema+(char)34, linea));
		return 0;
	} else if(caracter==10 || caracter==13) {
		throw new Exception("Error lexico [linea "+linea/2+"]: Cadena no válida");
	} else	{
		//Si no es ", continua con la cadena
		lexema += caracter;
		cont++;
		return 6;
	}
}

//Estado para += y +
public static int automata8(char caracter) {
	if(caracter=='=') {
		//System.out.println("<AsignacionSuma,>");
		tokens.add(new Token("AsignacionSuma", linea));
		return 0;
	}
	else {
		//System.out.println("<Suma,>");
		tokens.add(new Token("Suma", linea));
		return -1;
	}
}

//Estado que genera id
public static int automata11(char caracter) throws Exception {
	//Comprueba si es letra, numero o _
	if((caracter>=65 && caracter<=90)||(caracter>=97 && caracter<=122)||(caracter>=48 && caracter<=57)
			||caracter=='_') {
		lexema += caracter;
		cont++;
		return 11;
	}
	//Si no, genera token
	else {
		if(!PRes(lexema)){ //Si no es palabra, genera token id			
			//System.out.println("<Id,"+id+">");
			tokens.add(new Token("Id", linea, lexema));
		}
		return -1;	
	}
}

//Estado para comentarios
public static int automata20(char caracter) throws Exception {
	if(caracter=='/') {
		return 21;
	}
	throw new Exception("Error lexico [linea "+linea/2+"]: Comentario erroneo");
}

public static int automata21(char caracter) throws Exception {
	if(caracter==10) {
		linea++;
		return 0;
	} else
		return 21;
}

//Funcion que comprueba si es palabra reservada
//(Si lo es, genera token)
public static boolean PRes(String str){
	switch(str) {
		case "let":
			//System.out.println("<Let,>");
			tokens.add(new Token("Let", linea));
			return true;
		case "if":
			//System.out.println("<If,>");
			tokens.add(new Token("If", linea));
			return true;
		case "else":
			//System.out.println("<Else,>");
			tokens.add(new Token("Else", linea));
			return true;
		case "function":
			//System.out.println("<Function,>");
			tokens.add(new Token("Function", linea));
			return true;
		case "number":
			//System.out.println("<TipoNumber,>");
			tokens.add(new Token("TipoNumber", linea));
			return true;
		case "boolean":
			//System.out.println("<TipoBoolean,>");
			tokens.add(new Token("TipoBoolean", linea));
			return true;
		case "string":
			//System.out.println("<TipoCadena,>");
			tokens.add(new Token("TipoCadena", linea));
			return true;
		case "return":
			//System.out.println("<Return,>");
			tokens.add(new Token("Return", linea));
			return true;
		case "alert":
			//System.out.println("<Alert,>");
			tokens.add(new Token("Alert", linea));
			return true;
		case "input":
			//System.out.println("<Input,>");
			tokens.add(new Token("Input", linea));
			return true;
		default:
			return false;
	}
}

}
