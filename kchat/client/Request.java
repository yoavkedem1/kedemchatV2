package kchat.client;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
// ask the server for information in a period of time
public interface Request<T> {
	boolean isFinished();
	Optional<T> get();
	T fetch();
	
	public static <T> Request<T> flat(Supplier<T> supplier) {
		return new BaseRequest<T>(() -> Optional.of(supplier.get()));
	}
	
	public static <T> Request<T> pure(T t) {
		return new BaseRequest<T>(() -> Optional.of(t));
	}
	
	public static <T> Request<T> future(Supplier<Optional<T>> supplier) {
		return new BaseRequest<T>(supplier);
	}

	default <F> Request<F> map(Function<T,F> function){
		return new BaseRequest<F>(() -> get().map(function));
	}
	
	default <F> Request<F> flatMap(Function<T,Request<F>> function){
		return new BaseRequest<F>(() -> get().flatMap(x -> function.apply(x).get()));
	}
	
	default T orElse(T value) {
		return get().orElse(value);
	}
	
	
	
	
	
}


