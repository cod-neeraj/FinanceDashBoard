package Finance.Finanace.Mapper;

import Finance.Finanace.DTO.Response.UserResponse;
import Finance.Finanace.Models.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
