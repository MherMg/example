package am.ejob.backend.api.util;

import com.google.common.net.MediaType;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class ImageUtils {

    public static boolean isImage(MultipartFile multipartFile) {
        return isImage(multipartFile.getContentType());
    }

    public static boolean isImage(String contentType) {
        if (contentType != null) {
            //noinspection UnstableApiUsage
            return MediaType.parse(contentType).is(MediaType.ANY_IMAGE_TYPE);
        }
        return false;
    }

    public static void resizeImage(File file, File targetFile, int newWidth, int newHeight) {
        try (
                InputStream inputStream = FileUtils.openInputStream(file);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))
        ) {

            resizeImage(inputStream, out, newWidth, newHeight);

            out.flush();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void resizeImage(InputStream inputStream, OutputStream out, int newWidth, int newHeight) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);

        int width = newWidth;
        int height = newHeight;
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        if (imgWidth * height < imgHeight * width) {
            width = imgWidth * height / imgHeight;
        } else {
            height = imgHeight * width / imgWidth;
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setBackground(Color.WHITE);
        graphics2D.clearRect(0, 0, newWidth, newHeight);
        graphics2D.drawImage(image, (newWidth - width) / 2, (newHeight - height) / 2, width, height, null);
        graphics2D.dispose();
        ImageIO.write(resizedImage, "png", out);
    }

    public static boolean invalidExtension(MultipartFile[] images) {
        ArrayList<MultipartFile> imagesList = new ArrayList<>(Arrays.asList(images));
        for (MultipartFile multipartFile : imagesList) {
            String contentType = multipartFile.getContentType();

            if (contentType == null
                    || !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidExtension(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") || contentType.equals("image/png");
    }


}
