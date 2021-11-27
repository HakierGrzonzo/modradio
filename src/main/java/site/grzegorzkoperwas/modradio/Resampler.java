package site.grzegorzkoperwas.modradio;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import org.bytedeco.ffmpeg.swresample.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;
import java.io.EOFException;

public class Resampler
{
    private SwrContext context;
    private AVFrame frame = av_frame_alloc();
    private AVCodecContext input_context;
    private AVCodecContext output_context;
    public Resampler(AVCodecContext input, AVCodecContext output) {
        this.input_context = input;
        this.output_context = output;
        context = swr_alloc();
        if (context.address() == 0) {
            throw new IllegalStateException("Failed to alloc swresample context");
        }
        av_opt_set_int(context, "in_channel_layout", input.channel_layout(), 0);
        av_opt_set_int(context, "in_sample_rate", input.sample_rate(), 0);
        av_opt_set_sample_fmt(context, "in_sample_fmt", input.sample_fmt(), 0);
        av_opt_set_int(context, "out_channel_layout", output.channel_layout(), 0);
        av_opt_set_int(context, "out_sample_rate", output.sample_rate(), 0);
        av_opt_set_sample_fmt(context, "out_sample_fmt", output.sample_fmt(), 0);
        frame.channel_layout(output.channel_layout());
        frame.sample_rate(output.sample_rate());
        frame.format(output.sample_fmt());
        int error = swr_init(context);
        if (error < 0) {
            throw new IllegalStateException("Failed to init swresample context");
        }
    }
    public AVFrame resampleFrame(AVFrame input) {
        int error = swr_convert_frame(context, frame, input);
        if (error < 0) {
            throw new IllegalStateException("Failed to resample frame!");
        }
        frame.pts(input.pts());
        frame.pkt_duration(input.pkt_duration());
        frame.pkt_dts(input.pkt_dts());
        frame.pkt_pos(input.pkt_pos());
        return frame;
    }
    public String toString() {
        return "<Resampler from " + 
            input_context.sample_rate() + "hz " +
            "to " +
            output_context.sample_rate() + "hz>";
    }
}
