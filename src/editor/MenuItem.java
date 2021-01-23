package editor;

import java.util.function.Consumer;

public class MenuItem
{
	public final String name;
	private final Consumer<MenuItem> func;
	
	public MenuItem(String name, Consumer<MenuItem> func)
	{
		this.name = name;
		this.func = func;
	}
	
	public void Accept()
	{
		func.accept(this);
	}
}
