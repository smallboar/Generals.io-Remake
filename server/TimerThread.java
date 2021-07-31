package kingbattle.server;



public class TimerThread extends Thread {
    GameEngine kingBattle;

    public TimerThread(GameEngine mg){
        kingBattle = mg;
    }
    @Override
    public void run(){
        while(true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(kingBattle.tick()){
                break;
            }


        }
    }
}
