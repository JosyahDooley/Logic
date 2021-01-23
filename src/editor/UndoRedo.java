package editor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import input.Input;
import input.KeyCode;
import math.Rect;
import math.Vector2;

class UndoElement
{
	public Object o;
	public Field f;
	
	public Object orginalValue;
	public int i;
	public double d;
	public float fl;
}

public class UndoRedo 
{
	private static List<UndoElement> undo = new ArrayList<UndoElement>();
	private static List<UndoElement> redo = new ArrayList<UndoElement>();
	
	public static void Poll()
	{
		if(Input.GetKey(KeyCode.LeftControl) && Input.GetKeyDown(KeyCode.Z)) Undo();
		else if(Input.GetKey(KeyCode.LeftControl) && Input.GetKeyDown(KeyCode.Y)) Redo(); 
	}
	
	public static void RegisterUndo(Object obj, Field field)
	{
		Class<?> c = null;
		try {c = field.get(obj).getClass();}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		if(c == null) return;
		
		redo.clear();
		
		UndoElement element = new UndoElement();
		element.o = obj;
		element.f = field;
		
		try
		{
			if(c == Integer.class) element.i = field.getInt(obj);
			else if(c == Float.class) element.fl = field.getFloat(obj);
			else if(c == Double.class) element.d = field.getDouble(obj);
			else if(c == String.class) element.orginalValue = new String((String) field.get(obj));
			else if(c == Vector2.class) {element.orginalValue = ((Vector2) field.get(obj)).clone();}
			else if(c == Rect.class) element.orginalValue = ((Rect) field.get(obj)).clone();
			else if(c.isInstance(engine.Object.class)) element.orginalValue = ((engine.Object) field.get(obj)).clone();
			else return;
		}
		catch (IllegalArgumentException | IllegalAccessException | CloneNotSupportedException e) {e.printStackTrace();}
		
		undo.add(element);
	}
	
	private static void RegisterRedo(Object obj, Field field)
	{
		Class<?> c = null;
		try {c = field.get(obj).getClass();}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		if(c == null) return;
		
		UndoElement element = new UndoElement();
		element.o = obj;
		element.f = field;
		
		try
		{
			if(c == Integer.class) element.i = field.getInt(obj);
			else if(c == Float.class) element.fl = field.getFloat(obj);
			else if(c == Double.class) element.d = field.getDouble(obj);
			else if(c == String.class) element.orginalValue = new String((String) field.get(obj));
			else if(c == Vector2.class) {element.orginalValue = ((Vector2) field.get(obj)).clone();}
			else if(c == Rect.class) element.orginalValue = ((Rect) field.get(obj)).clone();
			else if(c.isInstance(engine.Object.class)) element.orginalValue = ((engine.Object) field.get(obj)).clone();
			else return;
		}
		catch (IllegalArgumentException | IllegalAccessException | CloneNotSupportedException e) {e.printStackTrace();}
		
		redo.add(element);
	}
	
	public static void Undo()
	{
		if(undo.size() == 0) return;
		
		UndoElement element = undo.get(undo.size() - 1);
		undo.remove(element);
		
		Class<?> c = null;
		try {c = element.f.get(element.o).getClass();}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		
		try
		{
			RegisterRedo(element.o, element.f);
			if(c == Integer.class) element.f.set(element.o, element.i);
			else if(c == Float.class) element.f.set(element.o, element.fl);
			else if(c == Double.class) element.f.set(element.o, element.d);
			else if(c == String.class) element.f.set(element.o, (String) element.orginalValue);
			else if(c == Vector2.class) element.f.set(element.o, (Vector2) element.orginalValue);
			else if(c == Rect.class) element.f.set(element.o, (Rect) element.orginalValue);
			else if(c.isInstance(engine.Object.class)) element.f.set(element.o, element.f.get(element.o).getClass().cast(element.orginalValue));
		}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
	}
	
	public static void Redo()
	{
		if(redo.size() == 0) return;
		
		UndoElement element = redo.get(redo.size() - 1);
		redo.remove(element);
		
		Class<?> c = null;
		try {c = element.f.get(element.o).getClass();}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
		
		try
		{
			RegisterUndo(element.o, element.f);
			if(c == Integer.class) element.f.set(element.o, element.i);
			else if(c == Float.class) element.f.set(element.o, element.fl);
			else if(c == Double.class) element.f.set(element.o, element.d);
			else if(c == String.class) element.f.set(element.o, (String) element.orginalValue);
			else if(c == Vector2.class) element.f.set(element.o, (Vector2) element.orginalValue);
			else if(c == Rect.class) element.f.set(element.o, (Rect) element.orginalValue);
			else if(c.isInstance(engine.Object.class)) element.f.set(element.o, element.f.get(element.o).getClass().cast(element.orginalValue));
		}
		catch (IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}
	}
}
