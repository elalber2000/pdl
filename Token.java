package Analizador;

public class Token {

	private String nombre = "";
	private int num = 0;
	private String lexema = "";
	private boolean id = false;
	private boolean valor = false;
	private boolean cadena = false;
	private int linea;
	
	public Token(String nombre, int linea) {
		this.nombre = nombre;
		this.linea = linea/2;
	}
	
	public Token(String nombre, int linea, String lexema) {
		this.nombre = nombre;
		this.lexema = lexema;
		this.linea = linea/2;
		if(nombre.equals("Id"))
			this.id = true;
	}
	
	public Token(String nombre, int num, int linea) {
		this.nombre = nombre;
		this.num = num;
		this.linea = linea/2;
		valor = true;
	}
	
	public Token(String nombre, String lexema, int linea) {
		this.nombre = nombre;
		this.lexema = lexema;
		this.linea = linea/2;
		cadena = true;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getLexema() {
		return lexema;
	}

	public void setLexema(String lexema) {
		this.lexema = lexema;
	}
	
	public int getLinea() {
		return linea;
	}

	public boolean isId() {
		return id;
	}

	public boolean isValor() {
		return valor;
	}

	public boolean isCadena() {
		return cadena;
	}
	
	public String toString() {
		if(valor)
			return "<"+nombre+","+num+">";
		if(cadena)
			return "<"+nombre+","+lexema+">";
		if(id)
			return "<"+nombre+","+num+">";
		else
			return "<"+nombre+",>";
	}
}
