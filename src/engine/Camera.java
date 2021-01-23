package engine;

import java.util.ArrayList;
import java.util.List;

public class Camera extends LogicBehaviour
{
	public Shader shader;
	public int renderLayer = 0;
	
	private static List<Camera> cameras = new ArrayList<Camera>();
	
	public Camera() {cameras.add(this);}
	
	public static void Clear() {cameras.clear();}
	public static final List<Camera> Cameras() {return cameras;}
}
