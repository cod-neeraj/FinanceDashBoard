package Finance.Finanace.Controller;

import Finance.Finanace.DTO.Request.CreateUserRequest;
import Finance.Finanace.DTO.Request.UpdateUserRequest;
import Finance.Finanace.DTO.Response.ApiResponse;
import Finance.Finanace.DTO.Response.UserResponse;
import Finance.Finanace.Security.UserBasicService;
import Finance.Finanace.Service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @PostMapping("/newUser")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping("/allUsers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully",
                userService.updateUser(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }
}
