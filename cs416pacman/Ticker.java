

// Ticker thread that updates the game state and refreshes the UI
// 
class Ticker extends Thread
{

	PacMan      m_pacMan;
   
	public Ticker(PacMan pacMan)
   {
		m_pacMan = pacMan;
   }

	public void run()
   {
      while (Thread.currentThread () == m_pacMan.m_ticker)
      {
         try {
            this.sleep (m_pacMan.m_delay);
            
         } catch (InterruptedException e) {
            break;
         }
         
         m_pacMan.tick ();
         m_pacMan.requestFocus ();
      }
	}
}