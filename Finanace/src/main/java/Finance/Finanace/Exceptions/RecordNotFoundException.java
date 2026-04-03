package Finance.Finanace.Exceptions;

public class RecordNotFoundException extends ResourceNotFoundException {
    public RecordNotFoundException(Long id) { super("Financial record not found with id: " + id); }
}
