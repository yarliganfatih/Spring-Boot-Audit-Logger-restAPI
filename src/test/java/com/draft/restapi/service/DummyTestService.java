package com.draft.restapi.service;

public class DummyTestService {
    public void methodWithoutArgs() {
        throw new IllegalArgumentException();
    }

    public void methodWithArgs(String strArg, Integer intArg) {
        throw new IllegalArgumentException();
    }

    public void methodWithArrayArg(String[] args) {
        throw new IllegalArgumentException();
    }

    public void methodWithObjectArrayArg(Object[] args) {
        throw new IllegalArgumentException();
    }

    public void methodWithPrimitiveArrayArg(int[] args) {
        throw new IllegalArgumentException();
    }

    public void methodWithVarArgs(String... args) {
        throw new IllegalArgumentException();
    }

    public void outerMethod() {
        innerMethod();
    }

    private void innerMethod() {
        throw new IllegalArgumentException();
    }
}
