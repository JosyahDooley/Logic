package engine;

import java.io.File;
import java.util.UUID;

public abstract class Object implements Cloneable
{
	public File f = null;
	public Long lastModified;
	
	private String name = "";
	private String instanceID = UUID.randomUUID().toString();
	
	public final String Name() {return name;}
	public void Name(String name) {this.name = name;}
	
	public final String instanceID() {return instanceID;}
	public void instanceID(String id) {instanceID = id;}
	
	public void Save() {}
	
	@Override
	public engine.Object clone() throws CloneNotSupportedException
	{
		return (engine.Object) super.clone();
	}
}
