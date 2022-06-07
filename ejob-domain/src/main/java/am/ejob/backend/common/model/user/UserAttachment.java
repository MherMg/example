package am.ejob.backend.common.model.user;


import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Document("user_image")
@Getter
@Setter
public class UserAttachment {

    @Id
    private String id;

    @DBRef
    private User user;

    private String contentType;

    private long size;

    private LocalDateTime createdAt;

    private String name;

    private String originalName;

    private boolean cover;

    private boolean removed;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("user", user)
                .add("contentType", contentType)
                .add("size", size)
                .add("createdAt", createdAt)
                .add("name", name)
                .add("originalName", originalName)
                .add("cover", cover)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAttachment that = (UserAttachment) o;
        return size == that.size &&
                cover == that.cover &&
                Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(name, that.name) &&
                Objects.equals(originalName, that.originalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, contentType, size, createdAt, name, cover);
    }
}
