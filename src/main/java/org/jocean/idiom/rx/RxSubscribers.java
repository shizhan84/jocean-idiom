package org.jocean.idiom.rx;

import org.jocean.idiom.COWCompositeSupport;

import rx.Subscriber;
import rx.functions.Action1;
import rx.observers.SerializedSubscriber;

public class RxSubscribers {
    private RxSubscribers() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> Subscriber<? super T> guardUnsubscribed(final Subscriber<? super T> subscriber) {
        if (subscriber instanceof GuardUnsubscribedSubscriber) {
            return subscriber;
        }
        else {
            return new GuardUnsubscribedSubscriber<T>(subscriber);
        }
    }
    
    private static final Action1<Object> NOP_ON_NEXT = new Action1<Object>() {
        @Override
        public void call(Object t) {
        }};
    private static final Action1<Throwable> NOP_ON_ERROR = new Action1<Throwable>() {
        @Override
        public void call(Throwable t) {
        }};

    @SuppressWarnings("unchecked")
    public static <T> Action1<T> ignoreNext() {
        return (Action1<T>)NOP_ON_NEXT;
    }

    public static Action1<Throwable> ignoreError() {
        return NOP_ON_ERROR;
    }
    
    public static <T> Subscriber<T> serialized(final Subscriber<T> subscriber) {
        if (subscriber instanceof SerializedSubscriber) {
            return subscriber;
        } else {
            return new SerializedSubscriber<T>(subscriber);
        }
    }

    public static <T> Subscriber<T> asSubscriber(
            final COWCompositeSupport<Subscriber<T>> composite) {
        final Action1<Subscriber<T>> onCompleted = new Action1<Subscriber<T>>() {
            @Override
            public void call(final Subscriber<T> subscriber) {
                subscriber.onCompleted();
            }};
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                composite.foreachComponent(onCompleted);
            }
   
            @Override
            public void onError(final Throwable e) {
                composite.foreachComponent(new Action1<Subscriber<T>>() {
                    @Override
                    public void call(final Subscriber<T> subscriber) {
                        subscriber.onError(e);
                    }});
            }
   
            @Override
            public void onNext(final T obj) {
                composite.foreachComponent(new Action1<Subscriber<T>>() {
                    @Override
                    public void call(final Subscriber<T> subscriber) {
                        subscriber.onNext(obj);
                    }});
            }};
    }
}
