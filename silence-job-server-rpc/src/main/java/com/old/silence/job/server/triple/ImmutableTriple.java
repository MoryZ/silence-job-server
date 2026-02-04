package com.old.silence.job.server.common.triple;


public final class ImmutableTriple<L, M, R> extends Triple<L, M, R> {

    
    private static final long serialVersionUID = 1L;

    private final L left;
    private final M middle;
    private final R right;

    public static <L, M, R> ImmutableTriple<L, M, R> of(L left, M middle, R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

    public ImmutableTriple(L left, M middle, R right) {
        super();
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return left;
    }

    @Override
    public M getMiddle() {
        return middle;
    }

    @Override
    public R getRight() {
        return right;
    }
}
