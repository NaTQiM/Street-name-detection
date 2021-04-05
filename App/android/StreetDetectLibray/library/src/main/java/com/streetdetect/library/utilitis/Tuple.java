package com.streetdetect.library.utilitis;

public class Tuple<F, S> {

    public F first;
    public S second;

    public Tuple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("Tuple (%s, %s)", first.toString(), second.toString());
    }
}