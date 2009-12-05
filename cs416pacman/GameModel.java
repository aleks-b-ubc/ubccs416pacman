import java.awt.*;
import java.io.Serializable;

// GameState is primarly maintained by the int[][] m_gameState
// 2D array where each integer is a location in the maze.  Integers
// consists of GameState Values ORd together.  By using bitwise
// operations, looking up and manipulating the gamestate can be done
// quickly.
@SuppressWarnings("serial")
public class GameModel implements Serializable{
	// Gamestate Values
	// GS_**** where in this location, a wall exists in the **** direction.
	static final int GS_NORTH = 1 << 0;
	static final int GS_EAST = 1 << 1;
	static final int GS_SOUTH = 1 << 2;
	static final int GS_WEST = 1 << 3;
	static final int GS_FOOD = 1 << 4; // Food == 10 Points
	static final int GS_POWERUP = 1 << 5; // Powerup == 50 Points

	// PAL_**** where in this location, a certain type of wall
	// must be drawn. These game state values tells the View (GameUI)
	// how to draw the maze.
	static final int PAL_BEND_TOPLEFT = 1 << 6;
	static final int PAL_BEND_BOTTOMLEFT = 1 << 7;
	static final int PAL_BEND_BOTTOMRIGHT = 1 << 8;
	static final int PAL_BEND_TOPRIGHT = 1 << 9;

	static final int PAL_EDGE_TOP = 1 << 10;
	static final int PAL_EDGE_LEFT = 1 << 12;
	static final int PAL_EDGE_BOTTOM = 1 << 13;
	static final int PAL_EDGE_RIGHT = 1 << 14;

	static final int PAL_LINE_HORIZ = 1 << 15;
	static final int PAL_LINE_VERT = 1 << 16;

	// Trying to model a FSM where the game is always
	// in one of the below States
	static final int STATE_START = 0; // Not used.
	static final int STATE_DEADPACMAN = 1; // Pacman's dead. Use next life or
											// GameOver?
	static final int STATE_GAMEOVER = 2; // All lives gone. Game Over
	static final int STATE_LEVELCOMPLETE = 3; // Flash the board and advance the
												// level
	static final int STATE_PLAYING = 4; // Normal playing
	static final int STATE_BEGIN_PLAY = 5; // Animation during start of play
											// "Ready !"
	static final int STATE_DEAD_PLAY = 6; // Animation of Pacman dying
	static final int STATE_NEWGAME = 7; // Initialization of a new game
	static final int STATE_PAUSED = 8; // Used to pause the game
	static final int STATE_INTRO = 9; // Intro to game with nice JPEG banner
	static final int STATE_MULTIPLAYER_WAITROOM = 10; // Used to go go the Multiplayer waiting page
	static final int STATE_SET_UP_CONNECTION = 13;		//Hosting a multiplayer game
	static final int STATE_HOSTING = 14; //After hosting is complete.
														//wait for connection
	static final int STATE_CONNECT = 15;	//Connecting to a multiplayer game
	
	 

	int[][] m_gameState; // Represents maze as integers
	int m_gameSizeX;
	int m_gameSizeY;
	int m_stage; // Same as level of difficulty
	int m_state = STATE_INTRO; // FSM state
	int m_pausedState; // Save FSM state when game is paused
	Thing[] m_things; // Contains references to Pacman and Ghosts
	Ghost[] m_ghosts; // Four Ghosts
	GhostPlayer m_ghostPlayer;
	Player m_player; // Pacman
	PacMan m_pacMan; // Controller
	Fruit m_fruit; // Wandering fruits
	int m_highScore = 10000; // Default highscore
	int m_nextFreeUp = 10000; // Every this many points, earn another life
	int m_doorLocX = 13; // Bad.. Hard code location of door
	int m_doorLocY = 12; // Bad.. Hard code location of door

	// TODO: This is where the multiplayer stuff lies
	// Variables for Multiplayer
	int numberPlayers = 1; // Number of human players
	boolean localMultiplayer = false; // Is set to true of the game is local multiplayer.

	// Variables for Powerup
	int m_nTicksPowerup; // Number of ticks power up lasts.

	// Variables for Level Complete animation
	int m_nTicks2LevelComp = 0;

	// Variables for begin play intro
	int m_nTicks2BeginPlay = 0;
	int m_readyY = 17; // Bad... Hard code location of Ready! string
	boolean m_bPlayStartClip = true;
	boolean m_bStartClipPlayed = false; // Only play start clip on a new game

	// Variables for deadPlay, i.e. animation for Pacman dying
	int m_nTicks2DeadPlay;
	int m_nOrigTicksPerSecond; // Saves the game speed before going into the
								// animation

	// Variables for tickGamePlay
	int m_nLives = 2; // Remaining Pacman lives
	int m_eatGhostPoints = 200; // Worth of next eaten ghost
	int m_totalFoodCount = 0; // Total food remaining in the maze
	int m_currentFoodCount = 0; // Number of food eaten thus far
	int m_nTicks2Backoff; // Every so often Ghosts backoff of Pacman

	// Variables for tickIntro, i.e. intro animation
	boolean m_bIntroInited = false;

	// Variable associated with about page
	int m_nTicks2AboutShow = 0;

	// Variable associated with game over
	int m_nTicks2GameOver = 0;

	GameModel(PacMan pacMan) {
		super();
		m_pacMan = pacMan;
		m_stage = 1;
		
		// Ghosts and Pacman
		m_player = new Player(this, Thing.PACMAN, 13, 23, true);
		m_ghosts = new Ghost[4];

		//By default all ghosts are set to AI ghosts. I'd rather not have it here
		//But moving it breaks a lot of things. 
		m_ghosts[0] = new GhostAI(this, Thing.GHOST, 13, 11, true, Color.red, 0);
		m_ghosts[1] = new GhostAI(this, Thing.GHOST, 12, 14, false, Color.pink,
				2000);
		m_ghosts[2] = new GhostAI(this, Thing.GHOST, 13, 14, true, Color.cyan,
				4000);
		m_ghosts[3] = new GhostAI(this, Thing.GHOST, 15, 14, false,
				Color.orange, 6000);

		// Fruit
		m_fruit = new Fruit(this, Thing.FRUIT, 13, 17, true);

		// GameState
		m_gameSizeX = 28;
		m_gameSizeY = 31;
		m_gameState = new int[m_gameSizeX][m_gameSizeY];

		fillThingArray();
		
	}

	// Use bitwise operations to test for gameCell contents
	static public boolean hasNorthWall(short gameCell) {
		return (gameCell & GS_NORTH) != 0;
	}

	static public boolean hasEastWall(short gameCell) {
		return (gameCell & GS_EAST) != 0;
	}

	static public boolean hasSouthWall(short gameCell) {
		return (gameCell & GS_SOUTH) != 0;
	}

	static public boolean hasWestWall(short gameCell) {
		return (gameCell & GS_WEST) != 0;
	}

	public void fillThingArray() {

		int thingsLength = m_ghosts.length + 2; // Plus 1 for the player and 1
												// for the fruit
		m_things = new Thing[thingsLength];
		m_things[0] = m_player;
		m_things[1] = m_fruit;
		m_things[2] = m_ghosts[0];
		m_things[3] = m_ghosts[1];
		m_things[4] = m_ghosts[2];
		m_things[5] = m_ghosts[3];
	}

	// This method will fill in the right half of the gamestate based on
	// the left half assuming symmetrical design.
	// Deprecated now that I have the Level Designer App
	public void completeGameState() {
		short gameState = 0;
		for (int x = 0; x < m_gameSizeX / 2; x++) {
			for (int y = 0; y < m_gameSizeY; y++) {
				if ((m_gameState[x][y] & GS_NORTH) != 0)
					gameState |= GS_NORTH;
				if ((m_gameState[x][y] & GS_SOUTH) != 0)
					gameState |= GS_SOUTH;
				if ((m_gameState[x][y] & GS_WEST) != 0)
					gameState |= GS_EAST;
				if ((m_gameState[x][y] & GS_EAST) != 0)
					gameState |= GS_WEST;
				if ((m_gameState[x][y] & GS_FOOD) != 0)
					gameState |= GS_FOOD;
				if ((m_gameState[x][y] & GS_POWERUP) != 0)
					gameState |= GS_POWERUP;

				m_gameState[m_gameSizeX - x - 1][y] |= gameState;
				gameState = 0;
			}
		}
	}

	// Pause Pacman and Ghosts
	public void setPausedGame(boolean bPaused) {
		for (int i = 0; i < m_things.length; i++) {
			m_things[i].setPaused(bPaused);
		}
	}

	// Hide/Show Pacman and Ghosts
	public void setVisibleThings(boolean bVisible) {
		for (int i = 0; i < m_things.length; i++) {
			m_things[i].setVisible(bVisible);
		}
	}

	// Called when Pacman eats a Powerup
	public void eatPowerup() {
		int gameState;
		for (int i = 0; i < m_ghosts.length; i++) {
			if (m_ghosts[i].m_bEaten)
				continue;

			m_ghosts[i].m_deltaMax = m_ghosts[i].m_ghostDeltaMax + 2; // Slow
																		// down
																		// the
																		// ghosts
																		// so
																		// they're
																		// easier
																		// to
																		// eat
			m_ghosts[i].m_nTicks2Flee = m_nTicksPowerup;
			gameState = m_gameState[m_ghosts[i].m_locX][m_ghosts[i].m_locY];

			// Don't disturb destination of Ghost when it's leaving or entering
			// the Hideout
			if ((m_ghosts[i].m_destinationX == m_doorLocX && // Entering
					m_ghosts[i].m_destinationY == m_doorLocY + 2 && m_ghosts[i].m_direction == Thing.DOWN)
					|| (m_ghosts[i].m_destinationX == m_doorLocX && // Leaving
							m_ghosts[i].m_destinationY == m_doorLocY - 1 && m_ghosts[i].m_direction == Thing.UP))
				continue;

			m_ghosts[i].m_destinationX = -1;
			m_ghosts[i].m_destinationY = -1;
		}
		m_eatGhostPoints = 200; // Reset the next eaten ghost's worth
	}

	// Returns the total count of Food and Powerups
	// that Pacman must eat for the current gameState
	// to complete the level.
	public int getTotalFoodCount() {
		int foodCount = 0;
		for (int x = 0; x < m_gameSizeX; x++) {
			for (int y = 0; y < m_gameSizeY; y++) {
				if ((m_gameState[x][y] & GS_FOOD) != 0)
					foodCount++;
				if ((m_gameState[x][y] & GS_POWERUP) != 0)
					foodCount++;
			}
		}
		return foodCount;
	}

	// Advances to next Level
	public void loadNextLevel() {
		int ticksPerSecond;

		m_stage++;
		// There are five different game boards which are
		// rotated after a board is completed twice
		switch (m_stage % 10) {
		case 1:
		case 2:
			loadPacManMaze();
			m_pacMan.m_gameUI.m_wallColor = Color.blue;
			m_pacMan.m_gameUI.m_wallAltColor = Color.white;
			break;
		case 3:
		case 4:
			loadMsPacManMaze1();
			m_pacMan.m_gameUI.m_wallColor = Color.red;
			m_pacMan.m_gameUI.m_wallAltColor = Color.white;
			break;
		case 5:
		case 6:
			loadMsPacManMaze2();
			m_pacMan.m_gameUI.m_wallColor = new Color(0, 255, 0); // Green
			m_pacMan.m_gameUI.m_wallAltColor = Color.white;
			break;
		case 7:
		case 8:
			loadMsPacManMaze3();
			m_pacMan.m_gameUI.m_wallColor = Color.cyan;
			m_pacMan.m_gameUI.m_wallAltColor = Color.white;
			break;
		case 9:
		case 0:
			loadMsPacManMaze4();
			m_pacMan.m_gameUI.m_wallColor = Color.magenta;
			m_pacMan.m_gameUI.m_wallAltColor = Color.white;
			break;
		}

		// Patch the maze for the ghost hideout
		loadGhostHideout();

		// For level 1-10, speed goes up by 3 ticks. After 10, speed goes up 1
		// tick per level.
		if (m_stage > 10)
			ticksPerSecond = 62 + m_stage - 10;
		else
			ticksPerSecond = 35 + (m_stage - 1) * 3;

		m_pacMan.setTicksPerSec(ticksPerSecond);

		// Decrease the blue ghost time up to the last board
		if (m_stage > 10)
			m_nTicksPowerup = 1000 / m_pacMan.m_delay;
		else
			m_nTicksPowerup = (10000 - (m_stage - 1) * 1000) / m_pacMan.m_delay;

		// Put Things back to start location
		for (int i = 0; i < m_things.length; i++) {
			m_things[i].returnToStart();
		}

		setVisibleThings(false);

		// Get food count for new board
		m_totalFoodCount = getTotalFoodCount();
		m_currentFoodCount = 0;

		// Make the fruit available for the level
		m_fruit.m_bAvailable = true;
		m_fruit.m_nTicks2Show = 15000 / m_pacMan.m_delay;

		// Recalculate the back off time of the ghost
		m_nTicks2Backoff = 20000 / m_pacMan.m_delay;

	}
	public GhostPlayer ghostAIToPlayer(Ghost ghost){
		return new GhostPlayer(this, Thing.GHOST, ghost.m_startX, ghost.m_startY, ghost.m_bMiddle,
				ghost.m_color, ghost.m_nExitMilliSec);
	}
	
	public GhostAI ghostPlayerToAI(Ghost ghost){
		return new GhostAI(this, Thing.GHOST, ghost.m_startX, ghost.m_startY, ghost.m_bMiddle,
				ghost.m_color, ghost.m_nExitMilliSec);
	}
	public GhostAI ghostPlayerToAI2(Ghost ghost){
		GhostAI ghostAI = new GhostAI(this, Thing.GHOST, ghost.m_startX, ghost.m_startY, ghost.m_bMiddle,
				ghost.m_color, ghost.m_nExitMilliSec);
		ghostAI.m_lastDeltaLocX= ghost.m_lastDeltaLocX;
		ghostAI.m_lastDeltaLocY= ghost.m_lastDeltaLocY;
		ghostAI.m_locX= ghost.m_locX;
		ghostAI.m_locY= ghost.m_locY;
		
		ghostAI.m_bCanBackTrack = ghost.m_bCanBackTrack;
		ghostAI.m_bCanFollow = ghost.m_bCanFollow;
		ghostAI.m_bCanPredict = ghost.m_bCanPredict;
		ghostAI.m_bCanUseNextBest = ghost.m_bCanUseNextBest;
		ghostAI.m_bEaten = ghost.m_bEaten;
		ghostAI.m_bEnteringDoor = ghost.m_bEnteringDoor;
		ghostAI.m_bInsaneAI = ghost.m_bInsaneAI;
		ghostAI.m_bOtherPolygon = ghost.m_bOtherPolygon;
		ghostAI.m_destinationX = ghost.m_destinationX;
		ghostAI.m_destinationY = ghost.m_destinationY;
		ghostAI.m_eatenPoints = ghost.m_eatenPoints;
		ghostAI.m_ghostDeltaMax = ghost.m_ghostDeltaMax;
		ghostAI.m_ghostMouthX = ghost.m_ghostMouthX;
		ghostAI.m_ghostMouthY = ghost.m_ghostMouthY;
		ghostAI.m_lastDirection = ghost.m_lastDirection;
		ghostAI.m_nExitMilliSec = ghost.m_nExitMilliSec;
		ghostAI.m_nTicks2Exit = ghost.m_nTicks2Exit;
		ghostAI.m_nTicks2Flee = ghost.m_nTicks2Flee;
		ghostAI.m_nTicks2Popup = ghost.m_nTicks2Popup;
		
		ghostAI.m_bPaused = ghost.m_bPaused;
		ghostAI.m_bInsideRoom = ghost.m_bInsideRoom;
		ghostAI.m_bVisible = ghost.m_bVisible;
		ghostAI.m_deltaLocX = ghost.m_deltaLocX;
		ghostAI.m_deltaLocY = ghost.m_deltaLocY;
		ghostAI.m_deltaStartX = ghost.m_deltaStartX;
		ghostAI.m_direction = ghost.m_direction;
		ghostAI.m_lastDeltaLocX = ghost.m_lastDeltaLocX;
		ghostAI.m_lastDeltaLocY = ghost.m_lastDeltaLocY;
		ghostAI.m_lastLocX = ghost.m_locX;
		ghostAI.m_lastLocY = ghost.m_locY;
		ghostAI.m_locX = ghost.m_locX;
		ghostAI.m_locY = ghost.m_locY;
		ghostAI.m_startX = ghost.m_startX;
		ghostAI.m_startY = ghost.m_startY;	
		
		return ghostAI;
	}
	// Called to reinitialize the game state and start a new game
	public void newGame() {

		// If the game is going to be multiplayer, we set the ghost as human.
		// Else it's an AI.
		if (localMultiplayer) {
			//m_ghostPlayer = new GhostPlayer(this, Thing.GHOST, 13, 11, true,
					//Color.red, 0);
			m_ghostPlayer = ghostAIToPlayer(m_ghosts[0]);
			m_ghosts[0] = m_ghostPlayer;
		}
		else if (m_pacMan.netMultiplayer) {
			for (int i=0; i< m_pacMan.m_numOfClients; i++){
				m_ghosts[i] = ghostAIToPlayer(m_ghosts[i]);
			}
		}
		else{
			m_ghosts[0] = new GhostAI(this, Thing.GHOST, 13, 11, true, Color.red, 0);
		}
		//then we re-fill the things array to reflect the change.
		fillThingArray();
		
		m_stage = 0;
		m_nLives = 2;
		m_bPlayStartClip = true;
		m_bStartClipPlayed = false;
		loadNextLevel();
		setVisibleThings(false);
		m_currentFoodCount = 0;
		m_player.m_score = 0;
		m_nextFreeUp = 10000;
	}

	// Initialize the gamestate for running the intro
	// animation
	public void initIntro() {
		loadIntroMaze();
		m_stage = 1;
		m_bPlayStartClip = true;

		// Put Things back to start location
		for (int i = 0; i < m_things.length; i++) {
			m_things[i].returnToStart();
		}

		// Overrides for Intro
		for (int i = 0; i < m_ghosts.length; i++) {
			m_ghosts[i].m_bInsideRoom = false;
			m_ghosts[i].m_nTicks2Exit = 0;
			m_ghosts[i].m_deltaLocX = 0;
			m_ghosts[i].m_deltaLocY = 0;
			m_ghosts[i].m_direction = Thing.LEFT;
			m_ghosts[i].m_lastLocX = 0;
			m_ghosts[i].m_lastLocY = 0;
		}

		m_ghosts[0].m_locX = 27;
		m_ghosts[0].m_locY = 10;
		m_ghosts[1].m_locX = 27;
		m_ghosts[1].m_locY = 12;
		m_ghosts[2].m_locX = 27;
		m_ghosts[2].m_locY = 14;
		m_ghosts[3].m_locX = 27;
		m_ghosts[3].m_locY = 16;

		m_fruit.m_bAvailable = false;
		m_fruit.m_lastLocX = -1;
		m_fruit.m_lastLocY = -1;

		// Overrides for Pacman
		m_player.m_locX = 0;
		m_player.m_locY = 19;
		m_player.m_lastLocX = -1;
		m_player.m_lastLocY = -1;
		m_player.m_deltaLocX = 0;
		m_player.m_deltaLocY = 0;
		m_player.m_direction = Thing.RIGHT;
		m_player.m_requestedDirection = Thing.RIGHT;

		setVisibleThings(false);
		setPausedGame(true);
		m_currentFoodCount = 0;
	}

	// Called to restart Game after Pacman is killed
	public void restartGame() {
		m_nLives--;
		// Put Things back to start location
		for (int i = 0; i < m_things.length; i++) {
			m_things[i].returnToStart();
		}
		setVisibleThings(false);
	}

	// //////////////////////////////////////////////////
	// //////////////////////////////////////////////////
	// //////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	// MAZES ARE ALL DEFINED BELOW
	// //////////////////////////////////////////////////
	// //////////////////////////////////////////////////
	// //////////////////////////////////////////////////
	// //////////////////////////////////////////////////

	// The original pacman maze!!!
	public void loadPacManMaze() {
		m_gameState[0][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | PAL_EDGE_LEFT
				| GS_SOUTH | GS_EAST;
		m_gameState[0][1] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][2] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][3] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][4] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][5] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][6] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][7] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][8] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][9] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM | PAL_EDGE_LEFT
				| GS_NORTH | GS_EAST;
		m_gameState[0][10] = GS_NORTH;
		m_gameState[0][11] = 0;
		m_gameState[0][12] = GS_SOUTH;
		m_gameState[0][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][14] = GS_NORTH | GS_SOUTH;
		m_gameState[0][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][16] = GS_NORTH;
		m_gameState[0][17] = 0;
		m_gameState[0][18] = GS_SOUTH;
		m_gameState[0][19] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP
				| PAL_EDGE_LEFT | GS_SOUTH | GS_EAST;
		m_gameState[0][20] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][21] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][22] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][23] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][24] = PAL_BEND_TOPRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][25] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][26] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][27] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][28] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][29] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM
				| PAL_EDGE_LEFT | GS_NORTH | GS_EAST;
		m_gameState[1][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][3] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[1][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][5] = GS_FOOD | GS_WEST;
		m_gameState[1][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][10] = GS_NORTH;
		m_gameState[1][11] = 0;
		m_gameState[1][12] = GS_SOUTH;
		m_gameState[1][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][14] = GS_NORTH | GS_SOUTH;
		m_gameState[1][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][16] = GS_NORTH;
		m_gameState[1][17] = 0;
		m_gameState[1][18] = GS_SOUTH;
		m_gameState[1][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][20] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][23] = GS_POWERUP | GS_WEST | GS_SOUTH;
		m_gameState[1][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][26] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[2][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][10] = GS_NORTH;
		m_gameState[2][11] = 0;
		m_gameState[2][12] = GS_SOUTH;
		m_gameState[2][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][14] = GS_NORTH | GS_SOUTH;
		m_gameState[2][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][16] = GS_NORTH;
		m_gameState[2][17] = 0;
		m_gameState[2][18] = GS_SOUTH;
		m_gameState[2][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][3] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][10] = GS_NORTH;
		m_gameState[3][11] = 0;
		m_gameState[3][12] = GS_SOUTH;
		m_gameState[3][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][14] = GS_NORTH | GS_SOUTH;
		m_gameState[3][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][16] = GS_NORTH;
		m_gameState[3][17] = 0;
		m_gameState[3][18] = GS_SOUTH;
		m_gameState[3][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][23] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[3][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][26] = GS_FOOD | GS_SOUTH;
		m_gameState[3][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][3] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][10] = GS_NORTH | GS_EAST;
		m_gameState[4][11] = GS_EAST;
		m_gameState[4][12] = GS_SOUTH | GS_EAST;
		m_gameState[4][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][14] = GS_NORTH | GS_SOUTH;
		m_gameState[4][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][16] = GS_NORTH | GS_EAST;
		m_gameState[4][17] = GS_EAST;
		m_gameState[4][18] = GS_SOUTH | GS_EAST;
		m_gameState[4][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][22] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[4][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][24] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[5][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[5][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][10] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[5][11] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[5][12] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[5][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][14] = GS_NORTH | GS_SOUTH;
		m_gameState[5][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][16] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[5][17] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[5][18] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[5][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][22] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][24] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[6][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][1] = GS_FOOD | GS_NORTH;
		m_gameState[6][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][5] = GS_FOOD;
		m_gameState[6][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][8] = GS_FOOD | GS_EAST;
		m_gameState[6][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][11] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][14] = GS_FOOD;
		m_gameState[6][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][20] = GS_FOOD;
		m_gameState[6][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][23] = GS_FOOD | GS_WEST;
		m_gameState[6][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[6][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[7][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[7][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][10] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][14] = GS_NORTH | GS_SOUTH;
		m_gameState[7][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[7][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[8][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][3] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][10] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][14] = GS_NORTH | GS_SOUTH;
		m_gameState[8][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[8][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[9][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[9][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][3] = GS_NORTH | GS_SOUTH;
		m_gameState[9][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][5] = GS_FOOD | GS_NORTH;
		m_gameState[9][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[9][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][11] = GS_NORTH | GS_WEST;
		m_gameState[9][12] = GS_WEST | GS_EAST;
		m_gameState[9][13] = GS_WEST | GS_EAST;
		m_gameState[9][14] = GS_EAST;
		m_gameState[9][15] = GS_WEST | GS_EAST;
		m_gameState[9][16] = GS_WEST | GS_EAST;
		m_gameState[9][17] = GS_WEST;
		m_gameState[9][18] = GS_WEST | GS_EAST;
		m_gameState[9][19] = GS_WEST | GS_EAST;
		m_gameState[9][20] = GS_FOOD | GS_SOUTH;
		m_gameState[9][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][23] = GS_FOOD | GS_NORTH;
		m_gameState[9][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[9][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[9][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[10][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][3] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][11] = GS_NORTH | GS_SOUTH;
		m_gameState[10][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][17] = GS_NORTH | GS_SOUTH;
		m_gameState[10][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[11][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][11] = GS_NORTH | GS_SOUTH;
		m_gameState[11][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][13] = GS_NORTH | GS_WEST;
		m_gameState[11][14] = GS_WEST;
		m_gameState[11][15] = GS_WEST | GS_SOUTH;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][17] = GS_NORTH | GS_SOUTH;
		m_gameState[11][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[12][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][5] = GS_FOOD | GS_SOUTH;
		m_gameState[12][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][8] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[12][9] = GS_WEST | GS_EAST;
		m_gameState[12][10] = GS_WEST | GS_EAST;
		m_gameState[12][11] = GS_SOUTH;
		m_gameState[12][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][13] = GS_NORTH;
		m_gameState[12][14] = 0;
		m_gameState[12][15] = GS_SOUTH;
		m_gameState[12][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][17] = GS_NORTH | GS_SOUTH;
		m_gameState[12][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][20] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[12][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][23] = GS_FOOD | GS_SOUTH;
		m_gameState[12][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][26] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[12][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][29] = GS_FOOD | GS_SOUTH;
		m_gameState[12][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[13][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][7] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[13][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][11] = GS_NORTH | GS_SOUTH;
		m_gameState[13][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][13] = GS_NORTH;
		m_gameState[13][14] = 0;
		m_gameState[13][15] = GS_SOUTH;
		m_gameState[13][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][17] = GS_NORTH | GS_SOUTH;
		m_gameState[13][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][19] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[13][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][25] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[13][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[14][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][3] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][7] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[14][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][11] = GS_NORTH | GS_SOUTH;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][13] = GS_NORTH;
		m_gameState[14][14] = 0;
		m_gameState[14][15] = GS_SOUTH;
		m_gameState[14][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][17] = GS_NORTH | GS_SOUTH;
		m_gameState[14][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][19] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[14][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][21] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][25] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[14][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[15][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][5] = GS_FOOD | GS_SOUTH;
		m_gameState[15][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][8] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[15][9] = GS_WEST | GS_EAST;
		m_gameState[15][10] = GS_WEST | GS_EAST;
		m_gameState[15][11] = GS_SOUTH;
		m_gameState[15][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][13] = GS_NORTH;
		m_gameState[15][14] = 0;
		m_gameState[15][15] = GS_SOUTH;
		m_gameState[15][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][17] = GS_NORTH | GS_SOUTH;
		m_gameState[15][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][20] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[15][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][23] = GS_FOOD | GS_SOUTH;
		m_gameState[15][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][26] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[15][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][29] = GS_FOOD | GS_SOUTH;
		m_gameState[15][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[16][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][11] = GS_NORTH | GS_SOUTH;
		m_gameState[16][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][13] = GS_NORTH | GS_EAST;
		m_gameState[16][14] = GS_EAST;
		m_gameState[16][15] = GS_SOUTH | GS_EAST;
		m_gameState[16][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][17] = GS_NORTH | GS_SOUTH;
		m_gameState[16][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[17][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][3] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][11] = GS_NORTH | GS_SOUTH;
		m_gameState[17][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][17] = GS_NORTH | GS_SOUTH;
		m_gameState[17][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[18][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[18][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][3] = GS_NORTH | GS_SOUTH;
		m_gameState[18][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][5] = GS_FOOD | GS_NORTH;
		m_gameState[18][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[18][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][11] = GS_NORTH | GS_EAST;
		m_gameState[18][12] = GS_WEST | GS_EAST;
		m_gameState[18][13] = GS_WEST | GS_EAST;
		m_gameState[18][14] = GS_WEST;
		m_gameState[18][15] = GS_WEST | GS_EAST;
		m_gameState[18][16] = GS_WEST | GS_EAST;
		m_gameState[18][17] = GS_EAST;
		m_gameState[18][18] = GS_WEST | GS_EAST;
		m_gameState[18][19] = GS_WEST | GS_EAST;
		m_gameState[18][20] = GS_FOOD | GS_SOUTH;
		m_gameState[18][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][23] = GS_FOOD | GS_NORTH;
		m_gameState[18][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[18][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[18][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[19][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][3] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][10] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][14] = GS_NORTH | GS_SOUTH;
		m_gameState[19][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[20][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[20][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][10] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][14] = GS_NORTH | GS_SOUTH;
		m_gameState[20][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[20][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[21][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][1] = GS_FOOD | GS_NORTH;
		m_gameState[21][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][5] = GS_FOOD;
		m_gameState[21][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][8] = GS_FOOD | GS_WEST;
		m_gameState[21][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][11] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][14] = GS_FOOD;
		m_gameState[21][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][20] = GS_FOOD;
		m_gameState[21][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][23] = GS_FOOD | GS_EAST;
		m_gameState[21][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[21][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[22][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[22][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][10] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[22][11] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[22][12] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[22][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][14] = GS_NORTH | GS_SOUTH;
		m_gameState[22][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][16] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[22][17] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[22][18] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[22][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][22] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][24] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][3] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][10] = GS_NORTH | GS_WEST;
		m_gameState[23][11] = GS_WEST;
		m_gameState[23][12] = GS_WEST | GS_SOUTH;
		m_gameState[23][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][14] = GS_NORTH | GS_SOUTH;
		m_gameState[23][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][16] = GS_NORTH | GS_WEST;
		m_gameState[23][17] = GS_WEST;
		m_gameState[23][18] = GS_WEST | GS_SOUTH;
		m_gameState[23][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][22] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[23][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][24] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][3] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][10] = GS_NORTH;
		m_gameState[24][11] = 0;
		m_gameState[24][12] = GS_SOUTH;
		m_gameState[24][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][14] = GS_NORTH | GS_SOUTH;
		m_gameState[24][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][16] = GS_NORTH;
		m_gameState[24][17] = 0;
		m_gameState[24][18] = GS_SOUTH;
		m_gameState[24][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][23] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[24][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][26] = GS_FOOD | GS_SOUTH;
		m_gameState[24][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[25][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][10] = GS_NORTH;
		m_gameState[25][11] = 0;
		m_gameState[25][12] = GS_SOUTH;
		m_gameState[25][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][14] = GS_NORTH | GS_SOUTH;
		m_gameState[25][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][16] = GS_NORTH;
		m_gameState[25][17] = 0;
		m_gameState[25][18] = GS_SOUTH;
		m_gameState[25][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][3] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[26][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][5] = GS_FOOD | GS_EAST;
		m_gameState[26][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][10] = GS_NORTH;
		m_gameState[26][11] = 0;
		m_gameState[26][12] = GS_SOUTH;
		m_gameState[26][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][14] = GS_NORTH | GS_SOUTH;
		m_gameState[26][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][16] = GS_NORTH;
		m_gameState[26][17] = 0;
		m_gameState[26][18] = GS_SOUTH;
		m_gameState[26][19] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][20] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][23] = GS_POWERUP | GS_SOUTH | GS_EAST;
		m_gameState[26][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][26] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[27][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][1] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][2] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][3] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][4] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][5] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][6] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][7] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][8] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][9] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
		m_gameState[27][10] = GS_NORTH;
		m_gameState[27][11] = 0;
		m_gameState[27][12] = GS_SOUTH;
		m_gameState[27][13] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][14] = GS_NORTH | GS_SOUTH;
		m_gameState[27][15] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][16] = GS_NORTH;
		m_gameState[27][17] = 0;
		m_gameState[27][18] = GS_SOUTH;
		m_gameState[27][19] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][20] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][21] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][22] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][23] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][24] = PAL_BEND_TOPLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][25] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][26] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][27] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][28] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][29] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
	}

	// The original MsPacman maze
	public void loadMsPacManMaze2() {
		m_gameState[0][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | PAL_EDGE_LEFT
				| GS_SOUTH | GS_EAST;
		m_gameState[0][1] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][2] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][3] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][4] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][5] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][6] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][7] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][8] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][9] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM | PAL_EDGE_LEFT
				| GS_NORTH | GS_EAST;
		m_gameState[0][10] = GS_NORTH;
		m_gameState[0][11] = GS_SOUTH;
		m_gameState[0][12] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][13] = GS_NORTH | GS_SOUTH;
		m_gameState[0][14] = PAL_LINE_HORIZ | GS_SOUTH | GS_EAST;
		m_gameState[0][15] = PAL_LINE_HORIZ | GS_NORTH | GS_EAST;
		m_gameState[0][16] = GS_NORTH | GS_SOUTH;
		m_gameState[0][17] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][18] = GS_NORTH;
		m_gameState[0][19] = 0;
		m_gameState[0][20] = 0;
		m_gameState[0][21] = GS_SOUTH;
		m_gameState[0][22] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP
				| PAL_EDGE_LEFT | GS_SOUTH | GS_EAST;
		m_gameState[0][23] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][24] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][25] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][26] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][27] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][28] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][29] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM
				| PAL_EDGE_LEFT | GS_NORTH | GS_EAST;
		m_gameState[1][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][3] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[1][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][10] = GS_NORTH | GS_EAST;
		m_gameState[1][11] = GS_SOUTH | GS_EAST;
		m_gameState[1][12] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][13] = GS_NORTH | GS_SOUTH;
		m_gameState[1][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][16] = GS_NORTH | GS_SOUTH;
		m_gameState[1][17] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][18] = GS_NORTH | GS_EAST;
		m_gameState[1][19] = GS_EAST;
		m_gameState[1][20] = GS_EAST;
		m_gameState[1][21] = GS_SOUTH | GS_EAST;
		m_gameState[1][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][23] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][26] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][27] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[1][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][5] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][10] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][11] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][12] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][13] = GS_NORTH | GS_SOUTH;
		m_gameState[2][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[2][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[2][16] = GS_NORTH | GS_SOUTH;
		m_gameState[2][17] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][18] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][19] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][20] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][21] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[3][3] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][5] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][6] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[3][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][8] = GS_FOOD | GS_NORTH;
		m_gameState[3][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][11] = GS_FOOD | GS_WEST;
		m_gameState[3][12] = GS_WEST | GS_EAST;
		m_gameState[3][13] = GS_SOUTH | GS_EAST;
		m_gameState[3][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][16] = GS_NORTH | GS_EAST;
		m_gameState[3][17] = GS_WEST | GS_EAST;
		m_gameState[3][18] = GS_FOOD | GS_WEST;
		m_gameState[3][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][20] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][23] = GS_FOOD | GS_SOUTH;
		m_gameState[3][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][25] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[3][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[3][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][1] = GS_FOOD | GS_NORTH;
		m_gameState[4][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][5] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[4][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][11] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][14] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[4][15] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[4][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][17] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][18] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][19] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][26] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[4][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][29] = GS_FOOD | GS_SOUTH;
		m_gameState[4][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[5][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[5][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[5][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[5][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][11] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][14] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][15] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][17] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][18] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][19] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][21] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[5][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[5][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[5][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[6][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][3] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[6][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][5] = GS_FOOD | GS_NORTH;
		m_gameState[6][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[6][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][11] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[6][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][14] = GS_FOOD | GS_WEST;
		m_gameState[6][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][18] = GS_FOOD | GS_EAST;
		m_gameState[6][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][20] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[6][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][23] = GS_FOOD | GS_NORTH;
		m_gameState[6][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[6][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[7][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][3] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[7][10] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[7][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][14] = GS_NORTH | GS_SOUTH;
		m_gameState[7][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[7][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[8][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[8][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][10] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][14] = GS_NORTH | GS_SOUTH;
		m_gameState[8][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][18] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[9][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][1] = GS_FOOD | GS_NORTH;
		m_gameState[9][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][5] = GS_FOOD | GS_EAST;
		m_gameState[9][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[9][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][11] = GS_NORTH | GS_WEST;
		m_gameState[9][12] = GS_WEST | GS_EAST;
		m_gameState[9][13] = GS_WEST | GS_EAST;
		m_gameState[9][14] = GS_EAST;
		m_gameState[9][15] = GS_WEST | GS_EAST;
		m_gameState[9][16] = GS_WEST | GS_EAST;
		m_gameState[9][17] = GS_WEST | GS_SOUTH;
		m_gameState[9][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][20] = GS_FOOD | GS_NORTH;
		m_gameState[9][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][23] = GS_FOOD | GS_EAST;
		m_gameState[9][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[9][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[9][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[10][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][5] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][11] = GS_NORTH | GS_SOUTH;
		m_gameState[10][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][17] = GS_NORTH | GS_SOUTH;
		m_gameState[10][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][20] = GS_NORTH | GS_SOUTH;
		m_gameState[10][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][22] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][24] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][3] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[11][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][5] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][11] = GS_NORTH | GS_SOUTH;
		m_gameState[11][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][13] = GS_NORTH | GS_WEST;
		m_gameState[11][14] = GS_WEST;
		m_gameState[11][15] = GS_WEST | GS_SOUTH;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][17] = GS_NORTH | GS_SOUTH;
		m_gameState[11][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][20] = GS_NORTH | GS_SOUTH;
		m_gameState[11][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][22] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][24] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[11][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[12][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][4] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[12][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][8] = GS_FOOD | GS_EAST;
		m_gameState[12][9] = GS_WEST | GS_EAST;
		m_gameState[12][10] = GS_WEST | GS_EAST;
		m_gameState[12][11] = GS_SOUTH;
		m_gameState[12][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][13] = GS_NORTH;
		m_gameState[12][14] = 0;
		m_gameState[12][15] = GS_SOUTH;
		m_gameState[12][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][17] = GS_NORTH;
		m_gameState[12][18] = GS_WEST | GS_EAST;
		m_gameState[12][19] = GS_WEST | GS_EAST;
		m_gameState[12][20] = GS_EAST;
		m_gameState[12][21] = GS_WEST | GS_EAST;
		m_gameState[12][22] = GS_WEST | GS_EAST;
		m_gameState[12][23] = GS_WEST | GS_SOUTH;
		m_gameState[12][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][26] = GS_FOOD | GS_NORTH;
		m_gameState[12][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[12][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][11] = GS_NORTH | GS_SOUTH;
		m_gameState[13][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][13] = GS_NORTH;
		m_gameState[13][14] = 0;
		m_gameState[13][15] = GS_SOUTH;
		m_gameState[13][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][17] = GS_NORTH | GS_SOUTH;
		m_gameState[13][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][19] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][23] = GS_NORTH | GS_SOUTH;
		m_gameState[13][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][28] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][29] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM | GS_NORTH
				| GS_WEST | GS_EAST;
		m_gameState[14][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][11] = GS_NORTH | GS_SOUTH;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][13] = GS_NORTH;
		m_gameState[14][14] = 0;
		m_gameState[14][15] = GS_SOUTH;
		m_gameState[14][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][17] = GS_NORTH | GS_SOUTH;
		m_gameState[14][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][19] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][21] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][23] = GS_NORTH | GS_SOUTH;
		m_gameState[14][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][28] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][29] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM | GS_NORTH
				| GS_WEST | GS_EAST;
		m_gameState[15][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[15][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][4] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[15][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][8] = GS_FOOD | GS_WEST;
		m_gameState[15][9] = GS_WEST | GS_EAST;
		m_gameState[15][10] = GS_WEST | GS_EAST;
		m_gameState[15][11] = GS_SOUTH;
		m_gameState[15][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][13] = GS_NORTH;
		m_gameState[15][14] = 0;
		m_gameState[15][15] = GS_SOUTH;
		m_gameState[15][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][17] = GS_NORTH;
		m_gameState[15][18] = GS_WEST | GS_EAST;
		m_gameState[15][19] = GS_WEST | GS_EAST;
		m_gameState[15][20] = GS_WEST;
		m_gameState[15][21] = GS_WEST | GS_EAST;
		m_gameState[15][22] = GS_WEST | GS_EAST;
		m_gameState[15][23] = GS_SOUTH | GS_EAST;
		m_gameState[15][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][26] = GS_FOOD | GS_NORTH;
		m_gameState[15][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[15][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][3] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[16][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][5] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][11] = GS_NORTH | GS_SOUTH;
		m_gameState[16][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][13] = GS_NORTH | GS_EAST;
		m_gameState[16][14] = GS_EAST;
		m_gameState[16][15] = GS_SOUTH | GS_EAST;
		m_gameState[16][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][17] = GS_NORTH | GS_SOUTH;
		m_gameState[16][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][20] = GS_NORTH | GS_SOUTH;
		m_gameState[16][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][22] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][24] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[16][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[17][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][3] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][5] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][11] = GS_NORTH | GS_SOUTH;
		m_gameState[17][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][17] = GS_NORTH | GS_SOUTH;
		m_gameState[17][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][20] = GS_NORTH | GS_SOUTH;
		m_gameState[17][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][22] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][24] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[18][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][1] = GS_FOOD | GS_NORTH;
		m_gameState[18][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][5] = GS_FOOD | GS_WEST;
		m_gameState[18][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[18][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][11] = GS_NORTH | GS_EAST;
		m_gameState[18][12] = GS_WEST | GS_EAST;
		m_gameState[18][13] = GS_WEST | GS_EAST;
		m_gameState[18][14] = GS_WEST;
		m_gameState[18][15] = GS_WEST | GS_EAST;
		m_gameState[18][16] = GS_WEST | GS_EAST;
		m_gameState[18][17] = GS_SOUTH | GS_EAST;
		m_gameState[18][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][20] = GS_FOOD | GS_NORTH;
		m_gameState[18][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][23] = GS_FOOD | GS_WEST;
		m_gameState[18][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[18][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[18][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[19][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[19][4] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][10] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][14] = GS_NORTH | GS_SOUTH;
		m_gameState[19][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][18] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[20][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][3] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][6] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[20][10] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[20][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][14] = GS_NORTH | GS_SOUTH;
		m_gameState[20][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[20][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[21][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][2] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][3] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[21][4] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][5] = GS_FOOD | GS_NORTH;
		m_gameState[21][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[21][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][11] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[21][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][14] = GS_FOOD | GS_EAST;
		m_gameState[21][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][18] = GS_FOOD | GS_WEST;
		m_gameState[21][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][20] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[21][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][23] = GS_FOOD | GS_NORTH;
		m_gameState[21][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[21][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[22][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[22][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[22][4] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[22][5] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][6] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][11] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][15] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][17] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][18] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][19] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][21] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[22][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[22][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[22][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][1] = GS_FOOD | GS_NORTH;
		m_gameState[23][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][5] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[23][6] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][11] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][14] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[23][15] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[23][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][17] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][18] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][19] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][21] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][26] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[23][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][29] = GS_FOOD | GS_SOUTH;
		m_gameState[23][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[24][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][5] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][6] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[24][7] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][8] = GS_FOOD | GS_NORTH;
		m_gameState[24][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][11] = GS_FOOD | GS_EAST;
		m_gameState[24][12] = GS_WEST | GS_EAST;
		m_gameState[24][13] = GS_WEST | GS_SOUTH;
		m_gameState[24][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][16] = GS_NORTH | GS_WEST;
		m_gameState[24][17] = GS_WEST | GS_EAST;
		m_gameState[24][18] = GS_FOOD | GS_EAST;
		m_gameState[24][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][20] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][23] = GS_FOOD | GS_SOUTH;
		m_gameState[24][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][25] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[24][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[24][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][3] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][5] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][10] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][11] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][12] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][13] = GS_NORTH | GS_SOUTH;
		m_gameState[25][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[25][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[25][16] = GS_NORTH | GS_SOUTH;
		m_gameState[25][17] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][18] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][19] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][20] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][21] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][3] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[26][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][9] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][10] = GS_NORTH | GS_WEST;
		m_gameState[26][11] = GS_WEST | GS_SOUTH;
		m_gameState[26][12] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][13] = GS_NORTH | GS_SOUTH;
		m_gameState[26][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][16] = GS_NORTH | GS_SOUTH;
		m_gameState[26][17] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][18] = GS_NORTH | GS_WEST;
		m_gameState[26][19] = GS_WEST;
		m_gameState[26][20] = GS_WEST;
		m_gameState[26][21] = GS_WEST | GS_SOUTH;
		m_gameState[26][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][23] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][26] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][27] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[26][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[27][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][1] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][2] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][3] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][4] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][5] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][6] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][7] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][8] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][9] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
		m_gameState[27][10] = GS_NORTH;
		m_gameState[27][11] = GS_SOUTH;
		m_gameState[27][12] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][13] = GS_NORTH | GS_SOUTH;
		m_gameState[27][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH;
		m_gameState[27][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST;
		m_gameState[27][16] = GS_NORTH | GS_SOUTH;
		m_gameState[27][17] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][18] = GS_NORTH;
		m_gameState[27][19] = 0;
		m_gameState[27][20] = 0;
		m_gameState[27][21] = GS_SOUTH;
		m_gameState[27][22] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][23] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][24] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][25] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][26] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][27] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][28] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][29] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
	}

	// Since the ghost hideout is drawn with the walls on the inside,
	// the ghosts will paint over the walls. We need to patch the hideout
	// to add invisble walls to make the ghosts stay within a
	// a safe boundary.
	void loadGhostHideout() {
		m_gameState[17][12] = PAL_BEND_BOTTOMLEFT;
		m_gameState[17][13] = PAL_EDGE_LEFT | PAL_LINE_VERT;
		m_gameState[17][14] = PAL_EDGE_LEFT | PAL_LINE_VERT;
		m_gameState[17][15] = PAL_EDGE_LEFT | PAL_LINE_VERT;
		m_gameState[17][16] = PAL_BEND_TOPLEFT;
		m_gameState[16][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[16][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[13][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[15][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[15][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[14][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[13][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[12][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[12][14] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[13][14] = GS_SOUTH | GS_NORTH;
		m_gameState[13][13] = 0;
		m_gameState[14][14] = GS_NORTH | GS_SOUTH;
		m_gameState[15][14] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[12][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[11][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ;
		m_gameState[10][12] = PAL_BEND_BOTTOMRIGHT;
		m_gameState[10][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT;
		m_gameState[10][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT;
		m_gameState[10][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT;
		m_gameState[10][16] = PAL_BEND_TOPRIGHT;
	}

	// Simple maze that allows the ghosts to run left
	public void loadIntroMaze() {
		m_gameState[0][0] = 0;
		m_gameState[0][1] = 0;
		m_gameState[0][2] = 0;
		m_gameState[0][3] = 0;
		m_gameState[0][4] = 0;
		m_gameState[0][5] = 0;
		m_gameState[0][6] = 0;
		m_gameState[0][7] = 0;
		m_gameState[0][8] = 0;
		m_gameState[0][9] = 0;
		m_gameState[0][10] = 0;
		m_gameState[0][11] = 0;
		m_gameState[0][12] = 0;
		m_gameState[0][13] = 0;
		m_gameState[0][14] = 0;
		m_gameState[0][15] = 0;
		m_gameState[0][16] = 0;
		m_gameState[0][17] = GS_SOUTH;
		m_gameState[0][18] = PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][19] = GS_NORTH | GS_SOUTH;
		m_gameState[0][20] = PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][21] = GS_NORTH;
		m_gameState[0][22] = 0;
		m_gameState[0][23] = 0;
		m_gameState[0][24] = 0;
		m_gameState[0][25] = 0;
		m_gameState[0][26] = 0;
		m_gameState[0][27] = 0;
		m_gameState[0][28] = 0;
		m_gameState[0][29] = 0;
		m_gameState[0][30] = 0;
		m_gameState[1][0] = 0;
		m_gameState[1][1] = 0;
		m_gameState[1][2] = 0;
		m_gameState[1][3] = 0;
		m_gameState[1][4] = 0;
		m_gameState[1][5] = 0;
		m_gameState[1][6] = 0;
		m_gameState[1][7] = 0;
		m_gameState[1][8] = 0;
		m_gameState[1][9] = 0;
		m_gameState[1][10] = 0;
		m_gameState[1][11] = 0;
		m_gameState[1][12] = 0;
		m_gameState[1][13] = 0;
		m_gameState[1][14] = 0;
		m_gameState[1][15] = 0;
		m_gameState[1][16] = 0;
		m_gameState[1][17] = GS_SOUTH;
		m_gameState[1][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][19] = GS_NORTH | GS_SOUTH;
		m_gameState[1][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][21] = GS_NORTH;
		m_gameState[1][22] = 0;
		m_gameState[1][23] = 0;
		m_gameState[1][24] = 0;
		m_gameState[1][25] = 0;
		m_gameState[1][26] = 0;
		m_gameState[1][27] = 0;
		m_gameState[1][28] = 0;
		m_gameState[1][29] = 0;
		m_gameState[1][30] = 0;
		m_gameState[2][0] = 0;
		m_gameState[2][1] = 0;
		m_gameState[2][2] = 0;
		m_gameState[2][3] = 0;
		m_gameState[2][4] = 0;
		m_gameState[2][5] = 0;
		m_gameState[2][6] = 0;
		m_gameState[2][7] = 0;
		m_gameState[2][8] = 0;
		m_gameState[2][9] = 0;
		m_gameState[2][10] = 0;
		m_gameState[2][11] = 0;
		m_gameState[2][12] = 0;
		m_gameState[2][13] = 0;
		m_gameState[2][14] = 0;
		m_gameState[2][15] = 0;
		m_gameState[2][16] = 0;
		m_gameState[2][17] = GS_SOUTH;
		m_gameState[2][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][19] = GS_NORTH | GS_SOUTH;
		m_gameState[2][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][21] = GS_NORTH;
		m_gameState[2][22] = 0;
		m_gameState[2][23] = 0;
		m_gameState[2][24] = 0;
		m_gameState[2][25] = 0;
		m_gameState[2][26] = 0;
		m_gameState[2][27] = 0;
		m_gameState[2][28] = 0;
		m_gameState[2][29] = 0;
		m_gameState[2][30] = 0;
		m_gameState[3][0] = 0;
		m_gameState[3][1] = 0;
		m_gameState[3][2] = 0;
		m_gameState[3][3] = 0;
		m_gameState[3][4] = 0;
		m_gameState[3][5] = 0;
		m_gameState[3][6] = 0;
		m_gameState[3][7] = 0;
		m_gameState[3][8] = 0;
		m_gameState[3][9] = 0;
		m_gameState[3][10] = 0;
		m_gameState[3][11] = 0;
		m_gameState[3][12] = 0;
		m_gameState[3][13] = 0;
		m_gameState[3][14] = 0;
		m_gameState[3][15] = 0;
		m_gameState[3][16] = 0;
		m_gameState[3][17] = GS_SOUTH;
		m_gameState[3][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][19] = GS_NORTH | GS_SOUTH;
		m_gameState[3][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][21] = GS_NORTH;
		m_gameState[3][22] = 0;
		m_gameState[3][23] = 0;
		m_gameState[3][24] = 0;
		m_gameState[3][25] = 0;
		m_gameState[3][26] = 0;
		m_gameState[3][27] = 0;
		m_gameState[3][28] = 0;
		m_gameState[3][29] = 0;
		m_gameState[3][30] = 0;
		m_gameState[4][0] = 0;
		m_gameState[4][1] = 0;
		m_gameState[4][2] = 0;
		m_gameState[4][3] = 0;
		m_gameState[4][4] = 0;
		m_gameState[4][5] = 0;
		m_gameState[4][6] = 0;
		m_gameState[4][7] = 0;
		m_gameState[4][8] = 0;
		m_gameState[4][9] = 0;
		m_gameState[4][10] = 0;
		m_gameState[4][11] = 0;
		m_gameState[4][12] = 0;
		m_gameState[4][13] = 0;
		m_gameState[4][14] = 0;
		m_gameState[4][15] = 0;
		m_gameState[4][16] = 0;
		m_gameState[4][17] = GS_SOUTH;
		m_gameState[4][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][19] = GS_NORTH | GS_SOUTH;
		m_gameState[4][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][21] = GS_NORTH;
		m_gameState[4][22] = 0;
		m_gameState[4][23] = 0;
		m_gameState[4][24] = 0;
		m_gameState[4][25] = 0;
		m_gameState[4][26] = 0;
		m_gameState[4][27] = 0;
		m_gameState[4][28] = 0;
		m_gameState[4][29] = 0;
		m_gameState[4][30] = 0;
		m_gameState[5][0] = 0;
		m_gameState[5][1] = 0;
		m_gameState[5][2] = 0;
		m_gameState[5][3] = 0;
		m_gameState[5][4] = 0;
		m_gameState[5][5] = 0;
		m_gameState[5][6] = 0;
		m_gameState[5][7] = 0;
		m_gameState[5][8] = 0;
		m_gameState[5][9] = 0;
		m_gameState[5][10] = 0;
		m_gameState[5][11] = 0;
		m_gameState[5][12] = 0;
		m_gameState[5][13] = 0;
		m_gameState[5][14] = 0;
		m_gameState[5][15] = 0;
		m_gameState[5][16] = 0;
		m_gameState[5][17] = GS_SOUTH;
		m_gameState[5][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][19] = GS_NORTH | GS_SOUTH;
		m_gameState[5][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][21] = GS_NORTH;
		m_gameState[5][22] = 0;
		m_gameState[5][23] = 0;
		m_gameState[5][24] = 0;
		m_gameState[5][25] = 0;
		m_gameState[5][26] = 0;
		m_gameState[5][27] = 0;
		m_gameState[5][28] = 0;
		m_gameState[5][29] = 0;
		m_gameState[5][30] = 0;
		m_gameState[6][0] = 0;
		m_gameState[6][1] = 0;
		m_gameState[6][2] = 0;
		m_gameState[6][3] = 0;
		m_gameState[6][4] = 0;
		m_gameState[6][5] = 0;
		m_gameState[6][6] = 0;
		m_gameState[6][7] = 0;
		m_gameState[6][8] = 0;
		m_gameState[6][9] = 0;
		m_gameState[6][10] = 0;
		m_gameState[6][11] = 0;
		m_gameState[6][12] = 0;
		m_gameState[6][13] = 0;
		m_gameState[6][14] = 0;
		m_gameState[6][15] = 0;
		m_gameState[6][16] = 0;
		m_gameState[6][17] = GS_SOUTH;
		m_gameState[6][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][19] = GS_NORTH | GS_SOUTH;
		m_gameState[6][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][21] = GS_NORTH;
		m_gameState[6][22] = 0;
		m_gameState[6][23] = 0;
		m_gameState[6][24] = 0;
		m_gameState[6][25] = 0;
		m_gameState[6][26] = 0;
		m_gameState[6][27] = 0;
		m_gameState[6][28] = 0;
		m_gameState[6][29] = 0;
		m_gameState[6][30] = 0;
		m_gameState[7][0] = 0;
		m_gameState[7][1] = 0;
		m_gameState[7][2] = 0;
		m_gameState[7][3] = 0;
		m_gameState[7][4] = 0;
		m_gameState[7][5] = 0;
		m_gameState[7][6] = 0;
		m_gameState[7][7] = 0;
		m_gameState[7][8] = 0;
		m_gameState[7][9] = 0;
		m_gameState[7][10] = 0;
		m_gameState[7][11] = 0;
		m_gameState[7][12] = 0;
		m_gameState[7][13] = 0;
		m_gameState[7][14] = 0;
		m_gameState[7][15] = 0;
		m_gameState[7][16] = 0;
		m_gameState[7][17] = GS_SOUTH;
		m_gameState[7][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][19] = GS_NORTH | GS_SOUTH;
		m_gameState[7][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][21] = GS_NORTH;
		m_gameState[7][22] = 0;
		m_gameState[7][23] = 0;
		m_gameState[7][24] = 0;
		m_gameState[7][25] = 0;
		m_gameState[7][26] = 0;
		m_gameState[7][27] = 0;
		m_gameState[7][28] = 0;
		m_gameState[7][29] = 0;
		m_gameState[7][30] = 0;
		m_gameState[8][0] = 0;
		m_gameState[8][1] = 0;
		m_gameState[8][2] = 0;
		m_gameState[8][3] = 0;
		m_gameState[8][4] = 0;
		m_gameState[8][5] = 0;
		m_gameState[8][6] = 0;
		m_gameState[8][7] = 0;
		m_gameState[8][8] = 0;
		m_gameState[8][9] = 0;
		m_gameState[8][10] = 0;
		m_gameState[8][11] = 0;
		m_gameState[8][12] = 0;
		m_gameState[8][13] = 0;
		m_gameState[8][14] = 0;
		m_gameState[8][15] = 0;
		m_gameState[8][16] = 0;
		m_gameState[8][17] = GS_SOUTH;
		m_gameState[8][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][19] = GS_NORTH | GS_SOUTH;
		m_gameState[8][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][21] = GS_NORTH;
		m_gameState[8][22] = 0;
		m_gameState[8][23] = 0;
		m_gameState[8][24] = 0;
		m_gameState[8][25] = 0;
		m_gameState[8][26] = 0;
		m_gameState[8][27] = 0;
		m_gameState[8][28] = 0;
		m_gameState[8][29] = 0;
		m_gameState[8][30] = 0;
		m_gameState[9][0] = 0;
		m_gameState[9][1] = 0;
		m_gameState[9][2] = 0;
		m_gameState[9][3] = 0;
		m_gameState[9][4] = 0;
		m_gameState[9][5] = 0;
		m_gameState[9][6] = 0;
		m_gameState[9][7] = 0;
		m_gameState[9][8] = 0;
		m_gameState[9][9] = 0;
		m_gameState[9][10] = 0;
		m_gameState[9][11] = 0;
		m_gameState[9][12] = 0;
		m_gameState[9][13] = 0;
		m_gameState[9][14] = 0;
		m_gameState[9][15] = 0;
		m_gameState[9][16] = 0;
		m_gameState[9][17] = GS_SOUTH;
		m_gameState[9][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][19] = GS_NORTH | GS_SOUTH;
		m_gameState[9][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][21] = GS_NORTH;
		m_gameState[9][22] = 0;
		m_gameState[9][23] = 0;
		m_gameState[9][24] = 0;
		m_gameState[9][25] = 0;
		m_gameState[9][26] = 0;
		m_gameState[9][27] = 0;
		m_gameState[9][28] = 0;
		m_gameState[9][29] = 0;
		m_gameState[9][30] = 0;
		m_gameState[10][0] = 0;
		m_gameState[10][1] = 0;
		m_gameState[10][2] = 0;
		m_gameState[10][3] = 0;
		m_gameState[10][4] = 0;
		m_gameState[10][5] = 0;
		m_gameState[10][6] = 0;
		m_gameState[10][7] = 0;
		m_gameState[10][8] = 0;
		m_gameState[10][9] = 0;
		m_gameState[10][10] = 0;
		m_gameState[10][11] = 0;
		m_gameState[10][12] = 0;
		m_gameState[10][13] = 0;
		m_gameState[10][14] = 0;
		m_gameState[10][15] = 0;
		m_gameState[10][16] = 0;
		m_gameState[10][17] = GS_SOUTH;
		m_gameState[10][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][19] = GS_NORTH | GS_SOUTH;
		m_gameState[10][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][21] = GS_NORTH;
		m_gameState[10][22] = 0;
		m_gameState[10][23] = 0;
		m_gameState[10][24] = 0;
		m_gameState[10][25] = 0;
		m_gameState[10][26] = 0;
		m_gameState[10][27] = 0;
		m_gameState[10][28] = 0;
		m_gameState[10][29] = 0;
		m_gameState[10][30] = 0;
		m_gameState[11][0] = 0;
		m_gameState[11][1] = 0;
		m_gameState[11][2] = 0;
		m_gameState[11][3] = 0;
		m_gameState[11][4] = 0;
		m_gameState[11][5] = 0;
		m_gameState[11][6] = 0;
		m_gameState[11][7] = 0;
		m_gameState[11][8] = 0;
		m_gameState[11][9] = 0;
		m_gameState[11][10] = 0;
		m_gameState[11][11] = 0;
		m_gameState[11][12] = 0;
		m_gameState[11][13] = 0;
		m_gameState[11][14] = 0;
		m_gameState[11][15] = 0;
		m_gameState[11][16] = 0;
		m_gameState[11][17] = GS_SOUTH;
		m_gameState[11][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][19] = GS_NORTH | GS_SOUTH;
		m_gameState[11][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][21] = GS_NORTH;
		m_gameState[11][22] = 0;
		m_gameState[11][23] = 0;
		m_gameState[11][24] = 0;
		m_gameState[11][25] = 0;
		m_gameState[11][26] = 0;
		m_gameState[11][27] = 0;
		m_gameState[11][28] = 0;
		m_gameState[11][29] = 0;
		m_gameState[11][30] = 0;
		m_gameState[12][0] = 0;
		m_gameState[12][1] = 0;
		m_gameState[12][2] = 0;
		m_gameState[12][3] = 0;
		m_gameState[12][4] = 0;
		m_gameState[12][5] = 0;
		m_gameState[12][6] = 0;
		m_gameState[12][7] = 0;
		m_gameState[12][8] = 0;
		m_gameState[12][9] = 0;
		m_gameState[12][10] = 0;
		m_gameState[12][11] = 0;
		m_gameState[12][12] = 0;
		m_gameState[12][13] = 0;
		m_gameState[12][14] = 0;
		m_gameState[12][15] = 0;
		m_gameState[12][16] = 0;
		m_gameState[12][17] = GS_SOUTH;
		m_gameState[12][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][19] = GS_NORTH | GS_SOUTH;
		m_gameState[12][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][21] = GS_NORTH;
		m_gameState[12][22] = 0;
		m_gameState[12][23] = 0;
		m_gameState[12][24] = 0;
		m_gameState[12][25] = 0;
		m_gameState[12][26] = 0;
		m_gameState[12][27] = 0;
		m_gameState[12][28] = 0;
		m_gameState[12][29] = 0;
		m_gameState[12][30] = 0;
		m_gameState[13][0] = 0;
		m_gameState[13][1] = 0;
		m_gameState[13][2] = 0;
		m_gameState[13][3] = 0;
		m_gameState[13][4] = 0;
		m_gameState[13][5] = 0;
		m_gameState[13][6] = 0;
		m_gameState[13][7] = 0;
		m_gameState[13][8] = 0;
		m_gameState[13][9] = 0;
		m_gameState[13][10] = 0;
		m_gameState[13][11] = 0;
		m_gameState[13][12] = 0;
		m_gameState[13][13] = 0;
		m_gameState[13][14] = 0;
		m_gameState[13][15] = 0;
		m_gameState[13][16] = 0;
		m_gameState[13][17] = GS_SOUTH;
		m_gameState[13][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][19] = GS_NORTH | GS_SOUTH;
		m_gameState[13][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][21] = GS_NORTH;
		m_gameState[13][22] = 0;
		m_gameState[13][23] = 0;
		m_gameState[13][24] = 0;
		m_gameState[13][25] = 0;
		m_gameState[13][26] = 0;
		m_gameState[13][27] = 0;
		m_gameState[13][28] = 0;
		m_gameState[13][29] = 0;
		m_gameState[13][30] = 0;
		m_gameState[14][0] = 0;
		m_gameState[14][1] = 0;
		m_gameState[14][2] = 0;
		m_gameState[14][3] = 0;
		m_gameState[14][4] = 0;
		m_gameState[14][5] = 0;
		m_gameState[14][6] = 0;
		m_gameState[14][7] = 0;
		m_gameState[14][8] = 0;
		m_gameState[14][9] = 0;
		m_gameState[14][10] = 0;
		m_gameState[14][11] = 0;
		m_gameState[14][12] = 0;
		m_gameState[14][13] = 0;
		m_gameState[14][14] = 0;
		m_gameState[14][15] = 0;
		m_gameState[14][16] = 0;
		m_gameState[14][17] = GS_SOUTH;
		m_gameState[14][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][19] = GS_NORTH | GS_SOUTH;
		m_gameState[14][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][21] = GS_NORTH;
		m_gameState[14][22] = 0;
		m_gameState[14][23] = 0;
		m_gameState[14][24] = 0;
		m_gameState[14][25] = 0;
		m_gameState[14][26] = 0;
		m_gameState[14][27] = 0;
		m_gameState[14][28] = 0;
		m_gameState[14][29] = 0;
		m_gameState[14][30] = 0;
		m_gameState[15][0] = 0;
		m_gameState[15][1] = 0;
		m_gameState[15][2] = 0;
		m_gameState[15][3] = 0;
		m_gameState[15][4] = 0;
		m_gameState[15][5] = 0;
		m_gameState[15][6] = 0;
		m_gameState[15][7] = 0;
		m_gameState[15][8] = 0;
		m_gameState[15][9] = 0;
		m_gameState[15][10] = 0;
		m_gameState[15][11] = 0;
		m_gameState[15][12] = 0;
		m_gameState[15][13] = 0;
		m_gameState[15][14] = 0;
		m_gameState[15][15] = 0;
		m_gameState[15][16] = 0;
		m_gameState[15][17] = GS_SOUTH;
		m_gameState[15][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][19] = GS_NORTH | GS_SOUTH;
		m_gameState[15][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][21] = GS_NORTH;
		m_gameState[15][22] = 0;
		m_gameState[15][23] = 0;
		m_gameState[15][24] = 0;
		m_gameState[15][25] = 0;
		m_gameState[15][26] = 0;
		m_gameState[15][27] = 0;
		m_gameState[15][28] = 0;
		m_gameState[15][29] = 0;
		m_gameState[15][30] = 0;
		m_gameState[16][0] = 0;
		m_gameState[16][1] = 0;
		m_gameState[16][2] = 0;
		m_gameState[16][3] = 0;
		m_gameState[16][4] = 0;
		m_gameState[16][5] = 0;
		m_gameState[16][6] = 0;
		m_gameState[16][7] = 0;
		m_gameState[16][8] = 0;
		m_gameState[16][9] = 0;
		m_gameState[16][10] = 0;
		m_gameState[16][11] = 0;
		m_gameState[16][12] = 0;
		m_gameState[16][13] = 0;
		m_gameState[16][14] = 0;
		m_gameState[16][15] = 0;
		m_gameState[16][16] = 0;
		m_gameState[16][17] = GS_SOUTH;
		m_gameState[16][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][19] = GS_NORTH | GS_SOUTH;
		m_gameState[16][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][21] = GS_NORTH;
		m_gameState[16][22] = 0;
		m_gameState[16][23] = 0;
		m_gameState[16][24] = 0;
		m_gameState[16][25] = 0;
		m_gameState[16][26] = 0;
		m_gameState[16][27] = 0;
		m_gameState[16][28] = 0;
		m_gameState[16][29] = 0;
		m_gameState[16][30] = 0;
		m_gameState[17][0] = 0;
		m_gameState[17][1] = 0;
		m_gameState[17][2] = 0;
		m_gameState[17][3] = 0;
		m_gameState[17][4] = 0;
		m_gameState[17][5] = 0;
		m_gameState[17][6] = 0;
		m_gameState[17][7] = 0;
		m_gameState[17][8] = 0;
		m_gameState[17][9] = GS_EAST;
		m_gameState[17][10] = GS_EAST;
		m_gameState[17][11] = GS_EAST;
		m_gameState[17][12] = GS_EAST;
		m_gameState[17][13] = GS_EAST;
		m_gameState[17][14] = GS_EAST;
		m_gameState[17][15] = GS_EAST;
		m_gameState[17][16] = GS_EAST;
		m_gameState[17][17] = GS_SOUTH | GS_EAST;
		m_gameState[17][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][19] = GS_NORTH | GS_SOUTH;
		m_gameState[17][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][21] = GS_NORTH;
		m_gameState[17][22] = 0;
		m_gameState[17][23] = 0;
		m_gameState[17][24] = 0;
		m_gameState[17][25] = 0;
		m_gameState[17][26] = 0;
		m_gameState[17][27] = 0;
		m_gameState[17][28] = 0;
		m_gameState[17][29] = 0;
		m_gameState[17][30] = 0;
		m_gameState[18][0] = 0;
		m_gameState[18][1] = 0;
		m_gameState[18][2] = 0;
		m_gameState[18][3] = 0;
		m_gameState[18][4] = 0;
		m_gameState[18][5] = 0;
		m_gameState[18][6] = 0;
		m_gameState[18][7] = 0;
		m_gameState[18][8] = GS_SOUTH;
		m_gameState[18][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[18][10] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[18][11] = PAL_BEND_TOPRIGHT | PAL_BEND_BOTTOMRIGHT
				| GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[18][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[18][13] = PAL_BEND_TOPRIGHT | PAL_BEND_BOTTOMRIGHT
				| PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[18][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[18][15] = PAL_BEND_TOPRIGHT | PAL_BEND_BOTTOMRIGHT
				| GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[18][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[18][17] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[18][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][19] = GS_NORTH | GS_SOUTH;
		m_gameState[18][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][21] = GS_NORTH;
		m_gameState[18][22] = 0;
		m_gameState[18][23] = 0;
		m_gameState[18][24] = 0;
		m_gameState[18][25] = 0;
		m_gameState[18][26] = 0;
		m_gameState[18][27] = 0;
		m_gameState[18][28] = 0;
		m_gameState[18][29] = 0;
		m_gameState[18][30] = 0;
		m_gameState[19][0] = 0;
		m_gameState[19][1] = 0;
		m_gameState[19][2] = 0;
		m_gameState[19][3] = 0;
		m_gameState[19][4] = 0;
		m_gameState[19][5] = 0;
		m_gameState[19][6] = 0;
		m_gameState[19][7] = 0;
		m_gameState[19][8] = GS_SOUTH;
		m_gameState[19][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][10] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[19][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][12] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[19][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][14] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[19][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][16] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[19][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][19] = GS_NORTH | GS_SOUTH;
		m_gameState[19][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][21] = GS_NORTH;
		m_gameState[19][22] = 0;
		m_gameState[19][23] = 0;
		m_gameState[19][24] = 0;
		m_gameState[19][25] = 0;
		m_gameState[19][26] = 0;
		m_gameState[19][27] = 0;
		m_gameState[19][28] = 0;
		m_gameState[19][29] = 0;
		m_gameState[19][30] = 0;
		m_gameState[20][0] = 0;
		m_gameState[20][1] = 0;
		m_gameState[20][2] = 0;
		m_gameState[20][3] = 0;
		m_gameState[20][4] = 0;
		m_gameState[20][5] = 0;
		m_gameState[20][6] = 0;
		m_gameState[20][7] = 0;
		m_gameState[20][8] = GS_SOUTH;
		m_gameState[20][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][10] = GS_NORTH | GS_SOUTH;
		m_gameState[20][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][12] = GS_NORTH | GS_SOUTH;
		m_gameState[20][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][14] = GS_NORTH | GS_SOUTH;
		m_gameState[20][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][16] = GS_NORTH | GS_SOUTH;
		m_gameState[20][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][19] = GS_NORTH | GS_SOUTH;
		m_gameState[20][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][21] = GS_NORTH;
		m_gameState[20][22] = 0;
		m_gameState[20][23] = 0;
		m_gameState[20][24] = 0;
		m_gameState[20][25] = 0;
		m_gameState[20][26] = 0;
		m_gameState[20][27] = 0;
		m_gameState[20][28] = 0;
		m_gameState[20][29] = 0;
		m_gameState[20][30] = 0;
		m_gameState[21][0] = 0;
		m_gameState[21][1] = 0;
		m_gameState[21][2] = 0;
		m_gameState[21][3] = 0;
		m_gameState[21][4] = 0;
		m_gameState[21][5] = 0;
		m_gameState[21][6] = 0;
		m_gameState[21][7] = 0;
		m_gameState[21][8] = GS_SOUTH;
		m_gameState[21][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][10] = GS_NORTH | GS_SOUTH;
		m_gameState[21][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][12] = GS_NORTH | GS_SOUTH;
		m_gameState[21][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][14] = GS_NORTH | GS_SOUTH;
		m_gameState[21][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][16] = GS_NORTH | GS_SOUTH;
		m_gameState[21][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][19] = GS_NORTH | GS_SOUTH;
		m_gameState[21][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][21] = GS_NORTH;
		m_gameState[21][22] = 0;
		m_gameState[21][23] = 0;
		m_gameState[21][24] = 0;
		m_gameState[21][25] = 0;
		m_gameState[21][26] = 0;
		m_gameState[21][27] = 0;
		m_gameState[21][28] = 0;
		m_gameState[21][29] = 0;
		m_gameState[21][30] = 0;
		m_gameState[22][0] = 0;
		m_gameState[22][1] = 0;
		m_gameState[22][2] = 0;
		m_gameState[22][3] = 0;
		m_gameState[22][4] = 0;
		m_gameState[22][5] = 0;
		m_gameState[22][6] = 0;
		m_gameState[22][7] = 0;
		m_gameState[22][8] = GS_SOUTH;
		m_gameState[22][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][10] = GS_NORTH | GS_SOUTH;
		m_gameState[22][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][12] = GS_NORTH | GS_SOUTH;
		m_gameState[22][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][14] = GS_NORTH | GS_SOUTH;
		m_gameState[22][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][16] = GS_NORTH | GS_SOUTH;
		m_gameState[22][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][19] = GS_NORTH | GS_SOUTH;
		m_gameState[22][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][21] = GS_NORTH;
		m_gameState[22][22] = 0;
		m_gameState[22][23] = 0;
		m_gameState[22][24] = 0;
		m_gameState[22][25] = 0;
		m_gameState[22][26] = 0;
		m_gameState[22][27] = 0;
		m_gameState[22][28] = 0;
		m_gameState[22][29] = 0;
		m_gameState[22][30] = 0;
		m_gameState[23][0] = 0;
		m_gameState[23][1] = 0;
		m_gameState[23][2] = 0;
		m_gameState[23][3] = 0;
		m_gameState[23][4] = 0;
		m_gameState[23][5] = 0;
		m_gameState[23][6] = 0;
		m_gameState[23][7] = 0;
		m_gameState[23][8] = GS_SOUTH;
		m_gameState[23][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][10] = GS_NORTH | GS_SOUTH;
		m_gameState[23][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][12] = GS_NORTH | GS_SOUTH;
		m_gameState[23][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][14] = GS_NORTH | GS_SOUTH;
		m_gameState[23][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][16] = GS_NORTH | GS_SOUTH;
		m_gameState[23][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][19] = GS_NORTH | GS_SOUTH;
		m_gameState[23][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][21] = GS_NORTH;
		m_gameState[23][22] = 0;
		m_gameState[23][23] = 0;
		m_gameState[23][24] = 0;
		m_gameState[23][25] = 0;
		m_gameState[23][26] = 0;
		m_gameState[23][27] = 0;
		m_gameState[23][28] = 0;
		m_gameState[23][29] = 0;
		m_gameState[23][30] = 0;
		m_gameState[24][0] = 0;
		m_gameState[24][1] = 0;
		m_gameState[24][2] = 0;
		m_gameState[24][3] = 0;
		m_gameState[24][4] = 0;
		m_gameState[24][5] = 0;
		m_gameState[24][6] = 0;
		m_gameState[24][7] = 0;
		m_gameState[24][8] = GS_SOUTH;
		m_gameState[24][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][10] = GS_NORTH | GS_SOUTH;
		m_gameState[24][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][12] = GS_NORTH | GS_SOUTH;
		m_gameState[24][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][14] = GS_NORTH | GS_SOUTH;
		m_gameState[24][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][16] = GS_NORTH | GS_SOUTH;
		m_gameState[24][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][19] = GS_NORTH | GS_SOUTH;
		m_gameState[24][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][21] = GS_NORTH;
		m_gameState[24][22] = 0;
		m_gameState[24][23] = 0;
		m_gameState[24][24] = 0;
		m_gameState[24][25] = 0;
		m_gameState[24][26] = 0;
		m_gameState[24][27] = 0;
		m_gameState[24][28] = 0;
		m_gameState[24][29] = 0;
		m_gameState[24][30] = 0;
		m_gameState[25][0] = 0;
		m_gameState[25][1] = 0;
		m_gameState[25][2] = 0;
		m_gameState[25][3] = 0;
		m_gameState[25][4] = 0;
		m_gameState[25][5] = 0;
		m_gameState[25][6] = 0;
		m_gameState[25][7] = 0;
		m_gameState[25][8] = GS_SOUTH;
		m_gameState[25][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][10] = GS_NORTH | GS_SOUTH;
		m_gameState[25][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][12] = GS_NORTH | GS_SOUTH;
		m_gameState[25][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][14] = GS_NORTH | GS_SOUTH;
		m_gameState[25][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][16] = GS_NORTH | GS_SOUTH;
		m_gameState[25][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[25][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[25][19] = GS_NORTH | GS_SOUTH;
		m_gameState[25][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][21] = GS_NORTH;
		m_gameState[25][22] = 0;
		m_gameState[25][23] = 0;
		m_gameState[25][24] = 0;
		m_gameState[25][25] = 0;
		m_gameState[25][26] = 0;
		m_gameState[25][27] = 0;
		m_gameState[25][28] = 0;
		m_gameState[25][29] = 0;
		m_gameState[25][30] = 0;
		m_gameState[26][0] = 0;
		m_gameState[26][1] = 0;
		m_gameState[26][2] = 0;
		m_gameState[26][3] = 0;
		m_gameState[26][4] = 0;
		m_gameState[26][5] = 0;
		m_gameState[26][6] = 0;
		m_gameState[26][7] = 0;
		m_gameState[26][8] = GS_SOUTH;
		m_gameState[26][9] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][10] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[26][11] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][12] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[26][13] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][14] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[26][15] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][16] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[26][17] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][19] = GS_NORTH | GS_SOUTH;
		m_gameState[26][20] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][21] = GS_NORTH;
		m_gameState[26][22] = 0;
		m_gameState[26][23] = 0;
		m_gameState[26][24] = 0;
		m_gameState[26][25] = 0;
		m_gameState[26][26] = 0;
		m_gameState[26][27] = 0;
		m_gameState[26][28] = 0;
		m_gameState[26][29] = 0;
		m_gameState[26][30] = 0;
		m_gameState[27][0] = 0;
		m_gameState[27][1] = 0;
		m_gameState[27][2] = 0;
		m_gameState[27][3] = 0;
		m_gameState[27][4] = 0;
		m_gameState[27][5] = 0;
		m_gameState[27][6] = 0;
		m_gameState[27][7] = 0;
		m_gameState[27][8] = GS_SOUTH;
		m_gameState[27][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[27][10] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[27][11] = PAL_BEND_TOPLEFT | PAL_BEND_BOTTOMLEFT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[27][13] = PAL_BEND_TOPLEFT | PAL_BEND_BOTTOMLEFT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[27][15] = PAL_BEND_TOPLEFT | PAL_BEND_BOTTOMLEFT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[27][17] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[27][18] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST;
		m_gameState[27][19] = GS_NORTH | GS_SOUTH;
		m_gameState[27][20] = PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][21] = GS_NORTH;
		m_gameState[27][22] = 0;
		m_gameState[27][23] = 0;
		m_gameState[27][24] = 0;
		m_gameState[27][25] = 0;
		m_gameState[27][26] = 0;
		m_gameState[27][27] = 0;
		m_gameState[27][28] = 0;
		m_gameState[27][29] = 0;
		m_gameState[27][30] = 0;
	}

	// The original MsPacman maze
	void loadMsPacManMaze1() {
		m_gameState[0][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | PAL_EDGE_LEFT
				| GS_SOUTH | GS_EAST;
		m_gameState[0][1] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][2] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][3] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][4] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][5] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM | PAL_EDGE_LEFT
				| GS_NORTH | GS_EAST;
		m_gameState[0][6] = GS_NORTH;
		m_gameState[0][7] = GS_SOUTH;
		m_gameState[0][8] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][9] = GS_NORTH | GS_SOUTH;
		m_gameState[0][10] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][11] = GS_NORTH;
		m_gameState[0][12] = 0;
		m_gameState[0][13] = 0;
		m_gameState[0][14] = 0;
		m_gameState[0][15] = GS_SOUTH;
		m_gameState[0][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][17] = GS_NORTH | GS_SOUTH;
		m_gameState[0][18] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][19] = GS_NORTH;
		m_gameState[0][20] = 0;
		m_gameState[0][21] = GS_SOUTH;
		m_gameState[0][22] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP
				| PAL_EDGE_LEFT | GS_SOUTH | GS_EAST;
		m_gameState[0][23] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][24] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][25] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][26] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][27] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][28] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][29] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM
				| PAL_EDGE_LEFT | GS_NORTH | GS_EAST;
		m_gameState[1][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][2] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[1][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][4] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][5] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][6] = GS_NORTH | GS_EAST;
		m_gameState[1][7] = GS_SOUTH | GS_EAST;
		m_gameState[1][8] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][9] = GS_NORTH | GS_SOUTH;
		m_gameState[1][10] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][11] = GS_NORTH | GS_EAST;
		m_gameState[1][12] = GS_EAST;
		m_gameState[1][13] = GS_EAST;
		m_gameState[1][14] = GS_EAST;
		m_gameState[1][15] = GS_SOUTH | GS_EAST;
		m_gameState[1][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][17] = GS_NORTH | GS_SOUTH;
		m_gameState[1][18] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][19] = GS_NORTH | GS_EAST;
		m_gameState[1][20] = GS_EAST;
		m_gameState[1][21] = GS_SOUTH | GS_EAST;
		m_gameState[1][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][23] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][26] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][27] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[1][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][6] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[2][7] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[2][8] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][9] = GS_NORTH | GS_SOUTH;
		m_gameState[2][10] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][11] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][12] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][17] = GS_NORTH | GS_SOUTH;
		m_gameState[2][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][19] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][20] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][21] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[2][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[2][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[2][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][4] = GS_FOOD | GS_NORTH;
		m_gameState[3][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][9] = GS_FOOD | GS_EAST;
		m_gameState[3][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][11] = GS_FOOD | GS_WEST;
		m_gameState[3][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][14] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][17] = GS_FOOD;
		m_gameState[3][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][20] = GS_FOOD | GS_WEST;
		m_gameState[3][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][23] = GS_FOOD | GS_SOUTH;
		m_gameState[3][24] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][25] = GS_NORTH | GS_WEST;
		m_gameState[3][26] = GS_WEST;
		m_gameState[3][27] = GS_WEST | GS_SOUTH;
		m_gameState[3][28] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][11] = GS_NORTH | GS_SOUTH;
		m_gameState[4][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][15] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][17] = GS_NORTH | GS_SOUTH;
		m_gameState[4][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][24] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][25] = GS_NORTH | GS_EAST;
		m_gameState[4][26] = GS_EAST;
		m_gameState[4][27] = GS_SOUTH | GS_EAST;
		m_gameState[4][28] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[5][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[5][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][11] = GS_NORTH | GS_SOUTH;
		m_gameState[5][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][13] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[5][14] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][15] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][17] = GS_NORTH | GS_SOUTH;
		m_gameState[5][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[5][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[5][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[5][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[6][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[6][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][4] = GS_FOOD;
		m_gameState[6][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[6][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][11] = GS_NORTH | GS_SOUTH;
		m_gameState[6][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][14] = GS_NORTH | GS_WEST;
		m_gameState[6][15] = GS_WEST | GS_EAST;
		m_gameState[6][16] = GS_WEST | GS_EAST;
		m_gameState[6][17] = GS_SOUTH | GS_EAST;
		m_gameState[6][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][23] = GS_FOOD | GS_NORTH;
		m_gameState[6][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][26] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][29] = GS_FOOD | GS_SOUTH;
		m_gameState[6][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[7][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[7][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[7][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][11] = GS_NORTH | GS_SOUTH;
		m_gameState[7][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][14] = GS_NORTH | GS_SOUTH;
		m_gameState[7][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][18] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[7][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[8][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][6] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][11] = GS_NORTH | GS_SOUTH;
		m_gameState[8][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][14] = GS_NORTH | GS_SOUTH;
		m_gameState[8][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][18] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[8][25] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[9][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[9][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][4] = GS_FOOD | GS_SOUTH;
		m_gameState[9][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][6] = GS_NORTH | GS_SOUTH;
		m_gameState[9][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][8] = GS_FOOD | GS_NORTH;
		m_gameState[9][9] = GS_WEST | GS_EAST;
		m_gameState[9][10] = GS_WEST | GS_EAST;
		m_gameState[9][11] = 0;
		m_gameState[9][12] = GS_WEST | GS_EAST;
		m_gameState[9][13] = GS_WEST | GS_EAST;
		m_gameState[9][14] = GS_EAST;
		m_gameState[9][15] = GS_WEST | GS_EAST;
		m_gameState[9][16] = GS_WEST | GS_EAST;
		m_gameState[9][17] = GS_WEST | GS_SOUTH;
		m_gameState[9][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][20] = GS_FOOD | GS_NORTH;
		m_gameState[9][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][23] = GS_FOOD | GS_SOUTH;
		m_gameState[9][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][26] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[9][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][29] = GS_FOOD | GS_SOUTH;
		m_gameState[9][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[10][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][6] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][11] = GS_NORTH | GS_SOUTH;
		m_gameState[10][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][17] = GS_NORTH | GS_SOUTH;
		m_gameState[10][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][20] = GS_NORTH | GS_SOUTH;
		m_gameState[10][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[11][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][11] = GS_NORTH | GS_SOUTH;
		m_gameState[11][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][13] = GS_NORTH | GS_WEST;
		m_gameState[11][14] = GS_WEST;
		m_gameState[11][15] = GS_WEST | GS_SOUTH;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][17] = GS_NORTH | GS_SOUTH;
		m_gameState[11][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][20] = GS_NORTH | GS_SOUTH;
		m_gameState[11][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[12][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][4] = GS_FOOD | GS_NORTH;
		m_gameState[12][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[12][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][11] = GS_NORTH | GS_SOUTH;
		m_gameState[12][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][13] = GS_NORTH;
		m_gameState[12][14] = 0;
		m_gameState[12][15] = GS_SOUTH;
		m_gameState[12][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][17] = GS_NORTH;
		m_gameState[12][18] = GS_WEST | GS_EAST;
		m_gameState[12][19] = GS_WEST | GS_EAST;
		m_gameState[12][20] = GS_SOUTH | GS_EAST;
		m_gameState[12][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][23] = GS_FOOD | GS_NORTH;
		m_gameState[12][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[12][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[12][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[13][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][11] = GS_NORTH | GS_SOUTH;
		m_gameState[13][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][13] = GS_NORTH;
		m_gameState[13][14] = 0;
		m_gameState[13][15] = GS_SOUTH;
		m_gameState[13][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][17] = GS_NORTH | GS_SOUTH;
		m_gameState[13][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][19] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][21] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[13][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[13][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[14][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][11] = GS_NORTH | GS_SOUTH;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][13] = GS_NORTH;
		m_gameState[14][14] = 0;
		m_gameState[14][15] = GS_SOUTH;
		m_gameState[14][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][17] = GS_NORTH | GS_SOUTH;
		m_gameState[14][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][19] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][21] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[14][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[14][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[15][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][4] = GS_FOOD | GS_NORTH;
		m_gameState[15][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[15][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][11] = GS_NORTH | GS_SOUTH;
		m_gameState[15][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][13] = GS_NORTH;
		m_gameState[15][14] = 0;
		m_gameState[15][15] = GS_SOUTH;
		m_gameState[15][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][17] = GS_NORTH;
		m_gameState[15][18] = GS_WEST | GS_EAST;
		m_gameState[15][19] = GS_WEST | GS_EAST;
		m_gameState[15][20] = GS_WEST | GS_SOUTH;
		m_gameState[15][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][23] = GS_FOOD | GS_NORTH;
		m_gameState[15][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[15][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[15][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[16][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][11] = GS_NORTH | GS_SOUTH;
		m_gameState[16][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][13] = GS_NORTH | GS_EAST;
		m_gameState[16][14] = GS_EAST;
		m_gameState[16][15] = GS_SOUTH | GS_EAST;
		m_gameState[16][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][17] = GS_NORTH | GS_SOUTH;
		m_gameState[16][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][20] = GS_NORTH | GS_SOUTH;
		m_gameState[16][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[17][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][6] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][11] = GS_NORTH | GS_SOUTH;
		m_gameState[17][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][17] = GS_NORTH | GS_SOUTH;
		m_gameState[17][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][20] = GS_NORTH | GS_SOUTH;
		m_gameState[17][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[18][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[18][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][4] = GS_FOOD | GS_SOUTH;
		m_gameState[18][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][6] = GS_NORTH | GS_SOUTH;
		m_gameState[18][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][8] = GS_FOOD | GS_NORTH;
		m_gameState[18][9] = GS_WEST | GS_EAST;
		m_gameState[18][10] = GS_WEST | GS_EAST;
		m_gameState[18][11] = 0;
		m_gameState[18][12] = GS_WEST | GS_EAST;
		m_gameState[18][13] = GS_WEST | GS_EAST;
		m_gameState[18][14] = GS_WEST;
		m_gameState[18][15] = GS_WEST | GS_EAST;
		m_gameState[18][16] = GS_WEST | GS_EAST;
		m_gameState[18][17] = GS_SOUTH | GS_EAST;
		m_gameState[18][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][20] = GS_FOOD | GS_NORTH;
		m_gameState[18][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][23] = GS_FOOD | GS_SOUTH;
		m_gameState[18][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][26] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[18][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][29] = GS_FOOD | GS_SOUTH;
		m_gameState[18][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[19][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][6] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][11] = GS_NORTH | GS_SOUTH;
		m_gameState[19][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][14] = GS_NORTH | GS_SOUTH;
		m_gameState[19][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][18] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][25] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[20][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[20][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[20][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][11] = GS_NORTH | GS_SOUTH;
		m_gameState[20][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][14] = GS_NORTH | GS_SOUTH;
		m_gameState[20][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][18] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[20][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[21][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[21][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][4] = GS_FOOD;
		m_gameState[21][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[21][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][11] = GS_NORTH | GS_SOUTH;
		m_gameState[21][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][14] = GS_NORTH | GS_EAST;
		m_gameState[21][15] = GS_WEST | GS_EAST;
		m_gameState[21][16] = GS_WEST | GS_EAST;
		m_gameState[21][17] = GS_WEST | GS_SOUTH;
		m_gameState[21][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][23] = GS_FOOD | GS_NORTH;
		m_gameState[21][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][26] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][29] = GS_FOOD | GS_SOUTH;
		m_gameState[21][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[22][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[22][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][11] = GS_NORTH | GS_SOUTH;
		m_gameState[22][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][13] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[22][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][15] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][17] = GS_NORTH | GS_SOUTH;
		m_gameState[22][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[22][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[22][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[22][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][11] = GS_NORTH | GS_SOUTH;
		m_gameState[23][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][14] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][15] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][17] = GS_NORTH | GS_SOUTH;
		m_gameState[23][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][24] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][25] = GS_NORTH | GS_WEST;
		m_gameState[23][26] = GS_WEST;
		m_gameState[23][27] = GS_WEST | GS_SOUTH;
		m_gameState[23][28] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][4] = GS_FOOD | GS_NORTH;
		m_gameState[24][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][9] = GS_FOOD | GS_WEST;
		m_gameState[24][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][11] = GS_FOOD | GS_EAST;
		m_gameState[24][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][14] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][17] = GS_FOOD;
		m_gameState[24][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][20] = GS_FOOD | GS_EAST;
		m_gameState[24][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][23] = GS_FOOD | GS_SOUTH;
		m_gameState[24][24] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][25] = GS_NORTH | GS_EAST;
		m_gameState[24][26] = GS_EAST;
		m_gameState[24][27] = GS_SOUTH | GS_EAST;
		m_gameState[24][28] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][6] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][7] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][8] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][9] = GS_NORTH | GS_SOUTH;
		m_gameState[25][10] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][11] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][12] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][17] = GS_NORTH | GS_SOUTH;
		m_gameState[25][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][19] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][20] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][21] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[25][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[25][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[25][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][2] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[26][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][4] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][5] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][6] = GS_NORTH | GS_WEST;
		m_gameState[26][7] = GS_WEST | GS_SOUTH;
		m_gameState[26][8] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][9] = GS_NORTH | GS_SOUTH;
		m_gameState[26][10] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][11] = GS_NORTH | GS_WEST;
		m_gameState[26][12] = GS_WEST;
		m_gameState[26][13] = GS_WEST;
		m_gameState[26][14] = GS_WEST;
		m_gameState[26][15] = GS_WEST | GS_SOUTH;
		m_gameState[26][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][17] = GS_NORTH | GS_SOUTH;
		m_gameState[26][18] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][19] = GS_NORTH | GS_WEST;
		m_gameState[26][20] = GS_WEST;
		m_gameState[26][21] = GS_WEST | GS_SOUTH;
		m_gameState[26][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][23] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][26] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][27] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[26][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[27][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][1] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][2] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][3] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][4] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][5] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
		m_gameState[27][6] = GS_NORTH;
		m_gameState[27][7] = GS_SOUTH;
		m_gameState[27][8] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][9] = GS_NORTH | GS_SOUTH;
		m_gameState[27][10] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][11] = GS_NORTH;
		m_gameState[27][12] = 0;
		m_gameState[27][13] = 0;
		m_gameState[27][14] = 0;
		m_gameState[27][15] = GS_SOUTH;
		m_gameState[27][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][17] = GS_NORTH | GS_SOUTH;
		m_gameState[27][18] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][19] = GS_NORTH;
		m_gameState[27][20] = 0;
		m_gameState[27][21] = GS_SOUTH;
		m_gameState[27][22] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][23] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][24] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][25] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][26] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][27] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][28] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][29] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
	}

	// The original MsPacman maze
	void loadMsPacManMaze4() {
		m_gameState[0][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | PAL_EDGE_LEFT
				| GS_SOUTH | GS_EAST;
		m_gameState[0][1] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][2] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][3] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][4] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][5] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][6] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][7] = PAL_BEND_TOPRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][8] = PAL_LINE_HORIZ | GS_NORTH | GS_EAST;
		m_gameState[0][9] = GS_NORTH | GS_SOUTH;
		m_gameState[0][10] = PAL_BEND_BOTTOMLEFT | GS_SOUTH;
		m_gameState[0][11] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][12] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][16] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][17] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][18] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][19] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][20] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][21] = PAL_BEND_TOPRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][22] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][23] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][24] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][25] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][26] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][27] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][28] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][29] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM
				| PAL_EDGE_LEFT | GS_NORTH | GS_EAST;
		m_gameState[1][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][3] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[1][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][6] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][7] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][8] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][9] = GS_FOOD | GS_NORTH;
		m_gameState[1][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][11] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][14] = GS_FOOD | GS_WEST;
		m_gameState[1][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][20] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][23] = GS_POWERUP | GS_NORTH | GS_WEST;
		m_gameState[1][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][26] = GS_FOOD | GS_WEST;
		m_gameState[1][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][5] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][6] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][7] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[2][8] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[2][9] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][10] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][14] = GS_NORTH | GS_SOUTH;
		m_gameState[2][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][3] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[3][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][5] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[3][6] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][7] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[3][8] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[3][9] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][10] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[3][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][12] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[3][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][14] = GS_NORTH | GS_SOUTH;
		m_gameState[3][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[3][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][18] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[3][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][20] = GS_FOOD | GS_NORTH;
		m_gameState[3][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][23] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[3][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][4] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[4][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][6] = GS_FOOD | GS_EAST;
		m_gameState[4][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][9] = GS_FOOD | GS_EAST;
		m_gameState[4][10] = GS_WEST | GS_EAST;
		m_gameState[4][11] = GS_WEST | GS_SOUTH;
		m_gameState[4][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][14] = GS_NORTH;
		m_gameState[4][15] = GS_WEST | GS_EAST;
		m_gameState[4][16] = GS_WEST | GS_EAST;
		m_gameState[4][17] = GS_WEST | GS_SOUTH;
		m_gameState[4][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][22] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][24] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[4][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[5][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[5][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[5][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[5][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[5][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[5][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[5][11] = GS_NORTH | GS_SOUTH;
		m_gameState[5][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][14] = GS_NORTH | GS_SOUTH;
		m_gameState[5][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[5][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[5][17] = GS_NORTH | GS_SOUTH;
		m_gameState[5][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][22] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][24] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[6][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[6][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[6][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[6][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[6][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[6][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][11] = GS_NORTH;
		m_gameState[6][12] = GS_WEST | GS_EAST;
		m_gameState[6][13] = GS_WEST | GS_EAST;
		m_gameState[6][14] = GS_SOUTH | GS_EAST;
		m_gameState[6][15] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][16] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][17] = GS_NORTH;
		m_gameState[6][18] = GS_WEST | GS_EAST;
		m_gameState[6][19] = GS_WEST | GS_EAST;
		m_gameState[6][20] = GS_FOOD | GS_EAST;
		m_gameState[6][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][23] = GS_FOOD | GS_WEST;
		m_gameState[6][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][26] = GS_FOOD | GS_EAST;
		m_gameState[6][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[6][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[7][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[7][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][4] = GS_FOOD | GS_NORTH;
		m_gameState[7][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[7][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[7][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[7][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[7][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][11] = GS_NORTH | GS_SOUTH;
		m_gameState[7][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][15] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[7][16] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][17] = GS_NORTH | GS_SOUTH;
		m_gameState[7][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][19] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][28] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][29] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM | GS_NORTH
				| GS_WEST | GS_EAST;
		m_gameState[8][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[8][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[8][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[8][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[8][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][11] = GS_NORTH | GS_SOUTH;
		m_gameState[8][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][14] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][15] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][17] = GS_NORTH | GS_SOUTH;
		m_gameState[8][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[8][19] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][21] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[8][25] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][28] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][29] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM | GS_NORTH
				| GS_WEST | GS_EAST;
		m_gameState[9][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[9][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][4] = GS_FOOD | GS_SOUTH;
		m_gameState[9][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][6] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[9][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][8] = GS_FOOD | GS_NORTH;
		m_gameState[9][9] = GS_WEST | GS_EAST;
		m_gameState[9][10] = GS_WEST | GS_EAST;
		m_gameState[9][11] = 0;
		m_gameState[9][12] = GS_WEST | GS_EAST;
		m_gameState[9][13] = GS_WEST | GS_EAST;
		m_gameState[9][14] = GS_WEST | GS_EAST;
		m_gameState[9][15] = GS_WEST | GS_EAST;
		m_gameState[9][16] = GS_WEST | GS_EAST;
		m_gameState[9][17] = GS_SOUTH;
		m_gameState[9][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][20] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[9][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][23] = GS_FOOD | GS_SOUTH;
		m_gameState[9][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][26] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[9][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[9][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[10][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[10][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][6] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][11] = GS_NORTH | GS_SOUTH;
		m_gameState[10][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][17] = GS_NORTH | GS_SOUTH;
		m_gameState[10][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[11][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[11][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][11] = GS_NORTH | GS_SOUTH;
		m_gameState[11][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][13] = GS_NORTH | GS_WEST;
		m_gameState[11][14] = GS_WEST;
		m_gameState[11][15] = GS_WEST | GS_SOUTH;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][17] = GS_NORTH | GS_SOUTH;
		m_gameState[11][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[12][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][4] = GS_FOOD | GS_EAST;
		m_gameState[12][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][8] = GS_FOOD | GS_SOUTH;
		m_gameState[12][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][11] = GS_NORTH | GS_SOUTH;
		m_gameState[12][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][13] = GS_NORTH;
		m_gameState[12][14] = 0;
		m_gameState[12][15] = GS_SOUTH;
		m_gameState[12][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][17] = GS_NORTH;
		m_gameState[12][18] = GS_WEST | GS_EAST;
		m_gameState[12][19] = GS_WEST | GS_EAST;
		m_gameState[12][20] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[12][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][23] = GS_FOOD | GS_NORTH;
		m_gameState[12][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[12][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[12][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][3] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][5] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][11] = GS_NORTH | GS_SOUTH;
		m_gameState[13][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][13] = GS_NORTH;
		m_gameState[13][14] = 0;
		m_gameState[13][15] = GS_SOUTH;
		m_gameState[13][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][17] = GS_NORTH | GS_SOUTH;
		m_gameState[13][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][19] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][21] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[13][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[13][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[13][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][3] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][5] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][11] = GS_NORTH | GS_SOUTH;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][13] = GS_NORTH;
		m_gameState[14][14] = 0;
		m_gameState[14][15] = GS_SOUTH;
		m_gameState[14][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][17] = GS_NORTH | GS_SOUTH;
		m_gameState[14][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][19] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][21] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[14][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[14][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[14][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[15][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][4] = GS_FOOD | GS_WEST;
		m_gameState[15][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][8] = GS_FOOD | GS_SOUTH;
		m_gameState[15][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][11] = GS_NORTH | GS_SOUTH;
		m_gameState[15][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][13] = GS_NORTH;
		m_gameState[15][14] = 0;
		m_gameState[15][15] = GS_SOUTH;
		m_gameState[15][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][17] = GS_NORTH;
		m_gameState[15][18] = GS_WEST | GS_EAST;
		m_gameState[15][19] = GS_WEST | GS_EAST;
		m_gameState[15][20] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[15][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][23] = GS_FOOD | GS_NORTH;
		m_gameState[15][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[15][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[15][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[16][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[16][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][11] = GS_NORTH | GS_SOUTH;
		m_gameState[16][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][13] = GS_NORTH | GS_EAST;
		m_gameState[16][14] = GS_EAST;
		m_gameState[16][15] = GS_SOUTH | GS_EAST;
		m_gameState[16][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][17] = GS_NORTH | GS_SOUTH;
		m_gameState[16][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[17][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[17][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][6] = GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][9] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][11] = GS_NORTH | GS_SOUTH;
		m_gameState[17][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][17] = GS_NORTH | GS_SOUTH;
		m_gameState[17][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[18][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[18][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][4] = GS_FOOD | GS_SOUTH;
		m_gameState[18][5] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][6] = GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[18][7] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][8] = GS_FOOD | GS_NORTH;
		m_gameState[18][9] = GS_WEST | GS_EAST;
		m_gameState[18][10] = GS_WEST | GS_EAST;
		m_gameState[18][11] = 0;
		m_gameState[18][12] = GS_WEST | GS_EAST;
		m_gameState[18][13] = GS_WEST | GS_EAST;
		m_gameState[18][14] = GS_WEST | GS_EAST;
		m_gameState[18][15] = GS_WEST | GS_EAST;
		m_gameState[18][16] = GS_WEST | GS_EAST;
		m_gameState[18][17] = GS_SOUTH;
		m_gameState[18][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][20] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[18][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][23] = GS_FOOD | GS_SOUTH;
		m_gameState[18][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][26] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[18][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[18][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[19][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[19][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[19][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[19][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[19][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][9] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][11] = GS_NORTH | GS_SOUTH;
		m_gameState[19][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][14] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][15] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][17] = GS_NORTH | GS_SOUTH;
		m_gameState[19][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][19] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][25] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][28] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][29] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM | GS_NORTH
				| GS_WEST | GS_EAST;
		m_gameState[20][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[20][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][4] = GS_FOOD | GS_NORTH;
		m_gameState[20][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[20][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[20][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[20][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[20][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][11] = GS_NORTH | GS_SOUTH;
		m_gameState[20][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][14] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][15] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[20][16] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][17] = GS_NORTH | GS_SOUTH;
		m_gameState[20][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][19] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][20] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][21] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][28] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][29] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM | GS_NORTH
				| GS_WEST | GS_EAST;
		m_gameState[21][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[21][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[21][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[21][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[21][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[21][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][11] = GS_NORTH;
		m_gameState[21][12] = GS_WEST | GS_EAST;
		m_gameState[21][13] = GS_WEST | GS_EAST;
		m_gameState[21][14] = GS_WEST | GS_SOUTH;
		m_gameState[21][15] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][16] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][17] = GS_NORTH;
		m_gameState[21][18] = GS_WEST | GS_EAST;
		m_gameState[21][19] = GS_WEST | GS_EAST;
		m_gameState[21][20] = GS_FOOD | GS_WEST;
		m_gameState[21][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][23] = GS_FOOD | GS_EAST;
		m_gameState[21][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][26] = GS_FOOD | GS_WEST;
		m_gameState[21][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[21][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[22][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[22][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[22][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[22][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[22][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[22][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[22][11] = GS_NORTH | GS_SOUTH;
		m_gameState[22][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][13] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][14] = GS_NORTH | GS_SOUTH;
		m_gameState[22][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[22][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[22][17] = GS_NORTH | GS_SOUTH;
		m_gameState[22][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][22] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][24] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][4] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[23][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][6] = GS_FOOD | GS_WEST;
		m_gameState[23][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][9] = GS_FOOD | GS_WEST;
		m_gameState[23][10] = GS_WEST | GS_EAST;
		m_gameState[23][11] = GS_SOUTH | GS_EAST;
		m_gameState[23][12] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][14] = GS_NORTH;
		m_gameState[23][15] = GS_WEST | GS_EAST;
		m_gameState[23][16] = GS_WEST | GS_EAST;
		m_gameState[23][17] = GS_SOUTH | GS_EAST;
		m_gameState[23][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][22] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][24] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[23][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][3] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[24][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][5] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[24][6] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][7] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[24][8] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[24][9] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][10] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[24][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][12] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[24][13] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][14] = GS_NORTH | GS_SOUTH;
		m_gameState[24][15] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[24][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][18] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[24][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][20] = GS_FOOD | GS_NORTH;
		m_gameState[24][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][23] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[24][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][3] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][5] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][6] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][7] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[25][8] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[25][9] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][10] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][13] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][14] = GS_NORTH | GS_SOUTH;
		m_gameState[25][15] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][3] = GS_POWERUP | GS_WEST | GS_EAST;
		m_gameState[26][4] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][6] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][7] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][8] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][9] = GS_FOOD | GS_NORTH;
		m_gameState[26][10] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][11] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][13] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][14] = GS_FOOD | GS_EAST;
		m_gameState[26][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][20] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][23] = GS_POWERUP | GS_NORTH | GS_EAST;
		m_gameState[26][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][26] = GS_FOOD | GS_EAST;
		m_gameState[26][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[27][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP
				| PAL_EDGE_RIGHT | GS_WEST | GS_SOUTH;
		m_gameState[27][1] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][2] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][3] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][4] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][5] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][6] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][7] = PAL_BEND_TOPLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][8] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST;
		m_gameState[27][9] = GS_NORTH | GS_SOUTH;
		m_gameState[27][10] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH;
		m_gameState[27][11] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][12] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][16] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][17] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][18] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][19] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][20] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][21] = PAL_BEND_TOPLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][22] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][23] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][24] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][25] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][26] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][27] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][28] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][29] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;

	}

	// The original MsPacman maze
	void loadMsPacManMaze3() {
		m_gameState[0][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][1] = GS_NORTH | GS_SOUTH;
		m_gameState[0][2] = PAL_LINE_HORIZ | GS_SOUTH | GS_EAST;
		m_gameState[0][3] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][4] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][5] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][6] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][7] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][8] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][9] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[0][10] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][11] = PAL_BEND_TOPRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][12] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][16] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][17] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM
				| PAL_EDGE_LEFT | GS_NORTH | GS_EAST;
		m_gameState[0][18] = GS_NORTH;
		m_gameState[0][19] = 0;
		m_gameState[0][20] = 0;
		m_gameState[0][21] = GS_SOUTH;
		m_gameState[0][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_EAST;
		m_gameState[0][23] = GS_NORTH | GS_SOUTH;
		m_gameState[0][24] = PAL_LINE_HORIZ | GS_SOUTH | GS_EAST;
		m_gameState[0][25] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_LEFT | GS_NORTH
				| GS_SOUTH | GS_EAST;
		m_gameState[0][26] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][27] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][28] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][29] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[0][30] = PAL_BEND_TOPRIGHT | PAL_EDGE_BOTTOM
				| PAL_EDGE_LEFT | GS_NORTH | GS_EAST;
		m_gameState[1][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][1] = GS_NORTH | GS_SOUTH;
		m_gameState[1][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][4] = GS_POWERUP | GS_NORTH | GS_WEST;
		m_gameState[1][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][10] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][13] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[1][14] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][16] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][17] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[1][18] = GS_NORTH | GS_EAST;
		m_gameState[1][19] = GS_EAST;
		m_gameState[1][20] = GS_EAST;
		m_gameState[1][21] = GS_SOUTH | GS_EAST;
		m_gameState[1][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[1][23] = GS_NORTH | GS_SOUTH;
		m_gameState[1][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[1][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[1][26] = GS_POWERUP | GS_NORTH | GS_WEST;
		m_gameState[1][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[1][29] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[1][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[2][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[2][1] = GS_NORTH | GS_SOUTH;
		m_gameState[2][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[2][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[2][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[2][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][10] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[2][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[2][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][14] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][15] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][16] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][17] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][18] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][19] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][20] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][21] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[2][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][23] = GS_NORTH | GS_SOUTH;
		m_gameState[2][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[2][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[2][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[2][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[2][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[2][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[3][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[3][1] = GS_NORTH | GS_SOUTH;
		m_gameState[3][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][6] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[3][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[3][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[3][10] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][16] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[3][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][20] = GS_FOOD | GS_WEST;
		m_gameState[3][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][23] = GS_FOOD;
		m_gameState[3][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[3][26] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[3][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[3][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[3][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[3][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[4][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[4][1] = GS_NORTH | GS_SOUTH;
		m_gameState[4][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][7] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[4][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[4][10] = GS_FOOD | GS_SOUTH;
		m_gameState[4][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[4][15] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[4][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[4][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[4][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[4][27] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[4][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[4][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[4][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[5][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[5][1] = GS_NORTH | GS_SOUTH;
		m_gameState[5][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][8] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[5][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[5][10] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][11] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][12] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][14] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][15] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[5][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[5][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[5][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[5][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[5][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[5][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[6][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[6][1] = GS_NORTH | GS_EAST;
		m_gameState[6][2] = GS_WEST | GS_EAST;
		m_gameState[6][3] = GS_WEST | GS_EAST;
		m_gameState[6][4] = GS_FOOD | GS_SOUTH;
		m_gameState[6][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[6][8] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][9] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][10] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[6][11] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][13] = GS_FOOD | GS_EAST;
		m_gameState[6][14] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][15] = GS_FOOD | GS_WEST;
		m_gameState[6][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][20] = GS_FOOD | GS_SOUTH;
		m_gameState[6][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[6][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[6][23] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[6][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][26] = GS_FOOD | GS_WEST;
		m_gameState[6][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[6][29] = GS_FOOD | GS_SOUTH;
		m_gameState[6][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[7][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[7][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[7][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][8] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][9] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[7][10] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][14] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][15] = GS_NORTH | GS_SOUTH;
		m_gameState[7][16] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[7][22] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[7][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][24] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[7][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[7][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[7][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[7][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[8][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][6] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][8] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][10] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][14] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][15] = GS_NORTH | GS_SOUTH;
		m_gameState[8][16] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[8][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][18] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[8][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[8][22] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[8][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][24] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[8][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[8][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[8][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[8][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[8][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[9][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[9][1] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[9][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][4] = GS_FOOD;
		m_gameState[9][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][7] = GS_FOOD | GS_EAST;
		m_gameState[9][8] = GS_WEST | GS_EAST;
		m_gameState[9][9] = GS_WEST | GS_EAST;
		m_gameState[9][10] = GS_WEST | GS_EAST;
		m_gameState[9][11] = GS_WEST;
		m_gameState[9][12] = GS_WEST | GS_EAST;
		m_gameState[9][13] = GS_WEST | GS_EAST;
		m_gameState[9][14] = GS_WEST | GS_EAST;
		m_gameState[9][15] = GS_EAST;
		m_gameState[9][16] = GS_WEST | GS_EAST;
		m_gameState[9][17] = GS_WEST | GS_SOUTH;
		m_gameState[9][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[9][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][23] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[9][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[9][26] = GS_FOOD | GS_SOUTH;
		m_gameState[9][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[9][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[9][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[9][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[10][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[10][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[10][10] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][11] = GS_NORTH | GS_SOUTH;
		m_gameState[10][12] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[10][16] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][17] = GS_NORTH | GS_SOUTH;
		m_gameState[10][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[10][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[10][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[10][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[10][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[10][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[10][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[10][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[10][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[10][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[11][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[11][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][11] = GS_NORTH | GS_SOUTH;
		m_gameState[11][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[11][13] = GS_NORTH | GS_WEST;
		m_gameState[11][14] = GS_WEST;
		m_gameState[11][15] = GS_WEST | GS_SOUTH;
		m_gameState[11][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[11][17] = GS_NORTH;
		m_gameState[11][18] = GS_WEST | GS_EAST;
		m_gameState[11][19] = GS_WEST | GS_EAST;
		m_gameState[11][20] = GS_FOOD | GS_EAST;
		m_gameState[11][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[11][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[11][23] = GS_FOOD | GS_SOUTH;
		m_gameState[11][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[11][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[11][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[11][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[11][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[11][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[12][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][4] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[12][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][8] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[12][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][11] = GS_NORTH | GS_SOUTH;
		m_gameState[12][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[12][13] = GS_NORTH;
		m_gameState[12][14] = 0;
		m_gameState[12][15] = GS_SOUTH;
		m_gameState[12][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[12][17] = GS_NORTH | GS_SOUTH;
		m_gameState[12][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[12][19] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[12][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[12][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[12][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[12][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[12][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[12][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[12][26] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[12][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[12][29] = GS_FOOD | GS_SOUTH;
		m_gameState[12][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][3] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[13][4] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][5] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][7] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[13][11] = GS_NORTH | GS_SOUTH;
		m_gameState[13][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[13][13] = GS_NORTH;
		m_gameState[13][14] = 0;
		m_gameState[13][15] = GS_SOUTH;
		m_gameState[13][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][17] = GS_NORTH | GS_SOUTH;
		m_gameState[13][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][19] = GS_NORTH | GS_WEST;
		m_gameState[13][20] = GS_WEST;
		m_gameState[13][21] = GS_WEST | GS_SOUTH;
		m_gameState[13][22] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[13][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[13][25] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[13][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[13][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[13][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[13][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][3] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[14][4] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][5] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][7] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][8] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[14][11] = GS_NORTH | GS_SOUTH;
		m_gameState[14][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[14][13] = GS_NORTH;
		m_gameState[14][14] = 0;
		m_gameState[14][15] = GS_SOUTH;
		m_gameState[14][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][17] = GS_NORTH | GS_SOUTH;
		m_gameState[14][18] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][19] = GS_NORTH | GS_EAST;
		m_gameState[14][20] = GS_EAST;
		m_gameState[14][21] = GS_SOUTH | GS_EAST;
		m_gameState[14][22] = PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[14][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[14][25] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[14][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][27] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[14][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[14][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[14][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[15][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][4] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[15][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][8] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[15][9] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][11] = GS_NORTH | GS_SOUTH;
		m_gameState[15][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[15][13] = GS_NORTH;
		m_gameState[15][14] = 0;
		m_gameState[15][15] = GS_SOUTH;
		m_gameState[15][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[15][17] = GS_NORTH | GS_SOUTH;
		m_gameState[15][18] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[15][19] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[15][20] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[15][21] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH;
		m_gameState[15][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[15][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[15][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[15][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[15][26] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[15][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[15][29] = GS_FOOD | GS_SOUTH;
		m_gameState[15][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][6] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[16][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[16][10] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][11] = GS_NORTH | GS_SOUTH;
		m_gameState[16][12] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[16][13] = GS_NORTH | GS_EAST;
		m_gameState[16][14] = GS_EAST;
		m_gameState[16][15] = GS_SOUTH | GS_EAST;
		m_gameState[16][16] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[16][17] = GS_NORTH;
		m_gameState[16][18] = GS_WEST | GS_EAST;
		m_gameState[16][19] = GS_WEST | GS_EAST;
		m_gameState[16][20] = GS_FOOD | GS_WEST;
		m_gameState[16][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[16][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[16][23] = GS_FOOD | GS_SOUTH;
		m_gameState[16][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[16][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[16][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][27] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[16][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[16][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[16][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[17][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[17][1] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][2] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][9] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[17][10] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][11] = GS_NORTH | GS_SOUTH;
		m_gameState[17][12] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][13] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][14] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][15] = PAL_EDGE_LEFT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[17][16] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][17] = GS_NORTH | GS_SOUTH;
		m_gameState[17][18] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[17][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[17][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][21] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[17][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[17][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[17][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[17][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[17][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[17][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[17][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[18][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[18][1] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[18][2] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][3] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][4] = GS_FOOD;
		m_gameState[18][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][7] = GS_FOOD | GS_WEST;
		m_gameState[18][8] = GS_WEST | GS_EAST;
		m_gameState[18][9] = GS_WEST | GS_EAST;
		m_gameState[18][10] = GS_WEST | GS_EAST;
		m_gameState[18][11] = GS_EAST;
		m_gameState[18][12] = GS_WEST | GS_EAST;
		m_gameState[18][13] = GS_WEST | GS_EAST;
		m_gameState[18][14] = GS_WEST | GS_EAST;
		m_gameState[18][15] = GS_WEST;
		m_gameState[18][16] = GS_WEST | GS_EAST;
		m_gameState[18][17] = GS_SOUTH | GS_EAST;
		m_gameState[18][18] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[18][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][23] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[18][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[18][26] = GS_FOOD | GS_SOUTH;
		m_gameState[18][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[18][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[18][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[18][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[19][0] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][1] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][2] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][5] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][6] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][8] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][9] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][10] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][11] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][12] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][13] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][14] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][15] = GS_NORTH | GS_SOUTH;
		m_gameState[19][16] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[19][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][18] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[19][19] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][22] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[19][23] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][24] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[19][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[19][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[19][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[19][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[19][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[20][0] = PAL_BEND_BOTTOMRIGHT | PAL_EDGE_TOP | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[20][1] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][2] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][3] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[20][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][8] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][9] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[20][10] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][11] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][12] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][13] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][14] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][15] = GS_NORTH | GS_SOUTH;
		m_gameState[20][16] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[20][22] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[20][23] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][24] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[20][25] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[20][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[20][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[20][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[21][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[21][1] = GS_NORTH | GS_WEST;
		m_gameState[21][2] = GS_WEST | GS_EAST;
		m_gameState[21][3] = GS_WEST | GS_EAST;
		m_gameState[21][4] = GS_FOOD | GS_SOUTH;
		m_gameState[21][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[21][8] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][9] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][10] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[21][11] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][12] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][13] = GS_FOOD | GS_WEST;
		m_gameState[21][14] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][15] = GS_FOOD | GS_EAST;
		m_gameState[21][16] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][20] = GS_FOOD | GS_SOUTH;
		m_gameState[21][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[21][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[21][23] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[21][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][26] = GS_FOOD | GS_EAST;
		m_gameState[21][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[21][29] = GS_FOOD | GS_SOUTH;
		m_gameState[21][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[22][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[22][1] = GS_NORTH | GS_SOUTH;
		m_gameState[22][2] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][3] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][7] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][8] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[22][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[22][10] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][11] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][12] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][14] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][15] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][16] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][17] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][18] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][19] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][21] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[22][22] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[22][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[22][25] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][26] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][27] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[22][28] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[22][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[22][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[23][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[23][1] = GS_NORTH | GS_SOUTH;
		m_gameState[23][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][6] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][7] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[23][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[23][10] = GS_FOOD | GS_SOUTH;
		m_gameState[23][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[23][15] = PAL_BEND_BOTTOMRIGHT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[23][16] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][17] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][18] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][19] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][20] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][21] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][22] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[23][23] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][24] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[23][25] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][26] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[23][27] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_WEST | GS_SOUTH
				| GS_EAST;
		m_gameState[23][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[23][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[23][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[24][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[24][1] = GS_NORTH | GS_SOUTH;
		m_gameState[24][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][5] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][6] = PAL_BEND_BOTTOMLEFT | GS_NORTH | GS_WEST
				| GS_SOUTH | GS_EAST;
		m_gameState[24][7] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][8] = PAL_LINE_VERT | GS_NORTH | GS_SOUTH | GS_EAST;
		m_gameState[24][9] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[24][10] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][14] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][15] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][16] = GS_FOOD | GS_NORTH | GS_WEST;
		m_gameState[24][17] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][18] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][19] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][20] = GS_FOOD | GS_EAST;
		m_gameState[24][21] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][22] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][23] = GS_FOOD;
		m_gameState[24][24] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][25] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[24][26] = GS_FOOD | GS_WEST | GS_SOUTH;
		m_gameState[24][27] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[24][28] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[24][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[24][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[25][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[25][1] = GS_NORTH | GS_SOUTH;
		m_gameState[25][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[25][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[25][4] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][5] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][6] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][7] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][8] = PAL_LINE_VERT | GS_NORTH | GS_WEST | GS_SOUTH;
		m_gameState[25][9] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][10] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[25][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[25][13] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][14] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][15] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][16] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][17] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][18] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][19] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][20] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][21] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[25][22] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][23] = GS_NORTH | GS_SOUTH;
		m_gameState[25][24] = PAL_BEND_BOTTOMRIGHT | GS_SOUTH | GS_EAST;
		m_gameState[25][25] = PAL_BEND_TOPRIGHT | GS_NORTH | GS_EAST;
		m_gameState[25][26] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][27] = PAL_BEND_BOTTOMLEFT | GS_WEST | GS_SOUTH;
		m_gameState[25][28] = PAL_BEND_TOPLEFT | GS_NORTH | GS_WEST;
		m_gameState[25][29] = GS_FOOD | GS_NORTH | GS_SOUTH;
		m_gameState[25][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][1] = GS_NORTH | GS_SOUTH;
		m_gameState[26][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][3] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][4] = GS_POWERUP | GS_NORTH | GS_EAST;
		m_gameState[26][5] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][6] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][7] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][8] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][9] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][10] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][11] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][12] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][13] = GS_FOOD | GS_NORTH | GS_EAST;
		m_gameState[26][14] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][15] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][16] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][17] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[26][18] = GS_NORTH | GS_WEST;
		m_gameState[26][19] = GS_WEST;
		m_gameState[26][20] = GS_WEST;
		m_gameState[26][21] = GS_WEST | GS_SOUTH;
		m_gameState[26][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST | GS_EAST;
		m_gameState[26][23] = GS_NORTH | GS_SOUTH;
		m_gameState[26][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH | GS_EAST;
		m_gameState[26][25] = PAL_LINE_HORIZ | GS_NORTH | GS_WEST | GS_EAST;
		m_gameState[26][26] = GS_POWERUP | GS_NORTH | GS_EAST;
		m_gameState[26][27] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][28] = GS_FOOD | GS_WEST | GS_EAST;
		m_gameState[26][29] = GS_FOOD | GS_SOUTH | GS_EAST;
		m_gameState[26][30] = PAL_EDGE_BOTTOM | PAL_LINE_HORIZ | GS_WEST
				| GS_EAST;
		m_gameState[27][0] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][1] = GS_NORTH | GS_SOUTH;
		m_gameState[27][2] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH;
		m_gameState[27][3] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][4] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][5] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][6] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][7] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][8] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][9] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][10] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][11] = PAL_BEND_TOPLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][12] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][13] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][14] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][15] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][16] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][17] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;
		m_gameState[27][18] = GS_NORTH;
		m_gameState[27][19] = 0;
		m_gameState[27][20] = 0;
		m_gameState[27][21] = GS_SOUTH;
		m_gameState[27][22] = PAL_EDGE_TOP | PAL_LINE_HORIZ | GS_WEST;
		m_gameState[27][23] = GS_NORTH | GS_SOUTH;
		m_gameState[27][24] = PAL_LINE_HORIZ | GS_WEST | GS_SOUTH;
		m_gameState[27][25] = PAL_BEND_BOTTOMLEFT | PAL_EDGE_RIGHT | GS_NORTH
				| GS_WEST | GS_SOUTH;
		m_gameState[27][26] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][27] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][28] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][29] = PAL_EDGE_RIGHT | PAL_LINE_VERT | GS_NORTH
				| GS_SOUTH;
		m_gameState[27][30] = PAL_BEND_TOPLEFT | PAL_EDGE_BOTTOM
				| PAL_EDGE_RIGHT | GS_NORTH | GS_WEST;

	}
}
