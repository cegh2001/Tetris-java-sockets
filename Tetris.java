import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.JOptionPane;
import java.io.IOException;


import javax.swing.JFrame;

/**
 * La clase {@code Tetris} es responsable del manejo de gran parte de la
 * logica del juego y de la lectura de los inputs del usuario.
 *
 */
public class Tetris extends JFrame {
	
	/**
	 * El UID de versión serial.
	 */
	private static final long serialVersionUID = -4722429764792514382L;

	private static TetrisServer server;

	private Leaderboard leaderboard = new Leaderboard();

	/**
	 * El número de milisegundos por fotograma.
	 */
	private static final long FRAME_TIME = 1000L / 50L;
	
	/**
	 * El número de piezas existentes.
	 */
	private static final int TYPE_COUNT = TileType.values().length;
		
	/**
	 * La instancia del panel de tablero (BoardPanel).
	 */
	private BoardPanel board;
	
	/**
	 * La instancia del panel lateral (SidePanel).
	 */
	private SidePanel side;
	
	/**
	 * Indica si el juego está pausado o no.
	 */
	private boolean isPaused;
	
	/**
	 * Indica si hemos jugado una partida aún. Se establece en verdadero
	 * inicialmente y luego se establece en falso cuando comienza el juego.
	 */
	private boolean isNewGame;
	
	/**
	 * Indica si el juego ha terminado.
	 */
	public boolean isGameOver;
	
	/**
	 * El nivel actual en el que nos encontramos.
	 */
	private int level;
	
	/**
	 * La puntuación actual.
	 */
	private int score;
	
	/**
	 * El generador de números aleatorios. Se utiliza para
	 * generar piezas de forma aleatoria.
	 */
	private Random random;
	
	/**
	 * El reloj que maneja la lógica de actualización.
	 */
	private Clock logicTimer;
				
	/**
	 * El tipo de pieza actual.
	 */
	private TileType currentType;
	
	/**
	 * El siguiente tipo de pieza.
	 */
	private TileType nextType;
		
	/**
	 * La columna actual de nuestra pieza.
	 */
	private int currentCol;
	
	/**
	 * La fila actual de nuestra pieza.
	 */
	private int currentRow;
	
	/**
	 * La rotación actual de nuestra pieza.
	 */
	private int currentRotation;
		
	/**
	 * Asegura que pase cierta cantidad de tiempo después de que se
	 * genere una pieza antes de que podamos dejarla caer.
	 */
	private int dropCooldown;
	
	/**
	 * La velocidad del juego.
	 */
	private float gameSpeed;
		
	/**
	 * Crea una nueva instancia de Tetris. Configura las propiedades de la ventana
	 * y agrega un controlador de eventos.
	 */
	private Tetris() {
		/*
		 * Establece las propiedades básicas de la ventana.
		 */
		super("TetrisAfroAsiatico");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		
		/*
		 * Inicializa las instancias del panel de tablero (BoardPanel) y del panel lateral (SidePanel).
		 */
		this.board = new BoardPanel(this);
		this.side = new SidePanel(this);
		
		/*
		 * Agrega las instancias del panel de tablero (BoardPanel) y del panel lateral (SidePanel) a la ventana.
		 */
		add(board, BorderLayout.CENTER);
		add(side, BorderLayout.EAST);
		
		/*
		 * Agrega un KeyListener anónimo personalizado al marco (frame).
		 */
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
								
				switch(e.getKeyCode()) {
				
				/*
				 * Soltar - Cuando se presiona, verificamos que el juego no esté pausado
				 * y que no haya un tiempo de espera para soltar, luego establecemos el
				 * temporizador lógico para que se ejecute a una velocidad de 25 ciclos por segundo.
				 */
				case KeyEvent.VK_S:
					if(!isPaused && dropCooldown == 0) {
						logicTimer.setCyclesPerSecond(25.0f);
					}
					break;
					
				/*
				 * Mover a la izquierda - Cuando se presiona, verificamos que el juego no esté pausado
				 * y que la posición a la izquierda de la posición actual sea válida. Si es así, decrementamos
				 * la columna actual en 1.
				 */
				case KeyEvent.VK_A:
					if(!isPaused && board.isValidAndEmpty(currentType, currentCol - 1, currentRow, currentRotation)) {
						currentCol--;
					}
					break;
					
				/*
				 * Mover a la derecha - Cuando se presiona, verificamos que el juego no esté pausado
				 * y que la posición a la derecha de la posición actual sea válida. Si es así, incrementamos
				 * la columna actual en 1.
				 */
				case KeyEvent.VK_D:
					if(!isPaused && board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {
						currentCol++;
					}
					break;
					
				/*
				 * Rotar en sentido contrario a las agujas del reloj - Cuando se presiona, verificamos que el juego no esté pausado
				 * y luego intentamos rotar la pieza en sentido contrario a las agujas del reloj. Debido al tamaño y
				 * complejidad del código de rotación, así como a su similitud con la rotación en el sentido de las agujas del reloj,
				 * el código para la rotación de la pieza se maneja en otro método.
				 */
				case KeyEvent.VK_Q:
					if(!isPaused) {
						rotatePiece((currentRotation == 0) ? 3 : currentRotation - 1);
					}
					break;
				
				/*
				 * Rotar en el sentido de las agujas del reloj - Cuando se presiona, verificamos que el juego no esté pausado
				 * y luego intentamos rotar la pieza en el sentido de las agujas del reloj. Debido al tamaño y
				 * complejidad del código de rotación, así como a su similitud con la rotación en sentido contrario a las agujas del reloj,
				 * el código para la rotación de la pieza se maneja en otro método.
				 */
				case KeyEvent.VK_E:
					if(!isPaused) {
						rotatePiece((currentRotation == 3) ? 0 : currentRotation + 1);
					}
					break;
					
				/*
				 * Pausar el juego - Cuando se presiona, verificamos que estemos jugando actualmente.
				 * Si es así, cambiamos el estado de pausa (isPaused) y actualizamos el temporizador lógico
				 * para reflejar este cambio. De lo contrario, el juego ejecutará una gran cantidad de actualizaciones y esencialmente
				 * causará un "game over" instantáneo al reanudar el juego si permanecemos en pausa durante más de
				 * un minuto aproximadamente.
				*/
				case KeyEvent.VK_P:
					if(!isGameOver && !isNewGame) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
					}
					break;
				
				/*
				 * Iniciar el juego - Cuando se presiona, verificamos si estamos en el estado de "game over" o "new game".
 				 * Si es así, reiniciamos el juego.
				 */
				case KeyEvent.VK_ENTER:
					if(isGameOver || isNewGame) {
						resetGame();
					}
					break;

				 case KeyEvent.VK_L: // Detectar la tecla “L”
				 	leaderboard.mostrarTabla(); // Mostrar el leaderboard
				 	break;
				
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
				switch(e.getKeyCode()) {
				
				/*
				 * Soltar - Cuando se suelta, establecemos la velocidad del temporizador lógico
				 * a la velocidad actual del juego y eliminamos cualquier ciclo que aún esté transcurriendo.
				 */
				case KeyEvent.VK_S:
					logicTimer.setCyclesPerSecond(gameSpeed);
					logicTimer.reset();
					break;
				}
				
			}
			
		});
		
		/*
		 * Aquí redimensionamos la ventana (frame) para contener las instancias de BoardPanel y SidePanel,
		 * centramos la ventana en la pantalla y la mostramos al usuario.
		 */
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
 	* Inicia el juego. Inicializa todo y entra en el bucle del juego.
 	*/
	private void startGame() {
		/*
		* Inicializamos nuestro generador de números aleatorios, temporizador lógico y variables de nuevo juego.
		*/
		this.random = new Random();
		this.isNewGame = true;
		this.gameSpeed = 1.0f;
		
		/*
		* Configuramos el temporizador para evitar que el juego se ejecute antes de que el usuario presione enter
		* para iniciarlo.
		*/
		this.logicTimer = new Clock(gameSpeed);
		logicTimer.setPaused(true);
		
		while(true) {
			
			// Obtenemos el tiempo en que se inició el frame.
			long start = System.nanoTime();
			
			// Actualizamos el temporizador lógico.
			logicTimer.update();
			
			/*
			* Si ha transcurrido un ciclo en el temporizador, podemos actualizar el juego y
			* mover nuestra pieza actual hacia abajo.
			*/
			if(logicTimer.hasElapsedCycle()) {
				updateGame();
			}
		
			// Decrementamos la "drop cooldown" si es necesario.
			if(dropCooldown > 0) {
				dropCooldown--;
			}
			
			// Mostramos la ventana al usuario.
			renderGame();
			
			/*
			* Dormir para limitar la velocidad de fotogramas.
			*/
			long delta = (System.nanoTime() - start) / 1000000L;
			if(delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			

		}
	}
	
	public static void  iniciarServidor(int port) {
        server = new TetrisServer();
        try {
            server.start(port);
            System.out.println("Servidor iniciado en el puerto " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Actualiza el juego y maneja la mayor parte de su lógica.
	 */
	private void updateGame() {
		/*
		* Comprueba si la posición de la pieza puede moverse hacia abajo a la siguiente fila.
		*/
		if (board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
			// Incrementa la fila actual si es seguro hacerlo.
			currentRow++;
		} else {
			/*
			* Hemos llegado al fondo del tablero o aterrizado sobre otra pieza, por lo que
			* necesitamos agregar la pieza al tablero.
			*/
			board.addPiece(currentType, currentCol, currentRow, currentRotation);

			/*
			* Comprueba si al agregar la nueva pieza se han eliminado líneas completas. En caso afirmativo,
			* incrementa la puntuación del jugador. (Se pueden eliminar hasta 4 líneas de una sola vez;
			* [1 = 100 puntos, 2 = 200 puntos, 3 = 400 puntos, 4 = 800 puntos]).
			*/
			int cleared = board.checkLines();
			if (cleared > 0) {
				score += 50 << cleared;
			}

			/*
			* Aumenta ligeramente la velocidad para la siguiente pieza y actualiza el temporizador del juego
			* para reflejar el aumento.
			*/
			gameSpeed += 0.035f;
			logicTimer.setCyclesPerSecond(gameSpeed);
			logicTimer.reset();

			/*
			* Establece el tiempo de espera para que la siguiente pieza no aparezca automáticamente
			* inmediatamente después de que esta pieza toque el fondo, si aún no hemos reaccionado.
			* (aproximadamente 0.5 segundos de margen).
			*/
			dropCooldown = 25;

			/*
			* Actualiza el nivel de dificultad. Esto no tiene ningún efecto en el juego y solo se
			* utiliza en la cadena "Nivel" en el SidePanel.
			*/
			level = (int) (gameSpeed * 1.70f);

			/*
			* Genera una nueva pieza para controlar.
			*/
			spawnPiece();
		}
	}

	/**
	 * Fuerza al BoardPanel y al SidePanel a repintarse.
	 */
	private void renderGame() {
		board.repaint();
		side.repaint();
	}

	/**
	 * Restablece las variables del juego a sus valores predeterminados al inicio de un nuevo juego.
	 */
	private void resetGame() {
		this.level = 1;
		this.score = 0;
		this.gameSpeed = 1.0f;
		this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
		this.isNewGame = false;
		this.isGameOver = false;
		board.clear();
		logicTimer.reset();
		logicTimer.setCyclesPerSecond(gameSpeed);
		spawnPiece();
	}
		
	/**
	 * Genera una nueva pieza y restablece las variables de la pieza a sus valores predeterminados.
	 */
	private void spawnPiece() {
		/*
		* Extrae la última pieza y restablece nuestra posición y rotación a sus variables predeterminadas,
		* luego elige la siguiente pieza a utilizar.
		*/
		this.currentType = nextType;
		this.currentCol = currentType.getSpawnColumn();
		this.currentRow = currentType.getSpawnRow();
		this.currentRotation = 0;
		this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
		
		/*
		* Si el punto de generación es inválido, debemos pausar el juego y marcar que hemos perdido,
		* porque significa que las piezas en el tablero se han acumulado demasiado alto.
		*/
		if (!board.isValidAndEmpty(currentType, currentCol, currentRow, currentRotation)) {
			String nombre = JOptionPane.showInputDialog("Fin del juego \n Por favor, introduce tu nombre aquí:");
    
                        // Crear un objeto Jugador con el nombre y la puntuación del jugador
                        Jugador jugador = new Jugador(nombre, score);
                        Leaderboard leaderboard = new Leaderboard();

                        // Agregar el jugador a la tabla de líderes
                        leaderboard.agregarJugador(jugador);

                        enviarScore(jugador);

                        // Mostrar la tabla de líderes
                        leaderboard.mostrarTabla();

			this.isGameOver = true;
			logicTimer.setPaused(true);
		}       
	}

	private void enviarScore(Jugador jugador) {
        System.out.printf("Enviando puntuación al servidor...%n");
       Multijugador multijugador = new Multijugador();
        TetrisClient client = new TetrisClient();
        try {
            String serverIP = multijugador.getIp(); // IP del servidor
            int serverPort = multijugador.getPort(); // Puerto del servidor
            client.startConnection(serverIP, serverPort);

            // Enviar nombre y puntuación al servidor
            client.sendScore(jugador.getNombre(), jugador.getScore());
            System.out.printf("Puntuación enviada al servidor.%n");

            // Cerrar la conexión con el servidor
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	/**
	 * Intenta establecer la rotación de la pieza actual a newRotation.
	 * @param newRotation La rotación de la nueva pieza.
	 */
	private void rotatePiece(int newRotation) {
		/*
		* A veces las piezas tendrán que moverse al rotar para evitar salir del tablero
		* (la pieza I es un buen ejemplo de esto). Aquí almacenamos una fila y columna temporal en caso
		* de que también necesitemos mover la ficha.
		*/
		int newColumn = currentCol;
		int newRow = currentRow;
		
		/*
		* Obtiene los desplazamientos para cada uno de los lados. Se utilizan para determinar cuántas filas
		* o columnas vacías hay en cada lado.
		*/
		int left = currentType.getLeftInset(newRotation);
		int right = currentType.getRightInset(newRotation);
		int top = currentType.getTopInset(newRotation);
		int bottom = currentType.getBottomInset(newRotation);
		
		/*
		* Si la pieza actual está demasiado a la izquierda o a la derecha, mueve la pieza lejos de los bordes
		* para evitar que la pieza se salga del mapa y se vuelva automáticamente inválida.
		*/
		if (currentCol < -left) {
			newColumn -= currentCol - left;
		} else if (currentCol + currentType.getDimension() - right >= BoardPanel.COL_COUNT) {
			newColumn -= (currentCol + currentType.getDimension() - right) - BoardPanel.COL_COUNT + 1;
		}
		
		/*
		* Si la pieza actual está demasiado arriba o abajo, mueve la pieza lejos de los bordes
		* para evitar que la pieza se salga del mapa y se vuelva automáticamente inválida.
		*/
		if (currentRow < -top) {
			newRow -= currentRow - top;
		} else if (currentRow + currentType.getDimension() - bottom >= BoardPanel.ROW_COUNT) {
			newRow -= (currentRow + currentType.getDimension() - bottom) - BoardPanel.ROW_COUNT + 1;
		}
		
		/*
		* Comprueba si la nueva posición es aceptable. Si lo es, actualiza la rotación y
		* posición de la pieza.
		*/
		if (board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) {
			currentRotation = newRotation;
			currentRow = newRow;
			currentCol = newColumn;
		}
	}
	
	/**
	 * Comprueba si el juego está pausado o no.
	 * @return Verdadero si el juego está pausado, falso de lo contrario.
	 */
	public boolean isPaused() {
		return isPaused;
	}

	/**
	 * Comprueba si el juego ha terminado o no.
	 * @return Verdadero si el juego ha terminado, falso de lo contrario.
	 */
	public boolean isGameOver() {
		return isGameOver;
	}

	/**
	 * Comprueba si es un nuevo juego o no.
	 * @return Verdadero si es un nuevo juego, falso de lo contrario.
	 */
	public boolean isNewGame() {
		return isNewGame;
	}

	/**
	 * Obtiene la puntuación actual.
	 * @return La puntuación.
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Obtiene el nivel actual.
	 * @return El nivel.
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Obtiene el tipo de pieza actual que se está utilizando.
	 * @return El tipo de pieza.
	 */
	public TileType getPieceType() {
		return currentType;
	}

	/**
	 * Obtiene el siguiente tipo de pieza que se utilizará.
	 * @return La siguiente pieza.
	 */
	public TileType getNextPieceType() {
		return nextType;
	}

	/**
	 * Obtiene la columna de la pieza actual.
	 * @return La columna.
	 */
	public int getPieceCol() {
		return currentCol;
	}

	/**
	 * Obtiene la fila de la pieza actual.
	 * @return La fila.
	 */
	public int getPieceRow() {
		return currentRow;
	}

	/**
	 * Obtiene la rotación de la pieza actual.
	 * @return La rotación.
	 */
	public int getPieceRotation() {
		return currentRotation;
	}

	/**
	 * Punto de entrada del juego. Responsable de crear y comenzar una nueva instancia de juego.
	 * @param args Sin uso.
	 */

	 public static void main(String[] args) {
		Multijugador multijugador = new Multijugador();
	
		Thread serverThread = new Thread(() -> {
			iniciarServidor(multijugador.getPort());
		});
	
		serverThread.start();
	
		Tetris tetris = new Tetris();
		tetris.startGame();
	}
	

}