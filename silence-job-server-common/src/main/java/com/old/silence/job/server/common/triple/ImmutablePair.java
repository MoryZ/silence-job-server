package com.old.silence.job.server.common.triple;


import com.old.silence.job.common.exception.SilenceJobCommonException;

public final class ImmutablePair<L, R> extends Pair<L, R> {

    
    private static final long serialVersionUID = 4954918890077093841L;

    public final L left;
    public final R right;


    public static <L, R> ImmutablePair<L, R> of(L left, R right) {
        return new ImmutablePair<L, R>(left, right);
    }


    public ImmutablePair(L left, R right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return left;
    }


    @Override
    public R getRight() {
        return right;
    }

    @Override
    public R setValue(R value) {
        throw new SilenceJobCommonException("非法操作");

    }
}
