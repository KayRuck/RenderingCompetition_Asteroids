package CGA.User;
/**
 * Created by Fabian on 16.09.2017.
 */
import CGA.User.Game.*;


public class main
{
    public static void main(String[] args)
    {
        Game game = new Game(1280, 720, false, true, "Testgame", 3, 3);
        game.run();
    }
}
