package calculator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Deprecated
@RequiredArgsConstructor
public class DelegateFunction implements Function {
	private @NonNull Function delegate;
	
	@Override
	public Object call(Scope scope, Object... args) {
		return delegate.call(scope, args);
	}
	
	@Override
	public String getName() {
		return delegate.getName();
	}
	
	@Override
	public String getDescription() {
		return delegate.getDescription();
	}
	
	@Override
	public int maxArgCount() {
		return delegate.maxArgCount();
	}
	
	@Override
	public int minArgCount() {
		return delegate.minArgCount();
	}
	
	@Override
	@Deprecated
	public boolean returnsValue() {
		return delegate.returnsValue();
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || delegate.equals(obj);
	}
	
}
