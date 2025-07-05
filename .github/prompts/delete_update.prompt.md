### Task: Remove Friend Code Deactivation Feature

Please remove the public API endpoint and all associated logic for deactivating/deleting friend codes. This feature is being removed from the application.

**Instructions:**

1.  **Controller Layer:**
    *   In FriendCodeController.java, delete the entire `deactivateFriendCode` method, including its `@DeleteMapping` annotation and associated comments/documentation.

2.  **Service Layer:**
    *   In FriendCodeService.java, find and delete the `deactivateFriendCode` method that was called by the controller.
    *   Review the `FriendCodeService` for any other private methods that were used exclusively by `deactivateFriendCode` and remove them if they are no longer needed.

3.  **Repository Layer:**
    *   Examine FriendCodeRepository.java. If there are any custom query methods that were only used by the `deactivateFriendCode` service method, please remove them.

4.  **Testing:**
    *   In FriendCodeControllerTest.java, find and delete all tests related to the deactivation endpoint. This might be a nested class (e.g., `DeleteFriendCodeTests`) or individual test methods.
    *   In FriendCodeServiceTest.java, find and delete all unit tests for the `deactivateFriendCode` service method.

After making these changes, ensure the project compiles successfully and all remaining tests pass.