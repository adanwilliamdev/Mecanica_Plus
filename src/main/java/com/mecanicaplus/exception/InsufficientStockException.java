package com.mecanicaplus.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(int available) {
        super("Estoque insuficiente. Disponível: " + available);
    }
}
