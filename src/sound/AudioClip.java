package sound;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class AudioClip extends engine.Object
{
	final int id;
	private ByteBuffer vorbis;
	private ShortBuffer pcm;
	
	private static List<AudioClip> clips = new ArrayList<AudioClip>();
	private static int i;
	
	public AudioClip(String fileName)
	{
		id = alGenBuffers();
		
		try(STBVorbisInfo info = STBVorbisInfo.malloc())
		{
			ShortBuffer pcm = null;
			try {pcm = readVorbis(fileName, 32 * 1024, info);}
			catch (Exception e) {e.printStackTrace();}
			
			if(pcm == null)
			{
				System.err.println(fileName + " could not be imported!");
				return;
			}
			alBufferData(id, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
			clips.add(this);
		}
	}
	
	public static AudioClip Find(String name)
	{
		for(i = 0; i < clips.size(); i++)
		{
			if(clips.get(i).Name().equals(name)) return clips.get(i);
		}
		return null;
	}
	
	public final int getID() {return id;}
	public void Destroy() {alDeleteBuffers(id);}
	public static final List<AudioClip> GetClips() {return clips;}
	public static void CleanUp() {for(i = 0; i < clips.size(); i++) clips.get(0).Destroy();}
	
	private ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) throws Exception
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
            vorbis = ioResourceToByteBuffer(resource, bufferSize);
            IntBuffer error = stack.mallocInt(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == 0) throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));

            stb_vorbis_get_info(decoder, info);

            int channels = info.channels();

            pcm = MemoryUtil.memAllocShort(stb_vorbis_stream_length_in_samples(decoder));

            pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            stb_vorbis_close(decoder);

            return pcm;
        }
	}
	
	private ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException
	{
		ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path))
        {
        	String[] split = resource.replaceAll(Pattern.quote("\\"), "\\\\").split("\\\\");
			Name(split[split.length - 1]);
			
            try (SeekableByteChannel fc = Files.newByteChannel(path))
            {
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) ;
            }
        }
        else
        {
        	Name(resource.replaceFirst("/", ""));
        	try (InputStream source = AudioClip.class.getResourceAsStream("/Audio" + resource); ReadableByteChannel rbc = Channels.newChannel(source))
            {
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true)
                {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0)
                    {
                        buffer.flip();
                    	buffer = BufferUtils.createByteBuffer(buffer.capacity() * 2).put(buffer);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }
}
