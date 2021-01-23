package sound;

import static org.lwjgl.openal.AL10.*;

import java.util.List;
import java.util.ArrayList;

import engine.LogicBehaviour;

public class AudioSource extends LogicBehaviour
{
	public AudioClip clip;
	public float startGain = -30;
	public boolean playOnStart = false;
	
	private final int id;
	
	private static List<AudioSource> sources = new ArrayList<AudioSource>();
	
	public AudioSource()
	{
		id = alGenSources();
		SetGain(startGain);
		sources.add(this);
	}
	
	public void Start()
	{
		SetClip(clip);
		if(playOnStart) Play();
	}
	
	public void Play()
	{
		if(clip == null) return;
		
		SetGain(startGain);
		alSourcePlay(id);
	}
	
	public void SetClip(AudioClip c) {clip = c; alSourcei(id, AL_BUFFER, c.id);}
	
	public void Pause() {alSourcePause(id);}
	
	public void Stop() {alSourceStop(id);}
	
	public void SetGain(float v) {alSourcef(id, AL_GAIN, v);}
	
	public void Destroy()
	{
		Stop();
		alDeleteSources(id);
		sources.remove(this);
	}
	
	public static void CleanUp() {for(int i = 0; i < sources.size(); i++) sources.get(0).Destroy();}
}
