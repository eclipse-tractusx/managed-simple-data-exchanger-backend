package com.catenax.dft.usecases.csvHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCsvHandlerUseCase<I, T> implements CsvHandlerUseCase<I> {

    protected CsvHandlerUseCase<T> nextUseCase;

    public AbstractCsvHandlerUseCase(CsvHandlerUseCase<T> nextUseCase) {
        this.nextUseCase = nextUseCase;
    }

    protected abstract T executeUseCase(I input);

    @Override
    public void run(I input) {
        T result = executeUseCase(input);

        if (nextUseCase != null) {
            log.info(String.format("[%s] is running now", this.getClass().getCanonicalName()));
            nextUseCase.run(result);
        }
    }
}
