package am.ejob.backend.api.rest.vo;

import lombok.Data;

@Data
public class AttachmentStreamVO {
    public String contentType;
    public byte[] stream;
}
