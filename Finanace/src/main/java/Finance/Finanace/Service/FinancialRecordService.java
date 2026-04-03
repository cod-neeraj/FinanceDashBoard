package Finance.Finanace.Service;

import Finance.Finanace.DTO.Request.CreateRecordRequest;
import Finance.Finanace.DTO.Request.RecordFilterRequest;
import Finance.Finanace.DTO.Request.UpdateRecordRequest;
import Finance.Finanace.DTO.Response.FinancialRecordResponse;
import Finance.Finanace.Exceptions.RecordNotFoundException;
import Finance.Finanace.Exceptions.UserNotFoundException;
import Finance.Finanace.Mapper.FinancialRecordMapper;
import Finance.Finanace.Mapper.UserMapper;
import Finance.Finanace.Models.FinancialRecord;
import Finance.Finanace.Models.User;
import Finance.Finanace.Repository.FinanceRecordRepository;
import Finance.Finanace.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinanceRecordRepository recordRepository;

    private final UserRepo userRepository;
    private final UserMapper userMapper;
    private final FinancialRecordMapper financialRecordMapper;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    @CacheEvict(value = "dashboard:summary", allEntries = true)
    public FinancialRecordResponse createRecord(CreateRecordRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> UserNotFoundException.withUsername(currentUsername));

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("Record created: id={}, type={}, amount={}, by={}",
                saved.getId(), saved.getType(), saved.getAmount(), currentUsername);


        return financialRecordMapper.toResponse(saved);
    }


    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        return financialRecordMapper.toResponse(
                recordRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(id))
        );

    }


    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecords(RecordFilterRequest filter) {

        PageRequest pageRequest = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "date", "createdAt")
        );
        Page<FinancialRecordResponse> responses = recordRepository.findRecords(filter.getCategory(),filter.getType(), filter.getFrom(), filter.getTo(), pageRequest);
        return responses;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = "dashboard:summary", allEntries = true)
    public FinancialRecordResponse updateRecord(Long id, UpdateRecordRequest request) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id));

        if (request.getAmount() != null)      record.setAmount(request.getAmount());
        if (request.getType() != null)        record.setType(request.getType());
        if (request.getCategory() != null)    record.setCategory(request.getCategory());
        if (request.getDate() != null)        record.setDate(request.getDate());
        if (request.getDescription() != null) record.setDescription(request.getDescription());

        FinancialRecord updated = recordRepository.save(record);
        log.info("Record updated: id={}", updated.getId());


        return financialRecordMapper.toResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = "dashboard:summary", allEntries = true)
    public void deleteRecord(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new RecordNotFoundException(id);
        }
        recordRepository.deleteById(id);
        log.info("Record deleted: id={}", id);
    }

}
