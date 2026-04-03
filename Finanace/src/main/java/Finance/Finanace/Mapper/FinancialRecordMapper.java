package Finance.Finanace.Mapper;

import Finance.Finanace.DTO.Response.FinancialRecordResponse;
import Finance.Finanace.Models.FinancialRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinancialRecordMapper {

    @Mapping(source = "createdBy.username", target = "createdByUsername")
    FinancialRecordResponse toResponse(FinancialRecord record);

    List<FinancialRecordResponse> toResponseList(List<FinancialRecord> records);
}
