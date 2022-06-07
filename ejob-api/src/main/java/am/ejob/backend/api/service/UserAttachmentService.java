package am.ejob.backend.api.service;

import am.ejob.backend.api.util.ImageUtils;
import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.model.user.UserAttachment;
import am.ejob.backend.common.repository.user.UserAttachmentRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class UserAttachmentService {
    private static final Logger log = LoggerFactory.getLogger(UserAttachmentService.class);

    private final String path = "src\\main\\resources\\files";
    private static final String IMAGE_THUMB_PREFIX = "thumb_";

    private final UserAttachmentRepository userAttachmentRepository;
    private final S3FileService s3FileService;

    @Autowired
    public UserAttachmentService(UserAttachmentRepository userImageRepository,

                                 S3FileService s3FileService) {
        this.userAttachmentRepository = userImageRepository;
        this.s3FileService = s3FileService;
    }


    public UserAttachment uploadUserImage(MultipartFile multipartFile, User user) {
        UserAttachment currentAttachment = findUserAttachment(user);
        UserAttachment userAttachment = new UserAttachment();
        userAttachment.setContentType(multipartFile.getContentType());
        userAttachment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userAttachment.setSize(multipartFile.getSize());
        userAttachment.setUser(user);
        userAttachment.setOriginalName(multipartFile.getOriginalFilename());
        userAttachment.setName(System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename());
        userAttachmentRepository.save(userAttachment);

        log.debug("Uploading file [userId:{};fileName:{};contentType:{};attachmentId:{}]...", user.getId(), multipartFile.getOriginalFilename(), multipartFile.getContentType(), userAttachment.getId());
        try {
            File originalFile = new File(userAttachment.getName());
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), originalFile);
            boolean s3Upload = s3FileService.upload(originalFile);
            if (!s3Upload) {
                userAttachmentRepository.delete(userAttachment);
                return null;
            }
            log.debug("Uploading file [userId:{};fileName:{};contentType:{};attachmentId:{}] - content saved", user.getId(), multipartFile.getOriginalFilename(), multipartFile.getContentType(), userAttachment.getId());

            if (currentAttachment != null) {
                deleteUserAttachment(currentAttachment);
            }
            if (ImageUtils.isImage(multipartFile)) {
                log.debug("Uploading file [userId:{};fileName:{};contentType:{};attachmentId:{}] - generating previews...", user.getId(), multipartFile.getOriginalFilename(), multipartFile.getContentType(), userAttachment.getId());
                createPreviews(userAttachment, originalFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
            userAttachmentRepository.delete(userAttachment);
            return null;
        }

        return userAttachment;
    }

    private void createPreviews(UserAttachment userAttachment, File originalFile) throws IOException {
        resizeImage(IMAGE_THUMB_PREFIX + userAttachment.getName(), originalFile);

    }

    private void resizeImage(String previewFileName, File originalFile) throws IOException {

        File resizedImageFile = new File(previewFileName);
        log.debug("Generate preview file [originalFile:{};previewFile:{};{}x{}]", originalFile, resizedImageFile, 43, 43);
        if (!resizedImageFile.createNewFile()) {
            throw new IllegalStateException("Unable to create new thumb file " + resizedImageFile);
        }
//        if (user.getUserType().equals(User.UserType.EMPLOYER)) {
//            ImageUtils.resizeImage(originalFile, resizedImageFile, 43, 43);
//        } else {
        ImageUtils.resizeImage(originalFile, resizedImageFile, 100, 100);
        s3FileService.upload(resizedImageFile);

//        }
    }

    public Optional<UserAttachment> findById(String id) {
        return userAttachmentRepository.findById(id);

    }

    public InputStream getAttachmentFile(String attachmentName) {
        try {
            return s3FileService.download(attachmentName);
        } catch (Exception e) {
            log.error("Error getting file... [attachmentName:{};printException:{}]", attachmentName, e.getMessage());
            return null;
        }
    }

    public byte[] getUserAvatar(User user) {
        byte[] icon = null;
        try {
            UserAttachment attachment = findUserAttachment(user);
            if (attachment != null) {
                InputStream thumb = getThumb(attachment);
                icon = IOUtils.toByteArray(thumb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return icon;
    }

    public InputStream getThumb(UserAttachment userAttachment) {
        InputStream inputStream;
        try {
            inputStream = s3FileService.download(IMAGE_THUMB_PREFIX + userAttachment.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return inputStream;
    }

    public void upload(MultipartFile file, String name, String direction) throws IOException {
        byte[] bytes = file.getBytes();
        Path path = Paths.get(direction + name);
        Files.write(path, bytes);
    }


    public UserAttachment findUserAttachment(User user) {
        return userAttachmentRepository.findByUser(user);
    }

    public void deleteUserAttachment(UserAttachment attachment) {
        s3FileService.delete(attachment.getName());
        s3FileService.delete(IMAGE_THUMB_PREFIX + attachment.getName());
        userAttachmentRepository.delete(attachment);

    }

}