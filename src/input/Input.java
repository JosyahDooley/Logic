package input;

import org.lwjgl.glfw.GLFWKeyCallback;
import static org.lwjgl.glfw.GLFW.*;

import java.awt.event.KeyEvent;

public class Input extends GLFWKeyCallback
{
	private static byte anyKey = 0;
	private static byte anyKeyDown = 0;
	private static byte anyKeyUp = 0;
	private static byte[] keys = new byte[65536];
	private static byte[] keysDown = new byte[65536];
	private static byte[] keysUp = new byte[65536];
	
	private static int i;
	
	public static void Reset()
	{
		for(i = 0; i < keysDown.length; i++) keysDown[i] = 0;
		for(i = 0; i < keysUp.length; i++) keysUp[i] = 0;
		anyKey = 0;
		anyKeyDown = 0;
		anyKeyUp = 0;
	}

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods)
	{
		//return if lwjgl doesn't recognize the key
		if(key == -1) return;
		
		anyKey = 1;
		if(action == GLFW_PRESS) {keysDown[key] = 1; anyKeyDown = 1; keys[key] = 1;}
		if(action == GLFW_RELEASE) {keysUp[key] = 1; anyKeyUp = 1; keys[key] = 0;}
	}
	
	public static boolean anyKey() {return anyKey == 1;}
	public static boolean GetKey(int keycode) {return keys[keycode] == 1;}
	public static boolean GetKey(char keycode) {return keys[KeyEvent.getExtendedKeyCodeForChar(keycode)] == 1;}
	
	public static boolean anyKeyDown() {return anyKeyDown == 1;}
	public static boolean GetKeyDown(int keycode) {return keysDown[keycode] == 1;}
	public static boolean GetKeyDown(char keycode) {return keysDown[KeyEvent.getExtendedKeyCodeForChar(keycode)] == 1;}
	
	public static boolean anyKeyUp() {return anyKeyUp == 1;}
	public static boolean GetKeyUp(int keycode) {return keysUp[keycode] == 1;}
	public static boolean GetKeyUp(char keycode) {return keysUp[KeyEvent.getExtendedKeyCodeForChar(keycode)] == 1;}
}
