package site.grzegorzkoperwas.modradio;

import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import java.io.EOFException;

class Muxer {
    private AVFormatContext context = new AVFormatContext(null);
    private AVStream stream;
    private String filename;
    private AVDictionary options;
    private AVRational inputTimeBase;
    private long prev_pts = 0;
    public Muxer(String filename, AVDictionary options, AVCodecContext input) {
        this.options = options;
        this.filename = filename;
        this.inputTimeBase = input.time_base();
        avformat_alloc_output_context2(this.context, null, null, this.filename);
        if (this.context.address() == 0) {
            throw new IllegalStateException("Failed to alloc output format");
        }
        stream = avformat_new_stream(this.context, null);
        if (stream.address() == 0) {
            throw new IllegalStateException("Failed to alloc output stream");
        }
        int error = avcodec_parameters_from_context(stream.codecpar(), input);
        if (error < 0) {
            throw new IllegalStateException(
                    "Failed copy codec parameters to output stream!"
            );
        }
        AVRational timeBase = new AVRational();
        timeBase.num(1);
        timeBase.den(100);
        stream.time_base(timeBase);
        error = avformat_write_header(context, this.options);
        if (error < 0) {
            throw new IllegalStateException(
                    "Failed to write header, check your muxer options!"
            );
        }
    }

    public void feed(AVPacket input) {
        av_packet_rescale_ts(input, inputTimeBase, stream.time_base());
        long now_pts = input.pts();
        int error = av_interleaved_write_frame(this.context, input);
        if ((now_pts - prev_pts) / 100000 > 1) {
            System.out.println("Sleep!");
            prev_pts = now_pts;
            try {
                Thread.sleep(2000);
            } catch (Exception e) {}
        }
        if (error < 0) {
            throw new IllegalStateException("Failed to write frame!");
        }
        
    }
    public void close() {
        av_write_trailer(context);
    }
}
