import java.awt.*;
import java.io.Serializable;

// Pacman and Ghosts
@SuppressWarnings("serial")
public class Thing implements Serializable
{
   // Thing's next step is one of these constants
   static final byte STILL = 0;
   static final byte UP = 1; 
   static final byte DOWN = 2;
   static final byte LEFT = 3;
   static final byte RIGHT = 4;
   int  m_deltaMax   = 3;      // m_deltaMax * 2 - 1 Intervals between two cells for smooth animation
                         
   
   // Thing Type
   static final byte PACMAN = 0;
   static final byte GHOST = 1;
   static final byte FRUIT = 2;
   
   boolean  m_bInsideRoom;
   
   int   m_locX;
   int   m_locY;
   int   m_deltaLocX = 0;   // Delta between cells, i.e. x -> x+1
   int   m_deltaLocY = 0;   // Delta between cells, i.e. x -> x+1
   int   m_lastLocX;
   int   m_lastLocY;
   int   m_lastDeltaLocX = 0;   // Delta between cells, i.e. x -> x+1
   int   m_lastDeltaLocY = 0;   // Delta between cells, i.e. x -> x+1
   byte  m_direction;
   int   m_startX;         // Starting X location of Thing when game is reset
   int   m_startY;         // Starting Y location of Thing when game is reset
   int   m_deltaStartX;    // Starting deltaX in case Thing needs to be between cells
   GameModel   m_gameModel;
   Rectangle   m_boundingBox; // Bounding box of Thing in pixels
   boolean     m_bPaused = false;
   boolean     m_bVisible = false;
   boolean 	   m_bMiddle;
    
   Thing (GameModel gameModel, byte type, int startX, int startY, boolean bMiddleX)
   {
	  this.m_bMiddle = bMiddleX;
      m_startX = startX;
      m_startY = startY;
      m_deltaStartX = 0;
      m_locX = -1;
      m_locY = -1;
      m_lastLocX = m_startX; 
      m_lastLocY = m_startY;
      m_direction = STILL;
      m_gameModel = gameModel;
      m_bInsideRoom = false;
      m_boundingBox = new Rectangle ();
            
      if (bMiddleX)
      {
         m_deltaLocX = m_deltaMax-1;
         m_lastDeltaLocX = m_deltaLocX;
         m_deltaStartX = m_deltaLocX;
      }
   }
   
   public void setColor(Color newColor) {}
   public void eatItem (int itemType) {}
   public void draw (GameUI gameUI, Graphics g2) {}
   public void tickThing () {}
   public int  checkCollision (Player player) {return 0;}
   
   // Called to return the Thing back to starting location
   public void returnToStart ()
   {
      m_locX = m_startX;
      m_locY = m_startY;
      m_lastLocX = m_startX;
      m_lastLocY = m_startY;
      m_deltaLocX = m_deltaStartX;
      m_deltaLocY = 0;
      m_lastDeltaLocX = m_deltaStartX;
      m_lastDeltaLocY = 0;
      m_bPaused = false;
      m_direction = STILL;
      m_boundingBox.setBounds (0, 0, 0, 0);
   }
   
   public boolean canMove ()
   {
      return !m_bPaused;
   }
   
   public void setVisible (boolean bVisible)
   {
      m_bVisible = bVisible;
   }

   public void setPaused (boolean bPaused)
   {
      m_bPaused = bPaused;
   }
   
   // This method will take the specified location and direction and determine
   // for the given location if the thing moved in that direction, what the
   // next possible turning location would be.
   boolean getDestination (int direction, int locX, int locY, Point point)
   {
      // If the request direction is blocked by a wall, then just return the current location
      if ((direction == UP && (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_NORTH) != 0) ||
          (direction == LEFT && (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_WEST) != 0) ||
          (direction == DOWN && (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_SOUTH) != 0) ||
          (direction == RIGHT && (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_EAST) != 0))
      {
         point.setLocation (locX, locY);
         return false;
      }
         
      // Start off by advancing one in direction for specified location
      switch (direction)
      {
      case UP:
         locY--;
         break;
      case DOWN:
         locY++;
         break;
      case LEFT:
         locX--;
         break;
      case RIGHT:
         locX++;
         break;
      }
      
      // If we violate the grid boundary,
      // then return false.
      if (locY < 0 ||
          locX < 0 ||
          locY >= m_gameModel.m_gameSizeY ||
          locX >= m_gameModel.m_gameSizeX)
         return false;
      
      // Determine next turning location..
      while (true)
      {
         if (direction == UP || direction == DOWN)
         {
            if ((m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_EAST) == 0 ||
                (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_WEST) == 0 ||
                (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_NORTH) != 0 ||
                (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_SOUTH) != 0)  
            {
               point.setLocation (locX, locY);
               break;
            } else {
               if (direction == UP)
               {
                  // Check for Top Warp
                  if (locY == 0)
                  {
                     point.setLocation (locX, m_gameModel.m_gameSizeY - 1);
                     break;
                  } else {
                     locY--;
                  }
               } else {
                  // Check for Bottom Warp
                  if (locY == m_gameModel.m_gameSizeY - 1)
                  {
                     point.setLocation (locX, 0);
                     break;
                  } else {
                     locY++;
                  }
               }
            }
         } else {
            if ((m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_NORTH) == 0 ||
                (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_SOUTH) == 0 ||
                (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_EAST) != 0 ||
                (m_gameModel.m_gameState[locX][locY] & m_gameModel.GS_WEST) != 0)  
            {
               point.setLocation (locX, locY);
               break;
            } else {
               if (direction == LEFT)
               {
                  // Check for Left Warp
                  if (locX == 0)
                  {
                     point.setLocation (m_gameModel.m_gameSizeX - 1, locY);
                     break;
                  } else {
                     locX--;
                  }
               } else {
                  // Check for Right Warp
                  if (locX == m_gameModel.m_gameSizeX - 1)
                  {
                     point.setLocation (0, locY);
                     break;
                  } else {
                     locX++;
                  }
               }
            }
         }
      }
      return true;
   }


}
