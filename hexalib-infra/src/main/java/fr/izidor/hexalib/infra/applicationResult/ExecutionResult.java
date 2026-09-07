package fr.izidor.hexalib.infra.applicationResult;


import java.time.LocalDateTime;

public sealed interface ExecutionResult permits SuccessResult, DomainExceptionResult, ConflictResult, ErrorResult {

    String commandId();
    String userId();
    LocalDateTime executedOn();
    boolean isSuccess();

}
