package input;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import engine.Application;
import engine.Time;
import math.Vector2;

public class Mouse extends GLFWMouseButtonCallback
{
	private static byte anyButton = 0;
	private static byte anyButtonDown = 0;
	private static byte anyButtonUp = 0;
	private static byte[] buttons = new byte[8];
	private static byte[] buttonsDown = new byte[8];
	private static byte[] buttonsUp = new byte[8];
	
	private static int i;
	private static long window;
	private static DoubleBuffer xBuffer;
	private static DoubleBuffer yBuffer;
	
	private static int scroll = 0;
	private static byte doubleClicked = 0;
	private static float lastUp = 0;
	
	private static int currentCursor = 0;
	private static long[] cursors = new long[4];
	
	public Mouse()
	{
		window = Application.Window();
		xBuffer = BufferUtils.createDoubleBuffer(1);
		yBuffer = BufferUtils.createDoubleBuffer(1);
		
		glfwSetScrollCallback(Application.Window(), new GLFWScrollCallback()
		{
			@Override
			public void invoke(long window, double xoffset, double yoffset)
			{
				scroll = (int)yoffset;
			}
			
		});
		
		cursors[0] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
		cursors[1] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
		cursors[2] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
		cursors[3] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
	}
	
	public static void Reset()
	{
		for(i = 0; i < buttonsDown.length; i++) buttonsDown[i] = 0;
		for(i = 0; i < buttonsUp.length; i++) buttonsUp[i] = 0;
		anyButton = 0;
		anyButtonDown = 0;
		anyButtonUp = 0;
		
		scroll = 0;
		lastUp += Time.UnscaledDelta();
		doubleClicked = 0;
	}
	
	@Override
	public void invoke(long window, int button, int action, int mods)
	{
		anyButton = 1;
		if(action == GLFW_PRESS)
		{
			buttonsDown[button] = 1;
			anyButtonDown = 1;
			buttons[button] = 1;
		}
		if(action == GLFW_RELEASE)
		{
			buttonsUp[button] = 1;
			anyButtonUp = 1;
			buttons[button] = 0;
			
			if(lastUp < 0.3f) doubleClicked = 1;
			lastUp = 0;
		}
	}
	
	public static final boolean MultiClicked() {return doubleClicked == 1;}
	
	public static boolean AnyButton() {return anyButton == 1;}
	public static boolean GetButton(int buttonCode) {return buttons[buttonCode] == 1;}
	
	public static boolean AnyButtonDown() {return anyButtonDown == 1;}
	public static boolean GetButtonDown(int buttonCode) {return buttonsDown[buttonCode] == 1;}
	
	public static boolean AnyButtonUp() {return anyButtonUp == 1;}
	public static boolean GetButtonUp(int buttonCode) {return buttonsUp[buttonCode] == 1;}
	
	public static Vector2 Position()
	{
		glfwGetCursorPos(window, xBuffer, yBuffer);
		return new Vector2((float)xBuffer.get(0), (float)yBuffer.get(0));
	}
	
	public static int Scroll() {return scroll;}
	
	public final static long GetCursor() {return currentCursor;}
	public static void SetCursor(int cursor)
	{
		if(currentCursor == cursor) return; 
		currentCursor = cursor;
		glfwSetCursor(window, cursors[cursor]);
	}
}
