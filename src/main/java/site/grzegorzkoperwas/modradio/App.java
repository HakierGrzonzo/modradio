package site.grzegorzkoperwas.modradio;

import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import java.io.*;
import java.util.*;

public class App 
{
    public static void main( String[] args )
    {
        //av_register_all();
        //String in_file = args[0];
        Scanner stdin = new Scanner(System.in);
        Encoder encoder = new Encoder(
                AV_CODEC_ID_AAC,
                320 * 1024,
                AV_SAMPLE_FMT_FLTP,
                44100,
                AV_CH_LAYOUT_STEREO);
        AVDictionary muxerOpts = new AVDictionary(null);
        av_dict_set(muxerOpts, "hls_time", "10", 0);
        av_dict_set(muxerOpts, "hls_playlist_type", "event", 0);
        Muxer muxer = new Muxer("stream.m3u8", muxerOpts, encoder.getContext());
        System.out.println(encoder);
        long cumulative_offset_pts = 0;
        long cumulative_offset_dts = 0;
        long cumulative_offset_pos = 0;
        while (true) {
            Reader reader = null;
            String filePath = stdin.nextLine();
            File file = new File(filePath);
            try {
                reader = new Reader(filePath);
            } catch (NoSuchElementException e) {
                break;
            } catch (Exception e) {
                System.out.println("Unable to open file: " + filePath);
                file.delete();
                continue;
            }
            System.out.println(reader);
            Decoder decoder = new Decoder(reader);
            System.out.println(decoder);
            Resampler resampler = new Resampler(
                    decoder.getContext(), 
                    encoder.getContext()
            );
            System.out.println(resampler);
            long pts_offset = 0;
            long dts_offset = 0;
            long pos_offset = 0;
            while (true) {
                try {
                    AVFrame frame = decoder.getNextFrame();
                    AVFrame resampled = resampler.resampleFrame(frame);
                    pts_offset = resampled.pts() + resampled.pkt_duration();
                    dts_offset = resampled.pkt_dts() + resampled.pkt_duration();
                    pos_offset = resampled.pkt_pos();
                    //System.out.println(resampled.pkt_dts() + "\t" + resampled.pts() + "\t" + resampled.pkt_pos());
                    resampled.pts(resampled.pts() + cumulative_offset_pts);
                    resampled.pkt_dts(resampled.pkt_dts() + cumulative_offset_dts);
                    resampled.pkt_pos(resampled.pkt_pos() + cumulative_offset_pos);
                    //System.out.println(resampled.pkt_dts() + "\t" + resampled.pts() + "\t" + resampled.pkt_pos());
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
            cumulative_offset_pts += pts_offset;
            cumulative_offset_dts += dts_offset;
            cumulative_offset_pos += pos_offset;
            System.out.println("done with file: " + filePath);
            file.delete();
        }
        muxer.close();
        stdin.close();
    }
}
