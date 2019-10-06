package mouseutils;

import java.util.concurrent.atomic.AtomicBoolean;

public interface MousePointerCaller {

	public AtomicBoolean hasFocus = new AtomicBoolean(false);
}
