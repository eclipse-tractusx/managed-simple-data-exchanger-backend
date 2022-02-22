package com.catenax.dft.usecases;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractUseCase<I, T> implements UseCase<I> {

    protected UseCase<T> nextUseCase;

    public AbstractUseCase(UseCase<T> nextUseCase) {
        this.nextUseCase = nextUseCase;
    }

    protected abstract T executeUseCase(I input);

    @Override
    public void run(I input) {
        T result = executeUseCase(input);

        if (nextUseCase != null) {
            log.info(String.format("[%s] is running now", this.getClass().getName()));
            nextUseCase.run(result);
        }
    }
}
