import java.awt.*;
import java.sql.*;
import java.util.*;
import java.applet.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Math;
import java.net.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class PacMan extends Applet {
	ServerNode serverNode;
	Connection con;
	Statement stmt;
	ResultSet rs;
	byte ghostDirection =-1;
	GameModel m_gameModel;
	TopCanvas m_topCanvas;
	BottomCanvas m_bottomCanvas;
	GameUI m_gameUI;
	Ticker m_ticker; // Used to update the game state and UI
	SoundManager m_soundMgr;
	int m_globalTickCount = 0;

	int m_ticksPerSec; // These two variables control game speed	int m_delay; // Milliseconds between ticks
	
	boolean testing = false;

	// int name = Integer.parseInt(getParameter("name"));
	boolean netMultiplayer; // This is for keeping track if
	// the game is in multiplayer
	boolean controller; // This is is for keeping track if this machine
	// is the controller.
	boolean playerIsGhost; // Used for multiplayer to determine if
	// if the player is a ghost
	boolean multiplayerActive = false;// Is active if the game is playing.

	ServerSocket serverSocket; //this is for accepting connections
	//Socket tcpSocket; //this is for receiving updates from slaves
	MulticastSocket updateSocket; //this is for sending game updates
	String group = "224.0.0.17"; // this is the group for broadcasting

	static int serverlistenPort = 45452; // the port on which local machine is listening
	int updateSendPort = 5555; // the port on which local machine is sending updates
	int updateListenPort = 6666; //the port on which machines listen for updates
	private ClientNode clientNode;
	 int m_numOfClients;
	
	 /**
	 @SuppressWarnings("deprecation")
	public static void main(String[] args) {
			PacMan pacMan = new PacMan();
			pacMan.init();
			pacMan.start();
			JFrame window = new JFrame("Sample Applet and Application");
	        window.setContentPane(pacMan);
	        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        window.pack();              // Arrange the components.
	        //System.out.println(theApplet.getSize());
	        window.setVisible(true);    // Make the window visible.

			//pacMan.setVisible(true);
		}
	 */
	 
	public synchronized void init() {
		/*
			this.appletIP =  new Socket(getDocumentBase().getHost(), 8080)
			.getLocalAddress().getHostAddress();
	*/
		setTicksPerSec(25);
		// Create canvases and layout
		m_gameModel = new GameModel(this);
		m_gameUI = new GameUI(this, m_gameModel, 409, 450);
		m_topCanvas = new TopCanvas(m_gameModel, 200, 200);
		m_bottomCanvas = new BottomCanvas(this, m_gameModel, 200, 250);

		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();

		setLayout(gridBagLayout);

		constraints.gridwidth = 1;
		constraints.gridheight = 3;

		gridBagLayout.setConstraints(m_gameUI, constraints);
		add(m_gameUI);

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 1;
		gridBagLayout.setConstraints(m_topCanvas, constraints);
		add(m_topCanvas);

		gridBagLayout.setConstraints(m_bottomCanvas, constraints);
		add(m_bottomCanvas);

		requestFocus();
		// Add event subscribers
		addKeyListener(new pacManKeyAdapter(this));

		validate();
		
		m_soundMgr = new SoundManager(this, getCodeBase());
		if( !testing) {
			

			m_soundMgr.loadSoundClips();
		}

	}

	// Master ticker that runs various parts of the game	// based on the GameModel's STATE
	public  void tick() {
		// long temp = System.currentTimeMillis ();		m_globalTickCount++;


		// This method sends the current state to other nodes!
		// but only if the game is multipalyer is running AND this is
		// the controller.
		if (multiplayerActive) {
			if (controller) {
				try {
					sendModel(m_gameModel);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				updateSlaveModel();
			}
		}

		switch (m_gameModel.m_state) {
		case GameModel.STATE_HOSTING:
			m_numOfClients = Integer.parseInt(JOptionPane.showInputDialog
					(null, "Enter number of clients : ", "", 1));
			serverNode.connectToClients(m_numOfClients);
			break;
		case GameModel.STATE_CONNECT:
			clientNode = new ClientNode(this);
			clientNode.connectMultiplayerGame();
			break;
		case GameModel.STATE_SET_UP_CONNECTION:
			serverNode= new ServerNode(this);
			serverNode.setUpHosting();
			break;
		case GameModel.STATE_MULTIPLAYER_WAITROOM:
			startMultiplayerScreen();
			break;
		case GameModel.STATE_INTRO:
			setIntroScreen();
			break;
		case GameModel.STATE_PAUSED:
			gamePaused();
			break;
		case GameModel.STATE_NEWGAME:
			startNewGame();
			break;
		case GameModel.STATE_GAMEOVER:
			gameOver();
			break;
		case GameModel.STATE_LEVELCOMPLETE:
			levelComplete();
			break;
		case GameModel.STATE_DEADPACMAN:
			deadPacman();
			break;
		case GameModel.STATE_BEGIN_PLAY:
			tickBeginPlay();
			break;
		case GameModel.STATE_PLAYING:
			tickGamePlay();
			break;
		case GameModel.STATE_DEAD_PLAY:
			tickDeadPlay();
			break;
		default:
			System.out.println("Well, you're screwd. Something broke!");
			break;
		}

		m_gameUI.repaint();
		m_topCanvas.repaint();
	}

	
	//here is where we update the game model!
	
	private void updateSlaveModel(){
		
		byte[] buffer = new byte[8192];
        PacmanDataPacket received;
        
        //try this on for size! 
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
			updateSocket.receive(packet);
			ByteArrayInputStream inBuffer = new ByteArrayInputStream(packet.getData());
	        ObjectInputStream in = new ObjectInputStream(inBuffer);
	        received = (PacmanDataPacket) in.readObject();

	        m_gameModel.m_state = received.state;
	        m_gameModel.m_gameState = received.gameState; 
	        m_gameModel.m_gameSizeX = received.gameSizeX;
	        m_gameModel.m_gameSizeY = received.gameSizeY;
	        m_gameModel.m_stage = received.stage; 
	        m_gameModel.m_pausedState = received.pausedState;
	        m_gameModel.m_nLives = received.nLives;
	        
	        
	        
	        Player temp = (Player) m_gameModel.m_things[0];
			temp.m_degreeRotation = received.degreeRotation;
			temp.m_score = received.score;
			temp.m_mouthDegree = received.mouthDegree;
			m_gameModel.m_things[0] = temp;
			
	        setFruit(received);
	        setGhosts(received);
	        setThings(received);    
	        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void setThings(PacmanDataPacket received) {
		for(int i = 0; i < received.things.length; i++){	
			m_gameModel.m_things[i].m_bPaused = received.things[i].m_bPaused;
			m_gameModel.m_things[i].m_bInsideRoom = received.things[i].m_bInsideRoom;
			m_gameModel.m_things[i].m_bVisible = received.things[i].m_bVisible;
			m_gameModel.m_things[i].m_deltaLocX = received.things[i].m_deltaLocX;
			m_gameModel.m_things[i].m_deltaLocY = received.things[i].m_deltaLocY;
			m_gameModel.m_things[i].m_deltaStartX = received.things[i].m_deltaStartX;
			m_gameModel.m_things[i].m_direction = received.things[i].m_direction;
			m_gameModel.m_things[i].m_lastDeltaLocX = received.things[i].m_lastDeltaLocX;
			m_gameModel.m_things[i].m_lastDeltaLocY = received.things[i].m_lastDeltaLocY;
			m_gameModel.m_things[i].m_lastLocX = received.things[i].m_locX;
			m_gameModel.m_things[i].m_lastLocY = received.things[i].m_locY;
			m_gameModel.m_things[i].m_locX = received.things[i].m_locX;
			m_gameModel.m_things[i].m_locY = received.things[i].m_locY;
			m_gameModel.m_things[i].m_startX = received.things[i].m_startX;
			m_gameModel.m_things[i].m_startY = received.things[i].m_startY;
		}
	}

	private  void setGhosts(PacmanDataPacket received) {
		for(int i = 0; i < received.ghosts.length; i++){
			
			m_gameModel.m_ghosts[i].m_bCanBackTrack = received.ghosts[i].m_bCanBackTrack;
			m_gameModel.m_ghosts[i].m_bCanFollow = received.ghosts[i].m_bCanFollow;
			m_gameModel.m_ghosts[i].m_bCanPredict = received.ghosts[i].m_bCanPredict;
			m_gameModel.m_ghosts[i].m_bCanUseNextBest = received.ghosts[i].m_bCanUseNextBest;
			m_gameModel.m_ghosts[i].m_bEaten = received.ghosts[i].m_bEaten;
			m_gameModel.m_ghosts[i].m_bEnteringDoor = received.ghosts[i].m_bEnteringDoor;
			m_gameModel.m_ghosts[i].m_bInsaneAI = received.ghosts[i].m_bInsaneAI;
			m_gameModel.m_ghosts[i].m_bOtherPolygon = received.ghosts[i].m_bOtherPolygon;
			m_gameModel.m_ghosts[i].m_destinationX = received.ghosts[i].m_destinationX;
			m_gameModel.m_ghosts[i].m_destinationY = received.ghosts[i].m_destinationY;
			m_gameModel.m_ghosts[i].m_eatenPoints = received.ghosts[i].m_eatenPoints;
			m_gameModel.m_ghosts[i].m_ghostDeltaMax = received.ghosts[i].m_ghostDeltaMax;
			m_gameModel.m_ghosts[i].m_ghostMouthX = received.ghosts[i].m_ghostMouthX;
			m_gameModel.m_ghosts[i].m_ghostMouthY = received.ghosts[i].m_ghostMouthY;
			m_gameModel.m_ghosts[i].m_lastDirection = received.ghosts[i].m_lastDirection;
			m_gameModel.m_ghosts[i].m_nExitMilliSec = received.ghosts[i].m_nExitMilliSec;
			m_gameModel.m_ghosts[i].m_nTicks2Exit = received.ghosts[i].m_nTicks2Exit;
			m_gameModel.m_ghosts[i].m_nTicks2Flee = received.ghosts[i].m_nTicks2Flee;
			m_gameModel.m_ghosts[i].m_nTicks2Popup = received.ghosts[i].m_nTicks2Popup;
			
		}
	}

	private  void setFruit(PacmanDataPacket received) {
		
		   m_gameModel.m_fruit.m_destinationX = received.fruit.m_destinationX;
		   m_gameModel.m_fruit.m_destinationY = received.fruit.m_destinationY;
		   m_gameModel.m_fruit.m_bAvailable = received.fruit.m_bAvailable;     
		   m_gameModel.m_fruit.m_nTicks2Show = received.fruit.m_nTicks2Show;
		   m_gameModel.m_fruit.m_nTicks2Hide = received.fruit.m_nTicks2Hide;
		   m_gameModel.m_fruit.m_bounceCount = received.fruit.m_bounceCount;
		   m_gameModel.m_fruit.m_nTicks2Popup = received.fruit.m_nTicks2Popup;
		   m_gameModel.m_fruit.m_eatenPoints = received.fruit.m_eatenPoints;
		
	}

	

	// This sends the game state. Yes the ENTIRE game state!
	private void sendModel(GameModel gameModel) throws IOException {

		//make a packet with the current game model
		PacmanDataPacket toSend = new PacmanDataPacket(gameModel);

		
		//write it out
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outBuffer);
        out.writeObject(toSend);
        out.close();
        
        //sending a datagram packet to the multicast group
        DatagramPacket packet = new DatagramPacket(outBuffer.toByteArray(), outBuffer.toByteArray().length,
                        InetAddress.getByName(group), updateListenPort);
        updateSocket.send(packet);
	}


	private synchronized void deadPacman() {
		

		m_soundMgr.stop();
		if (m_gameModel.m_nLives == 0) {
			m_gameModel.m_state = GameModel.STATE_GAMEOVER;
			m_gameModel.m_nTicks2GameOver = 0;

		} else {
			m_gameModel.restartGame();
			m_gameModel.m_state = GameModel.STATE_BEGIN_PLAY;
			m_bottomCanvas.repaint();

		}
	}

	private void levelComplete() {
		m_soundMgr.stop();
		tickLevelComplete();
	}

	private synchronized void gameOver() {
		if (m_gameModel.m_nTicks2GameOver == 0) {

			if (m_gameModel.m_player.m_score > m_gameModel.m_highScore) {
				m_gameModel.m_highScore = m_gameModel.m_player.m_score;
				m_topCanvas.repaint();
			}
		}

		m_gameModel.m_nTicks2GameOver++;

		// After 3 seconds go to the intro page
		if (m_gameModel.m_nTicks2GameOver == 3000 / m_delay) {
			m_gameModel.m_state = GameModel.STATE_INTRO;
			m_gameModel.m_nTicks2GameOver = 0;
		}

		m_gameUI.m_bDrawGameOver = true;
		m_gameUI.m_bRedrawAll = true;
		m_gameUI.repaint();
		return;

	}

	private synchronized void startNewGame() {

		m_soundMgr.stop();
		m_gameModel.newGame();
		m_gameModel.m_state = GameModel.STATE_BEGIN_PLAY;
		m_gameModel.m_nTicks2BeginPlay = 0;
		m_gameModel.m_bIntroInited = false;
		m_gameUI.m_bShowIntro = false;
		m_gameUI.m_bShowMultiplayer = false;
		m_gameUI.m_bRedrawAll = true;
		m_gameUI.m_bShowHostingGame = false;

	}

	private void gamePaused() {
		m_gameUI.m_bDrawPaused = true;
		m_gameUI.m_bRedrawAll = true;
		m_gameUI.repaint();
		return;
	}

	private void setIntroScreen() {
		tickIntro();
		m_gameUI.m_bShowIntro = true;
		m_gameUI.m_bShowMultiplayer = false;
		netMultiplayer = false;

	}

	private synchronized void startMultiplayerScreen() {

		// This is going to show up the Multiplayer waiting area

		m_soundMgr.stop();
		m_gameModel.m_bIntroInited = false;
		m_gameUI.m_bShowIntro = false;
		m_gameUI.m_bShowMultiplayer = true;
		m_gameUI.m_bRedrawAll = true;

	}

	// Ticked when level has completed
	public synchronized void tickLevelComplete() {
		if (m_gameModel.m_nTicks2LevelComp == 0) {
			m_gameModel.setPausedGame(true);
			m_gameUI.m_bRedrawAll = true;
		}

		m_gameModel.m_nTicks2LevelComp++;

		// One second later, hide things and start flashing the board
		if (m_gameModel.m_nTicks2LevelComp == 600 / m_delay) {
			m_gameModel.setVisibleThings(false);
			m_gameModel.m_player.m_bVisible = true;
			m_gameUI.m_bFlipWallColor = true;
			m_gameUI.m_bRedrawAll = true;

		} else if (m_gameModel.m_nTicks2LevelComp > 600 / m_delay
				&& (m_gameModel.m_nTicks2LevelComp % (200 / m_delay)) == 0) {
			m_gameUI.m_bFlipWallColor = !m_gameUI.m_bFlipWallColor;
			m_gameUI.m_bRedrawAll = true;
		}

		if (m_gameModel.m_nTicks2LevelComp == 1900 / m_delay) {
			// This will advance the level and set the State to STATE_BEGIN_PLAY
			m_gameModel.loadNextLevel();
			m_gameModel.m_state = GameModel.STATE_BEGIN_PLAY;
			m_gameModel.m_nTicks2LevelComp = 0;
			m_gameUI.m_bFlipWallColor = false;
			m_gameUI.m_bRedrawAll = true;
			m_bottomCanvas.repaint();
		}
	}

	// Ticked when Pacman has died
	public synchronized void tickDeadPlay() {
		// Get another statement object initialized
		// as shown.

		if (m_gameModel.m_nTicks2DeadPlay == 0) {
			m_gameModel.setPausedGame(true);
			m_gameModel.m_player.m_rotationDying = 0;
			m_gameModel.m_player.m_mouthDegreeDying = 45;
			m_gameModel.m_player.m_mouthArcDying = 135;
			m_gameUI.m_bRedrawAll = true;
			m_gameModel.m_nOrigTicksPerSecond = m_ticksPerSec;
			setTicksPerSec(35);
			
			m_soundMgr.stop();
		}

		m_gameModel.m_nTicks2DeadPlay++;

		if (m_gameModel.m_nTicks2DeadPlay == 1000 / m_delay) {
			m_gameModel.m_player.m_bDrawDead = true;
			for (int i = 0; i < m_gameModel.m_ghosts.length; i++) {
				m_gameModel.m_ghosts[i].setVisible(false);
			}
			m_gameModel.m_fruit.setVisible(false);
			m_gameUI.m_bRedrawAll = true;
			
			m_soundMgr.playSound(SoundManager.SOUND_PACMANDIES);
		}

		if (m_gameModel.m_nTicks2DeadPlay == (SoundManager.SOUND_PACMANDIES_LENGTH + 1000)
				/ m_delay) {
			m_gameModel.m_state = GameModel.STATE_DEADPACMAN; // STATE_LEVELCOMPLETE
			m_gameModel.m_nTicks2DeadPlay = 0;
			setTicksPerSec(m_gameModel.m_nOrigTicksPerSecond);
			m_gameUI.m_bRedrawAll = true;
		}
	}

	// Ticked when the game is about to begin play
	public synchronized void tickBeginPlay() {
		if (m_gameModel.m_nTicks2BeginPlay == 0) {
			m_gameModel.setVisibleThings(false);
			m_gameModel.setPausedGame(true);
			m_gameUI.m_bDrawReady = true;
			m_gameUI.m_bDrawGameOver = false;
			m_gameUI.m_bRedrawAll = true;
			m_gameUI.m_bFlipWallColor = false;
			m_gameUI.refreshRedrawHash();
			if (m_gameModel.m_bPlayStartClip) {
				m_soundMgr.playSound(SoundManager.SOUND_START);
				m_gameModel.m_bPlayStartClip = false;
			}
			m_bottomCanvas.repaint();
		}

		m_gameModel.m_nTicks2BeginPlay++;

		if (m_gameModel.m_nTicks2BeginPlay == 500 / m_delay) {
			m_gameModel.setVisibleThings(true);
			m_gameModel.m_fruit.setVisible(false);
		}

		
		if ((m_gameModel.m_nTicks2BeginPlay == SoundManager.SOUND_START_LENGTH
				/ m_delay && !m_gameModel.m_bStartClipPlayed)
				||(m_gameModel.m_nTicks2BeginPlay == 1000 / m_delay && m_gameModel.m_bStartClipPlayed)) {
			m_gameModel.m_state = GameModel.STATE_PLAYING;
			m_gameModel.setVisibleThings(true);
			m_gameModel.m_fruit.setVisible(false);
			m_gameModel.setPausedGame(false);
			m_gameUI.m_bDrawReady = false;
			m_gameUI.m_bRedrawAll = true;
			m_gameModel.m_nTicks2BeginPlay = 0;
			m_gameModel.m_bStartClipPlayed = true;
			
			m_soundMgr.playSound(SoundManager.SOUND_SIREN);
		}
	}

	// Ticked when the game is playing normally
	
	public synchronized void tickGamePlay() {
		/*if (ghostDirection !=-1){
			m_gameModel.m_ghostPlayer.m_requestedDirection = ghostDirection;
		}*/
		if (serverNode !=null)
			serverNode.updateGhostPlayers();
		boolean bFleeing = false;
		int nCollisionCode;

		// Check if player has earned free life
		if (m_gameModel.m_player.m_score >= m_gameModel.m_nextFreeUp) {
			
			m_soundMgr.playSound(SoundManager.SOUND_EXTRAPAC);
			m_gameModel.m_nLives += 1;
			m_gameModel.m_nextFreeUp += 10000;
			m_bottomCanvas.repaint();
		}

		// Check for collisions between Things and Pacman
		for (int i = 0; i < m_gameModel.m_things.length; i++) {
			nCollisionCode = m_gameModel.m_things[i]
					.checkCollision(m_gameModel.m_player);

			if (nCollisionCode == 1) // Ghost was eaten
			{
				m_soundMgr.playSound(SoundManager.SOUND_EATGHOST);
				break; // Must be eaten one tick at a time
			} else if (nCollisionCode == 2) // Pacman was caught.
			{
				m_gameModel.m_state = GameModel.STATE_DEAD_PLAY; // STATE_LEVELCOMPLETE;
				m_gameModel.m_player.m_direction = Thing.STILL;
				m_gameModel.m_nTicks2DeadPlay = 0;
				return;
			} else if (nCollisionCode == 3) // Pacman ate a Fruit
			{
				m_soundMgr.playSound(SoundManager.SOUND_EATFRUIT);
				break; // Must be eaten one tick at a time
			}
		}

		// Tick and then Move each Thing (includes Pacman and Ghosts)
		for (int i = 0; i < m_gameModel.m_things.length; i++) {
			m_gameModel.m_things[i].tickThing();
			if (m_gameModel.m_things[i].canMove())
				Move(m_gameModel.m_things[i]);
		}

		// Check to see if there are any fleeing Ghosts left
		// because of a power up pacman ate.
		for (int i = 0; i < m_gameModel.m_ghosts.length; i++) {
			bFleeing |= m_gameModel.m_ghosts[i].m_nTicks2Flee > 0;
		}
		// If no fleeing ghosts, then reset the Power Up eat ghost score back to
		// 200
		// and kill the BlueGhost loop
		if (bFleeing != true) {
			m_gameModel.m_eatGhostPoints = 200;
			
			m_soundMgr.stopSound(SoundManager.SOUND_GHOSTBLUE);
			m_soundMgr.playSound(SoundManager.SOUND_SIREN);
		}

		if (m_gameModel.m_totalFoodCount == m_gameModel.m_currentFoodCount) {
			m_gameModel.m_state = GameModel.STATE_LEVELCOMPLETE;
			m_gameModel.m_nTicks2LevelComp = 0;
		}
		// Tick the sound manager (mainly to check if the Chomping loop needs to
		// be stopped)
		m_soundMgr.tickSound();
	}

	// Ticked when the game is running the intro
	public synchronized void tickIntro() {
		boolean bFleeing = false;
		int nCollisionCode;

		if (!m_gameModel.m_bIntroInited) {
			m_gameModel.initIntro();
			setTicksPerSec(35);
			m_gameModel.m_bIntroInited = true;
			m_gameUI.m_bRedrawAll = true;
		}

		// Check if Ghost has run to the left of the Runway
		for (int i = 0; i < m_gameModel.m_ghosts.length; i++) {
			if (m_gameModel.m_ghosts[i].m_locX == 19)
				m_gameModel.m_ghosts[i].m_bPaused = true;
		}

		if (!m_gameModel.m_ghosts[0].m_bVisible) {
			m_gameModel.m_ghosts[0].m_bVisible = true;
			m_gameModel.m_ghosts[0].m_bPaused = false;
		}

		if (!m_gameModel.m_ghosts[1].m_bVisible
				&& m_gameModel.m_ghosts[0].m_locX == 19) {
			m_gameModel.m_ghosts[1].m_bVisible = true;
			m_gameModel.m_ghosts[1].m_bPaused = false;
		}

		if (!m_gameModel.m_ghosts[2].m_bVisible
				&& m_gameModel.m_ghosts[1].m_locX == 19) {
			m_gameModel.m_ghosts[2].m_bVisible = true;
			m_gameModel.m_ghosts[2].m_bPaused = false;
		}

		if (!m_gameModel.m_ghosts[3].m_bVisible
				&& m_gameModel.m_ghosts[2].m_locX == 19) {
			m_gameModel.m_ghosts[3].m_bVisible = true;
			m_gameModel.m_ghosts[3].m_bPaused = false;
		}

		if (!m_gameModel.m_player.m_bVisible
				&& m_gameModel.m_ghosts[3].m_locX == 19) {
			m_gameModel.m_player.m_bVisible = true;
			m_gameModel.m_player.m_bPaused = false;
		}

		if (m_gameModel.m_player.m_locX == 23)
			m_gameModel.m_player.m_requestedDirection = Thing.LEFT;

		if (m_gameModel.m_player.m_locX == 5)
			m_gameModel.m_player.m_requestedDirection = Thing.RIGHT;

		// Tick and then Move each Thing (includes Pacman and Ghosts)
		for (int i = 0; i < m_gameModel.m_things.length; i++) {
			m_gameModel.m_things[i].tickThing();
			if (m_gameModel.m_things[i].canMove())
				Move(m_gameModel.m_things[i]);
		}
	}

	// This method is called to update the Thing's Location and delta Locations	// based on Thing's m_direction. The ONLY update to Thing's m_direction is	// if Thing hits a wall and m_direction is set to STILL. Otherwise, all	// m_direction changes occur in the Thing's virtual method tickThing ().
	public void Move(Thing thing) {
		if (thing.m_direction == Thing.STILL)
			return;

		boolean bMoved = false;

		thing.m_lastLocX = thing.m_locX;
		thing.m_lastLocY = thing.m_locY;
		thing.m_lastDeltaLocX = thing.m_deltaLocX;
		thing.m_lastDeltaLocY = thing.m_deltaLocY;

		// See if thing can eat any nearby items		thing.eatItem(GameModel.GS_FOOD);
		thing.eatItem(GameModel.GS_POWERUP);

		// Based on the current direction, update thing's location in that
		// direction.		// The thing.m_deltaLocX != 0 is so that if the thing is in a cell with
		// a wall		// and the thing is directed towards the wall, he will still need to
		// move towards the		// wall until the thing is dead center in the cell.
		if (thing.m_direction == Thing.LEFT
				&& ((m_gameModel.m_gameState[thing.m_locX][thing.m_locY] & GameModel.GS_WEST) == 0 || thing.m_deltaLocX != 0)) {
			thing.m_deltaLocX--;
			bMoved = true;
		} else if (thing.m_direction == Thing.RIGHT
				&& ((m_gameModel.m_gameState[thing.m_locX][thing.m_locY] & GameModel.GS_EAST) == 0 || thing.m_deltaLocX != 0)) {
			thing.m_deltaLocX++;
			bMoved = true;
		} else if (thing.m_direction == Thing.UP
				&& ((m_gameModel.m_gameState[thing.m_locX][thing.m_locY] & GameModel.GS_NORTH) == 0 || thing.m_deltaLocY != 0)) {
			thing.m_deltaLocY--;
			bMoved = true;
		} else if (thing.m_direction == Thing.DOWN
				&& ((m_gameModel.m_gameState[thing.m_locX][thing.m_locY] & GameModel.GS_SOUTH) == 0 || thing.m_deltaLocY != 0)) {
			thing.m_deltaLocY++;
			bMoved = true;
		}

		// If the thing has moved past the middle of the two cells, then switch
		// his		// location to the other side.
		if (thing.m_deltaLocX <= -thing.m_deltaMax) // Shift thing to adjacent
		// cell on left
		{
			if (thing.m_locX != 0) {
				thing.m_deltaLocX = thing.m_deltaMax - 1;
				thing.m_locX--;
				bMoved = true;

			} else {
				// Check to see if thing should warp to right side
				if (thing.m_deltaLocX < -thing.m_deltaMax) {
					thing.m_deltaLocX = thing.m_deltaMax - 1;
					thing.m_locX = m_gameModel.m_gameSizeX - 1;
					bMoved = true;
				}
			}
		} else if (thing.m_deltaLocX >= thing.m_deltaMax) // Shift thing to
		// adjacent cell on
		// right		{
			if (thing.m_locX != m_gameModel.m_gameSizeX - 1) {
				thing.m_deltaLocX = 1 - thing.m_deltaMax;
				thing.m_locX++;
				bMoved = true;
			} else {
				// Check to see if thing should warp to left side				if (thing.m_deltaLocX > thing.m_deltaMax) {
					thing.m_deltaLocX = 1 - thing.m_deltaMax;
					thing.m_locX = 0;
					bMoved = true;
				}
			}
		} else if (thing.m_deltaLocY <= -thing.m_deltaMax) // Shift thing to
		// adjacent cell on
		// top
		{
			if (thing.m_locY != 0) {
				thing.m_deltaLocY = thing.m_deltaMax - 1;
				thing.m_locY--;
				bMoved = true;

			} else {
				// Check to see if thing should warp to bottom side
				if (thing.m_deltaLocY < -thing.m_deltaMax) {
					thing.m_deltaLocY = thing.m_deltaMax - 1;
					thing.m_locY = m_gameModel.m_gameSizeY - 1;
					bMoved = true;
				}
			}
		} else if (thing.m_deltaLocY >= thing.m_deltaMax) // Shift thing to
		// adjacent cell on
		// bottom		{
			if (thing.m_locY != m_gameModel.m_gameSizeY - 1) {
				thing.m_deltaLocY = 1 - thing.m_deltaMax;
				thing.m_locY++;
				bMoved = true;
			} else {
				// Check to see if thing should warp to top side				if (thing.m_deltaLocY > thing.m_deltaMax) {
					thing.m_deltaLocY = 1 - thing.m_deltaMax;
					thing.m_locY = 0;
					bMoved = true;
				}
			}
		}

		if (!bMoved)
			thing.m_direction = Thing.STILL;
	}

	public void start() {
		if (m_ticker == null) {
			m_ticker = new Ticker(this);
			m_ticker.start();
		}
	}

	public void stop() {
		m_ticker = null;
		
		m_soundMgr.stop();
	}

	void setTicksPerSec(int ticks) {
		m_ticksPerSec = ticks;
		m_delay = 1000 / m_ticksPerSec;
	}

	void toggleGhostAI() {
		for (int i = 0; i < m_gameModel.m_ghosts.length; i++) {
			m_gameModel.m_ghosts[i].m_bInsaneAI = !m_gameModel.m_ghosts[i].m_bInsaneAI;
		}
	}
	/*
	public URL getCodeBase()
	{
		if( !testing )
		{
			return this.getCodeBase();
		}
		else
		{
			URL url = null;
			try {
				url = new URL("http://code.google.com/p/ubccs410pacman/source/browse/#svn/trunk/PacMan");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return url;
		}
	}
	*/



	/*
	 * Can't run Pacman as an application since it use sound-related methods.
	 * public static void main (String args[]) { // Create new window MainFrame
	 * frame = new MainFrame ("PacMan");
	 * 
	 * // Create PacMan instance PacMan pacMan = new PacMan ();
	 * 
	 * // Initialize instance pacMan.init ();
	 * 
	 * frame.add ("Center", pacMan); frame.pack (); frame.show ();
	 * 
	 * pacMan.start (); }
	 */
}

/*
 * class MainFrame extends Frame { MainFrame (String title) { super (title); }
 * 
 * public boolean handleEvent (Event e) { if (e.id ==Event.WINDOW_DESTROY) {
 * System.exit (0); } return super.handleEvent (e); } }
 */
/*
private  void connectMultiplayerGame() {
	netMultiplayer = true;
	controller = false;
	playerIsGhost = true;
	
	try {
		//initialize the socket over which slave receive updates
		updateSocket = new MulticastSocket(updateListenPort);
		updateSocket.joinGroup(InetAddress.getByName(group));
		//initialize the socket over which slave send updates
		//tcpSocket = new Socket(InetAddress.getLocalHost(), listenPort);
		
	} catch (IOException e) {
		e.printStackTrace();
	}
	//once connections are opened, we start the game!
	m_gameModel.m_state = GameModel.STATE_NEWGAME;
	ClientWorker cw;
	cw = new ClientWorker(m_gameModel);
    Thread t2 = new Thread(cw);
    t2.start();	
}

private  void acceptConnection() {
	//tcpSocket = serverSocket.accept();
	ServerWorker sw;
	  try{
		  //serverSocket.accept();
	    sw = new ServerWorker(serverSocket.accept(), this);
	    Thread t = new Thread(sw);
	    //t.setPriority(Thread.MIN_PRIORITY);
	    t.start();
	  } catch (IOException e) {
	    System.out.println("Accept failed: 4444");
	    //System.exit(-1);
	  }
	multiplayerActive = true;

	// start new game?
	m_gameModel.m_state = GameModel.STATE_NEWGAME;

}

private void setUpHosting() {
	netMultiplayer = true;
	controller = true;
	playerIsGhost = false;

	
	try {
		//create a new server socket for accepting connections from slaves
		serverSocket = new ServerSocket(listenPort);
		
		//Also initialize the update socket for SENDING
		updateSocket = new MulticastSocket(updateSendPort);

		// Oh. Here we do necessary UI changes
		// Then we accept the connection?
		m_gameUI.m_bShowHostingGame = true;

		m_gameUI.hostingIP = InetAddress.getLocalHost().getHostAddress();
		m_gameUI.portNumber = Integer.toString(serverSocket.getLocalPort());
		m_gameUI.m_bRedrawAll = true;

		m_gameModel.m_state = GameModel.STATE_HOSTING;
	} catch (IOException e) {
		e.printStackTrace();
	}

}*/