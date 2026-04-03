package Finance.Finanace.Controller;

import Finance.Finanace.DTO.Request.CreateRecordRequest;
import Finance.Finanace.DTO.Request.RecordFilterRequest;
import Finance.Finanace.DTO.Request.UpdateRecordRequest;
import Finance.Finanace.DTO.Response.ApiResponse;
import Finance.Finanace.DTO.Response.FinancialRecordResponse;
import Finance.Finanace.Service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getRecords(
            @ModelAttribute RecordFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(recordService.getRecords(filter)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(recordService.getRecordById(id)));
    }


    @PostMapping("/createRecord")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request) {
        FinancialRecordResponse response = recordService.createRecord(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Record updated successfully",
                recordService.updateRecord(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Record deleted successfully", null));
    }
}
