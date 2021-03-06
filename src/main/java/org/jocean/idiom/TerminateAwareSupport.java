package org.jocean.idiom;

import org.jocean.idiom.rx.Action1_N;
import org.jocean.idiom.rx.RxActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;

public class TerminateAwareSupport<T> {
    private static final Logger LOG =
            LoggerFactory.getLogger(TerminateAwareSupport.class);

    public TerminateAwareSupport(final InterfaceSelector selector) {
        this._doAddTerminate = selector.build(AddTerminate.class,
            ADD_TERMINATE_WHEN_ACTIVE,
            CALL_TERMINATE_NOW);
    }

    public void fireAllTerminates(final T self) {
        this._onTerminates.foreachComponent(_CALL_ONTERMINATE, self);
    }

    public Action1<Action0> onTerminate(final T self) {
        return new Action1<Action0>() {
            @Override
            public void call(final Action0 action) {
                doOnTerminate(self, action);
            }};
    }

    public Action1<Action1<T>> onTerminateOf(final T self) {
        return new Action1<Action1<T>>() {
            @Override
            public void call(final Action1<T> action) {
                doOnTerminate(self, action);
            }};
    }

    public Action0 doOnTerminate(final T self, final Action0 onTerminate) {
        return doOnTerminate(self, RxActions.<T>toAction1(onTerminate));
    }

    @SuppressWarnings("unchecked")
    public Action0 doOnTerminate(final T self, final Action1<T> onTerminate) {
        return this._doAddTerminate.addAddTerminate(this,
                self,
                (Action1<Object>) onTerminate);
    }

    public int onTerminateCount() {
        return this._onTerminates.componentCount();
    }

    protected interface AddTerminate {
        public Action0 addAddTerminate(final TerminateAwareSupport<?> support,
            final Object self,
            final Action1<Object> onTerminate);
    }

    private static final AddTerminate ADD_TERMINATE_WHEN_ACTIVE = new AddTerminate() {
        @Override
        public Action0 addAddTerminate(final TerminateAwareSupport<?> support,
                final Object self,
                final Action1<Object> onTerminate) {
            support._onTerminates.addComponent(onTerminate);
            return new Action0() {
                @Override
                public void call() {
                    support._onTerminates.removeComponent(onTerminate);
                }};
        }};

    private static final AddTerminate CALL_TERMINATE_NOW = new AddTerminate() {
        @Override
        public Action0 addAddTerminate(final TerminateAwareSupport<?> support,
                final Object self,
                final Action1<Object> onTerminate) {
            onTerminate.call(self);
            return Actions.empty();
        }};

    private static final Action1_N<Action1<Object>> _CALL_ONTERMINATE =
        new Action1_N<Action1<Object>>() {
            @Override
            public void call(final Action1<Object> onTerminate, final Object...args) {
                try {
                    onTerminate.call(args[0]);
                } catch (final Exception e) {
                    LOG.warn("exception when ({}) invoke onTerminate({}), detail: {}",
                            args[0], onTerminate, ExceptionUtils.exception2detail(e));
                }
            }};

    private final AddTerminate _doAddTerminate;

    private final COWCompositeSupport<Action1<Object>> _onTerminates =
            new COWCompositeSupport<>();
}
