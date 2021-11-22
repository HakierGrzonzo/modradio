package site.grzegorzkoperwas.modradio;

import java.io.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        av_register_all();
        System.out.println( "Hello World!" );
        int error = 0;
        AVFormatContext context = new AVFormatContext(null);
        String in_file = args[0];
        System.out.println(in_file);
        error = avformat_open_input(context, in_file, null, null);
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
        av_dump_format(context, 0, in_file, 0);
        // output 
        AVFormatContext out_cntxt = new AVFormatContext(null);
        if (out_cntxt == null) {
            System.out.println("Failed to open output context!");
            throw new IllegalStateException();
        }
        avformat_alloc_output_context2(out_cntxt, null, null, "stream.m3u8");
        if (out_cntxt.address() == 0) {
            System.out.println("Failed to open output");
            throw new IllegalStateException();
        }
        AVOutputFormat output = out_cntxt.oformat();
        AVStream out_stream = avformat_new_stream(out_cntxt, null);
        if (out_stream.address() == 0) {
            System.out.println("Failed to open output stream!");
            throw new IllegalStateException();
        }
        error = avcodec_parameters_copy(out_stream.codecpar(), context.streams(0).codecpar());
        if (error < 0) {
            System.out.println("Failed to copy output stream params!");
            throw new IllegalStateException();
        }
        out_stream.codecpar().codec_tag(0);
        av_dump_format(out_cntxt, 0, "stream.m3u8", 1);
        error = avformat_write_header(out_cntxt, (PointerPointer) null);
        if (error < 0) {
            System.out.println("Failed to write header");
            throw new IllegalStateException();
        }
        AVPacket packet = av_packet_alloc();
        while (true) {
            error = av_read_frame(context, packet);
            if (error < 0) {
                break;
            }
            AVStream input_stream = context.streams(0);
            av_packet_rescale_ts(packet, input_stream.time_base(), out_stream.time_base());
            packet.pos(-1);
            error = av_interleaved_write_frame(out_cntxt, packet);
            if (error < 0) {
                System.out.println("Failed to write frame");
                throw new IllegalStateException();
            }
        }
    }
}
