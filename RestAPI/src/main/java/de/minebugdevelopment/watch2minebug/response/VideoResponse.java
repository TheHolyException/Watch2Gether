package de.minebugdevelopment.watch2minebug.response;

import de.minebugdevelopment.watch2minebug.utils.AWTUtil;
import lombok.Getter;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class VideoResponse {

    @Getter
    private String title;

    @Getter
    private String image;

    public VideoResponse(File file) {
        this.title = file.getName();

        // Loading first frame as Preview
        try {
            Picture pic = FrameGrab.getFrameFromFile(file, 0);
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(pic);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            this.image = Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException | JCodecException ex) {
            ex.printStackTrace();
        }

    }

}
