package kchat.client;

import java.util.Optional;
import java.util.function.Supplier;

public class BaseRequest<T> implements Request<T> {
	
	private Supplier<Optional<T>> m_supplier;
	
	private Optional<T> m_lastValue = Optional.empty();
	
	public BaseRequest(Supplier<Optional<T>> supplier) {
		m_supplier = supplier;
	}
	
	
	public T fetch() {
		while (!get().isPresent());
		return m_lastValue.get();
	}
	
	public Optional<T> get() {
		return (m_lastValue.isPresent() ? m_lastValue : (m_lastValue = m_supplier.get()));
	}
	
	public boolean isFinished() {
		return get().isPresent();
		
	}
}
