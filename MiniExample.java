class Example {
    public static void main(String[] args) {
    }
}

class A {
    int i;
    boolean k;
    A a;

    public int foo(int i, int j) { return i+j; }
    public int bar(){ return 1; }
}

class B extends A {
    int i;
    int z;

    public int foo(int i, int j) { return i+j; }
    public int foobar(boolean k){ return 1; }
}