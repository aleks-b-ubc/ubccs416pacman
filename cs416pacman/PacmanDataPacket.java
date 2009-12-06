
import java.io.Serializable;


//creating my own class for encapsulation of necessary data
//mostly because I can't send the game state entirely as 
//some objects inside the game state are not serializable
//that will go between the nodes
@SuppressWarnings("serial")
public class PacmanDataPacket implements Serializable{
	

	int[][] gameState; // Represents maze as integers
	int gameSizeX;
	int gameSizeY;
	int stage; // Same as level of difficulty
	int state;
	int pausedState; // Save FSM state when game is paused
	int nLives;
	
	int degreeRotation;
	int score;
	int mouthDegree;
	byte requestedDirection;
	
	transportThing[] things; // Contains references to Pacman and Ghosts
	transportGhost[] ghosts;
	transportFruit fruit;
	
	
	public PacmanDataPacket(int gameState){
		state = gameState;
	}
	
	public PacmanDataPacket(GameModel model){
		state = model.m_state;
		gameState = model.m_gameState; 
		gameSizeX = model.m_gameSizeX;
		gameSizeY = model.m_gameSizeY;
		stage = model.m_stage; 
		pausedState = model.m_pausedState;
		nLives = model.m_nLives;
		
		Player temp = (Player) model.m_things[0];
		degreeRotation = temp.m_degreeRotation;
		score = temp.m_score;
		mouthDegree = temp.m_mouthDegree;
		
		setGhosts(model);
		setThings(model);
		setFruit(model);
		
	}

	private void setFruit(GameModel model) {
		   fruit = new transportFruit();
		   
		   fruit.m_destinationX = model.m_fruit.m_destinationX;
		   fruit.m_destinationY = model.m_fruit.m_destinationY;
		   fruit.m_bAvailable = model.m_fruit.m_bAvailable;     
		   fruit.m_nTicks2Show = model.m_fruit.m_nTicks2Show;
		   fruit.m_nTicks2Hide = model.m_fruit.m_nTicks2Hide;
		   fruit.m_bounceCount = model.m_fruit.m_bounceCount;
		   fruit.m_nTicks2Popup = model.m_fruit.m_nTicks2Popup;
		   fruit.m_eatenPoints = model.m_fruit.m_eatenPoints;
		   
		
	}

	private void setThings(GameModel model) {
		things = new transportThing[model.m_things.length];
		for(int i = 0; i < model.m_things.length; i++){
			things[i] = new transportThing();
			
			things[i].m_bPaused = model.m_things[i].m_bPaused;
			things[i].m_bInsideRoom = model.m_things[i].m_bInsideRoom;
			things[i].m_bVisible = model.m_things[i].m_bVisible;
			things[i].m_deltaLocX = model.m_things[i].m_deltaLocX;
			things[i].m_deltaLocY = model.m_things[i].m_deltaLocY;
			things[i].m_deltaStartX = model.m_things[i].m_deltaStartX;
			things[i].m_direction = model.m_things[i].m_direction;
			things[i].m_lastDeltaLocX = model.m_things[i].m_lastDeltaLocX;
			things[i].m_lastDeltaLocY = model.m_things[i].m_lastDeltaLocY;
			things[i].m_lastLocX = model.m_things[i].m_locX;
			things[i].m_lastLocY = model.m_things[i].m_locY;
			things[i].m_locX = model.m_things[i].m_locX;
			things[i].m_locY = model.m_things[i].m_locY;
			things[i].m_startX = model.m_things[i].m_startX;
			things[i].m_startY = model.m_things[i].m_startY;	
		}
	}

	private void setGhosts(GameModel model) {
			ghosts = new transportGhost[model.m_ghosts.length];
			for(int i = 0; i < model.m_ghosts.length; i++){
				ghosts[i] = new transportGhost();
				
				ghosts[i].m_bCanBackTrack = model.m_ghosts[i].m_bCanBackTrack;
				ghosts[i].m_bCanFollow = model.m_ghosts[i].m_bCanFollow;
				ghosts[i].m_bCanPredict = model.m_ghosts[i].m_bCanPredict;
				ghosts[i].m_bCanUseNextBest = model.m_ghosts[i].m_bCanUseNextBest;
				ghosts[i].m_bEaten = model.m_ghosts[i].m_bEaten;
				ghosts[i].m_bEnteringDoor = model.m_ghosts[i].m_bEnteringDoor;
				ghosts[i].m_bInsaneAI = model.m_ghosts[i].m_bInsaneAI;
				ghosts[i].m_bOtherPolygon = model.m_ghosts[i].m_bOtherPolygon;
				ghosts[i].m_destinationX = model.m_ghosts[i].m_destinationX;
				ghosts[i].m_destinationY = model.m_ghosts[i].m_destinationY;
				ghosts[i].m_eatenPoints = model.m_ghosts[i].m_eatenPoints;
				ghosts[i].m_ghostDeltaMax = model.m_ghosts[i].m_ghostDeltaMax;
				ghosts[i].m_ghostMouthX = model.m_ghosts[i].m_ghostMouthX;
				ghosts[i].m_ghostMouthY = model.m_ghosts[i].m_ghostMouthY;
				ghosts[i].m_lastDirection = model.m_ghosts[i].m_lastDirection;
				ghosts[i].m_nExitMilliSec = model.m_ghosts[i].m_nExitMilliSec;
				ghosts[i].m_nTicks2Exit = model.m_ghosts[i].m_nTicks2Exit;
				ghosts[i].m_nTicks2Flee = model.m_ghosts[i].m_nTicks2Flee;
				ghosts[i].m_nTicks2Popup = model.m_ghosts[i].m_nTicks2Popup;
				
			}
	}

	public String stateToString(){
		switch(state){
		case GameModel.STATE_HOSTING:
			return "STATE_HOSTING";
		case GameModel.STATE_SET_UP_CONNECTION:
			return "STATE_SET_UP_CONNECTION";
		case GameModel.STATE_CONNECT:
			return "STATE_CONNECT";
	    case GameModel.STATE_MULTIPLAYER_WAITROOM:
	    	  return "STATE_MULTIPLAYER_WAITROOM";
	    case GameModel.STATE_INTRO:
	    	  return "STATE_INTRO";
	    case GameModel.STATE_PAUSED:
	    	  return "STATE_PAUSED";
	    case GameModel.STATE_NEWGAME:
	    	  return "STATE_NEWGAME";
	    case GameModel.STATE_GAMEOVER:
	    	  return "STATE_GAMEOVER";
	    case GameModel.STATE_LEVELCOMPLETE:
	    	  return "STATE_LEVELCOMPLETE";
        case GameModel.STATE_DEADPACMAN:
	    	  return "STATE_DEADPACMAN";
	    case GameModel.STATE_BEGIN_PLAY:
	    	  return "STATE_BEGIN_PLAY";
        case GameModel.STATE_PLAYING:
	    	  return "STATE_PLAYING";
	    case GameModel.STATE_DEAD_PLAY:
	    	  return "STATE_DEAD_PLAY";
		default:
	    	  return new String("Well, you're screwd. Something broke! State: "+state);
	      }
	}
}

@SuppressWarnings("serial")
class transportThing implements Serializable{
		boolean  m_bInsideRoom;
	   int   m_locX;
	   int   m_locY;
	   int   m_deltaLocX;   // Delta between cells, i.e. x -> x+1
	   int   m_deltaLocY;   // Delta between cells, i.e. x -> x+1
	   int   m_lastLocX;
	   int   m_lastLocY;
	   int   m_lastDeltaLocX;   // Delta between cells, i.e. x -> x+1
	   int   m_lastDeltaLocY;   // Delta between cells, i.e. x -> x+1
	   byte  m_direction;
	   int   m_startX;         // Starting X location of Thing when game is reset
	   int   m_startY;         // Starting Y location of Thing when game is reset
	   int   m_deltaStartX;    // Starting deltaX in case Thing needs to be between cells
	   boolean     m_bPaused;
	   boolean     m_bVisible;
	   
	   public transportThing(){
	   }
}

@SuppressWarnings("serial")
class transportGhost extends transportThing{
	   int[]    m_ghostMouthX;    // X points of Ghost's crooked mouth when Pacman powersup
	   int[]    m_ghostMouthY;    // Y points of Ghost's crooked mouth when Pacman powersup
	   boolean  m_bOtherPolygon = false;
	   int      m_lastDirection;
	   int      m_destinationX;
	   int      m_destinationY;
	   int      m_nTicks2Exit;       // Ticks before ghost is allowed to exit.
	   int      m_nExitMilliSec;     // Milliseconds before exiting.
	   int      m_nTicks2Flee = 0;   // How long the Ghost will run from Pacman
	   boolean  m_bEaten = false;    // Set to true when Pacman has eaten this ghost
	   int      m_ghostDeltaMax = 4; // Should never change
	   int      m_eatenPoints;       // Point worth for eaten Ghost
	   int      m_nTicks2Popup;      // Ticks to display eaten points
	   boolean  m_bEnteringDoor = false;
	   
	   boolean  m_bCanFollow         = false;  // Can ghosts follow each other, i.e. Same destination and direction
	   boolean  m_bCanPredict        = true;   // Can ghosts predict pacman's destination
	   boolean  m_bCanBackTrack      = false;  // Can ghost go back the direction they came
	   boolean  m_bCanUseNextBest    = true;   // Can ghost try the next best direction first 25% of the time
	   boolean  m_bInsaneAI          = false;   // No holds barred!
	   
	   public transportGhost(){
	   }
}

@SuppressWarnings("serial")
class transportFruit extends transportThing{
	   int      m_destinationX;
	   int      m_destinationY;
	   boolean  m_bAvailable;     // FALSE if the fruit has been eaten for this round
	   int      m_nTicks2Show;
	   int      m_nTicks2Hide;
	   int      m_bounceCount;
	   int      m_nTicks2Popup;
	   int      m_eatenPoints;
	   
	   public transportFruit(){	   
	   }
}

