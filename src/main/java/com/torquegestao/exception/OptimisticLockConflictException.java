package com.torquegestao.exception;

public class OptimisticLockConflictException extends ConflictException {

    public static final String MESSAGE =
            "Este registro foi alterado por outra operação simultânea. Tente novamente";

    public OptimisticLockConflictException() {
        super(MESSAGE);
    }
}
