package site.grzegorzkoperwas.modradio;
import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import java.io.EOFException;

public class Encoder 
{
    private AVCodecContext context;
    private AVCodec codec = null;
    private AVPacket packet = av_packet_alloc();
    public Encoder(
            int codec_id, 
            int bitrate, 
            int sample_fmt,
            int sample_rate,
            long channel_layout) {
        this.codec = avcodec_find_encoder(codec_id);
        this.context = avcodec_alloc_context3(this.codec);
        if (this.context.address() == 0) {
            throw new IllegalStateException(
                    "Failed to alloc context"
            );
        }
        if (this.codec.address() == 0) {
            throw new IllegalStateException(
                    "Failed to get codec for " +
                    avcodec_get_name(codec_id).getString()
            );
        }
        context.bit_rate(bitrate);
        if (!checkSampleFormat(sample_fmt)) {
            throw new IllegalStateException(
                    "sample_fmt " +
                    av_get_sample_fmt_name(sample_fmt).getString() +
                    " is not supported in codec " +
                    avcodec_get_name(codec_id).getString()
            );
        }
        context.sample_fmt(sample_fmt);
        AVRational time_base = new AVRational();
        time_base.num(1);
        time_base.den(AV_TIME_BASE);
        context.sample_rate(sample_rate);
        context.time_base(time_base);
        context.channel_layout(channel_layout);
        context.channels(av_get_channel_layout_nb_channels(context.channel_layout()));
        int error = avcodec_open2(context, codec, (PointerPointer) null);
        if (error < 0) {
            throw new IllegalStateException(
                    "Failed to open codec context for " +
                    avcodec_get_name(codec_id).getString() + error
            );
        }

    }

    private boolean checkSampleFormat(int sample_fmt) {
        IntPointer allowed = this.codec.sample_fmts();
        long i = 0;
        while (allowed.get(i) != AV_SAMPLE_FMT_NONE) {
            if (allowed.get(i) == sample_fmt)
                return true;
            i++;
        }
        return false;
    }

    public String toString() {
        return 
            "<Encoder for " +
            avcodec_get_name(context.codec_id()).getString() +
            ">";
    }
    
    public void feed(AVFrame frame) {
        int error = avcodec_send_frame(context, frame);
        if (error < 0) {
            throw new IllegalStateException("Failed to send frame to encoder");
        }
    }

    public AVPacket getPacket() throws EOFException {
        int error = avcodec_receive_packet(context, packet);
        if (error == AVERROR_EAGAIN() || error == AVERROR_EOF) {
            throw new EOFException();
        } else if (error < 0) {
            throw new IllegalStateException("Error encoding audio!");
        }
        return packet;
    }

    public AVCodecContext getContext() {
        return context;
    }
}
