package site.grzegorzkoperwas.modradio;
import org.bytedeco.javacpp.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import java.io.EOFException;

public class Reader
{
    private AVFormatContext context = new AVFormatContext(null);
    private String path;
    private AVPacket packet = av_packet_alloc();
    private int stream_index = -1;
    public Reader(String path) throws IllegalStateException {
        this.path = path;
        int error = 0;
        error = avformat_open_input(context, this.path, null, null);
        if (error < 0) {
            throw new IllegalStateException(
                    "Failed to open input " + this.path + " -> " + error
            );
        }
        error = avformat_find_stream_info(context, (PointerPointer) null);
        if (error < 0) {
            throw new IllegalStateException(
                    "Failed to get stream info " + this.path + " -> " + error
            );
        }
        for (int i = 0; i < context.nb_streams(); i++) {
            AVStream stream = context.streams(i);
            if (stream.codecpar().codec_type() == AVMEDIA_TYPE_AUDIO) {
                stream_index = i;
                break;
            }
        }
        if (stream_index < 0) {
            throw new IllegalStateException(
                    "Failed to find audio track in " + this.path + " !"
            );
        }
    }
    public AVStream getAudioStream() {
        return this.context.streams(this.stream_index);
    }

    public String toString() {
        return "<Reader for " + this.path + ">";
    }
    /**
     * Is not very accurate, decode first
     */
    public double getDuration() {
        double duration = this.getAudioStream().duration();
        return  duration / AV_TIME_BASE;
    }

    public AVPacket getNextPacket() throws EOFException {
        if (packet.address() == 0) {
            throw new IllegalStateException("packet not allocated!");
        }
        while (true) {
            int error = av_read_frame(context, packet);
            if (error >= 0) {
                if (packet.stream_index() == this.stream_index) {
                    return packet;
                }
            } else {
                throw new EOFException();
            }
        }
    }
}
