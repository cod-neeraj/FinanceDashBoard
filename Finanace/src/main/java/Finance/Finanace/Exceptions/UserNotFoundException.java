package Finance.Finanace.Exceptions;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) { super(message); }
    public static UserNotFoundException withUsername(String u) { return new UserNotFoundException("User not found: " + u); }
    public static UserNotFoundException withId(Long id) { return new UserNotFoundException("User not found with id: " + id); }
}
