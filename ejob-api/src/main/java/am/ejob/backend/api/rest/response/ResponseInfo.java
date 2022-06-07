package am.ejob.backend.api.rest.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseInfo {

    public HttpStatus code;
    public ResponseMessage status;

    public static ResponseInfo createResponse(HttpStatus code, ResponseMessage status) {
        return new ResponseInfo(code, status);
    }
}
