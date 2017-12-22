package org.jocean.idiom;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

public class DisposableWrapperUtil {
    private DisposableWrapperUtil() {
        throw new IllegalStateException("No instances!");
    }
    
    public static <E> Func1<DisposableWrapper<E>, E> unwrap() {
        return new Func1<DisposableWrapper<E>, E>() {
            @Override
            public E call(final DisposableWrapper<E> wrapper) {
                return wrapper.unwrap();
            }
        };
    }
    
    public static <E> DisposableWrapper<E> wrap(final E unwrap, final Action1<E> disposer) {
        final Subscription subscription = Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (null != disposer) {
                    disposer.call(unwrap);
                }
            }
        });
        return new DisposableWrapper<E>() {

            @Override
            public int hashCode() {
                return unwrap().hashCode();
            }

            @Override
            public boolean equals(final Object o) {
                return unwrap().equals(DisposableWrapperUtil.unwrap(o));
            }

            @Override
            public E unwrap() {
                return unwrap;
            }

            @Override
            public void dispose() {
                subscription.unsubscribe();
            }

            @Override
            public boolean isDisposed() {
                return subscription.isUnsubscribed();
            }

            @Override
            public String toString() {
                return "DisposableWrapper[" + unwrap().toString() + "]";
            }
        };
    }

    public static <E> Func1<E, DisposableWrapper<E>> wrap(final Action1<E> disposer,
            final Terminable terminable) {
        return new Func1<E, DisposableWrapper<E>>() {
            @Override
            public DisposableWrapper<E> call(final E unwrap) {
                return disposeOn(terminable, wrap(unwrap, disposer));
            }
        };
    }
    
    public static <E> DisposableWrapper<E> wrap(final E unwrap, final DisposableWrapper<?> org) {
        return new DisposableWrapper<E>() {

            @Override
            public int hashCode() {
                return unwrap().hashCode();
            }

            @Override
            public boolean equals(final Object o) {
                return unwrap().equals(DisposableWrapperUtil.unwrap(o));
            }
            
            @Override
            public E unwrap() {
                return unwrap;
            }

            @Override
            public void dispose() {
                org.dispose();
            }

            @Override
            public boolean isDisposed() {
                return org.isDisposed();
            }
            
            @Override
            public String toString() {
                return "DisposableWrapper[" + unwrap.toString() + "]";
            }};
    }
    
    public static <E> DisposableWrapper<E> disposeOn(final Terminable terminable,
            final DisposableWrapper<E> wrapper) {
        if (null!=terminable) {
            terminable.doOnTerminate(new Action0() {
                @Override
                public void call() {
                    wrapper.dispose();
                }});
        }
        return wrapper;
    }
    
    public static <E> Action1<DisposableWrapper<E>> disposeOn(final Terminable terminable) {
        return new Action1<DisposableWrapper<E>>() {
            @Override
            public void call(final DisposableWrapper<E> wrapper) {
                disposeOn(terminable, wrapper);
            }};
    }
    
    public static Action1<Object> disposeOnForAny(final Terminable terminable) {
        return new Action1<Object>() {
            @Override
            public void call(final Object obj) {
                if (obj instanceof DisposableWrapper) {
                    disposeOn(terminable, (DisposableWrapper<?>)obj);
                }
            }};
    }
    
    public static void dispose(final Object obj) {
        if (obj instanceof DisposableWrapper) {
            ((DisposableWrapper<?>)obj).dispose();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Object unwrap(final Object obj) {
        if (obj instanceof DisposableWrapper) {
            return unwrap(((DisposableWrapper<Object>)obj).unwrap());
        } else {
            return obj;
        }
    }
}
