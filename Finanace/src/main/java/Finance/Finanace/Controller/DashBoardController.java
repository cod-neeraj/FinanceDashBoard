package Finance.Finanace.Controller;

import Finance.Finanace.DTO.Response.ApiResponse;
import Finance.Finanace.DTO.Response.DashBoardSummaryResponse;
import Finance.Finanace.Service.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashBoardController {


    private final DashBoardService dashBoardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashBoardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashBoardService.getSummary()));
    }
}
