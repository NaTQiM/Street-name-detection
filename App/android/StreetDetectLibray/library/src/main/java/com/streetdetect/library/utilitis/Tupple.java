package com.streetdetect.library.utilitis;

public class Tupple<F, S> {

    public F first;
    public S second;

    public Tupple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("Tupple (%s, %s)", first.toString(), second.toString());
    }
}