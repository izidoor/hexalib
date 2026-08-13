package fr.izidor.hexalib.infra.applicationResult;


import java.time.LocalDateTime;

public sealed interface ExecutionResult permits SuccessResult, ErrorResult {

    String commandId();
    String userId();
    LocalDateTime executedOn();
    boolean isSuccess();

}
