package android.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Denotes that the annotated element requires no permissions.
 * <p>
 * This explicit annotation helps distinguish which of three states that an
 * element may exist in:
 * <ul>
 * <li>Annotated with {@link RequiresPermission}, indicating that an element
 * requires (or may require) one or more permissions.
 * <li>Annotated with {@link RequiresNoPermission}, indicating that an element
 * requires no permissions.
 * <li>Neither annotation, indicating that no explicit declaration about
 * permissions has been made for that element.
 * </ul>
 *
 * @hide
 * @see RequiresPermission
 */
@Retention(CLASS)
@Target({ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD, PARAMETER})
public @interface RequiresNoPermission {
}