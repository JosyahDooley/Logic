package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Scene extends engine.Object
{
	private static List<Scene> scenes = new ArrayList<Scene>();
	
	public Scene(String name)
	{
		String[] split = name.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
		Name(split[split.length - 1]);
		
		scenes.add(this);
	}
	
	public static List<Scene> GetScenes() {return scenes;}
}
