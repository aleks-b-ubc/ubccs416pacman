import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// Key event handlers
class pacManKeyAdapter extends KeyAdapter {
	PacMan m_pacMan;

	pacManKeyAdapter(PacMan pacMan) {
		super();
		m_pacMan = pacMan;
	}

	public void keyPressed(KeyEvent event) {
		
		
		switch (event.getKeyCode()) {
		
		//Arrow keys are used for Pacman singleplayer AND for network multiplayer
		//if the player is a ghost
		case KeyEvent.VK_LEFT:
			m_pacMan.m_gameModel.m_player.m_requestedDirection = Thing.LEFT;

			break;

		case KeyEvent.VK_RIGHT:
			m_pacMan.m_gameModel.m_player.m_requestedDirection = Thing.RIGHT;
			break;

		case KeyEvent.VK_UP:
			m_pacMan.m_gameModel.m_player.m_requestedDirection = Thing.UP;
			break;

		case KeyEvent.VK_DOWN:
			m_pacMan.m_gameModel.m_player.m_requestedDirection = Thing.DOWN;
			
			break;
		
			//KEYS W,A,S,D are used for same machine multiplayer.
		case KeyEvent.VK_A:
			m_pacMan.m_gameModel.m_ghostPlayer.m_requestedDirection = Thing.LEFT;
			break;

		case KeyEvent.VK_D:
			m_pacMan.m_gameModel.m_ghostPlayer.m_requestedDirection = Thing.RIGHT;
			break;

		case KeyEvent.VK_W:
			m_pacMan.m_gameModel.m_ghostPlayer.m_requestedDirection = Thing.UP;
			break;

		case KeyEvent.VK_S:
			m_pacMan.m_gameModel.m_ghostPlayer.m_requestedDirection = Thing.DOWN;
			break;

			//N is for NEW SINGLE PLAYER GAME
		case KeyEvent.VK_N:
			// make sure we DO NOT carry over multiplayer state accidently
			// as such we set multiplayer as false
			m_pacMan.m_gameModel.localMultiplayer = false;
			m_pacMan.m_gameModel.m_state = GameModel.STATE_NEWGAME;
			m_pacMan.m_gameUI.m_bDrawPaused = false;
			
			m_pacMan.netMultiplayer = false;
			m_pacMan.multiplayerActive = false;
			m_pacMan.playerIsGhost = false;
			m_pacMan.controller = false;
			break;
			
			//C is for CHOOSING color OR Connecting to a game
		case KeyEvent.VK_C:
			if(m_pacMan.m_gameModel.m_state == GameModel.STATE_MULTIPLAYER_WAITROOM){
				m_pacMan.m_gameModel.m_state = GameModel.STATE_CONNECT;
			}
			break;

			//L is for LOCAL MULTIPLAYER
		case KeyEvent.VK_L:
			// set Local multiplayer as true.
			m_pacMan.m_gameModel.localMultiplayer = true;
			m_pacMan.m_gameModel.m_state = GameModel.STATE_NEWGAME;
			m_pacMan.m_gameUI.m_bDrawPaused = false;
			
			m_pacMan.netMultiplayer = false;
			m_pacMan.multiplayerActive = false;
			m_pacMan.playerIsGhost = false;
			m_pacMan.controller = false;
			
			break;

			//P is for PAUSE
		case KeyEvent.VK_P:

			if (m_pacMan.m_gameModel.m_state == GameModel.STATE_GAMEOVER)
				break;

			if (m_pacMan.m_gameModel.m_state == GameModel.STATE_PAUSED) {
				m_pacMan.m_gameModel.m_state = m_pacMan.m_gameModel.m_pausedState;
				m_pacMan.m_gameUI.m_bDrawPaused = false;
				m_pacMan.m_gameUI.m_bRedrawAll = true;

			} else {
				m_pacMan.m_gameModel.m_pausedState = m_pacMan.m_gameModel.m_state;
				m_pacMan.m_gameModel.m_state = GameModel.STATE_PAUSED;
			}
			break;

		// K is for Multiplayer waiting screen
		case KeyEvent.VK_M:
			m_pacMan.m_gameModel.m_state = GameModel.STATE_MULTIPLAYER_WAITROOM;
			m_pacMan.m_gameModel.m_nTicks2AboutShow = 0;
			break;

			//H for Highscores OR if Multiplayer Waiting room, for HOSTING a game
		case KeyEvent.VK_H:
			if(m_pacMan.m_gameModel.m_state == GameModel.STATE_MULTIPLAYER_WAITROOM){
				m_pacMan.m_gameModel.m_state = GameModel.STATE_SET_UP_CONNECTION;
			}
			break;
			
			
		// V is for SOUND
		case KeyEvent.VK_V:
			m_pacMan.m_soundMgr.m_bEnabled = !m_pacMan.m_soundMgr.m_bEnabled;
			if (m_pacMan.m_soundMgr.m_bEnabled == false)
				m_pacMan.m_soundMgr.stop();
			m_pacMan.m_bottomCanvas.repaint();
			break;

		case KeyEvent.VK_I:
			m_pacMan.toggleGhostAI();
			break;
			

		default:
			//System.out.println("Hello World!");
		}
	}

}