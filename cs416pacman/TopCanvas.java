import java.awt.*;
import java.util.*;
// Top Right Canvas which is repainted many times because// it contains the Score string.
class TopCanvas extends Canvas{
	Font        m_font;   GameModel   m_gameModel;      Image       m_offImage;
   Graphics    m_offGraphics;   Dimension   m_offDim;
   
   boolean     m_seeScore = false;

	public TopCanvas (GameModel gameModel, int width, int height)   {
      super ();      setSize (width, height);
		m_gameModel = gameModel;      m_font = new Font ("Helvetica", Font.BOLD, 18);
	}

	public void update(Graphics g)   {
		paint(g);
	}

	public void paint(Graphics g)   {      int         y;      int         x;
		Dimension   dim = getSize ();
      
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
            m_offGraphics.setColor (Color.black);
	   m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);

		m_offGraphics.setColor (Color.white);

		m_offGraphics.setFont (m_font);
		FontMetrics fm = m_offGraphics.getFontMetrics ();
      // HIGH SCORE
		y = 20 + fm.getAscent() + fm.getDescent();      x = 0; 
		m_offGraphics.drawString ("HIGH SCORE", x, y);
      y += fm.getAscent () + fm.getDescent ();       x = fm.stringWidth ("HIGH SCORE") - fm.stringWidth (Integer.toString (m_gameModel.m_highScore));      m_offGraphics.drawString (Integer.toString (m_gameModel.m_highScore), x, y);      
      // SCORE      y += 10 + fm.getAscent() + fm.getDescent();       x = fm.stringWidth ("HIGH SCORE") - fm.stringWidth ("SCORE");      m_offGraphics.drawString ("SCORE", x, y);            y += fm.getAscent() + fm.getDescent();       x = fm.stringWidth ("HIGH SCORE") - fm.stringWidth (Integer.toString (m_gameModel.m_player.m_score));      m_offGraphics.drawString (Integer.toString (m_gameModel.m_player.m_score), x, y);

      m_seeScore = g.drawImage(m_offImage, 0, 0, this);
	}
}
