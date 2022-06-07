package am.ejob.backend.api.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;


public class FileUtils {


    public static boolean invalidSize(MultipartFile[] images, long sizeInMb) {
        ArrayList<MultipartFile> imagesList = new ArrayList<>(Arrays.asList(images));
        long sizeInByte = sizeInMb * 1024 * 1024;
        for (MultipartFile multipartFile : imagesList) {
            if (multipartFile.getSize() > sizeInByte) {
                return true;
            }
        }
        return false;
    }

}
