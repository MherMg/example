package am.ejob.backend.api.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    public static final String VALIDATION_EMAIL =
            "^[A-Z0-9._%+-]+@[A-Z0-9/-]+\\.[A-Z]{2,6}$";

    public static boolean isValidEmail(final String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        Pattern pattern = Pattern.compile(VALIDATION_EMAIL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
