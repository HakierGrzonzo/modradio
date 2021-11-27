package site.grzegorzkoperwas.modradio;
import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import java.io.EOFException;

public class Decoder
{
    private AVCodecContext context = avcodec_alloc_context3(null);
    private AVCodec codec = null;
    private AVStream in_stream = null;
    private AVFrame frame = av_frame_alloc();
    private Reader reader;
    private AVRational timeBase;
    public Decoder(Reader reader) {
        this.reader = reader;
        this.timeBase = new AVRational();
        this.timeBase.num(1);
        this.timeBase.den(AV_TIME_BASE);
        in_stream = this.reader.getAudioStream();
        avcodec_parameters_to_context(context, in_stream.codecpar());
        codec = avcodec_find_decoder(context.codec_id());
        int error = avcodec_open2(context, codec, (PointerPointer) null);
        if (error < 0) {
            throw new IllegalStateException(
                    "Failed to open codec in " + this.toString()
            );
        }
    }
    public String toString() {
        return 
            "<Decoder for " +
            avcodec_get_name(context.codec_id()).getString() +
            ">";
    }

    public AVFrame getNextFrame() throws EOFException {
        if (frame.address() == 0) {
            throw new IllegalStateException("frame not allocated");
        }
        AVPacket packet = this.reader.getNextPacket();
        av_packet_rescale_ts(packet, this.in_stream.time_base(), timeBase);
        avcodec_send_packet(context, packet);
        avcodec_receive_frame(context, frame);
        return frame;
    }

    public AVCodecContext getContext() {
        return context;
    }
}
