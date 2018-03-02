package CGA.User;
/**
 * Created by Fabian on 16.09.2017.
 * Edited by Team A (Kay Ruck, Philipp Schmeier, Merle Struckmann)
 */
import CGA.User.Game.*;


public class main
{
    public static void main(String[] args)
    {
        // 3840, 2160 || 1920, 1080 || 1280, 720 // ggf. Fullscreen true/false setzen!
        Game game = new Game(1920, 1080, false, true, "Team-A's Steroids", 3, 3);
        game.run();
    }
}
