import java.awt.*;

@SuppressWarnings("serial")
class TopCanvas extends Canvas
	Font        m_font;
   Graphics    m_offGraphics;
   
   boolean     m_seeScore = false;

	public TopCanvas (GameModel gameModel, int width, int height)
      super ();
		m_gameModel = gameModel;
	}

	public void update(Graphics g)
		paint(g);
	}

	public void paint(Graphics g)
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
      
	   m_offGraphics.fillRect (0, 0, m_offDim.width, m_offDim.height);

		m_offGraphics.setColor (Color.white);

		m_offGraphics.setFont (m_font);
		FontMetrics fm = m_offGraphics.getFontMetrics ();

		y = 20 + fm.getAscent() + fm.getDescent();
		m_offGraphics.drawString ("HIGH SCORE", x, y);

      // SCORE

      m_seeScore = g.drawImage(m_offImage, 0, 0, this);
	}
}
