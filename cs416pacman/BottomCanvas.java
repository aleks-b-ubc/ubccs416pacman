import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

// Bottom right canvas that is repainted infrequently.
// It contains the number of lives, level and instructions
class BottomCanvas extends Canvas
{
	Font        m_font;
   GameModel   m_gameModel;
   PacMan      m_pacMan;
   
   Image       m_offImage;
   Graphics    m_offGraphics;
   Dimension   m_offDim;
   
   Color pacmanColor = Color.yellow; //changes as the pacman character changes color

	public BottomCanvas (PacMan pacMan, GameModel gameModel, int width, int height)
   {
      super ();
      setSize (width, height);
		m_gameModel = gameModel;
      m_pacMan = pacMan;
      m_font = new Font ("Helvetica", Font.BOLD, 14);
	}

	public void update(Graphics g)
   {
		paint(g);
	}

	public void paint(Graphics g)
   {
      Dimension dim = getSize ();
      
      // Create double buffer if it does not exist or is not
      // the right size
      if (m_offImage == null ||
          m_offDim.width != dim.width ||
          m_offDim.height != dim.height)
      {
         m_offDim = dim;
         m_offImage = createImage (m_offDim.width, m_offDim.height);
         m_offGraphics = m_offImage.getGraphics ();
      }
      
      //The rest of the code positions the writing on the canvas.
      double pacManDiameter = m_offDim.height / 15;
      int x = 0;
      int y = 0;
      String stageString = "Level " + Integer.toString (m_gameModel.m_stage);
     
      m_offGraphics.setColor (Color.black);
	   m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);

      m_offGraphics.setFont(m_font);
		FontMetrics fm = m_offGraphics.getFontMetrics();

      y = fm.getAscent() + fm.getDescent();
      x = 0;
      m_offGraphics.setColor (Color.white);
		m_offGraphics.drawString (stageString, x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.setColor (pacmanColor);
		for (int i = 0; i < m_gameModel.m_nLives; i++)
      {
         m_offGraphics.fillArc (x, y, (int)pacManDiameter, (int)pacManDiameter, 45, 200);
         m_offGraphics.fillArc (x, y, (int)pacManDiameter, (int)pacManDiameter, -45, -200);
         x += pacManDiameter * 1.5;  
      }
      
      m_offGraphics.setColor (Color.white);
      y += 2 * (int)pacManDiameter + fm.getAscent() + fm.getDescent();
      x = 0;
      m_offGraphics.drawString ("Use Arrow Keys", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'P\' to Pause", x, y);
     
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'N\' for New Game", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'L\' for Local Multiplayer", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'M\' for Multiplayer", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'C\' for Color Selection", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      if (m_pacMan.m_soundMgr != null && m_pacMan.m_soundMgr.m_bEnabled)
         m_offGraphics.drawString ("\'V\' for No Sound", x, y);
      else
         m_offGraphics.drawString ("\'V\' for Sound", x, y);
      
      y += fm.getAscent() + fm.getDescent();
      m_offGraphics.drawString ("\'H\' for High Scores", x, y);
   
      //y += fm.getAscent() + fm.getDescent();
      //m_offGraphics.drawString ("\'I\'  for Insane AI", x, y);
      
      // buffer to front
		g.drawImage (m_offImage, 0, 0, this);
	}
	
	public void setPacManColor(Color newColor){
		pacmanColor = newColor;
	}
}
