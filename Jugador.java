public class Jugador implements Comparable<Jugador> {
    private String nombre;
    private int score;

    public Jugador(String nombre, int score) {
        this.nombre = nombre;
        this.score = score;
    }

    public String getNombre() {
        return nombre;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(Jugador otroJugador) {
        // Comparar por puntuación de mayor a menor
        return Integer.compare(otroJugador.getScore(), this.score);
    }
}

