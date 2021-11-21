package site.grzegorzkoperwas.modradio;

import java.io.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import static org.bytedeco.ffmpeg.global.avformat.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        int error = 0;
        AVFormatContext context = new AVFormatContext(null);
        System.out.println(args[0]);
        error = avformat_open_input(context, "/tmp/tests/test.aac", null, null);
        if (error < 0) {
            System.out.println(error);
            System.out.println("Failed to open file!");
            throw new IllegalStateException();
        }
        error = avformat_find_stream_info(context, (PointerPointer) null);
        if (error < 0) {
            System.out.println("Failed to get stream info!");
            throw new IllegalStateException();
        }
        System.out.println(context.nb_streams());
        for (int i = 0; i < context.nb_streams(); i++) {
            System.out.println(context.streams(i).codecpar().codec_type());
        }
    }
}
