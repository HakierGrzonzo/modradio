package site.grzegorzkoperwas.modradio;

import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import java.io.EOFException;

public class App 
{
    public static void main( String[] args )
    {
        av_register_all();
        String in_file = args[0];
        Reader reader = new Reader(in_file);
        Decoder decoder = new Decoder(reader);
        System.out.println(reader);
        System.out.println(decoder);
        Encoder encoder = new Encoder(
                AV_CODEC_ID_AAC,
                320 * 1024,
                AV_SAMPLE_FMT_FLTP,
                44100,
                AV_CH_LAYOUT_STEREO);
        System.out.println(encoder);
        Resampler resampler = new Resampler(
                decoder.getContext(), 
                encoder.getContext()
        );
        AVDictionary muxerOpts = new AVDictionary(null);
        av_dict_set(muxerOpts, "hls_time", "10", 0);
        av_dict_set(muxerOpts, "hls_playlist_type", "event", 0);
        Muxer muxer = new Muxer("stream.m3u8", muxerOpts, encoder.getContext());
        System.out.println(resampler);
        while (true) {
            try {
                AVFrame frame = decoder.getNextFrame();
                AVFrame resampled = resampler.resampleFrame(frame);
                encoder.feed(resampled);
            } catch (EOFException e) {
                break;
            }
            while (true) {
                try {
                    AVPacket packet = encoder.getPacket();
                    muxer.feed(packet);
                } catch (EOFException e) {
                    break;
                }
            }
        }
        muxer.close();
        /*
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

        // set options
        AVDictionary opt = new AVDictionary(null);
        av_dict_set(opt, "hls_time", "10", 0);

        error = avformat_write_header(out_cntxt, opt);
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
        */
    }
}
