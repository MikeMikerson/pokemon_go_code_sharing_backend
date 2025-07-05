I am working on a Java Spring Boot application. In my `FriendCodeSubmissionRequest` DTO, the `trainerName` field is optional. However, the current validation, `@Pattern(regexp = "^[a-zA-Z0-9]*$")`, causes an error when a blank string is submitted from the frontend, as a blank string does not match the required pattern.

My analysis shows that the `location` and `description` fields use the `@Size` annotation, which correctly handles blank values, so they do not require any changes.

Please implement the following changes to fix the validation for `trainerName`:

1.  **Create a new custom validation annotation** named `NotBlankOrPattern` in the `com.devs.simplicity.poke_go_friends.validation` package. This annotation should allow blank strings but validate non-blank strings against a given regex pattern.

2.  **Create the corresponding validator class**, `NotBlankOrPatternValidator`, in the same package. The validator's logic should return `true` if the string is blank (null, empty, or whitespace) and only apply the regex validation if the string has text.

3.  **Update the `FriendCodeSubmissionRequest.java` file**. Replace the `@Pattern` annotation on the `trainerName` field with the new `@NotBlankOrPattern` annotation.

4.  **Create a unit test** for the new `NotBlankOrPatternValidator` to ensure it correctly validates null, blank, and patterned strings.

5.  **Update the integration tests** in `FriendCodeControllerTest` to verify that API requests with blank, valid, and invalid `trainerName` values behave as expected.
