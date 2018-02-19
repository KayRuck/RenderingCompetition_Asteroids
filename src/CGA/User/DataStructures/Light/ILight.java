package CGA.User.DataStructures.Light;

import CGA.User.DataStructures.Camera.Camera;
import CGA.User.DataStructures.Shader;

public interface ILight
{
    void bind(Shader shader, String name);
}
